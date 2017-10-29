package saac.clockedComponents;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;
import java.awt.Graphics2D;
import saac.dataObjects.Instruction;
import saac.dataObjects.InstructionResult;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponent;
import saac.utils.DrawingHelper;
import saac.utils.Instructions;
import saac.utils.Instructions.Opcode;


public class InstructionsSource implements ClockedComponent, VisibleComponent{
	static final int[][] instructions = new int[][]{
			/*0*/new int[] {Opcode.toInt(Opcode.Ldc), 0, 0, 0},
			/*1*/new int[] {Opcode.toInt(Opcode.Ldc), 1, 0, 0},
			/*2*/new int[] {Opcode.toInt(Opcode.Ldc), 2, 0x10, 0},
			/*3*/new int[] {Opcode.toInt(Opcode.Ldc), 3, 0x20, 0},
			/*4*/new int[] {Opcode.toInt(Opcode.Ldc), 7, 0x30, 0},
			/*5*/new int[] {Opcode.toInt(Opcode.Ldmi), 4, 2, 0},
			/*6*/new int[] {Opcode.toInt(Opcode.Ldmi), 5, 3, 0},
			/*7*/new int[] {Opcode.toInt(Opcode.Mul), 4, 4, 5},
			/*8*/new int[] {Opcode.toInt(Opcode.Stmi), 4, 7, 0},
			/*9*/new int[] {Opcode.toInt(Opcode.Add), 1, 1, 4},
			/*A*/new int[] {Opcode.toInt(Opcode.Addi), 0, 0, 1},
			/*B*/new int[] {Opcode.toInt(Opcode.Subi), 6, 0, 10},
			/*C*/new int[] {Opcode.toInt(Opcode.JmpZ), 1, 6, 0},
			/*D*/new int[] {Opcode.toInt(Opcode.Jmp), -9, 0, 0},
			/*E*/new int[] {Opcode.toInt(Opcode.Addi), 1, 1, 0},
			/*F*/new int[] {Opcode.toInt(Opcode.Jmp), -2, 0, 0},
		};
	
	public static int[] getInstruction(int addr) {
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
			) {
		this.addrInput = addrInput;
		this.clearInput = clearInput;
		this.instructionOutput = instructionOutput;
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
		if(clearInput.ready() && clearInput.get()) {
			bufferOut.clear();
			if(addrInput.ready())
				addrInput.get();
		}
		
		if(!addrInput.ready())
			return;
		if(bufferOut.size() == BufferSize)
			return;
		int pc = addrInput.get();
		int[] bytes = getInstruction(pc);
		bufferOut.add(new Item(new int[] { bytes[0], bytes[1], bytes[2], bytes[3], pc}, Instructions.InstructionDelay.get(Opcode.Ldma)));
	}
	
	class View implements ComponentView {
		
		Point position; 
		View(int x, int y){
			position = new Point(x, y);
		}
		
		public void paint(Graphics2D gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawBox(gc, "Instruction Source");
			for(int i = 0; i<bufferOut.size(); i++) {
				int[] item = bufferOut.get(i).value;
				int delay = bufferOut.get(i).delay;
				gc.drawString(new Instruction(Opcode.fromInt(item[0]), item[1], item[2], item[3]).toString() + " (" + delay + ")", 5, 22+10*i);
			}
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}
	
}
