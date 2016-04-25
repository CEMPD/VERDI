/**
 * Dataset implementation for those datasets that are read using the netcdf
 * library and represent a collection of unstructured grid cells using the MPAS convention
 * 
 * @author Tony Howard
 * @version $Revision$ $Date$
 */

package anl.verdi.loaders;

import java.awt.Color;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unitsofmeasurement.unit.Unit;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import anl.verdi.data.AbstractDataset;
import anl.verdi.data.ArrayReader;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.Dataset;
import anl.verdi.data.DatasetMetadata;
import anl.verdi.data.DefaultVariable;
import anl.verdi.data.MPASDataFrameIndex;
import anl.verdi.data.MPASPlotDataFrame;
import anl.verdi.data.MeshCellInfo;
import anl.verdi.data.MultiAxisDataset;
import anl.verdi.data.Variable;
import anl.verdi.plot.color.Palette;
import anl.verdi.plot.color.PavePaletteCreator;
import anl.verdi.plot.data.IMPASDataset;
import anl.verdi.plot.data.MinMaxInfo;
import anl.verdi.plot.data.MinMaxLevelListener;
import anl.verdi.util.VUnits;

@SuppressWarnings("rawtypes")
public class MPASDataset extends AbstractDataset implements MultiAxisDataset, IMPASDataset {
	static final Logger Logger = LogManager.getLogger(MPASDataset.class.getName());
	
	public static final String VAR_AVG_CELL_DIAM = "verdi.avgCellDiam";

	private NetcdfDataset dataset;
	private Axes<CoordAxis> coordAxes;
	private CoordAxis defaultLayer = null;
	private CoordAxis defaultTime = null;
	private List<Variable> vars;
	private List<Variable> verdiRenderVars;
	private Map<String, ucar.nc2.Variable> renderVars;
	private String name = "";
	private int conv = -1;
	int numCells;
	GregorianCalendar startCal = null;
	Map<String, MPASMinMaxCalculator> levelCalculators = new HashMap<String, MPASMinMaxCalculator>();
	
	public static final double RAD_TO_DEG = 180 / Math.PI;
	
	private static final String VAR_ELEVATION = "nVertLevels";
	private static final String VAR_DEPTH = "nSoilLevels";
	
	Array cellVertices, latVert, lonVert, latCell, lonCell, indexToVertexId, indexToCellId;
	ArrayReader depth, elevation;
	ucar.ma2.ArrayInt.D2 vertexList = null;
	private Map<Integer, Integer> vertexPositionMap = new HashMap<Integer, Integer>();
	private Map<Integer, CellInfo> cellIdInfoMap;
	private CellInfo[] cellsToRender = null;
	private List<CellInfo> cellsToRenderList = null;
	Collection<MeshCellInfo> allCells = null;
	private Map<MeshCellInfo, Integer> splitCells = null;
	double dataRatio = 0;

	//double[] legendLevels = null;
	
	private static Set<String> hiddenVars = new HashSet<String>();
	
	private static Set<String> renderVarList = new HashSet<String>();
	
	private static Set<String> TIME_DIMENSIONS = new HashSet<String>();
	private static Set<String> LAYER_DIMENSIONS = new HashSet<String>();
	
	static {
		TIME_DIMENSIONS.add("Time");
		TIME_DIMENSIONS.add("nMonths");
		LAYER_DIMENSIONS.add("nVertLevels");
		LAYER_DIMENSIONS.add("nVertLevelsP1");
		LAYER_DIMENSIONS.add("nSoilLevels");
		LAYER_DIMENSIONS.add("nFGSoilLevels");
		LAYER_DIMENSIONS.add("nFGLevels");
		LAYER_DIMENSIONS.add("nOznLevels,");
	}

	
	public class CellInfo implements MeshCellInfo { 
		double[] latCoords;
		double[] lonCoords;
		boolean visible;
		int cellId;
		int colorIndex;
		double lon = 0;
		double lat = 0;
		int minX;
		int minY;
		int maxX;
		int maxY;
		boolean cellClicked = false;
		
		private CellInfo(int id, int numVertices) {
			latCoords = new double[numVertices];
			lonCoords = new double[numVertices];
			cellId = id;
			cellIdInfoMap.put(id,  this);
		}
		
		public void setLat(double val) {
			lat = val;
		}
		
