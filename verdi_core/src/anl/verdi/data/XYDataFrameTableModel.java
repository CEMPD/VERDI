package anl.verdi.data;

//import anl.verdi.core.VerdiConstants;

/**
 * DataFrameTableModel for data where each x,y cell value
 * contains the data to show in the table.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class XYDataFrameTableModel extends AbstractDataFrameTableModel {

    /**
	 * 
	 */
	private static final long serialVersionUID = 578395857148082141L;
	private boolean isLog = false;
    private double logBase = 10.0;
    
	public XYDataFrameTableModel(DataFrame frame) {
		super(frame);
		Axes<DataFrameAxis> axes = frame.getAxes();
		colCount = axes.getXAxis().getExtent() + 1;
		rowCount = axes.getYAxis().getExtent() + 1;
		colNameOffset = frame.getAxes().getXAxis().getOrigin() + 1;
		rowNameOffset = frame.getAxes().getYAxis().getOrigin() + 1;
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
			index.setXY(columnIndex - 1, rowIndex - 1);
			if ( !isLog) {
				return (frame.getDouble(index) > DataUtilities.BADVAL3 && frame.getDouble(index) < DataUtilities.NC_FILL_FLOAT) ? frame.getDouble(index) : DataUtilities.BADVAL3;
			} else {
				double logvalue = Math.log(frame.getDouble(index)) / Math.log(logBase);
				return (logvalue > DataUtilities.BADVAL3 && logvalue < DataUtilities.NC_FILL_FLOAT) ? logvalue : DataUtilities.BADVAL3;
			}
		}
	}


	public void setLog(boolean isLog) {
		this.isLog = isLog;
	}


	public boolean isLog() {
		return isLog;
	}


	public void setLogBase(double logBase) {
		this.logBase = logBase;
	}


	public double getLogBase() {
		return logBase;
	}
}
