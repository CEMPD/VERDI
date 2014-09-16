/*CopyrightHere*/
package saf.core.ui.util;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

/**
 * Utility functions for working with user interfaces.
 */
public class UIUtilities {
	/**
	 * Centers a given window onto the primary display.
	 * 
	 * @param window
	 *            the window to center on the display
	 */
	public static void centerWindowOnScreen(Window window) {
		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

		int x = (screenDim.width - window.getSize().width) / 2;
		int y = (screenDim.height - window.getSize().height) / 2;
		window.setLocation(x, y);
	}
}
