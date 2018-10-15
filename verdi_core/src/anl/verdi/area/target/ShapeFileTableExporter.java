package anl.verdi.area.target;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;

import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import anl.verdi.area.AreaDataFrameTableModel;
import anl.verdi.data.Variable;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Exports the data from a JTable into a shp file.
 *
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 */
public class ShapeFileTableExporter {
	
	static final Logger Logger = LogManager.getLogger(ShapeFileTableExporter.class.getName());

	private JTable table;

	public ShapeFileTableExporter(JTable myTable) {
		this.table = myTable;
	}

	/**
	 * Run the exporter. This will display a file
	 * chooser and save the table data in the
	 * selected file.
	 *
	 * @throws java.io.IOException if there is an error while creating
	 *                             the image or saving the plot.
	 */
	public void run() throws IOException {
		//save("C:\\testOutput\\test");
		JFileChooser chooser = new JFileChooser();
		chooser.setFileFilter(new FileFilter() {

			public String getDescription() {
				return "Shape Files (*.shp)";
			}

			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				String ext = findExtension(f);
				return ext != null && (ext.toLowerCase().equals("shp"));
			}
		});

		int res = chooser.showSaveDialog(table);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			save(file.getAbsolutePath());
		}

	}

	private void save(String fileName) throws IOException {
		String baseName = new String(fileName);
		if (fileName.endsWith(".shp")) {
			baseName = fileName.substring(0, fileName.length() - 4);	// better to use File methods
		}

		// get list of areas to save
		AreaDataFrameTableModel model = (AreaDataFrameTableModel)table.getModel();
		ArrayList<Target> targets = model.getAreas();

		// get list of variables for shapefile
		Variable[] vars = model.getVariables();
		
		// create the feature type
		SimpleFeatureTypeBuilder typeBuilder = new SimpleFeatureTypeBuilder();
		typeBuilder.setName("Area Data Type");
		typeBuilder.setCRS(targets.get(0).sourceData.projInfo);
		
		typeBuilder.add("the_geom", MultiPolygon.class);
		typeBuilder.add("GeoName", String.class);
		typeBuilder.add("GeoLayer", String.class);
		typeBuilder.add("Area", Double.class);
		
		for (int i = 0; i < vars.length; i++) {
			typeBuilder.add(vars[i].getName(), Double.class);
			typeBuilder.add(vars[i].getName() + "_A", Double.class);
		}
		
		final SimpleFeatureType AREA_TYPE = typeBuilder.buildFeatureType();

		// for each area, create a feature and add it to the collection
		DefaultFeatureCollection collection = new DefaultFeatureCollection();
		SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(AREA_TYPE);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory(null);
		
		for (int count = 0; count < targets.size(); count++) {
			Target target = targets.get(count);
			
			Geometry featureGeometry = target.getDataObject();
			Geometry multiPolygon = featureGeometry;
			if (featureGeometry instanceof Polygon) {
				multiPolygon = geometryFactory.createMultiPolygon(new Polygon[] { (Polygon)featureGeometry });
			}
			featureBuilder.add(multiPolygon);
			featureBuilder.add(target.toString());
			featureBuilder.add(target.getSource().getName());
			featureBuilder.add(target.area);
			for (int i = 0; i < vars.length; i++) {
				// calculate column number for this variable, skipping the name and 
				// area columns; this assumes that the user hasn't rearranged the
				// columns
				int colNum = 2 + (i * 2);
				featureBuilder.add(model.getValueAt(count, colNum));
				featureBuilder.add(model.getValueAt(count, colNum + 1));
			}
			SimpleFeature feature = featureBuilder.buildFeature(target.toString());
			collection.add(feature);
		}
		
		// create the shapefile data store to write the shapefiles
		File outFile = new File(baseName + ".shp");
		ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();
		
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		params.put("url", outFile.toURI().toURL());
		params.put("create spatial index", Boolean.TRUE);

		ShapefileDataStore newDataStore = (ShapefileDataStore)dataStoreFactory.createNewDataStore(params);
		newDataStore.createSchema(AREA_TYPE);

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
	
	private String findExtension(File f)
	{
		String fileName = f.toString();
		String ext = org.apache.commons.io.FilenameUtils.getExtension(fileName);
		return ext;
	}

}
