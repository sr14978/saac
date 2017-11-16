package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;

import saac.dataObjects.Instruction;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.FullChannelException;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Output;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BranchExecutionUnit implements ClockedComponentI, VisibleComponentI{
	private FConnection<Instruction>.Output instructionIn;
	FConnection<Integer>.Input output;
	Integer bufferOut;
	
	public BranchExecutionUnit(FConnection<Instruction>.Output instructionIn, FConnection<Integer>.Input output) {
		this.instructionIn = instructionIn;
		this.output = output;
	}
	
	@Override
	public void tick() throws FullChannelException {
		if(bufferOut != null)
			return;
		if(!instructionIn.ready())
			return;
		Instruction inst = instructionIn.pop();
		switch(inst.getOpcode()) {
		case Br:
			bufferOut = inst.getParamA();
			break;
		case Jmp:
			bufferOut = inst.getParamA() + inst.getParamC();
			Output.jumping_info.println("jumping to " + bufferOut);
			break;
		case JmpN:
			if(inst.getParamB() < 0) {
				bufferOut = inst.getParamA() + inst.getParamC();
				Output.jumping_info.println("jumping to " + bufferOut);
			} else
				bufferOut = inst.getParamC();
			break;
		case JmpZ:
			if(inst.getParamB() == 0) {
				bufferOut = inst.getParamA() + inst.getParamC();
				Output.jumping_info.println("jumping to " + bufferOut);
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
	
	class View extends ComponentView {
			
		View(int x, int y) {
			super(x, y);
		}

		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Branches");
			gc.setColor(Color.BLACK);
			if(bufferOut != null)
				gc.drawString("pc: " + bufferOut, 10, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

}
