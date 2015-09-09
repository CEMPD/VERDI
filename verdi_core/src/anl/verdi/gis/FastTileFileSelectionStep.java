package anl.verdi.gis;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

//import simphony.util.messages.MessageCenter;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

import anl.verdi.plot.gui.VerdiBoundaries;
//import gov.epa.emvl.MapLines;

/**
 * @author User #2
 * @version $Revision: 1.06 $ $Date: 2009/05/12 17:43:53 $
 */
public class FastTileFileSelectionStep extends PanelWizardStep {
	private static final long serialVersionUID = -343980766506758133L;
	static final Logger Logger = LogManager.getLogger(FastTileFileSelectionStep.class.getName());

//	private static MessageCenter msg = MessageCenter.getMessageCenter(FastTileFileSelectionStep.class);

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
				return pathname.getName().endsWith(".shp")
						|| pathname.isDirectory();
			}

			public String getDescription() {
				return "Shapefiles (.shp)";
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
		VerdiBoundaries layer = new VerdiBoundaries();
		File mapFile = chooser.getSelectedFile();
		File modelMapFile = model.getMapFile();
		if (modelMapFile == null
				|| (modelMapFile != null && !mapFile.equals(modelMapFile))) {
			// create a new default map layer from the shapefile.
			try {
				layer.setFileName(mapFile.getAbsolutePath());
				// MapLines layer = new MapLines(mapFile.getAbsolutePath());
				model.setLayer(layer);
				model.setMapFile(mapFile);
			} catch(Exception ex) {
				Logger.error("Error creating layer from map file " + ex.getMessage());
			}
		}
	}
}
