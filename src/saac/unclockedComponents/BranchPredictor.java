package saac.unclockedComponents;

import saac.Settings;
import saac.Settings.BranchPrediciton;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class BranchPredictor {
	public boolean predict(int[] inst) {
		if(Settings.BRANCH_PREDICTION_MODE == BranchPrediciton.Simple_Static)
			return false;
		else if(Settings.BRANCH_PREDICTION_MODE == BranchPrediciton.Static)
			return inst[1] < 0;
		else if(Settings.BRANCH_PREDICTION_MODE == BranchPrediciton.Dynamic) {
			return true;
		}
		throw new NotImplementedException();
	}
}
