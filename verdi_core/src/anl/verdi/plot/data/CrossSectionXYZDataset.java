package anl.verdi.plot.data;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.XYZDataset;

import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameIndex;

/**
 * JChart XYZDataset implemented in terms of a DataFrame for
 * a vertical cross-section. Vertical cross section shows the
 * values at each layer for some constant row or col value.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class CrossSectionXYZDataset extends AbstractDataset implements XYZDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2520787127506498240L;

	private interface SeriesData {

		/**
		 * Gets the value of the specified item.
		 *
		 * @param item the item
		 * @return the value of the specified item.
		 */
		double getValue(int item);

		/**
		 * @param domain the domain coordinate, in this case that is the
		 *               frame Y
		 * @param range  the range coordinate, in this case that is the
		 *               layer value.
		 * @return the value at the specified coords
		 */
		double getValue(int domain, int range);

		/**
		 * Gets the number of items in the series.
		 * @return the number of items in the series.
		 */
		int size();

		String getName();

		int getDomainExtent();

		int getDomainOrigin();
	}

	private static class RowSeriesData implements SeriesData {

		DataFrame frame;
		int domainExtent, layerExtent, domainOrigin;
		int timeStep, row;
		DataFrameIndex index;

		public RowSeriesData(DataFrame frame, int timeStep, int row) {
			this.frame = frame;
			this.timeStep = timeStep;
			this.row = row;
			index = frame.getIndex();
			index.set(timeStep, 0, 0, row);
			domainExtent = frame.getAxes().getXAxis().getExtent();
			domainOrigin = frame.getAxes().getXAxis().getOrigin();
			layerExtent = frame.getAxes().getZAxis().getExtent();
		}

		public int getDomainExtent() {
			return domainExtent;
		}

		public double getValue(int item) {
			// x is the frame y value (row is the domain here)
			// y is the layer
			int frameX = item % domainExtent;
			int frameLayer = (item - frameX) / domainExtent;
			index.set(timeStep, frameLayer, frameX, row);
			return frame.getDouble(index);
		}

		/**
		 * @param domain the domain coordinate, in this case that is the
		 *               frame Y
		 * @param range  the range coordinate, in this case that is the
		 *               layer value.
		 * @return the value at the specified coords
		 */
		public double getValue(int domain, int range) {
			index.set(timeStep, range, domain, row);
			return frame.getDouble(index);
		}

		public int size() {
			return domainExtent * layerExtent;
		}

		public String getName() {
			return frame.getVariable().getName() + "Col";
		}


		public int getDomainOrigin() {
			return domainOrigin;
		}
	}

	private static class ColSeriesData implements SeriesData {

		DataFrame frame;
		int domainExtent, domainOrigin, layerExtent;
		int timeStep, col;
		DataFrameIndex index;

		public ColSeriesData(DataFrame frame, int timeStep, int col) {
			this.frame = frame;
			this.timeStep = timeStep;
			this.col = col;
			index = frame.getIndex();
			index.set(timeStep, 0, col, 0);
			domainExtent = frame.getAxes().getYAxis().getExtent();
			domainOrigin = frame.getAxes().getYAxis().getOrigin();
			layerExtent = frame.getAxes().getZAxis().getExtent();
		}


		public int getDomainExtent() {
			return domainExtent;
		}

		public double getValue(int item) {
			// x is the frame y value (row is the domain here)
			// y is the layer
			int frameY = item % domainExtent;
			int frameLayer = (item - frameY) / domainExtent;
			index.set(timeStep, frameLayer, col, frameY);
			return frame.getDouble(index);
		}

		/**
		 * @param domain the domain coordinate, in this case that is the
		 *               frame Y
		 * @param range  the range coordinate, in this case that is the
		 *               layer value.
		 * @return the value at the specified coords
		 */
		public double getValue(int domain, int range) {
			index.set(timeStep, range, col, domain);
			return frame.getDouble(index);
		}

		public int size() {
			return domainExtent * layerExtent;
		}

		public String getName() {
			return frame.getVariable().getName() + "Col";
		}

		public int getDomainOrigin() {
			return domainOrigin;
		}
	}

	private List<SeriesData> frames = new ArrayList<SeriesData>();

	/**
	 * Gets the value for the series at the specified x, y coordinate.
	 *
	 * @param series the series
	 * @param x      x coordinate
	 * @param y      y coordinate
	 * @return the value for the series at the specified x, y coordinate.
	 */
	public double getValue(int series, int x, int y) {
		SeriesData data = frames.get(series);
		return data.getValue(x, y);
	}

	public void addColSeries(DataFrame frame, int timeStep, int constantCol) {
		SeriesData seriesData = new ColSeriesData(frame, timeStep, constantCol);
		addSeriesData(seriesData);
	}

	private void addSeriesData(SeriesData seriesData) {
		int index = indexOf(seriesData.getName());
		if (index > -1) {
			frames.add(index, seriesData);
			frames.remove(index + 1);
		} else {
			frames.add(seriesData);
		}
		notifyListeners(new DatasetChangeEvent(this, this));
	}

	public void addRowSeries(DataFrame frame, int timeStep, int constantRow) {
		SeriesData seriesData = new RowSeriesData(frame, timeStep, constantRow);
		addSeriesData(seriesData);
	}

	/**
	 * Returns the z-value for the specified series and item.
	 *
	 * @param series the series index (zero-based).
	 * @param item   the item index (zero-based).
	 * @return The z-value (possibly <code>null</code>).
	 */
	public Number getZ(int series, int item) {
		return new Double(getZValue(series, item));
	}

	/**
	 * Returns the z-value (as a double primitive) for an item within a series.
	 *
	 * @param series the series (zero-based index).
	 * @param item   the item (zero-based index).
	 * @return The z-value.
	 */
	public double getZValue(int series, int item) {
		SeriesData data = frames.get(series);
		return data.getValue(item);
	}

	/**
	 * Returns the order of the domain (or X) values returned by the dataset.
	 *
	 * @return The order (never <code>null</code>).
	 */
	public DomainOrder getDomainOrder() {
		return DomainOrder.NONE;
	}

	/**
	 * Returns the number of items in a series.
	 * <br><br>
	 * It is recommended that classes that implement this method should throw
	 * an <code>IllegalArgumentException</code> if the <code>series</code>
	 * argument is outside the specified range.
	 *
	 * @param series the series index (in the range <code>0</code> to
	 *               <code>getSeriesCount() - 1</code>).
	 * @return The item count.
	 */
	public int getItemCount(int series) {
		return frames.get(series).size();
	}

	/**
	 * Returns the x-value for an item within a series.  The x-values may or
	 * may not be returned in ascending order, that is up to the class
	 * implementing the interface.
	 *
	 * @param series the series index (in the range <code>0</code> to
	 *               <code>getSeriesCount() - 1</code>).
	 * @param item   the item index (in the range <code>0</code> to
	 *               <code>getItemCount(series)</code>).
	 * @return The x-value (never <code>null</code>).
	 */
	public Number getX(int series, int item) {
		return new Double(getXValue(series, item));
	}

	/**
	 * Returns the x-value for an item within a series.
	 *
	 * @param series the series index (in the range <code>0</code> to
	 *               <code>getSeriesCount() - 1</code>).
	 * @param item   the item index (in the range <code>0</code> to
	 *               <code>getItemCount(series)</code>).
	 * @return The x-value.
	 */
	public double getXValue(int series, int item) {
		SeriesData data = frames.get(series);
		return (item % data.getDomainExtent()) + data.getDomainOrigin();
	}

	private double getXValueNoOffset(int series, int item) {
		SeriesData data = frames.get(series);
		return item % data.getDomainExtent();
	}

	/**
	 * Returns the y-value for an item within a series.
	 *
	 * @param series the series index (in the range <code>0</code> to
	 *               <code>getSeriesCount() - 1</code>).
	 * @param item   the item index (in the range <code>0</code> to
	 *               <code>getItemCount(series)</code>).
	 * @return The y-value (possibly <code>null</code>).
	 */
	public Number getY(int series, int item) {
		return new Double(getYValue(series, item));
	}

	/**
	 * Returns the y-value (as a double primitive) for an item within a series.
	 *
	 * @param series the series index (in the range <code>0</code> to
	 *               <code>getSeriesCount() - 1</code>).
	 * @param item   the item index (in the range <code>0</code> to
	 *               <code>getItemCount(series)</code>).
	 * @return The y-value.
	 */
	public double getYValue(int series, int item) {
		SeriesData data = frames.get(series);
		return (item - getXValueNoOffset(series, item)) / data.getDomainExtent();
	}

	/**
	 * Returns the number of series in the dataset.
	 *
	 * @return The series count.
	 */
	public int getSeriesCount() {
		return frames.size();
	}

	/**
	 * Returns the key for a series.
	 *
	 * @param series the series index (in the range <code>0</code> to
	 *               <code>getSeriesCount() - 1</code>).
	 * @return The key for the series.
	 */
	public Comparable getSeriesKey(int series) {
		if ((series < 0) || (series >= getSeriesCount())) {
			throw new IllegalArgumentException("Series index out of bounds");
		}
		CrossSectionXYZDataset.SeriesData data = frames.get(series);
		return data.getName();
	}

	/**
	 * Returns the index of the series with the specified key, or -1 if there
	 * is no such series in the dataset.
	 *
	 * @param seriesKey the series key (<code>null</code> permitted).
	 * @return The index, or -1.
	 */
	public int indexOf(Comparable seriesKey) {
		for (int i = 0; i < frames.size(); i++) {
			CrossSectionXYZDataset.SeriesData data = frames.get(i);
			if (data.getName().equals(seriesKey)) return i;
		}
		return -1;
	}
}
