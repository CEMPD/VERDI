package saf.core.ui.dock;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JMenu;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import saf.core.ui.GUIBarManager;
import saf.core.ui.GUIConstants;
import saf.core.ui.actions.CloseAction;
import saf.core.ui.actions.FloatAction;
import saf.core.ui.actions.MaxAction;
import saf.core.ui.actions.MinAction;
import saf.core.ui.actions.RestoreAction;
import saf.core.ui.actions.WindowMenuAction;
import saf.core.ui.event.DockableFrameAdapter;
import saf.core.ui.event.DockableFrameListener;
import saf.core.ui.event.DockableSelectionListener;
import saf.core.ui.event.DockableSelectionSupport;
import saf.core.ui.event.PerspectiveSelectionListener;
import saf.core.ui.event.PerspectiveSelectionSupport;
import simphony.util.messages.MessageCenter;
import bibliothek.gui.DockUI;
import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.action.predefined.CBlank;
import bibliothek.gui.dock.common.event.CFocusListener;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.facile.mode.ExternalizedMode;
import bibliothek.gui.dock.facile.mode.MaximizedMode;
import bibliothek.gui.dock.facile.mode.MinimizedMode;
import bibliothek.gui.dock.facile.mode.NormalMode;
import bibliothek.gui.dock.themes.BasicTheme;
import bibliothek.gui.dock.themes.basic.BasicColorScheme;
import bibliothek.gui.dock.util.IconManager;
import bibliothek.gui.dock.util.laf.LookAndFeelColors;
/**
 * Default implementation of a DockingManager that uses docking frames
 * docking framework.
 *
 * @author Nick Collier
 */
