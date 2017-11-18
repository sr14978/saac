package saac.clockedComponents;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import saac.Main;
import saac.Saac;
import saac.Settings;
import saac.Settings.BranchPrediciton;
import saac.dataObjects.BranchResult;
import saac.dataObjects.InstructionResult;
import saac.dataObjects.MemoryResult;
import saac.dataObjects.RegisterResult;
import saac.dataObjects.StopResult;
import saac.interfaces.BufferedConnection;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Output;

public class WritebackHandler implements ClockedComponentI, VisibleComponentI {
	List<FConnection<InstructionResult>.Output> inputs;
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
			List<FConnection<InstructionResult>.Output> inputEUs,
			FConnection<InstructionResult>.Output inputLS,
			FConnection<InstructionResult>.Output inputBr,
			FConnection<RegisterResult>.Input resultOutput,
			BufferedConnection<Integer>.Input dirtyOutput) {
		this.inputs = new ArrayList<>(inputEUs);
		this.inputs.add(inputLS);
		this.inputs.add(inputBr);
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
			}
			
			if(res instanceof BranchResult) {
				Saac.InstructionCounter++;
				if(Settings.BRANCH_PREDICTION_MODE != BranchPrediciton.Blocking) {
			
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
		
	}

	private FConnection<? extends InstructionResult>.Output getInput() throws Exception {
		FConnection<? extends InstructionResult>.Output input = null;
		for(int i = 0; i<inputs.size(); i++) {
			int j = nextInput;
			nextInput = (nextInput + 1) % inputs.size();
			if(inputs.get(j).ready()) {
				input = inputs.get(j);
				break;
			}
		}
		
		return input;
	}

	@Override
	public void tock() throws Exception {
		InstructionResult res = reorderBuffer[bufferIndexStart];
		if(res == null)
			return;
		
		reorderBuffer[bufferIndexStart] = null;
		
		if(res instanceof MemoryResult) {
			Saac.InstructionCounter++;
			MemoryResult mr = (MemoryResult) res;
			depChecker.dirtyMem.remove(mr.getValue());
		} else if(res instanceof RegisterResult) {
			Saac.InstructionCounter++;
			RegisterResult rr = (RegisterResult) res;
			Output.info.println(String.format("%d is written back to r%d", rr.getValue(), rr.getTarget()));
			resultOutput.put(rr);
			dirtyOutput.put(rr.getTarget());
		} else if(res instanceof StopResult) {
			Main.worker.interrupt();
			Main.finished = true;
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
