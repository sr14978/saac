package saac;

public class Settings {
	public static enum BranchPrediciton {Blocking, Simple_Static, Static, Dynamic};
	public static BranchPrediciton BRANCH_PREDICTION_MODE = BranchPrediciton.Blocking;
	
	public static boolean RESERVATION_STATION_BYPASS_ENABLED = true;
	
	public static int NUMBER_OF_EXECUTION_UNITS = 1;
	
	public static int SUPERSCALER_WIDTH = 2;
	
	public static boolean OUT_OF_ORDER_ENABLED = true;
	
	public static int VIRTUAL_ADDRESS_NUM = 8;
	
	public static boolean REGISTER_RENAMING_ENABLED = true;
}

//Branch: Blocking, Bypass: true, EUs: 1, Width:2, OOO: true, VirtAdresses: 8, Renaming: true
