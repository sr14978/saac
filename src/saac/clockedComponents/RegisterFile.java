package saac.clockedComponents;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.dataObjects.RegisterResult;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponent;
import saac.utils.DrawingHelper;


public class RegisterFile implements VisibleComponent, ClockedComponent{

	static final int registerNum = 10;
	static final int PC = registerNum;
	private int[] values = new int[registerNum];
	private boolean[] dirtyBits = new boolean[registerNum];

	public void set(int index, int value) {
		if(index < registerNum && index >= 0)
			values[index] = value;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	public int get(int index) {
		if(index < registerNum && index >= 0)
			return values[index];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	public void setDirty(int index, boolean bool) {
		if(index < registerNum && index >= 0)
			dirtyBits[index] = bool;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	public boolean isDirty(int index) {
		if(index < registerNum && index >= 0)
			return dirtyBits[index];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	FConnection<Integer>.Output readInput;
	FConnection<Integer>.Input readOutput;
	FConnection<RegisterResult>.Output writeInputs;
	
	public RegisterFile(
			FConnection<Integer>.Output readInput,
			FConnection<Integer>.Input readOutput,
			FConnection<RegisterResult>.Output writeInputs
			) {
	this.readInput = readInput;
	this.readOutput = readOutput;
	this.writeInputs = writeInputs;
	}
	
	@Override
	public void tick() throws Exception {
		if(readInput.ready() && readOutput.clear()) {
			readOutput.put(get(readInput.get()));
		}
		
		if(writeInputs.ready()) {
			RegisterResult res = writeInputs.get();
			set(res.getTarget(), res.getValue());
		}
		
	}

	@Override
	public void tock() throws Exception {
		
		
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
