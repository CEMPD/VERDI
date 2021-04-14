package anl.verdi.plot.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.GregorianCalendar;		// added 2014
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.data.FeatureSource;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.Range;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import anl.verdi.area.AreaTilePlot;
import anl.verdi.area.MapPolygon;
import anl.verdi.area.target.Target;
import anl.verdi.data.Axes;
import anl.verdi.data.BoundingBoxer;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.Dataset;
import anl.verdi.data.ObsData;
import anl.verdi.data.ObsEvaluator;
import anl.verdi.gis.OverlayObject;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.util.Graphics2DShapesTool;
import ucar.unidata.geoloc.Projection;

import com.vividsolutions.jts.geom.Coordinate;

public class ObsAnnotation extends AbstractXYAnnotation {

	private static final long serialVersionUID = -3758349487734798674L;
	static final Logger Logger = LogManager.getLogger(ObsAnnotation.class.getName());

	public enum Symbol {
		CIRCLE, DIAMOND, SQUARE, STAR, SUN, TRIANGLE
	};

	private ObsEvaluator eval;
	private Axes<DataFrameAxis> axes;
//	private java.util.List<ObsData> list;
	private List<ObsData> list;
	private int strokeSize = 1;
	private int shapeSize = 8;
	private ColorMap map;
	private String idString;
	private Symbol symbol;
	private static final int X = 0;
	private static final int Y = 1;
	private static final int MINIMUM = 0;
	private static final int MAXIMUM = 1;
	
	Date baseDate = null;
	int baseTimestep = 0;
	OverlayObject overlay = null;

	public ObsAnnotation(ObsEvaluator eval, Axes<DataFrameAxis> axes,
			int timeStep, int layer) {
		Logger.debug("in constructor for ObsAnnotation");
		this.eval = eval;
		this.axes = axes;
		update(timeStep);
	}

	public ObsAnnotation(ObsEvaluator eval, Axes<DataFrameAxis> axes,
			Date date, int layer) {
		this.eval = eval;
		this.axes = axes;
		baseDate = date;
		update(date);
	}

	public ObsAnnotation(ObsEvaluator eval, Axes<DataFrameAxis> axes,
			GregorianCalendar aCalendar, int layer, OverlayObject overlay) {
		Logger.debug("in ObsAnnotation constructor using GregorianCalendar");
		this.eval = eval;
		this.axes = axes;
		this.overlay = overlay;
		baseDate = aCalendar.getTime();
		update(aCalendar.getTime());
	}
	
	public void updateDrawingParams(ColorMap cmap) {
		if (overlay == null)
			return;
		setDrawingParams(overlay.getSymbol(), overlay.getStrokeSize(), overlay.getShapeSize(), cmap);
	}

	public void setDrawingParams(int stroke, int size, ColorMap cMap) {
		strokeSize = stroke;
		shapeSize = size;
		map = cMap;
		symbol = Symbol.CIRCLE;
	}

	public void setDrawingParams(Symbol symb, int stroke, int size,
			ColorMap cMap) {
		strokeSize = stroke;
		shapeSize = size;
		map = cMap;
		symbol = symb;
	}

	private Color getColor(double val) throws Exception {
		if (val <= map.getMin())
			return map.getColor(0);
		Color color = map.getColor(0);
		for (int i = 1, n = map.getColorCount(); i < n; i++) {
			double start = map.getIntervalStart(i);
			if (val < start)
				return color;
			color = map.getColor(i);
		}

		return map.getColor(map.getColorCount() - 1);
	}

	public synchronized void update(int timeStep) {
		//System.out.println("ObsAnnotation timestep " + timeStep);
		list = eval.evaluate(baseDate, timeStep);
		//list = eval.evaluate(null, timeStep);
		updateList();
	}

	public synchronized void update(Date date) {
		list = eval.evaluate(date);
		updateList();
	}

	private void updateList() {
		/*BoundingBoxer boxer = axes.getBoundingBoxer();
		//int values = 0;
		for (Iterator<ObsData> iter = list.iterator(); iter.hasNext();) {
			ObsData data = iter.next();
			if (data.getValue() > 0.01)
				++values;
			Point2D p = boxer.latLonToAxisPoint(data.getLat(), data.getLon());
			if (p.getX() < 0 || p.getY() < 0)
				iter.remove();
			else {
				data.setX(p.getX());
				data.setY(p.getY());
			}
		}*/
		//System.out.println("ObsAnnotation updateList values: " + values);
	}

