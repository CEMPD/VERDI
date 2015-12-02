/**
 * FastTilePlot - Callback for FastTilePlot button.
 * @author plessel.todd@epa.gov 2008-09-16
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.gui.action;

import java.awt.event.ActionEvent;

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

public class FastTilePlot extends AbstractSAFAction<VerdiApplication> {

	// FastTilePlot button callback:

	/**
	 * 
	 */
	private static final long serialVersionUID = 7433688932017847111L;
	static final Logger Logger = LogManager.getLogger(anl.verdi.plot.gui.action.FastTilePlot.class.getName());

	public void actionPerformed( ActionEvent unused ) {
		Logger.debug("in action.FastTilePlot.actionPerformed");
		final VerdiApplication application = workspace.getApplicationMediator();

		if ( application.getProject().getSelectedFormula() != null ) {
			Logger.debug("getSelectedFormula() != null; ready to calculate dataFrame");
			final DataFrame dataFrame = application.evaluateFormula( Formula.Type.TILE );
			Logger.debug("look at dataFrame, check if null");
			if ( dataFrame != null ) {
				Logger.debug("dataFrame is not null; ready to generate Plot for FastTilePlot");
				final Plot plot = new anl.verdi.plot.gui.FastTilePlot(application, dataFrame);
				final String variableName = dataFrame.getVariable().getName();
				Logger.debug("have variableName = " + variableName);	// O3[1]
				JMapPane aJMapPane = ((anl.verdi.plot.gui.FastTilePlot)plot).getMapPane();
				Logger.debug("plot.getMapPane = " + ((anl.verdi.plot.gui.FastTilePlot)plot).getMapPane());	// anl.verdi.plot.gui.FastTilePlot[,0,0,0x0,invalid,layout=java.awt.FlowLayout,alignmentX=0.0,alignmentY=0.0,border=,flags=8,maximumSize=,minimumSize=,preferredSize=]
				Logger.debug("aJMapPane = " + aJMapPane);	// anl.verdi.plot.gui.FastTilePlot[,0,0,0x0,invalid,layout=java.awt.FlowLayout,alignmentX=0.0,alignmentY=0.0,border=,flags=8,maximumSize=,minimumSize=,preferredSize=]
				MapContent aMapContent = ((anl.verdi.plot.gui.FastTilePlot)plot).getMapContent();
				Logger.debug("aMapContent = " + aMapContent);		// null here
				RenderingExecutor aRenderingExecutor = ((anl.verdi.plot.gui.FastTilePlot)plot).getRenderingExecutor();
				Logger.debug("aRenderingExecutor = " + aRenderingExecutor);
				GTRenderer aRenderer = ((anl.verdi.plot.gui.FastTilePlot)plot).getRenderer();
				Logger.debug("aRenderer = " + aRenderer);
				Logger.debug("ready to generate PlotPanel for a plot, variableName, and additional values");
				final PlotPanel panel = new PlotPanel(plot, variableName, aJMapPane, aMapContent, aRenderingExecutor, aRenderer);	// Sept 2015 changed from PlotPanel and
				Logger.debug("ready to call addPlot to add the new panel to application GUI");
				application.getGui().addPlot( panel );
				Logger.debug("ready to add plot listener");
				panel.addPlotListener( application );
				Logger.debug("all done with actionPerformed");
			}
		}
	}

}