package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.function.BiFunction;

import saac.Settings;
import saac.dataObjects.Instruction.Complete.CompleteInstruction;
import saac.dataObjects.Instruction.Results.BlankResult;
import saac.dataObjects.Instruction.Results.InstructionResult;
import saac.dataObjects.Instruction.Results.RegisterResult;
import saac.dataObjects.Instruction.Results.StopResult;
import saac.interfaces.ChannelException;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.MultiFConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ArithmeticUnit implements ClockedComponentI, VisibleComponentI, ClearableComponent{

	private FConnection<CompleteInstruction>.Output instructionIn;
	private FConnection<InstructionResult>.Input resultOut;
	MultiFConnection<RegisterResult>.Input virtualRegisterValueBus;
	private InstructionResult bufferOut;
	private int instructionDelay = 0;
	
	public ArithmeticUnit(FConnection<CompleteInstruction>.Output instructionIn,
			FConnection<InstructionResult>.Input resultOut,
			MultiFConnection<RegisterResult>.Input virtualRegisterValueBus) {
		this.instructionIn = instructionIn;
		this.resultOut = resultOut;
		this.virtualRegisterValueBus = virtualRegisterValueBus;
	}
	
	@Override
	public void tick() throws ChannelException {
		if(bufferOut != null)
			return;
		if(!instructionIn.ready())
			return;
		CompleteInstruction inst = instructionIn.pop();
		switch(inst.getOpcode()) {
		case Ldc:
			bufferOut = new RegisterResult(inst.getVirtualNumber(), inst.getDest().get(), inst.getParamA().get());
			break;
		case Add:
		case Addi:
			bufferOut = binaryOperator(inst, (x,y)->x+y);
			break;
		case Sub:
		case Subi:
			bufferOut = binaryOperator(inst, (x,y)->x-y);;
			break;
		case Mul:
		case Muli:
			bufferOut = binaryOperator(inst, (x,y)->x*y);;
			break;
		case Nop:
			bufferOut = new BlankResult(inst.getVirtualNumber());
			break;
		case Stop:
			bufferOut = new StopResult(inst.getVirtualNumber());
			break;
		case Div:
		case Divi:
			bufferOut = binaryOperator(inst, (x,y)->x/y);;
			break;
		default:
			throw new NotImplementedException();
		}
		instructionDelay = Instructions.InstructionDelay.get(inst.getOpcode());
			
	}

	private static RegisterResult binaryOperator(CompleteInstruction inst, BiFunction<Integer, Integer, Integer> f) {
		return new RegisterResult(inst.getVirtualNumber(), inst.getDest().get(), f.apply(inst.getParamA().get(), inst.getParamB().get()));
	}
	
	@Override
	public void tock() throws ChannelException {
		if(instructionDelay > 0)
			instructionDelay -= 1;
		else if(bufferOut == null)
			return;
		else if(resultOut.clear() && virtualRegisterValueBus.clear()) {
			resultOut.put(bufferOut);
			if(bufferOut instanceof RegisterResult && Settings.REGISTER_RENAMING_ENABLED) {
				virtualRegisterValueBus.put((RegisterResult) bufferOut);
			}
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

	@Override
	public void clear(int i) {
		if(bufferOut != null && bufferOut.getVirtualNumber() > i)
			bufferOut = null;
	}
	
}
