package saac;

public class RegisterFile{

	static final int registerNum = 10;
	static final int PC = registerNum;
	private int[] values = new int[registerNum];
	private boolean[] dirtyBits = new boolean[registerNum];
	private int pc;
	private boolean dirtyPC;
	
	void set(int index, int value) {
		if(index < registerNum && index >= 0)
			values[index] = value;
		else if(index == PC)
			pc = value;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	int get(int index) {
		if(index < registerNum && index >= 0)
			return values[index];
		else if(index == PC)
			return pc;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	void setDirty(int index, boolean bool) {
		if(index < registerNum && index >= 0)
			dirtyBits[index] = bool;
		else if(index == PC)
			dirtyPC = bool;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	boolean isDirty(int index) {
		if(index < registerNum && index >= 0)
			return dirtyBits[index];
		else if(index == PC)
			return dirtyPC;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
}
