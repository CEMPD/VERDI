package anl.verdi.data;

import java.awt.geom.Point2D;

import org.geotools.geometry.jts.ReferencedEnvelope;

import ucar.unidata.geoloc.Projection;

/**
 * Interface for class that creates a referenced envelope bounding box.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface BoundingBoxer {

	/**
	 * Creates a bounding box from the specified ranges. The xRange and yRange
	 * are specified in terms of x / y cell coordinates.
	 * 
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax @return the created bounding box.
	 * @param netcdfConv TODO
	 */
	ReferencedEnvelope createBoundingBox(double xMin, double xMax, double yMin, double yMax, int netcdfConv);
	
	/**
	 * Gets the Projection associated with this BoundingBoxer.
	 * 
	 * @return the Projection associated with this BoundingBoxer.
	 */
	public Projection getProjection();

	/**
	 * Converts the grid cell coordinate to a lat / lon coordinate.
	 * @param x
	 *            the x value
	 * @param y
	 *            the y value
	 * @return the lat / lon coordinate.
	 */
	Point2D axisPointToLatLonPoint(int x, int y);

	/**
	 * Converts the CRS coordinate to a grid Cell coordinate
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	Point2D CRSPointToAxis(double x, double y);

	Point2D latLonToAxisPoint(double lat, double lon);
}
