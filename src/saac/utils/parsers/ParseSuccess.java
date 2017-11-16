package saac.utils.parsers;

import java.util.function.Function;

class ParseSuccess<T> extends ParseResult<T> {
	
	final T value;
	final String left;
	
	public ParseSuccess(T value, String leftover) {
		this.value = value;
		this.left = leftover;
	}
	
	public String toString() { return "(" + value.toString() + ", " + left + ")"; }
	
	<H> ParseResult<H> thenSecond(Function<String, ParseResult<H>> function) {
		return function.apply(left);
	}
	
	<H> ParseResult<T> thenFirst(Function<String, ParseResult<H>> function) {
		if(function.apply(left) instanceof ParseFail)
			return new ParseFail<T>();
		else
			return this;
	}
	
	<H> ParseResult<H> thenWith(Function<T,Function<String, ParseResult<H>>> function) {
		return function.apply(value).apply(left);
	}
	
}
