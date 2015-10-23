/**
 * SpreadsheetReader.java - Reads tab-delimited ASCII spreadsheet data files.
 * @author Todd Plessel
 * @version $Revision$ $Date$
 */

package anl.verdi.loaders;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.List;
import java.util.ArrayList;
import java.io.File;
import java.io.RandomAccessFile;

import ucar.ma2.ArrayDouble;

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.AxisType;
import anl.verdi.data.DataReader;
import anl.verdi.data.Dataset;
import anl.verdi.data.DataFrame;
import anl.verdi.data.Variable;
import anl.verdi.data.DefaultVariable;
import anl.verdi.data.AxisRange;
import anl.verdi.util.VUnits;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameBuilder;

public class SpreadsheetReader implements DataReader<SpreadsheetDataset> {

	private static final String headerStart =
		"timestamp(utc)\tlongitude(deg)\tlatitude(deg)\t";
	private static final int requiredColumns = headerStart.split("\t").length;
	private static final String EOL = "\n"; // '\r' characters have been filtered.
	
	private static final String[] supportedTimeZones = 
	{"EST", "EDT", "PST", "PDT", "MST", "MDT", 
	 "CSD", "CDT", "HAST", "HADT", "AKST", "AKDT", 
	 "LST", "LDT", "GMT", "UTC"};

	private SpreadsheetDataset dataset = null;
	
	private String timezone = null;
	
	public static String validateHeaderAndGetTimezone( String header) throws Exception{
		if ( header == null || header.trim().length() == 0) {
			throw new Exception("Invalid header!");
		}
		String timeZone = "";
		final String[] words = header.split( "\t" );
		final int columns = words.length;
		if ( columns <= SpreadsheetReader.requiredColumns) {
			throw new Exception("Number of columns are less than required!");
		}
		if ( !words[0].toLowerCase().startsWith("timestamp(") ||
			 !words[0].toLowerCase().endsWith(")") ||
			 !words[1].toLowerCase().startsWith("longitude(deg)") ||
			 !words[2].toLowerCase().startsWith("latitude(deg)") )
		{
			throw new Exception("Invalid header!");
		}
		
		int inx1 = words[0].indexOf("(");
		int inx2 = words[0].indexOf(")");
		timeZone = words[0].substring(inx1+1, inx2);
		
		if ( timeZone == null || timeZone.length() == 0) {
			throw new Exception("Invalid timezone!");
		}
		timeZone = timeZone.trim().toUpperCase();
		
		boolean supported = false;
		for (String tZone : supportedTimeZones) {
			if (tZone.equals( timeZone)) {
				supported = true;
				break;
			}
		}
		if ( !supported) {
			throw new Exception("Timezone not supported! Supported timezones are: EST EDT PST PDT MST MDT CSD CDT HAST HADT AKST AKDT LST LDT GMT and UTC.");
		}
		return timeZone;
	}

	public SpreadsheetReader( final String fileName ) {
		final StringBuffer buffer = readFile( fileName );

		if ( buffer != null ) {
			final int endOfLine = buffer.indexOf( EOL );

			if ( endOfLine > 0 ) {
				final String header =
					buffer.substring( 0, endOfLine ).toLowerCase();
				try {
					timezone = validateHeaderAndGetTimezone( header);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					throw new IllegalArgumentException( fileName +
					" is not a valid tab-delimited ASCII Spreadsheet data file: " + e.getMessage() );
				}

				//if ( header.startsWith( headerStart ) ) {
					final String[] words = header.split( "\t" );
					final int columns = words.length;

					if ( columns > requiredColumns  ) {
			 			final boolean hasElevation =
			 				words[ requiredColumns ].equals( "elevation(m)" );
			 			final int[] points = computePointsPerHour( buffer );
			 			final int rows = countLines( buffer ) - 1;
			 			final double[][] data = new double[ columns ][ rows ];
			 				
			 			if ( parseData( buffer, data ) ) {
				 			final Axes<CoordAxis> axes =
				 				createAxes( hasElevation, points, data );
			 				dataset =
			 					new SpreadsheetDataset(fileName, axes, points,
			 											data );

			 				final List<Variable> variables =
			 					new ArrayList<Variable>( columns );

			 				final Variable timestamp =
			 					new DefaultVariable("timestamp",
			 										"YYYYMMDD.HHMMSS.",
			 										VUnits.createUnit( timezone), // "UTC" ),
			 										dataset );
			 				final Variable longitude =
			 					new DefaultVariable("longitude",
			 										"Longitude [-180, 180].",
			 										VUnits.createUnit( "deg" ),
			 										dataset );
			 				final Variable latitude =
			 					new DefaultVariable("latitude",
			 										"Latitude [-90, 90].",
			 										VUnits.createUnit( "deg" ),
				 									dataset );
					 		variables.add( 0, timestamp );
					 		variables.add( 1, longitude );
					 		variables.add( 2, latitude );
					 		parseDataVariablesAndUnits( words, variables,
					 									dataset );
					 		
					 		if ( variables.size() == columns ) {
					 			dataset.setVariables( variables );
					 		} else {
					 			dataset = null;
					 		}
			 			}
					}
				}
			}
		//}

		if ( dataset == null ) {
			throw new IllegalArgumentException( fileName +
				" is not a valid tab-delimited ASCII Spreadsheet data file." );
		}
	}

