package saac;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import saac.clockedComponents.ArithmeticUnit;
import saac.clockedComponents.BranchExecutionUnit;
import saac.clockedComponents.Decoder;
import saac.clockedComponents.Fetcher;
import saac.clockedComponents.InstructionsSource;
import saac.clockedComponents.LSReservationStation;
import saac.clockedComponents.LoadStoreExecutionUnit;
import saac.clockedComponents.Memory;
import saac.clockedComponents.RegisterFile;
import saac.clockedComponents.ReservationStation;
import saac.clockedComponents.WritebackHandler;
import saac.dataObjects.Instruction.Complete.CompleteInstruction;
import saac.dataObjects.Instruction.Partial.PartialInstruction;
import saac.dataObjects.Instruction.Results.BranchResult;
import saac.dataObjects.Instruction.Results.InstructionResult;
import saac.dataObjects.Instruction.Results.MemoryResult;
import saac.dataObjects.Instruction.Results.RegisterResult;
import saac.interfaces.BufferedConnection;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.interfaces.MultiFConnection;
import saac.unclockedComponents.BranchPredictor;
import saac.unclockedComponents.ReorderBuffer;
import saac.utils.Output;
import saac.utils.RateUtils;

public class Saac implements ClockedComponentI {

	public static int InstructionCounter = 0;
	//lock used for pausing
	Lock mutex = new ReentrantLock();

	static int CycleCounter = 0;
	
	int delay = 200;
	void worker(Runnable paint) throws Exception {
		while(true) {
			mutex.lock();
        	mutex.unlock();
        	Thread.sleep(delay);
			step();
			paint.run();
			if(Thread.interrupted()) {
				registerFile.tick();
				throw new InterruptedException();
			}
				
		}
	}
	
	boolean phase = true;
	void step() throws Exception {
		if(phase)
			tick();
		else {
			tock();
			CycleCounter++;
			Output.state.println(RateUtils.getRate(InstructionCounter, CycleCounter));
		}
		phase = !phase;
	}
	
	
	
	List<ClockedComponentI> clockedComponents = new ArrayList<>();;
	List<ComponentViewI> visibleComponents = new ArrayList<>();
	RegisterFile registerFile;
	
