package saac;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
import saac.clockedComponents.WritebackHandler;
import saac.dataObjects.BranchResult;
import saac.dataObjects.Instruction;
import saac.dataObjects.InstructionResult;
import saac.dataObjects.RegisterResult;
import saac.interfaces.BufferedConnection;
import saac.interfaces.ClearableComponent;
import saac.interfaces.ClockedComponentI;
import saac.interfaces.ComponentViewI;
import saac.interfaces.Connection;
import saac.interfaces.FConnection;
import saac.unclockedComponents.Memory;
import saac.utils.Instructions.Opcode;
import saac.utils.parsers.ParserException;

public class Saac implements ClockedComponentI {

	public static int InstructionCounter = 0;
	//lock used for pausing
	Lock mutex = new ReentrantLock();

	int cycleCounter = 0;
	
	void worker(Runnable f) throws Exception {
		while(true) {
			mutex.lock();
        	mutex.unlock();
			step(f);
		}
	}
	
	int delay = 200;
	boolean phase = true;
	void step(Runnable paint) throws Exception {
		Thread.sleep(delay);
		if(phase)
			tick();
		else {
			tock();
			cycleCounter++;
			System.out.println("Rate: " + (float) InstructionCounter / cycleCounter);
		}
		paint.run();
		phase = !phase;
	}
	
	List<ClockedComponentI> clockedComponents;
	
	public Saac(List<ComponentViewI> visibleComponents) throws IOException, ParserException {

		List<ClearableComponent> clearables = new ArrayList<>();
		
		Memory memory = new Memory();
						
		FConnection<Instruction> dualRSToEU_A = new FConnection<>();
		FConnection<InstructionResult> EU_AtoWB = new FConnection<>();
		ExecutionUnit executionUnit_A = new ExecutionUnit(dualRSToEU_A.getOutputEnd(), EU_AtoWB.getInputEnd());
		
		FConnection<Instruction> dualRSToEU_B = new FConnection<>();
		FConnection<InstructionResult> EU_BtoWB = new FConnection<>();
		ExecutionUnit executionUnit_B = new ExecutionUnit(dualRSToEU_B.getOutputEnd(), EU_BtoWB.getInputEnd());
		
		FConnection<Instruction> issueToLS = new FConnection<>();
		FConnection<InstructionResult> LStoWB = new FConnection<>();
		LoadStoreExecutionUnit LSEU = new LoadStoreExecutionUnit(issueToLS.getOutputEnd(), LStoWB.getInputEnd(), memory);
		
		FConnection<Instruction> issueToBr = new FConnection<>();
		FConnection<BranchResult> brToFetch = new FConnection<>();
		FConnection<BranchResult> brToWB = new FConnection<>();
		BranchExecutionUnit brUnit = new BranchExecutionUnit(
				issueToBr.getOutputEnd(), brToFetch.getInputEnd(), brToWB.getInputEnd());
		
		FConnection<Instruction> issueToDualRS = new FConnection<>();
		DualReservationStation dualRS = new DualReservationStation(
				dualRSToEU_A.getInputEnd(), dualRSToEU_B.getInputEnd(), issueToDualRS.getOutputEnd());
				
		FConnection<int[]> fetchToDecode = new FConnection<>();
		FConnection<Instruction> decodeToDep = new FConnection<>();
		Decoder decoder = new Decoder(decodeToDep.getInputEnd(), fetchToDecode.getOutputEnd());
				
		Connection<Integer> paramADepToReg = new Connection<>();
		Connection<Integer> paramBDepToReg = new Connection<>();
		Connection<Integer> paramCDepToReg = new Connection<>();
		
		Connection<Integer> paramAReg_RegToIssue = new Connection<>();
		Connection<Integer> paramBReg_RegToIssue = new Connection<>();
		Connection<Integer> paramCReg_RegToIssue = new Connection<>();
				
		FConnection<RegisterResult> WBtoRegister = new FConnection<>();
				
		RegisterFile registerFile = new RegisterFile(
				paramADepToReg.getOutputEnd(),
				paramAReg_RegToIssue.getInputEnd(),
				paramBDepToReg.getOutputEnd(),
				paramBReg_RegToIssue.getInputEnd(),
				paramCDepToReg.getOutputEnd(),
				paramCReg_RegToIssue.getInputEnd(),
				WBtoRegister.getOutputEnd()
				);
		
		FConnection<Integer> addrInput = new FConnection<>();
		FConnection<Boolean> clearInput = new FConnection<>();
		FConnection<int[]> instructionOutput = new FConnection<>();
		InstructionsSource instructionSource = new InstructionsSource(
				addrInput.getOutputEnd(),
				clearInput.getOutputEnd(),
				instructionOutput.getInputEnd()
			);
		
		Fetcher fetcher = new Fetcher(registerFile, clearables,
				fetchToDecode.getInputEnd(),
				brToFetch.getOutputEnd(),
				addrInput.getInputEnd(),
				clearInput.getInputEnd(),
				instructionOutput.getOutputEnd()
				);
		
		FConnection<Instruction> opcodeDepToIssue = new FConnection<>();
		BufferedConnection<Integer> dirtyWBtoDep = new BufferedConnection<>(WritebackHandler.BUFF_SIZE);

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
				issueToLS.getInputEnd(),
				issueToBr.getInputEnd()
			);
		
