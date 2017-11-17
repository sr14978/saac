package saac;

public class Settings {
	public static enum BranchPrediciton {Blocking, Simple_Static, Static, Dynamic};
	public static final BranchPrediciton BRANCH_PREDICTION_MODE = BranchPrediciton.Static;	
}
