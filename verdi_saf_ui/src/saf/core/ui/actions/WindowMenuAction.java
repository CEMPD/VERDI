package saf.core.ui.actions;


import saf.core.ui.dock.DockableFrame;
import saf.core.ui.dock.DefaultDockableFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;

import bibliothek.gui.dock.common.intern.CDockable;

/**
 * Action that is triggered when selecting a frame from
 * the "window" menu. If the dockable is minimized it will
 * be restored and selected. If the dockable is hidden it will
 * be restored and selected. Otherwise, the dockable will just
 * be selected.
 *
 * @author Nick Collier
 */
public class WindowMenuAction extends AbstractAction {

  private DockableFrame dockable;

  /**
   * Creates a WindowMenuAction that will work on the
   * the specified dockable.
   *
   * @param dockable the dockable that is the subject of this action
   */
  public WindowMenuAction(DockableFrame dockable) {
    super(dockable.getTitle());
    this.dockable = dockable;
  }

  public void actionPerformed(ActionEvent e) {
    if (dockable.isMinimized()) dockable.restore();
    dockable.toFront();
  }
}
