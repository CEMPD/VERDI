package saf.core.ui;

import saf.core.ui.dock.*;

import java.util.List;
import java.util.ArrayList;

/**
 * Default implementation of docking factory that uses the Docking Frames
 * library for its docking behavior.
 *
 * @author Nick Collier
 */
public class DefaultDockingFactory implements DockingFactory {

  private DockingManager vManager;
  private DefaultSAFDisplay display;

  /**
   * Gets the DockingManager. If the docking manager has not
   * yet been created this should create one using the
   * specified barManager and perspectives.
   *
   * @param barManager   the bar manager
   * @param perspectives list of user-defined perspectives
   * @return the ViewManager.
   */
  public DockingManager getViewManager(GUIBarManager barManager, List<Perspective> perspectives) {
    if (vManager == null) {
      DefaultSAFDisplay disp = (DefaultSAFDisplay) getDisplay();
      List<DefaultPerspective> defPs = new ArrayList<DefaultPerspective>();
      for (Perspective p : perspectives) {
        defPs.add((DefaultPerspective) p);
      }

      vManager = new DefaultDockingManager(barManager, disp.getCControl(), defPs);
    }
    return vManager;
  }

  /**
   * Gets the display.
   *
   * @return the display.
   */
  public ISAFDisplay getDisplay() {
    if (display == null) {
      display = new DefaultSAFDisplay();
    }
    return display;
  }


  /**
   * Creates a Perspective.
   *
   * @param id the unique id of the perspective
   * @param name the name of the perspective.
   *
   * @return the created Perspective.
   */
  public Perspective createPerspective(String id, String name) {
    if (display == null) getDisplay();
    return new DefaultPerspective(id, name, display.getCControl());
  }
}
