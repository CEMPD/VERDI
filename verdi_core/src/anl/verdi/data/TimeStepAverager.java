package anl.verdi.data;

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


	/**
	 * Averages the data at each time step. For each time step, each
	 * layer's x,y domain data is averaged into a value for each layer.
	 *
	 * @param frame the data to transform
	 * @return the data averaged for each time step.
	 */
	public DataFrame transform(DataFrame frame) {
		if (frame.getAxes().getZAxis() == null) {
			return new TXYAvg().transform(frame);
		} else {
			return new TLXYAvg().transform(frame);
		}
	}
}
