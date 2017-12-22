package saac.test;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import saac.Settings;
import saac.Settings.BranchPrediction;
import saac.Settings.IssueWindow;

public class TestOutput {
	
	public static void writeOutputs(float[][][][][][][][][] resultsArray) throws IOException {
		FileWriter fileWriter = new FileWriter("results.out");
		PrintWriter printer = new PrintWriter(fileWriter);
		
		for(IssueWindow window : Settings.IssueWindow.values()) {
			for(BranchPrediction branch : Settings.BranchPrediction.values()) {
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
}