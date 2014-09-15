package anl.verdi.data;

import ucar.ma2.Index;

/**
 * Encapsulates an index into a DataFrame data array.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DataFrameIndex {

	private final int xIndex, yIndex, tIndex, kIndex;
	private final int[] indices;
	/* private */ final Index index;

	/**
	 * Creates a DataFrameIndex for the specified DataFrame.
	 *
	 * @param frame the DataFrame that this is an index for
	 */
	public DataFrameIndex(DataFrame frame) {
		Axes<DataFrameAxis> axes = frame.getAxes();
		int count = 0;

		if (axes.getXAxis() != null) {
			xIndex = axes.getXAxis().getArrayIndex();
			count++;
		} else {
			xIndex = -1;
		}

		if (axes.getYAxis() != null) {
			yIndex = axes.getYAxis().getArrayIndex();
			count++;
		} else {
			yIndex = -1;
		}

		if (axes.getTimeAxis() != null) {
			tIndex = axes.getTimeAxis().getArrayIndex();
			count++;
		} else {
			tIndex = -1;
		}

		if (axes.getZAxis() != null) {
			kIndex = axes.getZAxis().getArrayIndex();
			count++;
		} else {
			kIndex = -1;
		}

		indices = new int[count];
		index = frame.getArray().getIndex();
	}

	/**
	 * Sets the time step index value.
	 *
	 * @param timeStep the time step.
	 */
	public void setTime(int timeStep) {

		if ( tIndex == -1 ) {

			if ( timeStep != 0 ) {
				throw new IllegalArgumentException( "Invalid call to DataFrameIndex.setTime( timeStep = " + timeStep + ")" );
			}
		} else {
			indices[tIndex] = timeStep;
			index.set(indices);
		}
	}

	/**
	 * Sets the layer index value.
	 *
	 * @param layer the layer index value
	 */
	public void setLayer(int layer) {

		if ( kIndex == -1 ) {

			if ( layer != 0 ) {
				throw new IllegalArgumentException( "Invalid call to DataFrameIndex.setLayer( layer = " + layer + ")" );
			}
		} else {
			indices[kIndex] = layer;
			index.set(indices);
		}
	}

	/**
	 * Sets the x and y index value
	 *
	 * @param x the x value
	 * @param y the y value
	 */
	public void setXY(int x, int y) {
		boolean updated = false;

		if ( xIndex == -1 ) {

			if ( x != 0 ) {
				throw new IllegalArgumentException( "Invalid call to DataFrameIndex.setXY( x = " + x + " ... )" );
			}
		} else {
			indices[ xIndex ] = x;
			updated = true;
		}

		if ( yIndex == -1 ) {

			if ( y != 0 ) {
				throw new IllegalArgumentException( "Invalid call to DataFrameIndex.setXY( ... y = " + y + " )" );
			}
		} else {
			indices[ yIndex ] = y;
			updated = true;
		}

		if ( updated ) {
			index.set( indices );			
		}
	}

	/**
	 * Sets all the indices for all the coordinate axes.
	 *
	 * @param timeStep the time set p index
	 * @param layer the layer index
	 * @param x the x index
	 * @param y the y index
	 */
	public void set(int timeStep, int layer, int x, int y) {
		setTime( timeStep );
		setLayer( layer );
		setXY( x, y );
	}
}
