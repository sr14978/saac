package saac;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import saac.Instructions.Opcode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Issuer implements ClockedComponent{
	
	static final Function<Opcode, Opcode> sameOp = Function.identity();
	static final Function<Integer, Integer> sameVal = Function.identity();
	
	Connection<Instruction>.Output instructionIn;
	Instruction bufferIn;
	Connection<Instruction>.Input outputEU;
	Connection<Instruction>.Input outputLS;
	Connection<Instruction>.Input outputBr;
	Instruction bufferOut;
	RegisterFile registerFile;
	
	public Issuer(RegisterFile rf,
			Connection<Instruction>.Output input,
			Connection<Instruction>.Input outputEU,
			Connection<Instruction>.Input outputLS,
			Connection<Instruction>.Input outputBr) {
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
			bufferIn = instructionIn.get();
			if(bufferIn == null)
				return;
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
			if(outputEU.isEmpty()) {
				outputEU.put(bufferOut);
				System.out.println(bufferOut + " sent to EU reservation station");
			}
			break;
		case Ldma:
		case Stmi:
		case Stma:
		case Ldmi:
			if(outputLS.isEmpty()) {
				outputLS.put(bufferOut);
				System.out.println(bufferOut + " sent for execution on LSU");
			}
			break;
		case Br:
		case Jmp:
		case JmpN:
		case JmpZ:
			if(outputBr.isEmpty()) {
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
	
	

}
