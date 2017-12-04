package saac.clockedComponents;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import saac.dataObjects.Instruction;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.FullChannelException;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Output;


public class DualReservationStation implements ClockedComponentI, VisibleComponentI, ClearableComponent{

	List<FConnection<Instruction>.Input> outputUnits;	
	FConnection<Instruction>.Output input;
	Connection<Boolean>.Input emptyFlag;
	List<Instruction> buffer = new LinkedList<>();
	static int bufferLimit = 3;
	int nextOutputToUse = 0;
	
	public DualReservationStation(List<FConnection<Instruction>.Input> outputUnits,
			FConnection<Instruction>.Output input, Connection<Boolean>.Input emptyFlag) {
		this.outputUnits = outputUnits;
		this.input = input;
		this.emptyFlag = emptyFlag;
		emptyFlag.put(true);
	}

	@Override
	public void tick() throws Exception {
		if(buffer.size() >= bufferLimit)
			return;
		if(!input.ready())
			return;
		buffer.add(input.pop());
		if(buffer.size() != 0)
			emptyFlag.put(false);
	}

	@Override
	public void tock() throws Exception {
		if(buffer.size() < 1)
			return;
		for(int i = 0; i<outputUnits.size(); i++) {
			int j = nextOutputToUse;
			nextOutputToUse = (nextOutputToUse + 1) % outputUnits.size();
			output(outputUnits.get(j), buffer, "EU " + j);
			if(buffer.isEmpty())
				break;
		}
		if(buffer.size() == 0)
			emptyFlag.put(true);
	}
	
	public static boolean output(FConnection<Instruction>.Input input, List<Instruction> buffer, String name) throws FullChannelException {
		if(input.clear()) {
			Instruction inst = buffer.remove(0);
			input.put(inst);
			Output.info.println(inst + " sent for execution on " + name);
			return true;
		}
		return false;
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

	@Override
	public void clear() {
		buffer.clear();
	}
}
