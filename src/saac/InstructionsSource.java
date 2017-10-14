package saac;

import saac.Instructions.Opcode;

public class InstructionsSource {
	static final byte[][] instructions = new byte[][]{
			/*0*/new byte[] {(byte) Opcode.Ldc.ordinal(), 0, 0, 0},
			/*1*/new byte[] {(byte) Opcode.Ldc.ordinal(), 1, 0, 0},
			/*2*/new byte[] {(byte) Opcode.Ldc.ordinal(), 2, 0x10, 0},
			/*3*/new byte[] {(byte) Opcode.Ldc.ordinal(), 3, 0x40, 0},
			/*4*/new byte[] {(byte) Opcode.Ldmi.ordinal(), 4, 2, 0},
			/*5*/new byte[] {(byte) Opcode.Ldmi.ordinal(), 5, 3, 0},
			/*6*/new byte[] {(byte) Opcode.Mul.ordinal(), 4, 4, 5},
			/*7*/new byte[] {(byte) Opcode.Add.ordinal(), 1, 1, 4},
			/*8*/new byte[] {(byte) Opcode.Addi.ordinal(), 0, 0, 1},
			/*9*/new byte[] {(byte) Opcode.Subi.ordinal(), 6, 0, 10},
			/*A*/new byte[] {(byte) Opcode.JmpZ.ordinal(), 1, 6, 0},
			/*B*/new byte[] {(byte) Opcode.Jmp.ordinal(), -8, 0, 0},
			/*C*/new byte[] {(byte) Opcode.Jmp.ordinal(), -1, 0, 0},
		};
	
	static byte[] getInstruction(int addr) {
		if(addr < instructions.length && addr >= 0)
			return instructions[addr];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
}
