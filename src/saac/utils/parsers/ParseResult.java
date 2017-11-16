package saac.utils.parsers;

import java.util.function.Function;

abstract class ParseResult<T> {
	
	abstract <H> ParseResult<H> thenSecond(Function<String, ParseResult<H>> function);
	
	abstract <H> ParseResult<T> thenFirst(Function<String, ParseResult<H>> function);
	
	abstract <H> ParseResult<H> thenWith(Function<T,Function<String, ParseResult<H>>> function);
	
}