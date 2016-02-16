/** MapPolygon.java - Read and draw projected grid-clipped map polygons.
 * 2008-09-01 plessel.todd@epa.gov
 * javac Map*.java
 */

package anl.verdi.area;

import gov.epa.emvl.Numerics;
import gov.epa.emvl.Projector;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;

import anl.gui.color.MoreColor;
import anl.verdi.area.target.Target;

//import visad.Unit;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

// Sequence of unprojected cartographic border polylines.
// TODO PROBABLY GET RID OF THIS .JAVA FILE
// OR REPLACE ALL OF ITS FUNCTIONS WITH GeoTools geospatial function calls

public class MapPolygon {
//	private java.awt.Color xorColor = new java.awt.Color(255,127,0);
	// Attributes:

	private static final int X = 0;
	private static final int Y = 1;
	private static final int MINIMUM = 0;
	private static final int MAXIMUM = 1;
//	private int polylineCount = 0;
//	private int vertexCount = 0;
//	private int[] counts = null;  // counts[polylineCount] vertices per polyline.
//	private int[] offsets = null; // offsets[polylineCount] vertex offset/poly.
//	private float[][] vertices = null; // vertices[vertexCount][2]. [][lon,lat].

	private Projector cachedProjector = null; // Optional cartographic projector.
	private double[][] cachedDomain  = { { -180.0, 180.0 }, { -90.0, 90.0 } };
	private double[][] cachedGridBounds =
	{ { -Numerics.DBL_MAX, Numerics.DBL_MAX },
			{ -Numerics.DBL_MAX, Numerics.DBL_MAX } };
	private int cachedXOffset = 0; // Cached from last call to draw().
	private int cachedYOffset = 0; // Cached from last call to draw().
	private int cachedWidth   = 0; // Cached from last call to draw().
	private int cachedHeight  = 0; // Cached from last call to draw().

//	private int cachedSegmentCount = 0;    // Cached clipped segments.
//	private int[][] cachedVertices = null; // [cachedSegmentCount][4].


	public MapPolygon(){}
	// Construct by reading a remote/URL or local file, e.g., "map_na.bin".

	// Draw domain-clipped projected grid-clipped polygons to graphics:

	public void draw( AreaTilePlot plot,final double[][] domain, final double[][] gridBounds,
			final Projector projector,double[] legendLevels,Color[] legendColors, 
			final Graphics graphics,float[][] data,String units,int firstColumn,int firstRow,
			int xOffset, int yOffset, int width, int height,int currentView, boolean showSelectedOnly ) {

		if (units==null || units.trim().equals(""))
			units = "none";
		
		final int yHeightOffset = height + yOffset;

		final double xMinimum = gridBounds[ X ][ MINIMUM ];
		final double xMaximum = gridBounds[ X ][ MAXIMUM ];
		final double yMinimum = gridBounds[ Y ][ MINIMUM ];
		final double yMaximum = gridBounds[ Y ][ MAXIMUM ];

		final double xRange   = xMaximum - xMinimum;
		final double yRange   = yMaximum - yMinimum;
		final double xScale   = width  / xRange;
		final double yScale   = height / yRange;

		double[] t = { 0.0, 1.0 }; 

		Shape oldclip = graphics.getClip();
		graphics.setClip(new Rectangle((int)xOffset, (int)yOffset, (int)width, (int)height));
		final boolean useCache =
			equalsCachedParameters( domain, gridBounds, projector,
					xOffset, yOffset, width, height );
		// get the polygons from the target overlay
		ArrayList polygons=Target.getTargets();
		//if(showSelectedOnly)polygons=Target.getSelectedTargets();
		Target.setCurrentTilePlot(plot);
		Target.setCurrentGridInfo(plot.getGridInfo());

		for(Target polygon:(ArrayList<Target>)polygons){
			Geometry poly=polygon.getDataObject();
			boolean isSelected = polygon.isSelectedPolygon();
			if(poly instanceof MultiPolygon){
				for(int i=0;i<((MultiPolygon)poly).getNumGeometries();i++){
					Geometry geo=((MultiPolygon)poly).getGeometryN(i);
					// get the vertices
					Polygon geoPolygon=(Polygon)geo;
					Coordinate[] coords=geoPolygon.getCoordinates();
					// draw that geometry

					// calculate the cachedCoords and path
					if(!useCache){
						Coordinate[] cachedCoords = new Coordinate[coords.length];
						for(int j=0;j<coords.length;j++){
							projector.project( coords[j].x, coords[j].y, t );
							cachedCoords[j]=new Coordinate(( t[X] - xMinimum ) * xScale + xOffset + 0.5,
									yHeightOffset - ( ( t[Y] - yMinimum ) * yScale ) + 0.5);
						}

						// create a general path for it
						GeneralPath p =new GeneralPath();
						for(int j=0;j<coords.length;j++){
							if(j==0)p.moveTo(cachedCoords[0].x,cachedCoords[0].y);
							else p.lineTo(cachedCoords[j].x,cachedCoords[j].y);
						}

						p.closePath();
						geoPolygon.setUserData(p);
					}

					// draw the polygon using cachedCoords
					if(coords.length>0){

						// calculate the deposition and draw filled
						if(polygon.containsDeposition()&&currentView!=AreaTilePlot.GRID){

							float val=0;
							if(currentView==AreaTilePlot.AVERAGES)val=polygon.calculateAverageDeposition(data);
							if(currentView==AreaTilePlot.TOTALS){
								val=polygon.calculateTotalDeposition(data);
							}
							if(!showSelectedOnly||(showSelectedOnly&&isSelected)){	
								// pick the right color in the legend 
								final int   colorIndex = indexOfValue( val, legendLevels );
								final Color cellColor  = legendColors[ colorIndex ];

								((Graphics2D)graphics).setColor(cellColor);
								((Graphics2D)graphics).fill((GeneralPath)geoPolygon.getUserData());
							}
						}

						// draw the outline

						if(isSelected){
							graphics.setColor(MoreColor.darkBlue);
							((Graphics2D)graphics).setStroke(new BasicStroke(2f));
						}else{
							graphics.setColor(MoreColor.forestGreen);
							((Graphics2D)graphics).setStroke(new BasicStroke(1f));
						}
						((Graphics2D)graphics).draw((GeneralPath)geoPolygon.getUserData());
					}
				}
			}
		}
		graphics.setClip(oldclip);

		// Update cached parameters.

		cacheParameters( domain, gridBounds, projector,
				xOffset, yOffset, width, height );
	}
	//Draw domain-clipped projected grid-clipped polygons to graphics:

