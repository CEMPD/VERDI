package anl.verdi.data;

import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Time axis for a DataFrame.
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DataFrameTimeAxis extends DataFrameAxis implements TimeCoordAxis {

	/**
	 * Creates a DataFrameAxis for the specified axis with origin and extent
	 * equal to that of the axis.
	 *
	 * @param axis  the axis
	 * @param index the array index of this axis
	 */
	public DataFrameTimeAxis(TimeCoordAxis axis, int index) {
		super(axis, index);
System.out.println("in constructor for DataFrameTimeAxis.java");
	}

	/**
	 * Creates a DataFrameAxis for the specified axis with the specified origin and extent.
	 *
	 * @param origin the origin of the range
	 * @param extent the extent of the range
	 * @param axis   the axis
	 * @param index  the array index of this axis
	 */
	public DataFrameTimeAxis(TimeCoordAxis axis, int origin, int extent, int index) {
		super(axis, origin, extent, index);
System.out.println("in alternate constructor for DataFrameTimeAxis");
	}

	/**
	 * Creates a DataFrameAxis from the specified axis and index. This
	 * DataFrameAxis will have the same origin and extent as the
	 * specified axis.
	 *
	 * @param axis  the axis to create this DataFrameAxis from
	 * @param index the array index of this axis
	 */
	public DataFrameTimeAxis(DataFrameTimeAxis axis, int index) {
		super(axis, index);
System.out.println("in another alternate constructor for DataFrameTimeAxis");
	}

	/**
	 * Returns the Date for the specified timestep, or null if
	 * the timestep is invalid.
	 *
	 * @param timestep the time step we want the date for.
	 * @return the Date for the specified timestep
	 */
	public GregorianCalendar getDate(int timestep) {
System.out.println("in DataFrameTimeAxis getDate");
		return ((TimeCoordAxis)this.axis).getDate(timestep);
	}

	/**
	 * Gets the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 *
	 * @param date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 *         timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(Date date) {
System.out.println("in DataFrameTimeAxis getTimeStep for a Date");
		return ((TimeCoordAxis)this.axis).getTimeStep(date);
	}
	/**
	 * Gets the timestep that corresponds to the specified date. If no such
	 * timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 *
	 * @param date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 *         timestep is found then return Axes.TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(GregorianCalendar aCalendar) {
System.out.println("in DataFrameTimeAxis getTimeStep for a GregorianCalendar");
		return ((TimeCoordAxis)this.axis).getTimeStep(aCalendar);
	}
}
