package anl.verdi.loaders;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
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
 * @version $Revision$ $Date$
 * @see anl.verdi.data.Dataset , DataLoader
 */
public class GridNetcdfReader extends AbstractDataReader<GridNetcdfDataset> {
	static final Logger Logger = LogManager.getLogger(GridNetcdfReader.class.getName());

	public GridNetcdfReader(GridNetcdfDataset set) {
		super(set);
	}

	private void createDefaultAxes(DataFrameBuilder builder, GridNetcdfDataset set, ucar.nc2.Variable variableDS) {
		//System.out.println("VariableDS Dimension String: " + variableDS.getDimensionsString());
		for (CoordAxis axis : set.getCoordAxes().getAxes()) {
			int index = 0;
			if (set.getNetDataset().findCoordinateAxis(axis.getName()) == null) {
				index = variableDS.findDimensionIndex(axis.getName());
			}
			else {
				index = variableDS.findDimensionIndex(set.getNetDataset().
							findCoordinateAxis(axis.getName()).getDimension(0).getShortName());	// .getName() is deprecated
			}
			builder.addAxis(DataFrameAxis.createDataFrameAxis(axis, index));
		}
	}

	/**
	 * get the values for the given data parameters
	 */
	public DataFrame getValues(GridNetcdfDataset set, List<AxisRange> ranges, Variable variable) {
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
				int dimIndex = -1;
				try {
					dimIndex = varDS.findDimensionIndex(set.getNetDataset().
							findCoordinateAxis(axis.getName()).getDimension(0).getShortName());	// getName() is deprecated
				} catch (Throwable t) {
					dimIndex = varDS.findDimensionIndex(axis.getName());
				}
				origin[dimIndex] = axis.getOrigin();
				shape[dimIndex] = axis.getExtent();
				DataFrameAxis frameAxis = DataFrameAxis.createDataFrameAxis(axis.getAxis(), axis.getOrigin(), axis.getExtent(), dimIndex);
				builder.addAxis(frameAxis);
			}

			Array array = varDS.read(origin, shape);
			builder.setArray(array);
			return builder.createDataFrame();
		} catch (IOException ie) {
			Logger.error("IOException " + ie.getMessage());
			ie.printStackTrace();
		} catch (InvalidRangeException e) {
			Logger.error("InvalidRangeException " + e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
