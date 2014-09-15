package anl.verdi.plot.config;

import java.awt.Color;


/**
 * Configuration for time series plot.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TimeSeriesPlotConfiguration extends PlotConfiguration {

	public static final String SERIES_COLOR = TimeSeriesPlotConfiguration.class.getName() + ".series_color";

	/**
	 * Sets the series color.
	 *
	 * @param color the color
	 */
	public void setSeriesColor(Color color) {
		props.put(SERIES_COLOR, color);
	}

	/**
	 * Gets the series color.
	 *
	 * @return the series color.
	 */
	public Color getSeriesColor() {
		return getColor(SERIES_COLOR);
	}
}
