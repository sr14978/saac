package saac.dataObjects.Instruction;

import saac.utils.Instructions.Opcode;

public abstract class Instruction<D,S> implements InstructionI, Comparable<Instruction<D, S>>{
	@Override
	public int compareTo(Instruction<D, S> o) {
		return Integer.compare(this.getVirtualNumber(), o.getVirtualNumber());
	}
	public abstract int getVirtualNumber();
	public abstract Opcode getOpcode();
	public abstract D getDest();
	public abstract S getParamA();
	public abstract S getParamB();
	public abstract S getParamC();
	public abstract S getParamD();
	
}
