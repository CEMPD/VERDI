/**
 * FastTilePlot - Callback for FastTilePlot button.
 * @author plessel.todd@epa.gov 2008-09-16
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.gui.action;

import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.util.List;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;
import anl.verdi.data.DataFrame;
import anl.verdi.data.Dataset;
import anl.verdi.formula.Formula;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotPanel;

public class FastTilePlot extends AbstractSAFAction<VerdiApplication> {

  // FastTilePlot button callback:

  /**
	 * 
	 */
	private static final long serialVersionUID = 7433688932017847111L;

public void actionPerformed( ActionEvent unused ) {
    final VerdiApplication application = workspace.getApplicationMediator();
    application.getGui().busyCursor();

    if ( application.getProject().getSelectedFormula() != null ) {
      final DataFrame dataFrame =
        application.evaluateFormula( Formula.Type.TILE );

      if ( dataFrame != null ) {
    	  List<Dataset> datasets = dataFrame.getDataset();
    	  
    	  if (datasets != null && datasets.size() > 0 && datasets.get(0).getClass().getName().toLowerCase().indexOf("mpas") != -1) {
    		  MeshPlot.performAction(application, dataFrame);
    		  application.getGui().defaultCursor();
    		  return;
    	  }
        final Plot plot = new anl.verdi.plot.gui.FastTilePlot(application, dataFrame );
		final String variableName = dataFrame.getVariable().getName();
        final PlotPanel panel = new PlotPanel( plot, "Tile " + variableName);
        application.getGui().addPlot( panel );
        panel.addPlotListener( application );
      }
    }
    application.getGui().defaultCursor();
  }

}




