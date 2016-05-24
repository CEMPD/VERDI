package saf.core.ui.dock;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.prefs.Preferences;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import saf.core.ui.AppPreferences;
import simphony.util.messages.MessageCenter;
import bibliothek.gui.DockStation;
import bibliothek.gui.Dockable;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.CStation;
import bibliothek.gui.dock.common.CWorkingArea;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.common.location.AbstractTreeLocation;
import bibliothek.gui.dock.common.location.CBaseLocation;
import bibliothek.gui.dock.common.location.CStackLocation;
import bibliothek.gui.dock.common.mode.ExtendedMode;
import bibliothek.util.xml.XElement;
import bibliothek.util.xml.XIO;

/**
 * Default implementation of a perspective. This uses the DockingFrames library
 * for docking.
 * 
 * @author Nick Collier
 */
public class DefaultPerspective implements Perspective {
	static final Logger Logger = LogManager.getLogger(DefaultPerspective.class.getName());

  static MessageCenter msg = MessageCenter.getMessageCenter(Perspective.class);

  private String name;

  private String id;
  private CControl control;
  private GroupPathCreator pathCreator = new GroupPathCreator();
  private CWorkingArea workingArea;
  // key is group id
  private Map<String, Group> groupData = new HashMap<String, Group>();
  private String rootGroupID;
  private List<DockableCommand> activations = new ArrayList<DockableCommand>();
  private boolean isActive = false, initialized = false;
  private CDockable selectedDockable;
  private File layoutFile = null;

  private XElement savedLayout;

  /**
   * Creates a DefaultPerspective.
   * 
   * @param name
   *          the name of the perspective
   * @param id
   *          the unique id of the perspective
   * @param control
   *          the CControl used for the docking of the frame's within the
   *          perspective
   */
  public DefaultPerspective(String id, String name, CControl control) {
    this.name = name;
    this.id = id;
    this.control = control;
  }

  private void initializeGroupData() {
    groupData.put(rootGroupID, new RootGroup(rootGroupID));
    List<GroupLocationPath> locPaths = pathCreator.createPaths(rootGroupID);
    for (GroupLocationPath path : locPaths) {
      groupData.put(path.getId(), new NonRootGroup(path.getId(), path));
    }
  }

  /**
   * Gets whether or not this Perspective is the active perspective.
   * 
   * @return true if this perspective is active, otherwise false.
   */
  public boolean isActive() {
    return isActive;
  }

  /**
   * Adds the specified DockableFrame into the specified group.
   * 
   * @param groupId
   *          the id of the group
   * @param dockable
   *          the DockableFrame to add
   * @param show
   *          whether or not to display the frame
   */
  public void addDockableFrame(String groupId, DockableFrame dockable, boolean show) {
	  Logger.debug("in DefaultPerspective.addDockableFrame, groupData.size() = " + groupData.size());
    if (groupData.size() == 0)
    {
      initializeGroupData();
      Logger.debug("just called initializeGroupData");
    }

    Group data = groupData.get(groupId);
    if (data == null) {
      msg.error("Error adding dockable: Group '" + groupId + "' does not exist.",
	  new IllegalArgumentException("Group does not exist"));
      return;
    }
    Logger.debug("data was not null, continuing by instantiating DockableActivation");
    DockableActivation activation = new DockableActivation(dockable, data);
    if (workingArea != null) {
    	Logger.debug("workingArea was not null, continuing by calling activation.activate()");
      activation.activate();
      Logger.debug("back from activate, ready to call .getDockable().toFront()");
      ((DefaultDockableFrame) dockable).getDockable().toFront();
      Logger.debug("done with that, now ready to return from member function addDockableFrame");
    } else
    {
    	Logger.debug("workingArea was null, so into else: call activations.add(activation)");
      activations.add(activation);
      Logger.debug("done with add, returning from member function addDockableFrame");
    }
  }

