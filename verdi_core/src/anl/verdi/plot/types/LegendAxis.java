package anl.verdi.plot.types;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.text.NumberFormat;
import java.util.List;

import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.Tick;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.TextAnchor;

import anl.verdi.plot.color.ColorMap;

/**
 * A NumberAxis that that runs from 0 to the number of colors in a color map. 
 * This overrides refreshTicksVertical to label the interval with the
 * color map starting values rather than the actual numeric values in the axis.
 * This also creates a LookupPaintScale for use with this axis so that the
 * paint values in the color map correspond with this axis' values. 
 * 
 * This whole thing is basically a trick to allow each color map interval
 * to be painted in an equal area, rather than have the painted area 
 * correspond to the range of the color map value. That is, if there are
 * two intervals 0 - 10, and 10 - 50, under normal circumstances the
 * legend will paint the 0 - 10 range in 1/5th the space of the 10 - 50 range.
 * This legend axis insures that they each get 1/2.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class LegendAxis extends NumberAxis {

	private double min, max; 
	private int intervalCount;
	private ColorMap map;
	private LookupPaintScale paintScale;


	/**
	 * Constructs a number axis, using default values where necessary.
	 *
	 * @param label the axis label (<code>null</code> permitted).
	 * @throws Exception 
	 */
	public LegendAxis(String label, double min, double max, ColorMap map) throws Exception {
		super(label);
		this.min = min;
		this.max = max;
		this.intervalCount = map.getColorCount();
		this.map = map;
		
		setNumberFormatOverride(map.getNumberFormat());
		
		paintScale = createPaintScale(map);
		
		if (max == min) {
			this.setRangeAboutValue(0, .1);
		} else {
			this.setRange(0, intervalCount);
		}
	}
	
	private LookupPaintScale createPaintScale(ColorMap map) {
		int colorCount = map.getColorCount();
		LookupPaintScale paintScale = null;
		if (min < max) paintScale = new LookupPaintScale(0, intervalCount, Color.WHITE);
		else paintScale = new LookupPaintScale();
		if (max == min) {
			paintScale.add(new Double(0), map.getColor(0));
		} else {
			for (int i = 0; i < colorCount; i++) {
				paintScale.add(new Double(i), map.getColor(i));
			}
		}
		
		/*
		if (map.getIntervalType() == ColorMap.IntervalType.AUTOMATIC) {
			double interval = (max - min) / colorCount;
			if (max == min) {
				paintScale.add(new Double(min), map.getColor(0));
			} else {
				for (int i = 0; i < colorCount; i++) {
					paintScale.add(new Double(min + (i * interval)), map.getColor(i));
				}
			}
		} else {
			// use the values directly from the color map itself
			for (int i = 0; i < colorCount; i++) {
				paintScale.add(new Double(map.getIntervalStart(i)), map.getColor(i));
			}
		}
		*/

		return paintScale;
	}
	
	public LookupPaintScale getPaintScale() {
		return paintScale;
	}

	/**
	 * Calculates the positions of the tick labels for the axis, storing the
	 * results in the tick label list (ready for drawing).
	 *
	 * @param g2       the graphics device.
	 * @param dataArea the area in which the plot should be drawn.
	 * @param edge     the location of the axis.
	 * @return A list of ticks.
	 */
	@Override
	protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea, RectangleEdge edge) {
		List result = new java.util.ArrayList();

		Font tickLabelFont = getTickLabelFont();
		g2.setFont(tickLabelFont);

		for (int i = 0; i <= intervalCount; i++) {
			double currentTickValue = 0;
			try {
				currentTickValue = i == intervalCount ? max : map.getIntervalStart(i);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}//min + i * interval;
			String tickLabel;
			NumberFormat formatter = getNumberFormatOverride();
			if (formatter != null) {
				tickLabel = formatter.format(currentTickValue);
			} else {
				tickLabel = getTickUnit().valueToString(currentTickValue);
			}

			TextAnchor anchor = null;
			TextAnchor rotationAnchor = null;
			double angle = 0.0;
			if (isVerticalTickLabels()) {
				if (edge == RectangleEdge.LEFT) {
					anchor = TextAnchor.BOTTOM_CENTER;
					rotationAnchor = TextAnchor.BOTTOM_CENTER;
					angle = -Math.PI / 2.0;
				} else {
					anchor = TextAnchor.BOTTOM_CENTER;
					rotationAnchor = TextAnchor.BOTTOM_CENTER;
					angle = Math.PI / 2.0;
				}
			} else {
				if (edge == RectangleEdge.LEFT) {
					anchor = TextAnchor.CENTER_RIGHT;
					rotationAnchor = TextAnchor.CENTER_RIGHT;
				} else {
					anchor = TextAnchor.CENTER_LEFT;
					rotationAnchor = TextAnchor.CENTER_LEFT;
				}
			}

			Tick tick = new NumberTick(
							new Double(i), tickLabel, anchor,
							rotationAnchor, angle
			);
			result.add(tick);

		}
		return result;

	}
}
