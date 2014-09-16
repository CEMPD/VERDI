package saf.core.ui;

import simphony.util.ThreadUtilities;

/**
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:55:17 $
 */
public class GUICreator {

  public static ISAFDisplay createDisplay(IAppConfigurator configurator, Workspace workspace) {
    return GUICreatorDelegate.getInstance().createDisplay(configurator, workspace);
  }

  public static void runDisplay(final IAppConfigurator configurator, final ISAFDisplay display) {
    ThreadUtilities.runInEventThread(new Runnable() {
      public void run() {
        GUICreatorDelegate.getInstance().runDisplay(configurator, display);
      }
    });
  }
}
