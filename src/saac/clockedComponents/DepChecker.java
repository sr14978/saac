package saac.clockedComponents;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import saac.dataObjects.Instruction;
import saac.interfaces.BufferedConnection;
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
	FConnection<Instruction[]>.Output instructionIn;
	Instruction[] bufferIn;
	BufferedConnection<Integer>.Output dirtyIn;
	FConnection<Instruction[]>.Input instructionOut;
	Instruction[] bufferOut;
	Connection<Integer[]>.Input paramAOut;
	Connection<Integer[]>.Input paramBOut;
	Connection<Integer[]>.Input paramCOut;
	Set<Integer> dirtyMem = new HashSet<>();

	public DepChecker(
			RegisterFile rf,
			FConnection<Instruction[]>.Output instructionIn,
			BufferedConnection<Integer>.Output dirtyIn,
			FConnection<Instruction[]>.Input instructionOut,
			Connection<Integer[]>.Input paramAOut,
			Connection<Integer[]>.Input paramBOut,
			Connection<Integer[]>.Input paramCOut
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
		if(!instructionOut.clear())
			return;
		if(bufferIn == null) {
			if(!instructionIn.ready())
				return;
			bufferIn = instructionIn.pop();
		}
		List<Instruction> instructionsOut = new LinkedList<>();
		List<Integer> paramsOutA = new LinkedList<>();
		List<Integer> paramsOutB = new LinkedList<>();
		List<Integer> paramsOutC = new LinkedList<>();
		List<Instruction> ins = new LinkedList<>(Arrays.asList(bufferIn));
		inst:
		for(int k = 0; k< bufferIn.length; k++) {
			Instruction inst  = bufferIn[k];
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
				break;
			}
			
			
			int addr;
			switch(inst.getOpcode()) {
			case Ldmi:
				addr = registerFile.get(inst.getParamB()) + registerFile.get(inst.getParamC());
				if(dirtyMem.contains(addr))
					break inst;
				break;
			case Stmi:
				addr = registerFile.get(inst.getParamB()) + registerFile.get(inst.getParamC());
				dirtyMem.add(addr);
				break;
			case Ldma:
				addr = registerFile.get(inst.getParamB());
				if(dirtyMem.contains(addr))
					break inst;
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
			
			paramsOutA.add(inst.getParamA());
			paramsOutB.add(inst.getParamB());
			paramsOutC.add(inst.getParamC());
			instructionsOut.add(inst);
			ins.remove(inst);
		}
		if(instructionsOut.isEmpty())
			return;

		bufferOut = instructionsOut.toArray(new Instruction[0]);
		paramAOut.put(paramsOutA.toArray(new Integer[0]));
		paramBOut.put(paramsOutB.toArray(new Integer[0]));
		paramCOut.put(paramsOutC.toArray(new Integer[0]));
		if(ins.size() == 0)
			bufferIn = null;
		else
			bufferIn = ins.toArray(new Instruction[0]);
	}

	@Override
	public void tock() throws Exception {
		
		if(dirtyIn.ready())
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
	public void clear() {
		bufferIn = null;
		bufferOut = null;
	}	
}
