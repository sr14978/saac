package saac;

import java.util.ArrayList;
import java.util.List;

public class Saac implements ClockedComponent {

	public static void main(String[] args) throws Exception {
		System.out.println("This is Saac: Started");
		
		Saac saac = new Saac();
		while (true) {
			Thread.sleep(500);
			saac.tick();
			Thread.sleep(500);
			saac.tock();
		}
	}
	
	List<ClockedComponent> components;
	
	Saac() throws Exception {
		components = new ArrayList<ClockedComponent>();
		//populated list
		Connection<Instruction> intoEU = new Connection<>();
		Connection<InstructionResult> outOfEU = new Connection<>();
		RegisterFile rf = new RegisterFile();
		components.add(new ExecutionUnit(intoEU.getOutputEnd(), outOfEU.getInputEnd()));
		components.add(new InstructionDecoder(intoEU.getInputEnd(), rf));
		components.add(new WritebackHandler(outOfEU.getOutputEnd(), rf));
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
