/**
 * MeshCellStatistics - Computes a set of statistics
         e.g., mean, hours-of-non-compliance, etc. for a data variable on a
         layer over various timesteps.
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package gov.epa.emvl;

//import anl.verdi.core.VerdiConstants;

public final class MeshCellStatistics {
	
	  public static void computeStatistics( int layer, int firstLayer, int lastLayer, int timestep, final double[][][] data,
              final double threshold,
              final double hoursPerTimestep,
              final double[][][][] statistics,
              final int statIndex,
              double customPercentile) throws Exception {
		  final double[][][][] dat = new double[1][][][];
		  dat[0] = data;
		  GridCellStatistics.computeStatistics(layer, firstLayer, lastLayer, timestep, dat, threshold, hoursPerTimestep, statistics, statIndex, customPercentile);
	  }

};