package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import saac.Settings;
import saac.dataObjects.DelayQueueItem;
import saac.dataObjects.MemReturn;
import saac.dataObjects.Instruction.Results.MemoryResult;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FListConnection;
import saac.interfaces.VisibleComponentI;
import saac.unclockedComponents.Cache;
import saac.utils.DrawingHelper;

public class Memory implements ClockedComponentI, VisibleComponentI, ClearableComponent{
	static final int addressMax = 0x10000;
	private int[] values = new int[addressMax];
	private Cache cache;
	
	class Tuple{
		int address;
		public int getAddress() {
			return address;
		}
		public int getVirtualNumber() {
			return virtualNumber;
		}
		int virtualNumber;
		boolean isVector;
		public boolean isVector() {
			return isVector;
		}
		Tuple(int address, int virtualNumber, boolean isVector) {
			this.address = address;
			this.virtualNumber = virtualNumber;
			this.isVector = isVector;
		}
		public String toString() {
			return "(" + Integer.toString(virtualNumber)+") [" + Integer.toString(address) + "], " + (isVector?"v":"s"); 
		}
	}
	
	List<DelayQueueItem<Tuple>> queue = new LinkedList<>(); 
	FListConnection<MemoryResult>.Output input;
	FListConnection<Integer>.Input output;
	Map<Integer, List<Integer>> memoryAddressDirtyLookup = new ConcurrentHashMap<Integer, List<Integer>>();
	
	public Memory(FListConnection<MemoryResult>.Output input, FListConnection<Integer>.Input output) {
		this.input = input;
		this.output = output;
		//initialise with values
		for(int i=0; i<0x100; i++)
			values[0x0+i] = i+1;
		
		for(int i=0; i<0x100; i++)
			values[0x100+i] = (i+2);
		
		/*
		for(int i=0; i<0x100; i++)
			values[0x200+i] = 0;
		
		for(int i=0; i<0x100; i++)
			values[0x200+i] = 1;
				
		for(int i=0; i<0x100; i++)
			values[0x200+i] = (int) ((Math.random()*100)%2);
		
		for(int i=0; i<0x100; i++)
			values[0x200+i] = (int) ((i/10)%2);
		*/ 
		
		cache = new Cache(this);
	}
	
	public MemReturn getWord(int addr) {
		if(addr < addressMax && addr >= 0) {
			if(Settings.CACHE_ENABLED) {
				if(cache.isInCache(addr/Cache.cacheLineLength)) {
					int[] cacheLine = cache.getCacheLine(addr/Cache.cacheLineLength);
					return MemReturn.Cache(Optional.of(cacheLine[addr%Cache.cacheLineLength]));
				} else {
					int[] cacheLine = loadCacheLineFromMemory(addr);
					boolean hadToEvict = cache.putCacheLine(addr/Cache.cacheLineLength,cacheLine, false);
					return MemReturn.Memory(Optional.of(cacheLine[addr%Cache.cacheLineLength]), hadToEvict);
				}
			} else {
				return MemReturn.Memory(Optional.of(values[addr]), false);
			}
		} else {
			throw new ArrayIndexOutOfBoundsException();
		}
	}
	private int[] loadCacheLineFromMemory(int addr) {
		int[] cacheLine = new int[Cache.cacheLineLength];
		for(int i = 0; i<Cache.cacheLineLength; i++) {
			cacheLine[i] = values[addr+i];
		}
		return cacheLine;
	}

	public MemReturn setWord(int addr, int value) {
		if(addr < addressMax && addr >= 0) {
			if(Settings.CACHE_ENABLED) {
				int cacheAddr = addr/Cache.cacheLineLength;
				if(cache.isInCache(cacheAddr)) {
					int values[] = cache.getCacheLine(cacheAddr);
					values[addr%Cache.cacheLineLength] = value;
					cache.updateCacheLine(cacheAddr, values);
					return MemReturn.Cache(Optional.empty());
				} else {
					int[] values = loadCacheLineFromMemory(addr);
					values[addr%Cache.cacheLineLength] = value;
					boolean hadToEvict = cache.putCacheLine(cacheAddr, values, true);
					return MemReturn.Memory(Optional.empty(), hadToEvict);
				}
			} else {
				values[addr] = value;
				return MemReturn.Memory(Optional.empty(), false);
			}
		} else { 
			throw new ArrayIndexOutOfBoundsException();
		}
	}

