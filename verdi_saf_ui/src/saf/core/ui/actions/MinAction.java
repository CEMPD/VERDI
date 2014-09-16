package saf.core.ui.actions;

import java.util.ResourceBundle;

import javax.swing.Icon;

import saf.core.ui.dock.DockingManager;
import saf.core.ui.dock.StateChanger;
import bibliothek.gui.dock.common.action.CButton;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.facile.mode.MinimizedMode;
import bibliothek.gui.dock.support.util.Resources;
import bibliothek.gui.dock.util.IconManager;

/**
 * Custom minimize action that will minimize to the appropriate
 * side.
 *
 * @author Nick Collier
 */
public class MinAction extends CButton {

  private CDockable dockable;
  private StateChanger stateChanger;
  private DockingManager.MinimizeLocation location;

  /**
   * Creates a minimize action that will minimize the specified
   * dockable to the specified location
   *
   * @param dockable the dockable to minimize
   * @param location the location to minimize
   * @param stateChanger the stateChanger used to perform the
   * actual minimize
   */
  public MinAction(CDockable dockable, DockingManager.MinimizeLocation location, StateChanger stateChanger,
                   IconManager iconManager) {
    this.stateChanger = stateChanger;
    this.dockable = dockable;
    this.location = location;
    ResourceBundle bundle = Resources.getBundle();
    setText(bundle.getString("minimize.in"));
    setTooltip("Minimize this view");

    Icon icon = iconManager.getIcon(MinimizedMode.ICON_IDENTIFIER);//StateManager.ICON_MANAGER_KEY_MINIMIZE);
    setIcon(icon);
    
  }

  protected void action() {
    stateChanger.minimizeDockable(dockable, location);
  }
}
