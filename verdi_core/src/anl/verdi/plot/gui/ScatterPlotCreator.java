package anl.verdi.plot.gui;

import anl.verdi.core.Project;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiGUI;
import anl.verdi.data.DataFrame;
import anl.verdi.formula.Formula;
import anl.verdi.gui.FormulaListElement;

/**
 * Creator for scatter plots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ScatterPlotCreator extends AbstractPlotCreator {

	/**
	 * Creates a ScatterPlotCreator from the app.
	 *
	 * @param app  the pave application
	 */
	public ScatterPlotCreator(VerdiApplication app) {
		super(Formula.Type.SCATTER_PLOT, app);
	}


	/**
	 * Perform the actual plot creation.
	 */
	public Plot doCreatePlot() {
		VerdiGUI gui = app.getGui();
		Project project = app.getProject();
		ScatterDialog dialog = new ScatterDialog(gui.getFrame());
		dialog.setLocationRelativeTo(gui.getFrame());
		dialog.setFormulas(project.getFormulasAsList());
		dialog.pack();
		dialog.setVisible(true);
		FormulaListElement xElement = dialog.getXElement();
		if (xElement != null) {
			FormulaListElement oldElement = project.getSelectedFormula();
			project.setSelectedFormula(xElement);
			DataFrame xFrame = app.evaluateFormula(type);
			FormulaListElement yElement = dialog.getYElement();
			project.setSelectedFormula(yElement);
			DataFrame yFrame = app.evaluateFormula(type);
			project.setSelectedFormula(oldElement);
			if (xFrame != null && yFrame != null) {
				PlotFactory factory = new PlotFactory();
				final PlotPanel panel = factory.getScatterPlot(xElement.getFormula(), yElement.getFormula(),
								xFrame, yFrame);
				gui.addPlot(panel);
				panel.addPlotListener(app);
				return panel.getPlot();
			}
		}

		return null;
	}
	
	/**this function is only for use from the command line	 */
	public Plot createPlotFromCommandLine(FormulaListElement xElement, FormulaListElement yElement)
	{
			VerdiGUI gui = app.getGui();
			Project project = app.getProject();
//			ScatterDialog dialog = new ScatterDialog(gui.getFrame());
//			dialog.setLocationRelativeTo(gui.getFrame());
//			dialog.setFormulas(project.getFormulasAsList());
//			dialog.pack();
//			dialog.setVisible(true);
			//Loop through this: project.getFormulasAsList()
			//and get the element matching the formula, error if not there
//			FormulaListElement xElement = dialog.getXElement();
			if (xElement != null) {
				FormulaListElement oldElement = project.getSelectedFormula();
				project.setSelectedFormula(xElement);
				DataFrame xFrame = app.evaluateFormula(type);
//				FormulaListElement yElement = dialog.getYElement();
				project.setSelectedFormula(yElement);
				DataFrame yFrame = app.evaluateFormula(type);
				project.setSelectedFormula(oldElement);
				if (xFrame != null && yFrame != null) {
					PlotFactory factory = new PlotFactory();
					final PlotPanel panel = factory.getScatterPlot(xElement.getFormula(), yElement.getFormula(),
									xFrame, yFrame);
					gui.addPlot(panel);
					panel.addPlotListener(app);
					return panel.getPlot();
				}
			}
			return null;
	}
	
}
