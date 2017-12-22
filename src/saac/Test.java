package saac;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import saac.Settings.BranchPrediction;
import saac.Settings.IssueWindow;
import saac.clockedComponents.RegisterFile;
import saac.test.TestOutput;
import saac.utils.RateUtils;
import saac.utils.parsers.ParserException;

public class Test {
	
	static class Results {
		Map<Config, Float> results;
		float[][][][][][][][][] resultsArray;
		List<Config> failures;
		double failureRate;
		Results(Map<Config, Float> results, List<Config> failures, double failureRate, float[][][][][][][][][] resultsArray) {
			this.results = results;
			this.failures = failures;
			this.failureRate = failureRate;
			this.resultsArray = resultsArray;
		}
	}
	
	static class Control {
		volatile Float val = null;
	}
	
	public static void main(String[] args) throws Exception {		
		String programName = "inner_product_stop.program";
		if(args.length == 1) {
			programName = args[0];
		}
		System.out.println("Testing...");
		/*
		Results results = new Results(new HashMap<Test.Config, Float>(), new ArrayList<Test.Config>(), 0, null);
		results.results.put(new Config(Settings.ISSUE_WINDOW_METHOD, Settings.BRANCH_PREDICTION_MODE, Settings.RESERVATION_STATION_BYPASS_ENABLED, 
				Settings.NUMBER_OF_EXECUTION_UNITS, Settings.SUPERSCALER_WIDTH, Settings.OUT_OF_ORDER_ENABLED,
				Settings.VIRTUAL_ADDRESS_NUM, Settings.REGISTER_RENAMING_ENABLED, Settings.LOAD_LIMIT),
				runTest("vector.program", (rf -> 
					rf.getScalarRegisterValue(8) == 2
					&& rf.getScalarRegisterValue(9) == 4
					&& rf.getScalarRegisterValue(10) == 6
					&& rf.getScalarRegisterValue(11) == 8)));
		*/
		/*
		Results results = runCombinations("vector.program", (rf -> {
			int[] r = rf.getVectorRegisterValue(8);
			return r[0] == 2 && r[1] == 4 && r[2] == 6 && r[3] == 8; 
			}));
		*/
		//Results results = runCombinations("inner_product_stop_vector.program", (rf -> rf.getScalarRegisterValue(1) == 5658112/*440*//*1632*/));
		Results results = runCombinations(programName, (rf -> rf.getScalarRegisterValue(1) == 5658112/*440*//*1632*/));
		//Results results = runCombinations("no_depend_ldc.program", (rf -> true));
		//Results results = runCombinations("dynamic_branch_pred.program", (rf -> rf.get(0, Reg.Architectural) == 0 && rf.get(1, Reg.Architectural) == 4));
		TestOutput.writeOutputs(results.resultsArray);		
		System.out.println(String.format("Results: %d%%", Math.round((1-results.failureRate) * 100)));
		printResults(results);
	}

	


