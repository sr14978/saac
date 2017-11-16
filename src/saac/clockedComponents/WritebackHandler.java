package saac.clockedComponents;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;

import saac.dataObjects.InstructionResult;
import saac.dataObjects.MemoryResult;
import saac.dataObjects.RegisterResult;
import saac.dataObjects.ReorderBufferFullException;
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
	RegisterFile registerFile;
	DepChecker depChecker;
	FConnection<RegisterResult>.Input resultOutput;
	FConnection<Integer>.Input dirtyOutput;
	
	final static int BUFF_SIZE = 8;
	InstructionResult[] reorderBuffer = new InstructionResult[BUFF_SIZE];
	
	int bufferIndexStart = 0;
	int bufferIndexEnd = 0;
	int bufferInstructionStart = 0;
	boolean bufferEmpty = true;
	
	public WritebackHandler(RegisterFile rf, DepChecker depChecker,
			FConnection<InstructionResult>.Output inputEU_A, 
			FConnection<InstructionResult>.Output inputEU_B, 
			FConnection<InstructionResult>.Output inputLS,
			FConnection<RegisterResult>.Input resultOutput,
			FConnection<Integer>.Input dirtyOutput) {
		this.inputEU_A = inputEU_A;
		this.inputEU_B = inputEU_B;
		this.inputLS = inputLS;
		this.registerFile = rf;
		this.depChecker = depChecker;
		this.resultOutput = resultOutput;
		this.dirtyOutput = dirtyOutput;
	}

	@Override
	public void tick() throws Exception {
		
		FConnection<InstructionResult>.Output input;
		if(inputLS.ready())
			input = inputLS;
		else if(inputEU_A.ready())
			input = inputEU_A;
		else if(inputEU_B.ready())
			input = inputEU_B;
		else
			return;
		
		InstructionResult res = input.peak();
				
		if( bufferEmpty || bufferIndexEnd != bufferIndexStart || res.getID() == bufferInstructionStart ) {
			input.pop();
			
			int instructionOffset = res.getID() - bufferInstructionStart;
			if(instructionOffset > BUFF_SIZE)
				throw new ReorderBufferFullException();
			
			int bufferIndex = (bufferIndexStart + instructionOffset) % BUFF_SIZE;
			if(instructionOffset > BUFF_SIZE - bufferIndexStart && bufferIndex > bufferIndexStart )
				throw new ReorderBufferFullException();
			
			System.out.println("bufferIndex: "+bufferIndex);
			
			reorderBuffer[bufferIndex] = res;
			
			System.out.println("1: "+(bufferIndex - bufferIndexStart + BUFF_SIZE + 1) % BUFF_SIZE );
			System.out.println("2: "+(bufferIndexEnd - bufferIndexStart + BUFF_SIZE) % BUFF_SIZE);
			
			if( (bufferIndex - bufferIndexStart + 2*BUFF_SIZE + 1) % (2*BUFF_SIZE )
					> (bufferIndexEnd - bufferIndexStart + 2*BUFF_SIZE) % (2*BUFF_SIZE )  ) {
				bufferIndexEnd = (bufferIndex + 1) % BUFF_SIZE;
				bufferEmpty = false;
				System.out.println("set: "+bufferIndexEnd);
			}
		}
		
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
			DrawingHelper.drawBox(gc, "Write Back Handler", 3*BOX_SIZE, 50);
			gc.setPaint(Color.BLACK);
			for(int i = 0; i<reorderBuffer.length; i++)
				if(reorderBuffer[i] != null)
					gc.drawString(Integer.toString(reorderBuffer[i].getID()), i*40 + 20, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
	
}
