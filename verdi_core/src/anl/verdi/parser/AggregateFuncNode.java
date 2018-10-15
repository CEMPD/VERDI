package anl.verdi.parser;

import anl.verdi.formula.IllegalFormulaException;
import anl.verdi.util.FormulaArray;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class AggregateFuncNode extends SimpleNode {

	public AggregateFuncNode(int i) {
		super(i);
	}

	public AggregateFuncNode(Parser p, int i) {
		super(p, i);
	}


	/**
	 * Sets has aggregates on the tree info object to true.
	 *
	 * @param info collection of tree info
	 */
	@Override
	public void gatherInfo(ASTTreeInfo info) {
		info.setHasAggregates(true);
		super.gatherInfo(info);
	}

	protected int min(int indexVal, Frame frame) throws IllegalFormulaException {
		FormulaArray array = jjtGetChild(0).evaluate(frame);
		int[] mins = array.minIndices();
		return mins[indexVal];
	}

	protected int max(int indexVal, Frame frame) throws IllegalFormulaException {
		FormulaArray array = jjtGetChild(0).evaluate(frame);
		int[] maxs = array.maxIndices();
		return maxs[indexVal];
	}
}


