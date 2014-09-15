package anl.verdi.formula;

import anl.verdi.data.DataFrame;
import anl.verdi.parser.ASTTreeInfo;
import anl.verdi.util.FormulaArray;


/**
 * Interface for classes that implement formula parsing and evaluation.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface FormulaParser {

	/**
	 * Parses the formula and returns information
	 * about the parse tree.
	 *
	 * @return returns information about the parse tree.
	 * @throws IllegalFormulaException if there is an error
	 * during parsing.
	 */
	ASTTreeInfo parse() throws IllegalFormulaException;

	/**
	 * Gets the String representation of the formula.
	 *
	 * @return the String representation of the formula.
	 */
	String getFormulaAsString();

	/**
	 * Sets the coordinate indices under which the next evaluation will
	 * take place.
	 *
	 * @param timeStep the time step index
	 * @param layer the layer index
	 * @param x the x index
	 * @param y the y index
	 */
	void setCoodinateIndices(int timeStep, int layer, int x, int y);

	/**
	 * Sets the value of the variable to the specified array.
	 *
	 * @param name the name of the variable
	 * @param value the array value
	 */
	void setVariable(String name, DataFrame value);

	/**
	 * Evaluates the formula and returns the result as a FormulaArray.
	 *
	 * @return the result of the evaluation
	 * @throws anl.verdi.formula.IllegalFormulaException if the formula is invalid
	 */
	FormulaArray evaluate() throws IllegalFormulaException;
}
