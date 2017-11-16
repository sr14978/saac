package saac.dataObjects;

public abstract class InstructionResult {
	protected int instructionNumber;
	
	public int getID() {
		return instructionNumber;
	}
	
	public String toString() {
		return "(" + Integer.toString(instructionNumber) + ") ";
	}
}
