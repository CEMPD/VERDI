package anl.verdi.io;

import anl.verdi.gui.FormulaListElement;

/**
 * Wraps a FormulaListElement in an easier to persist form.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class FormulaElementDescriptor extends ListElementDescriptor {

	private String formula;

	public FormulaElementDescriptor(FormulaListElement element) {
		super(element);
		this.formula = element.getFormula();
	}

	public String getFormula() {
		return formula;
	}
}