	public void calculateValues( AreaTilePlot plot,final double[][] domain, final double[][] gridBounds,
			final Projector projector,double[] legendLevels,Color[] legendColors, 
			final Graphics graphics,float[][] data,String units,int firstColumn,int firstRow,
			int xOffset, int yOffset, int width, int height,int currentView,boolean showSelectedOnly ) {

		if (units==null || units.trim().equals(""))
			units = "none";
		
		final int yHeightOffset = height + yOffset;

		final double xMinimum = gridBounds[ X ][ MINIMUM ];
		final double xMaximum = gridBounds[ X ][ MAXIMUM ];
		final double yMinimum = gridBounds[ Y ][ MINIMUM ];
		final double yMaximum = gridBounds[ Y ][ MAXIMUM ];

		final double xRange   = xMaximum - xMinimum;
		final double yRange   = yMaximum - yMinimum;
		final double xScale   = width  / xRange;
		final double yScale   = height / yRange;

		double[] t = { 0.0, 1.0 };

		final boolean useCache =
			equalsCachedParameters( domain, gridBounds, projector,
					xOffset, yOffset, width, height );
		// get the polygons from the target overlay
		ArrayList polygons=Target.getTargets();
		if(showSelectedOnly)polygons=Target.getSelectedTargets();
		Target.setCurrentTilePlot(plot);
		Target.setCurrentGridInfo(plot.getGridInfo());

		for(Target polygon:(ArrayList<Target>)polygons){
			Geometry poly=polygon.getDataObject();
			if(poly instanceof MultiPolygon){
				for(int i=0;i<((MultiPolygon)poly).getNumGeometries();i++){
					Geometry geo=((MultiPolygon)poly).getGeometryN(i);
					// get the vertices
					Polygon geoPolygon=(Polygon)geo;
					Coordinate[] coords=geoPolygon.getCoordinates();
					// draw that geometry

					// calculate the cachedCoords and path
					if(!useCache){
						Coordinate[] cachedCoords = new Coordinate[coords.length];
						for(int j=0;j<coords.length;j++){
							projector.project( coords[j].x, coords[j].y, t );
							cachedCoords[j]=new Coordinate(( t[X] - xMinimum ) * xScale + xOffset + 0.5,
									yHeightOffset - ( ( t[Y] - yMinimum ) * yScale ) + 0.5);
						}

						// create a general path for it
						GeneralPath p =new GeneralPath();
						for(int j=0;j<coords.length;j++){
							if(j==0)p.moveTo(cachedCoords[0].x,cachedCoords[0].y);
							else p.lineTo(cachedCoords[j].x,cachedCoords[j].y);
						}

						p.closePath();
						geoPolygon.setUserData(p);
					}

					// draw the polygon using cachedCoords
					if(coords.length>0){

						// calculate the deposition and draw filled
						if(polygon.containsDeposition()&&currentView!=AreaTilePlot.GRID){
							if(currentView==AreaTilePlot.AVERAGES)polygon.calculateAverageDeposition(data);
							if(currentView==AreaTilePlot.TOTALS){
								polygon.calculateTotalDeposition(data);
							}
						}
					}
				}
			}
		}

		// Update cached parameters.

		cacheParameters( domain, gridBounds, projector,
				xOffset, yOffset, width, height );
	}
	/** indexOfValue - Clamped index of value within range.
	 * @pre ! Numerics.isNan( value )
	 * @pre values != null
	 * @pre values.length >= 2
	 * @post return >= 0
	 * @post return < values.length - 1
	 * @post value >= values[ return ]
	 * @post value <= values[ return ]
	 */

