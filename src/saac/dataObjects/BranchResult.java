package saac.dataObjects;

public class BranchResult extends InstructionResult {

	private int pc;
	private boolean prediction;
	private boolean actual;
	
	public BranchResult(int instructionNumber, int pc, boolean prediction, boolean actual) {
		this.instructionNumber = instructionNumber;
		this.pc = pc;
		this.prediction = prediction;
		this.actual = actual;
	}
	
	public BranchResult(int instructionNumber, int pc, int prediction, boolean actual) {
		this(instructionNumber, pc, prediction == 1, actual);
	}
	
	public boolean wasCorrect() {
		return prediction == actual;
	}
	
	public int getPc() {
		return pc;
	}
	
	public String toString() {
		return super.toString() + (wasCorrect()?"correct":"wrong");
	}
	
}
