package anl.verdi.gui;

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;
import anl.verdi.data.MultiAxisDataset;

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
		CoordAxis time = null;
		if (dataset instanceof MultiAxisDataset)
			time = ((MultiAxisDataset)dataset).getDefaultTimeAxis();
		else
			time = axes.getTimeAxis();
		if (time == null) timeMin = timeMax = NO_TIME_VALUE;
		else {
			timeMin = (int) time.getRange().getOrigin();
			timeMax = timeMin + (int)time.getRange().getExtent() - 1;
		}

		CoordAxis layer = null;
		if (dataset instanceof MultiAxisDataset)
			layer = ((MultiAxisDataset)dataset).getDefaultZAxis();
		else
			layer = axes.getZAxis();
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
	
	public CoordAxis getDefaultTimeAxis() {
		if (dataset instanceof MultiAxisDataset) {
			return ((MultiAxisDataset)dataset).getDefaultTimeAxis();
		}
		return getAxes().getTimeAxis();
	}
	
	public CoordAxis getDefaultZAxis() {
		if (dataset instanceof MultiAxisDataset) {
			return ((MultiAxisDataset)dataset).getDefaultZAxis();
		}
		return getAxes().getZAxis();
	}
	
	public CoordAxis getTimeAxisForVariable(String variable) {
		if (dataset instanceof MultiAxisDataset) {
			return ((MultiAxisDataset)dataset).getTimeAxis(variable);
		}
		return getAxes().getTimeAxis();
	}
	
	public CoordAxis getZAxisForVariable(String variable) {
		if (dataset instanceof MultiAxisDataset) {
			return ((MultiAxisDataset)dataset).getZAxis(variable);
		}
		return getAxes().getZAxis();
	}
	
	public void close() {
		dataset = null;
	}

}
