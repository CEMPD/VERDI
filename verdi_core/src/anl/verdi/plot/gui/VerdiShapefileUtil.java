package anl.verdi.plot.gui;

import java.awt.geom.AffineTransform;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.Transaction;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import anl.verdi.area.target.Target;
import ucar.unidata.geoloc.LatLonPointImpl;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPointImpl;
import ucar.unidata.geoloc.projection.LatLonProjection;

public class VerdiShapefileUtil {
	
	/**
	 * 
	 * VERDI renders NetCDF files that have been using NetCDF projections.  However it displays map
	 * boundaries that are encoded as shapefiles and rendered using Geotools.  For the map boundaries
	 * to line up with the NetCDF data, Geotools and NetCDF must be using the same projection algorithm,
	 * with the same parameters set.  In some cases, this is impossible because Geotools doesn't support
	 * the same set of projections as NetCDF (EPSG 9804, EPSG 9805, EPSG 9810, and EPSG 9829, possibly others
	 * are used in NetCDF but missing from Geotools).  Support for other projections would need to be added
	 * on a case by case basis, since each one has a different set of parameters that would need to be 
	 * accounted for and copied from the NetCDF java object into the proper place in a WKT string to be
	 * parsed by Geotools.
	 * 
	 * Instead, VERDI converts all shapefiles to Lat/Lon (if not lat/lon already), then uses the NetCDF
	 * projection to manually reproject the lat/lon shapefile into projected coordinates.  All Geotools
	 * CoordindateReferenceSystems objects are set to a single placeholder object, so Geotools never attempts
	 * any further projections, and the correct NetCDF projected shapefile is rendered.
	 * 
	 * Shapefiles are also cached in memory, so they're only opened / projected once per session.
	 * 
	 */
	
	static GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
	
