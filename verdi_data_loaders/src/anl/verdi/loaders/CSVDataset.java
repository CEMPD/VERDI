package anl.verdi.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import ucar.ma2.ArrayDouble;
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
 * Dataset implementation for those datasets that are read as CSV files
 *
 * @author Nick Collier
 * @author Eric Tatara
 * @version $Revision$ $Date$
 */
public class CSVDataset extends AbstractDataset {
	static final Logger Logger = LogManager.getLogger(CSVDataset.class.getName());

	private static Map<ucar.nc2.constants.AxisType, AxisType> types = new HashMap<ucar.nc2.constants.AxisType, AxisType>();

	static {
		types.put(ucar.nc2.constants.AxisType.GeoX, AxisType.X_AXIS);
		types.put(ucar.nc2.constants.AxisType.GeoY, AxisType.Y_AXIS);
		types.put(ucar.nc2.constants.AxisType.GeoZ, AxisType.LAYER);
		types.put(ucar.nc2.constants.AxisType.Height, AxisType.LAYER);
		types.put(ucar.nc2.constants.AxisType.Time, AxisType.TIME);
	}

	private int conv = -1;
	private Axes<CoordAxis> coordAxes;
	private int urlIndex = Dataset.SINGLE_DATASET;
	private List<Variable> vars;
	private String name = "";
	private List<String> columnNames;
	private boolean canceledGuiInput = false;
	private int numCols, numRows;
  private Map<String,String> columnNameMap;
  private String[] fields = {"Time Axis Column","X Axis Column","Y Axis Column","Layer Axis Column","Value Axis Column"};
	
	Map<String,Double[]> dataMap;
	Map<String,Double[]> axesMap;
	private ArrayDouble.D4 array;

	Map<String,Map<Double,Integer>> axesPosMap;

	/**
	 * Creates a CSVDataset from the specified url,
	 *
	 * @param url         the url of the dataset
	 */
	protected CSVDataset(URL url) {
		this(url, Dataset.SINGLE_DATASET);
	}


	/**
	 * Creates a CSVDataset from the specified url
	 * 
	 * @param url         the url of the dataset
	 * @param urlIndex the cardinality of this Dataset inside the specified URL.
	 */
	protected CSVDataset(URL url, int urlIndex) {
		super(url);

		this.urlIndex = urlIndex;

		try {
			loadColumnHeaders(url);
		} catch (IOException e) {
			e.printStackTrace();
		}

		new CSVDialog(this,columnNames,fields);

		if (canceledGuiInput)
			return;

		try {
			load(url);
		} catch (IOException e) {
			e.printStackTrace();
		}

		init();
	}

	
	private void loadColumnHeaders(URL url) throws IOException {
		// Read in the column headers from the CSV file and check
		// the size of the data in the file (rows and columns)
		
		BufferedReader buf  = new BufferedReader(new InputStreamReader(url.openStream()));
		columnNames = new ArrayList<String>();

		// Grab the column names from the first line
		String line = buf.readLine();
		StringTokenizer st = new StringTokenizer(line,","); 

		while (st.hasMoreTokens())
			columnNames.add(st.nextToken());

		// count num of rows to allocate storage  
		while (buf.readLine() != null)
			numRows++;
		numCols = columnNames.size();

		buf.close();
	}

