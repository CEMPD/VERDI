package saf.core.ui.dock;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * Interface for a SAF perspective. A perspective is essentially
 * a spatially arranged collection of dockable frames.
 * 
 * @author Nick Collier
 */
public interface Perspective {

  /**
   * Gets whether or not this Perspective is the active
   * perspective.
   *
   * @return true if this perspective is active, otherwise false.
   */
  boolean isActive();

  /**
   * Loads a layout. Depending on whether this Perspective uses
   * the Preferences to store the layout, the prefs object
   * may contain the layout data.
   *
   * @param prefs repository of the layout data
   */
  void loadLayout(Preferences prefs);

  /**
   * Saves the frame layout.
   *
   * @param prefs optional preferences repository specifying where the layout can be saved
   */
  void saveLayout(Preferences prefs);
  
  /**
   * Saves the current layout to the specified file.
   * 
   * @param file the file to save the layout to
   */
  void saveLayout(File file);
  
  /**
   * Removes the specified dockable from this Perspective.
   *
   * @param dockable the dockable to remove
   */
  void removeDockable(DockableFrame dockable);

  /**
   * Gets all the DockableFrames in the specified group.
   *
   * @param groupID the id of the group whose dockables should be returned
   * @return all the DockableFrames in the specified group.
   */
  Set<DockableFrame> getDockables(String groupID);

  /**
   * Gets the ids of all the groups in this Perspective.
   *
   * @return the ids of all the groups in this Perspective.
   */
  Set<String> getGroupIDs();


  /**
   * Gets the id of this Perspective.
   *
   * @return the id of this Perspective.
   */
  String getID();

  /**
   * Gets the name of this Perspective.
   *
   * @return the name of this Perspective.
   */
  String getName();

  /**
   * Creates the root or main group of this Perspective. The root
   * group will have the specified id.
   *
   * @param id the id of the root group
   */
  void createRootGroup(String id);

  /**
   * Creates group with the specified parameters.
   *
   * @param id the id of the group
   * @param location the location of the group
   * @param relativeTo the id of the parent of the created group
   * @param fillPercentage the fill percentage of the created group.
   */
  void createGroup(String id, Location location, String relativeTo, float fillPercentage);

  /**
   * Adds the specified DockableFrame into the specified group.
   *
   * @param groupId the id of the group
   * @param dockable the DockableFrame to add
   * @param show whether or not to display the frame
   */
  void addDockableFrame(String groupId, DockableFrame dockable, boolean show);

  /**
   * Deactivates this Perspective.
   */
  void deactivate();

  /**
   * Activates this Perspective.
   */
  void activate();


  /**
   * Gets all the DockableFrames currently in this perspective.
   *
   * @return all the DockableFrames currently in this perspective.
   */
  List<DockableFrame> getDockables();

  /**
   * Resets the layout of the perspective to match the given file
   * @param file the file containing the desired layout
   * @return whether or not the layout change was successful
   */
  public boolean reset(File file);
  
  /**
   * Resets the layout of the perspective to match the given stream
   * @param stream the stream containing the desired layout
   * @return whether or not the layout change was successful
   */
  public boolean reset(InputStream stream);

}
