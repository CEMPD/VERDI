package anl.verdi.gui;

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;

/**
 * Element in the dataset list model. Stores info about the dataset and its current ranges.
 * 
 * @author Nick Collier
* @version $Revision$ $Date$
*/
public class DatasetListElement extends AbstractListElement {

	private Dataset dataset;

	public DatasetListElement(Dataset dataset) {
		this.dataset = dataset;

		Axes<CoordAxis> axes = dataset.getCoordAxes();
		CoordAxis time = axes.getTimeAxis();
		if (time == null) timeMin = timeMax = NO_TIME_VALUE;
		else {
			timeMin = (int) time.getRange().getOrigin();
			timeMax = timeMin + (int)time.getRange().getExtent() - 1;
		}

		CoordAxis layer = axes.getZAxis();
		if (layer == null) layerMin = layerMax = NO_LAYER_VALUE;
		else {
			layerMin = (int) layer.getRange().getOrigin();
			layerMax = layerMin + (int)layer.getRange().getExtent() - 1;
		}
	}

	public Dataset getDataset() {
		return dataset;
	}

	public String toString() {
		String dsname = dataset.getName();
		
		if (dataset.isObs() && !dsname.endsWith("(OBS)")) return dsname + " (OBS)";
		
		return dsname;
	}
	
	public Axes<CoordAxis> getAxes(){
		return dataset.getCoordAxes();
	}
}
