package anl.verdi.loaders;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.unitsofmeasurement.unit.Unit;

import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Range;
import anl.verdi.util.VUnits;

/**
 * CoordAxis that can be built from net cdf specific variables.
 *
 * @author Nick Collier
 * @author Eric Tatara
 * @version $Revision$ $Date$
 */
public class CSVCoordAxis implements CoordAxis {

	static final Logger Logger = LogManager.getLogger(CSVCoordAxis.class.getName());
	private Range range;
	private AxisType type;
	private Unit unit;
	protected String name, description;
	protected Double data[];
	
	public CSVCoordAxis(Double data[], String name, String description, AxisType type) {
		this.name = name;
		this.description = description;
		this.unit = VUnits.MISSING_UNIT;
		this.type = type;
		this.range = new Range(0, data.length);
		this.data = data;
		Logger.debug("in CSVCoordAxis constructor, unit = " + this.unit);
	}

	/**
	 * Gets the value at the specified index.
	 *
	 * @return the value at the specified index.
	 */
	public double getValue(int index) {
		
		return data[index];
	}

	/**
	 * Gets the unit of measurement for this coordinate axis.
	 *
	 * @return the unit of measurement for this coordinate axis.
	 */
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
