package saac;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.Instructions.Opcode;

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
		
		public void paint(GraphicsContext gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawBox(gc, "Decoder");
			gc.setFill(Color.BLACK);
			if(bufferOut != null)
				gc.fillText(bufferOut.toString(), 10, 30);
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}
	
}
