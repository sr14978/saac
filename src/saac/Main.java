package saac;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import saac.interfaces.ComponentViewI;
import saac.utils.parsers.ParserException;

public class Main extends JFrame {
	
	private static final long serialVersionUID = 1L;

	public static void main(String[] args) throws IOException, ParserException {
		
		Main window = new Main();
		window.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
	
	List<ComponentViewI> visibleComponents = new ArrayList<>();;
	Saac saac;
	boolean stopped = false;
    Main self = this;
	
    JLabel rateLable;
    Gui gui;
    
	public Main() throws IOException, ParserException {
		saac = new Saac(visibleComponents);
		setTitle("Saac - Sam's Advanced Architecture Computer");
		
		JButton start = new JButton("Start");
		JButton stop = new JButton("Stop");
		JButton step1 = new JButton("Step 1");
		JButton step8 = new JButton("Step 8");
		JButton step64 = new JButton("Step 64");
		
		start.addActionListener(e -> start());
		stop.addActionListener(e -> stop());
		step1.addActionListener(e -> step(1));
		step8.addActionListener(e -> step(8));
		step64.addActionListener(e -> step(64));
		
		JSlider slider = new JSlider();
		slider.setMinimum(0);
		slider.setMaximum(900);
		slider.setValue(450);
		saac.delay = (int) ((150)*1 + 0.25*300);
		slider.addChangeListener(e -> {
			int v = slider.getValue();
			if(v>600)
				saac.delay = (int) ((v-600)*4 + 2*300 + 0.25*300);
			else if(v>300)
				saac.delay = (int) ((v-300)*1 + 0.25*300);
			else
				saac.delay = (int) (v*0.25);
		});
		
		rateLable = new JLabel();
		
		JPanel toolbar = new JPanel(new FlowLayout());
		toolbar.add(start);
		toolbar.add(stop);
		toolbar.add(step1);
		toolbar.add(step8);
		toolbar.add(step64);
		toolbar.add(slider);
		toolbar.add(rateLable);
		
		setLayout(new BorderLayout());
		add(toolbar, BorderLayout.NORTH);
		
		gui = new Gui();
		add(gui, BorderLayout.CENTER);
		
		setSize(1500, 900);
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
			rateLable.setText(Saac.getRate());
			rateLable.repaint();
    	}
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

    void step(int n) {
    	if(stopped) {
    		try {
	    		for(int i = 0; i<n; i++)
		        	saac.step(()->{});
	    		paint();
    		} catch (Exception e) {
				e.printStackTrace();
			}
		}
    }	
}
