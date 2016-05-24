package anl.verdi.util;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class FormulaArray {

	private static interface Evaluator {
		void evaluate(Array local, Array other, Array result, BinaryDoubleFunction func);
	}

	private static class ArraySize1 implements Evaluator {

		public void evaluate(Array local, Array other, Array result, BinaryDoubleFunction func) {
			double val = local.getIndexIterator().getDoubleNext();
			IndexIterator iter = other.getIndexIterator();
			IndexIterator tmpIter = result.getIndexIterator();
			while (iter.hasNext()) {
				tmpIter.setDoubleNext(func.apply(val, iter.getDoubleNext()));
			}
		}
	}

	private static class ArrayEvaluator implements Evaluator {
		public void evaluate(Array local, Array other, Array result, BinaryDoubleFunction func) {
			if (other.getSize() == 1) {
				double val = other.getIndexIterator().getDoubleNext();
				IndexIterator iter = local.getIndexIterator();
				IndexIterator tmpIter = result.getIndexIterator();
				while (iter.hasNext()) {
					tmpIter.setDoubleNext(func.apply(iter.getDoubleNext(), val));
				}
			} else {
				IndexIterator otherIter = other.getIndexIterator();
				IndexIterator iter = local.getIndexIterator();
				IndexIterator tmpIter = result.getIndexIterator();
				while (iter.hasNext()) {
					tmpIter.setDoubleNext(func.apply(iter.getDoubleNext(), otherIter.getDoubleNext()));
				}
			}
		}
	}

	private static class Multiplier implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 * val2;
		}
	}

	private static class Divider implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 / val2;
		}
	}

	private static class Adder implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 + val2;
		}
	}

	private static class Subtracter implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 - val2;
		}
	}

	private static class LessThan implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 < val2 ? 1 : 0;
		}
	}

	private static class LessThanEq implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 <= val2 ? 1 : 0;
		}
	}

	private static class GreaterThan implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 > val2 ? 1 : 0;
		}
	}

	private static class GreaterThanEq implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 >= val2 ? 1 : 0;
		}
	}

	private static class NotEqual implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 != val2 ? 1 : 0;
		}
	}

	private static class Equal implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 == val2 ? 1 : 0;
		}
	}

	private static class And implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 != 0 && val2 != 0 ? 1 : 0;
		}
	}

	private static class Or implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 != 0 || val2 != 0 ? 1 : 0;
		}
	}

	private static class Mod implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return val1 % val2;
		}
	}

	private static class Pow implements BinaryDoubleFunction {
		public double apply(double val1, double val2) {
			return Math.pow(val1, val2);
		}
	}

	private Array array;
	private Evaluator evaluator;
	private boolean isTmp;
	private int[] minIndices, maxIndices;
	private double min = Double.NaN;
	private double max = Double.NaN;
	
	public FormulaArray(double val) {
		this(val, false);
	}

	public FormulaArray(double val, boolean isTmp) {
		this.isTmp = isTmp;
		array = ArrayFactory.createDoubleArray(new int[]{1});
		Index index = array.getIndex();
		index.set(0);
		array.setDouble(index, val);
		evaluator = new ArraySize1();
	}

	public FormulaArray(Array array) {
		this(array, false);
	}

	public FormulaArray(Array array, boolean isTmp) {
		this.isTmp = isTmp;
		this.array = array;
		if (this.array.getSize() == 1) evaluator = new ArraySize1();
		else evaluator = new ArrayEvaluator();
	}

	public FormulaArray foreach(DoubleFunction func) {
		FormulaArray tmp = new FormulaArray(ArrayFactory.createDoubleArray(array.getShape()), true);
		foreach(func, tmp);
		return tmp;
	}

	public void foreach(DoubleFunction func, FormulaArray result) {
		IndexIterator iter = array.getIndexIterator();
		IndexIterator tmpIter = result.array.getIndexIterator();
		while (iter.hasNext()) {
			tmpIter.setDoubleNext(func.apply(iter.getDoubleNext()));
		}
	}

	public Array getArray() {
		return array;
	}

	public boolean isTmp() {
		return isTmp;
	}

	private FormulaArray createFormulaArray(FormulaArray other) {
		Array tmp;
		if (array.getSize() == 1 && other.array.getSize() == 1) tmp = ArrayFactory.createDoubleArray(new int[]{1});
		if (array.getSize() > 1) tmp = ArrayFactory.createDoubleArray(array.getShape());
		else tmp = ArrayFactory.createDoubleArray(other.array.getShape());
		return new FormulaArray(tmp, true);
	}

	public FormulaArray times(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		times(other, fa);
		return fa;
	}

	public void times(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new Multiplier());
	}

	public FormulaArray divide(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		divide(other, fa);
		return fa;
	}

	public void divide(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new Divider());
	}

	public FormulaArray minus(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		minus(other, fa);
		return fa;
	}

	public void minus(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new Subtracter());
	}

	public FormulaArray plus(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		plus(other, fa);
		return fa;
	}

	public void plus(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new Adder());
	}

	public FormulaArray lessThan(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		lessThan(other, fa);
		return fa;
	}

	public void lessThan(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new LessThan());
	}

	public FormulaArray lessThanEq(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		lessThanEq(other, fa);
		return fa;
	}

	public void lessThanEq(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new LessThanEq());
	}

	public FormulaArray greaterThan(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		greaterThan(other, fa);
		return fa;
	}

	public void greaterThan(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new GreaterThan());
	}

	public FormulaArray greaterThanEq(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		greaterThanEq(other, fa);
		return fa;
	}

	public void greaterThanEq(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new GreaterThanEq());
	}

	public FormulaArray notEqual(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		notEqual(other, fa);
		return fa;
	}

	public void notEqual(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new NotEqual());
	}

	public FormulaArray equal(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		equal(other, fa);
		return fa;
	}

	public void equal(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new Equal());
	}

	public FormulaArray and(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		and(other, fa);
		return fa;
	}

	public void and(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new And());
	}

	public FormulaArray or(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		or(other, fa);
		return fa;
	}

	public void or(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new Or());
	}

	public FormulaArray mod(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		mod(other, fa);
		return fa;
	}

	public void mod(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new Mod());
	}

	public FormulaArray pow(FormulaArray other) {
		FormulaArray fa = createFormulaArray(other);
		pow(other, fa);
		return fa;
	}

	public void pow(FormulaArray other, FormulaArray result) {
		evaluator.evaluate(array, other.array, result.array, new Pow());
	}

	public boolean equals(FormulaArray other) {
		return isTmp == other.isTmp && array.equals(other.array);
	}

	/**
	 * Gets the minimum value in this formula array.
	 *
	 * @return the minimum value in this formula array.
	 */
	public double min() {
		if (Double.isNaN(min)) calcMinMax();
		return min;
	}

	/**
	 * Gets the maximum value in this formula array.
	 *
	 * @return the maximum value in this formula array.
	 */
	public double max() {
		if (Double.isNaN(max)) calcMinMax();
		return max;
	}

	/**
	 * Gets the indices where the min value occurs.
	 *
	 * @return the indices where the min value occurs.
	 */
	public int[] minIndices() {
		if (minIndices == null) calcMinMax();
		return minIndices;
	}

	/**
	 * Gets the indices where the max value occurs.
	 *
	 * @return the indices where the max value occurs.
	 */
	public int[] maxIndices() {
		if (maxIndices == null) calcMinMax();
		return maxIndices;
	}

	private void calcMinMax() {
		int rank = array.getRank();

		minIndices = new int[rank];
		maxIndices = new int[rank];
		min = Double.POSITIVE_INFINITY;
		max = Double.NEGATIVE_INFINITY;

		int[] shape = array.getShape();
		if (rank == 1) {
			Index index = array.getIndex();
			for (int i = 0; i < shape[0]; i++) {
				index.set(i);
				double val = array.getDouble(index);
				if (val < min) {
					min = val;
					minIndices[0] = i;
				}

				if (val > max) {
					max = val;
					maxIndices[0] = i;
				}
			}
		} else {
			// assumes rank of 4
			Index index = array.getIndex();

			for (int t = 0; t < shape[0]; t++) {
				for (int k = 0; k < shape[1]; k++) {
					for (int i = 0; i < shape[2]; i++) {
						for (int j = 0; j < shape[3]; j++) {
							index.set(t, k, i, j);
							double val = array.getDouble(index);
							if (val < min) {
								min = val;
								minIndices[0] = t;
								minIndices[1] = k;
								minIndices[2] = i;
								minIndices[3] = j;
							}

							if (val > max) {
								max = val;
								maxIndices[0] = t;
								maxIndices[1] = k;
								maxIndices[2] = i;
								maxIndices[3] = j;
							}
						}
					}
				}
			}
		}
	}
}
