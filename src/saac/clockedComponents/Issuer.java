package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.function.Function;

import saac.Saac;
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

public class Issuer implements ClockedComponentI, VisibleComponentI, ClearableComponent{
	
	static final Function<Opcode, Opcode> sameOp = Function.identity();
	static final Function<Integer, Integer> sameVal = Function.identity();
	
	FConnection<Instruction>.Output opcodeIn;
	Connection<Integer>.Output paramARegInput;
	Connection<Integer>.Output paramBRegInput;
	Connection<Integer>.Output paramCRegInput;
	FConnection<Instruction>.Input outputEU;
	FConnection<Instruction>.Input outputLS;
	FConnection<Instruction>.Input outputBr;
	Instruction bufferOut;
	RegisterFile registerFile;
	
	public Issuer(RegisterFile rf,
			FConnection<Instruction>.Output opcodeIn,
			Connection<Integer>.Output paramARegInput,
			Connection<Integer>.Output paramBRegInput,
			Connection<Integer>.Output paramCRegInput,
			FConnection<Instruction>.Input outputEU,
			FConnection<Instruction>.Input outputLS,
			FConnection<Instruction>.Input outputBr) {
		this.opcodeIn = opcodeIn;
		this.paramARegInput = paramARegInput;
		this.paramBRegInput = paramBRegInput;
		this.paramCRegInput = paramCRegInput;
		this.outputEU = outputEU;
		this.outputLS = outputLS;
		this.outputBr = outputBr;
		this.registerFile = rf;
	}
	
	@Override
	public void tick() throws Exception {
		if(opcodeIn.ready() && bufferOut == null) {
			final boolean paramAreg, paramBreg, paramCreg;
			Instruction inst= opcodeIn.pop();
			switch(inst.getOpcode()) {
			case Ldc:
			case Nop:
			case Br:
			case Ldma:
			case Jmp:
				paramAreg = paramBreg = paramCreg = false;
				break;
			case Add:
			case Sub:
			case Mul:
			case Div:
			case Ldmi:
				paramBreg = paramCreg = true;
				paramAreg = false;
				break;
			case Stmi:
				paramAreg = paramBreg = paramCreg = true;
				break;
			case Addi:
			case Subi:
			case Muli:
			case Divi:
			case Ln:
			case JmpN:
			case JmpZ:
				paramBreg = true;
				paramAreg = paramCreg = false;
				break;
			case Stma:
				paramAreg = true;
				paramBreg = paramCreg = false;
				break;
			default:
				throw new NotImplementedException();
			}
			
			bufferOut = inst.transform(
					sameOp,
					a -> paramAreg? paramARegInput.get():a,
					b -> paramBreg? paramBRegInput.get():b,
					c -> paramCreg? paramCRegInput.get():c
							);
			
		}
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
				Output.debug.println(bufferOut + " sent to EU reservation station");
				bufferOut = null;
				Saac.InstructionCounter++;
			}
			break;
		case Ldma:
		case Stmi:
		case Stma:
		case Ldmi:
			if(outputLS.clear()) {
				outputLS.put(bufferOut);
				Output.debug.println(bufferOut + " sent for execution on LSU");
				bufferOut = null;
				Saac.InstructionCounter++;
			}
			break;
		case Br:
		case Jmp:
		case JmpN:
		case JmpZ:
			if(outputBr.clear()) {
				outputBr.put(bufferOut);
				Output.debug.println(bufferOut + " sent for execution on BrU");
				bufferOut = null;
				Saac.InstructionCounter++;
			}
			break;
		default:
			System.err.println(bufferOut.getOpcode());
			throw new NotImplementedException();
		}
	}
		
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Issuer");
			gc.setColor(Color.BLACK);
			if(bufferOut != null)
				gc.drawString(bufferOut.toString(), 10, 35);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear() {
		bufferOut = null;
	}

}
