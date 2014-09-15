package anl.verdi.gui;

import java.util.ArrayList;
import java.util.List;

import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;
import anl.verdi.formula.FormulaVariable;

/**
 * Element in a formula list.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class FormulaListElement extends AbstractListElement {

	private String formula;
	private List<FormulaVariable> variables;

	public FormulaListElement(String formula) {
		this(formula, new ArrayList<FormulaVariable>());
		System.out.println("in FormulaListElement constructor String");
	}

	public FormulaListElement(String formula, List<FormulaVariable> variables) {
		System.out.println("in FormulaListElement constructor String List");
		this.formula = formula;
		this.variables = variables;

		if (variables.size() > 0) {
			FormulaVariable var = variables.get(0);
			CoordAxis axis = var.getDataset().getCoordAxes().getZAxis();
			if (axis != null) {
				layerMin = (int) axis.getRange().getOrigin();
				layerMax = layerMin + (int) axis.getRange().getExtent() - 1;
			} else {
				layerMin = NO_LAYER_VALUE;
			}

			CoordAxis time = var.getDataset().getCoordAxes().getTimeAxis();
			if (time != null) {
				for (int i = 1; i < variables.size(); i++) {
					FormulaVariable v = variables.get(i);
					CoordAxis otherTime = v.getDataset().getCoordAxes().getTimeAxis();
					if (!otherTime.getUnits().equals(time.getUnits()) || !time.getRange().equals(otherTime.getRange())) {
						timeMin = NO_TIME_VALUE;
						break;
					}
				}
			} else {
				timeMin = NO_TIME_VALUE;
			}
			if (timeMin != NO_TIME_VALUE) {
				timeMin = (int) time.getRange().getOrigin();
				timeMax = timeMin + (int) time.getRange().getExtent() - 1;
			}
		}
	}

	/**
	 *
	 * @return an iterable over the formula variables in this element.
	 */
	public Iterable<FormulaVariable> variables() {
		System.out.println("in FormulaListElement variables");
		return variables;
	}

	public Axes<CoordAxis> getAxes() {
		System.out.println("in FormulaListElement getAxes");
		if (variables.size() > 0) return variables.get(0).getDataset().getCoordAxes();
		return null;
	}

	public String getFormula() {
		System.out.println("in FormulaListElement getFormula");
		return formula;
	}
	
	public Dataset getDataset() {
		System.out.println("in FormulaListElement getDataset");
		if (variables.size() > 0) return variables.get(0).getDataset();
		return null;
	}

	public String toString() {
		System.out.println("in FormulaListElement toString");
		Dataset ds = getDataset();
		
		return formula + (ds != null && ds.isObs() ? "(OBS)" : "");
	}
}
