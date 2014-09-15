package anl.verdi.plot.config;

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.bbn.openmap.util.DeepCopyUtil;

/**
 * Extensible plot configuration data.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class PlotConfiguration {

	public static final String TITLE = PlotConfiguration.class.getName()
			+ ".title";
	public static final String TITLE_FONT = PlotConfiguration.class.getName()
			+ ".title_font";
	public static final String TITLE_COLOR = PlotConfiguration.class.getName()
			+ ".title_color";
	public static final String SUBTITLE_1 = PlotConfiguration.class.getName()
			+ ".subtitle1";
	public static final String SUBTITLE_1_FONT = PlotConfiguration.class.getName()
			+ ".subtitle1_font";
	public static final String SUBTITLE_1_COLOR = PlotConfiguration.class.getName()
			+ ".subtitle1_color";
	public static final String SUBTITLE_2 = PlotConfiguration.class.getName()
			+ ".subtitle2";
	public static final String SUBTITLE_2_FONT = PlotConfiguration.class.getName()
			+ ".subtitle2_font";
	public static final String SUBTITLE_2_COLOR = PlotConfiguration.class.getName()
			+ ".subtitle2_color";
	public static final String UNITS = PlotConfiguration.class.getName()
			+ ".units";
	public static final String UNITS_FONT = PlotConfiguration.class.getName()
			+ ".units_font";
	public static final String UNITS_COLOR = PlotConfiguration.class.getName()
			+ ".units_color";
	public static final String UNITS_SHOW_TICK = PlotConfiguration.class.getName()
			+ ".units_show_tick";
	public static final String UNITS_TICK_COLOR = PlotConfiguration.class.getName()
			+ ".units_tick_color";
	public static final String UNITS_TICK_FONT = PlotConfiguration.class.getName()
			+ ".units_tick_font";
	public static final String UNITS_TICK_NUMBER = PlotConfiguration.class.getName() + ".units_tick_number";

	public static final String DOMAIN_LABEL = PlotConfiguration.class.getName()
			+ ".domain";
	public static final String DOMAIN_FONT = PlotConfiguration.class.getName()
			+ ".domain_font";
	public static final String DOMAIN_COLOR = PlotConfiguration.class.getName()
			+ ".domain_color";
	public static final String DOMAIN_SHOW_TICK = PlotConfiguration.class.getName()
			+ ".domain_show_tick";
	public static final String DOMAIN_TICK_COLOR = PlotConfiguration.class.getName()
			+ ".domain_tick_color";
	public static final String DOMAIN_TICK_FONT = PlotConfiguration.class.getName()
			+ ".domain_tick_font";
	public static final String DOMAIN_TICK_NUMBER = PlotConfiguration.class.getName() + ".domain_tick_number";
	
	public static final String RANGE_LABEL = PlotConfiguration.class.getName()
			+ ".range";
	public static final String RANGE_FONT = PlotConfiguration.class.getName()
			+ ".range_font";
	public static final String RANGE_COLOR = PlotConfiguration.class.getName()
			+ ".range_color";
	public static final String RANGE_SHOW_TICK = PlotConfiguration.class.getName()
			+ ".range_show_tick";
	public static final String RANGE_TICK_COLOR = PlotConfiguration.class.getName()
			+ ".range_tick_color";
	public static final String RANGE_TICK_FONT = PlotConfiguration.class.getName()
			+ ".range_tick_font";
	public static final String RANGE_TICK_NUMBER = PlotConfiguration.class.getName() + ".range_tick_number";
	
	public static final String Z_LABEL = PlotConfiguration.class.getName()
			+ ".z";
	public static final String Z_FONT = PlotConfiguration.class.getName()
			+ ".z_font";
	public static final String Z_COLOR = PlotConfiguration.class.getName()
			+ ".z_color";
	public static final String Z_SHOW_TICK = PlotConfiguration.class.getName()
			+ ".z_show_tick";
	public static final String Z_TICK_COLOR = PlotConfiguration.class.getName()
			+ ".z_tick_color";
	public static final String Z_TICK_FONT = PlotConfiguration.class.getName()
			+ ".z_tick_font";
	public static final String Z_TICK_NUMBER = PlotConfiguration.class.getName() + ".z_tick_number";
	
	public static final String FOOTER1 = PlotConfiguration.class.getName() + ".footer_line_1";
	public static final String FOOTER1_SHOW_LINE = PlotConfiguration.class.getName() + ".footer_show_line_1";
	public static final String FOOTER1_AUTO_TEXT = PlotConfiguration.class.getName() + ".footer_line_1_auto_text";
    public static final String FOOTER1_COLOR = PlotConfiguration.class.getName() + ".footer_line_1_color";
    public static final String FOOTER1_FONT = PlotConfiguration.class.getName() + ".footer_line_1_font";
	public static final String FOOTER2 = PlotConfiguration.class.getName() + ".footer_line_2";
	public static final String FOOTER2_SHOW_LINE = PlotConfiguration.class.getName() + ".footer_show_line_2";
	public static final String FOOTER2_AUTO_TEXT = PlotConfiguration.class.getName() + ".footer_line_2_auto_text";
    public static final String FOOTER2_COLOR = PlotConfiguration.class.getName() + ".footer_line_2_color";
    public static final String FOOTER2_FONT = PlotConfiguration.class.getName() + ".footer_line_2_font";
	public static final String OBS_SHOW_LEGEND = PlotConfiguration.class.getName() + ".obs_show_legend";
    public static final String OBS_LEGEND_COLOR = PlotConfiguration.class.getName() + ".obs_legend_color";
    public static final String OBS_LEGEND_FONT = PlotConfiguration.class.getName() + ".obs_legend_font";

	public static final String CONFIG_FILE = PlotConfiguration.class.getName()
			+ ".config_file";

	private static File previousFolder;

	protected Properties props = new Properties();

	public PlotConfiguration() {
		System.out.println("in PlotConfiguration, default constructor");
	}

	/**
	 * Loads the configuration from the specified File.
	 * 
	 * @param file
	 *            the file to load from
	 * @throws IOException
	 *             if there is an error during loading.
	 */
	public PlotConfiguration(File file) throws IOException {
		PlotConfiguration config = new PlotConfigurationIO().loadConfiguration(file);
		this.merge(config);
		System.out.println("in PlotConfiguration, constructor from a file");
	}

	/**
	 * Copy constructor.
	 * 
	 * @param config
	 *            the configuration to copy into this PlotConfiguration
	 */
	public PlotConfiguration(PlotConfiguration config) {
//		PlotConfiguration config2 = null; 
		System.out.println("in Plot Configuration, copy constructor");
		try {
//			config2 = (PlotConfiguration)DeepCopyUtil.copy(config);
			for (Object key : config.props.keySet()) {
//				props.put(key, config.props.get(key));
				props.put(key, DeepCopyUtil.copy(config.props.get(key)));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		props = config2.props;
//		for (Object key : config.props.keySet()) {
//			props.put(key, String.valueOf(config.props.get(key)));
//		}
		
		
	}

	/**
	 * Puts the specified object into the configuration map with the specified
	 * key.
	 * 
	 * @param key
	 *            the configuration key
	 * @param value
	 *            the configuration value
	 */
	public void putObject(Object key, Object value) {
		if (value != null)
			props.put(key, value);
	}

	/**
	 * Removes the specified object from the configuration map.
	 * 
	 * @param key
	 *            the configuration key
	 */
	public void removeObject(Object key) {
		if (props.contains(key))
			props.remove(key);
	}

	/**
	 * Gets the object associated with the specified key from the configuration
	 * map.
	 * 
	 * @param key
	 *            the key of the object to return
	 * @return the object associated with the specified key from the
	 *         configuration map or null if no such object exists.
	 */
	public Object getObject(Object key) {
		return props.get(key);
	}

	/**
	 * Sets the named configuration property to the specified value.
	 * 
	 * @param name
	 *            the name of the property to set
	 * @param value
	 *            the value of the property
	 */
	public void setProperty(String name, String value) {
		props.setProperty(name, value);
	}

	/**
	 * Gets the value of the named property.
	 * 
	 * @param name
	 *            the name of the property to get
	 * @return the value of the named property.
	 */
	public String getProperty(String name) {
		return props.getProperty(name);
	}

	/**
	 * Sets the text for subtitle 1.
	 * 
	 * @param val
	 *            the text of subtitle 1.
	 */
	public void setSubtitle1(String val) {
		props.setProperty(SUBTITLE_1, val);
	}

	/**
	 * Sets the text for subtitle 2.
	 * 
	 * @param val
	 *            the text of subtitle 2.
	 */
	public void setSubtitle2(String val) {
		props.setProperty(SUBTITLE_2, val);
	}

	/**
	 * Sets the title text.
	 * 
	 * @param val
	 *            the title text
	 */
	public void setTitle(String val) {
		props.setProperty(TITLE, val);
	}

	/**
	 * Sets the units text.
	 * 
	 * @param val
	 *            the units text
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
	 * @param fileName
	 *            the name of the configuration file
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
	 * specified config will override any configuration info in this
	 * PlotConfiguration.
	 * 
	 * @param config
	 *            the configuration to merge with
	 */
	public void merge(PlotConfiguration config) {
		for (Object key : config.props.keySet()) {
			props.put(key, config.props.get(key));
		}
	}

	/**
	 * Saves this PlotConfiguration to a file.
	 * 
	 * @param file
	 *            the file to save the configuration to
	 * @param saveTitle
	 *            indicates whether to save the tile as part of the
	 *            configuration or not
	 * @throws IOException
	 *             if there is an error during saving.
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
