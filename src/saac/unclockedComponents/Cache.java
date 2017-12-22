package saac.unclockedComponents;

import java.awt.Graphics2D;
import java.util.Arrays;

import saac.clockedComponents.Memory;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.NotImplementedException;

public class Cache implements VisibleComponentI{

	class CacheLine {
		boolean dirty;
		int address;
		int values[];
		CacheLine(int address, int values[], boolean dirty) {
			this.address = address;
			this.values = values;
			this.dirty = dirty;
		}
		public String toString() {
			return (dirty?"x":"o") + " " + Integer.toString(address*cacheLineLength) + " " + Arrays.toString(values);
		}
	}
	public static final int cacheLineLength = 0x2;
	public static final int cacheSize = 0x1;
	private CacheLine[] cache = new CacheLine[cacheSize];
	private int cacheLinesUsed = 0; 
	Memory memory;
	
	public Cache(Memory memory) {
		this.memory = memory;
	}
	
	public boolean isInCache(int addr) {
		for(CacheLine cl : cache) {
			if(cl != null && cl.address == addr) {
				return true;
			}
		}
		return false;
	}
	
	public int[] getCacheLine(int addr) {
		for(CacheLine cl : cache) {
			if(cl.address == addr) {
				return cl.values;
			}
		}
		throw new RuntimeException("That address is not contained in cache");
	}
	
	public void updateCacheLine(int addr, int[] values) {
		if(isInCache(addr)) {
			for(CacheLine cl : cache) {
				if(cl.address == addr) {
					cl.values = values;
					cl.dirty = true;
					return;
				}
			}
		} else {
			throw new RuntimeException("Not in cache");
		}
	}
	
	public boolean putCacheLine(int addr, int[] values, boolean dirty) {
		int i = pickCachLine();
		final boolean evict; 
		if(cache[i] != null && cache[i].dirty) {
			System.out.println("dirty so evicting");
			for(int j = 0; j<cacheLineLength; j++) {
				System.out.println(cache[i].address*cacheLineLength + j);
				memory.putWordInMainMemory(cache[i].address*cacheLineLength + j, cache[i].values[j]);
			}
			evict = true;
		} else {
			evict = false;
		}
		cache[i] = new CacheLine(addr, values, dirty);
		return evict;
	}

	enum ReplaceMentMethod {Random, LeastRecentlyUsed, MostRecentlyUsed};
	ReplaceMentMethod currentMethod = ReplaceMentMethod.Random;
	private int pickCachLine() {
		if(cacheLinesUsed < cacheSize) {
			for(int i = 0; i<cacheSize; i++) {
				if(cache[i] == null) {
					cacheLinesUsed++;
					return i;
				}
			}
			throw new RuntimeException("cacheLinesUsed < cacheSize but no unused slots");
		} else {
			switch(currentMethod) {
			case Random:
				return (int) (Math.random()*cacheSize);
			default:
				throw new NotImplementedException();
			}
		}
	}

	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Cache");
			for(int i = 0; i<cacheSize; i++) {
				if(cache[i] != null)
					gc.drawString(cache[i].toString(), 50, 12 + 10*i);
			}
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
}
