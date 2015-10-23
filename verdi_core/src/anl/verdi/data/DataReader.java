package anl.verdi.data;

import java.util.List;

/**
 * Interface for classes that handle reading data from 
 * datasets.  This data could be originally from a file
 * or from a model or other source.
 *
 * @see Dataset, DataLoader
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 *
 */
public interface DataReader<T extends Dataset> {
	/**
	 * Get the values corresponding to the given data
	 * @param set the dataset of interest
	 * @param ranges the ranges of the axes
	 * @param variable the variable desired
	 * @return
	 */
	public DataFrame getValues(T set, List<AxisRange> ranges, Variable variable);


}