  /**
   * Attempts to dock the specified dockable to the target, stacking the
   * dockable on top of the target. This will do nothing if the target is
   * minimized or the dockable has not been added to a group.
   * 
   * @param dockable
   *          the dockable to dock
   * @param target
   *          the target to dock to
   */
  public void dock(DockableFrame dockable, DockableFrame target) {
    StackActivation activation = new StackActivation(dockable, target);
    if (isActive)
      activation.activate();
    else
      activations.add(activation);
  }

  /**
   * Attempts to dock the specified dockable in some location relative to
   * another dockable. This will not work if relativeTo is minimized or
   * maximized, or if the dockable hasn't been added to a group yet.
   * 
   * @param dockable
   *          the dockable to dock
   * @param relativeTo
   *          the dockable to dock relative to
   * @param location
   *          the relative location
   * @param fillPercent
   *          the fill percent that the dockable should take up
   */
  public void dock(DockableFrame dockable, DockableFrame relativeTo, Location location,
      float fillPercent) {
    DockingActivation activation = new DockingActivation(dockable, relativeTo, location,
	fillPercent);
    if (isActive)
      activation.activate();
    else
      activations.add(activation);
  }

  /**
   * Creates group with the specified parameters.
   * 
   * @param id
   *          the id of the group
   * @param location
   *          the location of the group
   * @param relativeTo
   *          the id of the parent of the created group
   * @param fillPercentage
   *          the fill percentage of the created group.
   */
  public void createGroup(String id, Location location, String relativeTo, float fillPercentage) {
    pathCreator.definePath(id, location, relativeTo, fillPercentage);
  }

  /**
   * Creates the root or main group of this Perspective. The root group will
   * have the specified id.
   * 
   * @param id
   *          the id of the root group
   */
  public void createRootGroup(String id) {
    rootGroupID = id;
    activations.add(0, new WorkingAreaActivation(id));
  }

  /**
   * Activates this Perspective.
   */
  public void activate() {
    if (!isActive) {
      isActive = true;
      savedLayout = null;

      if (groupData.size() == 0)
	initializeGroupData();

      for (DockableCommand command : activations) {
	command.activate();
      }

      // load the stored layout
      if (!initialized && layoutFile != null) {
	reset(layoutFile);
	layoutFile = null;
      }

      activations.clear();
      initialized = true;
    }
  }

  /**
   * Resets the layout from a layout stored in a file.
   * 
   * @param file the file to reset the layout from.
   */
  public boolean reset(File file) {
    try {
      return reset(new FileInputStream(file));
    } catch (FileNotFoundException ex) {
      msg.error("Error reseting layout from file", ex);
      return false;
    }
  }

  /**
   * Resets the layout from a layout stored in a stream.
   * 
   * @param stream the stream to reset the layout from.
   */
  public boolean reset(InputStream stream) {
    boolean success = false;
    try {
      // System.out.printf("Reading: %s%n", file.getAbsolutePath());
      BufferedInputStream in = new BufferedInputStream(stream);
      XElement element = XIO.readUTF(in);
      in.close();
      control.readXML(element);
    } catch (Exception ex) {
      control.delete(id);
      msg.warn("Error reading saved layout for perspective '" + id + "'", ex);
      return false;
    }

    try {
      control.load(id);

      // this is to insure that any dockables that are not
      // part of the loaded layout, but should be added to
      // the perspective, are made visible.
      for (int i = 0, n = control.getCDockableCount(); i < n; i++) {
	CDockable dockable = control.getCDockable(i);
	if (!dockable.isVisible()) {
	  dockable.setVisible(true);
	  dockable.setExtendedMode(ExtendedMode.NORMALIZED);
	}
      }
      success = true;
    } catch (Exception ex) {
      control.delete(id);
      msg.warn("Error loading layout for perspective '" + id + "'", ex);
    }

    return success;
  }

  private CDockable findSelectedDockable() {
    // we do this because the selected dockable may not correspond
    // to a DockableFrame.
    Dockable dockable = control.intern().getController().getFocusedDockable();
    for (DockableFrame frame : getDockables()) {
      DefaultCDockable cDockable = ((DefaultDockableFrame) frame).getDockable();
      if (cDockable.intern().equals(dockable))
	return cDockable;
    }

    return null;
  }

