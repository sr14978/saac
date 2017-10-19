package saac;

public class RegisterResult extends InstructionResult {

	private int target;
	private int value;

	RegisterResult(int target, int value) {
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
		return String.format("target: %d, value: %d", target, value); 
	}

}