	public Saac(String programName) throws Exception {
		InstructionCounter = 0;
		CycleCounter = 0;
		List<ClearableComponent> clearables = new ArrayList<>();
		
		FListConnection<RegisterResult> writeBackToRegisters = new FListConnection<>();
		registerFile = new RegisterFile(writeBackToRegisters.getOutputEnd());
		
		FListConnection<MemoryResult> sendStoresToMem = new FListConnection<>();
		FListConnection<Integer> storeDonesFromMem = new FListConnection<>();
		Memory memory = new Memory(sendStoresToMem.getOutputEnd(), storeDonesFromMem.getInputEnd());
		
		FConnection<Integer> addrInput = new FConnection<>();
		FConnection<Boolean> clearInput = new FConnection<>();
		FListConnection<int[]> instructionOutput = new FListConnection<>();
		InstructionsSource instructionSource = new InstructionsSource(
				addrInput.getOutputEnd(),
				clearInput.getOutputEnd(),
				instructionOutput.getInputEnd(),
				programName
			);
		
		BranchPredictor branchPredictor = new BranchPredictor();
		BufferedConnection<int[]> fetchToDecode = new BufferedConnection<>(Settings.SUPERSCALER_WIDTH);
		FConnection<BranchResult> BRToFetch = new FConnection<>();

		Fetcher fetcher = new Fetcher(clearables, branchPredictor,
				fetchToDecode.getInputEnd(),
				BRToFetch.getOutputEnd(),
				addrInput.getInputEnd(),
				clearInput.getInputEnd(),
				instructionOutput.getOutputEnd()
			);
		
		Connection<Boolean> isAUReservationStationEmpty = new Connection<>();
		Connection<Boolean> isLSReservationStationEmpty = new Connection<>();
		Connection<Boolean> isBRReservationStationEmpty = new Connection<>();
		FListConnection<PartialInstruction> decodeToAUReservationStation = new FListConnection<>();
		FListConnection<PartialInstruction> decodeToLSReservationStation = new FListConnection<>();
		FListConnection<PartialInstruction> decodeToBRReservationStation = new FListConnection<>();
		
		MultiFConnection<RegisterResult> virtualRegisterValueBus = new MultiFConnection<>(4);
		
		List<FConnection<CompleteInstruction>> resevationStationToAUs = new ArrayList<>();
		List<ArithmeticUnit> AUs = new ArrayList<>();
		List<FConnection<InstructionResult>> AUToWritebacks = new ArrayList<>();
		for(int i = 0; i<Settings.NUMBER_OF_EXECUTION_UNITS; i++) {
			FConnection<CompleteInstruction> resevationStationToAU = new FConnection<>();
			resevationStationToAUs.add(resevationStationToAU);
			FConnection<InstructionResult> AUToWriteback = new FConnection<>();
			AUToWritebacks.add(AUToWriteback);
			AUs.add(new ArithmeticUnit(resevationStationToAU.getOutputEnd(),
					AUToWriteback.getInputEnd(),
					virtualRegisterValueBus.getInputEnd()));
		}
		
		ReservationStation AUreservationStation = new ReservationStation(decodeToAUReservationStation.getOutputEnd(),
				resevationStationToAUs.stream().map(x -> x.getInputEnd()).collect(Collectors.toList()),
				virtualRegisterValueBus.getOutputEnd(),
				isAUReservationStationEmpty.getInputEnd()); 
		
		FConnection<CompleteInstruction> resevationStationToLS = new FConnection<>();
		LSReservationStation LSreservationStation = new LSReservationStation(decodeToLSReservationStation.getOutputEnd(),
				resevationStationToLS.getInputEnd(),
				virtualRegisterValueBus.getOutputEnd(),
				isLSReservationStationEmpty.getInputEnd(),
				storeDonesFromMem.getOutputEnd()
				);
		
		FConnection<CompleteInstruction> resevationStationToBR = new FConnection<>();
		ReservationStation BRreservationStation = new ReservationStation(decodeToBRReservationStation.getOutputEnd(),
				resevationStationToBR.getInputEnd(),
				virtualRegisterValueBus.getOutputEnd(),
				isBRReservationStationEmpty.getInputEnd());
		
		FConnection<InstructionResult> LSToWriteback = new FConnection<>();
		LoadStoreExecutionUnit loadStoreExecutionUnit = new LoadStoreExecutionUnit(memory,
				resevationStationToLS.getOutputEnd(),
				LSToWriteback.getInputEnd(),
				virtualRegisterValueBus.getInputEnd()
				);
		
		FConnection<InstructionResult> BRToWriteback = new FConnection<>();
		BranchExecutionUnit branchExecutionUnit = new BranchExecutionUnit(resevationStationToBR.getOutputEnd(),
				BRToFetch.getInputEnd(), BRToWriteback.getInputEnd());

		ReorderBuffer reorderBuffer = new ReorderBuffer();
		
		Decoder decoder = new Decoder(fetchToDecode.getOutputEnd(), registerFile, reorderBuffer, memory,
				decodeToAUReservationStation.getInputEnd(),
				decodeToLSReservationStation.getInputEnd(),
				decodeToBRReservationStation.getInputEnd(),
				isAUReservationStationEmpty.getOutputEnd(),
				isLSReservationStationEmpty.getOutputEnd(),
				isBRReservationStationEmpty.getOutputEnd(),
				resevationStationToAUs.get(0).getInputEnd(),
				resevationStationToLS.getInputEnd(),
				resevationStationToBR.getInputEnd(),
				virtualRegisterValueBus.getOutputEnd()
				);
		
		WritebackHandler writebackHandler = new WritebackHandler(memory, reorderBuffer,
				AUToWritebacks.stream().map(x->x.getOutputEnd()).collect(Collectors.toList()),
				LSToWriteback.getOutputEnd(),
				BRToWriteback.getOutputEnd(),
				writeBackToRegisters.getInputEnd(),
				virtualRegisterValueBus.getInputEnd(),
				sendStoresToMem.getInputEnd());
		
		//add the components to the list of things drawn on screen - specifying the location and size
		{
			clockedComponents.add(registerFile);
			clockedComponents.add(fetcher);
			clockedComponents.add(instructionSource);
			clockedComponents.add(decoder);
			clockedComponents.add(AUreservationStation);
			clockedComponents.add(LSreservationStation);
			clockedComponents.add(BRreservationStation);
			for(ArithmeticUnit au : AUs) {
				clockedComponents.add(au);
			}
			clockedComponents.add(loadStoreExecutionUnit);
			clockedComponents.add(branchExecutionUnit);
			clockedComponents.add(writebackHandler);
			clockedComponents.add(memory);
		}
		
		{
			int BOX_SIZE = 300;
			int middleOffset = (int) (1.5*BOX_SIZE);
			int boxHeight = 50;
			int c = 0;
			visibleComponents.add(fetcher.createView(middleOffset, boxHeight*c));
			visibleComponents.add(addrInput.createView(0, boxHeight*c));
			visibleComponents.add(clearInput.createView(BOX_SIZE/2, boxHeight*c));
			visibleComponents.add(branchPredictor.createView((int) (3.5*BOX_SIZE), boxHeight*c));	
			c++;
			visibleComponents.add(instructionSource.createView(0, boxHeight*c));
			visibleComponents.add(fetchToDecode.createView(middleOffset, boxHeight*c));
			c++;
			visibleComponents.add(decoder.createView(middleOffset, boxHeight*c));
			visibleComponents.add(instructionOutput.createView(0, boxHeight*c));
			visibleComponents.add(registerFile.createView((int) (3.5*BOX_SIZE), boxHeight*c));
			c++;
			visibleComponents.add(decodeToAUReservationStation.createView(middleOffset - BOX_SIZE, boxHeight*c));
			visibleComponents.add(decodeToLSReservationStation.createView(middleOffset, boxHeight*c));
			visibleComponents.add(decodeToBRReservationStation.createView(middleOffset + BOX_SIZE, boxHeight*c));
			c++;
			visibleComponents.add(AUreservationStation.createView(middleOffset - BOX_SIZE, boxHeight*c));
			visibleComponents.add(LSreservationStation.createView(middleOffset, boxHeight*c));
			visibleComponents.add(BRreservationStation.createView(middleOffset + BOX_SIZE, boxHeight*c));
			c++;
			visibleComponents.add(resevationStationToAUs.get(0).createView(middleOffset - BOX_SIZE, boxHeight*c));
			visibleComponents.add(resevationStationToLS.createView(middleOffset, boxHeight*c));
			visibleComponents.add(resevationStationToBR.createView(middleOffset + BOX_SIZE, boxHeight*c));
			visibleComponents.add(virtualRegisterValueBus.createView((int) (3.5*BOX_SIZE), boxHeight*c));
			c++;
			visibleComponents.add(AUs.get(0).createView(middleOffset - BOX_SIZE, boxHeight*c));
			visibleComponents.add(loadStoreExecutionUnit.createView(middleOffset, boxHeight*c));
			visibleComponents.add(branchExecutionUnit.createView(middleOffset + BOX_SIZE, boxHeight*c));
			c++;
			visibleComponents.add(AUToWritebacks.get(0).createView(middleOffset - BOX_SIZE, boxHeight*c));
			visibleComponents.add(LSToWriteback.createView(middleOffset, boxHeight*c));
			visibleComponents.add(BRToWriteback.createView(middleOffset + BOX_SIZE, boxHeight*c));
			visibleComponents.add(BRToFetch.createView(middleOffset + 2*BOX_SIZE, boxHeight*c));
			c++;
			visibleComponents.add(writebackHandler.createView(0, boxHeight*c));
			c++;
			visibleComponents.add(writeBackToRegisters.createView(0, boxHeight*c));
			visibleComponents.add(sendStoresToMem.createView(middleOffset, boxHeight*c));
			c++;
			visibleComponents.add(memory.createView(middleOffset, boxHeight*c));
			c++;
			visibleComponents.add(storeDonesFromMem.createView(middleOffset, boxHeight*c));
		}
		
		{
			clearables.add(registerFile);
			clearables.add(instructionSource);
			clearables.add(decoder);
			clearables.add(decodeToAUReservationStation);
			clearables.add(AUreservationStation);
			clearables.add(decodeToLSReservationStation);
			clearables.add(LSreservationStation);
			clearables.add(resevationStationToLS);
			clearables.add(decodeToBRReservationStation);
			clearables.add(BRreservationStation);
			clearables.add(resevationStationToBR);
			for(int i = 0; i<Settings.NUMBER_OF_EXECUTION_UNITS; i++) {
				clearables.add(AUs.get(i));
				clearables.add(resevationStationToAUs.get(i));
				clearables.add(AUToWritebacks.get(i));
			}
			clearables.add(loadStoreExecutionUnit);
			clearables.add(branchExecutionUnit);
			clearables.add(LSToWriteback);
			clearables.add(BRToWriteback);
			clearables.add(fetchToDecode);
			clearables.add(addrInput);
			clearables.add(clearInput);
			clearables.add(instructionOutput);
			clearables.add(memory);
		}
		
		/*
		
		
		
		List<FConnection<CompleteInstruction>> dualRSToEUs = new ArrayList<>();
		List<FConnection<InstructionResult>> EUToWBs = new ArrayList<>();
		List<ExecutionUnit> EUs = new ArrayList<>();
		for(int i = 0; i<Settings.NUMBER_OF_EXECUTION_UNITS; i++) {
			FConnection<CompleteInstruction> dualRSToEU = new FConnection<>();
			FConnection<InstructionResult> EUtoWB = new FConnection<>();
			EUs.add(new ExecutionUnit(dualRSToEU.getOutputEnd(), EUtoWB.getInputEnd()));
			dualRSToEUs.add(dualRSToEU);
			EUToWBs.add(EUtoWB);
		}
	
		FConnection<CompleteInstruction> issueToLS = new FConnection<>();
		FConnection<InstructionResult> LStoWB = new FConnection<>();
		LoadStoreExecutionUnit LSEU = new LoadStoreExecutionUnit(issueToLS.getOutputEnd(), LStoWB.getInputEnd(), memory);
		
		FConnection<CompleteInstruction> issueToBr = new FConnection<>();
		FConnection<BranchResult> brToFetch = new FConnection<>();
		FConnection<InstructionResult> brToWB = new FConnection<>();
		BranchExecutionUnit brUnit = new BranchExecutionUnit(
				issueToBr.getOutputEnd(), brToFetch.getInputEnd(), brToWB.getInputEnd());
		
		FListConnection<CompleteInstruction> issueToDualRS = new FListConnection<>();
		Connection<Boolean> dualToIssuer = new Connection<>();
		EUReservationStation dualRS = new EUReservationStation(
				dualRSToEUs.stream().map(x->x.getInputEnd()).collect(Collectors.toList()),
				issueToDualRS.getOutputEnd(), 
				dualToIssuer.getInputEnd()
			);
				
		FListConnection<PartialInstruction> decodeToDep = new FListConnection<>();
		
		FListConnection<RegItem> paramADepToReg = new FListConnection<>();
		FListConnection<RegItem> paramBDepToReg = new FListConnection<>();
		FListConnection<RegItem> paramCDepToReg = new FListConnection<>();
		
		FListConnection<RegVal> paramAReg_RegToIssue = new FListConnection<>();
		FListConnection<RegVal> paramBReg_RegToIssue = new FListConnection<>();
		FListConnection<RegVal> paramCReg_RegToIssue = new FListConnection<>();
				
		FListConnection<RegisterResult> WBtoRegister = new FListConnection<>();
				
		registerFile = new RegisterFile(
				paramADepToReg.getOutputEnd(),
				paramAReg_RegToIssue.getInputEnd(),
				paramBDepToReg.getOutputEnd(),
				paramBReg_RegToIssue.getInputEnd(),
				paramCDepToReg.getOutputEnd(),
				paramCReg_RegToIssue.getInputEnd(),
				WBtoRegister.getOutputEnd()
			);
		Decoder decoder = new Decoder(decodeToDep.getInputEnd(), fetchToDecode.getOutputEnd(), registerFile);
		
		FConnection<Integer> addrInput = new FConnection<>();
		FConnection<Boolean> clearInput = new FConnection<>();
		FListConnection<int[]> instructionOutput = new FListConnection<>();
		InstructionsSource instructionSource = new InstructionsSource(
				addrInput.getOutputEnd(),
				clearInput.getOutputEnd(),
				instructionOutput.getInputEnd(),
				programName
			);
		
		BranchPredictor branchPredictor = new BranchPredictor();
		
		Fetcher fetcher = new Fetcher(registerFile, clearables, branchPredictor,
				fetchToDecode.getInputEnd(),
				brToFetch.getOutputEnd(),
				addrInput.getInputEnd(),
				clearInput.getInputEnd(),
				instructionOutput.getOutputEnd()
			);
		
		FListConnection<PartialInstruction> opcodeDepToIssue = new FListConnection<>();
		BufferedConnection<Integer> dirtyWBtoDep = new BufferedConnection<>(RegisterFile.BUFF_SIZE);

		DepChecker depChecker = new DepChecker(registerFile,
				decodeToDep.getOutputEnd(),
				dirtyWBtoDep.getOutputEnd(),
				opcodeDepToIssue.getInputEnd(),
				paramADepToReg.getInputEnd(),
				paramBDepToReg.getInputEnd(),
				paramCDepToReg.getInputEnd()
			);
				
		Issuer issuer = new Issuer(registerFile,
				opcodeDepToIssue.getOutputEnd(),
				paramAReg_RegToIssue.getOutputEnd(),
				paramBReg_RegToIssue.getOutputEnd(),
				paramCReg_RegToIssue.getOutputEnd(),
				issueToDualRS.getInputEnd(),
				dualRSToEUs.get(0).getInputEnd(),
				dualToIssuer.getOutputEnd(),
				issueToLS.getInputEnd(),
				issueToBr.getInputEnd()
			);
		
		WritebackHandler writeBack = new WritebackHandler(registerFile, depChecker, memory,
				EUToWBs.stream().map(x->x.getOutputEnd()).collect(Collectors.toList()),
				LStoWB.getOutputEnd(),
				brToWB.getOutputEnd(),
				WBtoRegister.getInputEnd(),
				dirtyWBtoDep.getInputEnd()
			);
		
		//add the components to the list of things drawn on screen - specifying the location and size
		{
			clockedComponents.add(fetcher);
			clockedComponents.add(instructionSource);
			clockedComponents.add(decoder);
			clockedComponents.add(registerFile);
			clockedComponents.add(depChecker);
			clockedComponents.add(issuer);
			clockedComponents.add(dualRS);
			for(ExecutionUnit eu : EUs)
				clockedComponents.add(eu);
			clockedComponents.add(LSEU);
			clockedComponents.add(brUnit);
			clockedComponents.add(writeBack);
		}
		{
			int middleOffset = (int) (1.5*BOX_SIZE);
			int boxHeight = 50;
			int c = 0;
			visibleComponents.add(fetcher.createView(middleOffset, boxHeight*c));
			visibleComponents.add(addrInput.createView(0, boxHeight*c));
			visibleComponents.add(branchPredictor.createView(3*BOX_SIZE, boxHeight*c));	
			c++;
			visibleComponents.add(instructionSource.createView(0, boxHeight*c));
			visibleComponents.add(fetchToDecode.createView(middleOffset, boxHeight*c));
			c++;
			visibleComponents.add(decoder.createView(middleOffset, boxHeight*c));
			visibleComponents.add(instructionOutput.createView(0, boxHeight*c));
			c++;
			visibleComponents.add(decodeToDep.createView(middleOffset, boxHeight*c++));
			visibleComponents.add(depChecker.createView(middleOffset, boxHeight*c));
			c++;
			visibleComponents.add(paramADepToReg.createView(middleOffset, boxHeight*c, 3));
			visibleComponents.add(paramBDepToReg.createView(middleOffset+BOX_SIZE/3, boxHeight*c, 3));
			visibleComponents.add(paramCDepToReg.createView(middleOffset+2*BOX_SIZE/3, boxHeight*c, 3));
			c++;
			visibleComponents.add(opcodeDepToIssue.createView(BOX_SIZE/2, boxHeight*c));
			visibleComponents.add(registerFile.createView(middleOffset, boxHeight*c));
			c++;
			visibleComponents.add(paramAReg_RegToIssue.createView(middleOffset, boxHeight*c, 3));
			visibleComponents.add(paramBReg_RegToIssue.createView(middleOffset+BOX_SIZE/3, boxHeight*c, 3));
			visibleComponents.add(paramCReg_RegToIssue.createView(middleOffset+2*BOX_SIZE/3, boxHeight*c, 3));
			c++;
			visibleComponents.add(issuer.createView(middleOffset, boxHeight*c++));
			
			visibleComponents.add(issueToDualRS.createView(BOX_SIZE, boxHeight*c));
			visibleComponents.add(issueToLS.createView(2*BOX_SIZE, boxHeight*c));
			visibleComponents.add(issueToBr.createView(3*BOX_SIZE, boxHeight*c));
			c++;
			visibleComponents.add(dualRS.createView(0, boxHeight*c++));
			visibleComponents.add(dualRSToEUs.get(0).createView(0, boxHeight*c));
			if(Settings.NUMBER_OF_EXECUTION_UNITS>1)
				visibleComponents.add(dualRSToEUs.get(1).createView(BOX_SIZE, boxHeight*c));
			c++;
			visibleComponents.add(EUs.get(0).createView(0, boxHeight*c));
			if(Settings.NUMBER_OF_EXECUTION_UNITS>1)
				visibleComponents.add(EUs.get(1).createView(BOX_SIZE, boxHeight*c));
			visibleComponents.add(LSEU.createView(2*BOX_SIZE, boxHeight*c));
			visibleComponents.add(brUnit.createView(7*BOX_SIZE/2, boxHeight*c));
			c++;
			visibleComponents.add(EUToWBs.get(0).createView(0, boxHeight*c));
			if(Settings.NUMBER_OF_EXECUTION_UNITS>1)
				visibleComponents.add(EUToWBs.get(1).createView(BOX_SIZE, boxHeight*c));
			visibleComponents.add(LStoWB.createView(2*BOX_SIZE, boxHeight*c));
			visibleComponents.add(brToWB.createView(3*BOX_SIZE, boxHeight*c));
			visibleComponents.add(brToFetch.createView(4*BOX_SIZE, boxHeight*c));
			c++;
			visibleComponents.add(writeBack.createView(0, boxHeight*c));
			visibleComponents.add(new Label(4*BOX_SIZE, boxHeight*c, "to fetch"));
			c++;
			visibleComponents.add(WBtoRegister.createView(BOX_SIZE/2, boxHeight*c));
			visibleComponents.add(dirtyWBtoDep.createView(3*BOX_SIZE/2, boxHeight*c));
			c++;
			visibleComponents.add(new Label(BOX_SIZE/2, boxHeight*c, "to registers"));
			visibleComponents.add(new Label(3*BOX_SIZE/2, boxHeight*c, "to dep checker"));
		}
		
		//add certain components to the clearables list given to fetch unit
		{
			clearables.add(instructionSource);
			clearables.add(decoder);
			clearables.add(depChecker);
			clearables.add(issuer);
			clearables.add(dualRS);
			for(int i = 0; i<Settings.NUMBER_OF_EXECUTION_UNITS; i++) {
				clearables.add(EUs.get(i));
				clearables.add(dualRSToEUs.get(i));
				clearables.add(EUToWBs.get(i));
			}
			clearables.add(LSEU);
			clearables.add(brUnit);
			clearables.add(issueToLS);
			clearables.add(paramADepToReg);
			clearables.add(paramBDepToReg);
			clearables.add(paramCDepToReg);
			clearables.add(paramAReg_RegToIssue);
			clearables.add(paramBReg_RegToIssue);
			clearables.add(paramCReg_RegToIssue);
			clearables.add(LStoWB);
			clearables.add(issueToBr);
			clearables.add(issueToDualRS);
			clearables.add(fetchToDecode);
			clearables.add(decodeToDep);
			clearables.add(opcodeDepToIssue);
			clearables.add(addrInput);
			clearables.add(clearInput);
			clearables.add(instructionOutput);
			clearables.add(registerFile);
		}
		*/
	}
	
	@Override
	public void tick() throws Exception {
		for(ClockedComponentI c : clockedComponents)
			c.tick();
	}

	@Override
	public void tock() throws Exception {
		for(ClockedComponentI c : clockedComponents)
			c.tock();
	}    
}
