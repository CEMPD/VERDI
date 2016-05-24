package saf.core.ui.dock;

import java.util.ArrayList;
import java.util.List;

import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;

/**
 * Abstract implemention of Group.
 * @author Nick Collier
 *         Date: Jul 10, 2008 4:42:12 PM
 */
public abstract class AbstractGroup implements Group {

  private String id;
  private List<DockableFrame> dockables = new ArrayList<DockableFrame>();

  public AbstractGroup(String id) {
    this.id = id;
  }

  /**
   * Gets the first "normal" Dockable in this group,
   * if any.
   *
   * @return the first "normal" Dockable in this group,
   *         if any.
   */
  public DockableFrame getNormalDockable() {
    for (DockableFrame frame : dockables) {
      DefaultCDockable cDockable = ((DefaultDockableFrame)frame).getDockable();
      if (cDockable.getExtendedMode().equals(ExtendedMode.NORMALIZED)) return frame;
    }
    return null;
  }

  /**
   * Adds a dockable to this group.
   *
   * @param dockable the dockable to add
   */
  public void add(DockableFrame dockable) {
    dockables.add(dockable);
  }

  /**
   * Adds the dockables in this group to the specified list.
   *
   * @param frames the list to add to
   * @return the new list
   */
  public List<DockableFrame> addTo(List<DockableFrame> frames) {
    frames.addAll(dockables);
    return frames;
  }

  /**
   * Gets the dockables in this Group.
   *
   * @return the dockables in this Group.
   */
  public List<DockableFrame> getDockables() {
    return new ArrayList<DockableFrame>(dockables);
  }

  /**
   * Removes the specified dockable from this group.
   *
   * @param dockable the dockable to remove
   * @return true if the the dockable was removed, otherwise false
   */
  public boolean removeDockable(DockableFrame dockable) {
    return dockables.remove(dockable);
  }

  /**
   * Gets the id of this Group.
   *
   * @return the id of this Group.
   */
  public String getID() {
    return id;
  }
}
