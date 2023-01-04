package anl.verdi.loaders;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SimpleTimeZone;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
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
 * Dataset for Models3 format observation data files.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Models3ObsDataset extends AbstractDataset {
	static final Logger Logger = LogManager.getLogger(Models3ObsDataset.class.getName());

	private NetcdfDataset dataset;
	private List<Variable> variables = new ArrayList<Variable>();

	private static Set<String> hiddenVars = new HashSet<String>();
	static private java.text.SimpleDateFormat dateFormatOut;
	private Axes<CoordAxis> axes;
	private String name = "";
	private int conv = -1;

	static {
		dateFormatOut = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		dateFormatOut.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));

		hiddenVars.add("TFLAG");
		hiddenVars.add("STNID");
		hiddenVars.add("LAT");
		hiddenVars.add("LON");
	}

	public Models3ObsDataset(URL url, NetcdfDataset dataset) {
		super(url);
		Logger.debug("in constructor for Models3ObsDataset");
		this.dataset = dataset;
		initVariables();
		initAxes();
	}

	private void initVariables() {
		List<ucar.nc2.Variable> vars = dataset.getVariables();
		for (ucar.nc2.Variable var : vars) {
			String name = var.getShortName();	// getName replaced by either getShortName or getFullName (with escapes)
			Unit unit = VUnits.MISSING_UNIT;
			//System.err.println("Models3ObsDataset loading " + name);
			Logger.debug("in Models3ObsDataset.initVariables, unit = " + unit);
			for (Attribute attribute : (Iterable<Attribute>) var.getAttributes()) {
				if (attribute.getShortName().equals("units")) {
					unit = VUnits.createUnit(attribute.getStringValue());
					Logger.debug("in Models3ObsDatabase.initVariables, unit now = " + unit);
				}
			}
			if (!hiddenVars.contains(name)) 
				{
					variables.add(new DefaultVariable(name, name, unit, this));
					Logger.debug("in Models3ObsDataset.initVariables, after check for hiddenVars, unit = " + unit);
				}
		}
	}

	private void initAxes() {
		makeZCoordAxis("LAY", "VGLVLS", "sigma");
		makeTimeCoordAxis("TSTEP");

		List<CoordAxis> list = new ArrayList<CoordAxis>();
		for (CoordinateAxis axis : (List<CoordinateAxis>) dataset.getCoordinateAxes()) {
			AxisType type = null;
			String attributeStr = findAxisAttributeStr(axis, "_CoordinateAxisType");
			if (attributeStr.equals("GeoZ")) {
				type = AxisType.LAYER;
				list.add(new NetCdfCoordAxis(axis, type));
			} else if (attributeStr.equals("Time")) {
				type = AxisType.TIME;
				list.add(new SyntheticTimeAxis(axis));
			}
		}

		axes = new Axes<CoordAxis>(list);
	}

	private String findAxisAttributeStr(CoordinateAxis axis, String attrib) {
		Attribute att = axis.findAttributeIgnoreCase(attrib);
		if (att != null) {
			return att.getStringValue();
		}
		return "";
	}

	private void makeZCoordAxis(String dimName, String layersName, String unitName) {
		Dimension dimz = dataset.findDimension(dimName);
		int nz = dimz.getLength();
		ArrayDouble.D1 dataLev = new ArrayDouble.D1(nz);
		ArrayDouble.D1 dataLayers = new ArrayDouble.D1(nz + 1);

		// layer values are a numeric global attribute array !!
		Attribute layers = dataset.findGlobalAttribute("VGLVLS");
		for (int i = 0; i <= nz; i++)
			dataLayers.set(i, layers.getNumericValue(i).doubleValue());

		for (int i = 0; i < nz; i++) {
			double midpoint = (dataLayers.get(i) + dataLayers.get(i + 1)) / 2;
			dataLev.set(i, midpoint);
		}

		CoordinateAxis v = new CoordinateAxis1D(dataset, null, "layer", DataType.DOUBLE, dimName, unitName,
						"synthesized coordinate from " + layersName + " global attributes");
		v.setCachedData(dataLev, true);
		v.addAttribute(new Attribute("positive", "down"));
		v.addAttribute(new Attribute(ucar.nc2.constants._Coordinate.AxisType, ucar.nc2.constants.AxisType.GeoZ.toString()));

		// layer edges
		String edge_name = "layer";
		Dimension lay_edge = new Dimension(edge_name, nz + 1, true);
		dataset.addDimension(null, lay_edge);
		CoordinateAxis vedge = new CoordinateAxis1D(dataset, null, edge_name, DataType.DOUBLE, edge_name, unitName,
						"synthesized coordinate from " + layersName + " global attributes");
		vedge.setCachedData(dataLayers, true);
		v.setBoundaryRef(edge_name);

		dataset.addCoordinateAxis(v);
		dataset.addCoordinateAxis(vedge);
	}

	private int findAttributeInt(String attname) {
		Attribute att = dataset.findGlobalAttributeIgnoreCase(attname);
		return att.getNumericValue().intValue();
	}

	private void makeTimeCoordAxis(String timeName) {
		int start_date = findAttributeInt("SDATE");
		int start_time = findAttributeInt("STIME");
		int time_step = (int) findAttributeInt("TSTEP");

		int year = start_date / 1000;
		int doy = start_date % 1000;
		int hour = start_time / 10000;
		start_time = start_time % 10000;
		int min = start_time / 100;
		int sec = start_time % 100;

		Calendar cal = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
		cal.clear();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.DAY_OF_YEAR, doy);
		cal.set(Calendar.HOUR, hour);
		cal.set(Calendar.MINUTE, min);
		cal.set(Calendar.SECOND, sec);
		//cal.setTimeZone( new SimpleTimeZone(0, "GMT"));

		String units = "seconds since " + dateFormatOut.format(cal.getTime()) + " UTC";

		// parse the time step
		hour = time_step / 10000;
		time_step = time_step % 10000;
		min = time_step / 100;
		sec = time_step % 100;
		time_step = hour * 3600 + min * 60 + sec;

		Dimension dimt = dataset.findDimension(timeName);
		int nt = dimt.getLength();
		ArrayInt.D1 data = new ArrayInt.D1(nt);
		for (int i = 0; i < nt; i++) {
			data.set(i, i * time_step);
		}

		// create the coord axis
		CoordinateAxis1D timeCoord = new CoordinateAxis1D(dataset, null, "time", DataType.INT, timeName, units,
						"synthesized time coordinate from SDATE, STIME, STEP global attributes");
		timeCoord.setCachedData(data, true);
		timeCoord.addAttribute(new Attribute(ucar.nc2.constants._Coordinate.AxisType, ucar.nc2.constants.AxisType.Time.toString()));

		dataset.addCoordinateAxis(timeCoord);
	}


	/**
	 * Closes this dataset. It will have to be recreated to be used again.
	 *
	 * @throws java.io.IOException if an error occurs while closing the dataset
	 */
	public void close() throws IOException {
		this.dataset.close();
	}


	/**
	 * Gets the coordindate Axes for this Dataset.
	 *
	 * @return the coordindate Axes for this Dataset.
	 */
	public Axes<CoordAxis> getCoordAxes() {
		return axes;
	}

	/**
	 * Gets the index of this Dataset inside its URL, or
	 * Dataset.SINGLE_DATASET if it is the only dataset
	 * inside the URL.
	 *
	 * @return the index of this Dataset inside its URL
	 */
	public int getIndexInURL() {
		return Dataset.SINGLE_DATASET;
	}

	/**
	 * Gets the named variable.
	 *
	 * @param name the name of the variable to get
	 * @return the named variable
	 */
	public Variable getVariable(String name) {
		for (Variable var : getVariables()) {
			if (var.getName().equals(name)) return var;
		}

		return null;
	}


	/**
	 * Gets the name of this Dataset.
	 *
	 * @return the name of this Dataset;
	 */
	@Override
	public String getName() {
		return name;
	}

	NetcdfDataset getNetDataset() {
		return dataset;
	}

	/**
	 * Get the netcdf VariableDS corresponding to the specified Variable
	 *
	 * @param var the Variable
	 * @return the corresponding netsdf varibleDS object
	 */
	ucar.nc2.Variable getVariableDS(Variable var) {
		List<ucar.nc2.Variable> variables = dataset.getVariables();
		for (ucar.nc2.Variable varDS : variables) {
			if (varDS.getShortName().equals(var.getName())) {	// getName() deprecated
				return varDS;
			}
		}
		return null;
	}

	/**
	 * Sets the alias for this Dataset.
	 *
	 * @param alias the alias for this Dataset.
	 */
	@Override
	public void setAlias(String alias) {
		super.setAlias(alias);
		name = url.toString();
		name = name.substring(name.lastIndexOf("/") + 1, name.length());
		name = getAlias() + " " + name + " (OBS)";
	}


	/**
	 * Gets the list of variable names in this Dataset.
	 *
	 * @return the list of variable names in this Dataset.
	 */
	public List<String> getVariableNames() {
		List<String> names = new ArrayList<String>();
		for (Variable var : getVariables()) {
			names.add(var.getName() + (" (OBS)"));
		}

		return names;
	}

	/**
	 * Gets the list of variables in this Dataset.
	 *
	 * @return the list of variables in this Dataset.
	 */
	public List<Variable> getVariables() {
		return variables;
	}

	/**
	 * @return true if there is a time axis, otherwise false
	 */
	public boolean hasTimeAxis() {
		return axes.getTimeAxis() != null;
	}

	/**
	 * @return true if there is a X axis, otherwise false
	 */
	public boolean hasXAxis() {
		return axes.getXAxis() != null;
	}

	/**
	 * @return true if there is a Y axis, otherwise false
	 */
	public boolean hasYAxis() {
		return axes.getYAxis() != null;
	}

	/**
	 * See if there is a z (e.g. layer) axis
	 *
	 * @return true if there is a Z axis, otherwise false
	 */
	public boolean hasZAxis() {
		return axes.getZAxis() != null;
	}


	/**
	 * Whether or not this dataset represents an observational dataset
	 *
	 * @return true if this contains obs data, otherwise false.
	 */
	@Override
	public boolean isObs() {
		return true;
	}

	/**
	 * Get the dataset meta data
	 *
	 * @return the DatasetMetadata object
	 */	
	@Override
	public DatasetMetadata getMetadata() {
		// TODO depend on what we need during the development
		return null;
	}
	
	@Override
	public int getNetcdfCovn() {
		return conv ;
	}


	@Override
	public void setNetcdfConv(int conv) {
		this.conv = conv;
	}
}
