package anl.verdi.formula;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

//import org.jscience.physics.measures.Measure;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.Measure;
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import ucar.ma2.Array;
import ucar.ma2.IndexIterator;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisRange;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameBuilder;
import anl.verdi.data.DataManager;
import anl.verdi.data.DataTransformer;
import anl.verdi.data.DefaultVariable;
import anl.verdi.data.Range;
import anl.verdi.data.Variable;
import anl.verdi.parser.ASTTreeInfo;
import anl.verdi.parser.ASTVar;
import anl.verdi.parser.Node;
import anl.verdi.util.DateRange;
import anl.verdi.util.FormulaArray;
import anl.verdi.util.VUnits;

/**
 * Formula implementation that produces data suitable for a tile plot. That is, the data
 * will include every value for each x,y cell in the domain.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DefaultFormula implements Formula {
	static final Logger Logger = LogManager.getLogger(DefaultFormula.class.getName());

	private ASTTreeInfo treeInfo;

	class VarFramePair {
		FormulaVariable var;
		DataFrame frame;
		IndexIterator iter;
		Unit unit;

		public VarFramePair(DataFrame frame, FormulaVariable var) {
			this.frame = frame;
			this.var = var;
			unit = var.getUnit();
			Logger.debug("in VarFramePair constructor within class DefaultFormula, unit = " + unit);
			iter = frame.getArray().getIndexIterator();
		}

		public void setFrame(DataFrame frame) {
			this.frame = frame;
			iter = frame.getArray().getIndexIterator();
		}
	}

	private FormulaParser parser;
	private DataTransformer transformer;
	private Unit commonUnit;
	private List<FormulaVariable> variables = new ArrayList<FormulaVariable>();

	public DefaultFormula(FormulaParser parser, DataTransformer transformer, Unit commonUnit) {
		Logger.debug("in constructor for DefaultFormula");	// 2014 PM2.5 bug OK to here
		this.parser = parser;
		this.transformer = transformer;
		this.commonUnit = commonUnit;
		Logger.debug(" in constructor for DefaultFormula, commonUnit = " + this.commonUnit);
	}

	private List<VarFramePair> readData(List<AxisRange> ranges, boolean convertUnits)
					throws IllegalFormulaException {
		Logger.debug("in DefaultFormula.readData, convertUnits = " + convertUnits);
		List<VarFramePair> results = new ArrayList<VarFramePair>();
		for (FormulaVariable var : variables) {
			DataFrame frame = var.evaluate(ranges);
			Logger.debug("var = " + var);
			Logger.debug("units = " + var.getUnit());
			// do the unit conversion
			if (convertUnits && !var.getUnit().equals(commonUnit)) {
				Logger.debug("performing unit conversion to commonUnit");
				Array array = frame.getArray();
				Unit unit = var.getUnit();
//				for (IndexIterator iter = array.getIndexIteratorFast(); iter.hasNext();) {	// 2014 NetCDF library replaced getIndexIteratorFast
				for (IndexIterator iter = array.getIndexIterator(); iter.hasNext();) {
					double val = iter.getDoubleNext();
					// 2014 I think that the next statement is trying to perform a unit conversion
					// the current value for the current unit is recalculated to a new value for the "commonUnit"
					// Needs to be changed to appropriately use org.eclipse.uomo methods
//					iter.setDoubleCurrent(Measure.valueOf(val, unit).doubleValue(commonUnit));
//					current method: double newValue = oldUnit.getConverterTo(newUnit).convert(oldValue);
					iter.setDoubleCurrent(unit.getConverterTo(commonUnit).convert(val));
				}
			}
			VarFramePair varFramePair = new VarFramePair(frame, var);
			results.add(varFramePair);
		}
		return results;
	}

//	private List<VarFramePair> transformData(List<VarFramePair> data) {
//		// transform the values in each array according to some strategy
//		for (VarFramePair pair : data) {
//			// transform the data (e.g. take the average over all values for each
//			// time step.
//			pair.setFrame(transformer.transform(pair.frame));
//		}
//
//		return data;
//	}

	// make sure all the variables in the formula can
	// be converted into the common unit
	private void createFormulaVars(DataManager manager) throws IllegalFormulaException {
		String name = "";
		try {
			for (String varName : treeInfo.getVariableNames()) {
				name = varName;		// need copy in case error is thrown and caught below
				FormulaVariable fVar = FormulaVariable.createVariable(varName, manager);
				variables.add(fVar);
			}
		} catch (StringIndexOutOfBoundsException ex) {
			throw new IllegalFormulaException("Invalid dataset alias on variable '" + name + "'");
		}
	}

	/**
	 * Validates the formula. This checks that the units,
	 * layers domains and timestep are coherent and within range.
	 *
	 * @param manager the data manager used to validate the formula
	 * @param ranges  the ranges that will filter or constrain the evaluation.
	 * @return a ValidationResult containing the results of the validation.
	 */
	public ValidationResult validate(DataManager manager, List<AxisRange> ranges) {
		try {
			treeInfo = parser.parse();
//Logger.debug("\tstarting DefaultFormula.validate(), commonUnit = " + variables.get(0));
			createFormulaVars(manager);
			Logger.debug("in DefaultFormala.validate, back from createFormulaVars, commonUnit = " + variables.get(0));
			checkIfObservational(variables);
			if (commonUnit == null && variables.size() > 0) {
				FormulaVariable var = variables.get(0);
				commonUnit = var.getUnit();
			} else if (variables.size() == 0) {
				// create a unit as a placeholder
				// this should be fine because if there are
				// no variables then the units don't matter.
				commonUnit = VUnits.createUnit("PPM");
			}
			Logger.debug("\tin DefaultFormula.validate(), commonUnit = " + commonUnit);
			FormulaValidator validator = new FormulaValidator(variables);
			Logger.debug("\tin Defaultformula.validate(), returning commonUnit = " + commonUnit);
			return validator.validate(commonUnit, ranges);
		} catch (IllegalFormulaException e) {
			return ValidationResult.fail(e);
		}
	}

	/**
	 * Check if any variable is from an observational dataset for constructing a 
	 * complex formula
	 * 
	 * @param vars List of variables to be checked
	 * @throws IllegalFormulaException if one of the variables is observational
	 */
	private void checkIfObservational(List<FormulaVariable> vars) throws IllegalFormulaException {
		int count = 0;
		FormulaVariable temp = null;
		
		for (FormulaVariable var : vars) {
			if (var.getDataset().isObs()) {
				count++;
				temp = var;
			}
		}
		
		if (count > 1 || (count == 1 && vars.size() > 1))
			throw new IllegalFormulaException("Observational variable is not supported (" + temp.getAliasedName() + ").");
	}

	/**
	 * Overrides any other time range and uses this one in the formula.
	 *
	 * @param range the time range to evaluate against
	 * @throws IllegalFormulaException if there is error setting the time range.
	 */
	public void overrideTimeRange(DateRange range) throws IllegalFormulaException {
		Logger.debug("in DefaultFormula overrideTimeRange(DateRange)");
		for (FormulaVariable var : variables) {
			GregorianCalendar startCalendar = new GregorianCalendar();
			GregorianCalendar endCalendar = new GregorianCalendar();
			Axes<CoordAxis> axes = var.getDataset().getCoordAxes();
//			int start = axes.getTimeStep(new Date(range.getStart()));
			startCalendar.setTimeInMillis(range.getStart());
			int start = (int) axes.getTimeStep(startCalendar);
//			int end = axes.getTimeStep(new Date(range.getEnd()));
			endCalendar.setTimeInMillis(range.getEnd());
			int end = (int) axes.getTimeStep(endCalendar);
			if (start == Axes.TIME_STEP_NOT_FOUND || end == Axes.TIME_STEP_NOT_FOUND) {
				throw new IllegalFormulaException("Error while setting resolved time range on formula: dates not found");
			}
			var.setTimeStepRange(new Range(start, end - start + 1));
			Logger.debug("just set new TimeStepRange");
		}
	}


	/**
	 * Evaluates the formula to produce a DataFrame.
	 *
	 * @param manager the manager used to get read access to the relevant data
	 * @param ranges  the ranges that constrain the evaluation
	 * @return a DataFrame containing the result of the evaluating the formula.
	 */
	public DataFrame evaluate(DataManager manager, List<AxisRange> ranges)
					throws IllegalFormulaException {
		return evaluate(manager, ranges, true);
	}

	/**
	 * Evaluates the formula to produce a DataFrame.
	 *
	 * @param manager      the manager used to get read access to the relevant data
	 * @param ranges       the ranges that constrain the evaluation
	 * @param convertUnits whether or not to convert the units when evaluating the formula
	 * @return a DataFrame containing the result of evaluating the formula.
	 * @throws IllegalFormulaException if the formula is unable to be executed.
	 */

	public DataFrame evaluate(DataManager manager, List<AxisRange> ranges, boolean convertUnits) throws IllegalFormulaException {
		if (treeInfo == null) throw new UnsupportedOperationException("Validate() must be called before evaluate");
		
		//NOTE: need to get the time step info from the formula and reset the Time Axis
		int numberOfChildren = treeInfo.getStart().jjtGetNumChildren();
		int timestep = -1;
		
		for (int i = 0; i < numberOfChildren; i++) {
			Node node = treeInfo.getStart().jjtGetChild(i);
			if (node instanceof ASTVar)
				timestep = ((ASTVar) node).getTimeStep();
		}
		
		if (timestep > -1) {
			AxisRange temp = null;
			timestep--; //NOTE: assume 1-based time step
			Axes<CoordAxis> axes = variables.get(0).getDataset().getCoordAxes();
			
			for (AxisRange range : ranges)
				if (range.getAxisType() == AxisType.TIME)
					temp = range;
			
			if (temp != null) {
				ranges.remove(temp);
				ranges.add(new AxisRange(temp, timestep, 1));
			}
			
			if (temp == null) {
				ranges.add(new AxisRange(axes.getTimeAxis(), timestep, 1));
			}
		}
		
		List<VarFramePair> results = readData(ranges, convertUnits);
		// transformData(results);
		DataFrame frame = results.get(0).frame;

		Axes<DataFrameAxis> axes = frame.getAxes();
		int timeIndex = axes.getTimeAxis() == null ? -1 : axes.getTimeAxis().getArrayIndex();
		int layerIndex = axes.getZAxis() == null ? -1 : axes.getZAxis().getArrayIndex();
		int xIndex = axes.getXAxis() == null ? -1 : axes.getXAxis().getArrayIndex();
		int yIndex = axes.getYAxis() == null ? -1 : axes.getYAxis().getArrayIndex();
		parser.setCoodinateIndices(timeIndex, layerIndex, xIndex, yIndex);

		DataFrameBuilder builder = new DataFrameBuilder();
		for (VarFramePair pair : results) {
			builder.addDataset(pair.frame.getDataset());
		}
		// the data frames produced by the readers should all have the same shape.
		// so we can take the axes from the first one.
		for (DataFrameAxis axis : axes.getAxes()) {
			builder.addAxis(axis);
		}

		builder.setArray(evaluateFormula(results));
		builder.setVariable(createVariable());
		DataFrame dataFrame = builder.createDataFrame();
		return transformer.transform(dataFrame);
	}

	private Array evaluateFormula(List<VarFramePair> results) throws IllegalFormulaException {
		for (VarFramePair pair : results) {
			String name = pair.var.getAliasedName();
			parser.setVariable(name, pair.frame);
		}
		FormulaArray array = parser.evaluate();
		return array.getArray();
	}

	private Variable createVariable() {
		String formula = parser.getFormulaAsString();
		return new DefaultVariable(formula, formula, commonUnit, null);
	}
}
