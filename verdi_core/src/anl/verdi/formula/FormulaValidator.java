package anl.verdi.formula;

import static anl.verdi.formula.ValidationResult.Status.FAIL;
import static anl.verdi.formula.ValidationResult.Status.PASS;
import static anl.verdi.formula.ValidationResult.Status.WARN;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import anl.verdi.data.Axes;
import anl.verdi.data.AxisRange;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Range;
import anl.verdi.data.Variable;
import anl.verdi.util.DateRange;
import anl.verdi.util.Utilities;
import javax.swing.JOptionPane;		// 2014 for displaying exception message to user

/**
 * Validates a formula for unit, layer, domain and timestep coherence.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class FormulaValidator {
	static final Logger Logger = LogManager.getLogger(FormulaValidator.class.getName());

	private List<FormulaVariable> variables = new ArrayList<FormulaVariable>();
	public static final String UNITS_WARN = "UNITS_WARN";

	public FormulaValidator(List<FormulaVariable> variables) {
		Logger.debug("in constructor for FormulaValidator");
		this.variables = variables;
	}

	/**
	 * Validates the formula. This checks that the units,
	 * layers domains and timestep are coherent and within range.
	 *
	 * @param commonUnit validate the units against this common unit.
	 * @param ranges     the ranges that will filter or constrain the evaluation.
	 * @return a ValidationResult containing the results of the validation.
	 */
	public ValidationResult validate(Unit commonUnit, List<AxisRange> ranges) {
		try {
			ValidationResult unitResult = checkUnits(commonUnit);
			checkAxes(ranges);
			ValidationResult timeResult = checkTime(ranges);
			if (timeResult.getStatus() == PASS &&
							(unitResult.getStatus() == PASS || unitResult.getStatus() == WARN)) {
				unitResult.setVariables(variables);
				timeResult.setVariables(variables);
			}
			return timeResult.getStatus() == FAIL ? timeResult : unitResult.getStatus() == WARN ? unitResult :
							timeResult;
		} catch (IllegalFormulaException e) {
			return ValidationResult.fail(e);
		}
	}

	private ValidationResult checkTime(List<AxisRange> ranges) throws IllegalFormulaException {
		if (variables.size() > 0) {
			boolean rangeChanged = false;
			FormulaVariable formulaVariable = variables.get(0);
			Axes<CoordAxis> axes = formulaVariable.getDataset().getCoordAxes();
			CoordAxis timeAxis = axes.getTimeAxis();
			
			if (timeAxis == null)
				return ValidationResult.pass();
			
			// start end is our current range in milliseconds.
			long endIndex = timeAxis.getRange().getOrigin() + (timeAxis.getRange().getExtent() - 1);
			DateRange currRange = new DateRange(axes.getDate(timeAxis.getRange().getOrigin()),
							axes.getDate((int) endIndex));
			for (int i = 1; i < variables.size(); i++) {
				FormulaVariable var = variables.get(i);
				if (!var.getDataset().equals(formulaVariable.getDataset())) {
					Axes<CoordAxis> otherAxes = formulaVariable.getDataset().getCoordAxes();
					CoordAxis otherTime = axes.getTimeAxis();
					long otherEndIndex = otherTime.getRange().getOrigin() + (otherTime.getRange().getExtent() - 1);
					DateRange otherRange = new DateRange(otherAxes.getDate(otherTime.getRange().getOrigin()),
									axes.getDate((int) (otherEndIndex)));
					if (!currRange.equals(otherRange)) {
						currRange = currRange.overlap(otherRange);
						rangeChanged = true;
						if (currRange == null) {
							throw createValidationEx(formulaVariable, var, "time");
						}
					}
				}
			}

			// if get here then currRange is the range that fits somewhere in all the time axes
			// now we need to match that up with actual timesteps for each axis and make sure the
			// date values between that range match for all variables.
			Date startDate = new Date(currRange.getStart());
			int timeStart = axes.getTimeStep(startDate);
			Date endDate = new Date(currRange.getEnd());
			int timeEnd = axes.getTimeStep(endDate);
			if (timeStart == Axes.TIME_STEP_NOT_FOUND || timeEnd == Axes.TIME_STEP_NOT_FOUND)
				throw new IllegalFormulaException("Time steps are not compatible across datasets");
//			Date[] tsDates = new Date[timeEnd - timeStart];
			GregorianCalendar[] tsDates = new GregorianCalendar[timeEnd - timeStart];
			for (int i = timeStart; i < timeEnd; i++) {
				tsDates[i] = axes.getDate(i);
			}

			for (int i = 1; i < variables.size(); i++) {
				FormulaVariable var = variables.get(i);
				Axes<CoordAxis> otherAxes = var.getDataset().getCoordAxes();
				int otherStart = otherAxes.getTimeStep(startDate);
				int otherEnd = otherAxes.getTimeStep(endDate);
				if (otherStart == Axes.TIME_STEP_NOT_FOUND || otherEnd == Axes.TIME_STEP_NOT_FOUND ||
								(otherEnd - otherStart != timeEnd - timeStart))
					throw new IllegalFormulaException("Time steps are not compatible across datasets");
				for (int j = timeStart; j < timeEnd; j++) {
					if (!tsDates[j].equals(otherAxes.getDate(j)))
						throw new IllegalFormulaException("Time steps are not compatible across datasets");
				}
				// extent so + 1
				var.setTimeStepRange(new Range(otherStart, (otherEnd - otherStart) + 1));
			}

			// extent so +1
			formulaVariable.setTimeStepRange(new Range(timeStart, (timeEnd - timeStart) + 1));

			// if get here then all the ranges overlap within that overlap have same date
			// values -- does that overlap range fall within the time step range?
			checkTimeRange(ranges, startDate, endDate);

			if (rangeChanged) {
				return ValidationResult.warn("Time step ranges overlap but do not match. Continue with range " +
								Utilities.formatDate(startDate) + " - " + Utilities.formatDate(endDate));
			}
		}
		return ValidationResult.pass();
	}

	private void checkTimeRange(List<AxisRange> ranges, Date startDate, Date endDate) throws IllegalFormulaException {
		AxisRange time = findAxisByType(AxisType.TIME, ranges);
		if (time == null) return;
		CoordAxis axis = time.getAxis();
		// find the dataset the time range axis came from
		Axes foundAxes = null;
		for (FormulaVariable var : variables) {
			Axes axes = var.getDataset().getCoordAxes();
			if (axis.equals(axes.getTimeAxis())) {
				foundAxes = axes;
				break;
			}
		}

		if (foundAxes == null) throw new IllegalFormulaException("Error while resolving time axis");
		long start = startDate.getTime();
		long end = endDate.getTime();
		Range range = time.getRange();
		GregorianCalendar rangeStartDate = foundAxes.getDate(range.getOrigin());
		long rangeStart = rangeStartDate.getTimeInMillis();
		GregorianCalendar rangeEndDate = foundAxes.getDate((int) (range.getOrigin() + (range.getExtent() - 1)));
		long rangeEnd = rangeEndDate.getTimeInMillis();

		if (rangeStart >= start && rangeStart <= end && rangeEnd >= start && rangeEnd <= end) {
			// range is within our "synthetic range" so redo the formula vars to match that
			for (FormulaVariable var : variables) {
				Axes<CoordAxis> otherAxes = var.getDataset().getCoordAxes();
				int tsStart = otherAxes.getTimeStep(rangeStartDate);
				int tsEnd = otherAxes.getTimeStep(rangeEndDate);
				// extent so + 1
				var.setTimeStepRange(new Range(tsStart, (tsEnd - tsStart) + 1));
			}
		} else {
			throw new IllegalFormulaException("Allowable time step range is outside of specified range");
		}
	}

	// make sure all the variables in the formula can
	// be converted into the common unit
	private ValidationResult checkUnits(Unit commonUnit) throws IllegalFormulaException {
		for (FormulaVariable var : variables) {
			Variable datasetVariable = var.getDataset().getVariable(var.getName());
			if (datasetVariable == null) {
				throw new IllegalFormulaException("Variable '" + var.getName() + "' " +
								"not found in " + var.getDataset().getName());
			}
			if (!commonUnit.isCompatible(var.getUnit())) {
				ValidationResult result = ValidationResult.warn(new IllegalFormulaException("Variables in this formula have " +
								"different units (" + commonUnit.toString() + ", " + var.getUnit() + ")"));
				result.putProperty(UNITS_WARN, Boolean.TRUE);
				return result;
			}
		}

		return ValidationResult.pass();
	}

	private void testLayer(CoordAxis layerAxis, FormulaVariable formulaVariable, FormulaVariable var)
					throws IllegalFormulaException {
		CoordAxis other = var.getDataset().getCoordAxes().getZAxis();
		
		if (layerAxis == null && other == null)
			return;
		
		if ((layerAxis == null && other != null) ||
						(layerAxis != null && other == null)) {
			throw createValidationEx(formulaVariable, var, "layer");
		}

		if (!layerAxis.isCompatible(other)) throw createValidationEx(formulaVariable, var, "layer");
	}

	private void testX(CoordAxis xAxis, FormulaVariable formulaVariable, FormulaVariable var)
					throws IllegalFormulaException {
		CoordAxis other = var.getDataset().getCoordAxes().getXAxis();
		if ((xAxis == null && other != null) ||
						(xAxis != null && other == null)) {
			throw createValidationEx(formulaVariable, var, "x");
		}

		if (!xAxis.isCompatible(other)) throw createValidationEx(formulaVariable, var, "x");
	}

	private void testY(CoordAxis yAxis, FormulaVariable formulaVariable, FormulaVariable var)
					throws IllegalFormulaException {
		CoordAxis other = var.getDataset().getCoordAxes().getYAxis();
		if ((yAxis == null && other != null) ||
						(yAxis != null && other == null)) {
			throw createValidationEx(formulaVariable, var, "y");
		}

		if (!yAxis.isCompatible(other)) throw createValidationEx(formulaVariable, var, "y");
	}

	private void testRange(List<AxisRange> ranges, CoordAxis axis, AxisType type, String axisName,
	                       FormulaVariable var) throws IllegalFormulaException {
		// check range compatibility
		AxisRange range = findAxisByType(type, ranges);
		if (range != null) {
			if (axis == null || !range.isCompatible(axis))
				throw createValidationRangeEx(var, range.getRange(), axisName);
		}
	}

	private void checkAxes(List<AxisRange> ranges) throws IllegalFormulaException {
		if (variables.size() > 0) {
			FormulaVariable formulaVariable = variables.get(0);
			Axes<CoordAxis> axes = formulaVariable.getDataset().getCoordAxes();
			CoordAxis layerAxis = axes.getZAxis();
			CoordAxis xAxis = axes.getXAxis();
			CoordAxis yAxis = axes.getYAxis();
			
			for (int i = 1; i < variables.size(); i++) {
				FormulaVariable var = variables.get(i);
				testLayer(layerAxis, formulaVariable, var);
				testY(yAxis, formulaVariable, var);
				testX(xAxis, formulaVariable, var);
			}

			testRange(ranges, layerAxis, AxisType.LAYER, "layer", formulaVariable);
			testRange(ranges, xAxis, AxisType.X_AXIS, "x", formulaVariable);
			testRange(ranges, yAxis, AxisType.Y_AXIS, "y", formulaVariable);
		}
	}

	private IllegalFormulaException createValidationEx(FormulaVariable var1,
	                                                   FormulaVariable var2, String axis) {
		// 2014 changed concatenation in IllegaLFormulaException call to a String; added in message dialog to show message to user
		String aMessage = "'" + var1.getAliasedName() + "' and '" +
				var2.getAliasedName() + "' are incompatible along the " + axis + " axis";
		JOptionPane.showMessageDialog(null, aMessage, "Formula Validation Failed", JOptionPane.ERROR_MESSAGE);
		return new IllegalFormulaException(aMessage);
	}

	private IllegalFormulaException createValidationRangeEx(FormulaVariable var1, Range range, String axis) {
		// 2014 changed concatenation in IllegaLFormulaException call to a String; added in message dialog to show message to user
		String aMessage = "'" + var1.getAliasedName() + "' dataset is incompatible with the " +
				"specified " + axis + " range (" + range.getOrigin() + ", " +
				(range.getOrigin() + (range.getExtent() - 1)) + ")";
		JOptionPane.showMessageDialog(null, aMessage, "Range Validation Failed", JOptionPane.ERROR_MESSAGE);
		return new IllegalFormulaException(aMessage);
	}

	private AxisRange findAxisByType(AxisType type, List<AxisRange> axes) {
		for (AxisRange axis : axes) {
			if (axis.getAxisType().equals(type)) return axis;
		}
		return null;
	}

}
