/**
 * Builds a DataFrame from the pieces added in the add methods.
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.data;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import ucar.unidata.geoloc.Projection;

public class MPASDataFrameBuilder extends DataFrameBuilder {


	// used to build the axes object and sort out
	// the different axes
	private static class MPASAxesBuilder extends DataFrameBuilder.AxesBuilder {

		List<DataFrameAxis> others = new ArrayList<DataFrameAxis>();

		DataFrameAxis timeAxis, nCellsAxis, layerAxis;

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
			if (nCellsAxis != null)
				others.add(nCellsAxis);
			if (timeAxis != null)
				others.add(timeAxis);
			if (layerAxis != null)
				others.add(layerAxis);
			return new Axes<DataFrameAxis>(others, boundingBoxer);
		}

		void addAxis(DataFrameAxis axis) {
			if (axis.getAxisType() == AxisType.CELL_AXIS) {
				this.nCellsAxis = axis;
			} else if (axis.getAxisType() == AxisType.TIME) {
				this.timeAxis = axis;
			} else if (axis.getAxisType() == AxisType.LAYER) {
				this.layerAxis = axis;
			} else {
				others.add(axis);
			}
		}
	}

	private MPASAxesBuilder mpasAxesBuilder = new MPASAxesBuilder();

	protected AxesBuilder getAxesBuilder() {
		return mpasAxesBuilder;
	}

}
