package anl.verdi.plot.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import anl.verdi.data.DataFrame;
import anl.verdi.formula.Formula;
import anl.verdi.plot.config.PlotConfiguration;

/**
 * Interface for Plot classes.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface Plot {
	
	public enum ConfigSoure { 
		GUI, FILE
	}

	/**
	 * Gets the panel that contains the plot component.
	 * 
	 * @return the panel that contains the plot component.
	 */
	JPanel getPanel();

	/**
	 * Gets a menu bar for this Plot. This may return null if
	 * there is no menu bar.
	 *
	 * @return a menu bar for this Plot.
	 */
	JMenuBar getMenuBar();

	/**
	 * Gets a tool bar for this plot. This may
	 * return null if there is no tool bar.
	 *
	 * @return a tool bar for this plot.
	 */
	JToolBar getToolBar();

	/**
	 * Adds the specified PlotListener.
	 *
	 * @param listener the plot listener to add
	 */
	void addPlotListener(PlotListener listener);

	/**
	 * Removes the specified PlotListener.
	 *
	 * @param listener the plot listener to remove
	 */
	void removePlotListener(PlotListener listener);

	/**
	 * Gets the type of the Plot.
	 *
	 * @return the type of the Plot.
	 */
	Formula.Type getType();

	/**
	 * Gets the data that this Plot plots.
	 *
	 * @return the data that this Plot plots.
	 */
	List<DataFrame> getData();

	/**
	 * Exports an image of this Plot to the specified file in the
	 * specified format.
	 *
	 * @param format the image format. One of PlotExporter.JPG, PlotExporter.TIF,
	 * PlotExporter.PNG, or PlotExporter.BMP
	 * @param file the file to save the image to.
	 * @param width width of image in pixels
	 * @param height height of image in pixels
	 * @throws IOException if there is an error creating the image
	 */
	void exportImage(String format, File file, int width, int height) throws IOException;

	/**
	 * Configure this Plot according to the specified PlotConfiguration.
	 *
	 * @param config the new plot configuration
	 */
	void configure(PlotConfiguration config);
	void configure(PlotConfiguration config, ConfigSoure source);

	/**
	 * Gets this Plot's configuration data.
	 *
	 * @return this Plot's configuration data.
	 */
	PlotConfiguration getPlotConfiguration();

	/**
	 * Gets a BufferedImage of the plot.
	 *
	 * @return a BufferedImage of the plot.
	 */
	BufferedImage getBufferedImage();

	/**
	 * Gets a BufferedImage of the plot.
	 *
	 * @param width  the width of the image in pixels
	 * @param height the height of the image in pixels
	 * @return a BufferedImage of the plot.
	 */
	BufferedImage getBufferedImage(int width, int height);
	
	/**
	 * Gets this plot's current title.
	 */
	String getTitle();
	

	/**
	 *  Instances may use this to get access to the DockingManager's view backing this panel 
	 * @param id
	 */
	public void setViewId(String id);
	
	void viewClosed();
	
//	public void setLog(boolean log);
//
//	public boolean isLog();
//
//	public void setLogBase(double logBase);
//
//	public double getLogBase();
}
