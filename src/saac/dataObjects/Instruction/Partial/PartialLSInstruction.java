package saac.dataObjects.Instruction.Partial;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import saac.utils.Instructions.Opcode;

public class PartialLSInstruction extends PartialInstruction {

	List<Integer> waitedForId;
	public void clearWaitedForId() {
		this.waitedForId.clear();
	}

	public List<Integer> getWaitedForId() {
		return waitedForId;
	}

	public PartialLSInstruction(int instructionNumber, Opcode opcode, Optional<DestItem> dest,
			Optional<SourceItem> paramA, Optional<SourceItem> paramB, Optional<SourceItem> paramC,
			Optional<SourceItem> paramD, List<Integer> waitedForId) {
		super(instructionNumber, opcode, dest, paramA, paramB, paramC, paramD);
		this.waitedForId = waitedForId;
	}
	
	public PartialLSInstruction(PartialInstruction inst, List<Integer> waitedForId) {
		super(inst.getVirtualNumber(), inst.getOpcode(), inst.getDest(), inst.getParamA(), inst.getParamB(), inst.getParamC(), inst.getParamD());
		this.waitedForId = waitedForId;
	}
	
	public PartialLSInstruction(PartialInstruction inst) {
		super(inst.getVirtualNumber(), inst.getOpcode(), inst.getDest(), inst.getParamA(), inst.getParamB(), inst.getParamC(), inst.getParamD());
		this.waitedForId = new LinkedList<>();
	}
	
	public PartialLSInstruction(PartialInstruction inst, Integer waitedForId) {
		super(inst.getVirtualNumber(), inst.getOpcode(), inst.getDest(), inst.getParamA(), inst.getParamB(), inst.getParamC(), inst.getParamD());
		this.waitedForId = new LinkedList<>();
		this.waitedForId.add(waitedForId);
	}

	public String toString() {
		return super.toString() + " " + waitedForId.toString();
	}
	
}
