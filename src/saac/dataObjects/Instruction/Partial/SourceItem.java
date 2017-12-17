package saac.dataObjects.Instruction.Partial;

import java.util.Optional;

public class SourceItem {
	private Optional<Integer> register;
	private Optional<Integer> data;

	public static SourceItem Register(int value) {
		return new SourceItem(Optional.of(value), Optional.empty());
	}
	
	public static SourceItem Data(int value) {
		return new SourceItem(Optional.empty(), Optional.of(value));
	}
	
	private SourceItem(Optional<Integer> register, Optional<Integer> data) {
		this.register = register;
		this.data = data;
	}
	public boolean isRegister() {
		return register.isPresent();
	}
	public boolean isDataValue() {
		return data.isPresent();
	}
	public int getValue() {
		return register.isPresent()?register.get():data.get();
	}
	public String toString() {
		return register.isPresent()?"v"+register.get():"d" + data.get();
	}
}
