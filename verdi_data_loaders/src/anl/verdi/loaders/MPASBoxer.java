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

import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPointImpl;
import ucar.unidata.geoloc.projection.LambertConformal;
import ucar.unidata.geoloc.projection.LatLonProjection;
import ucar.unidata.geoloc.projection.Mercator;
import ucar.unidata.geoloc.projection.Stereographic;
import ucar.unidata.geoloc.projection.UtmProjection;
import anl.verdi.core.VerdiConstants;
import anl.verdi.data.BoundingBoxer;

public class MPASBoxer implements BoundingBoxer {

	static final Logger Logger = LogManager.getLogger(MPASBoxer.class.getName());

	MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
	ReferencingFactoryContainer factories = new ReferencingFactoryContainer(null);

	private CoordinateReferenceSystem crs;
	
	//TODO - use a real projection here
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
		return proj;
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

		Projection proj = getProjection();

		if (proj instanceof LambertConformal) {
			Logger.debug("proj = " + proj.toString() + '\n' + "  projection is of type LambertConformal");
			if (crs == null) {
				Logger.info("NOTE: crs is null");
				try {
					Logger.info("within try/catch block");
					String strCRS = new LambertWKTCreator().createWKT((LambertConformal) proj);
					Logger.info("created strCRS = " + strCRS);
					Logger.info("Ready to call CRS.parseWKT for LambertConformal");
					crs = CRS.parseWKT(strCRS);	// NOTE: preferred method (docs.geotools.org/stable/userguide/library/referencing/crs.html)
					Logger.info("parsed CRS: " + crs.toString());
					Logger.info("done printing crs");
				} catch (IOException ex) {
					Logger.info("into exception handling");
					Logger.error("Error while creating CRS for LambertConformal");
					ex.printStackTrace();
				} catch (FactoryException e) {
					Logger.error("Caught FactoryException while creating CRS for LambertConformal");
					e.printStackTrace();
				}
			}
		} else if (proj instanceof UtmProjection) {
			Logger.debug("projection is of type UtmProjection");
			if (crs == null) {
				Logger.info("NOTE: crs is null");
				try {
					Logger.info("within try/catch block");
					String strCRS = new UtmWKTCreator().createWKT((UtmProjection) proj);
					Logger.info("created strCRS = " + strCRS.toString());
					Logger.info("Ready to call CRS.parseWKT for UTM Projection");
					crs = CRS.parseWKT(strCRS);
				} catch (Exception ex) {
					Logger.error("Error while creating CRS for UTM " + ex.getMessage());
				}
			}
		} else if (proj instanceof Stereographic) {
			Logger.debug("projection is of type Stereographic");
			if (crs == null) {
				Logger.info("NOTE: crs is null");
				try {
					Logger.info("within try/catch block");
					String strCRS = new PolarStereographicWKTCreator().createWKT((Stereographic) proj);
					Logger.info("created strCRS = " + strCRS.toString());	// FAILURE CAUSE: PARAMETER["scale_factor", -98.0],
					Logger.info("Ready to call CRS.parseWKT for Stereographic Projection");
					crs = CRS.parseWKT(strCRS);		// FAILURE POINT
					Logger.info("parsed CRS: " + crs.toString());
				} catch (Exception ex) {
					Logger.error("Error while creating CRS for Stereographic " + ex.getMessage());
				}
			}
		} else if (isLatLon) {
			if (crs == null) {
				try {
					String strCRS = new LatlonWKTCreator().createWKT((LatLonProjection)proj);
					crs = CRS.parseWKT(strCRS);
				} catch (Exception ex) {
					Logger.error("Error while creating CRS for Lat-Lon " + ex.getMessage());
				}
			}
		} else if (proj instanceof Mercator) {
			if (crs == null) {
				try {
					String strCRS = new MercatorWKTCreator().createWKT((Mercator)proj);
					crs = CRS.parseWKT(strCRS);
				} catch (Exception e) {
					Logger.error("Error while creating CRS for Mercator " + e.getMessage());
				}
			}
		}

		// TODO: add more projections here
		else {
			Logger.error("Projection is not recognized!!");
		} 

		//return new ReferencedEnvelope(xStart, xEnd, yStart, yEnd, crs);
		return new ReferencedEnvelope(xMin, xMax, yMin, yMax, crs);

	}
	
	public CoordinateReferenceSystem getCRS() {
		Logger.debug("in getCRS(): returning crs = " + crs);
		return crs;
	}

}
