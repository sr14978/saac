package saac.dataObjects.Instruction.Partial;

import java.util.Optional;

import saac.utils.Instructions.Opcode;

public class PartialLSInstruction extends PartialInstruction {

	Optional<Integer> waitedForId;
	public void clearWaitedForId() {
		this.waitedForId = Optional.empty();
	}

	public Optional<Integer> getWaitedForId() {
		return waitedForId;
	}

	public PartialLSInstruction(int instructionNumber, Opcode opcode, Optional<DestItem> dest,
			Optional<SourceItem> paramA, Optional<SourceItem> paramB, Optional<SourceItem> paramC,
			Optional<SourceItem> paramD, Optional<Integer> waitedForId) {
		super(instructionNumber, opcode, dest, paramA, paramB, paramC, paramD);
		this.waitedForId = waitedForId;
	}
	
	public PartialLSInstruction(PartialInstruction inst, Optional<Integer> waitedForId) {
		super(inst.getVirtualNumber(), inst.getOpcode(), inst.getDest(), inst.getParamA(), inst.getParamB(), inst.getParamC(), inst.getParamD());
		this.waitedForId = waitedForId;
	}

	public String toString() {
		return super.toString() + (waitedForId.isPresent()?"("+waitedForId.get().toString() + ")":":-)");
	}
	
}
