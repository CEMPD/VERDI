package anl.verdi.plot.gui;

import java.awt.Graphics;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;

import javax.swing.JPanel;

public abstract class AbstractPlotPanel extends JPanel implements ComponentListener {
	
	protected String viewId = null;
	
	protected ActionListener animationHandler = null;
	
	protected long timestepSize;
	
	protected boolean matchObsTimesteps = false;

	
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
	
	public void setViewId(String id) {
		viewId = id;
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
	
	//Set GUI size when called from headless script
	public void setScriptSize(int width, int height) { }
	
	public boolean getMatchObsTimesteps() {
		return matchObsTimesteps;
	}
	
	// Paint/draw:

	public void paintComponent(final Graphics graphics) {
		super.paintComponent(graphics);
		draw();
	}
	
	public void setAnimationHandler(ActionListener listener) {
		animationHandler = listener;
	}

	public long getTimestepSize() {
		return timestepSize;
	}
	
	public abstract void exportShapefile( String baseFileName ) throws IOException;
	
	public abstract void drawBatchImage(int wdth, int hght);
	
	public void setBackgroundImage(String imagePath) {}

}
