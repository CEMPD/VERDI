package anl.verdi.plot.gui;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.FeatureSource;
import org.geotools.data.FeatureWriter;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.Transaction;
import org.geotools.data.memory.MemoryDataStore;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.NameImpl;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import anl.verdi.area.target.Target;
import anl.verdi.data.ObsData;
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
	private static CoordinateReferenceSystem WEB_MERC_CRS = null;
	static {
		try {
			LAT_LON_CRS = CRS.decode("EPSG:4326");
			WEB_MERC_CRS = CRS.decode("EPSG:3857");
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
			//Integer epsgCode = CRS.lookupEpsgCode(sourceCRS, true);
			
	        boolean lenient = true; // allow for some error due to different datums

			MathTransform transform = CRS.findMathTransform(sourceCRS, LAT_LON_CRS, lenient);
			convertedSource = transformFeature(sourceShapefile, transform, mapTargets);
		} catch (FactoryException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
        return convertedSource;
        
	}
	
	public static void main(String[] args) {
		try {
			enumFeatures(args[0]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	static int[][] bounds = new int[4][2];
	
	public static void enumFeatures(String vFileName) throws IOException {
		
		//bounds[0][1] = Integer.MIN_VALUE;
		//bounds[1][1] = Double.MAX_VALUE;
		//bounds[2][0] = Double.MIN_VALUE;
		
		//String vFileName = "/home/verdi/verdi-shapefile/VERDI/verdi_bootstrap/data/map_state/cb_2014_us_state_500k.shp";
		java.io.File vFile = new File(vFileName);
		FileDataStore vStore = FileDataStoreFinder.getDataStore(vFile);
		SimpleFeatureSource sourceShapefile = (SimpleFeatureSource)vStore.getFeatureSource();
		
        SimpleFeatureCollection featureCollection = sourceShapefile.getFeatures();
        SimpleFeatureIterator iterator = featureCollection.features();

        while (iterator.hasNext()) {
            SimpleFeature feature = iterator.next();
            List<Object> attrs = feature.getAttributes();
            if (attrs.contains("CT")) {
            	System.out.println("Found CT");
            

	            
	            Geometry geometry = (Geometry) feature.getDefaultGeometry();
	
	            double yMax = 0;
	            int maxIndex = 0;
            	System.err.println("VerdiShapefileLoader Found Connecticut"); //DBG
            	Coordinate[] c1 = geometry.getCoordinates();
            	for (int i = 0; i < c1.length; ++i) {
            		if (c1[i].y > yMax) {
            			yMax = c1[i].y;
            			maxIndex = i;
            		}
            	}
            	System.out.println("Max " + c1[maxIndex].x + ", " + c1[maxIndex].y);
	            
            }

        }

	}
	
	public static SimpleFeatureSource transformFeature(SimpleFeatureSource sourceShapefile, MathTransform transform, boolean mapTargets) {
		SimpleFeatureSource convertedSource = null;
		try {
	        
	        
	        SimpleFeatureCollection featureCollection = sourceShapefile.getFeatures();
	
	        MemoryDataStore dataStore = new MemoryDataStore();
	        SimpleFeatureType featureType = SimpleFeatureTypeBuilder.retype(sourceShapefile.getSchema(), LAT_LON_CRS);
	        dataStore.setNamespaceURI(featureType.getName().getNamespaceURI());
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
	                
	                double yMax = 0;
	                int maxIndex = 0;
	                List<Object> attrs = copy.getAttributes();
	                /*System.out.println("Found " + attrs.size() + " attriutes");
	                for (int i = 0; i < attrs.size(); ++i) {
	                	Object attr = attrs.get(i);
	                	if (attr instanceof MultiLineString) {
	                		MultiLineString str = (MultiLineString)attr;
	                		System.out.println(i + ": attr " + str.getClass().getName() + " points: " + str.getNumPoints()+ " length: " + str.getLength() + " gemometries: " + str.getNumGeometries() + " cooordinates: " + str.getCoordinates().length);
	                	} else if (attr instanceof MultiPolygon) {
	                		MultiPolygon str = (MultiPolygon) attr;
	                		System.out.println(i + ": attr " + str.getClass().getName() + " length: " + str.getLength() + " geometries: " + str.getNumGeometries() + " numPoints: " + str.getNumPoints() + " coordinates: " + str.getCoordinates().length );
	                	} else
	                		System.out.println(i + ": attr " + attrs.get(i).getClass().getName() + " " + attrs.get(i));
	                }*/
	                /*if (copy.getAttributes().contains("CT")) {
	                	System.err.println("Found Connecticut"); //DBG
	                	Coordinate[] c1 = geometry.getCoordinates();
	                	Coordinate[] c2 = geometry2.getCoordinates();
	                	for (int i = 0; i < c1.length; ++i) {
	                		if (c1[i].y != c2[i].y || c1[i].x != c2[i].x)
	                			System.out.println("Found ct diff ");
	                		if (c1[i].y > yMax) {
	                			yMax = c1[i].y;
	                			maxIndex = i;
	                		}
	                	}
	                	System.out.println("Max  " + c1[maxIndex].x + ", " + c1[maxIndex].y);
	                }*/
	               // geometry2 = geometry; //DBG
	
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
    
	static Hashtable<String, String> PROJECTION_MAP = new Hashtable<String, String>();

    static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");
	//targetProjection - NetCDF projection used to create map data
    //targetCRS - CRS that Verdi determines the map to be using
    public static FeatureSource projectShapefile(String filename, SimpleFeatureSource sourceShapefile, Projection targetProjection, CoordinateReferenceSystem targetCRS, boolean mapTargets) {
    	//System.out.println("Projecting " + filename);
    	//long start = System.currentTimeMillis();
    	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " "+ format.format( new Date()) + " " + filename + " to " + targetProjection.getName());
    	SimpleFeatureSource convertedSource = getCachedShapefile(filename, targetProjection, sourceShapefile);
        /*if (convertedSource != null && false) { //DBG
        	//long duration = System.currentTimeMillis() - start;
        	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " " + duration + "ms "+ filename + " cached");
        	return convertedSource;
        }*/
        
       // if (sourceShapefile.getSchema().getCoordinateReferenceSystem().getCoordinateSystem().toString().toLowerCase().indexOf("longitude") == -1) {
        if (sourceShapefile.getSchema().getCoordinateReferenceSystem().toString().indexOf("WGS84") == -1) {
        	//Not in lat/lon, reproject
        	sourceShapefile = projectionToLatLon(sourceShapefile, mapTargets);
        } else if (targetProjection instanceof LatLonProjection && ((LatLonProjection)targetProjection).getCenterLon() == 0 ) {
        	//long duration = System.currentTimeMillis() - start;
        	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " " + duration + "ms "+ filename + " already lat lon");
        	PROJECTION_MAP.put(sourceShapefile.toString(), filename);
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
    	PROJECTION_MAP.put(convertedSource.toString(), filename);

    	return convertedSource;
    }
    
    public static FeatureSource projectObsData(Projection proj, List<ObsData> list, CoordinateReferenceSystem targetCRS) {
    	//long start = System.currentTimeMillis();
    	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " "+ format.format( new Date()) + " " + filename + " to " + targetProjection.getName());
    	/*SimpleFeatureSource convertedSource = getCachedShapefile(filename, targetProjection, sourceShapefile);
        if (convertedSource != null) {
        	//long duration = System.currentTimeMillis() - start;
        	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " " + duration + "ms "+ filename + " cached");
        	return convertedSource;
        }*/
        

        //targetCRS = WEB_MERC_CRS;
        targetCRS = LAT_LON_CRS;
        /*try {
			targetCRS = CRS.decode("EPSG:3785");
		} catch (NoSuchAuthorityCodeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};*/
        MemoryDataStore dataStore = null;
        SimpleFeatureSource convertedSource = null;
        
        
        
        try {
			//TODO - make this a memory object	
			
	        
	        dataStore = new MemoryDataStore();
	        Name name = new NameImpl("http://www.opengis.net/gml", "Location");
	        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
	        builder.setName(name);
	        //builder.setName("Location");
	        builder.setCRS(targetCRS);
	        builder.add("the_geom", Point.class);
	        builder.add("number", Double.class);
	        SimpleFeatureType featureType = builder.buildFeatureType(); //SimpleFeatureTypeBuilder.retype(sourceShapefile.getSchema(), targetCRS);
	        dataStore.createSchema(featureType);
	
	        //Get the name of the new Shapefile, which will be used to open the FeatureWriter
	        String createdName = dataStore.getTypeNames()[0];
	        
	        Transaction transaction = new DefaultTransaction("Reproject");
	        

	        try ( FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
	                        dataStore.getFeatureWriterAppend(createdName, transaction);
	              ){
	        	
		        for (int i = 0; i < list.size(); ++i) {
		        	ObsData data = list.get(i);
		        
		        	//Point point = geometryFactory.createPoint(new Coordinate(data.getLat(), data.getLon())); //Gives IllegalAgumentexxception: Exponent out of bounds
		        	Point point = geometryFactory.createPoint(new Coordinate(data.getLon(), data.getLat()));
		        	//TODO - this was making points vanish, why?
		        	if (proj != null) {
		        		point = projectPoint(point, proj);
		        	}
	                SimpleFeature copy = writer.next();
	                point.setUserData(data.getValue());
	                copy.setDefaultGeometry(point);
	                writer.write();
		        }

	        	
	            transaction.commit();

	            convertedSource = dataStore.getFeatureSource("Location");
	            
	            //convertedSource = projectionToLatLon(convertedSource, false);
	        } catch (Exception e) {
	            e.printStackTrace();
	            transaction.rollback();
	        } finally {
	            transaction.close();
	        }
        } catch (Exception e) {
        	e.printStackTrace();
        }
        //cacheShapefile(filename, targetProjection, convertedSource);
    	//long duration = System.currentTimeMillis() - start;
    	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " " + duration + "ms "+ filename + " projected");
    	return convertedSource;
    }
    
    public static FeatureSource projectObsDataOld(List<ObsData> list, CoordinateReferenceSystem targetCRS) {
    	//long start = System.currentTimeMillis();
    	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " "+ format.format( new Date()) + " " + filename + " to " + targetProjection.getName());
    	/*SimpleFeatureSource convertedSource = getCachedShapefile(filename, targetProjection, sourceShapefile);
        if (convertedSource != null) {
        	//long duration = System.currentTimeMillis() - start;
        	//System.err.println("VerdiShapefileUtil projectShapefile " + Thread.currentThread().getId() + " " + duration + "ms "+ filename + " cached");
        	return convertedSource;
        }*/
        

        
        MemoryDataStore dataStore = null;
        SimpleFeatureSource convertedSource = null;
        
        
        
        try {
			//TODO - make this a memory object	
			
	        
	        dataStore = new MemoryDataStore();
	        SimpleFeatureTypeBuilder builder = new SimpleFeatureTypeBuilder();
	        builder.setName("Location");
	        builder.setCRS(targetCRS);
	        builder.add("the_geom", MultiPoint.class);
	        builder.add("number", Double.class);
	        SimpleFeatureType featureType = builder.buildFeatureType(); //SimpleFeatureTypeBuilder.retype(sourceShapefile.getSchema(), targetCRS);
	        dataStore.createSchema(featureType);
	
	        //Get the name of the new Shapefile, which will be used to open the FeatureWriter
	        String createdName = dataStore.getTypeNames()[0];
	        
	        Transaction transaction = new DefaultTransaction("Reproject");
	        Point[] points = new Point[list.size()];
	        
	        for (int i = 0; i < list.size(); ++i) {
	        	ObsData data = list.get(i);
	        
	        	Point point = geometryFactory.createPoint(new Coordinate(data.getLat(), data.getLon()));
	        	points[i] = point;
	        }
	        try ( FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
	                        dataStore.getFeatureWriterAppend(createdName, transaction);
	              ){
	        	Geometry targetGeometry = geometryFactory.createMultiPoint(points);
                SimpleFeature copy = writer.next();
                copy.setDefaultGeometry(targetGeometry);
                writer.write();
	        	
	            transaction.commit();

	            convertedSource = dataStore.getFeatureSource("Location");
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
        //cacheShapefile(filename, targetProjection, convertedSource);
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
        	//latLon.set(73.487559, 42.049);
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

    private static Point projectPoint(Point sourceGeometry, Projection proj) {
    	if (proj == null)
    		return sourceGeometry;
    	double factor = 1000;
    	if (proj instanceof LatLonProjection)
    		factor = 1;
    	Point targetPolygon = null;
    	//valid polygons must end with the same coordinate that they start with.  Add if not present.

    	Coordinate[] sourceCoordinates = sourceGeometry.getCoordinates();

        Coordinate[] targetCoordinates = new Coordinate[sourceCoordinates.length];
        LatLonPointImpl latLon = new LatLonPointImpl();
        ProjectionPointImpl xy = new ProjectionPointImpl();
        
    	
        for (int i = 0; i < sourceCoordinates.length; ++i) {
        	Coordinate source = sourceCoordinates[i];
        	latLon.set(source.y, source.x);
        	//latLon.set(73.487559, 42.049);
        	proj.latLonToProj(latLon, xy);
        	targetCoordinates[i] = new Coordinate(xy.getX() * factor, xy.getY() * factor);
        	//targetCoordinates[i] = new Coordinate(-16711049.005249446, 9000579.46370098); // - from map file, not shown onscreen
        	//targetCoordinates[i] = new Coordinate(-109.0448, 37.0004); // Works - whatever renders is looking for lat/lon, not native
        	
        }
        
        try {
        	targetPolygon = geometryFactory.createPoint(targetCoordinates[0]);
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return targetPolygon;   	
    }


}
