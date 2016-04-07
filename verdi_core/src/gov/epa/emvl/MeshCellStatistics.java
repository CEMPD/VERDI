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
	
	  public static void computeStatistics( final float[][] data,
              final double threshold,
              final double hoursPerTimestep,
              final float[][][] statistics,
              final int statIndex) throws Exception {
		  final float[][][] dat = new float[1][][];
		  dat[0] = data;
		  GridCellStatistics.computeStatistics(dat, threshold, hoursPerTimestep, statistics, statIndex);
	  }

};