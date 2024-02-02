package anl.verdi.loaders;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;

import java.util.Date;
import java.util.List;

import anl.verdi.data.AxisRange;
import anl.verdi.data.Variable;


public class ICTDataArrayWrapper extends ucar.ma2.ArrayDouble.D4 {
	
	ICTDataArray source = null;
	Variable var = null;
	Long startIndex = null;
	Long endIndex = null;
	
	int variableIndex = -1;

	public ICTDataArrayWrapper(ICTDataArray source, Variable var, Long startIndex, Long endIndex) {
		super(0, 0, 0, 0);
		this.source = source;
		
		this.startIndex = startIndex;
		this.endIndex = endIndex;
	
		
		try {
			variableIndex = source.columnNameMap.get(var.getName());
		} catch (Throwable t) {
			System.err.println("Could locate column for " + var.getName());
			t.printStackTrace();
			throw t;
		}
		
	}
	
	public IndexIterator getIndexIterator() {
		return new ICTIndexIterator(source.rawData, source.time, variableIndex, startIndex, endIndex);
	}

}
