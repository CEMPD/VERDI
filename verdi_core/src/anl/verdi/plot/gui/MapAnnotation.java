package anl.verdi.plot.gui;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.geotools.geometry.jts.ReferencedEnvelope;
//import org.geotools.map.MapContext;		// MapContext was deprecated
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.opengis.referencing.operation.MathTransform2D;

import anl.verdi.data.Axes;
import anl.verdi.data.BoundingBoxer;
import anl.verdi.data.DataFrameAxis;

public class MapAnnotation extends AbstractXYAnnotation {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1832169091574647868L;
	GTRenderer renderer;
	MathTransform2D transform;

//	MapContext context;
	MapContent context;
//	private long domainOrigin, domainMax, rangeOrigin, rangeMax;
//	private ReferencedEnvelope origEnv;
	private BufferedImage image;
	private BoundingBoxer boxer;
	private boolean update = true;
	private double curWidth, curHeight;
	private Range xRange, yRange;
	//private XYBlockRenderer blockRenderer;
	private int netcdfConv = -1;

	public MapAnnotation() {
	}


	public boolean isUpdate() {
		return update;
	}

	public void setUpdate(boolean update) {
		this.update = update;
	}


//	public MapContext getMapContext() {
	public MapContent getMapContext() {
		return context;
	}

//	public MapAnnotation(MapContext context, Axes<DataFrameAxis> axes, int netcdfConv) {
	public MapAnnotation(MapContent context, Axes<DataFrameAxis> axes, int netcdfConv) {
		this.netcdfConv  = netcdfConv;
		renderer = new StreamingRenderer();
//		renderer.setContext(context);
		renderer.setMapContent(context);
		this.context = context;
		this.boxer = axes.getBoundingBoxer();
//		domainOrigin = axes.getXAxis().getOrigin();
//		domainMax = axes.getXAxis().getRange().getUpperBound();
//		rangeOrigin = axes.getYAxis().getOrigin();
//		rangeMax = axes.getYAxis().getRange().getUpperBound();
		//transform = (MathTransform2D) coverage.getGridGeometry().getGridToCRS();
//		origEnv = context.getAreaOfInterest();
//		origEnv = context.getMaxBounds();
		xRange = new Range(0, 0);
		yRange = new Range(0, 0);
	}

	/**
	 * Resets the annotation forcing a redraw.
	 */
	public void reset() {
		image = null;
		xRange = new Range(0, 0);
	}

	@Override
	public void draw(Graphics2D g2d, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis,
	                 int rendererIndex, PlotRenderingInfo info) {
		MapViewport aViewport = new MapViewport();

		if (update) {
			boolean render = false;
			Rectangle bounds = dataArea.getBounds();
			if (image == null || curWidth != bounds.width || curHeight != bounds.height) {
				image = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
				curWidth = bounds.width;
				curHeight = bounds.height;
				render = true;
			}

//			double x = dataArea.getX();
//			double y = dataArea.getY();
//			double width = dataArea.getWidth();
//			double height = dataArea.getHeight();

			if (!xRange.equals(domainAxis.getRange()) || !yRange.equals(rangeAxis.getRange())) {
				image = new BufferedImage(bounds.width, bounds.height, BufferedImage.TYPE_4BYTE_ABGR);
				render = true;
				xRange = domainAxis.getRange();
				yRange = rangeAxis.getRange();

				// reset the aoe
				double xMin = xRange.getLowerBound() + .5;
				/*
				if (xMin < domainOrigin) {
					// zoomed out so that plotted data area is smaller
					// than the entire plot.
					xMin = domainOrigin;
					double xOffset = ((XYBlockRenderer)plot.getRenderer()).getXOffset();
					x = domainAxis.valueToJava2D(xMin + xOffset, dataArea, plot.getDomainAxisEdge());

				}
				*/

				double xMax = xRange.getUpperBound() - .5;

				/*
				if (xMax > domainMax) {
					xMax = domainMax;
					double xOffset = ((XYBlockRenderer)plot.getRenderer()).getXOffset();
					double lastX = domainAxis.valueToJava2D(domainMax - xOffset, dataArea, plot.getDomainAxisEdge());
					width = lastX - x;
				}
				*/

				double yMin = yRange.getLowerBound() + .5;
				/*
				if (yMin < rangeOrigin) {
					yMin = rangeOrigin;
					double yOffset =((XYBlockRenderer)plot.getRenderer()).getYOffset();
					y = rangeAxis.valueToJava2D(rangeMax - yOffset, dataArea, plot.getRangeAxisEdge());
				}
				*/

				double yMax = yRange.getUpperBound() - .5;
				/*
				if (yMax > rangeMax) {
					yMax = rangeMax;
					double yOffset = ((XYBlockRenderer)plot.getRenderer()).getYOffset();
					double firstY = rangeAxis.valueToJava2D(rangeOrigin + yOffset , dataArea, plot.getRangeAxisEdge());
					height = firstY - y;
				}
				*/


				ReferencedEnvelope env = boxer.createBoundingBox(xMin, xMax, yMin, yMax, this.netcdfConv);
//				context.setAreaOfInterest(env);
				aViewport.setBounds(env);
			}

			if (render) {
				Graphics2D g2d2 = image.createGraphics();
//				renderer.paint(g2d2, new Rectangle(bounds.width, bounds.height), context.getAreaOfInterest());
				renderer.paint(g2d2, new Rectangle(bounds.width, bounds.height), aViewport.getBounds());
			}
			//g2d.drawImage(image, (int) (x), (int) y, (int) width, (int)Math.rint(height), null);
			g2d.drawImage(image, bounds.x, bounds.y, bounds.width, bounds.height, null);

		}
	}
}
