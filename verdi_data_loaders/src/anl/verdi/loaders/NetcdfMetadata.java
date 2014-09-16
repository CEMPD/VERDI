package anl.verdi.loaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.util.Parameter;
import anl.verdi.data.DatasetMetadata;

/**
 * Dataset meta data implementation for those datasets that are read using the netcdf library.
 *
 * @author Qun He
 * @version $Revision$ $Date$
 */

public class NetcdfMetadata implements DatasetMetadata {
	private GridDataset gridDataset;
	
	private List<GridDatatype> grids;
	
	private List<String> names = new ArrayList<String>();
	
	private Map<String, GridDatatype> gridMap = new HashMap<String, GridDatatype>();

	public NetcdfMetadata(GridDataset gridDataset) {
		this.gridDataset = gridDataset;
		this.grids = gridDataset.getGrids();
		initMap(gridMap, grids, names);
	}
	
	private void initMap(Map<String, GridDatatype> gridMap, List<GridDatatype> grids, List<String> names) {
		int i = 0;
		
		for(Iterator<GridDatatype> iter = this.grids.iterator(); iter.hasNext();) {
			GridDatatype grid = iter.next();
			String name = grid.getProjection().getName();
			
			if (name == null || name.trim().isEmpty())
				name = ++i + "";
			
			names.add(name);
			this.gridMap.put(name, grid);
		}
	}

	/**
	 * Gets the list of projection parameters in this Dataset if it is a Grid type dataset.
	 *
	 * @return the list of projection parameters in this Dataset.
	 */
	public List<String> getProjectionParameters(String projectionName) {
		GridDatatype grid = this.gridMap.get(projectionName);
		
		if (grid == null)
			return new ArrayList<String>();
		
		List<String> values = new ArrayList<String>();
		List<Parameter> params = grid.getProjection().getProjectionParameters();
		
		for (Parameter param : params)
			values.add(param.getStringValue());
		
		return values;
	}
	
	/**
     * String representation of the projection parameters.
     * 
     * @return String representation of the projection parameters.
     */
	public String paramsToString(String projectionName){
		GridDatatype grid = this.gridMap.get(projectionName);
		
		return (grid == null) ? null : grid.getProjection().paramsToString();
	}

	/**
	 * Gets the names of projections this Dataset supports.
	 *
	 * @return the names of projections this Dataset supports.
	 */
	public List<String> getProjectionNames() {
		return names;
	}
	
	/**
	 * Gets the class names of projections this Dataset supports.
	 *
	 * @return the class names of projections this Dataset supports.
	 */
	public List<String> getProjectionClassNames() {
		List<String>names = new ArrayList<String>();
		
		for(Iterator<GridDatatype> iter = this.grids.iterator(); iter.hasNext();) 
			names.add(iter.next().getProjection().getClassName());
		
		return names;
	}
	
	/**
	 * Gets the global attributes from this Dataset.
	 *
	 * @return the global attributes from this Dataset.
	 */
	public List<String> getGlobalAttributes(String projectionName) {
		List<String> attr = new ArrayList<String>();
		List<Attribute> globalAtts = gridDataset.getGlobalAttributes();
		
		for(Attribute attrObj : globalAtts)
			attr.add(attrObj.toString());
		
		return attr;
	}
	
	public List<String> getDimensionInfo() {
		List<String> dimensionInfo = new ArrayList<String>();
		List<Dimension> dimensions = gridDataset.getNetcdfDataset().getDimensions();
		
		for(Iterator<Dimension> iter = dimensions.iterator(); iter.hasNext();) {
			Dimension dimension = iter.next();
			dimensionInfo.add(dimension.writeCDL(false));
		}
		
		return dimensionInfo;
	}


}
