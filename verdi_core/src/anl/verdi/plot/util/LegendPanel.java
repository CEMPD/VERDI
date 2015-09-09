package anl.verdi.plot.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.swing.JFrame;
import org.geotools.swing.JMapFrame;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.PavePaletteCreator;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class LegendPanel extends JComponent {

	private static final long serialVersionUID = -9100503749455967327L;
	static final Logger Logger = LogManager.getLogger(LegendPanel.class.getName());
	private PaintScaleLegend legend;

	public LegendPanel(ColorMap map, double min, double max, String units) {
		Logger.debug("in constructor for LegendPanel for a ColorMap, min, max, and units");
		initLegend(map, min, max, units);
	}


	@Override
	protected void paintComponent(Graphics g) {
		Logger.debug("in LegendPanel.paintComponent");
		if (isOpaque()) { //paint background
			g.setColor(getBackground());
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		Graphics2D g2d = (Graphics2D) g.create();
		legend.draw(g2d, g.getClipBounds());
		g2d.dispose();
	}

	public Dimension getPreferredSize() {
		return new Dimension(100, 100);
	}

	public Dimension getMinimumSize() {
		return new Dimension(100, 100);
	}

	public void initLegend(ColorMap map, double min, double max, String units) {
		Logger.debug("in LegendPanel.initLegend for ColorMap, min, max, units");
		LookupPaintScale scale = createPaintScale(map, min, max);
		NumberAxis scaleAxis = new NumberAxis(units);
		scaleAxis.setTickMarkPaint(Color.BLACK);
		scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 9));
		scaleAxis.setRange(min, max);
		if (legend == null) {
			legend = new PaintScaleLegend(scale, scaleAxis);
			legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
			legend.setAxisOffset(5.0);
			legend.setMargin(new RectangleInsets(5, 5, 5, 5));
//			legend.setBorder(new BlockBorder(Color.red));	// 2014 deprecated, changed .setBorder(BlockBorder) to setFrame(BlockFrame)
			legend.setFrame(new BlockBorder(Color.red));
			legend.setPadding(new RectangleInsets(10, 10, 10, 10));
			legend.setStripWidth(10);
			legend.setPosition(RectangleEdge.RIGHT);
			legend.setBackgroundPaint(Color.WHITE);
		} else {
			legend.setScale(scale);
			legend.setAxis(scaleAxis);
		}
	}

	public PaintScaleLegend getLegend() {
		Logger.debug("in LegendPanel.getLegend");
		return legend;
	}

	// creates the legend scale from the color map
	protected LookupPaintScale createPaintScale(ColorMap map, double min, double max) {
		Logger.debug("in LegendPanel.createPaintScale");
		int colorCount = map.getColorCount();
		LookupPaintScale paintScale = new LookupPaintScale(min, max, Color.GRAY);
		if (map.getIntervalType() == ColorMap.IntervalType.AUTOMATIC) { // TODO: need to add logarithm
			double interval = (max - min) / colorCount;
			if (max == min) {
//				paintScale.add(new Double(min), map.getColor(0));	// 2014 old add deprecated
				paintScale.add(min, map.getColor(0));
			} else {
				for (int i = 0; i < colorCount; i++) {
//					paintScale.add(new Double(min + (i * interval)), map.getColor(i));	// 2014 old add deprecated
					paintScale.add((min + (i * interval)), map.getColor(i));
				}
			}
		} else {
			// use the values directly from the color map itself
			for (int i = 0; i < colorCount; i++) {
				try {
//					paintScale.add(new Double(map.getIntervalStart(i)), map.getColor(i));	// 2014 old add deprecated
					paintScale.add(map.getIntervalStart(i), map.getColor(i));
					} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}

		return paintScale;
	}

	public static void main(String[] args) {
//		ColorMap map = new ColorMap(new PavePaletteCreator().createPalettes(8).get(0), 0, 10);
		ColorMap map = new ColorMap(new PavePaletteCreator().createPavePalette(), 0, 10);
		LegendPanel panel = new LegendPanel(map, 0, 10, "Units");
//		JFrame frame = new JFrame();
//		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JMapFrame frame = new JMapFrame();
		frame.setDefaultCloseOperation(JMapFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}
}
