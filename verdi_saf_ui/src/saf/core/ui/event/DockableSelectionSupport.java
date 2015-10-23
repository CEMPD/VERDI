package saf.core.ui.event;

import bibliothek.gui.dock.common.event.CFocusListener;
import bibliothek.gui.dock.common.intern.CDockable;
import saf.core.ui.dock.DefaultDockingManager;
import saf.core.ui.dock.DockableFrame;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility support class for managing dockable frame
 * selection events. For this to be effective it must be
 * added as CFocusListener to a CControl.
 *
 * @author Nick Collier
 */
public class DockableSelectionSupport implements CFocusListener {

  private DefaultDockingManager manager;

  public DockableSelectionSupport(DefaultDockingManager manager) {
    this.manager = manager;
  }

  private List<DockableSelectionListener> listeners = new ArrayList<DockableSelectionListener>();

  public void addSelectionListener(DockableSelectionListener listener) {
    listeners.add(listener);
  }

  public void removeSelectionListener(DockableSelectionListener listener) {
    listeners.remove(listener);
  }

  /**
   * Called when a dockable gains focus.
   *
   * @param dockable the dockable that gained focus
   */
  public void focusGained(CDockable dockable) {
    DockableSelectionEvent evt = createEvent(dockable, DockableSelectionEvent.Type.GAINED);
    DockableFrame frame = evt.getDockable();
    // frame can be null if its the "invisible" working area that gets
    // the focus
    if (frame != null) {
      manager.getBarManager().selectMenuItem(frame.getID());
      for (DockableSelectionListener listener : listeners) {
        listener.selectionGained(evt);
      }
    }
  }

  /**
   * Called when a dockable looses focus.
   *
   * @param dockable the dockable that has lost focus
   */
  public void focusLost(CDockable dockable) {
    DockableSelectionEvent evt = createEvent(dockable, DockableSelectionEvent.Type.LOST);
    // frame can be null if its the "invisible" working area that gets
    // the focus
    if (evt.getDockable() != null) {
      for (DockableSelectionListener listener : listeners) {
        listener.selectionLost(evt);
      }
    }

  }

  private DockableSelectionEvent createEvent(CDockable dockable, DockableSelectionEvent.Type type) {
    DockableFrame frame = manager.getDockableFrameFor(dockable);
    return new DockableSelectionEvent(frame, type);
  }
}
