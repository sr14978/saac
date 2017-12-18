package saac.utils.parsers;

import static saac.utils.parsers.ParserUtils.number;
import static saac.utils.parsers.ParserUtils.decimalNumber;
import static saac.utils.parsers.ParserUtils.padded;
import static saac.utils.parsers.ParserUtils.pure;
import static saac.utils.parsers.ParserUtils.string;

import java.util.ArrayList;
import java.util.List;

import saac.utils.Instructions.Opcode;

public class Parsers {

	static Parser<int[]> constant(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(pure(new int[] { Opcode.toInt(op), 0, 0, 0, 0 }));
	}
		
	static Parser<int[]> unaryR(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
					string("r").thenSecond(padded(number)).thenWith((a) ->
						pure(new int[] { Opcode.toInt(op), a, 0, 0, 0 })));
	}
	
	static Parser<int[]> unaryN(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
				padded(number).thenWith((a) ->
					pure(new int[] { Opcode.toInt(op), a, 0, 0, 0 })));
	}
	
	static Parser<int[]> binaryRR(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
					string("r").thenSecond(padded(decimalNumber)).thenWith((a) ->
						string("r").thenSecond(padded(decimalNumber).thenWith((b) ->
							pure(new int[] { Opcode.toInt(op), a, b, 0, 0 })))));
	}
	
	static Parser<int[]> binaryRN(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
					string("r").thenSecond(padded(decimalNumber)).thenWith((a) ->
						padded(number).thenWith((b) ->
							pure(new int[] { Opcode.toInt(op), a, b, 0, 0 }))));
	}
	
	static Parser<int[]> binary_RN(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
					string("r").thenSecond(padded(decimalNumber)).thenWith((a) ->
						padded(number).thenWith((b) ->
							pure(new int[] { Opcode.toInt(op), 0, a, b, 0 }))));
	}
	
	static Parser<int[]> binaryNR(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
					padded(number).thenWith((a) ->
						string("r").thenSecond(padded(decimalNumber)).thenWith((b) ->
								pure(new int[] { Opcode.toInt(op), a, b, 0, 0 }))));
	}
		
	static Parser<int[]> tertiaryRRR(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
					string("r").thenSecond(padded(decimalNumber)).thenWith((rI) -> 
						string("r").thenSecond(padded(decimalNumber)).thenWith((rJ) -> 
							string("r").thenSecond(padded(decimalNumber)).thenWith((n) -> 
									pure(new int[] { Opcode.toInt(op), rI, rJ, n, 0 })))));
	}
	
	static Parser<int[]> tertiary_RRR(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
					string("r").thenSecond(padded(decimalNumber)).thenWith((rI) -> 
						string("r").thenSecond(padded(decimalNumber)).thenWith((rJ) -> 
							string("r").thenSecond(padded(decimalNumber)).thenWith((n) -> 
									pure(new int[] { Opcode.toInt(op), 0, rI, rJ, n })))));
	}
	
	static Parser<int[]> tertiaryRRN(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
					string("r").thenSecond(padded(decimalNumber)).thenWith((rI) -> 
						string("r").thenSecond(padded(decimalNumber)).thenWith((rJ) -> 
							padded(number).thenWith((n) -> 
								pure(new int[] { Opcode.toInt(op), rI, rJ, n, 0 })))));
	}
		
	static List<Parser<int[]>> instructions = new ArrayList<Parser<int[]>>();
	static {
		instructions.add(constant("nop", Opcode.Nop));
		instructions.add(constant("stop", Opcode.Stop));
		instructions.add(binaryRN("ldc", Opcode.Ldc));
		instructions.add(tertiaryRRR("add", Opcode.Add));
		instructions.add(tertiaryRRN("addi", Opcode.Addi));
		instructions.add(tertiaryRRR("sub", Opcode.Sub));
		instructions.add(tertiaryRRN("subi", Opcode.Subi));
		instructions.add(tertiaryRRR("mul", Opcode.Mul));
		instructions.add(tertiaryRRN("muli", Opcode.Muli));
		instructions.add(tertiaryRRR("div", Opcode.Div));
		instructions.add(tertiaryRRN("divi", Opcode.Divi));
		instructions.add(binaryRN("ldma", Opcode.Ldma));
		instructions.add(binary_RN("stma", Opcode.Stma));
		instructions.add(tertiaryRRR("ldmi", Opcode.Ldmi));
		instructions.add(tertiary_RRR("stmi", Opcode.Stmi));
		instructions.add(unaryN("br", Opcode.Br));
		instructions.add(unaryR("ln", Opcode.Ln));
		instructions.add(unaryN("jmp", Opcode.Jmp));
		instructions.add(binaryNR("jmpc", Opcode.JmpC));
		instructions.add(binaryRN("ldpc", Opcode.Ldpc));
		instructions.add(tertiaryRRR("and", Opcode.And));
		instructions.add(tertiaryRRR("or", Opcode.Or));
		instructions.add(tertiaryRRR("lteq", Opcode.Lteq));
		instructions.add(tertiaryRRR("eq", Opcode.Eq));
		instructions.add(binaryRR("not", Opcode.Not));
	}

	public static int[] parseInstruction(String line) throws ParserException {
		ParseResult<int[]> result = ParserUtils.either(instructions).parse(line);
		if (result instanceof ParseFail)
			throw new ParserException(String.format("'%s' is not an recognised instruction", line));

		return ((ParseSuccess<int[]>) result).value;
	}

}