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
	private Optional<SourceItem> paramD;
	
	public PartialInstruction(int instructionNumber, Opcode opcode,
			Optional<DestItem> dest, 
			Optional<SourceItem> paramA,
			Optional<SourceItem> paramB,
			Optional<SourceItem> paramC,
			Optional<SourceItem> paramD) {
		this.instructionNumber = instructionNumber;
		this.opcode = opcode;
		this.dest = dest;
		this.paramA = paramA;
		this.paramB = paramB;
		this.paramC = paramC;
		this.paramD = paramD;
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
	
	public Optional<SourceItem> getParamD() {
		return paramD;
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
	
	public void setParamD(SourceItem d) {
		paramD = Optional.of(d);
	}
	
	public String toString() {
		return String.format("%d: %s = %s (%s, %s, %s, %s)", instructionNumber,
				dest.isPresent()?dest.get():"/",
				opcode.toString(),
				paramA.isPresent()?paramA.get():"/",
				paramB.isPresent()?paramB.get():"/",
				paramC.isPresent()?paramC.get():"/",
				paramD.isPresent()?paramD.get():"/"); 
	}
}