public class DefaultDockingManager extends DockableFrameAdapter implements DockingManager {
	static final Logger Logger = LogManager.getLogger(DefaultDockingManager.class.getName());
  private GUIBarManager barManager;
  private Map<String, DefaultPerspective> perspectiveMap = new HashMap<String, DefaultPerspective>();
  private Map<String, DefaultDockableFrame> idToFrameMap = new HashMap<String, DefaultDockableFrame>();
  private Map<CDockable, DefaultDockableFrame> cDockToFrameMap = new HashMap<CDockable, DefaultDockableFrame>();
  private DefaultPerspective currentPerspective;
  private StateChanger stateChanger;
  private DockableSelectionSupport dSelectionSupport;
  private PerspectiveSelectionSupport pSelectionSupport;
  private IconManager iconManager;
  /**
   * Creates a DockingManager from the specified GUIBarManager, viewport and list of perspectives.
   *
   * @param barManager     the bar manager used by this DockingManager
   * @param dockingControl the CControl used by this DockingManager
   * @param perspectives   the list of perspectives managed by this DockingManager
   */
  public DefaultDockingManager(GUIBarManager barManager, CControl dockingControl, List<DefaultPerspective> perspectives) {
    this.barManager = barManager;
    dockingControl.putProperty(BasicTheme.BASIC_COLOR_SCHEME, new BasicColorScheme() {
    	@Override
    	public boolean updateUI() {
    		super.updateUI();
    		setColor( "title.active.left",  new Color(0x236B8E));
            setColor( "title.inactive.left", DockUI.getColor( LookAndFeelColors.TITLE_BACKGROUND ));
            setColor( "title.active.right", new Color(0x74BBFB));
            setColor( "title.inactive.right", DockUI.getColor( LookAndFeelColors.TITLE_BACKGROUND ));
            setColor( "title.active.text", DockUI.getColor( LookAndFeelColors.TITLE_SELECTION_FOREGROUND ));
            setColor( "title.inactive.text", DockUI.getColor( LookAndFeelColors.TITLE_FOREGROUND ));
    		return true;
    	}
    });
    iconManager = dockingControl.intern().getController().getIcons();
    currentPerspective = perspectives.get(0);
    iconManager.setIconTheme(MinimizedMode.ICON_IDENTIFIER, 
    		new ImageIcon(this.getClass().getResource("min.gif")));
    iconManager.setIconTheme(MaximizedMode.ICON_IDENTIFIER, 
    		new ImageIcon(this.getClass().getResource("maximize.png")));
    iconManager.setIconTheme(NormalMode.ICON_IDENTIFIER,
    		new ImageIcon(this.getClass().getResource("restore.png")));
    iconManager.setIconTheme(ExternalizedMode.ICON_IDENTIFIER,
    		new ImageIcon(this.getClass().getResource("externalize.png")));
    iconManager.setIconTheme("close",
    		new ImageIcon(this.getClass().getResource("close.gif")));
    for (DefaultPerspective perspective : perspectives) {
      perspectiveMap.put(perspective.getID(), perspective);
    }
    stateChanger = new StateChanger(dockingControl, this);
    stateChanger.addDockableFrameListener(this);
    dSelectionSupport = new DockableSelectionSupport(this);
    pSelectionSupport = new PerspectiveSelectionSupport(this);
    dockingControl.addFocusListener(dSelectionSupport);
  }
  /**
   * Attempts to dock the specified dockable to the target, stacking the
   * dockable on top of the target. This will do nothing if the
   * target is minimized or the dockable has not been added to a group.
   *
   * @param dockable the dockable to dock
   * @param target   the target to dock to
   */
  public void dock(DockableFrame dockable, DockableFrame target) {
    currentPerspective.dock(dockable, target);
  }
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
  public void dock(DockableFrame dockable, DockableFrame relativeTo, Location location,
                   float fillPercent) {
    currentPerspective.dock(dockable, relativeTo, location, fillPercent);
  }
  /**
   * Adds the specified dockable frame to the specified group in the specified perspective.
   *
   * @param perspectiveID the id of the perspective to add the dockable frame to
   * @param groupID       the id of the group to add the dockable frame to
   * @param dockable      the dockable frame to add
   */
  public void addDockableToGroup(String perspectiveID, String groupID, DockableFrame dockable) {
	  Logger.debug("in DefaultDockingManager.addDockableToGroup: perspectiveID = " + perspectiveID
		 + ", groupID = " + groupID + ", dockable.getID() = " + dockable.getID());
    Perspective perspective = perspectiveMap.get(perspectiveID);
    if (perspective == null) {
      String message = "Perspective '" + perspectiveID + "' does not exist";
      MessageCenter.getMessageCenter(getClass()).error("Unable to add dockable frame to group. " + message, new IllegalArgumentException(message));
      return;
    }
    boolean show = perspectiveID.equals(currentPerspective.getID());
    Logger.debug("ready to call addDockableFrame with show = " + show);
    perspective.addDockableFrame(groupID, dockable, show);
    if (show) {
      updateWindowsMenu();
    }
  }
  /**
   * Gets the group ids of the groups in the specified perspective.
   *
   * @param perspectiveID the perspective whose groups are of interest
   * @return group ids of the groups in the specified perspective
   */
  public Set<String> getGroupIDs(String perspectiveID) {
    return perspectiveMap.get(perspectiveID).getGroupIDs();
  }
  /**
   * Gets all the dockable frames in the specified group in the specified perspective.
   *
   * @param perspectiveID the id of the perspective
   * @param groupID       the id the group
   * @return all the dockable frames in the specified group in the specified perspective.
   */
  public List<DockableFrame> getDockableFrames(String perspectiveID, String groupID) {
    Perspective perspective = perspectiveMap.get(perspectiveID);
    if (perspective != null) {
      return new ArrayList<DockableFrame>(perspective.getDockables(groupID));
    }
    return null;
  }
  /**
   * Creates a Dockable frame with the specified id containing the specified component. This only creates
   * the dockable frame. To add a created dockable frame into a window use the
   * {@link DockingManager#addDockableToGroup(String,String,DockableFrame) addDockableToGroup} method.
   *
   * @param id   the id that uniquely identifies the created Dockable frame
   * @param comp the component displayed by the dockable frame
   * @return the created dockable frame
   * @see #addDockableToGroup
   */
  public DockableFrame createDockable(String id, JComponent comp) {
    return createDockable(id, comp, MinimizeLocation.UNSPECIFIED, CLOSE | MINIMIZE | MAXIMIZE | FLOAT);
  }
  /**
   * Creates a dockable frame with the specified id, hide location and containing the specified component.
   * This only creates the dockable frame. To add a created dockable frame into a window use the
   * {@link DockingManager#addDockableToGroup(String,String,DockableFrame) addDockableToGroup} method.<p>
   *
   * @param id           the id that uniquely identifies the created Dockable frame
   * @param comp         the component displayed by the dockable frame
   * @param hideLocation the location where the dockable frame is minimized to
   * @return the created dockable frame
   */
  public DockableFrame createDockable(String id, JComponent comp, MinimizeLocation hideLocation) {
    return createDockable(id, comp, hideLocation, CLOSE | MINIMIZE | MAXIMIZE | FLOAT);
  }
  /**
   * Creates a dockable frame with the specified id, hide location, window controls and
   * containing the specified component.  This only creates the dockable frame. To add a created dockable frame
   * into a window use the
   * {@link DockingManager#addDockableToGroup(String,String,DockableFrame) addDockableToGroup} method.<p>
   * <p/>
   * The dockable frame controls are:
   * <ul>
   * <li>DockingManager.CLOSE</li>
   * <li>DockingManager.HIDE</li>
   * <li>DockingManager.MAXIMIZE</li>
   * <li>DockingManager.FLOAT</li>
   * </ul>
   * These can be logically or'ed together.
   *
   * @param id               the id that uniquely identifies the created Dockable frame
   * @param comp             the component displayed by the dockable frame
   * @param location         the location where the dockable frame is minimized to
   * @param dockableControls specifies what controls (close, maximize, float etc.)
   *                         are displayed on the dockable frame's title bar
   * @return the created dockable frame
   */
  public DockableFrame createDockable(String id, JComponent comp, MinimizeLocation location, int dockableControls) {
    DefaultDockableFrame frame = new DefaultDockableFrame(id, comp, stateChanger);
    DefaultCDockable dockable = frame.getDockable();
    if (comp instanceof CFocusListener)
    	frame.getDockable().addFocusListener((CFocusListener)comp);
    setDockableActions(dockable, dockableControls, location);
    idToFrameMap.put(id, frame);
    cDockToFrameMap.put(dockable, frame);
    return frame;
  }
  /**
   * Gets the DockableFrame associated with the specified CDockable.
   *
   * @param dockable the CDockable whose associated DockableFrame we want
   * @return the DockableFrame associated with the specified CDockable.
   */
  public DockableFrame getDockableFrameFor(CDockable dockable) {
    return cDockToFrameMap.get(dockable);
  }

