package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import saac.Settings;
import saac.dataObjects.DelayQueueItem;
import saac.dataObjects.Instruction.Complete.CompleteInstruction;
import saac.dataObjects.Instruction.Results.InstructionResult;
import saac.dataObjects.Instruction.Results.MemoryResult;
import saac.dataObjects.Instruction.Results.RegisterResult;
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

public class LoadStoreExecutionUnit implements ClockedComponentI, VisibleComponentI, ClearableComponent{
	int LDLimit = Settings.LOAD_LIMIT;
		
	private FConnection<CompleteInstruction>.Output instructionIn;
	private FConnection<InstructionResult>.Input resultOut;
	MultiFConnection<RegisterResult>.Input virtualRegisterValueBus;
	private List<DelayQueueItem<InstructionResult>> buffer = new LinkedList<>();
	private Memory memory;
	
	public LoadStoreExecutionUnit(Memory memory,
			FConnection<CompleteInstruction>.Output instructionIn,
			FConnection<InstructionResult>.Input resultOut,
			MultiFConnection<RegisterResult>.Input virtualRegisterValueBus) {
		this.instructionIn = instructionIn;
		this.resultOut = resultOut;
		this.memory = memory;
		this.resultOut = resultOut;
		this.virtualRegisterValueBus = virtualRegisterValueBus;
	}
	
	@Override
	public void tick() throws ChannelException {
		if(buffer.size() >= LDLimit)
			return;
		
		
		if(!instructionIn.ready())
			return;
		CompleteInstruction inst = instructionIn.pop();
		
		InstructionResult res = null;		
		int delay = 0;
		switch(inst.getOpcode()) {
		case Ldma:
			res = new RegisterResult(inst.getVirtualNumber(), inst.getDest().get(), memory.getWord(inst.getParamA().get()));
			delay = Instructions.InstructionDelay.get(inst.getOpcode());
			break;
		case Stma:
			//memory.setWord(inst.getParamB(), inst.getParamA());
			res = new MemoryResult(inst.getVirtualNumber(), inst.getParamB().get(), inst.getParamA().get());
			break;
		case Ldmi:
			res = new RegisterResult(inst.getVirtualNumber(), inst.getDest().get(), memory.getWord(inst.getParamA().get() + inst.getParamB().get()));
			delay = Instructions.InstructionDelay.get(inst.getOpcode());
			break;
		case Stmi:
			//memory.setWord(inst.getParamB() + inst.getParamC(), inst.getParamA());
			res = new MemoryResult(inst.getVirtualNumber(), inst.getParamB().get() + inst.getParamC().get(), inst.getParamA().get());
			break;
		default:
			throw new NotImplementedException();
		}
		buffer.add(new DelayQueueItem<InstructionResult>(res, delay));
	}

	@Override
	public void tock() throws ChannelException {
		InstructionResult res = null;
		for(DelayQueueItem<InstructionResult> i : new LinkedList<>(buffer)) {
			if(i.getDelay() > 0)
				i.decrementResultToZero();
			else if(res == null && resultOut.clear()) {
				res = i.getResult();
				buffer.remove(i);
			}
		}
		if(res == null)
			return;
		else {
			resultOut.put(res);
			if(res instanceof RegisterResult && Settings.REGISTER_RENAMING_ENABLED) {
				virtualRegisterValueBus.put((RegisterResult) res);
			}
			res = null;
		}
	}
	
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Load/Store");
			gc.setColor(Color.BLACK);
			int i = 0;
			for(DelayQueueItem<InstructionResult> it : new ArrayList<>(buffer)) {
				gc.drawString(it.getResult()  + " (" + Integer.toString(it.getDelay()) + ")", 5, 22+10*i);
				i++;
			}
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear(int i) {
		if(buffer == null)
			return;
		List<DelayQueueItem<InstructionResult>> results = new LinkedList<>();
		for(DelayQueueItem<InstructionResult> inst : buffer) {
			if(inst.getResult().getVirtualNumber() <= i) {
				results.add(inst);
			}
		}
		buffer = results;
	}
}
