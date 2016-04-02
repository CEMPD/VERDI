package saf.core.ui.dock;

import javax.swing.*;

/**
 * Interface for classes that implement a dockable frame.
 */
public interface DockableFrame {

  /**
   * Adds an arbitrary key/value "client property" to this component.
   *
   * @param key   the new client property key
   * @param value the new client property value; if this is null the property will be removed
   */
  void putClientProperty(Object key, Object value);

  /**
   * Returns the value of the property with the specified key.
   * Only properties added with putClientProperty will return a non-null value.
   *
   * @param key the property key whose value should be returned.
   * 
   * @return the value of the property with the specified key.
   */
  Object getClientProperty(Object key);

  /**
   * Gets the toolbar, if any, for this frame.
   *
   * @return the toolbar for this frame if one exists, otherwise null.
   */
  JToolBar getToolBar();

  /**
   * Adds a toolbar to this frame.
   *
   * @param bar the toolbar to add
   */
  void addToolBar(JToolBar bar);

  /**
   * Gets the panel that contains the component
   * displayed in this frame. By default this component
   * can be retrieved by calling <code>getComponent(0)</code> on the
   * returned panel.<p>
   * <p/>
   * Note that this panel should not contain the toolbar.
   *
   * @return the panel that contains the component
   *         displayed in this frame.
   */
  JPanel getContentPane();

  /**
   * Gets whether or not this DockableFrame is minimized.
   *
   * @return true if this DockableFrame is minimized, otherwise false.
   */
  boolean isMinimized();
  
  /**
   * Gets whether or not this DockableFrame is hidden - possibly by another tab.
   *
   * @return true if this DockableFrame is minimized, otherwise false.
   */
 
  boolean isHidden();

  /**
   * Gets whether or not this DockableFrame is maximized.
   *
   * @return true if this DockableFrame is maximized, otherwise false.
   */
  boolean isMaximized();

  /**
   * Gets whether or not this DockableFrame is floating.
   *
   * @return true if this DockableFrame is floating, otherwise false.
   */
  boolean isFloating();

  /**
   * Gets the unique id for this DockableFrame.
   *
   * @return the unique id for this DockableFrame.
   */
  String getID();

  /**
   * Gets the title text for this DockableFrame.
   *
   * @return the title text for this DockableFrame.
   */
  String getTitle();

  /**
   * Sets the title text for this DockableFrame.
   *
   * @param title the title text for this DockableFrame.
   */
  void setTitle(String title);

  /**
   * Selects this DockableFrame.
   */
  void toFront();

  /**
   * Restores this DockableFrame.
   */
  void restore();

  /**
   * Minimizes this DockableFrame.
   *
   * @param location the location to minimize to
   */
  void minimize(DockingManager.MinimizeLocation location);

  /**
   * Maximizes this DockableFrame.
   */
  void maximize();

  /**
   * Floats this DockableFrame.
   */
  void doFloat();

  /**
   * Close this dockable. Assumes the dockable is
   * in the current perspective.
   */
  void close();

  /**
   * Closes this dockable in the specified perspective.
   *
   * @param perspectiveID the id of the perspective to close
   *                      the dockable in.
   */
  void close(String perspectiveID);
}
