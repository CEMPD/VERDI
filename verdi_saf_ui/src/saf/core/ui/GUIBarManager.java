package saf.core.ui;

import static saf.core.ui.GUIConstants.WINDOW_MENU_ID;
import saf.core.ui.actions.SetPerspectiveAction;
import saf.core.ui.dock.DockingManager;
import saf.core.ui.dock.Perspective;
import simphony.util.messages.MessageCenter;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Managers menu, tool and status bar configuration and manipulation. The toolbar
 * is divided into named groups divided by separators. This allows buttons
 * to be grouped in the toolbar.
 *
 * @author Nick Collier
 */
public class GUIBarManager {

  private static final String MENU_ID = "MENU_ID";
  private static final MessageCenter msg = MessageCenter.getMessageCenter(GUIBarManager.class);

  private Map<String, Component> toolBarMap = new HashMap<String, Component>();
  // group name -> list of comps in that group
  private Map<String, BarGroup> toolBarGroupMap = new HashMap<String, BarGroup>();
  private Map<String, JMenuItem> menuItemMap = new HashMap<String, JMenuItem>();
  private Map<String, JMenu> menuMap = new HashMap<String, JMenu>();
  private Map<String, StatusBarItem> statusBarMap = new HashMap<String, StatusBarItem>();
  private List<ActionDescriptor> windowItems = new ArrayList<ActionDescriptor>();

  private ButtonGroup windowButtonGroup = new ButtonGroup();

  private JMenuBar menuBar;
  private JToolBar toolBar;
  private JPanel statusBar = new JPanel();


  private class BarGroup {
    List<Component> list = new ArrayList<Component>();

    void sort(String groupID, ToolBarOrder order) {

      Iterable<String> iterable = order.actionsInOrder(groupID);
      // iterable may be null if its a group with no actions in it yet
      // or the order for that group was not defined
      if (iterable != null) {
        List<Component> tmp = new ArrayList<Component>(list);
        list.clear();
        for (String compID : iterable) {
          Component comp = toolBarMap.get(compID);
          if (comp == null) msg.warn("Error while ordering toolbar items. ID '" + compID + "' not found");
          else {
            list.add(comp);
            tmp.remove(comp);
          }
        }
        // add anything not specified to the end
        list.addAll(tmp);
      }
    }

    public void addComponent(Component comp, JToolBar bar) {
      if (list.size() == 0) {
        bar.add(comp);
      } else {
        Component lastComp = list.get(list.size() - 1);
        int index = bar.getComponentIndex(lastComp);
        if (lastComp instanceof JToolBar.Separator) {
          bar.add(comp, index);
        } else {
          if (index == bar.getComponentCount() - 1) {
            bar.add(comp);
          } else {
            bar.add(comp, index + 1);
          }
        }
      }
      list.add(comp);
    }

    void addAll(JToolBar bar) {
      for (Component comp : list) {
        bar.add(comp);
      }
    }

    public void addSeparator(JToolBar toolBar) {
      if (list.size() == 0) {
        toolBar.addSeparator();
      } else {
        Component lastComp = list.get(list.size() - 1);
        JToolBar.Separator separator = new JToolBar.Separator();
        int index = toolBar.getComponentIndex(lastComp);
        if (index == toolBar.getComponentCount() - 1) {
          toolBar.add(separator);
        } else {
          toolBar.add(separator, index + 1);
        }
      }
    }
  }

  Comparator<JMenuItem> jMenuItemComparator = new Comparator<JMenuItem>() {
    public int compare(JMenuItem jMenuItem, JMenuItem jMenuItem1) {
      return jMenuItem.getText().compareTo(jMenuItem1.getText());
    }
  };

  /**
   * Creates a GUIBarManager managing the specified tool and menu bars.
   *
   * @param toolBar the toolbar to manage
   * @param menuBar the menubar to manage
   */
  public GUIBarManager(JToolBar toolBar, JMenuBar menuBar) {
    this.toolBar = toolBar;
    this.menuBar = menuBar;
  }

  /**
   * Gets the JMenu with the specified label.
   *
   * @param label the menu's label.
   * @return the JMenu with the specified label.
   */
  JMenu getMenuByLabel(String label) {
    for (JMenu menu : menuMap.values()) {
      if (menu.getText().equals(label)) return menu;
    }
    return null;
  }


