package saac.clockedComponents;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import saac.dataObjects.Instruction;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Output;


public class DualReservationStation implements ClockedComponentI, VisibleComponentI{
	FConnection<Instruction>.Input outputUnit1;
	FConnection<Instruction>.Input outputUnit2;
	FConnection<Instruction>.Output input;
	List<Instruction> buffer = new LinkedList<>();
	static int bufferLimit = 3;
	public DualReservationStation(FConnection<Instruction>.Input outputUnit1, FConnection<Instruction>.Input outputUnit2,
			FConnection<Instruction>.Output input) {
		this.outputUnit1 = outputUnit1;
		this.outputUnit2 = outputUnit2;
		this.input = input;
	}

	@Override
	public void tick() throws Exception {
		if(buffer.size() >= bufferLimit)
			return;
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
			Output.info.println(inst + " sent for execution on EU 1");
		} else if(outputUnit2.clear()) {
			Instruction inst = buffer.remove(0);
			outputUnit2.put(inst);
			Output.info.println(inst + " sent for execution on EU 2");
		}
		
	}
	
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Dual Reservation Station", 2*BOX_SIZE, 50);
			gc.setColor(Color.BLACK);
			for( int i = 0; i<buffer.size(); i++)
				gc.drawString(buffer.get(i).toString(), 5, 25+10*i);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
}
