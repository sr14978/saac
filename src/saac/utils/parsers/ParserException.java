package saac.utils.parsers;

@SuppressWarnings("serial")
public class ParserException extends Exception {
	public ParserException(String line) {
		super(line);
	}
}
