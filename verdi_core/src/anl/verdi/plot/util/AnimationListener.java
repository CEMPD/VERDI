package anl.verdi.plot.util;

/**
 * Interface for classes that want to listen for animation events.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface AnimationListener {

	/**
	 * Called when the animation is stopped.
	 * 
	 */
	void animationStopped();
}
