package anl.verdi.plot.util;

import java.awt.event.ActionEvent;
import java.awt.print.PageFormat;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import anl.verdi.plot.gui.GTTilePlot;

/**
 * Action for printing plots. Based on old FastTilePlotPrintAction.
 *
 * @author Qun He; Jo Ellen Brandmeyer
 * @version $Revision$ $Date$
 */
public class GTTilePlotPrintAction extends AbstractAction {
	private static final long serialVersionUID = 3718644254313035055L;
	private GTTilePlot panel;

	/**
	 * Creates a GTTilePlotPrintAction that will print the specified chart panel.
	 *
	 * @param panel the panel to print
	 */
	public GTTilePlotPrintAction(GTTilePlot panel) {
		super("Print");
		this.panel = panel;
	}

	public void actionPerformed(ActionEvent e) {
		createChartPrintJob();
	}

	/**
	 * Creates a print job for the chart.
	 */
	public void createChartPrintJob() {

		PrinterJob job = PrinterJob.getPrinterJob();
		PageFormat pf = job.defaultPage();
		PageFormat pf2 = job.pageDialog(pf);
		if (pf2 != pf) {
			job.setPrintable(panel, pf2);
			if (job.printDialog()) {
				try {
					job.print();
				}
				catch (PrinterException e) {
					JOptionPane.showMessageDialog(panel, e);
				}
			}
		}

	}
}
