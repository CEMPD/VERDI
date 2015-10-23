/*CopyrightHere*/
package simphony.util.error;

/**
 * A handler used by {@link simphony.util.error.ErrorCenter} to handle errors.
 * 
 * @author Jerry Vos
 * @version $Revision: 1.2 $ $Date: 2005/11/10 23:24:25 $
 */
public interface ErrorHandler {
	/**
	 * Attempts to handle an error.
	 * 
	 * @param event
	 *            an error event with information about the error that occurred
	 * @return whether or not the error was handled
	 */
	public boolean handleError(ErrorEvent event);
}
