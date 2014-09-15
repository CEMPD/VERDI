package anl.verdi.core.action;

import java.awt.event.ActionEvent;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;

/**
 * Action to animate tile plots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class AnimatePlots extends AbstractSAFAction<VerdiApplication> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4989573220773788245L;

	/**
	 * Invoked when an action occurs.
	 */
	public void actionPerformed(ActionEvent e) {
		workspace.getApplicationMediator().animateTilePlots();
	}
}
