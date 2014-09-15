package anl.verdi.plot.probe;

import javax.swing.table.TableModel;

import anl.verdi.data.DataFrame;
import anl.verdi.data.Slice;
import anl.verdi.data.TLDataFrameTableModel;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TimeSeriesProbeCreator implements ProbeCreator {

	private DataFrame frame;
	private String name;

	public TimeSeriesProbeCreator(DataFrame frame, Slice slice) {
		this.frame = frame;
		int timeStart = frame.getAxes().getTimeAxis().getOrigin();
		int timeEnd = timeStart + (int)slice.getTimeRange().getExtent() - 1;
		if (frame.getAxes().getZAxis() != null) {
		int layer = frame.getAxes().getZAxis().getOrigin();
		name = "Time Series Probe: " + frame.getVariable().getName() + " (" +
						timeStart + " - " + timeEnd + ", " + layer + ")";
		} else {
			name = "Time Series Probe: " + frame.getVariable().getName() + " (" +
						timeStart + " - " + timeEnd + ")";
		}
	}

	/**
	 * Creates the probe.
	 */
	public TableModel createTableModel() {
		return new TLDataFrameTableModel(frame);
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
		return "Value";
	}
}
