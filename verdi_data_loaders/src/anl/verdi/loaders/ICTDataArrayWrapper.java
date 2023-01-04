package anl.verdi.loaders;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;

import java.util.List;

import anl.verdi.data.Variable;


public class ICTDataArrayWrapper extends ucar.ma2.ArrayDouble.D4 {
	
	ICTDataArray source = null;
	Variable var = null;
	
	int variableIndex = -1;

	public ICTDataArrayWrapper(ICTDataArray source, Variable var) {
		super(0, 0, 0, 0);
		this.source = source;
		
		try {
			variableIndex = source.columnNameMap.get(var.getName());
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
		
	}
	
	public IndexIterator getIndexIterator() {
		return new ICTIndexIterator(source.rawData, variableIndex);
	}

}
