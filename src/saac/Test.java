package saac;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import saac.Settings.BranchPrediciton;
import saac.clockedComponents.RegisterFile;
import saac.clockedComponents.RegisterFile.Reg;
import saac.utils.RateUtils;
import saac.utils.parsers.ParserException;

public class Test {
	
	static Map<Config, Float> results = new HashMap<>();
	
	static class Control {
		volatile Float val = null;
	}
	
	public static void main(String[] args) throws Exception {
		/*
		Settings.RESERVATION_STATION_BYPASS_ENABLED = true;
		Settings.OUT_OF_ORDER_ENABLED = true;
		Settings.REGISTER_RENAMING_ENABLED = true;
		Settings.SUPERSCALER_WIDTH = 1;
		Settings.NUMBER_OF_EXECUTION_UNITS = 1;
		Settings.VIRTUAL_ADDRESS_NUM = 8;
		Settings.BRANCH_PREDICTION_MODE = BranchPrediciton.Blocking;
		runTest("inner_product_stop.program", (rf -> rf.get(1, Reg.Architectural) == 440));
		*/
		
		for(boolean bypass : new boolean[] {true, false}) {
			Settings.RESERVATION_STATION_BYPASS_ENABLED = bypass;
			for(boolean order : new boolean[] {true, false}) {
				Settings.OUT_OF_ORDER_ENABLED = order;
				for(boolean renaming : new boolean[] {true, false}) {
					Settings.REGISTER_RENAMING_ENABLED = renaming;
					for(int width = 1; width<5; width++) {
						Settings.SUPERSCALER_WIDTH = width;
						for(int units = 1; units<5; units++) {
							Settings.NUMBER_OF_EXECUTION_UNITS = units;
							for(int addr = 8; addr<33; addr*=2) {
								Settings.VIRTUAL_ADDRESS_NUM = addr;
								for(BranchPrediciton branch : Settings.BranchPrediciton.values()) {
									Settings.BRANCH_PREDICTION_MODE = branch;
									final Control control = new Control();
									Thread worker = new Thread() {
										public void run() {
											try {
												control.val = runTest("inner_product_stop.program", (rf -> rf.get(1, Reg.Architectural) == 440));
											} catch (InterruptedException e) {
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									};
									worker.start();
									Thread.sleep(10);
									if(control.val == null) {
										worker.interrupt();
									} else {
										results.put(new Config(branch, bypass, units, width, order, addr, renaming), control.val);
									}
								}
							}
						}
					}
				}
				System.out.println(results);
				Set<Config> bestConfigs = null;
				float bestRate = 0;
				for(Config c : results.keySet()) {
					float val = results.get(c);
					if(val == bestRate) {
						bestConfigs.add(c);
					}
					if(val > bestRate) {
						bestConfigs = new HashSet<>();
						bestConfigs.add(c);
						bestRate = val;
					}
				}
				System.out.println(bestConfigs);
				Set<BranchPrediciton> branchPrediction = new HashSet<>();
				Set<Boolean> reservationStationBypassEnabled = new HashSet<>();
				Set<Integer> numberOfExecutionUnits = new HashSet<>();
				Set<Integer> superScalerWidth = new HashSet<>();
				Set<Boolean> outOfOrderEnabled = new HashSet<>();
				Set<Integer> virtualAddressNum = new HashSet<>();
				Set<Boolean> registerRenaming = new HashSet<>();
				for(Config c : bestConfigs) {
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
				}
				System.out.println("branchPrediction:" + branchPrediction);
				System.out.println("reservationStationBypassEnabled:" + reservationStationBypassEnabled);
				System.out.println("numberOfExecutionUnits:" + numberOfExecutionUnits);
				System.out.println("superScalerWidth:" + superScalerWidth);
				System.out.println("outOfOrderEnabled:" + outOfOrderEnabled);
				System.out.println("virtualAddressNum:" + virtualAddressNum);
				System.out.println("registerRenaming:" + registerRenaming);
				System.out.println(bestRate);
			}
		}
	}

	private static float runTest(String programName, Function<RegisterFile, Boolean> p) throws IOException, ParserException, Exception {
		Worker.finished = false;
		Saac saac = new Saac(programName);
		while(!Worker.finished) {
        	saac.step(()->{});
        	if(Thread.interrupted())
    			throw new InterruptedException();
		}
		if(p.apply(saac.registerFile))
			return RateUtils.round((float) Saac.InstructionCounter / Saac.CycleCounter);
		else
			throw new Exception("Test Failed");
	}
	
	static class Config {
		BranchPrediciton branchPrediction;
		boolean reservationStationBypassEnabled;
		int numberOfExecutionUnits;
		int superScalerWidth;
		boolean outOfOrderEnabled;
		int virtualAddressNum;
		boolean registerRenaming;
		Config(BranchPrediciton branchPrediction,
			boolean reservationStationBypassEnabled,
			int numberOfExecutionUnits,
			int superScalerWidth,
			boolean outOfOrderEnabled,
			int virtualAddressNum,
			boolean registerRenaming) {
			this.branchPrediction = branchPrediction;
			this.reservationStationBypassEnabled = reservationStationBypassEnabled;
			this.numberOfExecutionUnits = numberOfExecutionUnits;
			this.superScalerWidth = superScalerWidth;
			this.outOfOrderEnabled = outOfOrderEnabled;
			this.virtualAddressNum = virtualAddressNum;
			this.registerRenaming = registerRenaming;
		}
		
		public String toString() {
			return String.format("Branch: %s, Bypass: %s, EUs: %d, Width:%d, OOO: %s, VirtAdresses: %d, Renaming: %s",
					branchPrediction.toString(),
					Boolean.toString(reservationStationBypassEnabled),
					numberOfExecutionUnits,
					superScalerWidth,
					Boolean.toString(outOfOrderEnabled),
					virtualAddressNum,
					Boolean.toString(registerRenaming)
					);
		}
	}
	
}
