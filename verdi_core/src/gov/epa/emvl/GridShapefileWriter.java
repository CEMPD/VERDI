
/******************************************************************************
PURPOSE: GridShapefileWriter.java - Write 2D grid cells and data as 2D polygon
         Shapefiles (creates .shp, .shx and .dbf).
NOTES:   See 1998 ESRI Shapefile Specification pages 2, 4, 5, 16, 23, 24.
         http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf
         http://www.clicketyclick.dk/databases/xbase/format/dbf.html#DBF_STRUCT
HISTORY: 2010-08-23 plessel.todd@epa.gov Created.
******************************************************************************/

package gov.epa.emvl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;

import com.bbn.openmap.io.FormatException;

public final class GridShapefileWriter {

  private GridShapefileWriter() {} // Non-instantiable.

  private static final int BIG = 4321;    // Byte-order.
  private static final int LITTLE = 1234; // Byte-order.
 // private static final int POLYGON = 5;   // Shapefile type for 2D polygon.
  
  private static String characterSetName = "cp437"; //"US-ASCII"; // US-ASCII: 20127 in Excel //"8859_1";
  static byte languageDriver = 0x01; // cp437: IBM437

  /**
   * write - Write a single layer of grid cells and a
   * single timestep of scalar data as lon-lat Shapefile Polygon files
   * (shp, shx, dbf).
   * INPUTS:
   * final String fileName  Base name of file to create. "example".
   * final int rows              Number of grid rows.
   * final int columns           Number of grid columns.
   * final double westEdge       Distance from origin to west edge of grid.
   * final double southEdge      Distance from origin to south edge of ".
   * final double cellWidth      Width of each grid cell (e.g., 12000 m).
   * final double cellWHeight    Height of each grid cell (e.g., 12000 m).
   * final String variable       Name of data variable.
   * final double[ rows ][ columns ] data  Scalar data at grid cell centers.
   * final Projector projector   To unproject (x,y) to (lon,lat).
   * OUTPUTS:
   * fileName.shp  Contains the grid cell polygons.
   * fileName.shx  Index file for the above.
   * fileName.dbf  Contains the data as a single-column table.
   * CONTRACT:
 * @throws IOException 
 * @pre fileName != null
   * @pre rows > 0
   * @pre columns > 0
   * @pre ! Numerics.isNan( westEdge )
   * @pre ! Numerics.isNan( southEdge )
   * @pre ! Numerics.isNan( cellWidth )
   * @pre ! Numerics.isNan( cellHeight )
   * @pre cellWidth > 0.0
   * @pre cellHeight > 0.0
   * @pre isValidLongitudeLatitude( westEdge, southEdge ) || projector != null
   * @pre isValidLongitudeLatitude( westEdge + columns * cellWidth,
   *                                southEdge + rows * cellHeight ) ||
   *      projector != null
   * @pre ( variable != null ) == ( data != null )
   */

