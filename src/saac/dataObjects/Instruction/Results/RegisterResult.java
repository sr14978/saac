package saac.dataObjects.Instruction.Results;

import saac.dataObjects.Instruction.Partial.DestItem;

public class RegisterResult extends InstructionResult {

	private DestItem target;
	private int value;

	public RegisterResult(int instructionNumber, DestItem target, int value) {
		this.instructionNumber = instructionNumber;
		this.value = value;
		this.target = target;
	}
	
	public int getValue() {
		return value;
	}
	public DestItem getTarget() {
		return target;
	}
	public String toString() {
		return super.toString() + String.format("target: %s, value: %d", target.toString(), value); 
	}

}
