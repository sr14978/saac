package saac.utils;

import java.util.HashMap;
import java.util.Map;


public class Instructions {
	
	/*
	 * All register values are 4 byte signed integers
	 */
	public enum Opcode {
		Nop,	// nop	 .  .  .  -> 
		Ldc, 	// ldc 	 rI #n .  -> rI = #n 
		Add, 	// add 	 rI rJ rK -> rI = rJ + rK
		Addi, 	// addi	 rI rJ #n -> rI = rJ + #n
		Sub, 	// Ssub	 rI rJ rK -> rI = rJ - rK
		Subi, 	// subi	 rI rJ #n -> rI = rJ - #n
		Mul, 	// mul	 rI rJ rK -> rI = rJ * rK
		vMul,	// vmul	 rI rJ rK -> rI[i] = rJ[i] * rK[i] for i in [0..3]
		Muli, 	// muli	 rI rJ #n -> rI = rJ * #n
		Div, 	// div	 rI rJ rK -> rI = rJ / rK
		Divi, 	// divi	 rI rJ #n -> rI = rJ / #n
		Ldma,	// ldma	 rI #n .  -> rI = mem[#m] 
		Stma,	// stma	 rI #n .  -> mem[#m] = rI 
		Ldmi,	// ldmi	 rI rJ rK -> rI = mem[rJ + rk] 
		Stmi,	// stmi	 rI rJ rK -> mem[rJ + rk] = rI 
		Br,  	// br 	 #n .  .  -> pc = #n
		Brr,  	// brr 	 rK .  .  -> pc = rK
		Jmp,	// jmp   #n .  .  -> pc = pc + #n
		JmpC,	// jmpc  #n rI .  -> pc = if rI!=0 then pc + #n else pc
		Ldpc,	// ldpc  rI #n .  -> rI = pc + 1 + #n
		And,	// and   rI rJ rK -> rI = rJ and rK
		Or,		// or    rI rJ rK -> rI = rJ or rK
		Not,	// not   rI rJ .  -> rI = not rJ
		Lteq,	// lteq  rI rJ rK -> rI = 1 if rJ <= rK else 0
		Eq,		// eq    rI rJ rK -> rI = 1 if rJ == rK else 0
		vLdc,   // vldc  rI #a #b #c #d -> rI[0]=#a, rI[1]=#b, rI[2]=#c, rI[3]=#d
		vLdmi,	// vldmi rI rJ rK -> rI[i]=mem[rJ+rk+i] for i in [0..3]
		vStmi,	// vstmi rI rJ rK -> mem[rJ+rk+i]=rI[i] for i in [0..3]
		vSum,	// vsum  rI rJ .  -> rI = rJ[0]+rJ[1]+rJ[2]+rJ[3] 
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
			case 0x7: return Muli;
			case 0x8: return Div;
			case 0x9: return Divi;
			case 0xA: return Ldma;
			case 0xB: return Stma;
			case 0xC: return Ldmi;
			case 0xD: return Stmi;
			case 0xE: return Br;
			case 0xF: return Brr;
			case 0x10: return Jmp;
			case 0x11: return JmpC;
			case 0x12: return Ldpc;
			case 0x13: return Stop;
			case 0x14: return And;
			case 0x15: return Or;
			case 0x16: return Not;
			case 0x17: return Lteq;
			case 0x18: return Eq;
			case 0x19: return vMul;
			case 0x1A: return vLdc;
			case 0x1B: return vLdmi;
			case 0x1C: return vStmi;
			case 0x1D: return vSum;
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
			case Muli: return 0x7;
			case Div: return 0x8;
			case Divi: return 0x9;
			case Ldma: return 0xA;
			case Stma: return 0xB;
			case Ldmi: return 0xC;
			case Stmi: return 0xD;
			case Br: return 0xE;
			case Brr: return 0xF;
			case Jmp: return 0x10;
			case JmpC: return 0x11;
			case Ldpc: return 0x12;
			case Stop: return 0x13;
			case And: return 0x14;
			case Or: return 0x15;
			case Not: return 0x16;
			case Lteq: return 0x17;
			case Eq: return 0x18;
			case vMul: return 0x19;
			case vLdc: return 0x1A;
			case vLdmi: return 0x1B;
			case vStmi: return 0x1C;
			case vSum: return 0x1D;
			default: throw new NotImplementedException();
			}
		}
	}
	public static Map<Opcode, Integer> InstructionDelay = new HashMap<Opcode, Integer>() {
		private static final long serialVersionUID = 1L;
	{
		put(Opcode.Nop, 0);
		put(Opcode.And, 0);
		put(Opcode.Or, 0);
		put(Opcode.Not, 0);
		put(Opcode.Lteq, 0);
		put(Opcode.Eq, 0);
		put(Opcode.Ldpc, 0);
		put(Opcode.Stop, 0);
		put(Opcode.Ldc, 0);
		put(Opcode.vLdc, 0);
		put(Opcode.Add, 0);
		put(Opcode.Addi, 0);
		put(Opcode.vSum, 0);
		put(Opcode.Sub, 0);
		put(Opcode.Subi, 0);
		put(Opcode.Mul, 2);
		put(Opcode.vMul, 2);
		put(Opcode.Muli, 2);
		put(Opcode.Div, 10);
		put(Opcode.Divi, 10);
		put(Opcode.Ldma, /*20*/10);
		put(Opcode.Stma, /*20*/10);
		put(Opcode.Ldmi, /*20*/10);
		put(Opcode.Stmi, /*20*/10);
		put(Opcode.vLdmi, /*20*/10);
		put(Opcode.vStmi, /*20*/10);
	}};
}
