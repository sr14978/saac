package saac;

import java.util.HashMap;
import java.util.Map;


public class Instructions {
	enum Opcode {
		Nop,
		Ldc,
		Add,
		Addi,
		Sub,
		Subi,
		Mul,
		Muli,
		Div,
		Divi,
		Ldma,
		Stma,
		Ldmi,
		Stmi,
		Br,
		Jmp,
		JmpZ,
		JmpN
	}
	static Map<Opcode, Integer> InstructionDelay = new HashMap<Opcode, Integer>(){{
		put(Opcode.Nop, 0);
		put(Opcode.Ldc, 0);
		put(Opcode.Add, 0);
		put(Opcode.Addi, 0);
		put(Opcode.Sub, 0);
		put(Opcode.Subi, 0);
		put(Opcode.Mul, 2);
		put(Opcode.Muli, 2);
		put(Opcode.Div, 10);
		put(Opcode.Divi, 10);
		put(Opcode.Ldma, 20);
		put(Opcode.Stma, 20);
		put(Opcode.Ldmi, 20);
		put(Opcode.Stmi, 20);
	}};
}
