package anl.verdi.plot.config;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;

import saf.core.ui.util.FileChooserUtilities;
import simphony.util.messages.MessageCenter;
import anl.verdi.plot.gui.Plot;
import anl.verdi.util.Tools;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class LoadConfiguration extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 28016951641413950L;

	private static final MessageCenter center = MessageCenter.getMessageCenter(LoadConfiguration.class);

	private Plot plot;

	public LoadConfiguration(Plot plot) {
		super("Load Configuration");
		this.plot = plot;
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		PlotConfiguration config = plot.getPlotConfiguration();
		File file = FileChooserUtilities.getOpenFile(Tools.getConfigFolder(config));
		
		if (file != null) {
			try {
				PlotConfiguration newConfig = new PlotConfigurationIO().loadConfiguration(file);
				config.merge(newConfig);
				plot.configure(config, Plot.ConfigSoure.FILE);
			} catch (IOException ex) {
				center.error("Error loading configuration", ex);
			}
		}
	}
	
}