		WritebackHandler writeBack = new WritebackHandler(registerFile, depChecker,
				EU_AtoWB.getOutputEnd(),
				EU_BtoWB.getOutputEnd(),
				LStoWB.getOutputEnd(),
				brToWB.getOutputEnd(),
				WBtoRegister.getInputEnd(),
				dirtyWBtoDep.getInputEnd()
			);
		
		
		clockedComponents = new ArrayList<>();
		clockedComponents.add(fetcher);
		clockedComponents.add(instructionSource);
		clockedComponents.add(decoder);
		clockedComponents.add(registerFile);
		clockedComponents.add(depChecker);
		clockedComponents.add(issuer);
		clockedComponents.add(dualRS);
		clockedComponents.add(executionUnit_A);
		clockedComponents.add(executionUnit_B);
		clockedComponents.add(LSEU);
		clockedComponents.add(brUnit);
		clockedComponents.add(writeBack);
		
		int middleOffset = (int) (1.5*BOX_SIZE);
		int boxHeight = 50;
		int c = 0;
		visibleComponents.add(fetcher.createView(middleOffset, boxHeight*c));
		visibleComponents.add(addrInput.createView(0, boxHeight*c));
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
		visibleComponents.add(dualRSToEU_A.createView(0, boxHeight*c));
		visibleComponents.add(dualRSToEU_B.createView(BOX_SIZE, boxHeight*c));
		c++;
		visibleComponents.add(executionUnit_A.createView(0, boxHeight*c));
		visibleComponents.add(executionUnit_B.createView(BOX_SIZE, boxHeight*c));
		visibleComponents.add(LSEU.createView(2*BOX_SIZE, boxHeight*c));
		visibleComponents.add(brUnit.createView(3*BOX_SIZE, boxHeight*c));
		c++;
		visibleComponents.add(EU_AtoWB.createView(0, boxHeight*c));
		visibleComponents.add(EU_BtoWB.createView(BOX_SIZE, boxHeight*c));
		visibleComponents.add(LStoWB.createView(2*BOX_SIZE, boxHeight*c));
		visibleComponents.add(brToWB.createView(3*BOX_SIZE, boxHeight*c));
		visibleComponents.add(brToFetch.createView(4*BOX_SIZE, boxHeight*c));
		c++;
		visibleComponents.add(writeBack.createView(0, boxHeight*c));
		c++;
		visibleComponents.add(WBtoRegister.createView(BOX_SIZE/2, boxHeight*c));
		visibleComponents.add(dirtyWBtoDep.createView(3*BOX_SIZE/2, boxHeight*c));
		
		clearables.add(instructionSource);
		clearables.add(decoder);
		clearables.add(depChecker);
		clearables.add(registerFile);
		clearables.add(issuer);
		clearables.add(dualRS);
		clearables.add(executionUnit_A);
		clearables.add(executionUnit_B);
		clearables.add(LSEU);
		clearables.add(brUnit);
		clearables.add(dualRSToEU_A);
		clearables.add(EU_AtoWB);
		clearables.add(dualRSToEU_B);
		clearables.add(EU_BtoWB);
		clearables.add(issueToLS);
		clearables.add(LStoWB);
		clearables.add(issueToBr);
		clearables.add(issueToDualRS);
		clearables.add(fetchToDecode);
		clearables.add(decodeToDep);
		clearables.add(instructionOutput);
		clearables.add(opcodeDepToIssue);
		clearables.add(addrInput);
		clearables.add(clearInput);
		clearables.add(instructionOutput);
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
