package saac.dataObjects;

public class BranchResult extends InstructionResult {

	private int newPc;
	private int addr;
	private boolean prediction;
	private boolean actual;
	
	public BranchResult(int instructionNumber, int pc, boolean prediction, boolean actual, int addr) {
		this.instructionNumber = instructionNumber;
		this.newPc = pc;
		this.prediction = prediction;
		this.actual = actual;
		this.addr = addr;
	}
	
	public BranchResult(int instructionNumber, int pc, int prediction, boolean actual, int addr) {
		this(instructionNumber, pc, prediction == 1, actual, addr);
	}
	
	public boolean wasCorrect() {
		return prediction == actual;
	}
	
	public int getNewPc() {
		return newPc;
	}
	
	public String toString() {
		return super.toString() + (wasCorrect()?"correct":"wrong");
	}

	public boolean wasTaken() {
		return actual;
	}

	public int getAddr() {
		return addr;
	}
	
}