  /**
   * Initializes the dockable frame manger by activitating the current perpspective.
   */
  public void init() {
    activatePerspective(currentPerspective.getID());
  }
  private void activatePerspective(String perspectiveID) {
    DefaultPerspective perspective = perspectiveMap.get(perspectiveID);
    if (perspective == null) {
      String message = "Perspective '" + perspectiveID + "' does not exist";
      MessageCenter.getMessageCenter(getClass()).error("Unable to activate perspective: " + message, new
              IllegalArgumentException(message));
      return;
    }
    pSelectionSupport.firePerpectiveChanging(currentPerspective, perspective);
    if (currentPerspective != null) currentPerspective.deactivate();
    perspective.activate();
    Perspective tmp = currentPerspective;
    currentPerspective = perspective;
    updateWindowsMenu();
    perspective.activate();
    barManager.selectMenuItem(currentPerspective.getID());
    pSelectionSupport.firePerpectiveChanged(tmp, currentPerspective);
  }
  /**
   * Sets the specified perspective as the currently active perspective.
   *
   * @param perspectiveID the id of the perspective to set
   */
  public void setPerspective(String perspectiveID) {
    if (!perspectiveID.equals(currentPerspective.getID())) activatePerspective(perspectiveID);
  }
  /**
   * Gets a list of all the perspective ids.
   *
   * @return a list of all the perspective ids.
   */
  public List<String> getPerspectiveIDs() {
    return new ArrayList<String>(perspectiveMap.keySet());
  }
  /**
   * Gets the specified perspective.
   *
   * @param id the id of the perspective to get
   * @return the specified perspective.
   */
  public Perspective getPerspective(String id) {
    return perspectiveMap.get(id);
  }
  /**
   * Gets the current perspective.
   *
   * @return the current perspective
   */
  public Perspective getPerspective() {
    return currentPerspective;
  }
  private void updateWindowsMenu() {
    JMenu menu = barManager.clearMenu(GUIConstants.WINDOW_MENU_ID);
    // user may not want a windows menu and so doesn't exist
    if (menu != null) {
      List<DockableFrame> dockables = currentPerspective.getDockables();
      for (DockableFrame dockable : dockables) {
        barManager.addMenuItem(dockable.getID(), GUIConstants.WINDOW_MENU_ID,
                new WindowMenuAction(dockable));
      }
      barManager.updateWindowMenu();
      barManager.sortMenu(GUIConstants.WINDOW_MENU_ID);
    }
  }
  /**
   * Gets the bar manager that managers the tool, menu and status bars.
   *
   * @return the bar manager that managers the tool, menu and status bars.
   */
  public GUIBarManager getBarManager() {
    return barManager;
  }
  private DefaultCDockable setDockableActions(DefaultCDockable dockable, int flags, MinimizeLocation loc) {
    // clear out the standard actions
    dockable.putAction(CDockable.ACTION_KEY_CLOSE, CBlank.BLANK);
    dockable.putAction(CDockable.ACTION_KEY_MAXIMIZE, CBlank.BLANK);
    dockable.putAction(CDockable.ACTION_KEY_MINIMIZE, CBlank.BLANK);
    dockable.putAction(CDockable.ACTION_KEY_EXTERNALIZE, CBlank.BLANK);
   // add our custom normalize action
    dockable.putAction(CDockable.ACTION_KEY_NORMALIZE, new RestoreAction(dockable, stateChanger, iconManager));
    if ((flags & CLOSE) > 0) {
      dockable.putAction(CDockable.ACTION_KEY_CLOSE,
              new CloseAction(dockable, stateChanger, iconManager));
      dockable.setCloseable(true);
    }
    if ((flags & MINIMIZE) > 0) dockable.putAction(CDockable.ACTION_KEY_MINIMIZE, new MinAction(dockable, loc,
            stateChanger, iconManager));
    if ((flags & MAXIMIZE) > 0) dockable.putAction(CDockable.ACTION_KEY_MAXIMIZE, new MaxAction(dockable,
            stateChanger, iconManager));
    if ((flags & FLOAT) > 0) dockable.putAction(CDockable.ACTION_KEY_EXTERNALIZE, new FloatAction(dockable,
            stateChanger, iconManager));
    return dockable;
  }
  /**
   * Gets the specified dockable frame by its id.
   *
   * @param id the id of the dockable frame to get
   * @return the specified dockable frame by its id.
   */
  public DockableFrame getDockable(String id) {
    return idToFrameMap.get(id);
  }
  /**
   * Sets the specified dockable frame as the active dockable frame.
   *
   * @param id the id of the dockable frame to make the active dockable frame
   */
  public void setActiveDockable(String id) {
    DefaultDockableFrame frame = idToFrameMap.get(id);
    if (frame == null) {
      String message = "Invalid dockable id '" + id + "'";
      MessageCenter.getMessageCenter(getClass()).error(message, new IllegalArgumentException(message));
    } else {
      frame.getDockable().toFront();
    }
  }