	private static CoordinateReferenceSystem LAT_LON_CRS = null;
	static {
		try {
			LAT_LON_CRS = CRS.decode("EPSG:4326");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static SimpleFeatureSource panShapefile(SimpleFeatureSource sourceShapefile, double panX, double panY) {
		AffineTransform affineTransform = AffineTransform.getTranslateInstance(panX, panY);
		MathTransform transform = new AffineTransform2D(affineTransform);


		return transformFeature(sourceShapefile, transform, false);
	}
	
	public static SimpleFeatureSource projectionToLatLon(SimpleFeatureSource sourceShapefile, boolean mapTargets) {
		SimpleFeatureSource convertedSource = null;
        try {
			CoordinateReferenceSystem sourceCRS = sourceShapefile.getSchema().getCoordinateReferenceSystem();
			
	        boolean lenient = true; // allow for some error due to different datums

			MathTransform transform = CRS.findMathTransform(sourceCRS, LAT_LON_CRS, lenient);
			convertedSource = transformFeature(sourceShapefile, transform, mapTargets);
		} catch (FactoryException e) {
			e.printStackTrace();
		}
        return convertedSource;
        
	}
	
	public static SimpleFeatureSource transformFeature(SimpleFeatureSource sourceShapefile, MathTransform transform, boolean mapTargets) {
		SimpleFeatureSource convertedSource = null;
		try {
	        
	        
	        SimpleFeatureCollection featureCollection = sourceShapefile.getFeatures();
	
	        MemoryDataStore dataStore = new MemoryDataStore();
	        SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(sourceShapefile.getSchema(), LAT_LON_CRS);
	        dataStore.createSchema(featureType);
	
	        //Get the name of the new Shapefile, which will be used to open the FeatureWriter
	        String createdName = dataStore.getTypeNames()[0];
	        
	        Transaction transaction = new DefaultTransaction("Reproject");
	        try ( FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
	                        dataStore.getFeatureWriterAppend(createdName, transaction);
	              SimpleFeatureIterator iterator = featureCollection.features()){
	            while (iterator.hasNext()) {
	                // copy the contents of each feature and transform the geometry
	                SimpleFeature feature = iterator.next();
	                SimpleFeature copy = writer.next();
	                copy.setAttributes(feature.getAttributes());
	
	                Geometry geometry = (Geometry) feature.getDefaultGeometry();
	                Geometry geometry2 = JTS.transform(geometry, transform);
	
	                copy.setDefaultGeometry(geometry2);
	                if (mapTargets)
	                	Target.mapProjection(geometry, geometry2);

	                writer.write();
	            }
	            transaction.commit();
	            convertedSource = dataStore.getFeatureSource(featureType.getName());
	        } finally {
	            transaction.close();
	        }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return convertedSource;
	}
	
	private static Map<String, Map<Projection, SimpleFeatureSource>> shapefileMap = new HashMap<String, Map<Projection, SimpleFeatureSource>>();
	private static Map<String, SimpleFeatureSource> unprojectedMap = new HashMap<String, SimpleFeatureSource>();
	
    private static void cacheShapefile(String filename, Projection targetProjection, SimpleFeatureSource convertedSource) {
    	Map<Projection, SimpleFeatureSource> projectionMap = shapefileMap.get(filename);
    	if (projectionMap == null) {
    		projectionMap = new HashMap<Projection, SimpleFeatureSource>();
    		shapefileMap.put(filename,  projectionMap);
    	}
    	projectionMap.put(targetProjection,  convertedSource);
    }
    
    public static SimpleFeatureSource getCachedShapefile(String filename, Projection targetProjection, SimpleFeatureSource originalShapefile) {
    	if (targetProjection == null)
    		return unprojectedMap.get(filename);
    	/*if (!(targetProjection instanceof Mercator) 
    			&& !(targetProjection instanceof Stereographic))
    		return originalShapefile;*/
    	SimpleFeatureSource cachedShapefile = null;
    	Map<Projection, SimpleFeatureSource> projectionMap = shapefileMap.get(filename);
    	if (projectionMap != null) {
    		cachedShapefile = projectionMap.get(targetProjection);
    	}
    	return cachedShapefile;
    	
    }

    public static FeatureSource projectShapefile(String filename, SimpleFeatureSource sourceShapefile, Projection targetProjection, CoordinateReferenceSystem targetCRS) {
    	return projectShapefile(filename, sourceShapefile, targetProjection, targetCRS, false);
    }

    static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
	//targetProjection - NetCDF projection used to create map data
    //targetCRS - CRS that Verdi determines the map to be using
    public static FeatureSource projectShapefile(String filename, SimpleFeatureSource sourceShapefile, Projection targetProjection, CoordinateReferenceSystem targetCRS, boolean mapTargets) {
    	//long start = System.currentTimeMillis();
    	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " "+ format.format( new Date()) + " " + filename + " to " + targetProjection.getName());
    	SimpleFeatureSource convertedSource = getCachedShapefile(filename, targetProjection, sourceShapefile);
        if (convertedSource != null) {
        	//long duration = System.currentTimeMillis() - start;
        	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " " + duration + "ms "+ filename + " cached");
        	return convertedSource;
        }
        
       // if (sourceShapefile.getSchema().getCoordinateReferenceSystem().getCoordinateSystem().toString().toLowerCase().indexOf("longitude") == -1) {
        if (sourceShapefile.getSchema().getCoordinateReferenceSystem().toString().indexOf("WGS84") == -1) {
        	//Not in lat/lon, reproject
        	sourceShapefile = projectionToLatLon(sourceShapefile, mapTargets);
        } else if (targetProjection instanceof LatLonProjection && ((LatLonProjection)targetProjection).getCenterLon() == 0 ) {
        	//long duration = System.currentTimeMillis() - start;
        	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " " + duration + "ms "+ filename + " already lat lon");       	
        	return sourceShapefile;
        }
        
        MemoryDataStore dataStore = null;
        
        
        
        try {
			//TODO - make this a memory object	
			
	        SimpleFeatureCollection featureCollection = sourceShapefile.getFeatures();
	        
	        dataStore = new MemoryDataStore();
	        SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(sourceShapefile.getSchema(), targetCRS);
	        dataStore.createSchema(featureType);
	
	        //Get the name of the new Shapefile, which will be used to open the FeatureWriter
	        String createdName = dataStore.getTypeNames()[0];
	        
	        Transaction transaction = new DefaultTransaction("Reproject");
	        try ( FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
	                        dataStore.getFeatureWriterAppend(createdName, transaction);
	              SimpleFeatureIterator iterator = featureCollection.features()){
	            while (iterator.hasNext()) {
	                // copy the contents of each feature and transform the geometry
	                SimpleFeature feature = iterator.next();
	                SimpleFeature copy = writer.next();
	                copy.setAttributes(feature.getAttributes());
	
	                Geometry sourceGeometry = (Geometry) feature.getDefaultGeometry();
	                Geometry targetGeometry = null;
	                
	                if (sourceGeometry instanceof MultiPolygon) {                	
	                	//System.out.println("Source has " + sourceGeometry.getNumGeometries() + " geometries");               	
	                	Polygon[] targetGeometries = new Polygon[sourceGeometry.getNumGeometries()];
	                	for (int i = 0; i < targetGeometries.length; ++i) {
	                		Geometry internalGeometry = sourceGeometry.getGeometryN(i);
	                		targetGeometries[i] = projectPolygon((Polygon)internalGeometry, targetProjection);
	    	                if (mapTargets) {
	    	                	Target.mapProjection(internalGeometry, targetGeometries[i]);
	    	                }
	                	}
	                	
	                	targetGeometry = geometryFactory.createMultiPolygon(targetGeometries);
	
	                }
	                else if (sourceGeometry instanceof MultiLineString) {
	                	List<LineString> lineStringList = new ArrayList<LineString>();
	                	LineString[] listArray = new LineString[0];
	                	LineString lineString = null;
	                	for (int i = 0; i < sourceGeometry.getNumGeometries(); ++i) {
	                		Geometry internalGeometry = sourceGeometry.getGeometryN(i);
	                		lineString = projectLineString(lineStringList, (LineString)internalGeometry, targetProjection);
	    	                if (mapTargets) {
	    	                	Target.mapProjection(internalGeometry, lineString);
	    	                }
	                	}
	                	targetGeometry = geometryFactory.createMultiLineString(lineStringList.toArray(listArray));
	                } else {
	                	throw new IllegalArgumentException("Unsupported shapefile type: " + sourceGeometry.getClass());
	                }
	
	                copy.setDefaultGeometry(targetGeometry);
	                writer.write();
	            }
	            transaction.commit();
	            convertedSource = dataStore.getFeatureSource(featureType.getName());
	            //convertedSource = dataStore.getFeatureSource();
	        } catch (Exception e) {
	            e.printStackTrace();
	            transaction.rollback();
	        } finally {
	            transaction.close();
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        cacheShapefile(filename, targetProjection, convertedSource);
    	//long duration = System.currentTimeMillis() - start;
    	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " " + duration + "ms "+ filename + " projected");
    	return convertedSource;
    }
    
    //If you went to far
      //If going west, subtract width
      //If going east, add width
    //Add fake point
    //Pick up where you left off, adding the previous point first
    private static LineString projectAndClipLineString(List<LineString> lineStringList, Coordinate[] sourceCoordinates, int startIndex, LatLonPointImpl latLon, ProjectionPointImpl xy, double factor, Projection proj, int reverseMode) {
   
    	Coordinate[] targetCoordinates = new Coordinate[sourceCoordinates.length - startIndex];
    	LineString targetLineString = null;
        
    	int sourceIndex = startIndex;
    	int targetIndex = 0;
    	Coordinate source = sourceCoordinates[sourceIndex];
    	double prevX = Double.POSITIVE_INFINITY;
    	double currentX = 0;
    	
    	if (reverseMode > 0) {
        	latLon.set(source.y, source.x);
        	proj.latLonToProj(latLon, xy);
        	targetCoordinates[targetIndex] = new Coordinate(xy.getX() * factor, xy.getY() * factor);
    		if (reverseMode == 1) { //crossed to the east, take current point and move left
    			targetCoordinates[targetIndex].x += 360;
    		} else //crossed to the west, take current point and move right
    			targetCoordinates[targetIndex].x -= 360;
    		//System.err.println("Continuing clip from " + targetCoordinates[targetIndex].x);
			prevX = Double.POSITIVE_INFINITY;
			++targetIndex;
			++sourceIndex;
    	}
    	
        for (; sourceIndex < sourceCoordinates.length; ++sourceIndex, ++targetIndex) { //TODO here
        	source = sourceCoordinates[sourceIndex];
        	latLon.set(source.y, source.x);
        	proj.latLonToProj(latLon, xy);
        	currentX = xy.getX() * factor;
        	if (prevX == Double.POSITIVE_INFINITY)
        		prevX = currentX;
        	
        	//TODO - see if this needs to be done for non lat/lon maps
        	/*
        	 * when lat/lon maps are rotated from 0 to a nonzero center lon, lines can get split across the edges,
        	 * resulting in horiontal lines running across the screen.  This code detects when that happens and
        	 * separates the line into 2 separate lines, one on each side
        	 */
        	if (proj instanceof LatLonProjection && ((LatLonProjection)proj).getCenterLon() != 0 && Math.abs(currentX - prevX) > 360 *.75) {
        		//System.err.println("Point from " + prevX + " to " + currentX + ", clipping length " + (targetIndex + 1));
        		//Going east, move point to the left and continue
        		if (currentX > prevX) {
        			currentX -= 360;
        			reverseMode = 1;
        		}
        		else { //going west, move point to the right and continue
        			currentX += 360;
        			reverseMode = 2;
        		}
        		//negative means current is on right, move left
            	targetCoordinates[targetIndex] = new Coordinate(currentX, xy.getY() * factor);    

            	Coordinate[] clippedSegment = Arrays.copyOf(targetCoordinates, targetIndex + 1);

        		
                try {
                	targetLineString = geometryFactory.createLineString(clippedSegment);
                	lineStringList.add(targetLineString);
            		projectAndClipLineString(lineStringList, sourceCoordinates, sourceIndex - 1, latLon, xy, factor, proj, reverseMode);
                	return targetLineString;
                } catch (Exception e) {
                	e.printStackTrace();
                }
        		
        	}
        	prevX = currentX;
        		
        	targetCoordinates[targetIndex] = new Coordinate(currentX, xy.getY() * factor);

        }

        
        try {
        	targetLineString = geometryFactory.createLineString(targetCoordinates);
        	lineStringList.add(targetLineString);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return targetLineString;   

    }

    
    private static LineString projectLineString(List<LineString> lineStringList, Geometry sourceGeometry, Projection proj) {
    	double factor = 1000;
    	if (proj instanceof LatLonProjection)
    		factor = 1;
        Coordinate[] sourceCoordinates = sourceGeometry.getCoordinates();
        LatLonPointImpl latLon = new LatLonPointImpl();
        ProjectionPointImpl xy = new ProjectionPointImpl();
        
        return projectAndClipLineString(lineStringList, sourceCoordinates, 0, latLon, xy, factor, proj, 0);
    }

    private static Polygon projectPolygon(Geometry sourceGeometry, Projection proj) {
    	if (proj == null)
    		return (Polygon)sourceGeometry;
    	double factor = 1000;
    	if (proj instanceof LatLonProjection)
    		factor = 1;
    	Polygon targetPolygon = null;
    	//valid polygons must end with the same coordinate that they start with.  Add if not present.
    	int closeRing = 0;
        Coordinate[] sourceCoordinates = sourceGeometry.getCoordinates();
        if (!sourceCoordinates[0].equals(sourceCoordinates[sourceCoordinates.length - 1]))
        	closeRing = 1;
        Coordinate[] targetCoordinates = new Coordinate[sourceCoordinates.length + closeRing];
        LatLonPointImpl latLon = new LatLonPointImpl();
        ProjectionPointImpl xy = new ProjectionPointImpl();
        
    	
        for (int i = 0; i < sourceCoordinates.length; ++i) {
        	Coordinate source = sourceCoordinates[i];
        	latLon.set(source.y, source.x);
        	proj.latLonToProj(latLon, xy);
        	targetCoordinates[i] = new Coordinate(xy.getX() * factor, xy.getY() * factor);
        }
        if (closeRing > 0)
        	targetCoordinates[targetCoordinates.length - 1] = (Coordinate) targetCoordinates[0].clone();
        
        try {
        	targetPolygon = geometryFactory.createPolygon(targetCoordinates);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return targetPolygon;   	
    }


}
