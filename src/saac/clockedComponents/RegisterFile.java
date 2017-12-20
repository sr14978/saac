package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import saac.Settings;
import saac.dataObjects.Instruction.Results.RegisterResult;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FListConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;

public class RegisterFile implements ClockedComponentI, VisibleComponentI, ClearableComponent {
	
	private final static int ArchitecturalRegistersNum = 16;
	private int[] architecturalRegisters = new int[ArchitecturalRegistersNum];
	private boolean[] architecturalDirties = new boolean[ArchitecturalRegistersNum];
	private static int VectorWidth = 4;
	List<List<RatItem>> RAT = new ArrayList<List<RatItem>>();
	
	FListConnection<RegisterResult>.Output writeBackToRegisters;
	
	public RegisterFile(FListConnection<RegisterResult>.Output writeBackToRegisters){
		for(int i = 0; i<ArchitecturalRegistersNum; i++) {
			List<RatItem> l = new LinkedList<>();
			l.add(RatItem.Architectural(i));
			RAT.add(l);
		}
		this.writeBackToRegisters = writeBackToRegisters;
	}
	
	public boolean isDirty(int registerNumber) {
		return architecturalDirties[registerNumber];
	}
	
	public void setDirty(int registerNumber, boolean value) {
		architecturalDirties[registerNumber] = value;
	}
	
	public int getScalarRegisterValue(int registerNumber) {
		return architecturalRegisters[registerNumber];
	}
		
	public void setScalarRegisterValue(int registerNumber, int value) {
		architecturalRegisters[registerNumber] = value;
	}
	
	public int[] getVectorRegisterValue(int registerNumber) {
		assert registerNumber + VectorWidth < ArchitecturalRegistersNum;
		return new int[] {
				architecturalRegisters[registerNumber],
				architecturalRegisters[registerNumber+1],
				architecturalRegisters[registerNumber+2],
				architecturalRegisters[registerNumber+3]
			};
	}
	
	public void setVectorRegisterValue(int registerNumber, int[] value) {
		assert value != null && value.length == VectorWidth && registerNumber + VectorWidth < ArchitecturalRegistersNum; 
		for(int i = 0; i<4; i++) {
			architecturalRegisters[registerNumber+i] = value[i];
		}
	}

	public RatItem getLatestRegister(int registerNumber) {
		return RAT.get(registerNumber).get(0);
	}
	
	public void setLatestRegister(int registerNumber, RatItem virtualRegisterNumber) {
		RAT.get(registerNumber).add(0,virtualRegisterNumber);
	}
	
	private void removeRatEntry(int registerNumber, int virtualNumber) {
		RAT.get(registerNumber).removeIf(i->i.isVirtual() && i.getValue() == virtualNumber);
	}
	
	private void removeRatEntry(int registerNumber, Function<Integer, Boolean> f) {
		RAT.get(registerNumber).removeIf(i->i.isVirtual() && f.apply(i.getValue()));
	}

	public static class RatItem {
		private Optional<Integer> virtualRegister;
		private Optional<Integer> architecturalRegister;

		public static RatItem Virtual(int value) {
			return new RatItem(Optional.of(value), Optional.empty());
		}
		
		public static RatItem Architectural(int value) {
			return new RatItem(Optional.empty(), Optional.of(value));
		}
		
		private RatItem(Optional<Integer> virtualRegister, Optional<Integer> architecturalRegister) {
			this.virtualRegister = virtualRegister;
			this.architecturalRegister = architecturalRegister;
		}
		public boolean isVirtual() {
			return virtualRegister.isPresent();
		}
		public boolean isArchitectural() {
			return architecturalRegister.isPresent();
		}
		public int getValue() {
			return virtualRegister.isPresent()?virtualRegister.get():architecturalRegister.get();
		}
		public String toString() {
			return virtualRegister.isPresent()?"v"+virtualRegister.get():"a"+architecturalRegister.get();
		}
	}

	@Override
	public void tick() throws Exception {
		if(writeBackToRegisters.ready()) {
			RegisterResult[] updates = writeBackToRegisters.pop();
			for(RegisterResult update : updates) {
				int archRegNum = update.getTarget().getRegNumber();
				setScalarRegisterValue(archRegNum, update.getValue());
				removeRatEntry(archRegNum, update.getVirtualNumber());
				if(!Settings.REGISTER_RENAMING_ENABLED) {
					setDirty(archRegNum, false);
				}
			}
		}
	}

	@Override
	public void tock() throws Exception {}
	
	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Register File");
			gc.setColor(Color.BLACK);
			for( int i = 0; i<ArchitecturalRegistersNum; i++) {
				gc.drawString(Integer.toString(architecturalRegisters[i]), 25*i+5, 35);
				if(architecturalDirties[i])
					gc.drawString("(d)", 25*i+5, 20);
			}
			gc.drawString(RAT.toString(), 5, 45);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear(int i) {
		for(int x = 0; x<ArchitecturalRegistersNum; x++) {
			removeRatEntry(x, y->y>=i);
		}
	}
	
}
