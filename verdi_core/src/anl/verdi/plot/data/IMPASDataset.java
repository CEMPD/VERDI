/**
 * IMPASDataset - Dataset that contains information from an unstructured grid
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.data;

import java.util.Collection;
import java.util.Map;

import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.Dataset;
import anl.verdi.data.MeshCellInfo;

public interface IMPASDataset extends Dataset {
		
	/**
	 * Returns min / max values for the given variable over the entire dataset.  Initially, values are only accurate for the
	 *  first timestep of the first layer, with remaining values calculated as quickly as possible.
	 *  
	 * @param listener - listener to be notified as min/max is updated over the entire dataset
	 * @return - min max values for the given variable
	 */
	public MinMaxInfo getPlotMinMax(DataFrame variable, MinMaxLevelListener listener);
	
	public MinMaxInfo getPlotMinMaxX(DataFrame variable, int timestep, int x);

	public MinMaxInfo getPlotMinMaxY(DataFrame variable, int timestep, int y);

	/**
	 * Returns min / max values for the given variable within the given layer.  Initially, values are only accurate for the
	 * first timestep, with remaining values calculated as quickly as possible.
	 *  
	 * @param listener - listener to be notified as min/max is updated over the entire layer
	 * @return - min max values for the given variable within the layer
	 */
	public MinMaxInfo getLayerMinMax(DataFrame variable, int layer, MinMaxLevelListener listener);	
	
	/**
	 * Returns min / max values for the given variable within the given layer and timetep.  Returns null
	 * if the min/max for the timestep hasn't been calculated yet.
	 *  
	 * @return - min max values for the given variable within the layer and timestep
	 */
	public MinMaxInfo getTimestepMinMax(DataFrame variable, int layer, int timestep);
	
	public double getAvgCellDiam();
	
	public MeshCellInfo getCellInfo(int id);
	
	public double getDataWidth();
	
	public double getDataHeight();
	
	public double getDataRatio();
	
	public double getLonMin();
	
	public double getLonMax();
	
	public double getLatMin();
	
	public double getLatMax();
	
	//Returns each cell to be displayed onscreen.  Cells that cross plot boundaries are split, and each section is returned separately
	public Collection<MeshCellInfo> getAllCells();
	
	public MeshCellInfo[] getAllCellsArray();

	public MeshCellInfo[] getLonSortedCellsArray();
	
	public MeshCellInfo[] getLatSortedCellsArray();

	//Returns all cells as defined by the dataset
	public MeshCellInfo[] getCellsToRender();
	
	//Returns all cells that cross plot boundaries
	public Map<MeshCellInfo, Integer> getSplitCells();
	
	//MPAS files can have multiple layer axes.  Returns the one for the given variable
	public CoordAxis getZAxis(String variable);
	
	/** 
	 * 
	 * DataFrames built by VERDI to not contain all attributes necessary for rendering.  They contain cell axis,
	 * are missing x/y axis, and have other issues.  This replaces the standard DataFrame with an updated version
	 * that can be used in rendering.  Alternate approaches may involve overriding CoordSysBuilder's
	 * augmentDataset method and manipulating the underlying dataset.
	 */
	public DataFrame augmentFrame(DataFrame frame);

}
