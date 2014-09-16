package anl.verdi.loaders;

import java.awt.geom.Point2D;
import java.text.ParseException;
import java.io.IOException;

import org.geotools.factory.FactoryRegistryException;
import org.geotools.factory.Hints;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
//import org.geotools.referencing.FactoryFinder;
import org.geotools.referencing.ReferencingFactoryFinder;
//import org.geotools.referencing.factory.FactoryGroup;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
//import org.geotools.referencing.wkt.AbstractParser;
//import org.geotools.referencing.wkt.Element;
//import org.geotools.referencing.wkt.MathTransformParser;
//import org.geotools.referencing.wkt.Parser;
//import org.geotools.referencing.wkt.Element.*;
//import org.geotools.referencing.wkt.AbstractParser.*;
import verdi.gtreferencing.wkt.*;
import verdi.gtreferencing.wkt.Element;
import verdi.gtreferencing.wkt.Parser;
import verdi.gtreferencing.wkt.Symbols;
import java.text.ParsePosition;

//import org.geotools.referencing.wkt.Symbols;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransformFactory;

import simphony.util.messages.MessageCenter;
import ucar.nc2.dataset.CoordinateAxis1D;
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
import org.geotools.resources.i18n.Errors;
import org.geotools.resources.i18n.ErrorKeys;

