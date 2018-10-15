package anl.verdi.plot.gui;

import java.awt.Cursor;
import java.awt.Frame;

import anl.verdi.core.VerdiApplication;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class AbstractPlotRequest implements PlotRequest {

	protected VerdiApplication app;
	private boolean postRequired = false;

	/**
	 * Initializes this PlotRequest with the specified application.
	 *
	 * @param app the application object
	 */
	public void init(VerdiApplication app) {
		this.app = app;
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
