package saac;

public class ExecutionUnit implements ClockedComponent{

	private Connection<Instruction>.Output instructionIn;
	private Connection<InstructionResult>.Input resultOut;
	private InstructionResult buffer;
	private int instructionDelay = 0;
	
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
		case Sub:
		case Subi:
			buffer = new InstructionResult(inst.getTarget(), inst.getSourceA() - inst.getSourceB());
			break;
		case Mul:
		case Muli:
			buffer = new InstructionResult(inst.getTarget(), inst.getSourceA() * inst.getSourceB());
			break;
		case Nop:
			break;
		case Div:
		case Divi:
			buffer = new InstructionResult(inst.getTarget(), inst.getSourceA() / inst.getSourceB());
			break;
		}
		instructionDelay = Instruction.InstructionDelay.get(inst.getOpcode());
			
	}

	@Override
	public void tock() throws FullChannelException {
		if(instructionDelay > 0)
			instructionDelay -= 1;
		else if(resultOut.isEmpty()) {
			resultOut.put(buffer);
			buffer = null;
		}
	}

}
