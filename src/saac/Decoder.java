package saac;

import saac.Instructions.Opcode;

public class Decoder implements ClockedComponent{

	FConnection<Instruction>.Input output;
	FConnection<int[]>.Output input;
	Instruction bufferOut;
	
	public Decoder(FConnection<Instruction>.Input output, FConnection<int[]>.Output input) {
		this.output = output;
		this.input = input;
	}

	@Override
	public void tick() throws Exception {
		if(bufferOut != null)
			return;
		
		if(!input.ready())
			return;
		int[] data = input.get();
		
		bufferOut = new Instruction(Opcode.fromInt(data[0]), data[1], data[2], data[3]);
	}

	@Override
	public void tock() throws Exception {
		if(bufferOut == null)
			return;
		if(output.clear()) {
			output.put(bufferOut);
			bufferOut = null;
		}
	}

}
