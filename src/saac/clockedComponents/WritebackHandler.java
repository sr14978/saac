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
import saac.dataObjects.BranchResult;
import saac.dataObjects.InstructionResult;
import saac.dataObjects.MemoryResult;
import saac.dataObjects.RegisterResult;
import saac.dataObjects.StopResult;
import saac.interfaces.BufferedConnection;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.interfaces.VisibleComponentI;
import saac.unclockedComponents.Memory;
import saac.utils.DrawingHelper;
import saac.utils.Instructions;
import saac.utils.Output;

public class WritebackHandler implements ClockedComponentI, VisibleComponentI {
	List<FConnection<InstructionResult>.Output> inputs;
	FConnection<InstructionResult>.Output inputLS;
	FConnection<BranchResult>.Output inputBr;
	RegisterFile registerFile;
	DepChecker depChecker;
	FListConnection<RegisterResult>.Input resultOutput;
	BufferedConnection<Integer>.Input dirtyOutput;
	
	//to enforce round robin collection of inputs
	int nextInput = 0;
	Memory memory;
		
	public WritebackHandler(RegisterFile rf, DepChecker depChecker, Memory memory,
			List<FConnection<InstructionResult>.Output> inputEUs,
			FConnection<InstructionResult>.Output inputLS,
			FConnection<InstructionResult>.Output inputBr,
			FListConnection<RegisterResult>.Input resultOutput,
			BufferedConnection<Integer>.Input dirtyOutput) {
		this.inputs = new ArrayList<>(inputEUs);
		this.inputs.add(inputLS);
		this.inputs.add(inputBr);
		this.registerFile = rf;
		this.depChecker = depChecker;
		this.resultOutput = resultOutput;
		this.dirtyOutput = dirtyOutput;
		Instance = this;
		this.memory = memory;
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
				
				if(registerFile.insert(res)) {
					input.pop();
				}
			}
		}
	}

	int delay = 0;
	@Override
	public void tock() throws Exception {
		if(delay != 0) {
			delay--;
			return;
		}
		List<RegisterResult> regResults = new ArrayList<>();
		loop:
		for(int i = 0; i<Settings.SUPERSCALER_WIDTH; i++) {
			InstructionResult res = registerFile.getFirst();
			if(res == null)
				break loop;
			registerFile.clearFirst();		
			if(res instanceof MemoryResult) {
				Saac.InstructionCounter++;
				MemoryResult mr = (MemoryResult) res;
				memory.setWord(mr.getAddr(), mr.getValue());
				delay = Instructions.InstructionDelay.get(Instructions.Opcode.Stmi);
				depChecker.dirtyMem.remove(mr.getValue());
				break loop;
			} else if(res instanceof RegisterResult) {
				Saac.InstructionCounter++;
				RegisterResult rr = (RegisterResult) res;
				Output.info.println(String.format("%d is written back to r%d", rr.getValue(), rr.getTarget()));
				regResults.add(rr);
				dirtyOutput.put(rr.getTarget());
			} else if(res instanceof StopResult) {
				if(Worker.worker != null)
					Worker.worker.interrupt();
				Worker.finished = true;
				break loop;
			} else if(res instanceof BranchResult) {
				Saac.InstructionCounter++;
				if(Settings.BRANCH_PREDICTION_MODE != BranchPrediciton.Blocking) {
					BranchResult br = (BranchResult) res;
					if(!br.wasCorrect()) {
						registerFile.clearAfter();
						registerFile.bufferInstructionStart--;
						registerFile.clearDirties();//not sure when to put this
					}
				}
			}
		}
		resultOutput.put(regResults.toArray(new RegisterResult[0]));
	}

	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			RegisterFile rf = registerFile;
			DrawingHelper.drawBox(gc, "Write Back Handler", 4*BOX_SIZE, 50);
			gc.setPaint(Color.BLACK);
			for(int i = 0; i<rf.reorderBuffer.length; i++)
				if(rf.reorderBuffer[i] != null)
					gc.drawString(Integer.toString(rf.reorderBuffer[i].getID()), i*40 + 20, 30);
				else
					gc.drawString("|X|", i*40 + 20, 30);
			
			gc.drawString("start index: " + Integer.toString(rf.bufferIndexStart), 700, 30);
			gc.drawString("end index: " + Integer.toString(rf.bufferIndexEnd), 800, 30);
			gc.drawString("start inst: " + Integer.toString(rf.bufferInstructionStart), 900, 30);
			gc.drawString("delay: " + Integer.toString(delay), 1000, 30);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
	
	public static WritebackHandler Instance;
	
}
