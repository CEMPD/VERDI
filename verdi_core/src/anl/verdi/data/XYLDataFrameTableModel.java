package anl.verdi.data;

/**
 * DataFrameTableModel for data where the
 * data is indexed by the layer, and x or y
 * where one of x or y is a constant value.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class XYLDataFrameTableModel extends AbstractDataFrameTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7026242960843663251L;
	private boolean constIsX = false;

	public XYLDataFrameTableModel(DataFrame frame, boolean isXConstant) {
		super(frame);
		Axes<DataFrameAxis> axes = frame.getAxes();
		constIsX = isXConstant;
		// find the non-constant axis
		DataFrameAxis nonConstAxis = null;
		if (constIsX) {
			nonConstAxis = axes.getYAxis();
			constIsX = true;
			colNameOffset =  axes.getYAxis().getOrigin();
		} else {
			nonConstAxis = axes.getXAxis();
			constIsX = false;
			colNameOffset =  axes.getXAxis().getOrigin();
		}

		// + 1 to account for extra col naming the range values
		colCount = nonConstAxis.getExtent() + 1;
		// + 1 to account for extra row naming the domain values
		rowCount = axes.getZAxis().getExtent() + 1;
		rowNameOffset = axes.getZAxis().getOrigin();
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
		if (rowIndex == 0 && columnIndex == 0) {
			return "";
		} else if (rowIndex == 0) {
			return columnIndex - 1 + colNameOffset;
		} else if (columnIndex == 0) {
			return rowIndex - 1 + rowNameOffset;
		} else {
			DataFrameIndex index = frame.getIndex();
			index.setLayer(rowIndex - 1);
			if (constIsX) index.setXY(0, columnIndex - 1);
			else index.setXY(columnIndex - 1, 0);
			return frame.getDouble(index) > DataUtilities.BADVAL3 ? frame.getDouble(index) : DataUtilities.BADVAL3;
		}
	}
}
