package saac;

public class Memory {
	static final int addressMax = 0x10000;
	static private int[] values = new int[addressMax];
	
	static {
		for(int i=0; i<10; i++)
			values[0x10+i] = i+1;
		
		for(int i=0; i<10; i++)
			values[0x40+i] = (i+2);
	}
	
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
