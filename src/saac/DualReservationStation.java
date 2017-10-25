package saac;

import java.util.List;
import java.util.LinkedList;

public class DualReservationStation implements ClockedComponent{
	FConnection<Instruction>.Input outputUnit1;
	FConnection<Instruction>.Input outputUnit2;
	FConnection<Instruction>.Output input;
	List<Instruction> buffer = new LinkedList<>();
	
	public DualReservationStation(FConnection<Instruction>.Input outputUnit1, FConnection<Instruction>.Input outputUnit2,
			FConnection<Instruction>.Output input) {
		this.outputUnit1 = outputUnit1;
		this.outputUnit2 = outputUnit2;
		this.input = input;
	}

	@Override
	public void tick() throws Exception {
		if(!input.ready())
			return;
		buffer.add(input.get());
	}

	@Override
	public void tock() throws Exception {
		if(buffer.size() < 1)
			return;
		
		if(outputUnit1.clear()) {
			Instruction inst = buffer.remove(0);
			outputUnit1.put(inst);
			System.out.println(inst + " sent for execution on EU 1");
		} else if(outputUnit2.clear()) {
			Instruction inst = buffer.remove(0);
			outputUnit2.put(inst);
			System.out.println(inst + " sent for execution on EU 2");
		}
		
	}

}
