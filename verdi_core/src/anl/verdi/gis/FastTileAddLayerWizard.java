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
System.out.println("in default constructor for FastTileAddLayerWizard");
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
		System.out.println("in constructor for FastTileAddLayerWizard, mapFile = " + mapFile + ", FeatureLayer control = "
				+ control + ", MapLines layer = " + layer + ", fileStep = " + fileStep);
				SimplePath path = new SimplePath();
System.out.println("just instantiated SimplePath: " + path + ", ready to path.addStep");				
				path.addStep(new FastTileEditorStep(control));
System.out.println("done with path.addStep, new FastTileEditorstep(control)");
				model = new FastTileAddLayerWizardModel(path);
System.out.println("done with FastTileAddLayerWizardModel, ready to setControlLayer");
				model.setControlLayer(control);
System.out.println("done with setControlLayer, ready to setMapFile, mapfile = " + mapFile);
				model.setMapFile(layer != null ? new File(layer.getMapFile()) : mapFile);
System.out.println("now ready to model.setLayer");
				model.setLayer(layer);
System.out.println("done with setting up model, now instantiate new wizard for that model");
				wizard = new Wizard(model);
System.out.println("Set defaultExitMode");
				wizard.setDefaultExitMode(Wizard.EXIT_ON_FINISH);
System.out.println("set Preferred Size");
				wizard.setPreferredSize(new Dimension(500, 600));
System.out.println("set LastVisible(false)");
				model.setLastVisible(false);
System.out.println("done with FastTileAddLayerWizard constructor");
	}

	public MapLines display(JFrame frame, boolean isEditing) {
System.out.println("in FastTileAddLayerWizard.display");
		if (isEditing)
			wizard.showInDialog("Edit Layer", frame, true);
		else 
			wizard.showInDialog("Add Layer", frame, true);
		
		if (!wizard.wasCanceled()) {
System.out.println("wizard was not canceled, get maplines, title, and return layer");
			MapLines layer = model.getLayer();
			layer.setTitle(model.getMapFile().getAbsolutePath());
			return layer;
		}
System.out.println("returning null from display");		
		return null;
	}
	
	public void setControlLayer(FeatureLayer layer) {
System.out.println("in FastTileAddLayerWizard.setControlLayer, layer = " + layer);
		model.setControlLayer(layer);
	}
	
	public void setMapFile(File file) {
System.out.println("in FastTileAddLayerWizard.setMapFile, file = " + file);
		model.setMapFile(file);
	}

}
