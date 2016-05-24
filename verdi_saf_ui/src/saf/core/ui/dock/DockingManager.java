package saf.core.ui.dock;


import saf.core.ui.GUIBarManager;
import saf.core.ui.event.DockableFrameListener;
import saf.core.ui.event.DockableSelectionListener;
import saf.core.ui.event.PerspectiveSelectionListener;

import javax.swing.*;
import java.util.List;
import java.util.Set;

/**
 * Interface for classes that manage dockable frames.
 */
public interface DockingManager {


  public enum MinimizeLocation {
    UNSPECIFIED, LEFT, RIGHT, BOTTOM, TOP
  }

  int CLOSE = 1;
  int MINIMIZE = 2;
  int MAXIMIZE = 4;
  int FLOAT = 8;

  /**
   * Gets the bar manager that manages the tool, menu and status bars.
   *
   * @return the bar manager that manages the tool, menu and status bars.
   */
  GUIBarManager getBarManager();

  /**
   * Attempts to dock the specified dockable to the target, stacking the
   * dockable on top of the target. This will do nothing if the
   * target is minimized or the dockable has not been added to a group.
   *
   * @param dockable the dockable to dock
   * @param target   the target to dock to
   */
  void dock(DockableFrame dockable, DockableFrame target);

  /**
   * Attempts to dock the specified dockable in some location relative
   * to another dockable. This will not work if relativeTo is minimized or
   * maximized, or if the dockable hasn't been added to a group yet.
   *
   * @param dockable    the dockable to dock
   * @param relativeTo  the dockable to dock relative to
   * @param location    the relative location
   * @param fillPercent the fill percent that the dockable should take up
   */
  void dock(DockableFrame dockable, DockableFrame relativeTo, Location location,
            float fillPercent);

  /**
   * Adds the specified dockable to the specified group in the specified perspective.
   *
   * @param perspectiveID the id of the perspective to add the dockable to
   * @param groupID       the id of the group to add the dockable to
   * @param dockable      the dockable to add
   */
  void addDockableToGroup(String perspectiveID, String groupID, DockableFrame dockable);

  /**
   * Gets the group ids of the groups in the specified perspective.
   *
   * @param perspectiveID the perspective whose groups are of interest
   * @return group ids of the groups in the specified perspective
   */
  Set<String> getGroupIDs(String perspectiveID);

  /**
   * Gets all the dockables in the specified group in the specified perspective.
   *
   * @param perspectiveID the id of the perspective
   * @param groupID       the id the group
   * @return all the dockables in the specified group in the specified perspective.
   */
  List<DockableFrame> getDockableFrames(String perspectiveID, String groupID);

  /**
   * Creates a Dockable with the specified id containing the specified component. This only creates
   * the dockable. To add a created dockable into a window use the
   * {@link #addDockableToGroup(String,String,DockableFrame) addDockableToGroup} method.
   *
   * @param id   the id that uniquely identifies the created Dockable
   * @param comp the component displayed by the dockable
   * @return the created dockable
   * @see #addDockableToGroup
   */
  DockableFrame createDockable(String id, JComponent comp);

  /**
   * Creates a dockable with the specified id, hide location and containing the specified component.
   * This only creates the dockable. To add a created dockable into a window use the
   * {@link #addDockableToGroup(String,String,DockableFrame) addDockableToGroup} method.<p>
   *
   * @param id           the id that uniquely identifies the created Dockable
   * @param comp         the component displayed by the dockable
   * @param hideLocation the location where the dockable is minimized to
   * @return the created dockable
   */
  DockableFrame createDockable(String id, JComponent comp, MinimizeLocation hideLocation);

  /**
   * Creates a dockable with the specified id, hide location, window controls and
   * containing the specified component.  This only creates the dockable. To add a created dockable
   * into a window use the
   * {@link #addDockableToGroup(String,String,DockableFrame) addDockableToGroup} method.<p>
   * <p/>
   * The dockable controls are:
   * <ul>
   * <li>DockingManager.CLOSE</li>
   * <li>DockingManager.HIDE</li>
   * <li>DockingManager.MAXIMIZE</li>
   * <li>DockingManager.FLOAT</li>
   * </ul>
   * These can be logically or'ed together.
   *
   * @param id               the id that uniquely identifies the created Dockable
   * @param comp             the component displayed by the dockable
   * @param location         the location where the dockable is minimized to
   * @param dockableControls specifies what controls (close, maximize, float etc.)
   *                         are displayed on the dockable's title bar
   * @return the created dockable
   */
  DockableFrame createDockable(String id, JComponent comp, MinimizeLocation location, int dockableControls);

  /**
   * Initializes the docking manger by activitating the current perpspective.
   */
  void init();

  /**
   * Sets the specified perspective as the currently active perspective.
   *
   * @param perspectiveID the id of the perspective to set
   */
  void setPerspective(String perspectiveID);

  /**
   * Gets a list of all the perspective ids.
   *
   * @return a list of all the perspective ids.
   */
  List<String> getPerspectiveIDs();

  /**
   * Gets the specified perspective.
   *
   * @param id the id of the perspective to get
   * @return the specified perspective.
   */
  Perspective getPerspective(String id);

  /**
   * Gets the current perspective.
   *
   * @return the current perspective
   */
  Perspective getPerspective();

  /**
   * Gets the specified dockable by its id.
   *
   * @param id the id of the dockable to get
   * @return the specified dockable by its id.
   */
  DockableFrame getDockable(String id);

  /**
   * Adds a dockable listener to this DockingManager to listen for dockable events.
   *
   * @param listener the listener to add.
   */
  void addDockableListener(DockableFrameListener listener);

  /**
   * Removes the specified dockable listener.
   *
   * @param listener the listener to remove
   */
  void removeDockableListener(DockableFrameListener listener);

  /**
   * Adds a dockable selection listener to this DockingManager to listen for dockable
   * selection events.
   *
   * @param listener the listener to add.
   */
  void addDockableSelectionListener(DockableSelectionListener listener);

  /**
   * Removes the specified dockable selection listener.
   *
   * @param listener the listener to remove
   */
  void removeDockableSelectionListener(DockableSelectionListener listener);

  /**
   * Adds a perspective selection listener to this DockingManager to listen for perspective
   * selection events.
   *
   * @param listener the listener to add.
   */
  void addPerspectiveSelectionListener(PerspectiveSelectionListener listener);

  /**
   * Removes the specified perspective selection listener.
   *
   * @param listener the listener to remove
   */
  void removePerspectiveSelectionListener(PerspectiveSelectionListener listener);

}
