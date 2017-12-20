package saac;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import saac.Settings.BranchPrediciton;
import saac.Settings.IssueWindow;
import saac.clockedComponents.RegisterFile;
import saac.utils.RateUtils;
import saac.utils.parsers.ParserException;

public class Test {
	
	static class Results {
		Map<Config, Float> results;
		List<Config> failures;
		double failureRate;
		Results(Map<Config, Float> results, List<Config> failures, double failureRate) {
			this.results = results;
			this.failures = failures;
			this.failureRate = failureRate;
		}
	}
	
	static class Control {
		volatile Float val = null;
	}
	
	public static void main(String[] args) throws Exception {		
		
		System.out.println("Testing...");
		Results results = runCombinations("inner_product_stop.program", (rf -> rf.getRegisterValue(1) == /*5658112/*440*/1632));
		/*
		Results results = new Results(new HashMap<Test.Config, Float>(), new ArrayList<Test.Config>(), 0);
		results.results.put(new Config(Settings.ISSUE_WINDOW_METHOD, Settings.BRANCH_PREDICTION_MODE, Settings.RESERVATION_STATION_BYPASS_ENABLED, 
				Settings.NUMBER_OF_EXECUTION_UNITS, Settings.SUPERSCALER_WIDTH, Settings.OUT_OF_ORDER_ENABLED,
				Settings.VIRTUAL_ADDRESS_NUM, Settings.REGISTER_RENAMING_ENABLED, Settings.LOAD_LIMIT), runTest("inner_product_stop.program", (rf -> rf.getRegisterValue(1) == 1632)));
		*/
		//Results results = runCombinations("no_depend_ldc.program", (rf -> true));
		//Results results = runCombinations("dynamic_branch_pred.program", (rf -> rf.get(0, Reg.Architectural) == 0 && rf.get(1, Reg.Architectural) == 4));
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
			Set<BranchPrediciton> branchPrediction = new HashSet<>();
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
			Set<BranchPrediciton> branchPrediction = new HashSet<>();
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
		List<Config> failures = new ArrayList<>();
		int total = /*bypass*/2 * /*units*/2 * /*width*/2 * /*order*/2 * /*addr*/2 * /*renaming*/2 * /*load*/2 * Settings.BranchPrediciton.values().length * Settings.IssueWindow.values().length;
		int runNum = 0;
		int failureNum = 0;
		for(IssueWindow window : Settings.IssueWindow.values()) {
		//IssueWindow window = Settings.IssueWindow.Aligned; {
			Settings.ISSUE_WINDOW_METHOD = window;
			for(BranchPrediciton branch : Settings.BranchPrediciton.values()) {
			//BranchPrediciton branch = Settings.BranchPrediciton.Simple_Static; {
				Settings.BRANCH_PREDICTION_MODE = branch;
				for(boolean bypass : new boolean[] {true, false}) {
					Settings.RESERVATION_STATION_BYPASS_ENABLED = bypass;
					for(int units = 32; units<=64; units*=2) {
						Settings.NUMBER_OF_EXECUTION_UNITS = units;
						for(int width = 32; width<=64; width*=2) {
							Settings.SUPERSCALER_WIDTH = width;
							for(boolean order : new boolean[] {true, false}) {
								Settings.OUT_OF_ORDER_ENABLED = order;
								for(int addr = 64; addr<=128; addr*=2) {
									Settings.VIRTUAL_ADDRESS_NUM = addr;
									for(boolean renaming : new boolean[] {true, false}) {
										Settings.REGISTER_RENAMING_ENABLED = renaming;
										for(int load = 16; load<=32; load*=2) {
											Settings.LOAD_LIMIT = load;
											System.out.println(String.format("%.2f%%",100*((float) runNum++)/total));
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
														//System.err.println(e.getClass());
														System.err.println(new Config(window, branch, bypass, units_w, width_w, order, addr_w, renaming, load_w));
														e.printStackTrace(System.err);
													}
												}
											};
											Thread timer = new Thread() {
												public void run() {
													try {
														Thread.sleep(1000);
													} catch (InterruptedException e) {}										
												}
											};
											worker.start();
											timer.start();
											while(true) {
												if(!timer.isAlive()) {
													failures.add(new Config(window, branch, bypass, units, width, order, addr, renaming, load));
													worker.interrupt();
													failureNum++;
													break;
												}
												if(!worker.isAlive()) {
													if(control.val != null) {
														results.put(new Config(window, branch, bypass, units, width, order, addr, renaming, load), control.val);
													} else {
														failures.add(new Config(window, branch, bypass, units, width, order, addr, renaming, load));
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
		return new Results(results, failures, (double) failureNum / total);
	}
	
	private static float runTest(String programName, Function<RegisterFile, Boolean> p) throws IOException, ParserException, Exception {
		Worker.finished = false;
		Saac saac = new Saac(programName);
		while(!Worker.finished) {
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
		BranchPrediciton branchPrediction;
		boolean reservationStationBypassEnabled;
		int numberOfExecutionUnits;
		int superScalerWidth;
		boolean outOfOrderEnabled;
		int virtualAddressNum;
		boolean registerRenaming;
		int loadLimit;
		Config(
			IssueWindow windowAlignment,
			BranchPrediciton branchPrediction,
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
			return String.format("Alignment: %s, Branch: %s, Bypass: %s, EUs: %d, Width:%d, OOO: %s, VirtAdresses: %d, Renaming: %s, LoadLimit %d",
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
