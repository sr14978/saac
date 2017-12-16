package saac.dataObjects.Instruction.Partial;

import java.util.Optional;

import saac.dataObjects.Instruction.Instruction;
import saac.utils.Instructions.Opcode;

public class PartialInstruction extends Instruction<Optional<DestItem>, Optional<SourceItem>>{
		
	private final int instructionNumber;
	private final Opcode opcode;
	//register numbers
	private final Optional<DestItem> dest; 
	private Optional<SourceItem> paramA;
	private Optional<SourceItem> paramB;
	private Optional<SourceItem> paramC;
	
	public PartialInstruction(int instructionNumber, Opcode opcode, Optional<DestItem> dest, Optional<SourceItem> paramA, Optional<SourceItem> paramB, Optional<SourceItem> paramC) {
		this.instructionNumber = instructionNumber;
		this.opcode = opcode;
		this.dest = dest;
		this.paramA = paramA;
		this.paramB = paramB;
		this.paramC = paramC;
	}
	
	public int getVirtualNumber() {
		return instructionNumber;
	}
	
	public Opcode getOpcode() {
		return opcode;
	}
	
	public Optional<DestItem> getDest() {
		return dest;
	}
	
	public Optional<SourceItem> getParamA() {
		return paramA;
	}

	public Optional<SourceItem> getParamB() {
		return paramB;
	}

	public Optional<SourceItem> getParamC() {
		return paramC;
	}
	
	public void setParamA(SourceItem a) {
		paramA = Optional.of(a);
	}

	public void setParamB(SourceItem b) {
		paramB = Optional.of(b);
	}

	public void setParamC(SourceItem c) {
		paramC = Optional.of(c);
	}
	
	public String toString() {
		return String.format("%d: %s = %s (%s, %s, %s)", instructionNumber, dest, opcode.toString(), paramA, paramB, paramC); 
	}
}
