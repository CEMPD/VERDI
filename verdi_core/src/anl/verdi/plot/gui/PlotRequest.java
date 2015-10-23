package anl.verdi.plot.gui;

import anl.verdi.core.VerdiApplication;


/**
 * A request by one plot to create another plot using some data from the first plot.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface PlotRequest extends PlotCreator {

	/**
	 * Initializes this PlotRequest with the specified application.
	 *
	 * @param app the application object
	 */
	void init(VerdiApplication app);
	
}
