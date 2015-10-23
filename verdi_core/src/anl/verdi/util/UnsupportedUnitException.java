package anl.verdi.util;

/**
 * Signals that a paricular unit representation is unsupported.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class UnsupportedUnitException extends Exception {

	public UnsupportedUnitException() {
		super();
	}

	public UnsupportedUnitException(Throwable cause) {
		super(cause);
	}

	public UnsupportedUnitException(String message) {
		super(message); 
	}

	public UnsupportedUnitException(String message, Throwable cause) {
		super(message, cause);
	}
}
