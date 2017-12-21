package saac.dataObjects.Instruction.Partial;

public class DestItem {
	private int regValue;
	private int virtualregValue;
	boolean isVector;
	
	public DestItem(int regValue, int virtualregValue, boolean isVector) {
		this.setRegNumber(regValue);
		this.setVirtualRegNumber(virtualregValue);
		this.isVector = isVector;
	}
	public int getRegNumber() {
		return regValue;
	}
	private void setRegNumber(int regValue) {
		this.regValue = regValue;
	}
	public int getVirtualRegNumber() {
		return virtualregValue;
	}
	private void setVirtualRegNumber(int virtualregValue) {
		this.virtualregValue = virtualregValue;
	}
	public boolean isVector() {
		return isVector;
	}
	public String toString() {
		return "("+ regValue + ", " + virtualregValue + ")";
	}
}
