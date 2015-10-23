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
public class TimeSeriesPlot extends AbstractSAFAction<VerdiApplication> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 692151849898230686L;

	public void actionPerformed(ActionEvent e) {
		 new DefaultPlotCreator(workspace.getApplicationMediator(), Formula.Type.TIME_SERIES_LINE).createPlot();
	}
}
