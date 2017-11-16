package saac.dataObjects;

import java.util.function.Function;

import saac.utils.Instructions.Opcode;

public class Instruction {
	
	private final int instructionNumber;
	private final Opcode opcode;
	//register numbers
	private final int paramA; 
	private final int paramB;
	private final int paramC;
	
	public Instruction(int instructionNumber, Opcode opcode, int paramA, int paramB, int paramC) {
		this.instructionNumber = instructionNumber;
		this.opcode = opcode;
		this.paramA = paramA;
		this.paramB = paramB;
		this.paramC = paramC;
	}
	
	public int getID() {
		return instructionNumber;
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
		return new Instruction(this.instructionNumber, opcode.apply(this.opcode), target.apply(this.paramA),
				sourceA.apply(this.paramB), sourceB.apply(this.paramC));
	}
	
	public String toString() {
		return String.format("%d: Opcode: %s, params: (a,%d), (b,%d), (c,%d)", instructionNumber, opcode.toString(), paramA, paramB, paramC); 
	}
}
