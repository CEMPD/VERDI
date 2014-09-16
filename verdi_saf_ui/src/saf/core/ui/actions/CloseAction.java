package saf.core.ui.actions;

import bibliothek.gui.DockUI;
import bibliothek.gui.dock.common.action.CButton;
import bibliothek.gui.dock.common.intern.CDockable;
import bibliothek.gui.dock.util.IconManager;
import saf.core.ui.dock.StateChanger;

import javax.swing.*;

/**
 * Custom close action that forwards the close
 * to a StateChanger.
 *
 * @author Nick Collier
 */
public class CloseAction extends CButton {

  private CDockable dockable;
  private StateChanger stateChanger;

  /**
   * Creates a close action that will close the specified
   * dockable.
   *
   * @param dockable     the dockable to close
   * @param stateChanger the stateChanger used to perform the
   *                     actual close
   */
  public CloseAction(CDockable dockable, StateChanger stateChanger, IconManager iconManager) {
    this.stateChanger = stateChanger;
    this.dockable = dockable;
    setText(DockUI.getDefaultDockUI().getString("close"));
    setTooltip("Close this view");

    Icon icon = iconManager.getIcon("close");
    setIcon(icon);
  }

  protected void action() {
    stateChanger.closeDockable(dockable);
  }
}