  /**
   * Creates the help menu.
   */
  public void createHelpMenu() {
    JMenu menu = getMenu(GUIConstants.HELP_MENU_ID);
    if (menu == null) {
      menu = addMenu(GUIConstants.HELP_MENU_ID, "Help");
      menu.setMnemonic('h');
    }
  }

  /**
   * Creates the window menu.
   */
  public void createWindowMenu() {
    // user may have defined a windows menu
    JMenu menu = getMenu(GUIConstants.WINDOW_MENU_ID);
    if (menu == null) {
      menu = addMenu(GUIConstants.WINDOW_MENU_ID, "Window");
      menu.setMnemonic('w');
    }
  }

  /**
   * Creates the perspective menu.
   *
   * @param vManager the docking manager that contains the perspectiv einfo
   */
  public void createPerspectiveMenu(DockingManager vManager) {
    JMenu menu = getMenu(GUIConstants.PERSPECTIVE_MENU_ID);
    if (menu == null) {
      menu = addMenu(GUIConstants.PERSPECTIVE_MENU_ID, "Perspective");
      menu.setMnemonic('p');
    }

    if (menu.getItemCount() == 0) {
      ButtonGroup grp = new ButtonGroup();
      for (String id : vManager.getPerspectiveIDs()) {
        Perspective perspective = vManager.getPerspective(id);

        JMenuItem item = new JCheckBoxMenuItem();
        grp.add(item);
        if (perspective.equals(vManager.getPerspective())) item.setSelected(true);
        // set the action after we set selected so we don't fire the action
        item.setAction(new SetPerspectiveAction(perspective.getID(), perspective.getName(), vManager));
        addMenuItem(perspective.getID(), GUIConstants.PERSPECTIVE_MENU_ID, item);
      }
      sortMenu(GUIConstants.PERSPECTIVE_MENU_ID);
    }
  }


  /**
   * Sets the toolbar's order
   *
   * @param order the order of the groups and toolbar actions
   */
  void setToolbarOrder(ToolBarOrder order) {
    toolBar.removeAll();
    for (String id : toolBarGroupMap.keySet()) {
      BarGroup group = toolBarGroupMap.get(id);
      group.sort(id, order);
    }


    for (String id : order.groupsInOrder()) {
      if (id.equals(ToolBarOrder.SEPARATOR)) {
        toolBar.addSeparator();
      } else {
        BarGroup group = toolBarGroupMap.get(id);
        if (group != null) {
          group.addAll(toolBar);
        } else {
          msg.warn("Error while order toolbar groups. ID '" + id + "' not found");
        }
      }
    }
  }

  /**
   * Sets the menu's order
   *
   * @param order a String defining the order
   */
  void setMenuOrder(String order) {
    if (order != null && order.trim().length() > 0) {
      menuBar.removeAll();
      StringTokenizer tok = new StringTokenizer(order, ",");
      while (tok.hasMoreTokens()) {
        String label = tok.nextToken().trim();
        JMenu menu = menuMap.get(label);
        if (menu == null) menu = getMenuByLabel(label);
        if (menu != null) {
          menuBar.add(menu);
        }
      }
    }
  }

  /**
   * Adds a separator to the toolbar.
   */
  public void addToolBarSeparator() {
    toolBar.addSeparator();
  }

  /**
   * Adds a separator of the specified size to the toolbar.
   *
   * @param size the size of the separator
   */
  public void addToolBarSeparator(Dimension size) {
    toolBar.addSeparator(size);
  }

  /**
   * Adds a tool bar separator to the specified tool bar group.
   *
   * @param groupID the name of the group
   */
  public void addToolBarSeparator(String groupID) {
    BarGroup group = toolBarGroupMap.get(groupID);
    if (group == null) {
      group = new BarGroup();
      toolBarGroupMap.put(groupID, group);
    }
    group.addSeparator(toolBar);
  }

  /**
   * Adds the specified component to the specified group.
   *
   * @param groupID   the group to add the component to
   * @param id        the id of the component
   * @param component the component to add
   * @return the added component
   */
  public Component addToolBarComponent(String groupID, String id, Component component) {
    toolBarMap.put(id, component);

    BarGroup group = toolBarGroupMap.get(groupID);
    if (group == null) {
      group = new BarGroup();
      toolBarGroupMap.put(groupID, group);
    }
    group.addComponent(component, toolBar);
    return component;
  }

