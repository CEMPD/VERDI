package anl.verdi.plot.config;

import java.awt.Color;
import java.awt.Font;

/**
 * Interface for configuring a Chart's titles.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface TitleConfigurator {

	/**
	 * Configure the title of a chart with the specified text, font, and color.
	 *
	 * @param text the title text
	 * @param font the title font
	 * @param color the title color
	 */
	void configureTitle(String text, Font font, Color color);

	/**
	 * Configure the subtitle 1 of a chart with the specified text, font, and color.
	 *
	 * @param text the subtitle 1 text
	 * @param font the subtitle 1 font
	 * @param color the subtitle 1 color
	 */
	void configureSubtitle1(String text, Font font, Color color);
	
	/**
	 * Configure the subtitle 2 of a chart with the specified text, font, and color.
	 *
	 * @param text the subtitle 2 text
	 * @param font the subtitle 2 font
	 * @param color the subtitle 2 color
	 */
	void configureSubtitle2(String text, Font font, Color color);
}
