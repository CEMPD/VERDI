package anl.verdi.plot.probe;

import javax.swing.table.TableModel;

import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.Slice;
import anl.verdi.data.XYDataFrameTableModel;

/**
 * A probe creator for Tile probing Tile plots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TileProbeCreator implements ProbeCreator {

	private DataFrame frame;
	private String name;
	
	private boolean isLog = false;
	private double logBase = 10;

	public TileProbeCreator(DataFrame frame, Slice slice) {
		this.frame = frame;
		Axes<DataFrameAxis> axes = frame.getAxes();
		int layer = -1;
		if (axes.getZAxis() != null) layer = axes.getZAxis().getOrigin() + 1;
		int time = axes.getTimeAxis().getOrigin() + 1;
		int xStart = axes.getXAxis().getOrigin();
		int xEnd = xStart + (int)slice.getXRange().getExtent() - 1;
		int yStart = axes.getYAxis().getOrigin();
		int yEnd = yStart + (int)slice.getYRange().getExtent() - 1;
		StringBuilder builder = new StringBuilder("Tile Probe: ");
		builder.append(frame.getVariable().getName());
		builder.append("  (");
		builder.append(time);
		builder.append(", ");
		if (layer != -1) {
			builder.append(layer);
			builder.append(", ");
		}
		builder.append(xStart + 1);
		builder.append(" - ");
		builder.append(xEnd + 1);
		builder.append(", ");
		builder.append(yStart + 1);
		builder.append(" - ");
		builder.append(yEnd + 1);
		builder.append(")");
		name = builder.toString();
	}

	/**
	 * Creates the probe.
	 */
	public TableModel createTableModel() {
		XYDataFrameTableModel xyModel = new XYDataFrameTableModel(frame);
		xyModel.setLog( this.isLog);
		xyModel.setLogBase( this.logBase);
		return xyModel;
	}


	/**
	 * Gets the name of the probe.
	 *
	 * @return the name of the probe.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Gets the name of the range axis.
	 *
	 * @return the name of the range axis.
	 */
	public String getRangeAxisName() {
		return "Y";
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
