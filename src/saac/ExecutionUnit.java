package saac;

public class ExecutionUnit implements ClockedComponent{

	Connection<Instruction>.Output instructionIn;
	Connection<InstructionResult>.Input resultOut;
	InstructionResult buffer;
	
	ExecutionUnit(Connection<Instruction>.Output instructionIn, Connection<InstructionResult>.Input resultOut) {
		this.instructionIn = instructionIn;
		this.resultOut = resultOut;
	}
	
	@Override
	public void tick() {
		if(buffer != null)
			return;
		Instruction inst = instructionIn.get();
		if(inst == null)
			return;
		switch(inst.getOpcode()) {
		case Ldc:
			buffer = new InstructionResult(inst.getTarget(), inst.getSourceA());
		case Add:
		case Addi:
			buffer = new InstructionResult(inst.getTarget(), inst.getSourceA() + inst.getSourceB());
			break;
		case Nop:
			break;
		default:
			break;
		}
			
	}

	@Override
	public void tock() throws FullChannelException {
		if(resultOut.isEmpty()) {
			resultOut.put(buffer);
			buffer = null;
		}
	}

}
