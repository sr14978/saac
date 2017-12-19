package saac.unclockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import saac.dataObjects.DelayQueueItem;
import saac.dataObjects.Instruction.Results.MemoryResult;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FListConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions;

public class Memory implements ClockedComponentI, VisibleComponentI, ClearableComponent{
	static final int addressMax = 0x10000;
	static private int[] values = new int[addressMax];
	
	//initialise with values
	static {
		for(int i=0; i<10; i++)
			values[0x10+i] = i+1;
		
		for(int i=0; i<10; i++)
			values[0x20+i] = (i+2);
	}
	
	List<DelayQueueItem<MemoryResult>> queue = new LinkedList<>(); 
	FListConnection<MemoryResult>.Output input;
	FListConnection<Integer>.Input output;
	Map<Integer, List<Integer>> memoryAddressDirtyLookup = new ConcurrentHashMap<Integer, List<Integer>>();
	
	public Memory(FListConnection<MemoryResult>.Output input, FListConnection<Integer>.Input output) {
		this.input = input;
		this.output = output;
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
				queue.add(new DelayQueueItem<MemoryResult>(store, Instructions.InstructionDelay.get(Instructions.Opcode.Stma)));
			}
		}
	}

	@Override
	public void tock() throws Exception {
		for(DelayQueueItem<MemoryResult> item : queue) {
			item.decrementResultToZero();
		}
		if(output.clear()) {
			List<Integer> outs = new LinkedList<>();
			for(DelayQueueItem<MemoryResult> item : new ArrayList<>(queue)) {
				if(item.getDelay() == 0) {
					MemoryResult store = item.getResult();
					setWord(store.getAddr(), store.getValue());
					removeMemoryAddressWrite(store.getAddr(), store.getVirtualNumber());
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
