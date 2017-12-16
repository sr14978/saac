package saac.dataObjects.Instruction.Complete;

import java.util.Optional;
import java.util.function.Consumer;

import saac.dataObjects.Instruction.Instruction;
import saac.dataObjects.Instruction.Partial.DestItem;
import saac.dataObjects.Instruction.Partial.PartialInstruction;
import saac.dataObjects.Instruction.Partial.SourceItem;
import saac.utils.Instructions.Opcode;

public class CompleteInstruction extends Instruction<Optional<DestItem>, Optional<Integer>>{
		
	private final int instructionNumber;
	private final Opcode opcode;

	private final Optional<DestItem> dest; 
	private Optional<Integer> paramA;
	private Optional<Integer> paramB;
	private Optional<Integer> paramC;
	
	public CompleteInstruction(int instructionNumber, Opcode opcode, Optional<DestItem> dest, Optional<Integer> paramA, Optional<Integer> paramB, Optional<Integer> paramC) {
		this.instructionNumber = instructionNumber;
		this.opcode = opcode;
		this.dest = dest;
		this.paramA = paramA;
		this.paramB = paramB;
		this.paramC = paramC;
	}
	
	public CompleteInstruction(PartialInstruction p) throws Exception {
		this.instructionNumber = p.getVirtualNumber();
		this.opcode = p.getOpcode();
		this.dest = p.getDest();
		setParam(p.getParamA(), this::setParamA);
		setParam(p.getParamB(), this::setParamB);
		setParam(p.getParamC(), this::setParamC);
	}
	
	private void setParam(Optional<SourceItem> param, Consumer<Optional<Integer>> setter) throws Exception {
		if(!param.isPresent()) {
			setter.accept(Optional.empty());
		} else {
			SourceItem i = param.get();
			if(i.isDataValue()) {
				setter.accept(Optional.of(i.getValue()));
			} else {
				throw new Exception("Instruction not Complete");
			}
		}
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
	
	public Optional<Integer> getParamA() {
		return paramA;
	}

	public Optional<Integer> getParamB() {
		return paramB;
	}

	public Optional<Integer> getParamC() {
		return paramC;
	}
	
	private void setParamA(Optional<Integer> a) {
		paramA = a;
	}

	private void setParamB(Optional<Integer> b) {
		paramB = b;
	}

	private void setParamC(Optional<Integer> c) {
		paramC = c;
	}
	
	public String toString() {
		return String.format("%d: %s = %s (%s, %s, %s)", instructionNumber, dest, opcode.toString(), paramA, paramB, paramC); 
	}
}
