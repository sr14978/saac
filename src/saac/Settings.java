package saac;

import java.util.function.Supplier;

public class Settings {
	
	public static enum IssueWindow {Aligned, Unaligned};
	public static IssueWindow ISSUE_WINDOW_METHOD = IssueWindow.Unaligned; 
	
	public static enum BranchPrediciton {Blocking, Simple_Static, Static, Dynamic};
	public static BranchPrediciton BRANCH_PREDICTION_MODE = BranchPrediciton.Simple_Static;
	
	public static boolean RESERVATION_STATION_BYPASS_ENABLED = false;
	
	public static int NUMBER_OF_EXECUTION_UNITS = 1;
	
	public static int SUPERSCALER_WIDTH = 32;
	
	public static boolean OUT_OF_ORDER_ENABLED = true;
	
	public static int VIRTUAL_ADDRESS_NUM = 32;
	
	public static boolean REGISTER_RENAMING_ENABLED = false;
	
	public static int LOAD_LIMIT = 1;
	
	public static Supplier<Integer> PARALLEL_INSTRUCTION_FETCH = () -> 4 * SUPERSCALER_WIDTH;
		
	public static Supplier<Integer> RESERVATION_STATION_SIZE = () -> Math.max(SUPERSCALER_WIDTH, 16);
	
}
