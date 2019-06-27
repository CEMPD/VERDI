package saf.core.ui;


import bibliothek.gui.dock.common.CContentArea;
import bibliothek.gui.dock.common.CControl;
import saf.core.ui.util.UIUtilities;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Default implementation of an ISAFDisplay that
 * uses the DockingFrames library for its docking
 * behavior.
 *
 * @author Nick Collier
 */
public class DefaultSAFDisplay implements ISAFDisplay {

  private JFrame frame;
  private CControl control;


  /**
   * Creates a DefaultSAFDisplay.
   */
  public DefaultSAFDisplay() {
	  try {
		  frame = new JFrame();
	  } catch (Throwable t) {}
    control = new CControl(frame);
  }

  /**
   * Gets the JFrame of the main application window.
   *
   * @return the JFrame of the main application window.
   */
  public JFrame getFrame() {
    return frame;
  }

  /**
   * Gets the CControl that controls the docking
   * for this display.
   *
   * @return the CControl that controls the docking
   *         for this display.
   */
  public CControl getCControl() {
    return control;
  }

  /**
   * Initializes the display based on info in the
   * customizer and using the menu, tool and
   * status bars from the gui bar manager.
   *
   * @param customizer contains main frame size, position etc. info
   * @param barManager the bar manager containing the menu, etc. bars
   */
  public void init(IWindowCustomizer customizer, GUIBarManager barManager) {
	  if (frame == null)
		  return;

    frame.setTitle(customizer.getTitle());
    frame.setBounds(customizer.getInitialBounds());
    frame.setExtendedState(customizer.getExtendedState());
    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    frame.setLayout(new BorderLayout());
    CContentArea contentArea = control.getContentArea();
    frame.add(contentArea, BorderLayout.CENTER);

    // fix the borders around the minimize areas to match the
    // content area
    JComponent north = (JComponent) contentArea.getNorth().getComponent();
    north.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

    JComponent south = (JComponent) contentArea.getSouth().getComponent();
    south.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

    JComponent west = (JComponent) contentArea.getWest().getComponent();
    west.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

    JComponent east = (JComponent) contentArea.getEast().getComponent();
    east.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 2));

    JPanel p = new JPanel(new GridLayout(1, 1));
    p.setBorder(new EmptyBorder(5, 5, 5, 5));
    p.add(barManager.getStatusBar());
    frame.add(p, BorderLayout.SOUTH);
    frame.setJMenuBar(barManager.getMenuBar());
    frame.add(barManager.getToolBar(), BorderLayout.NORTH);
  }

  /**
   * Displays the main application window.
   */
  public void display() {
    if (frame.getBounds().x < 0 || frame.getBounds().y < 0) UIUtilities.centerWindowOnScreen(frame);
    frame.setVisible(true);
  }

  /**
   * Sets the main application frame's title.
   *
   * @param title the title
   */
  public void setTitle(String title) {
    if (frame != null) {
      frame.setTitle(title);
    }
  }
}
