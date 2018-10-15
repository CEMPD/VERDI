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
 * Interface for classes that handle reading data from datasets. This data could
 * be originally from a file or from a model or other source.
 * 
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 * @see anl.verdi.data.Dataset , DataLoader
 */
public class Models3ObsReader extends AbstractDataReader<Models3ObsDataset> {
	static final Logger Logger = LogManager.getLogger(Models3ObsReader.class.getName());

	public Models3ObsReader(Models3ObsDataset set) {
		super(set);
	}

	private void createDefaultAxes(DataFrameBuilder builder,
			Models3ObsDataset set, ucar.nc2.Variable variableDS) {
		for (CoordAxis axis : set.getCoordAxes().getAxes()) {
			int index = variableDS.findDimensionIndex(set.getNetDataset()
					.findCoordinateAxis(axis.getName()).getDimension(0)
					.getShortName());		// getName() was deprecated
			builder.addAxis(DataFrameAxis.createDataFrameAxis(axis, index));
		}
	}

	/**
	 * get the values for the given data parameters
	 */
	public synchronized DataFrame getValues(Models3ObsDataset set, List<AxisRange> ranges,
			Variable variable) {
		ucar.nc2.Variable varDS = set.getVariableDS(variable);
		
		if (varDS == null) return null;
		
		try {
			ucar.nc2.Variable slice = varDS;
			DataFrameBuilder builder = new DataFrameBuilder();
			builder.addDataset(set).setVariable(variable);
			createDefaultAxes(builder, set, varDS);
			int count = 0;
			int rank = varDS.getRank();
			int[] shape = new int[rank];
			// default the shape array to the extent of each rank in the variable.
			System.arraycopy(varDS.getShape(), 0, shape, 0, rank);
			
			for (AxisRange axis : ranges) {
				if (count + 1 > rank) break;
				
				int dimIndex = varDS.findDimensionIndex(set.getNetDataset()
						.findCoordinateAxis(axis.getName()).getDimension(0)
						.getShortName());		// getName() was deprecated
				// make sure the extent <= maximum shape vale in varDS
				int maxExt = shape[dimIndex];
				int ext = (axis.getExtent() > maxExt ? maxExt : axis.getExtent()); 

				if (slice.getDimension(0).getLength() > 1)
					slice = slice.slice(0, ext);
				
				DataFrameAxis frameAxis = DataFrameAxis.createDataFrameAxis(
						axis.getAxis(), axis.getOrigin(), ext, dimIndex);
				builder.addAxis(frameAxis);
				count++;
			}

			Array array = slice.read();
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
