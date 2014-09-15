package anl.verdi.formula;

//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import anl.verdi.data.DataFrame;
import anl.verdi.data.DataTransformer;
import anl.verdi.data.TimeStepAverager;
import anl.verdi.parser.Frame;

/**
 * Factory for producing formulas for different type data plots. 
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class FormulaFactory {

	// no op transformer that just returns the
	// passed in data frame.
	private static final DataTransformer NO_OP_TRANSFORMER = new DataTransformer() {
		public DataFrame transform(DataFrame frame) {
			return frame;
		}
	};

	private FormulaParser createParser(String formula) {
		return new DefaultParser(formula, new Frame());
	}

	public Formula createFormula(Formula.Type type, String formula, Unit commonUnit) {
		if (type == Formula.Type.TILE || type == Formula.Type.VERTICAL_CROSS_SECTION ||
						type == Formula.Type.SCATTER_PLOT || type == Formula.Type.VECTOR) {
			return createTileFormula(formula, commonUnit);
		}
		
		else if(type == Formula.Type.TIME_SERIES_LINE || type == Formula.Type.TIME_SERIES_BAR ) {
			return createTimeStepFormula(formula,commonUnit);
		}
		
		
		else if(type == Formula.Type.CONTOUR) {
			return createTileFormula(formula, commonUnit);
		}
		
		return null;
	}

	/**
	 * Creates a formula that produces data appropriate for a tile plot. The
	 * data produced by the formula will contain individual values for each
	 * x,y cell in the domain for each layer and timestep.
	 *
	 * @param formula the formula to evaluate
	 * @param commonUnit the desired unit for the result
	 * @return a Formula that will produce tile-style data.
	 */
	public Formula createTileFormula(String formula, Unit commonUnit) {
		return new DefaultFormula(createParser(formula), NO_OP_TRANSFORMER, commonUnit);
	}

	/**
	 * Creates a formula that produces data appropriate for a time step plot. The
	 * data produced by the formula will contain an average of the data at each time
	 * step. For each time step, first each layer's x,y domain data is averaged into a value
	 * of each layer. Then these layer values are themselves averaged.
	 *
	 * @param formula the formula to evaluate
	 * @param commonUnit the desired unit for the result
	 * @return a Formula that will produce tile-style data.
	 */
	public Formula createTimeStepFormula(String formula, Unit commonUnit) {
		return new DefaultFormula(createParser(formula), new TimeStepAverager(), commonUnit);
	}
}