  public static void write( final String fileName,
                            final int rows,
                            final int columns,
                            final double westEdge,
                            final double southEdge,
                            final double cellWidth,
                            final double cellHeight,
                            final String variable,
                            final float[][] data,
                            final Projector projector ) throws IOException {

    if ( projector == null &&
         ! ( isValidLongitudeLatitude( westEdge, southEdge ) &&
             isValidLongitudeLatitude( westEdge + columns * cellWidth,
                                       southEdge + rows * cellHeight ) ) ) {
      throw new
        IllegalArgumentException( "Projector required for non-lon-lat grid." );
    }

    // What it is NOT:  Many Java users and developers assume that a 64-bit implementation
    // means that many of the built-in Java types are doubled in size from 32 to 64.  
    // This is not true.  We did not increase the size of Java integers from 32 to 64 and 
    // since Java longs were already 64 bits wide, they didn't need updating.  Array indexes, 
    // which are defined in the Java Virtual Machine Specification, are not widened from 32 
    // to 64.  We were extremely careful during the creation of the first 64-bit Java port
    // to insure Java binary and API compatibility so all existing 100% pure Java programs 
    // would continue running just as they do under a 32-bit VM.
    
    final int BYTES_PER_INT = 4;
    final int BYTES_PER_DOUBLE = 8;
    
    final int POLYGON = 5;
    final int PARTS_PER_POLYGON = 1;
    final int VERTICES_PER_POLYGON = 5;
    final int HEADER_BYTES = 100;
    final int RECORD_HEADER_BYTES_SHP = 8;
    final int RECORD_BYTES_SHX = 8;
    final int RECORD_CONTENT_BYTES_SHP =
        1 * BYTES_PER_INT +        // int ShapeType = POLYGON.
        4 * BYTES_PER_DOUBLE +     // double Box[ 4 ] = xMin,yMin,xMax,yMax.
        1 * BYTES_PER_INT +        // int NumParts = 1.
        1 * BYTES_PER_INT +        // int NumPoints = 5.
        1 * BYTES_PER_INT +        // int Parts[ NumParts = 1 ] = 0.
        VERTICES_PER_POLYGON * 2 * BYTES_PER_DOUBLE; // double [NumPoints*2].
    final byte[] header = { 
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
    }; // 100 bytes .shp .shx
    final byte[] recordHeader = { 0, 0, 0, 0, 0, 0, 0, 0 }; // 8 bytes .shp
    final byte[] recordContents = {
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0
    }; // variable length 128 bytes here .shp
    final int records = rows * columns;
    final int shxFileBytes = HEADER_BYTES + records * RECORD_BYTES_SHX;
    final int shpFileBytes = HEADER_BYTES + records * (RECORD_HEADER_BYTES_SHP + RECORD_CONTENT_BYTES_SHP);
    int byteIndex = 0;
    final double[] xyRange = { 0.0, 0.0, 0.0, 0.0 };
    FileOutputStream file = null;

    computeGridBounds( rows, columns, westEdge, southEdge,
                       cellWidth, cellHeight, projector, xyRange );

    // Initialize shx file header and records:

    writeInt( header, 0, 9994, BIG ); // file code, always 9994
    byteIndex = writeInt( header, 24, shxFileBytes / 2, BIG ); // file length
    byteIndex = writeInt( header, 28, 1000, LITTLE );
    byteIndex = writeInt( header, 32, POLYGON, LITTLE );
    byteIndex = writeDouble( header, byteIndex, xyRange[ 0 ], LITTLE );
    byteIndex = writeDouble( header, byteIndex, xyRange[ 1 ], LITTLE );
    byteIndex = writeDouble( header, byteIndex, xyRange[ 2 ], LITTLE );
    byteIndex = writeDouble( header, byteIndex, xyRange[ 3 ], LITTLE );

    writeInt( recordHeader, 0, HEADER_BYTES / 2, BIG );
    writeInt( recordHeader, 4, RECORD_CONTENT_BYTES_SHP / 2, BIG );

    writeInt( recordContents, 0, POLYGON, LITTLE );
    writeInt( recordContents, 36, PARTS_PER_POLYGON, LITTLE );
    writeInt( recordContents, 40, VERTICES_PER_POLYGON, LITTLE );

    // Write shx file:

    try {
      file = new FileOutputStream( fileName + ".shx" );

      file.write( header );
      file.flush();

      int offsetWords = HEADER_BYTES / 2;
      int offsetStep  = (RECORD_HEADER_BYTES_SHP + RECORD_CONTENT_BYTES_SHP) / 2; // store information for main file (.shp)
      for ( int record = 0; record < records; ++record ) {
    	file.write( recordHeader );
    	file.flush();
        offsetWords += offsetStep;
        writeInt( recordHeader, 0, offsetWords, BIG );
      }
    } catch ( Exception unused_ ) {
    } finally {

      if ( file != null ) {
        try { file.close(); } catch ( Exception unused2 ) { }
        file = null;
      }
    }


    // Write shp file:

    try {
      file = new FileOutputStream( fileName + ".shp" );
      writeInt( header, 24, shpFileBytes / 2, BIG );
      file.write( header );
      file.flush();

      for ( int record = 0; record < records; ++record ) {
        writeInt( recordHeader, 0, record + 1, BIG );
        file.write( recordHeader );
        file.flush();
        
        // Compute and write POLYGON record contents:

        final int row    = record / columns;
        final int column = record % columns;
        final double[] xy = {
          0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0
        };

        computePolygonVertices( row, column,
                                westEdge, southEdge, cellWidth, cellHeight,
                                projector, xy, xyRange );

        byteIndex = writeDouble( recordContents, 4, xyRange[ 0 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xyRange[ 1 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xyRange[ 2 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xyRange[ 3 ], LITTLE);
        byteIndex =
          writeDouble( recordContents, 48, xy[ 0 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xy[ 1 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xy[ 2 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xy[ 3 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xy[ 4 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xy[ 5 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xy[ 6 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xy[ 7 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xy[ 8 ], LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, xy[ 9 ], LITTLE );
        file.write( recordContents );
        file.flush();
      }
    } catch ( Exception unused_ ) {
    } finally {

      if ( file != null ) {
        try { file.close(); } catch ( Exception unused2 ) { }
        file = null;
      }
    }

    if ( variable != null && data != null ) {
      writeDBF( fileName, variable, rows, columns, data );
      writePRJ( fileName );
    }
  }
  
  private static void writeDBF( final String fileName, final String variable,
          final int rows, final int columns,
          final float[][] data ) throws IOException {
	  DbaseFileHeader header = new DbaseFileHeader();
      header.addColumn(variable, 'F', 20, 6);
      header.setNumRecords(rows*columns);
      File f = new File(fileName + ".dbf");
      FileOutputStream fout = new FileOutputStream(f);
      DbaseFileWriter dbf = new DbaseFileWriter(header, fout.getChannel());
      for (int i = 0; i < rows; i++) {
    	  for (int j=0; j<columns; j++) {
    		  Object[] row = new Object[1];
    		  row[0] = new Double(data[i][j]);
    		  dbf.write(row);
    	  }
      }
      dbf.close();  
  }
// 2014 writeDBF_old appears to not be used. Commenting it out to check.
//  private static void writeDBF_old( final String fileName, final String variable,
//                                final int rows, final int columns,
//                                final float[][] data ) {
//	boolean ok = true;
//	String fileNameDbfConvert;
//	
//    final int HEADER_BYTES = 32 + 32 + 1;
//    final int FIELD_WIDTH = 17; // "%17.6lf".
//    final int FIELD_DECIMAL_DIGITS = 6; // # of digits to the right of decimal
//    final int records = rows * columns;
//    final byte[] header = {
//      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//      0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
//      0, 0, 0, 0, 0
//    };
//
//    // Initialize header:
//    
//    GregorianCalendar calendar = new GregorianCalendar();
//System.out.println("in GridShapefileWriter, writeDBF_old, created GregorianCalendar calendar");
//	byte year = (byte)( calendar.get( Calendar.YEAR) - 1900);
//	byte month = (byte)( calendar.get( Calendar.MONTH)+1);
//	byte day = (byte)( calendar.get( Calendar.DAY_OF_MONTH));
//
//    header[ 0 ] = 0x3;   // dBASE Level 5.
//    header[ 1 ] = year;  // Update timestamp: YY - 1900.
//    header[ 2 ] = month;   // Update timestamp: MM.
//    header[ 3 ] = day;  // Update timestamp: DD.
//    writeInt( header, 4, records, LITTLE );
//    writeShort( header, 8, HEADER_BYTES, LITTLE );
//    writeShort( header, 10, FIELD_WIDTH, LITTLE );
//    header[29] = languageDriver;
//
//    // Field Descriptor:
//
//    final int length = variable.length();
//    final int letters = length < 10 ? length : 10;
//    String newVar = variable.substring(0, letters);
//    byte[] varBytes = newVar.getBytes(java.nio.charset.Charset.forName(characterSetName));
//
//    for ( int letter = 0; letter < letters; ++letter ) {
//      header[ 32 + letter ] = (byte) varBytes[letter]; //variable.charAt( letter );
//    }
//
//    header[ 43 ] = 'N';  /* Formatted real number. */
//    header[ 48 ] = FIELD_WIDTH;
//    header[ 49 ] = FIELD_DECIMAL_DIGITS;
//    header[ 64 ] = 0x0D; /* Terminator. */
//    FileOutputStream file = null;
//
//    try {
//      file = new FileOutputStream( fileName + ".dbf" );
//      file.write( header );
//      file.flush();
//      
//      for ( int row = 0; row < rows; ++row ) {
//
//        for ( int column = 0; column < columns; ++column ) {
//          final float value = data[ row ][ column ];
//          final String format =
//            String.format( "%%%d.%df", FIELD_WIDTH, FIELD_DECIMAL_DIGITS );
//          final String formattedValue = String.format( format, value );
//          file.write( formattedValue.getBytes( characterSetName ), 0, FIELD_WIDTH );
//          file.flush();
//        }
//      }
//    } catch ( Exception unused_ ) {
//      System.err.println( unused_ );
//      ok = false;
//    } finally {
//
//      if ( file != null ) {
//        try { file.close(); } catch ( Exception unused2 ) { }
//        file = null;
//      }
//    }
//    
////    try {
////		Thread.sleep(200);
////	} catch (InterruptedException e1) {
////		// TODO Auto-generated catch block
////		e1.printStackTrace();
////	}
//    
//	
//    if ( ok) {
//    	com.bbn.openmap.dataAccess.shape.DbfFile dtm = (com.bbn.openmap.dataAccess.shape.DbfFile) com.bbn.openmap.dataAccess.shape.DbfFile.getDbfTableModel(fileName + ".dbf");
//    	OutputStream os;
//		try {
//			double num = Double.MAX_VALUE;
//			dtm.readData(0, (int) num);
//			os = new FileOutputStream(fileName + ".dbf");
//			com.bbn.openmap.dataAccess.shape.output.DbfOutputStream dos = new com.bbn.openmap.dataAccess.shape.output.DbfOutputStream(os);
//			dos.writeModel(dtm);				
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (FormatException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} 
//    }
//  }

  private static void writePRJ( final String fileName ) {
    final String contentString =
      "GEOGCS[\"GCS_WGS_1984\"," +
      "DATUM[\"D_WGS_1984\"," +
      "SPHEROID[\"WGS_1984\",6378137.0,298.257223563]]," +
      "PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\",0.0174532925199433]]";
    FileOutputStream file = null;

    try {
      file = new FileOutputStream( fileName + ".prj" );
      file.write( contentString.getBytes(characterSetName) );
    } catch ( Exception unused_ ) {
      System.err.println( unused_ );
    } finally {

      if ( file != null ) {
        try { file.close(); } catch ( Exception unused2 ) { }
        file = null;
      }
    }
  }

  private static boolean isValidLongitudeLatitude( final double longitude,
                                                   final double latitude ) {
    final boolean result =
      longitude >= -180.0 && longitude <= 180.0 &&
      latitude  >=  -90.0 && latitude  <=  90.0;
    return result;
  }

  // Compute xy vertex range[ 4 ] = { xMinimum, yMinimum, xMaximum, yMaximum }:

  private static void computeGridBounds( final int rows,
                                         final int columns,
                                         final double westEdge,
                                         final double southEdge,
                                         final double cellWidth,
                                         final double cellHeight,
                                         final Projector projector,
                                         double[] range ) {
    final int rows1 = rows + 1;
    final int columns1 = columns + 1;
    double x = westEdge;
    double y = southEdge;
    double[] pxy = { 0.0, 0.0 };
    double minimumX = x;
    double maximumX = minimumX;
    double minimumY = y;
    double maximumY = minimumY;

    // Initialize to west/south point:

    if ( projector != null ) {
      projector.unproject( x, y, pxy );
      minimumX = maximumX = pxy[ 0 ];
      minimumY = maximumY = pxy[ 1 ];
    } else {
      pxy[ 0 ] = x;
      pxy[ 1 ] = y;
    }
    
    // Check against rest of west edge of grid:

    y += cellHeight;

    for ( int row = 1; row < rows1; ++row, y += cellHeight ) {

      if ( projector != null ) {
        projector.unproject( x, y, pxy );
       } else {
        pxy[ 0 ] = x;
        pxy[ 1 ] = y;
      }

      if ( pxy[ 0 ] < minimumX ) {
        minimumX = pxy[ 0 ];
      } else if ( pxy[ 0 ] > maximumX ) {
        maximumX = pxy[ 0 ];
      }

      if ( pxy[ 1 ] < minimumY ) {
        minimumY = pxy[ 1 ];
      } else if ( pxy[ 1 ] > maximumY ) {
        maximumY = pxy[ 1 ];
      }
    }

    // Check against east edge of grid:

    x = westEdge + columns * cellWidth;
    y = southEdge;

    for ( int row = 0; row < rows1; ++row, y += cellHeight ) {

      if ( projector != null ) {
        projector.unproject( x, y, pxy );
      } else {
        pxy[ 0 ] = x;
        pxy[ 1 ] = y;
      }

      if ( pxy[ 0 ] < minimumX ) {
        minimumX = pxy[ 0 ];
      } else if ( pxy[ 0 ] > maximumX ) {
        maximumX = pxy[ 0 ];
      }

      if ( pxy[ 1 ] < minimumY ) {
        minimumY = pxy[ 1 ];
      } else if ( pxy[ 1 ] > maximumY ) {
        maximumY = pxy[ 1 ];
      }
    }

    // Check against rest of south edge of grid:

    x = westEdge + cellWidth;
    y = southEdge;

    for ( int column = 1; column < columns1; ++column, x += cellWidth ) {

      if ( projector != null ) {
        projector.unproject( x, y, pxy );
       } else {
        pxy[ 0 ] = x;
        pxy[ 1 ] = y;
      }

      if ( pxy[ 0 ] < minimumX ) {
        minimumX = pxy[ 0 ];
      } else if ( pxy[ 0 ] > maximumX ) {
        maximumX = pxy[ 0 ];
      }

      if ( pxy[ 1 ] < minimumY ) {
        minimumY = pxy[ 1 ];
      } else if ( pxy[ 1 ] > maximumY ) {
        maximumY = pxy[ 1 ];
      }
    }

    // Check against north edge of grid:

    x = westEdge;
    y = southEdge + rows * cellHeight;

    for ( int column = 0; column < columns1; ++column, x += cellWidth ) {

      if ( projector != null ) {
        projector.unproject( x, y, pxy );
       } else {
        pxy[ 0 ] = x;
        pxy[ 1 ] = y;
      }

      if ( pxy[ 0 ] < minimumX ) {
        minimumX = pxy[ 0 ];
      } else if ( pxy[ 0 ] > maximumX ) {
        maximumX = pxy[ 0 ];
      }

      if ( pxy[ 1 ] < minimumY ) {
        minimumY = pxy[ 1 ];
      } else if ( pxy[ 1 ] > maximumY ) {
        maximumY = pxy[ 1 ];
      }
    }

    range[ 0 ] = minimumX;
    range[ 1 ] = minimumY;
    range[ 2 ] = maximumX;
    range[ 3 ] = maximumY;

    if ( projector != null ) {
      // Adjust latitudes from sphere to WGS84/GRS80/NAD83 spheroid/datum:
        range[ 1 ] = latitudeWGS84( range[ 1 ] );
        range[ 3 ] = latitudeWGS84( range[ 3 ] );
    }
  }

  /**
   * Compute vertices of grid cell as an explicitly closed 2D 5-vertex polygon
   * ring in clockwise order.
   */

  private static void computePolygonVertices( final int row,
                                              final int column,
                                              final double westEdge,
                                              final double southEdge,
                                              final double cellWidth,
                                              final double cellHeight,
                                              final Projector projector,
                                              double[] xy,
                                              double[] xyRange ) {

    double x = westEdge + column * cellWidth;
    double y = southEdge + row * cellHeight;
    double[] pxy = { 0.0, 0.0 };

    if ( projector != null ) {
      projector.unproject( x, y, pxy );
    } else {
      pxy[ 0 ] = x;
      pxy[ 1 ] = y;
    }

    xy[ 0 ] = pxy[ 0 ];
    xy[ 1 ] = pxy[ 1 ];

    y += cellHeight;

    if ( projector != null ) {
      projector.unproject( x, y, pxy );
    } else {
      pxy[ 0 ] = x;
      pxy[ 1 ] = y;
    }

    xy[ 2 ] = pxy[ 0 ];
    xy[ 3 ] = pxy[ 1 ];

    x += cellWidth;

    if ( projector != null ) {
      projector.unproject( x, y, pxy );
    } else {
      pxy[ 0 ] = x;
      pxy[ 1 ] = y;
    }

    xy[ 4 ] = pxy[ 0 ];
    xy[ 5 ] = pxy[ 1 ];

    y -= cellHeight;

    if ( projector != null ) {
      projector.unproject( x, y, pxy );
    } else {
      pxy[ 0 ] = x;
      pxy[ 1 ] = y;
    }

    xy[ 6 ] = pxy[ 0 ];
    xy[ 7 ] = pxy[ 1 ];

    xy[ 8 ] = xy[ 0 ];
    xy[ 9 ] = xy[ 1 ];

    xyRange[ 0 ] = xyRange[ 1 ] = xyRange[ 2 ] = xyRange[ 3 ] = 0.0;
    computeRange( xy, 0, 10, 2, xyRange );
    computeRange( xy, 1, 10, 2, pxy );
    final double xMaximum = xyRange[ 1 ];
    xyRange[ 1 ] = pxy[ 0 ];
    xyRange[ 2 ] = xMaximum;
    xyRange[ 3 ] = pxy[ 1 ];

   if ( projector != null ) {
      // Adjust latitudes from sphere to WGS84/GRS80/NAD83 spheroid/datum:
      xyRange[ 1 ] = latitudeWGS84( xyRange[ 1 ] );
      xyRange[ 3 ] = latitudeWGS84( xyRange[ 3 ] );
      xy[ 1 ] = latitudeWGS84( xy[ 1 ] );
      xy[ 3 ] = latitudeWGS84( xy[ 3 ] );
      xy[ 5 ] = latitudeWGS84( xy[ 5 ] );
      xy[ 7 ] = latitudeWGS84( xy[ 7 ] );
      xy[ 9 ] = latitudeWGS84( xy[ 9 ] );
    }
  }

  private static void computeRange( final double[] array,
                                    final int start,
                                    final int count,
                                    final int stride,
                                    double[] range ) {
    double minimum = array[ start ];
    double maximum = minimum;

    for ( int index = start + stride; index < count; index += stride ) {
      final double item = array[ index ];

      if ( item < minimum ) {
        minimum = item;
      } else if ( item > maximum ) {
        maximum = item;
      }
    }

    range[ 0 ] = minimum;
    range[ 1 ] = maximum;
  }

  // Convert latitude on sphere to latitude on WGS84/GRS80/NAD83 spheroid.
  // http://en.wikipedia.org/wiki/Latitude#Geocentric_latitude.
  
  private static double latitudeWGS84( final double latitudeSphere ) {
    final double inverseWGS84AxisRatioSquared = 1.006739496756587;
    final double latitudeSphereRadians = latitudeSphere * ( Math.PI / 180.0 );
    final double latitudeWGS84Radians =
      Math.atan( Math.tan( latitudeSphereRadians ) * inverseWGS84AxisRatioSquared );
    final double result = latitudeWGS84Radians * ( 180.0 / Math.PI );
    return result;
  }

  private static int writeShort( byte[] bytes, final int index,
                                 final int value, final int endian ) {
    final int result = index + 4;
    final byte byte1 = (byte)   ( value & 0x0000ff );
    final byte byte2 = (byte) ( ( value & 0x00ff00 ) >> 8 );

    if ( endian == BIG ) {
      bytes[ index     ] = byte2;
      bytes[ index + 1 ] = byte1;
    } else {
      bytes[ index     ] = byte1;
      bytes[ index + 1 ] = byte2;
    }

    return result;
  }

  private static int writeInt( byte[] bytes, final int index,
                     final int value, final int endian ) {
    final int result = index + 4;
    final byte byte1 = (byte)   ( value & 0x000000ff );
    final byte byte2 = (byte) ( ( value & 0x0000ff00 ) >> 8 );
    final byte byte3 = (byte) ( ( value & 0x00ff0000 ) >> 16 );
    final byte byte4 = (byte) ( ( value & 0xff000000 ) >> 24 );

    if ( endian == BIG ) {
      bytes[ index     ] = byte4;
      bytes[ index + 1 ] = byte3;
      bytes[ index + 2 ] = byte2;
      bytes[ index + 3 ] = byte1;
    } else {
      bytes[ index     ] = byte1;
      bytes[ index + 1 ] = byte2;
      bytes[ index + 2 ] = byte3;
      bytes[ index + 3 ] = byte4;
    }

    return result;
  }

  private static int writeDouble( byte[] bytes, final int index,
                                  final double value, final int endian ) {
    final int result = index + 8;
    final long ivalue = Double.doubleToRawLongBits( value );
    final byte byte1 = (byte) (   ivalue & 0x00000000000000ffL );
    final byte byte2 = (byte) ( ( ivalue & 0x000000000000ff00L ) >> 8 );
    final byte byte3 = (byte) ( ( ivalue & 0x0000000000ff0000L ) >> 16 );
    final byte byte4 = (byte) ( ( ivalue & 0x00000000ff000000L ) >> 24 );
    final byte byte5 = (byte) ( ( ivalue & 0x000000ff00000000L ) >> 32 );
    final byte byte6 = (byte) ( ( ivalue & 0x0000ff0000000000L ) >> 40 );
    final byte byte7 = (byte) ( ( ivalue & 0x00ff000000000000L ) >> 48 );
    final byte byte8 = (byte) ( ( ivalue & 0xff00000000000000L ) >> 56 );

    if ( endian == BIG ) {
      bytes[ index     ] = byte8;
      bytes[ index + 1 ] = byte7;
      bytes[ index + 2 ] = byte6;
      bytes[ index + 3 ] = byte5;
      bytes[ index + 4 ] = byte4;
      bytes[ index + 5 ] = byte3;
      bytes[ index + 6 ] = byte2;
      bytes[ index + 7 ] = byte1;
    } else {
      bytes[ index     ] = byte1;
      bytes[ index + 1 ] = byte2;
      bytes[ index + 2 ] = byte3;
      bytes[ index + 3 ] = byte4;
      bytes[ index + 4 ] = byte5;
      bytes[ index + 5 ] = byte6;
      bytes[ index + 6 ] = byte7;
      bytes[ index + 7 ] = byte8;
    }

    return result;
  }
};


