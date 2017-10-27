package saac.clockedComponents;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponent;
import saac.unclockedComponents.InstructionsSource;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;

public class Fetcher implements ClockedComponent, VisibleComponent {

	RegisterFile registerFile;
	FConnection<int[]>.Input output;
	int[] bufferOut;
	FConnection<Integer>.Output fromBrUnit;
	int programCounter = 0;
	
	boolean halt = false;
	
	public Fetcher(RegisterFile registerFile, FConnection<int[]>.Input output, FConnection<Integer>.Output fromBrUnit) {
		this.output = output;
		this.fromBrUnit = fromBrUnit;
		this.registerFile = registerFile;
	}
	
	@Override
	public void tick() throws Exception {
		
		if(halt) {
			
			if(!fromBrUnit.ready())
				return;
			Integer newPC = fromBrUnit.get();
			halt = false;
			programCounter = newPC;
		}
		
		if(bufferOut != null)
			return;
		
		bufferOut = InstructionsSource.getInstruction(programCounter);
		System.out.println("Fetching instruction: " + programCounter + " = " + Opcode.fromInt(bufferOut[0]));
		programCounter++;
		
		switch(Opcode.fromInt(bufferOut[0])) {
		case Jmp:
			programCounter += bufferOut[1];
			bufferOut = null;
			System.out.println("Fetch is jumping");
			tick();
			break;
		case Br:
			programCounter = bufferOut[1];
			bufferOut = null;
			System.out.println("Fetch is Branching");
			tick();
			break;
		case JmpN:
		case JmpZ:
			bufferOut[3] = programCounter;
		case Ln:
			halt = true;
			break;
		default:
			break;
		}		
	}
	
	@Override
	public void tock() throws Exception {
		if(bufferOut == null)
			return;
		else if(output.clear()) {
			output.put(bufferOut);
			bufferOut = null;
		}
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
