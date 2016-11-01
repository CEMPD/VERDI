package anl.verdi.plot.gui;

import java.util.List;

import anl.verdi.area.target.TargetCalculator;
import anl.verdi.core.Project;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiGUI;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.Dataset;
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
				boolean meshInput = false;
				List<Dataset> datasets = frame.getDataset();
		    	if (datasets != null && datasets.size() > 0 && datasets.get(0).getClass().getName().toLowerCase().indexOf("mpas") != -1)
		    		meshInput = true;

				if (showDialog()) {
					VerticalCrossDialog dialog = new VerticalCrossDialog(gui.getFrame(), meshInput);
					dialog.init(frame);
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
						return createVertPlot(vConfig, frame, meshInput);
					}
				} else {
					return createVertPlot((VertCrossPlotConfiguration) config, frame, meshInput);
				}
			}
		}
		return null;
	}

	private Plot createVertPlot(VertCrossPlotConfiguration config, DataFrame frame, boolean meshInput) {
		// we need to convert the row / col index because
		// the user specifies it in terms of the entire domain
		// but we need to convert it into an index into the dataframe
		Axes axes = null;
		if (meshInput)
			axes = frame.getDataset().get(0).getCoordAxes();
		else	
			axes = frame.getAxes();
		if (config.getCrossSectionType() == VerticalCrossSectionPlot.CrossSectionType.X) {
			int x = config.getCrossSectionRowCol() - (int)axes.getXAxis().getRange().getOrigin();
			if (meshInput)
				++x;
			config.setCrossSectionRowCol(x);
		} else {
			int y = config.getCrossSectionRowCol() - (int)axes.getYAxis().getRange().getOrigin();
			if (meshInput)
				++y;
			config.setCrossSectionRowCol(y);
		}
		VerdiGUI gui = app.getGui();
		Project project = app.getProject();
		PlotFactory factory = new PlotFactory();
		String formula = project.getSelectedFormula().getFormula();
		PlotPanel panel = factory.getVerticalCrossPlot(formula, frame, config, meshInput);
		gui.addPlot(panel);
		panel.addPlotListener(app);
		return panel.getPlot();
	}
}
