package anl.verdi.gis;

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

import anl.verdi.plot.gui.VerdiBoundaries;

// From FastTileAddLayerWizard
/**
 * @author IE, UNC at Chapel Hill
 * @version $Revision: 1.2 $ $Date: 2010/04/27 22:20:57 $
 */
public class GTTileAddLayerWizard {
	static final Logger Logger = LogManager.getLogger(GTTileAddLayerWizard.class.getName());

	private GTTileAddLayerWizardModel model;

	private Wizard wizard;


	public GTTileAddLayerWizard() {
		this(null, null, null, true);
		Logger.debug("in default constructor for GTTileAddLayerWizard");
	}
		
	public GTTileAddLayerWizard(File mapFile, FeatureLayer control, VerdiBoundaries layer, boolean fileStep) {
		Logger.debug("in constructor for GTTileAddLayerWizard, mapFile = " + mapFile + ", FeatureLayer control = "
				+ control + ", VerdiBoundaries layer = " + layer + ", fileStep = " + fileStep);
		SimplePath path = new SimplePath();
		Logger.debug("just instantiated SimplePath: " + path + ", ready to path.addStep");				
		path.addStep(new GTTileEditorStep(control));
		Logger.debug("done with path.addStep, new GTTileEditorstep(control)");
		model = new GTTileAddLayerWizardModel(path);
		Logger.debug("done with GTTileAddLayerWizardModel, ready to setControlLayer");
		model.setControlLayer(control);
		Logger.debug("done with setControlLayer, ready to setMapFile, mapfile = " + mapFile);
//		model.setMapFile(layer != null ? new File(layer.getMapFile()) : mapFile);
		model.setMapFile(layer != null ? layer.getFile() : mapFile);
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
		Logger.debug("done with GTTileAddLayerWizard constructor");
	}

	public VerdiBoundaries display(JFrame frame, boolean isEditing) {
			Logger.debug("in GTTileAddLayerWizard.display");
		if(frame == null)
			Logger.debug("in display, frame == null");
		else
			Logger.debug("in display, frame != null");
		if (isEditing)
		{
			Logger.debug("heading into showInDialog for Edit Layer");
			wizard.showInDialog("Edit Layer", frame, true);
			Logger.debug("done with showInDialog for Edit Layer");
		}
		else 
		{
			Logger.debug("heading into showInDialog for Add Layer");
			wizard.showInDialog("Add Layer", frame, true);
			Logger.debug("done with showInDialog for Add Layer");
		}
		
		if (!wizard.wasCanceled()) {
			Logger.debug("wizard was not canceled, get VerdiBoundaries and title; return layer");
			VerdiBoundaries layer = model.getLayer();
			layer.setPath(model.getMapFile().getAbsolutePath());
			return layer;
		}
		Logger.debug("returning null from display");		
		return null;
	}
	
	public void setControlLayer(FeatureLayer layer) {
		Logger.debug("in GTTileAddLayerWizard.setControlLayer, layer = " + layer);
		model.setControlLayer(layer);
	}
	
	public void setMapFile(File file) {
		Logger.debug("in GTTileAddLayerWizard.setMapFile, file = " + file);
		model.setMapFile(file);
	}

}
