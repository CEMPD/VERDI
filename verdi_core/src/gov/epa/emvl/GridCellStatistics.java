
/******************************************************************************
PURPOSE: GridCellStatistics.java - Computes a set of statistics
         e.g., mean, hours-of-non-compliance, etc. for a data variable on a
         layer over various timesteps.
NOTES:   
HISTORY: 2010-06-18 plessel.todd@epa.gov, Created.
STATUS: unreviewed, tested.
*****************************************************************************/

package gov.epa.emvl;

//import anl.verdi.core.VerdiConstants;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.data.DataUtilities;

public final class GridCellStatistics {
	static final Logger Logger = LogManager.getLogger(GridCellStatistics.class.getName());
  
  private GridCellStatistics() {} // Non-instantiable.

  public static final int MINIMUM                 = 0;
  public static final int MAXIMUM                 = 1;
  public static final int MEAN                    = 2;
  public static final int GEOMETRIC_MEAN          = 3;
  public static final int MEDIAN                  = 4;
  public static final int FIRST_QUARTILE          = 5;
  public static final int THIRD_QUARTILE          = 6;
  public static final int VARIANCE                = 7;
  public static final int STANDARD_DEVIATION      = 8;
  public static final int COEFFICIENT_OF_VARIANCE = 9;
  public static final int RANGE                   = 10;
  public static final int INTERQUARTILE_RANGE     = 11;
  public static final int SUM                     = 12;
  public static final int TIMESTEP_OF_MINIMUM     = 13;
  public static final int TIMESTEP_OF_MAXIMUM     = 14;
  public static final int HOURS_OF_NON_COMPLIANCE = 15;
  public static final int MAXIMUM_8HOUR_MEAN      = 16;
  public static final int COUNT                   = 17;
  public static final int FOURTH_MAX              = 18;
  public static final int STATISTICS              = 20;

  public static final float BADVAL3 = (float)DataUtilities.BADVAL3;
  public static final float AMISS3  = (float)DataUtilities.AMISS3;

  // If units is null then the units for the variable are used:

  private static final int NAME = 0;
  private static final int UNITS = 1;
  private static final int SHORT_NAME = 2;

  private static final String[][] statisticNamesAndUnits = {
    { "minimum",                  null,    "min"       },
    { "maximum",                  null,    "max"       },
    { "mean",                     null,    "mean"      },
    { "geometric_mean",           null,    "gmean"     },
    { "median",                   null,    "med"       },
    { "first_quartile",           null,    "25th"      },
    { "third_quartile",           null,    "75th"      },
    { "variance",                 null,    "var"       },
    { "standard_deviation",       null,    "std"       },
    { "coefficient_of_variance",  "%",     "cov"       },
    { "range",                    null,    "range"     },
    { "interquartile_range",      null,    "iqran"     },
    { "sum",                      null,    "sum"       },
    { "timestep_of_minimum",      "#",     "tmin"      },
    { "timestep_of_maximum",      "#",     "tmax"      },
    { "hours_of_non_compliance",  "hours", "honc"      },
    { "maximum_8hour_mean",       null,    "max8hrAve" },
    { "count",                    "#",     "count"     },
    { "fourth_max",               null,    "4thMax"    }
  };

  public static String name( final int statistic ) {
    return statisticNamesAndUnits[ statistic ][ NAME ];
  }

  public static String units( final int statistic ) {
    return statisticNamesAndUnits[ statistic ][ UNITS ];
  }

  public static String shortName( final int statistic ) {
    return statisticNamesAndUnits[ statistic ][ SHORT_NAME ];
  }

  /**
   * PURPOSE: computeStatistics - For each cell, compute a set of
   *          time-aggregate statistics.
   * INPUTS:  final float[][][] data   data[ rows ][ columns ][ timesteps ]
   *          double threshold         Exceedance threshold, e.g., 0.12 ppb.
   *          double hoursPerTimestep  Timestep size in decimal hours.
   * OUTPUTS: final float[][][] data         data sorted across timesteps.
   *          final float[][][] statistics   Time-aggregated statistics.
   *          statistics[ STATISTICS ][ rows ][ columns ]
 * @throws Exception 
   **/

