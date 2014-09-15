package anl.verdi.gui;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractListModel;

/**
 *
 * @version $Revision$ $Date$
 * @author Nick Collier
 */
public class RowHeaderModel extends AbstractListModel {

  /**
	 * 
	 */
	private static final long serialVersionUID = -5027139102077459767L;
private java.util.List<RowItem> items = new ArrayList<RowItem>();

  public RowHeaderModel(java.util.List<String> labels) {
	  int i = 0;
    for (String label : labels) {
      items.add(new RowItem(label, i++));
    }
  }

  public int getSize() {
    return items.size();
  }

  /*
  public void removeItem(int index) {
    items.remove(index);
    fireIntervalRemoved(this, index, index);
    resetItemRows(index);
  }
  */

  public void addItem(String label) {
    int row = items.size();
    RowItem ri = new RowItem(label, row);
    items.add(ri);
    fireIntervalAdded(this, row, row);
  }

  public Object getElementAt(int index) {
    return items.get(index);
  }

  public void removeItems(Set toRemove) {
    for (Iterator iter = toRemove.iterator(); iter.hasNext(); ) {
      RowItem item = (RowItem)iter.next();
      items.remove(item);
      fireIntervalRemoved(this, item.row,  item.row);
    }
    //items.removeAll(toRemove);
    resetItemRows();
  }


  private void resetItemRows() {
    for (int i = 0, n = items.size(); i < n; i++) {
      RowItem item = (RowItem)items.get(i);
      item.row = i;
    }
  }

  public static class RowItem {
    String label;
    int row;
    Color color;

    public RowItem(String label, int row) {
      this.label = label;
      this.row = row;
    }

    public String toString() {
      return label;
    }
  }
}

