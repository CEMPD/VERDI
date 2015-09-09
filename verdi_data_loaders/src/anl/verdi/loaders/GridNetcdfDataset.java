package anl.verdi.loaders;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

//import simphony.util.messages.MessageCenter;
import ucar.nc2.Attribute;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableEnhanced;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import anl.verdi.data.AbstractDataset;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;
import anl.verdi.data.DatasetMetadata;
import anl.verdi.data.DefaultVariable;
import anl.verdi.data.Variable;
import anl.verdi.util.VUnits;

/**
 * Dataset implementation for those datasets that are read using the netcdf
 * library and represent a collection of GridDatatypes having the same
 * coordindate system.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class GridNetcdfDataset extends AbstractDataset {
	static final Logger Logger = LogManager.getLogger(GridNetcdfDataset.class.getName());

//	private static final MessageCenter msgCenter = MessageCenter.getMessageCenter(GridNetcdfDataset.class);
	private static Map<ucar.nc2.constants.AxisType, AxisType> types = new HashMap<ucar.nc2.constants.AxisType, AxisType>();

	static {
		types.put(ucar.nc2.constants.AxisType.GeoX, AxisType.X_AXIS);
		types.put(ucar.nc2.constants.AxisType.GeoY, AxisType.Y_AXIS);
		types.put(ucar.nc2.constants.AxisType.Lon, AxisType.X_AXIS);
		types.put(ucar.nc2.constants.AxisType.Lat, AxisType.Y_AXIS);
		types.put(ucar.nc2.constants.AxisType.GeoZ, AxisType.LAYER);
		types.put(ucar.nc2.constants.AxisType.Height, AxisType.LAYER);
		types.put(ucar.nc2.constants.AxisType.Time, AxisType.TIME);
	}

	
	private GridDataset gridDataset;
	private Axes<CoordAxis> coordAxes;
	private int urlIndex = Dataset.SINGLE_DATASET;
	private List<Variable> vars;
	private String name = "";
	private int conv = -1;

	/**
	 * Creates a GridNetcdfDataset from the specified url, grids and grid
	 * dataset
	 * 
	 * @param url
	 *            the url of the dataset
	 * @param grids
	 *            the grids that make up this dataset
	 * @param gridDataset
	 *            the source of the data
	 */
	protected GridNetcdfDataset(URL url, List<GridDatatype> grids,
			GridDataset gridDataset) {
		this(url, grids, gridDataset, Dataset.SINGLE_DATASET);
	}

	/**
	 * Creates a GridNetcdfDataset from the specified url, grids, grid dataset
	 * and suffix. The suffix will be appended to the datasets name.
	 * 
	 * @param url
	 *            the url of the dataset
	 * @param grids
	 *            the grids that make up this dataset
	 * @param gridDataset
	 *            the source of the data
	 * @param urlIndex
	 *            the cardinality of this Dataset inside the specified URL.
	 */
	protected GridNetcdfDataset(URL url, List<GridDatatype> grids,
			GridDataset gridDataset, int urlIndex) {
		super(url);
		this.gridDataset = gridDataset;
		this.urlIndex = urlIndex;
		init(grids);
	}

	/**
	 * Gets the list of variable names in this Dataset.
	 * 
	 * @return the list of variable names in this Dataset.
	 */

	public List<String> getVariableNames() {
		List<String> vars = new ArrayList<String>();
		List<Variable> netVars = getVariables();
		for (Variable var : netVars) {
			vars.add(var.getName());
		}
		return vars;
	}

	/**
	 * Gets the named variable.
	 * 
	 * @param name
	 *            the name of the variable to get
	 * @return the named variable
	 */
	@Override
	public Variable getVariable(String name) {
		List<Variable> vars = getVariables();
		for (Variable var : vars) {
			if (var.getName().equals(name))
				return var;
		}
		return null;
	}

	private void init(List<GridDatatype> grids) {
		Collections.sort(grids, new Comparator<GridDatatype>() {
			public int compare(GridDatatype o1, GridDatatype o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});

		createAxes(grids.get(0));

		vars = new ArrayList<Variable>();
		for (GridDatatype grid : grids) {
			VariableEnhanced var = grid.getVariable();
			if (!(var instanceof CoordinateAxis)) {
				// ignore the TSTEP variable and any vars (such as
				// projection vars) that have a dimensionality of 0
				if (!(var.getShortName().equals("TFLAG") || var.getDimensions()	// 2014 replaced deprecated getName with getShortName
						.size() == 0)) {
					Unit aUnit;
					aUnit = VUnits.MISSING_UNIT;
					Logger.debug("in GridNetcdfDataset.init, aUnit = " + aUnit);
					for (Attribute attribute : (Iterable<Attribute>) var.getAttributes()) 
					{
						if (attribute.getShortName().equals("units")) {	// 2014 replaced deprecated getName with getShortName
							aUnit = VUnits.createUnit(attribute.getStringValue());
							Logger.debug("in GridNetcdfDataset.init, aUnit now = " + aUnit);
						}
						if ( aUnit == null) {
							Logger.debug( attribute.getStringValue());
						}
					}
					Logger.debug("in GridNetcdfDataset.init, aUnit sent to DefaultVariable as " + aUnit);
					vars.add(new DefaultVariable(var.getShortName(), var.getShortName(),	// 2014 replaced deprecated getName with getShortName
							aUnit, (Dataset)this));	// trying it with a cast to Dataset
				}
			}
		}
	}

	private void createAxes(GridDatatype grid) {
		GridCoordSystem system = grid.getCoordinateSystem();
		List coords = system.getCoordinateAxes();
		List<CoordAxis> axes = new ArrayList<CoordAxis>();
		
		for (CoordinateAxis axis : (List<CoordinateAxis>) coords) {
			ucar.nc2.constants.AxisType ncfType = axis.getAxisType();

			if (ncfType != null) {
				AxisType type = types.get(ncfType);

				if (type == null)
					type = AxisType.OTHER;
				if (type == AxisType.TIME) {
					axes.add(new NetcdfTimeAxis((CoordinateAxis1DTime) axis));
				} else {
					axes.add(new NetCdfCoordAxis(axis, type));
				}
			}
		}
		coordAxes = new Axes<CoordAxis>(axes, new NetcdfBoxer(grid));
	}

	/**
	 * Gets the coordinate Axes for this Dataset.
	 * 
	 * @return the coordinate Axes for this Dataset.
	 */
	@Override
	public Axes<CoordAxis> getCoordAxes() {
		return coordAxes;
	}

	@Override
	/**
	 * Gets the variables contained by this dataset.
	 * 
	 * @return the variables contained by this dataset.
	 */
	public List<Variable> getVariables() {
		return vars;
	}

	/**
	 * Sets the alias for this Dataset.
	 * 
	 * @param alias
	 *            the alias for this Dataset.
	 */
	@Override
	public void setAlias(String alias) {
		super.setAlias(alias);
		name = url.toString();
		name = name.substring(name.lastIndexOf("/") + 1, name.length());
		if (urlIndex != Dataset.SINGLE_DATASET)
			name += "_" + urlIndex;
		name = getAlias() + " " + name;
	}

	/**
	 * Gets the index of this Dataset inside its URL, or Dataset.SINGLE_DATASET
	 * if it is the only dataset inside the URL.
	 * 
	 * @return the index of this Dataset inside its URL
	 */
	public int getIndexInURL() {
		return urlIndex;
	}

	/**
	 * Gets the name of this Dataset.
	 * 
	 * @return the name of this Dataset.
	 */
	public String getName() {
		return name;
	}

	NetcdfDataset getNetDataset() {
		return gridDataset.getNetcdfDataset();
	}

	/**
	 * Get the netcdf VariableDS corresponding to the specified Variable
	 * 
	 * @param var
	 *            the Variable
	 * @return the corresponding netsdf varibleDS object
	 */
	ucar.nc2.Variable getVariableDS(Variable var) {
		List<ucar.nc2.Variable> variables = gridDataset.getNetcdfDataset().getVariables();
		for (ucar.nc2.Variable varDS : variables) {
			if (varDS.getShortName().equals(var.getName())) {	// 2014 replaced deprecated getName() with getShortName()
				return varDS;
			}
		}
		return null;
	}

	/**
	 * Closes this dataset. It will have to be recreated to be used again.
	 */
	public void close() throws IOException {
		// BUG: since gridDataset can be shared among several GridNetcdfDataset objects,
		// it needs a reference count to avoid closing the Netcdf file when the user deletes
		// one of the Datasets.
		gridDataset.close();
	}

	/**
	 * Get the netcdf dataset meta data
	 * 
	 * @return the DatasetMetadata object
	 */
	@Override
	public DatasetMetadata getMetadata() {
		return new NetcdfMetadata(gridDataset);
	}
	
	@Override
	public int getNetcdfCovn() {
		return conv;
	}


	@Override
	public void setNetcdfConv(int conv) {
		this.conv = conv;
	}
}
