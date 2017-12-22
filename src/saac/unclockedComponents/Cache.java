package saac.unclockedComponents;

import saac.clockedComponents.Memory;
import saac.utils.NotImplementedException;

public class Cache {

	class CacheLine {
		boolean dirty;
		int address;
		int values[];
		CacheLine(int address, int values[], boolean dirty) {
			this.address = address;
			this.values = values;
			this.dirty = dirty;
		}
	}
	public static final int cacheLineLength = 0x8;
	public static final int cacheSize = 0x10;
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
			for(int j = 0; j<cacheLineLength; j++) {
				memory.setWord(cache[i].address*cacheLineLength + j, cache[i].values[j]);
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
}
