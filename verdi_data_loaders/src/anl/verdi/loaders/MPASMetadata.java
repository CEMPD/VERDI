package anl.verdi.loaders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.dataset.NetcdfDataset;
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

public class MPASMetadata implements DatasetMetadata {
	private NetcdfDataset dataset;
		
	private List<String> names = new ArrayList<String>();
	
	private Map<String, GridDatatype> gridMap = new HashMap<String, GridDatatype>();

	public MPASMetadata(NetcdfDataset dataset) {
		this.dataset = dataset;
	}

	/**
	 * Gets the list of projection parameters in this Dataset if it is a Grid type dataset.
	 *
	 * @return the list of projection parameters in this Dataset.
	 */
	public List<String> getProjectionParameters(String projectionName) {
		return new ArrayList<String>();
	}
	
	/**
     * String representation of the projection parameters.
     * 
     * @return String representation of the projection parameters.
     */
	public String paramsToString(String projectionName){
		return null;
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
		return names;
	}
	
	/**
	 * Gets the global attributes from this Dataset.
	 *
	 * @return the global attributes from this Dataset.
	 */
	public List<String> getGlobalAttributes(String projectionName) {
		List<String> attr = new ArrayList<String>();
		List<Attribute> globalAtts = dataset.getGlobalAttributes();
		
		for(Attribute attrObj : globalAtts)
			attr.add(attrObj.toString());
		
		return attr;
	}
	
	public List<String> getDimensionInfo() {
		List<String> dimensionInfo = new ArrayList<String>();
		List<Dimension> dimensions = dataset.getDimensions();
		
		for(Iterator<Dimension> iter = dimensions.iterator(); iter.hasNext();) {
			Dimension dimension = iter.next();
			dimensionInfo.add(dimension.writeCDL(false));
		}
		
		return dimensionInfo;
	}


}
