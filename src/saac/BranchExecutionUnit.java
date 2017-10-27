package saac;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.ExecutionUnit.View;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BranchExecutionUnit implements ClockedComponent, VisibleComponent{
	private FConnection<Instruction>.Output instructionIn;
	FConnection<Integer>.Input output;
	Integer bufferOut;
	
	BranchExecutionUnit(FConnection<Instruction>.Output instructionIn, FConnection<Integer>.Input output) {
		this.instructionIn = instructionIn;
		this.output = output;
	}
	
	@Override
	public void tick() throws FullChannelException {
		if(bufferOut != null)
			return;
		if(!instructionIn.ready())
			return;
		Instruction inst = instructionIn.get();
		switch(inst.getOpcode()) {
		case Br:
			bufferOut = inst.getParamA();
			break;
		case Jmp:
			bufferOut = inst.getParamA() + inst.getParamC();
			System.out.println("jumping to " + bufferOut);
			break;
		case JmpN:
			if(inst.getParamB() < 0) {
				bufferOut = inst.getParamA() + inst.getParamC();
				System.out.println("jumping to " + bufferOut);
			} else
				bufferOut = inst.getParamC();
			break;
		case JmpZ:
			if(inst.getParamB() == 0) {
				bufferOut = inst.getParamA() + inst.getParamC();
				System.out.println("jumping to " + bufferOut);
			} else
				bufferOut = inst.getParamC();
			break;
		default:
			throw new NotImplementedException();
		}
			
	}

	@Override
	public void tock() throws FullChannelException {
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
			DrawingHelper.drawBox(gc, "Branches");
			gc.setFill(Color.BLACK);
			if(bufferOut != null)
				gc.fillText("pc: " + bufferOut, 10, 30);
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}

}
