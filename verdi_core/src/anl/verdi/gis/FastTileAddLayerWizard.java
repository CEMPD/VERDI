package anl.verdi.gis;

import gov.epa.emvl.MapLines;

import java.awt.Dimension;
import java.io.File;

import javax.swing.JFrame;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
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
	static final Logger Logger = LogManager.getLogger(FastTileAddLayerWizard.class.getName());

	private FastTileAddLayerWizardModel model;

	private Wizard wizard;


	public FastTileAddLayerWizard() {
		this(null, null, null, true);
		Logger.debug("in default constructor for FastTileAddLayerWizard");
	}
		
//	public FastTileAddLayerWizard(File mapFile, MapLayer control, MapLines layer, boolean fileStep) {
	public FastTileAddLayerWizard(File mapFile, FeatureLayer control, MapLines layer, boolean fileStep) {
//System.out.println("in constructor for FastTileAddLayerWizard, mapFile = " + mapFile + ", FeatureLayer control = "
//		+ control + ", MapLines layer = " + layer + ", fileStep = " + fileStep);
//		SimplePath path = new SimplePath();
//		
//		if (fileStep)
//			path.addStep(mapFile == null ? new FastTileFileSelectionStep() : new FastTileFileSelectionStep(mapFile));
//		
//		path.addStep(control == null ? new FastTileEditorStep() : new FastTileEditorStep(control));
//		model = new FastTileAddLayerWizardModel(path);
//		model.setControlLayer(control);
//		model.setMapFile(layer != null ? new File(layer.getMapFile()) : mapFile);
//		model.setLayer(layer);
//		wizard = new Wizard(model);
//		wizard.setDefaultExitMode(Wizard.EXIT_ON_FINISH);
//		wizard.setPreferredSize(new Dimension(500, 600));
//		model.setLastVisible(false);
		Logger.debug("in constructor for FastTileAddLayerWizard, mapFile = " + mapFile + ", FeatureLayer control = "
				+ control + ", MapLines layer = " + layer + ", fileStep = " + fileStep);
		SimplePath path = new SimplePath();
		Logger.debug("just instantiated SimplePath: " + path + ", ready to path.addStep");				
		path.addStep(new FastTileEditorStep(control));
		Logger.debug("done with path.addStep, new FastTileEditorstep(control)");
		model = new FastTileAddLayerWizardModel(path);
		Logger.debug("done with FastTileAddLayerWizardModel, ready to setControlLayer");
		model.setControlLayer(control);
		Logger.debug("done with setControlLayer, ready to setMapFile, mapfile = " + mapFile);
		model.setMapFile(layer != null ? new File(layer.getMapFile()) : mapFile);
		Logger.debug("now ready to model.setLayer");
		model.setLayer(layer);
		Logger.debug("done with setting up model, now instantiate new wizard for that model");
		wizard = new Wizard(model);
		Logger.debug("Set defaultExitMode");
		wizard.setDefaultExitMode(Wizard.EXIT_ON_FINISH);
		Logger.debug("set Preferred Size");
		wizard.setPreferredSize(new Dimension(500, 600));
		Logger.debug("set LastVisible(false)");
		model.setLastVisible(false);
		Logger.debug("done with FastTileAddLayerWizard constructor");
	}

	public MapLines display(JFrame frame, boolean isEditing) {
		Logger.debug("in FastTileAddLayerWizard.display");
		if (isEditing)
			wizard.showInDialog("Edit Layer", frame, true);
		else 
			wizard.showInDialog("Add Layer", frame, true);
		
		if (!wizard.wasCanceled()) {
			Logger.debug("wizard was not canceled, get maplines, title, and return layer");
			MapLines layer = model.getLayer();
			layer.setTitle(model.getMapFile().getAbsolutePath());
			return layer;
		}
		Logger.debug("returning null from display");		
		return null;
	}
	
	public void setControlLayer(FeatureLayer layer) {
		Logger.debug("in FastTileAddLayerWizard.setControlLayer, layer = " + layer);
		model.setControlLayer(layer);
	}
	
	public void setMapFile(File file) {
		Logger.debug("in FastTileAddLayerWizard.setMapFile, file = " + file);
		model.setMapFile(file);
	}

}
