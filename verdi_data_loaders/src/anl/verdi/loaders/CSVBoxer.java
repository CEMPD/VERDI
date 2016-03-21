package anl.verdi.loaders;

import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.geotools.geometry.jts.ReferencedEnvelope;
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
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.GeodeticDatum;
import org.opengis.referencing.operation.MathTransformFactory;

import ucar.unidata.geoloc.Projection;
import anl.verdi.data.BoundingBoxer;

/**
 * Bounding boxer that creates simple bounding box for CVS data.
 *
 * @author Nick Collier
 * @author Eric Tatara
 * @version $Revision$ $Date$
 */
public class CSVBoxer implements BoundingBoxer {

//	MathTransformFactory mtFactory = FactoryFinder.getMathTransformFactory(null);
	MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
//	FactoryGroup factories = new FactoryGroup(null);
	ReferencingFactoryContainer factories = new ReferencingFactoryContainer(null);

	private CoordinateReferenceSystem crs;

	public CSVBoxer() {

	}

	public Point2D axisPointToLatLonPoint(int x, int y) {
			return new Point2D.Double(x, y);
	}

	public Point2D CRSPointToAxis(double x, double y) {		
		return new Point2D.Double(x, y);
	}

	public Point2D latLonToAxisPoint(double x, double y) {
		return new Point2D.Double(x, y);
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
	public ReferencedEnvelope createBoundingBox(double xMin, double xMax, double yMin, double yMax, int netcdfConv) {
		ReferencedEnvelope env = null;
		crs = getLambert();  // Make a default Lamert CRS
		env = new ReferencedEnvelope(xMin, xMax, yMin, yMax, crs);
		
		return env;
	}
	
	

	/* (non-Javadoc)
	 * @see anl.verdi.data.BoundingBoxer#getProjection()
	 * returns null
	 */
	@Override
	public Projection getProjection() {
		return null;
	}
	
	public CoordinateReferenceSystem getCRS() {
		return crs;
	}

	private CoordinateReferenceSystem getLambert() {
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", "North_American_Datum_1983");
//			GeodeticDatum datum = FactoryFinder.getDatumFactory(null).createGeodeticDatum(params,
//							DefaultEllipsoid.SPHERE, DefaultPrimeMeridian.GREENWICH);
			GeodeticDatum datum = ReferencingFactoryFinder.getDatumFactory(null).createGeodeticDatum(params,
					DefaultEllipsoid.SPHERE, DefaultPrimeMeridian.GREENWICH);
			params = new HashMap<String, Object>();
			params.put("name", "NAD83");

//			GeographicCRS crs = FactoryFinder.getCRSFactory(null).createGeographicCRS(params, datum,
//							DefaultEllipsoidalCS.GEODETIC_2D);
			GeographicCRS crs = ReferencingFactoryFinder.getCRSFactory(null).createGeographicCRS(params, datum,
					DefaultEllipsoidalCS.GEODETIC_2D);
			ParameterValueGroup parameters = mtFactory.getDefaultParameters("Lambert_Conformal_Conic_2SP");
			parameters.parameter("standard_parallel_1").setValue(30);
			parameters.parameter("standard_parallel_2").setValue(60);
			parameters.parameter("latitude_of_origin").setValue(40);
			parameters.parameter("longitude_of_origin").setValue(-90);
			parameters.parameter("central_meridian").setValue(-90);
			parameters.parameter("false_easting").setValue(0);
			parameters.parameter("false_northing").setValue(0);
			Map<String, String> properties = Collections.singletonMap("name", "unknown");	// changed Map to Map<String, String>
//			return factories.createProjectedCRS(properties, crs, null, parameters, DefaultCartesianCS.GENERIC_2D);
			CRSFactory crsFactory = factories.getCRSFactory();
			DefiningConversion conv = new DefiningConversion("sample", parameters);
			return crsFactory.createProjectedCRS(properties, crs, conv, DefaultCartesianCS.GENERIC_2D);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to create coordinate reference system", e);
		}
	}
}
