package anl.verdi.gui;

import anl.verdi.data.Dataset;

/**
 * Listens for events from the DatasetListModel.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface DatasetModelListener {

	/**
	 * Called when the specified dataset has been removed.
	 *
	 * @param set the removed dataset
	 * @param model the DatasetListModel from which the set was removed
	 */
	public void datasetRemoved(Dataset set, DatasetListModel model);

	/**
	 * Called when the specified dataset has been added.
	 *
	 * @param set the added dataset
	 * @param model the DatasetListModel from which the set was removed
	 */
	public void datasetAdded(Dataset set, DatasetListModel model);
}
