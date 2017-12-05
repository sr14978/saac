package saac.dataObjects;

import java.util.function.Function;

import saac.utils.Instructions.Opcode;

public class FilledInInstruction implements InstructionI{
	
	private final int instructionNumber;
	private final Opcode opcode;
	//register numbers
	private final int paramA; 
	private final int paramB;
	private final int paramC;
	private final int paramD;
	
	public FilledInInstruction(int instructionNumber, Opcode opcode, int paramA, int paramB, int paramC, int paramD) {
		this.instructionNumber = instructionNumber;
		this.opcode = opcode;
		this.paramA = paramA;
		this.paramB = paramB;
		this.paramC = paramC;
		this.paramD = paramD;
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
	
	public int getParamD() {
		return paramD;
	}
	
	public FilledInInstruction transform(Function<Opcode, Opcode> opcode, Function<Integer, Integer> target,
			Function<Integer, Integer> sourceA, Function<Integer, Integer> sourceB) {
		return transformOp(opcode).transformParamA(target).transformParamB(sourceA).transformParamC(sourceB);
	}
		
	public FilledInInstruction transformOp(Function<Opcode, Opcode> f) {
		return new FilledInInstruction(this.instructionNumber, f.apply(this.opcode), this.paramA, this.paramB, this.paramC, paramD);
	}
	
	public FilledInInstruction transformParamA(Function<Integer, Integer> f) {
		return new FilledInInstruction(this.instructionNumber, this.opcode, f.apply(this.paramA), this.paramB, this.paramC, paramD);
	}
	
	public FilledInInstruction transformParamB(Function<Integer, Integer> f) {
		return new FilledInInstruction(this.instructionNumber, this.opcode, this.paramA, f.apply(this.paramB), this.paramC, paramD);
	}
	
	public FilledInInstruction transformParamC(Function<Integer, Integer> f) {
		return new FilledInInstruction(this.instructionNumber, this.opcode, this.paramA, this.paramB, f.apply(this.paramC), paramD);
	}
	
	public FilledInInstruction transformParamD(Function<Integer, Integer> f) {
		return new FilledInInstruction(this.instructionNumber, this.opcode, this.paramA, this.paramB, this.paramC, f.apply(paramD));
	}
	
	public String toString() {
		return String.format("%d: Opcode: %s, params: (a,%d), (b,%d), (c,%d)", instructionNumber, opcode.toString(), paramA, paramB, paramC); 
	}
}
