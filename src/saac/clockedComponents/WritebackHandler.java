package saac.clockedComponents;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.awt.Point;

import java.awt.Graphics2D;
import java.awt.Color;
import saac.dataObjects.InstructionResult;
import saac.dataObjects.MemoryResult;
import saac.dataObjects.RegisterResult;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.FConnection;
import saac.interfaces.VisibleComponent;
import saac.utils.DrawingHelper;

public class WritebackHandler implements ClockedComponent, VisibleComponent {
	FConnection<InstructionResult>.Output inputEU_A; 
	FConnection<InstructionResult>.Output inputEU_B; 
	FConnection<InstructionResult>.Output inputLS;
	RegisterFile registerFile;
	DepChecker depChecker;
	FConnection<RegisterResult>.Input resultOutput;
	FConnection<Integer>.Input dirtyOutput;
	
	public WritebackHandler(RegisterFile rf, DepChecker depChecker,
			FConnection<InstructionResult>.Output inputEU_A, 
			FConnection<InstructionResult>.Output inputEU_B, 
			FConnection<InstructionResult>.Output inputLS,
			FConnection<RegisterResult>.Input resultOutput,
			FConnection<Integer>.Input dirtyOutput) {
		this.inputEU_A = inputEU_A;
		this.inputEU_B = inputEU_B;
		this.inputLS = inputLS;
		this.registerFile = rf;
		this.depChecker = depChecker;
		this.resultOutput = resultOutput;
		this.dirtyOutput = dirtyOutput;
	}

	@Override
	public void tick() throws Exception {
		InstructionResult res;
		if(inputLS.ready())
			res = inputLS.get();
		else if(inputEU_A.ready())
			res = inputEU_A.get();
		else if(inputEU_B.ready())
			res = inputEU_B.get();
		else
			return;
		
		if(res instanceof MemoryResult) {
			MemoryResult mr = (MemoryResult) res;
			depChecker.dirtyMem.remove(mr.getValue());
		} else if(res instanceof RegisterResult) {
			RegisterResult rr = (RegisterResult) res;
			System.out.println(String.format("%d is written back to r%d", rr.getValue(), rr.getTarget()));
			resultOutput.put(new RegisterResult(rr.getTarget(), rr.getValue()));
			dirtyOutput.put(rr.getTarget());
		}
	}

	@Override
	public void tock() throws Exception {
		
	}

	class View implements ComponentView {
		
		Point position; 
		View(int x, int y){
			position = new Point(x, y);
		}
		
		public void paint(Graphics2D gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawBox(gc, "Write Back Handler", 3*BOX_SIZE, 50);
			gc.setColor(Color.BLACK);
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}
	
}
