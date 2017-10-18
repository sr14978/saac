package saac;

public class WritebackHandler implements ClockedComponent {
	Connection<InstructionResult>.Output inputEU_A; 
	Connection<InstructionResult>.Output inputEU_B; 
	Connection<InstructionResult>.Output inputLS;
	RegisterFile registerFile;
	
	public WritebackHandler(RegisterFile rf,
			Connection<InstructionResult>.Output inputEU_A, 
			Connection<InstructionResult>.Output inputEU_B, 
			Connection<InstructionResult>.Output inputLS) {
		this.inputEU_A = inputEU_A;
		this.inputEU_B = inputEU_B;
		this.inputLS = inputLS;
		this.registerFile = rf;
	}

	@Override
	public void tick() throws Exception {
		InstructionResult res = inputLS.get();
		if(res == null) {
			res = inputEU_A.get();
			if(res == null) {
				res = inputEU_B.get();
				if(res == null)
					return;
			}			
		}
		System.out.println(String.format("%d is written back to r%d", res.getValue(), res.getTarget()));
		registerFile.set(res.getTarget(), res.getValue());
		registerFile.setDirty(res.getTarget(), false);
	}

	@Override
	public void tock() throws Exception {
		
	}

}