	public Dataset getDataset() {
		final Dataset result = dataset;
		return result;
	}

	// Get a subset (by space-time) of data for a given variable:

    public DataFrame getValues( final SpreadsheetDataset dataset,
    							final List<AxisRange> ranges,
    							final Variable variable ) {
    	DataFrame result = null;
    	// Note this routine's code is copied from CSVReader.java.

    	final DataFrameBuilder builder = new DataFrameBuilder();
		builder.addDataset( dataset ).setVariable( variable );

		// Create default axes:

		{
		int index = 0;

		for ( CoordAxis axis : dataset.getCoordAxes().getAxes() ) {			
			builder.addAxis( DataFrameAxis.createDataFrameAxis( axis, index));
			++index;
		}
		}
		
/*
HACK: This loop causes a null pointer exception.

		for ( AxisRange axis : ranges ) {
			final anl.verdi.data.Range range = axis.getRange();
System.err.println( "ranges: axis = " + axis +
" axis.getAxis() = " + axis.getAxis() +
" axis.getOrigin() = " + axis.getOrigin() +
" axis.getExtent() = " + axis.getExtent() +
" range.getLowerBound() = " + range.getLowerBound() +
" range.getUpperBound() = " + range.getUpperBound() );

 			DataFrameAxis frameAxis =
				DataFrameAxis.createDataFrameAxis( axis.getAxis(),
						axis.getOrigin(), axis.getExtent(), 0 );
			builder.addAxis( frameAxis );

		}
*/

		// Create ucar.ma2.array from T,X,Y,Z data for this variable:

		final String requestedVariableName = variable.getName();
		final String variableName =
			requestedVariableName.equals( "LON" ) ? "longitude"
			: requestedVariableName.equals( "LAT" ) ? "latitude"
			: requestedVariableName;
		final int variableIndex = dataset.getVariableIndex( variableName );
		final int hours = dataset.getHours();
		final int hour0 = ranges.get( 0 ).getExtent();
		final int hour = hour0 < 0 ? 0 : hour0 >= hours ? hours - 1 : hour0;
		final int offset = dataset.getHourOffset( hour );
		final int pointsInHour = dataset.getPointsInHour( hour );
		ArrayDouble.D1 array = new ArrayDouble.D1( pointsInHour );

		for ( int point = 0; point < pointsInHour; ++point ) {
			final int index = offset + point;
			final double value =
				dataset.getValueForVariable( variableIndex, index );
			array.set( point, value );
		}

		builder.setArray( array );
		result = builder.createDataFrame();
    	return result;
    }

    private static void parseDataVariablesAndUnits( final String[] words,
    												List<Variable> variables,
    											SpreadsheetDataset dataset ) {
    	final int columns = words.length;

    	for ( int column = requiredColumns; column < columns; ++column ) {
    		final String word = words[ column ];
    		final int length = word.length();
    		final int left = word.indexOf( "(" );
    		final int right = word.indexOf( ")" );

    		if ( left > 0 && right > left && right <= length - 1 ) {
    			final String name = word.substring( 0, left );
    			final String unit = word.substring( left + 1, right );
    	 		final Variable variable =
    	 			new DefaultVariable( name, name,
    	 								VUnits.createUnit( unit ), dataset );
    	 		variables.add( column, variable );

    		}
    	}
    }

