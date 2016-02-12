package anl.verdi.loaders;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;


import java.awt.*;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import anl.verdi.data.AbstractDataset;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.Dataset;
import anl.verdi.data.DatasetMetadata;
import anl.verdi.data.DefaultVariable;
import anl.verdi.data.Variable;
import anl.verdi.plot.color.Palette;
import anl.verdi.plot.color.PavePaletteCreator;
import anl.verdi.util.VUnits;

/**
 * Dataset implementation for those datasets that are read using the netcdf
 * library and represent a collection of GridDatatypes having the same
 * coordindate system.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class MPASDataset extends AbstractDataset {
	static final Logger Logger = LogManager.getLogger(MPASDataset.class.getName());
	
	public static final String VAR_AVG_CELL_DIAM = "verdi.avgCellDiam";

	private NetcdfDataset dataset;
	private Axes<CoordAxis> coordAxes;
	private List<Variable> vars;
	private List<Variable> verdiRenderVars;
	private Map<String, ucar.nc2.Variable> renderVars;
	private String name = "";
	private int conv = -1;
	private Map<Integer, Integer> vertexPositionMap = new HashMap<Integer, Integer>();
	int numCells;
	int maxEdges = 0;

	//double[] legendLevels = null;
	
	private static Set<String> hiddenVars = new HashSet<String>();
	
	private static Set<String> renderVarList = new HashSet<String>();
	
	private class CellInfo { 
		double[] latCoords;
		double[] lonCoords;
		int cellId;
		
		private CellInfo(int id, int numVertices) {
			latCoords = new double[numVertices];
			lonCoords = new double[numVertices];
			cellId = id;
		}
		
		public CellInfo clone() {
			CellInfo clone = new CellInfo(cellId, lonCoords.length);
			clone.latCoords = Arrays.copyOf(latCoords, latCoords.length);
			clone.lonCoords = Arrays.copyOf(lonCoords, lonCoords.length);
			return clone;
		}
	}
	
	private CellInfo[] cellsToRender = null;
	private ArrayList<CellInfo> splitCells = null;
	
	static {
		renderVarList.add("verticesOnCell");
		renderVarList.add("nEdgesOnCell");
		renderVarList.add("latVertex");
		renderVarList.add("lonVertex");
		renderVarList.add("xVertex");
		renderVarList.add("yVertex");
		renderVarList.add("zVertex");
		renderVarList.add("indexToVertexID");
		renderVarList.add("indexToCellID");
		renderVarList.add("latCell");
		renderVarList.add("lonCell");
		renderVarList.add("zgrid");
		renderVarList.add("zs");
	}
	
	static {
		hiddenVars.add("xCell");
		hiddenVars.add("yCell");
		hiddenVars.add("zCell");
		hiddenVars.add("indexToCellID");
		hiddenVars.add("edgesOnCell");
		hiddenVars.add("areaCell");
		hiddenVars.add("cellsOnCell");
		hiddenVars.add("verticesOnCell");
		hiddenVars.add("meshDensity");
		hiddenVars.add("zz");
	}
	
	ucar.ma2.Array cellVertices = null;
	ucar.ma2.Array latCell = null;
	ucar.ma2.Array lonCell = null;
	ucar.ma2.Array latVert = null;
	ucar.ma2.Array lonVert = null;
	ucar.ma2.Array indexToVertexId = null;
	ucar.ma2.ArrayInt.D2 vertexList = null;
	
	
	//TODO = get these from ranges instead of storing here
	double minX = Double.MAX_VALUE;
	double minY = Double.MAX_VALUE;
	double maxX = Double.MAX_VALUE * -1;
	double maxY = Double.MAX_VALUE * -1;
	double latMin = Double.MAX_VALUE;
	double lonMin = Double.MAX_VALUE;
	double latMax = Double.MAX_VALUE * -1;
	double lonMax = Double.MAX_VALUE * -1;

	double dataWidth = 0;
	double dataHeight = 0;
	
	double avgCellDiam = 0;
	
	public double getAvgCellDiam() {
		return avgCellDiam;
	}

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
	protected MPASDataset(URL url, 
			NetcdfDataset netcdfDataset) {
		super(url);
		long start = System.currentTimeMillis();
		this.dataset = netcdfDataset;
		initVariables();
		try {
			initAxes();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Dataset loaded in " + (System.currentTimeMillis() - start) + "ms");
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
		for (Variable var : verdiRenderVars) {
			if (var.getName().equals(name))
				return var;
		}
		return null;
	}
	
	private ucar.nc2.Variable getRenderVariable(String name) {
		return renderVars.get(name);
	}
	
	private static HashMap<String, String> KNOWN_UNITS = new HashMap<String, String>();
	
	static {
		KNOWN_UNITS.put("qv", "-");
		KNOWN_UNITS.put("qc", "-");
		KNOWN_UNITS.put("qr", "-");
		KNOWN_UNITS.put("qi", "-");
		KNOWN_UNITS.put("qs", "-");
		KNOWN_UNITS.put("qg", "-");
		KNOWN_UNITS.put("u", "m/s");
		KNOWN_UNITS.put("w", "m/s");
		KNOWN_UNITS.put("v", "m/s");
		KNOWN_UNITS.put("ter", "m");
		KNOWN_UNITS.put("dsz", "m");
		KNOWN_UNITS.put("sz", "m");
		KNOWN_UNITS.put("sh2o", "-");
		KNOWN_UNITS.put("surface_pressure", "Pa");
		KNOWN_UNITS.put("pressure_base", "Pa");
		KNOWN_UNITS.put("pressure", "Pa");
		KNOWN_UNITS.put("rho_base", "kg/m\u00b3");
		KNOWN_UNITS.put("rho", "kg/m\u00b3");
		KNOWN_UNITS.put("ndays_accum", "-");
		KNOWN_UNITS.put("tlag", "K");
		KNOWN_UNITS.put("tday_accum", "K");
		KNOWN_UNITS.put("tyear_mean", "K");
		KNOWN_UNITS.put("tyear_accum", "K");
		KNOWN_UNITS.put("theta_base", "K");
		KNOWN_UNITS.put("theta", "K");
		KNOWN_UNITS.put("relhum", "-");
		KNOWN_UNITS.put("cldfrac", "-");
		KNOWN_UNITS.put("coszr", "-");
		KNOWN_UNITS.put("gsw", "W/m\u00b2");
		KNOWN_UNITS.put("swdnb", "W/m\u00b2");
		KNOWN_UNITS.put("swdnbc", "W/m\u00b2");
		KNOWN_UNITS.put("swupb", "W/m\u00b2");
		KNOWN_UNITS.put("swupbc", "W/m\u00b2");
		KNOWN_UNITS.put("swdnt", "W/m\u00b2");
		KNOWN_UNITS.put("swdntc", "W/m\u00b2");
		KNOWN_UNITS.put("swupt", "W/m\u00b2");
		KNOWN_UNITS.put("swuptc", "W/m\u00b2");
		KNOWN_UNITS.put("swcf", "W/m\u00b2");
		KNOWN_UNITS.put("acswdnb", "W/m\u00b2");
		KNOWN_UNITS.put("acswdnbc", "W/m\u00b2");
		KNOWN_UNITS.put("acswdnt", "W/m\u00b2");
		KNOWN_UNITS.put("acswdntc", "W/m\u00b2");
		KNOWN_UNITS.put("acswupb", "W/m\u00b2");
		KNOWN_UNITS.put("acswupbc", "W/m\u00b2");
		KNOWN_UNITS.put("acswupt", "W/m\u00b2");
		KNOWN_UNITS.put("acswuptc", "W/m\u00b2");
		KNOWN_UNITS.put("rthratensw", "K/s");
		KNOWN_UNITS.put("lwdnb", "W/m\u00b2");
		KNOWN_UNITS.put("lwdnbc", "W/m\u00b2");
		KNOWN_UNITS.put("lwupb", "W/m\u00b2");
		KNOWN_UNITS.put("lwupbc", "W/m\u00b2");
		KNOWN_UNITS.put("lwdnt", "W/m\u00b2");
		KNOWN_UNITS.put("lwdntc", "W/m\u00b2");
		KNOWN_UNITS.put("lwupt", "W/m\u00b2");
		KNOWN_UNITS.put("lwuptc", "W/m\u00b2");
		KNOWN_UNITS.put("lwc", "W/m\u00b2");
		KNOWN_UNITS.put("olrtoa", "W/m\u00b2");
		KNOWN_UNITS.put("aclwdnb", "W/m\u00b2");
		KNOWN_UNITS.put("aclwdnbc", "W/m\u00b2");
		KNOWN_UNITS.put("aclwdnt", "W/m\u00b2");
		KNOWN_UNITS.put("aclwdntc", "W/m\u00b2");
		KNOWN_UNITS.put("aclwupb", "W/m\u00b2");
		KNOWN_UNITS.put("aclwupbc", "W/m\u00b2");
		KNOWN_UNITS.put("aclwupt", "W/m\u00b2");
		KNOWN_UNITS.put("aclwuptc", "W/m\u00b2");
		KNOWN_UNITS.put("rthratenlw", "K/s");
		KNOWN_UNITS.put("plrad", "Pa");
		KNOWN_UNITS.put("absnxt", "-");
		KNOWN_UNITS.put("abstot", "-");
		KNOWN_UNITS.put("emstot", "-");
		KNOWN_UNITS.put("pin", "hPa");
		KNOWN_UNITS.put("ozmixm", "-");
		KNOWN_UNITS.put("m_hybi", "-");
		KNOWN_UNITS.put("m_ps", "-");
		KNOWN_UNITS.put("sul", "kg/m\u00b3");
		KNOWN_UNITS.put("sslt", "kg/m\u00b3");
		KNOWN_UNITS.put("dust1", "kg/m\u00b3");
		KNOWN_UNITS.put("dust2", "kg/m\u00b3");
		KNOWN_UNITS.put("dust3", "kg/m\u00b3");
		KNOWN_UNITS.put("dust4", "kg/m\u00b3");
		KNOWN_UNITS.put("ocpho", "kg/m\u00b3");
		KNOWN_UNITS.put("bcpho", "kg/m\u00b3");
		KNOWN_UNITS.put("ocphi", "kg/m\u00b3");
		KNOWN_UNITS.put("bcphi", "kg/m\u00b3");
		KNOWN_UNITS.put("bg", "kg/m\u00b3");
		KNOWN_UNITS.put("volc", "kg/m\u00b3");
		KNOWN_UNITS.put("br", "-");
		KNOWN_UNITS.put("cd", "-");
		KNOWN_UNITS.put("cda", "-");
		KNOWN_UNITS.put("ck", "-");
		KNOWN_UNITS.put("cka", "-");
		KNOWN_UNITS.put("cpm", "-");
		KNOWN_UNITS.put("hfx", "W/m\u00b2");
		KNOWN_UNITS.put("lh", "W/m\u00b2");
		KNOWN_UNITS.put("mavail", "-");
		KNOWN_UNITS.put("mol", "K");
		KNOWN_UNITS.put("psim", "-");
		KNOWN_UNITS.put("psih", "-");
		KNOWN_UNITS.put("q2", "-");
		KNOWN_UNITS.put("qfx", "-");
		KNOWN_UNITS.put("qsfc", "-");
		KNOWN_UNITS.put("u10", "m/s");
		KNOWN_UNITS.put("ust", "m/s");
		KNOWN_UNITS.put("ustm", "m/s");
		KNOWN_UNITS.put("t2m", "K");
		KNOWN_UNITS.put("th2m", "K");
		KNOWN_UNITS.put("v10", "m/s");
		KNOWN_UNITS.put("wspd", "m/s");
		KNOWN_UNITS.put("zol", "-");
		KNOWN_UNITS.put("znt", "m");
		KNOWN_UNITS.put("dtaux3d", "m/s");
		KNOWN_UNITS.put("dtauy3d", "m/s");
		KNOWN_UNITS.put("acsnom", "kg/m\u00b2");
		KNOWN_UNITS.put("canwat", "kg/m\u00b2");
		KNOWN_UNITS.put("grdflx", "W/m\u00b2");
		KNOWN_UNITS.put("lai", "-");
		KNOWN_UNITS.put("noahres", "W/m\u00b2");
		KNOWN_UNITS.put("potevp", "W/m\u00b2");
		KNOWN_UNITS.put("qz0", "-");
		KNOWN_UNITS.put("sfcrunoff", "mm");
		KNOWN_UNITS.put("smstav", "-");
		KNOWN_UNITS.put("smstot", "-");
		KNOWN_UNITS.put("snopcx", "W/m\u00b2");
		KNOWN_UNITS.put("sstsk", "K");
		KNOWN_UNITS.put("sstsk_diurn", "K");
		KNOWN_UNITS.put("udrunoff", "mm");
		KNOWN_UNITS.put("z0", "m");
		KNOWN_UNITS.put("zs", "m");
		KNOWN_UNITS.put("divergence", "-");
		KNOWN_UNITS.put("ke", "J");
		KNOWN_UNITS.put("uReconstructZonal", "m/s");
		KNOWN_UNITS.put("uReconstructMeridional", "m/s");
		KNOWN_UNITS.put("i_rainnc", "-");
		KNOWN_UNITS.put("refl10cm_max", "dBz");
		KNOWN_UNITS.put("rthcuten", "K/s");
		KNOWN_UNITS.put("nca", "s");
		KNOWN_UNITS.put("zgrid", "m");
		KNOWN_UNITS.put("cubot", "-");
		KNOWN_UNITS.put("cutop", "-");
		KNOWN_UNITS.put("wavg0", "m/s");
		KNOWN_UNITS.put("rain", "mm");
		KNOWN_UNITS.put("raincv", "mm");
		KNOWN_UNITS.put("rainnc", "mm");
		KNOWN_UNITS.put("rainncv", "mm");
		KNOWN_UNITS.put("snownc", "mm");
		KNOWN_UNITS.put("snowncv", "mm");
		KNOWN_UNITS.put("graupelncv", "mm");
		KNOWN_UNITS.put("graupelnc", "mm");
		KNOWN_UNITS.put("sr", "-");
		KNOWN_UNITS.put("kpbl", "layer");
		KNOWN_UNITS.put("precipw", "kg/m\u00b2");
		KNOWN_UNITS.put("cuprec", "mm/s");
		KNOWN_UNITS.put("i_rainc", "-");
		KNOWN_UNITS.put("rainc", "mm");
		KNOWN_UNITS.put("hpbl", "m");
		KNOWN_UNITS.put("hfx", "W/m\u00b2");
		KNOWN_UNITS.put("qfx", "kg/m\u00b2/s");
		KNOWN_UNITS.put("cd", "-");
		KNOWN_UNITS.put("cda", "-");
		KNOWN_UNITS.put("ck", "-");
		KNOWN_UNITS.put("cka", "-");
		KNOWN_UNITS.put("lh", "W/m\u00b2");
		KNOWN_UNITS.put("u10", "m/s");
		KNOWN_UNITS.put("v10", "m/s");
		KNOWN_UNITS.put("q2", "-");
		KNOWN_UNITS.put("t2m", "K");
		KNOWN_UNITS.put("th2m", "K");
		KNOWN_UNITS.put("gsw", "W/m\u00b2");
		KNOWN_UNITS.put("glw", "W/m\u00b2");
		KNOWN_UNITS.put("acsnow", "kg/m\u00b2");
		KNOWN_UNITS.put("skintemp", "K");
		KNOWN_UNITS.put("snow", "kg/m\u00b2");
		KNOWN_UNITS.put("snowh", "m");
		KNOWN_UNITS.put("sst", "K");
		KNOWN_UNITS.put("sh2o", "-");
		KNOWN_UNITS.put("smois", "-");
		KNOWN_UNITS.put("tslb", "K");		
		KNOWN_UNITS.put("tmn", "K");	
		KNOWN_UNITS.put("snow", "kg/m\u00b2");
		KNOWN_UNITS.put("snowc", "-");
		KNOWN_UNITS.put("dzs", "m");
		KNOWN_UNITS.put("smcrel", "-");
		KNOWN_UNITS.put("vegfra", "-");		
		KNOWN_UNITS.put("xland", "-");		
		KNOWN_UNITS.put("seaice", "-");		
		KNOWN_UNITS.put("xice", "-");		
	}

	private void initVariables() {
		List<ucar.nc2.Variable> vars = dataset.getVariables();
		this.vars = new ArrayList<Variable>();
		this.verdiRenderVars = new ArrayList<Variable>();
		this.renderVars = new HashMap<String, ucar.nc2.Variable>();
		for (ucar.nc2.Variable var : vars) {
			String name = var.getShortName();	// getName replaced by either getShortName or getFullName (with escapes)
			//System.out.println("Got variable " + name + " dim " + var.getDimensionsString());
			Unit unit = null;

			Logger.debug("in Models3ObsDataset.initVariables, unit = " + unit);
			for (Attribute attribute : (Iterable<Attribute>) var.getAttributes()) {
				if (attribute.getShortName().equals("units")) {
					unit = VUnits.createUnit(attribute.getStringValue());
					Logger.debug("in Models3ObsDatabase.initVariables, unit now = " + unit);
				}
			}
			if (unit == null) {
				String  unitStr = KNOWN_UNITS.get(name);
				if (unitStr != null)
					unit = VUnits.createUnit(unitStr);
			}
			if (unit == null)
				unit = VUnits.MISSING_UNIT;
			else {
				String unitStr = unit.toString();
				String cleanedString = unitStr.replaceAll(" ", "").trim();
				if (cleanedString.equals("") || cleanedString.equals("none"))
					cleanedString = "-";
				if (!cleanedString.equals(unitStr))
					unit = VUnits.createUnit(cleanedString);
			}
			
			if (renderVarList.contains(name) || name.startsWith("verdi.")) {
				this.renderVars.put(name, var);
				this.verdiRenderVars.add(new DefaultVariable(name, name, VUnits.MISSING_UNIT, this));
			}
			else if (!hiddenVars.contains(name) &&
					var.getDimensionsString().indexOf("nCells") != -1
					) 
				{
					this.vars.add(new DefaultVariable(name, name, unit, this));
					Logger.debug("in Models3ObsDataset.initVariables, after check for hiddenVars, unit = " + unit);
				}

				
		}
		this.verdiRenderVars.add(new DefaultVariable(VAR_AVG_CELL_DIAM, VAR_AVG_CELL_DIAM, VUnits.MISSING_UNIT, this));
		numCells = dataset.findDimension("nCells").getLength();
		maxEdges = dataset.findDimension("maxEdges").getLength();
	    //xCords = new int[maxEdges];
	    //yCords = new int[maxEdges];
	}
	
	private int findAttributeInt(String attname) {
		Attribute att = dataset.findGlobalAttributeIgnoreCase(attname);
		return att.getNumericValue().intValue();
	}
	
	private String findAttributeStr(String attname) {
		Attribute att = dataset.findGlobalAttributeIgnoreCase(attname);
		return att.getStringValue();
	}
	
	Palette defaultPalette =  new PavePaletteCreator().createPalettes(8).get(0);
	Color[] legendColors = defaultPalette.getColors();
	
	private void setCalendar(String timeStr, Calendar cal) {
		String[] arr = timeStr.split("[-_:.]");
		cal.clear();
		cal.set(Calendar.YEAR, Integer.parseInt(arr[0]));
		cal.set(Calendar.MONTH, Integer.parseInt(arr[1]) - 1);
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(arr[2]));
		if (arr.length > 3) {
			cal.set(Calendar.HOUR, Integer.parseInt(arr[3]));
			cal.set(Calendar.MINUTE, Integer.parseInt(arr[4]));
			cal.set(Calendar.SECOND, Integer.parseInt(arr[5]));	
		}
		if (arr.length > 6)
			cal.set(Calendar.MILLISECOND, Integer.parseInt(arr[6]));	
	}
		
	private CoordAxis makeTimeCoordAxis(String timeName) {
		//TODO - come up with exception handling strategy
		String start_str = findAttributeStr("config_start_time");
		String stop_str = findAttributeStr("config_stop_time");
		String duration_str = findAttributeStr("config_run_duration");
		double duration = 0;

		Calendar startCal = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
		setCalendar(start_str, startCal);
		
		Calendar endCal = new GregorianCalendar(new SimpleTimeZone(0, "GMT"));
		endCal.setTimeInMillis(0);

		
		if (!duration_str.equals("none")) {
			String[] arr = duration_str.split("\\.");
			if (arr.length > 1)
				endCal.add(Calendar.MILLISECOND, Integer.parseInt(arr[1]));
			arr = arr[0].split("_");
			if (arr.length > 1) {
				endCal.add(Calendar.DAY_OF_YEAR, Integer.parseInt(arr[0]));
				arr[0] = arr[1];
			}
			arr = arr[0].split(":");
			endCal.add(Calendar.HOUR, Integer.parseInt(arr[0]));
			endCal.add(Calendar.MINUTE, Integer.parseInt(arr[1]));
			endCal.add(Calendar.SECOND, Integer.parseInt(arr[2]));
			duration = endCal.getTimeInMillis() / 1000;
		}
		else {
			setCalendar(stop_str, endCal);
			duration = (endCal.getTimeInMillis() - startCal.getTimeInMillis()) / 1000;
		}

		String units = null; //TODO - seconds since " + dateFormatOut.format(cal.getTime()) + " UTC";

		int time_step = (int) findAttributeInt("config_dt");
		int steps = (int)(duration / time_step);


		Dimension dimt = dataset.findDimension(timeName);
		int nt = dimt.getLength();
		if (nt < steps)
			steps = nt;
		ArrayInt.D1 data = new ArrayInt.D1(nt);
		Double[] timeVals = new Double[steps];
		for (int i = 0; i < steps; i++) {
			data.set(i, i * time_step);
			timeVals[i] = new Double(startCal.getTimeInMillis() + i * time_step);
		}

		// create the coord axis
		CSVTimeAxis timeAxis = new CSVTimeAxis(timeVals, "Time", "Time");
		CoordinateAxis1D timeCoord = new CoordinateAxis1D(dataset, null, "time", DataType.INT, timeName, units,
						"synthesized time coordinate from SDATE, STIME, STEP global attributes");
		timeCoord.setCachedData(data, true);
		timeCoord.addAttribute(new Attribute(ucar.nc2.constants._Coordinate.AxisType, ucar.nc2.constants.AxisType.Time.toString()));

		dataset.addCoordinateAxis(timeCoord);
		return timeAxis;
	}


	private void initAxes() throws IOException {

		List<CoordAxis> list = new ArrayList<CoordAxis>();
		
		list.add(makeTimeCoordAxis("Time"));
				
		MPASBoxer boxer = new MPASBoxer();
		
		try {
			cellVertices = getRenderVariable("nEdgesOnCell").read();
			latVert = getRenderVariable("latVertex").read();
			lonVert = getRenderVariable("lonVertex").read();
			latCell = getRenderVariable("lonCell").read();
			lonCell = getRenderVariable("lonCell").read();
			vertexList = (ucar.ma2.ArrayInt.D2) getRenderVariable("verticesOnCell").read();

			
			indexToVertexId = getRenderVariable("indexToVertexID").read();
			int numVertices = indexToVertexId.getShape()[0];
			for (int i = 0; i < numVertices; ++i)
				vertexPositionMap.put(indexToVertexId.getInt(i), i);
			
			int[] vertexShape = vertexList.getShape();

			cellsToRender = new CellInfo[vertexShape[0]];
			splitCells = new ArrayList<CellInfo>();

		
			long cellDiamCount = 0;
			double cellDiamSum = 0;
			avgCellDiam = 0;
			for (int i = 0; i < vertexShape[0]; ++i) { //for each cell
				int vertices = cellVertices.getInt(i);
				cellsToRender[i] = new CellInfo(i, vertices);
				for (int j = 0; j < vertices; ++j) { //for each vertex
					int vId = vertexList.get(i,j);
					vId = vertexPositionMap.get(vId);
					cellsToRender[i].latCoords[j] =latVert.getDouble(vId) * -1;
					cellsToRender[i].lonCoords[j] =lonVert.getDouble(vId) - Math.PI;
					if (cellsToRender[i].lonCoords[j] < 0)
						cellsToRender[i].lonCoords[j] = 2 * Math.PI + cellsToRender[i].lonCoords[j];
					
					if (cellsToRender[i].latCoords[j] < latMin)
						latMin = cellsToRender[i].latCoords[j];
					if (cellsToRender[i].lonCoords[j] < lonMin)
						lonMin = cellsToRender[i].lonCoords[j];
					if (cellsToRender[i].latCoords[j] > latMax)
						latMax = cellsToRender[i].latCoords[j];
					if (cellsToRender[i].lonCoords[j] > lonMax)
						lonMax = cellsToRender[i].lonCoords[j];

					//latCoords.add(cellsToRender[i].latCoords[j]);
					//lonCoords.add(cellsToRender[i].lonCoords[j]);
					//System.out.println("Cell " + i + " vertex " + j + " id " + vId + " x " + xCord.getDouble(vId) + " y " + yCord.getDouble(vId) + " z " + zCord.getDouble(vId));

				}
				
				boolean splitCell = false;
				boolean splitLat = false;
				CellInfo cell = cellsToRender[i];
				CellInfo cellHalf = null;
				double minCellLon = Double.MAX_VALUE;
				double maxCellLon = Double.MAX_VALUE * -1;; 
				double minCellLat = Double.MAX_VALUE;
				double maxCellLat = Double.MAX_VALUE * -1;
				
				for (int j = 0; j < cell.lonCoords.length; ++j) {
					if (cell.lonCoords[j] < minCellLon)
						minCellLon = cell.lonCoords[j];
					if (cell.lonCoords[j] > maxCellLon)
						maxCellLon = cell.lonCoords[j];
					if (maxCellLon - minCellLon > Math.PI * 1.5) {
						splitCell = true;
						cellHalf = cell.clone();
					}
				}
				if (splitCell) {
					for (int j = 0; j < cell.lonCoords.length; ++j) {
						if (cell.lonCoords[j] < Math.PI) {
							cell.lonCoords[j] = Math.PI * 2;
						}
						else if (cellHalf.lonCoords[j] > Math.PI)
							cellHalf.lonCoords[j] = 0;
					}
					splitCells.add(cellHalf); //Longitude ranges from 0 to 2pi
				}
				else {
					++cellDiamCount;
					cellDiamSum += maxCellLon - minCellLon;
				}
				for (int j = 0; j < cell.latCoords.length; ++j) {
					if (cell.latCoords[j] < minCellLat)
						minCellLat = cell.latCoords[j];
					if (cell.latCoords[j] > maxCellLat)
						maxCellLat = cell.latCoords[j];
					if (maxCellLat - minCellLat > Math.PI * 75) {
						if (!splitCell)
							cellHalf = cell.clone();
						splitCell = true;
						splitLat = true;
					}
				}
				if (splitLat) {
					for (int j = 0; j < cell.latCoords.length; ++j) {
						if (cell.latCoords[j] < 0) {
							cell.latCoords[j] = Math.PI / 2;
						}
						else if (cellHalf.lonCoords[j] > 0)
							cellHalf.lonCoords[j] = Math.PI / -2;
					}
					splitCells.add(cellHalf);
				}
			}
						
			dataWidth = lonMax - lonMin;
			dataHeight = latMax - latMin;
			System.out.println("Lat min " + latMin + " max " + latMax + " lon min " + lonMin + " max " + lonMax);
			
			addLayer("nVertLevels", list);
			addLayer("nVertLevelsP1", list);
			addLayer("nSoilLevels", list);
			
			//Construct axes for latitude and longitude, using average diameter as spacing
			avgCellDiam = cellDiamSum / cellDiamCount;
			
			list.add(new MPASCoordAxis("x", "x", lonMin, lonMax, AxisType.X_AXIS));
			list.add(new MPASCoordAxis("y", "y", latMin, latMax, AxisType.Y_AXIS));
						
		} catch (IOException e) {
			e.printStackTrace();
		}

		coordAxes = new Axes<CoordAxis>(list, boxer);
		
	}
	
	private void addLayer(String layerName, List<CoordAxis> axisList) {
		int numLevels = dataset.findDimension(layerName).getLength();
		Double[] vertList = new Double[numLevels];
		for (int i = 0; i < vertList.length; ++i)
			vertList[i] = (double)i;
		if (vertList.length > 0)
			axisList.add(new CSVCoordAxis(vertList, layerName, layerName, AxisType.LAYER));
	}

	/**
	 * Gets the coordindate Axes for this Dataset.
	 * 
	 * @return the coordindate Axes for this Dataset.
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
		//if (urlIndex != Dataset.SINGLE_DATASET)
		//	name += "_" + urlIndex;
		name = getAlias() + " " + name;
	}

	/**
	 * Gets the index of this Dataset inside its URL, or Dataset.SINGLE_DATASET
	 * if it is the only dataset inside the URL.
	 * 
	 * @return the index of this Dataset inside its URL
	 */
	public int getIndexInURL() {
		return Dataset.SINGLE_DATASET;
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
		return dataset;
	}

	/**
	 * Get the netcdf VariableDS corresponding to the specified Variable
	 * 
	 * @param var
	 *            the Variable
	 * @return the corresponding netsdf varibleDS object
	 */
	ucar.nc2.Variable getVariableDS(Variable var) {
		List<ucar.nc2.Variable> variables = dataset.getVariables();
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
		dataset.close();
	}

	/**
	 * Get the netcdf dataset meta data
	 * 
	 * @return the DatasetMetadata object
	 */
	@Override
	public DatasetMetadata getMetadata() {
		return new MPASMetadata(dataset);
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
