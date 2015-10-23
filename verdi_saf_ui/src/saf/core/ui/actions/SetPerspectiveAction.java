package saf.core.ui.actions;

import saf.core.ui.dock.DockingManager;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:55:17 $
 */
public class SetPerspectiveAction extends AbstractAction {

  private String perspectiveID;
	private DockingManager manager;

	public SetPerspectiveAction(String perspectiveID, String label, DockingManager manager) {
    super(label);
    this.perspectiveID = perspectiveID;
		this.manager = manager;
	}

  public void actionPerformed(ActionEvent e) {
		manager.setPerspective(perspectiveID);
  }
}
