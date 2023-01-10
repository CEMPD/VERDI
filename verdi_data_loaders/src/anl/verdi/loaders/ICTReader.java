package anl.verdi.loaders;

import java.util.List;

import ucar.ma2.Array;
import anl.verdi.data.AbstractDataReader;
import anl.verdi.data.AxisRange;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameBuilder;
import anl.verdi.data.Variable;

/**
 * Interface for classes that handle reading data from
 * datasets.  This data could be originally from a file
 * or from a model or other source.
 *
 * @author Mary Ann Bitz
 * @author Eric Tatara
 * @version $Revision$ $Date$
 * @see anl.verdi.data.Dataset , DataLoader
 */
public class ICTReader extends AbstractDataReader<ICTDataset> {

	public ICTReader(ICTDataset set) {
		super(set);
	}

	private void createDefaultAxes(DataFrameBuilder builder, ICTDataset set) {
		int index = 0;
		for (CoordAxis axis : set.getCoordAxes().getAxes()) {			
			builder.addAxis(DataFrameAxis.createDataFrameAxis(axis, index));
			index++;
		}
	}

	/**
	 * get the values for the given data parameters
	 */
	public DataFrame getValues(ICTDataset set, List<AxisRange> ranges, Variable variable) {

		DataFrameBuilder builder = new DataFrameBuilder();

		builder.addDataset(set).setVariable(variable);

		createDefaultAxes(builder, set);

		for (AxisRange axis : ranges) {
			if (axis.getExtent() < 0)
				return null;
			int dimIndex = 0;

			DataFrameAxis frameAxis = DataFrameAxis.createDataFrameAxis(axis.getAxis(), axis.getOrigin(), axis.getExtent(), dimIndex);

			builder.addAxis(frameAxis);
		}

		Array array = new ICTDataArrayWrapper((ICTDataArray)set.getArray(), variable);

		builder.setArray(array);

		return builder.createDataFrame();

	}
}
