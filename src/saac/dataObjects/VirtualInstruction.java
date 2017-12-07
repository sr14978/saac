package saac.dataObjects;

import java.util.function.Function;

import saac.clockedComponents.RegisterFile.Reg;
import saac.clockedComponents.RegisterFile.RegItem;
import saac.utils.Instructions.Opcode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class VirtualInstruction implements InstructionI{
	
	private final int instructionNumber;
	private final Opcode opcode;
	//register numbers
	private final RegItem paramA_v;
	private final int paramA_a;
	private final RegItem paramB_v;
	private final int paramB_a;
	private final RegItem paramC_v;
	private final int paramC_a;
	private final RegItem paramD_v;
	private final int paramD_a;
	
	public VirtualInstruction(int instructionNumber,Opcode opcode,
			RegItem paramA_v, int paramA_a, RegItem paramB_v, int paramB_a, RegItem paramC_v, int paramC_a, RegItem paramD_v, int paramD_a) {
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
	
	public RegItem getVirtualParamA() {
		return paramA_v;
	}
	
	public RegItem getVirtualParamB() {
		return paramB_v;
	}

	public RegItem getVirtualParamC() {
		return paramC_v;
	}
	
	public RegItem getVirtualParamD() {
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
		return String.format("%d: Op: %s, (a,%d|%d%c), (b,%d|%d%c), (c,%d|%d%c)", instructionNumber, opcode.toString(),
				paramA_a, paramA_v.value, typeToChar(paramA_v.type),
				paramB_a, paramB_v.value, typeToChar(paramB_v.type),
				paramC_a, paramC_v.value, typeToChar(paramC_v.type)
				); 
	}
	
	public static char typeToChar(Reg t) {
		switch(t) {
		case Virtual:
			return 'v';
		case Architectural:
			return 'a';
		case Data:
			return 'd';
		default:
			throw new NotImplementedException();
		}
	}
}
