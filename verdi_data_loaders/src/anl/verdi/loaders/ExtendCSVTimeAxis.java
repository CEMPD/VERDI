package anl.verdi.loaders;

import java.util.Date;
import java.util.GregorianCalendar;

import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.ExtendTimeCoordAxis;

public class ExtendCSVTimeAxis extends ExtendCSVCoordAxis implements ExtendTimeCoordAxis {

	public ExtendCSVTimeAxis(Object data[], String name, String description) {
		super(data, name, description, AxisType.TIME);
		System.out.println("in constructor for ExtendCSVTimeAxis");
	}

	/**
	 * Returns the Date for the specified timestep, or null if
	 * the timestep is invalid.
	 *
	 * @param timestep the time step we want the date for.
	 * @return the Date for the specified timestep
	 */
	public GregorianCalendar getDate(int timestep) {
		if (timestep < 0 || timestep > data.length - 1) 
			return null;
		
		GregorianCalendar aCalendar = new GregorianCalendar();
		aCalendar.setTimeInMillis((long) data[timestep]);
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
			
		for (int i = 0; i < data.length; i++) {
			Double d = (Double)data[i];
			if (d.longValue() == time) return i;
		}
		return Axes.TIME_STEP_NOT_FOUND;
	}
	
	public int getTimeStep(GregorianCalendar aCalendar)
	{
		long time = aCalendar.getTimeInMillis();
		
		for (int i = 0; i < data.length; i++)
		{
			Double d = (Double)data[i];
			if (d.longValue() == time)
				return i;
			
		}
		return Axes.TIME_STEP_NOT_FOUND;
	}
}
