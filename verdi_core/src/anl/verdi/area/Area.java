package anl.verdi.area;


/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface Area<T> {

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
	 * Gets the area file in which this variable appears. 
	 *
	 * @return the area file in which this variable appears.
	 */
	AreaFile getAreaFile();
}
