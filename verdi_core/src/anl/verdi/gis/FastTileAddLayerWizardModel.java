package anl.verdi.gis;

import gov.epa.emvl.MapLines;

import java.io.File;

import org.geotools.map.FeatureLayer;
//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer
// NOTE: where FeatureLayer is now used in this code, MapLayer had been used
import org.pietschy.wizard.models.MultiPathModel;
import org.pietschy.wizard.models.Path;

/**
 * @author User #2
 * @version $Revision: 1.4 $ $Date: 2009/05/12 19:26:12 $
 */
public class FastTileAddLayerWizardModel extends MultiPathModel {

	private File mapFile;
	private MapLines layer;
	private FeatureLayer controlLayer;

	public FastTileAddLayerWizardModel(Path path) {
		super(path);
	}

	public File getMapFile() {
		return mapFile;
	}

	public void setMapFile(File mapFile) {
		this.mapFile = mapFile;
	}

	public MapLines getLayer() {
		return layer;
	}

	public void setLayer(MapLines layer) {
		this.layer = layer;
	}
	
	public void setControlLayer(FeatureLayer controlLayer) {
		this.controlLayer = controlLayer;
	}
	
	public FeatureLayer getControlLayer() {
		return this.controlLayer;
	}
}
