/**
 * MPASNetcdfReader - reads MPAS data from netcdf files
 *
 * @author Tony Howard
 * @version $Revision$ $Date$
 * @see anl.verdi.data.Dataset , DataLoader
 */

package anl.verdi.loaders;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Dimension;
import ucar.nc2.dataset.NetcdfDataset;
import anl.verdi.data.AbstractDataReader;
import anl.verdi.data.Axes;
import anl.verdi.data.AxisRange;
import anl.verdi.data.AxisType;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameBuilder;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.Dataset;
import anl.verdi.data.MPASDataFrameBuilder;
import anl.verdi.data.Slice;
import anl.verdi.data.Variable;

@SuppressWarnings("rawtypes")
public class MPASNetcdfReader extends AbstractDataReader<MPASDataset> {
	static final Logger Logger = LogManager.getLogger(MPASNetcdfReader.class.getName());

	public MPASNetcdfReader(MPASDataset set) {
		super(set);
	}

	private void createDefaultAxes(DataFrameBuilder builder, MPASDataset set, ucar.nc2.Variable variableDS) {
		/*From NetCDF
		for (CoordAxis axis : set.getCoordAxes().getAxes()) {
			int index = variableDS.findDimensionIndex(set.getNetDataset().
							findCoordinateAxis(axis.getName()).getDimension(0).getShortName());	// .getName() is deprecated
			builder.addAxis(DataFrameAxis.createDataFrameAxis(axis, index));
		}
		*/
		Set<String> dimSet = new HashSet<String>();
		List<Dimension> dimensions = variableDS.getDimensions();
		HashMap<String, Integer> indexMap = new HashMap<String, Integer>();
		int i = 0;
		for (Dimension dim : dimensions) {
			dimSet.add(dim.getShortName());
			indexMap.put(dim.getShortName(), i++);
		}
		//Workaround for the nCells -> x/y mapping
		if (dimSet.contains("nCells"))
			dimSet.add("x");
		for (CoordAxis axis : set.getCoordAxes().getAxes()) {
			if (!dimSet.contains(axis.getName()))
				continue;
			if (axis.getAxisType().equals(AxisType.TIME))
				builder.addAxis(DataFrameAxis.createDataFrameAxis(axis, indexMap.get(axis.getName())));
			else if (axis.getAxisType().equals(AxisType.X_AXIS)) { //MPAS has "virtual"  x/y axes, which can't be referenced within the data.  Use cell index as axis, and include simulated x/y axes
				CoordAxis yAxis = set.getCoordAxes().getYAxis();
				CoordAxis cellAxis = new MPASCellCoordAxis(axis, yAxis, set.numCells, "nCells", "nCells", set);
				builder.addAxis(DataFrameAxis.createDataFrameAxis(cellAxis, indexMap.get("nCells")));
			}
			else if (axis.getAxisType().equals(AxisType.LAYER)) {
				builder.addAxis(DataFrameAxis.createDataFrameAxis(axis, indexMap.get(axis.getName())));
			}

		}
		
		/*Alternate from CSVReader
		for (AxisRange axis : ranges) {
			int dimIndex = 0;

			DataFrameAxis frameAxis = DataFrameAxis.createDataFrameAxis(axis.getAxis(), axis.getOrigin(), axis.getExtent(), dimIndex);

			builder.addAxis(frameAxis);
		}
		*/
	}
	
	private class MPASDataFrame implements DataFrame {
		
		MPASDataset dataset;
		NetcdfDataset netDs;
		String currentVar = null;
		List<Dataset> setList = new ArrayList<Dataset>();
		Map<String, ucar.nc2.Variable> varMap;
		
		
		
		private MPASDataFrame(MPASDataset ds) {
			dataset = ds;
			if (dataset != null) {
				setList.add(dataset);
				netDs = dataset.getNetDataset();
				varMap = new HashMap<String, ucar.nc2.Variable>();
				List<ucar.nc2.Variable> vars = netDs.getVariables();
				for (ucar.nc2.Variable var : vars) {
					varMap.put(var.getShortName(), var);
				}
			}

		}
		
