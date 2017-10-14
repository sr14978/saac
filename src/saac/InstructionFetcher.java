package saac;

import saac.Instructions.Opcode;

public class InstructionFetcher implements ClockedComponent{

	RegisterFile registerFile;
	Connection<byte[]>.Input output;
	byte[] bufferOut;
	Connection<Integer>.Output fromBrUnit;
	
	boolean halt = false;
	
	InstructionFetcher(Connection<byte[]>.Input output, Connection<Integer>.Output fromBrUnit, RegisterFile registerFile) {
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
				registerFile.set(RegisterFile.PC, newPC);
			}
		}
		
		int programCounter = registerFile.get(RegisterFile.PC);
		System.out.println("Fetching instruction: " + programCounter);
		if(bufferOut != null)
			return;
		bufferOut = InstructionsSource.getInstruction(programCounter);
		if(bufferOut[0] == Opcode.Br.ordinal() || bufferOut[0] == Opcode.Jmp.ordinal() || bufferOut[0] == Opcode.JmpN.ordinal() || bufferOut[0] == Opcode.JmpZ.ordinal())
			halt = true;
		
		registerFile.set(RegisterFile.PC, programCounter+1);
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
