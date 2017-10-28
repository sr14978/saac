package saac.clockedComponents;

import java.awt.Point;
import java.util.LinkedList;
import java.util.List;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import saac.dataObjects.Instruction;
import saac.dataObjects.InstructionResult;
import saac.dataObjects.MemoryResult;
import saac.dataObjects.RegisterResult;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.FConnection;
import saac.interfaces.FullChannelException;
import saac.interfaces.VisibleComponent;
import saac.unclockedComponents.Memory;
import saac.utils.DrawingHelper;
import saac.utils.Instructions;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class LoadStoreExecutionUnit implements ClockedComponent, VisibleComponent{
	static final int LDLimit = 3;
	private class Item{
		InstructionResult result;
		int delay;
		Item(InstructionResult r, int d) {
			this.result = r;
			this.delay = d;
		}
	}
	
	private FConnection<Instruction>.Output instructionIn;
	private FConnection<InstructionResult>.Input resultOut;
	private List<Item> buffer = new LinkedList<>();
	private Memory memory;
	
	public LoadStoreExecutionUnit(FConnection<Instruction>.Output instructionIn, FConnection<InstructionResult>.Input resultOut, Memory memory) {
		this.instructionIn = instructionIn;
		this.resultOut = resultOut;
		this.memory = memory;
	}
	
	@Override
	public void tick() throws FullChannelException {
		if(buffer.size() >= LDLimit)
			return;
		
		
		if(!instructionIn.ready())
			return;
		Instruction inst = instructionIn.get();
		
		InstructionResult res = null;		
		switch(inst.getOpcode()) {
		case Ldma:
			res = new RegisterResult(inst.getParamA(), memory.getWord(inst.getParamB()));
			break;
		case Stma:
			memory.setWord(inst.getParamB(), inst.getParamA());
			res = new MemoryResult(inst.getParamB());
			break;
		case Ldmi:
			res = new RegisterResult(inst.getParamA(), memory.getWord(inst.getParamB() + inst.getParamC()));
			break;
		case Stmi:
			memory.setWord(inst.getParamB() + inst.getParamC(), inst.getParamA());
			res = new MemoryResult(inst.getParamB() + inst.getParamC());
			break;
		default:
			throw new NotImplementedException();
		}
		buffer.add(new Item(res, Instructions.InstructionDelay.get(inst.getOpcode())));
	}

	@Override
	public void tock() throws FullChannelException {
		InstructionResult res = null;
		for(Item i : new LinkedList<>(buffer)) {
			if(i.delay > 0)
				i.delay -= 1;
			else if(res == null) {
				res = i.result;
				buffer.remove(i);
			}
		}
		if(res == null)
			return;
		else if (resultOut.clear()) {
			resultOut.put(res);
			res = null;
		}
	}
	
	class View implements ComponentView {
		
		Point position; 
		View(int x, int y){
			position = new Point(x, y);
		}
		
		public void paint(GraphicsContext gc) {
			gc.translate(position.x, position.y);
			DrawingHelper.drawBox(gc, "Load/Store");
			gc.setFill(Color.BLACK);
			for( int i = 0; i<buffer.size(); i++)
				gc.fillText(buffer.get(i).result  + " (" + Integer.toString(buffer.get(i).delay) + ")", 5, 22+10*i);
			gc.translate(-position.x, -position.y);
		}
	}

	@Override
	public ComponentView createView(int x, int y) {
		return new View(x, y);
	}
}
