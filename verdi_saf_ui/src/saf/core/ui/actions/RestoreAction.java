package saf.core.ui.actions;

import java.util.ResourceBundle;

import javax.swing.Icon;

import saf.core.ui.dock.StateChanger;
import bibliothek.gui.dock.common.action.CButton;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.facile.mode.NormalMode;
import bibliothek.gui.dock.support.util.Resources;
import bibliothek.gui.dock.util.IconManager;

/**
 * Custom restore action that forwards the restore
 * to a StateChanger.
 *
 * @author Nick Collier
 */
public class RestoreAction extends CButton {

  private CDockable dockable;
  private StateChanger stateChanger;

  /**
   * Creates a restore action that will restore the specified
   * dockable.
   *
   * @param dockable the dockable to restore
   * @param stateChanger the stateChanger used to perform the
   * actual restore
   */
  public RestoreAction(CDockable dockable, StateChanger stateChanger, IconManager iconManager) {
    this.stateChanger = stateChanger;
    this.dockable = dockable;
    ResourceBundle bundle = Resources.getBundle();
    setText(bundle.getString("normalize.in"));
    setTooltip("Return this view to its previously docked location");

    Icon icon = iconManager.getIcon(NormalMode.ICON_IDENTIFIER); //StateManager.ICON_MANAGER_KEY_NORMALIZE);
    setIcon(icon);
  }

  protected void action() {
    stateChanger.restoreDockable(dockable);
  }
}