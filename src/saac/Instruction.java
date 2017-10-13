package saac;

import java.util.function.Function;

public class Instruction {
	enum Opcode {Nop, Ldc, Add, Addi}
	
	private Opcode opcode;
	//register numbers
	private int target; 
	private int sourceA;
	private int sourceB;
	
	Instruction(Opcode opcode, int target, int sourceA, int sourceB) {
		this.opcode = opcode;
		this.target = target;
		this.sourceA = sourceA;
		this.sourceB = sourceB;
	}
	
	Opcode getOpcode() {
		return opcode;
	}
	
	int getTarget() {
		return target;
	}

	int getSourceA() {
		return sourceA;
	}

	int getSourceB() {
		return sourceB;
	}
	
	Instruction transform(Function<Opcode, Opcode> opcode, 
			Function<Integer, Integer> target,
			Function<Integer, Integer> sourceA,
			Function<Integer, Integer> sourceB) {
		return new Instruction(opcode.apply(this.opcode),
				target.apply(this.target),
				sourceA.apply(this.sourceA),
				sourceB.apply(this.sourceB)
			);
	}
	
	public String toString() {
		return String.format("Opcode: %s, target: %d, source: %d, %d", opcode.toString(), target, sourceA, sourceB); 
	}
}
