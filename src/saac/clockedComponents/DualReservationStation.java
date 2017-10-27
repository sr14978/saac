package saac.clockedComponents;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.dataObjects.Instruction;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponent;
import saac.utils.DrawingHelper;


public class DualReservationStation implements ClockedComponent, VisibleComponent{
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
			System.out.println(inst + " sent for execution on EU 1");
		} else if(outputUnit2.clear()) {
			Instruction inst = buffer.remove(0);
			outputUnit2.put(inst);
			System.out.println(inst + " sent for execution on EU 2");
		}
		
	}
	
	class View implements ComponentView {
		
		Point position; 
		View(int x, int y){
			position = new Point(x, y);
		}
		
		public void paint(GraphicsContext gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawBox(gc, "Dual Reservation Station", 2*BOX_SIZE, 50);
			gc.setFill(Color.BLACK);
			for( int i = 0; i<buffer.size(); i++)
				gc.fillText(buffer.get(i).toString(), 5, 25+10*i);
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}
}
