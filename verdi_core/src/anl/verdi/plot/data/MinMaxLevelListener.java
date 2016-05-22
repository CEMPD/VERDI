/**
 * MinMaxLevelListener - Used to notify callers of updates to min/max level calculations
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.data;

import java.awt.event.ActionListener;

public interface MinMaxLevelListener {

	public void layerUpdated(int level, double min, int minIndex, double max, int maxIndex, double percentComplete, boolean isLog);
	
	public void datasetUpdated(double min, int minIndex, double max, int maxIndex, double percentComplete, boolean isLog);
	
	public long getRenderTime();
}
