package saac.interfaces;

import java.awt.Graphics2D;
import java.awt.Point;

public interface ComponentViewI {
	Point getPosition();
	void paint(Graphics2D g) throws Exception;
}
