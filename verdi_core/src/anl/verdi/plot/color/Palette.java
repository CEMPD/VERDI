package anl.verdi.plot.color;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Palette implements Serializable {

	private static final long serialVersionUID = 1L;
	static final Logger Logger = LogManager.getLogger(Palette.class.getName());
	private Color[] colors;
	private String description = "";
	private boolean reverseColors;
	
	public Palette(Palette pal) {
		colors = new Color[pal.colors.length];
		System.arraycopy(pal.colors, 0, colors, 0, pal.colors.length);
		this.description = pal.description;
		this.reverseColors = pal.reverseColors;
	}

	public Palette(Color[] colors, String description, boolean reverseColors) {
		this.colors = colors;
		this.description = description;
		this.reverseColors = reverseColors;
	}

	public String getDescription() {
		return description;
	}

	public Color[] getColors() {
		if (!reverseColors) {
			return colors;
		} else {
			return reverseColorMap();
		}
	}

	public Color[] getOriginalColors() {
		return colors;
	}
	
	public String getHexColor(Color color) {
		return "#"+Integer.toHexString(color.getRGB()).substring(2).toUpperCase();
	}

	private Color[] reverseColorMap() {
		List<Color> reversedColors = new ArrayList<Color>();
		reversedColors.addAll(Arrays.asList(colors));
		Collections.reverse(reversedColors);
		
		return reversedColors.toArray(new Color[0]);
	}

	public void setColor(int index, Color color) {
//		Logger.debug("in Palette.setColor for index = " + index);
		this.colors[index] = color;
	}

	public int getColorCount() {
//		Logger.debug("in Palette.getColorCount() = " + colors.length);
		return colors.length;
	}

	public Color getColor(int index) {
//		Logger.debug("in Palette.getColor for index = " + index);
		return colors[(!reverseColors ? index : getColorCount() - 1 - index)];
	}

	public static Palette getDefaultPalette() {
//		Logger.debug("in Palette.getDefaultPalette");
		ColorBrewer brewer = new ColorBrewer();	// calls to GeoTools library
//		Logger.debug("just completed call to GeoTools ColorBrewer default constructor");
		brewer.loadPalettes();
//		Logger.debug("finished loading palettes");
		BrewerPalette palette = brewer.getPalettes(ColorBrewer.QUALITATIVE)[0];
//		Logger.debug("back from call to GeoTools .getPalettes to set BrewerPalette object");
		// 2014 to get the identifiers of the 8 default colors
		for(int i=0; i<8; i++)
		{
			Logger.debug("color[" + i + "] = " + palette.getColors(i).toString());
		}
		return new Palette(palette.getColors(8), palette.getDescription(), false);
	}

	public void setReverseColors(boolean reverseColors) {
		this.reverseColors = reverseColors;
	}

	public boolean isReverseColors() {
		return reverseColors;
	}
}
