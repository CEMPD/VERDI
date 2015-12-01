/**
 * FastTilePlot - Callback for FastTilePlot button.
 * @author plessel.todd@epa.gov 2008-09-16
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.gui.action;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.Set;

import saf.core.ui.actions.AbstractSAFAction;
import anl.verdi.core.VerdiApplication;
import anl.verdi.data.AxisRange;
import anl.verdi.data.DataFrame;
import anl.verdi.data.Dataset;
import anl.verdi.formula.DefaultParser;
import anl.verdi.formula.Formula;
import anl.verdi.formula.FormulaFactory;
import anl.verdi.formula.IllegalFormulaException;
import anl.verdi.formula.ValidationResult;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.parser.ASTTreeInfo;
import anl.verdi.parser.Frame;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotPanel;

public class MeshPlot extends AbstractSAFAction<VerdiApplication> {

  // FastTilePlot button callback:

  /**
	 * 
	 */
	private static final long serialVersionUID = 7433688932017847111L;

public void actionPerformed( ActionEvent unused ) {
    final VerdiApplication application = workspace.getApplicationMediator();

    if ( application.getProject().getSelectedFormula() != null ) {
    	/*ASTTreeInfo treeInfo = null;
		FormulaListElement listElement = application.getProject().getSelectedFormula();

		String strFormula = listElement.getFormula();
		DefaultParser parser = new DefaultParser(strFormula, new Frame());
		try {
			treeInfo = parser.parse();
		} catch (IllegalFormulaException e) {
			e.printStackTrace();
			return;
		}
		Set <String> variables = treeInfo.getVariableNames();
		String[] split = application.getDataManager().splitVarName(variables.iterator().next());
		String varName = split[1];*/



      final DataFrame dataFrame =
        application.evaluateFormula( Formula.Type.TILE );

      if ( dataFrame != null ) {
        final Plot plot = new anl.verdi.plot.gui.MeshPlot(application, dataFrame );
		final String variableName = dataFrame.getVariable().getName();
        final PlotPanel panel = new PlotPanel( plot, "Tile " + variableName);
        application.getGui().addPlot( panel );
        panel.addPlotListener( application );
      }
    }
  }

}




