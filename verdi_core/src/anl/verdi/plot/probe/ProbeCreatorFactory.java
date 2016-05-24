package anl.verdi.plot.probe;

import anl.verdi.data.DataFrame;
import anl.verdi.data.Slice;
import anl.verdi.formula.Formula.Type;

/**
 * Factory for creating ProbeCreators based on the type of the
 * probed plot.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ProbeCreatorFactory {

	/**
	 * Creates a ProbeCreator based on the info in
	 * the specified ProbeEvent.
	 *
	 * @param evt the ProbeEvent to create the Probe from
	 * @return the new ProbeCreator.
	 */
	public static ProbeCreator createProbeCreator(ProbeEvent evt) {
		DataFrame frame = evt.getProbedData();
		Slice slice = evt.getSlice();
		Type type = evt.getSourceType();
		if (type == Type.TILE) {
			TileProbeCreator tpc = new TileProbeCreator(frame, slice);
			tpc.setLog( evt.getIsLog());
			tpc.setLogBase( evt.getLogBase());
			return tpc; //new TileProbeCreator(frame, slice);
		} else if (type == Type.TIME_SERIES_LINE || type == Type.TIME_SERIES_BAR) {
			return new TimeSeriesProbeCreator(frame, slice);
		} else if (type == Type.VERTICAL_CROSS_SECTION) {
			return new VerticalCrossProbeCreator(frame, slice, evt.getXConstant());
		}

		return null;
	}
}
