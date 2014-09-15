package anl.verdi.plot.color;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;

/**
 * Implements icon as a colored square.
 * 
 */
public class SquareIcon implements Icon {

  private Color color;
  private int width, height;

  public SquareIcon(Color color, int size) {
    this.color = color;
	  this.width = height = size;
  }

  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
  }

	public Color getColor() {
		return color;
	}

  public void paintIcon(Component comp, Graphics g, int x, int y) {
    Color c = g.getColor();
    g.setColor(color);
    g.fillRect(x, y, width, height);
	  g.setColor(Color.BLACK);
	  g.drawRect(x, y, width, height);
    g.setColor(c);

  }
}
