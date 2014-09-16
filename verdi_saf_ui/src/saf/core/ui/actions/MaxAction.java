package saf.core.ui.actions;

import java.util.ResourceBundle;

import javax.swing.Icon;

import saf.core.ui.dock.StateChanger;
import bibliothek.gui.dock.common.action.CButton;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.facile.mode.MaximizedMode;
import bibliothek.gui.dock.support.util.Resources;
import bibliothek.gui.dock.util.IconManager;

/**
 * Custom maximize action that forwards the maximize
 * to a StateChanger.
 *
 * @author Nick Collier
 */
public class MaxAction extends CButton {

  private CDockable dockable;
  private StateChanger stateChanger;

  /**
   * Creates a maximize action that will maximize the specified
   * dockable.
   *
   * @param dockable the dockable to maximize
   * @param stateChanger the stateChanger used to perform the
   * actual maximize
   */
  public MaxAction(CDockable dockable, StateChanger stateChanger, IconManager iconManager) {
    this.stateChanger = stateChanger;
    this.dockable = dockable;
    ResourceBundle bundle = Resources.getBundle();
    setText(bundle.getString("maximize.in"));
    setTooltip("Maximize this view");

    Icon icon = iconManager.getIcon(MaximizedMode.ICON_IDENTIFIER); //StateManager.ICON_MANAGER_KEY_MAXIMIZE);
    setIcon(icon);
  }

  protected void action() {
    stateChanger.maximizeDockable(dockable);
  }
}
