package saac.utils.parsers;

import static saac.utils.parsers.ParserUtils.number;
import static saac.utils.parsers.ParserUtils.padded;
import static saac.utils.parsers.ParserUtils.pure;
import static saac.utils.parsers.ParserUtils.string;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import saac.dataObjects.Instruction;
import saac.utils.Instructions.Opcode;

public class Parsers {

	public static void main(String[] args) throws IOException, ParserException {

		int[] item = parseInstruction("ldmi 0 1 5");
		System.out.println(new Instruction(Opcode.fromInt(item[0]), item[1], item[2], item[3]));

	}

	static Parser<int[]> nop = padded(string("nop")).thenSecond(pure(new int[] { Opcode.toInt(Opcode.Nop), 0, 0, 0 }));
		
	static Parser<int[]> unary(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
				padded(number).thenWith((a) ->
					pure(new int[] { Opcode.toInt(op), a, 0, 0 })));
	}
	
	static Parser<int[]> binary(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
					padded(number).thenWith((a) ->
						padded(number).thenWith((b) ->
							pure(new int[] { Opcode.toInt(op), a, b, 0 }))));
	}
	
	static Parser<int[]> tertiary(String inst, Opcode op) {
		return padded(string(inst)).thenSecond(
					padded(number).thenWith((a) -> 
						padded(number).thenWith((b) -> 
							padded(number).thenWith((c) -> 
								pure(new int[] { Opcode.toInt(op), a, b, c })))));
	}
	
	static List<Parser<int[]>> instructions = new ArrayList<Parser<int[]>>();
	static {
		instructions.add(nop);
		instructions.add(binary("ldc", Opcode.Ldc));
		instructions.add(tertiary("add", Opcode.Add));
		instructions.add(tertiary("addi", Opcode.Addi));
		instructions.add(tertiary("sub", Opcode.Sub));
		instructions.add(tertiary("subi", Opcode.Subi));
		instructions.add(tertiary("mul", Opcode.Mul));
		instructions.add(tertiary("muli", Opcode.Muli));
		instructions.add(tertiary("div", Opcode.Div));
		instructions.add(tertiary("divi", Opcode.Divi));
		instructions.add(binary("ldma", Opcode.Ldma));
		instructions.add(binary("stma", Opcode.Stma));
		instructions.add(tertiary("ldmi", Opcode.Ldmi));
		instructions.add(tertiary("stmi", Opcode.Stmi));
		instructions.add(unary("br", Opcode.Br));
		instructions.add(unary("ln", Opcode.Ln));
		instructions.add(unary("jmp", Opcode.Jmp));
		instructions.add(binary("jmpz", Opcode.JmpZ));
		instructions.add(binary("jmpn", Opcode.JmpN));
	}

	public static int[] parseInstruction(String line) throws ParserException {
		ParseResult<int[]> result = ParserUtils.either(instructions).parse(line);
		if (result instanceof ParseFail)
			throw new ParserException(String.format("'%s' is not an recognised instruction", line));

		return ((ParseSuccess<int[]>) result).value;
	}

}