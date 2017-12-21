package saac.clockedComponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import saac.Settings;
import saac.dataObjects.Instruction.Complete.CompleteInstruction;
import saac.dataObjects.Instruction.Partial.PartialInstruction;
import saac.dataObjects.Instruction.Partial.PartialLSInstruction;
import saac.dataObjects.Instruction.Results.RegisterResult;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.interfaces.MultiFConnection;

public class LSReservationStation extends ReservationStation {

	FListConnection<Integer>.Output stores;
	public LSReservationStation(FListConnection<PartialInstruction>.Output instructionInput,
			FConnection<CompleteInstruction>.Input instructionOutputs,
			MultiFConnection<RegisterResult>.Output virtualRegisterValueBus,
			Connection<Boolean>.Input isEmpty,
			FListConnection<Integer>.Output stores
			) {
		super(instructionInput, instructionOutputs, virtualRegisterValueBus, isEmpty);
		this.stores = stores;
	}
	
	public void tick() throws Exception {
		if(stores.ready()) {
			Integer[] ids = stores.pop();
			if(Settings.REGISTER_RENAMING_ENABLED) {
				for(Integer id : ids) {
					for(PartialInstruction inst : partialBuffer) {
						PartialLSInstruction LSInst = (PartialLSInstruction) inst;
						List<Integer> is = LSInst.getWaitedForId();
						for(Integer i : new ArrayList<>(is)) {
							if(i.equals(id)) {
								LSInst.getWaitedForId().remove(i);
							}
						}
					}
				}
			}
		}
		super.tick();
	}

	public boolean isReady(PartialInstruction i) {
		return isAllParametersAndMemPresent(i);
	}
	
	public static boolean isAllParametersAndMemPresent(PartialInstruction i) {
		if(Settings.REGISTER_RENAMING_ENABLED) {
			return ReservationStation.isAllParametersPresent(i) && ((PartialLSInstruction) i).getWaitedForId().isEmpty();
		} else {
			return ReservationStation.isAllParametersPresent(i);
		}
	}
	
}
