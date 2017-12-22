package saac;

import static saac.utils.parsers.ParserUtils.either;
import static saac.utils.parsers.ParserUtils.number;
import static saac.utils.parsers.ParserUtils.padded;
import static saac.utils.parsers.ParserUtils.string;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import saac.utils.parsers.ParseResult;
import saac.utils.parsers.ParseSuccess;
import saac.utils.parsers.Parser;

public class Settings {
	
	public static enum IssueWindow {Aligned, Unaligned};
	public static IssueWindow ISSUE_WINDOW_METHOD = IssueWindow.Aligned; 
	
	public static enum BranchPrediction {Blocking, Simple_Static, Static, Dynamic};
	public static BranchPrediction BRANCH_PREDICTION_MODE = BranchPrediction.Dynamic;
	
	public static boolean RESERVATION_STATION_BYPASS_ENABLED = true;
	
	public static int NUMBER_OF_EXECUTION_UNITS = 4;
	
	public static int SUPERSCALER_WIDTH = 16;
	
	public static boolean OUT_OF_ORDER_ENABLED = true;
	
	public static int VIRTUAL_ADDRESS_NUM = 128;
	
	public static boolean REGISTER_RENAMING_ENABLED = true;
	
	public static int LOAD_LIMIT = 2;
	
	public static boolean LINK_BRANCH_PREDICTION = false;
	
	public static boolean CACHE_ENABLED = false;
	
	public static Supplier<Integer> PARALLEL_INSTRUCTION_FETCH = () -> 4 * SUPERSCALER_WIDTH;
		
	public static Supplier<Integer> RESERVATION_STATION_SIZE = () -> Math.max(SUPERSCALER_WIDTH, 16);
	
	static {
		try {
			List<String> lines = Files.readAllLines(new File("saac.conf").toPath());
			for(String line : lines) {
				getParam(line);
			}
		} catch (NoSuchFileException e) {
			//defaults
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void getParam(String line) {
		if(getBranchParam(line));
		else if(getAlignParam(line));
		else if(getBoolParam("RESERVATION_STATION_BYPASS_ENABLED", line, (b)->RESERVATION_STATION_BYPASS_ENABLED=b));
		else if(getNumberParam("NUMBER_OF_EXECUTION_UNITS", line, (x)->NUMBER_OF_EXECUTION_UNITS=x));
		else if(getNumberParam("SUPERSCALER_WIDTH", line, (x)->SUPERSCALER_WIDTH=x));
		else if(getBoolParam("OUT_OF_ORDER_ENABLED", line, (b)->OUT_OF_ORDER_ENABLED=b));
		else if(getNumberParam("VIRTUAL_ADDRESS_NUM", line, (x)->VIRTUAL_ADDRESS_NUM=x));
		else if(getBoolParam("REGISTER_RENAMING_ENABLED", line, (b)->REGISTER_RENAMING_ENABLED=b));
		else if(getNumberParam("LOAD_LIMIT", line, (x)->LOAD_LIMIT=x));
		else if(getBoolParam("LINK_BRANCH_PREDICTION", line, (b)->LINK_BRANCH_PREDICTION=b));
		else if(getBoolParam("CACHE_ENABLED", line, (b)->CACHE_ENABLED=b));
	}
	
	private static boolean getBranchParam(String line) {
		@SuppressWarnings("unchecked")
		ParseResult<BranchPrediction> res = padded(string("BRANCH_PREDICTION_MODE"))
				.thenSecond(padded(string("="))
				.thenSecond(padded(either(new Parser[] {
					string("Blocking").thenPure(BranchPrediction.Blocking),
					string("Simple_Static").thenPure(BranchPrediction.Simple_Static),
					string("Static").thenPure(BranchPrediction.Static),
					string("Dynamic").thenPure(BranchPrediction.Dynamic)
				})))).parse(line);
		if(res instanceof ParseSuccess) {
			BRANCH_PREDICTION_MODE = ((ParseSuccess<BranchPrediction>) res).value;
			return true;
		}
		return false;
	}
	
	private static boolean getAlignParam(String line) {
		ParseResult<IssueWindow> res =  padded(string("ISSUE_WINDOW_METHOD"))
				.thenSecond(padded(string("="))
				.thenSecond(padded(either(
						string("Aligned").thenPure(IssueWindow.Aligned),
						string("Unaligned").thenPure(IssueWindow.Unaligned)
					)))).parse(line);
		if(res instanceof ParseSuccess) {
			ISSUE_WINDOW_METHOD = ((ParseSuccess<IssueWindow>) res).value;
			return true;
		}
		return false;
	}
	
	private static boolean getBoolParam(String name, String line, Consumer<Boolean> f) {
		ParseResult<Boolean> res =  padded(string(name))
				.thenSecond(padded(string("="))
						.thenSecond(padded(either(
								string("true").thenPure(true),
								string("false").thenPure(false))))).parse(line);
		if(res instanceof ParseSuccess) {
			f.accept(((ParseSuccess<Boolean>) res).value);
			return true;
		}
		return false;
	}
	
	private static boolean getNumberParam(String name, String line, Consumer<Integer> f) {
		ParseResult<Integer> res = padded(string(name))
				.thenSecond(padded(string("="))
						.thenSecond(padded(number))).parse(line);
		if(res instanceof ParseSuccess) {
			f.accept(((ParseSuccess<Integer>) res).value);
			return true;
		}
		return false;
	}
}
