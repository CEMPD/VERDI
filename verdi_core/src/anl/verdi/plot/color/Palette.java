package anl.verdi.plot.color;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.geotools.brewer.color.BrewerPalette;
import org.geotools.brewer.color.ColorBrewer;


/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Palette implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
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

	private Color[] reverseColorMap() {
		List<Color> reversedColors = new ArrayList<Color>();
		reversedColors.addAll(Arrays.asList(colors));
		Collections.reverse(reversedColors);
		
		return reversedColors.toArray(new Color[0]);
	}

	public void setColor(int index, Color color) {
System.out.println("in Palette.setColor for index = " + index);
		this.colors[index] = color;
	}

	public int getColorCount() {
System.out.println("in Palette.getColorCount() = " + colors.length);
		return colors.length;
	}

	public Color getColor(int index) {
System.out.println("in Palette.getColor for index = " + index);
		return colors[(!reverseColors ? index : getColorCount() - 1 - index)];
	}

	public static Palette getDefaultPalette() {
System.out.println("in Palette.getDefaultPalette");
		ColorBrewer brewer = new ColorBrewer();	// calls to GeoTools library
System.out.println("just completed call to GeoTools ColorBrewer default constructor");
		brewer.loadPalettes();
System.out.println("finished loading palettes");
		BrewerPalette palette = brewer.getPalettes(ColorBrewer.QUALITATIVE)[0];
System.out.println("back from call to GeoTools .getPalettes to set BrewerPalette object");
		// 2014 to get the identifiers of the 8 default colors
		for(int i=0; i<8; i++)
		{
			System.out.println("color[" + i + "] = " + palette.getColors(i).toString());
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
