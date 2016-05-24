package anl.verdi.data;

import javax.swing.table.AbstractTableModel;

/**
 * Abstract base class for table models based on
 * DataFrames. 
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class AbstractDataFrameTableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4120021060873529418L;
	protected DataFrame frame;
	protected int colCount;
	protected int rowCount;
	protected int colNameOffset, rowNameOffset;

	public AbstractDataFrameTableModel(DataFrame frame) {
		this.frame = frame;
	}

	/**
	 * Returns the number of columns in the model. A
	 * <code>JTable</code> uses this method to determine how many columns it
	 * should create and display by default.
	 *
	 * @return the number of columns in the model
	 * @see #getRowCount
	 */
	public int getColumnCount() {
		return colCount;
	}

	/**
	 * Returns the number of rows in the model. A
	 * <code>JTable</code> uses this method to determine how many rows it
	 * should display.  This method should be quick, as it
	 * is called frequently during rendering.
	 *
	 * @return the number of rows in the model
	 * @see #getColumnCount
	 */
	public int getRowCount() {
		return rowCount;
	}
}
