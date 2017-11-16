package saac.dataObjects;

public class MemoryResult extends InstructionResult {
	
	private int value;
	
	public MemoryResult(int instructionNumber, int i) {
		this.instructionNumber = instructionNumber;
		value = i;
	}

	public int getValue() {
		return value;
	}
	
	public String toString() {
		return super.toString() + "target addr: " + Integer.toString(value); 
	}
}
