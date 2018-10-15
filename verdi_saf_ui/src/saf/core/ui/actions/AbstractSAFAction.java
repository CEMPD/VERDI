package saf.core.ui.actions;

import saf.core.ui.Workspace;

import javax.swing.*;

/**
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:55:17 $
 */
public abstract class AbstractSAFAction<T> extends AbstractAction implements ISAFAction<T> {

	protected Workspace<T> workspace;

	public void initialize(Workspace<T> workspace) {
		this.workspace = workspace;
	}
}
