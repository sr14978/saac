package saac.dataObjects.Instruction.Results;

import saac.dataObjects.Instruction.InstructionI;

public abstract class InstructionResult implements InstructionI{
	protected int instructionNumber;
	
	public int getVirtualNumber() {
		return instructionNumber;
	}
	
	public String toString() {
		return "(" + Integer.toString(instructionNumber) + ") ";
	}
}