	private static void printResults(Results results) {
		System.out.println("Bests");
		Set<Config> bestConfigs = null;
		float bestRate = 0;
		for(Config c : results.results.keySet()) {
			float val = results.results.get(c);
			if(val == bestRate) {
				bestConfigs.add(c);
			}
			if(val > bestRate) {
				bestConfigs = new HashSet<>();
				bestConfigs.add(c);
				bestRate = val;
			}
		}
		if(bestConfigs != null) {
			Set<IssueWindow> windowAlignment = new HashSet<>();
			Set<BranchPrediction> branchPrediction = new HashSet<>();
			Set<Boolean> reservationStationBypassEnabled = new HashSet<>();
			Set<Integer> numberOfExecutionUnits = new HashSet<>();
			Set<Integer> superScalerWidth = new HashSet<>();
			Set<Boolean> outOfOrderEnabled = new HashSet<>();
			Set<Integer> virtualAddressNum = new HashSet<>();
			Set<Boolean> registerRenaming = new HashSet<>();
			Set<Integer> loadLimit = new HashSet<>();
			for(Config c : bestConfigs) {
				if(!windowAlignment.contains(c.windowAlignment))
					windowAlignment.add(c.windowAlignment);
				if(!branchPrediction.contains(c.branchPrediction))
					branchPrediction.add(c.branchPrediction);
				if(!reservationStationBypassEnabled.contains(c.reservationStationBypassEnabled))
					reservationStationBypassEnabled.add(c.reservationStationBypassEnabled);
				if(!numberOfExecutionUnits.contains(c.numberOfExecutionUnits))
					numberOfExecutionUnits.add(c.numberOfExecutionUnits);
				if(!superScalerWidth.contains(c.superScalerWidth))
					superScalerWidth.add(c.superScalerWidth);
				if(!outOfOrderEnabled.contains(c.outOfOrderEnabled))
					outOfOrderEnabled.add(c.outOfOrderEnabled);
				if(!virtualAddressNum.contains(c.virtualAddressNum))
					virtualAddressNum.add(c.virtualAddressNum);
				if(!registerRenaming.contains(c.registerRenaming))
					registerRenaming.add(c.registerRenaming);
				if(!loadLimit.contains(c.loadLimit))
					loadLimit.add(c.loadLimit);
			}
			System.out.println("branchPrediction:" + branchPrediction);
			System.out.println("reservationStationBypassEnabled:" + reservationStationBypassEnabled);
			System.out.println("numberOfExecutionUnits:" + numberOfExecutionUnits);
			System.out.println("superScalerWidth:" + superScalerWidth);
			System.out.println("outOfOrderEnabled:" + outOfOrderEnabled);
			System.out.println("virtualAddressNum:" + virtualAddressNum);
			System.out.println("registerRenaming:" + registerRenaming);
			System.out.println("loadLimit:" + loadLimit);
			System.out.println(bestRate);
		}
		
		System.out.println("Failures");
		if(results.failures.isEmpty())
			System.out.println("All passed");
		else {
			Set<IssueWindow> windowAlignment = new HashSet<>();
			Set<BranchPrediction> branchPrediction = new HashSet<>();
			Set<Boolean> reservationStationBypassEnabled = new HashSet<>();
			Set<Integer> numberOfExecutionUnits = new HashSet<>();
			Set<Integer> superScalerWidth = new HashSet<>();
			Set<Boolean> outOfOrderEnabled = new HashSet<>();
			Set<Integer> virtualAddressNum = new HashSet<>();
			Set<Boolean> registerRenaming = new HashSet<>();
			Set<Integer> loadLimit = new HashSet<>();
			for(Config c : results.failures) {
				if(!windowAlignment.contains(c.windowAlignment))
					windowAlignment.add(c.windowAlignment);
				if(!branchPrediction.contains(c.branchPrediction))
					branchPrediction.add(c.branchPrediction);
				if(!reservationStationBypassEnabled.contains(c.reservationStationBypassEnabled))
					reservationStationBypassEnabled.add(c.reservationStationBypassEnabled);
				if(!numberOfExecutionUnits.contains(c.numberOfExecutionUnits))
					numberOfExecutionUnits.add(c.numberOfExecutionUnits);
				if(!superScalerWidth.contains(c.superScalerWidth))
					superScalerWidth.add(c.superScalerWidth);
				if(!outOfOrderEnabled.contains(c.outOfOrderEnabled))
					outOfOrderEnabled.add(c.outOfOrderEnabled);
				if(!virtualAddressNum.contains(c.virtualAddressNum))
					virtualAddressNum.add(c.virtualAddressNum);
				if(!registerRenaming.contains(c.registerRenaming))
					registerRenaming.add(c.registerRenaming);
				if(!loadLimit.contains(c.loadLimit))
					loadLimit.add(c.loadLimit);
			}
			System.out.println("branchPrediction:" + branchPrediction);
			System.out.println("reservationStationBypassEnabled:" + reservationStationBypassEnabled);
			System.out.println("numberOfExecutionUnits:" + numberOfExecutionUnits);
			System.out.println("superScalerWidth:" + superScalerWidth);
			System.out.println("outOfOrderEnabled:" + outOfOrderEnabled);
			System.out.println("virtualAddressNum:" + virtualAddressNum);
			System.out.println("registerRenaming:" + registerRenaming);
			System.out.println("loadLimit:" + loadLimit);
		}
	}

