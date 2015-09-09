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



//	// Draw the cached line segments:
//
//	private void drawCachedSegments( final Graphics graphics ) {
//
//		for ( int segment = 0; segment < cachedSegmentCount; ++segment ) {
//			final int x1 = cachedVertices[ segment ][ 0 ];
//			final int y1 = cachedVertices[ segment ][ 1 ];
//			final int x2 = cachedVertices[ segment ][ 2 ];
//			final int y2 = cachedVertices[ segment ][ 3 ];
//			graphics.drawLine( x1, y1, x2, y2 );
//		}
//	}



//	// Clip a line segment to a rectangle using Liang-Barsky Algorithm.
//
//	private static boolean clipLine( double wxl, double wyl,
//			double wxu, double wyu,
//			double[] t,
//			double[] vertex1, double[] vertex2 ) {
//		double x1 = vertex1[ X ];
//		double y1 = vertex1[ Y ];
//		double x2 = vertex2[ X ];
//		double y2 = vertex2[ Y ];
//
//		boolean result = false;
//		final double dx = x2 - x1;
//		t[ 0 ] = 0.0; // t holds new start point.
//		t[ 1 ] = 1.0;
//
//		// Check boundaries: left, right, bottom, top:
//
//		if ( clipCoordinate( -dx, x1 - wxl, t ) ) { // left.
//
//			if ( clipCoordinate( dx, wxu - x1, t ) ) { // right.
//				final double dy = y2 - y1;
//
//				if ( clipCoordinate( -dy, y1 - wyl, t ) ) { // bottom.
//
//					if ( clipCoordinate( dy, wyu - y1, t ) ) { // top.
//						final double t1 = t[ 0 ];
//						final double t2 = t[ 1 ];
//
//						/*
//						 * At least some of the line is within the window so
//						 * calculate the new end and start points (in that order).
//						 */
//
//						if ( t2 < 1.0 ) { // Calculate new end point first.
//							x2 = x1 + t2 * dx;
//							y2 = y1 + t2 * dy;
//						}
//
//						if ( t1 > 0.0 ) { // Calculate new start point.
//							x1 += t1 * dx;
//							y1 += t1 * dy;
//						}
//
//						result = true; // Successfully clipped.
//						vertex1[ X ] = x1;
//						vertex1[ Y ] = y1;
//						vertex2[ X ] = x2;
//						vertex2[ Y ] = y2;
//					}
//				}
//			}
//		}
//
//		return result;
//	}


	// Check and clip line segment:

//	private static boolean clipCoordinate( double p, double q, double[] t ) {
//		boolean result = true; // Clipped.
//		double t1 = t[ 0 ];
//		double t2 = t[ 1 ];
//
//		if ( p < 0.0 ) { // Line from outside to inside of that boundary.
//			final double r = q / p; // Intersection coordinate.
//
//			if ( r > t2 ) {
//				result = false; // Intersection past segment end point.
//			} else if ( r > t1 ) {
//				t1 = r; // Intersection is past start point.
//			}
//		} else if ( p > 0.0 ) { // Line from inside to outside of that boundary.
//			final double r = q / p;
//
//			if ( r < t1 ) {
//				result = false; // Intersection is before start point.
//			} else if ( r < t2 ) {
//				t2 = r; // Intersection is before end point.
//			}
//		} else if ( q < 0.0 ) { // p == 0.0.
//			result = false; // Line is parallel to that boundary.
//		}
//
//		if ( result ) {
//			t[ 0 ] = t1;
//			t[ 1 ] = t2;
//		}
//
//		return result;
//	}


	// Copy from byte array to int/float arrays.

//	private void copyBytes( final byte[] bytes ) {
//		final int count1 = polylineCount * 4;
//		final int count2 = count1 + count1;
//		final int count3 = bytes.length;
//		int byteIndex = 0;
//
//		for ( int valueIndex = 0; byteIndex < count1;
//		++valueIndex, byteIndex += 4 ) {
//			final int byte4 = bytes[ byteIndex     ];
//			final int byte3 = bytes[ byteIndex + 1 ];
//			final int byte2 = bytes[ byteIndex + 2 ];
//			final int byte1 = bytes[ byteIndex + 3 ];
//			final int value =
//				( 0xff000000 & ( byte4 << 24 ) ) |
//				( 0x00ff0000 & ( byte3 << 16 ) ) |
//				( 0x0000ff00 & ( byte2 <<  8 ) ) |
//				( 0x000000ff & byte1 );
//			counts[ valueIndex ] = value;
//		}
//
//		for ( int valueIndex = 0; byteIndex < count2;
//		++valueIndex, byteIndex += 4 ) {
//			final int byte4 = bytes[ byteIndex     ];
//			final int byte3 = bytes[ byteIndex + 1 ];
//			final int byte2 = bytes[ byteIndex + 2 ];
//			final int byte1 = bytes[ byteIndex + 3 ];
//			final int value =
//				( 0xff000000 & ( byte4 << 24 ) ) |
//				( 0x00ff0000 & ( byte3 << 16 ) ) |
//				( 0x0000ff00 & ( byte2 <<  8 ) ) |
//				( 0x000000ff & byte1 );
//			offsets[ valueIndex ] = value;
//		}
//
//		for ( int valueIndex = 0; byteIndex < count3;
//		++valueIndex, byteIndex += 8 ) {
//			int byte4 = bytes[ byteIndex     ];
//			int byte3 = bytes[ byteIndex + 1 ];
//			int byte2 = bytes[ byteIndex + 2 ];
//			int byte1 = bytes[ byteIndex + 3 ];
//			int value =
//				( 0xff000000 & ( byte4 << 24 ) ) |
//				( 0x00ff0000 & ( byte3 << 16 ) ) |
//				( 0x0000ff00 & ( byte2 <<  8 ) ) |
//				( 0x000000ff & byte1 );
//			float fvalue = Float.intBitsToFloat( value );
//			vertices[ valueIndex ][ 0 ] = fvalue;
//			byte4 = bytes[ byteIndex + 4 ];
//			byte3 = bytes[ byteIndex + 5 ];
//			byte2 = bytes[ byteIndex + 6 ];
//			byte1 = bytes[ byteIndex + 7 ];
//			value =
//				( 0xff000000 & ( byte4 << 24 ) ) |
//				( 0x00ff0000 & ( byte3 << 16 ) ) |
//				( 0x0000ff00 & ( byte2 <<  8 ) ) |
//				( 0x000000ff & byte1 );
//			fvalue = Float.intBitsToFloat( value );
//			vertices[ valueIndex ][ 1 ] = fvalue;
//		}
//	}


