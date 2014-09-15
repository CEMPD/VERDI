package anl.verdi.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import anl.verdi.data.Dataset;

/**
 * @author Nick Collier
* @version $Revision$ $Date$
*/
public class DatasetListModel extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6884894135223912769L;
	private List<DatasetListElement> datasets = new ArrayList<DatasetListElement>();
	private List<DatasetModelListener> listeners = new ArrayList<DatasetModelListener>();

	public DatasetListModel()
	{
		System.out.println("in DatasetListModel default constructor");
	}
	
	public void addDatasetModelListener(DatasetModelListener listener) {
		System.out.println("in DatasetListModel addDatasetModelListener");
		listeners.add(listener);
	}

	public void removeDatasetModelListener(DatasetModelListener listener) {
		System.out.println("in DatasetListModel removeDatasetModelListener");
		listeners.remove(listener);
	}

	public Object getElementAt(int index) {
		System.out.println("in DatasetListModel getElementAt");
		return datasets.get(index);
	}

	public Dataset getDatasetAt(int index) {
		System.out.println("in DatasetListModel getDatasetAt");
		return datasets.get(index).getDataset();
	}

	public int getSize() {
//		System.out.println("in DatasetListModel getSize");	// NOTE: called VERY frequently
		return datasets == null ? 0 : datasets.size();
	}

	private int indexOf(Dataset dataset) {
		System.out.println("in DatasetListModel indexOf");
		int index = 0;
		String name = dataset.getName();
		for (DatasetListElement dt : datasets) {

			if (dt.getDataset().getURL().equals(dataset.getURL()) &&
							dt.getDataset().getName().equals(name)) return index;
			index++;
		}
		return -1;
	}

	public Iterable<DatasetListElement> elements() {
		System.out.println("in DatasetListModel elements");
		return datasets;
	}

	public int addDataset(Dataset dataset) {
		System.out.println("in DatasetListModel addDataset");
		int index = indexOf(dataset);
		if (index == -1) {
			index = datasets.size();
			datasets.add(new DatasetListElement(dataset));
			fireIntervalAdded(this, index, index);
			fireDatasetAdded(dataset);
		}
		return index;
	}

	public void removeDataset(DatasetListElement item) {
		System.out.println("in DatasetListModel removeDataset (DatasetListElement)");
		int index = datasets.indexOf(item);
		datasets.remove(index);
		fireIntervalRemoved(this, index, index);
		fireDatasetRemoved(item.getDataset());
	}

	public void removeDatasetAt(int index) {
		System.out.println("in DatasetListModel removeDataSetAt (int)");
		if (index < 0)
			return;
		
		DatasetListElement element = datasets.remove(index);
		fireIntervalRemoved(this, index, index);
		fireDatasetRemoved(element.getDataset());
	}

	private void fireDatasetRemoved(Dataset dataset) {
		System.out.println("in DatasetListModel fireDatasetRemoved");
		for (DatasetModelListener listener : listeners) {
			listener.datasetRemoved(dataset, this);
		}
	}

	private void fireDatasetAdded(Dataset dataset) {
		System.out.println("in DatasetListModel fireDatasetAdded");
		for (DatasetModelListener listener : listeners) {
			listener.datasetAdded(dataset, this);
		}
	}

	/**
	 * Removes all the elements from this model.
	 */
	public void clear() {
		System.out.println("in DatasetListModel clear");
		int size = datasets.size();
		List<DatasetListElement> temp = new ArrayList<DatasetListElement>();
		temp.addAll(datasets);
		
		for (DatasetListElement elem : temp)
			removeDataset(elem);
		
		fireIntervalRemoved(this, 0, size);
	}

	public void addAll(List<DatasetListElement> elements) {
		System.out.println("in DatasetListModel addAll");
		int index = datasets.size();
		datasets.addAll(elements);
		fireIntervalAdded(this, index, datasets.size() - 1);
		for (DatasetListElement elem : elements) {
			fireDatasetAdded(elem.getDataset());
		}
	}

}
