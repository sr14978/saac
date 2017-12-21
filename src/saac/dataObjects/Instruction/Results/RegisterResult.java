package saac.dataObjects.Instruction.Results;

import saac.dataObjects.Instruction.Value;
import saac.dataObjects.Instruction.Partial.DestItem;

public class RegisterResult extends InstructionResult {

	private DestItem target;
	private Value value;

	public RegisterResult(int instructionNumber, DestItem target, Value value) {
		this.instructionNumber = instructionNumber;
		this.value = value;
		this.target = target;
	}
	
	public Value getValue() {
		return value;
	}
	public DestItem getTarget() {
		return target;
	}
	public String toString() {
		return super.toString() + String.format("target: %s, value: %s", target.toString(), value.toString()); 
	}

}
