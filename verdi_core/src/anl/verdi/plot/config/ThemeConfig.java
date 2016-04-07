package anl.verdi.plot.config;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.StandardChartTheme;

/***
 * To save and load a JFreeChart theme using a delegate (PlotConfiguration) pattern.
 * @author qun
 *
 */

public class ThemeConfig {
	static final Logger Logger = LogManager.getLogger(ThemeConfig.class.getName());
	public static final String SHOW_SHADOW = ThemeConfig.class.getName() + ".show_shadow";
	public static final String X_LARGE_FONT = ThemeConfig.class.getName() + ".extra_large_font";
	public static final String LARGE_FONT = ThemeConfig.class.getName() + ".large_font";
	public static final String REGULAR_FONT = ThemeConfig.class.getName() + ".regular_font";
	public static final String SMALL_FONT = ThemeConfig.class.getName() + ".small_font";
	public static final String TITLE_PAINT = ThemeConfig.class.getName() + ".title_paint";
	public static final String SUBTITLE_PAINT = ThemeConfig.class.getName() + ".subtitle_paint";
	public static final String LEGEND_PAINT = ThemeConfig.class.getName() + ".legend_paint";
	public static final String LEGEND_BG_PAINT = ThemeConfig.class.getName() + ".legend_background_paint";
	public static final String AXIS_LABEL_PAINT = ThemeConfig.class.getName() + ".axis_label_paint";
	public static final String TICK_LABEL_PAINT = ThemeConfig.class.getName() + ".tick_label_paint";
	public static final String CHART_BG_PAINT = ThemeConfig.class.getName() + ".chart_background_paint";
	public static final String PLOT_BG_PAINT = ThemeConfig.class.getName() + ".plot_background_paint";
	public static final String PLOT_OUTLINE_PAINT = ThemeConfig.class.getName() + ".plot_outline_paint";
	public static final String DOMAIN_GRDLN_PAINT = ThemeConfig.class.getName() + ".domain_gridline_paint";
	public static final String RANGE_GRDLN_PAINT = ThemeConfig.class.getName() + ".range_gridline_paint";
	public static final String BASELINE_PAINT = ThemeConfig.class.getName() + ".baseline_paint";
	public static final String CROSSHAIR_PAINT = ThemeConfig.class.getName() + ".crosshair_paint";
	public static final String ITEM_LABEL_PAINT = ThemeConfig.class.getName() + ".item_label_paint";
	public static final String SHADOW_PAINT = ThemeConfig.class.getName() + ".shadow_paint";
	
	private PlotConfiguration delegate = new PlotConfiguration();
	
	public ThemeConfig() {
		Logger.debug("in ThemeConfig, default constructor");
	}
	
	public ThemeConfig(File file) throws IOException {
		PlotConfiguration config = new PlotConfigurationIO().loadConfiguration(file);
		delegate.merge(config);
		Logger.debug("in ThemeConfig, constructor from a file: " + file.getAbsolutePath());
	}
	
	/**
	 * Puts the specified object into the configuration map with the specified
	 * key.
	 * 
	 * @param key the configuration key
	 * @param value the configuration value
	 */
	public void putObject(Object key, Object value) {
		if (value != null && key != null)
			delegate.props.put(key, value);
	}

	/**
	 * Removes the specified object from the configuration map.
	 * 
	 * @param key the configuration key
	 */
	public void removeObject(Object key) {
		if (delegate.props.contains(key))
			delegate.props.remove(key);
	}

	/**
	 * Gets the object associated with the specified key from the configuration
	 * map.
	 * 
	 * @param key the key of the object to return
	 * @return the object associated with the specified key from the
	 *         configuration map or null if no such object exists.
	 */
	public Object getObject(Object key) {
		return delegate.props.get(key);
	}

	/**
	 * Sets the named configuration property to the specified value.
	 * 
	 * @param key the name of the property to set
	 * @param value the value of the property
	 */
	public void setProperty(String key, String value) {
		delegate.props.setProperty(key, value);
	}

	/**
	 * Gets the value of the named property.
	 * 
	 * @param key the name of the property to get
	 * @return the value of the named property.
	 */
	public String getProperty(String key) {
		return delegate.props.getProperty(key);
	}

	/**
	 * Gets the file name of a configuration file.
	 * 
	 * @return the file name of a configuration file.
	 */
	public String getConfigFileName() {
		return delegate.getConfigFileName();
	}

