package saac.clockedComponents;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import saac.Settings;
import saac.dataObjects.Instruction.Complete.CompleteInstruction;
import saac.dataObjects.Instruction.Partial.PartialInstruction;
import saac.dataObjects.Instruction.Partial.SourceItem;
import saac.dataObjects.Instruction.Results.RegisterResult;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentView;
import saac.interfaces.ComponentViewI;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.interfaces.MultiFConnection;
import saac.interfaces.VisibleComponentI;
import saac.utils.DrawingHelper;
import saac.utils.Output;

public class ReservationStation implements ClockedComponentI, VisibleComponentI{
	FListConnection<PartialInstruction>.Output instructionInput;
	List<FConnection<CompleteInstruction>.Input> instructionOutputs;
	MultiFConnection<RegisterResult>.Output virtualRegisterValueBus;
	static final int partialBufferMaxSize = 8;
	List<PartialInstruction> partialBuffer = new LinkedList<>();
	static final int completeBufferMaxSize = 8;
	List<CompleteInstruction> completeBuffer = new LinkedList<>();
	
	
	public ReservationStation(FListConnection<PartialInstruction>.Output instructionInput,
			List<FConnection<CompleteInstruction>.Input> instructionOutputs,
			MultiFConnection<RegisterResult>.Output virtualRegisterValueBus) {
		this.instructionInput = instructionInput;
		this.virtualRegisterValueBus = virtualRegisterValueBus;
		this.instructionOutputs = instructionOutputs;
	}
	
	public ReservationStation(FListConnection<PartialInstruction>.Output instructionInput,
			FConnection<CompleteInstruction>.Input instructionOutputs,
			MultiFConnection<RegisterResult>.Output virtualRegisterValueBus) {
		this.instructionInput = instructionInput;
		this.virtualRegisterValueBus = virtualRegisterValueBus;
		this.instructionOutputs = new ArrayList<>();
		this.instructionOutputs.add(instructionOutputs);
	}
	
	@Override
	public void tick() throws Exception {
		
		if(instructionInput.ready() && partialBuffer.size() < partialBufferMaxSize - Settings.SUPERSCALER_WIDTH) {
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
				}
			}
		}
		
		int i = 0;
		while(i<partialBuffer.size() && completeBuffer.size()<completeBufferMaxSize) {
			PartialInstruction inst = partialBuffer.get(i);
			if(isReady(inst)) {
				partialBuffer.remove(i);
				completeBuffer.add(new CompleteInstruction(inst));
			} else {
				i++;
			}
		}
		
	}

	private boolean isReady(PartialInstruction i) {
		return 		( !i.getParamA().isPresent() || (i.getParamA().isPresent() && i.getParamA().get().isDataValue()) )
				&& 	( !i.getParamB().isPresent() || (i.getParamB().isPresent() && i.getParamB().get().isDataValue()) )
				&& 	( !i.getParamC().isPresent() || (i.getParamC().isPresent() && i.getParamC().get().isDataValue()) );
	}

	private void fillInSingleParamWithResult(Supplier<Optional<SourceItem>> getter, Consumer<SourceItem> setter,  RegisterResult result) {
		if(getter.get().isPresent()) {
			SourceItem p = getter.get().get();
			if(p.isRegisterNum() && p.getValue() == result.getVirtualNumber()) {
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
				CompleteInstruction inst = completeBuffer.remove(0);
				output.put(inst);
				Output.info.println(inst + " sent for execution on EU " + i);
			}
			if(completeBuffer.isEmpty())
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
			gc.drawString(partialBuffer.toString(), 10, 25);
			gc.drawString(completeBuffer.toString(), 10, 40);
		}
	}

	@Override
	public ComponentViewI createView(int x, int y) {
		return new View(x, y);
	}

}
