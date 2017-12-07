package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

import saac.Settings;
import saac.dataObjects.InstructionResult;
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
	
	public final static int BUFF_SIZE = Settings.VIRTUAL_ADDRESS_NUM;
	InstructionResult[] reorderBuffer = new InstructionResult[BUFF_SIZE];
	int bufferIndexStart = 0;
	int bufferIndexEnd = 0;
	int bufferInstructionStart = 0;
	
	public static class RegItem {
		public Integer value;
		public Reg type;
		RegItem(Integer value, Reg type) {
			this.value = value;
			this.type = type;
		}
		public String toString() { return value.toString() + (type==Reg.Virtual?"(v)":""); }
	}
	public enum Reg {Architectural, Virtual, Data};
	
	public boolean insert(InstructionResult res) throws Exception {
		if(res.getID() < bufferInstructionStart + BUFF_SIZE) {
			
			int instructionOffset = res.getID() - bufferInstructionStart;
			if(instructionOffset > BUFF_SIZE)
				return false;
			
			int bufferIndex = (bufferIndexStart + instructionOffset) % BUFF_SIZE;
			if(instructionOffset >= BUFF_SIZE - bufferIndexStart && bufferIndex >= bufferIndexStart )
				return false;

			reorderBuffer[bufferIndex] = res;

			if( (res.getID() - bufferInstructionStart + 1)
					> (bufferIndexEnd - bufferIndexStart + BUFF_SIZE) % BUFF_SIZE ) {
				bufferIndexEnd = (bufferIndex + 1) % BUFF_SIZE;
			}
			
			return true;
		} else 
			return false;
	}
	
	public InstructionResult getFirst() {
		return reorderBuffer[bufferIndexStart];
	}
	
	public void clearFirst() {
		reorderBuffer[bufferIndexStart] = null;
		bufferIndexStart = (bufferIndexStart + 1) % BUFF_SIZE;
		bufferInstructionStart++;
	}
	
	public void clearAfter() {
		int bufferIndex = bufferIndexStart;
		while(bufferIndex != bufferIndexEnd) {
			reorderBuffer[bufferIndexStart] = null;
			bufferIndex = (bufferIndex + 1) % BUFF_SIZE;
		}
		bufferIndexEnd = bufferIndex;
	}
	
	public InstructionResult getOffsetted(int offset) {
		return reorderBuffer[(bufferIndexStart+(offset-bufferInstructionStart)) % BUFF_SIZE];
	}
	
	public boolean inReorderBuffer(int addr) {
		return addr >= bufferInstructionStart && addr < bufferInstructionStart + BUFF_SIZE;
	}
	
	public void set(int index, int value) {
		if(index < registerNum && index >= 0)
			values[index] = value;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	public int get(int index, Reg type) {
		switch(type) {
		case Architectural:
			if(index < registerNum && index >= 0)
				return values[index];
			else 
				return 0;
		case Virtual:
			if(index < bufferInstructionStart)
				return 0;
			int instructionOffset = index - bufferInstructionStart;
			if(instructionOffset > BUFF_SIZE)
				return 0;
			int bufferIndex = (bufferIndexStart + instructionOffset) % BUFF_SIZE;
			if(instructionOffset >= BUFF_SIZE - bufferIndexStart && bufferIndex >= bufferIndexStart )
				return 0;
			Object obj = reorderBuffer[bufferIndex];
			if(obj == null)
				return 0;
			if(!(obj instanceof RegisterResult))
				return 0;
			return ((RegisterResult) obj).getValue();	
		case Data:
			return 0;
		}
		throw new RuntimeException();
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
	
	Connection<RegItem[]>.Output readInputA;
	Connection<Integer[]>.Input readOutputAReg;
	Connection<RegItem[]>.Output readInputB;
	Connection<Integer[]>.Input readOutputBReg;
	Connection<RegItem[]>.Output readInputC;
	Connection<Integer[]>.Input readOutputCReg;
	FConnection<RegisterResult>.Output writeInputs;
	
	public RegisterFile(
			Connection<RegItem[]>.Output readInputA,
			Connection<Integer[]>.Input readOutputAReg,
			Connection<RegItem[]>.Output readInputB,
			Connection<Integer[]>.Input readOutputBReg,
			Connection<RegItem[]>.Output readInputC,
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
		resetRAT();
	}
	
	@Override
	public void tick() throws Exception {
		if(writeInputs.ready()) {
			RegisterResult res = writeInputs.pop();
			set(res.getTarget(), res.getValue());
			if(getRAT(res.getTarget()).type == Reg.Virtual && getRAT(res.getTarget()).value == res.getID())
				setRAT(res.getTarget(), res.getTarget(), Reg.Architectural);
		}
	}

	@Override
	public void tock() throws Exception {
		RegItem[] a_in = readInputA.get();
		if(a_in != null) {
			Integer[] a_out = new Integer[a_in.length];
			for(int i = 0; i<a_in.length; i++)
				if(a_in[i] != null)
					a_out[i] = get(a_in[i].value, a_in[i].type);
			readOutputAReg.put(a_out);
		}
		
		RegItem[] b_in = readInputB.get();
		if(b_in != null) {
			Integer[] b_out = new Integer[b_in.length];

			for(int i = 0; i<b_in.length; i++)
				if(b_in[i] != null)
					b_out[i] = get(b_in[i].value, b_in[i].type);
			readOutputBReg.put(b_out);
		}
		
		RegItem[] c_in = readInputC.get();
		if(c_in != null) {
			Integer[] c_out = new Integer[c_in.length];
			for(int i = 0; i<c_in.length; i++)
				if(c_in[i] != null)
					c_out[i] = get(c_in[i].value, c_in[i].type);
			readOutputCReg.put(c_out);
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
				gc.drawString(Integer.toString(values[i]), 25*i+5, 35);
				if(dirtyBits[i])
					gc.drawString("(d)", 25*i+5, 20);
			}
			gc.drawString(RAT.toString(), 5, 45);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	public void clearDirties() {
		for(int j = 0; j<dirtyBits.length; j++)
			dirtyBits[j] = false;
	}
	
	Map<Integer, RegItem> RAT = new HashMap<>();
	
	public void resetRAT() {
		for(int j = 0; j<registerNum; j++)
			RAT.put(j, new RegItem(j, Reg.Architectural));
	}
	
	public void resetRAT(int i) {
		for(int j = 0; j<registerNum; j++)
			if(RAT.get(j).type == Reg.Virtual && RAT.get(j).value > i)
				RAT.put(j, new RegItem(j, Reg.Architectural));
	}
	
	public void setRAT(int a, int b, Reg c) { 
		RAT.put(a, new RegItem(b, c));
	}
	
	public RegItem getRAT(int a) { 
		return RAT.get(a);
	}

	@Override
	public void clear(int i) {
		resetRAT(i);
	}
	
}
