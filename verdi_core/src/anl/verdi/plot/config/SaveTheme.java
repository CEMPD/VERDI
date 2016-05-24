package anl.verdi.plot.config;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;		 
import org.apache.logging.log4j.Logger;			 

import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.util.PlotProperties;
import anl.verdi.util.Tools;
import saf.core.ui.util.FileChooserUtilities;

/**
 * @author qun
 * @version $Revision$ $Date$
 */
public class SaveTheme extends AbstractAction {

	private static final long serialVersionUID = -8610404173487269858L;
	static final Logger Logger = LogManager.getLogger(SaveTheme.class.getName());
	private Plot plot;

	public SaveTheme(Plot plot) {
		super("Save Chart Theme");
		this.plot = plot;
	}

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		ThemeConfig theme = PlotProperties.getInstance().getThemeConfig();
		
		if (theme == null) {
			String msg = "You haven't specified a valid theme.";
			JOptionPane.showMessageDialog(plot.getPanel(), msg);
			return;
		}
			
		File file = FileChooserUtilities.getSaveFile(Tools.getConfigFolder(null));

		if (file != null) {
			try {
				theme.save(file);
			} catch (IOException ex) {
				Logger.error("Error saving chart theme " + ex.getMessage());
			}
		}
	}

}
