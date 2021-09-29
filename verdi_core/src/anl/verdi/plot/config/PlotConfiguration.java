package anl.verdi.plot.config;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.PavePaletteCreator;

import com.bbn.openmap.util.DeepCopyUtil;

/**
 * Extensible plot configuration data.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotConfiguration {
	static final Logger Logger = LogManager.getLogger(PlotConfiguration.class.getName());

	public static final String PLOT_TYPE = PlotConfiguration.class.getName() + ".plot_type";
	public static final String TITLE = PlotConfiguration.class.getName() + ".title";
	public static final String TITLE_FONT = PlotConfiguration.class.getName() + ".title_font";
	public static final String TITLE_COLOR = PlotConfiguration.class.getName() + ".title_color";
	public static final String TITLE_SIZE = PlotConfiguration.class.getName() + ".title_size";
	public static final String TITLE_SHOW_LINE = PlotConfiguration.class.getName() + ".title_show_line";
	public static final String SUBTITLE_1 = PlotConfiguration.class.getName() + ".subtitle1";
	public static final String SUBTITLE_1_FONT = PlotConfiguration.class.getName() + ".subtitle1_font";
	public static final String SUBTITLE_1_COLOR = PlotConfiguration.class.getName() + ".subtitle1_color";
	public static final String SUBTITLE_1_SIZE = PlotConfiguration.class.getName() + ".subtitle1_size";
	public static final String SUBTITLE_1_SHOW_LINE = PlotConfiguration.class.getName() + ".subtitle1_show_line";
	public static final String SUBTITLE_2 = PlotConfiguration.class.getName() + ".subtitle2";
	public static final String SUBTITLE_2_FONT = PlotConfiguration.class.getName() + ".subtitle2_font";
	public static final String SUBTITLE_2_COLOR = PlotConfiguration.class.getName() + ".subtitle2_color";
	public static final String SUBTITLE_2_SIZE = PlotConfiguration.class.getName() + ".subtitle2_size";
	public static final String SUBTITLE_2_SHOW_LINE = PlotConfiguration.class.getName() + ".subtitle2_show_line";

	public static final String LEGEND_SHOW = PlotConfiguration.class.getName() + ".legend_show";
	public static final String UNITS = PlotConfiguration.class.getName() + ".units";
	public static final String UNITS_FONT = PlotConfiguration.class.getName() + ".units_font";
	public static final String UNITS_COLOR = PlotConfiguration.class.getName() + ".units_color";
	public static final String UNITS_SIZE = PlotConfiguration.class.getName() + ".units_size";
	public static final String UNITS_SHOW_TICK = PlotConfiguration.class.getName() + ".units_show_tick";
	public static final String UNITS_TICK_COLOR = PlotConfiguration.class.getName() + ".units_tick_color";
	public static final String UNITS_TICK_FONT = PlotConfiguration.class.getName() + ".units_tick_font";
	public static final String UNITS_TICK_SIZE = PlotConfiguration.class.getName() + ".units_tick_size";
	public static final String UNITS_TICK_NUMBER = PlotConfiguration.class.getName() + ".units_tick_number";

	public static final String DOMAIN_LABEL = PlotConfiguration.class.getName() + ".domain";
	public static final String DOMAIN_FONT = PlotConfiguration.class.getName() + ".domain_font";
	public static final String DOMAIN_COLOR = PlotConfiguration.class.getName() + ".domain_color";
	public static final String DOMAIN_SIZE = PlotConfiguration.class.getName() + ".domain_size";
	public static final String DOMAIN_SHOW_TICK = PlotConfiguration.class.getName() + ".domain_show_tick";
	public static final String DOMAIN_TICK_COLOR = PlotConfiguration.class.getName() + ".domain_tick_color";
	public static final String DOMAIN_TICK_FONT = PlotConfiguration.class.getName() + ".domain_tick_font";
	public static final String DOMAIN_TICK_SIZE = PlotConfiguration.class.getName() + ".domain_tick_size";
	public static final String DOMAIN_TICK_NUMBER = PlotConfiguration.class.getName() + ".domain_tick_number";
	public static final String DOMAIN_TICK_LABEL_FORMAT = PlotConfiguration.class.getName() + ".domain_tick_label_format";
	public static final String DOMAIN_TICK_LABEL_FORMAT_4CAT = PlotConfiguration.class.getName() + ".domain_tick_label_format_4cat";
	public static final String DOMAIN_TICK_LABEL_ORIENTATION = PlotConfiguration.class.getName() + ".domain_tick_label_orientation";
	
	public static final String RANGE_LABEL = PlotConfiguration.class.getName() + ".range";
	public static final String RANGE_FONT = PlotConfiguration.class.getName() + ".range_font";
	public static final String RANGE_COLOR = PlotConfiguration.class.getName() + ".range_color";
	public static final String RANGE_SIZE = PlotConfiguration.class.getName() + ".range_size";
	public static final String RANGE_SHOW_TICK = PlotConfiguration.class.getName() + ".range_show_tick";
	public static final String RANGE_TICK_COLOR = PlotConfiguration.class.getName() + ".range_tick_color";
	public static final String RANGE_TICK_FONT = PlotConfiguration.class.getName() + ".range_tick_font";
	public static final String RANGE_TICK_SIZE = PlotConfiguration.class.getName() + ".range_tick_size";
	public static final String RANGE_TICK_NUMBER = PlotConfiguration.class.getName() + ".range_tick_number";
	
	public static final String Z_LABEL = PlotConfiguration.class.getName() + ".z";
	public static final String Z_FONT = PlotConfiguration.class.getName() + ".z_font";
	public static final String Z_COLOR = PlotConfiguration.class.getName() + ".z_color";
	public static final String Z_SHOW_TICK = PlotConfiguration.class.getName() + ".z_show_tick";
	public static final String Z_TICK_COLOR = PlotConfiguration.class.getName() + ".z_tick_color";
	public static final String Z_TICK_FONT = PlotConfiguration.class.getName() + ".z_tick_font";
	public static final String Z_TICK_NUMBER = PlotConfiguration.class.getName() + ".z_tick_number";
	
	public static final String FOOTER1 = PlotConfiguration.class.getName() + ".footer_line_1";
	public static final String FOOTER1_SHOW_LINE = PlotConfiguration.class.getName() + ".footer_show_line_1";
	public static final String FOOTER1_AUTO_TEXT = PlotConfiguration.class.getName() + ".footer_line_1_auto_text";
    public static final String FOOTER1_COLOR = PlotConfiguration.class.getName() + ".footer_line_1_color";
    public static final String FOOTER1_FONT = PlotConfiguration.class.getName() + ".footer_line_1_font";
	public static final String FOOTER1_SIZE = PlotConfiguration.class.getName() + ".footer_line_1_size";
	public static final String FOOTER2 = PlotConfiguration.class.getName() + ".footer_line_2";
	public static final String FOOTER2_SHOW_LINE = PlotConfiguration.class.getName() + ".footer_show_line_2";
	public static final String FOOTER2_AUTO_TEXT = PlotConfiguration.class.getName() + ".footer_line_2_auto_text";
    public static final String FOOTER2_COLOR = PlotConfiguration.class.getName() + ".footer_line_2_color";
    public static final String FOOTER2_FONT = PlotConfiguration.class.getName() + ".footer_line_2_font";
	public static final String FOOTER2_SIZE = PlotConfiguration.class.getName() + ".footer_line_2_size";
	public static final String OBS_SHOW_LEGEND = PlotConfiguration.class.getName() + ".obs_show_legend";
    public static final String OBS_LEGEND_COLOR = PlotConfiguration.class.getName() + ".obs_legend_color";
    public static final String OBS_LEGEND_FONT = PlotConfiguration.class.getName() + ".obs_legend_font";
	public static final String OBS_LEGEND_SIZE = PlotConfiguration.class.getName() + ".obs_legend_size";

	public static final String CONFIG_FILE = PlotConfiguration.class.getName() + ".config_file";

	private static File previousFolder;

	protected Properties props = new Properties();

	public PlotConfiguration() {
		Logger.debug("in PlotConfiguration, default constructor");
	}

	/**
	 * Loads the configuration from the specified File.
	 * 
	 * @param file	the file to load from
	 * @throws IOException	if there is an error during loading.
	 */
	public PlotConfiguration(File file) throws IOException {
		PlotConfiguration config = new PlotConfigurationIO().loadConfiguration(file);
		this.merge(config);
		Logger.debug("in PlotConfiguration, constructor from a file");
	}

	/**
	 * Copy constructor.
	 * 
	 * @param config	the configuration to copy into this PlotConfiguration
	 */
	public PlotConfiguration(PlotConfiguration config) {
		Logger.debug("in Plot Configuration, copy constructor");
		try {
			for (Object key : config.props.keySet()) {
				props.put(key, DeepCopyUtil.copy(config.props.get(key)));
			}
		} catch (Exception e) {
			Logger.error("Error in copy constructor for PlotConfiguration: " + e.getMessage());
		}
	}
	
	public void updateConfig(PlotConfiguration config) {
		Logger.debug("in Plot Configuration updateConfig");
		config.props.get(PlotConfiguration.TITLE);
		try {
			for (Object key : config.props.keySet()) {
				props.put(key, DeepCopyUtil.copy(config.props.get(key)));
			}
		} catch (Exception e) {
			Logger.error("Error in PlotConfiguration.updateConfig", e);
		}
	}
	
	/**
	 * Initialize a PlotConfiguration object to the default contents and values for a TilePlot
	 */
	public void initTilePlotConf()
	{
		Logger.debug("in PlotConfiguration.initTilePlotConf");
		Boolean bTrue = true;
		Boolean bFalse = false;
		Integer a16 = 16;
		Integer a12 = 12;
		Integer a10 = 10;
		Integer a2 = 2;
		String blank = " ";
		props.setProperty(TITLE_SHOW_LINE, bTrue.toString());		// show title
		props.setProperty(TITLE, blank);							// default title is null
		props.setProperty(TITLE_FONT, Font.DIALOG);					// title uses DIALOG
		props.setProperty(TITLE_COLOR, (Color.black).toString());	// title in black
		props.setProperty(TITLE_SIZE, a16.toString());				// title size = 16
		props.setProperty(SUBTITLE_1_SHOW_LINE, bTrue.toString());	// show subtitle1
		props.setProperty(SUBTITLE_1, blank);						// subtitle1 is null
		props.setProperty(SUBTITLE_1_FONT, Font.DIALOG);			// subtitle1 uses Dialog
		props.setProperty(SUBTITLE_1_COLOR, (Color.gray).toString());	// subtitle1 in gray
		props.setProperty(SUBTITLE_1_SIZE, a12.toString());			// subtitle1 size = 12
		props.setProperty(SUBTITLE_2_SHOW_LINE, bTrue.toString());	// show subtitle2
		props.setProperty(SUBTITLE_2, blank);						// subtitle2 is null
		props.setProperty(SUBTITLE_2_FONT, Font.DIALOG);			// subtitle2 uses DIALOG
		props.setProperty(SUBTITLE_2_COLOR, (Color.gray).toString());	// subtitle2 in gray
		props.setProperty(SUBTITLE_2_SIZE, a10.toString());			// subtitle2 size = 10
		props.setProperty(TilePlotConfiguration.GRID_LINE_COLOR, (Color.LIGHT_GRAY).toString());	// grid lines lt gray
		props.setProperty(TilePlotConfiguration.SHOW_GRID_LINES, bFalse.toString());	// do not show grid lines
		props.setProperty(FOOTER1_SHOW_LINE, bTrue.toString());		// show footer1
		props.setProperty(FOOTER1, blank);							// default footer1 is null
		props.setProperty(FOOTER1_FONT, Font.SANS_SERIF);			// footer1 uses sansserif font
		props.setProperty(FOOTER1_COLOR, (Color.LIGHT_GRAY).toString());	// footer1 in light gray
		props.setProperty(FOOTER1_SIZE, a10.toString());			// footer1 size = 10
		props.setProperty(FOOTER2_SHOW_LINE, bFalse.toString());		// do not show footer2
		props.setProperty(FOOTER2, blank);							// default footer2 is null
		props.setProperty(FOOTER2_FONT, Font.SANS_SERIF);			// footer2 uses sansserif font
		props.setProperty(FOOTER2_COLOR, (Color.LIGHT_GRAY).toString());	// footer2 in light gray
		props.setProperty(FOOTER2_SIZE, a10.toString());			// footer2 size = 10
		props.setProperty(CONFIG_FILE, blank);						// no default configuration file
		props.setProperty(OBS_SHOW_LEGEND, bFalse.toString());		// default no obs legend
		props.setProperty(OBS_LEGEND_FONT, Font.SANS_SERIF);		// obs legend uses sansserif		
		props.setProperty(OBS_LEGEND_COLOR, (Color.LIGHT_GRAY).toString());	// obs legend color in light gray
		props.setProperty(OBS_LEGEND_SIZE, a10.toString());			// obs legend size = 10
		props.setProperty(TilePlotConfiguration.LEGEND_SHOW, bTrue.toString());	// default to show legend
		props.setProperty(UNITS, "ppmV");		// default units to "ppmV"
		props.setProperty(UNITS_COLOR, (Color.LIGHT_GRAY).toString());	// units in light gray
		props.setProperty(UNITS_FONT, Font.SANS_SERIF);				// units uses sans serif font
		props.setProperty(UNITS_SIZE, a10.toString());				// units size = 10
		props.setProperty(UNITS_SHOW_TICK, bFalse.toString());		// do not show tick for units ???
		props.setProperty(UNITS_TICK_COLOR, (Color.LIGHT_GRAY).toString());	// units tick to light gray
		props.setProperty(UNITS_TICK_FONT, Font.SANS_SERIF);		// units tick to sans serif
		props.setProperty(UNITS_TICK_SIZE, a2.toString());			// units tick size to 2
	}

	/**
	 * Puts the specified object into the configuration map with the specified key.
	 * 
	 * @param key	the configuration key
	 * @param value	the configuration value
	 */
	public void putObject(Object key, Object value) {
		if (value != null)
			props.put(key, value);
	}

	/**
	 * Removes the specified object from the configuration map.
	 * 
	 * @param key	the configuration key
	 */
	public void removeObject(Object key) {
		if (props.contains(key))
			props.remove(key);
	}

	/**
	 * Gets the object associated with the specified key from the configuration map.
	 * 
	 * @param key	the key of the object to return
	 * @return the object associated with the specified key from the configuration map or null if no such object exists.
	 */
	public Object getObject(Object key) {
		return props.get(key);
	}

	/**
	 * Sets the named configuration property to the specified value.
	 * 
	 * @param name	the name of the property to set
	 * @param value	the value of the property
	 */
	public void setProperty(String name, String value) {
		props.setProperty(name, value);
	}

	/**
	 * Gets the value of the named property.
	 * 
	 * @param name	the name of the property to get
	 * @return the value of the named property.
	 */
	public String getProperty(String name) {
		return props.getProperty(name);
	}

	/**
	 * Sets the text for subtitle 1.
	 * 
	 * @param val	the text of subtitle 1.
	 */
	public void setSubtitle1(String val) {
		props.setProperty(SUBTITLE_1, val);
	}

	/**
	 * Sets the text for subtitle 2.
	 * 
	 * @param val	the text of subtitle 2.
	 */
	public void setSubtitle2(String val) {
		props.setProperty(SUBTITLE_2, val);
	}

	/**
	 * Sets the title text.
	 * 
	 * @param val	the title text
	 */
	public void setTitle(String val) {
		props.setProperty(TITLE, val);
	}

	/**
	 * Sets the units text.
	 * 
	 * @param val	the units text
	 */
	public void setUnits(String val) {
		props.setProperty(UNITS, val);
	}

	/**
	 * Gets subtitle 1.
	 * 
	 * @return subtitle 1.
	 */
	public String getSubtitle1() {
		return props.getProperty(SUBTITLE_1);
	}

	/**
	 * Gets subtitle 2
	 * 
	 * @return subtitle 2.
	 */
	public String getSubtitle2() {
		return props.getProperty(SUBTITLE_2);
	}

	/**
	 * Gets the title.
	 * 
	 * @return the title.
	 */
	public String getTitle() {
		return props.getProperty(TITLE);
	}

	/**
	 * Gets value to show or not the title
	 * @return	TRUE or FALSE
	 */
	public String getShowTitle() {
		return props.getProperty(TITLE_SHOW_LINE);
	}
	
	/**
	 * Gets value to show or not the subtitle #1
	 * @return	TRUE or FALSE
	 */
	public String getShowSubtitle1() {
		return props.getProperty(SUBTITLE_1_SHOW_LINE);
	}
	
	/**
	 * Gets value to show or not the subtitle #2
	 * @return	TRUE or FALSE
	 */
	public String getShowSubtitle2() {
		return props.getProperty(SUBTITLE_2_SHOW_LINE);
	}
	
	/**
	 * Gets value to show or not the footer #1
	 * @return	TRUE or FALSE
	 */
	public String getShowFooter1() {
		return props.getProperty(FOOTER1_SHOW_LINE);
	}
	
	/**
	 * Gets value to show or not the footer #2
	 * @return	TRUE or FALSE
	 */
	public String getShowFooter2() {
		return props.getProperty(FOOTER2_SHOW_LINE);
	}
	
	/**
	 * Gets the units text.
	 * 
	 * @return the units text.
	 */
	public String getUnits() {
		return props.getProperty(UNITS);
	}

	/**
	 * Gets the file name of a configuration file.
	 * 
	 * @return the file name of a configuration file.
	 */
	public String getConfigFileName() {
		return props.getProperty(CONFIG_FILE);
	}

	/**
	 * Sets the file name of a configuration file.
	 * 
	 * @param fileName	the name of the configuration file
	 */
	public void setConfigFileName(String fileName) {
		props.setProperty(CONFIG_FILE, fileName);
	}

	public String getString(String key) {
		return (String) props.get(key);
	}

	public Color getColor(String key) {
		return (Color) props.get(key);
	}

	public Font getFont(String key) {
		return (Font) props.get(key);
	}

	public File getPreviousFolder() {
		return previousFolder;
	}

	public void setPreviousFolder(File file) {
		if (file == null || !file.exists() || !file.isDirectory())
			return;

		if (file.isDirectory())
			previousFolder = file;
	}

	/**
	 * Merges this PlotConfiguration with the specified configuration. The
	 * specified config will override any configuration info in this PlotConfiguration.
	 * 
	 * @param config	the configuration to merge with
	 */
	public void merge(PlotConfiguration config) {
		for (Object key : config.props.keySet()) {
			props.put(key, config.props.get(key));
		}
	}

	/**
	 * Sets value to show or not the title
	 * @return	TRUE or FALSE
	 */
	public void setShowTitle(String show) {
		props.setProperty(TITLE_SHOW_LINE, show);
	}
	
	public void setShowTitle(Boolean aBool)
	{
		props.setProperty(TITLE_SHOW_LINE, aBool.toString());
	}
	
	/**
	 * Sets value to show or not the subtitle #1
	 * @return	TRUE or FALSE
	 */
	public void setShowSubtitle1(String show) {
		props.setProperty(SUBTITLE_1_SHOW_LINE, show);
	}
	
	/**
	 * Sets value to show or not the subtitle #2
	 * @return	TRUE or FALSE
	 */
	public void setShowSubtitle2(String show) {
		props.setProperty(SUBTITLE_2_SHOW_LINE, show);
	}

	/**
	 * Saves this PlotConfiguration to a file.
	 * 
	 * @param file	the file to save the configuration to
	 * @param saveTitle	indicates whether to save the tile as part of the configuration or not
	 * @throws IOException	if there is an error during saving.
	 */
	public void save(File file, boolean saveTitle) throws IOException {
		PlotConfigurationIO io = new PlotConfigurationIO();
		Map<String, Object> map = new HashMap<String, Object>();
		for (Object key : props.keySet()) {
			if (key instanceof String && props.get(key) != null) {
				if (saveTitle) {
					map.put((String) key, props.get(key));
					continue;
				}
				
				if (!key.equals(TITLE) 
						&& !key.equals(TITLE_FONT) 
						&& !key.equals(TITLE_COLOR)
						&& !key.equals(SUBTITLE_1)
						&& !key.equals(SUBTITLE_1_FONT)
						&& !key.equals(SUBTITLE_1_COLOR)
						&& !key.equals(SUBTITLE_2)
						&& !key.equals(SUBTITLE_2_FONT)
						&& !key.equals(SUBTITLE_2_COLOR))
					map.put((String) key, props.get(key));
			}
		}

		io.saveConfiguration(file, map);

		previousFolder = file.getParentFile();
	}
}
