package saac.dataObjects.Instruction.Complete;

import java.util.Optional;

import saac.dataObjects.Instruction.Instruction;
import saac.dataObjects.Instruction.Partial.DestItem;
import saac.utils.Instructions.Opcode;

public class CompleteInstruction extends Instruction<Optional<DestItem>, Optional<Integer>>{
		
	private final int instructionNumber;
	private final Opcode opcode;
	//register numbers
	private final Optional<DestItem> dest; 
	private final Optional<Integer> paramA;
	private final Optional<Integer> paramB;
	private final Optional<Integer> paramC;
	
	public CompleteInstruction(int instructionNumber, Opcode opcode, Optional<DestItem> dest, Optional<Integer> paramA, Optional<Integer> paramB, Optional<Integer> paramC) {
		this.instructionNumber = instructionNumber;
		this.opcode = opcode;
		this.dest = dest;
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
	
	public Optional<DestItem> getDest() {
		return dest;
	}
	
	public Optional<Integer> getParamA() {
		return paramA;
	}

	public Optional<Integer> getParamB() {
		return paramB;
	}

	public Optional<Integer> getParamC() {
		return paramC;
	}
	
	public String toString() {
		return String.format("%d: %s = %s (%s, %s, %s)", instructionNumber, dest, opcode.toString(), paramA, paramB, paramC); 
	}
}
