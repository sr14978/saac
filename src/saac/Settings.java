package saac;

public class Settings {
	public static enum BranchPrediciton {Blocking, Simple_Static, Static, Dynamic};
	public static BranchPrediciton BRANCH_PREDICTION_MODE = BranchPrediciton.Simple_Static;
	
	public static boolean RESERVATION_STATION_BYPASS_ENABLED = true;
	
	public static int NUMBER_OF_EXECUTION_UNITS = 1;
	
	public static int SUPERSCALER_WIDTH = 4;
	
	public static boolean OUT_OF_ORDER_ENABLED = true;
	
	public static int VIRTUAL_ADDRESS_NUM = 32;
	
	public static boolean REGISTER_RENAMING_ENABLED = true;
		
	public static int PARALLEL_INSTRUCTION_FETCH = 4 * SUPERSCALER_WIDTH;
}
