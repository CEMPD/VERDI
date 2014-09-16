package anl.verdi.loaders;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.time.CalendarDate;
import anl.verdi.data.ExtendAxes;
import anl.verdi.data.AxisType;
import anl.verdi.data.ExtendTimeCoordAxis;

public class ExtendNetcdfTimeAxis extends ExtendNetCdfCoordAxis implements ExtendTimeCoordAxis {

	public ExtendNetcdfTimeAxis(CoordinateAxis1DTime axis) {
		super(axis, AxisType.TIME);
		System.out.println("in constructor for ExtendNetcdfTimeAxis");
	}

	/**
	 * Returns the GregorianCalendar for the specified timestep, or null if
	 * the timestep is invalid.
	 *
	 * @param timestep the time step we want the date for.
	 * @return the GregorianCalendar date for the specified timestep
	 */
	public GregorianCalendar getDate(int timestep) {
		List<CalendarDate> dates = ((CoordinateAxis1DTime) axis).getCalendarDates();		//getTimeDates();
		if (timestep < 0 || timestep > dates.size() - 1) return null;
		long aDateInMilllis = dates.get(timestep).getMillis();
		GregorianCalendar aCalendar = new GregorianCalendar();
		aCalendar.setTimeInMillis(aDateInMilllis);
		return aCalendar;
	}

	/**
	 * Gets the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 *
	 * @param date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(Date date) {
		long time = date.getTime();
//		Date[] dates = ((CoordinateAxis1DTime) axis).getTimeDates();
		List<CalendarDate> dates = ((CoordinateAxis1DTime) axis).getCalendarDates();
		//for (int i = 0; i < dates.length; i++) {
		for(int i=0; i<dates.size(); i++)
		{
			long axisDate = dates.get(i).getMillis();
			if (axisDate == time) return i;
		}
		return ExtendAxes.TIME_STEP_NOT_FOUND;
	}
	/**
	 * Gets the timestep that corresponds to the specified GregorianCalendar date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 *
	 * @param GregorianCalendar date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(GregorianCalendar date) {
		long time = date.getTimeInMillis();
//		Date[] dates = ((CoordinateAxis1DTime) axis).getTimeDates();
		List<CalendarDate> dates = ((CoordinateAxis1DTime) axis).getCalendarDates();
		//for (int i = 0; i < dates.length; i++) {
		for(int i=0; i<dates.size(); i++)
		{
			long axisDate = dates.get(i).getMillis();
			if (axisDate == time) return i;
		}
		return ExtendAxes.TIME_STEP_NOT_FOUND;
	}

}
