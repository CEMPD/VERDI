package anl.verdi.loaders;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import ucar.nc2.dataset.CoordinateAxis;
import anl.verdi.data.AxisType;
import anl.verdi.data.ExtendCoordAxis;
import anl.verdi.data.ExtendTimeCoordAxis;
import anl.verdi.data.Range;

public class ExtendSyntheticTimeAxis implements ExtendTimeCoordAxis {

	private static final String SECONDS_SINCE = "seconds since";
	public static final int TIME_STEP_NOT_FOUND = -1;

	private ExtendNetCdfCoordAxis axis;
	private Date date;
	private int calendarField;


	public ExtendSyntheticTimeAxis(CoordinateAxis axis) {
		this.axis = new ExtendNetCdfCoordAxis(axis, AxisType.TIME);
		System.out.println("in constructor for ExtendSyntheticTimeAxis");
		processAxis();
	}

	private void processAxis() {
		String unit = axis.getUnits().toString().trim();
		String tmp = unit.toLowerCase();
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
	 * Returns the Date for the specified timestep. If the
	 * we are unable to parse the time step axis sufficient to
	 * create a date then this returns null.
	 *
	 * @param timestep the time step we want the date for.
	 * @return the Date for the specified timestep, or null if
	 *         there is no time axis, or null if unable to parse
	 *         the time axis units.
	 */
	public GregorianCalendar getDate(int timestep) {
//		if (date == null) return null;
		GregorianCalendar calendar = new GregorianCalendar();	//.getInstance();
		calendar.setTime(date);
		calendar.add(calendarField, ((Double) axis.getValue(timestep)).intValue()); // (int) axis.getValue(timestep));
		return calendar;
	}

	/**
	 * Gets the timestep that corresponds to the specified date. If no such
	 * timestep is found then return TIME_STEP_NOT_FOUND.
	 *
	 * @param date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(Date date) {
		if (date == null) return TIME_STEP_NOT_FOUND;
		Range range = axis.getRange();
		long origin = range.getOrigin();
		for (long i = origin, n = (range.getOrigin() + range.getExtent()); i < n; i++) {
			GregorianCalendar d = getDate((int) i);
			if (date.equals(d)) return (int) (i - origin);
		}

		return TIME_STEP_NOT_FOUND;
	}


	/**
	 * Gets the timestep that corresponds to the specified GregorianCalendar (date). If no such
	 * timestep is found then return TIME_STEP_NOT_FOUND.
	 *
	 * @param GregorianCalendar date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(GregorianCalendar date) {
		if (date == null) return TIME_STEP_NOT_FOUND;
		Range range = axis.getRange();
		long origin = range.getOrigin();
		for (long i = origin, n = (range.getOrigin() + range.getExtent()); i < n; i++) {
			GregorianCalendar d = getDate((int) i);
			if (date.equals(d)) return (int) (i - origin);
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
		return axis.getUnits();
	}

	/**
	 * Gets the value at the specified index.
	 *
	 * @param index the index
	 * @return the value at the specified index.
	 */
	public Object getValue(int index) {
		return axis.getValue(index);
	}

	/**
	 * Gets whether or not the this axis is compatible with
	 * the specified axis. Two axes are compatible if they
	 * can be used together in the same formula.
	 *
	 * @param axis the axis to check for compatibility
	 * @return true if this axis is compatibilty with the other axis,
	 *         otherise false.
	 */
	public boolean isCompatible(ExtendCoordAxis axis) {
		return axis.isCompatible(axis);
	}
}
