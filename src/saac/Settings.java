package saac;

import java.util.StringTokenizer;
import java.util.function.Supplier;

public class Settings {
	
	public static enum IssueWindow {Aligned, Unaligned};
	public static IssueWindow ISSUE_WINDOW_METHOD = IssueWindow.Aligned; 
	
	public static enum BranchPrediciton {Blocking, Simple_Static, Static, Dynamic};
	public static BranchPrediciton BRANCH_PREDICTION_MODE = BranchPrediciton.Simple_Static;
	
	public static boolean RESERVATION_STATION_BYPASS_ENABLED = false;
	
	public static int NUMBER_OF_EXECUTION_UNITS = 1;
	
	public static int SUPERSCALER_WIDTH = 4;
	
	public static boolean OUT_OF_ORDER_ENABLED = true;
	
	public static int VIRTUAL_ADDRESS_NUM = 8;
	
	public static boolean REGISTER_RENAMING_ENABLED = true;
	
	public static int LOAD_LIMIT = 2;
	
	public static Supplier<Integer> PARALLEL_INSTRUCTION_FETCH = () -> 4 * SUPERSCALER_WIDTH;
		
	public static Supplier<Integer> RESERVATION_STATION_SIZE = () -> Math.max(SUPERSCALER_WIDTH, 16);
	
	static {
		String input = "Alignment: Aligned Branch: Simple_Static Bypass: false EUs: 1 Width: 1 OOO: false VirtAdresses: 8 Renaming: true LoadLimit 2";
		if(input.length()>0) {
			StringTokenizer st = new StringTokenizer(input, " ");
			st.nextToken();
			ISSUE_WINDOW_METHOD = IssueWindow.valueOf(st.nextToken());
			st.nextToken();
			BRANCH_PREDICTION_MODE = BranchPrediciton.valueOf(st.nextToken());
			st.nextToken();
			RESERVATION_STATION_BYPASS_ENABLED = Boolean.valueOf(st.nextToken());
			st.nextToken();
			NUMBER_OF_EXECUTION_UNITS = Integer.valueOf(st.nextToken());
			st.nextToken();
			SUPERSCALER_WIDTH = Integer.valueOf(st.nextToken());
			st.nextToken();
			OUT_OF_ORDER_ENABLED = Boolean.valueOf(st.nextToken());
			st.nextToken();
			VIRTUAL_ADDRESS_NUM = Integer.valueOf(st.nextToken());
			st.nextToken();
			REGISTER_RENAMING_ENABLED = Boolean.valueOf(st.nextToken());
			st.nextToken();
			LOAD_LIMIT = Integer.valueOf(st.nextToken());
		}
	}
}
