package anl.verdi.loaders;

import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

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
public class ICTTimeAxis extends CSVCoordAxis implements TimeCoordAxis {
	static final Logger Logger = LogManager.getLogger(ICTTimeAxis.class.getName());
	
	GregorianCalendar startDate;

	public ICTTimeAxis(GregorianCalendar startDate, Double data[], String name, String description) {
		super(data, name, description, AxisType.TIME);
		this.startDate = startDate;
		Logger.debug("in constructor for ICTTimeAxis");
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
		
		GregorianCalendar aCalendar = (GregorianCalendar) GregorianCalendar.getInstance();
		aCalendar.setTimeInMillis(data[timestep].longValue());
		return aCalendar;
	}
	

	/**
	 * Gets the first timestep equal or after the specified date (Date). If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 *
	 * @param date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(Date date) {
		//Date firstDate = getDate(0).getTime();
		//if (firstDate.after(date))
			//return Axes.TIME_STEP_NOT_FOUND;
			
		//TODO - use start and stop columns instead
		//double stepSize = (data[1].longValue() - data[0].longValue()) * 1000;
		
		Date stepDate = null;
		
		for (int i = 0; i < data.length; i++) {
			stepDate = getDate(i).getTime();
			if (stepDate.equals(date) || stepDate.after(date)) {
				return i;
			}
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
	
	public GregorianCalendar getStartDate() {
		return startDate;
	}
}
