package saac;

import saac.Instructions.Opcode;

public class Fetcher implements ClockedComponent{

	RegisterFile registerFile;
	Connection<int[]>.Input output;
	int[] bufferOut;
	Connection<Integer>.Output fromBrUnit;
	
	int programCounter = 0;
	
	boolean halt = false;
	
	Fetcher(RegisterFile registerFile, Connection<int[]>.Input output, Connection<Integer>.Output fromBrUnit) {
		this.output = output;
		this.fromBrUnit = fromBrUnit;
		this.registerFile = registerFile;
	}
	
	@Override
	public void tick() throws Exception {
		
		if(halt) {
			
			Integer newPC = fromBrUnit.get();
			if(newPC == null)
				return;
			else {
				halt = false;
				programCounter = newPC;
			}
		}
		
		if(bufferOut != null)
			return;
		
		bufferOut = InstructionsSource.getInstruction(programCounter);
		System.out.println("Fetching instruction: " + programCounter + " = " + Opcode.fromInt(bufferOut[0]));
		programCounter++;
		
		switch(Opcode.fromInt(bufferOut[0])) {
		case Jmp:
		case JmpN:
		case JmpZ:
			bufferOut[3] = programCounter;
		case Br:
		case Ln:
			halt = true;
			break;
		default:
			break;
		}		
	}
	
	@Override
	public void tock() throws Exception {
		if(bufferOut == null)
			return;
		else if(output.isEmpty()) {
			output.put(bufferOut);
			bufferOut = null;
		}
	}

}
