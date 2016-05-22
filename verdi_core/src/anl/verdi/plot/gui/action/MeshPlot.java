/**
 * MeshPlot - Callback for FastTilePlot button used when displaying unstructured grid data
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.gui.action;

import java.awt.event.ActionEvent;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;
import anl.verdi.data.DataFrame;
import anl.verdi.formula.Formula;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotPanel;

public class MeshPlot extends AbstractSAFAction<VerdiApplication> {

  // MeshPlot/FastTilePlot button callback:

	private static final long serialVersionUID = 7433688932017847111L;
	
	public void actionPerformed( ActionEvent unused ) {
	    final VerdiApplication application = workspace.getApplicationMediator();
	    if ( application.getProject().getSelectedFormula() != null ) {

	      final DataFrame dataFrame =
	        application.evaluateFormula( Formula.Type.TILE );

	      if ( dataFrame != null ) {
	    	  performAction(application, dataFrame);
	      }
	    }
	}


public static void performAction( final VerdiApplication application, final DataFrame dataFrame) {
    final Plot plot = new anl.verdi.plot.gui.MeshPlot(application, dataFrame );
	final String variableName = dataFrame.getVariable().getName();
    final PlotPanel panel = new PlotPanel( plot, "Tile " + variableName);
    application.getGui().addPlot( panel );
    panel.addPlotListener( application );
  }

}




