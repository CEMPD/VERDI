package anl.verdi.plot.gui;

import anl.verdi.core.Project;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiGUI;
import anl.verdi.data.DataFrame;
import anl.verdi.formula.Formula;
import anl.verdi.plot.config.VertCrossPlotConfiguration;
import anl.verdi.plot.types.VerticalCrossSectionPlot;

/**
 * Creator for vertical cross section plots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VerticalCrossPlotCreator extends AbstractPlotCreator {

	/**
	 * Creates a VerticalCrossPlotCreator from the app.
	 *
	 * @param app the pave applicaiton
	 */
	public VerticalCrossPlotCreator(VerdiApplication app) {
		super(Formula.Type.VERTICAL_CROSS_SECTION, app);
	}

	/**
	 * Creates a VerticalCrossPlotCreator from the app with the specified configuration.
	 *
	 * @param app the pave applicaiton
	 * @param config the configuration
	 */
	public VerticalCrossPlotCreator(VerdiApplication app, VertCrossPlotConfiguration config) {
		super(Formula.Type.VERTICAL_CROSS_SECTION, app, config);
	}

	private boolean showDialog() {
		return config == null || !(config.getObject(VertCrossPlotConfiguration.CROSS_SECTION_TYPE) != null &&
						config.getObject(VertCrossPlotConfiguration.CROSS_SECTION_INDEX) != null);
	}

	private VertCrossPlotConfiguration createConfig() {
		if (config != null && config instanceof VertCrossPlotConfiguration) {
			return (VertCrossPlotConfiguration) config;
		}	else {
			return new VertCrossPlotConfiguration(config);
		}
	}


	/**
	 * Perform the actual plot creation.
	 */
	public Plot doCreatePlot() {
		VerdiGUI gui = app.getGui();
		Project project = app.getProject();
		if (project.getSelectedFormula() != null) {
			DataFrame frame = app.evaluateFormula(type);
			if (frame != null) {
				if (showDialog()) {
					VerticalCrossDialog dialog = new VerticalCrossDialog(gui.getFrame());
					dialog.init(frame.getAxes());
					dialog.setLocationRelativeTo(gui.getFrame());
					dialog.pack();
					dialog.setVisible(true);
					if (!dialog.isCanceled()) {
						VertCrossPlotConfiguration vConfig = createConfig();
						if (dialog.isXSelected()) {
							int x = dialog.getColumn();
							vConfig.setCrossSectionType(VerticalCrossSectionPlot.CrossSectionType.X);
							vConfig.setCrossSectionRowCol(x);
						} else {
							int y = dialog.getRow();
							vConfig.setCrossSectionType(VerticalCrossSectionPlot.CrossSectionType.Y);
							vConfig.setCrossSectionRowCol(y);
						}
						return createVertPlot(vConfig, frame);
					}
				} else {
					return createVertPlot((VertCrossPlotConfiguration) config, frame);
				}
			}
		}
		return null;
	}

	private Plot createVertPlot(VertCrossPlotConfiguration config, DataFrame frame) {
		// we need to convert the row / col index because
		// the user specifies it in terms of the entire domain
		// but we need to convert it into an index into the dataframe
		if (config.getCrossSectionType() == VerticalCrossSectionPlot.CrossSectionType.X) {
			int x = config.getCrossSectionRowCol() - frame.getAxes().getXAxis().getOrigin();
			config.setCrossSectionRowCol(x);
		} else {
			int y = config.getCrossSectionRowCol() - frame.getAxes().getYAxis().getOrigin();
			config.setCrossSectionRowCol(y);
		}
		VerdiGUI gui = app.getGui();
		Project project = app.getProject();
		PlotFactory factory = new PlotFactory();
		String formula = project.getSelectedFormula().getFormula();
		PlotPanel panel = factory.getVerticalCrossPlot(formula, frame, config);
		gui.addPlot(panel);
		panel.addPlotListener(app);
		return panel.getPlot();
	}
}
