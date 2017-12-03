package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;

import saac.dataObjects.RegisterResult;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;


public class RegisterFile implements VisibleComponentI, ClockedComponentI, ClearableComponent{

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
	
	Connection<Integer[]>.Output readInputA;
	Connection<Integer[]>.Input readOutputAReg;
	Connection<Integer[]>.Output readInputB;
	Connection<Integer[]>.Input readOutputBReg;
	Connection<Integer[]>.Output readInputC;
	Connection<Integer[]>.Input readOutputCReg;
	FConnection<RegisterResult>.Output writeInputs;
	
	public RegisterFile(
			Connection<Integer[]>.Output readInputA,
			Connection<Integer[]>.Input readOutputAReg,
			Connection<Integer[]>.Output readInputB,
			Connection<Integer[]>.Input readOutputBReg,
			Connection<Integer[]>.Output readInputC,
			Connection<Integer[]>.Input readOutputCReg,
			FConnection<RegisterResult>.Output writeInputs
			) {
		this.readInputA = readInputA;
		this.readOutputAReg = readOutputAReg;
		this.readInputB = readInputB;
		this.readOutputBReg = readOutputBReg;
		this.readInputC = readInputC;
		this.readOutputCReg = readOutputCReg;
		this.writeInputs = writeInputs;
	}
	
	@Override
	public void tick() throws Exception {
	}

	@Override
	public void tock() throws Exception {
		Integer[] a = readInputA.get();
		if(a != null)
			for(int i = 0; i<a.length; i++)
				if(a[i] != null)
					a[i] = get(a[i]);
		readOutputAReg.put(a);
		
		Integer[] b = readInputB.get();
		if(b != null)
			for(int i = 0; i<b.length; i++)
				if(b[i] != null)
					b[i] = get(b[i]);
		readOutputBReg.put(b);
		
		Integer[] c = readInputC.get();
		if(c != null)
			for(int i = 0; i<c.length; i++)
				if(c[i] != null)
					c[i] = get(c[i]);
		readOutputCReg.put(c);
		
		if(writeInputs.ready()) {
			RegisterResult res = writeInputs.pop();
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
				gc.drawString(Integer.toString(values[i]), 25*i+5, 40);
				if(dirtyBits[i])
					gc.drawString("(d)", 25*i+5, 25);
			}
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear() {
		for(int i = 0; i<dirtyBits.length; i++) {
			dirtyBits[i] = false;
		}
	}
}
