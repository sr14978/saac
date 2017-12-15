package saac.dataObjects.Instruction.Empty;

import java.util.Optional;

public class Item {
	private Optional<Integer> register;
	private Optional<Integer> data;

	public static Item Register(int value) {
		return new Item(Optional.of(value), Optional.empty());
	}
	
	public static Item Data(int value) {
		return new Item(Optional.empty(), Optional.of(value));
	}
	
	private Item(Optional<Integer> register, Optional<Integer> data) {
		this.register = register;
		this.data = data;
	}
	public boolean isRegisterNum() {
		return register.isPresent();
	}
	public boolean isDataValue() {
		return data.isPresent();
	}
	public int getValue() {
		return register.isPresent()?register.get():data.get();
	}
	public String toString() {
		return register.isPresent()?"r"+register.get():"d" + data.get();
	}
}
