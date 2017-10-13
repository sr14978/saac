package saac;

import saac.Instructions.Opcode;

public class InstructionsSource {
	static final byte[][] instructions = new byte[][]{
			new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 42, 0},
			new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 42, 0},
			new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 42, 0},
			new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 42, 0},
			new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 42, 0},
			new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 42, 0},
			new byte[] {(byte) Opcode.Jmp.ordinal(), 2, 0, 0},
			new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 42, 0},
			new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 42, 0},
			new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 42, 0},
			new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 42, 0},
		};
	
	static byte[] getInstruction(int addr) {
		if(addr < instructions.length && addr >= 0)
			return instructions[addr];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
}
