package saf.core.ui.dock;

import bibliothek.gui.dock.common.location.CBaseLocation;
import bibliothek.gui.dock.common.location.TreeLocationNode;
import bibliothek.gui.dock.common.location.TreeLocationRoot;
import bibliothek.gui.dock.common.location.AbstractTreeLocation;

/**
 * Enum describing a relative group location.
 *
 * @author Nick Collier
 */
public enum Location {

  NORTH() {
    public TreeLocationRoot createPath(CBaseLocation location, float fill) {
      return location.normalNorth(fill);
    }

    public TreeLocationNode createPath(AbstractTreeLocation location, float fill) {
      return location.north(fill);
    }
  },

  SOUTH() {
    public TreeLocationRoot createPath(CBaseLocation location, float fill) {
      return location.normalSouth(fill);
    }

    public TreeLocationNode createPath(AbstractTreeLocation location, float fill) {
      return location.south(fill);
    }
  },

  EAST() {
    public TreeLocationRoot createPath(CBaseLocation location, float fill) {
      return location.normalEast(fill);
    }

    public TreeLocationNode createPath(AbstractTreeLocation location, float fill) {
      return location.east(fill);
    }
  },

  WEST() {
    public TreeLocationRoot createPath(CBaseLocation location, float fill) {
      return location.normalWest(fill);
    }

    public TreeLocationNode createPath(AbstractTreeLocation location, float fill) {
      return location.west(fill);
    }
  };

  /**
   * Create TreeLocationRoot using the CBaseLocation and the fill
   *
   * @param location the CBaseLocation to create the TreeLocationRoot from
   * @param fill     the percent fill
   * @return a TreeLocationRoot
   */
  public abstract TreeLocationRoot createPath(CBaseLocation location, float fill);


  /**
   * Adds to the path encapsulateed by the specified TreeLocationNode.
   *
   * @param location the TreeLocationNode to create the path from
   * @param fill the percent fill
   * @return a new TreeLocationNode that encapsulates the new path
   */
  public abstract TreeLocationNode createPath(AbstractTreeLocation location, float fill);


}
