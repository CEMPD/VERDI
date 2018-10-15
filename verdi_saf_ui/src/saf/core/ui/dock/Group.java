package saf.core.ui.dock;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.intern.DefaultCDockable;

import java.util.List;

/**
 * A docking group.
 *
 * @author Nick Collier
 */
public interface Group {

  /**
   * Gets the first "normal" Dockable in this group,
   * if any.
   *
   * @return the first "normal" Dockable in this group,
   *         if any.
   */
  DockableFrame getNormalDockable();

  /**
   * Gets the default location of this Group.
   *
   * @return the location of this Group.
   */
  CLocation getLocation();

  /**
   * Adds the dockables in this group to the specified list.
   *
   * @param frames the list to add to
   *
   * @return the new list
   *
   */
  List<DockableFrame> addTo(List<DockableFrame> frames);

  /**
   * Gets the dockables in this Group.
   * 
   * @return the dockables in this Group.
   */
  List<DockableFrame> getDockables();

  /**
   * Adds a dockable to this group.
   *
   * @param dockable the dockable to add
   */
  void add(DockableFrame dockable);

  /**
   * Gets the id of this Group.
   *
   * @return the id of this Group.
   */
  String getID();

  /**
   * Removes the specified dockable from this group.
   *
   * @param dockable the dockable to remove
   *
   * @return true if the the dockable was removed, otherwise false
   */
  boolean removeDockable(DockableFrame dockable);

}
