package saac;

import java.util.function.Function;

public class InstructionDecoder implements ClockedComponent{
	Connection<Instruction>.Input output;
	Instruction bufferOut;
	Instruction bufferIn;
	RegisterFile registerFile;
	
	final Instruction[] instructions = new Instruction[]{
		new Instruction(Instruction.Opcode.Ldc, 0, 42, 0),
		new Instruction(Instruction.Opcode.Addi, 1, 1, 1),
		new Instruction(Instruction.Opcode.Add, 2, 0, 1)
	};
	int count = -1;
	
	public InstructionDecoder(Connection<Instruction>.Input output, RegisterFile rf) {
		this.output = output;
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
		
		switch(inst.getOpcode()) {
		case Ldc:
			registerFile.setDirty(inst.getTarget(), true);
			bufferOut = inst;
			break;
		case Add:
			if(registerFile.isDirty(inst.getSourceA()) || registerFile.isDirty(inst.getSourceB()))
				return;
			registerFile.setDirty(inst.getTarget(), true);
			bufferOut = inst.transform(Function.identity(), Function.identity(), registerFile::get, registerFile::get);
			break;
		case Addi:
			if(registerFile.isDirty(inst.getSourceA()))
				return;
			registerFile.setDirty(inst.getTarget(), true);
			bufferOut = inst.transform(Function.identity(), Function.identity(), registerFile::get, Function.identity());
			break;
		case Nop:
			break;
		default:
			break;
		}
		bufferIn = null;
	}
	@Override
	public void tock() throws Exception {
		if(output.isEmpty()) {
			output.put(bufferOut);
			bufferOut = null;
		}
	}
	
	

}
