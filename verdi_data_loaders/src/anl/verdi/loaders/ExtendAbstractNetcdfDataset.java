package anl.verdi.loaders;

//import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import simphony.util.messages.MessageCenter;
//import ucar.nc2.Attribute;
//import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import anl.verdi.data.AxisType;
//import ucar.nc2.dataset.VariableEnhanced;
//import ucar.nc2.dt.GridCoordSystem;
//import ucar.nc2.dt.GridDatatype;
//import ucar.nc2.dt.grid.GridDataset;
import anl.verdi.data.ExtendAbstractDataset;
import anl.verdi.data.ExtendAxes;
import anl.verdi.data.ExtendCoordAxis;
//import anl.verdi.data.DefaultVariable;
import anl.verdi.data.Variable;
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
//import anl.verdi.util.Units;

public abstract class ExtendAbstractNetcdfDataset extends ExtendAbstractDataset {

	protected static final MessageCenter msgCenter = MessageCenter.getMessageCenter(AbstractNetcdfDataset.class);
	protected static Map<ucar.nc2.constants.AxisType, AxisType> types = new HashMap<ucar.nc2.constants.AxisType, AxisType>();

	// TODO: need to extend this??
	static {
		types.put(ucar.nc2.constants.AxisType.GeoX, AxisType.X_AXIS);
		types.put(ucar.nc2.constants.AxisType.GeoY, AxisType.Y_AXIS);
		types.put(ucar.nc2.constants.AxisType.GeoZ, AxisType.LAYER);
		types.put(ucar.nc2.constants.AxisType.Height, AxisType.LAYER);
		types.put(ucar.nc2.constants.AxisType.Time, AxisType.TIME);
	}

	protected NetcdfDataset netcdfDataset;
	protected List<ExtendAxes<ExtendCoordAxis>> axesList = null;
	protected ExtendAxes<ExtendCoordAxis> coordAxes;
//	private List<Variable> vars;


	/**
	 * Creates an AbstractNetcdfDataset for the dataset at the specified url.
	 *
	 * @param url the url of the dataset
	 */
	protected ExtendAbstractNetcdfDataset(URL url) {
		super(url);
	}


	/**
	 * Gets the index of this Dataset inside its URL, or
	 * Dataset.SINGLE_DATASET if it is the only dataset
	 * inside the URL.
	 *
	 * @return the index of this Dataset inside its URL
	 */
	public int getIndexInURL() {
		return 0;  //todo implement method
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
	 * @param name the name of the variable to get
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

//	private ExtendAxes<ExtendCoordAxis> createAxes(GridDatatype grid) {
//		GridCoordSystem system = grid.getCoordinateSystem();
//		List coords = system.getCoordinateAxes();
//		List<CoordAxis> list = new ArrayList<CoordAxis>();
//		for (CoordinateAxis axis : (List<CoordinateAxis>) coords) {
//			ucar.nc2.constants.AxisType ncfType = axis.getAxisType();
//			if (ncfType != null) {
//				AxisType type = types.get(ncfType);
//				if (type == null)
//					type = AxisType.OTHER;
//				list.add(new NetCdfCoordAxis(axis, type));
//			}
//		}
//		return new Axes<CoordAxis>(list, new NetcdfBoxer(grid));
//	}
//
//	private void createAxesVars() {
//		axesList = new ArrayList<Axes<CoordAxis>>();
//		vars = new ArrayList<Variable>();
//		Map<GridCoordSystem, Axes<CoordAxis>> map = new HashMap<GridCoordSystem, Axes<CoordAxis>>();
//		for (GridDatatype grid : (List<GridDatatype>) gridDataset.getGrids()) {
//			GridCoordSystem system = grid.getCoordinateSystem();
//			Axes<CoordAxis> axes = map.get(system);
//			if (axes == null) {
//				axes = createAxes(grid);
//				map.put(system, axes);
//				axesList.add(axes);
//			}
//
//			VariableEnhanced var = grid.getVariable();
//			if (!(var instanceof CoordinateAxis)) {
//				// ignore the TSTEP variable and any vars (such as
//				// projection vars) that have a dimensionality of 0
//				if (!(var.getName().equals("TFLAG") || var.getDimensions().size() == 0)) {
//					Unit unit = Units.MISSING_UNIT;
//					for (Attribute attribute : (Iterable<Attribute>) var.getAttributes()) {
//						if (attribute.getName().equals("units")) {
//							unit = Units.createUnit(attribute.getStringValue());
//						}
//					}
//					vars.add(new DefaultVariable(var.getName(), var.getName(), unit, this));
//				}
//			}
//		}
//	}
//
//
//	/**
//	 * Gets an iterable over all the Axes in this dataset.
//	 *
//	 * @return an iterable over all the Axes in this dataset.
//	 */
//	public Iterable<Axes<CoordAxis>> getAllCoordAxes() {
//		if (axesList == null) createAxesVars();
//		return axesList;
//	}
//
//	/**
//	 * Gets the number of coordinate Axes in this dataset.
//	 *
//	 * @return the number of coordinate Axes in this dataset.
//	 */
//	public int getCoordAxesCount() {
//		if (axesList == null) createAxesVars();
//		return axesList.size();
//	}
//
//	public Axes<CoordAxis> getCoordAxes() {
//		if (coordAxes == null) {
//			List<CoordAxis> list = new ArrayList<CoordAxis>();
//			GridDatatype grid = (GridDatatype) gridDataset.getGrids().get(0);
//			GridCoordSystem system = grid.getCoordinateSystem();
//			List coords = system.getCoordinateAxes();
//			for (CoordinateAxis axis : (List<CoordinateAxis>) coords) {
//				ucar.nc2.constants.AxisType ncfType = axis.getAxisType();
//				if (ncfType != null) {
//					AxisType type = types.get(ncfType);
//					if (type == null)
//						type = AxisType.OTHER;
//					list.add(new NetCdfCoordAxis(axis, type));
//				}
//			}
//			coordAxes = new Axes<CoordAxis>(list, new NetcdfBoxer(grid));
//		}
//		return coordAxes;
//	}
//
//	public List<Variable> getVariables() {
//		if (vars == null) createAxesVars();
//		return vars;
//	}
//
//	NetcdfDataset getNetDataset() {
//		return gridDataset.getNetcdfDataset();
//	}
//
//	/**
//	 * Get the netcdf VariableDS corresponding to the specified Variable
//	 *
//	 * @param var the Variable
//	 * @return the corresponding netsdf ucar.nc2.Variable object
//	 */
//	ucar.nc2.Variable getVariableDS(Variable var) {
//		List<ucar.nc2.Variable> variables = gridDataset.getNetcdfDataset().getVariables();
//		for (ucar.nc2.Variable varDS : variables) {
//			if (varDS.getName().equals(var.getName())) {
//				return varDS;
//			}
//		}
//		return null;
//	}
//
//	/**
//	 * Closes this dataset. It will have to be recreated to be used again.
//	 */
//	public void close() throws IOException {
//		gridDataset.close();
//	}
}

