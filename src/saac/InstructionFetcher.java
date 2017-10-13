package saac;

public class InstructionFetcher implements ClockedComponent{

	Connection<byte[]>.Input output;
	byte[] bufferOut;
	
	int programCounter = 0;
	
	InstructionFetcher(Connection<byte[]>.Input output) {
		this.output = output;
	}
	
	@Override
	public void tick() throws Exception {
		if(bufferOut != null)
			return;
		bufferOut = InstructionsSource.getInstruction(programCounter);
		programCounter++;
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
