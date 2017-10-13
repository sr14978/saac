package saac;

public class WritebackHandler implements ClockedComponent {
	Connection<InstructionResult>.Output input;
	RegisterFile registerFile;
	
	public WritebackHandler(Connection<InstructionResult>.Output input, RegisterFile rf) {
		this.input = input;
		this.registerFile = rf;
	}

	@Override
	public void tick() throws Exception {
		InstructionResult res = input.get();
		if(res == null)
			return;
		System.out.println(res + " is written back");
		registerFile.set(res.getTarget(), res.getValue());
		registerFile.setDirty(res.getTarget(), false);
	}

	@Override
	public void tock() throws Exception {
		
	}

}
