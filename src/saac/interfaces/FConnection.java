package saac.interfaces;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.utils.DrawingHelper;

public class FConnection<T> implements VisibleComponent{

	private T value;
	
	public Input getInputEnd() {
		return new Input();
	}
	
	public Output getOutputEnd() {
		return new Output();
	}
	
	public class Input {
		public void put(T val) throws FullChannelException {
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
		public T get() throws FullChannelException {
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
	}
	
	static final int C_BOX_SIZE = BOX_SIZE-50;
	class View implements ComponentView {
		Point position; 
		View(int x, int y){
			position = new Point(x + (BOX_SIZE - C_BOX_SIZE)/2, y+15);
		}
		
		public void paint(GraphicsContext gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawArrow(gc, C_BOX_SIZE/2, -12);
			DrawingHelper.drawArrow(gc, C_BOX_SIZE/2, 23);
			DrawingHelper.drawBox(gc, "", 0, 0, C_BOX_SIZE, 20, Color.LIGHTGRAY, Color.BLACK);
			if(value != null) {
				if(value instanceof int[]) {
					int[] val = (int[]) value;
					if(val.length > 3)
						gc.fillText(val[0] + " " + val[1] + " " + val[2] + " " + val[3], 5, 15);
				} else
					gc.fillText(value.toString(), 5, 15);
			}
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}
}
