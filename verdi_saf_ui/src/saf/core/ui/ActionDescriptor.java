package saf.core.ui;

import saf.core.ui.actions.ActionFactory;
import saf.core.ui.actions.ISAFAction;
import simphony.util.messages.MessageCenter;

import javax.swing.*;

/**
 * Describes an saf action as defined in the xml.
 *
 * @author Nick Collier
 */
public class ActionDescriptor implements BarItemDescriptor {

	private String id;
	protected ISAFAction action;
	private String label, menuID, toolbarGroupID, actionCommand;
	private Icon icon;
	private String tooltip;

	public ActionDescriptor(String id, ISAFAction action) {
		this.id = id;
		this.action = action;
    actionCommand = "";
  }

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getMenuID() {
		return menuID;
	}

	public void setMenuID(String menuID) {
		this.menuID = menuID;
	}

	public String getToolbarGroupID() {
		return toolbarGroupID;
	}

	public void setToolbarGroupID(String toolbarGroupID) {
		this.toolbarGroupID = toolbarGroupID;
	}

	public Icon getIcon() {
		return icon;
	}

	public void setIcon(Icon icon) {
		this.icon = icon;
	}

	public void fillBars(GUIBarManager barManager, Workspace workspace) {

		action.initialize(workspace);
		ActionFactory.getInstance().registerAction(id, action);
		// if the bar paths are not null then add to the bars
		if (toolbarGroupID != null) {
			JButton button = barManager.addToolBarAction(toolbarGroupID, id, action);
			if (tooltip != null) button.setToolTipText(tooltip);

      if (icon != null)
				button.setIcon(icon);
			else if (label != null)
				button.setText(label);
			else
				button.setText(id);
      button.setActionCommand(actionCommand);
    }

		if (menuID != null) {
			JMenu menu = barManager.getMenu(menuID);
			if (menu == null) {
				if (menuID.equals(GUIConstants.WINDOW_MENU_ID)) {
					barManager.addWindowItem(this);
				} else {
					MessageCenter.getMessageCenter(getClass()).warn("Unable to add action to menu '" + menuID + "'. Menu not found.");
				}
			} else {
				createMenuItem(barManager);
			}
		}
	}

	public JMenuItem createMenuItem(GUIBarManager barManager) {
    String text = label == null ? id : label;
		JMenuItem item = barManager.addMenuItem(id, menuID, action);

		int mnIndex = text.indexOf("&");
		if (mnIndex != -1 && mnIndex != text.length() - 1) {
			item.setMnemonic(text.charAt(mnIndex + 1));
			text = text.substring(0, mnIndex) + text.substring(mnIndex + 1, text.length());
		}
		item.setText(text);
    item.setActionCommand(actionCommand);

    return item;
	}

	public void setTooltip(String tip) {
		this.tooltip = tip;
	}

	public String getTooltip() {
		return tooltip;
	}

  public String getActionCommand() {
    return actionCommand;
  }

  public void setActionCommand(String actionCommand) {
    this.actionCommand = actionCommand;
  }
}


