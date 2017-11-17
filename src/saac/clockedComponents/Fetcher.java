package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.List;

import saac.Settings;
import saac.Settings.BranchPrediciton;
import saac.dataObjects.BranchResult;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponentI;
import saac.unclockedComponents.BranchPredictor;
import saac.utils.DrawingHelper;
import saac.utils.Instructions.Opcode;
import saac.utils.Output;

public class Fetcher implements ClockedComponentI, VisibleComponentI {

	RegisterFile registerFile;
	FConnection<int[]>.Input output;

	FConnection<BranchResult>.Output fromBrUnit;
	int programCounter = 0;
	int instructionCounter = 0;
	FConnection<Integer>.Input addrOutput;
	FConnection<Boolean>.Input clearOutput;
	FConnection<int[]>.Output instructionInput;
	List<ClearableComponent> clearables;
	boolean halt = false;
	BranchPredictor predictor;
	
	public Fetcher(RegisterFile registerFile, List<ClearableComponent> clearables, BranchPredictor predictor,
			FConnection<int[]>.Input output,
			FConnection<BranchResult>.Output fromBrUnit,
			FConnection<Integer>.Input addrOutput,
			FConnection<Boolean>.Input clearOutput,
			FConnection<int[]>.Output instructionInput
			) {
		this.output = output;
		this.fromBrUnit = fromBrUnit;
		this.registerFile = registerFile;
		this.addrOutput = addrOutput;
		this.clearOutput = clearOutput;
		this.instructionInput = instructionInput;
		this.clearables = clearables;
		this.predictor = predictor;
	}
	
	@Override
	public void tick() throws Exception {
		
		
		if(halt) {
			if(!fromBrUnit.ready())
				return;
			BranchResult res = fromBrUnit.pop();
			programCounter = res.getNewPc();
			halt = false;
		} else if(fromBrUnit.ready()) {
			
			BranchResult res = fromBrUnit.pop();
			predictor.update(res);
			if(!res.wasCorrect()) {
				for(ClearableComponent cc : clearables)
					cc.clear();
				instructionCounter = res.getID();
				programCounter = res.getNewPc();
			}
		} else if(addrOutput.clear()) {
			addrOutput.put(programCounter);
			programCounter++;
		}
	}
	
	@Override
	public void tock() throws Exception {
		
		if(!instructionInput.ready())
			return;
		if(!output.clear())
			return;
		
		int[] inst = instructionInput.pop();
		inst = new int[] {inst[0], inst[1], inst[2], inst[3], inst[4], 0 }; 
				
		switch(Opcode.fromInt(inst[0])) {
		case Jmp:
			programCounter = inst[4] + 1 + inst[1];
			Output.jumping_info.println("Fetch is jumping");
			clearOutput.put(true);
			return;
		case Br:
			programCounter = inst[1];
			Output.jumping_info.println("Fetch is Branching");
			clearOutput.put(true);
			return;
		case JmpN:
		case JmpZ:
			inst[3] = inst[4] + 1;
			inst[5] = instructionCounter++;
			output.put(inst);
			clearOutput.put(true);
			
			if(Settings.BRANCH_PREDICTION_MODE != BranchPrediciton.Blocking) {
				boolean prediction = predictor.predict(inst);
				inst[4] = prediction?1:0;
				if(prediction)
					programCounter = inst[3] + inst[1];
				else
					programCounter = inst[3];
			} else {
				halt = true;
			}
			break;
		case Ln:
			clearOutput.put(true);
			inst[5] = instructionCounter++;
			output.put(inst);
			halt = true;
			break;
		default:
			inst[5] = instructionCounter++;
			output.put(inst);
			break;
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
