package anl.verdi.plot.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;

import anl.verdi.data.BoundingBoxer;
import anl.verdi.data.VectorData;
import anl.verdi.data.VectorEvaluator;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VectorAnnotation extends AbstractXYAnnotation {
	static final Logger Logger = LogManager.getLogger(VectorAnnotation.class.getName());

	private static final long serialVersionUID = 4048473737201936769L;
	private double baseLength = 0.10;
	private double headLength = 0.14;
	private VectorEvaluator eval;
	private BoundingBoxer tileBoxer;
	private List<VectorData> list;


	public VectorAnnotation(VectorEvaluator eval, int timeStep, BoundingBoxer tileBoxer) {
		this.eval = eval;
		this.tileBoxer = tileBoxer;
		update(timeStep);
		Logger.debug("VectorAnnotation constructor");			
	}

	public synchronized void update(int timeStep) {
		Logger.debug("VectorAnnotation.update for timeStep = " + timeStep);		
		//data = eval.getData(timeStep, layer);
		list = eval.evaluate(timeStep);
		Logger.debug("back from eval.evaluate for this time step"); 
		Logger.debug("list has size: " + list.size());

		for (Iterator<VectorData> iter = list.iterator(); iter.hasNext();) {
			VectorData data = iter.next();
			Point2D p = tileBoxer.latLonToAxisPoint(data.getLat(), data.getLon());
			// if outside of grid then remove
			if (p.getX() == -1 || p.getY() == -1) iter.remove();
			else {
				data.setTileX(p.getX());
				data.setTileY(p.getY());
			}
		}
	}

	public void draw(Graphics2D g2d, XYPlot plot, Rectangle2D rect,
	                 ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo plotRenderingInfo) {
		g2d.setColor(Color.BLACK);
		g2d.setStroke(new BasicStroke(1f));
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Logger.debug("in VectorAnnotation.draw for XYPlot"); 
		Logger.debug("size of list = " + list.size());
		for (VectorData data : list) {
			double x = data.getTileX();
			double y = data.getTileY();
			if (domainAxis.getRange().contains(x) && rangeAxis.getRange().contains(y)) {
				double uVal = data.getUVal();
				double vVal = data.getVVal();

				double xx0 = domainAxis.valueToJava2D(x, rect, plot.getDomainAxisEdge());
				double yy0 = rangeAxis.valueToJava2D(y, rect, plot.getRangeAxisEdge());
				double xx1 = domainAxis.valueToJava2D(x + uVal, rect, plot.getDomainAxisEdge());
				double yy1 = rangeAxis.valueToJava2D(y + vVal, rect, plot.getRangeAxisEdge());
				Line2D line = new Line2D.Double(xx0, yy0, xx1, yy1);
				Logger.debug(Math.sqrt(Math.pow(line.getX2() - line.getX1(),2.0) /Math.pow(line.getY2() - line.getY1(), 2.0)));			
				g2d.draw(line);
				g2d.draw(createArrowHead(xx0, xx1, yy0, yy1));
			}
		}

	}
	
	/**
	 * draw vector data
	 * 
	 */
	
	// TODO: jizhen
	public synchronized void draw(Graphics graphics,
			int xOffset, int yOffset, int width, int height,
			int firstRow, int lastRow, int firstColumn, int lastColumn) {
		Logger.debug("in VectorAnnotation.draw (not for XYPlot)");	// the one that is used for the Fast Tile Plot & vectors
		Stroke stroke = ((Graphics2D) graphics).getStroke();
		Color defaultColor = graphics.getColor();
		((Graphics2D) graphics).setStroke(new BasicStroke(1.5f));
		graphics.setColor(Color.BLACK);

		final int xMinimum = xOffset;
		final int xMaximum = xOffset + width;
		final int yMinimum = yOffset;
		final int yMaximum = yMinimum + height;
		final int rows = 1 + lastRow - firstRow;
		final int columns = 1 + lastColumn - firstColumn;
		final float widthF = width;
		final float heightF = height;
		final float xDelta = widthF / columns; // Width in pixels of a grid// cell.
		final float yDelta = heightF / rows; // Height in pixels of a grid cell.
		
		Logger.debug("getting ready to go into for loop in list");
		int numElements = list.size();
		Logger.debug("size of list = " + numElements);
		

		for (VectorData data : list) {

			double uVal = data.getUVal();
			double vVal = data.getVVal();
			// trying to skip vectors of length 0
			if(uVal == 0.0 && vVal == 0.0)	// usually don't want to compare a float/double this way, but faster than trying abs val < 1e-6
											// and it is working in tests
				continue;
			
			//old version
//			double x0 = xMinimum + Math.round((data.getTileX() - firstColumn) * xDelta) + Math.round(xDelta / 2);
//			double y0 = yMaximum - Math.round((data.getTileY() - firstRow) * yDelta) - Math.round(yDelta / 2);
//			double x1 = xMinimum + Math.round((data.getTileX() + uVal - firstColumn) * xDelta) + Math.round(xDelta / 2);
//			double y1 = yMaximum - Math.round((data.getTileY() + vVal - firstRow) * yDelta) - Math.round(yDelta / 2);

			
//new approach			
			double x0 = xMinimum + Math.round((data.getTileX() - firstColumn) * xDelta) + Math.round(xDelta / 2);
			double y0 = yMaximum - Math.round((data.getTileY() - firstRow) * yDelta) - Math.round(yDelta / 2);
			
			double x1 = x0 + uVal * xDelta;
			double y1 = y0 + vVal * yDelta;
			Logger.debug("uVal = " + uVal + ", VVal = " + vVal + ", x0 = " + x0 + ", x1 = " + x1 + ", y0 = " + y0 + ", y1 = " + y1);

			if (x0 < xMinimum || x0 > xMaximum)
				continue;
			
			if (x1 < xMinimum || x1 > xMaximum)
				continue;

			if (y0 < yMinimum || y0 > yMaximum)
				continue;
			
			if (y1 < yMinimum || y1 > yMaximum)
				continue;
Logger.debug("ready to create new Line2D.Double");
			Line2D line = new Line2D.Double(x0, y0, x1, y1);
			
//			Logger.debug("rows=" + rows + " columns=" + columns + " xDelta=" + xDelta + " yDelta=" + yDelta + " x0=" + x0 + " y0=" + y0 + " x1=" + x1 + " y1=" + y1 + " x0-x1=" + (x0-x1) + " y0-y1=" + (y0-y1) + /*" maxUVal=" + maxUVal + " maxVVal=" + maxVVal +*/ " xOffset=" + xOffset + " yOffset=" + yOffset + " TileX=" + data.getTileX() + " TileY=" + data.getTileY() + " firstColumn=" + firstColumn + " firstRow=" + firstRow + " uVal=" + data.getUVal() + " vVal=" + data.getVVal() /*+ " uValFrac=" + uVal + " vValFrac=" + vVal*/ + " r=" + Math.sqrt(Math.pow(line.getX2() - line.getX1(),2.0) + Math.pow(line.getY2() - line.getY1(), 2.0)));			
Logger.debug("ready to draw line");			
			((Graphics2D) graphics).draw(line);
Logger.debug("ready to draw arrowhead");
			((Graphics2D) graphics).draw(createArrowHead(x0, x1, y0, y1));
		}

		Logger.debug("done with list; now setting stroke and color");
		((Graphics2D) graphics).setStroke(stroke); // reset graphics stroke
		graphics.setColor(defaultColor); // reset graphics color
	}


	private Shape createArrowHead(double xx0, double xx1, double yy0, double yy1) {
		// calculate the arrow head and draw it...
		double dxx = (xx1 - xx0);
		double dyy = (yy1 - yy0);
		double bx = xx0 + (1.0 - this.baseLength) * dxx;
		double by = yy0 + (1.0 - this.baseLength) * dyy;

		double cx = xx0 + (1.0 - this.headLength) * dxx;
		double cy = yy0 + (1.0 - this.headLength) * dyy;

		double angle = 0.0;
		if (dxx != 0.0) {
			angle = Math.PI / 2.0 - Math.atan(dyy / dxx);
		}
		double deltaX = 2.0 * Math.cos(angle);
		double deltaY = 2.0 * Math.sin(angle);

		double leftx = cx + deltaX;
		double lefty = cy - deltaY;
		double rightx = cx - deltaX;
		double righty = cy + deltaY;

		GeneralPath p = new GeneralPath();
		p.moveTo((float) xx1, (float) yy1);
		p.lineTo((float) rightx, (float) righty);
		p.lineTo((float) bx, (float) by);
		p.lineTo((float) leftx, (float) lefty);
		p.closePath();

		return p;
	}
}
