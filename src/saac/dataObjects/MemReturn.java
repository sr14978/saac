package saac.dataObjects;

import java.util.Optional;
import saac.utils.Instructions;

public class MemReturn {
	int delay;
	public int getDelay() {
		return delay;
	}
	public void setDelay(int delay) {
		this.delay = delay;
	}
	public Optional<Integer> getValue() {
		return value;
	}
	public void setValue(Optional<Integer> value) {
		this.value = value;
	}
	Optional<Integer> value;
	MemReturn(int delay, Optional<Integer> value) {
		this.delay = delay;
		this.value = value;
	}
	public static MemReturn Cache(Optional<Integer> value) {
		return new MemReturn(1, value);
	}
	public static MemReturn Memory(Optional<Integer> value, boolean hadToEvict) {
		return new MemReturn(
				Instructions.InstructionDelay.get(Instructions.Opcode.Stma) * (hadToEvict?2:1),
				value);
	}
	
	public String toString() {
		return "delay: " + Integer.toString(delay) + ", value: " + value.toString();
	}
	
}