package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Arrays;
import java.util.Optional;

import saac.dataObjects.Instruction.Results.RegisterResult;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FListConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;

public class RegisterFile implements ClockedComponentI, VisibleComponentI {

	private final static int ArchitecturalRegistersNum = 12;
	private int[] architecturalRegisters = new int[ArchitecturalRegistersNum];
	private boolean[] architecturalDirties = new boolean[ArchitecturalRegistersNum];
	
	RatItem[] RAT = new RatItem[ArchitecturalRegistersNum];
	
	FListConnection<RegisterResult>.Output writeBackToRegisters;
	
	public RegisterFile(FListConnection<RegisterResult>.Output writeBackToRegisters){
		for(int i = 0; i<RAT.length; i++) {
			RAT[i] = RatItem.Architectural(i);
		}
		this.writeBackToRegisters = writeBackToRegisters;
	}
	
	public boolean isDirty(int registerNumber) {
		return architecturalDirties[registerNumber];
	}
	
	public void setDirty(int registerNumber, boolean value) {
		architecturalDirties[registerNumber] = value;
	}
	
	public int getRegisterValue(int registerNumber) {
		return architecturalRegisters[registerNumber];
	}
		
	public void setRegisterValue(int registerNumber, int value) {
		architecturalRegisters[registerNumber] = value;
	}

	public RatItem getLatestRegister(int registerNumber) {
		return RAT[registerNumber];
	}
	
	public void setLatestRegister(int registerNumber, RatItem virtualRegisterNumber) {
		RAT[registerNumber] = virtualRegisterNumber;
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
				setRegisterValue(archRegNum, update.getValue());
				RatItem i = getLatestRegister(archRegNum);
				if(i.isArchitectural()) {
					throw new Exception("RAT item should be virtual");
				} else if(update.getVirtualNumber() == i.getValue()) {
					setLatestRegister(archRegNum, RatItem.Architectural(archRegNum));
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
			gc.drawString(Arrays.toString(RAT), 5, 45);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}
	
}
