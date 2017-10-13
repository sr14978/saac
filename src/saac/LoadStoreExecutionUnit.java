package saac;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class LoadStoreExecutionUnit implements ClockedComponent{

	private Connection<Instruction>.Output instructionIn;
	private Connection<InstructionResult>.Input resultOut;
	private InstructionResult buffer;
	private int instructionDelay = 0;
	private Memory memory;
	
	LoadStoreExecutionUnit(Connection<Instruction>.Output instructionIn, Connection<InstructionResult>.Input resultOut, Memory memory) {
		this.instructionIn = instructionIn;
		this.resultOut = resultOut;
		this.memory = memory;
	}
	
	@Override
	public void tick() {
		if(buffer != null)
			return;
		Instruction inst = instructionIn.get();
		if(inst == null)
			return;
		switch(inst.getOpcode()) {
		case Ldma:
			buffer = new InstructionResult(inst.getParamA(), memory.getWord(inst.getParamB()));
			break;
		case Stma:
			memory.setWord(inst.getParamA(), inst.getParamB());
			break;
		case Ldmi:
			buffer = new InstructionResult(inst.getParamA(), memory.getWord(inst.getParamB() + inst.getParamC()));
			break;
		case Stmi:
			memory.setWord(inst.getParamA(), inst.getParamB() + inst.getParamB());
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
		else if(buffer == null)
			return;
		else if(resultOut.isEmpty()) {
			resultOut.put(buffer);
			buffer = null;
		}
	}

}
