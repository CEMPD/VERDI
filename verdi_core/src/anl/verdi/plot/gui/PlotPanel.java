package anl.verdi.plot.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import saf.core.ui.event.DockableFrameEvent;
import anl.verdi.formula.Formula;

public class PlotPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2937963505375601326L;
	private Plot plot;
	private String name;

	public PlotPanel(Plot plot, String name) {
		super(new BorderLayout());
		JMenuBar bar = plot.getMenuBar();
		JToolBar toolBar = plot.getToolBar();
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.Y_AXIS));
		if (bar != null) {
			bar.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
			bar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			topPanel.add(bar);
		}
		if (toolBar != null) {
			toolBar.setAlignmentX(JComponent.LEFT_ALIGNMENT);
			if (topPanel.getComponentCount() == 1) {
				topPanel.add(Box.createRigidArea(new Dimension(0, 4)));
			}
			topPanel.add(toolBar);
			topPanel.add(Box.createRigidArea(new Dimension(0, 4)));
		}
		if (topPanel.getComponentCount() > 0) add(topPanel, BorderLayout.NORTH);
		add(plot.getPanel(), BorderLayout.CENTER);
		this.plot = plot;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void addPlotListener(PlotListener listener) {
		plot.addPlotListener(listener);
	}

	public void removePlotListener(PlotListener listener) {
		plot.removePlotListener(listener);
	}

	/**
	 * Gets the type of the plot that this panel shows.
	 *
	 * @return the type of the plot that this panel shows.
	 */
	public Formula.Type getPlotType() {
		return plot.getType();
	}

	/**
	 * Gets the plot this PlotPanel contains.
	 *
	 * @return  the plot this PlotPanel contains.
	 */
	public Plot getPlot() {
		return plot;
	}
	
	public void setViewId(String id) {
		plot.setViewId(id);
	}

	/**
	 * Notifies Plot when its View has been closed. HACK!
	 */

	public void viewClosed() {

		if ( plot instanceof anl.verdi.plot.gui.FastTilePlot ) {
			( (anl.verdi.plot.gui.FastTilePlot) plot).stopThread();
			try {
				Thread.sleep(500);
			} catch( Exception e) {

			}
		}
		if ( plot != null) {
			plot.viewClosed();
			this.removeAll();
			plot = null;
		}

	}
	public void viewFloated(DockableFrameEvent evt){
		if ( plot instanceof anl.verdi.plot.gui.FastTilePlot ) {
			( (anl.verdi.plot.gui.FastTilePlot) plot).viewFloated(evt);
		}
	}
	public void viewRestored(DockableFrameEvent evt){		
		if ( plot instanceof anl.verdi.plot.gui.FastTilePlot ) {
			( (anl.verdi.plot.gui.FastTilePlot) plot).viewRestored(evt);
		}
	}
}
