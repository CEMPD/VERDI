package anl.verdi.plot.data;

import java.util.ArrayList;
import java.util.List;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.XYDataset;

import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.MPASDataFrameIndex;

/**
 * Dataset appropriate for a scatter plot. Each series has two frames. The xValue is
 * the value from one frame and the yValue from another.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ScatterXYDataset extends AbstractDataset implements XYDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4325988415625810735L;

	private static class FrameData {
		DataFrame frame;
		int xExtent, yExtent;
		DataFrameIndex index;


		public FrameData(DataFrame frame, int timeStep, int layer) {
			this.frame = frame;
			index = frame.getIndex();
			if (frame.getAxes().getZAxis() == null) {
				index.setTime(timeStep);
			} else {
				if (index instanceof MPASDataFrameIndex) {
					((MPASDataFrameIndex)index).set(timeStep,  layer,  0);
					xExtent = frame.getAxes().getCellAxis().getExtent();
					yExtent = 1;
					return;
				}
				else
					index.set(timeStep, layer, 0, 0);
			}
			xExtent = frame.getAxes().getXAxis().getExtent();
			yExtent = frame.getAxes().getYAxis().getExtent();
		}

		public double getValue(int item) {
			if (index instanceof MPASDataFrameIndex) {
				((MPASDataFrameIndex)index).setCell(item);
			}
			else {
				int x = item % xExtent;
				int y = (item - x) / xExtent;
				index.setXY(x, y);
			}
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

	private static class SeriesData {
		private FrameData xFrame, yFrame;
		private String name;

		public SeriesData(DataFrame xFrame, DataFrame yFrame, int timeStep, int layer) {
			this.xFrame = new FrameData(xFrame, timeStep, layer);
			this.yFrame = new FrameData(yFrame, timeStep, layer);
			name = xFrame.getVariable().getName() + ":" + yFrame.getVariable().getName();
		}

		public FrameData getXFrame() {
			return xFrame;
		}

		public FrameData getYFrame() {
			return yFrame;
		}

		public int size() {
			return xFrame.size();
		}

		public Comparable getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
	}

	private List<SeriesData> frames = new ArrayList<SeriesData>();

	public void addSeries(DataFrame xFrame, DataFrame yFrame, int timeStep, int layer) {
		SeriesData seriesData = new SeriesData(xFrame, yFrame, timeStep, layer);
		int index = indexOf(seriesData.getName());
		if (index > -1) {
			frames.add(index, seriesData);
			frames.remove(index + 1);
		} else {
			frames.add(seriesData);
		}
		notifyListeners(new DatasetChangeEvent(this, this));
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
		return data.getXFrame().getValue(item);
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
		return data.getYFrame().getValue(item);
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
		return data.getName();
	}

	public void setSeriesKey(int series, String key) {
			if ((series < 0) || (series >= getSeriesCount())) {
			throw new IllegalArgumentException("Series index out of bounds");
		}
		SeriesData data = frames.get(series);
		data.setName(key);
		fireDatasetChanged();
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
			if (data.getName().equals(seriesKey)) return i;
		}
		return -1;
	}
}
