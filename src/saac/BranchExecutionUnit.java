package saac;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BranchExecutionUnit implements ClockedComponent{
	private FConnection<Instruction>.Output instructionIn;
	FConnection<Integer>.Input output;
	Integer bufferOut;
	
	BranchExecutionUnit(FConnection<Instruction>.Output instructionIn, FConnection<Integer>.Input output) {
		this.instructionIn = instructionIn;
		this.output = output;
	}
	
	@Override
	public void tick() throws FullChannelException {
		if(bufferOut != null)
			return;
		if(!instructionIn.ready())
			return;
		Instruction inst = instructionIn.get();
		switch(inst.getOpcode()) {
		case Br:
			bufferOut = inst.getParamA();
			break;
		case Jmp:
			bufferOut = inst.getParamA() + inst.getParamC();
			System.out.println("jumping to " + bufferOut);
			break;
		case JmpN:
			if(inst.getParamB() < 0) {
				bufferOut = inst.getParamA() + inst.getParamC();
				System.out.println("jumping to " + bufferOut);
			} else
				bufferOut = inst.getParamC();
			break;
		case JmpZ:
			if(inst.getParamB() == 0) {
				bufferOut = inst.getParamA() + inst.getParamC();
				System.out.println("jumping to " + bufferOut);
			} else
				bufferOut = inst.getParamC();
			break;
		default:
			throw new NotImplementedException();
		}
			
	}

	@Override
	public void tock() throws FullChannelException {
		if(bufferOut == null)
			return;
		else if(output.clear()) {
			output.put(bufferOut);
			bufferOut = null;
		}
	}

}
