package saac.dataObjects;

import java.util.function.Function;

import saac.utils.Instructions.Opcode;

public class Instruction {
	
	private Opcode opcode;
	//register numbers
	private int paramA; 
	private int paramB;
	private int paramC;
	
	public Instruction(Opcode opcode, int paramA, int paramB, int paramC) {
		this.opcode = opcode;
		this.paramA = paramA;
		this.paramB = paramB;
		this.paramC = paramC;
	}
	
	public Opcode getOpcode() {
		return opcode;
	}
	
	public int getParamA() {
		return paramA;
	}

	public int getParamB() {
		return paramB;
	}

	public int getParamC() {
		return paramC;
	}
	
	public Instruction transform(Function<Opcode, Opcode> opcode, Function<Integer, Integer> target,
			Function<Integer, Integer> sourceA, Function<Integer, Integer> sourceB) {
		return new Instruction(opcode.apply(this.opcode), target.apply(this.paramA),
				sourceA.apply(this.paramB), sourceB.apply(this.paramC));
	}
	
	public String toString() {
		return String.format("Opcode: %s, params: (a,%d), (b,%d), (c,%d)", opcode.toString(), paramA, paramB, paramC); 
	}
}
