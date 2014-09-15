package anl.verdi.area;

import java.util.ArrayList;


/**
 * Listens for events from the AreaFileListModel.
 *
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 */
public interface AreaSelectionListener {

	/**
	 * Called when the specified area file has been added.
	 *
	 * @param set the added area file
	 * @param model the AreaFileListModel from which the set was removed
	 */
	public void areasSelected(ArrayList<Area> selections);
}
