package saf.core.ui.dock;

import java.util.*;

/**
 * Factory for creating GroupLocationPaths. This takes
 * descriptions (relative location etc.) of single path
 * elements and produces a list of GroupLocationPaths.
 *
 * @author Nick Collier
 */
public class GroupPathCreator {

  private static class PathData {
    Location location;
    String parent, id;
    float fill;

    private PathData(String id, String parent, Location location, float fill) {
      this.id = id;
      this.parent = parent;
      this.location = location;
      this.fill = fill;
    }
  }

  private List<PathData> data = new ArrayList<PathData>();

  /**
   * Defines a path with the specified parameters.
   *
   * @param id             the id of the group
   * @param location       the relative location of the group
   * @param parent         the id of the group this group is located relative to
   * @param fillPercentage the percentage of space this group takes up relative to its parent
   */
  public void definePath(String id, Location location, String parent, float fillPercentage) {
    data.add(new PathData(id, parent, location, fillPercentage));
  }

  /**
   * Creates a List of GroupLocationPaths consisting of the paths that have
   * been defined. 
   *
   * @param rootID the id of the root group.
   * @return  a List of GroupLocationPaths consisting of the paths that have
   * been defined.
   */
  public List<GroupLocationPath> createPaths(String rootID) {
    Map<String, GroupLocationPath> paths = new HashMap<String, GroupLocationPath>();
    List<String> parents = new ArrayList<String>();

    // find all the paths whose parent is root
    for (Iterator<PathData> iter = data.iterator(); iter.hasNext();) {
      PathData pathData = iter.next();
      if (pathData.parent.equals(rootID)) {
        iter.remove();
        GroupPathElement element = new GroupPathElement(pathData.id, pathData.location, pathData.fill);
        GroupLocationPath path = new GroupLocationPath(pathData.id, element);
        paths.put(pathData.id, path);
        parents.add(pathData.id);
      }
    }

    findPaths(parents, paths);

    return new ArrayList<GroupLocationPath>(paths.values());
  }

  private void findPaths(List<String> parents, Map<String, GroupLocationPath> paths) {
    List<String> newParents = new ArrayList<String>();
    for (String parent : parents) {
      for (Iterator<PathData> iter = data.iterator(); iter.hasNext();) {
        PathData pathData = iter.next();
        if (pathData.parent.equals(parent)) {
          iter.remove();
          GroupPathElement element = new GroupPathElement(pathData.id, pathData.location, pathData.fill);
          GroupLocationPath path = new GroupLocationPath(pathData.id, element, paths.get(parent));
          paths.put(pathData.id, path);
          newParents.add(pathData.id);
        }
      }
    }

    if (newParents.size() > 0) findPaths(newParents, paths);
  }
}
