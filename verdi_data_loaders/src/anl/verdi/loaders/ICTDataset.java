package anl.verdi.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
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
import anl.verdi.plot.data.TextDataset;
import anl.verdi.util.VUnits;

/**
 * Dataset implementation for those datasets that are read as CSV files
 *
 * @author Nick Collier
 * @author Eric Tatara
 * @version $Revision$ $Date$
 */
public class ICTDataset extends AbstractDataset implements TextDataset {
	static final Logger Logger = LogManager.getLogger(ICTDataset.class.getName());

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
	private boolean latLonDetected = false;
  private Map<String,String> columnNameMap;
  private String[] fields = {"Time Axis Column","X Axis Column","Y Axis Column","Layer Axis Column","Value Axis Column"};
	
	Map<String,Double[]> dataMap;
	Map<String,Double[]> axesMap;
	private ArrayDouble.D4 array;

	Map<String,Map<Double,Integer>> axesPosMap;

	/**
	 * Creates a ICTDataset from the specified url,
	 *
	 * @param url         the url of the dataset
	 */
	protected ICTDataset(URL url) {
		this(url, Dataset.SINGLE_DATASET);
	}

	private String findField(String[] sourceCols) {
		for (int i = 0; i < sourceCols.length; ++i) {
			if (columnNames.contains(sourceCols[i])) {
				return sourceCols[i];
			}
		}
		return null;
	}
	
	public boolean hasColumn(String name) {
		return name != null && columnNames.contains(name);
	}

