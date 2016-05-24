package saf.core.ui.dock;

import saf.core.ui.GUIBarManager;
import saf.core.ui.ISAFDisplay;

import java.util.List;

/**
 * Interface for classes that create the basic docking components.
 *
 */
public interface DockingFactory {

  /**
   * Gets the ViewManager. If the view manager has not
   * yet been created this should create one using the
   * specified barManager and perspectives.
   *
   * @param barManager the bar manager
   * @param perspectives list of user-defined perspectives
   *
   * @return the ViewManager.
   */
  DockingManager getViewManager(GUIBarManager barManager, List<Perspective> perspectives);

  /**
   * Gets the display.
   * 
   * @return the display.
   */
  ISAFDisplay getDisplay();

  /**
   * Creates a Perspective.
   *
   * @return the created Perspective.
   * @param id the id of the perspective
   * @param name the name of the perspective
   */
  Perspective createPerspective(String id, String name);
}
