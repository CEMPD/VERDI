package saf.core.ui.dock;

import bibliothek.gui.dock.common.location.CBaseLocation;
import bibliothek.gui.dock.common.location.TreeLocationNode;
import bibliothek.gui.dock.common.location.TreeLocationRoot;
import bibliothek.gui.dock.common.location.AbstractTreeLocation;

/**
 * An element in a GroupPath. This defines the location
 * and size of a group space with respect to its parent and can produce
 * the correct CLocation.
 *
 * @author Nick Collier
 */
public class GroupPathElement {

  private Location groupLocation;
  private float fill;
  private String id;

  /**
   * Creates a GroupPathElement with the specified location and fill.
   *
   * @param id the id of the group
   * @param location the relative location of the group
   * @param fill the fill percentage of this group
   */
  public GroupPathElement(String id, Location location, float fill) {
    this.groupLocation = location;
    this.fill = fill;
    this.id = id;
  }

  /**
   * Gets the id for the group that this is a path element for.
   *
   * @return the id for the group that this is a path element for.
   */
  public String getID() {
    return id;
  }

  /**
   * Adds to the path encapsulated by the specified CBaseLocation.
   *
   * @param location the CBaseLocation to create the path from
   * @return a TreeLocationRoot that encapsulates the new path
   */
  public TreeLocationRoot createPath(CBaseLocation location) {
    return groupLocation.createPath(location, fill);
  }

  /**
   * Adds to the path encapsulated by the specified AbstractTreeLocation.
   *
   * @param location the AbstractTreeLocation to create the path from
   * @return a new TreeLocationNode that encapsulates the new path
   */
  public TreeLocationNode createPath(AbstractTreeLocation location) {
    return groupLocation.createPath(location, fill);
  }
}
