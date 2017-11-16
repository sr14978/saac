package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;

import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;

public class Fetcher implements ClockedComponentI, VisibleComponentI {

	RegisterFile registerFile;
	FConnection<int[]>.Input output;

	FConnection<Integer>.Output fromBrUnit;
	int programCounter = 0;
	int instructionCounter = 0;
	FConnection<Integer>.Input addrOutput;
	FConnection<Boolean>.Input clearOutput;
	FConnection<int[]>.Output instructionInput;
	boolean halt = false;
	
	public Fetcher(RegisterFile registerFile,
			FConnection<int[]>.Input output,
			FConnection<Integer>.Output fromBrUnit,
			FConnection<Integer>.Input addrOutput,
			FConnection<Boolean>.Input clearOutput,
			FConnection<int[]>.Output instructionInput
			) {
		this.output = output;
		this.fromBrUnit = fromBrUnit;
		this.registerFile = registerFile;
		this.addrOutput = addrOutput;
		this.clearOutput = clearOutput;
		this.instructionInput = instructionInput;
	}
	
	@Override
	public void tick() throws Exception {
		
		if(halt) {
			if(!fromBrUnit.ready())
				return;
			Integer newPC = fromBrUnit.get();
			halt = false;
			programCounter = newPC;
		} else if(addrOutput.clear()) {
			addrOutput.put(programCounter);
			programCounter++;
		}
		
	}
	
	@Override
	public void tock() throws Exception {
		
		if(!instructionInput.ready())
			return;
		if(!output.clear())
			return;
		
		int[] inst = instructionInput.get();
				
		switch(Opcode.fromInt(inst[0])) {
		case Jmp:
			programCounter = inst[4] + 1 + inst[1];
			Output.jumping_info.println("Fetch is jumping");
			clearOutput.put(true);
			return;
		case Br:
			programCounter = inst[1];
			Output.jumping_info.println("Fetch is Branching");
			clearOutput.put(true);
			return;
		case JmpN:
		case JmpZ:
			inst[3] = inst[4] + 1;
		case Ln:
			halt = true;
			clearOutput.put(true);
			inst[4] = instructionCounter++;
			output.put(inst);
			break;
		default:
			inst[4] = instructionCounter++;
			output.put(inst);
			break;
		}		
	}

	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Fetcher");
			gc.setColor(Color.BLACK);
			gc.drawString("pc: " + Integer.toString(programCounter), 10, 35);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
}
