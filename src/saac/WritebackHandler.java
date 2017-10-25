package saac;

public class WritebackHandler implements ClockedComponent {
	FConnection<InstructionResult>.Output inputEU_A; 
	FConnection<InstructionResult>.Output inputEU_B; 
	FConnection<InstructionResult>.Output inputLS;
	RegisterFile registerFile;
	Issuer issuer;
	
	public WritebackHandler(RegisterFile rf, Issuer issuer,
			FConnection<InstructionResult>.Output inputEU_A, 
			FConnection<InstructionResult>.Output inputEU_B, 
			FConnection<InstructionResult>.Output inputLS) {
		this.inputEU_A = inputEU_A;
		this.inputEU_B = inputEU_B;
		this.inputLS = inputLS;
		this.registerFile = rf;
		this.issuer = issuer;
	}

	@Override
	public void tick() throws Exception {
		InstructionResult res;
		if(inputLS.ready())
			res = inputLS.get();
		else if(inputEU_A.ready())
			res = inputEU_A.get();
		else if(inputEU_B.ready())
			res = inputEU_B.get();
		else
			return;
		
		if(res instanceof MemoryResult) {
			MemoryResult mr = (MemoryResult) res;
			issuer.dirtyMem.remove(mr.getValue());
		} else if(res instanceof RegisterResult) {
			RegisterResult rr = (RegisterResult) res;
			System.out.println(String.format("%d is written back to r%d", rr.getValue(), rr.getTarget()));
			registerFile.set(rr.getTarget(), rr.getValue());
			registerFile.setDirty(rr.getTarget(), false);
		}
	}

	@Override
	public void tock() throws Exception {
		
	}

}
