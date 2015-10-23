package anl.verdi.plot.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsConfiguration;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;

import javax.media.j3d.J3DGraphics2D;

import visad.VisADException;
import visad.java3d.DisplayRendererJ3D;
import visad.java3d.VisADCanvasJ3D;
import anl.verdi.plot.config.Title;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VerdiCanvas3D extends VisADCanvasJ3D {

	private Object lock = new Object();
	private Title title = new Title("", Font.decode("Dialog-plain-14"), Color.WHITE);
	private Title sub1 = new Title("", null, Color.WHITE);
	private Title sub2 = new Title("", null, Color.WHITE);
	private Title time = new Title("", null, Color.WHITE);
	private Font defaultFont;
	private Color defaultColor = Color.WHITE;

	public VerdiCanvas3D(DisplayRendererJ3D renderer) {
		super(renderer);
	}

	public VerdiCanvas3D(DisplayRendererJ3D renderer, GraphicsConfiguration config) {
		super(renderer, config);
	}

	public VerdiCanvas3D(DisplayRendererJ3D renderer, int w, int h) throws VisADException {
		super(renderer, w, h);
	}

	public void setTitleText(String text) {
		if (text == null) text = "";
		synchronized (lock) {
			title.setText(text);
		}
	}

	public void setTitleFont(Font font) {
		synchronized (lock) {
			title.setFont(font);
		}
	}

	public void setTitleColor(Color color) {
		synchronized (lock) {
			title.setColor(color);
		}
	}

	public void setTimeText(String text) {
		if (text == null) text = "";
		synchronized (lock) {
			time.setText(text);
		}
	}

	public void setTimeFont(Font font) {
		synchronized (lock) {
			time.setFont(font);
		}
	}

	public void setTimeColor(Color color) {
		synchronized (lock) {
			time.setColor(color);
		}
	}

	public void setSub1Text(String text) {
		if (text == null) text = "";
		synchronized (lock) {
			this.sub1.setText(text);
		}
	}

	public void setSub1Font(Font font) {
		synchronized (lock) {
			this.sub1.setFont(font);
		}
	}

	public void setSub1Color(Color color) {
		synchronized (lock) {
			sub1.setColor(color);
		}
	}

	public void setSub2Text(String text) {
		if (text == null) text = "";
		synchronized (lock) {
			this.sub2.setText(text);
		}
	}

	public void setSub2Font(Font font) {
		synchronized (lock) {
			this.sub2.setFont(font);
		}
	}

	public void setSub2Color(Color color) {
		synchronized (lock) {
			sub2.setColor(color);
		}
	}

	public Title getSub1() {
		return sub1;
	}

	public Title getSub2() {
		return sub2;
	}

	public Title getTime() {
		return time;
	}

	public Title getTitle() {
		return title;
	}

	public void renderField(int i) {
		J3DGraphics2D g2D = this.getGraphics2D();
		if (defaultFont == null) {
			defaultFont = g2D.getFont();
		}
		synchronized (lock) {
			int yOffset = 1;
			yOffset = drawString(title, g2D, yOffset);
			yOffset = drawString(sub1, g2D, yOffset);
			yOffset = drawString(sub2, g2D, yOffset);
			drawString(time, g2D, yOffset);
		}
		super.renderField(i);
		g2D.flush(false);
	}

	private int drawString(Title title, J3DGraphics2D g2D, int yOffset) {
		String str = title.getText();
		Font font = title.getFont();
		Color color = title.getColor();
		if (str == null || str.length() == 0) return yOffset;
		if (font == null) font = defaultFont;
		if (color == null) color = defaultColor;
		TextLayout layout = new TextLayout(str, font, g2D.getFontRenderContext());
		Rectangle2D rect = layout.getPixelBounds(null, 0, 0);
		int x = getWidth() / 2 - (int) rect.getWidth() / 2;
		int y = (int) rect.getHeight() + yOffset;
		Color c = g2D.getColor();
		g2D.setColor(color);
		layout.draw(g2D, x, y);
		g2D.setColor(c);
		if (y != yOffset) y += 6;
		return y;
	}
}
