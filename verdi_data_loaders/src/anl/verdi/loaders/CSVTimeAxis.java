package anl.verdi.loaders;

import java.util.Date;
import java.util.GregorianCalendar;

import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.TimeCoordAxis;

/**
 * Coordinate axis wrapping the netcdf time axis.
 *
 * @author Nick Collier
 * @author Eric Tatara
 * @version $Revision$ $Date$
 */
public class CSVTimeAxis extends CSVCoordAxis implements TimeCoordAxis {

	public CSVTimeAxis(Double data[], String name, String description) {
		super(data, name, description, AxisType.TIME);
		System.out.println("in constructor for CSVTimeAxis");
	}
//	/**
//	 * Returns the GregorianCalendar date for the specified timestep, or null if
//	 * the timestep is invalid.
//	 *
//	 * @param timestep the time step we want the date for.
//	 * @return the GregorianCalendar for the specified timestep
//	 */
	public GregorianCalendar getDate(int timestep) {
		if (timestep < 0 || timestep > data.length - 1) 
			return null;
		
		GregorianCalendar aCalendar = new GregorianCalendar();
		aCalendar.setTimeInMillis(data[timestep].longValue());
		return aCalendar;
	}

	/**
	 * Gets the timestep that corresponds to the specified date (Date). If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 *
	 * @param date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(Date date) {
		long time = date.getTime();
			
		for (int i = 0; i < data.length; i++) {
			Double d = data[i];
			if (d.longValue() == time) return i;
		}
		return Axes.TIME_STEP_NOT_FOUND;
	}
	/**
	 * Gets the timestep that corresponds to the specified GregorianCalendar date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 *
	 * @param date the GregorianCalendar date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(GregorianCalendar date)
	{
		long time = date.getTimeInMillis();	//.getTime();
		for(int i = 0; i<data.length; i++)
		{
			Double d = data[i];
			if (d.longValue() == time)
				return i;
		}
		return Axes.TIME_STEP_NOT_FOUND;
	}
}
