package anl.verdi.core.action;

import java.awt.event.ActionEvent;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;

/**
 * Action to undock all docked plots.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class UndockAllAction extends AbstractSAFAction<VerdiApplication> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2897808551442068725L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		workspace.getApplicationMediator().getGui().undockAllPlots();
	}
}
