/**
 * ArrayFloatReader - ArrayReader to return data from float arrays as double values
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.data;

import ucar.ma2.ArrayFloat;

public class ArrayFloatReader extends ArrayReader {
	
	int dimensions = 0;
	
	public ArrayFloatReader(ArrayFloat source) {
		array = source;
		if (source instanceof ArrayFloat.D3)
			dimensions = 3;
		else if (source instanceof ArrayFloat.D2)
			dimensions = 2;
	}
	
	public double get(int d1) {
		return ((ArrayFloat.D1)array).getDouble(d1);
	}
	
	public double get(int d1, int d2) {
		return ((ArrayFloat.D2)array).get(d1, d2);
	}
	
	public double get(int d1, int d2, int d3) {
		return ((ArrayFloat.D3)array).get(d1, d2, d3);
	}
	
	public int getRank() {
		return dimensions;
	}

}
