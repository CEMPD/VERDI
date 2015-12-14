package anl.verdi.gis;

import java.io.File;

import org.apache.logging.log4j.LogManager;		// 2015
import org.apache.logging.log4j.Logger;			// 2015 replacing System.out.println with logger messages
import org.geotools.map.FeatureLayer;
//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer
// NOTE: where FeatureLayer is now used in this code, MapLayer had been used
import org.pietschy.wizard.models.MultiPathModel;
import org.pietschy.wizard.models.Path;

import anl.verdi.plot.gui.VerdiBoundaries;

/**
 * @author User #2
 * @version $Revision: 1.4 $ $Date: 2009/05/12 19:26:12 $
 */
public class FastTileAddLayerWizardModel extends MultiPathModel {

	static final Logger Logger = LogManager.getLogger(FastTileAddLayerWizardModel.class.getName());
	private File mapFile;
	private VerdiBoundaries layer;
	private FeatureLayer controlLayer;

	public FastTileAddLayerWizardModel(Path path) {
		super(path);
		Logger.debug("done with FastTileAddLayerWizardModel constructor for path = " + path);
	}

	public File getMapFile() {
		Logger.debug("returning mapfile = " + mapFile);
		return mapFile;
	}

	public void setMapFile(File mapFile) {
		Logger.debug("setting mapfile to: " + mapFile);
		this.mapFile = mapFile;
	}

	public VerdiBoundaries getLayer() {
		Logger.debug("returning layer = " + layer);
		return layer;
	}

	public void setLayer(VerdiBoundaries layer) {
		this.layer = layer;
		Logger.debug("set layer = " + layer);
	}
	
	public void setControlLayer(FeatureLayer controlLayer) {
		this.controlLayer = controlLayer;
		Logger.debug("set controlLayer = " + controlLayer);
	}
	
	public FeatureLayer getControlLayer() {
		Logger.debug("returning controlLayer = " + controlLayer);
		return this.controlLayer;
	}
}
