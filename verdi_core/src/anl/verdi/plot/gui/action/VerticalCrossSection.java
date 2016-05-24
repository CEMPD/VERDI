package anl.verdi.plot.gui.action;

import java.awt.event.ActionEvent;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;
import anl.verdi.plot.gui.VerticalCrossPlotCreator;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VerticalCrossSection extends AbstractSAFAction<VerdiApplication> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2813483762872396872L;

	public void actionPerformed(ActionEvent e) {
		new VerticalCrossPlotCreator(workspace.getApplicationMediator()).createPlot();
	}
}
