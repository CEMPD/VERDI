package saf.core.ui.dock;

import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.location.CBaseLocation;
import bibliothek.gui.dock.common.location.AbstractTreeLocation;

import java.util.List;
import java.util.ArrayList;

/**
 * The location path of a group relative to the root group.
 *
 * @author Nick Collier
 */
public class GroupLocationPath {

  private String id;
  private List<GroupPathElement> elements;

  /**
   * Creates a GroupLocation path for the specified group id with the specified path.
   *
   * @param id   the group of that this is the path for
   * @param path the list of PathElements that constitute the path.
   */
  public GroupLocationPath(String id, List<GroupPathElement> path) {
    this.id = id;
    this.elements = new ArrayList<GroupPathElement>(path);
  }

  /**
   * Creates a new GroupLocationPath for the specified group with a single
   * element path.
   *
   * @param id the id of the group this is the path for
   * @param element the single element that constitutes the path
   */
  public GroupLocationPath(String id, GroupPathElement element) {
    this.id = id;
    this.elements = new ArrayList<GroupPathElement>();
    this.elements.add(element);
  }

  /**
   * Creates a GroupLocationPath for the specified group. The path will be
   * the parent path plus the element.
   *
   * @param id the id of the group this is the path for
   * @param element the element to append to the parent path
   * @param parentPath the parent path of this GroupLocation path
   */
  public GroupLocationPath(String id, GroupPathElement element, GroupLocationPath parentPath) {
    this(id, parentPath.elements);
    elements.add(element);
  }

  /**
   * Gets the id of the group that this is the path for.
   *
   * @return the id of the group that this is the path for.
   */
  public String getId() {
    return id;
  }

  /**
   * Gets the CLocation for this path.
   *
   * @return the CLocation for this path.
   */
  public CLocation getLocation() {
    CBaseLocation basePath = CLocation.base();
    AbstractTreeLocation path = elements.get(0).createPath(basePath);
    for (int i = 1, n = elements.size(); i < n; i++) {
      path = elements.get(i).createPath(path);
    }

    return path;
  }
}
