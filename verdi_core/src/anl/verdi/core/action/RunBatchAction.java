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
public class RunBatchAction extends AbstractSAFAction<VerdiApplication> {
	private static final long serialVersionUID = -1667247960539144194L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		workspace.getApplicationMediator().runBatchScript();
	}
}
