package anl.verdi.util;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface DoubleFunction {

	/**
	 * Apply some function to the specified double and return
	 * the result.
	 *
	 * @param val the value to operation on
	 * @return the result of the operation.
	 */
	public double apply(double val);
}