	/**
	 * Sets the file name of a configuration file.
	 * 
	 * @param fileName the name of the configuration file
	 */
	public void setConfigFileName(String fileName) {
		delegate.setConfigFileName(fileName);
	}

	public String getString(String key) {
		return delegate.getString(key);
	}

	public Color getColor(String key) {
		return delegate.getColor(key);
	}

	public Font getFont(String key) {
		return delegate.getFont(key);
	}

	public File getPreviousFolder() {
		return delegate.getPreviousFolder();
	}

	public void setPreviousFolder(File file) {
		delegate.setPreviousFolder(file);
	}

	/**
	 * Merges this theme with the specified configuration. The
	 * specified theme will override any configuration info in this
	 * theme.
	 * 
	 * @param theme the configuration to merge with
	 */
	public void merge(PlotConfiguration theme) {
		delegate.merge(theme);
	}

	/**
	 * Saves this theme to a file.
	 * 
	 * @param file the file to save the configuration to
	 * @throws IOException if there is an error during saving.
	 */
	public void save(File file) throws IOException {
		delegate.save(file, false);
		Logger.info("Theme saved to file: " + file.getAbsolutePath());
	}

	public ChartTheme getTheme() {
		String date = new Date().getTime() + "";
		
		if (delegate.props.isEmpty()) {
			return new StandardChartTheme(date, false);
		}
		
		boolean showshadow = (Boolean)getObject(SHOW_SHADOW);
		File cfgFile = getPreviousFolder();
		StandardChartTheme theme = new StandardChartTheme(cfgFile == null ? date : cfgFile.getAbsolutePath(), showshadow);
		if (getObject(X_LARGE_FONT) != null) theme.setExtraLargeFont((Font)getObject(X_LARGE_FONT));
		if (getObject(LARGE_FONT) != null) theme.setLargeFont((Font)getObject(LARGE_FONT));
		if (getObject(REGULAR_FONT) != null) theme.setRegularFont((Font)getObject(REGULAR_FONT));
		if (getObject(SMALL_FONT) != null) theme.setSmallFont((Font)getObject(SMALL_FONT));
		if (getObject(TITLE_PAINT) != null) theme.setTitlePaint((Color)getObject(TITLE_PAINT));
		if (getObject(SUBTITLE_PAINT) != null) theme.setSubtitlePaint((Color)getObject(SUBTITLE_PAINT));
		if (getObject(LEGEND_PAINT) != null) theme.setLegendItemPaint((Color)getObject(LEGEND_PAINT));
		if (getObject(LEGEND_BG_PAINT) != null) theme.setLegendBackgroundPaint((Color)getObject(LEGEND_BG_PAINT));
		if (getObject(AXIS_LABEL_PAINT) != null) theme.setAxisLabelPaint((Color)getObject(AXIS_LABEL_PAINT));
		if (getObject(TICK_LABEL_PAINT) != null) theme.setTickLabelPaint((Color)getObject(TICK_LABEL_PAINT));
		if (getObject(CHART_BG_PAINT) != null) theme.setChartBackgroundPaint((Color)getObject(CHART_BG_PAINT));
		if (getObject(PLOT_BG_PAINT) != null) theme.setPlotBackgroundPaint((Color)getObject(PLOT_BG_PAINT));
		if (getObject(PLOT_OUTLINE_PAINT) != null) theme.setPlotOutlinePaint((Color)getObject(PLOT_OUTLINE_PAINT));
		if (getObject(DOMAIN_GRDLN_PAINT) != null) theme.setDomainGridlinePaint((Color)getObject(DOMAIN_GRDLN_PAINT));
		if (getObject(RANGE_GRDLN_PAINT) != null) theme.setRangeGridlinePaint((Color)getObject(RANGE_GRDLN_PAINT));
		if (getObject(BASELINE_PAINT) != null) theme.setBaselinePaint((Color)getObject(BASELINE_PAINT));
		if (getObject(CROSSHAIR_PAINT) != null) theme.setCrosshairPaint((Color)getObject(CROSSHAIR_PAINT));
		if (getObject(ITEM_LABEL_PAINT) != null) theme.setItemLabelPaint((Color)getObject(ITEM_LABEL_PAINT));
		if (getObject(SHADOW_PAINT) != null) theme.setShadowPaint((Color)getObject(SHADOW_PAINT));
		
		return theme;
	}

}
