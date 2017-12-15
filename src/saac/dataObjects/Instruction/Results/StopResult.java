package saac.dataObjects.Instruction.Results;

public class StopResult extends InstructionResult {
		
	public StopResult(int instructionNumber) {
		this.instructionNumber = instructionNumber;
	}
	
	public String toString() {
		return super.toString() + "Stop Result"; 
	}
}
