package anl.verdi.gui;

/**
 * Encapsulates a request to validate a formula.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface FormulaElementCreator {

	/**
	 * Creates a FormulaListElement out of the specified formula.
	 *
	 * @param strFormula the formula as a string
	 * @return the created FormulaListElement or null if the creation was unsuccessful.
	 */
	FormulaListElement create(String strFormula);
}
