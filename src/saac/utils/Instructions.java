package saac.utils;

import java.util.HashMap;
import java.util.Map;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class Instructions {
	public enum Opcode {
		Nop,	// nop	.  .  .  -> 
		Ldc, 	// ldc 	rI #n .  -> rI = #n  
		Add, 	// add 	rI rJ rK -> rI = rJ + rK
		Addi, 	// addi	rI rJ #n -> rI = rJ + #n
		Sub, 	// Ssub	rI rJ rK -> rI = rJ - rK
		Subi, 	// subi	rI rJ #n -> rI = rJ - #n
		Mul, 	// mul	rI rJ rK -> rI = rJ * rK
		Muli, 	// muli	rI rJ #n -> rI = rJ * #n
		Div, 	// div	rI rJ rK -> rI = rJ / rK
		Divi, 	// divi	rI rJ #n -> rI = rJ / #n
		Ldma,	// ldma	rI #n .  -> rI = mem[#m] 
		Stma,	// stma	rI #n .  -> mem[#m] = rI 
		Ldmi,	// ldmi	rI rJ rK -> rI = mem[rJ + rk] 
		Stmi,	// stmi	rI rJ rK -> mem[rJ + rk] = rI 
		Br,  	// br 	#n .  .  -> pc = #n
		Ln,  	// ln 	rK .  .  -> pc = rK
		Jmp,	// jmp  #n .  .  -> pc = pc + #n
		JmpZ,	// jmpz  #n rI .  -> pc = if rI=0 then pc + #n else pc
		JmpN,	// jmpn  #n rI .  -> pc = if rI<0 then pc + #n else pc
		Stop;
		
		public static Opcode fromInt(int code) {
			switch(code) {
			case 0x0: return Nop;
			case 0x1: return Ldc;
			case 0x2: return Add;
			case 0x3: return Addi;
			case 0x4: return Sub;
			case 0x5: return Subi;
			case 0x6: return Mul;
			case 0x7: return Div;
			case 0x8: return Divi;
			case 0x9: return Ldma;
			case 0xA: return Stma;
			case 0xB: return Ldmi;
			case 0xC: return Stmi;
			case 0xD: return Br;
			case 0xE: return Ln;
			case 0xF: return Jmp;
			case 0x10: return JmpZ;
			case 0x11: return JmpN;
			case 0x12: return Stop;
			default: throw new NotImplementedException();
			}
		}
		
		public static int toInt(Opcode op) {
			switch(op) {
			case Nop: return 0x0;
			case Ldc: return 0x1;
			case Add: return 0x2;
			case Addi: return 0x3;
			case Sub: return 0x4;
			case Subi: return 0x5;
			case Mul: return 0x6;
			case Div: return 0x7;
			case Divi: return 0x8;
			case Ldma: return 0x9;
			case Stma: return 0xA;
			case Ldmi: return 0xB;
			case Stmi: return 0xC;
			case Br: return 0xD;
			case Ln: return 0xE;
			case Jmp: return 0xF;
			case JmpZ: return 0x10;
			case JmpN: return 0x11;
			case Stop: return 0x12;
			default: throw new NotImplementedException();
			}
		}
	}
	public static Map<Opcode, Integer> InstructionDelay = new HashMap<Opcode, Integer>(){
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
