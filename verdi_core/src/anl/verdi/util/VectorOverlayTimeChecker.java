package anl.verdi.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.Dataset;
import anl.verdi.data.Range;
import anl.verdi.formula.FormulaVariable;
import anl.verdi.gui.DatasetListElement;
import anl.verdi.gui.FormulaListElement;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VectorOverlayTimeChecker {
	static final Logger Logger = LogManager.getLogger(VectorOverlayTimeChecker.class.getName());
	private Map<Dataset, DatasetListElement> datasetMap = new HashMap<Dataset, DatasetListElement>();
	private DataFrame tileFrame;

	public VectorOverlayTimeChecker(Collection<DatasetListElement> elements, DataFrame tileFrame) {
		Logger.debug("in constructor for VectorOverlayTimeChecker");
		for (DatasetListElement item : elements) {
			datasetMap.put(item.getDataset(), item);
		}
		this.tileFrame = tileFrame;
	}

	public boolean isCompatible(Collection<FormulaListElement> formulaElements) {
		return checkTime(formulaElements);
	}

	private boolean checkTime(Collection<FormulaListElement> formulaElements) {
		// 1. are the unconstrained axes equal to each other
		Axes<CoordAxis> axes = tileFrame.getDataset().get(0).getCoordAxes();
		CoordAxis timeAxis = axes.getTimeAxis();
		for (FormulaListElement elem : formulaElements) {
			CoordAxis axis = elem.getAxes().getTimeAxis();
			if (timeAxis.getRange().equals(axis.getRange())) {
				// are the dates equal
				for (int i = 0; i < timeAxis.getRange().getExtent(); i++) {
					GregorianCalendar aCalendar1 = elem.getAxes().getDate(i);
					GregorianCalendar aCalendar2 = axes.getDate(i);
					Logger.debug("in VectorOverlayTimeChecker checkTime, 2 GregorianCalendar objects, aCalendar1 = " + aCalendar1
		+ ", aCalendar2 = " + aCalendar2); 
					if(!aCalendar1.equals(aCalendar2))
						return false;
				}
			} else {
				return false;
			}
		}

		// 2. are the constraints equal to each other

		Range timeRange = tileFrame.getAxes().getTimeAxis().getRange();
		for (Range range : getTimeRanges(formulaElements)) {
			if (!timeRange.equals(range)) return false;
		}

		return true;
	}

	private boolean checkZ(Collection<FormulaListElement> formulaElements) {
		// 1. are the unconstricted z axes compatible with each other and
		//    tile frame
		// 2. are the constraints, if any, the same
		// use the unconstrained zAxis from the dataset
		CoordAxis zAxis = tileFrame.getDataset().get(0).getCoordAxes().getZAxis();
		for (FormulaListElement elem : formulaElements) {
			CoordAxis axis = elem.getAxes().getZAxis();
			if (axis == null && zAxis != null) return false;
			if (zAxis == null && axis != null) return false;
			if (zAxis != null && axis != null && !zAxis.isCompatible(axis)) return false;
		}

		if (zAxis != null) {
			Range zRange = tileFrame.getAxes().getZAxis().getRange();
			for (Range range : getZRanges(formulaElements)) {
				if (!zRange.equals(range)) return false;
			}
		}

		return true;
	}

	private List<Range> getTimeRanges(Collection<FormulaListElement> formulaElements) {
		List<Range> ranges = new ArrayList<Range>();
		for (FormulaListElement element : formulaElements) {
			boolean dscFound = false;
			for (FormulaVariable var : element.variables()) {
				DatasetListElement dle = datasetMap.get(var.getDataset());
				if (dle.isTimeUsed()) {
					dscFound = true;
					ranges.add(new Range(dle.getTimeMin(), (dle.getTimeMax() - dle.getTimeMin()) + 1));
				}
			}

			if (!dscFound && element.isTimeUsed()) {
				ranges.add(new Range(element.getTimeMin(), (element.getTimeMax() - element.getTimeMin()) + 1));
			}
		}

		return ranges;
	}

	private List<Range> getZRanges(Collection<FormulaListElement> formulaElements) {
		List<Range> ranges = new ArrayList<Range>();
		for (FormulaListElement element : formulaElements) {
			boolean dscFound = false;
			for (FormulaVariable var : element.variables()) {
				DatasetListElement dle = datasetMap.get(var.getDataset());
				if (dle.isLayerUsed()) {
					dscFound = true;
					ranges.add(new Range(dle.getLayerMin(), (dle.getLayerMax() - dle.getLayerMin()) + 1));
				}
			}

			if (!dscFound && element.isLayerUsed()) {
				ranges.add(new Range(element.getLayerMin(), (element.getLayerMax() - element.getLayerMin()) + 1));
			}
		}

		return ranges;
	}
}
