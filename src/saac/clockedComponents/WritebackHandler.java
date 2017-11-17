package saac.clockedComponents;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;

import saac.Settings;
import saac.Settings.BranchPrediciton;
import saac.dataObjects.BranchResult;
import saac.dataObjects.InstructionResult;
import saac.dataObjects.MemoryResult;
import saac.dataObjects.RegisterResult;
import saac.interfaces.BufferedConnection;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;

public class WritebackHandler implements ClockedComponentI, VisibleComponentI {
	FConnection<InstructionResult>.Output inputEU_A; 
	FConnection<InstructionResult>.Output inputEU_B; 
	FConnection<InstructionResult>.Output inputLS;
	FConnection<BranchResult>.Output inputBr;
	RegisterFile registerFile;
	DepChecker depChecker;
	FConnection<RegisterResult>.Input resultOutput;
	BufferedConnection<Integer>.Input dirtyOutput;
	
	//to enforce round robin collection of inputs
	int nextInput = 0;
	
	public final static int BUFF_SIZE = 8;
	InstructionResult[] reorderBuffer = new InstructionResult[BUFF_SIZE];
	
	int bufferIndexStart = 0;
	int bufferIndexEnd = 0;
	int bufferInstructionStart = 0;
	boolean bufferEmpty = true;
	
	public WritebackHandler(RegisterFile rf, DepChecker depChecker,
			FConnection<InstructionResult>.Output inputEU_A, 
			FConnection<InstructionResult>.Output inputEU_B, 
			FConnection<InstructionResult>.Output inputLS,
			FConnection<BranchResult>.Output inputBr,
			FConnection<RegisterResult>.Input resultOutput,
			BufferedConnection<Integer>.Input dirtyOutput) {
		this.inputEU_A = inputEU_A;
		this.inputEU_B = inputEU_B;
		this.inputLS = inputLS;
		this.inputBr = inputBr;
		this.registerFile = rf;
		this.depChecker = depChecker;
		this.resultOutput = resultOutput;
		this.dirtyOutput = dirtyOutput;
	}

	@Override
	public void tick() throws Exception {
		
		FConnection<? extends InstructionResult>.Output input = getInput();
		if(input == null)
			return;
		
		InstructionResult res = input.peak();
				
		if( bufferEmpty || bufferIndexEnd != bufferIndexStart || res.getID() == bufferInstructionStart ) {
			
			int instructionOffset = res.getID() - bufferInstructionStart;
			if(instructionOffset > BUFF_SIZE)
				return;
			
			int bufferIndex = (bufferIndexStart + instructionOffset) % BUFF_SIZE;
			if(instructionOffset >= BUFF_SIZE - bufferIndexStart && bufferIndex >= bufferIndexStart )
				return;

			input.pop();
			
			reorderBuffer[bufferIndex] = res;
			
			if( (res.getID() - bufferInstructionStart + 1)
					> (bufferIndexEnd - bufferIndexStart + BUFF_SIZE) % BUFF_SIZE ) {
				bufferIndexEnd = (bufferIndex + 1) % BUFF_SIZE;
				bufferEmpty = false;
				System.out.println("set: "+bufferIndexEnd);
			}
			
			if(res instanceof BranchResult && Settings.BRANCH_PREDICTION_MODE != BranchPrediciton.Blocking) {
				BranchResult br = (BranchResult) res;
				if(!br.wasCorrect()) {
					reorderBuffer[bufferIndex] = null;
					for(int i = bufferIndex; i<bufferIndexEnd; i++)
						reorderBuffer[i] = null;
				}
				bufferIndexEnd = bufferIndex;
				if(bufferIndexStart == bufferIndexEnd)
					bufferEmpty = true;
			}
			
		}
		
	}

	private FConnection<? extends InstructionResult>.Output getInput() throws Exception {
		FConnection<? extends InstructionResult>.Output input;
		
		switch(nextInput) {
		case 0:
			if(inputLS.ready())
				input = inputLS;
			else if(inputEU_A.ready())
				input = inputEU_A;
			else if(inputEU_B.ready())
				input = inputEU_B;
			else if(inputBr.ready())
				input = inputBr;
			else
				return null;
			break;
		case 1:
			if(inputEU_A.ready())
				input = inputEU_A;
			else if(inputEU_B.ready())
				input = inputEU_B;
			else if(inputBr.ready())
				input = inputBr;
			else if(inputLS.ready())
				input = inputLS;
			else 
				return null;
			break;
		case 2:
			if(inputEU_B.ready())
				input = inputEU_B;
			else if(inputBr.ready())
				input = inputBr;
			else if(inputLS.ready())
				input = inputLS;
			else if(inputEU_A.ready())
				input = inputEU_A;
			else 
				return null;
			break;
		case 3:
			if(inputBr.ready())
				input = inputBr;
			else if(inputEU_B.ready())
				input = inputEU_B;
			else if(inputLS.ready())
				input = inputLS;
			else if(inputEU_A.ready())
				input = inputEU_A;
			else 
				return null;
			break;
		default:
			throw new Exception();
		}
		
		nextInput = (nextInput + 1) % 4;
		return input;
	}

	@Override
	public void tock() throws Exception {
		InstructionResult res = reorderBuffer[bufferIndexStart];
		if(res == null)
			return;
		
		reorderBuffer[bufferIndexStart] = null;
		
		if(res instanceof MemoryResult) {
			MemoryResult mr = (MemoryResult) res;
			depChecker.dirtyMem.remove(mr.getValue());
		} else if(res instanceof RegisterResult) {
			RegisterResult rr = (RegisterResult) res;
			System.out.println(String.format("%d is written back to r%d", rr.getValue(), rr.getTarget()));
			resultOutput.put(rr);
			dirtyOutput.put(rr.getTarget());
		}
		
		bufferIndexStart = (bufferIndexStart + 1) % BUFF_SIZE;
		if(bufferIndexStart == bufferIndexEnd)
			bufferEmpty = true;
		bufferInstructionStart++;
	}

	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Write Back Handler", 4*BOX_SIZE, 50);
			gc.setPaint(Color.BLACK);
			for(int i = 0; i<reorderBuffer.length; i++)
				if(reorderBuffer[i] != null)
					gc.drawString(Integer.toString(reorderBuffer[i].getID()), i*40 + 20, 30);
				else
					gc.drawString("|X|", i*40 + 20, 30);
			
			gc.drawString("start index: " + Integer.toString(bufferIndexStart), 400, 30);
			gc.drawString("end index: " + Integer.toString(bufferIndexEnd), 500, 30);
			gc.drawString("start inst: " + Integer.toString(bufferInstructionStart), 600, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
	
}
