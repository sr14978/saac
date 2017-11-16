package saac.utils.parsers;

import java.util.function.Function;
import static saac.utils.parsers.ParserUtils.pure;

public class Parser<T> {
	private final Function<String, ParseResult<T>> f;
	Parser(Function<String, ParseResult<T>> f) {
		this.f = f;
	}
	
	ParseResult<T> parse(String s) {
		return f.apply(s);
	}
	
	<H> Parser<H> thenWith(Function<T, Parser<H>> p) {
		return new Parser<H>(
				(String s) -> {
						ParseResult<T> res = f.apply(s);
						if(res instanceof ParseFail) {
							return new ParseFail<H>();
						} else {
							ParseSuccess<T> resSuccess = (ParseSuccess<T>) res;
							return p.apply(resSuccess.value).parse(resSuccess.left);
						}
					}
				);
	}
	
	<H> Parser<H> thenSecond(Parser<H> p) {
		return this.thenWith((any) -> p);
	}
	
	<H> Parser<T> thenFirst(Parser<H> p) {
		return this.thenWith((first) -> p.thenSecond(pure(first)));
	}
	
}