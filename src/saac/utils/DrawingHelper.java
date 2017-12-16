package saac.utils;

import java.awt.Graphics2D;

import java.awt.Color;

public class DrawingHelper {
	
	public static final int BOX_SIZE = 300;
	
	public static void drawBox(Graphics2D gc, String name) {
		drawBox(gc, name, BOX_SIZE, 50);
	}
	
	public static void drawBox(Graphics2D gc, String name, int w, int h) {
		drawBox(gc, name, 0, 0, w, h);
	}
	
	public static void drawBox(Graphics2D gc, String name, int x, int y, int w, int h) {
		drawBox(gc, name, x, y, w, h, Color.GRAY, Color.BLACK);
	}
	
	public static void drawBox(Graphics2D gc, String name, int x, int y, int w, int h, Color fill, Color line) {
		gc.setColor(fill);
		gc.fillRect(x, y, w, h);
		gc.setColor(line);
		gc.drawRect(x, y, w, h);
		gc.drawString(name, x+2, y+12);
	}
		
	public enum Orientation {Up, Down};
	public static void drawArrow(Graphics2D gc, int x, int y) {
		drawArrow(gc, x, y, Orientation.Down);
	}
	
	public static void drawArrow(Graphics2D gc, int x, int y, Orientation o) {
		gc.setColor(Color.BLACK);
		final int width = 20;
		final int height = 10;
		final int[] xCoords = new int[] {x-width/2, x, x+width/2};
		final int[] yCoords;
		if(o.equals(Orientation.Down)) {
			yCoords = new int[] {y, y+height, y};
		} else {
			yCoords = new int[] {y+height, y, y+height};
		}
		gc.fillPolygon(xCoords, yCoords, 3);
	}
}
