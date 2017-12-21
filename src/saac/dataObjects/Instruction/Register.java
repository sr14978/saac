package saac.dataObjects.Instruction;

import java.util.Optional;

public class Register {
	Optional<Integer> scalar;
	Optional<Integer> vector;
	
	private Register(Optional<Integer> scalar, Optional<Integer> vector) {
		this.scalar = scalar;
		this.vector = vector;
	}
	
	public static Register Scalar(int value) {
		return new Register(Optional.of(value), Optional.empty());
	}
	public static Register Vector(int value) {
		return new Register(Optional.empty(), Optional.of(value));
	}
	
	public boolean isScalar() {
		return scalar.isPresent();
	}
	public boolean isVector() {
		return vector.isPresent();
	}
	public int getScalarValue() {
		return scalar.get();
	}
	public int getVectorValues() {
		return vector.get();
	}
	public int getValue() {
		return vector.isPresent()?vector.get():scalar.get();
	}
	public String toString() {
		return scalar.isPresent()?scalar.get().toString():vector.get().toString();
	}
}