		public void setLon(double val) {
			lon = val;
		}
		
		public int getNumVertices() {
			return latCoords.length;
		}
		
		public double getLon() {
			return lon;
		}
		
		public double getLon(int index) {
			return lonCoords[index] * RAD_TO_DEG;
		}
		
		public double getLonRad(int index) {
			return lonCoords[index];
		}
		
		public double getLat() {
			return lat;
		}

		public double getLat(int index) {
			return latCoords[index] * RAD_TO_DEG;
		}
		
		public double getLatRad(int index) {
			return latCoords[index];
		}
		
		public int getId() {
			return cellId;
		}
		
		public int getMinXPosition() {
			return minX;
		}
		
		public int getMaxXPosition() {
			return maxX;
		}
		
		public int getMinYPosition() {
			return minY;
		}
		
		public int getMaxYPosition() {
			return maxY;
		}
		
		public String getElevation(String axisName, int currentLayer, int currentTimestep) {
			String e = null;
			if (axisName != null) {
				if (axisName.equals(VAR_ELEVATION)) {
					double h1 = elevation.get(cellId, currentLayer);
					double h2 = elevation.get(cellId, currentLayer + 1);
					e = Long.toString(Math.round((h2 - h1) / 2 + h1));
				}
				if (axisName.equals(VAR_DEPTH) && depth != null) {
					double h1 = Math.round(depth.get(currentTimestep, cellId, currentLayer));
					double h2 = Math.round(depth.get(currentTimestep, cellId, currentLayer + 1));
					e = Long.toString(Math.round((h2 - h1) / 2 + h1));

				}
				if (e != null)
					return ", " + e + "m";
			}
			return "";
		}
				
		public double getValue(ArrayReader renderVariable, DataFrame frame, MPASDataFrameIndex index, int timestep, int layer) {			
			index.set(timestep, layer, cellId);
			return renderVariable.get(frame, index);
		}
		
		public double getMinX() {
			return getLon(minX);
		}
		
		public double getMinLon() {
			return lonCoords[minX];
		}
		
		public double getMaxX() {
			return getLon(maxX);
		}
		
		public double getMaxLon() {
			return lonCoords[maxX];
		}
		
		public double getMinY() {
			return getLat(minY);
		}
		
		public double getMaxY() {
			return getLat(maxY);
		}
		
		public double getMinLat() {
			return latCoords[minY];
		}
		
		public double getMaxLat() {
			return latCoords[maxY];
		}
		
		public void calculateCellBounds() {
			for (int j = 0; j < latCoords.length; ++j) {
				if (lonCoords[j] < lonCoords[minX])
					minX = j;
				if (lonCoords[j] > lonCoords[maxX])
					maxX = j;
				if (latCoords[j] < latCoords[minY])
					minY = j;
				if (latCoords[j] > latCoords[maxY])
					maxY = j;
			}	
		}
		
		public CellInfo split(int index) {			
			CellInfo clone = new CellInfo(cellId, lonCoords.length);
			clone.latCoords = Arrays.copyOf(latCoords, latCoords.length);
			clone.lonCoords = Arrays.copyOf(lonCoords, lonCoords.length);
			
			for (int j = 0; j < lonCoords.length; ++j) {
				if (lonCoords[j] < 0) {
					lonCoords[j] = Math.PI;
				}
				else if (clone.lonCoords[j] > 0)
					clone.lonCoords[j] = Math.PI * -1;
			}
			
			calculateCellBounds();
			clone.calculateCellBounds();
			
			clone.lat = lat;
			
			lon = (lonCoords[maxX] + lonCoords[minX]) / 2;
			clone.lon = (clone.lonCoords[clone.maxX] + clone.lonCoords[clone.minX]) / 2;
			
			
			splitCells.put(clone, index);
			
			//System.out.println("Split cell " + cell.cellId + " negative: " + negativeCell);
			//System.out.println("longitudes " + Arrays.toString(cell.lonCoords));
			//System.out.println("latitudes " + Arrays.toString(cell.latCoords));
			//System.out.println("longitudes " + Arrays.toString(cellHalf.lonCoords));
			//System.out.println("latitudes " + Arrays.toString(cellHalf.latCoords));

			return clone;
		}
	}

	/*
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
	
	*/
	
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
		renderVarList.add("xtime");
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
	
