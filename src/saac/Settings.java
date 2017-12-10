package saac;

public class Settings {
	public static enum BranchPrediciton {Blocking, Simple_Static, Static, Dynamic};
	public static BranchPrediciton BRANCH_PREDICTION_MODE = BranchPrediciton.Dynamic;
	
	public static boolean RESERVATION_STATION_BYPASS_ENABLED = false;
	
	public static int NUMBER_OF_EXECUTION_UNITS = 4;
	
	public static int SUPERSCALER_WIDTH = 4;
	
	public static boolean OUT_OF_ORDER_ENABLED = false;
	
	public static int VIRTUAL_ADDRESS_NUM = 8;
	
	public static boolean REGISTER_RENAMING_ENABLED = true;
		
}

//3 false true false 4 8

/*
 Settings.NUMBER_OF_EXECUTION_UNITS,
						Settings.OUT_OF_ORDER_ENABLED,
						Settings.REGISTER_RENAMING_ENABLED,
						Settings.RESERVATION_STATION_BYPASS_ENABLED,
						Settings.SUPERSCALER_WIDTH,
						Settings.VIRTUAL_ADDRESS_NUM
 */
