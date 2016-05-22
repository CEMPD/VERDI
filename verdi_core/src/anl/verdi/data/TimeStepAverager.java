package anl.verdi.data;

import java.util.ArrayList;
import java.util.List;

import anl.verdi.plot.data.IMPASDataset;

import anl.verdi.util.ArrayFactory;
import ucar.ma2.Array;
import ucar.ma2.Index;

/**
 * Averages the data at each time step.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TimeStepAverager implements DataTransformer {

	// averages x,y for each timestep -- assumes
	// input data has no layer -- results in a 1D
	// DataFrame
	private class TXYAvg implements DataTransformer {

		/**
		 * Averages the data at each time step. For each time step, each
		 * layer's x,y domain data is averaged into a value for each layer.
		 *
		 * @param frame the data to transform
		 * @return the data averaged for each time step.
		 */
		public DataFrame transform(DataFrame frame) {
			Axes<DataFrameAxis> axes = frame.getAxes();
			DataFrameAxis timeAxis = axes.getTimeAxis();
			DataFrameAxis xAxis = axes.getXAxis();
			DataFrameAxis yAxis = axes.getYAxis();
			int xExtent = xAxis.getExtent();
			int yExtent = yAxis.getExtent();
			int xySize = xExtent * yExtent;

			Array array = ArrayFactory.createDoubleArray(new int[]{timeAxis.getExtent()});
			Index index = array.getIndex();
			DataFrameIndex frameIndex = frame.getIndex();

			for (int time = 0; time < timeAxis.getExtent(); time++) {
				frameIndex.setTime(time);
				double xySum = 0;
				for (int x = 0; x < xExtent; x++) {
					for (int y = 0; y < yExtent; y++) {
						frameIndex.setXY(x, y);
						xySum += frame.getDouble(frameIndex);
					}
				}
				double avg = xySum / xySize;
				index.set(time);
				array.setDouble(index, avg);
			}

			DataFrameBuilder builder = new DataFrameBuilder();
			builder.addDataset(frame.getDataset()).setArray(array).setVariable(frame.getVariable());
			builder.addAxis(DataFrameAxis.createDataFrameAxis(timeAxis, 0));
			return builder.createDataFrame();
		}
	}

	// averages x,y for each timestep, for each layer
	// results in a 2D DataFrame
	private class TLXYAvg implements DataTransformer {

		/**
		 * Averages the data at each time step. For each time step, each
		 * layer's x,y domain data is averaged into a value for each layer.
		 *
		 * @param frame the data to transform
		 * @return the data averaged for each time step.
		 */
		public DataFrame transform(DataFrame frame) {
			Axes<DataFrameAxis> axes = frame.getAxes();
			DataFrameAxis timeAxis = axes.getTimeAxis();
			DataFrameAxis layerAxis = axes.getZAxis();
			DataFrameAxis xAxis = axes.getXAxis();
			DataFrameAxis yAxis = axes.getYAxis();
			int xExtent = xAxis.getExtent();
			int yExtent = yAxis.getExtent();
			int xySize = xExtent * yExtent;
			int layerExtent = layerAxis.getExtent();

			Array array = ArrayFactory.createDoubleArray(new int[]{timeAxis.getExtent(), layerAxis.getExtent()});
			Index index = array.getIndex();
			DataFrameIndex frameIndex = frame.getIndex();

			for (int time = 0; time < timeAxis.getExtent(); time++) {
				double avg = 0;

				for (int layer = 0; layer < layerExtent; layer++) {
					double xySum = 0;
					frameIndex.set(time, layer, 0, 0);

					for (int x = 0; x < xExtent; x++) {
						for (int y = 0; y < yExtent; y++) {
							frameIndex.setXY(x, y);
							xySum += frame.getDouble(frameIndex);
						}
					}
					avg = xySum / xySize;
					index.set(time, layer);
					array.setDouble(index, avg);
				}
			}

			DataFrameBuilder builder = new DataFrameBuilder();
			builder.addDataset(frame.getDataset()).setArray(array).setVariable(frame.getVariable());
			builder.addAxis(DataFrameAxis.createDataFrameAxis(timeAxis, 0));
			builder.addAxis(DataFrameAxis.createDataFrameAxis(layerAxis, 1));
			return builder.createDataFrame();
		}
	}


	// averages cell for each timestep, for each layer
	// results in a 2D DataFrame
	private class TLCellAvg implements DataTransformer {

		/**
		 * Averages the data at each time step. For each time step, each
		 * layer's x,y domain data is averaged into a value for each layer.
		 *
		 * @param frame the data to transform
		 * @return the data averaged for each time step.
		 */
		public DataFrame transform(DataFrame frame) {
			Axes<DataFrameAxis> axes = frame.getAxes();
			DataFrameAxis timeAxis = axes.getTimeAxis();
			DataFrameAxis layerAxis = axes.getZAxis();
			DataFrameAxis cellAxis = axes.getCellAxis();
			int cellExtent = cellAxis.getExtent();
			int layerExtent = layerAxis.getExtent();

			Array array = ArrayFactory.createDoubleArray(new int[]{timeAxis.getExtent(), layerAxis.getExtent()});
			Index index = array.getIndex();
			MPASDataFrameIndex frameIndex = (MPASDataFrameIndex)frame.getIndex();

			for (int time = 0; time < timeAxis.getExtent(); time++) {
				double avg = 0;

				for (int layer = 0; layer < layerExtent; layer++) {
					double cellSum = 0;
					frameIndex.set(time, layer, 0);

					for (int cell = 0; cell < cellExtent; cell++) {
						frameIndex.setCell(cell);
						cellSum += frame.getDouble(frameIndex);
					}
					avg = cellSum / cellExtent;
					index.set(time, layer);
					array.setDouble(index, avg);
				}
			}

			DataFrameBuilder builder = new DataFrameBuilder();
			builder.addDataset(frame.getDataset()).setArray(array).setVariable(frame.getVariable());
			builder.addAxis(DataFrameAxis.createDataFrameAxis(timeAxis, 0));
			builder.addAxis(DataFrameAxis.createDataFrameAxis(layerAxis, 1));
			return builder.createDataFrame();
		}
	}


	// averages cell for each timestep
	// results in a 2D DataFrame
	private class TCellAvg implements DataTransformer {

		/**
		 * Averages the data at each time step. For each time step, each
		 * layer's x,y domain data is averaged into a value for each layer.
		 *
		 * @param frame the data to transform
		 * @return the data averaged for each time step.
		 */
		public DataFrame transform(DataFrame frame) {
			Axes<DataFrameAxis> axes = frame.getAxes();
			DataFrameAxis timeAxis = axes.getTimeAxis();
			DataFrameAxis cellAxis = axes.getCellAxis();
			int cellExtent = cellAxis.getExtent();

			Array array = ArrayFactory.createDoubleArray(new int[]{timeAxis.getExtent()});
			Index index = array.getIndex();
			MPASDataFrameIndex frameIndex = (MPASDataFrameIndex)frame.getIndex();

			for (int time = 0; time < timeAxis.getExtent(); time++) {
				double avg = 0;

				double cellSum = 0;
				frameIndex.set(time, -1, 0);

				for (int cell = 0; cell < cellExtent; cell++) {
					frameIndex.setCell(cell);
					cellSum += frame.getDouble(frameIndex);
				}
				avg = cellSum / cellExtent;
				index.set(time);
				array.setDouble(index, avg);
			}

			DataFrameBuilder builder = new DataFrameBuilder();
			builder.addDataset(frame.getDataset()).setArray(array).setVariable(frame.getVariable());
			builder.addAxis(DataFrameAxis.createDataFrameAxis(timeAxis, 0));
			return builder.createDataFrame();
		}
	}


	/**
	 * Averages the data at each time step. For each time step, each
	 * layer's x,y domain data is averaged into a value for each layer.
	 *
	 * @param frame the data to transform
	 * @return the data averaged for each time step.
	 */
	public DataFrame transform(DataFrame frame) {
		DataTransformer transformer;
		if (frame.getDataset().get(0) instanceof IMPASDataset)
			frame = ((IMPASDataset)frame.getDataset().get(0)).augmentFrame(frame);
		boolean hasLayer = frame.getAxes().getZAxis() != null;
		if (frame instanceof MPASPlotDataFrame) {
			if (hasLayer)
				transformer = new TLCellAvg();
			else
				transformer = new TCellAvg();
		}
		else {
			if (hasLayer)
				transformer = new TLXYAvg();
			else
				transformer = new TXYAvg();
		}
		return transformer.transform(frame);
	}
	
	//Average a list of SINGLE cell data frames
	public DataFrame transform(List<DataFrame> frames) {
		DataFrame frame = frames.get(0);
		Axes<DataFrameAxis> axes = frame.getAxes();
		DataFrameAxis timeAxis = axes.getTimeAxis();
		DataFrameAxis layerAxis = axes.getZAxis();
		int cellExtent = frames.size();
		int layerExtent = 1;
		if (layerAxis != null)
			layerExtent = layerAxis.getExtent();

		Array array = null;
		if (layerAxis != null)
			array = ArrayFactory.createDoubleArray(new int[]{timeAxis.getExtent(), layerAxis.getExtent()});
		else
			array = ArrayFactory.createDoubleArray(new int[]{timeAxis.getExtent()});
		Index index = array.getIndex();
		MPASDataFrameIndex frameIndex = null;
		List<MPASDataFrameIndex> frameIndices = new ArrayList<MPASDataFrameIndex>();
		for (int i = 0; i < frames.size(); ++i)
			frameIndices.add((MPASDataFrameIndex)frames.get(i).getIndex());

		for (int time = 0; time < timeAxis.getExtent(); time++) {
			double avg = 0;

			for (int layer = 0; layer < layerExtent; layer++) {
				double cellSum = 0;

				for (int cell = 0; cell < cellExtent; cell++) {
					frameIndex = frameIndices.get(cell);
					if (layerAxis != null)
						frameIndex.set(time, layer, 0);
					else {
						frameIndex.setTime(time);
						frameIndex.setCell(0);
					}
					
					DataFrame cellFrame = frames.get(cell);
					double cellVal = cellFrame.getDouble(frameIndex);
					cellSum += cellVal;
				}
				avg = cellSum / cellExtent;
				if (layerAxis != null)
					index.set(time, layer);
				else
					index.set(time);
				array.setDouble(index, avg);
			}
		}

		DataFrameBuilder builder = new DataFrameBuilder();
		builder.addDataset(frame.getDataset()).setArray(array).setVariable(frame.getVariable());
		builder.addAxis(DataFrameAxis.createDataFrameAxis(timeAxis, 0));
		if (layerAxis != null)
			builder.addAxis(DataFrameAxis.createDataFrameAxis(layerAxis, 1));
		return builder.createDataFrame();
	}

}
