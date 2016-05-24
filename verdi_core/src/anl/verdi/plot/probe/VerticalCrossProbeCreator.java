package anl.verdi.plot.probe;

import javax.swing.table.TableModel;

import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.Slice;
import anl.verdi.data.XYLDataFrameTableModel;

/**
 * A probe creator for probing VerticalCrossSection plots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VerticalCrossProbeCreator implements ProbeCreator {

	private DataFrame frame;
	private String name;
	private boolean isXConst;

	public VerticalCrossProbeCreator(DataFrame frame, Slice slice, boolean isXConst) {
		this.frame = frame;
		this.isXConst = isXConst;

		Axes<DataFrameAxis> axes = frame.getAxes();
		int layerStart = axes.getZAxis().getOrigin();
		int layerEnd = layerStart + (int) slice.getLayerRange().getExtent() - 1;
		int time = axes.getTimeAxis().getOrigin();

		StringBuilder builder = new StringBuilder("Vertical Cross Section Probe: ");
		builder.append(frame.getVariable().getName());
		builder.append("  (");
		builder.append(time);
		builder.append(", ");
		builder.append(layerStart);
		builder.append(" - ");
		builder.append(layerEnd);
		builder.append(", ");
		if (isXConst) {
			builder.append( axes.getXAxis().getOrigin());
			int yStart =  axes.getYAxis().getOrigin();
			int yEnd = yStart + (int) slice.getYRange().getExtent() - 1;
			builder.append(", ");
			builder.append(yStart);
			builder.append(" - ");
			builder.append(yEnd);
		} else {
			int xStart =  axes.getXAxis().getOrigin();
			int xEnd = xStart + (int) slice.getXRange().getExtent() - 1;
			builder.append(xStart);
			builder.append(" - ");
			builder.append(xEnd);
			builder.append(", ");
			builder.append( axes.getYAxis().getOrigin());
		}

		builder.append(")");
		name = builder.toString();
	}

	/**
	 * Creates the probe.
	 */
	public TableModel createTableModel() {
		return new XYLDataFrameTableModel(frame, isXConst);
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
		return "Layer";
	}
}
