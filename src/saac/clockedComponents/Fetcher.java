package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import saac.Settings;
import saac.Settings.BranchPrediction;
import saac.dataObjects.Instruction.Results.BranchResult;
import saac.interfaces.BufferedConnection;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.interfaces.VisibleComponentI;
import saac.unclockedComponents.BranchPredictor;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;

public class Fetcher implements ClockedComponentI, VisibleComponentI {

	BufferedConnection<int[]>.Input output;

	FConnection<BranchResult>.Output fromBrUnit;
	int programCounter = 0;
	int instructionCounter = 0;
	FConnection<Integer>.Input addrOutput;
	FConnection<Boolean>.Input clearOutput;
	FListConnection<int[]>.Output instructionInput;
	int[][] instructionInputBuff;
	List<ClearableComponent> clearables;
	boolean halt = false;
	int haltingInstruction = -1;
	BranchPredictor predictor;
	
	public Fetcher(List<ClearableComponent> clearables, BranchPredictor predictor,
			BufferedConnection<int[]>.Input output,
			FConnection<BranchResult>.Output fromBrUnit,
			FConnection<Integer>.Input addrOutput,
			FConnection<Boolean>.Input clearOutput,
			FListConnection<int[]>.Output instructionInput
			) {
		this.output = output;
		this.fromBrUnit = fromBrUnit;
		this.addrOutput = addrOutput;
		this.clearOutput = clearOutput;
		this.instructionInput = instructionInput;
		this.clearables = clearables;
		this.predictor = predictor;
	}
	
	@Override
	public void tick() throws Exception {
		
		if(fromBrUnit.ready()){
			BranchResult res = fromBrUnit.pop();
			if(res.isLinkBranch()) {
				predictor.updateLinkBranch(res);
			} else {
				predictor.update(res);
			}
			if(halt && res.getVirtualNumber() == haltingInstruction) {
				programCounter = res.getNewPc();
				instructionCounter = res.getVirtualNumber();
				halt = false;
			} else {
				if(!res.wasCorrect()) {
					for(ClearableComponent cc : clearables)
						cc.clear(res.getVirtualNumber());
					instructionCounter = res.getVirtualNumber();
					programCounter = res.getNewPc();
					clearOutput.put(true);
					halt = false;
				}
			}
		} else {
			if(halt || !addrOutput.clear())
				return;
			addrOutput.put(programCounter);
			programCounter += Settings.SUPERSCALER_WIDTH;
		}
	}
	
	@Override
	public void tock() throws Exception {
		
		if(!output.empty()) {
			return;
		}
		
		if(instructionInputBuff == null) {
			if(instructionInput.ready()) {
				instructionInputBuff = instructionInput.pop();
			} else {
				return;
			}
		}
		
		List<int[]> inInsts = new LinkedList<>();
		inInsts.addAll(inInsts);
		List<int[]> outInsts = new LinkedList<>();
		insts:
		for(int i = 0; i<instructionInputBuff.length; i++) {
			int[] inst = instructionInputBuff[i];
			inInsts.remove(inst);
			inst = new int[] {inst[0], inst[1], inst[2], inst[3], inst[4], inst[5], inst[6], 0}; 
			switch(Opcode.fromInt(inst[0])) {
			case Jmp:
				programCounter = inst[6] + 1 + inst[1];
				Output.jumping_info.println("Fetch is jumping");
				clearOutput.put(true);
				inInsts.clear();
				break insts;
			case Br:
				programCounter = inst[1];
				Output.jumping_info.println("Fetch is Branching");
				clearOutput.put(true);
				inInsts.clear();
				break insts;
			case JmpC:
				inst[4] = inst[6] + 1;
				inst[3] = inst[2];
				inst[2] = inst[1];
				inst[6] = instructionCounter++;
				outInsts.add(inst);
				
				if(Settings.BRANCH_PREDICTION_MODE != BranchPrediction.Blocking) {
					boolean prediction = predictor.predictBinary(inst);
					inst[5] = prediction?1:0;
					if(prediction) {
						programCounter = inst[3] + inst[1];
						clearOutput.put(true);
					}
				} else {
					clearOutput.put(true);
					inInsts.clear();
					halt = true;
					haltingInstruction = inst[6]; //instruction number
					break insts;
				}
				break;
			case Brr:
				clearOutput.put(true);
				inst[2] = inst[1];
				inst[4] = inst[6] + 1;
				inst[6] = instructionCounter++;
				outInsts.add(inst);
				Optional<Integer> prediction = predictor.predictLink(inst);
				if(Settings.LINK_BRANCH_PREDICTION && prediction.isPresent()) {
					inst[5] = prediction.get();
				} else {
					inst[5] = -1;
					halt = true;
					haltingInstruction = inst[6]; //instruction number
				}
				break insts;
			case Ldpc:
				inst[3] = inst[6] + 1;
			default:
				inst[6] = instructionCounter++;
				outInsts.add(inst);
				break;
			}
		}
		if(outInsts.size() > 0) {
			output.put(outInsts.toArray(new int[0][]));
		}
		if(inInsts.isEmpty()) {
			instructionInputBuff = null;
		} else {
			instructionInputBuff = inInsts.toArray(new int[0][]);
		}
	}

	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Fetcher");
			gc.setColor(Color.BLACK);
			gc.drawString("pc: " + Integer.toString(programCounter), 10, 35);
			gc.drawString("inst count: " + Integer.toString(instructionCounter), 60, 35);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
}
