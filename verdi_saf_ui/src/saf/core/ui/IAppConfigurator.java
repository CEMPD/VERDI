package saf.core.ui;

import saf.core.ui.dock.DockingManager;


/**
 * Inteface for application configuration. The methods in this interface are
 * called by the SAF application initialization mechanism during points in the
 * applications lifecycle. On application start up, the order in which they
 * are called is:
 * <ol>
 * <li> #preWindowOpen </li>
 * <li> #createLayout </li>
 * <li> #fillBars </li>
 * <li> #postWindowOpen </li>
 * </ol>
 *
 * @author Nick Collier
 * @version $Revision: 1.3 $ $Date: 2005/11/21 18:55:17 $
 */
public interface IAppConfigurator {

	/**
	 * Performs some arbitrary actions prior to the main application window
	 * opening. This can be setting the application's look and feel, using the
	 * customizer parameter to set the initial window's size, title and
	 * so on.
	 *
	 * @param customizer the customizer used to customize the initial application
	 * window
	 * 
	 * @return true if the application should continue to open, or false to
	 * close stop application initialization. Note that return false can be a normal
	 * condition, such as a login failing.
	 */
  boolean preWindowOpen(IWindowCustomizer customizer);

	/**
	 * Optionally adds menu items and actions to the menu and tool bars.
	 * This can be used to programmatically add tool bar and menus / menu items
	 * for those that are not described in an xml plugin file.
	 * 
	 * @param barManager the GUIBarManager used to configure tool and menu bars.
	 */
  void fillBars(GUIBarManager barManager);

	/**
	 * Performs some arbitrary actions immediately after the main application window
	 * has been open.
	 *
	 * @param display the display representing the main application window.
	 */
  void postWindowOpen(ISAFDisplay display);

  /**
   * Performs some arbitrary clean up type actions immediately prior to closing the main
   * application window. Implementors can return false to veto application exit and window
   * close.
   * 
   * @return true if the window can continue to close, false to veto the window close.
   */
  boolean preWindowClose();

	/**
	 * Performs some arbitrary clean up type actions immediately prior to closing the main
   * application window.
	 */
  void postWindowClose();

	/**
	 * Creates the initial layout in the main application window. Typically, implementors
	 * would add the initial application views here, setting up the initial gui
	 * layout.
	 *
	 * @param dockingManager the ViewManager used to create the initial layout
	 */
  void createLayout(DockingManager dockingManager);
}
