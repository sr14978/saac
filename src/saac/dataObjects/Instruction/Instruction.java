package saac.dataObjects.Instruction;

import saac.utils.Instructions.Opcode;

public abstract class Instruction<D,S> implements InstructionI{
	public abstract int getVirtualNumber();
	public abstract Opcode getOpcode();
	public abstract D getDest();
	public abstract S getParamA();
	public abstract S getParamB();
	public abstract S getParamC();
}
