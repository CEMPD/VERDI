package anl.verdi.gis;

import gov.epa.emvl.MapLines;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;

//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer
// NOTE: where FeatureLayer is now used in this code, MapLayer had been used
import org.geotools.map.FeatureLayer;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.models.SimplePath;

/**
 * @author IE, UNC at Chapel Hill
 * @version $Revision: 1.2 $ $Date: 2010/04/27 22:20:57 $
 */
public class FastTileAddLayerWizard {

	private FastTileAddLayerWizardModel model;

	private Wizard wizard;


	public FastTileAddLayerWizard() {
		this(null, null, null, true);
	}
		
//	public FastTileAddLayerWizard(File mapFile, MapLayer control, MapLines layer, boolean fileStep) {
	public FastTileAddLayerWizard(File mapFile, FeatureLayer control, MapLines layer, boolean fileStep) {
		SimplePath path = new SimplePath();
		
		if (fileStep)
			path.addStep(mapFile == null ? new FastTileFileSelectionStep() : new FastTileFileSelectionStep(mapFile));
		
		path.addStep(control == null ? new FastTileEditorStep() : new FastTileEditorStep(control));
		model = new FastTileAddLayerWizardModel(path);
		model.setControlLayer(control);
		model.setMapFile(layer != null ? new File(layer.getMapFile()) : mapFile);
		model.setLayer(layer);
		wizard = new Wizard(model);
		wizard.setDefaultExitMode(Wizard.EXIT_ON_FINISH);
		wizard.setPreferredSize(new Dimension(500, 600));
		model.setLastVisible(false);
	}

	public MapLines display(JFrame frame, boolean isEditing) {
		if (isEditing)
			wizard.showInDialog("Edit Layer", frame, true);
		else 
			wizard.showInDialog("Add Layer", frame, true);
		
		if (!wizard.wasCanceled()) {
			MapLines layer = model.getLayer();
			layer.setTitle(model.getMapFile().getAbsolutePath());
			return layer;
		}
		
		return null;
	}
	
	public void setControlLayer(FeatureLayer layer) {
		model.setControlLayer(layer);
	}
	
	public void setMapFile(File file) {
		model.setMapFile(file);
	}
}
