package saac;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import saac.interfaces.ComponentViewI;
import saac.utils.Output;
import saac.utils.RateUtils;

public class Main extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws Exception {
		final Main window;
		if(args.length == 1) {
			window= new Main(args[0]);
		} else {
			window = new Main();
		}		
		window.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	List<ComponentViewI> visibleComponents;
	Saac saac;
	boolean paused = false;
    Main self = this;
	
    JLabel rateLable;
    Gui gui;
    
    public Main() throws Exception {
    	new Main(null);
    }
    
	public Main(String programName) throws Exception {
		saac = new Saac(programName);
		Worker.init();
		visibleComponents = saac.visibleComponents;
		setTitle("Saac - Sam's Advanced Architecture Computer - " + programName);
		setLayout(new BorderLayout());
		
		JPanel toolbar = new JPanel(new FlowLayout());
		rateLable = new JLabel();
		add(toolbar, BorderLayout.NORTH);
		{
			JButton start = new JButton("Start");
			JButton stop = new JButton("Stop");
			JButton step1 = new JButton("Step 1");
			JButton step8 = new JButton("Step 8");
			JButton step64 = new JButton("Step 64");
			JButton step512 = new JButton("Step 512");
			
			start.addActionListener(e -> start());
			stop.addActionListener(e -> stop());
			step1.addActionListener(e -> step(1));
			step8.addActionListener(e -> step(8));
			step64.addActionListener(e -> step(64));
			step512.addActionListener(e -> step(512));
			
			JSlider slider = new JSlider();
			slider.setMinimum(0);
			slider.setMaximum(900);
			slider.setValue(calculateSliderValue(saac.delay));
			slider.addChangeListener(e -> saac.delay = calculateDelay(slider.getValue()) );
						
			toolbar.add(start);
			toolbar.add(stop);
			toolbar.add(step1);
			toolbar.add(step8);
			toolbar.add(step64);
			toolbar.add(step512);
			toolbar.add(slider);
			toolbar.add(rateLable);
		}
		
		gui = new Gui();
		add(gui, BorderLayout.CENTER);
		
		JLabel settings = new JLabel(
				String.format("Alignment: %s Branch: %s Bypass: %s EUs: %d Width: %d OOO: %s VirtAdresses: %d Renaming: %s LoadLimit %d",
						Settings.ISSUE_WINDOW_METHOD.toString(),
						Settings.BRANCH_PREDICTION_MODE.toString(),
						Boolean.toString(Settings.RESERVATION_STATION_BYPASS_ENABLED),
						Settings.NUMBER_OF_EXECUTION_UNITS,
						Settings.SUPERSCALER_WIDTH,
						Boolean.toString(Settings.OUT_OF_ORDER_ENABLED),
						Settings.VIRTUAL_ADDRESS_NUM,
						Boolean.toString(Settings.REGISTER_RENAMING_ENABLED),
						Settings.LOAD_LIMIT
						)
				); 
		add(settings, BorderLayout.SOUTH);
		
		setSize(1500, 700);
		setVisible(true);
		
		Worker.worker = new Thread(){
			public void run() {
				try {
					saac.worker(self::flagRepaint);
				} catch (InterruptedException e) {
					Output.state.println("Program Finished");
					paint();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Worker.worker.start();		
	}
	
	private static final double s0 = 0.25;
	private static final double s1 = 1;
	private static final double s2 = 4;
	private static final int x1 = 300;
	private static final int x2 = 600;
	private static final int dx = 300;
	private static final int y1 = (int) (s0*dx);
	private static final int y2 = (int) (s0*dx + s1*dx);
	
	private static int calculateSliderValue(int delay) {
		if(delay>y2)
			return (int) (x2 + (delay - y2) / s2);
		else if(delay>y1)
			return (int) (x1 + (delay - y1) / s1);
		else
			return (int) (delay / s0);
	}
	
	private static int calculateDelay(int sliderValue) {
		if(sliderValue>x2)
			return (int) ((sliderValue-x2)*s2 + s1*dx + s0*dx);
		else if(sliderValue>x1)
			return (int) ((sliderValue-x1)*s1 + s0*dx);
		else
			return (int) (sliderValue*s0);
	}
		
	@SuppressWarnings("serial")
	class Gui extends JPanel {
		@Override
	    public void paintComponent(Graphics gg) {
			Graphics2D g = (Graphics2D) gg;
			g.setColor(getBackground());
			g.fillRect(0, 0, 1500, 900);
			for(ComponentViewI cv : visibleComponents) {
				Point pos = cv.getPosition();
				g.translate(pos.x, pos.y);
				try {
					cv.paint(g);
				} catch (Exception e) {
					//don't worry too much
				}
				g.translate(-pos.x, -pos.y);
			}
		}
	}
    
	
    long lastRepaintTime = 0;
    public void flagRepaint() {
    	long currentTime = System.currentTimeMillis();
    	if(currentTime - lastRepaintTime > 1000/30) {
    		lastRepaintTime = currentTime;	
    		paint();
    	}
	}
    
    private void paint() {
    	gui.repaint();
		rateLable.setText(RateUtils.getRate(Saac.InstructionCounter, Saac.CycleCounter)
				+ ", Instruction Count: " + Integer.toString(Saac.InstructionCounter)
				+ ", Cycle Count: " + Integer.toString(Saac.CycleCounter)
				+ (Worker.isFinished()? " - Finished":""));
		rateLable.repaint();
    }
    
    void start() {
    	if(paused && !Worker.isFinished()) {
    		saac.mutex.unlock();
    		paused = false;
    	}
    }
    
    void stop() {
    	if(!paused) {
			saac.mutex.lock();
    		paused = true;
    	}
    }

    void step(int n) {
    	if(paused && !Worker.isFinished()) {
    		try {
	    		for(int i = 0; i<n && !Worker.isFinished(); i++)
		        	saac.step();
	    		paint();
    		} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }	
}
