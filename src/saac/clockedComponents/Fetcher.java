package saac.clockedComponents;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponent;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;

public class Fetcher implements ClockedComponent, VisibleComponent {

	RegisterFile registerFile;
	FConnection<int[]>.Input output;

	FConnection<Integer>.Output fromBrUnit;
	int programCounter = 0;
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
			break;
		default:
			break;
		}
		
		output.put(inst);
	}

	class View implements ComponentView {
		
		Point position; 
		View(int x, int y){
			position = new Point(x, y);
		}
		
		public void paint(GraphicsContext gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawBox(gc, "Fetcher");
			gc.setFill(Color.BLACK);
			gc.fillText("pc: " + Integer.toString(programCounter), 10, 35);
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}
}