		public void setVariable(Variable var) {
			currentVar = var.getName();
		}

		@Override
		public Array getArray() {
			try {
				return varMap.get(currentVar).read();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public Axes<DataFrameAxis> getAxes() {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<Dataset> getDataset() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Variable getVariable() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Class getArrayType() {
			throw new UnsupportedOperationException();
		}

		@Override
		public long getSize() {
			throw new UnsupportedOperationException();
		}

		@Override
		public int[] getShape() {
			throw new UnsupportedOperationException();
		}

		@Override
		public DataFrameIndex getIndex() {
			throw new UnsupportedOperationException();
		}

		@Override
		public double getDouble(DataFrameIndex index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public float getFloat(DataFrameIndex index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getInt(DataFrameIndex index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public long getLong(DataFrameIndex index) {
			throw new UnsupportedOperationException();
		}

		@Override
		public DataFrame slice(Slice slice) throws InvalidRangeException {
			throw new UnsupportedOperationException();
		}

		@Override
		public DataFrame sliceCopy(Slice slice) throws InvalidRangeException {
			throw new UnsupportedOperationException();
		}
		
	}
	
	private class MPASDoubleVariableFrame extends MPASDataFrame {
		
		double value;
		
		public MPASDoubleVariableFrame(double val) {
			super(null);
			value = val;
			
		}
		
		@Override
		public double getDouble(DataFrameIndex index) {
			return value;
		}
		
	}
	
	MPASDataFrame renderingFrame;

	/**
	 * get the values for the given data parameters
	 */
	public DataFrame getValues(MPASDataset set, List<AxisRange> ranges, Variable variable) {
		
		if (variable.getName().equals(MPASDataset.VAR_AVG_CELL_DIAM)) {
			return new MPASDoubleVariableFrame(set.getAvgCellDiam());
		}
		ucar.nc2.Variable varDS = set.getVariableDS(variable);
		if (varDS == null) {
			return null;
		}
		if (ranges == null) {
			if (renderingFrame == null)
				renderingFrame = new MPASDataFrame(set);
			renderingFrame.setVariable(variable);
		}
		try {
			DataFrameBuilder builder = new MPASDataFrameBuilder();
			builder.addDataset(set).setVariable(variable);
			createDefaultAxes(builder, set, varDS);
			int rank = varDS.getRank();
			int[] origin = new int[rank];
			int[] shape = new int[rank];
			// default the shape array to the extent of
			// each rank in the variable.
			System.arraycopy(varDS.getShape(), 0, shape, 0, rank);
			Array array = null;
			if (ranges != null) {
				for (AxisRange axis : ranges) {
					int dimIndex =  -1;
					try {
						dimIndex = varDS.findDimensionIndex(set.getNetDataset().
								findCoordinateAxis(axis.getName()).getDimension(0).getShortName());	// getName() is deprecated
					} catch (NullPointerException e) {
						//System.out.println("Could not find axis " + axis.getName() + " for var " + varDS.getName());
						//Not available from NetDataset in MPAS files
						dimIndex = varDS.findDimensionIndex(axis.getName());
					}
					if (dimIndex == -1) {
						continue;
					}
					origin[dimIndex] = axis.getOrigin();
					shape[dimIndex] = axis.getExtent();
					DataFrameAxis frameAxis = DataFrameAxis.createDataFrameAxis(axis.getAxis(), axis.getOrigin(), axis.getExtent(), dimIndex);
					builder.addAxis(frameAxis);
				}
				array = set.read(variable.getName(), origin, shape);
			}
			else
				array = set.read(variable.getName());
			builder.setArray(array);
			return builder.createDataFrame();
		} catch (IOException ie) {
			Logger.error((String)null, ie);
		} catch (InvalidRangeException e) {
			Logger.error((String)null, e);
		}
		return null;
	}
}
