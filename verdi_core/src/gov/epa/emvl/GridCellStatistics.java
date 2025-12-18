
/******************************************************************************
PURPOSE: GridCellStatistics.java - Computes a set of statistics
         e.g., mean, hours-of-non-compliance, etc. for a data variable on a
         layer over various timesteps.
NOTES:   
HISTORY: 2010-06-18 plessel.todd@epa.gov, Created.
STATUS: unreviewed, tested.
*****************************************************************************/

package gov.epa.emvl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.lang3.text.WordUtils;
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
  public static final int CUSTOM_PERCENTILE       = 19;
  public static final int LAYER_MEAN              = 20;
  public static final int LAYER_SUM               = 21;
  public static final int STATISTICS              = 23;

  public static final double BADVAL3 = (double)DataUtilities.BADVAL3;
  public static final double AMISS3  = (double)DataUtilities.AMISS3;

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
    { "fourth_max",               null,    "4thMax"    },
    { "custom_percentile",        null,    "custPer"   },
    { "layer_mean",               null,    "mean"      },
    { "layer_sum",                null,    "sum"       }
  };
  
  public static String getDisplayString(int selectedStat, double percentile, int firstLayer, int lastLayer) {
	  --selectedStat;
	  if (selectedStat < 0 || selectedStat >= statisticNamesAndUnits.length)
		  return null;
	  String stat = WordUtils.capitalizeFully(statisticNamesAndUnits[selectedStat][0].replace("_"," "));
	  if (selectedStat == 19)
		  stat += " (" + Math.round(percentile) + ") ";
	  else if (selectedStat == 20 || selectedStat == 21)
		  stat += " (" + (firstLayer + 1) + "-" + (lastLayer + 1) + ")";
	  return stat;
  }

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
   * INPUTS:  final double[][][] data   data[ rows ][ columns ][ timesteps ]
   *          double threshold         Exceedance threshold, e.g., 0.12 ppb.
   *          double hoursPerTimestep  Timestep size in decimal hours.
   * OUTPUTS: final double[][][] data         data sorted across timesteps.
   *          final double[][][] statistics   Time-aggregated statistics.
   *          statistics[ TIMESTEP] [ STATISTICS ][ rows ][ columns ]
 * @throws Exception 
   **/

  public static void computeStatistics( int layer, int firstLayer, int lastLayer, int timestep,
		                                final double[][][][] data,
                                        final double threshold,
                                        final double hoursPerTimestep,
                                        final double[][][][] statistics,
                                        final int statIndex,
                                        final double customPercentileValue) throws Exception {
	  
	  boolean separate = true;
	  //Logger.debug( "statIndex = " + statIndex);
	  
	  if ( separate) {
		  if (statIndex == 20 || statIndex == 21) {
			  computeLayers(timestep, firstLayer, lastLayer, data,
					  statistics);
			  return;
		  }
		  if ( statIndex == 13 || statIndex == 14) {
			  computeTimestepsOfExtrema( layer, data,
					  statistics[0][ TIMESTEP_OF_MINIMUM ],
					  statistics[0][ TIMESTEP_OF_MAXIMUM ] );
			  
//			  for ( int i = 0; i<statistics[0][ TIMESTEP_OF_MINIMUM ].length; i++) {
//				  for ( int j = 0; j<statistics[0][ TIMESTEP_OF_MINIMUM ][0].length; j++) {
//					  System.out.print(statistics[TIMESTEP_OF_MINIMUM][i][j]+" ");
//				  }
//				  Logger.debug("\n");
//			  }	
			  
			  return;
		  }
		  
		  if ( statIndex == 16 ){
			  computeMaximum8HourMeans( layer, data, hoursPerTimestep,
					  statistics[0][ MAXIMUM_8HOUR_MEAN ] );
			  return;
		  }

		  sortDataAcrossTimesteps( layer, data ); // Required for quartiles.	  
		  computeValidTimesteps( layer, data, statistics[0][ COUNT ] );

		  if ( statIndex == 0 || statIndex == 1 || statIndex == 4 ||
				  statIndex == 5 || statIndex == 6 ||
				  statIndex == 10 || statIndex == 11 || 
				  statIndex == 17 || statIndex == 18 || statIndex == 19) {
			  computeQuartiles( layer, data,
					  statistics[0][ COUNT ],
					  statistics[0][ MINIMUM ],
					  statistics[0][ MAXIMUM ],
					  statistics[0][ MEDIAN ],
					  statistics[0][ FIRST_QUARTILE ],
					  statistics[0][ THIRD_QUARTILE ],
					  statistics[0][ RANGE ],
					  statistics[0][ INTERQUARTILE_RANGE ],
					  statistics[0][ FOURTH_MAX ],
					  statistics[0][ CUSTOM_PERCENTILE ],
					  customPercentileValue);
			  return;
		  }

		  if ( statIndex == 12 ) {
			  computeSums( layer, data, statistics[0][ COUNT ], statistics[0][ SUM ] );
			  
//			  for ( int i = 0; i<statistics[][ SUM ].length; i++) {
//				  for ( int j = 0; j<statistics[][ SUM ][0].length; j++) {
//					  System.out.print(statistics[][SUM][i][j]+" ");
//				  }
//				  Logger.debug("\n");
//			  }
			  
			  return;
		  }

		  if ( statIndex == 2) {
			  computeSums( layer, data, statistics[0][ COUNT ], statistics[0][ SUM ] );
			  computeMeans( statistics[0][ COUNT ],
					  statistics[0][ SUM ],
					  statistics[0][ MEAN ] );
			  // JEB 2016 WHY NO return; FOR THIS "ELSE"?
		  }
		  

		  if ( statIndex == 3) {
			  computeGeometricMeans( layer, data,
					  statistics[0][ COUNT ],
					  statistics[0][ GEOMETRIC_MEAN ] );
			  return;
		  }

		  if ( statIndex == 7 || statIndex == 8 || statIndex == 9) {
			  computeSums( layer, data, statistics[0][ COUNT ], statistics[0][ SUM ] );
			  computeMeans( statistics[0][ COUNT ],
					  statistics[0][ SUM ],
					  statistics[0][ MEAN ] );
			  computeVariance( layer, data,
					  statistics[0][ COUNT ],
					  statistics[0][ MEAN ],
					  statistics[0][ VARIANCE ],
					  statistics[0][ STANDARD_DEVIATION ],
					  statistics[0][ COEFFICIENT_OF_VARIANCE ] );
			  return;
		  }

		  if ( statIndex == 15) {
			  computeHoursOfNonCompliance( layer, data,
					  statistics[0][ COUNT ],
					  hoursPerTimestep,
					  threshold,
					  statistics[0][ HOURS_OF_NON_COMPLIANCE ] );
			  return;
		  }
	  } else {
	  
	  //////////////////////////////////////////////////////////////////////////
  
		  computeTimestepsOfExtrema( layer, data,
				  statistics[0][ TIMESTEP_OF_MINIMUM ],
				  statistics[0][ TIMESTEP_OF_MAXIMUM ] );

		  computeMaximum8HourMeans( layer, data, hoursPerTimestep,
				  statistics[0][ MAXIMUM_8HOUR_MEAN ] );

		  sortDataAcrossTimesteps( layer, data ); // Required for quartiles.

		  // For each data value (cell/site), count the number of valid timesteps:

		  computeValidTimesteps( layer, data, statistics[0][ COUNT ] );

		  computeQuartiles( layer, data,
				  statistics[0][ COUNT ],
				  statistics[0][ MINIMUM ],
				  statistics[0][ MAXIMUM ],
				  statistics[0][ MEDIAN ],
				  statistics[0][ FIRST_QUARTILE ],
				  statistics[0][ THIRD_QUARTILE ],
				  statistics[0][ RANGE ],
				  statistics[0][ INTERQUARTILE_RANGE ],
				  statistics[0][ FOURTH_MAX ],
				  statistics[0][ CUSTOM_PERCENTILE],
				  customPercentileValue);

		  computeSums( layer, data, statistics[0][ COUNT ], statistics[0][ SUM ] );

		  computeMeans( statistics[0][ COUNT ],
				  statistics[0][ SUM ],
				  statistics[0][ MEAN ] );

		  computeGeometricMeans( layer, data,
				  statistics[0][ COUNT ],
				  statistics[0][ GEOMETRIC_MEAN ] );

		  computeVariance( layer, data,
				  statistics[0][ COUNT ],
				  statistics[0][ MEAN ],
				  statistics[0][ VARIANCE ],
				  statistics[0][ STANDARD_DEVIATION ],
				  statistics[0][ COEFFICIENT_OF_VARIANCE ] );

		  computeHoursOfNonCompliance( layer, data,
				  statistics[0][ COUNT ],
				  hoursPerTimestep,
				  threshold,
				  statistics[0][ HOURS_OF_NON_COMPLIANCE ] );
	  
	  /////////////////////////////////////////////////////////////////////////////////
	  }
  }

  /**
   * PURPOSE: computeTimestepsOfExtrema - For each cell, compute the
   *          1-based timestep of the minimum and the timestep of the maximum.
   * INPUTS:  final double[][][] data        data[ rows ][ columns][ timesteps ]
   * OUTPUTS: double[][] timestepOfMinimum   timestepOfMinimum[ rows ][ columns]
   *                                        For each cell,
   *                                        this is the timestep number
   *                                        (whole, 1-based) of its minimum
   *                                        value over all of the timesteps.
   *          double[][] timestepOfMaximum   timestepOfMaximum[ rows ][ columns]
   *                                        For each cell,
   *                                        this is the timestep number
   *                                        (whole, 1-based) of its maximum
   *                                        value over all of the timesteps.
   **/

  private static void computeTimestepsOfExtrema( int layer, final double[][][][] data,
                                                 double[][] timestepOfMinimum,
                                                 double[][] timestepOfMaximum) {

    final int rows    = data.length;
    final int columns = data[ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        timestepOfMinimum[ row ][ column ] =
          indexOfMinimum( data[ row ][ column ], layer) + 1;
        timestepOfMaximum[ row ][ column ] =
          indexOfMaximum( data[ row ][ column ], layer) + 1;
      }
    }
  }

  /**
   * PURPOSE: computeMaximum8HourMeans - For each cell, compute the
   *          1-based timestep of the minimum and the timestep of the maximum.
   * INPUTS:  final double[][][] data      data[ rows ][ columns][ timesteps ]
   *          double hoursPerTimestep  Timestep size in decimal hours.
   * OUTPUTS: double[][] maximum8HourMean  maximum8HourMean[ rows ][ columns]
   *                                      For each cell,
   *                                      this is the maximum of the means
   *                                      of each moving 8-hour set of values.
   **/

  private static void computeMaximum8HourMeans( int layer, final double[][][][] data,
                                                final double hoursPerTimestep,
                                                double[][] maximum8HourMean ) {

    final int rows    = data.length;
    final int columns = data[ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        maximum8HourMean[ row ][ column ] =
          maximum8HourMeans( data[ row ][ column ], layer, hoursPerTimestep );
      }
    }
  }
  
  

  private static class LayerSorter implements Comparator<double[]> {
	  
	  int layer;
	  
	  LayerSorter(int targetLayer) {
		  layer = targetLayer;
	  }

	@Override
	public int compare(double[] o1, double[] o2) {
		// TODO Auto-generated method stub
		return Double.compare(o1[layer], o2[layer]);
	}


	  
  }
  /**
   * PURPOSE: sortDataAcrossTimesteps - For each cell, sort its timestep data.
   * INPUTS:  final double[][][] data  data[ rows ][ columns][ timesteps ].
   * OUTPUTS: final double[][][] data  data sorted over timesteps.
   **/

  private static void sortDataAcrossTimesteps( int layer, final double[][][][] data  ) {

    final int rows    = data.length;
    final int columns = data[ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        java.util.Arrays.sort( data[ row ][ column ], new GridCellStatistics.LayerSorter(layer) );
      }
    }
  }

  /**
   * PURPOSE: computeLayers - For each cell, compute the average and total value across all 
   *          layers and timesteps
   * INPUTS:  final double[][][] data  data[ rows ][ columns][ timesteps ].
   * OUTPUTS: double[][] layerMean           layerMean[ rows ][ columns]
   *                                        For each cell,
   *                                        this is the average value over 
   *                                        all of the layers and timesteps.
   *          double[][] layerSum            layerSum[ rows ][ columns]
   *                                        For each cell, this is the total value
   *                                        over all of the layers and timesteps.
   **/

  private static void computeLayers( int timestep, int firstLayer, int lastLayer, final double data[][][][],
                                             double[][][][] stats) {

    final int rows    = data.length;
    final int columns = data[ 0 ].length;
    final int timesteps = data[ 0 ][ 0 ].length;
    //convert to 0 based instead of 1
    //--firstLayer;
    //--lastLayer;
    // System.out.println("GridCellStatistics compute layers " + firstLayer + " through " + lastLayer + " timestep " + timestep);
    //final int layers = data[ 0 ][ 0 ][ 0 ].length;
    
    int firstTs = 0;
    int lastTs = stats.length;

    for (int ts = firstTs; ts < lastTs; ++ts) {
	    for ( int row = 0; row < rows; ++row ) {
	
	      for ( int column = 0; column < columns; ++column ) {  	  
	    		  
			  for (int layer = firstLayer; layer <= lastLayer; ++layer) {
				  
				  if (layer == firstLayer) {
					  stats[ts][LAYER_MEAN][row][column] = 0;
					  stats[ts][LAYER_SUM][row][column] = 0;
				  }
				  stats[ts][LAYER_MEAN][row][column] += data[row][column][ts][layer];
				  stats[ts][LAYER_SUM][row][column] += data[row][column][ts][layer];
				  
				  if (layer  == lastLayer)
					  stats[ts][LAYER_MEAN][row][column] = stats[ts][LAYER_SUM][row][column] / (lastLayer - firstLayer + 1);
				  
				  //if (row == 0 && column == 0)
				  //System.out.println("Cell 0 0 " + layer + ": " + data[row][column][timestep][layer]);
			  }
	    	  
	      }
	    }
    }
  }

  /**
   * PURPOSE: sortDataAcrossTimesteps - For each cell, compute the number of
   *          valid (i.e., not marked as "missing/bad") timestep values.
   * INPUTS:  final double[][][] data  data[ rows ][ columns][ timesteps ].
   * OUTPUTS: double[][][] count       count[ rows ][ columns ] valid values.
   **/

  private static void computeValidTimesteps( int layer, final double data[][][][],
                                             double[][] count ) {

    final int rows    = data.length;
    final int columns = data[ 0 ].length;
    final int timesteps = data[ 0 ][ 0 ].length;

    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
        final int firstIndex = firstValidIndex( data[ row ][ column ], layer );

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
   * INPUTS:  final double[][][] data   data[ rows ][ columns][ timesteps ].
   *          final double[][][] count  count[ rows ][ columns] valid values.
   * OUTPUTS: double[][] minimum        minimum[ rows ][ columns ].
   *          double[][] maximum        maximum[ rows ][ columns ].
   *          double[][] median         median[ rows ][ columns ]
   *                                   middle of sorted sequence of values.
   *          double[][] firstQuartile  firstQuartile[ rows ][ columns ]
   *                                   25th percentile of sorted of values.
   *          double[][] thirdQuartile  thirdQuartile[ rows ][ columns ]
   *                                   75th percentile of sorted of values.
   *          double[][] range          range[ rows ][ columns ]
   *                                   maximum - minimum.
   *          double[][] interquartileRange
   *                                   interquartileRange[ rows ][ columns ]
   *                                   75th - 25th.
   **/

  private static void computeQuartiles( int layer,
		                                final double[][][][] data,
                                        final double[][] count,
                                        final double[][] minimum,
                                        final double[][] maximum,
                                        final double[][] median,
                                        final double[][] firstQuartile,
                                        final double[][] thirdQuartile,
                                        final double[][] range,
                                        final double[][] interquartileRange,
                                        final double[][] fourthMax,
                                        final double[][] customPercentile,
                                        final double customPercentileValue) {
    final int rows    = data.length;
    final int columns = data[ 0 ].length;

    ArrayList<Double> sortedValues = new ArrayList<Double>();
    for ( int row = 0; row < rows; ++row ) {

      for ( int column = 0; column < columns; ++column ) {
    	  sortedValues.clear();
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
          customPercentile[   row ][ column ] = BADVAL3;
        } else {          
            final int index    = 0;
            final int last     = numberOfValidTimesteps - 1;
            final int middle   = Math.max(numberOfValidTimesteps / 2 - 1, 0);
            final int lower    = Math.max(numberOfValidTimesteps / 4 - 1, 0);
            final int upper    = Math.max(numberOfValidTimesteps * 3 / 4 - 1, 0);
            final int custom   = Math.max((int)Math.round(numberOfValidTimesteps * customPercentileValue / 100.0) - 1, 0);
            final boolean oddNumberOfTimesteps = numberOfValidTimesteps % 2 != 0;
            for(int i = 0; i < numberOfValidTimesteps; ++i) {
            	sortedValues.add(data[row][column][i][layer]);
            }
            
          Collections.sort(sortedValues);
          
          final double firstValue  = sortedValues.get(0);
          final double lastValue   = sortedValues.get(last);

          int fourthMaxIndex = last - 3;         
          double fourthMaxValue = BADVAL3;
          if (fourthMaxIndex < data[row][column].length && fourthMaxIndex >= 0)
        	  fourthMaxValue = sortedValues.get(fourthMaxIndex);
          final double middleValue = sortedValues.get(middle);
          final double lowerValue  = sortedValues.get(lower);
          final double upperValue  = sortedValues.get(upper);

          minimum[ row ][ column ] = firstValue;
          maximum[ row ][ column ] = lastValue;
          fourthMax[ row ][ column ] = fourthMaxValue;
          range[   row ][ column ] = lastValue - firstValue;
          customPercentile[ row ][ column ] = sortedValues.get(custom);

          if ( oddNumberOfTimesteps ) {
            median[             row ][ column ] = middleValue;
            firstQuartile[      row ][ column ] = lowerValue;
            thirdQuartile[      row ][ column ] = upperValue;
            interquartileRange[ row ][ column ] = upperValue - lowerValue;
          } else {
        	int upper_1 = Math.min(upper + 1,  last);
        	int lower_1 = Math.min(lower + 1,  last);
        	int middle_1 = Math.min(middle + 1,  last);
            final double averageLowerValue = ( lowerValue + sortedValues.get(lower_1 ) ) * 0.5f;
            final double averageUpperValue = ( upperValue + sortedValues.get( upper_1  )) * 0.5f;

            median[             row ][ column ] =
              ( middleValue + sortedValues.get( middle_1) ) * 0.5f;
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
   * INPUTS:  final double[][][] data  data[ rows ][ columns][ timesteps ].
   *          double[][] count         count[ rows ][ columns ] valid values.
   * OUTPUTS: double[][] sum           sum[ rows ][ columns ].
   **/

  private static void computeSums( int layer, final double[][][][] data,
                                   final double[][] count,
                                   double[][] sum ) {

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
            sumf( data[ row ][ column ],  layer, index );
        }
      }
    }
  }

  /**
   * PURPOSE: computeMeans - For each cell, compute the mean over timesteps.
   * INPUTS:  final double[][] count   count[ rows ][ columns ] valid values.
   *          final double[][] sum     sum[ rows ][ columns ] summed values.
   * OUTPUTS: double[][] mean          mean[ rows ][ columns ].
   **/

  private static void computeMeans( final double[][] count,
                                    final double[][] sum,
                                    double[][] mean ) {

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
   * INPUTS:  final double[][][] data  data[ rows ][ columns][ timesteps ].
   *          final double[][] count   count[ rows ][ columns ] valid values.
   * OUTPUTS: double[][] mean          mean[ rows ][ columns ].
 * @throws Exception 
   **/

  private static void computeGeometricMeans( int layer, final double[][][][] data,
                                             final double[][] count,
                                             double[][] geometricMean ) throws Exception {

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
            geometricMeanf( data[ row ][ column ], layer, index );
        }
      }
    }
  }

  /**
   * PURPOSE: computeVariance - For each cell, compute the variance, standard
   *          deviation and the coefficient of variance over timesteps.
   * INPUTS:  final double[][][] data  data[ rows ][ columns][ timesteps ].
   *          final double[][] count   count[ rows ][ columns ] valid values.
   *          final double[][] mean    mean[ rows ][ columns ] mean values.
   * OUTPUTS: double[][] variance      variance[ rows ][ columns ].
   *          double[][] standardDeviation      std[ rows ][ columns ].
   *          double[][] coefficientsOfVariance cov[ rows ][ columns ].
   **/

  private static void computeVariance( int layer, final double[][][][] data,
                                       final double[][] count,
                                       final double[][] mean,
                                       double[][] variance,
                                       double[][] standardDeviation,
                                       double[][] coefficientsOfVariance ) {

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
          final double meanValue = mean[ row ][ column ];
          final double s2 = (float)
            variancef( data[ row ][ column ], layer, index, meanValue );
          final double std = (float) java.lang.Math.sqrt( s2 );
          final double cv = meanValue > 0.0f ? 100.0f * std / meanValue : 0.0f;
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
   * INPUTS:  final double[][][] data  data[ rows ][ columns][ timesteps ].
   *          final double[][] count   count[ rows ][ columns ] valid values.
   *          final double hoursPerTimestep  Timestep size in decimal hours.
   *          final double threshold         Exceedance value to compare to.
   * OUTPUTS: double[][] hoursOfNonCompliance honc[ rows ][ columns ].
   **/

  private static void computeHoursOfNonCompliance( int layer, final double[][][][] data,
                                                  final double[][] count,
                                                 final double hoursPerTimestep,
                                                  final double threshold,
                                            double[][] hoursOfNonCompliance ) {

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
            exceedance( data[ row ][ column ], layer, index, threshold );
          hoursOfNonCompliance[ row ][ column ] = (float)
            ( numberOfTimestepsExceedingThreshhold * hoursPerTimestep );
        }
      }
    }
  }

  /**
   * PURPOSE: indexOfMinimum - Compute the index of the minimum valid value.
   * INPUTS:  final double[] data  Data to compare.
   * RETURNS: int 0-based index into data[] or -1 if all invalid.
   **/

  private static int indexOfMinimum( final double[][] data, int layer ) {

    final int count = data.length;
    double minimum = 0.0d;
    int result = -1;

    for ( int index = 0; index < count; ++index ) {
      final double value = data[ index ][layer];

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
   * INPUTS:  final double[] data  Data to compare.
   * RETURNS: int 0-based index into data[] or -1 if all invalid.
   **/

  private static int indexOfMaximum( final double[][] data, int layer ) {

    final int count = data.length;
    double maximum = 0.0d;
    int result = -1;

    for ( int index = 0; index < count; ++index ) {
      final double value = data[ index ][layer];

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
   * INPUTS:  final double[] data       Data to compare.
   *          double hoursPerTimestep  Decimal hours per timestep.
   * RETURNS: float maximum of a set of 8-hour means.
   **/

  private static double maximum8HourMeans( final double[][] data, int layer,
                                          final double hoursPerTimestep ) {

    final int count = (int) ( 8.0 / hoursPerTimestep + 0.5 );
    final int timesteps = data.length - count;
    double result = BADVAL3;

    for ( int timestep = 0; timestep <= timesteps; ++timestep ) {
      final int end = timestep + count;
      final double windowMean = mean( data, layer, timestep, end );

      if ( windowMean > result ) {
        result = windowMean;
      }
    }

    return result;
  }

  /**
   * PURPOSE: mean - Compute the mean of a set of valid values from
   *                 data[ first ] .. data[ end - 1 ].
   * INPUTS:  final double[] data  Data to compare.
   * RETURNS: float mean or BADVAL3 if all are invalid.
   **/

  private static double mean( final double[][] data, int layer,
                             final int first, final int end ) {

    double result = BADVAL3;
    int count = 0;

    for ( int index = first; index < end; ++index ) {
      final double value = data[ index ][layer];

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
   * INPUTS:  final double[] data  Data to compare.
   * RETURNS: int 0-based index into data[] or -1 if all invalid.
   **/

  private static int firstValidIndex( final double[][] data, int layer ) {

    final int count = data.length;
    int result = -1;

    for ( int index = 0; index < count; ++index ) {
      final double value = data[ index ][layer];
      
      if ( value > AMISS3 ) {
        result = index;
        index = count;
      }
    }

    return result;
  }

  /**
   * PURPOSE: sumf - Kahan Sum an array of floats.
   * INPUTS:  final double[] data  Data to sum.
   *          final int first     Starting index into data[].
   * RETURNS: double sum of valid values.
   * NOTES:   Based on version published in: "Floating-Point Summation"
   *          C/C++ Users Journal Sept 1996, pp 51-55, by Evan Manning.
   **/

  private static double sumf( final double[][] data, int layer, final int first ) {

    final int count = data.length;
    double sum        = 0.0; // The sum of the values in the array.
    double correction = 0.0; // Kahan corrector subtracts each round-off error

    for ( int index = first; index < count; ++index ) {
      final double nextTerm = data[ index ][layer];
      final double correctedNextTerm = nextTerm - correction;
      final double newSum = sum + correctedNextTerm;
      correction = ( newSum - sum ) - correctedNextTerm;
      sum = newSum;
    }

    return sum;
  }

  /**
   * PURPOSE: geometricMeanf - Compute the geometric mean of data[].
   * INPUTS:  final double[] data  Data to read.
   *          final int first     Starting index into data[].
   * RETURNS: double geometric mean of valid values.
   * @throws Exception 
   **/

  private static double geometricMeanf( final double[][] data, int layer, final int first ) throws Exception {

    final int count = data.length;
    final int numberOfValues = count - first;
    final double root    = numberOfValues > 0 ? 1.0 / numberOfValues : 0.0;
    double geometricMean = numberOfValues > 0 ? 1.0 : 0.0;

    for ( int index = first; index < numberOfValues; ++index ) {
      final double nextTerm = data[ index ][layer];
      geometricMean *= java.lang.Math.pow( nextTerm, root );
    }
    
    if ( Double.isNaN(geometricMean)){
    	//throw new Exception("Geometric Mean is not a number");
    }

    return geometricMean;
  }

  /**
   * PURPOSE: variancef - Compute the variance of data[].
   * INPUTS:  final double[] data  Data to read.
   *          final int first     Starting index into data[].
   *          final double mean   mean of the data.
   * RETURNS: double variance of the data from its mean.
   * NOTES:   Based on Kahan summation.
   **/

  private static double variancef( final double[][] data, int layer, final int first,
                                   final double mean ) {

    final int count = data.length;
    final int numberOfValues = count - first;
    double s2         = 0.0; // The variance.
    double sum        = 0.0; // The sum of the squares of the differences of
                             // each data value and the mean.
    double correction = 0.0; // Kahan corrector subtracts each round-off error

    for ( int index = first; index < count; ++index ) {
      final double value             = data[ index ][layer];
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
   * INPUTS:  final double[] data  Data to read.
   *          final int first     Starting index into data[].
   *          final double threshold  Threshold to compare to.
   * RETURNS: int the number of data values greater than threshold.
   **/

  private static int exceedance( final double[][] data, int layer, final int first,
                                 final double threshold ) {
    final int count = data.length;
    int result = 0;

    for ( int index = first; index < count; ++index ) {
      
      if ( data[ index ][layer] > threshold ) {
        ++result;
      }
    }

    return result;
  }
};