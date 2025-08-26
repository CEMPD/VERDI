package anl.verdi.util;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory for creating arrays.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ArrayFactory {

	// key is type to convert, map -> key = type to convert, value is result
	static Map<Class, Map<Class, Class>> conversionMap = new HashMap<Class, Map<Class, Class>>();

	static {
		// double
		Map<Class, Class> map = new HashMap<Class, Class>();
		map.put(int.class, double.class);
		map.put(float.class, double.class);
		map.put(long.class, double.class);
		map.put(double.class, double.class);
		conversionMap.put(double.class, map);

		// float
		map = new HashMap<Class, Class>();
		map.put(int.class, float.class);
		map.put(float.class, float.class);
		map.put(long.class, double.class);
		map.put(double.class, double.class);
		conversionMap.put(float.class, map);

		// int
		map = new HashMap<Class, Class>();
		map.put(int.class, int.class);
		map.put(float.class, float.class);
		map.put(long.class, long.class);
		map.put(double.class, double.class);
		conversionMap.put(int.class, map);

		// long
		map = new HashMap<Class, Class>();
		map.put(int.class, long.class);
		map.put(float.class, double.class);
		map.put(long.class, long.class);
		map.put(double.class, double.class);
		conversionMap.put(long.class, map);
	}

	/**
	 * Creates an array whose element type is sufficient to hold all
	 * the values in each of the passed in arrays. Only works with primitive
	 * numeric types.
	 *
	 * @param arrays the arrays whose types the created array must be able
	 *               to hold.
	 * @return the created array
	 * @throws IllegalArgumentException if the passed in arrays do not
	 *                                  have the same shape, or array element type is not an int, float,
	 *                                  long or double.
	 */
	public static Array createArray(Array... arrays) throws IllegalArgumentException {
		int[] shape = arrays[0].getShape();
		Class type = arrays[0].getElementType();
		for (Array array : arrays) {
			if (!Arrays.equals(shape, array.getShape())) throw new IllegalArgumentException("Arrays have unequal shapes");
			Class elementType = array.getElementType();
			if (!conversionMap.containsKey(elementType)) throw new IllegalArgumentException("Array is not a numeric type");
			Map<Class, Class> map = conversionMap.get(type);
			type = map.get(elementType);
		}
		
		return Array.factory(DataType.getType(type, false), shape);
	}

	/**
	 * Creates a double array of the specified shape. This will attempt
	 * to use the more efficient ArrayDouble.D* classes if possible.
	 *
	 * @param shape the shape of the created array
	 * @return the created array.
	 */
	public static Array createDoubleArray(int[] shape) {
		switch (shape.length) {
			case 0:
				return new ArrayDouble.D0();
			case 1:
				return new ArrayDouble.D1(shape[0]);
			case 2:
				return new ArrayDouble.D2(shape[0], shape[1]);
			case 3:
				return new ArrayDouble.D3(shape[0], shape[1], shape[2]);
			case 4:
				return new ArrayDouble.D4(shape[0], shape[1], shape[2], shape[3]);
			case 5:
				return new ArrayDouble.D5(shape[0], shape[1], shape[2], shape[3], shape[4]);
			case 6:
				return new ArrayDouble.D6(shape[0], shape[1], shape[2], shape[3], shape[4], shape[5]);
			case 7:
				return new ArrayDouble.D7(shape[0], shape[1], shape[2], shape[3], shape[4], shape[5], shape[6]);
			default:
				return Array.factory(DataType.DOUBLE, shape);
		}
	}
}
