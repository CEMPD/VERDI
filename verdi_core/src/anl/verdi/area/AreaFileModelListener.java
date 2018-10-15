package anl.verdi.area;


/**
 * Listens for events from the AreaFileListModel.
 *
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 */
public interface AreaFileModelListener {

	/**
	 * Called when the specified area file has been removed.
	 *
	 * @param set the removed area file
	 * @param model the area AreaFileListModel from which the set was removed
	 */
	public void areaFileRemoved(AreaFile set, AreaFileListModel model);

	/**
	 * Called when the specified area file has been added.
	 *
	 * @param set the added area file
	 * @param model the AreaFileListModel from which the set was removed
	 */
	public void areaFileAdded(AreaFile set, AreaFileListModel model);
}
