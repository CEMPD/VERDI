package anl.verdi.plot.types;

import java.awt.Window;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import org.jfree.chart.JFreeChart;

import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.gui.ConfigDialog;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.ThemeDialog;
import anl.verdi.plot.jfree.ChartPanel;
import anl.verdi.plot.util.PlotExporter;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class AbstractPlot implements Plot {

	protected enum ControlAction {
		ZOOM, PROBE
	}

	protected VerdiChartPanel panel;

	/**
	 * Exports an image of this Plot to the specified file in the
	 * specified format.
	 *
	 * @param format the image format. One of PlotExporter.JPG, PlotExporter.TIF,
	 *               PlotExporter.PNG, or PlotExporter.BMP
	 * @param width width of image in pixels
	 * @param height height of image in pixels
	 * @param file   the file to save the image to.
	 */
	public void exportImage(String format, File file, int width, int height) throws IOException {
		PlotExporter exporter = new PlotExporter(this);
		exporter.save(format, file, width, height);
	}
	
	/**
	 * Gets a BufferedImage of the plot.
	 *
	 * @return a BufferedImage of the plot.
	 */
	public BufferedImage getBufferedImage() {
		return getBufferedImage(panel.getWidth(), panel.getHeight());
	}

	/**
	 * Gets a BufferedImage of the plot.
	 *
	 * @param width  the width of the image in pixels
	 * @param height the height of the image in pixels
	 * @return a BufferedImage of the plot.
	 */
	public BufferedImage getBufferedImage(int width, int height) {
		return panel.getChart().createBufferedImage(width, height);
	}



	/**
	 * Configure this Plot according to the specified PlotConfiguration.
	 *
	 * @param config the new plot configuration
	 */
	public void configure(PlotConfiguration config) {
		//todo implement method
	}
	
	public void configure(PlotConfiguration config, Plot.ConfigSoure source) {
		//todo implement method
	}

	/**
	 * Gets this Plot's configuration data.
	 *
	 * @return this Plot's configuration data.
	 */
	public PlotConfiguration getPlotConfiguration() {
		return null;  //todo implement method
	}

	// customized chart panel for VerdiPlots
	protected class VerdiChartPanel extends ChartPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4069425929226151472L;
		private boolean zoom = false;

		public VerdiChartPanel(JFreeChart chart) {
			super(chart);
		}

		public VerdiChartPanel(JFreeChart chart, boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips) {
			super(chart, properties, save, print, zoom, tooltips);
		}

		public VerdiChartPanel(JFreeChart chart, boolean useBuffer) {
			super(chart, useBuffer);
		}

		public VerdiChartPanel(JFreeChart chart, int width, int height, int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth, int maximumDrawHeight, boolean useBuffer, boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips) {
			super(chart, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight, useBuffer, properties, save, print, zoom, tooltips);
		}

		public void doZoom(Rectangle2D selection) {
			zoom = true;
			zoom(selection);
			zoom = false;
		}

		public void zoom(Rectangle2D selection) {
			if (zoom) super.zoom(selection);
			repaint();
		}


		public boolean isZoom() {
			return zoom;
		}

		public void setZoom(boolean zoom) {
			this.zoom = zoom;
		}

		/**
		 * Displays a dialog that allows the user to edit the properties for the
		 * current chart.
		 *
		 * @since 1.0.3
		 */
		public void doEditChartProperties() {
			Window window = SwingUtilities.getWindowAncestor(panel);
			ConfigDialog dialog = null;
			if (window instanceof JFrame) dialog = new ConfigDialog((JFrame) window);
			else dialog = new ConfigDialog((JDialog)window);
			dialog.init(AbstractPlot.this, null);
			dialog.setSize(500, 600);
			dialog.setVisible(true);
		}
		
		/**
		 * Displays a dialog that allows the user to edit the theme for the
		 * current chart.
		 *
		 * @since 1.6
		 */
		public void doEditChartTheme() {
			Window window = SwingUtilities.getWindowAncestor(panel);
			ThemeDialog dialog = null;
			if (window instanceof JFrame) dialog = new ThemeDialog((JFrame) window);
			else dialog = new ThemeDialog((JDialog)window);
			dialog.init(AbstractPlot.this, null);
			dialog.setSize(500, 600);
			dialog.setVisible(true);
		}
	}
}
