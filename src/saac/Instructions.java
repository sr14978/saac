package saac;

import java.util.HashMap;
import java.util.Map;


public class Instructions {
	enum Opcode {
		Nop,	// Nop	.  .  .  -> 
		Ldc, 	// Ldc 	rI #n .  -> rI = #n  
		Add, 	// Add 	rI rJ rK -> rI = rJ + rK
		Addi, 	// Addi	rI rJ #n -> rI = rJ + #n
		Sub, 	// Sub	rI rJ rK -> rI = rJ - rK
		Subi, 	// Subi	rI rJ #n -> rI = rJ - #n
		Mul, 	// Mul	rI rJ rK -> rI = rJ * rK
		Muli, 	// Muli	rI rJ #n -> rI = rJ * #n
		Div, 	// Div	rI rJ rK -> rI = rJ / rK
		Divi, 	// Divi	rI rJ #n -> rI = rJ / #n
		Ldma,	// Ldma	rI #n .  -> rI = mem[#m] 
		Stma,	// Stma	rI #n .  -> mem[#m] = rI 
		Ldmi,	// Ldmi	rI rJ rK -> rI = mem[rJ + rk] 
		Stmi,	// Stmi	rI rJ rK -> mem[rJ + rk] = rI 
		Br,		// Br 	#n .  .  -> pc = #n
		Jmp,	// Jmp  #n .  .  -> pc = pc + #n
		JmpZ,	// Jmp  #n rI .  -> pc = if rI=0 then pc + #n else pc
		JmpN	// Jmp  #n rI .  -> pc = if rI<0 then pc + #n else pc
	}
	static Map<Opcode, Integer> InstructionDelay = new HashMap<Opcode, Integer>(){
		private static final long serialVersionUID = 1L;
	{
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
