package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Supplier;

import saac.Settings;
import saac.dataObjects.Instruction.Complete.CompleteInstruction;
import saac.dataObjects.Instruction.Partial.PartialInstruction;
import saac.dataObjects.Instruction.Partial.SourceItem;
import saac.dataObjects.Instruction.Results.RegisterResult;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.interfaces.MultiFConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Output;

public class ReservationStation implements ClockedComponentI, VisibleComponentI, ClearableComponent{
	Connection<Boolean>.Input isEmpty;
	FListConnection<PartialInstruction>.Output instructionInput;
	List<FConnection<CompleteInstruction>.Input> instructionOutputs;
	MultiFConnection<RegisterResult>.Output virtualRegisterValueBus;
	static final int MaxSize = 16;
	
	TreeSet<PartialInstruction> partialBuffer = new TreeSet<>();
	TreeSet<CompleteInstruction> completeBuffer = new TreeSet<>();
		
	public ReservationStation(FListConnection<PartialInstruction>.Output instructionInput,
			List<FConnection<CompleteInstruction>.Input> instructionOutputs,
			MultiFConnection<RegisterResult>.Output virtualRegisterValueBus,
			Connection<Boolean>.Input isEmpty) {
		this.instructionInput = instructionInput;
		this.virtualRegisterValueBus = virtualRegisterValueBus;
		this.instructionOutputs = instructionOutputs;
		this.isEmpty = isEmpty;
		isEmpty.put(true);
	}
	
	public ReservationStation(FListConnection<PartialInstruction>.Output instructionInput,
			FConnection<CompleteInstruction>.Input instructionOutputs,
			MultiFConnection<RegisterResult>.Output virtualRegisterValueBus,
			Connection<Boolean>.Input isEmpty) {
		this.instructionInput = instructionInput;
		this.virtualRegisterValueBus = virtualRegisterValueBus;
		this.instructionOutputs = new ArrayList<>();
		this.instructionOutputs.add(instructionOutputs);
		this.isEmpty = isEmpty;
		isEmpty.put(true);
	}
	
	@Override
	public void tick() throws Exception {
		
		if(instructionInput.ready() && partialBuffer.size() + completeBuffer.size() < MaxSize - Settings.SUPERSCALER_WIDTH) {
			PartialInstruction[] insts = instructionInput.pop();
			for(PartialInstruction inst : insts) {
				partialBuffer.add(inst);
			}
		}
				
		if(virtualRegisterValueBus.ready()) {
			List<RegisterResult> results = virtualRegisterValueBus.pop();
			for(RegisterResult result : results) {
				for(PartialInstruction inst : partialBuffer) {
					fillInSingleParamWithResult(inst::getParamA, inst::setParamA, result);
					fillInSingleParamWithResult(inst::getParamB, inst::setParamB, result);
					fillInSingleParamWithResult(inst::getParamC, inst::setParamC, result);
					fillInSingleParamWithResult(inst::getParamD, inst::setParamD, result);
				}
			}
		}
		
		for(PartialInstruction inst : new TreeSet<>(partialBuffer)) {
			if(isReady(inst)) {
				partialBuffer.remove(inst);
				completeBuffer.add(new CompleteInstruction(inst));
			}
		}
		
		if(!partialBuffer.isEmpty() || !completeBuffer.isEmpty()) {
			isEmpty.put(false);
		}
	}

	public static boolean isReady(PartialInstruction i) {
		return 		( !i.getParamA().isPresent() || (i.getParamA().isPresent() && i.getParamA().get().isDataValue()) )
				&& 	( !i.getParamB().isPresent() || (i.getParamB().isPresent() && i.getParamB().get().isDataValue()) )
				&& 	( !i.getParamC().isPresent() || (i.getParamC().isPresent() && i.getParamC().get().isDataValue()) )
				&& 	( !i.getParamD().isPresent() || (i.getParamD().isPresent() && i.getParamD().get().isDataValue()) );
	}

	public static void fillInSingleParamWithResult(Supplier<Optional<SourceItem>> getter, Consumer<SourceItem> setter,  RegisterResult result) {
		if(getter.get().isPresent()) {
			SourceItem p = getter.get().get();
			if(p.isRegister() && p.getValue() == result.getTarget().getVirtualRegNumber()) {
				setter.accept(SourceItem.Data(result.getValue()));
			}
		}
	}

	@Override
	public void tock() throws Exception {
		if(completeBuffer.size() < 1) {
			return;
		}
		for(int i = 0; i<instructionOutputs.size(); i++) {
			FConnection<CompleteInstruction>.Input output = instructionOutputs.get(i);
			if(output.clear()) {
				CompleteInstruction inst = completeBuffer.pollFirst();
				output.put(inst);
				Output.info.println(inst + " sent for execution on EU " + i);
			}
			if(completeBuffer.isEmpty())
				break;
		}
		if(partialBuffer.isEmpty() && completeBuffer.isEmpty()) {
			isEmpty.put(true);
		}
	}

	class View extends ComponentView {
		
		View(int x, int y) {
			super(x, y);
		}
		
		public void paint(Graphics2D gc) {
			DrawingHelper.drawBox(gc, "Fetcher");
			gc.setColor(Color.BLACK);
			gc.drawString(partialBuffer.size()==MaxSize?"F":"", 5, 32);
			gc.drawString(partialBuffer.toString(), 15, 25);
			gc.drawString(completeBuffer.toString(), 15, 40);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

	@Override
	public void clear(int i) {
		TreeSet<PartialInstruction> newPartialBuffer = new TreeSet<>();
		for(PartialInstruction inst : partialBuffer) {
			if(inst.getVirtualNumber() <= i) {
				newPartialBuffer.add(inst);
			}
		}
		partialBuffer = newPartialBuffer;
		
		TreeSet<CompleteInstruction> newCompleteBuffer = new TreeSet<>();
		for(CompleteInstruction inst : completeBuffer) {
			if(inst.getVirtualNumber() <= i) {
				newCompleteBuffer.add(inst);
			}
		}
		completeBuffer = newCompleteBuffer;
	}

}
