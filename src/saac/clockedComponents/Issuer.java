package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import saac.Settings;
import saac.clockedComponents.RegisterFile.RegVal;
import saac.dataObjects.FilledInInstruction;
import saac.dataObjects.VirtualInstruction;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Issuer implements ClockedComponentI, VisibleComponentI, ClearableComponent{
	
	static final Function<Opcode, Opcode> sameOp = Function.identity();
	static final Function<Integer, Integer> sameVal = Function.identity();
	
	FListConnection<VirtualInstruction>.Output instructionIn;
	FListConnection<RegVal>.Output paramARegInput;
	FListConnection<RegVal>.Output paramBRegInput;
	FListConnection<RegVal>.Output paramCRegInput;
	FListConnection<FilledInInstruction>.Input outputEU;
	FConnection<FilledInInstruction>.Input toEU_A;
	Connection<Boolean>.Output dualToIssuer;
	FConnection<FilledInInstruction>.Input outputLS;
	FConnection<FilledInInstruction>.Input outputBr;
	FilledInInstruction[] bufferOut;
	RegisterFile registerFile;
	
	public Issuer(RegisterFile rf,
			FListConnection<VirtualInstruction>.Output instructionIn,
			FListConnection<RegVal>.Output paramARegInput,
			FListConnection<RegVal>.Output paramBRegInput,
			FListConnection<RegVal>.Output paramCRegInput,
			FListConnection<FilledInInstruction>.Input outputEU,
			FConnection<FilledInInstruction>.Input toEU_A,
			Connection<Boolean>.Output dualToIssuer,
			FConnection<FilledInInstruction>.Input outputLS,
			FConnection<FilledInInstruction>.Input outputBr) {
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
		
		if(instructionIn.ready() && bufferOut == null
				&& paramARegInput.ready() && paramBRegInput.ready() && paramCRegInput.ready()) {
			VirtualInstruction[] insts= instructionIn.pop();
			RegVal[] paramARegValues = paramARegInput.pop();
			RegVal[] paramBRegValues = paramBRegInput.pop();
			RegVal[] paramCRegValues = paramCRegInput.pop();
			List<FilledInInstruction> instructionsOut = new LinkedList<>();
			for(int i = 0; i<insts.length; i++) {
				VirtualInstruction inst = insts[i];
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
				final int areg = paramARegValues[i].value;
				final int breg = paramBRegValues[i].value;
				final int creg = paramCRegValues[i].value;
				instructionsOut.add(inst.fillIn(
								a -> paramAreg? areg:a,
								b -> paramBreg? breg:b,
								c -> paramCreg? creg:c,
								d -> paramCreg? creg:d
							)
						);
			}
			bufferOut = instructionsOut.toArray(new FilledInInstruction[0]);
		}
	}
	
	@Override
	public void tock() throws Exception {
		if(bufferOut == null)
			return;
		List<FilledInInstruction> out = new LinkedList<>(Arrays.asList(bufferOut));
		List<FilledInInstruction> outEUs = new LinkedList<>();
		for(int i = 0; i<out.size(); i++) {
			FilledInInstruction inst = out.get(i);
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
			bufferOut = out.toArray(new FilledInInstruction[0]);
		
		if(!outEUs.isEmpty())
			outputEU.put(outEUs.toArray(new FilledInInstruction[0]));
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
	public void clear(int i) {
		if(bufferOut == null)
			return;
		List<FilledInInstruction> insts = new LinkedList<>();
		for(FilledInInstruction inst : bufferOut)
			if(inst.getID() <= i)
				insts.add(inst);
		if(insts.isEmpty())
			bufferOut = null;
		else
			bufferOut = insts.toArray(new FilledInInstruction[0]);
	}

}
