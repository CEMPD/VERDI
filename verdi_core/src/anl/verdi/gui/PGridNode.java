package anl.verdi.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;

import com.vividsolutions.jts.geom.Envelope;

// NOTE: using old piccolo library because only class using this one is DomainPanel, which uses Repast Simphony
// and requires the old piccolo library
import edu.umd.cs.piccolo.PNode;
//import org.piccolo2d.extras.util.PFixedWidthStroke;
import edu.umd.cs.piccolo.util.PPaintContext;
//import org.piccolo2d.util.PPaintContext;
//import org.piccolo2d.PNode;
import edu.umd.cs.piccolox.util.PFixedWidthStroke;

public class PGridNode extends PNode {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4604235963288960657L;
	static final Logger Logger = LogManager.getLogger(PGridNode.class.getName());

	protected Axes<CoordAxis> axes;
	
	private int netcdfConv = -1;

	protected int numXCells;

	protected int numYCells;

	protected Line2D gridLine = new Line2D.Double();

	protected Rectangle2D rect = new Rectangle2D.Double();
	protected boolean drawCompleteGrid = true;
	protected Envelope e;
	
	public PNode getPNode()		// added 2014 to provide the parent as a PNode
	{
		// return this.getPNode();
		return (PNode)this;
	}

	class IntPoint {
		int x, y;

		public IntPoint(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public IntPoint() {
		}

		public boolean equals(Object o) {
			if (!(o instanceof IntPoint)) {
				return false;
			}
			IntPoint other = (IntPoint) o;
			return other.x == x && other.y == y;
		}

		@Override
		public int hashCode() {
			int seed = 17;
			seed = seed + x * 17;
			seed = seed + y * 17;
			return seed;
		}

	}

	// private Set<IntPoint> selectedPoints = new HashSet<IntPoint>();
	protected Rectangle2D range;

	public PGridNode(Axes<CoordAxis> axes, int netcdfConv) {
		this.axes = axes;
		this.netcdfConv = netcdfConv;
		numXCells = (int) axes.getXAxis().getRange().getExtent();
		numYCells = (int) axes.getYAxis().getRange().getExtent();
		Rectangle2D bounds = new Rectangle2D.Float();
		e = axes.getBoundingBox(netcdfConv);
		bounds.setFrame(e.getMinX(), e.getMinY(), e.getWidth(), e.getHeight());
		setBounds(bounds);
		range = new Rectangle2D.Float(0, 0, -1, -1);
	}

