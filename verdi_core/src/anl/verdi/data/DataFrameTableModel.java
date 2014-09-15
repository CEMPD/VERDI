package anl.verdi.data;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DataFrameTableModel extends AbstractDataFrameTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7476947973193636097L;
	private boolean isTile = true;

	public DataFrameTableModel(DataFrame frame, Slice slice) {
		super(frame);
		Axes<DataFrameAxis> axes = frame.getAxes();
		if (axes.getXAxis() != null) {
			// assume tile
			colCount = axes.getXAxis().getExtent();
			rowCount = axes.getYAxis().getExtent();
			if (slice.getXRange() != null) {
				colNameOffset = (int) slice.getXRange().getOrigin();
			}
		} else {
			// assume 1D time series
			rowCount = 1;
			colCount = axes.getTimeAxis().getExtent();
			colNameOffset = (int) slice.getTimeRange().getOrigin();
			isTile = false;
		}
	}


	/**
	 * Returns the value for the cell at <code>columnIndex</code> and
	 * <code>rowIndex</code>.
	 *
	 * @param	rowIndex	the row whose value is to be queried
	 * @param	columnIndex the column whose value is to be queried
	 * @return the value Object at the specified cell
	 */
	public Object getValueAt(int rowIndex, int columnIndex) {
		DataFrameIndex index = frame.getIndex();
		// todo: only for Tile
		if (isTile) index.setXY(columnIndex, rowIndex);
		else index.setTime(columnIndex);
		return frame.getDouble(index);
	}
}
