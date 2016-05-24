package saf.core.ui.actions;

import java.util.ResourceBundle;

import javax.swing.Icon;

import saf.core.ui.dock.StateChanger;
import bibliothek.gui.dock.common.action.CButton;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.facile.mode.ExternalizedMode;
import bibliothek.gui.dock.support.util.Resources;
import bibliothek.gui.dock.util.IconManager;

/**
 * Custom float action that forwards the float
 * to a StateChanger.
 *
 * @author Nick Collier
 */
public class FloatAction extends CButton {

  private CDockable dockable;
  private StateChanger stateChanger;

  /**
   * Creates a float action that will float the specified
   * dockable.
   *
   * @param dockable the dockable to float
   * @param stateChanger the stateChanger used to perform the
   * actual float
   */
  public FloatAction(CDockable dockable, StateChanger stateChanger, IconManager iconManager) {
    this.stateChanger = stateChanger;
    this.dockable = dockable;
    ResourceBundle bundle = Resources.getBundle();
    setText(bundle.getString("externalize.in"));
    setTooltip("Externalize this view into a floating window");

    Icon icon = iconManager.getIcon(ExternalizedMode.ICON_IDENTIFIER); //StateManager.ICON_MANAGER_KEY_EXTERNALIZE);
    setIcon(icon);
  }

  protected void action() {
    stateChanger.floatDockable(dockable);
  }
}