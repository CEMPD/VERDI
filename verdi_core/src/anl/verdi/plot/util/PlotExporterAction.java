package anl.verdi.plot.util;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

//import simphony.util.messages.MessageCenter;
import anl.verdi.plot.gui.Plot;

/**
 * Action that invokes the PlotExporter.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotExporterAction extends AbstractAction {

	private static final long serialVersionUID = -5959532901013666768L;

	static final Logger Logger = LogManager.getLogger(PlotExporterAction.class.getName());

//	private static final MessageCenter ctr = MessageCenter.getMessageCenter(PlotExporter.class);
	private Plot plot;


	public PlotExporterAction(Plot plot) {
		super("Export as Image");	// 2014 "Export As Image/GIS"
		this.plot = plot;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			PlotExporter exporter = new PlotExporter(plot);
			exporter.run();
		} catch (IOException ex) {
			Logger.error("Error while exporting plot as image " + ex.getMessage());
		}
	}
}