	public static Results runCombinations(String programName, Function<RegisterFile, Boolean> validationFunction) {
		Map<Config, Float> results = new HashMap<>();
		float[][][][][][][][][] resultsArray = new float[Settings.IssueWindow.values().length][Settings.BranchPrediction.values().length][2][7][7][2][5][2][6];
		List<Config> failures = new ArrayList<>();
		//int total = /*bypass*/2 * /*units*/7 * /*width*/7 * /*order*/2 * /*addr*/5 * /*renaming*/2 * /*load*/6 * Settings.BranchPrediction.values().length * Settings.IssueWindow.values().length;
		int total = /*bypass*/1 * /*units*/3 * /*width*/4 * /*order*/2 * /*addr*/4 * /*renaming*/2 * /*load*/4 * Settings.BranchPrediction.values().length * Settings.IssueWindow.values().length;
		int runNum = 0;
		int failureNum = 0;
		long startTime = System.currentTimeMillis();
		//for(IssueWindow window : Settings.IssueWindow.values()) {
		IssueWindow window = Settings.IssueWindow.Aligned; {
			Settings.ISSUE_WINDOW_METHOD = window;
			for(BranchPrediction branch : Settings.BranchPrediction.values()) {
			//BranchPrediciton branch = Settings.BranchPrediciton.Simple_Static; {
				Settings.BRANCH_PREDICTION_MODE = branch;
				//for(boolean bypass : new boolean[] {false, true}) {
				boolean bypass = false; {
					Settings.RESERVATION_STATION_BYPASS_ENABLED = bypass;
					//for(int units = 1, u=0; units<=64; units*=2, u++) {
					for(int units = 2, u=0; units<=2; units*=2, u++) {
						Settings.NUMBER_OF_EXECUTION_UNITS = units;
						//for(int width = 1, w=0; width<=64; width*=2, w++) {
						for(int width = 2, w=0; width<=8; width*=2, w++) {
							Settings.SUPERSCALER_WIDTH = width;
							for(boolean order : new boolean[] {false, true}) {
								if(runNum>0) {
									long timeLeft = (System.currentTimeMillis() - startTime) * (total-runNum) / runNum;
									long seconds = timeLeft/1000 % 60;
									long minutes = timeLeft/60000;
									System.out.println(String.format("%.2f%%, time left: %d:%02d",100*((float) runNum)/total, minutes, seconds));
								}
								Settings.OUT_OF_ORDER_ENABLED = order;
								//for(int addr = 8, a=0; addr<=128; addr*=2, a++) {
								for(int addr = 16, a=0; addr<=32; addr*=2, a++) {
									Settings.VIRTUAL_ADDRESS_NUM = addr;
									for(boolean renaming : new boolean[] {false, true}) {
										Settings.REGISTER_RENAMING_ENABLED = renaming;
										//for(int load = 1, l=0; load<=16; load*=2, l++) {
										for(int load = 2, l=0; load<=4; load*=2, l++) {
											Settings.LOAD_LIMIT = load;
											runNum++;
											final Control control = new Control();
											final int units_w = units;
											final int width_w = width;
											final int addr_w = addr;
											final int load_w = load;
											Thread worker = new Thread() {
												public void run() {
													try {
														control.val = runTest(programName, validationFunction);
													} catch (InterruptedException e) {
														System.err.println("Timeout: " + new Config(window, branch, bypass, units_w, width_w, order, addr_w, renaming, load_w).toString());
													}
													catch (WrongAnswerException e) {
														System.err.println("Incrorrect Answer: " + new Config(window, branch, bypass, units_w, width_w, order, addr_w, renaming, load_w).toString());
													}
													catch (Exception e) {
														System.err.println(new Config(window, branch, bypass, units_w, width_w, order, addr_w, renaming, load_w));
														e.printStackTrace(System.err);
													}
												}
											};
											Thread timer = new Thread() {
												public void run() {
													try {
														Thread.sleep(4000);
													} catch (InterruptedException e) {}										
												}
											};
											worker.start();
											timer.start();
											while(true) {
												if(!timer.isAlive()) {
													failures.add(new Config(window, branch, bypass, units, width, order, addr, renaming, load));
													resultsArray[window.ordinal()][branch.ordinal()][bypass?1:0][u][w][order?1:0][a][renaming?1:0][l] = -1;
													worker.interrupt();
													failureNum++;
													break;
												}
												if(!worker.isAlive()) {
													if(control.val != null) {
														results.put(new Config(window, branch, bypass, units, width, order, addr, renaming, load), control.val);
														resultsArray[window.ordinal()][branch.ordinal()][bypass?1:0][u][w][order?1:0][a][renaming?1:0][l] = control.val;
													} else {
														failures.add(new Config(window, branch, bypass, units, width, order, addr, renaming, load));
														resultsArray[window.ordinal()][branch.ordinal()][bypass?1:0][u][w][order?1:0][a][renaming?1:0][l] = -1;
														failureNum++;
													}
													timer.interrupt();
													break;
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return new Results(results, failures, (double) failureNum / total, resultsArray);
	}
	
	private static float runTest(String programName, Function<RegisterFile, Boolean> p) throws IOException, ParserException, Exception {
		Worker.init();
		Saac saac = new Saac(programName);
		while(!Worker.isFinished()) {
        	saac.step();
        	if(Thread.interrupted())
    			throw new InterruptedException();
		}
		if(p.apply(saac.registerFile)) {
			return RateUtils.round((float) Saac.InstructionCounter / Saac.CycleCounter);
		} else {
			throw new WrongAnswerException();
		}
	}
	
	@SuppressWarnings("serial")
	static class WrongAnswerException extends Exception {}
	
	static class Config {
		IssueWindow windowAlignment;
		BranchPrediction branchPrediction;
		boolean reservationStationBypassEnabled;
		int numberOfExecutionUnits;
		int superScalerWidth;
		boolean outOfOrderEnabled;
		int virtualAddressNum;
		boolean registerRenaming;
		int loadLimit;
		Config(
			IssueWindow windowAlignment,
			BranchPrediction branchPrediction,
			boolean reservationStationBypassEnabled,
			int numberOfExecutionUnits,
			int superScalerWidth,
			boolean outOfOrderEnabled,
			int virtualAddressNum,
			boolean registerRenaming,
			int loadLimit) {
			this.windowAlignment = windowAlignment;
			this.branchPrediction = branchPrediction;
			this.reservationStationBypassEnabled = reservationStationBypassEnabled;
			this.numberOfExecutionUnits = numberOfExecutionUnits;
			this.superScalerWidth = superScalerWidth;
			this.outOfOrderEnabled = outOfOrderEnabled;
			this.virtualAddressNum = virtualAddressNum;
			this.registerRenaming = registerRenaming;
			this.loadLimit = loadLimit;
		}
		
		public String toString() {
			return String.format("Alignment: %s Branch: %s Bypass: %s EUs: %d Width: %d OOO: %s VirtAdresses: %d Renaming: %s LoadLimit %d",
					windowAlignment.toString(),
					branchPrediction.toString(),
					Boolean.toString(reservationStationBypassEnabled),
					numberOfExecutionUnits,
					superScalerWidth,
					Boolean.toString(outOfOrderEnabled),
					virtualAddressNum,
					Boolean.toString(registerRenaming),
					loadLimit
					);
		}
	}
	
}
