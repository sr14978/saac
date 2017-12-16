package saac.interfaces;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import saac.dataObjects.Instruction.Instruction;
import saac.utils.DrawingHelper;

public class FListConnection<T extends Object> implements VisibleComponentI, ClearableComponent{

	private T[] value;
	
	public Input getInputEnd() {
		return new Input();
	}
	
	public Output getOutputEnd() {
		return new Output();
	}
	
	public class Input {
		public <H extends T> void put(H[] val) throws ChannelException {
			if(value == null)
				value = val;
			else
				throw new ChannelException();
		}
		public boolean clear() {
			return value == null;
		}
	}
	
	public class Output {
		public T[] pop() throws ChannelException {
			if(value == null) {
				throw new ChannelException();
			} else {
				T[] val = value;
				value = null;
				return val;
			}
		}
		public boolean ready() {
			return value != null;
		}
		public T[] peak() throws ChannelException {
			if(value == null)
				throw new ChannelException();
			else
				return value;
		}
	}
	
	static final int C_BOX_SIZE = BOX_SIZE-50;
	class View extends ComponentView {

		int num;
		View(int x, int y, int num){
			super(x + (BOX_SIZE - C_BOX_SIZE)/2, y+15);
			this.num = num;
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
		return new View(x, y, 1);
	}
	
	public ComponentViewI createView(int x, int y, int num) {
		return new View(x, y, num);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void clear(int i) {
		if(value == null)
			return;
		if(value instanceof Instruction[]) {
			List<T> keeps = new LinkedList<>();
			for(T val : value)
				if(((Instruction) val).getVirtualNumber() <= i)
					keeps.add(val);
			if(!keeps.isEmpty())
				value = keeps.toArray((T[]) java.lang.reflect.Array.newInstance(value[0].getClass(), 0));
			else
				value = null;
		} else
			throw new RuntimeException(value.toString());
	}
}
