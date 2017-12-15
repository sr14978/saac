package saac.clockedComponents;

import java.util.Optional;

public class RegisterFile {

	private final static int ArchitecturalRegistersNum = 12;
	private int[] architecturalRegisters = new int[ArchitecturalRegistersNum];
	private boolean[] architecturalDirties = new boolean[ArchitecturalRegistersNum];
	
	RatItem[] RAT = new RatItem[ArchitecturalRegistersNum];
	
	public boolean isDirty(int registerNumber) {
		return architecturalDirties[registerNumber];
	}
	
	public void setDirty(int registerNumber, boolean value) {
		architecturalDirties[registerNumber] = value;
	}
	
	public int getRegisterValue(int registerNumber) {
		return architecturalRegisters[registerNumber];
	}
	
	public void setLatestRegister(int registerNumber, int value) {
		architecturalRegisters[registerNumber] = value;
	}

	public RatItem getLatestRegister(int registerNumber) {
		return RAT[registerNumber];
	}
	
	public void setVirtualRegister(int registerNumber, RatItem virtualRegisterNumber) {
		RAT[registerNumber] = virtualRegisterNumber;
	}

	public static class RatItem {
		private Optional<Integer> virtualRegister;
		private Optional<Integer> architecturalRegister;

		public static RatItem Virtual(int value) {
			return new RatItem(Optional.of(value), Optional.empty());
		}
		
		public static RatItem Architectural(int value) {
			return new RatItem(Optional.empty(), Optional.of(value));
		}
		
		private RatItem(Optional<Integer> virtualRegister, Optional<Integer> architecturalRegister) {
			this.virtualRegister = virtualRegister;
			this.architecturalRegister = architecturalRegister;
		}
		public boolean isVirtual() {
			return virtualRegister.isPresent();
		}
		public boolean isArchitectural() {
			return architecturalRegister.isPresent();
		}
		public int getValue() {
			return virtualRegister.isPresent()?virtualRegister.get():architecturalRegister.get();
		}
	}
	
}
