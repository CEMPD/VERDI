/**
 * ArrayReader - Interface to allow callers to read numeric array data without needing to account for
 * underlying data type of the array
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.data;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayFloat;

public abstract class ArrayReader {
	
	protected Array array;
	
	public static ArrayReader getReader(Array array) {
		if (array == null)
			return null;
		if (array instanceof ArrayDouble)
			return new ArrayDoubleReader((ArrayDouble)array);
		if (array instanceof ArrayFloat)
			return new ArrayFloatReader((ArrayFloat)array);
		return null;
	}
	
	public abstract int getRank();
	
	public abstract double get(int d1);
	
	public abstract double get(int d1, int d2);
	
	public abstract double get(int d1, int d2, int d3);
	
	public Array getArray() {
		return array;
	}
}
