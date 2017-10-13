package saac;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import saac.Instructions.Opcode;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class InstructionDecoder implements ClockedComponent{
	Connection<Instruction>.Input outputEU_A;
	Connection<Instruction>.Input outputEU_B;
	Connection<Instruction>.Input outputLS;
	Instruction bufferOut;
	Instruction bufferIn;
	RegisterFile registerFile;
	
	final Instruction[] instructions = new Instruction[]{
		new Instruction(Opcode.Ldc, 1, 42, 0),
		new Instruction(Opcode.Ldmi, 0, 1, 10),
		new Instruction(Opcode.Addi, 0, 0, 1),
		new Instruction(Opcode.Stma, 1, 52, 0)
	};
	int count = -1;
	
	public InstructionDecoder(Connection<Instruction>.Input outputA,
			Connection<Instruction>.Input outputB, Connection<Instruction>.Input outputC, RegisterFile rf) {
		this.outputEU_A = outputA;
		this.outputEU_B = outputB;
		this.outputLS = outputC;
		this.registerFile = rf;
	}
	
	@Override
	public void tick() throws Exception {
		if(bufferOut != null)
			return;
		
		if(bufferIn == null) {
			count = (count + 1) % instructions.length;
			bufferIn = instructions[count];
		}
		
		Instruction inst = bufferIn;
		boolean dependOnA = false, dependOnB = false, dependOnC = false, dirtyA = false;
		switch(inst.getOpcode()) {
		case Ldc:
			dirtyA = true;
			break;
		case Add:
		case Sub:
		case Mul:
		case Div:
			dirtyA = dependOnB = dependOnC = true;
			break;
		case Stmi:
			dependOnA = dependOnB = true;
			break;
		case Addi:
		case Subi:
		case Muli:
		case Divi:
		case Ldmi:
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
				Function.identity(),
				dependOnA? registerFile::get : Function.identity(),
				dependOnB? registerFile::get : Function.identity(),
				dependOnC? registerFile::get : Function.identity()
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
		default:
			throw new NotImplementedException();
		}
		
	}
	
	

}
