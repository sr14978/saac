package saac;

public class Memory {
	static final int addressMax = 0x10000;
	private int[] values = new int[addressMax];
	
	public int getWord(int addr) {
		if(addr < addressMax && addr >= 0)
			return values[addr];
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	public void setWord(int addr, int value) {
		if(addr < addressMax && addr >= 0)
			values[addr] = value;
		else 
			throw new ArrayIndexOutOfBoundsException();
	}
	
}
