package saac.unclockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.LinkedList;

import saac.Settings;
import saac.Settings.BranchPrediction;
import saac.dataObjects.Instruction.Results.BranchResult;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.RateUtils;
import saac.utils.NotImplementedException;

public class BranchPredictor implements VisibleComponentI{
	
	class Item {
		int addr;
		int value;
		Item(int addr) {
			this(addr, 1);
		}
		Item(int addr, int value) {
			this.addr = addr;
			this.value = value;
		}
	}
	
	@SuppressWarnings({ "serial" })
	class Storage extends LinkedList<Item>{
		Item find(int addr) {
			for(Item i : this ) {
				if(i.addr == addr)
					return i;
			}
			return null;
		}
	}; 
	
	private final static int numberOfBits = 1;
	private final static int MAX_STORAGE_SIZE = 4;
	Storage dynamicStorage = new Storage();
	
	public static int totalBranches = 0;
	public static int totalCorrectlyPredicted = 0;
	
	public BranchPredictor() {
		totalBranches = 0;
		totalCorrectlyPredicted = 0;
	}
	
	public boolean predict(int[] inst) {
		if(Settings.BRANCH_PREDICTION_MODE == BranchPrediction.Simple_Static)
			return predictSimpleStatic();
		else if(Settings.BRANCH_PREDICTION_MODE == BranchPrediction.Static)
			return predictStatic(inst);
		else if(Settings.BRANCH_PREDICTION_MODE == BranchPrediction.Dynamic) {
			return predictDynamic(inst);
		}
		throw new NotImplementedException();
	}
	
	public void update(BranchResult br) {
		int addr = br.getAddr();
		Item i = dynamicStorage.find(addr);
		if(i == null) {
			if(dynamicStorage.size() == MAX_STORAGE_SIZE) {
				dynamicStorage.remove((int) Math.random()*MAX_STORAGE_SIZE);
			}
			if(br.wasTaken())
				dynamicStorage.add(new Item(addr, 1 << (numberOfBits-1)));
			else
				dynamicStorage.add(new Item(addr, (1 << (numberOfBits-1))-1));
		} else {
			if(br.wasTaken() && i.value < (1<<numberOfBits) -1)
				i.value++;
			else if(!br.wasTaken() && i.value >0)
				i.value--;
		}
		if(br.wasCorrect())
			totalCorrectlyPredicted++;
		totalBranches++;
	}

	private boolean predictDynamic(int[] inst) {
		Item i = dynamicStorage.find(inst[4]);
		if(i == null)
			return predictStatic(inst);
		return i.value>= 1 << (numberOfBits-1);
	}

	private boolean predictStatic(int[] inst) {
		return inst[1] < 0;
	}

	private boolean predictSimpleStatic() {		
		return false;
	}

	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Branch Predictor");
			gc.setColor(Color.BLACK);
			gc.drawString("addr", 5, 25);
			gc.drawString("pred", 5, 40);
			for(int i = 0; i<dynamicStorage.size(); i++) {
				gc.drawString(Integer.toString(dynamicStorage.get(i).addr), 30 * i + 35, 25);
				gc.drawString(Integer.toString(dynamicStorage.get(i).value), 30 * i + 35, 40);
			}
			gc.drawString(RateUtils.getRate(totalCorrectlyPredicted, totalBranches), 200, 30);
		}
	}
	
	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x,y);
	}
	
}