  /**
   * Deactivates this Perspective.
   */
  public void deactivate() {
    if (isActive) {
      isActive = false;
      selectedDockable = findSelectedDockable();

      WorkingAreaActivation waActivation = new WorkingAreaActivation(rootGroupID);
      activations.add(waActivation);

      for (Group group : groupData.values()) {
	for (DockableFrame frame : group.getDockables()) {
	  DefaultCDockable dockable = ((DefaultDockableFrame) frame).getDockable();
	  activations.add(new AddActivation(frame, group, dockable.getExtendedMode()));
	}
      }

      // work-around for bug in dockingFrames where floating dockables
      // don't save
      normalizeExtDockables();
      control.save(id);
      savedLayout = new XElement("root");
      control.writeXML(savedLayout);
      // this activation needs to occur before the
      // the select activation so selections that
      // occur during the layout are not the final
      // selections
      activations.add(new LayoutActivation());

      for (DockableFrame frame : groupData.get(rootGroupID).getDockables()) {
	activations.add(new ModeSetActivation(frame));
      }

      for (DockableFrame frame : getDockables()) {
	DefaultCDockable dockable = ((DefaultDockableFrame) frame).getDockable();
	control.remove((SingleCDockable) dockable);
      }

      if (selectedDockable != null)
	activations.add(new SelectActivation());

      control.remove((SingleCDockable) workingArea);
      control.remove((CStation) workingArea);
      workingArea = null;
    }
  }

  /**
   * Gets all the DockableFrames currently in this perspective.
   * 
   * @return all the DockableFrames currently in this perspective.
   */
  public List<DockableFrame> getDockables() {
    List<DockableFrame> dockables = new ArrayList<DockableFrame>();
    for (Group data : groupData.values()) {
      dockables = data.addTo(dockables);
    }

    return dockables;
  }

  /**
   * Gets all the DockableFrames in the specified group.
   * 
   * @param groupID
   *          the id of the group whose dockables should be returned
   * @return all the DockableFrames in the specified group.
   */
  public Set<DockableFrame> getDockables(String groupID) {
    Group data = groupData.get(groupID);
    if (data == null) {
      msg.error("Error getting dockables for group: Group '" + groupID + "does not exist.",
	  new IllegalArgumentException("Group does not exist"));
      return null;
    }

    return new HashSet<DockableFrame>(data.getDockables());
  }

  /**
   * Gets the ids of all the groups in this Perspective.
   * 
   * @return the ids of all the groups in this Perspective.
   */
  public Set<String> getGroupIDs() {
    return new HashSet<String>(groupData.keySet());
  }

  /**
   * Gets the id of this Perspective.
   * 
   * @return the id of this Perspective.
   */
  public String getID() {
    return id;
  }

  /**
   * Gets the name of this Perspective.
   * 
   * @return the name of this Perspective.
   */
  public String getName() {
    return name;
  }

  /**
   * Loads a layout. Depending on whether this Perspective uses the Preferences
   * to store the layout, the prefs object may contain the layout data.
   * 
   * @param prefs
   *          repository of the layout data
   */
  public void loadLayout(Preferences prefs) {
    File file = createFileName(prefs, false);
    if (file.exists()) {
      layoutFile = file;
    } else {
      // use the default layout if one exists
      file = new File(System.getProperty("applicationRoot") + "/props", "default_" + id + ".layout");
      if (file.exists())
	layoutFile = file;
    }
  }

  private File createFileName(Preferences prefs, boolean mkdir) {
    String path = prefs.get(AppPreferences.SAVE_PATH_KEY, "").trim();
    File file;
    if (path.length() == 0) {
      String home = System.getProperty("user.home");
      file = new File(home + "/." + prefs.name() + "/");
    } else {
      file = new File(path);
    }
    if (mkdir && !file.exists())
      file.mkdirs();
    return new File(file, id + ".layout");
  }

  /*
   * Normalizes any externalized dockables
   */
  private void normalizeExtDockables() {
    for (DockableFrame frame : getDockables()) {
      if (frame.isFloating())
	frame.restore();
    }
  }

