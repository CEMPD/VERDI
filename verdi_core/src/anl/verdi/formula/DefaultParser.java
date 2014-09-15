package anl.verdi.formula;

import java.io.StringReader;

import anl.verdi.data.DataFrame;
import anl.verdi.parser.ASTTreeInfo;
import anl.verdi.parser.ASTstart;
import anl.verdi.parser.Frame;
import anl.verdi.parser.ParseException;
import anl.verdi.parser.Parser;
import anl.verdi.util.FormulaArray;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DefaultParser implements FormulaParser {

	private String formula;
	private Parser parser;
	private Frame frame;
	private ASTTreeInfo treeInfo;

	public DefaultParser(String formula, Frame frame) {
		this.formula = formula;
		this.frame = frame;
		String tmp = formula;
		if (!tmp.endsWith(";")) tmp += ";";
		this.parser = new Parser(new StringReader(tmp));
	}

	private void preprocess() throws IllegalFormulaException {
		if (treeInfo == null) parse();
		treeInfo.getStart().preprocess(frame);
	}

	/**
	 * Sets the value of the variable to the specified array.
	 *
	 * @param name the name of the variable
	 * @param value the array value
	 */
	public void setVariable(String name, DataFrame value) {
		frame.setValue(name, value);
	}

	/**
	 * Evaluates the formula and returns the result as a FormulaArray.
	 *
	 * @return the result of the evaluation
	 * @throws IllegalFormulaException if the formula is invalid
	 */
	public FormulaArray evaluate() throws IllegalFormulaException {
		preprocess();
		return treeInfo.getStart().evaluate(frame);
	}

	public String getFormulaAsString() {
		return formula;
	}

	public ASTTreeInfo parse() throws IllegalFormulaException {
		try {
			ASTstart start = parser.start();
			treeInfo = new ASTTreeInfo(start);
		} catch (ParseException e) {
			throw new IllegalFormulaException("Error parsing formula", e);
		}
		return treeInfo;
	}

	/**
	 * Sets the coordinate indices under which the next evaluation will
	 * take place.
	 *
	 * @param timeStep the time step index
	 * @param layer the layer index
	 * @param x the x index
	 * @param y the y index
	 */
	public void setCoodinateIndices(int timeStep, int layer, int x, int y) {
		frame.setCoordinateIndices(timeStep, layer, x, y);
	}
}
