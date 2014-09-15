package anl.verdi.plot.gui;

import java.awt.Cursor;
import java.awt.Frame;

import anl.verdi.core.VerdiApplication;
import anl.verdi.formula.Formula;
import anl.verdi.plot.config.PlotConfiguration;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class AbstractPlotCreator implements PlotCreator {

	protected VerdiApplication app;
	protected Formula.Type type;
	protected PlotConfiguration config;

	private boolean postRequired = false;

	public AbstractPlotCreator(Formula.Type type, VerdiApplication app, PlotConfiguration config) {
		this.type = type;
		this.app = app;
		this.config = config;
	}

	public AbstractPlotCreator(Formula.Type type, VerdiApplication app) {
		this(type, app, new PlotConfiguration());
	}


	/**
	 * Creates the plot.
	 */
	public Plot createPlot() {
		try {
			preCreatePlot();
			return doCreatePlot();
		} finally {
			if (postRequired) {
				postCreatePlot();
			}
		}
	}

	protected void preCreatePlot() {
		Frame frame = app.getGui().getFrame();
		if (frame != null) frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		postRequired = true;
	}

	protected void postCreatePlot() {
		Frame frame = app.getGui().getFrame();
		if (frame != null) frame.setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Perform the actual plot creation.
	 * 
	 */
	public abstract Plot doCreatePlot();

}
