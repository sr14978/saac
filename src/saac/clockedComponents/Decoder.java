package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;

import saac.dataObjects.Instruction;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;

public class Decoder implements ClockedComponentI, VisibleComponentI{

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
		
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Decoder");
			gc.setColor(Color.BLACK);
			if(bufferOut != null)
				gc.drawString(bufferOut.toString(), 10, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
	
}
