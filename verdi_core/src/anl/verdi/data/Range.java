package anl.verdi.data;

/**
 * class that stores a range of values along a coordinate axis
 *
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 * @see Range
 */
public class Range {
	/**
	 * the initial cell in the set of values
	 */
	protected long origin;		// changed int => long to handle time axis correctly 2014
	/**
	 * the extent or number of cells in the set of values
	 */
	protected long extent;

	/**
	 * Constructs a range for a given set of values
	 *
	 * @param
	 */
	public Range(long origin, long extent) {
		this.origin = origin;
		this.extent = extent;
	}

	/**
	 * Copy constructor.
	 *
	 * @param range
	 */
	public Range(Range range) {
		this.origin = range.origin;
		this.extent = range.extent;
	}

	/**
	 * Gets the lower bound of this range.
	 *
	 * @return the lower bound of this range.
	 */
	public long getLowerBound() {
		return origin;
	}

	/**
	 * Gets upper bound of this range.
	 *
	 * @return upper bound of this range.
	 */
	public long getUpperBound() {
		return origin + extent - 1;
	}

	/**
	 * Get the extent of the cells in the set of values
	 *
	 * @return the extent of the cells
	 */
	public long getExtent() {
		return extent;
	}

	/**
	 * Set the extent of the cells in the set of values
	 *
	 * @param extent of the cells desired
	 */
	public void setExtent(long extent) {
		this.extent = extent;
	}

	/**
	 * Get the origin or initial location of the cells in the set of values
	 *
	 * @return the origin of the cells
	 */
	public long getOrigin() {		// 2014 changed int to long
		return origin;
	}

	/**
	 * Set the origin or initial location of the cells in the set of values
	 *
	 * @param origin of the cells desired
	 */
	public void setOrigin(long origin) {
		this.origin = origin;
	}

	public String toString() {
		return "Range: " + origin + " " + extent;
	}

	public int hashCode() {
		int result = 17;
		result = 37 * result + (int)origin;		// problem? if using time
		result = 37 * result + (int) (extent ^ (extent >>> 32));
		return result;
	}

	public boolean equals(Object other) {
		if (other instanceof Range) {
			Range otherRange = (Range) other;
			return this.origin == otherRange.origin && this.extent == otherRange.extent;
		}
		return false;
	}
}
