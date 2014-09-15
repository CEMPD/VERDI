package anl.verdi.gis;

import gov.epa.emvl.MapLines;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

import simphony.util.messages.MessageCenter;

/**
 * @author User #2
 * @version $Revision: 1.06 $ $Date: 2009/05/12 17:43:53 $
 */
public class FastTileFileSelectionStep extends PanelWizardStep {
	private static final long serialVersionUID = -343980766506758133L;

	private static MessageCenter msg = MessageCenter
			.getMessageCenter(FastTileFileSelectionStep.class);

	private JFileChooser chooser;

	private FastTileAddLayerWizardModel model;
	
	public FastTileFileSelectionStep() {
		this(null);
	}

	public FastTileFileSelectionStep(File file) {
		super("Select Map File",
				"Please select the map file to import into the base map");
		setLayout(new BorderLayout());
		chooser = new JFileChooser(".");
		chooser.setFileFilter(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".bin")
						|| pathname.isDirectory();
			}

			public String getDescription() {
				return "Map files (.bin)";
			}
		});

		chooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
		chooser.setControlButtonsAreShown(false);
		chooser.addPropertyChangeListener(
				JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
				new PropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent evt) {
						setComplete(chooser.getSelectedFile() != null);
					}
				});
		
		if (file != null)
			chooser.setSelectedFile(file);
		
		this.add(chooser, BorderLayout.CENTER);
	}

	@Override
	public void init(WizardModel wizardModel) {
		model = (FastTileAddLayerWizardModel) wizardModel;
	}

	@Override
	public void prepare() {
		if (model.getMapFile() != null)
			chooser.setSelectedFile(model.getMapFile());
	}

	@Override
	public void applyState() throws InvalidStateException {
		File mapFile = chooser.getSelectedFile();
		File modelMapFile = model.getMapFile();
		if (modelMapFile == null
				|| (modelMapFile != null && !mapFile.equals(modelMapFile))) {
			// create a new default map layer from the shape file.
			try {
				MapLines layer = new MapLines(mapFile.getAbsolutePath());
				model.setLayer(layer);
				model.setMapFile(mapFile);
			} catch (MalformedURLException e) {
				msg.error("Error creating layer from map file", e);
			} catch (IOException e) {
				msg.error("Error creating layer from map file", e);
			}
		}
	}
}