/**
 * Bounding boxer that uses netcdf to create the bounding box.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class NetcdfBoxer implements BoundingBoxer {

	private static MessageCenter msg = MessageCenter.getMessageCenter(BoundingBoxer.class);

//	MathTransformFactory mtFactory = FactoryFinder.getMathTransformFactory(null);
	MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);

//	FactoryGroup factories = new FactoryGroup(null);
	ReferencingFactoryContainer factories = new ReferencingFactoryContainer(null);

	private GridDatatype grid;
	private CoordinateReferenceSystem crs;
	private boolean isLatLon;

	public NetcdfBoxer(GridDatatype grid) {
		this.grid = grid;
		this.isLatLon = grid.getCoordinateSystem().isLatLon();
	}
	
	/**
	 * Gets the Projection associated with this BoundingBoxer.
	 * 
	 * @return the Projection associated with this BoundingBoxer.
	 */
	public Projection getProjection() {
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
		CoordinateAxis1D xaxis = getXAxis();
		CoordinateAxis1D yaxis = getYAxis();
		// coordVal is the sw corner, so coordEdge + 1 should be the center
		// of that cell.
		double xVal = xaxis.getCoordEdge(x + 1);
		double yVal = yaxis.getCoordEdge(y + 1);
		if (isLatLon) {
			return new Point2D.Double(xVal, yVal);
		} else {
			Projection proj = grid.getCoordinateSystem().getProjection();
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
		Projection proj = grid.getCoordinateSystem().getProjection();
		ProjectionPointImpl point = new ProjectionPointImpl();
		proj.latLonToProj(new LatLonPointImpl(lat, lon), point);

		CoordinateAxis1D xaxis = getXAxis();
		CoordinateAxis1D yaxis = getYAxis();
		double x = xaxis.findCoordElement(point.x);
		double y = yaxis.findCoordElement(point.y);
		if (x != -1 && y != -1) {
			double leftEdge = xaxis.getCoordValue((int) x);
			double temp = (float) point.x; //NOTE: leftEdge is acutally a float value
			if (leftEdge == 0.0) temp = (float) Math.round(temp); //NOTE: rounding gets pretty silly here
			
			if (leftEdge > temp) {
				x -= 1;
			}

			double bottomEdge = yaxis.getCoordValue((int) y);
			temp = (float) point.y; //NOTE: bottomEdge is actually a float value
			if (bottomEdge == 0.0) temp = (float) Math.round(temp);
			
			if (bottomEdge > temp) {
				y -= 1;
			}
		}
		
		return new Point2D.Double(x, y);
	}

	public Point2D CRSPointToAxis(double x, double y) {
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
System.out.println("in ReferencedEnvelope; printing parameter values");
System.out.println("xMin = " + xMin);
System.out.println("xMax = " + xMax);
System.out.println("yMin = " + yMin);
System.out.println("yMax = " + yMax);
System.out.println("netcdfConv = " + netcdfConv);
		CoordinateAxis1D xaxis = getXAxis();
System.out.println("xaxis = " + xaxis.toString());
		CoordinateAxis1D yaxis = getYAxis();
System.out.println("yaxis = " + yaxis.toString());
System.out.println("Axis limits:");
System.out.println("xaxis.getStart = " + xaxis.getStart());
System.out.println("xaxis.getIncrement = " + xaxis.getIncrement());
System.out.println("xaxis.getMinValue = " + xaxis.getMinValue());
System.out.println("xaxis.getMaxValue = " + xaxis.getMaxValue());
System.out.println("yaxis.getStart = " + yaxis.getStart());
System.out.println("yaxis.getIncrement = " + yaxis.getIncrement());
System.out.println("yaxis.getMinValue = " + yaxis.getMinValue());
System.out.println("yaxis.getMaxValue = " + yaxis.getMaxValue());
		
		// 
		double xStart, xEnd, yStart, yEnd; //
	    /**
	     * The object to use for parsing <cite>Well-Known Text</cite> (WKT) strings.
	     * Will be created only when first needed.
	     */
	   Parser parser = null;

		// latlon coord does not need to be scaled
		double scaler = isLatLon ? 1.0 : 1000.0;
System.out.println("scaler = " + scaler);
		double xInc = xaxis.getIncrement() * scaler;
System.out.println("xInc = " + xInc);
		double yInc = yaxis.getIncrement() * scaler;
System.out.println("yInc = " + yInc);
		
		int limit = xaxis.getCoordValues().length - 1;
System.out.println("limit = " + limit);
		double start = xaxis.getStart() * scaler;
System.out.println("start = " + start);
		double end = (xaxis.getCoordValue(limit) + xaxis.getIncrement()) * scaler;
System.out.println("end = " + end);
		
		xStart = start + xMin * xInc;
System.out.println("xStart = start + xMin * xInc = " + xStart );
		xEnd = end - ((limit - xMax) * xInc);
System.out.println("xEnd = end - ((limit - xMax) * xInc) = " + xEnd);

		if ( netcdfConv == VerdiConstants.NETCDF_CONV_ARW_WRF) { // JIZHEN-SHIFT
System.out.println("in recompute code section");
			xStart = xStart - xaxis.getIncrement() * scaler * 0.5;
System.out.println("xStart now set to:xStart - xaxis.getIncrement() * scaler * 0.5 = " + xStart );
			//xEnd = xEnd - xaxis.getIncrement() * scaler * ( 0.5 + 1);
			xEnd = xEnd - xaxis.getIncrement() * scaler * 0.5;
System.out.println("xEnd now set to:  xEnd - xaxis.getIncrement() * scaler * 0.5 = " + xEnd);
		}

		limit = yaxis.getCoordValues().length - 1;
System.out.println("limit = yaxis.getCoordValues().length - 1 = " + limit);
		start = yaxis.getStart() * scaler;
System.out.println("start = yaxis.getStart() * scaler = " + start );
System.out.println("yaxis.getCoordValue(limit) = " + yaxis.getCoordValue(limit));
		end = (yaxis.getCoordValue(limit) + yaxis.getIncrement()) * scaler;
System.out.println("end = (yaxis.getCoordValue(limit) + yaxis.getIncrement()) * scaler = " + end);

		yStart = start + yMin * yInc;
System.out.println("yStart =start + yMin * yInc = " + yStart );
		yEnd = end - ((limit - yMax) * yInc);
System.out.println("yEnd = end - ((limit - yMax) * yInc) = " + yEnd);
		if ( netcdfConv == VerdiConstants.NETCDF_CONV_ARW_WRF) { // JIZHEN-SHIFT
System.out.println("within if block:");
			yStart = yStart - yaxis.getIncrement() * scaler * 0.5; // bottom_left
System.out.println("yStart = yStart - yaxis.getIncrement() * scaler * 0.5 = " + yStart);
			//yEnd = yEnd - yaxis.getIncrement() * scaler * ( 0.5 + 1);
			yEnd = yEnd - yaxis.getIncrement() * scaler * 0.5; // top_right
System.out.println("yEnd = yEnd - yaxis.getIncrement() * scaler * 0.5 = " + yEnd);
		}
//		Hints hints = new Hints(Hints.COMPARISON_TOLERANCE, 1E-9);	// TODO: 2014  probably need to do in beginning of VERDI
		Hints.putSystemDefault(Hints.COMPARISON_TOLERANCE, 10e-9);

		Projection proj = grid.getCoordinateSystem().getProjection();

		if (proj instanceof LambertConformal) {
System.out.println("proj = " + proj.toString() + '\n' + "  projection is of type LambertConformal");
			if (crs == null) 
			{ // BEGIN IF crs == null
System.out.println("NOTE: crs is null");
				try 
				{ // BEGIN try to get value for crs
System.out.println("within try/catch block");
					String strCRS = new LambertWKTCreator().createWKT((LambertConformal) proj);
System.out.println("created strCRS = " + strCRS);
System.out.println("Ready to call CRS.parseWKT - with substitutions of function bodies for function calls");
//					crs = CRS.parseWKT(strCRS);	// NOTE: preferred method (docs.geotools.org/stable/userguide/library/referencing/crs.html)
					   /**
				     * Parses a
				     * <A HREF="http://geoapi.sourceforge.net/snapshot/javadoc/org/opengis/referencing/doc-files/WKT.html"><cite>Well
				     * Known Text</cite></A> (WKT) into a CRS object. This convenience method is a
				     * shorthand for the following:
				     *
				     * <blockquote><code>
				     * FactoryFinder.{@linkplain ReferencingFactoryFinder#getCRSFactory getCRSFactory}(null).{@linkplain
				     * org.opengis.referencing.crs.CRSFactory#createFromWKT createFromWKT}(wkt);
				     * </code></blockquote>
				     */
//				    public static CoordinateReferenceSystem parseWKT(final String wkt) throws FactoryException {
System.out.println("ready to call replacement for parseWKT: actually calls ReferencingFactoryFinder.getCRSFactory.createFromWKT");
System.out.println("keeping call to ReferencingFactoryfinder.getCRSFactory(null)");					
				CRSFactory aCRSFactory = ReferencingFactoryFinder.getCRSFactory(null);
System.out.println("got aCRSFactory; onward to createFromWKT");
System.out.println("substituting for .createFromWKT");
{		// open block for code being substituted 
    if (parser == null) 
    {	// parser == null
//        createParser(ReferencingFactoryFinder.getDatumFactory(null), ReferencingFactoryFinder.getMathTransformFactory(null));
    	// createParser calls a constructor for Parser
        /**
         * Creates inconditionnaly the WKT parser. This is factored out as a single private method
         * for making easier to spot the places in this code that need to create the parser and the
         * datum-alias patch.
         */
//        private void createParser(final DatumFactory datumFactory, final MathTransformFactory mtFactory) {
//            parser = new Parser(Symbols.DEFAULT, datumFactory, this, this, mtFactory);
  System.out.println("Creating a parser because it is null; using contents of createParser directly");
  			parser = new Parser(Symbols.DEFAULT, ReferencingFactoryFinder.getDatumFactory(null), 
  					ReferencingFactoryFinder.getCSFactory(null), aCRSFactory,
  					ReferencingFactoryFinder.getMathTransformFactory(null));
System.out.println("Created a new parser because parser was null"); 		// OK TO THIS POINT
        }	// end parser was == null
    System.out.println("now have a parser - either old or new");
    System.out.println("parser = " + parser.toString());
    try 
    { // BEGIN try work with parser
System.out.println("Into TRY block: have to break up parser.parseCoordinateReferenceSystem");
/**
 * Parses a coordinate reference system element.
 *
 * @param  text The text to be parsed.
 * @return The coordinate reference system.
 * @throws ParseException if the string can't be parsed.
 */
//public CoordinateReferenceSystem parseCoordinateReferenceSystem(final String text)
//        throws ParseException
//{
/**
 * Returns a tree of {@link Element} for the specified text.
 *
 * @param  text       The text to parse.
 * @param  position   In input, the position where to start parsing from.
 *                    In output, the first character after the separator.
 * @return The tree of elements to parse.
 * @throws ParseException If an parsing error occured while creating the tree.
 */
//protected final Element getTree(final String text, final ParsePosition position)
 //       throws ParseException
//{
 //   return new Element(new Element(this, text, position));
//}


ParsePosition aParsePosition = new ParsePosition(0);
final Element elementDirect = (parser).getTree(strCRS, new ParsePosition(0));
System.out.println("Element created directly from getTree: " + elementDirect.toString());
//Element innerElement = new Element((MathTransformParser)parser, strCRS, aParsePosition);
//System.out.println("innerElement: " + innerElement.toString());
//Element outerElement = new Element(innerElement);	// outerElement is what is returned by getTree
//System.out.println("outerElement: " + outerElement.toString());
//	org.geotools.referencing.wkt.Element element = new org.geotools.referencing.wkt.Element(new org.geotools.referencing.wkt.Element());
//    final Element element = org.geotools.referencing.wkt.AbstractParser.getTree(strCRS, new ParsePosition(0));
//    final CoordinateReferenceSystem crs = parseCoordinateReferenceSystem(element);
System.out.println("replace for parseCoordinateReferenceSystem");
//    element.close();
//    return crs;
//}
        /**
         * Parses a coordinate reference system element.
         *
         * @param  parent The parent element.
         * @return The next element as a {@link CoordinateReferenceSystem} object.
         * @throws ParseException if the next element can't be parsed.
         */
//        private CoordinateReferenceSystem parseCoordinateReferenceSystem(final Element element)	// elementDirect
 //               throws ParseException
  //      {
            final Object key = elementDirect.peek();
            if (key instanceof Element) 
            { // BEGIN found element for keyword
            	System.out.println("BEGIN found element for keyword: " + key.toString());
                final String keyword = ((Element) key).keyword.trim().toUpperCase(parser.symbols.locale);
                System.out.println("keyword: " + keyword.toString());
//                CoordinateReferenceSystem r = null;
                try 
                { // BEGIN try handle keyword
                	System.out.println("BEGIN try handle keyword: " + keyword.toString()); 		// OK TO THIS POINT
                    if (   "GEOGCS".equals(keyword)) crs=parser.parseGeoGCS  (elementDirect);
                    else if (   "PROJCS".equals(keyword)) crs=parser.parseProjCS  (elementDirect);
                    else if (   "GEOCCS".equals(keyword)) crs=parser.parseGeoCCS  (elementDirect);
                    else if (  "VERT_CS".equals(keyword)) crs=parser.parseVertCS  (elementDirect);
                    else if ( "LOCAL_CS".equals(keyword)) crs=parser.parseLocalCS (elementDirect);
                    else if ( "COMPD_CS".equals(keyword)) crs=parser.parseCompdCS (elementDirect);
                    else if ("FITTED_CS".equals(keyword)) crs=parser.parseFittedCS(elementDirect);
                    System.out.println("END try handle keyword - got to end of parser list");
                } // END  try handle keyword
                catch (Exception e){
                    // Work around for simulating post-conditions in Java.
                    assert Parser.isValid(crs, keyword) : elementDirect;
                } // FINALLY block: changed to CATCH block because crs above is now being set instead of returned
            } // END found element for keyword
  //      crs = parser.parseCoordinateReferenceSystem(strCRS);		// wkt
            System.out.println("END try to handle parser - now should have crs");	// GOT TO HERE BUT MISSED LOTS OF PRINTS
            elementDirect.close();
    } // END try to handle parser 
    catch (ParseException exception) 
    { // BEGIN catch handle parser
        final Throwable cause = exception.getCause();
        if (cause instanceof FactoryException) {
            throw (FactoryException) cause;
        }
        throw new FactoryException(exception);
    } // END catch handle parser

} // END BLOCK BEING SUBSTITUTED
// --->				    crs = aCRSFactory.createFromWKT(strCRS);
//				    }
System.out.println("parsed CRS: " + crs.toString());	// NullPointerException on this line
				} // END of try parse 
				catch (IOException ex) {
System.out.println("into exception handling");
					System.out.println("Error while creating CRS for LambertConformal\n");
					ex.printStackTrace();
				} catch (FactoryException e) {
					System.out.println("Caught FactoryException while creating CRS for LambertConformal");
					e.printStackTrace();
				} catch (FactoryRegistryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} // END if crs == null
		} // END if proj is LambertConformal 
		else if (proj instanceof UtmProjection) {
System.out.println("projection is of type UtmProjection");
			if (crs == null) {
System.out.println("NOTE: crs is null");
				try {
System.out.println("within try/catch block");
					String strCRS = new UtmWKTCreator().createWKT((UtmProjection) proj);
System.out.println("created strCRS = " + strCRS.toString());
System.out.println("Ready to call CRS.parseWKT");
					crs = CRS.parseWKT(strCRS);
				} catch (Exception ex) {
					msg.error("Error while creating CRS for UTM", ex);
				}
			}
		} else if (proj instanceof Stereographic) {
System.out.println("projection is of type Stereographic");
			if (crs == null) {
System.out.println("NOTE: crs is null");
				try {
System.out.println("within try/catch block");
					String strCRS = new PolarStereographicWKTCreator().createWKT((Stereographic) proj);
System.out.println("created strCRS = " + strCRS.toString());
System.out.println("Ready to call CRS.parseWKT");
					crs = CRS.parseWKT(strCRS);
System.out.println("parsed CRS: " + crs.toString());
				} catch (Exception ex) {
					msg.error("Error while creating CRS for Stereographic", ex);
				}
			}
		} else if (isLatLon) {
			if (crs == null) {
				try {
					String strCRS = new LatlonWKTCreator().createWKT((LatLonProjection)proj);
					crs = CRS.parseWKT(strCRS);
				} catch (Exception ex) {
					msg.error("Error while creating CRS for Lat-Lon", ex);
				}
			}
		} else if (proj instanceof Mercator) {
			if (crs == null) {
				try {
					String strCRS = new MercatorWKTCreator().createWKT((Mercator)proj);
					crs = CRS.parseWKT(strCRS);
				} catch (Exception e) {
					msg.error("Error while creating CRS for Mercator", e);
				}
			}
		}

		// TODO: add more projections here
		else {
			msg.warn("Projection is not recognized!!");
		} 
		
		return new ReferencedEnvelope(xStart, xEnd, yStart, yEnd, crs);
	}

	private CoordinateAxis1D getXAxis() {
		GridCoordSystem gcs = grid.getCoordinateSystem();
		return (CoordinateAxis1D) gcs.getXHorizAxis();
	}

	private CoordinateAxis1D getYAxis() {
		GridCoordSystem gcs = grid.getCoordinateSystem();
		return (CoordinateAxis1D) gcs.getYHorizAxis();
	}

}

/*
 * private CoordinateReferenceSystem getLambert(LambertConformal gcs) { try {
 * String wkt = "PROJCS[\"\", GEOGCS[\"Normal Sphere (r=6371007\"," + "
 * DATUM[\"unknown\",\n" + " SPHEROID[\"SPHERE\", 6370997, 0]],\n" + "
 * PRIMEM[\"Greenwich\", 0],\n" + " UNIT[\"degree\", 0.0174532925199433],\n" + "
 * AXIS[\"Geodetic longitude\", EAST],\n" + " AXIS[\"Geodetic latitude\",
 * NORTH]]," + "PROJECTION[\"Lambert_Conformal_Conic_2SP\"],\n" + "
 * PARAMETER[\"central_meridian\", -90.0],\n" + "
 * PARAMETER[\"latitude_of_origin\", 40.0],\n" + "
 * PARAMETER[\"longitude_of_origin\", -90.0],\n" + "
 * PARAMETER[\"standard_parallel_1\", 30],\n" + "
 * PARAMETER[\"standard_parallel_2\", 60],\n" + " PARAMETER[\"false_easting\",
 * 0.0],\n" + " PARAMETER[\"false_northing\", 0.0],\n" + " UNIT[\"m\", 1.0],\n" + "
 * AXIS[\"x\", EAST],\n" + " AXIS[\"y\", NORTH]]";
 * 
 * return CRS.parseWKT(wkt); } catch (Exception e) { throw new
 * IllegalArgumentException("Unable to create coordinate reference system", e); } }
 */

/*
 * Map<String, Object> params = new HashMap<String, Object>();
 * params.put("name", "unknown"); DefaultEllipsoid sphere =
 * DefaultEllipsoid.createEllipsoid("SPHERE", 6371007.0, 6371007.0,
 * DefaultEllipsoid.SPHERE.getAxisUnit()); GeodeticDatum datum =
 * FactoryFinder.getDatumFactory(null).createGeodeticDatum(params,
 * DefaultEllipsoid.SPHERE, DefaultPrimeMeridian.GREENWICH); params = new
 * HashMap<String, Object>(); params.put("name", "Normal Sphere (r=6371007)");
 * 
 * 
 * GeographicCRS crs =
 * FactoryFinder.getCRSFactory(null).createGeographicCRS(params, datum,
 * DefaultEllipsoidalCS.GEODETIC_2D); // Lambert_Conformal_Conic_1SP (EPSG code
 * 9801) // Lambert_Conformal_Conic_2SP (EPSG code 9802) //
 * Lambert_Conic_Conformal_2SP_Belgium (EPSG code 9803) //
 * Lambert_Conformal_Conic - An alias for the ESRI 2SP case that includes a
 * scale_factor parameter ParameterValueGroup parameters =
 * mtFactory.getDefaultParameters("Lambert_Conformal_Conic_2SP");
 * parameters.parameter("standard_parallel_1").setValue(gcs.getParallelOne());
 * parameters.parameter("standard_parallel_2").setValue(gcs.getParallelTwo());
 * parameters.parameter("latitude_of_origin").setValue(gcs.getOriginLat());
 * parameters.parameter("longitude_of_origin").setValue(gcs.getOriginLon());
 * parameters.parameter("central_meridian").setValue(gcs.getOriginLon());
 * parameters.parameter("false_easting").setValue(gcs.getFalseEasting());
 * parameters.parameter("false_northing").setValue(gcs.getFalseNorthing());
 * //parameters.parameter("scale_factor").setValue(1.0); Map properties =
 * Collections.singletonMap("name", "unknown"); return
 * factories.createProjectedCRS(properties, crs, null, parameters,
 * DefaultCartesianCS.GENERIC_2D);
 */
