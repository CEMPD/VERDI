package saf.core.ui;

import saf.core.ui.actions.ISAFAction;
import saf.core.ui.actions.ActionFactory;

/**
 * Lazily instantiates the exit action.
 *
 * @author Nick Collier
 * @version $Revision: 1.1 $ $Date: 2005/12/22 20:15:39 $
 */
public class ExitActionDescriptor extends ActionDescriptor {

	public ExitActionDescriptor(String id) {
		super(id, null);
		setLabel("E&xit");
	}

	public void fillBars(GUIBarManager barManager, Workspace workspace) {
		action = (ISAFAction) ActionFactory.getInstance().getAction(GUIConstants.EXIT_ACTION);
		super.fillBars(barManager, workspace);
	}
}
