package saac;

public class WritebackHandler implements ClockedComponent {
	Connection<InstructionResult>.Output inputEU_A; 
	Connection<InstructionResult>.Output inputEU_B; 
	Connection<InstructionResult>.Output inputLS;
	RegisterFile registerFile;
	Issuer issuer;
	
	public WritebackHandler(RegisterFile rf, Issuer issuer,
			Connection<InstructionResult>.Output inputEU_A, 
			Connection<InstructionResult>.Output inputEU_B, 
			Connection<InstructionResult>.Output inputLS) {
		this.inputEU_A = inputEU_A;
		this.inputEU_B = inputEU_B;
		this.inputLS = inputLS;
		this.registerFile = rf;
		this.issuer = issuer;
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
		if(res instanceof MemeoryResult) {
			MemeoryResult mr = (MemeoryResult) res;
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
