package saac;

public class MemoryResult extends InstructionResult {
	
	private int value;
	
	public MemoryResult(int i) {
		value = i;
	}

	public int getValue() {
		return value;
	}
}
