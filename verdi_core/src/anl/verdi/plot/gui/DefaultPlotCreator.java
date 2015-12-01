package anl.verdi.plot.gui;

import anl.verdi.core.VerdiApplication;
import anl.verdi.data.DataFrame;
import anl.verdi.formula.Formula;
import anl.verdi.plot.config.PlotConfiguration;

/**
 * Default implementation of PlotCreator. This should handle those plots
 * that do not need any input beyond a single formula.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DefaultPlotCreator extends AbstractPlotCreator {

	/**
	 * Creates a DefaultPlotCreator from the app and for the specified type of plot.
	 *
	 * @param app  the pave applicaiton
	 * @param type the plot type
	 * @param config plot configuration
	 */
	public DefaultPlotCreator(VerdiApplication app, Formula.Type type, PlotConfiguration config) {
		super(type, app, config);
	}


	/**
	 * Creates a DefaultPlotCreator from the app and for the specified type of plot.
	 *
	 * @param app  the pave applicaiton
	 * @param type the plot type
	 */
	public DefaultPlotCreator(VerdiApplication app, Formula.Type type) {
		super(type, app);
	}


	/**
	 * Perform the actual plot creation.
	 */
	public Plot doCreatePlot() {
		if (app.getProject().getSelectedFormula() != null) {
			DataFrame frame = app.evaluateFormula(type);			
			if (frame != null) {
				PlotFactory factory = new PlotFactory();
				PlotPanel panel = factory.getPlot(type, app.getProject().getSelectedFormula().getFormula(),
								frame, config);
				app.getGui().addPlot(panel);
				panel.addPlotListener(app);
				return panel.getPlot();
			}
		}
		return null;
	}
}
