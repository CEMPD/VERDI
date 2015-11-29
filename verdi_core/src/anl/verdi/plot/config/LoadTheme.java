package anl.verdi.plot.config;

import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.AbstractAction;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;

import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.util.PlotProperties;
import anl.verdi.util.Tools;
import saf.core.ui.util.FileChooserUtilities;

/**
 * @author qun
 * @version $Revision$ $Date$
 */
public class LoadTheme extends AbstractAction {

	private static final long serialVersionUID = 28016951641413950L;
	static final Logger Logger = LogManager.getLogger(LoadTheme.class.getName());
	private JFreeChart chart;

	public LoadTheme(Plot plot, JFreeChart chart) {
		super("Load Chart Theme");
		this.chart = chart;
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		File file = FileChooserUtilities.getOpenFile(Tools.getConfigFolder(null));
		
		if (file != null) {
			try {
				ThemeConfig config = new ThemeConfig(file);
				ChartTheme theme = config.getTheme();
				PlotProperties.getInstance().setCurrentTheme(theme);
				theme.apply(chart);
			} catch (Exception ex) {
				Logger.error("Error loading configuration " + ex.getMessage());
			}
		}
	}
	
}
