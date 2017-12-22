package saac.unclockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;

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
		public String toString() {
			return Integer.toString(addr) + "=" + Integer.toString(value);
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
	
	public static int numberOfBits = 2;
	private final static int MAX_STORAGE_SIZE = 4;
	Storage dynamicStorage = new Storage();
	
	public static int totalBinaryBranches;
	public static int totalBinaryCorrectlyPredicted;
	public static int totalLinkBranches;
	public static int totalLinkCorrectlyPredicted;
		
	private Map<Integer, Integer> lastLinkTargets = new HashMap<>();
	
	public BranchPredictor() {
		totalBinaryBranches = 0;
		totalBinaryCorrectlyPredicted = 0;
		totalLinkBranches = 0;
		totalLinkCorrectlyPredicted = 0;
	}
	
	public boolean predictBinary(int[] inst) {
		if(Settings.BRANCH_PREDICTION_MODE == BranchPrediction.Smart_Static)
			return predictSmartStatic(inst);
		else if(Settings.BRANCH_PREDICTION_MODE == BranchPrediction.Always_Taken)
			return predictTakenStatic();
		else if(Settings.BRANCH_PREDICTION_MODE == BranchPrediction.Always_Not_Taken)
			return predictNotTakenStatic();
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
			totalBinaryCorrectlyPredicted++;
		totalBinaryBranches++;
	}

	private boolean predictDynamic(int[] inst) {
		Item i = dynamicStorage.find(inst[4]-1);
		if(i == null)
			return predictSmartStatic(inst);
		return i.value>= 1 << (numberOfBits-1);
	}

	private boolean predictSmartStatic(int[] inst) {
		return inst[1] < 0;
	}

	private boolean predictNotTakenStatic() {		
		return false;
	}
	
	private boolean predictTakenStatic() {		
		return true;
	}
	
	public Optional<Integer> predictLink(int[] inst) {
		if(Settings.LINK_BRANCH_PREDICTION) {
			if(lastLinkTargets.containsKey(inst[4])) {
				return Optional.of(lastLinkTargets.get(inst[4]));
			} else {
				return Optional.empty();
			}
		} else {
			return Optional.empty();
		}
	}
	
	public void updateLinkBranch(BranchResult br) {
		int addr = br.getAddr();
		int newPc = br.getNewPc();
		
		if(!lastLinkTargets.containsKey(addr) && lastLinkTargets.size() == MAX_STORAGE_SIZE) {
			lastLinkTargets.remove(lastLinkTargets.keySet().toArray()[(int) Math.random()*MAX_STORAGE_SIZE]);
		}
		lastLinkTargets.put(addr, newPc);
		if(br.wasCorrect())
			totalLinkCorrectlyPredicted++;
		totalLinkBranches++;
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
			int i;
			for(i = 0; i<dynamicStorage.size(); i++) {
				gc.drawString(Integer.toString(dynamicStorage.get(i).addr), 30 * i + 35, 25);
				gc.drawString(Integer.toString(dynamicStorage.get(i).value), 30 * i + 35, 40);
			}
			for(Integer addr : lastLinkTargets.keySet()) {
				gc.drawString(Integer.toString(addr), 30 * i + 35, 25);
				gc.drawString(Integer.toString(lastLinkTargets.get(addr)), 30 * i + 35, 40);
				i++;
			}
			gc.drawString(RateUtils.getRate(totalBinaryCorrectlyPredicted, totalBinaryBranches), 200, 25);
			gc.drawString(RateUtils.getRate(totalBinaryCorrectlyPredicted, totalBinaryBranches), 200, 40);
		}
	}
	
	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x,y);
	}
	
}
