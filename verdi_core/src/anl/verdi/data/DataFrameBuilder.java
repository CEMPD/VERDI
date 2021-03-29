package anl.verdi.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ucar.ma2.Array;
import ucar.unidata.geoloc.Projection;

/**
 * Builds a DataFrame from the pieces added in the add methods.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DataFrameBuilder {

	// the DataFrame implementation
	private static class BuilderDataFrame extends AbstractDataFrame {

		BuilderDataFrame() {
			datasets = new ArrayList<Dataset>();
		}

		void setArray(Array array) {
			this.array = array;
		}

		void setAxes(Axes<DataFrameAxis> axes) {
			this.axes = axes;
		}

		void addDataset(Dataset set) {
			datasets.add(set);
		}

		void setVariable(Variable var) {
			this.variable = var;
		}

		public int[] getShape() {
			return array.getShape();
		}
	}

	// used to build the axes object and sort out
	// the different axes
	protected static class AxesBuilder {

		List<DataFrameAxis> others = new ArrayList<DataFrameAxis>();

		DataFrameAxis xAxis, yAxis, timeAxis, layerAxis;

		Axes<DataFrameAxis> buildAxes(BoundingBoxer boundingBoxer) {
			if (boundingBoxer == null) {
				boundingBoxer = new BoundingBoxer() {

					public Point2D CRSPointToAxis(double x, double y) {
						return null;
					}
					
					public Projection getProjection() {
						return null;
					}

					public ReferencedEnvelope createBoundingBox(double xMin, double xMax, double yMin, double yMax, int netcdfConv) {
						return null;
					}

					public Point2D axisPointToLatLonPoint(int x, int y) {
						return null;
					}
					
					public Point2D axisPointToLatLonPoint(double x, double y) {
						return null;
					}

					public Point2D latLonToAxisPoint(double lat, double lon) {
						return null;  //todo implement method
					}
					
					public CoordinateReferenceSystem getCRS() {
						return null;
					}
					
					public CoordinateReferenceSystem getOriginalCRS() {
						return null;
					}
				};
			}
			if (xAxis != null)
				others.add(xAxis);
			if (yAxis != null)
				others.add(yAxis);
			if (timeAxis != null)
				others.add(timeAxis);
			if (layerAxis != null)
				others.add(layerAxis);
			return new Axes<DataFrameAxis>(others, boundingBoxer);
		}

		void addAxis(DataFrameAxis axis) {
			if (axis.getAxisType() == AxisType.X_AXIS) {
				this.xAxis = axis;
			} else if (axis.getAxisType() == AxisType.Y_AXIS) {
				this.yAxis = axis;
			} else if (axis.getAxisType() == AxisType.TIME) {
				this.timeAxis = axis;
			} else if (axis.getAxisType() == AxisType.LAYER) {
				this.layerAxis = axis;
			} else {
				others.add(axis);
			}
		}
	}

	protected BuilderDataFrame frame = new BuilderDataFrame();

	private AxesBuilder axesBuilder = new AxesBuilder();
	
	protected AxesBuilder getAxesBuilder() {
		return axesBuilder;
	}

	private Set<Dataset> sets = new HashSet<Dataset>();

	/**
	 * Adds a Dataset to the frame that this builder is creating.
	 * 
	 * @param set
	 *            the DataSet to add.
	 * @return this DataFrameBuilder
	 */
	public DataFrameBuilder addDataset(Dataset set) {
		frame.addDataset(set);
		return this;
	}

	/**
	 * Adds the specified Datasets to the frame that this builder is creating.
	 * 
	 * @param sets
	 *            the DataSets to add.
	 * @return this DataFrameBuilder
	 */
	public DataFrameBuilder addDataset(Collection<Dataset> sets) {
		this.sets.addAll(sets);
		return this;
	}

	/**
	 * Sets the Array contained by the DataFrame that this builder will create.
	 * 
	 * @param array
	 *            the array for the DataFrame
	 * @return this DataFrameBuilder
	 */
	public DataFrameBuilder setArray(Array array) {
		frame.setArray(array);
		return this;
	}

	/**
	 * Sets the Variable contained by the DataFrame that this builder will
	 * create.
	 * 
	 * @param var
	 *            the Variable for the DataFrame
	 * @return this DataFrameBuilder
	 */
	public DataFrameBuilder setVariable(Variable var) {
		frame.setVariable(var);
		return this;
	}

	/**
	 * Adds a DataFrameAxis to the frame that this builder is creating.
	 * 
	 * @param axis
	 *            the axis to add.
	 * @return this DataFrameBuilder
	 */
	public DataFrameBuilder addAxis(DataFrameAxis axis) {
		getAxesBuilder().addAxis(axis);
		return this;
	}

	/**
	 * Creates a DataFrame from the Datasets, Array and so forth that have been
	 * set or added to this DataFrameBuilder.
	 * 
	 * @return the created DataFrame.
	 */
	public DataFrame createDataFrame() {
		for (Dataset set : sets) {
			frame.addDataset(set);
		}

		BoundingBoxer env = null;
		if (sets.size() > 0) {
			// we can do this from the first one
			// because if we are creating a frame with mult datasets
			// they all must have the same bounding box.
			env = sets.iterator().next().getCoordAxes().getBoundingBoxer();
		}
		frame.setAxes(getAxesBuilder().buildAxes(env));
		return frame;
	}

	/**
	 * Resets this DataFrameBuilder clearing any previously added Datasets and
	 * so forth.
	 */
	public void reset() {
		frame = new BuilderDataFrame();
		axesBuilder = new AxesBuilder();
		sets.clear();
	}
}
