package anl.verdi.loaders;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.unitsofmeasurement.unit.Unit;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.dataset.CoordinateAxis;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Range;
import anl.verdi.util.VUnits;

/**
 * CoordAxis that can be built from net cdf specific variables.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class NetCdfCoordAxis implements CoordAxis {
	static final Logger Logger = LogManager.getLogger(NetCdfCoordAxis.class.getName());

	private Range range;
	protected CoordinateAxis axis;
	private AxisType type;
	private Array values;
	private Unit javaxUnit;

	public NetCdfCoordAxis(CoordinateAxis axis, AxisType type) {
		this.axis = axis;
		this.javaxUnit = VUnits.createUnit(axis.getUnitsString());	// calls createUnit in anl.verdi.util.Unit
		Logger.debug("in NetCdfCoordAxis constructor, javaxUnit = " + this.javaxUnit);
		this.type = type;
		this.range = new Range(0, axis.getSize());
	}

	/**
	 * Gets the value at the specified index.
	 *
	 * @return the value at the specified index.
	 */
	public double getValue(int index) {
		if (values == null) try {
			values = axis.read();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		Index arrayIndex = values.getIndex();
		arrayIndex.set(index);
		return values.getDouble(arrayIndex);
	}

	/**
	 * Gets the unit of measurement for this coordinate axis.
	 *
	 * @return the unit of measurement for this coordinate axis.
	 */
	public Unit getUnits() {
		Logger.debug("NetCdfCoordAxis.getUnits = " + javaxUnit);
		return javaxUnit;
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
			// Liz_20111107
//			for (int i = 0; i < extent; i++) {
//				double v1 = getValue(i);
//				double v2 = axis.getValue(i);
//				if ( v1 != v2) return false;
//			}
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
//		return axis.getName();		// getName deprecated, use either getShortName or getLongName
		return axis.getShortName();
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
	
	public String getDatasetDimension() {
		return axis.getDimensionsString();
	}

}