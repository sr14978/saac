package saac.dataObjects.Instruction.Results;

public class MemoryResult extends InstructionResult {
	
	private int value;
	private int address;
	
	public MemoryResult(int instructionNumber, int a, int v) {
		this.instructionNumber = instructionNumber;
		value = v;
		address = a;
	}

	public int getValue() {
		return value;
	}
	
	public int getAddr() {
		return address;
	}
	
	public String toString() {
		return super.toString() + "addr: " + Integer.toString(address) + ", val: " + Integer.toString(value); 
	}
}
