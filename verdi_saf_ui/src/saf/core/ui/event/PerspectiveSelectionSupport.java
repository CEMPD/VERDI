package saf.core.ui.event;

import saf.core.ui.dock.Perspective;
import saf.core.ui.dock.DockingManager;

import java.util.List;
import java.util.ArrayList;

/**
 * Utility support class for firing perspective related events
 * to perspective selection listeners.
 *
 * @author Nick Collier
 */
public class PerspectiveSelectionSupport {

  private List<PerspectiveSelectionListener> listeners = new ArrayList<PerspectiveSelectionListener>();
  private DockingManager manager;

  /**
   * Creates a PerspectiveSelectionSupport that will support the
   * selection of perspectives manager by the specified manager.
   *
   * @param manager the manager whose perspectives this class works with
   */
  public PerspectiveSelectionSupport(DockingManager manager) {
    this.manager = manager;
  }

  /**
   * Adds a PerspectiveSelectionListener to the list of listeners to notify
   * when the perspective changes.
   *
   * @param listener the listener to add
   */
  public void addPerspectiveListener(PerspectiveSelectionListener listener) {
    listeners.add(listener);
  }

  /**
   * Removes a PerspectiveSelectionListener from the list of listeners to notify
   * when the perspective changes.
   *
   * @param listener the listener to remove
   */
  public void removePerspectiveListener(PerspectiveSelectionListener listener) {
    listeners.remove(listener);
  }

  /**
   * Fires a perspective changing notification to all registered listeners.
   *
   * @param currentPerspective the current perspective that will become the old perspective
   * @param newPerspective the perspectve that will be the newly selected perspective
   */
  public void firePerpectiveChanging(Perspective currentPerspective, Perspective newPerspective) {
    PerspectiveSelectionEvent evt = new PerspectiveSelectionEvent(manager, currentPerspective, newPerspective);
    for (PerspectiveSelectionListener listener : listeners) {
      listener.perspectiveChanging(evt);
    }
  }

  /**
   * Fires a perspective changed notification to all registered listeners.
   *
   * @param priorPerspective the previously selected perspective
   * @param newPerspective the newly selected perspective
   */
  public void firePerpectiveChanged(Perspective priorPerspective, Perspective newPerspective) {
    PerspectiveSelectionEvent evt = new PerspectiveSelectionEvent(manager, priorPerspective, newPerspective);
    for (PerspectiveSelectionListener listener : listeners) {
      listener.perspectiveChanged(evt);
    }
  }
}
