package saf.core.ui;


import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import simphony.util.messages.MessageCenter;

/**
 * Describes a menu / menu item.
 *
 * @author Nick Collier
 * @version $Revision: 1.3 $ $Date: 2005/12/05 14:52:15 $
 */
public class MenuTreeDescriptor {

	private static class MenuDescriptor {

		String id, label, parent;
		char mnemonic;
		List<MenuDescriptor> children = new ArrayList<MenuDescriptor>();

		public MenuDescriptor(String id, String label, String parent) {
			this.id = id;
			this.label = label;
			this.parent = parent;
			int mnIndex = label.indexOf("&");
			if (mnIndex != -1 && mnIndex != label.length() - 1) {
				mnemonic = label.charAt(mnIndex + 1);
				this.label = label.substring(0, mnIndex) + label.substring(mnIndex + 1, label.length());
			}
		}

		public void addChild(MenuDescriptor child) {
			children.add(child);
		}

		public void createMenu(GUIBarManager manager) {
			JMenu menu = manager.addMenu(id, label);
			if (mnemonic != 0) menu.setMnemonic(mnemonic);
			for (MenuDescriptor child : children) {
				child.createMenu(manager, id);
			}
		}

		public void createMenu(GUIBarManager manager, String parentID) {
			JMenu parentMenu = manager.getMenu(parentID);
			JMenu menu = manager.addMenu(parentMenu, id, label);
			if (mnemonic != 0) menu.setMnemonic(mnemonic);
			for (MenuDescriptor child : children) {
				child.createMenu(manager, id);
			}
		}
	}

	private Map<String, MenuDescriptor> menuMap = new LinkedHashMap<String, MenuDescriptor>();

	public void addMenu(String id, String label, String parent) {
		menuMap.put(id, new MenuDescriptor(id, label, parent));
	}

	public void createMenus(GUIBarManager manager) {
		List<MenuDescriptor> rootList = new ArrayList<MenuDescriptor>();
		for (MenuDescriptor desc : menuMap.values()) {
			if (desc.parent != null) {
				MenuDescriptor parent = menuMap.get(desc.parent);
				if (parent == null) {
					String message = "Menu '" + desc.id + "' has an undefined parent '" + desc.parent + "'";
					MessageCenter.getMessageCenter(getClass()).error(message, new IllegalArgumentException(message));
					return;
				}
				parent.addChild(desc);
			} else {
				rootList.add(desc);
			}
		}

		for (MenuDescriptor desc : rootList) {
			desc.createMenu(manager);
		}
	}
}
