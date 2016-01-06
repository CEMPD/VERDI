
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