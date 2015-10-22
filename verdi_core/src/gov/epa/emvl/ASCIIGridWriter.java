
/******************************************************************************
PURPOSE: ASCIIGridWriter.java - Write 2D grid cells and data as an ASCII Grid
         file (.asc).
NOTES:   See ASCII Grid file specification:
         http://en.wikipedia.org/wiki/ESRI_grid
HISTORY: 2010-08-26 plessel.todd@epa.gov Created.
******************************************************************************/

package gov.epa.emvl;

import java.io.FileOutputStream;

public final class ASCIIGridWriter {

  private ASCIIGridWriter() {} // Non-instantiable.

  /**
   * write - Write a single layer of grid cells and a
   * single timestep of scalar data as lon-lat Shapefile Polygon files	// JEB does not pertain to Shapefile
   * (shp, shx, dbf).													// irrelevant
   * INPUTS:
   * final String fileName  Base name of file to create. "example.asc".
   * final int rows              Number of grid rows.
   * final int columns           Number of grid columns.
   * final double westEdge       Distance from origin to west edge of grid.
   * final double southEdge      Distance from origin to south edge of ".
   * final double cellSize       Width/height of each grid cell (e.g., 2000 m).
   * final float[ rows ][ columns ] data  Scalar data at grid cell centers.
   * OUTPUTS:
   * fileName  Contains the grid description and cell data.
   * CONTRACT:
   * @pre fileName != null
   * @pre rows > 0
   * @pre columns > 0
   * @pre ! Numerics.isNan( westEdge )
   * @pre ! Numerics.isNan( southEdge )
   * @pre ! Numerics.isNan( cellSize )
   * @pre cellSize > 0.0
   * @pre data != null
   */

  public static void write( final String fileName,		// fully-qualified name of file to output
                            final int rows,				// number of rows (y)
                            final int columns,			// number of columns (x)
                            final double westEdge,		
                            final double southEdge,
                            final double cellSize,
                            final float[][] data ) {

    FileOutputStream file = null;

    try {
      file = new FileOutputStream( fileName );
      final String header =
        String.format( "ncols %d\nnrows %d\nxllcorner %g\nyllcorner %g\n" +
                       "cellsize %g\nNODATA_value -9999.0\n",
                       columns, rows, westEdge, southEdge, cellSize );
      file.write( header.getBytes( "US-ASCII" ) );

      for ( int row = rows - 1; row >= 0; --row ) {	// starts in upper-left cell & moves downward

        for ( int column = 0; column < columns; ++column ) {	// for a row moves left-to-right
          final float dataValue = data[ row ][ column ];
          final float clampedValue = dataValue > -9999.0f ? dataValue : -9999.0f;
          final String formattedValue = String.format( "%g ", clampedValue );
          file.write( formattedValue.getBytes( "US-ASCII" ) );
        }

        file.write( '\n' );		// newline at end of each row
      }
    } catch ( Exception unused_ ) {
    } finally {

      if ( file != null ) {
        try { file.close(); } catch ( Exception unused2 ) { }
        file = null;
      }
    }
  }
};


