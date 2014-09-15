package anl.verdi.data;

import java.util.Date;
import java.util.GregorianCalendar;

public interface ExtendTimeCoordAxis extends ExtendCoordAxis {
	/**
	 * Returns the Date for the specified timestep, or null if
	 * the timestep is invalid.
	 *
	 * @param timestep the time step we want the date for.
	 * @return the Date for the specified timestep
	 */
	GregorianCalendar getDate(int timestep);

	/**
	 * Gets the timestep that corresponds to the specified Date (date). If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 *
	 * @param date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 */
	int getTimeStep(Date date);
	
	/**
	 * Gets the timestep that corresponds to the specified GregorianCalendar (date). If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 *
	 * @param date the GregorianCalendar (date) whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 */
	int getTimeStep(GregorianCalendar aCalendar);
}
