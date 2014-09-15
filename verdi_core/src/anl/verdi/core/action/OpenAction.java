package anl.verdi.core.action;

import java.awt.event.ActionEvent;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;

/**
 * Action for opening files.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class OpenAction extends AbstractSAFAction<VerdiApplication> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6236609575697751046L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		workspace.getApplicationMediator().openProject();
	}
}
