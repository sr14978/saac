package saac.dataObjects;

public class BranchResult extends InstructionResult {

	private int pc;
	
	public BranchResult(int instructionNumber, int pc) {
		this.instructionNumber = instructionNumber;
		this.pc = pc;
	}
	
	public int getPc() {
		return pc;
	}
	
	public String toString() {
		return super.toString() + "";
	}
	
}
