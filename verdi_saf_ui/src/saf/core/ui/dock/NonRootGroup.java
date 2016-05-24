package saf.core.ui.dock;

import bibliothek.gui.dock.common.CLocation;

/**
 * Encapsulates a perspective's non-root Group.
 * @author Nick Collier
 */
public class NonRootGroup extends AbstractGroup {

  private GroupLocationPath path;

  /**
   * Creates a NonRootGroup.
   *
   * @param id the id of the group
   * @param path the default location path of the group
   */
  public NonRootGroup(String id, GroupLocationPath path) {
    super(id);
    this.path = path;
  }

  /**
   * Gets the default location of this Group.
   *
   * @return the location of this Group.
   */
  public CLocation getLocation() {
    return path.getLocation();
  }
}
