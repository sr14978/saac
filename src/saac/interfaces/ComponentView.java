package saac.interfaces;

import java.awt.Point;

public abstract class ComponentView implements ComponentViewI{

	Point position; 
	protected ComponentView(int x, int y){
		position = new Point(x, y);
	}

	public Point getPosition() {
		return position;
	}
}
