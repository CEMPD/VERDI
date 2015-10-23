package anl.verdi.area;



/**
 * Element in the area file list model. Stores info about the area file and its current ranges.
 * 
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 */
public class AreaFileListElement{

	private AreaFile areaFile;

	public AreaFileListElement(AreaFile areaFile) {
		this.areaFile = areaFile;

		//		Axes<CoordAxis> axes = areaFile.getCoordAxes();
		//		CoordAxis time = axes.getTimeAxis();
		//		if (time == null) timeMin = timeMax = NO_TIME_VALUE;
		//		else {
		//			timeMin = time.getRange().getOrigin();
		//			timeMax = timeMin + (int)time.getRange().getExtent() - 1;
		//		}
		//
		//		CoordAxis layer = axes.getZAxis();
		//		if (layer == null) layerMin = layerMax = NO_LAYER_VALUE;
		//		else {
		//			layerMin = layer.getRange().getOrigin();
		//			layerMax = layerMin + (int)layer.getRange().getExtent() - 1;
		//		}
	}

	public AreaFile getAreaFile() {
		return areaFile;
	}

	public String toString() {
		return areaFile.getName();
	}

}