  /**
   * Adds the specified action to the toolbar as part of the specified
   * group.
   *
   * @param groupID the group to add the action to
   * @param id      the id of the action
   * @param action  the action to
   * @return the JButton associated with the action
   */
  public JButton addToolBarAction(String groupID, String id, Action action) {
    // need to add it and remove so we get the kind of button
    // that toolbar likes for an action
    JButton button = toolBar.add(action);
    toolBar.remove(button);
    addToolBarComponent(groupID, id, button);
    return button;
  }

  /**
   * Gets the toolbar component with the specified id. The id
   * is specified when the component is added.
   *
   * @param id the id of the component to return
   * @return the toolbar component with the specified id.
   */
  public Component getToolBarComponent(String id) {
    return toolBarMap.get(id);
  }

  /**
   * Gets all the toolbar components in the specified group.
   *
   * @param groupId the id of the group of components to return
   * @return all the toolbar components in the specified group.
   */
  public Collection<Component> getComponents(String groupId) {
    BarGroup group = toolBarGroupMap.get(groupId);
    return Collections.unmodifiableCollection(group.list);
  }

  /**
   * Adds a menu with the specified id and name.
   *
   * @param id       the id that uniquely identifies the menu
   * @param menuName the name or label of the mnu
   * @return the created JMenu
   */
  public JMenu addMenu(String id, String menuName) {
    JMenu menu = new JMenu(menuName);
    menu.putClientProperty(MENU_ID, id);
    menuBar.add(menu);
    menuMap.put(id, menu);

    return menu;
  }

  /**
   * Updates the window menu, adding the
   * user's window actions.
   */
  public void updateWindowMenu() {
    for (ActionDescriptor descriptor : windowItems) {
      JMenuItem item = descriptor.createMenuItem(this);
      String id = (String) item.getClientProperty(MENU_ID);
      this.removeMenuItem(id);

      JMenuItem newItem = new JCheckBoxMenuItem(item.getAction());
      newItem.setText(item.getText());
      newItem.setMnemonic(item.getMnemonic());
      newItem.putClientProperty(MENU_ID, id);
      menuItemMap.put(id, newItem);
      menuMap.get(WINDOW_MENU_ID).add(newItem);
      windowButtonGroup.add(newItem);
    }
  }

  /**
   * Adds an action to be added to the window menu. If the window
   * menu does not yet exist, then the item will be added when its created.
   *
   * @param descriptor the action
   */
  public void addWindowItem(ActionDescriptor descriptor) {
    if (menuMap.containsKey(WINDOW_MENU_ID)) {
      descriptor.createMenuItem(this);
    } else {
      windowItems.add(descriptor);
    }
  }

  /**
   * Adds a menu to the specified parent menu as a submenu.
   *
   * @param parentMenu the parent menu
   * @param id         the id uniquely identifying the submenu
   * @param menuName   the name or label of the submenu
   * @return the created JMenu
   */
  public JMenu addMenu(JMenu parentMenu, String id, String menuName) {
    JMenu menu = new JMenu(menuName);
    parentMenu.add(menu);
    menu.putClientProperty(MENU_ID, id);
    menuMap.put(id, menu);
    return menu;
  }

  /**
   * Selects the specified menu item. Note that depending on the
   * item, this may have the same effect as clicking that menu item.
   *
   * @param id the id of the item to select
   */
  public void selectMenuItem(String id) {
    JMenuItem item = menuItemMap.get(id);
    if (item != null) item.setSelected(true);
  }

  /**
   * Adds the specified action as a menu item to the specified menu.
   *
   * @param id     an id that will uniquely identify the menu item
   * @param menuID the id of the menu to add the item to
   * @param action the action that will occur when the menu item
   *               is clicked
   * @return the created menu item.
   */
  public JMenuItem addMenuItem(String id, String menuID, Action action) {
    JMenuItem item;
    if (menuID.equals(WINDOW_MENU_ID)) {
      item = new JCheckBoxMenuItem(action);
      menuMap.get(menuID).add(item);
      windowButtonGroup.add(item);
    } else {
      item = menuMap.get(menuID).add(action);
    }
    return addMenuItem(item, id);
  }

  /**
   * Adds a menu item to the specified menu.
   *
   * @param id            an id that will uniquely identify the menu item
   * @param menuID        the id of the menu to add the item to
   * @param menuItemLabel the label of the menu item
   * @return the created menu item.
   */
  public JMenuItem addMenuItem(String id, String menuID, String menuItemLabel) {
    JMenuItem item = menuMap.get(menuID).add(menuItemLabel);
    return addMenuItem(item, id);
  }

