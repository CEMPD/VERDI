package anl.verdi.plot.gui;

import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

import javax.swing.JPanel;

public abstract class AbstractPlotPanel extends JPanel implements ComponentListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -9094397586091328371L;
	
	public AbstractPlotPanel(Boolean doubleBuffered) {
		super(doubleBuffered);
		addComponentListener(this);
	}
	
	public void viewClosed() {
		removeComponentListener(this);
	}

	public abstract void draw();
	
	public void markDirty() {}
	
	// Window hidden callback:

	public void componentHidden(ComponentEvent unused) { }

	// Window shown callback:

	public void componentShown(ComponentEvent unused) {
		draw();
	}

	// Window resized callback:

	//Force redraw to adapt to new screen size
	public void componentResized(ComponentEvent unused) {
		markDirty();
	}

	// Window moved callback:

	public void componentMoved(ComponentEvent unused) {	}
	
	// Paint/draw:

	public void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		draw();
	}


}
