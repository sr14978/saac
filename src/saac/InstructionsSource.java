package saac;

import saac.Instructions.Opcode;

public class InstructionsSource {
	static final byte[][] instructions = new byte[][]{
			new byte[] {(byte) Opcode.Ldc.ordinal(), 0, 5, 0},
			new byte[] {(byte) Opcode.Subi.ordinal(), 0, 0, 1},
			new byte[] {(byte) Opcode.JmpZ.ordinal(), 1, 0, 0},
			new byte[] {(byte) Opcode.Jmp.ordinal(), -3, 0, 0},
		};
	
	static byte[] getInstruction(int addr) {
		if(addr < instructions.length && addr >= 0)
			return instructions[addr];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
}
