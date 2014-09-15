package anl.verdi.plot.config;

/**
 * Interface for classes that can convert a Class to and from a String representation.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface StringConvertor<T> {

	/**
	 * Convert the specified Object to a String representation.
	 *
	 * @param obj the object to convert
	 * @return a String representation of the object
	 */
	String convertToString(Object obj);

	/**
	 * Converts the specified String into the appropriate Object.
	 *
	 * @param str the string to convert
	 * @return the Object represented by the String.
	 */
	T convertFromString(String str);
}
