package saac.clockedComponents;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.Saac;
import saac.dataObjects.Instruction;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponent;
import saac.unclockedComponents.RegisterFile;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Issuer implements ClockedComponent, VisibleComponent{
	
	static final Function<Opcode, Opcode> sameOp = Function.identity();
	static final Function<Integer, Integer> sameVal = Function.identity();
	
	FConnection<Instruction>.Output instructionIn;
	Instruction bufferIn;
	FConnection<Instruction>.Input outputEU;
	FConnection<Instruction>.Input outputLS;
	FConnection<Instruction>.Input outputBr;
	Instruction bufferOut;
	RegisterFile registerFile;
	Set<Integer> dirtyMem = new HashSet<>();
	
	public Issuer(RegisterFile rf,
			FConnection<Instruction>.Output input,
			FConnection<Instruction>.Input outputEU,
			FConnection<Instruction>.Input outputLS,
			FConnection<Instruction>.Input outputBr) {
		this.instructionIn = input;
		this.outputEU = outputEU;
		this.outputLS = outputLS;
		this.outputBr = outputBr;
		this.registerFile = rf;
	}
	
	@Override
	public void tick() throws Exception {
		if(bufferOut != null)
			return;
		
		if(bufferIn == null) {
			if(!instructionIn.ready())
				return;
			bufferIn = instructionIn.get();
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
			System.out.println(inst + " is blocked by " + paramDependances);
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
		
		bufferOut = inst.transform(
				sameOp,
				dependOnA? registerFile::get : sameVal,
				dependOnB? registerFile::get : sameVal,
				dependOnC? registerFile::get : sameVal
						);
		bufferIn = null;
	}
	
	@Override
	public void tock() throws Exception {
		if(bufferOut == null)
			return;
		switch(bufferOut.getOpcode()) {
		case Ldc:
		case Add:
		case Sub:
		case Mul:
		case Div:
		case Addi:
		case Subi:
		case Muli:
		case Divi:
		case Nop:
			if(outputEU.clear()) {
				outputEU.put(bufferOut);
				System.out.println(bufferOut + " sent to EU reservation station");
			}
			break;
		case Ldma:
		case Stmi:
		case Stma:
		case Ldmi:
			if(outputLS.clear()) {
				outputLS.put(bufferOut);
				System.out.println(bufferOut + " sent for execution on LSU");
			}
			break;
		case Br:
		case Jmp:
		case JmpN:
		case JmpZ:
			if(outputBr.clear()) {
				outputBr.put(bufferOut);
				System.out.println(bufferOut + " sent for execution on BrU");
			}
			break;
		default:
			System.err.println(bufferOut.getOpcode());
			throw new NotImplementedException();
		}
		bufferOut = null;
		Saac.InstructionCounter++;
	}
		
	class View implements ComponentView {
		
		Point position; 
		View(int x, int y){
			position = new Point(x, y);
		}
		
		public void paint(GraphicsContext gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawBox(gc, "Issuer");
			gc.setFill(Color.BLACK);
			if(bufferIn != null)
				gc.fillText(bufferIn.toString(), 10, 25);
			if(bufferOut != null)
				gc.fillText(bufferOut.toString(), 10, 40);
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}

}