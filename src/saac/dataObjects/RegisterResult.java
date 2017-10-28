package saac.dataObjects;

public class RegisterResult extends InstructionResult {

	private int target;
	private int value;

	public RegisterResult(int target, int value) {
		this.value = value;
		this.target = target;
	}
	
	public int getValue() {
		return value;
	}
	public int getTarget() {
		return target;
	}
	public String toString() {
		return String.format("target reg: %d, value: %d", target, value); 
	}

}
