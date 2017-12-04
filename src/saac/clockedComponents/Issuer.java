package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import saac.Settings;
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
	
	FConnection<Instruction[]>.Output instructionIn;
	Connection<Integer[]>.Output paramARegInput;
	Connection<Integer[]>.Output paramBRegInput;
	Connection<Integer[]>.Output paramCRegInput;
	FConnection<Instruction[]>.Input outputEU;
	FConnection<Instruction>.Input toEU_A;
	Connection<Boolean>.Output dualToIssuer;
	FConnection<Instruction>.Input outputLS;
	FConnection<Instruction>.Input outputBr;
	Instruction[] bufferOut;
	RegisterFile registerFile;
	
	public Issuer(RegisterFile rf,
			FConnection<Instruction[]>.Output instructionIn,
			Connection<Integer[]>.Output paramARegInput,
			Connection<Integer[]>.Output paramBRegInput,
			Connection<Integer[]>.Output paramCRegInput,
			FConnection<Instruction[]>.Input outputEU,
			FConnection<Instruction>.Input toEU_A,
			Connection<Boolean>.Output dualToIssuer,
			FConnection<Instruction>.Input outputLS,
			FConnection<Instruction>.Input outputBr) {
		this.instructionIn = instructionIn;
		this.paramARegInput = paramARegInput;
		this.paramBRegInput = paramBRegInput;
		this.paramCRegInput = paramCRegInput;
		this.outputEU = outputEU;
		this.toEU_A = toEU_A;
		this.dualToIssuer = dualToIssuer;
		this.outputLS = outputLS;
		this.outputBr = outputBr;
		this.registerFile = rf;
	}
	
	@Override
	public void tick() throws Exception {
		if(instructionIn.ready() && bufferOut == null) {
			Instruction[] insts= instructionIn.pop();
			List<Instruction> instructionsOut = new LinkedList<>();
			for(int i = 0; i<insts.length; i++) {
				Instruction inst = insts[i];
				final boolean paramAreg, paramBreg, paramCreg;
				switch(inst.getOpcode()) {
				case Ldc:
				case Nop:
				case Br:
				case Ldma:
				case Jmp:
				case Stop:
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
				final int areg = paramARegInput.get()[i];
				final int breg = paramBRegInput.get()[i];
				final int creg = paramCRegInput.get()[i];
				instructionsOut.add(inst.transform(
						sameOp,
						a -> paramAreg? areg:a,
						b -> paramBreg? breg:b,
						c -> paramCreg? creg:c
								)
						);
			}
			bufferOut = instructionsOut.toArray(new Instruction[0]);
		}
	}
	
	@Override
	public void tock() throws Exception {
		if(bufferOut == null)
			return;
		List<Instruction> out = new LinkedList<>(Arrays.asList(bufferOut));
		List<Instruction> outEUs = new LinkedList<>();
		for(int i = 0; i<out.size(); i++) {
			Instruction inst = out.get(i);
			switch(inst.getOpcode()) {
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
			case Stop:
				if(outputEU.clear()) {
					
					//bypassing
					if(Settings.RESERVATION_STATION_BYPASS_ENABLED && toEU_A.clear() && dualToIssuer.get() == true)
						toEU_A.put(inst);
					else
						outEUs.add(inst);
					
					Output.debug.println(inst + " sent to EU reservation station");
					out.remove(i--);
				}
				break;
			case Ldma:
			case Stmi:
			case Stma:
			case Ldmi:
				if(outputLS.clear()) {
					outputLS.put(inst);
					Output.debug.println(bufferOut + " sent for execution on LSU");
					out.remove(i--);
				}
				break;
			case Br:
			case Jmp:
			case JmpN:
			case JmpZ:
				if(outputBr.clear()) {
					outputBr.put(inst);
					Output.debug.println(bufferOut + " sent for execution on BrU");
					out.remove(i--);
				}
				break;
			default:
				System.err.println(inst.getOpcode());
				throw new NotImplementedException();
			}
		}
		if(out.isEmpty())
			bufferOut = null;
		else
			bufferOut = out.toArray(new Instruction[0]);
		
		if(!outEUs.isEmpty())
			outputEU.put(outEUs.toArray(new Instruction[0]));
	}
		
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Issuer");
			gc.setColor(Color.BLACK);
			if(bufferOut != null)
				gc.drawString(Arrays.toString(bufferOut), 10, 35);
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
