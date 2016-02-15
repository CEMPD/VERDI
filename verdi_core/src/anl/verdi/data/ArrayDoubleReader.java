package anl.verdi.data;

import ucar.ma2.ArrayDouble;

public class ArrayDoubleReader extends ArrayReader {
	
	ArrayDouble array;
	int dimensions = 0;

	public ArrayDoubleReader(ArrayDouble source) {
		array = source;
		if (source instanceof ArrayDouble.D3)
			dimensions = 3;
		else if (source instanceof ArrayDouble.D2)
			dimensions = 2;
	}
	
	public double get(int d1, int d2) {
		return ((ArrayDouble.D2)array).get(d1, d2);
	}
	
	public double get(int d1, int d2, int d3) {
		return ((ArrayDouble.D3)array).get(d1, d2, d3);
	}
	
	public int getRank() {
		return dimensions;
	}

}
