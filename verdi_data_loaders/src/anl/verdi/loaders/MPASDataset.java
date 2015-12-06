package anl.verdi.loaders;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SimpleTimeZone;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;














import javax.swing.*;

import java.awt.image.*;
import java.awt.*;

import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
//import simphony.util.messages.MessageCenter;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableEnhanced;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPoint;
import ucar.unidata.geoloc.ProjectionPointImpl;
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

//	private static final MessageCenter msgCenter = MessageCenter.getMessageCenter(GridNetcdfDataset.class);
	private static Map<ucar.nc2.constants.AxisType, AxisType> types = new HashMap<ucar.nc2.constants.AxisType, AxisType>();

	/*static {
		types.put(ucar.nc2.constants.AxisType.GeoX, AxisType.X_AXIS);
		types.put(ucar.nc2.constants.AxisType.GeoY, AxisType.Y_AXIS);
		types.put(ucar.nc2.constants.AxisType.Lon, AxisType.X_AXIS);
		types.put(ucar.nc2.constants.AxisType.Lat, AxisType.Y_AXIS);
		types.put(ucar.nc2.constants.AxisType.GeoZ, AxisType.LAYER);
		types.put(ucar.nc2.constants.AxisType.Height, AxisType.LAYER);
		types.put(ucar.nc2.constants.AxisType.Time, AxisType.TIME);
	}*/

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
	//int[] xCords = null;
    //int[] yCords = null;

	double[] legendLevels = null;
	
	private static Set<String> hiddenVars = new HashSet<String>();
	
	private static Set<String> renderVarList = new HashSet<String>();
	
	private class CellInfo { 
		double[] latCoords;
		double[] lonCoords;
		int[] latTransformed;
		int[] lonTransformed;
		int cellId;
		int colorIndex;
		
		private CellInfo(int id, int numVertices) {
			latCoords = new double[numVertices];
			lonCoords = new double[numVertices];
			latTransformed = new int[numVertices];
			lonTransformed = new int[numVertices];
			cellId = id;
		}
		
		private void transformCell(double factor, int imageWidth, int imageHeight) {
			for (int i = 0; i < lonCoords.length; ++i) {
				lonTransformed[i] = (int)Math.round((lonCoords[i] - lonMin) * factor);
				latTransformed[i] = (int)Math.round((latCoords[i] - latMin) * factor);
			}
			colorIndex = indexOfObsValue((float) renderVariable.get(0, cellId), legendLevels);
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
	int previousWindowWidth = 0;
	int sscaledWidth = 0;
	int sscaledHeight = 0;
	
	static {
		renderVarList.add("verticesOnCell");
		renderVarList.add("nEdgesOnCell");
		renderVarList.add("latVertex");
		renderVarList.add("lonVertex");
		renderVarList.add("xVertex");
		renderVarList.add("yVertex");
		renderVarList.add("zVertex");
		renderVarList.add("indexToVertexID");
		renderVarList.add("lonCell");
	}
	
	ucar.ma2.Array cellVertices = null;
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
		initLegend();
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

	private void initVariables() {
		List<ucar.nc2.Variable> vars = dataset.getVariables();
		this.vars = new ArrayList<Variable>();
		this.verdiRenderVars = new ArrayList<Variable>();
		this.renderVars = new HashMap<String, ucar.nc2.Variable>();
		for (ucar.nc2.Variable var : vars) {
			String name = var.getShortName();	// getName replaced by either getShortName or getFullName (with escapes)
			//System.out.println("Got variable " + name + " dim " + var.getDimensionsString());
			Unit unit = VUnits.MISSING_UNIT;
			Logger.debug("in Models3ObsDataset.initVariables, unit = " + unit);
			for (Attribute attribute : (Iterable<Attribute>) var.getAttributes()) {
				if (attribute.getShortName().equals("units")) {
					unit = VUnits.createUnit(attribute.getStringValue());
					Logger.debug("in Models3ObsDatabase.initVariables, unit now = " + unit);
				}
			}
			
			if (!hiddenVars.contains(name) &&
					var.getDimensionsString().indexOf("Time") != -1 &&
					var.getDimensionsString().indexOf("nCells") != -1
					) 
				{
					this.vars.add(new DefaultVariable(name, name, unit, this));
					Logger.debug("in Models3ObsDataset.initVariables, after check for hiddenVars, unit = " + unit);
				}
			else if (renderVarList.contains(name)) {
				this.renderVars.put(name, var);
				//TODO - what should units be?
				this.verdiRenderVars.add(new DefaultVariable(name, name, VUnits.MISSING_UNIT, this));
			}
				
		}
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
	
	   private static final Stroke STROKE = new BasicStroke(12f,
	            BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	   
	   public static void main(String[] args) {
		   MPASSwingTest.main(args);
	   }
	public static void mainn(String[] args) {
	      java.awt.EventQueue.invokeLater(new Runnable() {
	          public void run() {
		String loc = "file:///home/tahoward/allstate/history.2015-08-12_00.00.00.nc";
		try {
			NetcdfDataset ds = NetcdfDataset.openDataset(loc);
			MPASDataset mpasDs = new MPASDataset(new URL(loc), ds);
			List<ucar.nc2.dataset.CoordinateAxis> list = ds.getCoordinateAxes();
			System.out.println("Got " + list.size() + " axes");
			Dimension dimt = ds.findDimension("Time");
			System.out.println("Got dim " + dimt);
			System.out.println("Dim " + dimt.getShortName() + " " + dimt.getLength());

			ucar.nc2.Variable snow = null;
			List<ucar.nc2.Variable> vars = ds.getVariables();
			for (ucar.nc2.Variable var : vars) {
				//System.out.println("Got var " + var.getNameAndDimensions());
				if (var.getShortName().equals("snow"))
					snow = var;
			}
			
			
			System.out.println("Snow size " + snow.getSize() + " shape " + Arrays.toString(snow.getShape()));
			System.out.println("Attr units: " + snow.getUnitsString() + " arr " + snow.read().getClass());
			System.out.println("With " + ds.getCoordinateAxes().size() + " axes");
			ucar.ma2.ArrayDouble.D2 arr = (ucar.ma2.ArrayDouble.D2)snow.read();
			System.out.println("Shape " + Arrays.toString(arr.getShape()));

			


			

		  			JFrame frame = new JFrame("HelloWorldSwing");
			        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

			        //Add the ubiquitous "Hello World" label.
			        //JLabel label = new JLabel("Hello World");
			        frame.setSize(1024, 768);
			       // frame.getContentPane().add(label);

			        //Display the window.
			        frame.pack();
			        frame.setVisible(true);
			        frame.setSize(1024, 768);
			        
			        /*java.awt.Graphics frameGraphics = frame.getContentPane().getGraphics();
			        frameGraphics.setColor(Color.RED);
			        frameGraphics.setPaintMode();
			        //frameGraphics.drawImage(img, 0, 0, Color.WHITE, null);
			        frameGraphics.fillRect(0,  0,  400,  400);*/


			
			ds.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	          }
	       });
	}
	
	Palette defaultPalette =  new PavePaletteCreator().createPalettes(8).get(0);
	Color[] legendColors = defaultPalette.getColors();
	ArrayDouble.D2 renderVariable = null;
	double varMin = Double.MAX_VALUE;
	double varMax = 0;

	private void initLegend() {
		//String renderVarName = "surface_pressure";
		String renderVarName = "snowh"; // 0 to 0.01452, little picture
		renderVarName = "xice"; //0 to 1, only red
		renderVarName = "vegfra"; //0 to 99, decent pic
		renderVarName = "glw";  //only 0
		renderVarName = "gsw";  //only 0
		renderVarName = "surface_pressure";
		
		renderVariable = null;
		List<ucar.nc2.Variable> vars = dataset.getVariables();
		for (ucar.nc2.Variable var : vars) {
			//System.out.println("Got var " + var.getNameAndDimensions());
			if (var.getShortName().equals(renderVarName))
				try {
					renderVariable = (ArrayDouble.D2)var.read();
				} catch (IOException e) {
					e.printStackTrace();
					return;
				}
		}
		
		for (int i = 0; i < numCells; ++i) {
			double v = renderVariable.get(0, i);
			if (v < varMin)
				varMin = v;
			if (v > varMax)
				varMax = v;
		}
		
		int count = legendColors.length + 1;
		final double delta = (varMax - varMin) / (count - 1);
		legendLevels = new double[count];
		for (int level = 0; level < count; ++level) {
			legendLevels[level] = varMin + level * delta;
		}
		previousWindowWidth = 0;
		
	}
	
	private static int indexOfObsValue(float value, final double[] values) {
		if (new Float(value).toString().equals("NaN"))
			return -1;
		
		if (value <= DataUtilities.BADVAL3 || value <= DataUtilities.AMISS3) 	// 2014 changed AMISS3 comparison from == to <=
			return -1;

		final int count = values.length;

		if (values[0] == values[values.length - 1])
			return 0;

		for (int index = 1; index < count; index++) {
			if (values[index] > value)
				return index - 1;
		}

		return count - 2;
	}

	public void renderCells(Graphics gr, int windowWidth, int windowHeight) {
		
		if (previousWindowWidth != windowWidth)
			transformCells(gr, windowWidth, windowHeight);
		
		long renderStart = System.currentTimeMillis();
		
		//int imageWidth = 8192;
		
		/*
		BufferedImage img = new java.awt.image.BufferedImage(imageWidth, imageHeight, java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
        java.awt.Graphics2D g = img.createGraphics();
        */

		for (int i = 0; i < numCells; ++i) { //for each cell
			if (cellsToRender[i].colorIndex == -1)
				continue;
			gr.setColor(legendColors[cellsToRender[i].colorIndex]);
			gr.fillPolygon(cellsToRender[i].lonTransformed, cellsToRender[i].latTransformed, cellsToRender[i].lonTransformed.length);
		}
		for (int i = 0; i < splitCells.size(); ++i) {
			if (splitCells.get(i).colorIndex == -1)
				continue;
			gr.setColor(legendColors[splitCells.get(i).colorIndex]);
			gr.fillPolygon(splitCells.get(i).lonTransformed, splitCells.get(i).latTransformed, splitCells.get(i).lonTransformed.length);
		}
		long renderTime = System.currentTimeMillis() - renderStart;
		System.out.println("Finished drawing image " + new Date() + " image in " + renderTime + "ms  window " + windowWidth + "x" + windowHeight);
		System.out.println("Var min " + varMin + " max " + varMax);
		/*
		java.io.File outputFile = new java.io.File("/tmp/mpasout.png");
		try {
			boolean res = javax.imageio.ImageIO.write(img, "png", outputFile);
			System.out.println("Image written: " + res + " " + new Date());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		gr.drawImage(img.getScaledInstance(windowWidth, -1, Image.SCALE_FAST), 0, 0, null);
		*/
		System.out.println("Image drawn to screen " + new Date());
		
	}
	
	private void setCalendar(String timeStr, Calendar cal) {
		String[] arr = timeStr.split("[-_:.]");
		cal.clear();
		cal.set(Calendar.YEAR, Integer.parseInt(arr[0]));
		cal.set(Calendar.MONTH, Integer.parseInt(arr[1]));
		cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(arr[2]));
		if (arr.length > 3) {
			cal.set(Calendar.HOUR, Integer.parseInt(arr[3]));
			cal.set(Calendar.MINUTE, Integer.parseInt(arr[4]));
			cal.set(Calendar.SECOND, Integer.parseInt(arr[5]));	
		}
		if (arr.length > 6)
			cal.set(Calendar.MILLISECOND, Integer.parseInt(arr[6]));	
	}
	
	private static final SimpleDateFormat TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss");
	private static final SimpleDateFormat TIME_DURATION_FORMAT = new SimpleDateFormat("D_HH:mm:ss");
	
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
			timeVals[i] = new Double(i * time_step);
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
			lonCell = getRenderVariable("lonCell").read();
			vertexList = (ucar.ma2.ArrayInt.D2) getRenderVariable("verticesOnCell").read();

			
			indexToVertexId = getRenderVariable("indexToVertexID").read();
			int numVertices = indexToVertexId.getShape()[0];
			for (int i = 0; i < numVertices; ++i)
				vertexPositionMap.put(indexToVertexId.getInt(i), i);

			
			//Set<Double> latCoords = new TreeSet<Double>();
			//Set<Double> lonCoords = new TreeSet<Double>();
			
			int[] vertexShape = vertexList.getShape();

			cellsToRender = new CellInfo[vertexShape[0]];
			splitCells = new ArrayList<CellInfo>();
			//get average cell diameter
			//get lat max min
			//get lon max min
			//num steps = ceil(lat range / diam)
			//step size = range / numSteps
			long cellDiamCount = 0;
			double cellDiamSum = 0;
			double avgDiam = 0;
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
			
			String vertLevels = "nVertLevels";
			int numLevels = dataset.findDimension("nVertLevels").getLength();
			Double[] vertList = new Double[numLevels];
			for (int i = 0; i < vertList.length; ++i)
				vertList[i] = (double)i;
			list.add(new CSVCoordAxis(vertList, vertLevels, vertLevels, AxisType.LAYER));

			//Construct axes for latitude and longitude, using average diameter as spacing
			avgDiam = cellDiamSum / cellDiamCount;

			
			list.add(new MPASCoordAxis("x", "x", lonMin, lonMax, avgDiam, AxisType.X_AXIS));
			list.add(new MPASCoordAxis("y", "y", latMin, latMax, avgDiam, AxisType.Y_AXIS));
						
		} catch (IOException e) {
			e.printStackTrace();
		}

		
		
		//list.add(new SyntheticTimeAxis(timeAxis));

		/*var = columnNameMap.get(fields[1]);
		list.add(new CSVCoordAxis(axesMap.get(var), var, var, AxisType.X_AXIS));
		
		var = columnNameMap.get(fields[2]);
		list.add(new CSVCoordAxis(axesMap.get(var), var, var, AxisType.Y_AXIS));*/

		coordAxes = new Axes<CoordAxis>(list, boxer);
		
	}
	
	private void transformCells(Graphics gr, int windowWidth, int windowHeight) {
		long start = System.currentTimeMillis();
		double factor = windowWidth / dataWidth;
		previousWindowWidth = windowWidth;

		gr.setColor(Color.BLACK);
		gr.fillRect(0,  0,  windowWidth, windowHeight);
		int imageWidth = windowWidth;
		int imageHeight = (int)Math.round(imageWidth * dataHeight / dataWidth);
		for (int i = 0; i < cellsToRender.length; ++i) {
			cellsToRender[i].transformCell(factor, imageWidth, imageHeight);		
		}
		for (int i = 0; i < splitCells.size(); ++i) {
			splitCells.get(i).transformCell(factor, imageWidth, imageHeight);
		}
		System.out.println("Scaled cells in " + (System.currentTimeMillis() - start) + "ms");
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
