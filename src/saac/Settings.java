package saac;

public class Settings {
	public static enum BranchPrediciton {Blocking, Simple_Static, Static, Dynamic};
	public static final BranchPrediciton BRANCH_PREDICTION_MODE = BranchPrediciton.Static;
	
	public static final boolean RESERVATION_STATION_BYPASS_ENABLED = true;
	
	public static final int NUMBER_OF_EXECUTION_UNITS = 2;
	
	public static final int SUPERSCALER_WIDTH = 4;
	
	public static final boolean OUT_OF_ORDER_ENABLED = true;
	
	public static final int VIRTUAL_ADDRESS_NUM = 16;
	
	public static final boolean REGISTER_RENAMING_ENABLED = true;
}
