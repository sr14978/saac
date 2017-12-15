package saac.dataObjects.Instruction.Partial;

import java.util.Optional;

public class SourceItem {
	private Optional<Integer> virtualRegister;
	private Optional<Integer> data;

	public static SourceItem Register(int value) {
		return new SourceItem(Optional.of(value), Optional.empty());
	}
	
	public static SourceItem Data(int value) {
		return new SourceItem(Optional.empty(), Optional.of(value));
	}
	
	private SourceItem(Optional<Integer> register, Optional<Integer> data) {
		this.virtualRegister = register;
		this.data = data;
	}
	public boolean isRegisterNum() {
		return virtualRegister.isPresent();
	}
	public boolean isDataValue() {
		return data.isPresent();
	}
	public int getValue() {
		return virtualRegister.isPresent()?virtualRegister.get():data.get();
	}
	public String toString() {
		return virtualRegister.isPresent()?"v"+virtualRegister.get():"d" + data.get();
	}
}
