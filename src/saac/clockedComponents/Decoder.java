package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import saac.Settings;
import saac.Settings.IssueWindow;
import saac.clockedComponents.RegisterFile.RatItem;
import saac.dataObjects.Instruction.Register;
import saac.dataObjects.Instruction.Complete.CompleteInstruction;
import saac.dataObjects.Instruction.Empty.EmptyInstruction;
import saac.dataObjects.Instruction.Empty.Item;
import saac.dataObjects.Instruction.Partial.DestItem;
import saac.dataObjects.Instruction.Partial.PartialInstruction;
import saac.dataObjects.Instruction.Partial.PartialLSInstruction;
import saac.dataObjects.Instruction.Partial.SourceItem;
import saac.dataObjects.Instruction.Results.InstructionResult;
import saac.dataObjects.Instruction.Results.RegisterResult;
import saac.interfaces.BufferedConnection;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.interfaces.MultiFConnection;
import saac.interfaces.VisibleComponentI;
import saac.unclockedComponents.ReorderBuffer;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class Decoder implements ClearableComponent, ClockedComponentI, VisibleComponentI {
	enum Usage {ScalarReg, VectorReg, Data, Null};
	enum Store {ScalarReg, VectorReg, Null};
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
	BufferedConnection<int[]>.Output input;
	FConnection<Boolean>.Output fromWriteback;
	PartialInstruction[] bufferOut;
	PartialInstruction[] bufferIn;
	RegisterFile registerFile;
	ReorderBuffer reorderBuffer;
	Memory memory;
	boolean halt;
	
	public Decoder(BufferedConnection<int[]>.Output input, RegisterFile registerFile, ReorderBuffer reorderBuffer, Memory memory,
			FListConnection<PartialInstruction>.Input outputAU,
			FListConnection<PartialInstruction>.Input outputLS,
			FListConnection<PartialInstruction>.Input outputBR,
			Connection<Boolean>.Output isAUReservationStationEmpty,
			Connection<Boolean>.Output isLSReservationStationEmpty,
			Connection<Boolean>.Output isBRReservationStationEmpty,
			FConnection<CompleteInstruction>.Input toSingleAU,
			FConnection<CompleteInstruction>.Input toSingleLS,
			FConnection<CompleteInstruction>.Input toSingleBR,
			MultiFConnection<RegisterResult>.Output virtualRegisterValueBus,
			FConnection<Boolean>.Output fromWriteback
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
		this.fromWriteback = fromWriteback;
	}

	@Override
	public void tick() throws Exception {
		
		List<RegisterResult> results = null;
		if(virtualRegisterValueBus.ready()) {
			results = virtualRegisterValueBus.pop();
		}
		
		if(fromWriteback.ready()) {
			halt = fromWriteback.pop();
		}
		
		if(bufferIn == null || Settings.ISSUE_WINDOW_METHOD == IssueWindow.Unaligned && bufferIn.length < Settings.SUPERSCALER_WIDTH) {
			if (input.ready() && !halt) {
				
				final int[][] incomingInsts;
				if(Settings.ISSUE_WINDOW_METHOD == IssueWindow.Aligned || bufferIn == null) {
					incomingInsts = input.popAll();
				} else if(Settings.ISSUE_WINDOW_METHOD == IssueWindow.Unaligned) {
					incomingInsts = input.pop(Math.min(Settings.SUPERSCALER_WIDTH - bufferIn.length, input.getCount()));
				} else {
					throw new Exception("Invalid window method");
				}
				
				List<PartialInstruction> instructions = new LinkedList<>();
				for (int i = 0; i < incomingInsts.length; i++) {
					int[] data = incomingInsts[i];
								
					final Usage usageA, usageB, usageC, usageD;
					final Store dest;
					switch (Opcode.fromInt(data[0])) {
					case Ldc:
						dest = Store.ScalarReg;
						usageA = Usage.Data; 
						usageB = usageC = usageD = Usage.Null;
						break;
					case Ldpc:
						dest = Store.ScalarReg;
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
						dest = Store.ScalarReg;
						usageA = usageB = Usage.ScalarReg; 
						usageC = usageD = Usage.Null;
						break;
					case vLdmi:
						dest = Store.VectorReg;
						usageA = usageB = Usage.ScalarReg; 
						usageC = usageD = Usage.Null;
						break;
					case vMul:
						dest = Store.VectorReg;
						usageA = usageB = Usage.VectorReg; 
						usageC = usageD = Usage.Null;
						break;
					case Not:
						dest = Store.ScalarReg;
						usageA = Usage.ScalarReg;
						usageB = usageC = usageD = Usage.Null;
						break;
					case Stmi:
						dest = Store.Null;
						usageA = usageB = usageC = Usage.ScalarReg; 
						usageD = Usage.Null;
						break;
					case vStmi:
						dest = Store.Null;
						usageA = Usage.VectorReg; 
						usageB = usageC = Usage.ScalarReg; 
						usageD = Usage.Null;
						break;
					case Addi:
					case Subi:
					case Muli:
					case Divi:
						dest = Store.ScalarReg;
						usageA = Usage.ScalarReg;
						usageB = Usage.Data;
						usageC = usageD = Usage.Null;
						break;
					case Nop:
						dest = Store.Null;
						usageA = usageB = usageC = usageD = Usage.Null;
						break;
					case Ldma:
						dest = Store.ScalarReg;
						usageA = Usage.Data;
						usageB = usageC = usageD = Usage.Null;
						break;
					case Stma:
						dest = Store.Null;
						usageA = Usage.ScalarReg;
						usageB = Usage.Data;
						usageC = usageD = Usage.Null;
						break;
					case Br:
					case Jmp:
						dest = Store.Null;
						usageA = Usage.Data;
						usageB = usageC = usageD = Usage.Null;
						break;
					case Ln:
						dest = Store.Null;
						usageA = Usage.ScalarReg;
						usageB = usageC = usageD = Usage.Null;
						break;
					case JmpC:
						dest = Store.Null;
						usageA = usageC = usageD = Usage.Data;
						usageB = Usage.ScalarReg;
						break;
					case Stop:
						dest = Store.Null;
						usageA = usageB = usageC = usageD = Usage.Null;
						break;
					case vLdc:
						dest = Store.ScalarReg;
						usageA = usageB = usageC = usageD = Usage.Data;
						break;
					default:
						throw new NotImplementedException();
					}
					
					EmptyInstruction inst = new EmptyInstruction(data[6],
							Opcode.fromInt(data[0]), 
							formatDest(data[1], dest),
							formatParam(data[2], usageA),
							formatParam(data[3], usageB),
							formatParam(data[4], usageC),
							formatParam(data[5], usageD)
					);
					
					PartialInstruction vinst = maybeRenameInstruction(inst);
					instructions.add(vinst);
				}
				if(bufferIn == null) {
					bufferIn = instructions.toArray(new PartialInstruction[0]);
				} else {
					List<PartialInstruction> allInstructions = new ArrayList<>();
					for(PartialInstruction inst : bufferIn) {
						allInstructions.add(inst);
					}
					allInstructions.addAll(instructions);
					bufferIn = allInstructions.toArray(new PartialInstruction[0]);
				}
				
			}
		}
		
		if(bufferIn == null) {
			return;
		}
		
		if(results != null) {
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
				if(inst.getOpcode() == Opcode.Stop
						&& (anyRegisterDirty() || !memory.memoryAddressDirtyLookup.isEmpty())) {
					notReadyInstructions.add(inst);
				} else if(isVirtualSlotAvailable(inst.getVirtualNumber())) {
					
					if(Settings.REGISTER_RENAMING_ENABLED) {
						tryGrabbingVirtualRegisterAddressValues(inst);						
						if(loadStoreInstructionsReady(inst)) {
							readyInstructions.add(addMemoryDependancies(inst));
						} else {
							notReadyInstructions.add(inst);
						}
					} else {
						List<String> dependancies = new ArrayList<>();
						if(inst.getParamA().isPresent() && inst.getParamA().get().isRegister()){
							Register reg = inst.getParamA().get().getRegisterNumber();
							int val = reg.getValue();
							if((reg.isVector() && isVectorRegisterDirty(val, dirtiesInWindow))
									|| (reg.isScalar() && isScalarRegisterDirty(val, dirtiesInWindow))) {
								dependancies.add("A (" + val + ")");
							} else {
								if(reg.isVector()) {
									inst.setParamA(SourceItem.VectorData(getVectorArchitecturalRegisterValue(val)));
								} else {
									inst.setParamA(SourceItem.ScalarData(getScalarArchitecturalRegisterValue(val)));
								}
							}
						}
						if(inst.getParamB().isPresent() && inst.getParamB().get().isRegister()){
							Register reg = inst.getParamB().get().getRegisterNumber();
							int val = reg.getValue();
							if((reg.isVector() && isVectorRegisterDirty(val, dirtiesInWindow))
									|| (reg.isScalar() && isScalarRegisterDirty(val, dirtiesInWindow))) {
								dependancies.add("B (" + val + ")");
							} else {
								if(reg.isVector()) {
									inst.setParamB(SourceItem.VectorData(getVectorArchitecturalRegisterValue(val)));
								} else {
									inst.setParamB(SourceItem.ScalarData(getScalarArchitecturalRegisterValue(val)));
								}
							}
						}
						if(inst.getParamC().isPresent() && inst.getParamC().get().isRegister()){
							Register reg = inst.getParamC().get().getRegisterNumber();
							int val = reg.getValue();
							if((reg.isVector() && isVectorRegisterDirty(val, dirtiesInWindow))
									|| (reg.isScalar() && isScalarRegisterDirty(val, dirtiesInWindow))) {
								dependancies.add("C (" + val + ")");
							} else {
								if(reg.isVector()) {
									inst.setParamC(SourceItem.VectorData(getVectorArchitecturalRegisterValue(val)));
								} else {
									inst.setParamC(SourceItem.ScalarData(getScalarArchitecturalRegisterValue(val)));
								}
							}
						}
						if(inst.getParamD().isPresent() && inst.getParamD().get().isRegister()){
							Register reg = inst.getParamD().get().getRegisterNumber();
							int val = reg.getValue();
							if((reg.isVector() && isVectorRegisterDirty(val, dirtiesInWindow))
									|| (reg.isScalar() && isScalarRegisterDirty(val, dirtiesInWindow))) {
								dependancies.add("D (" + val + ")");
							} else {
								if(reg.isVector()) {
									inst.setParamD(SourceItem.VectorData(getVectorArchitecturalRegisterValue(val)));
								} else {
									inst.setParamD(SourceItem.ScalarData(getScalarArchitecturalRegisterValue(val)));
								}
							}
						}
						
						if(inst.getDest().isPresent()) {
							if((inst.getDest().get().isVector()
									&& isVectorRegisterDirty(inst.getDest().get().getRegNumber(), dirtiesInWindow))
									|| (!inst.getDest().get().isVector()
											&& isScalarRegisterDirty(inst.getDest().get().getRegNumber(), dirtiesInWindow))) {
								dependancies.add("Dest (" + inst.getDest().get().getRegNumber() + ")");
							}
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
							if(inst.getDest().get().isVector()) {
								setVectorRegisterDirty(inst.getDest().get().getRegNumber());
							} else {
								setScalarRegisterDirty(inst.getDest().get().getRegNumber());
							}
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
	
	private void tryGrabbingVirtualRegisterAddressValues(PartialInstruction inst) {
		tryGrabbingVirtualRegisterValue(inst::getParamA, inst::setParamA);
		tryGrabbingVirtualRegisterValue(inst::getParamB, inst::setParamB);
		tryGrabbingVirtualRegisterValue(inst::getParamC, inst::setParamC);
		tryGrabbingVirtualRegisterValue(inst::getParamD, inst::setParamD);
	}

	private void tryGrabbingVirtualRegisterValue(Supplier<Optional<SourceItem>> getter, Consumer<SourceItem> setter) {
		if(Settings.REGISTER_RENAMING_ENABLED) {
			Optional<SourceItem> o = getter.get();
			if(o.isPresent()) {
				SourceItem item = o.get();
				if(item.isRegister()) {
					if(item.getRegisterNumber().isVector()) {
						Optional<int[]> value = getVectorVirtualSlotValue(item.getRegisterNumber().getVectorValues());
						if(value.isPresent()) {
							setter.accept(SourceItem.VectorData(value.get()));
						}
					} else {
						Optional<Integer> value = getScalarVirtualSlotValue(item.getRegisterNumber().getScalarValue());
						if(value.isPresent()) {
							setter.accept(SourceItem.ScalarData(value.get()));
						}
					}
				}
			}
		}
	}

	private boolean loadStoreInstructionsReady(PartialInstruction inst) {
		if(inst.getOpcode().equals(Opcode.Ldmi) || inst.getOpcode().equals(Opcode.vLdmi)) {
			return inst.getParamA().get().isDataValue() && inst.getParamB().get().isDataValue();
		} else if(inst.getOpcode().equals(Opcode.Stmi) || inst.getOpcode().equals(Opcode.vStmi)) {
			return inst.getParamB().get().isDataValue() && inst.getParamC().get().isDataValue();
		} else {
			return true;
		}
	}

	private PartialInstruction addMemoryDependancies(PartialInstruction inst) {
		if(inst.getOpcode().equals(Opcode.Ldma)
				|| inst.getOpcode().equals(Opcode.Ldmi)
				|| inst.getOpcode().equals(Opcode.vLdmi)) {
			final List<Integer> val = new LinkedList<>();
			if(inst.getOpcode().equals(Opcode.Ldma)) {
				Optional<Integer> o = memory.getLatestMemoryAddressWrite(inst.getParamA().get().getDataValue().getScalarValue());
				if(o.isPresent()) {
					val.add(o.get());
				}
			} else if(inst.getOpcode().equals(Opcode.Ldmi)){
				Optional<Integer> o = memory.getLatestMemoryAddressWrite(inst.getParamA().get().getDataValue().getScalarValue()
						+ inst.getParamB().get().getDataValue().getScalarValue());
				if(o.isPresent()) {
					val.add(o.get());
				}
			} else if(inst.getOpcode().equals(Opcode.vLdmi)){
				Optional<Integer> a = memory.getLatestMemoryAddressWrite(inst.getParamA().get().getDataValue().getScalarValue()
						+ inst.getParamB().get().getDataValue().getScalarValue());
				if(a.isPresent()) val.add(a.get());
				Optional<Integer> b = memory.getLatestMemoryAddressWrite(inst.getParamA().get().getDataValue().getScalarValue()
						+ inst.getParamB().get().getDataValue().getScalarValue() +1);
				if(b.isPresent()) val.add(b.get());
				Optional<Integer> c = memory.getLatestMemoryAddressWrite(inst.getParamA().get().getDataValue().getScalarValue()
						+ inst.getParamB().get().getDataValue().getScalarValue() +2);
				if(c.isPresent()) val.add(c.get());
				Optional<Integer> d = memory.getLatestMemoryAddressWrite(inst.getParamA().get().getDataValue().getScalarValue()
						+ inst.getParamB().get().getDataValue().getScalarValue() +3);
				if(d.isPresent()) val.add(d.get());
			} else {
				throw new RuntimeException("Must be either ldma or ldmi");
			}
			if(!val.isEmpty()) {
				return new PartialLSInstruction(inst, val);
			} else {
				return new PartialLSInstruction(inst);
			}
		} else if(inst.getOpcode().equals(Opcode.Stma)
					|| inst.getOpcode().equals(Opcode.Stmi)
					|| inst.getOpcode().equals(Opcode.vStmi)){
			if(inst.getOpcode().equals(Opcode.Stma)) {
				memory.addLatestMemoryAddressWrite(
						inst.getParamB().get().getDataValue().getScalarValue(), inst.getVirtualNumber());
			} else if(inst.getOpcode().equals(Opcode.Stmi)){
				memory.addLatestMemoryAddressWrite(
						inst.getParamB().get().getDataValue().getScalarValue()
						+ inst.getParamC().get().getDataValue().getScalarValue(),
						inst.getVirtualNumber());
			} else if(inst.getOpcode().equals(Opcode.vStmi)){
				memory.addLatestMemoryAddressWrite(
						inst.getParamB().get().getDataValue().getScalarValue()
						+ inst.getParamC().get().getDataValue().getScalarValue(),
						inst.getVirtualNumber());
				memory.addLatestMemoryAddressWrite(
						inst.getParamB().get().getDataValue().getScalarValue()
						+ inst.getParamC().get().getDataValue().getScalarValue() + 1,
						inst.getVirtualNumber());
				memory.addLatestMemoryAddressWrite(
						inst.getParamB().get().getDataValue().getScalarValue()
						+ inst.getParamC().get().getDataValue().getScalarValue() + 2,
						inst.getVirtualNumber());
				memory.addLatestMemoryAddressWrite(
						inst.getParamB().get().getDataValue().getScalarValue()
						+ inst.getParamC().get().getDataValue().getScalarValue() + 3,
						inst.getVirtualNumber());
			} else {
				throw new RuntimeException("Must be either ldma or ldmi");
			}
			return new PartialLSInstruction(inst);
		} else {
			return inst;
		}
	}
	
	private boolean isVirtualSlotAvailable(int virtualNumber) {
		return reorderBuffer.inReorderBuffer(virtualNumber);
	}
	
	private Optional<Integer> getScalarVirtualSlotValue(int virtualNumber) {
		InstructionResult res = reorderBuffer.getVirtualSlotValue(virtualNumber);
		if(res == null) {
			return Optional.empty();
		} else if(res instanceof RegisterResult) {
			return Optional.of(((RegisterResult) res).getValue().getScalarValue());
		} else {
			throw new RuntimeException("Getting value of a non register result");
		}
	}

	private Optional<int[]> getVectorVirtualSlotValue(int virtualNumber) {
		InstructionResult res = reorderBuffer.getVirtualSlotValue(virtualNumber);
		if(res == null) {
			return Optional.empty();
		} else if(res instanceof RegisterResult) {
			return Optional.of(((RegisterResult) res).getValue().getVectorValues());
		} else {
			throw new RuntimeException("Getting value of a non register result");
		}
	}
	
	private Optional<Register> formatDest(int data, Store usage) {
		switch(usage) {
		case Null:
			return Optional.empty();
		case ScalarReg:
			return Optional.of(Register.Scalar(data));
		case VectorReg:
			return Optional.of(Register.Vector(data));
		default:
			throw new NotImplementedException();
		}
	}
	
	private Optional<Item> formatParam(int data, Usage usage) {
		switch(usage) {
		case Null:
			return Optional.empty();
		case Data:
			return Optional.of(Item.Data(data));
		case ScalarReg:
			return Optional.of(Item.Register(Register.Scalar(data)));
		case VectorReg:
			return Optional.of(Item.Register(Register.Vector(data)));
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
					RatItem item = getLatestRegister(i.getRegisterNumber().isScalar()?
							i.getRegisterNumber().getScalarValue():
							i.getRegisterNumber().getVectorValues()
						);
					if(item.isArchitectural()) {
						if(i.getRegisterNumber().isVector()) {
							return Optional.of(SourceItem.VectorData(getVectorArchitecturalRegisterValue(item.getValue())));
						} else {
							return Optional.of(SourceItem.ScalarData(getScalarArchitecturalRegisterValue(item.getValue())));
						}
					} else {
						if(i.getRegisterNumber().isScalar()) {
							return Optional.of(SourceItem.Register(Register.Scalar(item.getValue())));
						} else {
							return Optional.of(SourceItem.Register(Register.Vector(item.getValue())));
						}
					}
				} else {
					return Optional.of(SourceItem.Register(i.getRegisterNumber()));
				}
			} else if(i.isDataValue()) {
				return Optional.of(SourceItem.ScalarData((i.getDataValue())));
			} else {
				throw new NotImplementedException();
			}
		} else {
			return Optional.empty();
		}
	}
	
	private Optional<DestItem> renameDest(Optional<Register> o, int id) {
		if(o.isPresent()) {
			if(Settings.REGISTER_RENAMING_ENABLED) {
				setLatestRegister(o.get().getValue(), id);
				return Optional.of(new DestItem(o.get().getValue(), id, o.get().isVector()));
			} else {
				return Optional.of(new DestItem(o.get().getValue(), -1, o.get().isVector()));
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
	
	private int getScalarArchitecturalRegisterValue(int registerNumber) {
		return registerFile.getScalarRegisterValue(registerNumber);
	}
	
	private int[] getVectorArchitecturalRegisterValue(int registerNumber) {
		return registerFile.getVectorRegisterValue(registerNumber);
	}

	private boolean isScalarRegisterDirty(int registerNumber, List<Integer> dirtiesInWindow) {
		return registerFile.isDirty(registerNumber) || dirtiesInWindow.contains(registerNumber);
	}
	
	private boolean isVectorRegisterDirty(int registerNumber, List<Integer> dirtiesInWindow) {
		return isScalarRegisterDirty(registerNumber, dirtiesInWindow)
				|| isScalarRegisterDirty(registerNumber+1, dirtiesInWindow)
				|| isScalarRegisterDirty(registerNumber+2, dirtiesInWindow)
				|| isScalarRegisterDirty(registerNumber+3, dirtiesInWindow);
	}
	
	private boolean anyRegisterDirty() {
		return registerFile.anyDirty();
	}
	
	private void setScalarRegisterDirty(int registerNumber) {
		registerFile.setDirty(registerNumber, true);
	}
	
	private void setVectorRegisterDirty(int registerNumber) {
		setScalarRegisterDirty(registerNumber);
		setScalarRegisterDirty(registerNumber+1);
		setScalarRegisterDirty(registerNumber+2);
		setScalarRegisterDirty(registerNumber+3);
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
			case vLdc:
			case vMul:
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
			case Stma:
			case vLdmi:
			case vStmi:
			case Ldmi:
			case Stmi:
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
		halt = true;
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
