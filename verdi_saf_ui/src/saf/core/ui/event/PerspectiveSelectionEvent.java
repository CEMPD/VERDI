package saf.core.ui.event;

import saf.core.ui.dock.Perspective;
import saf.core.ui.dock.DockingManager;

import java.util.EventObject;

/**
 * Event fired when a perspective is selected.
 *
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:52:43 $
 */
public class PerspectiveSelectionEvent extends EventObject {

	private Perspective oldPerspective, newPerspective;

	/**
	 * Creates a PerspectiveSelectionEvent from the specificed DockingManager, and old and new perspectives.
	 *
	 * @param source the source of the selection
	 * @param oldPerspective the prior selected perspective (may be null)
	 * @param newPerspective the selected perspective
	 */
	public PerspectiveSelectionEvent(DockingManager source, Perspective oldPerspective, Perspective newPerspective) {
		super(source);
		this.oldPerspective = oldPerspective;
		this.newPerspective = newPerspective;
	}

	/**
	 * Gets the DockingManager that fired this PerspectiveSelectionEvent.
	 *
	 * @return the DockingManager that fired this PerspectiveSelectionEvent.
	 */
	public DockingManager getDockingManager() {
		return (DockingManager)source;
	}

	/**
	 * Gets the perspective that was selected prior to the current selection.
	 *
	 * @return the perspective that was selected prior to the current selection.
	 */
	public Perspective getPriorPerspective() {
		return oldPerspective;
	}

	/**
	 * Gets the selected perspective.
	 *
	 * @return the selected perspective.
	 */
	public Perspective getSelectedPerspective() {
		return newPerspective;
	}

	public String toString() {
		String properties =
	    " source=" + getSource() +
            " selectedPerspective = " + newPerspective +
            " priorPerspective = " + oldPerspective +
            " ";
        return getClass().getName() + "[" + properties + "]";
	}
}
