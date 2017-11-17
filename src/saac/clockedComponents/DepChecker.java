package saac.clockedComponents;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import saac.dataObjects.Instruction;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class DepChecker implements VisibleComponentI, ClockedComponentI, ClearableComponent{

	static final Function<Opcode, Opcode> sameOp = Function.identity();
	static final Function<Integer, Integer> sameVal = Function.identity();
		
	RegisterFile registerFile;
	FConnection<Instruction>.Output instructionIn;
	Instruction bufferIn;
	FConnection<Integer>.Output dirtyIn;
	FConnection<Instruction>.Input instructionOut;
	Instruction bufferOpOut;
	Connection<Integer>.Input paramAOut;
	Connection<Integer>.Input paramBOut;
	Connection<Integer>.Input paramCOut;
	Set<Integer> dirtyMem = new HashSet<>();

	public DepChecker(
			RegisterFile rf,
			FConnection<Instruction>.Output instructionIn,
			FConnection<Integer>.Output dirtyIn,
			FConnection<Instruction>.Input instructionOut,
			Connection<Integer>.Input paramAOut,
			Connection<Integer>.Input paramBOut,
			Connection<Integer>.Input paramCOut
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
		if(bufferOpOut != null)
			return;
		if(bufferIn == null) {
			if(!instructionIn.ready())
				return;
			bufferIn = instructionIn.pop();
		}
		
		Instruction inst  = bufferIn;
		
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
		default:
			throw new NotImplementedException();
		}
		
		List<Character> paramDependances = new ArrayList<>();
		if( (dependOnA || dirtyA) && registerFile.isDirty(inst.getParamA()) ) {
			paramDependances.add('A');
		}
		if( dependOnB && registerFile.isDirty(inst.getParamB()) ) {
			paramDependances.add('B');
		}
		if( dependOnC && registerFile.isDirty(inst.getParamC()) ) {
			paramDependances.add('C');
		}
		if(!paramDependances.isEmpty()) {
			Output.info.println(inst + " is blocked by " + paramDependances);
			return;
		}
		
		
		int addr;
		switch(inst.getOpcode()) {
		case Ldmi:
			addr = registerFile.get(inst.getParamB()) + registerFile.get(inst.getParamC());
			if(dirtyMem.contains(addr))
				return;
			break;
		case Stmi:
			addr = registerFile.get(inst.getParamB()) + registerFile.get(inst.getParamC());
			dirtyMem.add(addr);
			break;
		case Ldma:
			addr = registerFile.get(inst.getParamB());
			if(dirtyMem.contains(addr))
				return;
			break;
		case Stma:
			addr = registerFile.get(inst.getParamB());
			dirtyMem.add(addr);
			break;
		default:
			break;
		}
		
		if(dirtyA)
			registerFile.setDirty(inst.getParamA(), true);
		
		paramAOut.put(inst.getParamA());
		paramBOut.put(inst.getParamB());
		paramCOut.put(inst.getParamC());
		bufferOpOut = inst;
		bufferIn = null;
		
	}

	@Override
	public void tock() throws Exception {
		
		if(dirtyIn.ready())
			registerFile.setDirty(dirtyIn.pop(), false);
		
		if(bufferOpOut == null)
			return;
		if(!instructionOut.clear())
			return;
		instructionOut.put(bufferOpOut);
		bufferOpOut = null;

	}
	
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Dependency Checker");
			if(bufferIn != null)
				gc.drawString("(Hold) " + bufferIn.toString(), 10, 35);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear() {
		bufferIn = null;
		bufferOpOut = null;
	}	
}
