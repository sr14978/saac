package saac.dataObjects;

import java.util.function.Function;

import saac.utils.Instructions.Opcode;

public class VirtualInstruction implements InstructionI{
	
	private final int instructionNumber;
	private final Opcode opcode;
	//register numbers
	private final int paramA_v;
	private final int paramA_a;
	private final int paramB_v;
	private final int paramB_a;
	private final int paramC_v;
	private final int paramC_a;
	private final int paramD_v;
	private final int paramD_a;
	
	public VirtualInstruction(int instructionNumber,Opcode opcode,
			int paramA_v, int paramA_a, int paramB_v, int paramB_a, int paramC_v, int paramC_a, int paramD_v, int paramD_a) {
		this.instructionNumber = instructionNumber;
		this.opcode = opcode;
		this.paramA_v = paramA_v;
		this.paramA_a = paramA_a;
		this.paramB_v = paramB_v;
		this.paramB_a = paramB_a;
		this.paramC_v = paramC_v;
		this.paramC_a = paramC_a;
		this.paramD_v = paramD_v;
		this.paramD_a = paramD_a;
	}
	
	public int getID() {
		return instructionNumber;
	}
	
	public Opcode getOpcode() {
		return opcode;
	}
	
	public int getVirtualParamA() {
		return paramA_v;
	}
	
	public int getVirtualParamB() {
		return paramB_v;
	}

	public int getVirtualParamC() {
		return paramC_v;
	}
	
	public int getVirtualParamD() {
		return paramD_v;
	}
	
	public int getArchParamA() {
		return paramA_a;
	}
	
	public int getArchParamB() {
		return paramB_a;
	}

	public int getArchParamC() {
		return paramC_a;
	}
	
	public int getArchParamD() {
		return paramD_a;
	}
	
	public FilledInInstruction fillIn(Function<Integer, Integer> a, Function<Integer, Integer> b, Function<Integer, Integer> c, Function<Integer, Integer> d) {
		return new FilledInInstruction(instructionNumber, opcode, a.apply(paramA_a), b.apply(paramB_a), c.apply(paramC_a), d.apply(paramD_a));
	}
	
	public String toString() {
		return String.format("%d: Op: %s, (a,%d|%d), (b,%d|%d), (c,%d|%d)", instructionNumber, opcode.toString(),
				paramA_a, paramA_v, paramB_a, paramB_v, paramC_a, paramC_v); 
	}
}
