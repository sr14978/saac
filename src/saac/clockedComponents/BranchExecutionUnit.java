package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;

import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.dataObjects.Instruction.Complete.CompleteInstruction;
import saac.dataObjects.Instruction.Results.BranchResult;
import saac.dataObjects.Instruction.Results.InstructionResult;
import saac.interfaces.ChannelException;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Output;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BranchExecutionUnit implements ClockedComponentI, VisibleComponentI, ClearableComponent{
	private FConnection<CompleteInstruction>.Output instructionIn;
	FConnection<BranchResult>.Input outputToFetch;
	FConnection<InstructionResult>.Input outputToWB;
	BranchResult bufferOut;
	
	public BranchExecutionUnit(
			FConnection<CompleteInstruction>.Output instructionIn,
			FConnection<BranchResult>.Input outputToFetch,
			FConnection<InstructionResult>.Input input) {
		this.instructionIn = instructionIn;
		this.outputToFetch = outputToFetch;
		this.outputToWB = input;
	}
	
	@Override
	public void tick() throws ChannelException {
		if(bufferOut != null)
			return;
		if(!instructionIn.ready())
			return;
		CompleteInstruction inst = instructionIn.pop();
		switch(inst.getOpcode()) {
		case Br:
			bufferOut = new BranchResult(inst.getVirtualNumber(), inst.getDest().get().getRegNumber(), true, true, inst.getParamD());
			break;
		case Jmp:
			bufferOut = new BranchResult(inst.getVirtualNumber(), inst.getParamA() + inst.getParamC(), true, true, inst.getParamD());
			Output.jumping_info.println("jumping to " + bufferOut);
			break;
		case JmpN:
			if(inst.getParamB().get() < 0) {
				bufferOut = new BranchResult(inst.getVirtualNumber(), inst.getParamA() + inst.getParamC(), inst.getParamD(), true, inst.getParamC()-1);
				Output.jumping_info.println("jumping to " + bufferOut);
			} else
				bufferOut = new BranchResult(inst.getVirtualNumber(), inst.getParamC(), inst.getParamD(), false, inst.getParamC()-1);
			break;
		case JmpZ:
			if(inst.getParamB().get() == 0) {
				bufferOut = new BranchResult(inst.getVirtualNumber(), inst.getParamA() + inst.getParamC(), inst.getParamD(), true, inst.getParamC()-1);
				Output.jumping_info.println("jumping to " + bufferOut);
			} else
				bufferOut = new BranchResult(inst.getVirtualNumber(), inst.getParamC(), inst.getParamD(), false, inst.getParamC()-1);
			break;
		default:
			throw new NotImplementedException();
		}
			
	}

	@Override
	public void tock() throws ChannelException {
		if(bufferOut == null)
			return;
		else if(outputToFetch.clear() && outputToWB.clear()) {
			outputToFetch.put(bufferOut);
			outputToWB.put(bufferOut);
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

	@Override
	public void clear(int i) {
		if(bufferOut != null && bufferOut.getVirtualNumber() > i)
			bufferOut = null;
	}
}
