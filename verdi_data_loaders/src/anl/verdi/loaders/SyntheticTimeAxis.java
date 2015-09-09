package anl.verdi.loaders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.opengis.referencing.crs.CoordinateReferenceSystem;
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import ucar.nc2.dataset.CoordinateAxis;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Range;
import anl.verdi.data.TimeCoordAxis;

/**
 * Creates a Time axis from an ordinate CoordAxis, assuming the
 * units of that axis are "seconds since" some date.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class SyntheticTimeAxis implements TimeCoordAxis {
	static final Logger Logger = LogManager.getLogger(SyntheticTimeAxis.class.getName());

	private static final String SECONDS_SINCE = "seconds since";
	public static final int TIME_STEP_NOT_FOUND = -1;

	private NetCdfCoordAxis axis;
	private Date date;
	private int calendarField;


	public SyntheticTimeAxis(CoordinateAxis axis) {
		this.axis = new NetCdfCoordAxis(axis, AxisType.TIME);
		Logger.debug("in constructor for SyntheticTimeAxis");
		processAxis();
	}

	private void processAxis() {
		String unit = axis.getUnits().toString().trim();
		Logger.debug("in SyntheticTimeAxis.processAxis, unit = " + unit);
		String tmp = unit.toLowerCase();
		Logger.debug("in SyntheticTimeAxis.processAxis, after toLowerCase, tmp = " + tmp);
		String dateString = null;
		if (tmp.startsWith(SECONDS_SINCE)) {
			dateString = unit.substring(SECONDS_SINCE.length(), unit.length()).trim();
			calendarField = Calendar.SECOND;
		}

		if (dateString != null) {
			try {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z");
				date = dateFormat.parse(dateString);
			} catch (ParseException ex) {
			}
		}
	}

	/**
	 * Returns the GregorianCalendar date for the specified timestep. If the
	 * we are unable to parse the time step axis sufficient to
	 * create a date then this returns null.
	 *
	 * @param timestep the time step we want the date for.
	 * @return the GregorianCalendar date for the specified timestep, or null if
	 *         there is no time axis, or null if unable to parse
	 *         the time axis units.
	 */
//	public Date getDate(int timestep) {
	public GregorianCalendar getDate(int timestep) {
		if (date == null) return null;
		GregorianCalendar aCalendar = //GregorianCalendar.getInstance();
				new GregorianCalendar();
		aCalendar.setTime(date);
		aCalendar.add(calendarField, (int) axis.getValue(timestep));
		return aCalendar;		//.getTime();
	}

	/**
	 * Gets the timestep that corresponds to the specified date. If no such
	 * timestep is found then return TIME_STEP_NOT_FOUND.
	 *
	 * @param date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(Date date) {		// 2014 changed to reflect changes in Java Date
		if (date == null) return TIME_STEP_NOT_FOUND;
		long dateInMillis = date.getTime();
		Range range = axis.getRange();
		int origin = (int)range.getOrigin();
		for (int i = origin, n = (int)(range.getOrigin() + range.getExtent()); i < n; i++) {
			GregorianCalendar d = getDate(i);
			//if (date.equals(d)) return i - origin;
			if(d.getTimeInMillis() == dateInMillis)
				return i - origin;
		}

		return TIME_STEP_NOT_FOUND;
	}

	/**
	 * Gets the timestep that corresponds to the specified GregorianCalendar date. If no such
	 * timestep is found then return TIME_STEP_NOT_FOUND.
	 *
	 * @param date the GregorianCalendar (date) whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(GregorianCalendar aCalendar) {		// 2014 new as part of changes in Java Date
		if (aCalendar == null) return TIME_STEP_NOT_FOUND;
		long dateInMillis = aCalendar.getTimeInMillis();
		Range range = axis.getRange();
		int origin = (int)range.getOrigin();
		for (int i = origin, n = (int)(range.getOrigin() + range.getExtent()); i < n; i++) {
			GregorianCalendar iCalendar = getDate(i);
			if(iCalendar.getTimeInMillis() == dateInMillis)
				return i - origin;
		}

		return TIME_STEP_NOT_FOUND;
	}


	/**
	 * Gets the type of this axis.
	 *
	 * @return the type of this axis.
	 */
	public AxisType getAxisType() {
		return axis.getAxisType();
	}

	/**
	 * Gets the description of this CoordAxis.
	 *
	 * @return the description of this CoordAxis.
	 */
	public String getDescription() {
		return axis.getDescription();
	}

	/**
	 * Gets the name of this CoordAxis.
	 *
	 * @return the name of this CoordAxis.
	 */
	public String getName() {
		return axis.getName();
	}

	/**
	 * Gets the range of this axis.
	 *
	 * @return the range of this axis.
	 */
	public Range getRange() {
		return axis.getRange();
	}

	/**
	 * Gets the unit of measurement for this coordinate axis.
	 *
	 * @return the unit of measurement for this coordinate axis.
	 */
	public Unit getUnits() {
		Logger.debug("in SyntheticTimeAxis.getUnits, value = " + axis.getUnits());
		return axis.getUnits();
	}

	/**
	 * Gets the value at the specified index.
	 *
	 * @param index the index
	 * @return the value at the specified index.
	 */
	public double getValue(int index) {
		return axis.getValue(index); // TODO: changed 0 -> index, MIGHT be a bug - 2011-11-28
	}

	/**
	 * Gets whether or not the this axis is compatible with
	 * the specified axis. Two axes are compatible if they
	 * can be used together in the same formula.
	 *
	 * @param axis the axis to check for compatibility
	 * @return true if this axis is compatibility with the other axis,
	 *         Otherwise false.
	 */
	public boolean isCompatible(CoordAxis axis) {
		return axis.isCompatible(axis);
	}
	
	/** 
	 * Does not apply to a time axis.
	 */
	public 	CoordinateReferenceSystem getCRS()
	{
		return null;
	}

}