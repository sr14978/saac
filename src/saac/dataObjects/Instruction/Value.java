package saac.dataObjects.Instruction;

import java.util.Arrays;
import java.util.Optional;

public class Value {
	Optional<Integer> scalar;
	Optional<int[]> vector;
	
	private Value(Optional<Integer> scalar, Optional<int[]> vector) {
		this.scalar = scalar;
		this.vector = vector;
	}
	
	public static Value Scalar(int value) {
		return new Value(Optional.of(value), Optional.empty());
	}
	public static Value Vector(int[] value) {
		return new Value(Optional.empty(), Optional.of(value));
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
	public int[] getVectorValues() {
		return vector.get();
	}
	public String toString() {
		return scalar.isPresent()?scalar.get().toString():Arrays.toString(vector.get());
	}
}
