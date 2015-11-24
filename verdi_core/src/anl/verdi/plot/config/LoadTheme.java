package anl.verdi.plot.config;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;

import saf.core.ui.util.FileChooserUtilities;
//import simphony.util.messages.MessageCenter;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.util.PlotProperties;
import anl.verdi.util.Tools;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class LoadTheme extends AbstractAction {

	private static final long serialVersionUID = 28016951641413950L;
	static final Logger Logger = LogManager.getLogger(LoadTheme.class.getName());

//	private static final MessageCenter center = MessageCenter.getMessageCenter(LoadConfiguration.class);

	private Plot plot;
	private JFreeChart chart;

	public LoadTheme(Plot plot, JFreeChart chart) {
		super("Load Chart Theme");
		this.plot = plot;
		this.chart = chart;
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		PlotConfiguration config = plot.getPlotConfiguration();
		File file = FileChooserUtilities.getOpenFile(Tools.getConfigFolder(config));
		
		if (file != null) {
			try {
				//ChartTheme darkness = StandardChartTheme.createDarknessTheme(); 
				ChartTheme std = new StandardChartTheme("Std", true);
				PlotProperties plotProperties = PlotProperties.getInstance();
				plotProperties.setCurrentTheme(std);
				std.apply(chart);
			} catch (Exception ex) {
				Logger.error("Error loading configuration " + ex.getMessage());
			}
		}
	}
	
}
