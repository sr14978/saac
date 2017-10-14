package saac;

import java.util.HashSet;
import java.util.Set;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class LoadStoreExecutionUnit implements ClockedComponent{
	static final int LDLimit = 4;
	private class Item{
		InstructionResult result;
		int delay;
		Item(InstructionResult r, int d) {
			this.result = r;
			this.delay = d;
		}
	}
	
	private Connection<Instruction>.Output instructionIn;
	private Connection<InstructionResult>.Input resultOut;
	private Set<Item> buffer = new HashSet<>();
	private Memory memory;
	
	LoadStoreExecutionUnit(Connection<Instruction>.Output instructionIn, Connection<InstructionResult>.Input resultOut, Memory memory) {
		this.instructionIn = instructionIn;
		this.resultOut = resultOut;
		this.memory = memory;
	}
	
	@Override
	public void tick() {
		if(buffer.size() >= LDLimit)
			return;
		Instruction inst = instructionIn.get();
		if(inst == null)
			return;
		
		InstructionResult res = null;		
		switch(inst.getOpcode()) {
		case Ldma:
			res = new InstructionResult(inst.getParamA(), memory.getWord(inst.getParamB()));
			break;
		case Stma:
			memory.setWord(inst.getParamA(), inst.getParamB());
			break;
		case Ldmi:
			res = new InstructionResult(inst.getParamA(), memory.getWord(inst.getParamB() + inst.getParamC()));
			break;
		case Stmi:
			memory.setWord(inst.getParamA(), inst.getParamB() + inst.getParamB());
			break;
		default:
			throw new NotImplementedException();
		}
		buffer.add(new Item(res, Instructions.InstructionDelay.get(inst.getOpcode())));
	}

	@Override
	public void tock() throws FullChannelException {
		InstructionResult res = null;
		for(Item i : new HashSet<>(buffer)) {
			if(i.delay > 0)
				i.delay -= 1;
			else if(i.result != null && res == null) {
				res = i.result;
				buffer.remove(i);
			} else if(i.result == null) {
				buffer.remove(i);
			}
		}
		if(res == null)
			return;
		else if(resultOut.isEmpty()) {
			resultOut.put(res);
			res = null;
		}
	}

}
