package saac.clockedComponents;

import java.awt.Point;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.Saac;
import saac.dataObjects.Instruction;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponent;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Issuer implements ClockedComponent, VisibleComponent{
	
	FConnection<Opcode>.Output opcodeIn;
	Connection<Integer>.Output paramARegInput;
	Connection<Integer>.Output paramAPassInput;
	Connection<Integer>.Output paramBRegInput;
	Connection<Integer>.Output paramBPassInput;
	Connection<Integer>.Output paramCRegInput;
	Connection<Integer>.Output paramCPassInput;
	FConnection<Instruction>.Input outputEU;
	FConnection<Instruction>.Input outputLS;
	FConnection<Instruction>.Input outputBr;
	Instruction bufferOut;
	RegisterFile registerFile;
	
	public Issuer(RegisterFile rf,
			FConnection<Opcode>.Output opcodeIn,
			Connection<Integer>.Output paramARegInput,
			Connection<Integer>.Output paramAPassInput,
			Connection<Integer>.Output paramBRegInput,
			Connection<Integer>.Output paramBPassInput,
			Connection<Integer>.Output paramCRegInput,
			Connection<Integer>.Output paramCPassInput,
			FConnection<Instruction>.Input outputEU,
			FConnection<Instruction>.Input outputLS,
			FConnection<Instruction>.Input outputBr) {
		this.opcodeIn = opcodeIn;
		this.paramARegInput = paramARegInput;
		this.paramAPassInput = paramAPassInput;
		this.paramBRegInput = paramBRegInput;
		this.paramBPassInput = paramBPassInput;
		this.paramCRegInput = paramCRegInput;
		this.paramCPassInput = paramCPassInput;
		this.outputEU = outputEU;
		this.outputLS = outputLS;
		this.outputBr = outputBr;
		this.registerFile = rf;
	}
	
	@Override
	public void tick() throws Exception {
		Output.debug1.println("Issue tock");
		Output.debug1.println("every");
		if(opcodeIn.ready() && bufferOut == null) {
			Output.debug1.println("ready");
			boolean paramAreg=false, paramBreg=false, paramCreg=false;
			Opcode opcode = opcodeIn.get();
			switch(opcode) {
			case Ldc:
			case Nop:
			case Br:
			case Ldma:
			case Jmp:
				break;
			case Add:
			case Sub:
			case Mul:
			case Div:
			case Ldmi:
				paramBreg = paramCreg = true;
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
				break;
			case Stma:
				paramAreg = true;
				break;
			default:
				throw new NotImplementedException();
			}
			
			bufferOut = new Instruction(opcode,
					paramAreg? paramARegInput.get():paramAPassInput.get(),
					paramBreg? paramBRegInput.get():paramBPassInput.get(), 		
					paramCreg? paramCRegInput.get():paramCPassInput.get());
			Output.debug1.println("bufferout: " + bufferOut);
			
		}
	}
	
	@Override
	public void tock() throws Exception {
		Output.debug1.println("Issue tock");
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
			}
			break;
		case Ldma:
		case Stmi:
		case Stma:
		case Ldmi:
			if(outputLS.clear()) {
				outputLS.put(bufferOut);
				Output.debug.println(bufferOut + " sent for execution on LSU");
			}
			break;
		case Br:
		case Jmp:
		case JmpN:
		case JmpZ:
			if(outputBr.clear()) {
				outputBr.put(bufferOut);
				Output.debug.println(bufferOut + " sent for execution on BrU");
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
			if(bufferOut != null)
				gc.fillText(bufferOut.toString(), 10, 35);
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}

}