	/**
	 * Creates a ICTDataset from the specified url
	 * 
	 * @param url         the url of the dataset
	 * @param urlIndex the cardinality of this Dataset inside the specified URL.
	 */
	protected ICTDataset(URL url, int urlIndex) {
		super(url);

		this.urlIndex = urlIndex;

		try {
			loadColumnHeaders(url);
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		//TODO - add me
		//new CSVDialog(this,columnNames,fields);

		if (canceledGuiInput)
			return;


		//TODO - remove me
		columnNameMap = new HashMap<String, String>();
		//columnNameMap.put(fields[0], "Start_UTC");
		columnNameMap.put(fields[1], "Longitude");
		columnNameMap.put(fields[2], "Latitude");
		
		
		String[] sourceCols = new String[] { "Start_UTC", "Mid_UTC", "UTC", "Stop_UTC" };
		columnNameMap.put(fields[0], findField(sourceCols));
		sourceCols = new String [] { "Longitude", "LONGITUDE", "Lon", "LON" };
		columnNameMap.put(fields[1], findField(sourceCols));
		sourceCols = new String [] { "Latitude", "LATITUDE", "Lat", "LAT" };
		columnNameMap.put(fields[2], findField(sourceCols));

		


		try {
			load(url);
		} catch (IOException e) {
			e.printStackTrace();
		}

		init();
		
		Set<String> reservedNames = new HashSet<String>();
		reservedNames.add("UTC");
		reservedNames.add("Start_UTC");
		reservedNames.add("Stop_UTC");
		reservedNames.add("Mid_UTC");
		reservedNames.add("Longitude");
		reservedNames.add("Latitude");
		
		vars = new ArrayList<Variable>();

		Unit missing = VUnits.MISSING_UNIT;
		Unit unit = null;
		
		for (String var: columnNames) {
			var = var.trim();
			if (!reservedNames.contains(var)) {
				String unitStr = units.get(var);
				unit = VUnits.createUnit(unitStr);
				vars.add(new DefaultVariable(var, var, unit, (Dataset)this));
			}
		}
	}
	
	String investigator = null;
	String investigatorOrg = null;
	String description = null;
	String missionName = null;
	String volume = null;;
	
	String dataInterval = null;
	String[] independentVar = null;
	String varCount = null;
	String scaleFactorList = null;
	String missingIndicatorList = null;
	String specialCommentCount = null;
	List<String> specialComments = new ArrayList<String>();
	String normalCommentCount = null;
	List<String> normalComments = new ArrayList<String>();
	Map<String, String> units = new HashMap<String, String>();
	Map<String, Double> missingIndicators = new HashMap<String, Double>();
	Map<String, String> scaleFactors = new HashMap<String, String>();
	
	
	
	private void loadColumnHeaders(URL url) throws IOException {
		BufferedReader buf  = new BufferedReader(new InputStreamReader(url.openStream()));
		String line;
		line = buf.readLine();
		int headerSize = Integer.parseInt(line.split(",")[0]);
		
		investigator = buf.readLine();
		investigatorOrg = buf.readLine();
		description = buf.readLine();
		missionName = buf.readLine();
		volume = buf.readLine();
		String dateLine = buf.readLine();
		String[] dateString = dateLine.split(",");
		
		startDate = new GregorianCalendar();
		startDate.set(GregorianCalendar.YEAR,  Integer.parseInt(dateString[0].trim()));
		startDate.set(GregorianCalendar.MONTH,  Integer.parseInt(dateString[1].trim()) - 1);
		startDate.set(GregorianCalendar.DAY_OF_MONTH,  Integer.parseInt(dateString[2].trim()));	
		startDate.set(GregorianCalendar.HOUR,  0);
		startDate.set(GregorianCalendar.MINUTE,  0);
		startDate.set(GregorianCalendar.SECOND,  0);
		startDate.set(GregorianCalendar.MILLISECOND,  0);
		startDate.set(GregorianCalendar.AM_PM, GregorianCalendar.AM);
		
		jdayBase = new GregorianCalendar();
		jdayBase.set(GregorianCalendar.YEAR,  Integer.parseInt(dateString[0].trim()));
		jdayBase.set(GregorianCalendar.MONTH,  0);
		jdayBase.set(GregorianCalendar.DAY_OF_MONTH,  1);	
		jdayBase.set(GregorianCalendar.HOUR,  0);
		jdayBase.set(GregorianCalendar.MINUTE,  0);
		jdayBase.set(GregorianCalendar.SECOND,  0);
		jdayBase.set(GregorianCalendar.MILLISECOND,  0);
		jdayBase.set(GregorianCalendar.AM_PM, GregorianCalendar.AM);

		dataInterval = buf.readLine();
		independentVar = buf.readLine().split(",");
		varCount = buf.readLine();
		scaleFactorList = buf.readLine();
		missingIndicatorList = buf.readLine();
		int listLength = Integer.parseInt(varCount);
		for (int i = 0; i < listLength; ++i) {
			String[] unitLine = buf.readLine().split(",");
			units.put(unitLine[0],  unitLine[1]);
		}
		specialCommentCount = buf.readLine();
		listLength = Integer.parseInt(specialCommentCount);
		for (int i = 0; i < listLength; ++i)
			specialComments.add(buf.readLine());
		normalCommentCount = buf.readLine();
		listLength = Integer.parseInt(normalCommentCount);
		for (int i = 0; i < listLength; ++i) {
			normalComments.add(buf.readLine());
		}
		String headerLine = normalComments.get(listLength - 1);
		loadColumnHeaders(headerLine, buf);
		latLonDetected = true;
		return;
		
	}
	
	GregorianCalendar startDate = null;
	GregorianCalendar jdayBase = null;

	
	Map<String, Integer> columnMap = new HashMap<String, Integer>();
	
	private void loadColumnHeaders(String line, BufferedReader buf) throws IOException {
		// Read in the column headers from the CSV file and check
		// the size of the data in the file (rows and columns)
		
		columnNames = new ArrayList<String>();

		// Grab the column names from the first line
		StringTokenizer st = new StringTokenizer(line,","); 

		int idx = 0;
		int missingIdx = 0;
		String[] missingMarkers = missingIndicatorList.split(",");
		while (st.hasMoreTokens()) {
			String header = st.nextToken().trim();
			columnNames.add(header);
			if (!header.equals(independentVar[0]))
				missingIndicators.put(header, Double.parseDouble(missingMarkers[missingIdx++]));
			columnMap.put(header, idx++);
		}

		// count num of rows to allocate storage  
		while (buf.readLine() != null)
			numRows++;
		numCols = columnNames.size();

		buf.close();
	}
	
	private void load(URL url) throws IOException {
		BufferedReader buf  = new BufferedReader(new InputStreamReader(url.openStream()));
		String line;
		buf  = new BufferedReader(new InputStreamReader(url.openStream()));
		if (latLonDetected) {
			while((line = buf.readLine()) != null){ //Skip preamble and look for Lat / Lon
				if (line.toLowerCase().indexOf("lat") != -1 && line.toLowerCase().indexOf("lon") != -1) {
					if (line.split(",").length > 4) {
						load(buf);
						return;
					}
				}
			}			
		} else {
			buf.readLine();                     // skip column headers
			load(buf);
		}		
		
	}

	private void load(BufferedReader buf) throws IOException {

		// Loads data from the CSV file and create the NetCDF Array object 
		// based on the user's data mappings from the CSV dialog.
		
		// Read in the data from the CSV file and store in a 2D
		// double array 
		double data[][] = new double[numRows][numCols];

		String line;
		StringTokenizer st;
  

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
		int jdayIndex = -1;
		if (columnMap.containsKey("JDAY")) {
			jdayIndex = columnMap.get("JDAY");
		}
		for (String varName : columnNames){
			Double d[] = new Double[numRows];

			if (varName.indexOf("UTC") != -1) {
				if (jdayIndex > -1) {
					for (int i=0; i<numRows; i++)
						d[i] = buildICTTime(data[i][jdayIndex], data[i][col]);
				}else {
					for (int i=0; i<numRows; i++)
						d[i] = buildICTTime(startDate, data[i][col]);
				}
			} else {
				for (int i=0; i<numRows; i++)
					d[i] = data[i][col];
			}


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
		
		//  Map<String,Double[]> dataMap; //map of data file column names to list of values for that column
		//  Map<String,Double[]> axesMap; //map of data file column names to sorted list of unique values for that column
		// 	Map<Double,Integer> posMap; //map of each unique value to its position in axesMap list	
		//	Map<String,Map<Double,Integer>> axesPosMap; //map of data file column names to posMap for that column
		//  int[] dim = new int[4]; //number of unique values for each column from fields
		
		//  private Map<String,String> columnNameMap; //key = standard name, value = data file name
		//  private String[] fields = {"Time Axis Column","X Axis Column","Y Axis Column","Layer Axis Column","Value Axis Column"};

		// double[] csvData = new double[5]; // values of each field for each row
		// int[] csvDataLoc = new int[5]; // axis position for value of each field of each row
		
		
		// Loop over all axis and build a unique set for each.  Since the
		// CSV data will probably contain multiple entries for each axis data,
		// we should build an array containing only the unique values,
		// ie [1,2,3...etc].
		for (int i=0; i< fields.length-1; i++){  // Don't grab the value field
			if (!columnNameMap.containsKey(fields[i])) {//Don't grab layer if it's not in the file
				continue;
			}
			
			//if (!dataMap.containsKey(columnNameMap.get(fields[i])))
				//System.out.println("pause");
					
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
		
		dim[3] =1 ;
		array = new ICTDataArray();
		
		// Now actually build the NetCDF Array object
		
		double[] csvData = new double[5];
		int[] csvDataLoc = new int[5];
		
		
		
		for (int i=0; i<numRows; i++){   // loop over each row in csv data
		
			for (int j=0; j< csvData.length; j++){
				if (!columnNameMap.containsKey(fields[j]))
					continue;
				csvData[j] = dataMap.get(columnNameMap.get(fields[j]))[i];
				if (j < csvData.length - 1)
				  csvDataLoc[j] = (Integer)((Map)(axesPosMap.get(columnNameMap.get(fields[j])))).get(csvData[j]);
			}
				
			// Set the data in the CDF Array type
			if (array != null) {
				array.set(csvDataLoc[0],csvDataLoc[1],csvDataLoc[2],csvDataLoc[3], csvData[4]);
				((ICTDataArray)array).addRow(csvData[0], csvData[1], csvData[2], (int)csvData[3], csvData[4]);
			}
			
		}
		((ICTDataArray)array).setRawData(columnMap, data);
	}
	
	private static final long SECS_IN_DAY = 24*60*60;
	
	private double buildICTTime(double jday, double utc) {
		--jday; //jday seems to be 1 based
		long ictTime = Math.round(jday * SECS_IN_DAY * 1000 + utc * 1000 + jdayBase.getTimeInMillis());
		return ictTime;
		
	}
	
	private double buildICTTime(GregorianCalendar baseDate, double utc) {
		long ictTime = (long)utc * 1000 + baseDate.getTime().getTime();
		return ictTime;
		
	}
	
	public static Set<String> hiddenVars = new HashSet<String>();
	static {
		//hiddenVars.add("JDAY");
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
			if (!hiddenVars.contains(var.getName()))
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
	}

	private void createAxes() {

		List<CoordAxis> list = new ArrayList<CoordAxis>();
		boolean layerFound = false;

		//TODO make generic
		String var = columnNameMap.get(fields[0]);
		list.add(new ICTTimeAxis(startDate, axesMap.get(var), var, var));
		
		var = columnNameMap.get(fields[1]);
		list.add(new CSVCoordAxis(axesMap.get(var), var, var, AxisType.X_AXIS));
		
		var = columnNameMap.get(fields[2]);
		list.add(new CSVCoordAxis(axesMap.get(var), var, var, AxisType.Y_AXIS));
		
		var = columnNameMap.get(fields[3]);
		if (var != null) {
			list.add(new CSVCoordAxis(axesMap.get(var), var, var, AxisType.LAYER));
			layerFound = true;
			
			if (!layerFound){
				var = "layerID";
				list.add(new CSVCoordAxis(axesMap.get(var), var, var, AxisType.LAYER));
			}
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
	
	public boolean isObs() {
		return true;
	}
	
	public Double getMissingDataMarker(Variable variable) {
		return missingIndicators.get(variable.getName());
	}

}