  /**
   * Adds the specified menu item to the specified menu.
   *
   * @param id     an id that will uniquely identify the menu item
   * @param menuId the id of the menu to add the item to
   * @param item   the menu item to add
   * @return the menu item
   */
  public JMenuItem addMenuItem(String id, String menuId, JMenuItem item) {
    item = menuMap.get(menuId).add(item);
    return addMenuItem(item, id);
  }

  private JMenuItem addMenuItem(JMenuItem item, String id) {
    item.putClientProperty(MENU_ID, id);
    menuItemMap.put(id, item);
    return item;
  }

  /**
   * Adds the specified action as a menu item to the specified menu.
   *
   * @param id     an id that will uniquely identify the menu item
   * @param menu   the menu to add the item to
   * @param action the action that will occur when the menu item
   *               is clicked
   * @return the created menu item.
   */
  public JMenuItem addMenuItem(String id, JMenu menu, Action action) {
    return addMenuItem(id, (String) menu.getClientProperty(MENU_ID), action);
  }

  /**
   * Adds a menu item to the specified menu.
   *
   * @param id            an id that will uniquely identify the menu item
   * @param menu          the menu to add the item to
   * @param menuItemLabel the label of the menu item
   * @return the created menu item.
   */
  public JMenuItem addMenuItem(String id, JMenu menu, String menuItemLabel) {
    return addMenuItem(id, (String) menu.getClientProperty(MENU_ID), menuItemLabel);
  }

  /**
   * Alphabetically sorts the menu entries in the specified menu.
   *
   * @param menuID the id of the menu to sort
   */
  public void sortMenu(String menuID) {
    JMenu menu = getMenu(menuID);
    sortMenu(menu);
  }

  private void sortMenu(JMenu menu) {
    if (menu != null) {
      List<JMenuItem> items = new ArrayList<JMenuItem>();
      for (int i = 0, n = menu.getItemCount(); i < n; i++) {
        JMenuItem item = menu.getItem(i);
        if (item instanceof JMenu) {
          sortMenu((JMenu) item);
        }
        items.add(item);
      }
      menu.removeAll();
      Collections.sort(items, jMenuItemComparator);
      for (JMenuItem item : items) {
        menu.add(item);
      }
    }
  }

  /**
   * Gets the menu with the specified id.
   *
   * @param menuID the id of the menu to return
   * @return the menu with the specified id.
   */
  public JMenu getMenu(String menuID) {
    return menuMap.get(menuID);
  }

  /**
   * Gets the menu item with the specified id.
   *
   * @param id the id of the menu to return
   * @return the menu item with the specified id.
   */
  public JMenuItem getMenuItem(String id) {
    return menuItemMap.get(id);
  }

  /**
   * Adds a separator to the specified menu.
   *
   * @param menuID the id of the menu
   */
  public void addMenuSeparator(String menuID) {
    menuMap.get(menuID).addSeparator();
  }

  /**
   * Gets the managed tool bar.
   *
   * @return the managed tool bar.
   */
  public JToolBar getToolBar() {
    return toolBar;
  }

  /**
   * Gets the managed menu.
   *
   * @return the managed menu.
   */
  public JMenuBar getMenuBar() {
    return menuBar;
  }


  /**
   * Removes the specified menu.
   *
   * @param menuID the id of the menu to remove
   */
  public void removeMenu(String menuID) {
    JMenu menu = menuMap.remove(menuID);
    removeMenu(menu);
  }

  /**
   * Empties the specified menu of any menu items.
   *
   * @param menuID the id of the menu clear
   *
   * @return the cleared menu
   */
  public JMenu clearMenu(String menuID) {
    if (menuID.equals(WINDOW_MENU_ID)) {
      windowButtonGroup = new ButtonGroup();
    }
    JMenu menu = menuMap.get(menuID);
    if (menu != null) {
      List<JMenu> subMenus = new ArrayList<JMenu>();

      for (int i = 0, n = menu.getItemCount(); i < n; i++) {
        JMenuItem item = menu.getItem(i);
        if (item instanceof JMenu) {
          subMenus.add((JMenu) item);
        } else {
          menuItemMap.remove(item.getClientProperty(MENU_ID).toString());
        }
      }

      for (JMenu aMenu : subMenus) {
        removeMenu(aMenu);
      }
      menu.removeAll();
    }
    return menu;
  }

