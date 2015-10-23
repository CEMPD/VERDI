package anl.verdi.data;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;


/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface Variable<T extends CoordAxis> {
	
	/**
 * Get the description of a variable
	 * @return the description
	 */
	String getDescription();

	/**
 * Get the name of the variable
	 * @return the name
	 */
	String getName();

	/**
	 * Gets this Variable's standard of measurement.
	 * 
	 * @return this Variable's standard of measurement.
	 */
	Unit getUnit();

	/**
	 * Gets the dataset in which this variable appears. This
	 * may be null if the Variable is a composite composed of
	 * multiple datasets.
	 *
	 * @return the dataset in which this variable appears.
	 */
	Dataset getDataset();
}
