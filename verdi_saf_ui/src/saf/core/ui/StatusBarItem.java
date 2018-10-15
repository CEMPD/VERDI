package saf.core.ui;

import javax.swing.*;

/**
 * Represents an item in a status bar.
 *
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2006/06/12 21:48:51 $
 */
public class StatusBarItem {

	private String layoutString;
	private JComponent comp;
	private String name;

	public StatusBarItem(String name, String layoutString, JComponent comp) {
		this.name = name;
		this.layoutString = layoutString;
		this.comp = comp;
	}

	public JComponent getComponent() {
		return comp;
	}

	public String getLayoutString() {
		return layoutString;
	}

	public String getName() {
		return name;
	}
}
