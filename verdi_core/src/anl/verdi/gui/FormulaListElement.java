package anl.verdi.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.data.Axes;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.Dataset;
import anl.verdi.data.MPASCellAxis;
import anl.verdi.data.MultiAxisDataset;
import anl.verdi.formula.FormulaVariable;

/**
 * Element in a formula list.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class FormulaListElement extends AbstractListElement {
	static final Logger Logger = LogManager.getLogger(FormulaListElement.class.getName());

	private String formula;
	private List<FormulaVariable> variables;
	private CoordAxis zAxis;
	private CoordAxis timeAxis;

	public FormulaListElement(String formula) {
		this(formula, new ArrayList<FormulaVariable>());
		Logger.info("in FormulaListElement constructor String");
	}

	public FormulaListElement(String formula, List<FormulaVariable> variables) {
		Logger.info("in FormulaListElement constructor String List");
		this.formula = formula;
		this.variables = variables;

		if (variables.size() > 0) {
			Dataset ds = variables.get(0).getDataset();
			if (ds instanceof MultiAxisDataset) {
				zAxis = ((MultiAxisDataset)ds).getZAxis(variables.get(0).getName());
				timeAxis = ((MultiAxisDataset)ds).getTimeAxis(variables.get(0).getName());
			}
			else {
				zAxis = getZAxisForVariable(variables.get(0));
				timeAxis = getTimeAxisForVariable(variables.get(0));
			}
			FormulaVariable var = variables.get(0);
			if (zAxis != null) {
				layerMin = (int) zAxis.getRange().getOrigin();
				layerMax = layerMin + (int) zAxis.getRange().getExtent() - 1;
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
	
	public CoordAxis getZAxisForVariable(FormulaVariable var) {		
		for (CoordAxis axis : var.getDataset().getCoordAxes().getAxes()) {
			if (axis instanceof MPASCellAxis) {
				return ((MPASCellAxis)axis).getZAxis(var.getName());
			}
		}
		return null;
	}

	public CoordAxis getTimeAxisForVariable(FormulaVariable var) {		
		for (CoordAxis axis : var.getDataset().getCoordAxes().getAxes()) {
			if (AxisType.TIME.equals(axis.getAxisType()))
				return axis;
		}
		return null;
	}

	/**
	 *
	 * @return an iterable over the formula variables in this element.
	 */
	public Iterable<FormulaVariable> variables() {
		Logger.info("in FormulaListElement variables");
		return variables;
	}

	public Axes<CoordAxis> getAxes() {
		Logger.info("in FormulaListElement getAxes");
		if (variables.size() > 0) return variables.get(0).getDataset().getCoordAxes();
		return null;
	}
	
	public CoordAxis getDefaultTimeAxis() {
		return timeAxis;
	}

	public CoordAxis getDefaultZAxis() {
		return zAxis;
	}

	public String getFormula() {
		Logger.info("in FormulaListElement getFormula");
		return formula;
	}
	
	public Dataset getDataset() {
		Logger.info("in FormulaListElement getDataset");
		if (variables.size() > 0) return variables.get(0).getDataset();
		return null;
	}

	public String toString() {
		Logger.info("in FormulaListElement toString");
		Dataset ds = getDataset();
		
		return formula + (ds != null && ds.isObs() ? "(OBS)" : "");
	}
}