  /**
   * Saves the frame layout.
   * 
   * @param prefs
   *          optional preferences repository where the layout can be saved
   */
  public void saveLayout(Preferences prefs) {
    // the perspective may have never been
    // activated so there may not be any layout info for it
    File file = createFileName(prefs, true);
    saveLayout(file);
  }
  
  /* (non-Javadoc)
   * @see saf.core.ui.dock.Perspective#saveLayout(java.io.File)
   */
  @Override
  public void saveLayout(File file) {
    if (initialized) {
      // only save if active otherwise save the setting
      // the control saved in deactivate
      if (isActive) {
	normalizeExtDockables();
	control.save(id);
      }
      try {
	if (savedLayout == null)
	  control.writeXML(file);
	else {
	  // write the layout that was saved to an XElement on deactivation
	  BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
	  XIO.writeUTF(savedLayout, out);
	  out.close();
	}
	// control.intern().write(new DataOutputStream(new
	// FileOutputStream(file)));
      } catch (IOException ex) {
	msg.warn("Error while saving layout for perspective '" + id + "'", ex);
      }
    }
  }

  /**
   * Removes the specified dockable from this Perspective.
   * 
   * @param dockable
   *          the dockable to remove
   */
  public void removeDockable(DockableFrame dockable) {
    if (isActive) {
      for (Group group : groupData.values()) {
	if (group.removeDockable(dockable))
	  break;
      }
      control.remove((SingleCDockable) ((DefaultDockableFrame) dockable).getDockable());
    } else {
      for (Iterator<DockableCommand> iter = activations.iterator(); iter.hasNext();) {
	DockableCommand act = iter.next();
	if (act.getSubjects().contains(dockable))
	  iter.remove();
      }
    }
    if (selectedDockable != null
	&& ((DefaultDockableFrame) dockable).getDockable().equals(selectedDockable))
      selectedDockable = null;
  }

  private DockableFrame findFrame(CDockable cDockable) {
    if (cDockable != null) {
      for (DockableFrame frame : getDockables()) {
	DefaultCDockable dockable = ((DefaultDockableFrame) frame).getDockable();
	if (dockable.equals(cDockable))
	  return frame;
      }
    }

    return null;

  }

  class LayoutActivation implements DockableCommand {

    public void activate() {
      control.load(id);
    }

    public List<DockableFrame> getSubjects() {
      return new ArrayList<DockableFrame>();
    }
  }

  class SelectActivation implements DockableCommand {

    public void activate() {
      DockableFrame frame = findFrame(selectedDockable);
      if (frame != null)
	frame.toFront();
    }

    public List<DockableFrame> getSubjects() {
      DockableFrame frame = findFrame(selectedDockable);
      if (frame != null)
	return DockUtilities.createList(frame);
      return new ArrayList<DockableFrame>();
    }
  }

  class WorkingAreaActivation implements DockableCommand {

    private String id;

    WorkingAreaActivation(String id) {
      this.id = id;
    }

    public void activate() {
      workingArea = control.createWorkingArea(id);
      workingArea.setLocation(CLocation.base().normalRectangle(0, 0, 1, 1));
      ((RootGroup) groupData.get(id)).initWorkingArea(workingArea);
      workingArea.setVisible(true);
    }

    public List<DockableFrame> getSubjects() {
      return new ArrayList<DockableFrame>();
    }
  }

  class DockableActivation implements DockableCommand {

    DockableFrame dockable;
    Group data;

    DockableActivation(DockableFrame dockable, Group data) {
      this.dockable = dockable;
      this.data = data;
    }

    public void activate() {
      DefaultCDockable cDockable = ((DefaultDockableFrame) dockable).getDockable();
      CLocation location;
      DockableFrame other = data.getNormalDockable();
      if (other == null)
	location = data.getLocation();
      else
	location = ((DefaultDockableFrame) other).getDockable().getBaseLocation();
      cDockable.setLocation(location);
      data.add(dockable);
      if (data.getID().equals(rootGroupID)) {
	workingArea.add((SingleCDockable) cDockable);
      } else {
	control.add((SingleCDockable) cDockable);
      }
      cDockable.setVisible(true);
    }