  private void removeMenu(JMenu menu) {
    List<JMenu> subMenus = new ArrayList<JMenu>();
    if (menu != null) {
      for (int i = 0, n = menu.getItemCount(); i < n; i++) {
        JMenuItem item = menu.getItem(i);
        if (item instanceof JMenu) {
          subMenus.add((JMenu) item);
        } else {
          menuItemMap.remove(item.getClientProperty(MENU_ID).toString());
        }
      }

      for (JMenu aMenu : subMenus) {
        removeMenu(aMenu);
      }
      menu.removeAll();
      menu.getParent().remove(menu);
      menuBar.remove(menu);
      menuMap.remove(menu.getClientProperty(MENU_ID).toString());
    }
  }

  /**
   * Removes the specified menu item.
   *
   * @param id the id of the menu item.
   */
  public void removeMenuItem(String id) {
    JMenuItem item = menuItemMap.remove(id);
    if (item != null) {
      Container parent = item.getParent();
      if (parent != null) parent.remove(item);
    }
  }

  /**
   * Gets the status bar that contains all the user defined
   * status fields.
   *
   * @return the status bar that contains all the user defined
   *         status fields.
   */
  public JPanel getStatusBar() {
    return statusBar;
  }

  void setStatusBar(JPanel panel, Map<String, StatusBarItem> statusBarMap) {
    statusBar = panel;
    this.statusBarMap = statusBarMap;
  }

  /**
   * Gets the named status bar component.
   *
   * @param statusFieldName the status field name
   * @return the named status bar component.
   */
  public JComponent getStatusBarComponent(String statusFieldName) {
    StatusBarItem item = statusBarMap.get(statusFieldName);
    if (item != null) {
      return item.getComponent();
    } else {
      return null;
    }
  }

  /**
   * Sets the status bar field to contain the specified component.
   *
   * @param statusFieldName the name (id) of the status field
   * @param comp            the component to use
   */
  public void setStatusBarComponent(String statusFieldName, JComponent comp) {
    StatusBarItem item = statusBarMap.get(statusFieldName);
    if (item != null) {
      statusBar.remove(item.getComponent());
      statusBar.add(comp, item.getLayoutString());
      statusBarMap.put(statusFieldName, new StatusBarItem(item.getName(), item.getLayoutString(),
              comp));
      statusBar.revalidate();
      statusBar.repaint();
    }
  }

  /**
   * Sets the foreground (eg text color) of the specified status field.
   *
   * @param statusFieldName the status field whose color we want to set
   * @param color           the color to set the field to
   */
  public void setStatusBarTextColor(String statusFieldName, Color color) {
    StatusBarItem item = statusBarMap.get(statusFieldName);
    if (item != null) {
      JComponent component = item.getComponent();
      component.setForeground(color);
    }
  }

  /**
   * Sets the font of the specified status field.
   *
   * @param statusFieldName the status field whose color we want to set
   * @param font            the new font for the status bar field
   */
  public void setStatusBarFont(String statusFieldName, Font font) {
    StatusBarItem item = statusBarMap.get(statusFieldName);
    if (item != null) {
      JComponent component = item.getComponent();
      component.setFont(font);
    }
  }

  /**
   * Gets the foreground (eg text color) of the specified status field.
   *
   * @param statusFieldName the status field whose color we want to get
   * @return the foreground (eg text color) of the specified status field, or null
   *         if the name status field is not found
   */
  public Color getStatusBarTextColor(String statusFieldName) {
    StatusBarItem item = statusBarMap.get(statusFieldName);
    if (item != null) {
      JComponent component = item.getComponent();
      return component.getForeground();
    }

    return null;
  }

  /**
   * Sets the text of the specified status field. If the actual
   * status bar component is not a text field this will do nothing.
   *
   * @param statusFieldName the status field whose text we want to set.
   * @param text            the text to put in the status field
   */
  public void setStatusBarText(String statusFieldName, String text) {
    StatusBarItem item = statusBarMap.get(statusFieldName);
    if (item != null) {
      JComponent component = item.getComponent();
      if (component instanceof JTextComponent) {
        ((JTextField) component).setText(text);
      } else if (component instanceof JLabel) {
        ((JLabel) component).setText(text);
      }
    }
  }
}
