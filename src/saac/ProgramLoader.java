package saac;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import saac.utils.parsers.ParserException;
import saac.utils.parsers.Parsers;

public class ProgramLoader {

	final static String programDirectory = "programs/";
	//final static String programName = "no_depend_mul.program";
	//final static String programName = "inner_product_stop.program";
	//final static String programName = "reorder.program";
	//final static String programName = "static_branch_pred.program";
	//final static String programName = "dynamic_branch_pred.program";
	final static String programName = "out of order dependancies test.program";
	
	
	public static int[][] loadProgram() throws IOException, ParserException {
		File program = new File(programDirectory + programName);
		if(!program.exists())
			throw new FileNotFoundException();
		List<String> progam = Files.readAllLines(program.toPath());
		
		List<int[]> instructions = new ArrayList<>();
		int lineNumber = 1;
		for(String line : progam) {
			if(line.equals(""))
				continue;
			if(line.startsWith("//"))
				continue;
			try {
				int[] instruction = Parsers.parseInstruction(line);
				instructions.add(instruction);
			} catch (ParserException e) {
				throw new ParserException("Error on line " + lineNumber + ": " + e.getMessage());
			}
			lineNumber++;
		}
				
		return instructions.toArray(new int[0][0]);
		
	}
}
