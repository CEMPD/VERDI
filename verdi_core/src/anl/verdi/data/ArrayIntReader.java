/**
 * ArrayIntReader - ArrayReader to return data from float arrays as double values
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.data;

import ucar.ma2.ArrayInt;

public class ArrayIntReader extends ArrayReader {
	
	int dimensions = 0;
	
	public ArrayIntReader(ArrayInt source) {
		array = source;
		if (source instanceof ArrayInt.D3)
			dimensions = 3;
		else if (source instanceof ArrayInt.D2)
			dimensions = 2;
	}
	
	public double get(int d1) {
		return ((ArrayInt.D1)array).getDouble(d1);
	}
	
	public double get(int d1, int d2) {
		return ((ArrayInt.D2)array).get(d1, d2);
	}
	
	public double get(int d1, int d2, int d3) {
		return ((ArrayInt.D3)array).get(d1, d2, d3);
	}
	
	public int getRank() {
		return dimensions;
	}

	@Override
	public double get(DataFrame frame, DataFrameIndex idx) {
		return frame.getDouble(idx);
	}

}
