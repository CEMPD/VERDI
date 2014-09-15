package anl.verdi.plot.probe;

import java.util.ArrayList;
import java.util.List;

import anl.verdi.plot.gui.AreaSelectionEvent;
import anl.verdi.plot.gui.OverlayRequest;
import anl.verdi.plot.gui.PlotListener;
import anl.verdi.plot.gui.PlotRequest;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotEventProducer {

	private List<PlotListener> listeners = new ArrayList<PlotListener>();

	public void addListener(PlotListener listener) {
		listeners.add(listener);
	}

	public void removeListener(PlotListener listener) {
		listeners.remove(listener);
	}

	public void firePlotRequest(PlotRequest event) {
		for (PlotListener listener : listeners) {
			listener.plotRequested(event);
		}
	}

	public void fireOverlayRequest(OverlayRequest event) {
		for (PlotListener listener : listeners) {
			listener.overlayRequested(event);
		}
	}

	/**
	 * Fires the specifed event to all registered listeners.
	 *
	 * @param event the event describing the area selection
	 */
	public void fireAreaSelectionEvent(AreaSelectionEvent event) {
		for (PlotListener listener : listeners) {
			listener.areaSelected(event);
		}
	}

	/**
	 * Fires the specifed event to all registered listeners.
	 *
	 * @param event the event describing the probe
	 */
	public void fireProbeEvent(ProbeEvent event) {
		for (PlotListener listener : listeners) {
			listener.plotProbed(event);
		}
	}
}
