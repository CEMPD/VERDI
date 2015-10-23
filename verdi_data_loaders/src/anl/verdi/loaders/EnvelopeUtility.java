package anl.verdi.loaders;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
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

import ucar.ma2.Array;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateAxis1D;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dt.GridCoordSystem;
import ucar.nc2.dt.GridDatatype;
import ucar.nc2.dt.grid.GridDataset;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.projection.LambertConformal;

public class EnvelopeUtility {
	static final Logger Logger = LogManager.getLogger(EnvelopeUtility.class.getName());
	
//	static MathTransformFactory mtFactory = FactoryFinder.getMathTransformFactory(null);	// 2014
	static MathTransformFactory mtFactory = ReferencingFactoryFinder.getMathTransformFactory(null);
//	static FactoryGroup factories = new FactoryGroup(null);									// 2014
	static ReferencingFactoryContainer factories = new ReferencingFactoryContainer(null);

	public static ReferencedEnvelope getReferencedEnvelope(AbstractNetcdfDataset dataset) {
		ReferencedEnvelope env = null;
		String gridName = dataset.getVariableNames().get(dataset.getVariableNames().size() - 1);
		GridDataset gridSet = null;
		
		try {
			gridSet = new GridDataset(dataset.getNetDataset());
		} catch (IOException e) {
			//
		}
		
		GridDatatype grid = gridSet.findGridDatatype(gridName);
		Projection proj = grid.getCoordinateSystem().getProjection();
		GridCoordSystem gcs = grid.getCoordinateSystem();
		CoordinateAxis1D xaxis = (CoordinateAxis1D) gcs.getXHorizAxis();
		CoordinateAxis1D yaxis = (CoordinateAxis1D) gcs.getYHorizAxis();

		// latlon coord does not need to be scaled
		double scaler = (gcs.isLatLon()) ? 1.0 : 1000.0;

		double xStart = xaxis.getStart() * scaler;
		double yStart = Math.abs(yaxis.getMaxValue()) * scaler;
		double xInc = xaxis.getIncrement() * scaler;
		double yInc = Math.abs(yaxis.getIncrement()) * scaler;

		CoordinateSystem system = (CoordinateSystem) gridSet.getNetcdfDataset().getCoordinateSystems().get(0);
		List<CoordinateAxis> coords = system.getCoordinateAxes();		// changed from List to List<CoordinateAxis>
//		Map<ucar.nc2.constants.AxisType, AxisType> types = new HashMap<ucar.nc2.constants.AxisType, AxisType>();	// types was not used
		long numXCells = 0;
		long numYCells = 0;
		for (CoordinateAxis axis : (List<CoordinateAxis>) coords) {
			ucar.nc2.constants.AxisType ncfType = axis.getAxisType();
			if (ncfType != null) {
				if (ncfType.compareTo(ucar.nc2.constants.AxisType.GeoX) == 0) {
					numXCells = axis.getSize();
				} else if (ncfType.compareTo(ucar.nc2.constants.AxisType.GeoY) == 0) {
					numYCells = axis.getSize();
				}
			}
		}

		if (proj instanceof LambertConformal) {
			env = new ReferencedEnvelope(xStart, xStart + numXCells * xInc, yStart - numYCells * yInc, yStart,
					getLambert((LambertConformal) proj));
		}
		return env;
	}

	public static void createGridCoverage(AbstractNetcdfDataset dataset, String gridName, int width, int height) {
		try {
			GridDataset gridSet = new GridDataset(dataset.getNetDataset());
			GridDatatype grid = gridSet.findGridDatatype(gridName);
//			WritableRaster raster = RasterFactory.createBandedRaster(DataBuffer.TYPE_FLOAT, width, height, 1, null);	// raster was not used

			Array array = grid.getVariable().read();
			float[] farray = (float[]) array.get1DJavaArray(Float.TYPE);
			for (float f : farray) {
				Logger.debug(f);
			}
		} catch (Exception e) {

		}
	}

	private static CoordinateReferenceSystem getLambert(LambertConformal gcs) {
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("name", "North_American_Datum_1983");
//			GeodeticDatum datum = FactoryFinder.getDatumFactory(null).createGeodeticDatum(params,
//					DefaultEllipsoid.GRS80, DefaultPrimeMeridian.GREENWICH);	// 2014
			GeodeticDatum datum = ReferencingFactoryFinder.getDatumFactory(null).createGeodeticDatum(params,
					DefaultEllipsoid.GRS80, DefaultPrimeMeridian.GREENWICH);
			params = new HashMap<String, Object>();
			params.put("name", "NAD83");

//			GeographicCRS crs = FactoryFinder.getCRSFactory(null).createGeographicCRS(params, datum,
//					DefaultEllipsoidalCS.GEODETIC_2D);							// 2014
			GeographicCRS crs = ReferencingFactoryFinder.getCRSFactory(null).createGeographicCRS(params, datum,
					DefaultEllipsoidalCS.GEODETIC_2D);
			ParameterValueGroup parameters = mtFactory.getDefaultParameters("Lambert_Conformal_Conic_2SP");
			parameters.parameter("standard_parallel_1").setValue(gcs.getParallelOne());
			parameters.parameter("standard_parallel_2").setValue(gcs.getParallelTwo());
			parameters.parameter("latitude_of_origin").setValue(gcs.getOriginLat());
			parameters.parameter("longitude_of_origin").setValue(gcs.getOriginLon());
			parameters.parameter("central_meridian").setValue(gcs.getOriginLon());
			parameters.parameter("false_easting").setValue(0);
			parameters.parameter("false_northing").setValue(0);
			Map<String, String> properties = Collections.singletonMap("name", "unknown");	// changed Map to Map<String, String>	2014
//			return factories.createProjectedCRS(properties, crs, null, parameters, DefaultCartesianCS.GENERIC_2D);	// deprecated, list of reasons provided in documentation
						// see www.geoapi.org/2.0/javadoc/org/opengis/referencing/crs/CRSFactory.html
			CRSFactory crsFactory = factories.getCRSFactory();		// 2014
			DefiningConversion conv = new DefiningConversion("sample", parameters);
			return crsFactory.createProjectedCRS(properties, crs, conv, DefaultCartesianCS.GENERIC_2D);
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to create coordinate reference system", e);
		}
	}
}
