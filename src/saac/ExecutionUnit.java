package saac;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ExecutionUnit implements ClockedComponent{

	private FConnection<Instruction>.Output instructionIn;
	private FConnection<InstructionResult>.Input resultOut;
	private InstructionResult bufferOut;
	private int instructionDelay = 0;
	
	ExecutionUnit(FConnection<Instruction>.Output instructionIn, FConnection<InstructionResult>.Input resultOut) {
		this.instructionIn = instructionIn;
		this.resultOut = resultOut;
	}
	
	@Override
	public void tick() throws FullChannelException {
		if(bufferOut != null)
			return;
		if(!instructionIn.ready())
			return;
		Instruction inst = instructionIn.get();
		switch(inst.getOpcode()) {
		case Ldc:
			bufferOut = new RegisterResult(inst.getParamA(), inst.getParamB());
			break;
		case Add:
		case Addi:
			bufferOut = new RegisterResult(inst.getParamA(), inst.getParamB() + inst.getParamC());
			break;
		case Sub:
		case Subi:
			bufferOut = new RegisterResult(inst.getParamA(), inst.getParamB() - inst.getParamC());
			break;
		case Mul:
		case Muli:
			bufferOut = new RegisterResult(inst.getParamA(), inst.getParamB() * inst.getParamC());
			break;
		case Nop:
			break;
		case Div:
		case Divi:
			bufferOut = new RegisterResult(inst.getParamA(), inst.getParamB() / inst.getParamC());
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

}
