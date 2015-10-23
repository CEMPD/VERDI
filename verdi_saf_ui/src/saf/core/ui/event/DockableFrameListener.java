package saf.core.ui.event;

import saf.core.ui.event.DockableFrameEvent;

/**
 * Listener interface for dockable events. Classes interested in receiveing notification of
 * dockable minimization, maximization, floating, closing and restoration should implement
 * this interface and add themselves to the dockableManager.
 *
 * @author Nick Collier
 */
public interface DockableFrameListener {

	/**
	 * Invoked when a dockable is closed.
	 *
	 * @param evt the detailes of the close event
	 */

	public void dockableClosed(DockableFrameEvent evt);

	/**
	 * Invoked when a dockable receives a close request. The
	 * close can be overriden by setting the events
	 * handled property to true.
	 *
	 * @param evt details the close request
	 */
	public void dockableClosing(DockableFrameEvent evt);

	/**
	 * Invoked when a dockable receives a float request. The
	 * float can be overriden by setting the events
	 * handled property to true.
	 *
	 * @param evt details the float request
	 */
	public void dockableFloating(DockableFrameEvent evt);

	/**
	 * Invoked when a dockable receives a restore request. The
	 * restore can be overriden by setting the events
	 * handled property to true.
	 *
	 * @param evt details the restore request
	 */
	public void dockableRestoring(DockableFrameEvent evt);

  /**
   * Invoked when a dockable receives a minimizing
   * request.  The minimize can be overriden by setting the events
	 * handled property to true.
   *
   * @param evt details the minimize request
   */
  public void dockableMinimizing(DockableFrameEvent evt);

  /**
	 * Invoked when a dockable is minimized.
	 *
	 * @param evt details of the event
	 */
	public void dockableMinimized(DockableFrameEvent evt);

	/**
	 * Invoked when a dockable is floated.
	 *
	 * @param evt details of the event
	 */
	public void dockableFloated(DockableFrameEvent evt);

   /**
   * Invoked when a dockable receives a maxmizing
   * request.  The maximize can be overriden by setting the events
	 * handled property to true.
    *
   * @param evt details the maximize request
   */
  public void dockableMaximizing(DockableFrameEvent evt);

  /**
	 * Invoked when a dockable is maximized.
	 *
	 * @param evt details of the event
	 */
	public void dockableMaximized(DockableFrameEvent evt);

	/**
	 * Invoked when a dockable is restored to its default docking position
	 * from a floated, minimized, or maximized state.
	 *
	 * @param evt details of the event
	 */
	public void dockableRestored(DockableFrameEvent evt);

}
