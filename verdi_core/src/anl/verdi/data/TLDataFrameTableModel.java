package anl.verdi.data;


/**
 * DataFrameTableModel for data where the value
 * is indexed by the time and layer is 0.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TLDataFrameTableModel extends AbstractDataFrameTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7765575767365605692L;

	public TLDataFrameTableModel(DataFrame frame) {
		super(frame);
		Axes<DataFrameAxis> axes = frame.getAxes();
		rowCount = 2;
		colCount = axes.getTimeAxis().getExtent();
		colNameOffset = axes.getTimeAxis().getOrigin();
	}

	/**
	 * Returns the value for the cell at <code>columnIndex</code> and
	 * <code>rowIndex</code>.
	 *
	 * @return the value Object at the specified cell
	 * @param	rowIndex	the row whose value is to be queried
	 * @param	columnIndex the column whose value is to be queried
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex == 0) return columnIndex + colNameOffset;

		DataFrameIndex index = frame.getIndex();
		index.setTime(columnIndex);
		return frame.getDouble(index) > DataUtilities.BADVAL3 ? frame.getDouble(index) : DataUtilities.BADVAL3;
	}
}
