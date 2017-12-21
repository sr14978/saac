package saac.dataObjects.Instruction.Results;

import saac.dataObjects.Instruction.Value;

public class MemoryResult extends InstructionResult {
	
	private Value value;
	private int address;
	
	public MemoryResult(int instructionNumber, int a, Value v) {
		this.instructionNumber = instructionNumber;
		value = v;
		address = a;
	}

	public Value getValue() {
		return value;
	}
	
	public int getAddr() {
		return address;
	}
	
	public String toString() {
		return super.toString() + "addr: " + Integer.toString(address) + ", val: " + value.toString(); 
	}
}