	private static int indexOfValue( float value, final double[] values ) {
		final int count = values.length;
		int result = count - 2;

		for ( int index = 1; index < count; ++index ) {

			if ( values[ index ] > value ) {
				result = index - 1;
				index  = count - 1;
			}
		}

		return result;
	}

	// Are the drawing parameters unchanged?

	public boolean equalsCachedParameters( final double[][] domain,
			final double[][] gridBounds,
			final Projector projector,
			int xOffset, int yOffset,
			int width,   int height ) {

		final boolean result =
			domain[     X ][ MINIMUM ] == cachedDomain[     X ][ MINIMUM ] &&
			domain[     X ][ MAXIMUM ] == cachedDomain[     X ][ MAXIMUM ] &&
			domain[     Y ][ MINIMUM ] == cachedDomain[     Y ][ MINIMUM ] &&
			domain[     Y ][ MAXIMUM ] == cachedDomain[     Y ][ MAXIMUM ] &&
			gridBounds[ X ][ MINIMUM ] == cachedGridBounds[ X ][ MINIMUM ] &&
			gridBounds[ X ][ MAXIMUM ] == cachedGridBounds[ X ][ MAXIMUM ] &&
			gridBounds[ Y ][ MINIMUM ] == cachedGridBounds[ Y ][ MINIMUM ] &&
			gridBounds[ Y ][ MAXIMUM ] == cachedGridBounds[ Y ][ MAXIMUM ] &&
			projector                  == cachedProjector &&
			xOffset                    == cachedXOffset   &&
			yOffset                    == cachedYOffset   &&
			width                      == cachedWidth     &&
			height                     == cachedHeight;

		return result;
	}

	// Cache the drawing parameters.

	public void cacheParameters( final double[][] domain,
			final double[][] gridBounds,
			final Projector projector,
			int xOffset, int yOffset,
			int width,   int height ) {

		cachedDomain[     X ][ MINIMUM ] = domain[     X ][ MINIMUM ];
		cachedDomain[     X ][ MAXIMUM ] = domain[     X ][ MAXIMUM ];
		cachedDomain[     Y ][ MINIMUM ] = domain[     Y ][ MINIMUM ];
		cachedDomain[     Y ][ MAXIMUM ] = domain[     Y ][ MAXIMUM ];
		cachedGridBounds[ X ][ MINIMUM ] = gridBounds[ X ][ MINIMUM ];
		cachedGridBounds[ X ][ MAXIMUM ] = gridBounds[ X ][ MAXIMUM ];
		cachedGridBounds[ Y ][ MINIMUM ] = gridBounds[ Y ][ MINIMUM ];
		cachedGridBounds[ Y ][ MAXIMUM ] = gridBounds[ Y ][ MAXIMUM ];
		cachedProjector = projector;
		cachedXOffset   = xOffset;
		cachedYOffset   = yOffset;
		cachedWidth     = width;
		cachedHeight    = height;
	}

	public static Target getTargetWithin(double lat,double lon  ) {

		// get the polygons from the target overlay
		ArrayList polygons=Target.getTargets();

		// make a point for the screen location
		Point pt= new GeometryFactory().createPoint(new Coordinate(lon,lat));
		for(Target polygon:(ArrayList<Target>)polygons){
			Geometry poly=polygon.getDataObject();
//			boolean isSelected = polygon.isSelectedPolygon();
			if(poly instanceof MultiPolygon){
				for(int i=0;i<((MultiPolygon)poly).getNumGeometries();i++){
					Geometry geo=((MultiPolygon)poly).getGeometryN(i);
					// get the vertices
					Polygon geoPolygon=(Polygon)geo;
//					Coordinate[] coords=geoPolygon.getCoordinates();
					if(geo.contains(pt))return polygon;
				}
			}

		}
		return null;
	}
	public static ArrayList<Target> getTargetsWithin(Rectangle rect) {

		// get the polygons from the target overlay
		ArrayList polygons=Target.getTargets();
		ArrayList matches=new ArrayList();	
		// make a point for the screen location

//		Geometry bounds = new GeometryFactory().toGeometry(
//				new Envelope(rect.getX(),rect.getX()+rect.getWidth(),
//						rect.getY(),rect.getY()+rect.getHeight()));
		for(Target polygon:(ArrayList<Target>)polygons){
			Geometry poly=polygon.getDataObject();

			if(poly instanceof MultiPolygon){
				for(int i=0;i<((MultiPolygon)poly).getNumGeometries();i++){
					Geometry geo=((MultiPolygon)poly).getGeometryN(i);
					// get the vertices
					Polygon geoPolygon=(Polygon)geo;
					GeneralPath coords=(GeneralPath)geoPolygon.getUserData();
					Rectangle boundBox = coords.getBounds();
					if(rect.contains(boundBox))matches.add(polygon);
				}
			}

		}
		return matches;
	}
}