    private //static 
    Axes<CoordAxis> createAxes( final boolean withZ,
    											final int[] points,
    											final double data[][] ) {
    	final int count = withZ ? 3 : 2;
    	final List<CoordAxis> list = new ArrayList<CoordAxis>( count );

    	// Create time axis with hourly timestamps:

    	final int hours = points.length;
    	final Double[] timestamps = new Double[ hours ];
    	int row = 0;

    	for ( int hour = 0; hour < hours; row += points[ hour ], ++hour ) {
    		final long yyyymmddhh = (long) ( data[ 0 ][ row ] * 100.0 );
			final int yyyy = (int) ( yyyymmddhh / 1000000 );
			final int mm   = (int) ( yyyymmddhh / 10000 % 100 );
			final int dd   = (int) ( yyyymmddhh / 100 % 100 );
			final int hh   = (int) ( yyyymmddhh % 100 );
			final long offset = timeOffset( yyyy, mm, dd, hh );
    		timestamps[ hour ] = (double) offset;
    	}

    	list.add( new CSVTimeAxis( timestamps, "timestamp", "YYYYMMDD.HHMMSS(" + timezone + ")" ) ); //UTC)" ) );

    	// Create X-Coordinates from longitudes:

    	final int rows = data[ 0 ].length;
    	final Double[] longitudes = new Double[ rows ];

    	for ( row = 0; row < rows; ++row ) {
    		longitudes[ row ] = data[ 1 ][ row ];
    	}

    	list.add( new CSVCoordAxis( longitudes, "longitude", "(deg)", AxisType.X_AXIS ) );

    	// Create Y-Coordinates from latitudes:

    	final Double[] latitudes = new Double[ rows ];

    	for ( row = 0; row < rows; ++row ) {
    		latitudes[ row ] = data[ 2 ][ row ];
    	}

    	list.add( new CSVCoordAxis( latitudes, "latitude", "(deg)", AxisType.Y_AXIS ) );

    	if ( withZ ) { // Create Z-Coordinates from elevations:
        	final Double[] elevations = new Double[ rows ];

        	for ( row = 0; row < rows; ++row ) {
        		elevations[ row ] = data[ 3 ][ row ];
        	}

        	list.add( new CSVCoordAxis( elevations, "elevation", "(m)", AxisType.LAYER ) );

    	}

    	final Axes<CoordAxis> result = new Axes<CoordAxis>( list, new CSVBoxer() );
    	return result;
    }

    private static long timeOffset( int yyyy, int mm, int dd, int hh ) {
        final int zero_based_offset = 1; // UGLY: Only month is zero-based!
        final java.util.Calendar calendar = Calendar.getInstance();
        final java.util.TimeZone timeZone = TimeZone.getTimeZone( "GMT" );
        calendar.setTimeZone( timeZone );
        calendar.set( Calendar.ZONE_OFFSET, 0 );
        calendar.set( yyyy, mm - zero_based_offset, dd, hh, 0, 0 );
        final long result = calendar.getTimeInMillis();
    	return result;
    }

    // int result[ hours ] counts number of data points in each hour.

    private static int[] computePointsPerHour( final StringBuffer buffer ) {
      int[] result = null;
      final int hours = computeHours( buffer );

      if ( hours > 0 ) {
        final int bufferLength = buffer.length();
        final int length = 13; // "2006-07-03T18".length().
        int start = 0;
        String hour = "";
        boolean done = false;
        result = new int[ hours ];

        // Compute number of points in each hour:

        start = 0;
        hour = null;
        int index = 0;

        do {
          final int thisHourStart = buffer.indexOf( EOL, start ) + 1;
          done = thisHourStart <= 0 || thisHourStart + length >= bufferLength;
          
          if ( ! done ) {
            final String thisHour = // E.g., "2006-07-03T18".
              buffer.substring( thisHourStart, thisHourStart + length );

            if ( hour == null ) {
              hour = thisHour; // Initialize to first hour.
              result[ index ] = 1;
            } else if ( thisHour.equals( hour ) ) {
              result[ index ] += 1;
            } else { // New hour:
              ++index;
              result[ index ] = 1;
              hour = thisHour;
            }

            start = thisHourStart;
          }

        } while ( ! done );
      }

      return result;
    }


