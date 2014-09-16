package anl.verdi.loaders;

import java.io.IOException;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import anl.verdi.data.AbstractDataReader;
import anl.verdi.data.AxisRange;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameBuilder;
import anl.verdi.data.Variable;

/**
 * Interface for classes that handle reading data from
 * datasets.  This data could be originally from a file
 * or from a model or other source.
 *
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 * @see Dataset, DataLoader
 */
public class Models3Reader extends AbstractDataReader<Models3Dataset> {

	public Models3Reader(Models3Dataset set) {
		super(set);
	}

	private void createDefaultAxes(DataFrameBuilder builder, Models3Dataset set, ucar.nc2.Variable variableDS) {
		for (CoordAxis axis : set.getCoordAxes().getAxes()) {
			int index = variableDS.findDimensionIndex(set.getNetDataset().
							findCoordinateAxis(axis.getName()).getDimension(0).getShortName());		// getName() was deprecated
			//builder.addAxis(new DataFrameAxis(axis, index));
		}
	}

	/**
	 * get the values for the given data parameters
	 */
	public DataFrame getValues(Models3Dataset set, List<AxisRange> ranges, Variable variable) {
		ucar.nc2.Variable varDS = set.getVariableDS(variable);
		if (varDS == null) return null;
		try {
			DataFrameBuilder builder = new DataFrameBuilder();
			builder.addDataset(set).setVariable(variable);
			createDefaultAxes(builder, set, varDS);
			int rank = varDS.getRank();
			int[] origin = new int[rank];
			int[] shape = new int[rank];
			// default the shape array to the extent of
			// each rank in the variable.
			System.arraycopy(varDS.getShape(), 0, shape, 0, rank);
			for (AxisRange axis : ranges) {
				int dimIndex = varDS.findDimensionIndex(set.getNetDataset().
							findCoordinateAxis(axis.getName()).getDimension(0).getShortName());		// getName() is deprecated
				origin[dimIndex] = axis.getOrigin();
				shape[dimIndex] = axis.getExtent();
				//DataFrameAxis frameAxis = new DataFrameAxis(axis.getAxis(), axis.getOrigin(), axis.getExtent(), dimIndex);
				//builder.addAxis(frameAxis);
			}

			Array array = varDS.read(origin, shape);
			builder.setArray(array);
			return builder.createDataFrame();
		} catch (IOException ie) {
			System.out.println("IOException " + ie);
			ie.printStackTrace();
		} catch (InvalidRangeException e) {
			System.out.println("InvalidRangeException " + e);
			e.printStackTrace();
		}
		return null;
	}
}
