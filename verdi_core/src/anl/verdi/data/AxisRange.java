package anl.verdi.data;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

/**
 * Defines a range along some coordinate axis.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class AxisRange implements CoordAxis {

	protected CoordAxis axis;
	protected Range range;

	/**
	 * Creates an AxisRange for the specified axis with origin and extent
	 * equal to that of the axis.
	 *
	 * @param axis the axis
	 */
	public AxisRange(CoordAxis axis) {
		this(axis, (int) axis.getRange().getOrigin(), (int)axis.getRange().getExtent());
	}

	/**
	 * Creates an AxisRange for the specified axis with the specified origin and extent.
	 *
	 * @param origin the origin of the range
	 * @param extent the extent of the range
	 * @param axis the axis
	 */
	public AxisRange(CoordAxis axis, int origin,  int extent) {
		this.axis = axis;
		this.range = new Range(origin, extent);
	}

	/**
	 * Gets the coordinate axis for which this is a range.
	 *
	 * @return the coordinate axis for which this is a range.
	 */
	public CoordAxis getAxis() {
		return axis;
	}


	/**
	 * Gets the unit of measurement for this coordinate axis.
	 *
	 * @return the unit of measurement for this coordinate axis.
	 */
	public Unit getUnits() {
		return axis.getUnits();
	}


	/**
	 * Gets whether or not the this axis is compatible with
	 * the specified axis. Two axes are compatible if they
	 * can be used together in the same formula.
	 *
	 * @param axis the axis to check for compatibility
	 * @return true if this axis is compatible with the other axis,
	 *         otherwise false.
	 */
	public boolean isCompatible(CoordAxis axis) {
		if (this.axis.isCompatible(axis)) {
			Range otherRange = axis.getRange();
			long otherLimit = otherRange.origin + otherRange.extent;
			long thisLimit = range.origin + range.extent;
			return range.origin >= otherRange.origin &&
							range.origin <= otherLimit &&
							thisLimit >= otherRange.origin &&
							thisLimit <= otherLimit;
		}
		return false;
	}

	/**
	 * Gets the value at the specified index.
	 *
	 * @param index the index
	 * @return the value at the specified index.
	 */
	public double getValue(int index) {
		return axis.getValue(index);
	}

	/**
	 * Gets the description of this CoordAxis.
	 *
	 * @return the description of this CoordAxis.
	 */
	public String getDescription() {
		return axis.getDescription();
	}

	/**
	 * Gets the name of this CoordAxis.
	 *
	 * @return the name of this CoordAxis.
	 */
	public String getName() {
		return axis.getName();
	}

	/**
	 * Gets the range of this axis.
	 *
	 * @return the range of this axis.
	 */
	public Range getRange() {
		return range;
	}

	/**
	 * Gets the extent of the range.
	 *
	 * @return the extent of the range.
	 */
	public int getExtent() {
		return (int)range.getExtent();
	}

	/**
	 * Gets the origin of the range.
	 *
	 * @return the origin of the range.
	 */
	public int getOrigin() {
		return (int) range.getOrigin();
	}

	/**
	 * Gets the type of this axis.
	 *
	 * @return the type of this axis.
	 */
	public AxisType getAxisType() {
		return axis.getAxisType();
	}
}
