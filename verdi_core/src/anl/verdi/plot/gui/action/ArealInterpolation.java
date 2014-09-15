/**
 * ArealInterpolation - Callback for FastTilePlot button.
 * @author Mary Ann Bitz 
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.gui.action;

import java.awt.event.ActionEvent;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.area.AreaTilePlot;
import anl.verdi.area.target.Target;
import anl.verdi.area.target.TargetCalculator;
import anl.verdi.core.VerdiApplication;
import anl.verdi.data.DataFrame;
import anl.verdi.formula.Formula;
import anl.verdi.plot.gui.FastAreaTilePlot;
import anl.verdi.plot.gui.PlotPanel;


public class ArealInterpolation extends AbstractSAFAction<VerdiApplication> {

	// FastTilePlot button callback:

	/**
	 * 
	 */
	private static final long serialVersionUID = 5898580138071998181L;

	public void actionPerformed( ActionEvent unused ) {

		final VerdiApplication application = workspace.getApplicationMediator();
		if ( application.getProject().getAreaFiles() == null || application.getProject().getAreaFiles().getSize()<=0) {
			application.getGui().showMessage("Fast Area Tile Plot", "No areal files selected! Please select one!");
			return;
		}
		if ( application.getProject().getSelectedFormula() == null) {
			application.getGui().showMessage("Fast Area Tile Plot", "No formula selected! Please select one!");
			return;
		}
		
		application.getGui().showBusyCursor();	// 2014 displays message at bottom of VERDI screen
// 2014 process typically fast enough to not need this message; NOTE: message blocks program from continuing until user presses OK button
//		try {
//			application.getGui().showMessage("Fast Area Tile Plot", "Loading data. This may take a while please be patient and click OK to continue...");
//			Thread.sleep(100);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
		if ( application.getProject().getSelectedFormula() != null ) {
			final DataFrame dataFrame =
				application.evaluateFormula( Formula.Type.TILE );

			if ( dataFrame != null ) {

				final FastAreaTilePlot plot = new anl.verdi.plot.gui.FastAreaTilePlot(application, dataFrame);

				// calculate the areas 
				TargetCalculator calc = new TargetCalculator();
				calc.calculateIntersections(Target.getTargets(),dataFrame,(AreaTilePlot)plot.getTilePlot());
				
				final PlotPanel panel = new PlotPanel( plot, "ArealInterpolation" );
				application.getGui().addPlot( panel );
				panel.addPlotListener( application );
		        new Thread(
		                new Runnable() {
		                    public void run() {
		                        try {
		                            Thread.sleep(50);
		                        } catch (Exception e) {
		                            e.printStackTrace();
		                        }
//		        				plot.draw();
		        				plot.init();
		        				plot.draw();
		                    }
		                }).start();
			}
		}
		application.getGui().restoreCursor();
		application.getGui().showMessage("Fast Area Tile Plot", "Loading data finished.");
	}
}




