package anl.verdi.plot.config;

import java.awt.Color;

import anl.verdi.plot.color.ColorMap;

/**
 * Configuration info for tile plots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TilePlotConfiguration extends PlotConfiguration {

	public static final String COLOR_MAP = TilePlotConfiguration.class.getName() + "_color_map";
	public static final String SHOW_GRID_LINES = TilePlotConfiguration.class.getName() + "_show_grid_lines";
	public static final String SHOW_WIND_VECTORS = TilePlotConfiguration.class.getName() + "_show_wind_vectors";
	public static final String GRID_LINE_COLOR = TilePlotConfiguration.class.getName() + "_grid_line_color";
	public static final String OBS_SHAPE_SIZE = TilePlotConfiguration.class.getName() + "_obs_shape_size";
	public static final String OBS_STROKE_SIZE = TilePlotConfiguration.class.getName() + "_obs_stroke_size";

	public TilePlotConfiguration() {
	}

	public TilePlotConfiguration(PlotConfiguration config) {
		super(config);
	}

	/**
	 * Gets the color map.
	 *
	 * @return the color map.
	 */
	public ColorMap getColorMap() {
		return (ColorMap)getObject(COLOR_MAP);
	}

	/**
	 * Sets the color map.
	 *
	 * @param map the color map
	 */
	public void setColorMap(ColorMap map) {
		putObject(COLOR_MAP, map);
	}
	
	/**
	 * Enable grid lines and set their color.
	 * 
	 * @param enable grid line on or off
	 * @param color the grid line color
	 */
	public void setGridLines(boolean enable, Color lineColor) {
		putObject(SHOW_GRID_LINES, new Boolean(enable));
		putObject(GRID_LINE_COLOR, lineColor);
	}
	
	/**
	 * Enable wind vectors
	 * 
	 * @param enable wind vector display on or off
	 */
	public void setWindVectors(boolean enable) {
		putObject(SHOW_WIND_VECTORS, new Boolean(enable));
	}
	
}
