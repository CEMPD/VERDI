package anl.verdi.plot.util;

import org.jfree.chart.ChartTheme;

import anl.verdi.plot.config.ThemeConfig;

/***
 * Make it a singleton class to gather cross plot properties
 * 
 * @author qun
 *
 */
public class PlotProperties {

	private static PlotProperties instance;
	private ChartTheme currentTheme;
	private ThemeConfig themeConfig;
	
	/***
	 *  A private Constructor prevents any other 
	 *  class from instantiating.
	 */
	private PlotProperties () {
		//NOTE: no initial properties
	}
	
	public static PlotProperties getInstance() {
		if (instance == null)
			instance = new PlotProperties();
		
		return instance;
	}
	
	public ChartTheme getCurrentTheme() {
		return currentTheme;
	}
	
	public ThemeConfig getThemeConfig() {
		return themeConfig;
	}

	/***
	 * Set a theme configuration object
	 * @param themeConfig
	 */
	public void setThemeConfig(ThemeConfig themeConfig) {
		this.themeConfig = themeConfig;
		this.currentTheme = themeConfig.getTheme();
	}
}
