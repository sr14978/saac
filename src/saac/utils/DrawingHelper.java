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
		
	public static void drawArrow(Graphics2D gc, int x, int y) {
		gc.setColor(Color.BLACK);
		int width = 20;
		int height = 10;
		int[] xCoords = new int[] {x-width/2, x, x+width/2};
		int[] yCoords = new int[] {y, y+height, y};
		gc.fillPolygon(xCoords, yCoords, 3);
	}
}
