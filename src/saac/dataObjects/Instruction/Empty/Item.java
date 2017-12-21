package saac.dataObjects.Instruction.Empty;
import saac.dataObjects.Instruction.Register;
import java.util.Optional;

public class Item {
	private Optional<Register> register;
	private Optional<Integer> data;

	public static Item Register(Register value) {
		return new Item(Optional.of(value), Optional.empty());
	}
	
	public static Item Data(int value) {
		return new Item(Optional.empty(), Optional.of(value));
	}
	
	private Item(Optional<Register> register, Optional<Integer> data) {
		this.register = register;
		this.data = data;
	}
	public boolean isRegisterNum() {
		return register.isPresent();
	}
	public boolean isDataValue() {
		return data.isPresent();
	}
	public Register getRegisterNumber() {
		return register.get();
	}
	public int getDataValue() {
		return data.get();
	}
	public String toString() {
		return register.isPresent()?"r"+register.get():"d" + data.get();
	}
}
