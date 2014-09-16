package anl.verdi.loaders;

import java.util.Date;				// added 2014
import java.util.GregorianCalendar;
import java.util.List;				// added 2014

import ucar.nc2.dataset.CoordinateAxis1DTime;
import ucar.nc2.time.CalendarDate;				// added 2014
import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.TimeCoordAxis;

/**
 * Coordinate axis wrapping the netcdf time axis.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class NetcdfTimeAxis extends NetCdfCoordAxis implements TimeCoordAxis {

	public NetcdfTimeAxis(CoordinateAxis1DTime axis) {
		super(axis, AxisType.TIME);
		System.out.println("in constructor for NetcdfTimeAxis");
	}

//	public Date getDate(int timestep) {		// replaced for GeoTools v10; updated NetCDF library
//		Date[] dates = ((CoordinateAxis1DTime) axis).getTimeDates();
//		if (timestep < 0 || timestep > dates.length - 1) return null;
//		return dates[timestep];
//	}
	/**
	 * Returns the Date for the specified timestep, or null if
	 * the timestep is invalid.
	 *
	 * @param timestep the time step we want the date for.
	 * @return the Date for the specified timestep
	 */
	public GregorianCalendar getDate(int timestep)
	{
		if(timestep < 0)
			return null;
		List<CalendarDate> myDates = ((CoordinateAxis1DTime) axis).getCalendarDates();
		if(timestep > myDates.size() - 1)
			return null;
		long dateTimeMs = myDates.get(timestep).getMillis();
		GregorianCalendar retDate = new GregorianCalendar();
		retDate.setTimeInMillis(dateTimeMs);
		return retDate;
	}


//	public int getTimeStep(Date date) {			// replaced for GeoTools v10; updated NetCDF library
//		long time = date.getTime();
//		Date[] dates = ((CoordinateAxis1DTime) axis).getTimeDates();  
//		for (int i = 0; i < dates.length; i++) {
//			Date axisDate = dates[i];
//			if (axisDate.getTime() == time) return i;
//		}
//		return Axes.TIME_STEP_NOT_FOUND;
//	}
	/**
	 * Gets the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 *
	 * @param date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(Date date) {			// replaced for GeoTools v10; updated NetCDF library
		long time = date.getTime();
		List<CalendarDate> myDates = ((CoordinateAxis1DTime) axis).getCalendarDates();  
		for (int i = 0; i < myDates.size(); i++) {
			long dateTimeMs = myDates.get(i).getMillis();
			if (dateTimeMs == time) return i;
		}
		return Axes.TIME_STEP_NOT_FOUND;
	}
	
	public int getTimeStep(GregorianCalendar aCalendar)
	{
		long time = aCalendar.getTimeInMillis();
		List<CalendarDate> myDates = ((CoordinateAxis1DTime) axis).getCalendarDates();  
		for (int i = 0; i < myDates.size(); i++) {
			long dateTimeMs = myDates.get(i).getMillis();
			if (dateTimeMs == time) return i;
		}
		return Axes.TIME_STEP_NOT_FOUND;

	}
}
