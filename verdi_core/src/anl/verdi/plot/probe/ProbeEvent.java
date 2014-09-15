package anl.verdi.plot.probe;

import anl.verdi.data.DataFrame;
import anl.verdi.data.Slice;
import anl.verdi.formula.Formula;
import anl.verdi.plot.gui.Plot;

/**
 * Encapsulates the details of plot being probed. 
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class ProbeEvent {

	protected Plot source;
	protected DataFrame probedData;
	protected Slice slice;
	protected Formula.Type sourceType;
	protected Boolean isXConstant = null;
	protected Boolean isLog = false;
	protected Double logBase = 1.0;

	/**
	 * Creates a ProbeEvent from the source, data, and slice
	 *
	 * @param source the plot source of the event
	 * @param probedData the probed data
	 * @param slice the slice of the original data this is represented by
	 * the probed data.
	 */
	public ProbeEvent(Plot source, DataFrame probedData, Slice slice, Formula.Type sourceType) {
		this.sourceType = sourceType;
		this.probedData = probedData;
		this.source = source;
		this.slice = slice;
	}

	
	public DataFrame getProbedData() {
		return probedData;
	}

	public Plot getSource() {
		return source;
	}

	public Slice getSlice() {
		return slice;
	}

	public Formula.Type getSourceType() {
		return sourceType;
	}

	public Boolean getXConstant() {
		return isXConstant;
	}

	public void setXConstant(Boolean XConstant) {
		isXConstant = XConstant;
	}
	
	public void setIsLog( Boolean log){
		this.isLog = log;
	}
	
	public Boolean getIsLog(){
		return this.isLog;
	}
	
	public void setLogBase( double base){
		this.logBase = base;
	}
	
	public Double getLogBase(){
		return this.logBase;
	}
}
