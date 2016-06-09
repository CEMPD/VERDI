
/******************************************************************************
PURPOSE: GridShapefileWriter.java - Write 2D grid cells and data as 2D polygon
         Shapefiles (creates .shp, .shx and .dbf).
NOTES:   See 1998 ESRI Shapefile Specification pages 2, 4, 5, 16, 23, 24.
         http://www.esri.com/library/whitepapers/pdfs/shapefile.pdf
         http://www.clicketyclick.dk/databases/xbase/format/dbf.html#DBF_STRUCT
HISTORY: 2010-08-23 plessel.todd@epa.gov Created.
******************************************************************************/

package gov.epa.emvl;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.IOException;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;

import anl.verdi.area.target.Target;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages


public final class GridShapefileWriter {
	static final Logger Logger = LogManager.getLogger(GridShapefileWriter.class.getName());

	private GridShapefileWriter() {} // Non-instantiable.

  /**
   * write - Write a single layer of grid cells and a
   * single timestep of scalar data as Shapefile Polygon files
   * (shp, shx, dbf).
   * INPUTS:
   * final String fileName  Base name of file to create. "example".
   * final int rows              Number of grid rows.
   * final int columns           Number of grid columns.
   * final double westEdge       Distance from origin to west edge of grid.
   * final double southEdge      Distance from origin to south edge of ".
   * final double cellWidth      Width of each grid cell (e.g., 12000 m).
   * final double cellWHeight    Height of each grid cell (e.g., 12000 m).
   * final String variable       Name of data variable.
   * final double[ rows ][ columns ] data  Scalar data at grid cell centers.
   * final CoordinateReferenceSystem gridCRS   Gridded data projection.
   * OUTPUTS:
   * fileName.shp  Contains the grid cell polygons.
   * fileName.shx  Index file for the above.
   * fileName.dbf  Contains the data as a single-column table.
   * fileName.prj  Projection definition.
   * CONTRACT:
   * @throws IOException 
   * @pre fileName != null
   * @pre rows > 0
   * @pre columns > 0
   * @pre ! Numerics.isNan( westEdge )
   * @pre ! Numerics.isNan( southEdge )
   * @pre ! Numerics.isNan( cellWidth )
   * @pre ! Numerics.isNan( cellHeight )
   * @pre cellWidth > 0.0
   * @pre cellHeight > 0.0
   * @pre gridCRS != null
   * @pre ( variable != null ) == ( data != null )
   */

	public static void write( final String fileName,
                            final int rows,
                            final int columns,
                            final double westEdge,
                            final double southEdge,
                            final double cellWidth,
                            final double cellHeight,
                            final String variable,
                            final float[][] data,
                            final CoordinateReferenceSystem gridCRS ) throws IOException {

		// create the feature type
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName("Gridded Data Type");
		typeBuilder.setCRS(gridCRS);
		
		typeBuilder.add("the_geom", Polygon.class);
		typeBuilder.add(variable, Float.class);
		
		final SimpleFeatureType GRID_TYPE = typeBuilder.buildFeatureType();
		
		// for each grid cell, create a feature and add it to the collection
		DefaultFeatureCollection collection = new DefaultFeatureCollection();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(GRID_TYPE);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
		
		for (int row = 0; row < rows; row++) {
			for (int col = 0; col < columns; col++) {
				double minX = westEdge + col * cellWidth;
				double minY = southEdge + row * cellHeight;
				double maxX = minX + cellWidth;
				double maxY = minY + cellHeight;
				Coordinate[] coords = new Coordinate[] {
						new Coordinate(minX, minY),
						new Coordinate(minX, maxY),
						new Coordinate(maxX, maxY),
						new Coordinate(maxX, minY),
						new Coordinate(minX, minY)
				};
				Polygon polygon = geometryFactory.createPolygon(coords);
				featureBuilder.add(polygon);
				featureBuilder.add(data[row][col]);
				SimpleFeature feature = featureBuilder.buildFeature(null);
				collection.add(feature);
			}
		}
		
		// create the shapefile data store to write the shapefiles
		File file = new File(fileName + ".shp");
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", file.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
		newDataStore.createSchema(GRID_TYPE);

		// write the feature collection to the shapefile in a single transaction
		Transaction transaction = new DefaultTransaction("create");
		String typeName = newDataStore.getTypeNames()[0];
		SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

		if (featureSource instanceof SimpleFeatureStore) {
			SimpleFeatureStore featureStore = (SimpleFeatureStore)featureSource;

			featureStore.setTransaction(transaction);
			try {
				featureStore.addFeatures(collection);
				transaction.commit();

			} catch (Exception problem) {
				transaction.rollback();
				Logger.error("Error while writing shapefile: " + problem.getMessage());

			} finally {
				transaction.close();
			}
		} else {
			Logger.error("Error while writing shapefile: " + typeName + " does not support read/write access");
		}
	}
	

