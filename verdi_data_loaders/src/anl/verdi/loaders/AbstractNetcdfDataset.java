package anl.verdi.loaders;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.unitsofmeasurement.unit.Unit;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
//import simphony.util.messages.MessageCenter;
import ucar.nc2.Attribute;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableEnhanced;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import anl.verdi.data.AbstractDataset;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DefaultVariable;
import anl.verdi.data.Variable;
import anl.verdi.util.VUnits;
/**
 * Abstract Dataset implementation for those datasets that are read using the netcdf library.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class AbstractNetcdfDataset extends AbstractDataset {
	static final Logger Logger = LogManager.getLogger(AbstractNetcdfDataset.class.getName());

//	protected static final MessageCenter msgCenter = MessageCenter.getMessageCenter(AbstractNetcdfDataset.class);
	protected static Map<ucar.nc2.constants.AxisType, AxisType> types = new HashMap<ucar.nc2.constants.AxisType, AxisType>();

	static {
		types.put(ucar.nc2.constants.AxisType.GeoX, AxisType.X_AXIS);
		types.put(ucar.nc2.constants.AxisType.GeoY, AxisType.Y_AXIS);
		types.put(ucar.nc2.constants.AxisType.GeoZ, AxisType.LAYER);
		types.put(ucar.nc2.constants.AxisType.Height, AxisType.LAYER);
		types.put(ucar.nc2.constants.AxisType.Time, AxisType.TIME);
	}

	protected GridDataset gridDataset;
	protected List<Axes<CoordAxis>> axesList = null;
	protected Axes<CoordAxis> coordAxes;
	private List<Variable> vars;


	/**
	 * Creates an AbstractNetcdfDataset for the dataset at the specified url.
	 *
	 * @param url the url of the dataset
	 */
	protected AbstractNetcdfDataset(URL url) {
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
	public Variable getVariable(String name) {	// 2014 changed variable type to anl.verdi.data.Variable
		List<Variable> vars = getVariables();			// for consistence with Dataset.getVariable(String)
		for (Variable var : vars) {
			if (var.getName().equals(name))
				return var;
		}
		return null;
	}

	private Axes<CoordAxis> createAxes(GridDatatype grid) {
		GridCoordSystem system = grid.getCoordinateSystem();
//		List coords = system.getCoordinateAxes();		// changed 2014
		List<CoordinateAxis> coords = system.getCoordinateAxes();
		List<CoordAxis> list = new ArrayList<CoordAxis>();
//		for (CoordinateAxis axis : (List<CoordinateAxis>) coords) {	// changed 2014
		for (CoordinateAxis axis : coords) {
			ucar.nc2.constants.AxisType ncfType = axis.getAxisType();
			if (ncfType != null) {
				AxisType type = types.get(ncfType);
				if (type == null)
					type = AxisType.OTHER;
				list.add(new NetCdfCoordAxis(axis, type));
			}
		}
		return new Axes<CoordAxis>(list, new NetcdfBoxer(grid));
	}

	private void createAxesVars() {
		axesList = new ArrayList<Axes<CoordAxis>>();
		vars = new ArrayList<anl.verdi.data.Variable>();
		Map<GridCoordSystem, Axes<CoordAxis>> map = new HashMap<GridCoordSystem, Axes<CoordAxis>>();
		for (GridDatatype grid : (List<GridDatatype>) gridDataset.getGrids()) {
			GridCoordSystem system = grid.getCoordinateSystem();
			Axes<CoordAxis> axes = map.get(system);
			if (axes == null) {
				axes = createAxes(grid);
				map.put(system, axes);
				axesList.add(axes);
			}

			VariableEnhanced var = grid.getVariable();
			if (!(var instanceof CoordinateAxis)) {
				// ignore the TSTEP variable and any vars (such as
				// projection vars) that have a dimensionality of 0
//				if (!(var.getName().equals("TFLAG") || var.getDimensions().size() == 0)) {
				if (!(var.getShortName().equals("TFLAG") || var.getDimensions().size() == 0)) {	// 2014 changed deprecated getName() to getShortName()
					Unit unit = VUnits.MISSING_UNIT;
					for (Attribute attribute : (Iterable<Attribute>) var.getAttributes()) {
						if (attribute.getShortName().equals("units")) {	// 2014 changed deprecated getName() to getShortName()
							unit = VUnits.createUnit(attribute.getStringValue());
							Logger.debug("in AbstractNetcdfDataset.createAxesVars, unit = " + unit);
						}
					}
					vars.add(new DefaultVariable(var.getShortName(), var.getShortName(), unit, this));	// 2014 changed deprecated getName() to getShortName()
				}
			}
		}
	}


	/**
	 * Gets an iterable over all the Axes in this dataset.
	 *
	 * @return an iterable over all the Axes in this dataset.
	 */
	public Iterable<Axes<CoordAxis>> getAllCoordAxes() {
		if (axesList == null) createAxesVars();
		return axesList;
	}

	/**
	 * Gets the number of coordinate Axes in this dataset.
	 *
	 * @return the number of coordinate Axes in this dataset.
	 */
	public int getCoordAxesCount() {
		if (axesList == null) createAxesVars();
		return axesList.size();
	}

	public Axes<CoordAxis> getCoordAxes() {
		if (coordAxes == null) {
			List<CoordAxis> list = new ArrayList<CoordAxis>();
			GridDatatype grid = (GridDatatype) gridDataset.getGrids().get(0);
			GridCoordSystem system = grid.getCoordinateSystem();
			List<CoordinateAxis> coords = system.getCoordinateAxes();
			for (CoordinateAxis axis : (List<CoordinateAxis>) coords) {
				ucar.nc2.constants.AxisType ncfType = axis.getAxisType();
				if (ncfType != null) {
					AxisType type = types.get(ncfType);
					if (type == null)
						type = AxisType.OTHER;
					list.add(new NetCdfCoordAxis(axis, type));
				}
			}
			coordAxes = new Axes<CoordAxis>(list, new NetcdfBoxer(grid));
		}
		return coordAxes;
	}

	public List<Variable> getVariables() {
		if (vars == null) createAxesVars();
		return vars;
	}

	NetcdfDataset getNetDataset() {
		return gridDataset.getNetcdfDataset();
	}

	/**
	 * Get the netcdf VariableDS corresponding to the specified Variable
	 *
	 * @param var the Variable
	 * @return the corresponding netcdf ucar.nc2.Variable object
	 */
	ucar.nc2.Variable getVariableDS(anl.verdi.data.Variable var) {	
		List<ucar.nc2.Variable> variables = gridDataset.getNetcdfDataset().getVariables();
		for (ucar.nc2.Variable varDS : variables) {
			if (varDS.getShortName().equals(var.getName())) {		// getName was deprecated
				return varDS;
			}
		}
		return null;
	}

	/**
	 * Closes this dataset. It will have to be recreated to be used again.
	 */
	public void close() throws IOException {
		gridDataset.close();
	}
}
