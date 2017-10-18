package saac;

import saac.Instructions.Opcode;

public class Decoder implements ClockedComponent{

	Connection<Instruction>.Input output;
	Connection<int[]>.Output input;
	Instruction bufferOut;
	
	public Decoder(Connection<Instruction>.Input output, Connection<int[]>.Output input) {
		this.output = output;
		this.input = input;
	}

	@Override
	public void tick() throws Exception {
		if(bufferOut != null)
			return;
		
		int[] data = input.get();
		if(data == null)
			return;
		bufferOut = new Instruction(Opcode.fromInt(data[0]), data[1], data[2], data[3]);
	}

	@Override
	public void tock() throws Exception {
		if(bufferOut == null)
			return;
		if(output.isEmpty()) {
			output.put(bufferOut);
			bufferOut = null;
		}
	}

}
