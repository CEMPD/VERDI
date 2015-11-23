package anl.verdi.plot.util;

import org.jfree.chart.ChartTheme;

/***
 * Make it a singleton class to gather cross plot properties
 * 
 * @author qun
 *
 */
public class PlotProperties {

	private static PlotProperties instance;
	
	private static ChartTheme currentTheme;
	
	/***
	 *  A private Constructor prevents any other 
	 *  class from instantiating.
	 */
	private PlotProperties () {
		//NOTE: no initial properties
	}
	
	public static PlotProperties getInstance() {
		if (instance == null)
			return new PlotProperties();
		
		return instance;
	}
	
	public ChartTheme getCurrentTheme() {
		return currentTheme;
	}
	
	/***
	 * Set a theme as cross-plot theme
	 * 
	 * @param theme
	 */
	public void setCurrentTheme(ChartTheme theme) {
		currentTheme = theme;
	}
}
