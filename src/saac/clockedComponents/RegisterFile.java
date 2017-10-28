package saac.clockedComponents;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.dataObjects.RegisterResult;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.Connection;
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
			return 0;
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
	
	Connection<Integer>.Output readInputA;
	Connection<Integer>.Input readOutputAReg;
	Connection<Integer>.Input readOutputAPass;
	Connection<Integer>.Output readInputB;
	Connection<Integer>.Input readOutputBReg;
	Connection<Integer>.Input readOutputBPass;
	Connection<Integer>.Output readInputC;
	Connection<Integer>.Input readOutputCReg;
	Connection<Integer>.Input readOutputCPass;
	FConnection<RegisterResult>.Output writeInputs;
	
	public RegisterFile(
			Connection<Integer>.Output readInputA,
			Connection<Integer>.Input readOutputAReg,
			Connection<Integer>.Input readOutputAPass,
			Connection<Integer>.Output readInputB,
			Connection<Integer>.Input readOutputBReg,
			Connection<Integer>.Input readOutputBPass,
			Connection<Integer>.Output readInputC,
			Connection<Integer>.Input readOutputCReg,
			Connection<Integer>.Input readOutputCPass,
			FConnection<RegisterResult>.Output writeInputs
			) {
		this.readInputA = readInputA;
		this.readOutputAReg = readOutputAReg;
		this.readOutputAPass = readOutputAPass;
		this.readInputB = readInputB;
		this.readOutputBReg = readOutputBReg;
		this.readOutputBPass = readOutputBPass;
		this.readInputC = readInputC;
		this.readOutputCReg = readOutputCReg;
		this.readOutputCPass = readOutputCPass;
		this.writeInputs = writeInputs;
	}
	
	@Override
	public void tick() throws Exception {
	}

	@Override
	public void tock() throws Exception {
		
		readOutputAPass.put(readInputA.get());
		readOutputBPass.put(readInputB.get());
		readOutputCPass.put(readInputC.get());
		
		readOutputAReg.put(get(readInputA.get()==null?0:readInputA.get()));
		readOutputBReg.put(get(readInputB.get()==null?0:readInputB.get()));
		readOutputCReg.put(get(readInputC.get()==null?0:readInputC.get()));
				
		if(writeInputs.ready()) {
			RegisterResult res = writeInputs.get();
			set(res.getTarget(), res.getValue());
		}
	}	
	
	class View implements ComponentView {
		
		Point position; 
		View(int x, int y){
			position = new Point(x, y);
		}
		
		public void paint(GraphicsContext gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawBox(gc, "Register File");
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