	  /**
	   * write - Write a single layer of grid cells and a
	   * single timestep of scalar data as Shapefile Polygon files
	   * (shp, shx, dbf).
	   * INPUTS:
	   * final String fileName  Base name of file to create. "example".
	   * final String variable		 Name of the data variable
	   * List<Polygon> areas         List of polygons to write.
	   * List<Float> data            Numeric values to write.
	   * final CoordinateReferenceSystem gridCRS   Gridded data projection.
	   * OUTPUTS:
	   * fileName.shp  Contains the grid cell polygons.
	   * fileName.shx  Index file for the above.
	   * fileName.dbf  Contains the data as a single-column table.
	   * fileName.prj  Projection definition.
	   * CONTRACT:
	   * @throws IOException 
	   * @pre fileName != null
	   * @pre areas != null
	   * @pre gridCRS != null
	   * @pre ( variable != null ) == ( data != null )
	   */

		public static void write( final String fileName,
	                            final String variable,
	                            List<Polygon> areas,
	                            List<Float> data,
	                            final CoordinateReferenceSystem gridCRS ) throws IOException {

			// create the feature type
			SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
			typeBuilder.setName("Gridded Data Type");
			typeBuilder.setCRS(gridCRS);
			
			typeBuilder.add("the_geom", Polygon.class);
			typeBuilder.add(variable, Float.class);
			
			final SimpleFeatureType GRID_TYPE = typeBuilder.buildFeatureType();
			
			// for each grid cell, create a feature and add it to the collection
			DefaultFeatureCollection collection = new DefaultFeatureCollection();
			SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(GRID_TYPE);
			
			for (int i = 0; i < data.size(); ++i) {
				Polygon polygon = areas.get(i);
				featureBuilder.add(polygon);
				featureBuilder.add(data.get(i));
				SimpleFeature feature = featureBuilder.buildFeature(null);
				collection.add(feature);

			}
			
			// create the shapefile data store to write the shapefiles
			File file = new File(fileName + ".shp");
			ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
			
			Map<String, Serializable> params = new HashMap<String, Serializable>();
			params.put("url", file.toURI().toURL());
			params.put("create spatial index", Boolean.TRUE);

			ShapefileDataStore newDataStore = (ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
			newDataStore.createSchema(GRID_TYPE);

			// write the feature collection to the shapefile in a single transaction
			Transaction transaction = new DefaultTransaction("create");
			String typeName = newDataStore.getTypeNames()[0];
			SimpleFeatureSource featureSource = newDataStore.getFeatureSource(typeName);

			if (featureSource instanceof SimpleFeatureStore) {
				SimpleFeatureStore featureStore = (SimpleFeatureStore)featureSource;

				featureStore.setTransaction(transaction);
				try {
					featureStore.addFeatures(collection);
					transaction.commit();

				} catch (Exception problem) {
					transaction.rollback();
					Logger.error("Error while writing shapefile: " + problem.getMessage());

				} finally {
					transaction.close();
				}
			} else {
				Logger.error("Error while writing shapefile: " + typeName + " does not support read/write access");
			}
		}

	
}
