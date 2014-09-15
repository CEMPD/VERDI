package anl.verdi.core.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;
import anl.verdi.gui.AboutDialog;

/**
 * Action to show the about dialog.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class About extends AbstractSAFAction<VerdiApplication> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8762402527178428690L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		JFrame frame = workspace.getApplicationMediator().getGui().getFrame();
		AboutDialog dialog = new AboutDialog(frame);
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
	}
}
