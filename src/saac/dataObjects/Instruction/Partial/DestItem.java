package saac.dataObjects.Instruction.Partial;

public class DestItem {
	private int regValue;
	private int virtualregValue;
	public DestItem(int regValue, int virtualregValue) {
		this.setRegValue(regValue);
		this.setVirtualRegValue(virtualregValue);
	}
	public int getRegValue() {
		return regValue;
	}
	private void setRegValue(int regValue) {
		this.regValue = regValue;
	}
	public int getVirtualRegValue() {
		return virtualregValue;
	}
	private void setVirtualRegValue(int virtualregValue) {
		this.virtualregValue = virtualregValue;
	}
	public String toString() {
		return "("+ regValue + ", " + virtualregValue + ")";
	}
}