    private static int computeHours( final StringBuffer buffer ) {
      final int bufferLength = buffer.length();
      final int length = 13; // "2006-07-03T18".length().
      int result = 0;
      int start = 0;
      String hour = null;
      boolean done = false;

      do {
        final int thisHourStart = buffer.indexOf( EOL, start ) + 1;
        done = thisHourStart <= 0 || thisHourStart + length >= bufferLength;

        if ( ! done ) {
          final String thisHour = // E.g., "2006-07-03T18".
            buffer.substring( thisHourStart, thisHourStart + length );

          if ( hour == null || ! thisHour.equals( hour ) ) {
            ++result;
            hour = thisHour;
          }

          start = thisHourStart;
        }

      } while ( ! done );

      return result;
    }


    // double data[ columns ][ rows ] has values parsed.

    private static boolean parseData( final StringBuffer buffer,
                                      final double[][] data ) {
      final int bufferLength = buffer.length();
      final int columns = data.length;
      final int rows = data[ 0 ].length;
      int row = 0;
      int index = 0;

      while ( ( index = buffer.indexOf( EOL, index ) + 1 ) > 0 &&
              index < bufferLength &&
              buffer.charAt( index ) != '\0' ) {
        final int end = buffer.indexOf( EOL, index + 1 ) + 1;
        String line = null;

        if ( end > 0 ) {
          line = buffer.substring( index, end );
        } else {
          line = buffer.substring( index );
        }

        final String[] words = line.split( "\t" );

        if ( words.length == columns ) {
          final String timestamp = words[ 0 ];

          // First convert timestamp
          // E.g., 2006-07-03T18:15:37-0000 to 20060703.181537:

          if ( timestamp.length() == 24 && timestamp.endsWith( "-0000" ) ) {
            final int yyyy = Integer.valueOf( timestamp.substring( 0, 4 ) );
            final int mo = Integer.valueOf( timestamp.substring( 5, 7 ) );
            final int dd = Integer.valueOf( timestamp.substring( 8, 10 ) );
            final int hh = Integer.valueOf( timestamp.substring( 11, 13 ) );
            final int mm = Integer.valueOf( timestamp.substring( 14, 16 ) );
            final int ss = Integer.valueOf( timestamp.substring( 17, 19 ) );
            final double value =
              yyyy * 1e4 + mo * 1e2 + dd + hh * 1e-2 + mm * 1e-4 + ss * 1e-6;
            data[ 0 ][ row ] = value;

            for ( int column = 1; column < columns; ++column ) {
            	try {
                    data[ column ][ row ] = Double.valueOf( words[ column ] );
            	} catch (NumberFormatException ex) {
            		//do nothing for now, just leave as zero....since we don't 
            		//know how to parse alphanumeric strings...
            	}
            }
          }

          ++row;
        }
      }

      final boolean result = row == rows;
      return result;
    }



    private static int countLines( final StringBuffer buffer ) {
    	int result = 0;
    	int index = 0;

    	while ( ( index = buffer.indexOf( EOL, index ) + 1 ) > 0 ) {
    		++result;
    	}

    	return result;
    }


    private static StringBuffer readFile( final String name ) {
		StringBuffer result = null;
		final int totalBytesToRead = fileSize( name );

		if ( totalBytesToRead > 0 ) {
			RandomAccessFile file = null;
			byte[] readBuffer = null;

			try {
				readBuffer = new byte[ totalBytesToRead ];
				file = new RandomAccessFile( name, "r" );
				int totalBytesRead = 0;
			    int bytesRead = 0;

				do {
					final int bytesToReadNow = totalBytesToRead-totalBytesRead;
			        bytesRead =
			        	file.read( readBuffer, totalBytesRead, bytesToReadNow );
					totalBytesRead += bytesRead;
				} while ( bytesRead > 0 && totalBytesRead < totalBytesToRead );

				if ( totalBytesRead == totalBytesToRead ) {

					// Filter any DOS control-M characters:

					int w = 0;

					for ( int r = 0; r < totalBytesRead; ++r ) {
						final byte c = readBuffer[ r ];

						if ( c != '\r' ) {
							readBuffer[ w ] = c;
							++w;
						}
					}

					result = new StringBuffer( new String( readBuffer, 0, w ) );
				}

			} catch ( Exception unused ) {
			} finally {
				try { if ( file != null ) file.close();} catch (Exception e) {}
				readBuffer = null;
			}
		}

		return result;
    }


    private static int fileSize( final String name ) {
    	int result = 0;
    	final File file = new File( name );

        if ( file.exists() && file.isFile() && file.canRead() ) {
        	final long length = file.length();

        	if ( length > 0 && length < 2147483647 ) {
        		result = (int) length;
        	}
        }

    	return result;
    }

}