	private void load(URL url) throws IOException {

		// Loads data from the CSV file and create the NetCDF Array object 
		// based on the user's data mappings from the CSV dialog.
		
		// Read in the data from the CSV file and store in a 2D
		// double array 
		double data[][] = new double[numRows][numCols];

		BufferedReader buf  = new BufferedReader(new InputStreamReader(url.openStream()));
		String line;
		StringTokenizer st;
  
		buf  = new BufferedReader(new InputStreamReader(url.openStream()));  
		buf.readLine();                     // skip column headers

		int row = 0;
		int col = 0;
		while((line = buf.readLine()) != null){
			st = new StringTokenizer(line,",");
			col = 0;
			while (st.hasMoreTokens()){
				data[row][col] = new Double(st.nextToken());
				col++;
			}
			row++;
		}
		buf.close(); 


		// build a map of <name, data> for easier access
		dataMap = new HashMap<String,Double[]>();
		col = 0;
		for (String varName : columnNames){
			Double d[] = new Double[numRows];

			for (int i=0; i<numRows; i++)
				d[i] = data[i][col];

			dataMap.put(varName, d);

			col++;
		}

		// Create the NetCDF Array object
		ArrayList<Double> list;
		axesMap = new HashMap<String,Double[]>();
		axesPosMap = new HashMap<String, Map<Double,Integer>>();
		Double[] axesData;
		Map<Double,Integer> posMap;
		int cnt;
		int[] dim = new int[4];
		
		
		// Loop over all axis and build a unique set for each.  Since the
		// CSV data will probably contain multiple entries for each axis data,
		// we should build an array containing only the unique values,
		// ie [1,2,3...etc].
		for (int i=0; i< fields.length-1; i++){  // Don't grab the value field
					
			// Get the entire axis column data from the data map (raw CSV data)
			list = new ArrayList(Arrays.asList(dataMap.get(columnNameMap.get(fields[i]))));
		
			// Make a unique set
			Set<Double> set = new HashSet<Double>();
			set.addAll(list);
			list.clear();
			list.addAll(set);
			Collections.sort(list);
			dim[i] = list.size();  // new array of size of unique axis data values

			// Also record the position of the data in the list for later retrieval
			// since the data in the CSV file may not be ordered sequentially wrt
			// the axis data
			axesData = new Double[dim[i]];
			posMap = new HashMap<Double,Integer>();
			cnt=0;
			for (Double d : list){
				axesData[cnt] = d;
				posMap.put(d, cnt);
				cnt++;
			}
			axesMap.put(columnNameMap.get(fields[i]), axesData);
			axesPosMap.put(columnNameMap.get(fields[i]), posMap);
		}
		
		array = new ArrayDouble.D4(dim[0],dim[1],dim[2],dim[3]);
		
		// Now actually build the NetCDF Array object
		
		double[] csvData = new double[5];
		int[] csvDataLoc = new int[5];
		
		for (int i=0; i<numRows; i++){   // loop over each row in csv data
		
			for (int j=0; j< csvData.length; j++){
				csvData[j] = dataMap.get(columnNameMap.get(fields[j]))[i];
				if (j < csvData.length - 1)
				  csvDataLoc[j] = (Integer)((Map)(axesPosMap.get(columnNameMap.get(fields[j])))).get(csvData[j]);
			}
				
			// Set the data in the CDF Array type
			array.set(csvDataLoc[0],csvDataLoc[1],csvDataLoc[2],csvDataLoc[3], csvData[4]);
		}
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

	private void init() {

		createAxes();

		vars = new ArrayList<Variable>();

		Unit unit = VUnits.MISSING_UNIT;
		Logger.debug("in CSVDataset.init, unit = " + unit);

		String var = columnNameMap.get(fields[4]);
		vars.add(new DefaultVariable(var, var, unit, (Dataset)this));
	}

	private void createAxes() {

		List<CoordAxis> list = new ArrayList<CoordAxis>();
		boolean layerFound = false;

		//TODO make generic
		String var = columnNameMap.get(fields[0]);
		list.add(new CSVTimeAxis(axesMap.get(var), var, var));
		
		var = columnNameMap.get(fields[1]);
		list.add(new CSVCoordAxis(axesMap.get(var), var, var, AxisType.X_AXIS));
		
		var = columnNameMap.get(fields[2]);
		list.add(new CSVCoordAxis(axesMap.get(var), var, var, AxisType.Y_AXIS));
		
		var = columnNameMap.get(fields[3]);
		list.add(new CSVCoordAxis(axesMap.get(var), var, var, AxisType.LAYER));
		layerFound = true;
		
		if (!layerFound){
			var = "layerID";
			list.add(new CSVCoordAxis(axesMap.get(var), var, var, AxisType.LAYER));
		}
		coordAxes = new Axes<CoordAxis>(list, new CSVBoxer());
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
	 * @param alias the alias for this Dataset.
	 */
	@Override
	public void setAlias(String alias) {
		super.setAlias(alias);
		name = url.toString();
		name = name.substring(name.lastIndexOf("/") + 1, name.length());
		if (urlIndex != Dataset.SINGLE_DATASET) name += "_" + urlIndex;
		name = getAlias() + " " + name;
	}

	/**
	 * Gets the index of this Dataset inside its URL, or
	 * Dataset.SINGLE_DATASET if it is the only dataset
	 * inside the URL.
	 *
	 * @return the index of this Dataset inside its URL
	 */
	public int getIndexInURL() {
		return urlIndex;
	}

	/**
	 * Gets the name of this Dataset.
	 *
	 * @return  the name of this Dataset.
	 */
	public String getName() {
		return name;
	}

	public Map<String, Double[]> getDataMap() {
		return dataMap;
	}

	public ArrayDouble.D4 getArray() {
		return array;
	}

	public void setCanceledGuiInput(boolean canceledGuiInput) {
		this.canceledGuiInput = canceledGuiInput;
	}

	public void setColumnNameMap(Map<String, String> columnNameMap) {
		this.columnNameMap = columnNameMap;
	}

	/**
	 * Get the dataset meta data
	 *
	 * @return the DatasetMetadata object
	 */	
	@Override
	public DatasetMetadata getMetadata() {
		// TODO depend on what we need in the development
		return null;
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
