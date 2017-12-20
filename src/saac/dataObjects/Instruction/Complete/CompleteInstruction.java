package saac.dataObjects.Instruction.Complete;

import java.util.Optional;
import java.util.function.Consumer;

import saac.dataObjects.Instruction.Instruction;
import saac.dataObjects.Instruction.Value;
import saac.dataObjects.Instruction.Partial.DestItem;
import saac.dataObjects.Instruction.Partial.PartialInstruction;
import saac.dataObjects.Instruction.Partial.SourceItem;
import saac.utils.Instructions.Opcode;

public class CompleteInstruction extends Instruction<Optional<DestItem>, Optional<Value>>{
		
	private final int instructionNumber;
	private final Opcode opcode;

	private final Optional<DestItem> dest; 
	private Optional<Value> paramA;
	private Optional<Value> paramB;
	private Optional<Value> paramC;
	private Optional<Value> paramD;
	
	public CompleteInstruction(int instructionNumber, Opcode opcode, Optional<DestItem> dest,
			Optional<Value> paramA,
			Optional<Value> paramB,
			Optional<Value> paramC,
			Optional<Value> paramD) {
		this.instructionNumber = instructionNumber;
		this.opcode = opcode;
		this.dest = dest;
		this.paramA = paramA;
		this.paramB = paramB;
		this.paramC = paramC;
		this.paramD = paramD;
	}
	
	public CompleteInstruction(PartialInstruction p) throws Exception {
		this.instructionNumber = p.getVirtualNumber();
		this.opcode = p.getOpcode();
		this.dest = p.getDest();
		setParam(p.getParamA(), this::setParamA);
		setParam(p.getParamB(), this::setParamB);
		setParam(p.getParamC(), this::setParamC);
		setParam(p.getParamD(), this::setParamD);
	}
	
	private void setParam(Optional<SourceItem> param, Consumer<Optional<Value>> setter) throws Exception {
		if(!param.isPresent()) {
			setter.accept(Optional.empty());
		} else {
			SourceItem i = param.get();
			if(i.isDataValue()) {
				setter.accept(Optional.of(i.getDataValue()));
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
	
	public Optional<Value> getParamA() {
		return paramA;
	}

	public Optional<Value> getParamB() {
		return paramB;
	}

	public Optional<Value> getParamC() {
		return paramC;
	}
	
	public Optional<Value> getParamD() {
		return paramD;
	}
	
	private void setParamA(Optional<Value> a) {
		paramA = a;
	}

	private void setParamB(Optional<Value> b) {
		paramB = b;
	}

	private void setParamC(Optional<Value> c) {
		paramC = c;
	}
	
	public void setParamD(Optional<Value> d) {
		paramD = d;
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
