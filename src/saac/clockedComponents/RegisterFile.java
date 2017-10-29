package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;

import saac.dataObjects.RegisterResult;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;


public class RegisterFile implements VisibleComponentI, ClockedComponentI{

	static final int registerNum = 12;
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
	
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Register File");
			gc.setColor(Color.BLACK);
			for( int i = 0; i<registerNum; i++) {
				gc.drawString(Integer.toString(values[i]) + (dirtyBits[i]?"(d)":"  "), 33*i+5, 30);
			}
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
}
