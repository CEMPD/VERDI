package anl.verdi.formula;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import anl.verdi.data.DataFrame;
import anl.verdi.parser.ASTTreeInfo;
import anl.verdi.util.FormulaArray;

/**
 * Simple parser just for testing.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class SimpleParser implements FormulaParser {

	private String formula;
	private List<String> vars = new ArrayList<String>();
	private Map<String, Double> values = new HashMap<String, Double>();

	public SimpleParser(String formula) {
		this.formula = formula;
		StringTokenizer tok = new StringTokenizer(formula);
		while (tok.hasMoreTokens()) {
			String token = tok.nextToken();
			if (!token.equals("+")) {
				vars.add(token);
			}
		}
	}

	public FormulaArray evaluate() {
		double sum = 0;
		for (double value : values.values()) {
			sum += value;
		}

		return new FormulaArray(sum);
	}


	/**
	 * Sets the value of the variable to the specified array.
	 *
	 * @param name  the name of the variable
	 * @param value the array value
	 */
	public void setVariable(String name, DataFrame value) {
		//todo implement method
	}

	public String getFormulaAsString() {
		return formula;
	}

	public void setValue(String varName, double value) {
		values.put(varName, value);
	}

	public ASTTreeInfo parse() {
		return new ASTTreeInfo() {
			public Set<String> getVariableNames() {
				return new HashSet<String>(vars);
			}
		};
	}


	public void setCoodinateIndices(int timeStep, int layer, int x, int y) {
		//todo implement method
	}

	public void aggregate() throws IllegalFormulaException {}

	public void postAggregate() throws IllegalFormulaException {

	}
}