  public static void computeStatistics( final float[][][] data,
                                        final double threshold,
                                        final double hoursPerTimestep,
                                        final float[][][] statistics,
                                        final int statIndex) throws Exception {
	  
	  boolean separate = true;
	  //Logger.debug( "statIndex = " + statIndex);
	  
	  if ( separate) {
		  if ( statIndex == 13 || statIndex == 14) {
			  computeTimestepsOfExtrema( data,
					  statistics[ TIMESTEP_OF_MINIMUM ],
					  statistics[ TIMESTEP_OF_MAXIMUM ] );
			  
//			  for ( int i = 0; i<statistics[ TIMESTEP_OF_MINIMUM ].length; i++) {
//				  for ( int j = 0; j<statistics[ TIMESTEP_OF_MINIMUM ][0].length; j++) {
//					  System.out.print(statistics[TIMESTEP_OF_MINIMUM][i][j]+" ");
//				  }
//				  Logger.debug("\n");
//			  }	
			  
			  return;
		  }
		  
		  if ( statIndex == 16 ){
			  computeMaximum8HourMeans( data, hoursPerTimestep,
					  statistics[ MAXIMUM_8HOUR_MEAN ] );
			  return;
		  }

		  sortDataAcrossTimesteps( data ); // Required for quartiles.	  
		  computeValidTimesteps( data, statistics[ COUNT ] );

		  if ( statIndex == 0 || statIndex == 1 || statIndex == 4 ||
				  statIndex == 5 || statIndex == 6 ||
				  statIndex == 10 || statIndex == 11 || 
				  statIndex == 17 || statIndex == 18) {
			  computeQuartiles( data,
					  statistics[ COUNT ],
					  statistics[ MINIMUM ],
					  statistics[ MAXIMUM ],
					  statistics[ MEDIAN ],
					  statistics[ FIRST_QUARTILE ],
					  statistics[ THIRD_QUARTILE ],
					  statistics[ RANGE ],
					  statistics[ INTERQUARTILE_RANGE ],
					  statistics[ FOURTH_MAX ]);
			  return;
		  }

		  if ( statIndex == 12 ) {
			  computeSums( data, statistics[ COUNT ], statistics[ SUM ] );
			  
//			  for ( int i = 0; i<statistics[ SUM ].length; i++) {
//				  for ( int j = 0; j<statistics[ SUM ][0].length; j++) {
//					  System.out.print(statistics[SUM][i][j]+" ");
//				  }
//				  Logger.debug("\n");
//			  }
			  
			  return;
		  }

		  if ( statIndex == 2) {
			  computeSums( data, statistics[ COUNT ], statistics[ SUM ] );
			  computeMeans( statistics[ COUNT ],
					  statistics[ SUM ],
					  statistics[ MEAN ] );
			  // JEB 2016 WHY NO return; FOR THIS "ELSE"?
		  }
		  

		  if ( statIndex == 3) {
			  computeGeometricMeans( data,
					  statistics[ COUNT ],
					  statistics[ GEOMETRIC_MEAN ] );
			  return;
		  }

		  if ( statIndex == 7 || statIndex == 8 || statIndex == 9) {
			  computeSums( data, statistics[ COUNT ], statistics[ SUM ] );
			  computeMeans( statistics[ COUNT ],
					  statistics[ SUM ],
					  statistics[ MEAN ] );
			  computeVariance( data,
					  statistics[ COUNT ],
					  statistics[ MEAN ],
					  statistics[ VARIANCE ],
					  statistics[ STANDARD_DEVIATION ],
					  statistics[ COEFFICIENT_OF_VARIANCE ] );
			  return;
		  }

		  if ( statIndex == 15) {
			  computeHoursOfNonCompliance( data,
					  statistics[ COUNT ],
					  hoursPerTimestep,
					  threshold,
					  statistics[ HOURS_OF_NON_COMPLIANCE ] );
			  return;
		  }
	  } else {
	  
	  //////////////////////////////////////////////////////////////////////////
  
		  computeTimestepsOfExtrema( data,
				  statistics[ TIMESTEP_OF_MINIMUM ],
				  statistics[ TIMESTEP_OF_MAXIMUM ] );

		  computeMaximum8HourMeans( data, hoursPerTimestep,
				  statistics[ MAXIMUM_8HOUR_MEAN ] );

		  sortDataAcrossTimesteps( data ); // Required for quartiles.

		  // For each data value (cell/site), count the number of valid timesteps:

		  computeValidTimesteps( data, statistics[ COUNT ] );

		  computeQuartiles( data,
				  statistics[ COUNT ],
				  statistics[ MINIMUM ],
				  statistics[ MAXIMUM ],
				  statistics[ MEDIAN ],
				  statistics[ FIRST_QUARTILE ],
				  statistics[ THIRD_QUARTILE ],
				  statistics[ RANGE ],
				  statistics[ INTERQUARTILE_RANGE ],
				  statistics[ FOURTH_MAX ]);

		  computeSums( data, statistics[ COUNT ], statistics[ SUM ] );

		  computeMeans( statistics[ COUNT ],
				  statistics[ SUM ],
				  statistics[ MEAN ] );

		  computeGeometricMeans( data,
				  statistics[ COUNT ],
				  statistics[ GEOMETRIC_MEAN ] );

		  computeVariance( data,
				  statistics[ COUNT ],
				  statistics[ MEAN ],
				  statistics[ VARIANCE ],
				  statistics[ STANDARD_DEVIATION ],
				  statistics[ COEFFICIENT_OF_VARIANCE ] );

		  computeHoursOfNonCompliance( data,
				  statistics[ COUNT ],
				  hoursPerTimestep,
				  threshold,
				  statistics[ HOURS_OF_NON_COMPLIANCE ] );
	  
	  /////////////////////////////////////////////////////////////////////////////////
	  }
  }

