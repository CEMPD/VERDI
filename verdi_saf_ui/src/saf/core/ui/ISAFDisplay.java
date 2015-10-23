package saf.core.ui;

import javax.swing.*;

/**
 * Encapsulates the main components of the SAF main application window.
 *
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:55:17 $
 */
public interface ISAFDisplay {

  /**
   * Gets the JFrame of the main application window.
   *
   * @return the JFrame of the main application window.
   */
  JFrame getFrame();

  /**
   * Displays the main application window.
   */
  void display();

  /**
   * Initializes the display based on info in the
   * customizer and using the menu, tool and
   * status bars from the gui bar manager.
   *
   * @param customizer contains main frame size, position etc. info
   * @param manager the bar manager containing the menu, etc. bars
   */
  void init(IWindowCustomizer customizer, GUIBarManager manager);


}
