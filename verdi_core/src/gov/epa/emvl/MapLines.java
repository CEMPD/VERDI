///** MapLines.java - Read and draw projected grid-clipped map lines.
//* 2008-09-01 plessel.todd@epa.gov
//* javac Map*.java
//*/
//
//package gov.epa.emvl;
//
//import java.awt.BasicStroke;
//import java.awt.Color;
//import java.awt.Graphics;
//import java.awt.Graphics2D;
//import java.awt.Stroke;
//import java.io.File;
//import java.io.FileNotFoundException;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.RandomAccessFile;
//import java.net.URL;
//
//import org.apache.logging.log4j.LogManager;		// 2014
//import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import org.geotools.styling.FeatureTypeStyle;
//import org.geotools.styling.Style;
//
//// Sequence of unprojected cartographic border polylines.
//public class MapLines {
//
//	static final Logger Logger = LogManager.getLogger(MapLines.class.getName());
//  // Attributes:
//
//  private static final int X = 0;
//  private static final int Y = 1;
//  private static final int MINIMUM = 0;
//  private static final int MAXIMUM = 1;
//  private int polylineCount = 0;
//  private int vertexCount = 0;
//  private int[] counts = null;  // counts[polylineCount] vertices per polyline.
//  private float[][] vertices = null; // vertices[vertexCount][2]. [][lon,lat].
//
//  private Projector cachedProjector = null; // Optional cartographic projector.
//  private double[][] cachedDomain  = { { -180.0, 180.0 }, { -90.0, 90.0 } };
//  private double[][] cachedGridBounds =
//    { { -Numerics.DBL_MAX, Numerics.DBL_MAX },
//      { -Numerics.DBL_MAX, Numerics.DBL_MAX } };
//  private int cachedXOffset = 0; // Cached from last call to draw().
//  private int cachedYOffset = 0; // Cached from last call to draw().
//  private int cachedWidth   = 0; // Cached from last call to draw().
//  private int cachedHeight  = 0; // Cached from last call to draw().
//
//  private int cachedSegmentCount = 0;    // Cached clipped segments.
//  private int[][] cachedVertices = null; // [cachedSegmentCount][4].
//  
//  private String mapFile = null;
//  private String title;
//  private Style style;
//
//  // Construct by reading a remote/URL or local file, e.g., "bootstrap/data/map_na".
//  public MapLines( String fileName) throws FileNotFoundException, IOException {
//	mapFile = fileName;
//	Logger.debug("in MapLines constructor, fileName = " + fileName);
//    RandomAccessFile file     = null;
//    InputStream stream        = null;
//    final String[] dimensions = new String[ 2 ];
//
//    try {
//      if ( fileName.startsWith( "http://"  ) ||
//           fileName.startsWith( "https://" ) ||
//           fileName.startsWith( "file:/"   ) ) {
//        stream = readHeaderFromURL( fileName, dimensions );
//      } else {
//        file = readHeaderFromFile( fileName, dimensions );
//      }
//
//      if ( dimensions[ 0 ] != null ) {
//        polylineCount = Integer.parseInt( dimensions[ 0 ] );
//        vertexCount   = Integer.parseInt( dimensions[ 1 ] );
//        counts   = new int[ polylineCount ];
//        vertices = new float[ vertexCount ][ 2 ];
//        final byte[] bytes =
//          new byte[ polylineCount * 4 + vertexCount * 2 * 4 ];
//
//        if ( stream != null ) {
//          readBytes( stream, bytes );
//        } else {
//          readBytes( file, bytes );
//        }
//
//        copyBytes( bytes );
//      }
//    } catch ( Exception unused ) {
//    }
//
//    if ( stream != null ) {
//      try { stream.close(); } catch ( Exception unused2 ) { }
//      stream = null;
//    } else {
//      try { file.close(); } catch ( Exception unused3 ) { }
//      file = null;
//    }
//
//    // Compute maximum size for cachedVertices[][4]:
//
//    for ( int polyline = 0; polyline < polylineCount; ++polyline ) {
//      final int thisPolylineVertexCount = counts[ polyline ];
//      final int thisPolylineSegmentCount = ( thisPolylineVertexCount - 1 ) * 2;
//      cachedSegmentCount += thisPolylineSegmentCount;
//    }
//
//    cachedVertices = new int[ cachedSegmentCount ][ 4 ];
//  }
//
//  // Draw domain-clipped projected grid-clipped polylines to graphics:
//  public void draw( final double[][] domain, final double[][] gridBounds,
//                    final Projector projector, final Graphics graphics,
//                    int xOffset, int yOffset, int width, int height ) {
//    final boolean useCache =
//      equalsCachedParameters( domain, gridBounds, projector,
//                              xOffset, yOffset, width, height );
//    Stroke defaultStroke = ((Graphics2D)graphics).getStroke();
//    setDrawStyle(graphics);
//    
//    if ( useCache ) {
//      drawCachedSegments( graphics );
//    } else { // Update cache:
//      final int yHeightOffset = height + yOffset;
//
//      final double xMinimum = gridBounds[ X ][ MINIMUM ];
//      final double xMaximum = gridBounds[ X ][ MAXIMUM ];
//      final double yMinimum = gridBounds[ Y ][ MINIMUM ];
//      final double yMaximum = gridBounds[ Y ][ MAXIMUM ];
//
//      final double lonMinimum = projector == null ? xMinimum : domain[ X ][ MINIMUM ];
//      final double lonMaximum = projector == null ? xMaximum : domain[ X ][ MAXIMUM ];
//      final double latMinimum = projector == null ? yMinimum : domain[ Y ][ MINIMUM ];
//      final double latMaximum = projector == null ? yMaximum : domain[ Y ][ MAXIMUM ];
//
//      final double xRange   = xMaximum - xMinimum;
//      final double yRange   = yMaximum - yMinimum;
//      final double xScale   = width  / xRange;
//      final double yScale   = height / yRange;
//
//      /*
//       * Handle case where grid domain is lon-lat with lon range [0, 360]:
//       * tooLong prevents wrapped-around lines that connect left and right edges
//       * of re-mapped points.
//       */
//      final double longitudeShift = lonMaximum > 180.0 ? 360.0 : 0.0;
//      final double tooLong = 10.0; // Don't draw lines longer than this number of degrees.
//
//      double[] t = { 0.0, 1.0 }; // Temp used by clipLine().
//      double[] vertex1 = { 0.0, 0.0 };
//      double[] vertex2 = { 0.0, 0.0 };
//      cachedSegmentCount = 0; // Reset segment count.
//      for ( int polyline = 0, offset = 0;
//            polyline < polylineCount;
//            offset += counts[ polyline ], ++polyline ) {
//        final int count  = counts[  polyline ];
//        double x1 = vertices[ offset ][ X ];
//        double y1 = vertices[ offset ][ Y ];
//        x1 = x1 >= 0.0 ? x1 : x1 + longitudeShift;
// 
//        for ( int vertex = 1; vertex < count; ++vertex ) {
//          final int offsetVertex = offset + vertex;
//          final double x2p = vertices[ offsetVertex ][ X ];
//          final double x2 = x2p >= 0.0 ? x2p : x2p + longitudeShift;
//          final double y2 = vertices[ offsetVertex ][ Y ];
//          vertex1[ X ] = x1;
//          vertex1[ Y ] = y1;
//          vertex2[ X ] = x2;
//          vertex2[ Y ] = y2;
//
//          if ( ( longitudeShift == 0.0 || Math.abs( x1 - x2 ) < tooLong ) &&
//        		 clipLine( lonMinimum, latMinimum, lonMaximum, latMaximum,
//                           t, vertex1, vertex2 ) ) {
//            if ( projector != null ) {
//              projector.project( vertex1[ X ], vertex1[ Y ], t );
//              vertex1[ X ] = t[ X ];
//              vertex1[ Y ] = t[ Y ];
//              projector.project( vertex2[ X ], vertex2[ Y ], t );
//              vertex2[ X ] = t[ X ];
//              vertex2[ Y ] = t[ Y ];
//            }
//            if ( projector == null ||
//                 clipLine( xMinimum, yMinimum, xMaximum, yMaximum,
//                           t, vertex1, vertex2 ) ) {
//              final double clippedX1 = vertex1[ X ];
//              final double clippedY1 = vertex1[ Y ];
//              final double clippedX2 = vertex2[ X ];
//              final double clippedY2 = vertex2[ Y ];
//              final double dScreenX1 =
//                ( clippedX1 - xMinimum ) * xScale + xOffset + 0.5; // here maybe where the error happens
//              final double dScreenX2 =
//                ( clippedX2 - xMinimum ) * xScale + xOffset + 0.5;
//              final double dScreenY1 =
//                yHeightOffset - ( ( clippedY1 - yMinimum ) * yScale ) + 0.5;
//              final double dScreenY2 =
//                yHeightOffset - ( ( clippedY2 - yMinimum ) * yScale ) + 0.5;
//
//              final int screenX1 = (int) dScreenX1;
//              final int screenX2 = (int) dScreenX2;
//              final int screenY1 = (int) dScreenY1;
//              final int screenY2 = (int) dScreenY2;
//
//              // Store cached vertices:
//
//              cachedVertices[ cachedSegmentCount ][ 0 ] = screenX1;
//              cachedVertices[ cachedSegmentCount ][ 1 ] = screenY1;
//              cachedVertices[ cachedSegmentCount ][ 2 ] = screenX2;
//              cachedVertices[ cachedSegmentCount ][ 3 ] = screenY2;
//              ++cachedSegmentCount;
//              graphics.drawLine( screenX1, screenY1, screenX2, screenY2 ); // MERCATOR
//            }
//          }
//          x1 = x2;
//          y1 = y2;
//        }
//      }
//      ((Graphics2D)graphics).setStroke(defaultStroke); // reset the stroke
//      // Update cached parameters.
//      cacheParameters( domain, gridBounds, projector,
//                       xOffset, yOffset, width, height );
//    }
//  }
// 
//  // Draw the cached line segments:
//  private void drawCachedSegments( final Graphics graphics ) {
//    for ( int segment = 0; segment < cachedSegmentCount; ++segment ) {
//      final int x1 = cachedVertices[ segment ][ 0 ];
//      final int y1 = cachedVertices[ segment ][ 1 ];
//      final int x2 = cachedVertices[ segment ][ 2 ];
//      final int y2 = cachedVertices[ segment ][ 3 ];
//      
//      graphics.drawLine( x1, y1, x2, y2 );
//    }
//  }
//
//  private void setDrawStyle(final Graphics graphics) {
//	  if (style == null)
//		  return;
//	  
//	  FeatureTypeStyle[] styles = style.getFeatureTypeStyles();
//	  
//	  String width = ((org.geotools.styling.LineSymbolizerImpl)styles[0].getRules()[0].getSymbolizers()[0]).getStroke().getWidth().toString();
//	  String opacity = ((org.geotools.styling.LineSymbolizerImpl)styles[0].getRules()[0].getSymbolizers()[0]).getStroke().getOpacity().toString();
//	  String clr = ((org.geotools.styling.LineSymbolizerImpl)styles[0].getRules()[0].getSymbolizers()[0]).getStroke().getColor().toString();
//	  
//	  int r = Integer.parseInt(clr.substring(1,3), 16);
//	  int g = Integer.parseInt(clr.substring(3,5), 16);
//	  int b = Integer.parseInt(clr.substring(5), 16);
//	  int alpha = Math.round(Float.parseFloat(opacity) * 255);
//	  
//	  Color color = new Color(r, g, b, alpha);
//	  Stroke stroke = new BasicStroke(Float.parseFloat(width));
//	  
//	  ((Graphics2D)graphics).setStroke(stroke);
//	  graphics.setColor(color);
//  }
//  // Are the drawing parameters unchanged?
//  public boolean equalsCachedParameters( final double[][] domain,
//                                         final double[][] gridBounds,
//                                         final Projector projector,
//                                         int xOffset, int yOffset,
//                                         int width,   int height ) {
//    final boolean result =
//      domain[     X ][ MINIMUM ] == cachedDomain[     X ][ MINIMUM ] &&
//      domain[     X ][ MAXIMUM ] == cachedDomain[     X ][ MAXIMUM ] &&
//      domain[     Y ][ MINIMUM ] == cachedDomain[     Y ][ MINIMUM ] &&
//      domain[     Y ][ MAXIMUM ] == cachedDomain[     Y ][ MAXIMUM ] &&
//      gridBounds[ X ][ MINIMUM ] == cachedGridBounds[ X ][ MINIMUM ] &&
//      gridBounds[ X ][ MAXIMUM ] == cachedGridBounds[ X ][ MAXIMUM ] &&
//      gridBounds[ Y ][ MINIMUM ] == cachedGridBounds[ Y ][ MINIMUM ] &&
//      gridBounds[ Y ][ MAXIMUM ] == cachedGridBounds[ Y ][ MAXIMUM ] &&
//      projector                  == cachedProjector &&
//      xOffset                    == cachedXOffset   &&
//      yOffset                    == cachedYOffset   &&
//      width                      == cachedWidth     &&
//      height                     == cachedHeight;
//    return result;
//  }
//
//  // Cache the drawing parameters.
//  public void cacheParameters( final double[][] domain,
//                               final double[][] gridBounds,
//                               final Projector projector,
//                               int xOffset, int yOffset,
//                               int width,   int height ) {
//    cachedDomain[     X ][ MINIMUM ] = domain[     X ][ MINIMUM ];
//    cachedDomain[     X ][ MAXIMUM ] = domain[     X ][ MAXIMUM ];
//    cachedDomain[     Y ][ MINIMUM ] = domain[     Y ][ MINIMUM ];
//    cachedDomain[     Y ][ MAXIMUM ] = domain[     Y ][ MAXIMUM ];
//    cachedGridBounds[ X ][ MINIMUM ] = gridBounds[ X ][ MINIMUM ];
//    cachedGridBounds[ X ][ MAXIMUM ] = gridBounds[ X ][ MAXIMUM ];
//    cachedGridBounds[ Y ][ MINIMUM ] = gridBounds[ Y ][ MINIMUM ];
//    cachedGridBounds[ Y ][ MAXIMUM ] = gridBounds[ Y ][ MAXIMUM ];
//    cachedProjector = projector;
//    cachedXOffset   = xOffset;
//    cachedYOffset   = yOffset;
//    cachedWidth     = width;
//    cachedHeight    = height;
//  }
//
//  // Clip a line segment to a rectangle using Liang-Barsky Algorithm.
//  private static boolean clipLine( double wxl, double wyl,
//                                   double wxu, double wyu,
//                                   double[] t,
//                                   double[] vertex1, double[] vertex2 ) {
//    double x1 = vertex1[ X ];
//    double y1 = vertex1[ Y ];
//    double x2 = vertex2[ X ];
//    double y2 = vertex2[ Y ];
//
//    boolean result = false;
//    final double dx = x2 - x1;
//    t[ 0 ] = 0.0; // t holds new start point.
//    t[ 1 ] = 1.0;
//    // Check boundaries: left, right, bottom, top:
//    if ( clipCoordinate( -dx, x1 - wxl, t ) ) { // left.
//
//      if ( clipCoordinate( dx, wxu - x1, t ) ) { // right.
//        final double dy = y2 - y1;
//
//        if ( clipCoordinate( -dy, y1 - wyl, t ) ) { // bottom.
//
//          if ( clipCoordinate( dy, wyu - y1, t ) ) { // top.
//            final double t1 = t[ 0 ];
//            final double t2 = t[ 1 ];
//
//            /*
//             * At least some of the line is within the window so
//             * calculate the new end and start points (in that order).
//             */
//
//            if ( t2 < 1.0 ) { // Calculate new end point first.
//              x2 = x1 + t2 * dx;
//              y2 = y1 + t2 * dy;
//            }
//
//            if ( t1 > 0.0 ) { // Calculate new start point.
//              x1 += t1 * dx;
//              y1 += t1 * dy;
//            }
//
//            result = true; // Successfully clipped.
//            vertex1[ X ] = x1;
//            vertex1[ Y ] = y1;
//            vertex2[ X ] = x2;
//            vertex2[ Y ] = y2;
//          }
//        }
//      }
//    }
//    return result;
//  }
//
//  // Check and clip line segment:
//  private static boolean clipCoordinate( double p, double q, double[] t ) {
//    boolean result = true; // Clipped.
//    double t1 = t[ 0 ];
//    double t2 = t[ 1 ];
//
//    if ( p < 0.0 ) { // Line from outside to inside of that boundary.
//      final double r = q / p; // Intersection coordinate.
//
//      if ( r > t2 ) {
//        result = false; // Intersection past segment end point.
//      } else if ( r > t1 ) {
//        t1 = r; // Intersection is past start point.
//      }
//    } else if ( p > 0.0 ) { // Line from inside to outside of that boundary.
//      final double r = q / p;
//
//      if ( r < t1 ) {
//        result = false; // Intersection is before start point.
//      } else if ( r < t2 ) {
//        t2 = r; // Intersection is before end point.
//      }
//    } else if ( q < 0.0 ) { // p == 0.0.
//      result = false; // Line is parallel to that boundary.
//    }
//
//    if ( result ) {
//      t[ 0 ] = t1;
//      t[ 1 ] = t2;
//    }
//
//    return result;
//  }
//
//  // Copy from byte array to int/float arrays.
//  private void copyBytes( final byte[] bytes ) {
//    final int count1 = polylineCount * 4;
//    final int count2 = bytes.length;
//    int byteIndex = 0;
//
//    for ( int valueIndex = 0; byteIndex < count1;
//          ++valueIndex, byteIndex += 4 ) {
//      final int byte4 = bytes[ byteIndex     ];
//      final int byte3 = bytes[ byteIndex + 1 ];
//      final int byte2 = bytes[ byteIndex + 2 ];
//      final int byte1 = bytes[ byteIndex + 3 ];
//      final int value =
//        ( 0xff000000 & ( byte4 << 24 ) ) |
//        ( 0x00ff0000 & ( byte3 << 16 ) ) |
//        ( 0x0000ff00 & ( byte2 <<  8 ) ) |
//        ( 0x000000ff & byte1 );
//      counts[ valueIndex ] = value;
//    }
//
//    for ( int valueIndex = 0; byteIndex < count2;
//          ++valueIndex, byteIndex += 8 ) {
//      int byte4 = bytes[ byteIndex     ];
//      int byte3 = bytes[ byteIndex + 1 ];
//      int byte2 = bytes[ byteIndex + 2 ];
//      int byte1 = bytes[ byteIndex + 3 ];
//      int value =
//        ( 0xff000000 & ( byte4 << 24 ) ) |
//        ( 0x00ff0000 & ( byte3 << 16 ) ) |
//        ( 0x0000ff00 & ( byte2 <<  8 ) ) |
//        ( 0x000000ff & byte1 );
//      float fvalue = Float.intBitsToFloat( value );
//      vertices[ valueIndex ][ 0 ] = fvalue;
//      byte4 = bytes[ byteIndex + 4 ];
//      byte3 = bytes[ byteIndex + 5 ];
//      byte2 = bytes[ byteIndex + 6 ];
//      byte1 = bytes[ byteIndex + 7 ];
//      value =
//        ( 0xff000000 & ( byte4 << 24 ) ) |
//        ( 0x00ff0000 & ( byte3 << 16 ) ) |
//        ( 0x0000ff00 & ( byte2 <<  8 ) ) |
//        ( 0x000000ff & byte1 );
//      fvalue = Float.intBitsToFloat( value );
//      vertices[ valueIndex ][ 1 ] = fvalue;
//    }
//  }
//
//  // Read the ASCII header of a local XDR file:
//  private static RandomAccessFile readHeaderFromFile( final String fileName,
//                                                      String[] dimensions )
//    throws FileNotFoundException, IOException {
//    RandomAccessFile result = null;
//    dimensions[ 0 ] = null;
//    dimensions[ 1 ] = null;
//
//    try {
//      result = new RandomAccessFile( fileName, "r" );
//      result.readLine(); // Skip content-type line.
//      String line = result.readLine(); // Skip comment line.
//      line = result.readLine(); // Dimensions.
//      String[] dimensions2 = line.split( " ", 2 );
//      dimensions[ 0 ] = dimensions2[ 0 ];
//      dimensions[ 1 ] = dimensions2[ 1 ];
//      dimensions2[ 0 ] = null;
//      dimensions2[ 1 ] = null;
//      result.readLine(); // Skip comment line.
//    } catch ( Exception unused ) {
//      dimensions[ 0 ] = null;
//      dimensions[ 1 ] = null;
//
//      if ( result != null ) {
//        try { result.close(); } catch ( Exception unused2 ) { }
//        result = null;
//      }
//    }
//    return result;
//  }
//
//  // Read the ASCII header of a remote/URL XDR file:
//  private static InputStream readHeaderFromURL( final String fileName,
//                                                String[] dimensions )
//    throws IOException {
//    InputStream result = null;
//    dimensions[ 0 ] = null;
//    dimensions[ 1 ] = null;
//
//    try {
//      final URL url = new URL( fileName );
//      result = url.openStream();
//      skipLine( result ); // Skip content-type line.
//      skipLine( result ); // Skip comment line.
//      final String line = readLine( result ); // Dimensions.
//      String[] dimensions2 = line.split( " ", 2 );
//      dimensions[ 0 ] = dimensions2[ 0 ];
//      dimensions[ 1 ] = dimensions2[ 1 ];
//      dimensions2[ 0 ] = null;
//      dimensions2[ 1 ] = null;
//      skipLine( result ); // Skip comment line.
//    } catch ( Exception unused ) {
//      dimensions[ 0 ] = null;
//      dimensions[ 1 ] = null;
//
//      if ( result != null ) {
//        try { result.close(); } catch ( Exception unused2 ) { }
//        result = null;
//      }
//    }
//    return result;
//  }
//
//  // Read and skip a line from a stream:
//  private static void skipLine( final InputStream stream ) throws IOException {
//    boolean done = false;
//
//    do {
//      final int c = stream.read();
//      done = c == '\n' || c == -1;
//    } while ( ! done );
//  }
//
//  // Read and return a line from a stream:
//  private static String readLine(final InputStream stream) throws IOException {
//    String result = null;
//    final StringBuffer buffer = new StringBuffer( 256 );
//    boolean done = false;
//
//    do {
//      final int c = stream.read();
//      done = c == '\n' || c == -1;
//
//      if ( ! done ) {
//        buffer.append( (char) c );
//      }
//
//    } while ( ! done );
//
//    result = buffer.toString();
//    return result;
//  }
//
//  // Read sized array of bytes from a stream:
//  private static void readBytes( final InputStream stream, final byte[] bytes )
//    throws IOException {
//    final int totalBytesToRead = bytes.length;
//    int offset = 0;
//    boolean done = false;
//
//    do {
//      final int bytesToReadNow = totalBytesToRead - offset;
//      final int bytesReadNow = stream.read( bytes, offset, bytesToReadNow );
//      offset += bytesReadNow;
//      done = bytesReadNow == -1 || offset >= totalBytesToRead;
//    } while ( ! done );
//  }
//
//  // Read sized array of bytes from a file:
//  private static void readBytes( final RandomAccessFile file,
//                                 final byte[] bytes ) throws IOException {
//    final int totalBytesToRead = bytes.length;
//    int offset = 0;
//    boolean done = false;
//
//    do {
//      final int bytesToReadNow = totalBytesToRead - offset;
//      final int bytesReadNow = file.read( bytes, offset, bytesToReadNow );
//      offset += bytesReadNow;
//      done = bytesReadNow == -1 || offset >= totalBytesToRead;
//    } while ( ! done );
//  }
//
//  public boolean equals(Object otherMap) {
//	  if (mapFile == null || otherMap == null)
//		  return false;
//	  
//	  File current = new File(mapFile);
//	  File other = new File(((MapLines) otherMap).getMapFile());
//	  
//	  return current.equals(other);
//  }
//
//  public String getMapFile() {
//	return this.mapFile;
//  }
//
//  public String getTitle() {
//	  return title;
//  }
//
//  public void setTitle(String title) {
//	  this.title = title;
//  }
//  
//  public void setStyle(Style style) {
//	  this.style = style;
//  }
//  
//  public Style getStyle() {
//	  return this.style;
//  }
//
//}