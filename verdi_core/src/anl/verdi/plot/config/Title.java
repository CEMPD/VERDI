package anl.verdi.plot.config;

import java.awt.Color;
import java.awt.Font;

/**
 * Encapsulates text color and font info.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Title {
	private String text;
	private Font font;
	private Color color;

	public Title(String text, Font font, Color color) {
		this.text = text;
		this.font = font;
		if (font == null) font = Font.decode("Dialog-plain-12");
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public Font getFont() {
		return font;
	}

	public void setFont(Font font) {
		this.font = font;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
