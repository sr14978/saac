package saac;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class DrawingHelper {
	
	static final int BOX_SIZE = 300;
	
	static void drawBox(GraphicsContext gc, String name) {
		drawBox(gc, name, BOX_SIZE, 50);
	}
	
	static void drawBox(GraphicsContext gc, String name, int w, int h) {
		drawBox(gc, name, 0, 0, w, h);
	}
	
	static void drawBox(GraphicsContext gc, String name, int x, int y, int w, int h) {
		drawBox(gc, name, x, y, w, h, Color.GRAY, Color.BLACK);
	}
	
	static void drawBox(GraphicsContext gc, String name, int x, int y, int w, int h, Color fill, Color line) {
		gc.setFill(fill);
		gc.fillRect(x, y, w, h);
		gc.setFill(line);
		gc.strokeRect(x, y, w, h);
		gc.fillText(name, x+2, y+12);
	}
}
