// NOTE: This class uses Geotools and OpenGis for geometry, Coordinate Reference System (crs), and Reference Envelope
// BUT uses ucar for projections.
package anl.verdi.loaders;

import java.awt.geom.Point2D;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
//import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.ReferencingFactoryFinder;
//import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransformFactory;

import com.vividsolutions.jts.geom.Coordinate;

import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.dataset.VariableEnhanced;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionImpl;
import ucar.unidata.geoloc.ProjectionPointImpl;
import ucar.unidata.geoloc.projection.LambertConformal;
import ucar.unidata.geoloc.projection.LatLonProjection;
import ucar.unidata.geoloc.projection.Mercator;
import ucar.unidata.geoloc.projection.Stereographic;
import ucar.unidata.geoloc.projection.UtmProjection;
import anl.verdi.core.VerdiConstants;
import anl.verdi.data.BoundingBoxer;
import anl.verdi.plot.gui.VerdiShapefileUtil;
import gov.epa.emvl.Mapper;

/**
 * Bounding boxer that uses netcdf to create the bounding box.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class NetcdfBoxer implements BoundingBoxer {
	static final Logger Logger = LogManager.getLogger(NetcdfBoxer.class.getName());

//	MathTransformFactory mtFactory = FactoryFinder.getMathTransformFactory(null);
	MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);

//	FactoryGroup factories = new FactoryGroup(null);
	ReferencingFactoryContainer factories = new ReferencingFactoryContainer(null);

	private GridDatatype grid;
	private VariableEnhanced var;
	
	/** 
	 * This is now a placeholder only, and is never used - any valid Geotools projection
	 * could be created here.  Instead, VERDI's VerdiShapefileUtil converts all shapefiles
	 * to lat/lon (if not already lat/lon, then uses the NetCDF projection to project the
	 * lat/lon shapefiles into new ones that are actually used.  All instances of the 
	 * Geotools CRS are set to the CRS created here, and Geotools won't try to do any 
	 * further projections on the shapefiles since it thinks all CRSs are the same.  This 
	 * allows us to use the Geotools framework to render shapefiles, while ensuring that 
	 * they're projected properly to match the underlying NetCDF data.  We don't have to 
	 * worry about making sure Geotools supports the NetCDF projection, or that the Geotools 
	 * projection parameters are set to match the NetCDF ones.
	 */
	/*public static CoordinateReferenceSystem PLACEHOLDER_CRS = null;
	static {
		try {
			PLACEHOLDER_CRS = CRS.decode("EPSG:4326");
		} catch (Exception e) {
			Logger.error("Could not create placeholder crs", e);
		}
	}*/
	
	CoordinateReferenceSystem origCRS = null;
	protected boolean isLatLon;
	Projection proj;
	CoordinateAxis1D xAxis = null;
	CoordinateAxis1D yAxis = null;
	
	protected NetcdfBoxer() {
	}
	
	public NetcdfBoxer(NetcdfDataset ds, Projection proj,CoordinateAxis1D x, CoordinateAxis1D y) {
		this.proj = proj;
	    int projType = ds.findGlobalAttribute("MAP_PROJ").getNumericValue().intValue();
	    isLatLon = projType == 6;
	    xAxis = x;
	    yAxis = y;
	}
	
	public NetcdfBoxer(VariableEnhanced var, GridCoordSystem system) {
		this.isLatLon = system.isLatLon();
		proj = system.getProjection();
		xAxis = (CoordinateAxis1D) system.getXHorizAxis();
		yAxis = (CoordinateAxis1D) system.getYHorizAxis();
	}

	public NetcdfBoxer(GridDatatype grid) {
		Logger.debug("in constructor for NetcdfBoxer for a GridDatatype");
		this.grid = grid;
		GridCoordSystem system = grid.getCoordinateSystem();
		this.isLatLon = system.isLatLon();
		proj = system.getProjection();
		xAxis = (CoordinateAxis1D) system.getXHorizAxis();
		yAxis = (CoordinateAxis1D) system.getYHorizAxis();
	}
	
	/**
	 * Gets the Projection associated with this BoundingBoxer.
	 * 
	 * @return the Projection associated with this BoundingBoxer.
	 */
	public Projection getProjection() {
		Logger.debug("in NetcdfBoxer.getProjection = " + proj.getName());
		return proj;
	}
	
	public Point2D axisPointToLatLonPoint(double x, double y) {
		return axisPointToLatLonPoint((int)Math.round(x), Math.round(y));
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
		Logger.debug("netcdfConv =" + netcdfConv);
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
		Logger.debug("netcdfConv=" + netcdfConv);
		if ( netcdfConv == VerdiConstants.NETCDF_CONV_ARW_WRF) { // JIZHEN-SHIFT
			Logger.info("within if block:");
			Logger.debug("yStart before shift=" + yStart);
			yStart = yStart - yaxis.getIncrement() * scaler * 0.5; // bottom_left
			Logger.debug("yStart after shift=" + yStart);
			Logger.debug("yaxis.getIncrement()=" + yaxis.getIncrement());
			Logger.debug("scaler=" + scaler);
			Logger.info("yStart = yStart - yaxis.getIncrement() * scaler * 0.5 = " + yStart);
			//yEnd = yEnd - yaxis.getIncrement() * scaler * ( 0.5 + 1);
			yEnd = yEnd - yaxis.getIncrement() * scaler * 0.5; // top_right
			Logger.info("yEnd = yEnd - yaxis.getIncrement() * scaler * 0.5 = " + yEnd);
		}
//		Hints hints = new Hints(Hints.COMPARISON_TOLERANCE, 1E-9);	// TODO: 2014  probably need to do in beginning of VERDI
		Hints.putSystemDefault(Hints.COMPARISON_TOLERANCE, 10e-9);

		detectCRS();
		/*ProjectionPointImpl raw = new ProjectionPointImpl(xStart / scaler, yEnd / scaler);
		LatLonPointImpl latlon = new LatLonPointImpl();
		getProjection().projToLatLon(raw, latlon);
		//getProjection().latLonToProj(latlon,  raw);
		System.err.println("Top Left Raw: " + xStart / scaler + ", " + yEnd / scaler + " Top left LatLon: " + latlon);
		raw = new ProjectionPointImpl(xEnd / scaler, yStart / scaler);
		getProjection().projToLatLon(raw, latlon);
		System.err.println("Bottom Right Raw: " + xEnd / scaler + ", " + yStart / scaler + " Bottom Right LatLon: " + latlon);

		

		Coordinate source = new Coordinate(xEnd, yEnd);
		Coordinate dest = new Coordinate();
		
		MathTransform transform;
		try {
			transform = CRS.findMathTransform(origCRS, VerdiShapefileUtil.LAT_LON_CRS, true);
			JTS.transform(source,  dest, transform);
			System.out.println("Transform  " + source + " to " + dest);
			//origCRS = null;

		} catch (Exception e) {
			e.printStackTrace();
		}*/
		/*double latStart = 5.656792;
		double latEnd = 54.0231;
		double lonStart = -63.23495;*/
		//double lonEnd = -148.2617;
		//latlon = new LatLonPointImpl(latEnd, lonEnd);
		//getProjection().latLonToProj(latlon,  raw);
		//System.err.println("Top Left Mod LatLon: " + raw);
//		xStart = raw.getX() * scaler;
//		yStart = raw.getY() * scaler;
		//latlon = new LatLonPointImpl(latStart, lonStart);
		//getProjection().latLonToProj(latlon,  raw);
		//System.err.println("Bottm Right Mod LatLon: " + raw);
//		xEnd = raw.getX() * scaler;
//		yEnd = raw.getY() * scaler;
		//return new ReferencedEnvelope(xStart, xEnd, yStart, yEnd, PLACEHOLDER_CRS);
		return new ReferencedEnvelope(xStart, xEnd, yStart, yEnd, origCRS);
	}
	
	
	private void detectCRS() {
		Projection proj = getProjection();
		Logger.debug("proj = " + proj.toString() + '\n' + "  projection is not checked yet");
		if (proj instanceof LambertConformal) {
			Logger.debug("proj = " + proj.toString() + '\n' + "  projection is of type LambertConformal");
			if (origCRS == null) {
				Logger.info("NOTE: crs is null");
				try {
					Logger.info("within try/catch block");
					String strCRS = new LambertWKTCreator().createWKT((LambertConformal) proj);
					Logger.info("created strCRS = " + strCRS);
					Logger.info("Ready to call CRS.parseWKT for LambertConformal");
					origCRS = CRS.parseWKT(strCRS);	// NOTE: preferred method (docs.geotools.org/stable/userguide/library/referencing/crs.html)
					Logger.info("parsed CRS: " + origCRS.toString()); // sphere radius is 6370000
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
			if (origCRS == null) {
				Logger.info("NOTE: crs is null ");
				try {
					Logger.info("within try/catch block");
					String strCRS = new UtmWKTCreator().createWKT((UtmProjection) proj);
					Logger.info("created strCRS = " + strCRS.toString());
					Logger.info("Ready to call CRS.parseWKT for UTM Projection");
					origCRS = CRS.parseWKT(strCRS);
				} catch (Exception ex) {
					Logger.error("Error while creating CRS for UTM " + ex.getMessage());
				}
			}
		} else if (proj instanceof Stereographic) {
			Logger.debug("projection is of type Stereographic");
			if (origCRS == null) {
				Logger.info("NOTE: crs is null");
				try {
					Logger.info("within try/catch block");
					String strCRS = new PolarStereographicWKTCreator().createWKT((Stereographic) proj);
					Logger.info("created strCRS = " + strCRS.toString());	// FAILURE CAUSE: PARAMETER["scale_factor", -98.0],
					Logger.info("Ready to call CRS.parseWKT for Stereographic Projection");
					origCRS = CRS.parseWKT(strCRS);		// FAILURE POINT
					Logger.info("parsed CRS: " + origCRS.toString());
				} catch (Exception ex) {
					Logger.error("Error while creating CRS for Stereographic " + ex.getMessage());
				}
			}
		} else if (isLatLon) {
			if (origCRS == null) {
				try {
					String strCRS = new LatlonWKTCreator().createWKT((LatLonProjection)proj);
					origCRS = CRS.parseWKT(strCRS);
				} catch (Exception ex) {
					Logger.error("Error while creating CRS for Lat-Lon " + ex.getMessage());
				}
			}
		} else if (proj instanceof Mercator) {			
			if (origCRS == null) {
				try {
					String strCRS = new MercatorWKTCreator().createWKT((Mercator)proj);
					origCRS = CRS.parseWKT(strCRS);
				} catch (Exception e) {
					Logger.error("Error while creating CRS for Mercator " + e.getMessage());
				}
			} 
		}

		// TODO: add more projections here
		else {
			Logger.error("Projection is not recognized!!");
		} 

		
		
	}
	
	
	public CoordinateReferenceSystem getCRS() {
		//Logger.debug("in getCRS(): returning crs = " + PLACEHOLDER_CRS);
		//return PLACEHOLDER_CRS;
		return origCRS;
	}
	
	public CoordinateReferenceSystem getOriginalCRS() {
		return origCRS;
	}

	protected CoordinateAxis1D getXAxis() {
		return xAxis;		
	}

	protected CoordinateAxis1D getYAxis() {
		return yAxis;
	}
}
