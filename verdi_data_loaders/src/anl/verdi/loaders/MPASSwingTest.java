package anl.verdi.loaders;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URL;

import javax.swing.*;

import ucar.nc2.dataset.NetcdfDataset;

public class MPASSwingTest extends JFrame {
	
	MPASDataset ds;
	
	private class MPASPanel extends JPanel {
		
		MPASDataset panelSet;

		
		public MPASPanel(MPASDataset set) {
			panelSet = set;
		}
				
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			doDrawing(g);
			invalidate();
			//repaint();
			//setVisible(true);
			//repaint();
			//super.paintComponent(g);
		}
		
		public void doDrawing(Graphics g) {
	        /*g.setColor(Color.BLACK);
	        int[] xPoints = new int[] { 60, 70, 70, 60 };
	        int[] yPoints = new int[] { 10, 10, 20, 20 };
	        g.drawPolygon(xPoints, yPoints, 4);
	        */
				//panelSet.renderCells(g, getWidth(), getHeight());
		}
	}
	
	public MPASSwingTest(MPASDataset set) {
		ds = set;
		init();
	}
	
	public void init() {
        
        JButton quitButton = new JButton("Quit");
        
        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }
        });
        
        createLayout(quitButton);
        pack();
        
		setTitle("MPAS Swing Test");
		setSize(650, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

	}
	


    private void createLayout(JComponent... arg) {

        Container pane = getContentPane();
        GroupLayout gl = new GroupLayout(pane);
        pane.setLayout(gl);

        gl.setAutoCreateContainerGaps(true);

        MPASPanel panel = new MPASPanel(ds);
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addComponent(panel)
        );

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addComponent(panel)
        );
    }
	
	public static void main(String[] args) {
		
		try {
			String loc = "file:///home/tahoward/allstate/history.2015-08-12_00.00.00.nc";
			NetcdfDataset ds = NetcdfDataset.openDataset(loc);
			final MPASDataset mpasDs = new MPASDataset(new URL(loc), ds);
	       
			EventQueue.invokeLater(new Runnable() {
	            
	            @Override
	            public void run() {
	            	MPASSwingTest ex = new MPASSwingTest(mpasDs);
	                ex.setVisible(true);
	            }
	        });
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
