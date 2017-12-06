package saac.interfaces;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;

import saac.dataObjects.InstructionI;
import saac.utils.DrawingHelper;

public class FConnection<T extends Object> implements VisibleComponentI, ClearableComponent{

	private T value;
	
	public Input getInputEnd() {
		return new Input();
	}
	
	public Output getOutputEnd() {
		return new Output();
	}
	
	public class Input {
		public <H extends T> void put(H val) throws FullChannelException {
			if(value == null)
				value = val;
			else
				throw new FullChannelException();
		}
		public boolean clear() {
			return value == null;
		}
	}
	
	public class Output {
		public T pop() throws FullChannelException {
			if(value == null) {
				throw new FullChannelException();
			} else {
				T val = value;
				value = null;
				return val;
			}
		}
		public boolean ready() {
			return value != null;
		}
		public T peak() throws FullChannelException {
			if(value == null)
				throw new FullChannelException();
			else
				return value;
		}
	}
	
	static final int C_BOX_SIZE = BOX_SIZE-50;
	class View extends ComponentView {

		View(int x, int y){
			super(x + (BOX_SIZE - C_BOX_SIZE)/2, y+15);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawArrow(gc, C_BOX_SIZE/2, -12);
			DrawingHelper.drawArrow(gc, C_BOX_SIZE/2, 23);
			DrawingHelper.drawBox(gc, "", 0, 0, C_BOX_SIZE, 20, Color.LIGHT_GRAY, Color.BLACK);
			if(value != null) {
				if(value instanceof int[][]) {
					int[][] vals = (int[][]) value;
					StringBuilder sb = new StringBuilder();
					sb.append("[");
					for(int[] val : vals)
						if(val.length > 3)
							sb.append(val[0] + " " + val[1] + " " + val[2] + " " + val[3] + ", ");
					sb.append("]");
					gc.drawString(sb.toString(), 5, 15);
				} else if(value instanceof Object[]) {
					Object[] vals = (Object[]) value;
					gc.drawString(Arrays.toString(vals), 5, 15);
				} else 
					gc.drawString(value.toString(), 5, 15);
			}
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear(int i) {
		if(value == null)
			return;
		if(value instanceof InstructionI && ((InstructionI) value).getID() > i)
			value = null;
		else if(value instanceof int[])
			value = null;			
	}
}
