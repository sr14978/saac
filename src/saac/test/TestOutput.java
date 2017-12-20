package saac.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import saac.Settings;
import saac.Settings.BranchPrediciton;
import saac.Settings.IssueWindow;

public class TestOutput {
	
	public static void writeOutputs(float[][][][][][][][][] resultsArray) throws IOException {
		FileWriter fileWriter = new FileWriter("results.out");
		PrintWriter printer = new PrintWriter(fileWriter);
		
		for(IssueWindow window : Settings.IssueWindow.values()) {
			for(BranchPrediciton branch : Settings.BranchPrediciton.values()) {
				for(boolean bypass : new boolean[] {false, true}) {
					for(int units = 1, u=0; units<=64; units*=2, u++) {
						for(int width = 1, w=0; width<=64; width*=2, w++) {
							for(boolean order : new boolean[] {false, true}) {
								for(int addr = 8, a=0; addr<=128; addr*=2, a++) {
									for(boolean renaming : new boolean[] {false, true}) {
										for(int load = 1, l=0; load<=32; load*=2, l++) {
											printer.println(resultsArray[window.ordinal()][branch.ordinal()][bypass?1:0][u][w][order?1:0][a][renaming?1:0][l]);
										}
									}
								}
							}
						}
					}
				}
			}
		}
		
		printer.close();
	}
	
	/*
	writeWindowOutput(resultsArray, printer);
	writeBranchOutput(resultsArray, printer);
	writeBypassOutput(resultsArray, printer);
	writeUnitsOutput(resultsArray, printer);
	writeWidthOutput(resultsArray, printer);
	writeOrderOutput(resultsArray, printer);
	writeAddrOutput(resultsArray, printer);
	writeRenamingOutput(resultsArray, printer);
	writeLoadOutput(resultsArray, printer);
	*/
	
	private static void writeWindowOutput(float[][][][][][][][][] resultsArray, PrintWriter printer) {
		float[] results = new float[Settings.IssueWindow.values().length];
		for(IssueWindow window : Settings.IssueWindow.values()) {
			float max = -1;
			for(BranchPrediciton branch : Settings.BranchPrediciton.values()) {
				for(boolean bypass : new boolean[] {true, false}) {
					for(int units = 1; units<=64; units*=2) {
						for(int width = 1; width<=64; width*=2) {
							for(boolean order : new boolean[] {true, false}) {
								for(int addr = 8; addr<=128; addr*=2) {
									for(boolean renaming : new boolean[] {true, false}) {
										for(int load = 1; load<=32; load*=2) {
											if(resultsArray[window.ordinal()][branch.ordinal()][bypass?1:0][units][width][order?1:0][addr][renaming?1:0][load] > max) {
												max = resultsArray[window.ordinal()][branch.ordinal()][bypass?1:0][units][width][order?1:0][addr][renaming?1:0][load];
											}
										}
									}
								}
							}
						}
					}
				}
			}
			results[window.ordinal()] = max;
		}
		for(float f : results) {
			printer.print(Float.toString(f) + " ");
		}
		printer.println();
	}
	
	private static void writeBranchOutput(float[][][][][][][][][] resultsArray, PrintWriter printer) {
		float[] results = new float[Settings.BranchPrediciton.values().length];
		for(BranchPrediciton branch : Settings.BranchPrediciton.values()) {
			float max = -1;
			for(IssueWindow window : Settings.IssueWindow.values()) {
				for(boolean bypass : new boolean[] {true, false}) {
					for(int units = 1; units<=64; units*=2) {
						for(int width = 1; width<=64; width*=2) {
							for(boolean order : new boolean[] {true, false}) {
								for(int addr = 8; addr<=128; addr*=2) {
									for(boolean renaming : new boolean[] {true, false}) {
										for(int load = 1; load<=32; load*=2) {
											if(resultsArray[window.ordinal()][branch.ordinal()][bypass?1:0][units][width][order?1:0][addr][renaming?1:0][load] > max) {
												max = resultsArray[window.ordinal()][branch.ordinal()][bypass?1:0][units][width][order?1:0][addr][renaming?1:0][load];
											}
										}
									}
								}
							}
						}
					}
				}
			}
			results[branch.ordinal()] = max;
		}
		for(float f : results) {
			printer.print(Float.toString(f) + " ");
		}
		printer.println();
	}
	
	private static void writeBypassOutput(float[][][][][][][][][] resultsArray, PrintWriter printer) {
		float[] results = new float[Settings.BranchPrediciton.values().length];
		for(BranchPrediciton branch : Settings.BranchPrediciton.values()) {
			float max = -1;
			for(IssueWindow window : Settings.IssueWindow.values()) {
				for(boolean bypass : new boolean[] {true, false}) {
					for(int units = 1; units<=64; units*=2) {
						for(int width = 1; width<=64; width*=2) {
							for(boolean order : new boolean[] {true, false}) {
								for(int addr = 8; addr<=128; addr*=2) {
									for(boolean renaming : new boolean[] {true, false}) {
										for(int load = 1; load<=32; load*=2) {
											if(resultsArray[window.ordinal()][branch.ordinal()][bypass?1:0][units][width][order?1:0][addr][renaming?1:0][load] > max) {
												max = resultsArray[window.ordinal()][branch.ordinal()][bypass?1:0][units][width][order?1:0][addr][renaming?1:0][load];
											}
										}
									}
								}
							}
						}
					}
				}
			}
			results[branch.ordinal()] = max;
		}
		for(float f : results) {
			printer.print(Float.toString(f) + " ");
		}
		printer.println();
	}
	}
