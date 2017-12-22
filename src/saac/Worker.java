package saac;

import saac.unclockedComponents.BranchPredictor;
import saac.utils.Output;
import saac.utils.RateUtils;

public class Worker {
    public static Thread worker;
	private static boolean finished = false;
	
	public static boolean isFinished() {
		return finished;
	}
	
	public static void finish() {
		finished = true;
		Output.final_state.println(RateUtils.getRate(Saac.InstructionCounter, Saac.CycleCounter)
		+ ", Count: " + Integer.toString(Saac.InstructionCounter)
		+ ", Branch Prediction " + RateUtils.getRate(BranchPredictor.totalBinaryCorrectlyPredicted, BranchPredictor.totalBinaryBranches));
	}
	public static void init() {
		finished = false;
	}
}
