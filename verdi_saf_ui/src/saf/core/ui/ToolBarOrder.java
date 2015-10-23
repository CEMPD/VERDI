package saf.core.ui;

import javax.swing.*;
import java.util.*;

/**
 * Encapsulates the order in which actions should appear in a toolbar.
 *
 * @author Nick Collier
 */
public class ToolBarOrder {

  private static final String ORDER_PROP_NAME = "toolbar.group.order";
  private static final String A_ORDER_PROP_NAME = "toolbar.group.order.";
  public static final String SEPARATOR = "__ZZ__SEPARATOR__ZZ__";

  private List<String> groupOrder = new ArrayList<String>();
  private Map<String, List<String>> actionOrder = new HashMap<String, List<String>>();

  public ToolBarOrder() {
  }

  public ToolBarOrder(Properties props) {
    String gOrder = props.getProperty(ORDER_PROP_NAME);
    if (gOrder != null) parseGroupOrder(gOrder);


    Enumeration names = props.propertyNames();
    while (names.hasMoreElements()) {
      String name = names.nextElement().toString();
      parsePossibleActionOrder(name, props.getProperty(name));
    }
  }

  private void parseGroupOrder(String gOrder) {
    StringTokenizer tok = new StringTokenizer(gOrder, ",:", true);
    while (tok.hasMoreTokens()) {
      String id = tok.nextToken().trim();
      if (id.equals(":")) {
        addSeparator();
      } else if (!id.equals(",")) {
        addGroupID(id);
      }
    }
  }

  private void parsePossibleActionOrder(String propName, String propValue) {
    String groupName = parseGroupName(propName);
    if (groupName != null) {
      StringTokenizer tok = new StringTokenizer(propValue, ",", false);
      while (tok.hasMoreTokens()) {
        String id = tok.nextToken().trim();
        addActionID(groupName, id);
      }
    }
  }

  /**
   * Gets an iterable over the ordered list of groups.
   * The order of iteration matches the defined order.
   *
   * @return an iterable over the ordered list of groups.
   */
  public Iterable<String> groupsInOrder() {
    return groupOrder;
  }

  /**
   * Gets an iterable over the ordered list of actions.
   * for the specified group. The order of iteration
   * matches the defined order.
   *
   * @param groupID the id of the groups whose actions we want
   *
   * @return an iterable over the ordered list of groups.
   */
  public Iterable<String> actionsInOrder(String groupID) {
    return actionOrder.get(groupID);
  }

  /**
   * Adds a group id to the ordered list of groups. These should
   * be added sequentially in the order in which the
   * toolbar groups should be displayed.
   *
   * @param id the group id to add
   */
  public void addGroupID(String id) {
    groupOrder.add(id);
  }

  /**
   * Adds a separator to the ordered list of groups.
   * The separator will appear after the group given
   * in the most recent call to <code>addGroupID</code>.
   */
  public void addSeparator() {
    groupOrder.add(SEPARATOR);
  }


  /**
   * Adds the actionID to the order list of actions for the
   * specified group. These should be add sequentially in the
   * order in which the actions should appear in the specified
   * group.
   *
   * @param groupID  the group to whose list of actions the actionID is added
   * @param actionID the id to add
   */
  public void addActionID(String groupID, String actionID) {
    List<String> actions = actionOrder.get(groupID);
    if (actions == null) {
      actions = new ArrayList<String>();
      actionOrder.put(groupID, actions);
    }
    actions.add(actionID);
  }

  private String parseGroupName(String propName) {
    propName = propName.trim();
    if (propName.startsWith(A_ORDER_PROP_NAME) && propName.length() > A_ORDER_PROP_NAME.length()) {
      return propName.substring(A_ORDER_PROP_NAME.length(), propName.length());
    }
    return null;
  }
}
