package anl.verdi.gis;

import java.io.File;

import org.geotools.map.FeatureLayer;
//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer
import org.pietschy.wizard.models.MultiPathModel;
import org.pietschy.wizard.models.Path;

/**
 * @author Nick Collier
 * @version $Revision: 1.4 $ $Date: 2007/04/18 19:26:12 $
 */
public class AddLayerWizardModel extends MultiPathModel {

	private File shpFile;
	private FeatureLayer layer;

	public AddLayerWizardModel(Path path) {
		super(path);
	}

	public File getShpFile() {
		return shpFile;
	}

	public void setShpFile(File shpFile) {
		this.shpFile = shpFile;
	}

	public FeatureLayer getLayer() {
		return layer;
	}

	public void setLayer(FeatureLayer layer) {
		this.layer = layer;
	}
}
