package anl.verdi.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;
import anl.verdi.data.Range;
import anl.verdi.formula.FormulaVariable;
import anl.verdi.gui.DatasetListElement;
import anl.verdi.gui.FormulaListElement;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class CompatibilityChecker {

	private static class DateData {
		DateRange range;
		int steps;
		Axes<CoordAxis> axes;

		public DateData(Axes<CoordAxis> axes, DateRange range, int steps) {
			this.range = range;
			this.axes = axes;
			this.steps = steps;
		}

		public DateData(Axes<CoordAxis> axes) {
			range = new DateRange(axes.getStartDate().getTimeInMillis(), axes.getEndDate().getTimeInMillis());
			Range timeRange = axes.getTimeAxis().getRange();
			// steps is like extent in this case
//			steps = (int) (timeRange.getExtent() - timeRange.getOrigin() + 1l);	// gives error in this form
			long lngSteps = timeRange.getExtent() - timeRange.getOrigin() + 1l;
			steps = (int)lngSteps;
			this.axes = axes;
		}

		public DateData(Axes<CoordAxis> axes, int start, int end) {
			range = new DateRange(axes.getDate(start).getTimeInMillis(), axes.getDate(end).getTimeInMillis());
			Range timeRange = axes.getTimeAxis().getRange();
			// steps is like extent in this case
//			steps = (int) ((int)timeRange.getExtent() - timeRange.getOrigin() + 1);	// gives error in this form
			long lngSteps = timeRange.getExtent() - timeRange.getOrigin() + 1l;
			steps = (int)lngSteps;
			this.axes = axes;
		}

		public DateData overlap(Axes<CoordAxis> axes, int start, int end) {
			DateData otherData = new DateData(axes, start, end);
			DateRange nRange = range.overlap(otherData.range);
			if (nRange == null) return null;
			// what is the number of steps for this range now.
			int steps = getSteps(this.axes, nRange);
			int oSteps = getSteps(axes, nRange);
			if (steps == Axes.TIME_STEP_NOT_FOUND || oSteps == Axes.TIME_STEP_NOT_FOUND) return null;
			if (oSteps == steps) {
				return new DateData(axes, nRange, steps);
			}
			return null;
		}

		public DateData overlap(Axes<CoordAxis> axes) {
			DateData otherData = new DateData(axes);
			DateRange nRange = range.overlap(otherData.range);
			if (nRange == null) return null;
			// what is the number of steps for this range now.
			int steps = getSteps(this.axes, nRange);
			int oSteps = getSteps(axes, nRange);
			if (steps == Axes.TIME_STEP_NOT_FOUND || oSteps == Axes.TIME_STEP_NOT_FOUND) return null;
			if (oSteps == steps) {
				return new DateData(axes, nRange, steps);
			}
			return null;
		}

		private int getSteps(Axes<CoordAxis> axes, DateRange range) {
			int start = this.axes.getTimeStep(new Date(range.getStart()));
			int end = this.axes.getTimeStep(new Date(range.getEnd()));
			if (start == Axes.TIME_STEP_NOT_FOUND || end == Axes.TIME_STEP_NOT_FOUND) return Axes.TIME_STEP_NOT_FOUND;
			// steps is like extent in this case
			return end - start + 1;
		}
	}

	private static class Ranges {
		private Range xRange, yRange, tRange, zRange;

		public Ranges() {
			xRange = new Range(-1, -1);
			yRange = new Range(-1, -1);
			tRange = new Range(-1, -1);
			zRange = new Range(-1, -1);
		}

		public void xRange(int origin, int extent) {
			xRange.setOrigin(origin);
			xRange.setExtent(extent);
		}

		public void yRange(int origin, int extent) {
			yRange.setOrigin(origin);
			yRange.setExtent(extent);
		}

		public void zRange(int origin, int extent) {
			zRange.setOrigin(origin);
			zRange.setExtent(extent);
		}

//		public void tRange(int origin, int extent) {
		public void tRange(long origin, long extent) {
			tRange.setOrigin(origin);
			tRange.setExtent(extent);
		}

		public boolean equals(Ranges other) {
			return xRange.equals(other.xRange) &&
							yRange.equals(other.yRange) &&
							zRange.equals(other.zRange) &&
							tRange.equals(other.tRange);
		}
	}

	private Map<Dataset, DatasetListElement> datasetMap = new HashMap<Dataset, DatasetListElement>();

	public CompatibilityChecker(Collection<DatasetListElement> elements) {
		for (DatasetListElement item : elements) {
			datasetMap.put(item.getDataset(), item);
		}
	}

	public DateRange isCompatible(Collection<FormulaListElement> formulaElements) {
		if (!checkXYZ(formulaElements)) return null;
		DateData data = null;
		for (FormulaListElement item : formulaElements) {
			data = checkFormulaTime(data, item);
			if (data == null) return null;
		}
		// by here should have a range and number of steps that
		// is compatible within individual formulas and over all the
		// formulas.
		for (FormulaListElement item : formulaElements) {
			boolean timeUsed = false;
			for (FormulaVariable var : item.variables()) {
				DatasetListElement ds = datasetMap.get(var.getDataset());
				if (ds.isTimeUsed()) {
					timeUsed = true;
					data = data.overlap(var.getDataset().getCoordAxes(), ds.getTimeMin(), ds.getTimeMax());
					if (data == null) return null;
				}
			}
			if (!timeUsed) {
				// check formula time constraint
				if (item.isTimeUsed()) {
					data = data.overlap(item.getAxes(), item.getTimeMin(), item.getTimeMax());
					if (data == null) return null;
				}
			}
		}
		return data.range;
	}

	private DateData checkFormulaTime(DateData data, FormulaListElement item) {
		for (FormulaVariable var : item.variables()) {
			Axes<CoordAxis> axes = var.getDataset().getCoordAxes();
			if (data == null) {
				data = new DateData(axes);
			} else {
				data = data.overlap(axes);
				if (data == null) return null;
			}
		}
		return data;
	}

	private boolean checkXYZ(Collection<FormulaListElement> formulaElements) {
		if (!checkXYZAxes(formulaElements)) return false;
		List<Ranges> ranges = new ArrayList<Ranges>();
		for (FormulaListElement item : formulaElements) {
			List<Ranges> fRanges = new ArrayList<Ranges>();
			for (FormulaVariable var : item.variables()) {
				DatasetListElement ds = datasetMap.get(var.getDataset());
				Ranges r = new Ranges();
				if (ds.isXYUsed()) {
					r = new Ranges();
					r.xRange(ds.getXMin(), ds.getXMax());
					r.yRange(ds.getYMin(), ds.getYMax());
				}
				if (ds.isLayerUsed()) {
					if (r == null) r = new Ranges();
					r.zRange(ds.getLayerMin(), ds.getLayerMax());
				}

				fRanges.add(r);
			}

			if (fRanges.size() == 0) fRanges.add(new Ranges());
			if (item.isXYUsed()) {
				int xMin = item.getXMin();
				int yMin = item.getYMin();
				int xMax = item.getXMax();
				int yMax = item.getYMax();
				for (Ranges r : fRanges) {
					if (r.xRange.getOrigin() == -1) {
						r.xRange(xMin, xMax);
						r.yRange(yMin, yMax);
					}
				}
			}

			if (item.isLayerUsed()) {
				for (Ranges r : fRanges) {
					if (r.zRange.getOrigin() == -1) {
						r.zRange(item.getLayerMin(), item.getLayerMax());
					}
				}
			}
			ranges.addAll(fRanges);
		}

		if (ranges.size() > 1) {
			// may be no ranges if no dataset / formula range
			// constraints used, or if just a single dataset.
			Ranges start = ranges.get(0);
			for (int i = 1; i < ranges.size(); i++) {
				if (!start.equals(ranges.get(i))) return false;
			}
		}

		return true;
	}

	private boolean checkXYZAxes(Collection<FormulaListElement> formulaElements) {
		Set<Axes<CoordAxis>> axes = new HashSet<Axes<CoordAxis>>();
		Axes<CoordAxis> xAxes = formulaElements.iterator().next().getAxes();
		for (FormulaListElement element : formulaElements) {
			axes.add(element.getAxes());
		}
		axes.remove(xAxes);
		for (Axes otherAxes : axes) {
			if (!xAxes.getXAxis().isCompatible(otherAxes.getXAxis())) {
				return false;
			}

			if (!xAxes.getYAxis().isCompatible(otherAxes.getYAxis())) {
				return false;
			}

			if (xAxes.getZAxis() != null) {
				if (otherAxes.getZAxis() == null) {
					return false;
				}
				if (!xAxes.getZAxis().isCompatible(otherAxes.getZAxis())) {
					return false;
				}
			} else {
				if (otherAxes.getZAxis() != null) {
					return false;
				}
			}
		}
		return true;
	}

	private DateRange getDateRange(Axes<CoordAxis> axes1) {
//		int origin = axes1.getTimeAxis().getRange().getOrigin();
//		Date start = axes1.getDate(origin);
//		Date end = axes1.getDate((int)
//						(origin + axes1.getTimeAxis().getRange().getExtent() + 1));
		long origin = axes1.getTimeAxis().getRange().getOrigin();
		GregorianCalendar start = axes1.getDate(origin);
		GregorianCalendar end = axes1.getDate((int)
						(origin + axes1.getTimeAxis().getRange().getExtent() + 1));
System.out.println("in CompatibilityChecker getDateRange, computed 2 GregorianCalendar objects, start = " + 
						start.toString() + ", end = " + end.toString());
		return new DateRange(start.getTimeInMillis(), end.getTimeInMillis());
	}
}
