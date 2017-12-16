package saac.interfaces;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class MultiFConnection<T extends Object> implements VisibleComponentI {

	private List<T> value;
	int seenNum;
	int fanOutMax;
	
	@SuppressWarnings("unchecked")
	public MultiFConnection(int fanOutMax) {
		this.fanOutMax = fanOutMax;
		value = new ArrayList<>();
		seenNum = 0;	
	}
	
	public Input getInputEnd() throws Exception {
		return new Input();
	}
	
	public Output getOutputEnd() throws Exception {
		return new Output();
	}
	
	public class Input {
		
		public <H extends T> void put(H val) throws ChannelException {
			if(seenNum != 0 && seenNum != fanOutMax) {
				throw new ChannelException();
			}
			if(seenNum == fanOutMax) {
				seenNum = 0;
				value.clear();
			}
			value.add(val);
		}
		public boolean clear() {
			return seenNum == 0 || seenNum == fanOutMax;
		}
	}
	
	public class Output {
		
		public List<T> pop() throws ChannelException {
			if(seenNum < fanOutMax) {
				seenNum++;
				return value;
			} else {
				throw new ChannelException();
			}
		}
		public boolean ready() {
			return seenNum < fanOutMax && !value.isEmpty();
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
