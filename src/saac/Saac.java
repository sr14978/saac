package saac;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import saac.clockedComponents.BranchExecutionUnit;
import saac.clockedComponents.Decoder;
import saac.clockedComponents.DepChecker;
import saac.clockedComponents.DualReservationStation;
import saac.clockedComponents.ExecutionUnit;
import saac.clockedComponents.Fetcher;
import saac.clockedComponents.InstructionsSource;
import saac.clockedComponents.Issuer;
import saac.clockedComponents.LoadStoreExecutionUnit;
import saac.clockedComponents.RegisterFile;
import saac.clockedComponents.RegisterFile.RegItem;
import saac.clockedComponents.WritebackHandler;
import saac.dataObjects.BranchResult;
import saac.dataObjects.FilledInInstruction;
import saac.dataObjects.InstructionResult;
import saac.dataObjects.RegisterResult;
import saac.dataObjects.VirtualInstruction;
import saac.interfaces.BufferedConnection;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.interfaces.FListConnection;
import saac.unclockedComponents.BranchPredictor;
import saac.unclockedComponents.Label;
import saac.unclockedComponents.Memory;
import saac.utils.Output;
import saac.utils.RateUtils;
import saac.utils.parsers.ParserException;

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
			if(Thread.interrupted())
				throw new InterruptedException();
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
	
	public Saac(String programName) throws IOException, ParserException {
		InstructionCounter = 0;
		CycleCounter = 0;
		
		List<ClearableComponent> clearables = new ArrayList<>();
		
		Memory memory = new Memory();
		
		List<FConnection<FilledInInstruction>> dualRSToEUs = new ArrayList<>();
		List<FConnection<InstructionResult>> EUToWBs = new ArrayList<>();
		List<ExecutionUnit> EUs = new ArrayList<>();
		for(int i = 0; i<Settings.NUMBER_OF_EXECUTION_UNITS; i++) {
			FConnection<FilledInInstruction> dualRSToEU = new FConnection<>();
			FConnection<InstructionResult> EUtoWB = new FConnection<>();
			EUs.add(new ExecutionUnit(dualRSToEU.getOutputEnd(), EUtoWB.getInputEnd()));
			dualRSToEUs.add(dualRSToEU);
			EUToWBs.add(EUtoWB);
		}
	
		FConnection<FilledInInstruction> issueToLS = new FConnection<>();
		FConnection<InstructionResult> LStoWB = new FConnection<>();
		LoadStoreExecutionUnit LSEU = new LoadStoreExecutionUnit(issueToLS.getOutputEnd(), LStoWB.getInputEnd(), memory);
		
		FConnection<FilledInInstruction> issueToBr = new FConnection<>();
		FConnection<BranchResult> brToFetch = new FConnection<>();
		FConnection<InstructionResult> brToWB = new FConnection<>();
		BranchExecutionUnit brUnit = new BranchExecutionUnit(
				issueToBr.getOutputEnd(), brToFetch.getInputEnd(), brToWB.getInputEnd());
		
		FListConnection<FilledInInstruction> issueToDualRS = new FListConnection<>();
		Connection<Boolean> dualToIssuer = new Connection<>();
		DualReservationStation dualRS = new DualReservationStation(
				dualRSToEUs.stream().map(x->x.getInputEnd()).collect(Collectors.toList()),
				issueToDualRS.getOutputEnd(), 
				dualToIssuer.getInputEnd()
			);
				
		FListConnection<int[]> fetchToDecode = new FListConnection<>();
		FListConnection<VirtualInstruction> decodeToDep = new FListConnection<>();
		
		Connection<RegItem[]> paramADepToReg = new Connection<>();
		Connection<RegItem[]> paramBDepToReg = new Connection<>();
		Connection<RegItem[]> paramCDepToReg = new Connection<>();
		
		Connection<Integer[]> paramAReg_RegToIssue = new Connection<>();
		Connection<Integer[]> paramBReg_RegToIssue = new Connection<>();
		Connection<Integer[]> paramCReg_RegToIssue = new Connection<>();
				
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
		
		FListConnection<VirtualInstruction> opcodeDepToIssue = new FListConnection<>();
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
