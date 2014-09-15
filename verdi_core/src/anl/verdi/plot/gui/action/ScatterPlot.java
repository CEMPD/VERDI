package anl.verdi.plot.gui.action;

import java.awt.event.ActionEvent;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;
import anl.verdi.plot.gui.ScatterPlotCreator;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ScatterPlot extends AbstractSAFAction<VerdiApplication> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1076785792893725393L;

	public void actionPerformed(ActionEvent e) {
		 new ScatterPlotCreator(workspace.getApplicationMediator()).createPlot();
	}
}
