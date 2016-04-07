/**
 * MPASShapefileWriter - Write 2D grid cells and data as 2D polygon
         Shapefiles (creates .shp, .shx and .dbf).
NOTES:   See 1998 ESRI Shapefile Specification pages 2, 4, 5, 16, 23, 24.
         http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf
         http://www.clicketyclick.dk/databases/xbase/format/dbf.html#DBF_STRUCT
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package gov.epa.emvl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.data.shapefile.dbf.DbaseFileHeader;
import org.geotools.data.shapefile.dbf.DbaseFileWriter;

import anl.verdi.data.ArrayReader;
import anl.verdi.data.MeshCellInfo;

public final class MPASShapefileWriter {
	static final Logger Logger = LogManager.getLogger(MPASShapefileWriter.class.getName());

  private MPASShapefileWriter() {} // Non-instantiable.

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
		  					double minLon, double maxLon,
		  					double minLat, double maxLat,
                            final String variable, final ArrayReader renderVariable,
                            final int timestep,
                            final int layer,
                            MeshCellInfo[] cells ) throws IOException {

    // What it is NOT:  Many Java users and developers assume that a 64-bit implementation
    // means that many of the built-in Java types are doubled in size from 32 to 64.  
    // This is not true.  We did not increase the size of Java integers from 32 to 64 and 
    // since Java longs were already 64 bits wide, they didn't need updating.  Array indexes, 
    // which are defined in the Java Virtual Machine Specification, are not widened from 32 
    // to 64.  We were extremely careful during the creation of the first 64-bit Java port
    // to insure Java binary and API compatibility so all existing 100% pure Java programs 
    // would continue running just as they do under a 32-bit VM.
    
	//Debug - export subset
	ArrayList<MeshCellInfo> newCells = new ArrayList<MeshCellInfo>();
	for (int i = 0; i < cells.length; ++i) {
		if (cells[i].getMinX() >= minLon &&
				cells[i].getMaxX() <= maxLon &&
				cells[i].getMinY() >= minLat &&
				cells[i].getMaxY() <= maxLat)
			newCells.add(cells[i]);
	}
	
	cells = newCells.toArray(new MeshCellInfo[0]);
	int numVertices = 0;
	minLon = Double.MAX_VALUE;
	maxLon = -1 * Double.MAX_VALUE;
	minLat = Double.MAX_VALUE;
	maxLat = -1 *Double.MAX_VALUE;
	for (int i = 0; i < cells.length; ++i) {
		numVertices = numVertices + cells[i].getNumVertices() + 1;
		if (cells[i].getMinX() < minLon)
			minLon = cells[i].getMinX();
		if (cells[i].getMaxX() > maxLon)
			maxLon = cells[i].getMaxX();
		if (cells[i].getMinY() < minLat)
			minLat = cells[i].getMinY();
		if (cells[i].getMaxY() > maxLat)
			maxLat = cells[i].getMaxY();
	}
		
    final int BYTES_PER_INT = 4;
    final int BYTES_PER_DOUBLE = 8;
    
    final int POLYGON = 5;
    final int PARTS_PER_POLYGON = 1;
    final int HEADER_BYTES = 100;
    final int RECORD_HEADER_BYTES_SHP = 8;
    final int RECORD_BYTES_SHX = 8;
    final int RECORD_CONTENT_BYTES_SHP =
        1 * BYTES_PER_INT +        // int ShapeType = POLYGON.
        4 * BYTES_PER_DOUBLE +     // double Box[ 4 ] = xMin,yMin,xMax,yMax.
        1 * BYTES_PER_INT +        // int NumParts = 1.
        1 * BYTES_PER_INT +        // int NumPoints = 5.
        1 * BYTES_PER_INT;        // int Parts[ NumParts = 1 ] = 0.
    final int RECORD_CONTENT_BYTES_VERTICES = 
        numVertices * 2 * BYTES_PER_DOUBLE; // double [NumPoints*2].
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
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
      0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 0, 0
    }; // variable length 128 bytes here .shp
    final int records = cells.length;
    final int shxFileBytes = HEADER_BYTES + records * RECORD_BYTES_SHX;
    final int shpFileBytes = HEADER_BYTES + records * (RECORD_HEADER_BYTES_SHP + RECORD_CONTENT_BYTES_SHP) + RECORD_CONTENT_BYTES_VERTICES;
    int byteIndex = 0;
    FileOutputStream file = null;

    // Initialize shx file header and records:

    writeInt( header, 0, 9994, BIG ); // file code, always 9994
    byteIndex = writeInt( header, 24, shxFileBytes / 2, BIG ); // file length
    byteIndex = writeInt( header, 28, 1000, LITTLE );
    byteIndex = writeInt( header, 32, POLYGON, LITTLE );
    byteIndex = writeDouble( header, byteIndex, minLon, LITTLE );
    byteIndex = writeDouble( header, byteIndex, minLat, LITTLE );
    byteIndex = writeDouble( header, byteIndex, maxLon, LITTLE );
    byteIndex = writeDouble( header, byteIndex, maxLat, LITTLE );

    writeInt( recordContents, 0, POLYGON, LITTLE );
    writeInt( recordContents, 36, PARTS_PER_POLYGON, LITTLE );

    // Write shx file:

    try {
      file = new FileOutputStream( fileName + ".shx" );

      file.write( header );
      file.flush();

      int offset = HEADER_BYTES / 2;
      for ( int i = 0; i < cells.length; ++i ) {
    	writeInt( recordHeader, 0, offset, BIG);
    	int length = (RECORD_CONTENT_BYTES_SHP + 2 * BYTES_PER_DOUBLE * (cells[i].getNumVertices() + 1)) / 2;
    	writeInt( recordHeader, 4, length, BIG );
    	offset += length + RECORD_HEADER_BYTES_SHP / 2;
    	file.write( recordHeader );
    	file.flush();
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

      for ( int i = 0; i < cells.length; ++i ) {	  
          writeInt( recordHeader, 0, i + 1, BIG );
          int length = (RECORD_CONTENT_BYTES_SHP + 2 * BYTES_PER_DOUBLE * (cells[i].getNumVertices() + 1)) / 2;
          writeInt( recordHeader, 4, length, BIG );
        file.write( recordHeader );
        file.flush();
        
        // Compute and write POLYGON record contents:



        byteIndex = writeDouble( recordContents, 4, cells[i].getMinX(), LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, cells[i].getMinY(), LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, cells[i].getMaxX(), LITTLE );
        byteIndex =
          writeDouble( recordContents, byteIndex, cells[i].getMaxY(), LITTLE);
        
        byteIndex = writeInt( recordContents, byteIndex, 1, LITTLE );
        byteIndex = writeInt( recordContents, byteIndex, cells[i].getNumVertices() + 1, LITTLE );
        byteIndex = writeInt( recordContents, byteIndex, 0, LITTLE );

        for (int j = cells[i].getNumVertices() - 1; j >= 0; --j) {
        	byteIndex =
        	          writeDouble( recordContents, byteIndex,cells[i].getLon(j), LITTLE );
        	byteIndex =
      	          writeDouble( recordContents, byteIndex,cells[i].getLat(j), LITTLE );
        }
    	byteIndex =
  	          writeDouble( recordContents, byteIndex,cells[i].getLon(cells[i].getNumVertices() - 1), LITTLE );
    	byteIndex =
	          writeDouble( recordContents, byteIndex,cells[i].getLat(cells[i].getNumVertices() - 1), LITTLE );

        file.write(recordContents, 0, length * 2);
        file.flush();
      }
    } catch ( Exception unused_ ) {
    } finally {

      if ( file != null ) {
        try { file.close(); } catch ( Exception unused2 ) { }
        file = null;
      }
    }

    if ( variable != null && cells != null ) {
      writeDBF( fileName, variable, renderVariable, timestep, layer, cells );
      writePRJ( fileName );
    }
  }
  
  
  private static void writeDBF( final String fileName, final String variable, final ArrayReader renderVariable,
          final int timestep, final int layer, MeshCellInfo[] cells ) throws IOException {
	  DbaseFileHeader header = new DbaseFileHeader();
	  header.addColumn("ID", 'N', 10, 0);
      header.addColumn(variable, 'F', 20, 6);
      header.setNumRecords(cells.length);
      File f = new File(fileName + ".dbf");
      @SuppressWarnings("resource")
	  FileOutputStream fout = new FileOutputStream(f);
      DbaseFileWriter dbf = new DbaseFileWriter(header, fout.getChannel());
      for (int i = 0; i < cells.length; ++i) {
    	  Object[] row = new Object[2];
    	  row[0] = new Double(cells[i].getId());
    	  row[1] = new Double(cells[i].getValue(renderVariable, timestep, layer));
       	  dbf.write(row);
      }
      dbf.close();  
  }

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


