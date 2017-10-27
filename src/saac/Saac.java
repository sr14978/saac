package saac;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import static saac.DrawingHelper.BOX_SIZE;

public class Saac extends Application implements ClockedComponent {

	static int InstructionCounter = 0;

	
	public static void main(String[] args) throws Exception {
		System.out.println("This is Saac: Started");
		launch(args);
	}
	
	List<ClockedComponent> clockedComponents;
	List<ComponentView> visibleComponents;
	
	public Saac() {
		
		RegisterFile registerFile = new RegisterFile();
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
		visibleComponents = new ArrayList<>();
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
		//System.out.println("Clock tick");
		for(ClockedComponent c : clockedComponents)
//			new Thread(){
//				public void run() {
//					try {
						c.tick();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			}.start();
	}

	@Override
	public void tock() throws Exception {
		//System.out.println("Clock tock");
		for(ClockedComponent c : clockedComponents)
//			new Thread(){
//			public void run() {
//				try {
					c.tock();
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		}.start();
	}
    
	public void paint(GraphicsContext gc) {
		gc.clearRect(0, 0, 1600, 600);
		for(ComponentView vc : visibleComponents)
			vc.paint(gc);
	}
	
	
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Hello World!");
        final Canvas canvas = new Canvas(1600,600);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        StackPane root = new StackPane();
        root.getChildren().add(canvas);
        primaryStage.setScene(new Scene(root, 1700, 900));
        primaryStage.show();
        
        
        new Thread(){
			public void run() {
				int cycleCounter = 0;
				while (true) {
					try {
						Thread.sleep(100);
						tick();
						paint(gc);
						Thread.sleep(100);
						tock();
						paint(gc);
						cycleCounter++;
						System.out.println("Rate: " + (float) InstructionCounter / cycleCounter);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
        
    }
    @Override
    public void stop(){
        System.exit(0);
    }
}
