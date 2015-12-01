package anl.verdi.data;

import ucar.ma2.Index;

/**
 * Encapsulates an index into a DataFrame data array.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class MPASDataFrameIndex extends DataFrameIndex {

	private final int cellIndex;

	/**
	 * Creates a DataFrameIndex for the specified DataFrame.
	 *
	 * @param frame the DataFrame that this is an index for
	 */
	public MPASDataFrameIndex(DataFrame frame) {
		super(frame);
		Axes<DataFrameAxis> axes = frame.getAxes();
		int count = 0;

		if (axes.getCellAxis() != null) {
			cellIndex = axes.getCellAxis().getArrayIndex();
			count++;
		} else {
			cellIndex = -1;
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
	 * Sets the x and y index value
	 *
	 * @param x the x value
	 * @param y the y value
	 */
	public void setXY(int x, int y) {
		throw new IllegalArgumentException("Unsupported operation on MPAS data frames");
	}

	/**
	 * Sets the x and y index value
	 *
	 * @param x the x value
	 * @param y the y value
	 */
	public void setCell(int cell) {
		boolean updated = false;

		if ( cellIndex == -1 ) {

			if ( cell != 0 ) {
				throw new IllegalArgumentException( "Invalid call to DataFrameIndex.setCell( cell = " + cell + " ... )" );
			}
		} else {
			indices[ cellIndex ] = cell;
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
		throw new IllegalArgumentException("Unsupported operation on MPAS data frames");
	}
	
	/**
	 * Sets all the indices for all the coordinate axes.
	 *
	 * @param timeStep the time set p index
	 * @param layer the layer index
	 * @param x the x index
	 * @param y the y index
	 */
	public void set(int timeStep, int layer, int cell) {
		setTime( timeStep );
		setLayer( layer );
		setCell( cell );
	}

}
