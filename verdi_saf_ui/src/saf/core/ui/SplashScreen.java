package saf.core.ui;


import saf.core.ui.util.UIUtilities;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

/**
 * Implements a start up splash screen with a progress bar. The splash screen consists
 * of an image or JPanel, an optional progress bar / status bar.
 *
 * @author Nick Collier
 * @version $Revision: 1.3 $ $Date: 2005/11/21 18:55:17 $
 */
public class SplashScreen extends JPanel {

  private JProgressBar bar = new JProgressBar();
  private JLabel status = new JLabel();
  private JWindow window;

  /**
   * Creates a SplashScreen using the specified image url and
   * progress bar.
   *
   * @param imageURL     the url of the splash screen image
   * @param showProgress whether or not to show a progress bar
   */
  public SplashScreen(URL imageURL, boolean showProgress) {
    this(new JLabel(new ImageIcon(imageURL)), showProgress);
  }

  /**
   * Creates a SplashScreen using the specified image url and
   * progress bar.
   *
   * @param image        the splash screen image
   * @param showProgress whether or not to show a progress bar
   */
  public SplashScreen(Image image, boolean showProgress) {
    this(new JLabel(new ImageIcon(image)), showProgress);
  }

  /**
   * Creates a SplashScreen using the specified panel
   * and progress bar.
   *
   * @param panel the panel to show
   * @param showProgress whether or not to show a progress bar.
   */
  public SplashScreen(JPanel panel, boolean showProgress) {
    this((JComponent)panel, showProgress);
  }

  /**
   * Creates a SplashScreen using the specified image url and
   * progress bar.
   *
   * @param comp        the splash screen component
   * @param showProgress whether or not to show a progress bar
   */
  private SplashScreen(JComponent comp, boolean showProgress) {
    super(new BorderLayout());
    this.add(comp, BorderLayout.NORTH);
    if (showProgress) {
      int width = comp.getPreferredSize().width;
      JPanel panel = new JPanel();
      bar.setAlignmentX(Component.LEFT_ALIGNMENT);
      status.setAlignmentX(Component.LEFT_ALIGNMENT);
      panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
      panel.add(Box.createRigidArea(new Dimension(0, 20)));

      JPanel p = new JPanel(new BorderLayout());
      p.setAlignmentX(Component.LEFT_ALIGNMENT);
      p.setBorder(BorderFactory.createEmptyBorder(0, width / 4, 0, width / 4));
      p.add(bar, BorderLayout.CENTER);
      panel.add(p);

      p = new JPanel(new BorderLayout());
      p.setAlignmentX(Component.LEFT_ALIGNMENT);
      p.add(status);
      p.setBorder(BorderFactory.createEmptyBorder(0, width / 4, 0, 0));
      panel.add(Box.createRigidArea(new Dimension(0, 4)));
      panel.add(p);
      panel.add(Box.createRigidArea(new Dimension(0, 6)));

      add(panel, BorderLayout.SOUTH);
    }
  }

  /**
   * Sets the progress bar's indeterminate mode.
   *
   * @param val true if bar should be indeterminate, otherwise false
   */
  public void setBarIndeterminate(boolean val) {
    bar.setIndeterminate(val);
  }

  /**
   * Sets the maximum value of the progress bar.
   *
   * @param max the maximum value of the progress bar
   */
  public void setMaxProgress(int max) {
    bar.setMaximum(max);
  }

  /**
   * Sets the minimum value of the progress bar
   *
   * @param min the minimum value of the progress bar
   */
  public void setMinimumProgesss(int min) {
    bar.setMaximum(min);
  }

  /**
   * Updates to the progress bar to the new value
   *
   * @param value the new value of the progress bar
   */
  public void setProgress(final int value) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        bar.setValue(value);
      }
    });
  }

  /**
   * Update the progress bar to the new value and sets the text
   * displayed in the bar.
   *
   * @param value   the new progress bar value
   * @param message the text to display in the bar
   */
  public void setProgress(final int value, final String message) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        bar.setValue(value);
        bar.setStringPainted(true);
        bar.setString(message);
      }
    });
  }

  /**
   * Updates the splash screen status.
   *
   * @param status the status to display
   */
  public void setStatus(final String status) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        SplashScreen.this.status.setText(status);
      }
    });
  }

  /**
   * Updates the progress bar value and status text.
   *
   * @param value  the new progress bar value
   * @param status the new status text
   */
  public void setProgressStatus(final int value, final String status) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        bar.setValue(value);
        SplashScreen.this.status.setText(status);
      }
    });
  }

  /**
   * Updates the progress bar value, progress bar message text and status text.
   *
   * @param value   the new progress bar value
   * @param message the new progress bar message text
   * @param status  the new status text
   */
  public void setProgressStatus(final int value, final String message, final String status) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        bar.setValue(value);
        bar.setStringPainted(true);
        bar.setString(message);
        SplashScreen.this.status.setText(status);
      }
    });
  }

  /**
   * Sets the progress bar message text.
   *
   * @param message the new progress bar message text.
   */
  public void setProgressMessage(final String message) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        bar.setStringPainted(true);
        bar.setString(message);
      }
    });
  }

  /**
   * Sets the whether or not the progress bar is indeterminate.
   *
   * @param indeterminate whether or not the bar is indeterminate
   */
  public void setProgressBarIndeterminate(boolean indeterminate) {
    bar.setIndeterminate(indeterminate);
  }

  /**
   * Displays the splash screen.
   *
   * @return the JWindow container for the splash screen.
   */
  public JWindow display() {
    window = new JWindow();
    window.setAlwaysOnTop(true);
    window.getContentPane().setLayout(new BorderLayout());
    window.getContentPane().add(this, BorderLayout.CENTER);
    window.pack();
    UIUtilities.centerWindowOnScreen(window);
    window.setVisible(true);
    return window;
  }

  /**
   * Closes the splash screen.
   */
  public void close() {
    if (window != null) window.dispose();
  }
}
