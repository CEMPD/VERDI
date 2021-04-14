package anl.verdi.gis;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.Query;
import org.geotools.data.Transaction;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.filter.LiteralExpressionImpl;
import org.geotools.map.FeatureLayer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.PolygonSymbolizerImpl;
import org.geotools.styling.Rule;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.pietschy.wizard.InvalidStateException;
import org.pietschy.wizard.PanelWizardStep;
import org.pietschy.wizard.WizardModel;

import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
//import org.geotools.data.DefaultQuery;
//import org.geotools.filter.Expression;
//import org.geotools.filter.Filter;
//import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;
//import org.geotools.map.DefaultMapLayer;
//import org.geotools.map.MapLayer;	// GeoTools deprecated the MapLayer class; need to use FeatureLayer, GridCoverageLayer, or GridReaderLayer
/**
 * @author Nick Collier
 * @version $Revision: 1.5 $ $Date: 2007/04/30 17:43:53 $
 */
public class FileSelectionStep extends PanelWizardStep {

	private static final long serialVersionUID = -2143961301487078170L;
	static final Logger Logger = LogManager.getLogger(FileSelectionStep.class.getName());

	private JFileChooser chooser;

	private AddLayerWizardModel model;

	public FileSelectionStep() {
		super("Select Shapefile",
				"Please select the shapefile to import into the base map");
		setLayout(new BorderLayout());
		chooser = new JFileChooser(".");
		chooser.setFileFilter(new FileFilter() {
			public boolean accept(File pathname) {
				return pathname.getName().endsWith(".shp")
						|| pathname.isDirectory();
			}

			public String getDescription() {
				return "Shapefile files";
			}
		});

		chooser.setFileSelectionMode(JFileChooser.OPEN_DIALOG);
		chooser.setControlButtonsAreShown(false);
		chooser.addPropertyChangeListener(
				JFileChooser.SELECTED_FILE_CHANGED_PROPERTY,
				new PropertyChangeListener() {

					public void propertyChange(PropertyChangeEvent evt) {
						setComplete(chooser.getSelectedFile() != null);
					}
				});
		this.add(chooser, BorderLayout.CENTER);
	}

	@Override
	public void init(WizardModel wizardModel) {
		model = (AddLayerWizardModel) wizardModel;
	}

	@Override
	public void prepare() {
		if (model.getShpFile() != null)
			chooser.setSelectedFile(model.getShpFile());
	}

	@Override
	public void applyState() throws InvalidStateException {
		File shpFile = chooser.getSelectedFile();
		File modelShpFile = model.getShpFile();
		if (modelShpFile == null
				|| (modelShpFile != null && !shpFile.equals(modelShpFile))) {
			// create a new default map layer from the shape file.
			try {
//				URL url = shpFile.toURL();
				URL url = org.geotools.util.URLs.fileToUrl(shpFile);
				//URL url = DataUtilities.fileToURL(shpFile);
				Map<String, Serializable> params = new HashMap<String, Serializable>();
//				params.put(IndexedShapefileDataStoreFactory.URLP.key, url);
//				params.put(
//								IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,
//								true);
//				params.put(IndexedShapefileDataStoreFactory.URLP.key, url);
				params.put(ShapefileDataStoreFactory.URLP.key, url);
				params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,true);
				params.put("wkb enabled", "true");
				params.put("loose bbox", "true");
//				IndexedShapefileDataStoreFactory dsfac = new IndexedShapefileDataStoreFactory();
				ShapefileDataStoreFactory dsfac = new ShapefileDataStoreFactory();
				DataStore datastore = dsfac.createDataStore(params);
//				datastore.getFeatureReader(new DefaultQuery(datastore
				datastore.getFeatureReader(new Query(datastore.getTypeNames()[0], Filter.INCLUDE, new String[0]),	// replaced Filter.NONE with Filter.INCLUDE (don't filter any out)
						Transaction.AUTO_COMMIT);
				SimpleFeatureSource fc = datastore.getFeatureSource(datastore.getTypeNames()[0]);
//				FeatureSource fc = datastore.getFeatureSource(datastore.getTypeNames()[0]);
//				Class geomtype = fc.getSchema().getDefaultGeometry().getType();
				AttributeDescriptor ad = fc.getSchema().getGeometryDescriptor();		// .getDefaultGeometry();
//				AttributeType at = ad.getType();	// 2014 getType() shows in source code but not found by this program; apparently at not used
				Class geomtype = ad.getClass();
				
				StyleBuilder builder = new StyleBuilder();
				Style style = null;
				if (geomtype.equals(com.vividsolutions.jts.geom.Point.class)
						|| geomtype.equals(MultiPoint.class)) {
					style = builder.createStyle(builder
							.createPointSymbolizer(builder.createGraphic(null,
									builder.createMark("square"), null)));
				} else if (geomtype.equals(LineString.class)
						|| geomtype.equals(MultiLineString.class)) {
					style = builder.createStyle(builder.createLineSymbolizer());
				} else {
					style = builder.createStyle(builder.createPolygonSymbolizer());
				//	Symbolizer smbl = style.getFeatureTypeStyles()[0].getRules()[0].getSymbolizers()[0];	// getFeatureTypeStyles()[0] => featureTypeStyles().toArray(new FeatureTypeStyle[0])
																	// getRules() etc. => rules() etc.
					Symbolizer smbl = style.featureTypeStyles().toArray(new FeatureTypeStyle[0])[0].rules().toArray(new Rule[0])[0].getSymbolizers()[0];
					Expression fill = ((PolygonSymbolizerImpl)smbl).getFill().getOpacity();
					((LiteralExpressionImpl)fill).setValue(0);
					Expression stroke = ((PolygonSymbolizerImpl)smbl).getStroke().getOpacity();
					((LiteralExpressionImpl)stroke).setValue(1);
				}

				FeatureLayer layer = new FeatureLayer(fc, style);
				model.setLayer(layer);
				model.setShpFile(shpFile);

			} catch (MalformedURLException e) {
				Logger.error("Malformed URL Exception creating layer from shapefile " + e.getMessage());
			} catch (IOException e) {
				Logger.error("IOException creating layer from shapefile " + e.getMessage());
			}
		}
	}
}