  void removeDockable(String perspectiveID, CDockable dockable) {
    DockableFrame frame = getDockableFrameFor(dockable);
    Perspective perspective = getPerspective(perspectiveID);
    perspective.removeDockable(frame);
    if (perspective.equals(currentPerspective)) {
      barManager.removeMenuItem(frame.getID());
    }
    idToFrameMap.remove(frame.getID());
    cDockToFrameMap.remove(dockable);
  }

  /**
   * Adds a dockable frame listener to this DockingManager to listen for dockable frame events.
   *
   * @param listener the listener to add.
   */
  public void addDockableListener(DockableFrameListener listener) {
    stateChanger.addDockableFrameListener(listener);
  }

  /**
   * Removes the specified dockable frame listener.
   *
   * @param listener the listener to remove
   */
  public void removeDockableListener(DockableFrameListener listener) {
    this.stateChanger.removeDockableFrameListener(listener);
  }

  /**
   * Adds a dockable frame selection listener to this DockingManager to listen for dockable frame
   * selection events.
   *
   * @param listener the listener to add.
   */
  public void addDockableSelectionListener(DockableSelectionListener listener) {
    dSelectionSupport.addSelectionListener(listener);
  }
  /**
   * Removes the specified dockable frame selection listener.
   *
   * @param listener the listener to remove
   */
  public void removeDockableSelectionListener(DockableSelectionListener listener) {
    dSelectionSupport.removeSelectionListener(listener);
  }

  /**
   * Adds a perspective selection listener to this DockingManager to listen for perspective
   * selection events.
   *
   * @param listener the listener to add.
   */
  public void addPerspectiveSelectionListener(PerspectiveSelectionListener listener) {
    pSelectionSupport.addPerspectiveListener(listener);
  }
  /**
   * Removes the specified perspective selection listener.
   *
   * @param listener the listener to remove
   */
  public void removePerspectiveSelectionListener(PerspectiveSelectionListener listener) {
    pSelectionSupport.removePerspectiveListener(listener);
  }
}