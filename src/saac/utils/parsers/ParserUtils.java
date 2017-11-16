package saac.utils.parsers;

import java.util.ArrayList;
import java.util.List;

public class ParserUtils {
	
	public static void main(String[] args) {
		System.out.println(number.parse("12"));	
	}
		
	static <T> Parser<T> pure(T item) {
		return new Parser<T>((String line) -> new ParseSuccess<T>(item, line));
	}
	
	static <T> Parser<T> fail() {
		return new Parser<T>((String line) -> new ParseFail<T>());
	}
	
	static <T> Parser<T> either(Parser<T> a, Parser<T> b) {
		return new Parser<T>(
				(String st) -> {
					ParseResult<T> instruction = a.parse(st);
					if(instruction instanceof ParseSuccess)
						return instruction;
					else
						return b.parse(st);
				}
			);
	}
	
	static <T> Parser<T> either(List<Parser<T>> ps) {
		return new Parser<T>(
				(String st) -> {
					for(Parser<T> p : ps) {
						ParseResult<T> instruction = p.parse(st);
						if(instruction instanceof ParseSuccess)
							return instruction;
					}
					return new ParseFail<T>();
					}
				);
	}
	
	static Parser<String> string(String s) {
		return new Parser<String>(
				(String line) -> {
						if(line.startsWith(s)) {
							return new ParseSuccess<String>(s, line.substring(s.length()));
						} else {
							return new ParseFail<String>();
						}
					}
				);
	}
	
	static <T> Parser<T> padded(Parser<T> p) {
		return p.thenFirst(whitespace);
	}
    
	static Parser<String> anyToken = new Parser<String>(
			(String line) -> {
				int i = 0;
				while(i < line.length() && line.charAt(i) != ' ' ) { i++; }
				return new ParseSuccess<String>(line.substring(0,i), line.substring(i));
			}
		);
	
	static Parser<String> whitespace = new Parser<String>(
			(String line) -> {
				int i = 0;
				while(i < line.length() && line.charAt(i) == ' ' ) { i++; }
				return new ParseSuccess<String>(line.substring(0,i), line.substring(i));
			}
		);
	
	static Parser<Integer> decimalNumber = anyToken.thenWith( (String s) -> {
		try {
			int i = Integer.parseInt(s);
			return pure(i);
		} catch (NumberFormatException e) {
			return fail();
		}
	});
	
	static Parser<Integer> hexNumber = string("0x").thenSecond(anyToken.thenWith( (String s) -> {
		try {
			int i = Integer.parseInt(s, 16);
			return pure(i);
		} catch (NumberFormatException e) {
			return fail();
		}
	}));
	
	static Parser<Integer> number = either(decimalNumber, hexNumber);
}
