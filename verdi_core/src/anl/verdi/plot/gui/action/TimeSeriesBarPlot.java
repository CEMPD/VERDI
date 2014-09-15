package anl.verdi.plot.gui.action;

import java.awt.event.ActionEvent;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;
import anl.verdi.formula.Formula;
import anl.verdi.plot.gui.DefaultPlotCreator;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TimeSeriesBarPlot extends AbstractSAFAction<VerdiApplication> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1717372478331250372L;

	public void actionPerformed(ActionEvent e) {
		new DefaultPlotCreator(workspace.getApplicationMediator(), Formula.Type.TIME_SERIES_BAR).createPlot();
	}
}
