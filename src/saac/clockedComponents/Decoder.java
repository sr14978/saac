package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import saac.clockedComponents.RegisterFile.RatItem;
import saac.dataObjects.Instruction.Empty.EmptyInstruction;
import saac.dataObjects.Instruction.Empty.Item;
import saac.dataObjects.Instruction.Partial.DestItem;
import saac.dataObjects.Instruction.Partial.PartialInstruction;
import saac.dataObjects.Instruction.Partial.SourceItem;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FListConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Decoder implements ClearableComponent, ClockedComponentI, VisibleComponentI {
	enum Usage {Reg, Data, Null};
		
	FListConnection<PartialInstruction>.Input outputAU;
	FListConnection<PartialInstruction>.Input outputLS;
	FListConnection<PartialInstruction>.Input outputBR;
	FListConnection<int[]>.Output input;
	PartialInstruction[] bufferOut;
	RegisterFile registerFile;

	public Decoder(FListConnection<int[]>.Output input, RegisterFile registerFile,
			FListConnection<PartialInstruction>.Input outputAU,
			FListConnection<PartialInstruction>.Input outputLS,
			FListConnection<PartialInstruction>.Input outputBR) {
		this.outputAU = outputAU;
		this.outputLS = outputLS;
		this.outputBR = outputBR;
		this.input = input;
		this.registerFile = registerFile;
	}

	@Override
	public void tick() throws Exception {
		if (bufferOut != null)
			return;

		if (!input.ready())
			return;
		int[][] datas = input.pop();
		List<PartialInstruction> outInsts = new LinkedList<>();
		for (int i = 0; i < datas.length; i++) {
			int[] data = datas[i];
						
			final Usage usageA, usageB, usageC;
			final boolean dirtyDest;
			switch (Opcode.fromInt(data[0])) {
			case Ldc:
				dirtyDest = true;
				usageA = Usage.Data; 
				usageB = usageC = Usage.Null;
				break;
			case Add:
			case Sub:
			case Mul:
			case Div:
			case Ldmi:
				dirtyDest = true;
				usageA = usageB = Usage.Reg; 
				usageC = Usage.Null;
				break;
			case Stmi:
				dirtyDest = false;
				usageA = usageB = usageC = Usage.Data; 
				break;
			case Addi:
			case Subi:
			case Muli:
			case Divi:
				dirtyDest = true;
				usageA = Usage.Reg;
				usageB = Usage.Data;
				usageC = Usage.Null;
				break;
			case Nop:
				dirtyDest = false;
				usageA = usageB = usageC = Usage.Null;
				break;
			case Ldma:
				dirtyDest = true;
				usageA = Usage.Data;
				usageB = usageC = Usage.Null;
				break;
			case Stma:
				dirtyDest = false;
				usageA = Usage.Reg;
				usageB = Usage.Data;
				usageC = Usage.Null;
				break;
			case Br:
			case Jmp:
				dirtyDest = false;
				usageA = Usage.Data;
				usageB = usageC = Usage.Null;
				break;
			case Ln:
				dirtyDest = false;
				usageA = Usage.Reg;
				usageB = usageC = Usage.Null;
				break;
			case JmpN:
			case JmpZ:
				dirtyDest = false;
				usageA = Usage.Data;
				usageB = Usage.Reg;
				usageC = Usage.Null;
				break;
			case Stop:
				dirtyDest = false;
				usageA = usageB = usageC = Usage.Null;
				break;
			default:
				throw new NotImplementedException();
			}
			
			EmptyInstruction inst = new EmptyInstruction(data[5],
					Opcode.fromInt(data[0]), 
					dirtyDest ? Optional.of(data[1]) : Optional.empty(),
					formatParam(data[2], usageA),
					formatParam(data[3], usageB),
					formatParam(data[4], usageC)
			);
			PartialInstruction vinst = renameInstruction(inst);
			outInsts.add(vinst);
		}
		bufferOut = outInsts.toArray(new PartialInstruction[0]);
	}
	
	private Optional<Item> formatParam(int data, Usage usage) {
		switch(usage) {
		case Null:
			return Optional.empty();
		case Data:
			return Optional.of(Item.Data(data));
		case Reg:
			return Optional.of(Item.Register(data));
		default:
			throw new NotImplementedException();
		}
	}
	
	private PartialInstruction renameInstruction(EmptyInstruction inst) {
		
		final Optional<SourceItem> a = renameParam(inst.getParamA());
		final Optional<SourceItem> b = renameParam(inst.getParamB());
		final Optional<SourceItem> c = renameParam(inst.getParamC());
		final Optional<DestItem> dest = renameDest(inst.getDest(), inst.getVirtualNumber());
				
		return new PartialInstruction(inst.getVirtualNumber(), inst.getOpcode(), dest, a, b, c);
	}

	private Optional<SourceItem> renameParam(Optional<Item> o) {
		if(o.isPresent()) {
			Item i = o.get();
			if(i.isRegisterNum()) {
				RatItem item = getLatestRegister(i.getValue());
				if(item.isArchitectural()) {
					return Optional.of(SourceItem.Data(getArchitecturalRegisterValue(item.getValue())));
				} else {
					return Optional.of(SourceItem.Register(item.getValue()));
				}
			} else if(i.isDataValue()) {
				return Optional.of(SourceItem.Data((i.getValue())));
			} else {
				throw new NotImplementedException();
			}
		} else {
			return Optional.empty();
		}
	}
	
	private Optional<DestItem> renameDest(Optional<Integer> o, int id) {
		if(o.isPresent()) {
			setLatestRegister(o.get(), id);
			return Optional.of(new DestItem(o.get(), id));
		} else {
			return Optional.empty();
		}
	}
	
	private void setLatestRegister(Integer registerNumber, int id) {
		registerFile.setLatestRegister(registerNumber, RatItem.Virtual(id));
	}

	private RatItem getLatestRegister(int registerNumber) {
		return registerFile.getLatestRegister(registerNumber);
	}
	
	private int getArchitecturalRegisterValue(int registerNumber) {
		return registerFile.getRegisterValue(registerNumber);
	}

	@Override
	public void tock() throws Exception {
		if(bufferOut == null) {
			return;
		}
		
		List<PartialInstruction> forAUs = new ArrayList<>();
		List<PartialInstruction> forLSs = new ArrayList<>();
		List<PartialInstruction> forBRs = new ArrayList<>();
		
		List<PartialInstruction> remaining = new LinkedList<>(Arrays.asList(bufferOut));
		for(int i = 0; i<remaining.size(); i++) {
			PartialInstruction inst = remaining.get(i);
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
				if(outputAU.clear()) {
					forAUs.add(inst);
					Output.debug.println(inst + " sent to EU reservation station");
					remaining.remove(i--);
				}
				break;
			case Ldma:
			case Stmi:
			case Stma:
			case Ldmi:
				if(outputLS.clear()) {
					forLSs.add(inst);
					Output.debug.println(bufferOut + " sent for execution on LSU");
					remaining.remove(i--);
				}
				break;
			case Br:
			case Jmp:
			case JmpN:
			case JmpZ:
				if(outputBR.clear()) {
					forBRs.add(inst);
					Output.debug.println(bufferOut + " sent for execution on BrU");
					remaining.remove(i--);
				}
				break;
			default:
				System.err.println(inst.getOpcode());
				throw new NotImplementedException();
			}
		}
		
		if(remaining.isEmpty()) {
			bufferOut = null;
		} else {
			bufferOut = remaining.toArray(new PartialInstruction[0]);
		}
		
		if(!forAUs.isEmpty()) {
			outputAU.put(forAUs.toArray(new PartialInstruction[0]));
		}
		
		if(!forLSs.isEmpty()) {
			outputLS.put(forLSs.toArray(new PartialInstruction[0]));
		}
		
		if(!forBRs.isEmpty()) {
			outputBR.put(forBRs.toArray(new PartialInstruction[0]));
		}
		
	}

	@Override
	public void clear(int i) {
		if (bufferOut != null) {
			List<PartialInstruction> insts = new LinkedList<>();
			for (PartialInstruction inst : bufferOut) {
				if (inst.getVirtualNumber() <= i) {
					insts.add(inst);
				}
			}
			if (insts.isEmpty()) {
				bufferOut = null;
			} else {
				bufferOut = insts.toArray(new PartialInstruction[0]);
			}
		}
	}

	class View extends ComponentView {

		View(int x, int y) {
			super(x, y);
		}

		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Decoder");
			gc.setColor(Color.BLACK);
			if (bufferOut != null)
				gc.drawString(Arrays.toString(bufferOut), 10, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	
}
