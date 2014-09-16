package anl.verdi.loaders;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import anl.verdi.data.AxisType;
import anl.verdi.data.ExtendCoordAxis;
import anl.verdi.data.Range;
import anl.verdi.util.VUnits;

public class ExtendCSVCoordAxis implements ExtendCoordAxis {

	private Range range;
	private AxisType type;
	private Unit unit;
	protected String name, description;
	protected Object data[];
	
	public ExtendCSVCoordAxis(Object data[], String name, String description, AxisType type) {
		this.name = name;
		this.description = description;
//		this.unit = Units.MISSING_UNIT;		// 2014 split into 2 statements to not get error message assigning between different Unit packages
		Unit tmpUnit = VUnits.MISSING_UNIT;
		this.unit = tmpUnit;
		this.type = type;
		this.range = new Range(0, data.length);
		this.data = data;
	}

	/**
	 * Gets the value at the specified index.
	 *
	 * @return the value at the specified index.
	 */
	public Object getValue(int index) {
		
		return data[index];
	}

	/**
	 * Gets the unit of measurement for this coordinate axis.
	 *
	 * @return the unit of measurement for this coordinate axis.
	 */
	public Unit getUnits() {
		return unit;
	}


	/**
	 * Gets whether or not the this axis is compatible with
	 * the specified axis. Two axes are compatible if they
	 * can be used together in the same formula.
	 *
	 * @param axis the axis to check for compatibility
	 * @return true if this axis is compatibilty with the other axis,
	 *         otherise false.
	 */
	public boolean isCompatible(ExtendCoordAxis axis) {
		// identity test
		if (axis == this) return true;
		if ( axis.getRange() != null ) {
			long extent = axis.getRange().getExtent();
			if (axis.getAxisType().equals(this.getAxisType()) &&
					extent == this.getRange().getExtent()) {
				for (int i = 0; i < extent; i++) {
					if (getValue(i) != axis.getValue(i)) return false;
				}
				return true;
			}
		} else {
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
