package saf.core.ui.dock;

import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.CLocation;

/**
 * Encapsulates a perspective's root group.
 *
 * @author Nick Collier
 */
public class RootGroup extends AbstractGroup {

  private CWorkingArea workingArea;

  /**
   * Creates a RootGroup with the specified id.
   *
   * @param id the id of the root group.
   */
  public RootGroup(String id) {
    super(id);
  }

  // intializes the working area
  void initWorkingArea(CWorkingArea workingArea) {
    this.workingArea = workingArea;
  }

  /**
   * Gets the default location of this Group.
   *
   * @return the location of this Group.
   */
  public CLocation getLocation() {
    return CLocation.working(workingArea).rectangle(0, 0, 1, 1);
  }
}
