/*CopyrightHere*/
package simphony.util.error;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Jerry Vos
 * @version $Revision: 1.5 $ $Date: 2005/11/14 21:58:40 $
 */
public class ErrorCenter {
	// TODO: decide about the static business
	private static List<ErrorHandler> errorHandlers = new ArrayList<ErrorHandler>();

	/**
	 * Gets an ErrorCenter.
	 * 
	 * @return
	 */
	public static ErrorCenter getErrorCenter() {
		return new ErrorCenter();
	}

	private ErrorCenter() {
	}

	/**
	 * Fires an error event with the specified error information.
	 * 
	 * @param errorCode
	 *            a code for the error
	 * @param info
	 *            information on the error
	 * @param error
	 *            an error object (useful for getting a stack trace)
	 * @param metaData
	 *            extra meta data about the erro
	 * @return if the error was handled or not by one of the ErrorHandlers
	 */
	public boolean error(int errorCode, Object info, Throwable error,
			Object... metaData) {
		return fireErrorEvent(errorCode, info, error, metaData);
	}

	/**
	 * Fires an error event with the specified error information.
	 * 
	 * @param errorCode
	 *            a code for the error
	 * @param info
	 *            information on the error
	 * @param error
	 *            an error object (useful for getting a stack trace)
	 * @param metaData
	 *            extra meta data about the erro
	 * @return if the error was handled or not by one of the ErrorHandlers
	 */
	public boolean fireErrorEvent(int errorCode, Object info, Throwable error,
			Object... metaData) {

		ErrorEvent event = new ErrorEvent(error, errorCode, info, metaData);

		boolean handled = false;
		synchronized (errorHandlers) {
			for (ErrorHandler listener : errorHandlers) {
				if (listener.handleError(event)) {
					handled = true;
				}
			}
		}

		if (!handled) {
			if (error instanceof RuntimeException) {
				throw (RuntimeException) error;
			} else {
				throw new RuntimeException(error);
			}
		}

		return handled;
	}

	/**
	 * Adds an ErrorHandler.
	 * 
	 * @param handler
	 *            the ErrorHandler to add
	 */
	public void addErrorHandler(ErrorHandler handler) {
		synchronized (errorHandlers) {
			errorHandlers.add(handler);
		}
	}

	/**
	 * Removes an ErrorHandler.
	 * 
	 * @param handler
	 *            the ErrorHandler to remove
	 */
	public void removeErrorHandler(ErrorHandler handler) {
		synchronized (errorHandlers) {
			errorHandlers.remove(handler);
		}
	}

	/**
	 * Retrieves the ErrorHandlers this ErrorCenter contains.
	 * 
	 * @return the ErrorHandlers this ErrorCenter contains
	 */
	public Collection<ErrorHandler> getErrorHandlers() {
		return Collections.unmodifiableCollection(errorHandlers);
	}
}
