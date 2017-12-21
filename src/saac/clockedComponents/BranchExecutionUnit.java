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
import saac.utils.NotImplementedException;

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
		/*
		case Br:
			bufferOut = new BranchResult(inst.getVirtualNumber(), inst.getDest().get().getRegNumber(), true, true, inst.getParamD());
			break;
		case Jmp:
			bufferOut = new BranchResult(inst.getVirtualNumber(), inst.getParamA() + inst.getParamC(), true, true, inst.getParamD());
			Output.jumping_info.println("jumping to " + bufferOut);
			break;
		*/
		case JmpC:
			if(inst.getParamB().get().getScalarValue() != 0) {
				bufferOut = new BranchResult(inst.getVirtualNumber(),
						inst.getParamA().get().getScalarValue() + inst.getParamC().get().getScalarValue(),
						inst.getParamD().get().getScalarValue(),
						true, inst.getParamC().get().getScalarValue()-1);
				Output.jumping_info.println("jumping to " + bufferOut);
			} else
				bufferOut = new BranchResult(inst.getVirtualNumber(),
						inst.getParamC().get().getScalarValue(),
						inst.getParamD().get().getScalarValue(),
						false, inst.getParamC().get().getScalarValue()-1);
			break;
		case Ln:
			bufferOut = new BranchResult(inst.getVirtualNumber(), inst.getParamA().get().getScalarValue(), true, true, -1);
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
