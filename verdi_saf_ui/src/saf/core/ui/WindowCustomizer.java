package saf.core.ui;
import java.awt.Dimension;import java.awt.Rectangle;import java.awt.Toolkit;
/** * @author Nick Collier * @version $Revision: 1.4 $ $Date: 2006/06/01 16:29:06 $ */
public class WindowCustomizer implements IWindowCustomizer {	private String title = "";	private Rectangle bounds = new Rectangle();	private int extendedState;	private AppPreferences prefs;	private String windowsLabel = "Window";
	public WindowCustomizer(AppPreferences prefs) {		this.prefs = prefs;	}
	/**	 * Gets the extended state of the main application window.	 *	 * @return the extended state of the main application window.	 * @see javax.swing.JFrame#setExtendedState(int)	 */	public int getExtendedState() {		return extendedState;	}
	/**	 * Sets the extended state of the main application window.	 *	 * @param extendedState the state	 * @see javax.swing.JFrame#setExtendedState(int)	 */	public void setExtendedState(int extendedState) {		this.extendedState = extendedState;	}
	/**	 * Gets the title of the initial application window. This may	 * be null or an empty String if no title has been set yet.	 *	 * @return the title of the initial application window. This may	 * be null or an empty String if no title has been set yet.	 */	public String getTitle() {		return title;	}
	/**	 * Sets the title of the initial application window.	 *	 * @param title the window title	 */	public void setTitle(String title) {		this.title = title;	}
	/**	 * Tells SAF to use the previously stored bounds for this application, or	 * if no stored bounds are found, use the default values and center the application	 * on the screen.	 *	 * @param defWidth the default width	 * @param defHeight the default height	 */
	public void useStoredFrameBounds(int defWidth, int defHeight) {		Dimension screenSize = null;
		int centerX = 0;
		int centerY = 0;
		
		try { //Headless
			screenSize = Toolkit.getDefaultToolkit().getScreenSize();					centerX = screenSize.width / 2;			centerY = screenSize.height / 2;
		} catch (Throwable t) {}
				int defaultX = centerX - defWidth / 2;		int defaultY = centerY - defHeight / 2;		int defX = (int)Math.max(0, defaultX);		int defY = (int)Math.max(0, defaultY);
		//		bounds = prefs.getApplicationBounds((int)Math.max(0, defaultX), (int)Math.max(0, defaultY),		//
		//						defWidth, defHeight);		bounds = prefs.getApplicationBounds(defX, defY, defWidth, defHeight);	}
	/**	 * Gets the initial bounds of the main application window. This may be	 * null or 0 size bounds if the initial bounds have not been set yet.	 *	 * @return the initial bounds of the main application window. This may be	 * null or 0 size bounds if the initial bounds have not been set yet.	 */	public Rectangle getInitialBounds() {		return bounds;	}
	/**	 * Sets the intial size of the application window to the specified width and height.	 * The application location  will be centered on the screen.	 *	 * @param width the initial width	 * @param height the initial height	 */	public void setInitialBounds(int width, int height) {		bounds = new Rectangle(-1 , -1, width, height);	}
	/**	 * Sets the intial bounds of the application window to the specified	 * width, height, and location.	 *	 * @param x the x screen coordinate	 * @param y the y screen coordinate	 * @param width the initial width	 * @param height the initial height	 */	public void setInitialBounds(int x, int y, int width, int height) {		bounds = new Rectangle(x, y, width, height);	}
	/**	 * Tells SAF to use the saved layout (dockable locations and fill percentages) in	 * place of those defined in the plugin. If nothing has been saved, the plugin values	 * are used by default. By default the layout data will be saved in directory named	 * after the plugin id in the user's home directory. This directory will be hidden, that	 * is, prefixed with a ".". So for example, assuming the application plugin id is "saf.demo.app",	 * then the layout data will be saved in the user's home directory in the ".saf.demo.app"	 * directory.	 *	 *	 */	public void useSavedLayout() {		useSavedLayout("");	}
	/**	 * Tells SAF to use the saved layout (dockable locations and fill percentages) in place of those	 * defined in the plugin. If nothing has been saved, the plugin values are used by	 * default. The layouts will be saved to the specified path. The layout data will be saved	 * to the specified directory.	 *	 * @param path the directory to save the layout(s) to	 */	public void useSavedLayout(String path) {		prefs.useSavedViewLayout(path);	}
	/**	 * Gets the window's menu label. The windows	 * menu is the menu that contains all of the	 * currently open views.	 *	 * @return the the window's menu label	 */	public String getWindowsMenuLabel() {		return windowsLabel;	}
	/**	 * Sets the window's menu label. The windows	 * menu is the menu that contains all of the	 * currently open views.	 *	 * @param label the new window's menu label	 */	public void setWindowMenuLabel(String label) {		windowsLabel = label;	}}
