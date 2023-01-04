/**
 * CoordAxis that can be built from net cdf specific variables.
 *
 * @author Tony Howard
 * @version $Revision$ $Date$
 */

package anl.verdi.loaders;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.unitsofmeasurement.unit.Unit;

import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Range;
import anl.verdi.util.VUnits;


public class BCONCoordAxis implements CoordAxis {

	static final Logger Logger = LogManager.getLogger(BCONCoordAxis.class.getName());
	private Range range;
	private AxisType type;
	@SuppressWarnings("rawtypes")
	private Unit unit;
	protected String name, description;
	private double minValue;
	private int length;
	
	public BCONCoordAxis(String name, String description, double min, AxisType type) {
		this.name = name;
		this.description = description;
		this.unit = VUnits.MISSING_UNIT;
		this.type = type;
		
		this.minValue = min;
		
		if (type == AxisType.X_AXIS) {
			this.minValue = -180;
			length = 361;
		}
		else if (type == AxisType.Y_AXIS) {
			this.minValue = -90;
			length = 181;
		}
		
		this.range = new Range(Math.round(this.minValue), length);
		Logger.debug("in CSVCoordAxis constructor, unit = " + this.unit);
	}

	/**
	 * Gets the value at the specified index.
	 *
	 * @return the value at the specified index.
	 */
	public double getValue(int index) {
		if (type == AxisType.X_AXIS)
			return index;
		else if (type == AxisType.Y_AXIS)
			return index;
		return 0;
	}

	/**
	 * Gets the unit of measurement for this coordinate axis.
	 *
	 * @return the unit of measurement for this coordinate axis.
	 */
	@SuppressWarnings("rawtypes")
	public Unit getUnits() {
		Logger.debug("in CSVCoordAxis.getUnits, unit = " + unit);
		return unit;
	}


	/**
	 * Gets whether or not the this axis is compatible with
	 * the specified axis. Two axes are compatible if they
	 * can be used together in the same formula.
	 *
	 * @param axis the axis to check for compatibility
	 * @return true if this axis is compatibility with the other axis,
	 *         Otherwise false.
	 */
	public boolean isCompatible(CoordAxis axis) {
		// identity test
		if (axis == this) return true;
		long extent = axis.getRange().getExtent();
		if (axis.getAxisType().equals(this.getAxisType()) &&
						extent == this.getRange().getExtent()) {
			for (int i = 0; i < extent; i++) {
				if (getValue(i) != axis.getValue(i)) return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Gets the description of this CoordAxis.
	 *
	 * @return the description of this CoordAxis.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the name of this CoordAxis.
	 *
	 * @return the name of this CoordAxis.
	 */
	public String getName() {
		return name;
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
	 * Gets the type of this axis.
	 *
	 * @return the type of this axis.
	 */
	public AxisType getAxisType() {
		return type;
	}
	
	
}
