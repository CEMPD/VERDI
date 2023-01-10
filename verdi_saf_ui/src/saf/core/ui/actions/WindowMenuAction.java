package saf.core.ui.actions;


import saf.core.ui.dock.DockableFrame;
import saf.core.ui.dock.DefaultDockableFrame;
import saf.core.ui.dock.DefaultDockingManager;

import javax.swing.*;

import bibliothek.gui.dock.common.intern.DefaultCDockable;

import java.awt.event.ActionEvent;
import java.awt.Container;


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
  
  private DefaultDockingManager dockingManager;

  /**
   * Creates a WindowMenuAction that will work on the
   * the specified dockable.
   *
   * @param dockable the dockable that is the subject of this action
   */
  public WindowMenuAction(DockableFrame dockable, DefaultDockingManager manager) {
    super(dockable.getTitle());
    this.dockable = dockable;
    this.dockingManager = manager;
  }

  public void actionPerformed(ActionEvent e) {
    if (dockable.isMinimized()) dockable.restore();
    dockable.toFront();
    
    
    String id = dockable.getID();
	JComponent comp = (JComponent) ((Container)((Container)dockable.getContentPane().getComponent(0)).getComponent(0)).getComponent(0);
	
	DockableFrame frame = dockingManager.readdDockable(id,  comp);
	frame.setTitle(dockable.getTitle());
	
    String perspectiveId = (String)dockable.getClientProperty("PERSPECTIVE_ID");
    String groupId = (String)dockable.getClientProperty("GROUP_ID");
	dockingManager.addDockableToGroup(perspectiveId, groupId, frame);
	this.dockable = frame;
  }
}
