/**
 * MeshPlot - Callback for FastTilePlot button used when displaying unstructured grid data
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.gui.action;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.area.target.Target;
import anl.verdi.area.target.TargetCalculator;
import anl.verdi.core.VerdiApplication;
import anl.verdi.data.DataFrame;
import anl.verdi.formula.Formula;
import anl.verdi.plot.data.IMPASDataset;
import anl.verdi.plot.gui.PlotPanel;

public class MeshPlot extends AbstractSAFAction<VerdiApplication> {

  // MeshPlot/FastTilePlot button callback:
	
	static final Logger Logger = LogManager.getLogger(MeshPlot.class.getName());

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
		performAction(application, dataFrame, null);
	}
	
public static void performAction( final VerdiApplication application, final DataFrame dataFrame, TargetCalculator calc) {
    final anl.verdi.plot.gui.MeshPlot plot = new anl.verdi.plot.gui.MeshPlot(application, dataFrame, calc != null);
    if (calc != null) {
		boolean retValue = calc.calculateIntersections(Target.getTargets(),(IMPASDataset)dataFrame.getDataset().get(0), plot.getTilePlot());
		Logger.debug("back from calculateIntersections, retValue = " + retValue);
		if(!retValue)
		{
			// 2014 added in message dialog to show message to user
			String aMessage = "Problem with areal interpolation calculations. Check if polygons intersect grid cells.";
			JOptionPane.showMessageDialog(null, aMessage, "Areal Interpolation Issue", JOptionPane.WARNING_MESSAGE);
			return;
		}
		plot.initInterpolation();
	  

    }
	final String variableName = dataFrame.getVariable().getName();
    String plotName = "Tile " + variableName;
    if (calc != null)
    	plotName = "ArealInterpolation";
    final PlotPanel panel = new PlotPanel( plot, plotName);
    application.getGui().addPlot( panel );
    panel.addPlotListener( application );
  }

}




