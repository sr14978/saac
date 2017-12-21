package saac.dataObjects.Instruction.Partial;
import saac.dataObjects.Instruction.Register;
import java.util.Optional;

import saac.dataObjects.Instruction.Value;

public class SourceItem {
	private Optional<Register> register;
	private Optional<Value> data;

	public static SourceItem Register(Register value) {
		return new SourceItem(Optional.of(value), Optional.empty());
	}
	
	public static SourceItem ScalarData(int value) {
		return new SourceItem(Optional.empty(), Optional.of(Value.Scalar(value)));
	}
	
	public static SourceItem VectorData(int[] value) {
		return new SourceItem(Optional.empty(), Optional.of(Value.Vector(value)));
	}
	
	private SourceItem(Optional<Register> register, Optional<Value> data) {
		this.register = register;
		this.data = data;
	}
	public boolean isRegister() {
		return register.isPresent();
	}
	public boolean isDataValue() {
		return data.isPresent();
	}
	public Register getRegisterNumber() {
		return register.get();
	}
	public Value getDataValue() {
		return data.get();
	}
	public String toString() {
		return register.isPresent()?"r"+register.get():"d" + data.get();
	}
}
