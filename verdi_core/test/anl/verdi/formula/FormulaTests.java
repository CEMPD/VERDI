package anl.verdi.formula;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Set;

import junit.framework.TestCase;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameBuilder;
import anl.verdi.data.Range;
import anl.verdi.data.TimeCoordAxis;
import anl.verdi.parser.ASTTreeInfo;
import anl.verdi.parser.Frame;
import anl.verdi.util.ArrayFactory;
import anl.verdi.util.FormulaArray;
import anl.verdi.util.VUnits;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class FormulaTests extends TestCase {

	public void testCosEval() throws IllegalFormulaException {
		String formula = "cos(12.3)";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		double val = Math.cos(12.3);
		assertEquals(val, doubleFromArray(parser.evaluate()));
	}

	public void testCosDEval() throws IllegalFormulaException {
		String formula = "cosd(12.3)";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		double val = Math.cos(12.3 / 180 * Math.PI);
		assertEquals(val, doubleFromArray(parser.evaluate()), .0000000000001);
		assertEquals(Math.cos(Math.toRadians(12.3)), doubleFromArray(parser.evaluate()), .0000000000001);
	}

	public void testTan() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("tan(3.2)", new Frame());
		assertEquals(Math.tan(3.2), doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("tand(3.2)", new Frame());
		assertEquals(Math.tan(Math.toRadians(3.2)), doubleFromArray(parser.evaluate()), .0000000001);
		assertEquals(Math.tan(3.2 / 180 * Math.PI), doubleFromArray(parser.evaluate()), .0000000001);
	}

	public void testSqr() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("sqr(2 + .2)", new Frame());
		assertEquals(Math.pow(2.2, 2), doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("sqrt(2)", new Frame());
		assertEquals(Math.sqrt(2), doubleFromArray(parser.evaluate()));
	}

	public void testEqEval() throws IllegalFormulaException {
		String formula = "12.2 == 10";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		assertEquals(0.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("1.23 == 1.23", new Frame());
		assertEquals(1.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("3 == 0", new Frame());
		assertEquals(0.0, doubleFromArray(parser.evaluate()));
	}

	public void testComparison() throws IllegalFormulaException {
		String formula = "12.2 > 12.2";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		assertEquals(0.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("12.2 >= 12.2", new Frame());
		assertEquals(1.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("12.2 < 12.2", new Frame());
		assertEquals(0.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("12.2 <= 12.2", new Frame());
		assertEquals(1.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("12.2 >= 234234.2", new Frame());
		assertEquals(0.0, doubleFromArray(parser.evaluate()));
	}

	public void testLog() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("log(3)", new Frame());
		assertEquals(Math.log10(3), doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("ln(3)", new Frame());
		assertEquals(Math.log(3), doubleFromArray(parser.evaluate()));
	}

	public void testEEval() throws IllegalFormulaException {
		String formula = "E * 2";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		assertEquals(Math.E * 2, doubleFromArray(parser.evaluate()));
	}

	public void testExpEval() throws IllegalFormulaException {
		String formula = "exp(2)";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		assertEquals(Math.exp(2), doubleFromArray(parser.evaluate()));
	}

	public void testPIEval() throws IllegalFormulaException {
		String formula = "PI / 2";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		assertEquals(Math.PI / 2, doubleFromArray(parser.evaluate()));
	}

	public void testAndEval() throws IllegalFormulaException {
		String formula = "12.2 && 0";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		assertEquals(0.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("1 && 1.23", new Frame());
		assertEquals(1.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("0 && 0", new Frame());
		assertEquals(0.0, doubleFromArray(parser.evaluate()));
	}

	private Array createArray(double initValue, int rows, int cols) {
		Array array = ArrayFactory.createDoubleArray(new int[]{rows, cols});
		Index index = array.getIndex();
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				index.set(i, j);
				array.setDouble(index, initValue++);
			}
		}

		return array;
	}

	private Array createRandomArray(int... dims) {
		Array array = ArrayFactory.createDoubleArray(dims);
		for (IndexIterator iter = array.getIndexIterator(); iter.hasNext(); ) {
			iter.setDoubleNext(Math.random());
		}

		return array;
	}

	private double calcSum(FormulaArray array) {
		double sum = 0;
		for (IndexIterator iter = array.getArray().getIndexIterator(); iter.hasNext(); ) {
			sum += iter.getDoubleNext();
		}

		return sum;
	}

	public void testVarEval() throws IllegalFormulaException {
		String formula = "O3[1]";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		FormulaArray fa = new FormulaArray(createArray(12.3, 10, 200));
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertTrue(fa.equals(parser.evaluate()));
	}

	public void testMod() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("10 % 3", new Frame());
		assertEquals(10.0 % 3.0, doubleFromArray(parser.evaluate()));
	}

	public void testNeg() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("-10 + 11", new Frame());
		assertEquals(1.0, doubleFromArray(parser.evaluate()));
	}

	public void testNotEqual() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("10 != 11", new Frame());
		assertEquals(1.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("1324.33 != 1324.33", new Frame());
		assertEquals(0.0, doubleFromArray(parser.evaluate()));
	}

	public void testPow() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("2 ** 0.5", new Frame());
		assertEquals(Math.pow(2, .5), doubleFromArray(parser.evaluate()));
		assertEquals(Math.sqrt(2), doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("2.5 ** 2", new Frame());
		assertEquals(Math.pow(2.5, 2), doubleFromArray(parser.evaluate()));

		// precedence
		parser = new DefaultParser("2 ** 2 / 3", new Frame());
		assertEquals(4.0 / 3.0, doubleFromArray(parser.evaluate()));
	}

	public void testSin() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("sin(2.3)", new Frame());
		assertEquals(Math.sin(2.3), doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("sind(10.5)", new Frame());
		double result = doubleFromArray(parser.evaluate());
		assertEquals(Math.sin(10.5 / 180 * Math.PI), result, .00000000001);
		assertEquals(Math.sin(Math.toRadians(10.5)), result, .00000000001);
	}

	public void testOr() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("2 - 2 || 3 + 4", new Frame());
		assertEquals(1.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("3 + 4 || 0", new Frame());
		assertEquals(1.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("2 - 2 || 3 - 3", new Frame());
		assertEquals(0.0, doubleFromArray(parser.evaluate()));
	}

	public void testFloatEval() throws IllegalFormulaException {
		String formula = String.valueOf(Double.MAX_VALUE / 2);
		DefaultParser parser = new DefaultParser(formula, new Frame());
		assertEquals(Double.MAX_VALUE / 2, doubleFromArray(parser.evaluate()), .000000000001);
	}

	public void testArithEval() throws IllegalFormulaException {
		String formula = "12.32 + 13.234 * 23423 / .232";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		double val = 13.234 * 23423 / 0.232 + 12.32;
		assertEquals(val, doubleFromArray(parser.evaluate()));
	}

	private double doubleFromArray(FormulaArray array) {
		Index index = array.getArray().getIndex();
		index.set(0);
		return array.getArray().getDouble(index);
	}

	public void testParensEval() throws IllegalFormulaException {
		String formula = "(12.32 + 13.234) * 23423 / .232";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		double val = (12.32 + 13.234) * 23423 / 0.232;
		assertEquals(val, doubleFromArray(parser.evaluate()));
	}

	public void testIntEval() throws IllegalFormulaException {
		String formula = "302";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		assertEquals(302.0, doubleFromArray(parser.evaluate()));
	}

	public void testVars() {
		String formula = "O3a / .001 * abs(ZAb)";
		DefaultParser parser = new DefaultParser(formula, new Frame());
		assertEquals(formula, parser.getFormulaAsString());

		try {
			Set<String> vars = parser.parse().getVariableNames();
			assertEquals(2, vars.size());
			assertTrue(vars.contains("O3a"));
			assertTrue(vars.contains("ZAb"));
		} catch (IllegalFormulaException e) {
			fail(e.getMessage());
		}
	}

	public void testTimeStepVar() throws IllegalFormulaException, InvalidRangeException {
		// t, l, y, x
		Array array = createRandomArray(4, 2, 10, 3);
		DataFrameBuilder builder = new DataFrameBuilder();
		builder.setArray(array);
		builder.addAxis(DataFrameAxis.createDataFrameAxis(new TestTimeCoordAxis(AxisType.TIME, 4), 0));
		builder.addAxis(DataFrameAxis.createDataFrameAxis(new TestCoordAxis(AxisType.LAYER, 2), 1));
		builder.addAxis(DataFrameAxis.createDataFrameAxis(new TestCoordAxis(AxisType.Y_AXIS, 10), 2));
		builder.addAxis(DataFrameAxis.createDataFrameAxis(new TestCoordAxis(AxisType.X_AXIS, 3), 3));
		DataFrame frame = builder.createDataFrame();

		DefaultParser parser = new DefaultParser("O3[1]:0", new Frame());
		parser.setVariable("O3[1]", frame);
		Array newArray = array.sectionNoReduce(new int[]{0, 0, 0, 0}, new int[]{1, 2, 10, 3}, null);
		Array result = parser.evaluate().getArray();
		assertEquals(1, result.getShape()[0]);
		assertEquals(2, result.getShape()[1]);
		assertEquals(10, result.getShape()[2]);
		assertEquals(3, result.getShape()[3]);
		assertTrue(equals(newArray, result));

		parser = new DefaultParser("O3[1]:3", new Frame());
		parser.setVariable("O3[1]", frame);
		newArray = array.sectionNoReduce(new int[]{3, 0, 0, 0}, new int[]{1, 2, 10, 3}, null);
		result = parser.evaluate().getArray();
		assertEquals(1, result.getShape()[0]);
		assertEquals(2, result.getShape()[1]);
		assertEquals(10, result.getShape()[2]);
		assertEquals(3, result.getShape()[3]);
		assertTrue(equals(newArray, result));
	}

	private boolean equals(Array one, Array two) {
		if (one.getRank() == two.getRank()) {
			for (int i = 0; i < one.getRank(); i++) {
				if (one.getShape()[i] != two.getShape()[i]) return false;
			}

			IndexIterator indexOne = one.getIndexIterator();	// getIndexIteratorFast deprecated
			IndexIterator indexTwo = two.getIndexIterator();	// getIndexIteratorFast deprecated
			while (indexOne.hasNext()) {
				if (indexOne.getDoubleNext() != indexTwo.getDoubleNext()) return false;
			}
			return true;
		}

		return false;
	}

	public void testSum() throws IllegalFormulaException {
		FormulaArray fa = new FormulaArray(createRandomArray(48, 6, 100, 200));
		double sum = calcSum(fa);
		DefaultParser parser = new DefaultParser("sum(O3[1])", new Frame());
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(sum, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("sum(O3[1] > .5)", new Frame());
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));


		sum = 0;
		for (IndexIterator iter = fa.getArray().getIndexIterator(); iter.hasNext(); ) {	// getIndexIteratorFast deprecated
			double next = iter.getDoubleNext();
			if (next > .5) sum++;
		}

		assertEquals(sum, doubleFromArray(parser.evaluate()));
	}

	public void testMean() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("mean(O3[1])", new Frame());
		FormulaArray fa = new FormulaArray(createRandomArray(48, 6, 100, 200));
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));


		double sum = calcSum(fa);
		assertEquals(sum / fa.getArray().getSize(),doubleFromArray(parser.evaluate()));
	}

	public void testMaxMin() throws IllegalFormulaException {
		// min = 0.029593393
		// max = 0.046018045
		Frame frame = new Frame();
		frame.setCoordinateIndices(0, 1, 3, 2);
		DefaultParser parser = new DefaultParser("min(O3[1])", frame);
		FormulaArray fa = TestData.getArray();
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(0.029593393, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("max(O3[1])", frame);
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(0.046018045, doubleFromArray(parser.evaluate()));
	}

	public void testMinMaxT() throws IllegalFormulaException {
		Frame frame = new Frame();
		frame.setCoordinateIndices(0, 1, 3, 2);
		DefaultParser parser = new DefaultParser("mint(O3[1])", frame);
		FormulaArray fa = TestData.getArray();
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(0.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("maxt(O3[1])", frame);
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(0.0, doubleFromArray(parser.evaluate()));
	}

	public void testMinMaxX() throws IllegalFormulaException {
		Frame frame = new Frame();
		frame.setCoordinateIndices(0, 1, 3, 2);
		DefaultParser parser = new DefaultParser("minx(O3[1])", frame);
		FormulaArray fa = TestData.getArray();
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(1.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("maxx(O3[1])", frame);
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(0.0, doubleFromArray(parser.evaluate()));
	}

	public void testMinMaxY() throws IllegalFormulaException {
		Frame frame = new Frame();
		frame.setCoordinateIndices(0, 1, 3, 2);
		DefaultParser parser = new DefaultParser("miny(O3[1])", frame);
		FormulaArray fa = TestData.getArray();
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(1.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("maxy(O3[1])", frame);
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(5.0, doubleFromArray(parser.evaluate()));
	}

	public void testMinMaxZ() throws IllegalFormulaException {
		Frame frame = new Frame();
		frame.setCoordinateIndices(0, 1, 3, 2);
		DefaultParser parser = new DefaultParser("minz(O3[1])", frame);
		FormulaArray fa = TestData.getArray();
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(0.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("maxz(O3[1])", frame);
		parser.setVariable("O3[1]", formulaArrayToDataFrame(fa));

		assertEquals(2.0, doubleFromArray(parser.evaluate()));
	}

	public void testConstants() throws IllegalFormulaException {
		Frame frame = new Frame();
		frame.setColumnCount(3);
		frame.setRowCount(2);
		frame.setLayerCount(1);

		DefaultParser parser = new DefaultParser("NROWS", frame);
		assertEquals(2.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("NCOLS", frame);
		assertEquals(3.0, doubleFromArray(parser.evaluate()));

		parser = new DefaultParser("NLAYERS", frame);
		assertEquals(1.0, doubleFromArray(parser.evaluate()));
	}

	public void testAggregateInfo() throws IllegalFormulaException {
		DefaultParser parser = new DefaultParser("sum(3)", new Frame());
		ASTTreeInfo info = parser.parse();
		assertTrue(info.hasAggregates());

		parser = new DefaultParser("mean(3)", new Frame());
		info = parser.parse();
		assertTrue(info.hasAggregates());

		parser = new DefaultParser("maxt(3)", new Frame());
		info = parser.parse();
		assertTrue(info.hasAggregates());

		parser = new DefaultParser("mint(3)", new Frame());
		info = parser.parse();
		assertTrue(info.hasAggregates());

		parser = new DefaultParser("maxx(3)", new Frame());
		info = parser.parse();
		assertTrue(info.hasAggregates());

		parser = new DefaultParser("minx(3)", new Frame());
		info = parser.parse();
		assertTrue(info.hasAggregates());

		parser = new DefaultParser("maxy(3)", new Frame());
		info = parser.parse();
		assertTrue(info.hasAggregates());

		parser = new DefaultParser("miny(3)", new Frame());
		info = parser.parse();
		assertTrue(info.hasAggregates());

		parser = new DefaultParser("maxz(3)", new Frame());
		info = parser.parse();
		assertTrue(info.hasAggregates());

		parser = new DefaultParser("minz(3)", new Frame());
		info = parser.parse();
		assertTrue(info.hasAggregates());

		parser = new DefaultParser("abs(3)", new Frame());
		info = parser.parse();
		assertTrue(!info.hasAggregates());
	}

	private DataFrame formulaArrayToDataFrame(FormulaArray array) {
		DataFrameBuilder builder = new DataFrameBuilder();
		builder.setArray(array.getArray());
		return builder.createDataFrame();
	}

	class TestCoordAxis implements CoordAxis {

		private Range range;
		private AxisType type;


		public TestCoordAxis(AxisType type, int extent) {
			this.type = type;
			range = new Range(0, extent);
		}

		/**
		 * Gets the name of this CoordAxis.
		 *
		 * @return the name of this CoordAxis.
		 */
		public String getName() {
			return null;
		}


		public Unit getUnits() {
			return VUnits.createUnit("TEST");
		}

		public double getValue(int index) {
			return 0;
		}


		public boolean isCompatible(CoordAxis axis) {
			return true;
		}

		/**
		 * Gets the description of this CoordAxis.
		 *
		 * @return the description of this CoordAxis.
		 */
		public String getDescription() {
			return null;
		}

		/**
		 * Gets the range of this axis.
		 *
		 * @return the range of this axis.
		 */
		public Range getRange() {
			return range;
		}

		/**
		 * Gets the type of this axis.
		 *
		 * @return the type of this axis.
		 */
		public AxisType getAxisType() {
			return type;
		}
	}

	class TestTimeCoordAxis extends TestCoordAxis implements TimeCoordAxis {

		private TimeCoordAxis axis;

		public TestTimeCoordAxis(AxisType type, int extent) {
			super(type, extent);
		}
		
		public void setAxis(TimeCoordAxis anAxis)		// 2014 adding in a method to set the value of axis
		{
			this.axis = anAxis;
		}


		public GregorianCalendar getDate(int timestep) {	// method not previously implemented 2014
			return ((TimeCoordAxis)this.axis).getDate(timestep);
		}

		public int getTimeStep(Date date) {			// method not previously implemented 2014
			return ((TimeCoordAxis)this.axis).getTimeStep(date);
		}
		
		public int getTimeStep(GregorianCalendar aCalendar)	{	// method not previously implemented 2014
			return ((TimeCoordAxis)this.axis).getTimeStep(aCalendar);
		}
	}
}