	public Point getGridCellFor(Point2D point) {
		try {
			Point2D newPoint = axes.getBoundingBoxer().CRSPointToAxis(point.getX(), point.getY());
			int x = (int) Math.round(newPoint.getX());
			int y = (int) Math.round(newPoint.getY());
			if (x >= 0 && y >= 0 && x < numXCells && y < numYCells) {
				return (new Point(x, y));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void addGridCell(Point2D point) {
		try {
			Point2D newPoint = axes.getBoundingBoxer().CRSPointToAxis(point.getX(), point.getY());
			if (newPoint.getX() > 0 && newPoint.getY() > 0 && newPoint.getX() < numXCells
					&& newPoint.getY() < numYCells) {
				range.setRect(newPoint.getX(), newPoint.getY(), 1, 1);
				// selectedPoints.add(new IntPoint((int)
				// Math.round(newPoint.getX()), (int)
				// Math.round(newPoint.getY())));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addEnvelope(Rectangle2D e) {
		try {
			Point2D uL = axes.getBoundingBoxer().CRSPointToAxis(e.getMinX(), e.getMinY());
			Point2D lR = axes.getBoundingBoxer().CRSPointToAxis(e.getMaxX(), e.getMaxY());
			int minX = (int) uL.getX();
			int maxY = (int) uL.getY();
			int maxX = (int) lR.getX();
			int minY = (int) lR.getY();
			//Logger.debug("vals "+minX+" "+minY+" "+maxX+" "+maxY);

			range.setRect(minX, minY, maxX - minX, maxY - minY);

			/*
			if (minX < 0)
				minX = 0;
			if (minY < 0){
				minY = 0;
			}
			if (maxY > numYCells)
				maxY = numYCells;
			if (maxX > numXCells)
				maxX = numXCells;
			ReferencedEnvelope env = axes.getBoundingBox(new Range(minX,1),new Range(numYCells-maxY-1,1));
			ReferencedEnvelope env2 = axes.getBoundingBox(new Range(maxX,1),new Range(numYCells-minY-1,1));
			
//			Logger.debug("env "+env.getMinX()+" "+ env.getMinY()+" "+ env.getMaxX()+" "+ env.getMaxY());
//			Logger.debug("env2 "+env2.getMinX()+" "+ env2.getMinY()+" "+ env2.getMaxX()+" "+ env2.getMaxY());
//			Logger.debug("e "+e.getMinX()+" "+ e.getMinY()+" "+ e.getMaxX()+" "+ e.getMaxY());
			// see if outside area totally
			if(e.getMinY()>env.getMaxY()&&e.getMaxY()>env.getMaxY()){
				range.setRect(0,0,-1,-1);
				
			}else{
			// if the point is below it, add one to the cell
			if(e.getMinX()>env.getMinX()){
				minX=minX+1;
			}
			if(maxY<numYCells-1){
			if(e.getMinY()<env2.getMinY()){
				maxY=maxY-1;
			}}
			if(e.getMaxX()<env2.getMaxX()){
				maxX=maxX-1;
			}
			if(minY>0){
			if(e.getMaxY()>env.getMaxY()){
				minY=minY+1;
			}}
			//Logger.debug("vals "+minX+" "+minY+" "+maxX+" "+maxY);
			range.setRect(minX, minY, maxX - minX, maxY - minY);
			}
			// domainRange.setRect(minX, maxY, Math.abs(maxX - minX),
			// Math.abs(maxY - minY));
//			Logger.debug(domainRange);
			*/
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		for (GridNodeListener listener : listeners) {
			listener.envelopeAdded(e);
		}
		invalidatePaint();
	}

	@Override
	protected void paint(PPaintContext arg0) {
		super.paint(arg0);
		Rectangle2D bounds = new Rectangle2D.Float();
		Envelope e = axes.getBoundingBox(netcdfConv);
		bounds.setFrame(e.getMinX(), e.getMinY(), e.getWidth(), e.getHeight());
		Graphics2D g2d = arg0.getGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		g2d.setStroke(new PFixedWidthStroke(1));
		g2d.setPaint(Color.BLACK);
		gridLine.setLine(e.getMinX(), e.getMaxY(), e.getMaxX(), e.getMaxY());
		g2d.draw(gridLine);
		gridLine.setLine(e.getMaxX(), e.getMaxY(), e.getMaxX(), e.getMinY());
		g2d.draw(gridLine);
		gridLine.setLine(e.getMaxX(), e.getMinY(), e.getMinX(), e.getMinY());
		g2d.draw(gridLine);
		gridLine.setLine(e.getMinX(), e.getMinY(), e.getMinX(), e.getMaxY());
		g2d.draw(gridLine);
		double cellWidth = bounds.getWidth() / numXCells;
		double cellHeight = bounds.getHeight() / numYCells;
		if(isDrawCompleteGrid()){
			
			double xVal = bounds.getMinX() + cellWidth;
			for (int i = 0; i < numXCells - 1; i++) {
				gridLine.setLine(xVal, bounds.getMaxY(), xVal, bounds.getMinY());
				g2d.draw(gridLine);
				xVal = xVal + cellWidth;
			}
			double yVal = bounds.getMinY() + cellHeight;
			for (int i = 0; i < numYCells - 1; i++) {
				gridLine.setLine(bounds.getMinX(), yVal, bounds.getMaxX(), yVal);
				g2d.draw(gridLine);
				yVal = yVal + cellHeight;
			}
		}
		Color c = new Color(.04f, .88f, 1, .5f);
		g2d.setPaint(c);
		if (range.getWidth() >= 0 && range.getHeight() >= 0) {
			double startX = (range.getX() * cellWidth) + e.getMinX();
			double startY = e.getMaxY() - (range.getY() * cellHeight);
			rect.setFrameFromDiagonal(startX, startY, startX + (range.getWidth() + 1) * cellWidth, startY
					- (range.getHeight() + 1) * cellHeight);
			g2d.fill(rect);
		}
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}

	private List<GridNodeListener> listeners = new ArrayList<GridNodeListener>();

	public void addGridNodeListener(GridNodeListener listener) {
		listeners.add(listener);
	}

	public Rectangle2D getRange() {
		return range;
	}

	public void setRange(Rectangle2D range) {
		this.range = range;
	}
	public Rectangle2D getDomainFromRange() {
		return new Rectangle2D.Double(range.getMinX(), numYCells-range.getMaxY()-1, 
				range.getWidth(), range.getHeight());
	}

	public void setRangeFromDomain(Rectangle2D domain) {
		if(domain.getMinX()<0){
			this.range = new Rectangle2D.Double(0,0,-1,-1);
		}
		else{
			this.range = new Rectangle2D.Double(domain.getMinX(), numYCells-domain.getMaxY()-1, 
				domain.getWidth(), domain.getHeight());
		}
	}
	public boolean isDrawCompleteGrid() {
		return drawCompleteGrid;
	}

	public void setDrawCompleteGrid(boolean drawCompleteGrid) {
		this.drawCompleteGrid = drawCompleteGrid;
	}
}