	double minX = Double.POSITIVE_INFINITY;
	double minY = Double.POSITIVE_INFINITY;
	double maxX = Double.NEGATIVE_INFINITY;
	double maxY = Double.NEGATIVE_INFINITY;
	double latMin = Double.POSITIVE_INFINITY;
	double lonMin = Double.POSITIVE_INFINITY;
	double latMax = Double.NEGATIVE_INFINITY;
	double lonMax = Double.NEGATIVE_INFINITY;

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
	 * @throws IOException 
	 */
	protected MPASDataset(URL url, 
			NetcdfDataset netcdfDataset) throws IOException {
		super(url);
		long start = System.currentTimeMillis();
		this.dataset = netcdfDataset;
		initVariables();
		initAxes();

		Logger.info("Dataset loaded in " + (System.currentTimeMillis() - start) + "ms");
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
	
	Map<String, Array> arrayCache = new HashMap<String, Array>();
	public Array read(String name) throws IOException {
		Array ret = arrayCache.get(name);
		if (ret != null)
			return ret;
		ucar.nc2.Variable var = getVariableDS(getVariable(name));
		if (var != null) {
			ret = var.read();
			arrayCache.put(name, ret);
		}
		return ret;
	}
	
	public Array read(String name, int[] origin, int[] shape) throws IOException, InvalidRangeException {
		if (origin == null && shape == null)
			return read(name);
		String key = name;
		if (origin != null)
			key += "." + Arrays.toString(origin);
		if (shape != null)
			key += "." + Arrays.toString(shape);
		Array ret = arrayCache.get(key);
		if (ret != null)
			return ret;
		ucar.nc2.Variable var = getVariableDS(getVariable(name));
		ret = var.read(origin, shape);
		arrayCache.put(key, ret);
		return ret;
	}
	

	
	private void initVariables() {
		List<ucar.nc2.Variable> vars = dataset.getVariables();
		String nCells = "nCells";
		this.vars = new ArrayList<Variable>();
		this.verdiRenderVars = new ArrayList<Variable>();
		this.renderVars = new HashMap<String, ucar.nc2.Variable>();
		for (ucar.nc2.Variable var : vars) {
			String name = var.getShortName();	// getName replaced by either getShortName or getFullName (with escapes)
			boolean valid = true;
			
			if (!renderVarList.contains(name)) {
				if (!var.getDimensionsString().contains(nCells))
					valid = false;
				String[] dimensions = var.getDimensionsString().split(" ");
				//Hide any variable with dimensions we don't know about
				for (String dim : dimensions) {
					if ( !dim.equals(nCells) &&
							!TIME_DIMENSIONS.contains(dim) &&
							!LAYER_DIMENSIONS.contains(dim))
						valid = false;
				}
				if (!valid)
					continue;
			}
					
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
	}
	
	Palette defaultPalette =  new PavePaletteCreator().createPalettes(8).get(0);
	Color[] legendColors = defaultPalette.getColors();
	
	public long getTimestepDuration(ArrayChar.D2 xtime, Calendar startCal) {
		int strLen = dataset.findDimension("StrLen").getLength();
		int timeSteps = dataset.findDimension("Time").getLength();

		StringBuffer buf = new StringBuffer();
		for (int j = 0; j < strLen; ++j)
			buf.append(xtime.get(0, j));
		Calendar startTime = stringToCal(buf.toString());
		startCal.setTimeInMillis(startTime.getTimeInMillis());
		
		if (timeSteps < 2)
			return 0;
		buf = new StringBuffer();
		for (int j = 0; j < strLen; ++j)
			buf.append(xtime.get(1, j));
		Calendar stopTime = stringToCal(buf.toString());
		return stopTime.getTimeInMillis() - startTime.getTimeInMillis();
	}
	
	private Calendar stringToCal(String timeStr) {
		timeStr = timeStr.trim();
		Calendar cal = new GregorianCalendar(new SimpleTimeZone(0, "UTC"));
		cal.clear();
		String[] arr = timeStr.split("\\.");
		if (arr.length > 1) {
			cal.set(Calendar.MILLISECOND, Integer.parseInt(arr[1]));
		}
		arr = timeStr.split("_");
		int i = 0;
		if (arr.length > 1) {
			String[] date = arr[i++].split("-");
			int j = date.length - 1;
			if (j >= 0)
				cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(date[j--]));
			if (j >= 0)
				cal.set(Calendar.MONTH, Integer.parseInt(date[j--]) - 1);
			if (j >= 0)
				cal.set(Calendar.YEAR, Integer.parseInt(date[j]) - 1);
		}
		String[] time = arr[i].split(":");
		int j = time.length - 1;
		if (j >= 0)
			cal.set(Calendar.SECOND, Integer.parseInt(time[j--]));
		if (j >= 0)
			cal.set(Calendar.MINUTE, Integer.parseInt(time[j--]));
		if (j >= 0)
			cal.set(Calendar.HOUR, Integer.parseInt(time[j--]));
		return cal;
	}
		
	private CoordAxis makeTimeCoordAxis(String timeName) {
		ArrayChar.D2 xtime = null;

		try {
			xtime = (ArrayChar.D2)read("xtime");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		startCal = new GregorianCalendar(new SimpleTimeZone(0, "UTC"));

		String units = null;
		
		int time_step = 0;

		if (xtime != null)
			time_step = (int) getTimestepDuration(xtime, startCal);

		int steps = dataset.findDimension(timeName).getLength();


		ArrayInt.D1 data = new ArrayInt.D1(steps);
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
		defaultTime = timeAxis;
		return timeAxis;
	}

	private void addMonths(List<CoordAxis> axisList) {
		String nMonths = "nMonths";
		Dimension dim = dataset.findDimension(nMonths);
		if (dim == null || startCal == null)
			return;
		
		int steps = dim.getLength();
		
		ArrayDouble.D1 data = new ArrayDouble.D1(steps);
		Double[] timeVals = new Double[steps];
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTimeInMillis(startCal.getTimeInMillis());
		for (int i = 0; i < steps; i++) {
			cal.add(GregorianCalendar.MONTH, 1);
			data.set(i, cal.getTimeInMillis());
			timeVals[i] = new Double((double)cal.getTimeInMillis());
		}

		// create the coord axis
		CSVTimeAxis timeAxis = new CSVTimeAxis(timeVals, nMonths, nMonths);
		CoordinateAxis1D timeCoord = new CoordinateAxis1D(dataset, null, "time", DataType.DOUBLE, nMonths, null,
						"synthesized time coordinate from nMonths dimension");
		timeCoord.setCachedData(data, true);
		timeCoord.addAttribute(new Attribute(ucar.nc2.constants._Coordinate.AxisType, ucar.nc2.constants.AxisType.Time.toString()));

		dataset.addCoordinateAxis(timeCoord);
		axisList.add(timeAxis);
	}


	private void initAxes() throws IOException {

		List<CoordAxis> list = new ArrayList<CoordAxis>();
		
		list.add(makeTimeCoordAxis("Time"));
		addMonths(list);
		
		MPASBoxer boxer = new MPASBoxer();
		
		cellVertices = read("nEdgesOnCell");
		latVert = read("latVertex");
		lonVert = read("lonVertex");
		latCell = read("latCell");
		lonCell = read("lonCell");
		elevation = ArrayReader.getReader(read("zgrid"));
		depth = ArrayReader.getReader(read("zs"));
		vertexList = (ucar.ma2.ArrayInt.D2) read("verticesOnCell");
		
		indexToVertexId = read("indexToVertexID");
		int numVertices = dataset.findDimension("nVertices").getLength();
		for (int i = 0; i < numVertices; ++i) {
			if (indexToVertexId != null)
				vertexPositionMap.put(indexToVertexId.getInt(i), i);
			else
				vertexPositionMap.put(i, i);
		}
		
		loadCellStructure();
					
		Logger.info("Lat min " + latMin + " max " + latMax + " lon min " + lonMin + " max " + lonMax);
		
		for (String layer : LAYER_DIMENSIONS)
			addLayer(layer, list);
		
		//Construct axes for latitude and longitude, using average diameter as spacing
		
		list.add(new MPASCoordAxis("x", "x", lonMin, AxisType.X_AXIS));
		list.add(new MPASCoordAxis("y", "y", latMin, AxisType.Y_AXIS));

		coordAxes = new Axes<CoordAxis>(list, boxer);
		
	}
	
	private void loadCellStructure() throws IOException {		
		int numCells =  dataset.findDimension("nCells").getLength();
		cellsToRender = new CellInfo[numCells];
		allCells = new HashSet<MeshCellInfo>();
		splitCells = new HashMap<MeshCellInfo, Integer>();
		cellIdInfoMap = new HashMap<Integer, CellInfo>();

		
		long cellDiamCount = 0;
		double cellDiamSum = 0;
		avgCellDiam = 0;		
		
		for (int i = 0; i < numCells; ++i) { //for each cell
			int vertices = cellVertices.getInt(i);
			CellInfo cell = new CellInfo(i, vertices);
			cellsToRender[i] = cell;
			
			for (int j = 0; j < vertices; ++j) { //for each vertex
				int vId = vertexList.get(i,j);
				vId = vertexPositionMap.get(vId);
				cell.latCoords[j] = normalizeLat(latVert.getDouble(vId));
				cell.lonCoords[j] = normalizeLon(lonVert.getDouble(vId));
			}
			
			cell.calculateCellBounds();
			cell.setLat(normalizeLat(latCell.getDouble(i)) * RAD_TO_DEG);
			cell.setLon(normalizeLon(lonCell.getDouble(i)) * RAD_TO_DEG);
			if (cell.getMinLon() < lonMin)
				lonMin = cell.getMinLon();
			if (cell.getMaxLon() > lonMax)
				lonMax = cell.getMaxLon();
			if (cell.getMinLat() < latMin)
				latMin = cell.getMinLat();
			if (cell.getMaxLat() > latMax)
				latMax = cell.getMaxLat();
			//if (vertices != 6)
				//System.out.println("Cell " + cell.getId() + " " + vertices + " vertices lon " + cell.lon + " lat " + cell.lat);
			
			if (cell.lonCoords[cell.maxX] - cell.lonCoords[cell.minX] > Math.PI * 1.5)
				cell.split(i);
			else {
				++cellDiamCount;
				cellDiamSum += cell.getMaxLon() - cell.getMinLon();
			}
		}
		
		dataWidth = lonMax - lonMin;
		dataHeight = latMax - latMin;
		
		//If almost full globe, set to full
		if (Math.PI * 1.9 < dataWidth) {
			lonMin = Math.PI * -1;
			lonMax = Math.PI;
			dataWidth = Math.PI * 2;
		}

		if (Math.PI * .95 < dataHeight) {
			latMin = Math.PI / -2;
			latMax = Math.PI / 2;
			dataHeight = Math.PI;
		}
		
		dataRatio = dataWidth / dataHeight;
		
		dataWidth = lonMax - lonMin;
		dataHeight = latMax - latMin;
		
		avgCellDiam = cellDiamSum / cellDiamCount;

		cellsToRenderList = Arrays.asList(cellsToRender);
		allCells.addAll(cellsToRenderList);
		allCells.addAll(splitCells.keySet());
		
		//System.out.println("Lat min " + latMin + " max " + latMax + " lon min " + lonMin + " max " + lonMax);
	}
	
	private static double normalizeLon(double lon) {
		lon = lon - 2 * Math.PI;
		if (lon < Math.PI * -1)
			lon = 2 * Math.PI + lon;
		return lon;
	}
	
	private static double normalizeLat(double lat) {
		return lat;
	}

	public double getLonMin() {
		return lonMin;
	}
	
	public double getLonMax() {
		return lonMax;
	}
	
	public double getLatMin() {
		return latMin;
	}
	
	public double getLatMax() {
		return latMax;
	}
	
	private void addLayer(String layerName, List<CoordAxis> axisList) {
		CoordAxis axis = null;
		Dimension dim = dataset.findDimension(layerName);
		if (dim == null)
			return;
		int numLevels = dim.getLength();
		Double[] vertList = new Double[numLevels];
		for (int i = 0; i < vertList.length; ++i)
			vertList[i] = (double)i;
		if (vertList.length > 0) {
			axis =new CSVCoordAxis(vertList, layerName, layerName, AxisType.LAYER);
			axisList.add(axis);
			if (layerName.equals("nVertLevels"))
				defaultLayer = axis;
			else if (defaultLayer == null)
				defaultLayer = axis;
		}
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
	
	public CoordAxis getZAxis(String variable) {
		List<CoordAxis> axisList = coordAxes.getAxes();
		ucar.nc2.Variable var = getVariableDS(getVariable(variable));
		Set<String> dimensions = new HashSet<String>();
		dimensions.addAll(Arrays.asList(var.getDimensionsString().split("\\s+")));
		for (CoordAxis axis : axisList) {
			if (axis.getAxisType().equals(AxisType.LAYER) && dimensions.contains(axis.getName()))
				return axis;
		}
		return null;
	}
	
	public CoordAxis getDefaultTimeAxis() {
		return defaultTime;
	}
	
	public CoordAxis getDefaultZAxis() {
		return defaultLayer;
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
		if (var == null)
			return null;
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
		cellIdInfoMap = null;
		cellsToRender = null;
		cellsToRenderList = null;
		allCells = null;
		splitCells = null;
		arrayCache.clear();
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
	
	private MPASMinMaxCalculator getLevelCalculator(DataFrame frame) {
		DataFrameAxis timeAxis = frame.getAxes().getTimeAxis();
		DataFrameAxis layerAxis = frame.getAxes().getZAxis();
		String timeStr = timeAxis != null ? timeAxis.getRange().toString() : "nt";
		String layerStr = layerAxis != null ? layerAxis.getRange().toString() : "nl";
		String cacheKey = frame.getVariable().getName() + frame.getArray().getClass().getName() + layerStr + timeStr;
		MPASMinMaxCalculator calculator = null;
		synchronized (levelCalculators) {
			calculator = levelCalculators.get(cacheKey);
			if (calculator == null) {
				calculator = new MPASMinMaxCalculator(this, frame);
				levelCalculators.put(cacheKey, calculator);
			}
			
		}
		return calculator;
	}

	@Override
	public MinMaxInfo getPlotMinMax(DataFrame variable, MinMaxLevelListener listener) {
		MPASMinMaxCalculator calculator = getLevelCalculator(variable);
		if (calculator == null)
			return null;
		return calculator.getMinMaxInfo(listener);
	}

	@Override
	public MinMaxInfo getLayerMinMax(DataFrame variable, int layer, MinMaxLevelListener listener) {
		MPASMinMaxCalculator calculator = getLevelCalculator(variable);
		if (calculator == null)
			return null;
		return calculator.getLayerInfo(layer, listener);
	}

	@Override
	public MinMaxInfo getTimestepMinMax(DataFrame variable, int layer, int timestep) {
		MPASMinMaxCalculator calculator = getLevelCalculator(variable);
		if (calculator == null)
			return null;
		return calculator.getTimestepMinMax(layer, timestep);
	}

	@Override
	public CoordAxis getTimeAxis(String variable) {
		List<CoordAxis> axisList = coordAxes.getAxes();
		ucar.nc2.Variable var = getVariableDS(getVariable(variable));
		Set<String> dimensions = new HashSet<String>();
		dimensions.addAll(Arrays.asList(var.getDimensionsString().split("\\s+")));
		for (CoordAxis axis : axisList) {
			if (axis.getAxisType().equals(AxisType.TIME) && dimensions.contains(axis.getName()))
				return axis;
		}
		return null;
	}
	
	public Collection<MeshCellInfo> getAllCells() {
		return allCells;
	}
	
	public MeshCellInfo getCellInfo(int id) {
		return cellIdInfoMap.get(id);
	}
	
	public double getDataWidth() {
		return dataWidth;
	}
	
	public double getDataHeight() {
		return dataHeight;
	}
	
	public double getDataRatio() {
		return dataRatio;
	}
	
	public MeshCellInfo[] getCellsToRender() {
		return cellsToRender;
	}
	
	public Map<MeshCellInfo, Integer> getSplitCells() {
		return splitCells;
	}

	/** 
	 * 
	 * DataFrames built by VERDI to not contain all attributes necessary for rendering.  They contain cell axis,
	 * are missing x/y axis, and have other issues.  This replaces the standard DataFrame with an updated version
	 * that can be used in rendering.  Alternate approaches may involve overriding CoordSysBuilder's
	 * augmentDataset method and manipulating the underlying dataset.
	 */
	public DataFrame augmentFrame(DataFrame frame) {
		if (frame instanceof MPASPlotDataFrame)
			return frame;
		return new MPASPlotDataFrame(frame);
	}

}
