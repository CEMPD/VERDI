package anl.verdi.formula;

import java.util.List;

import anl.verdi.data.AxisRange;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataManager;
import anl.verdi.util.DateRange;

/**
 * Encapsulates a pave style formula and its evaluation.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface Formula {
	/**
	 * Overrides any other time range and uses this one in the formula.
	 *
	 * @param range the time range to evaluate against
	 * @throws anl.verdi.formula.IllegalFormulaException
	 *          if there is error setting the time range.
	 */
	void overrideTimeRange(DateRange range) throws IllegalFormulaException;

	public enum Type {
		TILE, TIME_SERIES_LINE, CONTOUR, TIME_SERIES_BAR, SCATTER_PLOT,
		VERTICAL_CROSS_SECTION, VECTOR, AREAL_INTERPOLATION
	}

	;

	/**
	 * Evaluates the formula to produce a DataFrame.
	 *
	 * @param manager the manager used to get read access to the relevant data
	 * @param ranges  the ranges that constrain the evaluation
	 * @return a DataFrame containing the result of evaluating the formula.
	 * @throws IllegalFormulaException if the formula is unable to be executed.
	 */
	DataFrame evaluate(DataManager manager, List<AxisRange> ranges) throws IllegalFormulaException;

	/**
	 * Evaluates the formula to produce a DataFrame.
	 *
	 * @param manager      the manager used to get read access to the relevant data
	 * @param ranges       the ranges that constrain the evaluation
	 * @param convertUnits whether or not to convert the units when evaluating the formula
	 * @return a DataFrame containing the result of evaluating the formula.
	 * @throws IllegalFormulaException if the formula is unable to be executed.
	 */
	DataFrame evaluate(DataManager manager, List<AxisRange> ranges, boolean convertUnits) throws IllegalFormulaException;

	/**
	 * Validates the formula. This checks that the units,
	 * layers domains and timestep are coherent and within range.
	 *
	 * @param manager the data manager used to validate the formula
	 * @param ranges  the ranges that will filter or constrain the evaluation.
	 * @return a ValidationResult containing the results of the validation.
	 */
	ValidationResult validate(DataManager manager, List<AxisRange> ranges);
}
