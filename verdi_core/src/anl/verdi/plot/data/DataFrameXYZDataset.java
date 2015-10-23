package anl.verdi.plot.data;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.XYZDataset;

import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameIndex;

/**
 * JChart XYZDataset implemented in terms of a DataFrame.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DataFrameXYZDataset extends AbstractDataset implements XYZDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = 670185923166638229L;

	private static class SeriesData {
		DataFrame frame;
		int xExtent, yExtent;
		int xOrigin, yOrigin;
		DataFrameIndex index;


		public SeriesData(DataFrame frame, int timeStep, int layer) {
			this.frame = frame;
			index = frame.getIndex();
			if (frame.getAxes().getZAxis() == null) {
				index.setTime(timeStep);
			} else {
				index.set(timeStep, layer, 0, 0);
			}

			DataFrameAxis xAxis = frame.getAxes().getXAxis();
			xExtent = xAxis.getExtent();
			xOrigin = xAxis.getOrigin();
			DataFrameAxis yAxis = frame.getAxes().getYAxis();
			yExtent = yAxis.getExtent();
			yOrigin = yAxis.getOrigin();
		}

		public double getValue(int item) {
			int x = item % xExtent;
			int y = (item - x) / xExtent;
			index.setXY(x, y);
			return frame.getDouble(index);
		}

		public double getValue(int x, int y) {
			index.setXY(x, y);
			return frame.getDouble(index);
		}

		public int size() {
			return xExtent * yExtent;
		}
	}

	private List<SeriesData> frames = new ArrayList<SeriesData>();

	/**
	 * Gets the value for the series at the specified x, y coordinate.
	 * @param series the series
	 * @param x x coordinate
	 * @param y y coordinate
	 * @return the value for the series at the specified x, y coordinate.
	 */
	public double getValue(int series, int x, int y) {
		SeriesData data = frames.get(series);
		return data.getValue(x, y);
	}

	public void addSeries(DataFrame frame, int timeStep, int layer) {
		SeriesData seriesData = new SeriesData(frame, timeStep, layer);
		int index = indexOf(frame.getVariable().getName());
		if (index > -1) {
			frames.add(index, seriesData);
			frames.remove(index + 1);
		} else {
			frames.add(seriesData);
		}
		notifyListeners(new DatasetChangeEvent(this, this));
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
		return (item % data.xExtent) + data.xOrigin;
	}

	private double getXValueNoOffset(int series, int item) {
		SeriesData data = frames.get(series);
		return item % data.xExtent;
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
		return (item - getXValueNoOffset(series, item)) / data.xExtent + data.yOrigin;
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
		SeriesData data = frames.get(series);
		return data.frame.getVariable().getName();
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
			SeriesData data = frames.get(i);
			if (data.frame.getVariable().getName().equals(seriesKey)) return i;
		}
		return -1;
	}
}
