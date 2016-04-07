/**
 * MPASPlotDataFrame - This class is used for plotting charts based on MPAS data.  Convenient because 
 * MPAS data is not structured along X Y coordinates
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/


package anl.verdi.data;

import java.util.ArrayList;
import java.util.List;

import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;


@SuppressWarnings("rawtypes")
public class MPASPlotDataFrame implements DataFrame {

	MeshCellInfo cellInfo;
	
	Array array;
	
	Variable variable;
	
	Axes<DataFrameAxis> axes;
	
	List<Dataset> datasets = new ArrayList<Dataset>();
	
	String title;
	
	public MPASPlotDataFrame(String label, Array source, Slice slice, Variable var, Axes<DataFrameAxis> srcAxes, Dataset set) throws InvalidRangeException {
		title = label;
		variable = var;
		datasets.add(set);
		array = sliceArray(slice, source, srcAxes);
		axes = sliceAxes(slice, srcAxes);
	}
	
	@Override
	public Array getArray() {
		return array;
	}

	@Override
	public Axes<DataFrameAxis> getAxes() {
		return axes;
	}

	@Override
	public List<Dataset> getDataset() {
		return datasets;
	}

	@Override
	public Variable getVariable() {
		return variable;
	}

	@Override
	public Class getArrayType() {
		return array.getElementType();
	}

	@Override
	public long getSize() {
		return array.getSize();
	}

	@Override
	public int[] getShape() {
		return array.getShape();
	}

	@Override
	public DataFrameIndex getIndex() {
		return new MPASDataFrameIndex(this);
	}

	@Override
	public double getDouble(DataFrameIndex index) {
		return array.getDouble(index.index);
	}

	@Override
	public float getFloat(DataFrameIndex index) {
		return array.getFloat(index.index);
	}

	@Override
	public int getInt(DataFrameIndex index) {
		return array.getInt(index.index);
	}

	@Override
	public long getLong(DataFrameIndex index) {
		return array.getLong(index.index);
	}
	
	private Array sliceArray(Slice slice, Array srcArray, Axes<DataFrameAxis> fullAxes) throws InvalidRangeException {
		if (slice == null)
			return srcArray;
		int[] origin = AbstractDataFrame.createOrigins(slice, fullAxes);
		int[] extents = AbstractDataFrame.createExtents(slice, fullAxes);
		return srcArray.sectionNoReduce(origin, extents, null);
	}
	
	private Axes<DataFrameAxis> sliceAxes(Slice slice, Axes<DataFrameAxis> srcAxes) {
		if (slice == null)
			return srcAxes;
		List<DataFrameAxis> axisList = new ArrayList<DataFrameAxis>();
		
		if (slice.getCellRange() == null || slice.getCellRange().getExtent() == srcAxes.getCellAxis().getRange().getExtent())
			axisList.add(srcAxes.getCellAxis());
		else {
			DataFrameAxis cellAxis = DataFrameAxis.createDataFrameAxis(srcAxes.getCellAxis().getAxis(), (int)slice.getCellRange().getOrigin(), (int)slice.getCellRange().getExtent(), srcAxes.getCellAxis().getArrayIndex());
			axisList.add(cellAxis);
		}

		if (slice.getTimeRange() == null || slice.getTimeRange().getExtent() == srcAxes.getTimeAxis().getRange().getExtent())
			axisList.add(srcAxes.getTimeAxis());
		else {
			DataFrameAxis newAxis = DataFrameAxis.createDataFrameAxis(srcAxes.getTimeAxis().getAxis(), (int)slice.getTimeRange().getOrigin(), (int)slice.getTimeRange().getExtent(), srcAxes.getTimeAxis().getArrayIndex());
			axisList.add(newAxis);
		}

		if (slice.getLayerRange() == null || slice.getLayerRange().getExtent() == srcAxes.getZAxis().getRange().getExtent())
			axisList.add(srcAxes.getZAxis());
		else {
			DataFrameAxis newAxis = DataFrameAxis.createDataFrameAxis(srcAxes.getZAxis().getAxis(), (int)slice.getLayerRange().getOrigin(), (int)slice.getLayerRange().getExtent(), srcAxes.getZAxis().getArrayIndex());
			axisList.add(newAxis);
		}

		return new Axes<DataFrameAxis>(axisList);
	}
	
	@Override
	public DataFrame slice(Slice slice) throws InvalidRangeException {
		Array newArray = sliceArray(slice, array, axes);
		Axes<DataFrameAxis> newAxes = sliceAxes(slice, axes);
		return new MPASPlotDataFrame(title, newArray, null, variable, newAxes,datasets.get(0));
	}

	@Override
	public DataFrame sliceCopy(Slice slice) throws InvalidRangeException {
		Array newArray = sliceArray(slice, array, axes).copy();
		Axes<DataFrameAxis> newAxes = sliceAxes(slice, axes);
		return new MPASPlotDataFrame(title, newArray, null, variable, newAxes,datasets.get(0));	
	}
	

	
	

}
