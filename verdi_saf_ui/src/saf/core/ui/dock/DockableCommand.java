package saf.core.ui.dock;

import java.util.List;

/**
 * Interface for commands that operate on a Dockable.
 *
 * @author Nick Collier
 */
public interface DockableCommand {

  /**
   * Performs the activation.
   */
  void activate();

  /**
   * Gets the DockableFrame(s) that are the subjects
   * of this command.
   *
   * @return the DockableFrame(s) that are the subjects
   * of this command.
   */
  List<DockableFrame> getSubjects();
}
