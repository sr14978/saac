package saac;

import java.util.List;
import java.util.LinkedList;

public class DualReservationStation implements ClockedComponent{
	Connection<Instruction>.Input outputUnit1;
	Connection<Instruction>.Input outputUnit2;
	Connection<Instruction>.Output input;
	List<Instruction> buffer = new LinkedList<>();
	
	public DualReservationStation(Connection<Instruction>.Input outputUnit1, Connection<Instruction>.Input outputUnit2,
			Connection<Instruction>.Output input) {
		this.outputUnit1 = outputUnit1;
		this.outputUnit2 = outputUnit2;
		this.input = input;
	}

	@Override
	public void tick() throws Exception {
		Instruction inst = input.get();
		if(inst != null)
			buffer.add(inst);		
	}

	@Override
	public void tock() throws Exception {
		if(buffer.size() < 1)
			return;
		
		if(outputUnit1.isEmpty()) {
			Instruction inst = buffer.remove(0);
			outputUnit1.put(inst);
			System.out.println(inst + " sent for execution on EU 1");
		} else if(outputUnit2.isEmpty()) {
			Instruction inst = buffer.remove(0);
			outputUnit2.put(inst);
			System.out.println(inst + " sent for execution on EU 2");
		}
		
	}

}
