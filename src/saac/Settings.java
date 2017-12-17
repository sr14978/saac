package saac;

public class Settings {
	public static enum BranchPrediciton {Blocking, Simple_Static, Static, Dynamic};
	public static BranchPrediciton BRANCH_PREDICTION_MODE = BranchPrediciton.Simple_Static;
	
	public static boolean RESERVATION_STATION_BYPASS_ENABLED = false;
	
	public static int NUMBER_OF_EXECUTION_UNITS = 4;
	
	public static int SUPERSCALER_WIDTH = 4;
	
	public static boolean OUT_OF_ORDER_ENABLED = false;
	
	public static int VIRTUAL_ADDRESS_NUM = 16;
	
	public static boolean REGISTER_RENAMING_ENABLED = false;
		
	public static int PARALLEL_INSTRUCTION_FETCH = 4 * SUPERSCALER_WIDTH;
}
