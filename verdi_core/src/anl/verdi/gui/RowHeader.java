package anl.verdi.gui;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

/**
 *
 * @version $Revision$ $Date$
 * @author Nick Collier
 */
public class RowHeader extends JPanel {

  /**
	 * 
	 */
	private static final long serialVersionUID = 3250076221826676110L;
private RowHeaderModel model;
  private JList rowList;

	public RowHeader(List<String> headers, JTable table) {
    super(new BorderLayout());
    model = new RowHeaderModel(headers);
    rowList = new JList(model);
    add(rowList, BorderLayout.CENTER);
    rowList.setFixedCellHeight(table.getRowHeight());
		RowHeaderRenderer rowRenderer = new RowHeaderRenderer();
		rowList.setCellRenderer(rowRenderer);
    rowList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
  }

  public void setHeaderColor(int row, Color color) {
    RowHeaderModel.RowItem item = (RowHeaderModel.RowItem) model.getElementAt(row);
    item.color = color;
  }

  public void setHeaderColor(String label, Color color) {
    for (int i = 0, n = model.getSize(); i < n; i++) {
      RowHeaderModel.RowItem item = (RowHeaderModel.RowItem) model.getElementAt(i);
      if (item.label.equals(label)) {
        item.color = color;
        break;
      }
    }
  }

  public void unstitch(int[] assetList) {
    for (int i = 0, n = assetList.length; i < n; i++) {
      RowHeaderModel.RowItem item = (RowHeaderModel.RowItem) model.getElementAt(assetList[i]);
      item.color = null;
    }
  }

  public int[] deleteSelected() {
    int[] rows = rowList.getSelectedIndices();
    deleteRows(rows);

    return rows;
  }

  public void addNamedRows(List names) {
    for (int i = 0, n = names.size(); i < n; i++) {
      model.addItem((String)names.get(i));
    }
  }

  public int[] deleteNamedRows(List names) {
    int[] rows = new int[names.size()];
    for (int i = 0, n = names.size(); i < n; i++) {
      String name = (String)names.get(i);
      for (int j = 0, l = model.getSize(); j < l; j++) {
        RowHeaderModel.RowItem item = (RowHeaderModel.RowItem) model.getElementAt(j);
        if (name.equals(item.label)) {
          rows[i] = j;
          break;
        }
      }
    }

    deleteRows(rows);
    return rows;
  }

  public void deleteRows(int[] rows) {
    Set toRemove = new HashSet();
    for (int i = 0, n = rows.length; i < n; i++) {
      toRemove.add(model.getElementAt(rows[i]));
    }
    model.removeItems(toRemove);
    //this.invalidate();
    //repaint();
  }

  public void addRow(String label) {
    model.addItem(label);
  }

  public void selectAll() {
    rowList.setSelectionInterval(0, model.getSize() - 1);
  }

  public void unselectAll() {
    rowList.getSelectionModel().clearSelection();
  }

  public List getSelectedRowLabels() {
  //  Object[] selectedVals = rowList.getSelectedValues();
    List selectedVals = rowList.getSelectedValuesList();
    List labels = new ArrayList();
//    for (int i = 0, n = selectedVals.length; i < n; i++) {
    int  n = selectedVals.size();
   for (int i = 0; i < n; i++) {
      labels.add(((RowHeaderModel.RowItem) selectedVals.get(i)).label);
    }

    return labels;
  }

  public int[] getSelectedRows() {
//    Object[] selectedVals = rowList.getSelectedValues();
    List selectedVals = rowList.getSelectedValuesList();
//    int[] retVal = new int[selectedVals.length];
    int[] retVal = new int[selectedVals.size()];
//    for (int i = 0, n = selectedVals.length; i < n; i++) {
    int n = selectedVals.size();
    for (int i = 0;  i < n; i++) {
      retVal[i] = ((RowHeaderModel.RowItem) selectedVals.get(i)).row;
    }

    return retVal;
  }

  public String getLabel(int row) {
    RowHeaderModel.RowItem item = (RowHeaderModel.RowItem) model.getElementAt(row);
    return item.label;
  }

  public List getRowLabels() {
    List labels = new ArrayList();
    for (int i = 0; i < model.getSize(); i++) {
      RowHeaderModel.RowItem item = (RowHeaderModel.RowItem) model.getElementAt(i);
      labels.add(item.label);
    }
    return labels;
  }

  class RowHeaderRenderer extends JToggleButton implements ListCellRenderer {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4877663901046434632L;
	Color background;

    public RowHeaderRenderer() {
      background = getBackground();
	    setHorizontalAlignment(JToggleButton.LEFT);
    }

    public Component getListCellRendererComponent(
            JList list,
            Object value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {


      setText((value == null) ? "" : value.toString());
      setSelected(isSelected);
      RowHeaderModel.RowItem item = (RowHeaderModel.RowItem) value;
      if (item.color == null)
        setBackground(background);
      else
        setBackground(item.color);

      return this;
    }
  }

}


