package saac.clockedComponents;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import saac.Settings;
import saac.clockedComponents.RegisterFile.Reg;
import saac.clockedComponents.RegisterFile.RegItem;
import saac.dataObjects.VirtualInstruction;
import saac.interfaces.BufferedConnection;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FListConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DepChecker implements VisibleComponentI, ClockedComponentI, ClearableComponent{

	static final Function<Opcode, Opcode> sameOp = Function.identity();
	static final Function<Integer, Integer> sameVal = Function.identity();
		
	RegisterFile registerFile;
	FListConnection<VirtualInstruction>.Output instructionIn;
	VirtualInstruction[] bufferIn;
	BufferedConnection<Integer>.Output dirtyIn;
	FListConnection<VirtualInstruction>.Input instructionOut;
	VirtualInstruction[] bufferOut;
	FListConnection<RegItem>.Input paramAOut;
	FListConnection<RegItem>.Input paramBOut;
	FListConnection<RegItem>.Input paramCOut;
	Set<Integer> dirtyMem = new HashSet<>();

	public DepChecker(
			RegisterFile rf,
			FListConnection<VirtualInstruction>.Output instructionIn,
			BufferedConnection<Integer>.Output dirtyIn,
			FListConnection<VirtualInstruction>.Input instructionOut,
			FListConnection<RegItem>.Input paramAOut,
			FListConnection<RegItem>.Input paramBOut,
			FListConnection<RegItem>.Input paramCOut
			) {
		this.registerFile = rf;
		this.instructionIn = instructionIn;
		this.instructionOut = instructionOut;
		this.paramAOut = paramAOut;
		this.paramBOut = paramBOut;
		this.paramCOut = paramCOut;
		this.dirtyIn = dirtyIn;
	}
	
	@Override
	public void tick() throws Exception {
		if(bufferOut != null)
			return;
		
		if(bufferIn == null) {
			if(!instructionIn.ready())
				return;
			bufferIn = instructionIn.pop();
		}
		List<VirtualInstruction> instructionsOut = new LinkedList<>();
		List<RegItem> paramsOutA = new LinkedList<>();
		List<RegItem> paramsOutB = new LinkedList<>();
		List<RegItem> paramsOutC = new LinkedList<>();
		List<VirtualInstruction> ins = new LinkedList<>(Arrays.asList(bufferIn));
		List<Integer> allDirties = new LinkedList<>();
		List<Integer> allUses= new LinkedList<>();
		List<Integer> acceptedDirties = new LinkedList<>();
		
		for(int k = 0; k< bufferIn.length; k++) {
			VirtualInstruction inst  = bufferIn[k];
			boolean dependOnA = false, dependOnB = false, dependOnC = false, dirtyA = false;
			switch(inst.getOpcode()) {
			case Ldc:
				dirtyA = true;
				break;
			case Add:
			case Sub:
			case Mul:
			case Div:
			case Ldmi:
				dirtyA = dependOnB = dependOnC = true;
				break;
			case Stmi:
				dependOnA = dependOnB = dependOnC = true;
				break;
			case Addi:
			case Subi:
			case Muli:
			case Divi:
				dirtyA = dependOnB = true;
				break;
			case Nop:
				break;
			case Ldma:
				dirtyA = true;
				break;
			case Stma:
				dependOnA = true;
				break;
			case Br:
				break;
			case Ln:
			case JmpN:
			case JmpZ:
				dependOnB = true;
				break;
			case Jmp:
				break;
			case Stop:
				for(int i = 0; i<RegisterFile.registerNum; i++)
					if(registerFile.isDirty(i))
						return;
				if(!dirtyMem.isEmpty())
					return;
				break;
			default:
				throw new NotImplementedException();
			}
		
			List<Character> paramDependances = new ArrayList<>();
			RegisterFile rf = registerFile;
			
			if(Settings.REGISTER_RENAMING_ENABLED) {
				if(dependOnA && inst.getVirtualParamA().type == Reg.Virtual) {
					if(allDirties.contains(inst.getVirtualParamA().value)
							|| (rf.inReorderBuffer(inst.getVirtualParamA().value)
									&& rf.getOffsetted(inst.getVirtualParamA().value) == null)
							|| rf.notYetinReorderBuffer(inst.getVirtualParamA().value))
						paramDependances.add('A');
				}
				if(dependOnB && inst.getVirtualParamB().type == Reg.Virtual) {
					if(allDirties.contains(inst.getVirtualParamB().value)
							|| (rf.inReorderBuffer(inst.getVirtualParamB().value)
									&& rf.getOffsetted(inst.getVirtualParamB().value) == null)
							|| rf.notYetinReorderBuffer(inst.getVirtualParamB().value))
						paramDependances.add('B');
				}
				if(dependOnC && inst.getVirtualParamC().type == Reg.Virtual) {
					if(allDirties.contains(inst.getVirtualParamC().value)
							|| (rf.inReorderBuffer(inst.getVirtualParamC().value)
									&& rf.getOffsetted(inst.getVirtualParamC().value) == null)
							|| rf.notYetinReorderBuffer(inst.getVirtualParamC().value))
						paramDependances.add('C');
				}
			} else {
				if(dirtyA) {
					if(allUses.contains(inst.getArchParamA()) || allDirties.contains(inst.getArchParamA()) || rf.isDirty(inst.getArchParamA()))
						paramDependances.add('A');
				} else if(dependOnA) {
					if(allDirties.contains(inst.getArchParamA()) || rf.isDirty(inst.getArchParamA()))
						paramDependances.add('A');
				}
				if(dependOnB) {
					if(allDirties.contains(inst.getArchParamB()) || rf.isDirty(inst.getArchParamB()))
						paramDependances.add('B');
				}
				if(dependOnC) {
					if(allDirties.contains(inst.getArchParamC()) || rf.isDirty(inst.getArchParamC()))
						paramDependances.add('C');
				}
				if(dependOnA)
					allUses.add(inst.getArchParamA());
				if(dependOnB)
					allUses.add(inst.getArchParamB());
				if(dependOnC)
					allUses.add(inst.getArchParamC());
			}
			
			if(dirtyA)
				if(Settings.REGISTER_RENAMING_ENABLED)
					allDirties.add(inst.getID());
				else
					allDirties.add(inst.getArchParamA());
			
			if(!paramDependances.isEmpty()) {
				Output.info.println(inst + " is blocked by " + paramDependances);
				if(Settings.OUT_OF_ORDER_ENABLED)
					continue;
				else
					break;
			}
			
			if(dirtyA)
				acceptedDirties.add(inst.getArchParamA());
			
			
			if(inst.getVirtualParamA().type == Reg.Virtual
					&& rf.inReorderBuffer(inst.getVirtualParamA().value)
					&& Settings.REGISTER_RENAMING_ENABLED)
				paramsOutA.add(new RegItem(inst.getID(), inst.getVirtualParamA().value, Reg.Virtual));
			else
				paramsOutA.add(new RegItem(inst.getID(), inst.getArchParamA(), Reg.Architectural));
			if(inst.getVirtualParamB().type == Reg.Virtual
					&& rf.inReorderBuffer(inst.getVirtualParamB().value)
					&& Settings.REGISTER_RENAMING_ENABLED)
				paramsOutB.add(new RegItem(inst.getID(), inst.getVirtualParamB().value, Reg.Virtual));
			else
				paramsOutB.add(new RegItem(inst.getID(), inst.getArchParamB(), Reg.Architectural));
			if(inst.getVirtualParamC().type == Reg.Virtual
					&& rf.inReorderBuffer(inst.getVirtualParamC().value)
					&& Settings.REGISTER_RENAMING_ENABLED)
				paramsOutC.add(new RegItem(inst.getID(), inst.getVirtualParamC().value, Reg.Virtual));
			else
				paramsOutC.add(new RegItem(inst.getID(), inst.getArchParamC(), Reg.Architectural));
			
			instructionsOut.add(inst);
			ins.remove(inst);
		}
		if(instructionsOut.isEmpty())
			return;
		//if(!instructionOut.clear())
		//	return;
		bufferOut = instructionsOut.toArray(new VirtualInstruction[0]);
		paramAOut.put(paramsOutA.toArray(new RegItem[0]));
		paramBOut.put(paramsOutB.toArray(new RegItem[0]));
		paramCOut.put(paramsOutC.toArray(new RegItem[0]));
		
		for(Integer r : acceptedDirties)
			registerFile.setDirty(r, true);
		
		if(ins.size() == 0)
			bufferIn = null;
		else
			bufferIn = ins.toArray(new VirtualInstruction[0]);
	}

	@Override
	public void tock() throws Exception {
		
		while(dirtyIn.ready())
			registerFile.setDirty(dirtyIn.pop(), false);
		
		if(bufferOut == null)
			return;
		if(!instructionOut.clear())
			return;
		instructionOut.put(bufferOut);
		bufferOut = null;

	}
	
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Dependency Checker");
			if(bufferIn != null)
				gc.drawString("(Hold) " + Arrays.toString(bufferIn), 10, 30);
			if(bufferOut != null)
				gc.drawString("(Out) " + Arrays.toString(bufferOut), 10, 45);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear(int i) {
		List<VirtualInstruction> insts;
		if(bufferOut != null) {
			insts = new LinkedList<>();
			for(VirtualInstruction inst : bufferOut)
				if(inst.getID() <= i)
					insts.add(inst);
			if(insts.isEmpty())
				bufferOut = null;
			else
				bufferOut = insts.toArray(new VirtualInstruction[0]);
		}
		if(bufferIn != null) {
			insts = new LinkedList<>();
			for(VirtualInstruction inst : bufferIn)
				if(inst.getID() <= i)
					insts.add(inst);
			if(insts.isEmpty())
				bufferIn = null;
			else
				bufferIn = insts.toArray(new VirtualInstruction[0]);
		}
	}	
}
