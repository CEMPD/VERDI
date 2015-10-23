package anl.verdi.util;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface BinaryDoubleFunction {

	/**
	 * Apply some function to the specified doubles and return
	 * the result. For example, addition.
	 *
	 * @param val1 the first value to operation on
	 * @param val2 the second value to operation on
	 *
	 * @return the result of the operation.
	 */
	public double apply(double val1, double val2);
}
