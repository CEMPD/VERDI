package saf.core.ui;

import javax.swing.*;

/**
 * @author Nick Collier
 * @version $Revision: 1.3 $ $Date: 2005/12/09 20:16:16 $
 */
public class SeparatorDescriptor implements BarItemDescriptor {

	private String menuID, toolbarGroupID;

	public SeparatorDescriptor(String menuID, String toolbarGroupID) {
		this.menuID = menuID;
		this.toolbarGroupID = toolbarGroupID;
	}

	public String getMenuID() {
		return menuID;
	}

	public String getToolbarGroupID() {
		return toolbarGroupID;
	}

	public void fillBars(GUIBarManager barManager, Workspace workspace) {
		if (toolbarGroupID != null) {
			barManager.addToolBarSeparator(toolbarGroupID);
		}
		if (menuID != null) {
			JMenu menu = barManager.getMenu(menuID);
			menu.addSeparator();
		}
	}
}


