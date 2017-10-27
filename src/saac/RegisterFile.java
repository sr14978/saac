package saac;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import static saac.DrawingHelper.BOX_SIZE;


public class RegisterFile implements VisibleComponent{

	static final int registerNum = 10;
	static final int PC = registerNum;
	private int[] values = new int[registerNum];
	private boolean[] dirtyBits = new boolean[registerNum];

	void set(int index, int value) {
		if(index < registerNum && index >= 0)
			values[index] = value;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	int get(int index) {
		if(index < registerNum && index >= 0)
			return values[index];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	void setDirty(int index, boolean bool) {
		if(index < registerNum && index >= 0)
			dirtyBits[index] = bool;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	boolean isDirty(int index) {
		if(index < registerNum && index >= 0)
			return dirtyBits[index];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	class View implements ComponentView {
		
		Point position; 
		View(int x, int y){
			position = new Point(x, y);
		}
		
		public void paint(GraphicsContext gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawBox(gc, "Register File", 2*BOX_SIZE, 50);
			gc.setFill(Color.BLACK);
			for( int i = 0; i<registerNum; i++) {
				gc.fillText(Integer.toString(values[i]) + (dirtyBits[i]?"(d)":"  "), 40*i+5, 30);
			}
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}
}
