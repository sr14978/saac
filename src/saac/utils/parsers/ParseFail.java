package saac.utils.parsers;

import java.util.function.Function;

class ParseFail<T> extends ParseResult<T> {
	
	<H> ParseResult<H> thenSecond(Function<String, ParseResult<H>> function) {
		return new ParseFail<H>();
	}
	
	<H> ParseResult<T> thenFirst(Function<String, ParseResult<H>> function) {
		return this;
	}
	
	<H> ParseResult<H> thenWith(Function<T,Function<String, ParseResult<H>>> function) {
		return new ParseFail<H>();
	}
	
	public String toString() { return "Parser Failed"; }
	
}