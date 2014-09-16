package saf.core.ui.event;

/**
 * Interface for classes that want to be notified when the
 * dockable frame selection changes.
 *
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:55:17 $
 */
public interface DockableSelectionListener {

	/**
	 * Called whenever dockable frame is selected.
	 *
	 * @param evt the event describing the selection
	 */
	void selectionGained(DockableSelectionEvent evt);


  /**
   * Called whenever a dockable frame looses selection.
   *
   * @param evt the event describing the selection
   */
  void selectionLost(DockableSelectionEvent evt);
}
