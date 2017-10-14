package saac;

import java.util.function.Function;

import saac.Instructions.Opcode;

public class Instruction {
	
	private Opcode opcode;
	//register numbers
	private int paramA; 
	private int paramB;
	private int paramC;
	
	Instruction(Opcode opcode, int paramA, int paramB, int paramC) {
		this.opcode = opcode;
		this.paramA = paramA;
		this.paramB = paramB;
		this.paramC = paramC;
	}
	
	Opcode getOpcode() {
		return opcode;
	}
	
	int getParamA() {
		return paramA;
	}

	int getParamB() {
		return paramB;
	}

	int getParamC() {
		return paramC;
	}
	
	Instruction transform(Function<Opcode, Opcode> opcode, Function<Integer, Integer> target,
			Function<Integer, Integer> sourceA, Function<Integer, Integer> sourceB) {
		return new Instruction(opcode.apply(this.opcode), target.apply(this.paramA),
				sourceA.apply(this.paramB), sourceB.apply(this.paramC));
	}
	
	public String toString() {
		return String.format("Opcode: %s, params: (a,%d), (b,%d), (c,%d)", opcode.toString(), paramA, paramB, paramC); 
	}
}
