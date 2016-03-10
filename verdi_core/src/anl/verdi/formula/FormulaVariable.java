package anl.verdi.formula;

import java.util.ArrayList;
import java.util.List;



//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import anl.verdi.data.AxisRange;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataManager;
import anl.verdi.data.DataReader;
import anl.verdi.data.Dataset;
import anl.verdi.data.MultiLayerDataset;
import anl.verdi.data.Range;
import anl.verdi.data.Variable;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class FormulaVariable {

	public static FormulaVariable createVariable(String name, DataManager manager) throws IllegalFormulaException {
		String[] split = manager.splitVarName(name);
		FormulaVariable var = new FormulaVariable(split[1], split[0]);
		var.init(manager);
		return var;
	}

	private String name;
	private String alias;
	private String aliasedName;
	private Dataset dataset;
	private DataReader reader;
	private Variable variable;
	private Range timeStepRange;


	private FormulaVariable(String name, String alias) {
		this.alias = alias;
		this.name = name;
		this.aliasedName = name + alias;
	}

	private void init(DataManager manager) throws IllegalFormulaException {
		if (dataset == null) {
			dataset = manager.getDataset(alias);
			if (dataset == null)
				throw new IllegalFormulaException("Alias '" + alias + "' does not identify a loaded dataset.");
			reader = manager.getDataReader(dataset);
			variable = dataset.getVariable(name);
			if (variable == null)
				throw new IllegalFormulaException("Variable '" + name + "' not found in dataset '" + alias + "'");
		}
	}


	public Range getTimeStepRange() {
		return timeStepRange;
	}

	public void setTimeStepRange(Range timeStepRange) {
		this.timeStepRange = timeStepRange;
	}

	public DataFrame evaluate(List<AxisRange> ranges) {
		List<AxisRange> myRanges = new ArrayList<AxisRange>(ranges);
		
//		NOTE: let the ranges control the subset
//		if (timeStepRange != null) {
//			removeTimeStepRange(myRanges);
//			myRanges.add(new AxisRange(dataset.getCoordAxes().getTimeAxis(), timeStepRange.getOrigin(),
//							(int)timeStepRange.getExtent()));
//		}
		return reader.getValues(dataset, myRanges, variable);
	}

//	private void removeTimeStepRange(List<AxisRange> ranges) {
//		for (Iterator<AxisRange> iter = ranges.iterator(); iter.hasNext(); ) {
//			AxisRange range = iter.next();
//			if (range.getAxisType() == AxisType.TIME) iter.remove();
//		}
//	}

	public String getAliasedName() {
		return aliasedName;
	}

	public String getName() {
		return name;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public Unit getUnit() {
		return variable.getUnit();
	}
	
	public CoordAxis getZAxis() {
		if (dataset instanceof MultiLayerDataset)
			return ((MultiLayerDataset)dataset).getZAxis(name);
		return dataset.getCoordAxes().getZAxis();
	}
}
