package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import saac.Settings;
import saac.dataObjects.Instruction;
import saac.dataObjects.VirtualInstruction;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FListConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Decoder implements ClockedComponentI, VisibleComponentI, ClearableComponent{

	FListConnection<VirtualInstruction>.Input output;
	FListConnection<int[]>.Output input;
	VirtualInstruction[] bufferOut;
	Map<Integer, Integer> virtualAddresses = new HashMap<>();
	
	public Decoder(FListConnection<VirtualInstruction>.Input output, FListConnection<int[]>.Output input) {
		this.output = output;
		this.input = input;
	}

	@Override
	public void tick() throws Exception {
		if(bufferOut != null)
			return;
		
		if(!input.ready())
			return;
		int[][] datas = input.pop();
		List<VirtualInstruction> outInsts = new LinkedList<>();
		for(int i = 0; i<datas.length; i++) {
			int[] data = datas[i];
			Instruction inst = new Instruction(data[5], Opcode.fromInt(data[0]), data[1], data[2], data[3], data[4]);
			VirtualInstruction vinst = rename(inst);
			outInsts.add(vinst);
		}
		bufferOut = outInsts.toArray(new VirtualInstruction[0]);
	}

	private VirtualInstruction rename(Instruction inst) {
		final boolean dependOnA, dependOnB, dependOnC, dirtyA;
		switch(inst.getOpcode()) {
		case Ldc:
			dirtyA = true;
			dependOnA = dependOnB = dependOnC = false;
			break;
		case Add:
		case Sub:
		case Mul:
		case Div:
		case Ldmi:
			dirtyA = dependOnB = dependOnC = true;
			dependOnA = false;
			break;
		case Stmi:
			dependOnA = dependOnB = dependOnC = true;
			dirtyA = false;
			break;
		case Addi:
		case Subi:
		case Muli:
		case Divi:
			dirtyA = dependOnB = true;
			dependOnA = dependOnC = false;
			break;
		case Nop:
			dependOnA = dependOnB = dependOnC = dirtyA = false;
			break;
		case Ldma:
			dirtyA = true;
			dependOnA = dependOnB = dependOnC = false;
			break;
		case Stma:
			dependOnA = true;
			dependOnB = dependOnC = dirtyA = false;
			break;
		case Br:
			dependOnA = dependOnB = dependOnC = dirtyA = false;
			break;
		case Ln:
		case JmpN:
		case JmpZ:
			dependOnB = true;
			dependOnA = dependOnC = dirtyA = false;
			break;
		case Jmp:
		case Stop:
			dependOnA = dependOnB = dependOnC = dirtyA = false;
			break;
		default:
			throw new NotImplementedException();
		}
		
		VirtualInstruction vinst;
		if(Settings.REGISTER_RENAMING_ENABLED)
			vinst = inst.virtualize(
				x->dependOnA?virtualAddresses.get(x):(dirtyA?inst.getID():x),
				x->dependOnB?virtualAddresses.get(x):x,
				x->dependOnC?virtualAddresses.get(x):x,
				x->x
			);
		else
			vinst = inst.virtualize(x->x, x->x, x->x, x->x);
		
		if(dirtyA) {
			virtualAddresses.put(inst.getParamA(), inst.getID());
			///if address not available - crash 
		}
		
		return vinst;

	}

	@Override
	public void tock() throws Exception {
		if(bufferOut == null)
			return;
		if(output.clear()) {
			output.put(bufferOut);
			bufferOut = null;
		}
	}
		
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Decoder");
			gc.setColor(Color.BLACK);
			if(bufferOut != null)
				gc.drawString(Arrays.toString(bufferOut), 10, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear(int i) {
		if(bufferOut == null)
			return;
		List<VirtualInstruction> insts = new LinkedList<>();
		for(VirtualInstruction inst : bufferOut)
			if(inst.getID() <= i)
				insts.add(inst);
		if(insts.isEmpty())
			bufferOut = null;
		else
			bufferOut = insts.toArray(new VirtualInstruction[0]);
	}
	
}
