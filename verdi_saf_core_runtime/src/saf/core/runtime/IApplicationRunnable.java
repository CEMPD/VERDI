package saf.core.runtime;

/**
 * Interface for the application entry point. Boot will
 * attempt to find a plugin class that implements this interface
 * and then will call the run method.
 * 
 * @author Nick Collier
 * @version $Revision: 1.3 $ $Date: 2006/02/07 20:39:51 $
 */
public interface IApplicationRunnable {

	/**
	 * Runs the application. This is the equivalent of 
	 * main(String[] args).
	 * 
	 * @param args the arguments to the application
	 */
	public void run(String[] args);

}
