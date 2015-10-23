package saf.core.ui.actions;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:55:17 $
 */
public class ActionFactory {

  private static ActionFactory instance = new ActionFactory();

  private Map<String, Action> actionMap = new HashMap<String, Action>();

  public static ActionFactory getInstance() {
    return instance;
  }

  protected ActionFactory() {}

  public void registerAction(String id, Action action) {
    actionMap.put(id, action);
  }

  public Action getAction(String id) {
    return actionMap.get(id);
  }
}
