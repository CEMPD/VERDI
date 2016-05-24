package anl.verdi.area;


import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 */
public class AreaFileListModel extends AbstractListModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 64719957432128951L;
	private List<AreaFileListElement> areaFiles = new ArrayList<AreaFileListElement>();
	private List<AreaFileModelListener> listeners = new ArrayList<AreaFileModelListener>();

	public void addAreaFileModelListener(AreaFileModelListener listener) {
		listeners.add(listener);
	}

	public void removeAreaFileModelListener(AreaFileModelListener listener) {
		listeners.remove(listener);
	}

	public Object getElementAt(int index) {
		return areaFiles.get(index);
	}

	public AreaFile getAreaFileAt(int index) {
		return areaFiles.get(index).getAreaFile();
	}

	public int getSize() {
		return areaFiles == null ? 0 : areaFiles.size();
	}

	private int indexOf(AreaFile areaFile) {
		int index = 0;
		String name = areaFile.getName();
		for (AreaFileListElement dt : areaFiles) {

			if (dt.getAreaFile().getURL().equals(areaFile.getURL()) &&
					dt.getAreaFile().getName().equals(name)) return index;
			index++;
		}
		return -1;
	}

	public Iterable<AreaFileListElement> elements() {
		return areaFiles;
	}

	public int addAreaFile(AreaFile areaFile) {
		int index = indexOf(areaFile);
		if (index == -1) {
			index = areaFiles.size();
			areaFiles.add(new AreaFileListElement(areaFile));
			fireIntervalAdded(this, index, index);
			fireAreaFileAdded(areaFile);
		}
		return index;
	}

	public void removeAreaFile(AreaFileListElement item) {
		int index = areaFiles.indexOf(item);
		areaFiles.remove(index);
		fireIntervalRemoved(this, index, index);
		fireAreaFileRemoved(item.getAreaFile());
	}

	public void removeAreaFileAt(int index) {
		AreaFileListElement element = areaFiles.remove(index);
		fireIntervalRemoved(this, index, index);
		fireAreaFileRemoved(element.getAreaFile());
	}

	private void fireAreaFileRemoved(AreaFile areaFile) {
		for (AreaFileModelListener listener : listeners) {
			listener.areaFileRemoved(areaFile, this);
		}
	}

	private void fireAreaFileAdded(AreaFile areaFile) {
		for (AreaFileModelListener listener : listeners) {
			listener.areaFileAdded(areaFile, this);
		}
	}

	/**
	 * Removes all the elements from this model.
	 */
	public void clear() {
		int size = areaFiles.size();
		areaFiles.clear();
		fireIntervalRemoved(this, 0, size);
	}

	public void addAll(List<AreaFileListElement> elements) {
		int index = areaFiles.size();
		areaFiles.addAll(elements);
		fireIntervalAdded(this, index, areaFiles.size() - 1);
		for (AreaFileListElement elem : elements) {
			fireAreaFileAdded(elem.getAreaFile());
		}
	}

}
