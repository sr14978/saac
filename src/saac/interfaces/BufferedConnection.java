package saac.interfaces;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
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
		public <H extends T> void put(H[] vals) throws ChannelException {
			if(buffer.size() + vals.length <= MAX_BUFFER_SIZE) {
				for(H val : vals)
					buffer.add(val);
			} else {
				throw new ChannelException();
			}
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
		@SuppressWarnings("unchecked")
		public T[] pop(int popNum) throws ChannelException {
			if(popNum < 1 || buffer.isEmpty() || buffer.size() < popNum)
				throw new ChannelException();
			else {
				List<T> out = new ArrayList<T>();
				for(int i = 0; i<popNum; i++) {
					out.add(buffer.remove(0));
				}
				return out.toArray((T[]) java.lang.reflect.Array.newInstance(out.get(0).getClass(), 0));
			}
		}
		@SuppressWarnings("unchecked")
		public T[] popAll() throws ChannelException {
			if(buffer.isEmpty())
				throw new ChannelException();
			else {
				List<T> out = new ArrayList<T>(buffer);
				buffer.clear();
				return out.toArray((T[]) java.lang.reflect.Array.newInstance(out.get(0).getClass(), 0));
			}
			
		}
		public boolean ready() {
			return !buffer.isEmpty();
		}
		public int getCount() {
			return buffer.size();
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
			gc.drawString(Arrays.deepToString(buffer.toArray(new int[0][])), 5, 15);
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
