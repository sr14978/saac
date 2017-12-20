package saac.clockedComponents;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import saac.Saac;
import saac.Settings;
import saac.Settings.BranchPrediciton;
import saac.Worker;
import saac.dataObjects.Instruction.Results.BranchResult;
import saac.dataObjects.Instruction.Results.InstructionResult;
import saac.dataObjects.Instruction.Results.MemoryResult;
import saac.dataObjects.Instruction.Results.RegisterResult;
import saac.dataObjects.Instruction.Results.StopResult;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.interfaces.MultiFConnection;
import saac.interfaces.VisibleComponentI;
import saac.unclockedComponents.ReorderBuffer;
import saac.utils.DrawingHelper;
import saac.utils.Output;

public class WritebackHandler implements ClockedComponentI, VisibleComponentI {
	List<FConnection<InstructionResult>.Output> inputs;
	//FConnection<InstructionResult>.Output inputLS;
	//FConnection<BranchResult>.Output inputBr;
	ReorderBuffer reorderBuffer;
	FListConnection<RegisterResult>.Input resultOutput;
	MultiFConnection<RegisterResult>.Input virtualRegisterValueBus;
	
	FListConnection<MemoryResult>.Input sendStoresToMem;
	
	//to enforce round robin collection of inputs
	int nextInput = 0;
	Memory memory;
	
	public WritebackHandler(Memory memory, ReorderBuffer reorderBuffer,
			List<FConnection<InstructionResult>.Output> inputEUs,
			FConnection<InstructionResult>.Output inputLS,
			FConnection<InstructionResult>.Output inputBr,
			FListConnection<RegisterResult>.Input resultOutput,
			MultiFConnection<RegisterResult>.Input virtualRegisterValueBus,
			FListConnection<MemoryResult>.Input sendStoresToMem) {
		this.inputs = new ArrayList<>(inputEUs);
		this.inputs.add(inputLS);
		this.inputs.add(inputBr);
		this.resultOutput = resultOutput;
		this.virtualRegisterValueBus = virtualRegisterValueBus;
		Instance = this;
		this.memory = memory;
		this.reorderBuffer = reorderBuffer;
		this.sendStoresToMem = sendStoresToMem;
	}

	@Override
	public void tick() throws Exception {
		int count = 0;
		for(int i = 0; i<inputs.size() && count < Settings.SUPERSCALER_WIDTH; i++) {
			int j = nextInput;
			nextInput = (nextInput + 1) % inputs.size();
			if(inputs.get(j).ready()) {
				count++;
				FConnection<? extends InstructionResult>.Output input = inputs.get(j);
				InstructionResult res = input.peak();
				
				if(reorderBuffer.insert(res)) {
					input.pop();
				}
			}
		}
	}

	boolean stop = false;
	@Override
	public void tock() throws Exception {
		if(stop) {
			if(Worker.worker != null)
				Worker.worker.interrupt();
			Worker.finished = true;
			return;
		}
		List<RegisterResult> regResults = new ArrayList<>();
		List<MemoryResult> stores= new ArrayList<>();
		loop:
		for(int i = 0; i<Settings.SUPERSCALER_WIDTH; i++) {
			InstructionResult res = reorderBuffer.getFirst();
			if(res == null)
				break loop;
			reorderBuffer.clearFirst();		
			if(res instanceof MemoryResult) {
				Saac.InstructionCounter++;
				MemoryResult mr = (MemoryResult) res;
				stores.add(mr);
				//memory.setWord(mr.getAddr(), mr.getValue());
				//delay = Instructions.InstructionDelay.get(Instructions.Opcode.Stmi);
				break loop;
			} else if(res instanceof RegisterResult) {
				Saac.InstructionCounter++;
				RegisterResult rr = (RegisterResult) res;
				Output.info.println(String.format("%d is written back to r%d", rr.getValue(), rr.getTarget().getRegNumber()));
				regResults.add(rr);
			} else if(res instanceof StopResult) {
				stop = true;
				break loop;
			} else if(res instanceof BranchResult) {
				Saac.InstructionCounter++;
				if(Settings.BRANCH_PREDICTION_MODE != BranchPrediciton.Blocking) {
					BranchResult br = (BranchResult) res;
					if(!br.wasCorrect()) {
						reorderBuffer.clearAfter();
						reorderBuffer.bufferInstructionStart--;
					}
				}
			}
		}
		if(!regResults.isEmpty()) {
			resultOutput.put(regResults.toArray(new RegisterResult[0]));
			for(RegisterResult res :regResults ) {
				virtualRegisterValueBus.put(res);
			}
		}
		if(!stores.isEmpty()) {
			sendStoresToMem.put(stores.toArray(new MemoryResult[0]));
		}
	}

	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Write Back Handler", 4*BOX_SIZE, 50);
			gc.setPaint(Color.BLACK);
			for(int i = 0; i<reorderBuffer.reorderBuffer.length; i++)
				if(reorderBuffer.reorderBuffer[i] != null)
					gc.drawString(Integer.toString(reorderBuffer.reorderBuffer[i].getVirtualNumber()), i*40 + 20, 30);
				else
					gc.drawString("|X|", i*40 + 20, 30);
			
			gc.drawString("start index: " + Integer.toString(reorderBuffer.bufferIndexStart), 900, 30);
			gc.drawString("end index: " + Integer.toString(reorderBuffer.bufferIndexEnd), 1000, 30);
			gc.drawString("start inst: " + Integer.toString(reorderBuffer.bufferInstructionStart), 1100, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
	
	public static WritebackHandler Instance;
	
}
