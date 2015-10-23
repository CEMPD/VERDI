package anl.verdi.plot.types;

import anl.verdi.plot.gui.Plot;


/**
 * Interface for plots that can have their timestep updated and be animated.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface TimeAnimatablePlot extends Plot {

	/**
	 * Update this plot to the specified timestep.
	 *
	 * @param timestep the new timestep.
	 */
	void updateTimeStep(int timestep);

}
