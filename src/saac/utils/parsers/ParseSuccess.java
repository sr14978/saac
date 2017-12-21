package saac.utils.parsers;

import java.util.function.Function;

public class ParseSuccess<T> extends ParseResult<T> {
	
	public final T value;
	final String left;
	
	public ParseSuccess(T value, String leftover) {
		this.value = value;
		this.left = leftover;
	}
	
	public String toString() { return "(" + value.toString() + ", " + left + ")"; }
	
	public <H> ParseResult<H> thenSecond(Function<String, ParseResult<H>> function) {
		return function.apply(left);
	}
	
	public <H> ParseResult<T> thenFirst(Function<String, ParseResult<H>> function) {
		if(function.apply(left) instanceof ParseFail)
			return new ParseFail<T>();
		else
			return this;
	}
	
	public <H> ParseResult<H> thenWith(Function<T,Function<String, ParseResult<H>>> function) {
		return function.apply(value).apply(left);
	}
	
}
