package anl.verdi.plot.data;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.Vector;
import org.jfree.data.xy.VectorXYDataset;

import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameIndex;
// 2014
// 2014 replacing System.out.println with logger messages

/**
 * Adapts data frame data to VectorXYDataset
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DFVectorXYDataset extends AbstractDataset implements VectorXYDataset {

	/**
	 * 
	 */
	private static final long serialVersionUID = -489641757180084647L;
	static final Logger Logger = LogManager.getLogger(DFVectorXYDataset.class.getName());

	private static class FrameData {
		DataFrame frame;
		int xExtent, yExtent, xOrigin, yOrigin;
		DataFrameIndex index;


		public FrameData(DataFrame frame, int timeStep, int layer) {
			Logger.debug("in constructor for private static class FrameData within DFVectorXYDataset");
			this.frame = frame;
			index = frame.getIndex();
			if (frame.getAxes().getZAxis() == null) {
				index.setTime(timeStep);
			} else {
				index.set(timeStep, layer, 0, 0);
			}
			xExtent = frame.getAxes().getXAxis().getExtent();
			xOrigin = frame.getAxes().getXAxis().getOrigin();
			yExtent = frame.getAxes().getYAxis().getExtent();
			yOrigin = frame.getAxes().getYAxis().getOrigin();
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

	private static class SeriesData {
		private FrameData xFrame, yFrame;
		private String name;

		public SeriesData(DataFrame xFrame, DataFrame yFrame, int timeStep, int layer) {
			Logger.debug("in constructor for private static class SeriesData within DFVectorXYDataset");
			this.xFrame = new FrameData(xFrame, timeStep, layer);
			this.yFrame = new FrameData(yFrame, timeStep, layer);
			name = xFrame.getVariable().getName() + "x" + yFrame.getVariable().getName();
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
	}


	private List<SeriesData> data = new ArrayList<SeriesData>();

	public void addSeries(DataFrame xVect, DataFrame yVect, int timeStep, int layer) {
			SeriesData seriesData = new SeriesData(xVect, yVect, timeStep, layer);
			int index = indexOf(seriesData.getName());
			if (index > -1) {
				data.add(index, seriesData);
				data.remove(index + 1);
			} else {
				data.add(seriesData);
			}
			notifyListeners(new DatasetChangeEvent(this, this));
		}

	/**
	 * Returns the x-component of the vector.
	 *
	 * @param series the series index.
	 * @param item   the item index.
	 * @return The x-component of the vector.
	 */
	public Number getDeltaX(int series, int item) {
		return new Double(getXValue(series, item));
	}

	/**
	 * Returns the x-component of the vector.
	 *
	 * @param series the series index.
	 * @param item   the item index.
	 * @return The x-component of the vector.
	 */
	public double getDeltaXValue(int series, int item) {
		SeriesData sd = data.get(series);
		return sd.xFrame.getValue(item);
		/*
		double yVal = sd.yFrame.getValue(item);
		// make unit vector
		double mag = Math.sqrt(xVal * xVal + yVal * yVal);
		return xVal / mag;
		*/

	}

	/**
	 * Returns the y-component of the vector.
	 *
	 * @param series the series index.
	 * @param item   the item index.
	 * @return The y-component of the vector.
	 */
	public Number getDeltaY(int series, int item) {
		return new Double(getYValue(series, item));
	}

	/**
	 * Returns the y-component of the vector.
	 *
	 * @param series the series index.
	 * @param item   the item index.
	 * @return The y-component of the vector.
	 */
	public double getDeltaYValue(int series, int item) {
		SeriesData sd = data.get(series);
		return sd.yFrame.getValue(item);
		/*
		double xVal = sd.xFrame.getValue(item);
		double yVal = sd.yFrame.getValue(item);
		// make unit vector
		double mag = Math.sqrt(xVal * xVal + yVal * yVal);
		return yVal / mag;
		*/
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
		return data.get(series).size();
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
		SeriesData sd = data.get(series);
		// we could use either frame here -- the requirement
		// is that both frames have identical x/y domain
		return (item % sd.xFrame.xExtent) + sd.xFrame.xOrigin;
	}

	private double getXValueNoOffset(int series, int item) {
		SeriesData sd = data.get(series);
		return item % sd.xFrame.xExtent;
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
		SeriesData sd = data.get(series);
		// we could use either frame here -- the requirement
		// is that both frames have identical x/y domain
		return (item - getXValueNoOffset(series, item)) / sd.xFrame.xExtent + sd.xFrame.yOrigin;
	}

	/**
	 * Returns the number of series in the dataset.
	 *
	 * @return The series count.
	 */
	public int getSeriesCount() {
		return data.size();
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
		SeriesData sd = data.get(series);
		return sd.getName();
	}

	/**
	 * Returns the index of the series with the specified key, or -1 if there
	 * is no such series in the dataset.
	 *
	 * @param seriesKey the series key (<code>null</code> permitted).
	 * @return The index, or -1.
	 */
	public int indexOf(Comparable seriesKey) {
		for (int i = 0; i < data.size(); i++) {
			SeriesData sd = data.get(i);
			if (sd.getName().equals(seriesKey)) return i;
		}
		return -1;
	}

	@Override		// 2014 NOTE: always returns 0
	public double getVectorXValue(int series, int item) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override		// 2014 NOTE: always returns 0
	public double getVectorYValue(int series, int item) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override		// 2014 NOTE: always returns NULL
	public Vector getVector(int series, int item) {
		// TODO Auto-generated method stub
		return null;
	}
}
