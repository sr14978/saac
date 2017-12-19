package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import saac.Settings;
import saac.clockedComponents.RegisterFile.RatItem;
import saac.dataObjects.Instruction.Complete.CompleteInstruction;
import saac.dataObjects.Instruction.Empty.EmptyInstruction;
import saac.dataObjects.Instruction.Empty.Item;
import saac.dataObjects.Instruction.Partial.DestItem;
import saac.dataObjects.Instruction.Partial.PartialInstruction;
import saac.dataObjects.Instruction.Partial.PartialLSInstruction;
import saac.dataObjects.Instruction.Partial.SourceItem;
import saac.dataObjects.Instruction.Results.RegisterResult;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.interfaces.MultiFConnection;
import saac.interfaces.VisibleComponentI;
import saac.unclockedComponents.Memory;
import saac.unclockedComponents.ReorderBuffer;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Decoder implements ClearableComponent, ClockedComponentI, VisibleComponentI {
	enum Usage {Reg, Data, Null};
	Connection<Boolean>.Output isAUReservationStationEmpty;
	Connection<Boolean>.Output isLSReservationStationEmpty;
	Connection<Boolean>.Output isBRReservationStationEmpty;	
	FConnection<CompleteInstruction>.Input toSingleAU;
	FConnection<CompleteInstruction>.Input toSingleLS;
	FConnection<CompleteInstruction>.Input toSingleBR;
	FListConnection<PartialInstruction>.Input outputAU;
	FListConnection<PartialInstruction>.Input outputLS;
	FListConnection<PartialInstruction>.Input outputBR;
	MultiFConnection<RegisterResult>.Output virtualRegisterValueBus;
	FListConnection<int[]>.Output input;
	PartialInstruction[] bufferOut;
	PartialInstruction[] bufferIn;
	RegisterFile registerFile;
	ReorderBuffer reorderBuffer;
	Memory memory;
	
	public Decoder(FListConnection<int[]>.Output input, RegisterFile registerFile, ReorderBuffer reorderBuffer, Memory memory,
			FListConnection<PartialInstruction>.Input outputAU,
			FListConnection<PartialInstruction>.Input outputLS,
			FListConnection<PartialInstruction>.Input outputBR,
			Connection<Boolean>.Output isAUReservationStationEmpty,
			Connection<Boolean>.Output isLSReservationStationEmpty,
			Connection<Boolean>.Output isBRReservationStationEmpty,
			FConnection<CompleteInstruction>.Input toSingleAU,
			FConnection<CompleteInstruction>.Input toSingleLS,
			FConnection<CompleteInstruction>.Input toSingleBR,
			MultiFConnection<RegisterResult>.Output virtualRegisterValueBus
			) {
		this.outputAU = outputAU;
		this.outputLS = outputLS;
		this.outputBR = outputBR;
		this.input = input;
		this.registerFile = registerFile;
		this.reorderBuffer = reorderBuffer;
		this.memory = memory;
		this.isAUReservationStationEmpty = isAUReservationStationEmpty;
		this.isLSReservationStationEmpty = isLSReservationStationEmpty;
		this.isBRReservationStationEmpty = isBRReservationStationEmpty;
		this.toSingleAU = toSingleAU;
		this.toSingleLS = toSingleLS;
		this.toSingleBR = toSingleBR;
		this.virtualRegisterValueBus = virtualRegisterValueBus;
	}

	@Override
	public void tick() throws Exception {
		
		List<RegisterResult> results = null;
		if(virtualRegisterValueBus.ready()) {
			results = virtualRegisterValueBus.pop();
		}
		
		if(bufferIn == null) {
			if (input.ready()) {
				final int[][] incomingInsts = input.pop();
				
				List<PartialInstruction> instructions = new LinkedList<>();
				for (int i = 0; i < incomingInsts.length; i++) {
					int[] data = incomingInsts[i];
								
					final Usage usageA, usageB, usageC, usageD;
					final boolean dirtyDest;
					switch (Opcode.fromInt(data[0])) {
					case Ldc:
						dirtyDest = true;
						usageA = Usage.Data; 
						usageB = usageC = usageD = Usage.Null;
						break;
					case Ldpc:
						dirtyDest = true;
						usageA = Usage.Data; 
						usageB = Usage.Data;
						usageC = usageD = Usage.Null;
						break;
					case Add:
					case Sub:
					case Mul:
					case Div:
					case Ldmi:
					case And:
					case Or:
					case Lteq:
					case Eq:
						dirtyDest = true;
						usageA = usageB = Usage.Reg; 
						usageC = usageD = Usage.Null;
						break;
					case Not:
						dirtyDest = true;
						usageA = Usage.Reg;
						usageB = usageC = usageD = Usage.Null;
						break;
					case Stmi:
						dirtyDest = false;
						usageA = usageB = usageC = Usage.Reg; 
						usageD = Usage.Null;
						break;
					case Addi:
					case Subi:
					case Muli:
					case Divi:
						dirtyDest = true;
						usageA = Usage.Reg;
						usageB = Usage.Data;
						usageC = usageD = Usage.Null;
						break;
					case Nop:
						dirtyDest = false;
						usageA = usageB = usageC = usageD = Usage.Null;
						break;
					case Ldma:
						dirtyDest = true;
						usageA = Usage.Data;
						usageB = usageC = usageD = Usage.Null;
						break;
					case Stma:
						dirtyDest = false;
						usageA = Usage.Reg;
						usageB = Usage.Data;
						usageC = usageD = Usage.Null;
						break;
					case Br:
					case Jmp:
						dirtyDest = false;
						usageA = Usage.Data;
						usageB = usageC = usageD = Usage.Null;
						break;
					case Ln:
						dirtyDest = false;
						usageA = Usage.Reg;
						usageB = usageC = usageD = Usage.Null;
						break;
					case JmpC:
						dirtyDest = false;
						usageA = usageC = usageD = Usage.Data;
						usageB = Usage.Reg;
						break;
					case Stop:
						dirtyDest = false;
						usageA = usageB = usageC = usageD = Usage.Null;
						break;
					default:
						throw new NotImplementedException();
					}
					
					EmptyInstruction inst = new EmptyInstruction(data[6],
							Opcode.fromInt(data[0]), 
							dirtyDest ? Optional.of(data[1]) : Optional.empty(),
							formatParam(data[2], usageA),
							formatParam(data[3], usageB),
							formatParam(data[4], usageC),
							formatParam(data[5], usageD)
					);
					
					PartialInstruction vinst = maybeRenameInstruction(inst);
					instructions.add(vinst);
				}
				
				bufferIn = instructions.toArray(new PartialInstruction[0]);
				
			} else {
				return;
			}
		} else if(results != null) {
			for(RegisterResult result : results) {
				for(PartialInstruction inst : bufferIn) {
					ReservationStation.fillInSingleParamWithResult(inst::getParamA, inst::setParamA, result);
					ReservationStation.fillInSingleParamWithResult(inst::getParamB, inst::setParamB, result);
					ReservationStation.fillInSingleParamWithResult(inst::getParamC, inst::setParamC, result);
					ReservationStation.fillInSingleParamWithResult(inst::getParamD, inst::setParamD, result);
				}
			}
		}
		
		if(bufferOut == null) {
			List<Integer> dirtiesInWindow = new ArrayList<>();
			List<PartialInstruction> readyInstructions = new LinkedList<>();
			List<PartialInstruction> notReadyInstructions = new LinkedList<>();
			for(int i = 0; i<bufferIn.length; i++) {
				PartialInstruction inst = bufferIn[i];
				if(isVirtualSlotAvailable(inst.getVirtualNumber())) {
					
					if(Settings.REGISTER_RENAMING_ENABLED) {
						if(loadStoreInstructionsReady(inst)) {
							readyInstructions.add(addMemoryDependancies(inst));
						} else {
							notReadyInstructions.add(inst);
						}
					} else {
						List<String> dependancies = new ArrayList<>();
						if(inst.getParamA().isPresent() && inst.getParamA().get().isRegister()){
							int val = inst.getParamA().get().getValue();
							if(isRegisterDirty(val) || dirtiesInWindow.contains(val)) {
								dependancies.add("A (" + val + ")");
							} else {
								inst.setParamA(SourceItem.Data(getArchitecturalRegisterValue(val)));
							}
						}
						if(inst.getParamB().isPresent() && inst.getParamB().get().isRegister()){
							int val = inst.getParamB().get().getValue();
							if(isRegisterDirty(val) || dirtiesInWindow.contains(val)) {
								dependancies.add("B (" + val + ")");
							} else {
								inst.setParamB(SourceItem.Data(getArchitecturalRegisterValue(val)));
							}
						}
						if(inst.getParamC().isPresent() && inst.getParamC().get().isRegister()){
							int val = inst.getParamC().get().getValue();
							if(isRegisterDirty(val) || dirtiesInWindow.contains(val)) {
								dependancies.add("C (" + val + ")");
							} else {
								inst.setParamC(SourceItem.Data(getArchitecturalRegisterValue(val)));
							}
						}
						if(inst.getParamD().isPresent() && inst.getParamD().get().isRegister()){
							int val = inst.getParamD().get().getValue();
							if(isRegisterDirty(val) || dirtiesInWindow.contains(val)) {
								dependancies.add("D (" + val + ")");
							} else {
								inst.setParamD(SourceItem.Data(getArchitecturalRegisterValue(val)));
							}
						}
						
						if(inst.getDest().isPresent()) {
							dirtiesInWindow.add(inst.getDest().get().getRegNumber());
						}
						
						if(!dependancies.isEmpty()) {
							Output.info.println(inst + " is blocked by " + dependancies);
							if(Settings.OUT_OF_ORDER_ENABLED) {
								notReadyInstructions.add(inst);
								continue;
							} else {
								for(int j = i; j<bufferIn.length; j++) {
									notReadyInstructions.add(bufferIn[j]);
								}
								break;
							}
						}
						if(inst.getDest().isPresent()) {
							setRegisterDirty(inst.getDest().get().getRegNumber());
						}
						
						readyInstructions.add(addMemoryDependancies(inst));
					}
					
				} else {
					notReadyInstructions.add(inst);
				}
			}
			
			if(notReadyInstructions.isEmpty())
				bufferIn = null;
			else
				bufferIn = notReadyInstructions.toArray(new PartialInstruction[0]);
			
			if(readyInstructions.isEmpty())
				bufferOut = null;
			else
				bufferOut = readyInstructions.toArray(new PartialInstruction[0]);
			
		}
	}
	
	private boolean loadStoreInstructionsReady(PartialInstruction inst) {
		if(inst.getOpcode().equals(Opcode.Ldmi)) {
			return inst.getParamA().get().isDataValue() && inst.getParamB().get().isDataValue();
		} else if(inst.getOpcode().equals(Opcode.Stmi)) {
			return inst.getParamB().get().isDataValue() && inst.getParamC().get().isDataValue();
		} else {
			return true;
		}
	}

	private PartialInstruction addMemoryDependancies(PartialInstruction inst) {
		if(inst.getOpcode().equals(Opcode.Ldma)|| inst.getOpcode().equals(Opcode.Ldmi)) {
			final Optional<Integer> val;
			if(inst.getOpcode().equals(Opcode.Ldma)) {
				val = memory.getLatestMemoryAddressWrite(inst.getParamA().get().getValue());				
			} else if(inst.getOpcode().equals(Opcode.Ldmi)){
				val = memory.getLatestMemoryAddressWrite(inst.getParamA().get().getValue() + inst.getParamB().get().getValue());
			} else {
				throw new RuntimeException("Must be either ldma or ldmi");
			}
			if(val.isPresent()) {
				return new PartialLSInstruction(inst, Optional.of(val.get()));
			} else {
				return new PartialLSInstruction(inst, Optional.empty());
			}
		} else if(inst.getOpcode().equals(Opcode.Stma)|| inst.getOpcode().equals(Opcode.Stmi)){
			if(inst.getOpcode().equals(Opcode.Stma)) {
				memory.addLatestMemoryAddressWrite(
						inst.getParamB().get().getValue(), inst.getVirtualNumber());
			} else if(inst.getOpcode().equals(Opcode.Stmi)){
				memory.addLatestMemoryAddressWrite(
						inst.getParamB().get().getValue() + inst.getParamC().get().getValue(),
						inst.getVirtualNumber());
			} else {
				throw new RuntimeException("Must be either ldma or ldmi");
			}
			return new PartialLSInstruction(inst, Optional.empty());
		} else {
			return inst;
		}
	}
	
	private boolean isVirtualSlotAvailable(int virtualNumber) {
		return reorderBuffer.inReorderBuffer(virtualNumber);
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
	
	private PartialInstruction maybeRenameInstruction(EmptyInstruction inst) throws Exception {
		
		final Optional<SourceItem> a = renameParam(inst.getParamA());
		final Optional<SourceItem> b = renameParam(inst.getParamB());
		final Optional<SourceItem> c = renameParam(inst.getParamC());
		final Optional<SourceItem> d = renameParam(inst.getParamD());
		final Optional<DestItem> dest = renameDest(inst.getDest(), inst.getVirtualNumber());
		
		return new PartialInstruction(inst.getVirtualNumber(), inst.getOpcode(), dest, a, b, c, d);
		
	}

	private Optional<SourceItem> renameParam(Optional<Item> o) throws Exception {
		if(o.isPresent()) {
			Item i = o.get();
			if(i.isRegisterNum()) {
				if(Settings.REGISTER_RENAMING_ENABLED) {
					RatItem item = getLatestRegister(i.getValue());
					if(item.isArchitectural()) {
						return Optional.of(SourceItem.Data(getArchitecturalRegisterValue(item.getValue())));
					} else {
						return Optional.of(SourceItem.Register(item.getValue()));
					}
				} else {
					return Optional.of(SourceItem.Register(i.getValue()));
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
			if(Settings.REGISTER_RENAMING_ENABLED) {
				setLatestRegister(o.get(), id);
				return Optional.of(new DestItem(o.get(), id));
			} else {
				return Optional.of(new DestItem(o.get(), -1));
			}
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

	private boolean isRegisterDirty(int registerNumber) {
		return registerFile.isDirty(registerNumber);
	}
	
	private void setRegisterDirty(int registerNumber) {
		registerFile.setDirty(registerNumber, true);
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
			case And:
			case Or:
			case Not:
			case Lteq:
			case Eq:
			case Ldpc:
				if(outputAU.clear()) {
					if(ReservationStation.isAllParametersPresent(inst)
							&& isAUReservationStationEmpty.get()
							&& toSingleAU.clear()
							&& Settings.RESERVATION_STATION_BYPASS_ENABLED) {
						toSingleAU.put(new CompleteInstruction(inst));
					} else {
						forAUs.add(inst);
						Output.debug.println(inst + " sent to EU reservation station");
					}
					remaining.remove(i--);
				}
				break;
			case Ldma:
			case Stmi:
			case Stma:
			case Ldmi:
				if(outputLS.clear()) {
					if(LSReservationStation.isAllParametersAndMemPresent(inst)
							&& isLSReservationStationEmpty.get()
							&& toSingleLS.clear()
							&& Settings.RESERVATION_STATION_BYPASS_ENABLED) {
						toSingleLS.put(new CompleteInstruction(inst));
					} else {
						forLSs.add(inst);
						Output.debug.println(bufferOut + " sent for execution on LSU");
					}
					remaining.remove(i--);
				}
				break;
			case Br:
			case Jmp:
			case JmpC:
			case Ln:
				if(outputBR.clear()) {
					if(ReservationStation.isAllParametersPresent(inst)
							&& isBRReservationStationEmpty.get()
							&& toSingleBR.clear()
							&& Settings.RESERVATION_STATION_BYPASS_ENABLED) {
						toSingleBR.put(new CompleteInstruction(inst));
					} else {
						forBRs.add(inst);
						Output.debug.println(bufferOut + " sent for execution on BrU");
					}
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
		if (bufferIn != null) {
			List<PartialInstruction> insts = new LinkedList<>();
			for (PartialInstruction inst : bufferIn) {
				if (inst.getVirtualNumber() <= i) {
					insts.add(inst);
				}
			}
			if (insts.isEmpty()) {
				bufferIn = null;
			} else {
				bufferIn = insts.toArray(new PartialInstruction[0]);
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
			if (bufferIn != null)
				gc.drawString(Arrays.toString(bufferIn), 10, 30);
			if (bufferOut != null)
				gc.drawString(Arrays.toString(bufferOut), 10, 45);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	
}
