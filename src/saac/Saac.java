package saac;

import java.util.ArrayList;
import java.util.List;

public class Saac implements ClockedComponent {

	static int InstructionCounter = 0;
	
	public static void main(String[] args) throws Exception {
		System.out.println("This is Saac: Started");
		
		Saac saac = new Saac();
		int cycleCounter = 0;
		while (true) {
			Thread.sleep(10);
			saac.tick();
			Thread.sleep(10);
			saac.tock();
			cycleCounter++;
			System.out.println("Rate: " + (float) InstructionCounter / cycleCounter);
		}
	}
	
	List<ClockedComponent> components;
	
	Saac() throws Exception {
		components = new ArrayList<ClockedComponent>();
		RegisterFile registerFile = new RegisterFile();
		Memory memory = new Memory();
		//populated list
		Connection<Instruction> intoEU_A = new Connection<>();
		Connection<InstructionResult> outOfEU_A = new Connection<>();
		components.add(new ExecutionUnit(intoEU_A.getOutputEnd(), outOfEU_A.getInputEnd()));
		
		Connection<Instruction> intoEU_B = new Connection<>();
		Connection<InstructionResult> outOfEU_B = new Connection<>();
		components.add(new ExecutionUnit(intoEU_B.getOutputEnd(), outOfEU_B.getInputEnd()));
		
		Connection<Instruction> intoLS = new Connection<>();
		Connection<InstructionResult> outOfLS = new Connection<>();
		components.add(new LoadStoreExecutionUnit(intoLS.getOutputEnd(), outOfLS.getInputEnd(), memory));
		
		Connection<Instruction> intoBr = new Connection<>();
		Connection<Integer> fromBr = new Connection<>();
		components.add(new BranchExecutionUnit(intoBr.getOutputEnd(), fromBr.getInputEnd()));
		
		Connection<Instruction> intoDualResStation = new Connection<>();
		components.add(new DualReservationStation(intoEU_A.getInputEnd(), intoEU_B.getInputEnd(), intoDualResStation.getOutputEnd()));
			
		Connection<int[]> intoDecode = new Connection<>();
		components.add(new Fetcher(registerFile, intoDecode.getInputEnd(), fromBr.getOutputEnd()));
		
		Connection<Instruction> intoIssue = new Connection<>();
		components.add(new Decoder(intoIssue.getInputEnd(), intoDecode.getOutputEnd()));
		
		Issuer issuer = new Issuer(registerFile,
				intoIssue.getOutputEnd(),
				intoDualResStation.getInputEnd(),
				intoLS.getInputEnd(),
				intoBr.getInputEnd()
				);
		components.add(new WritebackHandler(registerFile, issuer, outOfEU_A.getOutputEnd(), outOfEU_B.getOutputEnd(), outOfLS.getOutputEnd()));
		components.add(issuer);
	}

	@Override
	public void tick() throws Exception {
		//System.out.println("Clock tick");
		for(ClockedComponent c : components)
			new Thread(){
				public void run() {
					try {
						c.tick();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
	}

	@Override
	public void tock() throws Exception {
		//System.out.println("Clock tock");
		for(ClockedComponent c : components)
			new Thread(){
			public void run() {
				try {
					c.tock();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

}
