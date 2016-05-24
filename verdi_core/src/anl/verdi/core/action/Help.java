package anl.verdi.core.action;

import java.awt.event.ActionEvent;

import javax.swing.JFrame;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;
import anl.verdi.gui.HelpDialog;

/**
 * Action to show the help
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Help extends AbstractSAFAction<VerdiApplication> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5473694155061786959L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		JFrame frame = workspace.getApplicationMediator().getGui().getFrame();
		HelpDialog dialog = new HelpDialog(frame);
		dialog.pack();
		dialog.setLocationRelativeTo(frame);
		dialog.setVisible(true);
//		workspace.getApplicationMediator().showHelp();
	}
}
