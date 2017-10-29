package saac.clockedComponents;

import java.awt.Graphics2D;
import java.awt.Point;

import java.awt.Color;
import saac.dataObjects.Instruction;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponent;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;

public class Decoder implements ClockedComponent, VisibleComponent{

	FConnection<Instruction>.Input output;
	FConnection<int[]>.Output input;
	Instruction bufferOut;
	
	public Decoder(FConnection<Instruction>.Input output, FConnection<int[]>.Output input) {
		this.output = output;
		this.input = input;
	}

	@Override
	public void tick() throws Exception {
		if(bufferOut != null)
			return;
		
		if(!input.ready())
			return;
		int[] data = input.get();
		
		bufferOut = new Instruction(Opcode.fromInt(data[0]), data[1], data[2], data[3]);
	}

	@Override
	public void tock() throws Exception {
		if(bufferOut == null)
			return;
		if(output.clear()) {
			output.put(bufferOut);
			bufferOut = null;
		}
	}
		
	class View implements ComponentView {
		
		Point position; 
		View(int x, int y){
			position = new Point(x, y);
		}
		
		public void paint(Graphics2D gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawBox(gc, "Decoder");
			gc.setColor(Color.BLACK);
			if(bufferOut != null)
				gc.drawString(bufferOut.toString(), 10, 30);
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}
	
}
