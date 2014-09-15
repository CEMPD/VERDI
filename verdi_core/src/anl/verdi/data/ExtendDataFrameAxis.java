package anl.verdi.data;

public class ExtendDataFrameAxis extends ExtendAxisRange {
	private int index;

	/**
	 * Creates a DataFrameAxis for the specified axis with origin and extent
	 * equal to that of the axis.
	 *
	 * @param axis  the axis
	 * @param index the array index of this axis
	 * @return the created DataFrameAxis
	 */
	public static ExtendDataFrameAxis createDataFrameAxis(ExtendCoordAxis axis, int index) {
		if (axis.getAxisType() == AxisType.TIME) {
			return new ExtendDataFrameTimeAxis((ExtendTimeCoordAxis) axis, index);
		} else {
			return new ExtendDataFrameAxis(axis, index);
		}
	}

	/**
	 * Creates a DataFrameAxis for the specified axis with the specified origin and extent.
	 *
	 * @param origin the origin of the range
	 * @param extent the extent of the range
	 * @param axis   the axis
	 * @param index  the array index of this axis
	 * @return the create axis.
	 */
	public static ExtendDataFrameAxis createDataFrameAxis(ExtendCoordAxis axis, int origin, int extent, int index) {
		if (axis.getAxisType() == AxisType.TIME) {
			return new ExtendDataFrameTimeAxis((ExtendTimeCoordAxis) axis, origin, extent, index);
		} else {
			return new ExtendDataFrameAxis(axis, origin, extent, index);
		}
	}

	/**
	 * Creates a DataFrameAxis from the specified axis and index. This
	 * DataFrameAxis will have the same origin and extent as the
	 * specified axis.
	 *
	 * @param axis  the axis to create this DataFrameAxis from
	 * @param index the array index of this axis
	 * @return the create axis
	 */
	public static ExtendDataFrameAxis createDataFrameAxis(ExtendDataFrameAxis axis, int index) {
		return ExtendDataFrameAxis.createDataFrameAxis(axis.axis, axis.getOrigin(), axis.getExtent(), index);
	}

	/**
	 * Creates a DataFrameAxis for the specified axis with origin and extent
	 * equal to that of the axis.
	 *
	 * @param axis  the axis
	 * @param index the array index of this axis
	 */
	protected ExtendDataFrameAxis(ExtendCoordAxis axis, int index) {
		this(axis, (int) axis.getRange().getOrigin(), (int) axis.getRange().getExtent(), index);
	}

	/**
	 * Creates a DataFrameAxis for the specified axis with the specified origin and extent.
	 *
	 * @param origin the origin of the range
	 * @param extent the extent of the range
	 * @param axis   the axis
	 * @param index  the array index of this axis
	 */
	protected ExtendDataFrameAxis(ExtendCoordAxis axis, int origin, int extent, int index) {
		super(axis, origin, extent);
		this.index = index;
	}

	/**
	 * Creates a DataFrameAxis from the specified axis and index. This
	 * DataFrameAxis will have the same origin and extent as the
	 * specified axis.
	 *
	 * @param axis  the axis to create this DataFrameAxis from
	 * @param index the array index of this axis
	 */
	protected ExtendDataFrameAxis(ExtendDataFrameAxis axis, int index) {
		this(axis.axis, axis.getOrigin(), axis.getExtent(), index);
	}

	/**
	 * Gets the index of the array dimension that corresponds to this axis.
	 *
	 * @return the index of the array dimension that corresponds to this axis.
	 */
	public int getArrayIndex() {
		return index;
	}
}
