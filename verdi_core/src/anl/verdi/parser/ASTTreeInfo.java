package anl.verdi.parser;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ASTTreeInfo {

	private Set<String> varNames = new HashSet<String>();
	private boolean hasAggregates = false;
	private Set<AggregateFuncNode> aggregateNodes = new HashSet<AggregateFuncNode>();
	private ASTstart start;

	public ASTTreeInfo() {}

	public ASTTreeInfo(ASTstart start) {
		this.start = start;
		start.gatherInfo(this);
		for (int i = 0; i < start.children.length; i++) {
			findAggregate((SimpleNode) start.children[i]);
		}
	}

	private void findAggregate(SimpleNode node) {
		if (node instanceof AggregateFuncNode) {
			aggregateNodes.add((AggregateFuncNode) node);
		} else {
			if (node.children != null) {
				for (int i = 0; i < node.children.length; i++) {
					findAggregate((SimpleNode) node.children[i]);
				}
			}
		}
	}

	public Set<AggregateFuncNode> getAggregateNodes() {
		return aggregateNodes;
	}

	public boolean hasAggregates() {
		return hasAggregates;
	}

	public void setHasAggregates(boolean hasAggregates) {
		this.hasAggregates = hasAggregates;
	}

	void addVarName(String name) {
		varNames.add(name);
	}

	public Set<String> getVariableNames() {
		return varNames;
	}

	public ASTstart getStart() {
		return start;
	}
}
