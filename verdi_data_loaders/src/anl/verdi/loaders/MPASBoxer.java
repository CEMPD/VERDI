/**
 * Bounding boxer that creates simple bounding box for CVS data.
 *
 * @author Tony Howard
 * @version $Revision$ $Date$
 */

package anl.verdi.loaders;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
//import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.cs.DefaultEllipsoidalCS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
//import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.referencing.operation.DefiningConversion;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.MathTransformFactory;

import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPointImpl;
import ucar.unidata.geoloc.projection.LatLonProjection;
import anl.verdi.data.BoundingBoxer;

public class MPASBoxer implements BoundingBoxer {
	
	static CoordinateReferenceSystem PLACEHOLDER_CRS = null;
	static {
		try {
			String strCRS = new LatlonWKTCreator().createWKT(new LatLonProjection());
			PLACEHOLDER_CRS = CRS.parseWKT(strCRS);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	static final Logger Logger = LogManager.getLogger(MPASBoxer.class.getName());

	MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
	ReferencingFactoryContainer factories = new ReferencingFactoryContainer(null);
	
	Projection proj = null; //new LambertConformal();
	boolean isLatLon = true;

	public MPASBoxer() {

	}

	public Point2D axisPointToLatLonPoint(int x, int y) {
		LatLonPointImpl result = new LatLonPointImpl();
		ProjectionPointImpl world = new ProjectionPointImpl((double)x, (double)y);
		proj.projToLatLon(world, result);
		return new Point2D.Double(result.getLatitude(), (double)result.getLongitude());
	}
	
	public Point2D axisPointToLatLonPoint(double x, double y) {
		LatLonPointImpl result = new LatLonPointImpl();
		ProjectionPointImpl world = new ProjectionPointImpl(x, y);
		proj.projToLatLon(world, result);
		return new Point2D.Double(result.getLatitude(), (double)result.getLongitude());
	}

	public Point2D CRSPointToAxis(double x, double y) {		
		return new Point2D.Double(x, y);
	}

	public Point2D latLonToAxisPoint(double x, double y) {
		LatLonPointImpl latLon = new LatLonPointImpl(x, y);
		ProjectionPointImpl result = new ProjectionPointImpl();
		proj.latLonToProj(latLon, result);
		return new Point2D.Double(result.getX(), result.getY());
	}
	


	
	

	/* (non-Javadoc)
	 * @see anl.verdi.data.BoundingBoxer#getProjection()
	 * returns null
	 */
	@Override
	public Projection getProjection() {
		return proj; //Try new LatLonProjection here
	}

	
	/**
	 * Creates a bounding box from the specified ranges. The
	 * xRange and yRange are specified in terms of x / y cell
	 * coordinates.
	 *
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 *@return the created bounding box.
	 */
	public ReferencedEnvelope createBoundingBox(double xMin, double xMax,
			double yMin, double yMax, int netcdfConv) {
		//CoordinateAxis1D xaxis = getXAxis();
		//CoordinateAxis1D yaxis = getYAxis();
		
		double xStart, xEnd, yStart, yEnd; //

		// latlon coord does not need to be scaled
		/*double scaler = isLatLon ? 1.0 : 1000.0;
		Logger.info("scaler = " + scaler);
		double xInc = xaxis.getIncrement() * scaler;
		Logger.info("xInc = " + xInc);
		double yInc = yaxis.getIncrement() * scaler;
		Logger.info("yInc = " + yInc);
		
		int limit = xaxis.getCoordValues().length - 1;
		Logger.info("limit = " + limit);
		double start = xaxis.getStart() * scaler;
		Logger.info("start = " + start);
		double end = (xaxis.getCoordValue(limit) + xaxis.getIncrement()) * scaler;
		Logger.info("end = " + end);
		
		xStart = start + xMin * xInc;
		Logger.info("xStart = start + xMin * xInc = " + xStart );
		xEnd = end - ((limit - xMax) * xInc);
		Logger.info("xEnd = end - ((limit - xMax) * xInc) = " + xEnd);

		if ( netcdfConv == VerdiConstants.NETCDF_CONV_ARW_WRF) { // JIZHEN-SHIFT
			Logger.info("in recompute code section");
			xStart = xStart - xaxis.getIncrement() * scaler * 0.5;
			Logger.info("xStart now set to:xStart - xaxis.getIncrement() * scaler * 0.5 = " + xStart );
			//xEnd = xEnd - xaxis.getIncrement() * scaler * ( 0.5 + 1);
			xEnd = xEnd - xaxis.getIncrement() * scaler * 0.5;
			Logger.info("xEnd now set to:  xEnd - xaxis.getIncrement() * scaler * 0.5 = " + xEnd);
		}

		limit = yaxis.getCoordValues().length - 1;
		Logger.info("limit = yaxis.getCoordValues().length - 1 = " + limit);
		start = yaxis.getStart() * scaler;
		Logger.info("start = yaxis.getStart() * scaler = " + start );
		end = (yaxis.getCoordValue(limit) + yaxis.getIncrement()) * scaler;
		Logger.info("end = (yaxis.getCoordValue(limit) + yaxis.getIncrement()) * scaler = " + end);

		yStart = start + yMin * yInc;
		Logger.info("yStart =start + yMin * yInc = " + yStart );
		yEnd = end - ((limit - yMax) * yInc);
		Logger.info("yEnd = end - ((limit - yMax) * yInc) = " + yEnd);
		if ( netcdfConv == VerdiConstants.NETCDF_CONV_ARW_WRF) { // JIZHEN-SHIFT
			Logger.info("within if block:");
			yStart = yStart - yaxis.getIncrement() * scaler * 0.5; // bottom_left
			Logger.info("yStart = yStart - yaxis.getIncrement() * scaler * 0.5 = " + yStart);
			//yEnd = yEnd - yaxis.getIncrement() * scaler * ( 0.5 + 1);
			yEnd = yEnd - yaxis.getIncrement() * scaler * 0.5; // top_right
			Logger.info("yEnd = yEnd - yaxis.getIncrement() * scaler * 0.5 = " + yEnd);
		}
//		Hints hints = new Hints(Hints.COMPARISON_TOLERANCE, 1E-9);	// TODO: 2014  probably need to do in beginning of VERDI
		Hints.putSystemDefault(Hints.COMPARISON_TOLERANCE, 10e-9);*/

		//return new ReferencedEnvelope(xStart, xEnd, yStart, yEnd, crs);
		return new ReferencedEnvelope(xMin, xMax, yMin, yMax, PLACEHOLDER_CRS);

	}
	
	public CoordinateReferenceSystem getCRS() {
		//Logger.debug("in getCRS(): returning crs = " + crs);
		return PLACEHOLDER_CRS;
	}

	public CoordinateReferenceSystem getOriginalCRS() {
		return PLACEHOLDER_CRS;
	}

}
