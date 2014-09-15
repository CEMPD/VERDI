package anl.verdi.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import anl.verdi.data.DataFrame;
import anl.verdi.data.Dataset;
import anl.verdi.data.Range;
import anl.verdi.data.Variable;
import anl.verdi.gui.DatasetListElement;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ObsTimeChecker {

	private Map<Dataset, DatasetListElement> datasetMap = new HashMap<Dataset, DatasetListElement>();
	private DataFrame tileFrame;

	public ObsTimeChecker(Collection<DatasetListElement> elements, DataFrame tileFrame) {
		for (DatasetListElement item : elements) {
			datasetMap.put(item.getDataset(), item);
		}
		this.tileFrame = tileFrame;
	}

	public boolean isCompatible(Variable obsVar) {
		return checkTime(obsVar);
	}

	private boolean checkTime(Variable obsVar) {
		// 1. are the unconstrained axes equal to each other
		/*
		Axes<CoordAxis> axes = tileFrame.getDataset().get(0).getCoordAxes();
		CoordAxis timeAxis = axes.getTimeAxis();

		CoordAxis axis = obsVar.getDataset().getCoordAxes().getTimeAxis();
		if (timeAxis.getRange().equals(axis.getRange())) {
			// are the dates equal
			for (int i = 0; i < timeAxis.getRange().getExtent(); i++) {
				Date d1 = obsVar.getDataset().getCoordAxes().getDate(i);
				Date d2 = axes.getDate(i);
				if (!d1.equals(d2)) return false;
			}
		} else {
			return false;
		}
		*/

		// 2. are the constraints equal to each other
		Range timeRange = tileFrame.getAxes().getTimeAxis().getRange();
		DatasetListElement dle = datasetMap.get(obsVar.getDataset());
		if (dle != null && dle.isTimeUsed()) {
			Range obsRange = new Range(dle.getTimeMin(), (dle.getTimeMax() - dle.getTimeMin()) + 1);
			return obsRange.equals(timeRange);
		}

		return true;
	}
}