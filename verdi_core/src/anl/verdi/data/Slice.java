package anl.verdi.data;

/**
 * Describes a slice along 4 dimensions. Not all the
 * ranges need to be set.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Slice {

	private Range timeRange, layerRange, xRange, yRange;

	/**
	 * Sets the time range of this Slice.
	 *
	 * @param origin the origin of the time range
	 * @param extent the extent of the time range
	 */
	public void setTimeRange(int origin, int extent) {
		timeRange = new Range(origin, extent);
	}

	/**
	 * Sets the time range of this Slice.		// 2014 added setTimeRange from long data types, part of change from Date to GregorianCalendar
	 *
	 * @param origin the origin of the time range
	 * @param extent the extent of the time range
	 */
	public void setTimeRange(long origin, long extent) {
		timeRange = new Range(origin, extent);
	}
	
	/**
	 * Sets the time range of this Slice.
	 *
	 * @param range the time range
	 */
	public void setTimeRange(Range range) {
		timeRange = new Range(range);
	}

	/**
	 * Sets the layer range of this Slice.
	 *
	 * @param origin the origin of the layer range
	 * @param extent the extent of the layer range
	 */
	public void setLayerRange(int origin, int extent) {
		layerRange = new Range(origin, extent);
	}

	/**
	 * Sets the layer range of this Slice.
	 *
	 * @param range the layer range
	 */
	public void setLayerRange(Range range) {
		layerRange = new Range(range);
	}

	/**
	 * Sets the x range of this Slice.
	 *
	 * @param origin the origin of the x range
	 * @param extent the extent of the x range
	 */
	public void setXRange(int origin, int extent) {
		xRange = new Range(origin, extent);
	}

	/**
	 * Sets the x range of this Slice.
	 *
	 * @param range the x range
	 */
	public void setXRange(Range range) {
		xRange = new Range(range);
	}

	/**
	 * Sets the y range of this Slice.
	 *
	 * @param range the y range
	 */
	public void setYRange(Range range) {
		yRange = new Range(range);
	}

	/**
	 * Sets the y range of this Slice.
	 *
	 * @param origin the origin of the y range
	 * @param extent the extent of the y range
	 */
	public void setYRange(int origin, int extent) {
		yRange = new Range(origin, extent);
	}


	/**
	 * Gets the range of slice along the layer dimension.
	 *
	 * @return the range of slice along the layer dimension. May be null.
	 */
	public Range getLayerRange() {
		return layerRange;
	}

	/**
	 * Gets the range of slice along the time dimension.
	 *
	 * @return the range of slice along the time dimension. May be null.
	 */
	public Range getTimeRange() {
		return timeRange;
	}

	/**
	 * Gets the range of slice along the x dimension.
	 *
	 * @return the range of slice along the x dimension. May be null.
	 */
	public Range getXRange() {
		return xRange;
	}

	/**
	 * Gets the range of slice along the y dimension.
	 *
	 * @return the range of slice along the y dimension. May be null.
	 */
	public Range getYRange() {
		return yRange;
	}
}
