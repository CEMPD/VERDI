package saf.core.ui;

import simphony.util.messages.MessageCenter;

import javax.swing.*;
import java.util.*;

/**
 * Encapsulates the order in which items should appear in a
 * menu.
 *
 * @author Nick Collier
 */
public class MenuOrder {

  private static final MessageCenter msg = MessageCenter.getMessageCenter(MenuOrder.class);

  private static final String ITEM_PREFIX = "menu.order.";
  private static final String MENU_KEY = "menu.order";
  private static final String SEPARATOR = "__ZZ__SEPARATOR__ZZ__";

  // key is menu id, value is ordered list of the item ids
  private Map<String, List<String>> items = new HashMap<String, List<String>>();
  private List<String> menus = new ArrayList<String>();

  public MenuOrder() {
  }

  /**
   * Creates a MenuItemOrder from the specified properties.
   *
   * @param props the properties to create the MenuItemOrder from
   */
  public MenuOrder(Properties props) {
    String order = props.getProperty(MENU_KEY, "").trim();

    if (order.length() > 0) {
      StringTokenizer tok = new StringTokenizer(order, ",");
      while (tok.hasMoreTokens()) {
        String id = tok.nextToken().trim();
        addMenu(id);
      }
    }

    Enumeration names = props.propertyNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement().toString();
      if (name.startsWith(ITEM_PREFIX)) {
        parseMenu(name, props.getProperty(name));
      }
    }
  }

  private void parseMenu(String propName, String propValue) {
    String menuID = propName.substring(ITEM_PREFIX.length(), propName.length());
    if (menuID.length() > 0) {
      StringTokenizer tok = new StringTokenizer(propValue, ",:", true);
      while (tok.hasMoreTokens()) {
        String token = tok.nextToken().trim();
        if (token.equals(":")) {
          addItem(menuID, SEPARATOR);
        } else if (!token.equals(",")) {
          addItem(menuID, token);
        }
      }
    }
  }

  /**
   * Adds a menu. Menu ids should be added in the
   * order which they should appear.
   *
   * @param menuID the id to add
   */
  public void addMenu(String menuID) {
    menus.add(menuID);
  }

  /**
   * Adds an item to this MenuItemOrder. Items ids
   * should be added in the order in which they should
   * appear in the menu.
   *
   * @param menuID the menu id
   * @param itemID the item id
   */
  public void addItem(String menuID, String itemID) {
    List<String> list = items.get(menuID);
    if (list == null) {
      list = new ArrayList<String>();
      items.put(menuID, list);
    }
    list.add(itemID);
  }

  /**
   * Orders the menus in the manager according to this
   * MenuItemOrder.
   *
   * @param manager         the manager containing the menus.
   * @param windowMenuLabel label for the window menu, can be ""
   */
  public void orderItems(GUIBarManager manager, String windowMenuLabel) {
    if (menus.indexOf(windowMenuLabel) != -1) {
      manager.createWindowMenu();
      JMenu menu = manager.getMenu(GUIConstants.WINDOW_MENU_ID);
      menu.setText(windowMenuLabel);
    }

    if (menus.size() > 0) {
      JMenuBar menuBar = manager.getMenuBar();
      menuBar.removeAll();
      for (String id : menus) {
        JMenu menu = manager.getMenu(id);
        if (menu == null) menu = manager.getMenuByLabel(id);
        if (menu != null) {
          menuBar.add(menu);
        } else {
          msg.warn("Error while ordering menus. Menu '" + id + "' not found.");
        }
      }
    }

    for (String menuID : items.keySet()) {
      JMenu menu = manager.getMenu(menuID);
      if (menu == null) {
        msg.warn("Error while ordering menus. Menu '" + menuID + "' not found.");
      } else {
        menu.removeAll();
        for (String id : items.get(menuID)) {
          if (id.equals(SEPARATOR)) menu.addSeparator();
          else {
            JMenuItem item = manager.getMenuItem(id);
            if (item == null) {
              msg.warn("Error while ordering menus. MenuItem '" + id + "' not found.");
            } else {
              menu.add(item);
            }
          }
        }
      }
    }
  }
}
