package saac;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import static saac.DrawingHelper.BOX_SIZE;

public class FConnection<T> implements VisibleComponent{

	private T value;
	
	Input getInputEnd() {
		return new Input();
	}
	
	Output getOutputEnd() {
		return new Output();
	}
	
	class Input {
		void put(T val) throws FullChannelException {
			if(value == null)
				value = val;
			else
				throw new FullChannelException();
		}
		boolean clear() {
			return value == null;
		}
	}
	
	class Output {
		T get() throws FullChannelException {
			if(value == null) {
				throw new FullChannelException();
			} else {
				T val = value;
				value = null;
				return val;
			}
			
		}
		boolean ready() {
			return value != null;
		}
	}
	
	static final int C_BOX_SIZE = BOX_SIZE-50;
	static final double[] arrowX = new double[] {C_BOX_SIZE/2-10, C_BOX_SIZE/2, C_BOX_SIZE/2+10};
	static final double[] arrowY_in = new double[] {-12, -2, -12};
	static final double[] arrowY_out = new double[] {23, 33, 23};
	class View implements ComponentView {
		Point position; 
		View(int x, int y){
			position = new Point(x + (BOX_SIZE - C_BOX_SIZE)/2, y+15);
		}
		
		public void paint(GraphicsContext gc) {
			gc.translate(position.x, position.y);
			gc.setFill(Color.BLACK);
			gc.fillPolygon(arrowX, arrowY_in, 3);
			gc.fillPolygon(arrowX, arrowY_out, 3);
			gc.setFill(Color.LIGHTGRAY);
			gc.fillRect(0, 0, C_BOX_SIZE, 20);
			gc.setFill(Color.BLACK);
			gc.strokeRect(0, 0, C_BOX_SIZE, 20);
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
