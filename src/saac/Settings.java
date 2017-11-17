package saac;

public class Settings {
	public static enum BranchPrediciton {Blocking, Simple_Static, Static, Dynamic};
	public static final BranchPrediciton BRANCH_PREDICTION_MODE = BranchPrediciton.Dynamic;
	
	public static final boolean RESERVATION_STATION_BYPASS_ENABLED = false;
	
	public static final int NUMBER_OF_EXECUTION_UNITS = 3;
}
