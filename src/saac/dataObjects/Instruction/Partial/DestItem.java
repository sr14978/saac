package saac.dataObjects.Instruction.Partial;

public class DestItem {
	private int regValue;
	private int virtualregValue;
	public DestItem(int regValue, int virtualregValue) {
		this.setRegNumber(regValue);
		this.setVirtualRegNumber(virtualregValue);
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
	public String toString() {
		return "("+ regValue + ", " + virtualregValue + ")";
	}
}
