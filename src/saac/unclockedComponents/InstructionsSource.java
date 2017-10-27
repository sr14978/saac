package saac.unclockedComponents;

import saac.utils.Instructions.Opcode;

public class InstructionsSource {
	static final int[][] instructions = new int[][]{
			/*0*/new int[] {Opcode.toInt(Opcode.Ldc), 0, 0, 0},
			/*1*/new int[] {Opcode.toInt(Opcode.Ldc), 1, 0, 0},
			/*2*/new int[] {Opcode.toInt(Opcode.Ldc), 2, 0x10, 0},
			/*3*/new int[] {Opcode.toInt(Opcode.Ldc), 3, 0x20, 0},
			/*4*/new int[] {Opcode.toInt(Opcode.Ldc), 7, 0x30, 0},
			/*5*/new int[] {Opcode.toInt(Opcode.Ldmi), 4, 2, 0},
			/*6*/new int[] {Opcode.toInt(Opcode.Ldmi), 5, 3, 0},
			/*7*/new int[] {Opcode.toInt(Opcode.Mul), 4, 4, 5},
			/*8*/new int[] {Opcode.toInt(Opcode.Stmi), 4, 7, 0},
			/*9*/new int[] {Opcode.toInt(Opcode.Add), 1, 1, 4},
			/*A*/new int[] {Opcode.toInt(Opcode.Addi), 0, 0, 1},
			/*B*/new int[] {Opcode.toInt(Opcode.Subi), 6, 0, 10},
			/*C*/new int[] {Opcode.toInt(Opcode.JmpZ), 1, 6, 0},
			/*D*/new int[] {Opcode.toInt(Opcode.Jmp), -9, 0, 0},
			/*F*/new int[] {Opcode.toInt(Opcode.Addi), 1, 1, 0},
			/*F*/new int[] {Opcode.toInt(Opcode.Jmp), -2, 0, 0},
		};
	
	public static int[] getInstruction(int addr) {
		if(addr < instructions.length && addr >= 0)
			return instructions[addr];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
}
