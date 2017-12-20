package saac.dataObjects.Instruction.Partial;

import java.util.Optional;

import saac.dataObjects.Instruction.Value;

public class SourceItem {
	private Optional<Integer> register;
	private Optional<Value> data;

	public static SourceItem Register(int value) {
		return new SourceItem(Optional.of(value), Optional.empty());
	}
	
	public static SourceItem ScalarData(int value) {
		return new SourceItem(Optional.empty(), Optional.of(Value.Scalar(value)));
	}
	
	public static SourceItem VectorData(int[] value) {
		return new SourceItem(Optional.empty(), Optional.of(Value.Vector(value)));
	}
	
	private SourceItem(Optional<Integer> register, Optional<Value> data) {
		this.register = register;
		this.data = data;
	}
	public boolean isRegister() {
		return register.isPresent();
	}
	public boolean isDataValue() {
		return data.isPresent();
	}
	public int getRegisterNumber() {
		return register.get();
	}
	public Value getDataValue() {
		return data.get();
	}
	public String toString() {
		return register.isPresent()?"r"+register.get():"d" + data.get();
	}
}
