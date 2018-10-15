package anl.verdi.plot.gui;

import anl.verdi.plot.probe.ProbeEvent;

/**
 * Interface for classes that listen to plot events.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface PlotListener {

	/**
	 * Notifies this PlotListener when a plot has some of its area
	 * selected.
	 *
	 * @param event the area selection
	 */
	void areaSelected(AreaSelectionEvent event);

	/**
	 * Notifies this PlotListener when a plot has been probed.
	 *
	 * @param event the probe
	 */
	void plotProbed(ProbeEvent event);

	/**
	 * Notifies this PlotListener of a request to create
	 * a plot fired from a another plot.
	 *
	 * @param request the request
	 */
	void plotRequested(PlotRequest request);

	/**
	 * Notifies the PlotListener of a request to
	 * create an overlay for the sending plot.
	 * The listener shoudl add the appropriate
	 * overlay evaluator to the request
	 *
	 * @param request the overlay request
	 */
	void overlayRequested(OverlayRequest request);

}
