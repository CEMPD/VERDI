package saf.core.ui.actions;

import java.awt.event.ActionEvent;import javax.swing.JFrame;import saf.core.ui.AppPreferences;import saf.core.ui.IAppConfigurator;

/** * @author Nick Collier */
public class ExitAction extends AbstractSAFAction {
/**	 * 	 */	private static final long serialVersionUID = -8799759286194850073L;private IAppConfigurator configurator;
  private JFrame frame;
  private AppPreferences prefs;
  public ExitAction(IAppConfigurator configurator, JFrame frame, AppPreferences prefs) {    this.configurator = configurator;    this.frame = frame;    this.prefs = prefs;  }
  public void actionPerformed(ActionEvent e) {    if (configurator.preWindowClose()) {      prefs.saveApplicationBounds(frame.getBounds());      if (prefs.usingSavedViewLayout()) prefs.saveViewLayout();      frame.dispose();      configurator.postWindowClose();      System.exit(0);    }  }}