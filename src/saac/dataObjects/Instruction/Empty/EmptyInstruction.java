package saac.dataObjects.Instruction.Empty;

import java.util.Optional;
import saac.dataObjects.Instruction.Instruction;
import saac.dataObjects.Instruction.Register;
import saac.utils.Instructions.Opcode;

public class EmptyInstruction extends Instruction<Optional<Register>, Optional<Item>>{
		
	private final int instructionNumber;
	private final Opcode opcode;
	//register numbers
	private final Optional<Register> dest; 
	private final Optional<Item> paramA;
	private final Optional<Item> paramB;
	private final Optional<Item> paramC;
	private final Optional<Item> paramD;
	
	public EmptyInstruction(int instructionNumber, Opcode opcode, Optional<Register> dest,
			Optional<Item> paramA,
			Optional<Item> paramB,
			Optional<Item> paramC,
			Optional<Item> paramD) {
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
	
	public Optional<Register> getDest() {
		return dest;
	}
	
	public Optional<Item> getParamA() {
		return paramA;
	}

	public Optional<Item> getParamB() {
		return paramB;
	}

	public Optional<Item> getParamC() {
		return paramC;
	}
	
	public Optional<Item> getParamD() {
		return paramD;
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
