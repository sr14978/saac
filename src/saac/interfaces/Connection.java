package saac.interfaces;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;

import saac.utils.DrawingHelper;

public class Connection<T> implements VisibleComponentI{
	
	private T value;
	
	public Input getInputEnd() {
		return new Input();
	}
	
	public Output getOutputEnd() {
		return new Output();
	}
	
	public class Input {
		public void put(T val) {
			value = val;
		}
	}
	
	public class Output {
		public T get() {
			return value;
		}
	}

	class View extends ComponentView {
			
		int num;
		View(int x, int y, int num){
			super(x, y+15);
			this.num = num;
		}
		
		@Override
		public void paint(Graphics2D gc) {
			DrawingHelper.drawArrow(gc, BOX_SIZE/(2*num), -12);
			DrawingHelper.drawArrow(gc, BOX_SIZE/(2*num), 23);
			DrawingHelper.drawBox(gc, "", 0, 0, BOX_SIZE/num, 20, Color.LIGHT_GRAY, Color.BLACK);
			if(value != null) {
				gc.drawString(value.toString(), 5, 15);
			}
		}
	}
	
	@Override
	public ComponentViewI createView(int x, int y) {
		return createView(x,y,3);
	}
	
	public ComponentViewI createView(int x, int y, int num) {
		return new View(x,y,num);
	}
	
}
