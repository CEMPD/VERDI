package ucar.ma2;

public class ArrayLogFactory {
	
	public static Array getArray(Array source, double base) {
		if (source instanceof ArrayFloat)
			return new ArrayFloatLog((ArrayFloat)source, base);
		else if (source instanceof ArrayDouble)
			return new ArrayDoubleLog((ArrayDouble)source, base);
		return null;
	}

}
