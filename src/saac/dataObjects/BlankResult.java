package saac.dataObjects;

public class BlankResult extends InstructionResult {
		
	public BlankResult(int instructionNumber) {
		this.instructionNumber = instructionNumber;
	}
	
	public String toString() {
		return super.toString() + "Nop Result"; 
	}
}
