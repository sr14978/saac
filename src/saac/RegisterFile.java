package saac;

public class RegisterFile{

	static final int registerNum = 10;
	private int[] values = new int[registerNum];
	private boolean[] dirtyBits = new boolean[registerNum];
	
	void set(int index, int value) {
		if(index < registerNum && index >= 0)
			values[index] = value;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	int get(int index) {
		if(index < registerNum && index >= 0)
			return values[index];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	void setDirty(int index, boolean bool) {
		if(index < registerNum && index >= 0)
			dirtyBits[index] = bool;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
	boolean isDirty(int index) {
		if(index < registerNum && index >= 0)
			return dirtyBits[index];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
}
