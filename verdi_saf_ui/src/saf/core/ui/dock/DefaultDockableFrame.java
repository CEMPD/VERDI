package saf.core.ui.dock;


import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.intern.DefaultCDockable;
import bibliothek.gui.dock.common.mode.ExtendedMode;

/**
 * Default implementation of a dockable frame delegating to
 * a Docking Frames' CDockable.
 *
 * @author Nick Collier
 */
public class DefaultDockableFrame implements DockableFrame {

  private DefaultCDockable dockable;
  private StateChanger changer;
  private String id;
  private JPanel panel, contentPane;
  private JToolBar bar;

  // client property map
  private Map<Object, Object> propMap = new HashMap<Object, Object>();

  /**
   * Creates a DefaultDockingFrame that will display
   * the specified component. This will create a DefaultSingleCDockable
   * and adapt that the DockableFrame interface.
   *
   * @param id this DefaultDockableFrame's unique id.
   * @param comp the component to display in this frame
   * @param changer the StateChanger to use when changing the
   * dockable's state
   */
  public DefaultDockableFrame(String id, JComponent comp, StateChanger changer) {
    this.changer = changer;
    this.id = id;
    panel = new JPanel(new BorderLayout());
    contentPane = new JPanel(new GridLayout(1, 1));
    contentPane.add(comp);
    panel.add(contentPane, BorderLayout.CENTER);
    dockable = new DefaultSingleCDockable(id, panel);
  }

  /**
   * Gets the panel that contains the component
   * displayed in this frame. By default this component
   * can be retrieved by calling getComponent(0) on the
   * returned panel.<p>
   *
   * Note that this panel should not contain the toolbar.
   *
   * @return the panel that contains the component
   * displayed in this frame.
   */
  public JPanel getContentPane() {
    return contentPane;
  }

  /**
   * Adds a toolbar to this frame.
   *
   * @param bar the toolbar to add
   */
  public void addToolBar(JToolBar bar) {
    this.bar = bar;
    panel.add(bar, BorderLayout.NORTH);
  }

  /**
   * Gets the toolbar, if any, for this frame.
   *
   * @return the toolbar for this frame if one exists, otherwise null.
   */
  public JToolBar getToolBar() {
    return bar;
  }

  /**
   * Gets the wrapped DefaultCDockable.
   *
   * @return the wrapped DefaultCDockable.
   */
  protected DefaultCDockable getDockable() {
    return dockable;
  }


  /**
   * Gets whether or not this DockableFrame is minimized.
   *
   * @return true if this DockableFrame is minimized, otherwise false.
   */
  public boolean isMinimized() {
    return dockable.getExtendedMode().equals(ExtendedMode.MINIMIZED);
  }

  /**
   * Gets whether or not this DockableFrame is maximized.
   *
   * @return true if this DockableFrame is maximized, otherwise false.
   */
  public boolean isMaximized() {
    return  dockable.getExtendedMode().equals(ExtendedMode.MAXIMIZED);
  }

  /**
   * Gets whether or not this DockableFrame is floating.
   *
   * @return true if this DockableFrame is floating, otherwise false.
   */
  public boolean isFloating() {
    return dockable.getExtendedMode().equals(ExtendedMode.EXTERNALIZED);
  }

  /**
   * Gets the unique id for this DockableFrame.
   *
   * @return the unique id for this DockableFrame.
   */
  public String getID() {
    return id;
  }

  /**
   * Gets the title text for this DockableFrame.
   *
   * @return the title text for this DockableFrame.
   */
  public String getTitle() {
    return dockable.getTitleText();
  }

  /**
   * Sets the title text for this DockableFrame.
   *
   * @param title the title text for this DockableFrame.
   */
  public void setTitle(String title) {
    dockable.intern().setTitleText(title);
  }

  /**
   * Floats this DockableFrame.
   */
  public void doFloat() {
    if (!isFloating()) changer.floatDockable(dockable);
  }

  /**
   * Close this dockable. Assumes the dockable is
   * in the current perspective.
   */
  public void close() {
    changer.closeDockable(dockable);
  }

  /**
   * Closes this dockable in the specified perspective.
   *
   * @param perspectiveID the id of the perspective to close
   *                      the dockable in.
   */
  public void close(String perspectiveID) {
    changer.closeDockable(perspectiveID, dockable);
  }

  /**
   * Maximizes this DockableFrame.
   */
  public void maximize() {
    changer.maximizeDockable(dockable);
  }

  /**
   * Minimizes this DockableFrame.
   *
   * @param location the location to minimize to
   */
  public void minimize(DockingManager.MinimizeLocation location) {
    changer.minimizeDockable(dockable, location);
  }

  /**
   * Restores this DockableFrame.
   */
  public void restore() {
    changer.restoreDockable(dockable);
  }

  /**
   * Selects this DockableFrame.
   */
  public void toFront() {
	  changer.fixFocusPolicy(this);
	  dockable.toFront();
  }

  /**
   * Returns the value of the property with the specified key.
   * Only properties added with putClientProperty will return a non-null value.
   *
   * @param key the property key whose value should be returned.
   * @return the value of the property with the specified key.
   */
  public Object getClientProperty(Object key) {
    return propMap.get(key);
  }

  /**
   * Adds an arbitrary key/value "client property" to this component.
   *
   * @param key   the new client property key
   * @param value the new client property value; if this is null the property will be removed
   */
  public void putClientProperty(Object key, Object value) {
    if (value == null) propMap.remove(key);
    else propMap.put(key, value);
  }
}
