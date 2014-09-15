package anl.verdi.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;

import ucar.unidata.geoloc.Projection;

/**
 * Encapsulates the axes / the dimensions of some data.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Axes<T extends CoordAxis> {

	public static final int TIME_STEP_NOT_FOUND = -1;

	private T xAxis, yAxis, timeAxis, layerAxis;
	private List<T> axes = new ArrayList<T>();
	private ReferencedEnvelope boundingBox;
	private BoundingBoxer boxer;

	/**
	 * Creates an empty set of axes.
	 */
	public Axes() {}

	public Axes(List<T> axes, BoundingBoxer boxer) {
		this(axes);
		this.boxer = boxer;
	}

	public Axes(List<T> axes) {
		this.axes = new ArrayList<T>(axes);
		
		for (T axis : axes) {
			if (axis.getAxisType() == AxisType.X_AXIS) this.xAxis = axis;
			else if (axis.getAxisType() == AxisType.Y_AXIS) this.yAxis = axis;
			else if (axis.getAxisType() == AxisType.TIME) this.timeAxis = axis;
			else if (axis.getAxisType() == AxisType.LAYER) this.layerAxis = axis;
		}
	}


	/**
	 * Gets the geographical bounding box as defined by
	 * the x and y axis. This may be null
	 * if the individual axes contained by this Axes do not
	 * define a bounding box.
	 * @param netcdfConv TODO
	 *
	 * @return the geographical bounding box as defined by
	 * the x and y axis. This may be null
	 * if the individual axes contained by this Axes do not
	 * define a bounding box.
	 */
	public ReferencedEnvelope getBoundingBox(int netcdfConv) {
		if (boundingBox == null && xAxis != null && yAxis != null) {
			Range xRange = xAxis.getRange();
			Range yRange = yAxis.getRange();
			boundingBox = boxer.createBoundingBox(xRange.getLowerBound(), xRange.getUpperBound(),
						yRange.getLowerBound(), yRange.getUpperBound(), netcdfConv);
		}

		return boundingBox;
	}

	/**
	 * Gets the bounding boxer that this Axes uses
	 * to create its bounding box.
	 *
	 * @return the bounding boxer that this Axes uses
	 * to create its bounding box.
	 */
	public BoundingBoxer getBoundingBoxer() {
		return boxer;
	}

	/**
	 * Gets the geographical bounding box as defined by
	 * the specified ranges. This may be null if the individual axes contained
	 * by this Axes do not define a bounding box. The ranges
	 * are in grid cell coordinates.
	 *
	 * @param xRange the range along the x dimension in grid cell coordinates
	 * @param yRange the range along the y dimension in grid cell coordinates
	 * @param netcdfConv TODO
	 * @return the geographical bounding box as defined by specified ranges.
	 */
	public ReferencedEnvelope getBoundingBox(Range xRange, Range yRange, int netcdfConv) {
		if (xAxis != null && yAxis != null) {
			return boxer.createBoundingBox(xRange.getLowerBound(), xRange.getUpperBound(),
						yRange.getLowerBound(), yRange.getUpperBound(), netcdfConv);
		}
		return null;
	}
	
	/**
	 * Gets the projection, if any, associated with the current Data.
	 */

	public Projection getProjection() {
		return boxer.getProjection();
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
	public GregorianCalendar getDate(long timestep) {
		GregorianCalendar aGregorianCalendar = ((TimeCoordAxis)timeAxis).getDate((int) timestep);
		if (timeAxis != null) return aGregorianCalendar; // ((TimeCoordAxis)timeAxis).getDate((int) timestep);
		return null;
	}

	/**
	 * Gets the starting date of the time axis.
	 *
	 * @return the starting date of the time axis.
	 */
	public GregorianCalendar getStartDate() {
		if (timeAxis != null) return getDate(timeAxis.getRange().getOrigin());
		return null;
	}

	/**
	 * Gets the ending date of the time axis.
	 *
	 * @return the ending date of the time axis.
	 */
	public GregorianCalendar getEndDate() {
		if (timeAxis != null) {
			Range range = timeAxis.getRange();
			return getDate(range.getOrigin() + (int)range.getExtent() - 1);
		}
		return null;
	}

	/**
	 * Gets the timestep that corresponds to the specified date. If no such
	 * timestep is found then return TIME_STEP_NOT_FOUND.
	 *
	 * @param date the date whose time step we want
	 * @return the timestep that corresponds to the specified date. If no such
	 * timestep is found then return TIME_STEP_NOT_FOUND.
	 */
	public int getTimeStep(GregorianCalendar date) {
		if (timeAxis != null) return ((TimeCoordAxis)timeAxis).getTimeStep(date);
		return TIME_STEP_NOT_FOUND;
	}

	/**
	 * Gets all the axis in this Axes object.
	 *
	 * @return all the axis in this Axes object.
	 */
	public List<T> getAxes() {
		return axes;
	}

	/**
	 * Gets meta data for the z or layer axis.
	 *
	 * @return the metadata for the z axis or null if it doesn't exist.
	 */
	public T getZAxis() {
		return layerAxis;
	}

	/**
	 * Gets meta data for the time axis.
	 *
	 * @return the metadata for the time axis or null if it doesn't exist.
	 */
	public T getTimeAxis() {
		return timeAxis;
	}

	/**
	 * Gets meta data for the x-axis.
	 *
	 * @return the metadata for the x-axis or null if it doesn't exist.
	 */
	public T getXAxis() {
		return xAxis;
	}

	/**
	 * Gets meta data for the y-axis.
	 *
	 * @return the metadata for the y-axis or null if it doesn't exist.
	 */
	public T getYAxis() {
		return yAxis;
	}

	public int getTimeStep(Date aDate) {
		if (timeAxis != null) return ((TimeCoordAxis)timeAxis).getTimeStep(aDate);
		return TIME_STEP_NOT_FOUND;
	}
}
