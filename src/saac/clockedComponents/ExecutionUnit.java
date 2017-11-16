package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;

import saac.dataObjects.BlankResult;
import saac.dataObjects.Instruction;
import saac.dataObjects.InstructionResult;
import saac.dataObjects.RegisterResult;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.FullChannelException;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ExecutionUnit implements ClockedComponentI, VisibleComponentI{

	private FConnection<Instruction>.Output instructionIn;
	private FConnection<InstructionResult>.Input resultOut;
	private InstructionResult bufferOut;
	private int instructionDelay = 0;
	
	public ExecutionUnit(FConnection<Instruction>.Output instructionIn, FConnection<InstructionResult>.Input resultOut) {
		this.instructionIn = instructionIn;
		this.resultOut = resultOut;
	}
	
	@Override
	public void tick() throws FullChannelException {
		if(bufferOut != null)
			return;
		if(!instructionIn.ready())
			return;
		Instruction inst = instructionIn.pop();
		switch(inst.getOpcode()) {
		case Ldc:
			bufferOut = new RegisterResult(inst.getID(), inst.getParamA(), inst.getParamB());
			break;
		case Add:
		case Addi:
			bufferOut = new RegisterResult(inst.getID(), inst.getParamA(), inst.getParamB() + inst.getParamC());
			break;
		case Sub:
		case Subi:
			bufferOut = new RegisterResult(inst.getID(), inst.getParamA(), inst.getParamB() - inst.getParamC());
			break;
		case Mul:
		case Muli:
			bufferOut = new RegisterResult(inst.getID(), inst.getParamA(), inst.getParamB() * inst.getParamC());
			break;
		case Nop:
			bufferOut = new BlankResult(inst.getID());
			break;
		case Div:
		case Divi:
			bufferOut = new RegisterResult(inst.getID(), inst.getParamA(), inst.getParamB() / inst.getParamC());
			break;
		default:
			throw new NotImplementedException();
		}
		instructionDelay = Instructions.InstructionDelay.get(inst.getOpcode());
			
	}

	@Override
	public void tock() throws FullChannelException {
		if(instructionDelay > 0)
			instructionDelay -= 1;
		else if(bufferOut == null)
			return;
		else if(resultOut.clear()) {
			resultOut.put(bufferOut);
			bufferOut = null;
		}
	}
	
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "EU");
			gc.setColor(Color.BLACK);
			if(bufferOut != null)
				gc.drawString(bufferOut.toString() + "(" + Integer.toString(instructionDelay) + ")", 10, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
	
}