	public Optional<Integer> getLatestMemoryAddressWrite(int addr) {
		if(!memoryAddressDirtyLookup.containsKey(addr)) {
			return Optional.empty();
		} else {
			return Optional.of(memoryAddressDirtyLookup.get(addr).get(0));
		}
	}
	
	public void addLatestMemoryAddressWrite(int addr, int id) {
		if(!memoryAddressDirtyLookup.containsKey(addr)) {
			memoryAddressDirtyLookup.put(addr, new LinkedList<>());
		}
		memoryAddressDirtyLookup.get(addr).add(0,id);
	}
	
	public void removeMemoryAddressWrite(int addr, int id) {
		removeMemoryAddressWrite(addr, (i->i==id));		
	}
	
	public void removeMemoryAddressWrite(int addr, Predicate<? super Integer> f) {
		if(!memoryAddressDirtyLookup.containsKey(addr)) {
			throw new RuntimeException("Something went wrong");
		}
		List<Integer> l = memoryAddressDirtyLookup.get(addr);
		l.removeIf(f);
		if(memoryAddressDirtyLookup.get(addr).isEmpty()) {
			memoryAddressDirtyLookup.remove(addr);
		}
	}
	
	@Override
	public void tick() throws Exception {
		if(input.ready()) {
			MemoryResult[] stores = input.pop();
			for(MemoryResult store : stores) {
				
				if(store.getValue().isScalar()) {
					MemReturn rm = setWord(store.getAddr(), store.getValue().getScalarValue());
					queue.add(new DelayQueueItem<Tuple>(
							new Tuple(store.getAddr(), store.getVirtualNumber(), false),
							rm.getDelay()));
				} else {
					int[] val = store.getValue().getVectorValues();
					MemReturn[] rets = new MemReturn[] {
						setWord(store.getAddr(), val[0]),
						setWord(store.getAddr()+1, val[1]),
						setWord(store.getAddr()+2, val[2]),
						setWord(store.getAddr()+3, val[3])
					};
					queue.add(new DelayQueueItem<Tuple>(new Tuple(store.getAddr(), store.getVirtualNumber(), true),
							rets[0].getDelay()+rets[1].getDelay()+rets[2].getDelay()+rets[3].getDelay()
							));
				}			
			}
		}
	}

	@Override
	public void tock() throws Exception {
		for(DelayQueueItem<Tuple> item : queue) {
			item.decrementResultToZero();
		}
		if(output.clear()) {
			List<Integer> outs = new LinkedList<>();
			for(DelayQueueItem<Tuple> item : new ArrayList<>(queue)) {
				if(item.getDelay() == 0) {
					Tuple store = item.getResult();
					removeMemoryAddressWrite(store.getAddress(), store.getVirtualNumber());
					if(store.isVector()) {
						removeMemoryAddressWrite(store.getAddress()+1, store.getVirtualNumber());
						removeMemoryAddressWrite(store.getAddress()+2, store.getVirtualNumber());
						removeMemoryAddressWrite(store.getAddress()+3, store.getVirtualNumber());
					}
					
					outs.add(store.getVirtualNumber());
					queue.remove(item);
				}
			}
			if(!outs.isEmpty()) {
				output.put(outs.toArray(new Integer[0]));
			}
		}
	}

	@Override
	public void clear(int i) {
		for(Integer addr : memoryAddressDirtyLookup.keySet()) {
			removeMemoryAddressWrite(addr, y->y>=i);
		}
	}

	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Memory File");
			gc.setColor(Color.BLACK);
			gc.drawString(memoryAddressDirtyLookup.toString(), 5, 35);
			gc.drawString(queue.toString(), 5, 45);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	
}
