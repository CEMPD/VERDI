package anl.verdi.plot.config;

import java.awt.Color;
import java.awt.Font;

/**
 * Interface for configuring a Chart's units label.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface UnitsConfigurator {

	/**
	 * Configure the units label of a chart with the specified text, font, and color.
	 *
	 * @param text the units label text
	 * @param font the units label font
	 * @param color the units label color
	 */
	void configureUnits(Boolean showLegend, String text, Font font, Color color);

	void configureUnitsTick(Boolean show, Font font, Color color);

}
