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
	final static String programName = "inner_product.program";
	
	public static int[][] loadProgram() throws IOException, ParserException {
		File program = new File(programDirectory + programName);
		if(!program.exists())
			throw new FileNotFoundException();
		List<String> progam = Files.readAllLines(program.toPath());
		
		List<int[]> instructions = new ArrayList<>();
		
		for(String line : progam) {
			if(line.equals(""))
				continue;
			if(line.startsWith("//"))
				continue;
			int[] instruction = Parsers.parseInstruction(line);
			instructions.add(instruction);
		}
				
		return instructions.toArray(new int[0][0]);
		
	}
}
