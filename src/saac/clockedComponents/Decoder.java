package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import saac.Settings;
import saac.clockedComponents.RegisterFile.Reg;
import saac.clockedComponents.RegisterFile.RegItem;
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
import sun.reflect.generics.reflectiveObjects.NotImplementedException;;

public class Decoder implements ClockedComponentI, VisibleComponentI, ClearableComponent {

	FListConnection<VirtualInstruction>.Input output;
	FListConnection<int[]>.Output input;
	VirtualInstruction[] bufferOut;
	RegisterFile registerFile;

	public Decoder(FListConnection<VirtualInstruction>.Input output, FListConnection<int[]>.Output input, RegisterFile registerFile) {
		this.output = output;
		this.input = input;
		this.registerFile = registerFile;
	}

	@Override
	public void tick() throws Exception {
		if (bufferOut != null)
			return;

		if (!input.ready())
			return;
		int[][] datas = input.pop();
		List<VirtualInstruction> outInsts = new LinkedList<>();
		for (int i = 0; i < datas.length; i++) {
			int[] data = datas[i];
			Instruction inst = new Instruction(data[5], Opcode.fromInt(data[0]), data[1], data[2], data[3], data[4]);
			VirtualInstruction vinst = rename(inst);
			outInsts.add(vinst);
		}
		bufferOut = outInsts.toArray(new VirtualInstruction[0]);
	}

	private VirtualInstruction rename(Instruction inst) {
		final boolean dependOnA, dependOnB, dependOnC, dirtyA;
		switch (inst.getOpcode()) {
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
		if (Settings.REGISTER_RENAMING_ENABLED)
			vinst = inst
					.virtualize(//make virt addreses store if in arch or virt and clear to arch on clear
							x -> dependOnA ? registerFile.getRAT(x)
									: (dirtyA ? new RegItem(inst.getID(), Reg.Virtual)
											: new RegItem(x, Reg.Data)),
							x -> dependOnB ? registerFile.getRAT(x)
									: new RegItem(x, Reg.Data),
							x -> dependOnC ? registerFile.getRAT(x)
									: new RegItem(x, Reg.Data),
							x -> new RegItem(x, Reg.Data));
		else {
			Function<Integer, RegItem> f = x -> new RegItem(x, Reg.Architectural);
			vinst = inst.virtualize(f, f, f, f);
		}
			

		if (dirtyA) {
			registerFile.setRAT(inst.getParamA(), inst.getID(), Reg.Virtual				);
			/// if address not available - crash
		}

		return vinst;

	}

	@Override
	public void tock() throws Exception {
		if (bufferOut == null)
			return;
		if (output.clear()) {
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
			if (bufferOut != null)
				gc.drawString(Arrays.toString(bufferOut), 10, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear(int i) {
		if (bufferOut != null) {
			List<VirtualInstruction> insts = new LinkedList<>();
			for (VirtualInstruction inst : bufferOut)
				if (inst.getID() <= i)
					insts.add(inst);
			if (insts.isEmpty())
				bufferOut = null;
			else
				bufferOut = insts.toArray(new VirtualInstruction[0]);
		}
	}

}
