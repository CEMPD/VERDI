/**
 * GTTilePlotAction - Callback for GTTilePlot button. Originally FastTilePlot in anl.verdi.plot.gui.action package
 * Modified January 2016 by Jo Ellen Brandmeyer for GeoTools version of TilePlot
 * @author plessel.todd@epa.gov 2008-09-16
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.map.MapContent;
import org.geotools.renderer.GTRenderer;
import org.geotools.swing.JMapPane;
import org.geotools.swing.RenderingExecutor;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;
import anl.verdi.data.DataFrame;
import anl.verdi.formula.Formula;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotPanel;
import anl.verdi.plot.gui.GTTilePlot;


public class GTTilePlotAction extends AbstractSAFAction<VerdiApplication> {

	// GTTilePlot button callback:

	/**
	 * 
	 */
	private static final long serialVersionUID = 7433688932017847111L;
	static final Logger Logger = LogManager.getLogger(GTTilePlotAction.class.getName());

	public void actionPerformed( ActionEvent unused ) {
		Logger.debug("in GTTilePlotAction.actionPerformed");
		final VerdiApplication application = workspace.getApplicationMediator();

		if ( application.getProject().getSelectedFormula() != null ) {
			Logger.debug("getSelectedFormula() != null; ready to calculate dataFrame");
			final DataFrame dataFrame = application.evaluateFormula( Formula.Type.TILE );
			Logger.debug("look at dataFrame, check if null");
			if ( dataFrame != null ) {
				Logger.debug("dataFrame is not null; ready to generate Plot for GTTilePlot");
				final Plot plot = new GTTilePlot(application, dataFrame);
				final String variableName = dataFrame.getVariable().getName();
				Logger.debug("have variableName = " + variableName);	// O3[1]
				JPanel aJPanel = ((GTTilePlot)plot).getPanel();
				JMapPane aJMapPane = ((GTTilePlot)plot).getMapPane();
				Logger.debug("plot.getMapPane = " + ((GTTilePlot)plot).getMapPane());
				Logger.debug("aJMapPane = " + aJMapPane);
				MapContent aMapContent = ((GTTilePlot)plot).getMapContent();
				Logger.debug("aMapContent = " + aMapContent);
				RenderingExecutor aRenderingExecutor = ((GTTilePlot)plot).getRenderingExecutor();
				Logger.debug("aRenderingExecutor = " + aRenderingExecutor);
				GTRenderer aRenderer = ((GTTilePlot)plot).getRenderer();
				Logger.debug("aRenderer = " + aRenderer);
				Logger.debug("ready to generate PlotPanel for a plot, variableName, and additional values");
				final PlotPanel panel = new PlotPanel(plot, variableName, aJMapPane, aMapContent, aRenderingExecutor, aRenderer);
				Logger.debug("ready to call addPlot to add the new panel to application GUI");
				application.getGui().addPlot( panel );
				Logger.debug("ready to add plot listener");
				panel.addPlotListener( application );
				Logger.debug("all done with actionPerformed");
			}
		}
	}
}