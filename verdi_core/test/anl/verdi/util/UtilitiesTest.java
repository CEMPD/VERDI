package anl.verdi.util;

import java.util.Arrays;

import junit.framework.TestCase;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.ma2.Range;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class UtilitiesTest extends TestCase {

	public void testArrayFactory() {
		Array array1 = ArrayInt.factory(int.class, new int[]{2, 2});
		Array array2 = ArrayFloat.factory(float.class, new int[]{2, 2});

		Array array = ArrayFactory.createArray(array1, array2);
		assertEquals(float.class, array.getElementType());
		assertTrue(Arrays.equals(new int[]{2, 2}, array.getShape()));
	}

	public void testSection() throws InvalidRangeException {
		Array array1 = ArrayInt.factory(int.class, new int[]{4, 4, 2, 2});
		int val = 0;
		Index index = array1.getIndex();
		for (int t = 0; t < 4; t++) {
			for (int k = 0; k < 4; k++) {
				for (int i = 0; i < 2; i++) {
					for (int j = 0; j < 2; j++) {
						index.set(t, k, i, j);
						array1.setDouble(index, val++);
					}
				}
			}
		}

		Range[] ranges = new Range[]{new Range(2, 2), new Range(2, 2), new Range(0, 1), new Range(0, 1)};
		Array array2 = array1.section(Arrays.asList(ranges));
		index = array2.getIndex();
		val = 40;
		for (int i = 0; i < 2; i++) {
			for (int j = 0; j < 2; j++) {
				index.set(i, j);
				assertEquals(val++, array2.getInt(index));
			}
		}
	}

	public void testDoubleArray() {
		for (int i = 0; i < 8; i++) {
			int[] shape = new int[i];
			Arrays.fill(shape, 2);
			Array array = ArrayFactory.createDoubleArray(shape);
			assertTrue(Arrays.equals(shape, array.getShape()));
		}
	}

	public void testAliasGenerator() {
		AliasGenerator gen = new AliasGenerator();
		for (int i = 0; i < 100; i++) {
			assertEquals("[" + (i + 1) + "]", gen.getNextAlias());
		}

		String[] vals = gen.splitAlias("O3[0]");
		assertEquals("O3", vals[1]);
		assertEquals("[0]", vals[0]);
	}

	public void testArrayTimes() {
		Array array = createArray(3, 2, 3);
		FormulaArray fa1 = new FormulaArray(array);
		FormulaArray fa2 = new FormulaArray(createArray(3));

		FormulaArray result = fa1.times(fa2);

		Index arrayIndex = array.getIndex();
		Index resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				assertEquals(array.getDouble(arrayIndex) * 3, result.getArray().getDouble(resultIndex));
			}
		}

		result = fa2.times(fa1);
		resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				assertEquals(array.getDouble(arrayIndex) * 3, result.getArray().getDouble(resultIndex));
			}
		}

		result = fa1.times(fa1);
		resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				double val = array.getDouble(arrayIndex);
				assertEquals(val * val, result.getArray().getDouble(resultIndex));
			}
		}

		result = fa2.times(fa2);
		resultIndex = result.getArray().getIndex();
		assertEquals(result.getArray().getSize(), 1);
		resultIndex.set(0);
		assertEquals(9.0, result.getArray().getDouble(resultIndex));
	}

	public void testArrayPlus() {
		Array array = createArray(3, 2, 3);
		FormulaArray fa1 = new FormulaArray(array);
		FormulaArray fa2 = new FormulaArray(createArray(3));

		FormulaArray result = fa1.plus(fa2);

		Index arrayIndex = array.getIndex();
		Index resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				assertEquals(array.getDouble(arrayIndex) + 3, result.getArray().getDouble(resultIndex));
			}
		}

		result = fa2.plus(fa1);
		resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				assertEquals(array.getDouble(arrayIndex) + 3, result.getArray().getDouble(resultIndex));
			}
		}

		result = fa1.plus(fa1);
		resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				double val = array.getDouble(arrayIndex);
				assertEquals(val + val, result.getArray().getDouble(resultIndex));
			}
		}

		result = fa2.plus(fa2);
		resultIndex = result.getArray().getIndex();
		assertEquals(result.getArray().getSize(), 1);
		resultIndex.set(0);
		assertEquals(6.0, result.getArray().getDouble(resultIndex));
	}

	public void testArrayMinus() {
		Array array = createArray(3, 2, 3);
		FormulaArray fa1 = new FormulaArray(array);
		FormulaArray fa2 = new FormulaArray(createArray(3));

		FormulaArray result = fa1.minus(fa2);

		Index arrayIndex = array.getIndex();
		Index resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				assertEquals(array.getDouble(arrayIndex) - 3, result.getArray().getDouble(resultIndex));
			}
		}

		result = fa2.minus(fa1);
		resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				assertEquals(3 - array.getDouble(arrayIndex), result.getArray().getDouble(resultIndex));
			}
		}

		result = fa1.minus(fa1);
		resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				double val = array.getDouble(arrayIndex);
				assertEquals(0.0, result.getArray().getDouble(resultIndex));
			}
		}

		result = fa2.minus(fa2);
		resultIndex = result.getArray().getIndex();
		assertEquals(result.getArray().getSize(), 1);
		resultIndex.set(0);
		assertEquals(0.0, result.getArray().getDouble(resultIndex));
	}

	public void testForEach() {
		Array array = createArray(3, 2, 3);
		FormulaArray fa1 = new FormulaArray(array);
		FormulaArray result = fa1.foreach(new DoubleFunction() {
			public double apply(double val) {
				return val * 3.3;
			}
		});

		Index arrayIndex = array.getIndex();
		Index resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				assertEquals(array.getDouble(arrayIndex) * 3.3, result.getArray().getDouble(resultIndex));
			}
		}
	}

	public void testArrayDivide() {
		Array array = createArray(3, 2, 3);
		FormulaArray fa1 = new FormulaArray(array);
		FormulaArray fa2 = new FormulaArray(createArray(3));

		FormulaArray result = fa1.divide(fa2);

		Index arrayIndex = array.getIndex();
		Index resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				assertEquals(array.getDouble(arrayIndex) / 3, result.getArray().getDouble(resultIndex));
			}
		}

		result = fa2.divide(fa1);
		resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				assertEquals(3 / array.getDouble(arrayIndex), result.getArray().getDouble(resultIndex));
			}
		}

		result = fa1.divide(fa1);
		resultIndex = result.getArray().getIndex();
		for (int i = 0; i < array.getShape()[0]; i++) {
			for (int j = 0; j < array.getShape()[1]; j++) {
				arrayIndex.set(i, j);
				resultIndex.set(i, j);
				assertEquals(1.0, result.getArray().getDouble(resultIndex));
			}
		}

		result = fa2.divide(fa2);
		resultIndex = result.getArray().getIndex();
		assertEquals(result.getArray().getSize(), 1);
		resultIndex.set(0);
		assertEquals(1.0, result.getArray().getDouble(resultIndex));
	}

	private Array createArray(int val) {
		Array array = ArrayFactory.createDoubleArray(new int[]{1});
		Index index = array.getIndex();
		index.set(0);
		array.setDouble(index, val);

		return array;
	}

	private Array createArray(int count, int rows, int cols) {
		Array array = ArrayFactory.createDoubleArray(new int[]{rows, cols});
		Index index = array.getIndex();
		count = 3;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				index.set(i, j);
				array.setDouble(index, count++);
			}
		}

		return array;
	}
}
