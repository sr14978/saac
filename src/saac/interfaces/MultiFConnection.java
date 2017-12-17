package saac.interfaces;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import saac.utils.DrawingHelper;
import saac.utils.DrawingHelper.Orientation;

public class MultiFConnection<T extends Object> implements VisibleComponentI {
	
	private List<T> value;
	int seenNum;
	boolean[] seens;
	int givenNum;
	int fanOutMax;
	
	public MultiFConnection(int fanOutMax) {
		this.fanOutMax = fanOutMax;
		value = Collections.synchronizedList(new ArrayList<>());
		seenNum = 0;
		givenNum = 0;
		seens = new boolean[fanOutMax];
	}
	
	public Input getInputEnd() throws Exception {
		return new Input();
	}
	
	public Output getOutputEnd() throws Exception {
		if(givenNum == fanOutMax) {
			throw new ChannelException();
		}
		return new Output(givenNum++);
	}
	
	public class Input {
		
		public <H extends T> void put(H val) throws ChannelException {
			if(clear()) {
				if(seenNum == fanOutMax) {
					seenNum = 0;
					for(int i = 0; i<fanOutMax; i++) {
						seens[i] = false;
					}
				}
				value.add(val);
			} else {
				throw new ChannelException();
			}
		}
		public boolean clear() {
			return seenNum == 0 || seenNum == fanOutMax;
		}
	}
	
	public class Output {
		final int num;
		Output(int num) {
			this.num = num;
		}
		
		public List<T> pop() throws ChannelException {
			if(ready()) {
				seenNum++;
				seens[num] = true;
				List<T> val = new ArrayList<>(value);
				if(seenNum == fanOutMax) {
					value.clear();
				}
				return val;
			} else {
				throw new ChannelException();
			}
		}
		public boolean ready() {
			return !seens[num] && seenNum < fanOutMax && !value.isEmpty();
		}
	}
	
	static final int C_BOX_SIZE = BOX_SIZE-50;
	class View extends ComponentView {

		int num;
		View(int x, int y, int num){
			super(x + (BOX_SIZE - C_BOX_SIZE)/2, y+15);
			this.num = num;
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawArrow(gc, C_BOX_SIZE/2, -12, Orientation.Up);
			DrawingHelper.drawArrow(gc, C_BOX_SIZE/2, 23, Orientation.Up);
			DrawingHelper.drawBox(gc, "", 0, 0, C_BOX_SIZE, 20, Color.LIGHT_GRAY, Color.BLACK);
			gc.drawString(Integer.toString(seenNum), 5, 15);
			
			gc.drawString(value.toString(), 15, 15);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y, 1);
	}
	
	public ComponentViewI createView(int x, int y, int num) {
		return new View(x, y, num);
	}

}
