package anl.verdi.plot.config;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import saf.core.ui.util.FileChooserUtilities;
//import simphony.util.messages.MessageCenter;
import anl.verdi.plot.gui.Plot;
import anl.verdi.util.Tools;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class SaveConfiguration extends AbstractAction {

	private static final long serialVersionUID = -8610404173487269858L;
	static final Logger Logger = LogManager.getLogger(SaveConfiguration.class.getName());

//	private static final MessageCenter center = MessageCenter.getMessageCenter(SaveConfiguration.class);

	private Plot plot;

	public SaveConfiguration(Plot plot) {
		super("Save Configuration");
		this.plot = plot;
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		boolean saveTitle = saveTile();
		PlotConfiguration config = plot.getPlotConfiguration();
		File file = FileChooserUtilities.getSaveFile(Tools.getConfigFolder(config));

		if (file != null) {
			try {
				config.save(file, saveTitle);
			} catch (IOException ex) {
				Logger.error("Error saving configuration " + ex.getMessage());
			}
		}
	}

	private boolean saveTile() {
		String title = "Save Title?";
		String msg = "Would you like to save the title/subtitles also?";
		int option = JOptionPane.showConfirmDialog(null, msg, title,
				JOptionPane.YES_OPTION);

		if (option == JOptionPane.YES_OPTION)
			return true;

		return false;
	}
	
	public void close() {
		plot = null;
	}

}
