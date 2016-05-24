package saf.core.ui.event;


/**
 * Interface for classes that want to be notified when the perspective changes.
 *
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:55:17 $
 */
public interface PerspectiveSelectionListener {

	/**
	 * Called whenever the perspective has changed.
	 *
	 * @param evt the event describing the change
	 */
	void perspectiveChanged(PerspectiveSelectionEvent evt);

	/**
	 * Called immediately prior to perspective changing.
	 *
	 * @param evt the event describing the change
	 */
	void perspectiveChanging(PerspectiveSelectionEvent evt);

}
