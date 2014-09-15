package anl.verdi.data;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.*;


/**
 * Interface for classes implementing Extended axis metadata: not only x,y,z,time.
 *
 * @author Jizhen (Jason) Zhao
 * @version $Revision$ $Date$
 */

public interface ExtendCoordAxis {

	/**
	 * Gets the name of this CoordAxis.
	 *
	 * @return the name of this CoordAxis.
	 */
	String getName();

	/**
	 * Gets the description of this CoordAxis.
	 *
	 * @return the description of this CoordAxis.
	 */
	String getDescription();

	/**
	 * Disable this option: some dim does not has a range
	 * Gets the range of this axis.
	 *
	 * @return the range of this axis.
	 */
	Range getRange();

	/**
	 * Gets the type of this axis.
	 *
	 * @return the type of this axis.
	 */
	AxisType getAxisType();

	/**
	 * Gets the value at the specified index.
	 *
	 * @param index the index 
	 * @return the value at the specified index.
	 */
	Object getValue(int index);

	/**
	 * Gets the unit of measurement for this coordinate axis.
	 *
	 * @return the unit of measurement for this coordinate axis.
	 */
	Unit getUnits();

	/**
	 * Gets whether or not the this axis is compatible with
	 * the specified axis. Two axes are compatible if they
	 * can be used together in the same formula.
	 *
	 * @param axis the axis to check for compatibility
	 * @return true if this axis is compatibilty with the other axis,
	 * otherise false.
	 */
	boolean isCompatible(ExtendCoordAxis axis);

}
