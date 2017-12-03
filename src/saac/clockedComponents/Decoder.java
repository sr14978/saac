package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import saac.dataObjects.Instruction;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;

public class Decoder implements ClockedComponentI, VisibleComponentI, ClearableComponent{

	FConnection<Instruction[]>.Input output;
	FConnection<int[][]>.Output input;
	Instruction[] bufferOut;
	
	
	public Decoder(FConnection<Instruction[]>.Input output, FConnection<int[][]>.Output input) {
		this.output = output;
		this.input = input;
	}

	@Override
	public void tick() throws Exception {
		if(bufferOut != null)
			return;
		
		if(!input.ready())
			return;
		int[][] data = input.pop();
		List<Instruction> outInsts = new LinkedList<>();
		for(int i = 0; i<data.length; i++) {
			int[] inst = data[i];
			outInsts.add(new Instruction(inst[5], Opcode.fromInt(inst[0]), inst[1], inst[2], inst[3], inst[4]));
		}
		bufferOut = outInsts.toArray(new Instruction[0]);
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
				gc.drawString(Arrays.toString(bufferOut), 10, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear() {
		bufferOut = null;
	}
	
}
