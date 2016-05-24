package anl.verdi.plot.util;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action for printing plots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotPrintAction extends AbstractAction {

	private anl.verdi.plot.jfree.ChartPanel panel;

	/**
	 * Creates a PlotPrintAction that will print the specified chart panel.
	 *
	 * @param panel the panel to print
	 */
	public PlotPrintAction(anl.verdi.plot.jfree.ChartPanel panel) {
		super("Print");
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent e) {
		print();
	}

	public void print() {
		panel.createChartPrintJob();
	}
}
