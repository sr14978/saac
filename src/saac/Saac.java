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
			Thread.sleep(500);
			saac.tick();
			Thread.sleep(500);
			saac.tock();
			cycleCounter++;
			System.out.println((float) InstructionCounter / cycleCounter);
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
		components.add(new WritebackHandler(outOfEU_A.getOutputEnd(), registerFile));
		
		Connection<Instruction> intoEU_B = new Connection<>();
		Connection<InstructionResult> outOfEU_B = new Connection<>();
		components.add(new ExecutionUnit(intoEU_B.getOutputEnd(), outOfEU_B.getInputEnd()));
		components.add(new WritebackHandler(outOfEU_B.getOutputEnd(), registerFile));
		
		Connection<Instruction> intoLS = new Connection<>();
		Connection<InstructionResult> outOfLS = new Connection<>();
		components.add(new LoadStoreExecutionUnit(intoLS.getOutputEnd(), outOfLS.getInputEnd(), memory));
		components.add(new WritebackHandler(outOfLS.getOutputEnd(), registerFile));
		
		components.add(new InstructionDecoder(intoEU_A.getInputEnd(), intoEU_B.getInputEnd(), intoLS.getInputEnd(), registerFile));
	}

	@Override
	public void tick() throws Exception {
		System.out.println("Clock tick");
		for(ClockedComponent c : components)
			c.tick();
	}

	@Override
	public void tock() throws Exception {
		System.out.println("Clock tock");
		for(ClockedComponent c : components)
			c.tock();
	}

}