    public List<DockableFrame> getSubjects() {
      return DockUtilities.createList(dockable);
    }
  }

  class StackActivation implements DockableCommand {

    DockableFrame dockable, target;

    StackActivation(DockableFrame dockable, DockableFrame target) {
      this.dockable = dockable;
      this.target = target;
    }

    public void activate() {
      if (!target.isMinimized() && getDockables().contains(dockable)) {
	DefaultCDockable cTarget = ((DefaultDockableFrame) target).getDockable();
	CLocation location = cTarget.getBaseLocation();
	DefaultCDockable cDockable = ((DefaultDockableFrame) dockable).getDockable();
	cDockable.setLocation(location);
      }
    }

    public List<DockableFrame> getSubjects() {
      return DockUtilities.createList(dockable, target);
    }
  }

  // re-adds dockables
  class AddActivation implements DockableCommand {
    DockableFrame dockable;
    ExtendedMode mode;
    Group data;

    AddActivation(DockableFrame dockable, Group data, ExtendedMode mode) {
      this.dockable = dockable;
      this.mode = mode;
      this.data = data;
    }

    public void activate() {
      DefaultCDockable cDockable = ((DefaultDockableFrame) dockable).getDockable();
      if (data.getID().equals(rootGroupID)) {
	workingArea.add((SingleCDockable) cDockable);
      } else {
	control.add((SingleCDockable) cDockable);
      }
      cDockable.setExtendedMode(mode);
      cDockable.setVisible(true);
    }

    public List<DockableFrame> getSubjects() {
      return DockUtilities.createList(dockable);
    }
  }

  class DockingActivation implements DockableCommand {

    DockableFrame dockable, target;
    Location location;
    float fill;

    DockingActivation(DockableFrame dockable, DockableFrame target, Location location, float fill) {
      this.dockable = dockable;
      this.target = target;
      this.location = location;
      this.fill = fill;
    }

    public void activate() {
      if (!(target.isMinimized() || target.isMaximized()) && getDockables().contains(dockable)) {
	CLocation rLocation = ((DefaultDockableFrame) target).getDockable().getBaseLocation();
	if (rLocation instanceof AbstractTreeLocation) {
	  AbstractTreeLocation treeLoc = (AbstractTreeLocation) rLocation;
	  ((DefaultDockableFrame) dockable).getDockable().setLocation(
	      location.createPath(treeLoc, fill));

	} else if (rLocation instanceof CBaseLocation) {
	  CBaseLocation baseLoc = (CBaseLocation) rLocation;
	  ((DefaultDockableFrame) dockable).getDockable().setLocation(
	      location.createPath(baseLoc, fill));
	} else if (rLocation instanceof CStackLocation) {
	  DefaultCDockable parent = ((DefaultDockableFrame) target).getDockable();
	  DockStation parentStation = parent.intern().getDockParent();
	  CLocation stationLoc = control.getLocationManager().getLocation(
	      parentStation.asDockable());
	  if (stationLoc != null) {
	    ((DefaultDockableFrame) dockable).getDockable().setLocation(
		location.createPath((AbstractTreeLocation) stationLoc, fill));
	  }
	}
      }
    }

    public List<DockableFrame> getSubjects() {
      return DockUtilities.createList(dockable, target);
    }
  }

  // this is necessary because the working area frames
  // don't seem to respect their location / mode when re-laid out
  class ModeSetActivation implements DockableCommand {

    CDockable dockable;
    ExtendedMode mode;
    CLocation location;

    ModeSetActivation(DockableFrame frame) {
      dockable = ((DefaultDockableFrame) frame).getDockable();
      this.mode = dockable.getExtendedMode();
      this.location = dockable.getBaseLocation();
    }

    public void activate() {
      dockable.setExtendedMode(mode);
      dockable.setLocation(location);
    }

    public List<DockableFrame> getSubjects() {
      DockableFrame frame = findFrame(dockable);
      return DockUtilities.createList(frame);
    }

  }
}
