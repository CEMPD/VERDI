package saf.core.runtime;

/**
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:55:38 $
 */
public class PluginDefinitionException extends Exception {

	public PluginDefinitionException() {
		super();
	}

	public PluginDefinitionException(String message) {
		super(message);
	}

	public PluginDefinitionException(String message, Throwable cause) {
		super(message, cause);
	}

	public PluginDefinitionException(Throwable cause) {
		super(cause);
	}
}
