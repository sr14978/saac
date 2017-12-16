package saac.interfaces;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;

import saac.utils.DrawingHelper;

public class BufferedConnection<T> implements VisibleComponentI, ClearableComponent{

	private int MAX_BUFFER_SIZE = 10;
	private List<T> buffer = new LinkedList<>();
	
	public BufferedConnection(int size) {
		this.MAX_BUFFER_SIZE = size;
	}
	
	public Input getInputEnd() {
		return new Input();
	}
	
	public Output getOutputEnd() {
		return new Output();
	}
	
	public class Input {
		public <H extends T> void put(H val) throws ChannelException {
			if(buffer.size() < MAX_BUFFER_SIZE)
				buffer.add(val);
			else
				throw new ChannelException();
		}
		public boolean notFull() {
			return buffer.size() < MAX_BUFFER_SIZE;
		}
		public boolean empty() {
			return buffer.isEmpty();
		}
	}
	
	public class Output {
		public T pop() throws ChannelException {
			if(buffer.isEmpty())
				throw new ChannelException();
			else
				return buffer.remove(0);
			
		}
		public boolean ready() {
			return !buffer.isEmpty();
		}
		public T peak() throws ChannelException {
			if(buffer.isEmpty())
				throw new ChannelException();
			else
				return buffer.get(0);
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
			for(int i = 0; i< buffer.size(); i++) {
				T value = buffer.get(i);
				if(value instanceof int[]) {
					int[] val = (int[]) value;
					if(val.length > 3)
						gc.drawString(val[0] + " " + val[1] + " " + val[2] + " " + val[3], 5, 15);
				} else
					gc.drawString(value.toString(), 5 + 20*i, 15);
			}
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear(int i) {
		buffer.clear();
	}
}
