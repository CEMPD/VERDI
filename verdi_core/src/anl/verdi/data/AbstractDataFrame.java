package anl.verdi.data;

import java.util.Collections;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

/**
 * Abstract implementation of DataFrame.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class AbstractDataFrame implements DataFrame {

	protected Array array;
	protected Axes<DataFrameAxis> axes;
	protected List<Dataset> datasets;
	protected Variable variable;
	protected double min = Double.NaN;
	protected double max = Double.NaN;

	/**
	 * Gets the Array that contains the data.
	 *
	 * @return the Array that contains the result data.
	 */
	public Array getArray() {
		return array;
	}

	/**
	 * Gets metadata describing this data frame's axes.
	 *
	 * @return metadata describing this data frame's axes.
	 */
	public Axes<DataFrameAxis> getAxes() {
		return axes;
	}

	/**
	 * Gets the dataset that produced this DataFrame.
	 *
	 * @return the dataset that produced this DataFrame.
	 */
	public List<Dataset> getDataset() {
		return Collections.unmodifiableList(datasets);
	}


	/**
	 * Gets a DataFrameIndex used to retrieve data from this DataFrame.
	 *
	 * @return a DataFrameIndex used to retrieve data from this DataFrame.
	 */
	public DataFrameIndex getIndex() {
		return new DataFrameIndex(this);
	}

	/**
	 * Gets the variable associated with this DataFrame.
	 *
	 * @return the variable associated with this DataFrame.
	 */
	public Variable getVariable() {
		return variable;
	}


	/**
	 * Gets the element type contained by the data array.
	 *
	 * @return the element type contained by the data array.
	 */
	public Class getArrayType() {
		return array.getElementType();
	}

	/**
	 * Gets the item at the specified indices as a double.
	 * The appropriate order of the indices can be gotten via the
	 * Axes information
	 *
	 * @param index the index of the data to retrieve.
	 * @return the item at the specified indices as a double.
	 */
	public double getDouble(DataFrameIndex index) {
		return array.getDouble(index.index);
	}

	/**
	 * Gets the item at the specified indices as a float.
	 * The appropriate order of the indices can be gotten via the
	 * Axes information
	 *
	 * @param index the index of the data to retrieve.
	 * @return the item at the specified indices as a float.
	 */
	public float getFloat(DataFrameIndex index) {
		return array.getFloat(index.index);
	}

	/**
	 * Gets the item at the specified indices as an int.
	 * The appropriate order of the indices can be gotten via the
	 * Axes information
	 *
	 * @param index the index of the data to retrieve.
	 * @return the item at the specified indices as an int.
	 */
	public int getInt(DataFrameIndex index) {
		return array.getInt(index.index);
	}

	/**
	 * Gets the item at the specified indices as a long.
	 * The appropriate order of the indices can be gotten via the
	 * Axes information
	 *
	 * @param index the index of the data to retrieve.
	 * @return the item at the specified indices as a long.
	 */
	public long getLong(DataFrameIndex index) {
		return array.getLong(index.index);
	}

	/**
	 * Gets the total number of items in the data array.
	 *
	 * @return the total number of items in the data array.
	 */
	public long getSize() {
		return array.getSize();
	}

	/**
	 * Gets the shape - the array dimensionality - of this DataFrame.
	 *
	 * @return the shape - the array dimensionality - of this DataFrame.
	 */
	public int[] getShape() {
		return array.getShape();
	}


	/**
	 * Creates a new DataFrame that is a slice or subsection of this one. The
	 * two DataFrames will share data, so any change to one will
	 * be reflected in the other. The slice ranges should be in terms of the
	 * array indices of the data frame which are NOT necessarily the axes
	 * origins and extents.
	 *
	 * @param slice describes the slice of data contained by the
	 *              new DataFrame.
	 * @return a new DataFrame that is a slice or subsection of this one
	 * @throws InvalidRangeException if the slice is not within the range
	 * of this DataFrame's dimensions.
	 */
	public DataFrame slice(Slice slice) throws InvalidRangeException {
		int[] origin = createOrigins(slice, axes);
		int[] extents = createExtents(slice, axes);
		Array newArray = array.sectionNoReduce(origin, extents, null);
		DataFrameBuilder builder = new DataFrameBuilder();
		builder.addDataset(datasets);
		builder.setArray(newArray);
		builder.setVariable(variable);
		for (DataFrameAxis dfAxis : axes.getAxes()) {
			int index = dfAxis.getArrayIndex();
			DataFrameAxis axis = DataFrameAxis.createDataFrameAxis(dfAxis.getAxis(), dfAxis.getOrigin() + origin[index], extents[index],
							index);
			builder.addAxis(axis);
		}
		return builder.createDataFrame();
	}

	/**
	 * Creates a new DataFrame that is a slice or subsection of this one. The
	 * two DataFrames will NOT share data, so any change to one will NOT
	 * be reflected in the other. The slice ranges should be in terms of the
	 * array indices of the data frame which are NOT necessarily the axes
	 * origins and extents.
	 *
	 * @param slice describes the slice of data contained by the
	 *              new DataFrame.
	 * @return a new DataFrame that is a slice or subsection of this one
	 * @throws InvalidRangeException if the slice is not within the range
	 * of this DataFrame's dimensions.
	 */
	public DataFrame sliceCopy(Slice slice) throws InvalidRangeException {
		int[] origin = createOrigins(slice, axes);
		int[] extents = createExtents(slice, axes);
		Array newArray = array.sectionNoReduce(origin, extents, null).copy();
		DataFrameBuilder builder = new DataFrameBuilder();
		builder.addDataset(datasets);
		builder.setArray(newArray);
		builder.setVariable(variable);
		for (DataFrameAxis dfAxis : axes.getAxes()) {
			int index = dfAxis.getArrayIndex();
			DataFrameAxis axis = DataFrameAxis.createDataFrameAxis(dfAxis.getAxis(), dfAxis.getOrigin() + origin[index], extents[index],
							index);
			builder.addAxis(axis);
		}
		return builder.createDataFrame();
	}

	public static int[] createExtents(Slice slice, Axes<DataFrameAxis> axes) {
		int size = axes.getAxes().size();
		int[] extents = new int[size];
		if (axes.getTimeAxis() != null) {
			DataFrameAxis time = axes.getTimeAxis();
			fillExtents(extents, slice.getTimeRange(), time);
		}

		if (axes.getZAxis() != null) {
			DataFrameAxis layer = axes.getZAxis();
			fillExtents(extents, slice.getLayerRange(), layer);
		}

		if (axes.getXAxis() != null) {
			DataFrameAxis x = axes.getXAxis();
			fillExtents(extents, slice.getXRange(), x);
		}

		if (axes.getYAxis() != null) {
			DataFrameAxis y = axes.getYAxis();
			fillExtents(extents, slice.getYRange(), y);
		}

		if (axes.getCellAxis() != null) {
			DataFrameAxis cell = axes.getCellAxis();
			fillExtents(extents, slice.getCellRange(), cell);
		}

		return extents;
	}

	private static void fillExtents(int[] extents, Range range, DataFrameAxis axis) {
		if (range == null) {
			extents[axis.getArrayIndex()] = (int)(axis.getRange().getExtent());
		} else {
			extents[axis.getArrayIndex()] = (int)(range.getExtent());
		}
	}

	private static void fillOrigin(int[] origins, Range range, DataFrameAxis axis) {
		if (range == null) {
			origins[axis.getArrayIndex()] = 0;
		} else {
			origins[axis.getArrayIndex()] = (int) range.getOrigin();
		}
	}

	public static int[] createOrigins(Slice slice, Axes<DataFrameAxis> axes) {
		int size = axes.getAxes().size();
		int[] origins = new int[size];
		if (axes.getTimeAxis() != null) {
			DataFrameAxis time = axes.getTimeAxis();
			fillOrigin(origins, slice.getTimeRange(), time);
		}

		if (axes.getZAxis() != null) {
			DataFrameAxis layer = axes.getZAxis();
			fillOrigin(origins, slice.getLayerRange(), layer);
		}

		if (axes.getXAxis() != null) {
			DataFrameAxis x = axes.getXAxis();
			fillOrigin(origins, slice.getXRange(), x);
		}

		if (axes.getYAxis() != null) {
			DataFrameAxis y = axes.getYAxis();
			fillOrigin(origins, slice.getYRange(), y);
		}

		if (axes.getCellAxis() != null) {
			DataFrameAxis cell = axes.getCellAxis();
			fillOrigin(origins, slice.getCellRange(), cell);
		}

		return origins;
	}}