  /**
   * PURPOSE: computeTimestepsOfExtrema - For each cell, compute the
   *          1-based timestep of the minimum and the timestep of the maximum.
   * INPUTS:  final float[][][] data        data[ rows ][ columns][ timesteps ]
   * OUTPUTS: float[][] timestepOfMinimum   timestepOfMinimum[ rows ][ columns]
   *                                        For each cell,
   *                                        this is the timestep number
   *                                        (whole, 1-based) of its minimum
   *                                        value over all of the timesteps.
   *          float[][] timestepOfMaximum   timestepOfMaximum[ rows ][ columns]
   *                                        For each cell,
   *                                        this is the timestep number
   *                                        (whole, 1-based) of its maximum
   *                                        value over all of the timesteps.
   **/

  private static void computeTimestepsOfExtrema( final float[][][] data,
                                                 float[][] timestepOfMinimum,
                                                 float[][] timestepOfMaximum) {

    final int rows    = data.length;
    final int columns = data[ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        timestepOfMinimum[ row ][ column ] =
          indexOfMinimum( data[ row ][ column ] ) + 1;
        timestepOfMaximum[ row ][ column ] =
          indexOfMaximum( data[ row ][ column ] ) + 1;
      }
    }
  }

  /**
   * PURPOSE: computeMaximum8HourMeans - For each cell, compute the
   *          1-based timestep of the minimum and the timestep of the maximum.
   * INPUTS:  final float[][][] data      data[ rows ][ columns][ timesteps ]
   *          double hoursPerTimestep  Timestep size in decimal hours.
   * OUTPUTS: float[][] maximum8HourMean  maximum8HourMean[ rows ][ columns]
   *                                      For each cell,
   *                                      this is the maximum of the means
   *                                      of each moving 8-hour set of values.
   **/

  private static void computeMaximum8HourMeans( final float[][][] data,
                                                final double hoursPerTimestep,
                                                float[][] maximum8HourMean ) {

    final int rows    = data.length;
    final int columns = data[ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        maximum8HourMean[ row ][ column ] =
          maximum8HourMeans( data[ row ][ column ], hoursPerTimestep );
      }
    }
  }

  /**
   * PURPOSE: sortDataAcrossTimesteps - For each cell, sort its timestep data.
   * INPUTS:  final float[][][] data  data[ rows ][ columns][ timesteps ].
   * OUTPUTS: final float[][][] data  data sorted over timesteps.
   **/

  private static void sortDataAcrossTimesteps( final float[][][] data  ) {

    final int rows    = data.length;
    final int columns = data[ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        java.util.Arrays.sort( data[ row ][ column ] );
      }
    }
  }

  /**
   * PURPOSE: sortDataAcrossTimesteps - For each cell, compute the number of
   *          valid (i.e., not marked as "missing/bad") timestep values.
   * INPUTS:  final float[][][] data  data[ rows ][ columns][ timesteps ].
   * OUTPUTS: float[][][] count       count[ rows ][ columns ] valid values.
   **/

  private static void computeValidTimesteps( final float data[][][],
                                             float[][] count ) {

    final int rows    = data.length;
    final int columns = data[ 0 ].length;
    final int timesteps = data[ 0 ][ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        final int firstIndex = firstValidIndex( data[ row ][ column ] );

        if ( firstIndex == -1 ) {
          count[ row ][ column ] = 0;
        } else {
          final int validCount = timesteps - firstIndex;
          count[ row ][ column ] = validCount;
        }
      }
    }
  }

  /**
   * PURPOSE: computeQuartiles - For each cell, compute the minimum, maximum,
   *          median, first quartile, third quartile and range
   *          over all timestep values.
   * INPUTS:  final float[][][] data   data[ rows ][ columns][ timesteps ].
   *          final float[][][] count  count[ rows ][ columns] valid values.
   * OUTPUTS: float[][] minimum        minimum[ rows ][ columns ].
   *          float[][] maximum        maximum[ rows ][ columns ].
   *          float[][] median         median[ rows ][ columns ]
   *                                   middle of sorted sequence of values.
   *          float[][] firstQuartile  firstQuartile[ rows ][ columns ]
   *                                   25th percentile of sorted of values.
   *          float[][] thirdQuartile  thirdQuartile[ rows ][ columns ]
   *                                   75th percentile of sorted of values.
   *          float[][] range          range[ rows ][ columns ]
   *                                   maximum - minimum.
   *          float[][] interquartileRange
   *                                   interquartileRange[ rows ][ columns ]
   *                                   75th - 25th.
   **/

  private static void computeQuartiles( final float[][][] data,
                                        final float[][] count,
                                        final float[][] minimum,
                                        final float[][] maximum,
                                        final float[][] median,
                                        final float[][] firstQuartile,
                                        final float[][] thirdQuartile,
                                        final float[][] range,
                                        final float[][] interquartileRange,
                                        final float[][] fourthMax) {
    final int rows    = data.length;
    final int columns = data[ 0 ].length;
    final int timesteps = data[ 0 ][ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        final int numberOfValidTimesteps =
          (int) ( count[ row ][ column ] + 0.5 );

        if ( numberOfValidTimesteps == 0 ) {
          minimum[            row ][ column ] = BADVAL3;
          maximum[            row ][ column ] = BADVAL3;
          range[              row ][ column ] = BADVAL3;
          median[             row ][ column ] = BADVAL3;
          firstQuartile[      row ][ column ] = BADVAL3;
          thirdQuartile[      row ][ column ] = BADVAL3;
          interquartileRange[ row ][ column ] = BADVAL3;
          fourthMax[          row ][ column ] = BADVAL3;
        } else {
          final int index    = timesteps - numberOfValidTimesteps;
          final int last     = numberOfValidTimesteps - 1;
          final int middle   = numberOfValidTimesteps / 2;
          final int middle_1 = middle > 0 ? middle - 1 : 0;
          final int lower    = numberOfValidTimesteps / 4;
          final int lower_1  = lower > 0 ? lower - 1 : 0;
          final int upper    = numberOfValidTimesteps * 3 / 4;
          final int upper_1  = upper > 0 ? upper - 1 : 0;
          final boolean oddNumberOfTimesteps = numberOfValidTimesteps % 2 != 0;
          final float firstValue  = data[ row ][ column ][ index          ];
          final float lastValue   = data[ row ][ column ][ index + last   ];

          int fourthMaxIndex = index + last - 3;         
          float fourthMaxValue = BADVAL3;
          if (fourthMaxIndex < data[row][column].length && fourthMaxIndex >= 0)
        		  fourthMaxValue = data[ row ][ column ][ fourthMaxIndex  ];
          final float middleValue = data[ row ][ column ][ index + middle ];
          final float lowerValue  = data[ row ][ column ][ index + lower  ];
          final float upperValue  = data[ row ][ column ][ index + upper  ];

          minimum[ row ][ column ] = firstValue;
          maximum[ row ][ column ] = lastValue;
          fourthMax[ row ][ column ] = fourthMaxValue;
          range[   row ][ column ] = lastValue - firstValue;

          if ( oddNumberOfTimesteps ) {
            median[             row ][ column ] = middleValue;
            firstQuartile[      row ][ column ] = lowerValue;
            thirdQuartile[      row ][ column ] = upperValue;
            interquartileRange[ row ][ column ] = upperValue - lowerValue;
          } else {
            final float averageLowerValue =
              ( lowerValue + data[ row ][ column ][ index + lower_1 ] ) * 0.5f;
            final float averageUpperValue =
              ( upperValue + data[ row ][ column ][ index + upper_1 ] ) * 0.5f;

            median[             row ][ column ] =
              ( middleValue + data[ row ][ column ][ index + middle_1]) * 0.5f;
            firstQuartile[      row ][ column ] = averageLowerValue;
            thirdQuartile[      row ][ column ] = averageUpperValue;
            interquartileRange[ row ][ column ] =
              averageUpperValue - averageLowerValue;

          }
        }
      }
    }
  }

  /**
   * PURPOSE: computeSums - For each cell, compute the sum over timesteps.
   * INPUTS:  final float[][][] data  data[ rows ][ columns][ timesteps ].
   *          float[][] count         count[ rows ][ columns ] valid values.
   * OUTPUTS: float[][] sum           sum[ rows ][ columns ].
   **/

  private static void computeSums( final float[][][] data,
                                   final float[][] count,
                                   float[][] sum ) {

    final int rows    = data.length;
    final int columns = data[ 0 ].length;
    final int timesteps = data[ 0 ][ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        final int numberOfValidTimesteps =
          (int) ( count[ row ][ column ] + 0.5 );

        if ( numberOfValidTimesteps == 0 ) {
          sum[ row ][ column ] = BADVAL3;
        } else {
          final int index = timesteps - numberOfValidTimesteps;
          sum[ row ][ column ] = (float)
            sumf( data[ row ][ column ], index );
        }
      }
    }
  }

  /**
   * PURPOSE: computeMeans - For each cell, compute the mean over timesteps.
   * INPUTS:  final float[][] count   count[ rows ][ columns ] valid values.
   *          final float[][] sum     sum[ rows ][ columns ] summed values.
   * OUTPUTS: float[][] mean          mean[ rows ][ columns ].
   **/

  private static void computeMeans( final float[][] count,
                                    final float[][] sum,
                                    float[][] mean ) {

    final int rows    = count.length;
    final int columns = count[ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        final int numberOfValidTimesteps =
          (int) ( count[ row ][ column ] + 0.5 );

        if ( numberOfValidTimesteps == 0 ) {
          mean[ row ][ column ] = BADVAL3;
        } else {
          mean[ row ][ column ] =
            sum[ row ][ column ] / numberOfValidTimesteps;
        }
      }
    }
  }

  /**
   * PURPOSE: computeGeometricMeans - For each cell, compute the geometric
   *          mean over timesteps.
   * INPUTS:  final float[][][] data  data[ rows ][ columns][ timesteps ].
   *          final float[][] count   count[ rows ][ columns ] valid values.
   * OUTPUTS: float[][] mean          mean[ rows ][ columns ].
 * @throws Exception 
   **/

  private static void computeGeometricMeans( final float[][][] data,
                                             final float[][] count,
                                             float[][] geometricMean ) throws Exception {

    final int rows      = data.length;
    final int columns   = data[ 0 ].length;
    final int timesteps = data[ 0 ][ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        final int numberOfValidTimesteps =
          (int) ( count[ row ][ column ] + 0.5 );

        if ( numberOfValidTimesteps == 0 ) {
          geometricMean[ row ][ column ] = BADVAL3;
        } else {
          final int index = timesteps - numberOfValidTimesteps;
          geometricMean[ row ][ column ] = (float)
            geometricMeanf( data[ row ][ column ], index );
        }
      }
    }
  }

  /**
   * PURPOSE: computeVariance - For each cell, compute the variance, standard
   *          deviation and the coefficient of variance over timesteps.
   * INPUTS:  final float[][][] data  data[ rows ][ columns][ timesteps ].
   *          final float[][] count   count[ rows ][ columns ] valid values.
   *          final float[][] mean    mean[ rows ][ columns ] mean values.
   * OUTPUTS: float[][] variance      variance[ rows ][ columns ].
   *          float[][] standardDeviation      std[ rows ][ columns ].
   *          float[][] coefficientsOfVariance cov[ rows ][ columns ].
   **/

  private static void computeVariance( final float[][][] data,
                                       final float[][] count,
                                       final float[][] mean,
                                       float[][] variance,
                                       float[][] standardDeviation,
                                       float[][] coefficientsOfVariance ) {

    final int rows      = data.length;
    final int columns   = data[ 0 ].length;
    final int timesteps = data[ 0 ][ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        final int numberOfValidTimesteps =
          (int) ( count[ row ][ column ] + 0.5 );

        if ( numberOfValidTimesteps == 0 ) {
          variance[ row ][ column ] = BADVAL3;
          standardDeviation[ row ][ column ] = BADVAL3;
          coefficientsOfVariance[ row ][ column ] = BADVAL3;
        } else {
          final int index = timesteps - numberOfValidTimesteps;
          final float meanValue = mean[ row ][ column ];
          final float s2 = (float)
            variancef( data[ row ][ column ], index, meanValue );
          final float std = (float) java.lang.Math.sqrt( s2 );
          final float cv = meanValue > 0.0f ? 100.0f * std / meanValue : 0.0f;
          variance[ row ][ column ] = s2;
          standardDeviation[ row ][ column ] = std;
          coefficientsOfVariance[ row ][ column ] = cv;
        }
      }
    }
  }

  /**
   * PURPOSE: computeHoursOfNonCompliance - For each cell, compute the hours
   *          of non-compliance (threshold exceedance) of the time series data.
   * INPUTS:  final float[][][] data  data[ rows ][ columns][ timesteps ].
   *          final float[][] count   count[ rows ][ columns ] valid values.
   *          final double hoursPerTimestep  Timestep size in decimal hours.
   *          final double threshold         Exceedance value to compare to.
   * OUTPUTS: float[][] hoursOfNonCompliance honc[ rows ][ columns ].
   **/

  private static void computeHoursOfNonCompliance( final float[][][] data,
                                                  final float[][] count,
                                                 final double hoursPerTimestep,
                                                  final double threshold,
                                            float[][] hoursOfNonCompliance ) {

    final int rows      = data.length;
    final int columns   = data[ 0 ].length;
    final int timesteps = data[ 0 ][ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        final int numberOfValidTimesteps =
          (int) ( count[ row ][ column ] + 0.5 );

        if ( numberOfValidTimesteps == 0 ) {
          hoursOfNonCompliance[ row ][ column ] = BADVAL3;
        } else {
          final int index = timesteps - numberOfValidTimesteps;
          final int numberOfTimestepsExceedingThreshhold =
            exceedance( data[ row ][ column ], index, threshold );
          hoursOfNonCompliance[ row ][ column ] = (float)
            ( numberOfTimestepsExceedingThreshhold * hoursPerTimestep );
        }
      }
    }
  }

  /**
   * PURPOSE: indexOfMinimum - Compute the index of the minimum valid value.
   * INPUTS:  final float[] data  Data to compare.
   * RETURNS: int 0-based index into data[] or -1 if all invalid.
   **/

  private static int indexOfMinimum( final float[] data ) {

    final int count = data.length;
    float minimum = 0.0f;
    int result = -1;

    for ( int index = 0; index < count; ++index ) {
      final float value = data[ index ];

      if ( value > AMISS3 ) {

        if ( result == -1 || value < minimum ) {
          minimum = value;
          result = index;
        }
      }
    }

    return result;
  }

  /**
   * PURPOSE: indexOfMaximum - Compute the index of the maximum valid value.
   * INPUTS:  final float[] data  Data to compare.
   * RETURNS: int 0-based index into data[] or -1 if all invalid.
   **/

  private static int indexOfMaximum( final float[] data ) {

    final int count = data.length;
    float maximum = 0.0f;
    int result = -1;

    for ( int index = 0; index < count; ++index ) {
      final float value = data[ index ];

      if ( value > AMISS3 ) {

        if ( result == -1 || value > maximum ) {
          maximum = value;
          result = index;
        }
      }
    }

    return result;
  }

  /**
   * PURPOSE: maximum8HourMeans - Compute the maximum of a set of 8-hour means.
   * INPUTS:  final float[] data       Data to compare.
   *          double hoursPerTimestep  Decimal hours per timestep.
   * RETURNS: float maximum of a set of 8-hour means.
   **/

  private static float maximum8HourMeans( final float[] data,
                                          final double hoursPerTimestep ) {

    final int count = (int) ( 8.0 / hoursPerTimestep + 0.5 );
    final int timesteps = data.length - count;
    float result = BADVAL3;

    for ( int timestep = 0; timestep <= timesteps; ++timestep ) {
      final int end = timestep + count;
      final float windowMean = mean( data, timestep, end );

      if ( windowMean > result ) {
        result = windowMean;
      }
    }

    return result;
  }

  /**
   * PURPOSE: mean - Compute the mean of a set of valid values from
   *                 data[ first ] .. data[ end - 1 ].
   * INPUTS:  final float[] data  Data to compare.
   * RETURNS: float mean or BADVAL3 if all are invalid.
   **/

  private static float mean( final float[] data,
                             final int first, final int end ) {

    float result = BADVAL3;
    int count = 0;

    for ( int index = first; index < end; ++index ) {
      final float value = data[ index ];

      if ( value > AMISS3 ) {
        
        if ( result <= BADVAL3 || result >= DataUtilities.NC_FILL_FLOAT) {
          result = value;
          count = 1;
        } else {
          final int count1 = count + 1;
          result = ( count * result + value ) / count1;
          count = count1;
        }
      }
    }

    return result;
  }

  /**
   * PURPOSE: firstValidIndex - Compute the index of the first valid value.
   * INPUTS:  final float[] data  Data to compare.
   * RETURNS: int 0-based index into data[] or -1 if all invalid.
   **/

  private static int firstValidIndex( final float[] data ) {

    final int count = data.length;
    int result = -1;

    for ( int index = 0; index < count; ++index ) {
      final float value = data[ index ];
      
      if ( value > AMISS3 ) {
        result = index;
        index = count;
      }
    }

    return result;
  }

  /**
   * PURPOSE: sumf - Kahan Sum an array of floats.
   * INPUTS:  final float[] data  Data to sum.
   *          final int first     Starting index into data[].
   * RETURNS: double sum of valid values.
   * NOTES:   Based on version published in: "Floating-Point Summation"
   *          C/C++ Users Journal Sept 1996, pp 51-55, by Evan Manning.
   **/

  private static double sumf( final float[] data, final int first ) {

    final int count = data.length;
    double sum        = 0.0; // The sum of the values in the array.
    double correction = 0.0; // Kahan corrector subtracts each round-off error

    for ( int index = first; index < count; ++index ) {
      final double nextTerm = data[ index ];
      final double correctedNextTerm = nextTerm - correction;
      final double newSum = sum + correctedNextTerm;
      correction = ( newSum - sum ) - correctedNextTerm;
      sum = newSum;
    }

    return sum;
  }

  /**
   * PURPOSE: geometricMeanf - Compute the geometric mean of data[].
   * INPUTS:  final float[] data  Data to read.
   *          final int first     Starting index into data[].
   * RETURNS: double geometric mean of valid values.
   * @throws Exception 
   **/

  private static double geometricMeanf( final float[] data, final int first ) throws Exception {

    final int count = data.length;
    final int numberOfValues = count - first;
    final double root    = numberOfValues > 0 ? 1.0 / numberOfValues : 0.0;
    double geometricMean = numberOfValues > 0 ? 1.0 : 0.0;

    for ( int index = first; index < numberOfValues; ++index ) {
      final double nextTerm = data[ index ];
      geometricMean *= java.lang.Math.pow( nextTerm, root );
    }
    
    if ( Double.isNaN(geometricMean)){
    	//throw new Exception("Geometric Mean is not a number");
    }

    return geometricMean;
  }

  /**
   * PURPOSE: variancef - Compute the variance of data[].
   * INPUTS:  final float[] data  Data to read.
   *          final int first     Starting index into data[].
   *          final double mean   mean of the data.
   * RETURNS: double variance of the data from its mean.
   * NOTES:   Based on Kahan summation.
   **/

  private static double variancef( final float[] data, final int first,
                                   final double mean ) {

    final int count = data.length;
    final int numberOfValues = count - first;
    double s2         = 0.0; // The variance.
    double sum        = 0.0; // The sum of the squares of the differences of
                             // each data value and the mean.
    double correction = 0.0; // Kahan corrector subtracts each round-off error

    for ( int index = first; index < count; ++index ) {
      final double value             = data[ index ];
      final double meanDifference    = value - mean;
      final double nextTerm          = meanDifference * meanDifference;
      final double correctedNextTerm = nextTerm - correction;
      final double newSum            = sum + correctedNextTerm;
      correction = ( newSum - sum ) - correctedNextTerm;
      sum = newSum;
    }

    if ( numberOfValues > 1 ) {
      s2 = sum / ( numberOfValues - 1 );
    }
    return s2;
  }

  /**
   * PURPOSE: exceedance - Compute the number of values in data[] that are
   *          greater than threshold.
   * INPUTS:  final float[] data  Data to read.
   *          final int first     Starting index into data[].
   *          final double threshold  Threshold to compare to.
   * RETURNS: int the number of data values greater than threshold.
   **/

  private static int exceedance( final float[] data, final int first,
                                 final double threshold ) {
    final int count = data.length;
    int result = 0;

    for ( int index = first; index < count; ++index ) {
      
      if ( data[ index ] > threshold ) {
        ++result;
      }
    }

    return result;
  }
};