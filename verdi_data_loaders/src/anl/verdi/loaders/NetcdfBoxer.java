package anl.verdi.loaders;

import java.awt.geom.Point2D;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
//import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.ReferencingFactoryFinder;
//import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransformFactory;




//import simphony.util.messages.MessageCenter;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
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

/**
 * Bounding boxer that uses netcdf to create the bounding box.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class NetcdfBoxer implements BoundingBoxer {
	static final Logger Logger = LogManager.getLogger(NetcdfBoxer.class.getName());

//	private static MessageCenter msg = MessageCenter.getMessageCenter(BoundingBoxer.class);

//	MathTransformFactory mtFactory = FactoryFinder.getMathTransformFactory(null);
	MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);

//	FactoryGroup factories = new FactoryGroup(null);
	ReferencingFactoryContainer factories = new ReferencingFactoryContainer(null);

	private GridDatatype grid;
	private CoordinateReferenceSystem crs;
	protected boolean isLatLon;
	
	protected NetcdfBoxer() {	
	}

	public NetcdfBoxer(GridDatatype grid) {
		Logger.debug("in constructor for NetcdfBoxer for a GridDatatype");
		this.grid = grid;
		this.isLatLon = grid.getCoordinateSystem().isLatLon();
	}
	
	
	/**
	 * Gets the Projection associated with this BoundingBoxer.
	 * 
	 * @return the Projection associated with this BoundingBoxer.
	 */
	public Projection getProjection() {
		Logger.debug("in NetcdfBoxer.getProjection = " + grid.getCoordinateSystem().getProjection().getName());
		return grid.getCoordinateSystem().getProjection();
	}

	/**
	 * Converts the grid cell coordinate to a lat / lon coordinate.
	 * 
	 * @param x
	 *            the x value
	 * @param y
	 *            the y value
	 * @return the lat / lon coordinate.
	 */
	public Point2D axisPointToLatLonPoint(int x, int y) {
		Logger.info("in NetcdfBoxer.axisPointToLatLonPoint for x = " + x + ", y = " + y);
		CoordinateAxis1D xaxis = getXAxis();
		CoordinateAxis1D yaxis = getYAxis();
		// coordVal is the sw corner, so coordEdge + 1 should be the center
		// of that cell.
		double xVal = xaxis.getCoordEdge(x + 1);
		double yVal = yaxis.getCoordEdge(y + 1);
		if (isLatLon) {
			return new Point2D.Double(xVal, yVal);
		} else {
			Projection proj = getProjection();
			LatLonPointImpl latLon = new LatLonPointImpl();
			proj.projToLatLon(new ProjectionPointImpl(xVal, yVal), latLon);
			return new Point2D.Double(latLon.getLongitude(), latLon
					.getLatitude());
		}
	}

	/**
	 * @param lat
	 *            latitude
	 * @param lon
	 *            longitude
	 * @return the location on the x and y axis if the latLon is with the grid,
	 *         otherwise (-1, -1).
	 */
	public Point2D latLonToAxisPoint(double lat, double lon) {
		Logger.info("in NetcdfBoxer.latLonToAxisPoint for lat = " + lat + ", lon = " + lon);
		Projection proj = getProjection();
		ProjectionPointImpl point = new ProjectionPointImpl();
		proj.latLonToProj(new LatLonPointImpl(lat, lon), point);

		CoordinateAxis1D xaxis = getXAxis();
		CoordinateAxis1D yaxis = getYAxis();
		double x = xaxis.findCoordElement(point.getX());		//.x);
		double y = yaxis.findCoordElement(point.getY());		//	.y);
		if (x != -1 && y != -1) {
			double leftEdge = xaxis.getCoordValue((int) x);
			double temp = (float) point.getX();		//.x; //NOTE: leftEdge is actually a float value
			if (leftEdge == 0.0) temp = (float) Math.round(temp); //NOTE: rounding gets pretty silly here
			
			if (leftEdge > temp) {
				x -= 1;
			}

			double bottomEdge = yaxis.getCoordValue((int) y);
			temp = (float) point.getY();		//.y; //NOTE: bottomEdge is actually a float value
			if (bottomEdge == 0.0) temp = (float) Math.round(temp);
			
			if (bottomEdge > temp) {
				y -= 1;
			}
		}
		
		return new Point2D.Double(x, y);
	}

	public Point2D CRSPointToAxis(double x, double y) {
		Logger.info("in NetcdfBoxer.CRSPointToAxis for x = " + x + ", y =" + y);
		CoordinateAxis1D xaxis = getXAxis();
		CoordinateAxis1D yaxis = getYAxis();
		x = x / 1000;
		y = y / 1000;
		x -= (xaxis.getIncrement() / 2);
		y -= (yaxis.getIncrement() / 2);
		int xCell = xaxis.findCoordElement(x);
		int yCell = yaxis.findCoordElement(y);
		if (xCell == -1) {
			double bottomX = xaxis.getCoordValue(0);
			if (x < bottomX) {
				xCell = 0;
			} else {
				xCell = xaxis.getCoordValues().length - 1;
			}
		}
		if (yCell != -1) {
			yCell = (yaxis.getCoordValues().length - 1) - yCell;
		} else {
			double bottomY = yaxis.getCoordValue(0);
			if (y > bottomY) {
				yCell = 0;
			} else {
				yCell = yaxis.getCoordValues().length - 1;
			}
		}
		return new Point2D.Double(xCell, yCell);
	}

	/**
	 * Creates a bounding box from the specified ranges. The xRange and yRange
	 * are specified in terms of x / y cell coordinates.
	 * 
	 * @param xMin
	 * @param xMax
	 * @param yMin
	 * @param yMax
	 * @return the created bounding box.
	 */
	public ReferencedEnvelope createBoundingBox(double xMin, double xMax,
			double yMin, double yMax, int netcdfConv) {
		CoordinateAxis1D xaxis = getXAxis();
		CoordinateAxis1D yaxis = getYAxis();
		
		double xStart, xEnd, yStart, yEnd; //

		// latlon coord does not need to be scaled
		double scaler = isLatLon ? 1.0 : 1000.0;
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
		Hints.putSystemDefault(Hints.COMPARISON_TOLERANCE, 10e-9);

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
		return new ReferencedEnvelope(xStart, xEnd, yStart, yEnd, crs);
	}

	protected CoordinateAxis1D getXAxis() {
		GridCoordSystem gcs = grid.getCoordinateSystem();
		return (CoordinateAxis1D) gcs.getXHorizAxis();
	}

	protected CoordinateAxis1D getYAxis() {
		GridCoordSystem gcs = grid.getCoordinateSystem();
		return (CoordinateAxis1D) gcs.getYHorizAxis();
	}
}
