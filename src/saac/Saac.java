package saac;

import static saac.utils.DrawingHelper.BOX_SIZE;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import saac.clockedComponents.BranchExecutionUnit;
import saac.clockedComponents.Decoder;
import saac.clockedComponents.DualReservationStation;
import saac.clockedComponents.ExecutionUnit;
import saac.clockedComponents.Fetcher;
import saac.clockedComponents.Issuer;
import saac.clockedComponents.LoadStoreExecutionUnit;
import saac.clockedComponents.RegisterFile;
import saac.clockedComponents.WritebackHandler;
import saac.dataObjects.Instruction;
import saac.dataObjects.InstructionResult;
import saac.dataObjects.RegisterResult;
import saac.interfaces.ClockedComponent;
import saac.interfaces.ComponentView;
import saac.interfaces.FConnection;
import saac.unclockedComponents.Memory;

public class Saac implements ClockedComponent {

	public static int InstructionCounter = 0;
	Lock mutex = new ReentrantLock();

	int cycleCounter = 0;
	
	void worker(Runnable f) throws Exception {
		while(true) {
			mutex.lock();
        	mutex.unlock();
			step(f);
		}
	}
	
	void step(Runnable paint) throws Exception {
		Thread.sleep(50);
		tick();
		paint.run();
		Thread.sleep(50);
		tock();
		paint.run();
		cycleCounter++;
		System.out.println("Rate: " + (float) InstructionCounter / cycleCounter);
	}
	
	List<ClockedComponent> clockedComponents;
	
	public Saac(List<ComponentView> visibleComponents) {
		
		Memory memory = new Memory();
		
		FConnection<Integer> issuerToRegister = new FConnection<>();
		FConnection<Integer> registerToIssuer = new FConnection<>();
		FConnection<RegisterResult> WBtoRegister = new FConnection<>();
		RegisterFile registerFile = new RegisterFile(issuerToRegister.getOutputEnd(), registerToIssuer.getInputEnd(), WBtoRegister.getOutputEnd());
		
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
		FConnection<Integer> brToFetch = new FConnection<>();
		BranchExecutionUnit brUnit = new BranchExecutionUnit(issueToBr.getOutputEnd(), brToFetch.getInputEnd());
		
		FConnection<Instruction> issueToDualRS = new FConnection<>();
		DualReservationStation dualRS = new DualReservationStation(dualRSToEU_A.getInputEnd(), dualRSToEU_B.getInputEnd(), issueToDualRS.getOutputEnd());
			
		FConnection<int[]> fetchToDecode = new FConnection<>();
		Fetcher fetcher = new Fetcher(registerFile, fetchToDecode.getInputEnd(), brToFetch.getOutputEnd());
		
		FConnection<Instruction> decodeToIssue = new FConnection<>();
		Decoder decoder = new Decoder(decodeToIssue.getInputEnd(), fetchToDecode.getOutputEnd());
		
		Issuer issuer = new Issuer(registerFile,
				decodeToIssue.getOutputEnd(),
				issueToDualRS.getInputEnd(),
				issueToLS.getInputEnd(),
				issueToBr.getInputEnd()
				);
		WritebackHandler writeBack = new WritebackHandler(registerFile, issuer, EU_AtoWB.getOutputEnd(), EU_BtoWB.getOutputEnd(), LStoWB.getOutputEnd());
		
		
		clockedComponents = new ArrayList<>();
		clockedComponents.add(fetcher);
		clockedComponents.add(decoder);
		clockedComponents.add(issuer);
		clockedComponents.add(dualRS);
		clockedComponents.add(executionUnit_A);
		clockedComponents.add(executionUnit_B);
		clockedComponents.add(LSEU);
		clockedComponents.add(brUnit);
		clockedComponents.add(writeBack);
		
		int middleOffset = (int) (1.5*BOX_SIZE);
		visibleComponents.add(fetcher.createView(middleOffset, 0));
		visibleComponents.add(fetchToDecode.createView(middleOffset, 50));
		visibleComponents.add(registerFile.createView(1200, 100));
		visibleComponents.add(decoder.createView(middleOffset, 100));
		visibleComponents.add(decodeToIssue.createView(middleOffset, 150));
		visibleComponents.add(issuer.createView(middleOffset, 200));
		visibleComponents.add(issueToDualRS.createView(BOX_SIZE, 250));
		visibleComponents.add(dualRS.createView(0, 300));
		visibleComponents.add(dualRSToEU_A.createView(0, 350));
		visibleComponents.add(executionUnit_A.createView(0, 400));
		visibleComponents.add(dualRSToEU_B.createView(BOX_SIZE, 350));
		visibleComponents.add(executionUnit_B.createView(BOX_SIZE, 400));
		visibleComponents.add(issueToLS.createView(2*BOX_SIZE, 250));
		visibleComponents.add(LSEU.createView(2*BOX_SIZE, 400));
		visibleComponents.add(issueToBr.createView(3*BOX_SIZE, 250));
		visibleComponents.add(brUnit.createView(3*BOX_SIZE, 400));
		visibleComponents.add(EU_AtoWB.createView(0, 450));
		visibleComponents.add(EU_BtoWB.createView(BOX_SIZE, 450));
		visibleComponents.add(LStoWB.createView(2*BOX_SIZE, 450));
		visibleComponents.add(writeBack.createView(0, 500));
	}
	
	@Override
	public void tick() throws Exception {
		for(ClockedComponent c : clockedComponents)
			c.tick();
	}

	@Override
	public void tock() throws Exception {
		for(ClockedComponent c : clockedComponents)
			c.tock();
	}    
}
