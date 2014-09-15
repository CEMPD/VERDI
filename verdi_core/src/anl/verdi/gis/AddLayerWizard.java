package anl.verdi.gis;

import javax.swing.JFrame;

//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer
import org.geotools.map.FeatureLayer;
import org.pietschy.wizard.Wizard;
import org.pietschy.wizard.models.SimplePath;

/**
 * @author Nick Collier
 * @version $Revision: 1.6 $ $Date: 2007/05/16 22:20:57 $
 */
public class AddLayerWizard {

	private AddLayerWizardModel model;

	private Wizard wizard;


	public AddLayerWizard() {
		init();
	}

	private void init() {
		SimplePath path = new SimplePath();
		path.addStep(new FileSelectionStep());
		path.addStep(new EditorStep());
		model = new AddLayerWizardModel(path);
		wizard = new Wizard(model);
		wizard.setDefaultExitMode(Wizard.EXIT_ON_FINISH);
		model.setLastVisible(false);
	}

//	public MapLayer display(JFrame frame) {
	public FeatureLayer display(JFrame frame) {
		wizard.showInDialog("Add Layer", frame, true);
		if (!wizard.wasCanceled()) {
//			MapLayer layer = model.getLayer();
			FeatureLayer layer = model.getLayer();
			layer.setTitle(model.getShpFile().getAbsolutePath());
			return layer;
		}
		return null;
	}
}
