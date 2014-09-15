package anl.verdi.plot.util;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import simphony.util.messages.MessageCenter;
import anl.verdi.plot.gui.Plot;

/**
 * Action that invokes the PlotExporter.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotExporterAction extends AbstractAction {

	private static final MessageCenter ctr = MessageCenter.getMessageCenter(PlotExporter.class);
	private Plot plot;


	public PlotExporterAction(Plot plot) {
		super("Export As Image/GIS");
		this.plot = plot;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			PlotExporter exporter = new PlotExporter(plot);
			exporter.run();
		} catch (IOException ex) {
			ctr.error("Error while exporting plot as image", ex);
		}
	}
}
