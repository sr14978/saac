package saac;

import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import saac.interfaces.ComponentView;

public class Main extends Application {
	
	public static void main(String[] args) {
		launch();
	}
	
	List<ComponentView> visibleComponents = new ArrayList<>();;
	Saac saac;
	boolean stopped = false;
	GraphicsContext gc;
	Label rate;
    Main self = this;
	
	public Main() {
		saac = new Saac(visibleComponents);
	}
	
    @Override
    public void start(Stage primaryStage) {
    	
        primaryStage.setTitle("Saac - Sam's Advanced Architecture Computer");
        
        final Canvas canvas = new Canvas(1600,800);
        gc = canvas.getGraphicsContext2D();
        
        Button start = new Button("Start");
        Button stop = new Button("Stop");
        Button step = new Button("Step");
        
        ButtonHandler buttonHandler = new ButtonHandler(start, stop, step);
        start.setOnAction(buttonHandler);
        stop.setOnAction(buttonHandler);
        step.setOnAction(buttonHandler);
        
        rate = new Label("#rate");
        
        HBox topBar= new HBox();
        topBar.getChildren().addAll(start, stop, step, rate);
        
        BorderPane border = new BorderPane();        
        border.setCenter(canvas);
        border.setTop(topBar);
        
        StackPane root = new StackPane();
        root.getChildren().add(border);
        
        primaryStage.setScene(new Scene(root, 1700, 900));
        primaryStage.show();
        
        //saac.mutex.lock();
		//stopped = true;
        
        new Thread(){
			public void run() {
				try {
					saac.worker(self::paint);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
        
    }
    
    public void paint() {
		for(ComponentView cv : visibleComponents)
			cv.paint(gc);
		float rateVal = round((float) Saac.InstructionCounter / saac.cycleCounter);
		Platform.runLater(()->rate.setText(Float.toString(rateVal)));
	}
    
    static float round(float i) {
    	return (float) Math.round(( i )*100 ) / 100;
    }
    
    @Override
    public void stop(){
        System.exit(0);
    }
    
    class ButtonHandler implements EventHandler<ActionEvent> {

    	Button start, stop, step;
    	
    	ButtonHandler(Button start, Button stop, Button step) {
    		this.start = start;
    		this.stop = stop;
    		this.step = step;
    	}
    	
		@Override
		public void handle(ActionEvent event) {
			if(event.getSource().equals(start)) {
				if(stopped) {
	        		saac.mutex.unlock();
	        		stopped = false;
	        	}
			} else if(event.getSource().equals(stop)) {
				if(!stopped) {
					saac.mutex.lock();
            		stopped = true;
            	}
			} else if(event.getSource().equals(step)) {
				if(stopped) {
	            	try {
	            		saac.step(self::paint);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} 
		}
    }
	
}
