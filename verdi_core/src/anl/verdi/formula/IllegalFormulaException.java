package anl.verdi.formula;

/**
 * Signals a problem with a formula, either in its construction or execution. 
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class IllegalFormulaException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 652045747371530852L;

	public IllegalFormulaException() {
		super();
	}

	public IllegalFormulaException(Throwable cause) {
		super(cause);
	}

	public IllegalFormulaException(String message) {
		super(message);
	}

	public IllegalFormulaException(String message, Throwable cause) {
		super(message, cause);
	}
}
