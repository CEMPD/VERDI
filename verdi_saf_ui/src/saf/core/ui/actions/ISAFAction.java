package saf.core.ui.actions;

import saf.core.ui.Workspace;

import javax.swing.*;

/**
 * @author Nick Collier
 * @version $Revision: 1.2 $ $Date: 2005/11/21 18:55:17 $
 */
public interface ISAFAction<T> extends Action {

	void initialize(Workspace<T> workspace);
}
