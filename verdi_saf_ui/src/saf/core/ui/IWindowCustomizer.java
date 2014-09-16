package saf.core.ui;

import java.awt.*;

/**
 * Inteface for customizing the initial application window in a SAF application.
 *
 * @author Nick Collier
 * @version $Revision: 1.4 $ $Date: 2006/06/01 16:29:06 $
 */
public interface IWindowCustomizer {

	/**
	 * Gets the title of the initial application window. This may
	 * be null or an empty String if no title has been set yet.
	 *
	 * @return the title of the initial application window. This may
	 *         be null or an empty String if no title has been set yet.
	 */
	String getTitle();

	/**
	 * Sets the title of the initial application window.
	 *
	 * @param title the window title
	 */
	void setTitle(String title);

	/**
	 * Gets the initial bounds of the main application window. This may be
	 * null or 0 size bounds if the initial bounds have not been set yet.
	 *
	 * @return the initial bounds of the main application window. This may be
	 *         null or 0 size bounds if the initial bounds have not been set yet.
	 */
	Rectangle getInitialBounds();

	/**
	 * Gets the extended state of the main application window.
	 *
	 * @return the extended state of the main application window.
	 * @see javax.swing.JFrame#setExtendedState(int)
	 */
	int getExtendedState();

	/**
	 * Sets the intial size of the application window to the specified width and height.
	 * The application location  will be centered on the screen.
	 *
	 * @param width  the initial width
	 * @param height the initial height
	 */
	void setInitialBounds(int width, int height);

	/**
	 * Sets the intial bounds of the application window to the specified
	 * width, height, and location.
	 *
	 * @param x      the x screen coordinate
	 * @param y      the y screen coordinate
	 * @param width  the initial width
	 * @param height the initial height
	 */
	void setInitialBounds(int x, int y, int width, int height);

	/**
	 * Sets the extended state of the main application window.
	 *
	 * @param state the state
	 * @see javax.swing.JFrame#setExtendedState(int)
	 */
	void setExtendedState(int state);

	/**
	 * Tells SAF to use the previously stored bounds for this application, or
	 * if no stored bounds are found, use the default values and center the application
	 * on the screen.
	 *
	 * @param defWidth  the default width
	 * @param defHeight the default height
	 */
	void useStoredFrameBounds(int defWidth, int defHeight);

	/**
   * Tells SAF to use the saved layout (dockable locations and fill percentages) in
   * place of those defined in the plugin. If nothing has been saved, the plugin values
   * are used by default. By default the layout data will be saved in directory named
   * after the plugin id in the user's home directory. This directory will be hidden, that
   * is, prefixed with a ".". So for example, assuming the application plugin id is "saf.demo.app",
   * then the layout data will be saved in the user's home directory in the ".saf.demo.app"
   * directory.
   */
	void useSavedLayout();


  /**
	 * Tells SAF to use the saved layout (dockable locations and fill percentages) in place of those
	 * defined in the plugin. If nothing has been saved, the plugin values are used by
	 * default. The layouts will be saved to the specified path. The layout data will be saved
   * to the specified directory.
   *
   * @param path the directory to save the layout(s) to
	 */
  void useSavedLayout(String path);

  /**
	 * Gets the window's menu label. The windows
	 * menu is the menu that contains all of the
	 * currently open views.
	 *
	 * @return the the window's menu label
	 */
	String getWindowsMenuLabel();

	/**
	 * Sets the window's menu label. The windows
	 * menu is the menu that contains all of the
	 * currently open views.
	 *
	 * @param label the new window's menu label
	 */
	void setWindowMenuLabel(String label);
}