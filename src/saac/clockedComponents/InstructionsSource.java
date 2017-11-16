package saac.clockedComponents;

import java.awt.Graphics2D;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import saac.ProgramLoader;
import saac.dataObjects.Instruction;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.parsers.ParserException;


public class InstructionsSource implements ClockedComponentI, VisibleComponentI{
	int[][] instructions;
	
	private int[] getInstruction(int addr) {
		if(addr < instructions.length && addr >= 0)
			return instructions[addr];
		else 
			return new int[] {0x0, 0x0, 0x0, 0x0};
	}
	
	FConnection<Integer>.Output addrInput;
	FConnection<Boolean>.Output clearInput;
	FConnection<int[]>.Input instructionOutput;
	
	private class Item{
		int[] value;
		int delay;
		Item(int[] r, int d) {
			this.value = r;
			this.delay = d;
		}
	}
	
	List<Item> bufferOut = new LinkedList<>();
	static final int BufferSize = 4;
	
	public InstructionsSource(
			FConnection<Integer>.Output addrInput,
			FConnection<Boolean>.Output clearInput,
			FConnection<int[]>.Input instructionOutput
			) throws IOException, ParserException {
		this.addrInput = addrInput;
		this.clearInput = clearInput;
		this.instructionOutput = instructionOutput;
		instructions = ProgramLoader.loadProgram();
	}

	@Override
	public void tick() throws Exception {
		if(!instructionOutput.clear())
			return;
		if(bufferOut.isEmpty())
			return;
		if(bufferOut.get(0).delay == 0)
			instructionOutput.put(bufferOut.remove(0).value);
		
		for(Item item : bufferOut)
			item.delay = item.delay > 0? item.delay-1 : 0;		
	}

	@Override
	public void tock() throws Exception {
		if(clearInput.ready() && clearInput.pop()) {
			bufferOut.clear();
			if(addrInput.ready())
				addrInput.pop();
		}
		
		if(!addrInput.ready())
			return;
		if(bufferOut.size() == BufferSize)
			return;
		int pc = addrInput.pop();
		int[] bytes = getInstruction(pc);
		bufferOut.add(new Item(new int[] { bytes[0], bytes[1], bytes[2], bytes[3], pc}, 4/*Instructions.InstructionDelay.get(Opcode.Ldma)*/));
	}
	
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Instruction Source");
			for(int i = 0; i<bufferOut.size(); i++) {
				int[] item = bufferOut.get(i).value;
				int delay = bufferOut.get(i).delay;
				gc.drawString(
						String.format("Opcode: %s, params: (a,%d), (b,%d), (c,%d)",
								Opcode.fromInt(item[0]).toString(), item[1], item[2], item[3]) 
						+ " (" + delay + ")", 5, 22+10*i);
			}
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
	
}
