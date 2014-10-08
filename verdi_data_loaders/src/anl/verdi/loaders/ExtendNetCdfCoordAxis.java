package anl.verdi.loaders;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.dataset.CoordinateAxis;
import anl.verdi.data.AxisType;
import anl.verdi.data.ExtendCoordAxis;
import anl.verdi.data.Range;
import anl.verdi.util.VUnits;

public class ExtendNetCdfCoordAxis implements ExtendCoordAxis {
	static final Logger Logger = LogManager.getLogger(ExtendNetCdfCoordAxis.class.getName());

	private Range range;
	protected CoordinateAxis axis;
	private AxisType type;
	private Array values;
	private Unit unit;

	public ExtendNetCdfCoordAxis(CoordinateAxis axis, AxisType type) {
		this.axis = axis;
		// breaking the next statement into components; trying to avoid conversion between unit libraries
		//this.unit = Units.createUnit(axis.getUnitsString());
		String unitString = axis.getUnitsString();
		this.unit = VUnits.createUnit(unitString);
		Logger.debug("in ExtendNetCdfCoordAxis constructor, unit = " + this.unit);
		this.type = type;
		this.range = new Range(0, axis.getSize());
	}

	/**
	 * Gets the value at the specified index.
	 *
	 * @return the value at the specified index.
	 */
	public Object getValue(int index) {
		if (values == null) try {
			values = axis.read();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Index arrayIndex = values.getIndex();
		arrayIndex.set(index);
		return values.getObject(arrayIndex); //.getDouble(arrayIndex);
	}

	/**
	 * Gets the unit of measurement for this coordinate axis.
	 *
	 * @return the unit of measurement for this coordinate axis.
	 */
	public Unit getUnits() {
		Logger.debug("in ExtendNetCdfCoordAxis.getUnits, unit = " + unit);
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
		if ( axis.getRange() != null) {
			long extent = axis.getRange().getExtent();
			if (axis.getAxisType().equals(this.getAxisType()) &&
					extent == this.getRange().getExtent()) {
				// Liz_20111107
				//			for (int i = 0; i < extent; i++) {
				//				double v1 = getValue(i);
				//				double v2 = axis.getValue(i);
				//				if ( v1 != v2) return false;
				//			}
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
		return axis.getDescription();
	}

	/**
	 * Gets the name of this CoordAxis.
	 *
	 * @return the name of this CoordAxis.
	 */
	public String getName() {
		return axis.getShortName();		// getName is deprecated, use getShortName or getFullName (full name with backslash escapes)
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