//	// Read the ASCII header of a local XDR file:
//
//	private static RandomAccessFile readHeaderFromFile( final String fileName,
//			String[] dimensions )
//	throws FileNotFoundException, IOException {
//		RandomAccessFile result = null;
//		dimensions[ 0 ] = null;
//		dimensions[ 1 ] = null;
//
//		try {
//			result = new RandomAccessFile( fileName, "r" );
//			result.readLine(); // Skip content-type line.
//			String line = result.readLine(); // Skip comment line.
//			line = result.readLine(); // Dimensions.
//			String[] dimensions2 = line.split( " ", 2 );
//			dimensions[ 0 ] = dimensions2[ 0 ];
//			dimensions[ 1 ] = dimensions2[ 1 ];
//			dimensions2[ 0 ] = null;
//			dimensions2[ 1 ] = null;
//			result.readLine(); // Skip comment line.
//		} catch ( Exception unused ) {
//			dimensions[ 0 ] = null;
//			dimensions[ 1 ] = null;
//
//			if ( result != null ) {
//				try { result.close(); } catch ( Exception unused2 ) { }
//				result = null;
//			}
//		}
//
//		return result;
//	}


//	// Read the ASCII header of a remote/URL XDR file:
//
//	private static InputStream readHeaderFromURL( final String fileName,
//			String[] dimensions )
//	throws IOException {
//		InputStream result = null;
//		dimensions[ 0 ] = null;
//		dimensions[ 1 ] = null;
//
//		try {
//			final URL url = new URL( fileName );
//			result = url.openStream();
//			skipLine( result ); // Skip content-type line.
//			skipLine( result ); // Skip comment line.
//			final String line = readLine( result ); // Dimensions.
//			String[] dimensions2 = line.split( " ", 2 );
//			dimensions[ 0 ] = dimensions2[ 0 ];
//			dimensions[ 1 ] = dimensions2[ 1 ];
//			dimensions2[ 0 ] = null;
//			dimensions2[ 1 ] = null;
//			skipLine( result ); // Skip comment line.
//		} catch ( Exception unused ) {
//			dimensions[ 0 ] = null;
//			dimensions[ 1 ] = null;
//
//			if ( result != null ) {
//				try { result.close(); } catch ( Exception unused2 ) { }
//				result = null;
//			}
//		}
//
//		return result;
//	}
//

//	// Read and skip a line from a stream:
//
//	private static void skipLine( final InputStream stream ) throws IOException {
//		boolean done = false;
//
//		do {
//			final int c = stream.read();
//			done = c == '\n' || c == -1;
//		} while ( ! done );
//	}
//

//	// Read and return a line from a stream:
//
//	private static String readLine(final InputStream stream) throws IOException {
//		String result = null;
//		final StringBuffer buffer = new StringBuffer( 256 );
//		boolean done = false;
//
//		do {
//			final int c = stream.read();
//			done = c == '\n' || c == -1;
//
//			if ( ! done ) {
//				buffer.append( (char) c );
//			}
//
//		} while ( ! done );
//
//		result = buffer.toString();
//		return result;
//	}
//

//	// Read sized array of bytes from a stream:
//
//	private static void readBytes( final InputStream stream, final byte[] bytes )
//	throws IOException {
//		final int totalBytesToRead = bytes.length;
//		int offset = 0;
//		boolean done = false;
//
//		do {
//			final int bytesToReadNow = totalBytesToRead - offset;
//			final int bytesReadNow = stream.read( bytes, offset, bytesToReadNow );
//			offset += bytesReadNow;
//			done = bytesReadNow == -1 || offset >= totalBytesToRead;
//		} while ( ! done );
//	}
//

//	// Read sized array of bytes from a file:
//
//	private static void readBytes( final RandomAccessFile file,
//			final byte[] bytes ) throws IOException {
//		final int totalBytesToRead = bytes.length;
//		int offset = 0;
//		boolean done = false;
//
//		do {
//			final int bytesToReadNow = totalBytesToRead - offset;
//			final int bytesReadNow = file.read( bytes, offset, bytesToReadNow );
//			offset += bytesReadNow;
//			done = bytesReadNow == -1 || offset >= totalBytesToRead;
//		} while ( ! done );
//	}
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

