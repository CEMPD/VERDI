package anl.verdi.loaders;

import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.conv.M3IOConvention;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SimpleTimeZone;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.unitsofmeasurement.unit.Unit;

import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DatasetMetadata;
import anl.verdi.data.DefaultVariable;
import anl.verdi.data.Variable;
import anl.verdi.util.VUnits;

/**
 * Model3 dataset.
 *
 * @author Nick Collier
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 */
public class BCONDataset extends AbstractNetcdfDataset {

	static final Logger Logger = LogManager.getLogger(BCONDataset.class.getName());
	private int conv = -1;
	private NetcdfDataset dataset;

	private CoordAxis defaultTime = null;


	/**
	 * Creates an AbstractDataset.
	 *
	 * @param url the url of the dataset
	 */
	protected BCONDataset(URL url) {
		super(url);
		try {
			String urlString = url.toExternalForm();
			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}
			gridDataset = GridDataset.open(urlString);
			if (!M3IOConvention.isMine(gridDataset.getNetcdfDataset())) {
				throw new IOException("Loading non-models3 file into BCONDataset");
			}
		} catch (Exception io) {
			Logger.error("Error reading netcdf file " + io.getMessage());
			try {
				if (gridDataset != null)
					gridDataset.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected BCONDataset(URL url, 
			NetcdfDataset netcdfDataset) throws IOException {
		super(url);
		long start = System.currentTimeMillis();
		this.dataset = netcdfDataset;
		initVariables();
		initAxes();

		Logger.info("Dataset loaded in " + (System.currentTimeMillis() - start) + "ms");
	}
	
	static Set<String> BCON_DIMS = new HashSet<String>();
	
	static {
		BCON_DIMS.add("TSTEP");
		BCON_DIMS.add("LAY");
		BCON_DIMS.add("PERIM");
	}
	
	List<Variable> bconVars = null;

	private void initVariables() {
		List<ucar.nc2.Variable> vars = dataset.getVariables();

		bconVars = new ArrayList<Variable>();
		
		
		for (ucar.nc2.Variable var : vars) {
			String name = var.getShortName();	// getName replaced by either getShortName or getFullName (with escapes)
			boolean valid = true;
			
			String[] dimensions = var.getDimensionsString().split(" ");
			//Hide any variable with dimensions we don't know about
			for (String dim : dimensions) {
				if (!BCON_DIMS.contains(dim))
					valid = false;
			}
			if (!valid)
				continue;
			
					
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
				String  unitStr = MPASDataset.KNOWN_UNITS.get(name);
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
			bconVars.add(new DefaultVariable(name, name, unit, this));

		}
		
	}

	
	private void initAxes() {
		List<CoordAxis> list = new ArrayList<CoordAxis>();
		list.add(makeTimeCoordAxis("TSTEP"));

		//coordAxes = new Axes<CoordAxis>(list, boxer);
		
	}
	
	private CoordAxis makeTimeCoordAxis(String timeName) {
	
		
		/*
		 * 
		 * https://www.cmascenter.org/ioapi/documentation/all_versions/html/INCLUDE.html#magic
		 * 
		 * 
	    SDATE3D file start date YYYYDDD
	    STIME3D file start time HHMMSS
	    TSTEP3D file time step HHMMSS
	    MXREC3D maximum time step record number (1,2,...) 	
	    
	         CDATE3D file-creation date
CTIME3D file-creation time
WDATE3D last-update date
WTIME3D last-update time 
	    */

		
		String attrStartDate = getAttribute("SDATE").toString();
		String attrStartTime = getAttribute("STIME").toString();
		String attrTstep = getAttribute("TSTEP").toString();
		
		if (attrStartTime == null)
			attrStartTime = "000000";
		while (attrStartTime.length() < 6) {
			attrStartTime = "0" + attrStartTime;
		}
		
		while (attrTstep.length() < 6)
			attrTstep = "0" + attrTstep;
				
		int steps = dataset.findDimension("TSTEP").getLength();
		
		GregorianCalendar startCal = new GregorianCalendar(new SimpleTimeZone(0, "UTC"));
		
		startCal.set(Calendar.YEAR, Integer.parseInt(attrStartDate.substring(0, 4)));
		startCal.set(Calendar.DAY_OF_YEAR, Integer.parseInt(attrStartDate.substring(4, 6)));
		startCal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(attrStartTime.substring(0, 2)));
		startCal.set(Calendar.MINUTE, Integer.parseInt(attrStartTime.substring(2, 4)));
		startCal.set(Calendar.SECOND, Integer.parseInt(attrStartTime.substring(4, 6)));
		
		GregorianCalendar stepDuration = new GregorianCalendar(new SimpleTimeZone(0, "UTC"));
		stepDuration.set(Calendar.HOUR_OF_DAY, Integer.parseInt(attrTstep.substring(0, 2)));
		stepDuration.set(Calendar.MINUTE, Integer.parseInt(attrTstep.substring(2, 4)));
		stepDuration.set(Calendar.SECOND, Integer.parseInt(attrTstep.substring(4, 6)));
		
		long time_step = stepDuration.getTimeInMillis();

		ArrayDouble.D1 data = new ArrayDouble.D1(steps);
		Double[] timeVals = new Double[steps];
		for (int i = 0; i < steps; i++) {
			data.set(i, (long)i * time_step);
			timeVals[i] = new Double(startCal.getTimeInMillis() + i * time_step);
		}

		String units = null;

		// create the coord axis
		CSVTimeAxis timeAxis = new CSVTimeAxis(timeVals, "Time", "Time");
		CoordinateAxis1D timeCoord = new CoordinateAxis1D(dataset, null, "TSTEP", DataType.DOUBLE, timeName, units,
						"synthesized time coordinate from SDATE, STIME, STEP global attributes");
		timeCoord.setCachedData(data, true);
		timeCoord.addAttribute(new Attribute(ucar.nc2.constants._Coordinate.AxisType, ucar.nc2.constants.AxisType.Time.toString()));

		dataset.addCoordinateAxis(timeCoord);
		defaultTime = timeAxis;
		return timeAxis;
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
			CoordAxis axis = new BCONCoordAxis(this, AxisType.X_AXIS);
			list.add(axis);
			axis = new BCONCoordAxis(this, AxisType.Y_AXIS);
			list.add(axis);
			axis = new BCONCoordAxis(this, AxisType.LAYER);
			list.add(axis);
			coordAxes = new Axes<CoordAxis>(list, new NetcdfBoxer(grid));
		}
		return coordAxes;
	}

	
	public Object getAttribute(String attrName) {
		List<Attribute> attrs = dataset.getGlobalAttributes();
		for (Attribute attr : attrs) { 
			if (attr.getShortName().equals(attrName))
				return attr.getValue(0);
		}
		return null;

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
