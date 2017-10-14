package saac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import saac.Instructions.Opcode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class InstructionDecoder implements ClockedComponent{
	
	static final Function identity = Function.identity();
	
	Connection<byte[]>.Output instructionIn;
	Connection<Instruction>.Input outputEU_A;
	Connection<Instruction>.Input outputEU_B;
	Connection<Instruction>.Input outputLS;
	Connection<Instruction>.Input outputBr;
	Instruction bufferOut;
	Instruction bufferIn;
	RegisterFile registerFile;

	int count = -1;
	
	public InstructionDecoder(Connection<byte[]>.Output input,
			Connection<Instruction>.Input outputA,
			Connection<Instruction>.Input outputB,
			Connection<Instruction>.Input outputC,
			Connection<Instruction>.Input outputBr,
			RegisterFile rf) {
		this.instructionIn = input;
		this.outputEU_A = outputA;
		this.outputEU_B = outputB;
		this.outputLS = outputC;
		this.outputBr = outputBr;
		this.registerFile = rf;
	}
	
	@Override
	public void tick() throws Exception {
		if(bufferOut != null)
			return;
		
		if(bufferIn == null) {
			byte[] bytes = instructionIn.get();
			if(bytes == null)
				return;
			bufferIn = new Instruction(Opcode.values()[bytes[0]], bytes[1], bytes[2], bytes[3]);
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
		case JmpN:
		case JmpZ:
			dependOnB = true;
		case Jmp:
			inst = inst.transform(identity, identity, identity, x->RegisterFile.PC);
			dependOnC = true;
			break;
		default:
			throw new NotImplementedException();
		}
		
		List<Character> paramDependances = new ArrayList();
		if( dependOnA && registerFile.isDirty(inst.getParamA()) ) {
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
				identity,
				dependOnA? registerFile::get : identity,
				dependOnB? registerFile::get : identity,
				dependOnC? registerFile::get : identity
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
			if(outputEU_A.isEmpty()) {
				outputEU_A.put(bufferOut);
				System.out.println(bufferOut + " sent for execution on EU 1");
				bufferOut = null;
				Saac.InstructionCounter++;
			} else if(outputEU_B.isEmpty()) {
				outputEU_B.put(bufferOut);
				System.out.println(bufferOut + " sent for execution on EU 2");
				bufferOut = null;
				Saac.InstructionCounter++;
			}
			break;
		case Ldma:
		case Stmi:
		case Stma:
		case Ldmi:
			if(outputLS.isEmpty()) {
				outputLS.put(bufferOut);
				System.out.println(bufferOut + " sent for execution on LSU");
				bufferOut = null;
				Saac.InstructionCounter++;
			}
			break;
		case Br:
		case Jmp:
		case JmpN:
		case JmpZ:
			if(outputBr.isEmpty()) {
				outputBr.put(bufferOut);
				System.out.println(bufferOut + " sent for execution on BrU");
				bufferOut = null;
				Saac.InstructionCounter++;
			}
			break;
		default:
			throw new NotImplementedException();
		}
		
	}
	
	

}
