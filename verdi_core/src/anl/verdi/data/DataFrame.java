package anl.verdi.data;

import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;

/**
 * Array-based data and metadata for a single variable.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface DataFrame {

	/**
	 * Gets the Array that contains the data.
	 *
	 * @return the Array that contains the result data.
	 */
	Array getArray();

	/**
	 * Gets metadata describing this data frame's axes.
	 *
	 * @return metadata describing this data frame's axes.
	 */
	Axes<DataFrameAxis> getAxes();

	/**
	 * Gets the dataset that produced this DataFrame.
	 *
	 * @return the dataset that produced this DataFrame.
	 */
	List<Dataset> getDataset();

	/**
	 * Gets the variable associated with this DataFrame.
	 *
	 * @return the variable associated with this DataFrame.
	 */
	Variable getVariable();

	/**
	 * Gets the element type contained by the data array.
	 *
	 * @return the element type contained by the data array.
	 */
	Class getArrayType();

	/**
	 * Gets the total number of items in the data array.
	 *
	 * @return the total number of items in the data array.
	 */
	long getSize();

	/**
	 * Gets the shape - the array dimensionality - of this DataFrame.
	 *
	 * @return the shape - the array dimensionality - of this DataFrame.
	 */
	int[] getShape();

	/**
	 * Gets a DataFrameIndex used to retrieve data from this DataFrame.
	 *
	 * @return a DataFrameIndex used to retrieve data from this DataFrame.
	 */
	DataFrameIndex getIndex();

	/**
	 * Gets the item at the specified indices as a double.
	 * The appropriate order of the indices can be gotten via the
	 * Axes information
	 *
	 * @param index the index of the data to retrieve.
	 * @return the item at the specified indices as a double.
	 */
	double getDouble(DataFrameIndex index);

	/**
	 * Gets the item at the specified indices as a float.
	 * The appropriate order of the indices can be gotten via the
	 * Axes information
	 *
	 * @param index the index of the data to retrieve.
	 * @return the item at the specified indices as a float.
	 */
	float getFloat(DataFrameIndex index);

	/**
	 * Gets the item at the specified indices as an int.
	 * The appropriate order of the indices can be gotten via the
	 * Axes information
	 *
	 * @param index the index of the data to retrieve.
	 * @return the item at the specified indices as an int.
	 */
	int getInt(DataFrameIndex index);

	/**
	 * Gets the item at the specified indices as a long.
	 * The appropriate order of the indices can be gotten via the
	 * Axes information
	 *
	 * @param index the index of the data to retrieve.
	 * @return the item at the specified indices as a long.
	 */
	long getLong(DataFrameIndex index);

	/**
	 * Creates a new DataFrame that is a slice or subsection of this one. The
	 * two DataFrames will share data, so any change to one will
	 * be reflected in the other. The slice ranges should be in terms of the
	 * array indices of the data frame which are NOT necessarily the axes
	 * origins and extents.
	 *
	 * @param slice describes the slice of data contained by the
	 * new DataFrame.
	 * @return a new DataFrame that is a slice or subsection of this one
	 * @throws InvalidRangeException if the slice is not within the range
	 * of this DataFrame's dimensions.
	 */
	DataFrame slice(Slice slice) throws InvalidRangeException;

	/**
	 * Creates a new DataFrame that is a slice or subsection of this one. The
	 * two DataFrames will NOT share data, so any change to one will NOT
	 * be reflected in the other. The slice ranges should be in terms of the
	 * array indices of the data frame which are NOT necessarily the axes
	 * origins and extents.
	 *
	 * @param slice describes the slice of data contained by the
	 * new DataFrame.
	 * @return a new DataFrame that is a slice or subsection of this one
	 * @throws InvalidRangeException if the slice is not within the range
	 * of this DataFrame's dimensions.
	 */
	DataFrame sliceCopy(Slice slice) throws InvalidRangeException;
}
