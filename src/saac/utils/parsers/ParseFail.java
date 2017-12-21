package saac.utils.parsers;

import java.util.function.Function;

class ParseFail<T> extends ParseResult<T> {
	
	public <H> ParseResult<H> thenSecond(Function<String, ParseResult<H>> function) {
		return new ParseFail<H>();
	}
	
	public <H> ParseResult<T> thenFirst(Function<String, ParseResult<H>> function) {
		return this;
	}
	
	public <H> ParseResult<H> thenWith(Function<T,Function<String, ParseResult<H>>> function) {
		return new ParseFail<H>();
	}
	
	public String toString() { return "Parser Failed"; }
	
}