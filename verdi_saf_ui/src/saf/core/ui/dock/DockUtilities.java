package saf.core.ui.dock;

import java.util.List;
import java.util.Arrays;

/**
 * Static utility methods for DockableFrames.
 *
 * @author Nick Collier
 */
public class DockUtilities {

  /**
   * Creates a list of dockable frames from the specified frames.
   *
   * @param frames the frames to add to the list
   * 
   * @return a list of dockable frames from the specified frames.
   */
  public static List<DockableFrame> createList(DockableFrame... frames) {
    return Arrays.asList(frames);
  }
}
