package anl.verdi.loaders;

import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.IndexIterator;

import java.util.Date;
import java.util.List;

import anl.verdi.data.AxisRange;
import anl.verdi.data.Variable;


public class GenericDataArrayWrapper extends ucar.ma2.ArrayDouble.D4 {
	
	GenericDataArray source = null;
	Variable var = null;
	long dataStart = 0;
	long dataEnd = 0;
	
	int variableIndex = -1;

	public GenericDataArrayWrapper(GenericDataArray source, Variable var, Date startDate, Date endDate) {
		super(0, 0, 0, 0);
		this.source = source;
		
		if (startDate != null) {
			ICTTimeAxis timeAxis = (ICTTimeAxis)((ICTDataset)var.getDataset()).getCoordAxes().getTimeAxis();
			if (startDate != null) {
				this.dataStart = (startDate.getTime() - timeAxis.getStartDate().getTime().getTime()) / 1000;
				if (endDate != null)
					this.dataEnd = (endDate.getTime() - timeAxis.getStartDate().getTime().getTime()) / 1000;
				else
					this.dataEnd = Long.MAX_VALUE;
			}
		}
		
		/*try {
			variableIndex = source.columnNameMap.get(var.getName());
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}*/
		
	}
	
	public Index getIndex() {
		return super.getIndex();
	}
	
	public IndexIterator getIndexIterator() {
		return new GenericIndexIterator(source.rawData, variableIndex, dataStart, dataEnd);
	}

}
