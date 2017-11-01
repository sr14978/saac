package saac;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import saac.interfaces.ComponentViewI;

public class Main extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) {
		Main window = new Main();
		window.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	List<ComponentViewI> visibleComponents = new ArrayList<>();;
	Saac saac;
	boolean stopped = false;
    Main self = this;
	
    RateLabel rateLable;
    Gui gui;
    
	public Main() {
		saac = new Saac(visibleComponents);
		setTitle("Saac - Sam's Advanced Architecture Computer");
		
		JButton start = new JButton("Start");
		JButton stop = new JButton("Stop");
		JButton step = new JButton("Step");
		
		start.addActionListener(e -> start());
		stop.addActionListener(e -> stop());
		step.addActionListener(e -> step());
		
		JSlider slider = new JSlider();
		slider.setMinimum(0);
		slider.setMaximum(1000);
		slider.setValue(saac.delay);
		slider.addChangeListener(e -> saac.delay = slider.getValue());
		
		rateLable = new RateLabel();
		
		JPanel toolbar = new JPanel(new FlowLayout());
		toolbar.add(start);
		toolbar.add(stop);
		toolbar.add(step);
		toolbar.add(slider);
		toolbar.add(rateLable);
		
		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		
		gui = new Gui();
		add(gui, BorderLayout.CENTER);
		
		setSize(1700, 900);
		setVisible(true);
		
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
	
	class RateLabel extends JLabel {
		@Override
	    public void paintComponent(Graphics gg) {
			System.out.println("hi");
			float rateVal = round((float) Saac.InstructionCounter / saac.cycleCounter);
			this.setText(String.format("%.2f", rateVal));
			super.paintComponent(gg);
		}
	}
	
	class Gui extends JPanel {
		@Override
	    public void paintComponent(Graphics gg) {
			Graphics2D g = (Graphics2D) gg;
			g.setColor(getBackground());
			g.fillRect(0, 0, 1700, 900);
			for(ComponentViewI cv : visibleComponents) {
				Point pos = cv.getPosition();
				g.translate(pos.x, pos.y);
				cv.paint(g);
				g.translate(-pos.x, -pos.y);
			}
		}
	}
    
	
    long lastRepaintTime = 0;
    public void paint() {
    	long currentTime = System.currentTimeMillis();
    	if(currentTime - lastRepaintTime > 1000/30) {
    		lastRepaintTime = currentTime;	
			gui.repaint();
			rateLable.repaint();
    	}
	}
    
    static float round(float i) {
    	return (float) Math.round(( i )*100 ) / 100;
    }
    
    void start() {
    	if(stopped) {
    		saac.mutex.unlock();
    		stopped = false;
    	}
    }
    
    void stop() {
    	if(!stopped) {
			saac.mutex.lock();
    		stopped = true;
    	}
    }

    void step() {
    	if(stopped) {
        	try {
        		saac.step(self::paint);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }	
}
