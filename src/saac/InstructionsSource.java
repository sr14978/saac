package saac;

import saac.Instructions.Opcode;

public class InstructionsSource {
	static final int[][] instructions = new int[][]{
			/*0*/new int[] {(int) Opcode.Ldc.ordinal(), 0, 0, 0},
			/*1*/new int[] {(int) Opcode.Ldc.ordinal(), 1, 0, 0},
			/*2*/new int[] {(int) Opcode.Ldc.ordinal(), 2, 0x10, 0},
			/*3*/new int[] {(int) Opcode.Ldc.ordinal(), 3, 0x20, 0},
			/*4*/new int[] {(int) Opcode.Ldc.ordinal(), 7, 0x30, 0},
			/*5*/new int[] {(int) Opcode.Ldmi.ordinal(), 4, 2, 0},
			/*6*/new int[] {(int) Opcode.Ldmi.ordinal(), 5, 3, 0},
			/*7*/new int[] {(int) Opcode.Mul.ordinal(), 4, 4, 5},
			/*8*/new int[] {(int) Opcode.Stmi.ordinal(), 4, 7, 0},
			/*9*/new int[] {(int) Opcode.Add.ordinal(), 1, 1, 4},
			/*A*/new int[] {(int) Opcode.Addi.ordinal(), 0, 0, 1},
			/*B*/new int[] {(int) Opcode.Subi.ordinal(), 6, 0, 10},
			/*C*/new int[] {(int) Opcode.JmpZ.ordinal(), 1, 6, 0},
			/*D*/new int[] {(int) Opcode.Jmp.ordinal(), -9, 0, 0},
			/*E*/new int[] {(int) Opcode.Jmp.ordinal(), -1, 0, 0},
		};
	
	static int[] getInstruction(int addr) {
		if(addr < instructions.length && addr >= 0)
			return instructions[addr];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
}