	@Override
	public void draw(Graphics2D g2d, XYPlot plot, Rectangle2D rect,
			ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex,
			PlotRenderingInfo info) {
		float halfSize = shapeSize / 2f;

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Stroke stroke = g2d.getStroke();
		g2d.setStroke(new BasicStroke(strokeSize));
		Range xRange = domainAxis.getRange();
		Range yRange = rangeAxis.getRange();

		for (ObsData data : list) {
			double x = data.getX();
			double y = data.getY();
			if (xRange.contains(x) && yRange.contains(y)) {
				// double xx0 = domainAxis.valueToJava2D(x + xOffset, rect,
				// plot.getDomainAxisEdge());
				// double yy0 = rangeAxis.valueToJava2D(y + yOffset, rect,
				// plot.getRangeAxisEdge());
				double xx1 = domainAxis.valueToJava2D(
						x /* + blockWidth + xOffset */, rect, plot
								.getDomainAxisEdge());
				double yy1 = rangeAxis.valueToJava2D(
						y /* + blockHeight+ yOffset */, rect, plot
								.getRangeAxisEdge());

				int x1 = (int) (xx1 - halfSize);
				int y1 = (int) (yy1 - halfSize);
				Color color;
				try {
					color = getColor(data.getValue());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}
				drawSymbol(g2d, color, data, x1, y1);
			}
		}

		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_DEFAULT);
		g2d.setStroke(stroke);
	}

	/**
	 * drawObsData - draw observational data
	 * 
	 * @param graphics
	 * @param xminimum
	 * @param xmaximum
	 * @param yminimum
	 * @param ymaximum
	 * @param legendLevels
	 * @param legendColors
	 */
	public synchronized void oldDraw(Graphics graphics, int xOffset, int yOffset, int width,
			int height, double[] legendLevels, Color[] legendColors,
			final CoordinateReferenceSystem gridCRS, final double[][] domain,
			final double[][] gridBounds) {
		Stroke stroke = ((Graphics2D) graphics).getStroke();
		Color defaultColor = graphics.getColor();
		((Graphics2D) graphics).setStroke(new BasicStroke(strokeSize));

		final double xMinimum = gridBounds[X][MINIMUM];
		final double xMaximum = gridBounds[X][MAXIMUM];
		final double yMinimum = gridBounds[Y][MINIMUM];
		final double yMaximum = gridBounds[Y][MAXIMUM];
		final double lonMaximum = (gridCRS == null) ? xMaximum : domain[X][MAXIMUM];
		final double longitudeShift = lonMaximum > 180.0 ? 360.0 : 0.0;
		final double xRange = xMaximum - xMinimum;
		final double yRange = yMaximum - yMinimum;
		final double xScale = width / xRange;
		final double yScale = height / yRange;

		int values = 0;
		
		double maxLat, maxLon;
		maxLat = 0;
		maxLon = -700;
		for (ObsData data : list) {
			double[] point = new double[2];
			double lon = data.getLon();
			lon = lon >= 0.0 ? lon : lon + longitudeShift;
			double lat = data.getLat();
			
			if (gridCRS == null) // no projection known so point becomes lon/lat
			{
				point[0] = lon; 
				point[1] = lat;
			}
			else
			{// have a projection so project lon/lat to the point
				point = lonlat2Point(lon, lat, gridCRS);
			}

			final int x = (int) Math.round((point[0] - xMinimum) * xScale + xOffset);
			final int y = (int) Math.round(height + yOffset - (point[1] - yMinimum) * yScale);

			if (x <= xOffset || x >= xOffset + width)
				continue;

			if (y <= yOffset || y >= yOffset + height)
				continue;

			if (data.getValue() > 0.01)
				++values;
			final int colorIndex = indexOfObsValue((float) data.getValue(), legendLevels);
			
			if (colorIndex == -1)
				continue;
			
			final Color cellColor = legendColors[colorIndex];
			drawSymbol((Graphics2D) graphics, cellColor, data, x, y);
		}
		//System.out.println("OVerlay lat " + maxLat + " lon " + maxLon);

		//System.out.println("ObsAnnotation draw values: " + values);

		((Graphics2D) graphics).setStroke(stroke); // reset graphics stroke
		graphics.setColor(defaultColor); // reset graphics color
	}
	
	public synchronized void draw(Graphics graphics, int xOffset, int yOffset, int width,
			int height, double[] legendLevels, Color[] legendColors,
			final CoordinateReferenceSystem gridCRS, final double[][] domain,
			final double[][] gridBounds) {
		draw(graphics, xOffset, yOffset, width, height, legendLevels, legendColors, gridCRS, domain, gridBounds, null);		
	}
	
	public synchronized void draw(Graphics graphics, int xOffset, int yOffset, int width,
			int height, double[] legendLevels, Color[] legendColors,
			final CoordinateReferenceSystem gridCRS, final double[][] domain,
			final double[][] gridBounds, Projection proj) {
	
	/*public void draw( TilePlot plot,final double[][] domain, final double[][] gridBounds,
			final CoordinateReferenceSystem gridCRS, Projection projection, double[] legendLevels,Color[] legendColors, 
			final Graphics graphics,Object data, MeshDataReader reader, String units,int firstColumn,int firstRow,
			int xOffset, int yOffset, int width, int height,int currentView, boolean showSelectedOnly ) {*/
		
		MapContent vMap = new MapContent();
		//Shape oldclip = graphics.getClip();
		//graphics.setClip(new Rectangle((int)xOffset, (int)yOffset, (int)width, (int)height));
				

		Style shapeStyle = null;


		shapeStyle = new VerdiStyle().getObsStyle(strokeSize, shapeSize, map, symbol);

			
			//graphics.setColor(Color.BLACK); //TODO - vColor from VerdiBoundaries
						
		    FeatureSource source = VerdiShapefileUtil.projectObsData(proj, list, MapPolygon.PLACEHOLDER_CRS);
		    		//VerdiShapefileUtil.projectShapefile(style.getShapePath(), (SimpleFeatureSource)source, projection, PLACEHOLDER_CRS, true);
		    
		    System.err.println("OBSAnnootation: " + source.getClass());
			System.out.println(source.getSchema().getCoordinateReferenceSystem());
			try {
				System.out.println(source.getFeatures().features().next());
			} catch (Exception e) {
				e.printStackTrace();
			}

		    //source = Mapper.staticSource;
			Layer aLayer = new FeatureLayer(source, shapeStyle);
			vMap.addLayer(aLayer);
			//vMap.getViewport().setCoordinateReferenceSystem(gridCRS);
			vMap.getViewport().setCoordinateReferenceSystem(MapPolygon.PLACEHOLDER_CRS);
			//TODO - this fails with NPE, workaround using placeholder CS
			//vMap.getViewport().setCoordinateReferenceSystem(source.getSchema().getCoordinateReferenceSystem());
			
			//ReferencedEnvelope displayBounds = new ReferencedEnvelope(gridBounds[0][0], gridBounds[0][1], gridBounds[1][0], gridBounds[1][1], gridCRS); // Use this when not projecting
			ReferencedEnvelope displayBounds = new ReferencedEnvelope(gridBounds[0][0], gridBounds[0][1], gridBounds[1][0], gridBounds[1][1], MapPolygon.PLACEHOLDER_CRS);
			vMap.getViewport().setBounds(displayBounds);

			//vMap = Mapper.staticMap.getMap();
			//vMap.getViewport().setBounds(displayBounds);

			GTRenderer renderer = new StreamingRenderer();
			renderer.setMapContent(vMap);
			Rectangle outputArea = new Rectangle(xOffset, yOffset, width, height);
			
			renderer.paint((Graphics2D)graphics, outputArea, vMap.getViewport().getBounds());
			
			//TODO - Once we figure out how to draw directly onto graphics without making the existing image fade out, uncomment
			//this line and remove the d2d.drawImage() code.
			
			//renderer.paint((Graphics2D)graphics, outputArea, vMap.getViewport().getBounds());
/*			Graphics2D g2d = (Graphics2D)graphics;
			g2d.setComposite(((AlphaComposite)g2d.getComposite()).derive(AlphaComposite.SRC_OVER));
			java.awt.image.BufferedImage shapefile =  new java.awt.image.BufferedImage(width + xOffset, height + yOffset,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
			Graphics2D gShape = (Graphics2D)shapefile.getGraphics();

			renderer.paint(gShape, outputArea, vMap.getViewport().getBounds());
			g2d.drawImage(shapefile, 0,  0, null);
			try {
				ImageIO.write(shapefile,  "PNG",  new java.io.File("c:\\dump\\tmp\\obsdata" + System.currentTimeMillis() + ".png"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

*/		
		
		//graphics.setClip(oldclip);
		vMap.dispose();
		
	}


	private void drawSymbol(Graphics2D g2d, Color color, ObsData data, int X,
			int Y) {
		if (symbol.equals(Symbol.CIRCLE)) {
			g2d.setColor(color);
			g2d.fillOval(X, Y, shapeSize, shapeSize);
			g2d.setColor(Color.BLACK);
			g2d.drawOval(X, Y, shapeSize, shapeSize);
			return;
		}

		Graphics2DShapesTool tool = new Graphics2DShapesTool();

		if (symbol.equals(Symbol.DIAMOND))
			tool.drawDiamond(X, Y, shapeSize, g2d, color);
		else if (symbol.equals(Symbol.SQUARE))
			tool.drawSquare(X, Y, shapeSize, g2d, color);
		else if (symbol.equals(Symbol.STAR))
			tool.draw4PointStar(X, Y, shapeSize, g2d, color);
		else if (symbol.equals(Symbol.SUN))
			tool.draw8PointStar(X, Y, shapeSize, g2d, color);
		else
			tool.drawTriangle(X, Y, shapeSize, g2d, color);
	}

	/**
	 * indexOfObsValue - Clamped index of value within range.
	 * 
	 * @pre ! Numerics.isNan( value )
	 * @pre values != null
	 * @pre values.length >= 2
	 * @post return >= 0
	 * @post return < values.length - 1
	 * @post value >= values[ return ]
	 * @post value <= values[ return ]
	 */

	private static int indexOfObsValue(float value, final double[] values) {
		if (Float.isNaN(value))
			return -1;
		
		if (value <= DataUtilities.BADVAL3 || value <= DataUtilities.AMISS3 || value >= DataUtilities.NC_FILL_FLOAT) 	// 2014 changed AMISS3 comparison from == to <=
			return -1;

		final int count = values.length;

		if (values[0] == values[values.length - 1])
			return 0;

		for (int index = 1; index < count; index++) {
			if (values[index] > value)
				return index - 1;
		}

		return count - 2;
	}

	public void updateMap(ColorMap map) {
		setDrawingParams(strokeSize, shapeSize, map);
	}

	public int getShapeSize() {
		return shapeSize;
	}

	public int getStrokeSize() {
		return strokeSize;
	}
	
	public Symbol getSymbol() {
		return symbol;
	}

	public List<ObsData> getData() {
		return list;
	}

	public void setID(String id) {
		idString = id;
	}

	public String getID() {
		return idString;
	}
	
	public String getVarString() {
		Dataset ds = eval.getVariable().getDataset();
		String alias = (ds == null) ? "" : ds.getAlias();
		
		return eval.getVariable().getName() + alias;
	}

	public boolean equals(ObsAnnotation other) {
		if (idString == null || idString.trim().isEmpty())
			return false;

		if (other.getID() == null || other.getID().trim().isEmpty())
			return false;

		return idString.equalsIgnoreCase(other.getID());
	}
	
	private double[] lonlat2Point(double lon, double lat, CoordinateReferenceSystem gridCRS)
	{	// convert lon/lat to point
		double[] aPoint = {0.0, 0.0};
		try{
		CRSAuthorityFactory crsFactory = CRS.getAuthorityFactory(true);
		CoordinateReferenceSystem crs = crsFactory.createCoordinateReferenceSystem("EPSG:4326");

		boolean lenient = true;
		MathTransform transform = CRS.findMathTransform(crs, gridCRS, lenient);
		Coordinate srcCoordinate = new Coordinate(lon, lat);
		Coordinate targetCoordinate = JTS.transform(srcCoordinate, null, transform);
		aPoint[0] = targetCoordinate.x;
		aPoint[1] = targetCoordinate.y;
		return aPoint;
		} catch(Exception ex) {
			Logger.error("Exception converting ObsAnnotation (lon,lat) to projected coordinate:"
					+ ex.toString());
			Logger.error(ex.getStackTrace());
		}
		return null;
	}

}
