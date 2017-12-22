package saac.dataObjects.Instruction.Results;

public class BranchResult extends InstructionResult {

	boolean isLinkBranch;
	private int newPc;
	private int addr;
	private boolean isPredictionCorrect;
	private boolean actual;
	
	public static BranchResult LinkBranch(int instructionNumber, int pc, boolean isPredictionCorrect, boolean actual, int addr) {
		return new BranchResult(instructionNumber, pc, isPredictionCorrect, actual, addr, true);
	}
	
	public static BranchResult StandardBranch(int instructionNumber, int pc, boolean isPredictionCorrect, boolean actual, int addr) {
		return new BranchResult(instructionNumber, pc, isPredictionCorrect, actual, addr, false);
	}
	
	public static BranchResult StandardBranch(int instructionNumber, int pc, int prediction, boolean actual, int addr) {
		return new BranchResult(instructionNumber, pc, (prediction==1) == actual, actual, addr, false);
	}
	
	private BranchResult(int instructionNumber, int pc, boolean isPredictionCorrect, boolean actual, int addr, boolean isLinkBranch) {
		this.instructionNumber = instructionNumber;
		this.newPc = pc;
		this.isPredictionCorrect = isPredictionCorrect;
		this.actual = actual;
		this.addr = addr;
		this.isLinkBranch = isLinkBranch;
	}
		
	public boolean wasCorrect() {
		return isPredictionCorrect;
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
	
	public boolean isLinkBranch() {
		return isLinkBranch;
	}
	
}
