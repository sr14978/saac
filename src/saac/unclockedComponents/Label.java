package saac.unclockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import saac.interfaces.ComponentView;
import saac.utils.DrawingHelper;

public class Label extends ComponentView {
	
	String text;
	
	public Label(int x, int y, String text) {
		super(x, y);
		this.text = text;
	}
	
	public void paint(Graphics2D gc) {
		gc.setColor(Color.black);
		Rectangle2D rect = gc.getFont().getStringBounds(text, gc.getFontRenderContext());
		gc.drawString(text, (int) (DrawingHelper.BOX_SIZE/2 - rect.getWidth()/2), 20);
	}
}
