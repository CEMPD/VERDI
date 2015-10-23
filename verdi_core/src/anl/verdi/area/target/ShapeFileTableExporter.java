package anl.verdi.area.target;

// 2014 disabled shapefile export in VERDI 1.5.0
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.util.ArrayList;
//import java.util.List;
//
//import javax.swing.JFileChooser;
//import javax.swing.JTable;
//import javax.swing.filechooser.FileFilter;
//import javax.swing.table.TableColumnModel;
//
//import org.geotools.data.DataUtilities;
//import org.geotools.data.FeatureWriter;
//import org.geotools.data.Transaction;
//import org.geotools.data.shapefile.ShapefileDataStore;
////import org.geotools.data.FeatureSource;	// was an Interface; GeoTools v10 moved to org.opengis.feature.*
////import org.geotools.data.FeatureWriter;
//import org.geotools.data.store.ContentFeatureSource;
//import org.geotools.feature.simple.SimpleFeatureBuilder;
//import org.geotools.feature.FeatureCollection;		// opengis -> geotools
//import org.opengis.feature.simple.SimpleFeature;
//import org.opengis.feature.simple.SimpleFeatureType;
//
//import anl.verdi.area.AreaDataFrameTableModel;
//import anl.verdi.area.AreaFile;
//import anl.verdi.area.target.Target.SourceData;
//import anl.verdi.data.Variable;
//
//import com.vividsolutions.jts.geom.Geometry;
//import com.vividsolutions.jts.geom.GeometryFactory;
//import com.vividsolutions.jts.geom.Polygon;
//// NOTE: needed to put full library name qualifier on function (below)
//
///**
// * Exports the data from a JTable into a shp file.
// *
// * @author Mary Ann Bitz
// * @version $Revision$ $Date$
// */
//public class ShapeFileTableExporter {
//
//	private JTable table;
////	private String title, rangeAxisName;
//	private boolean exportHeader=false;
//
//	public ShapeFileTableExporter(JTable myTable, String myTitle, String myRangeAxisName) {
//		this.table = myTable;
////		this.title = myTitle;
////		this.rangeAxisName = myRangeAxisName;
//
//	}
//
//	/**
//	 * Run the exporter. This will display a file
//	 * chooser and save the table data in the
//	 * selected file.
//	 *
//	 * @throws java.io.IOException if there is an error while creating
//	 *                             the image or saving the plot.
//	 */
//	public void run() throws IOException {
//		//save("C:\\testOutput\\test");
//		JFileChooser chooser = new JFileChooser();
//		chooser.setFileFilter(new FileFilter() {
//
//			public String getDescription() {
//				return "Shape Files (*.shp)";
//			}
//
//			public boolean accept(File f) {
//				if (f.isDirectory()) return true;
//				String ext = findExtension(f);
//				return ext != null && (ext.toLowerCase().equals("shp"));
//			}
//		});
//
//		int res = chooser.showSaveDialog(table);
//		if (res == JFileChooser.APPROVE_OPTION) {
//			File file = chooser.getSelectedFile();
//			save(file.getAbsolutePath());
//		}
//
//	}
//
////	private void save(String baseName) throws IOException {
//	private void save(String fileName) throws IOException {
//		String baseName = new String(fileName);
////		if(baseName.endsWith(".shp")){
////			baseName=baseName.substring(0,baseName.length()-4);
//		if(fileName.endsWith(".shp")){
//			baseName=fileName.substring(0,fileName.length()-4);	// better to use File methods
//		}
//
//		AreaDataFrameTableModel model = (AreaDataFrameTableModel)table.getModel();
//		ArrayList targets = Target.getSelectedTargets();
//		targets= model.getAreas();
//
//		File outFile = new File(baseName+".shp");
//
//		// grab the first one
//		ArrayList sourceFiles = Target.getSources();
//		if(sourceFiles.isEmpty())return;
//		AreaFile data = (AreaFile)sourceFiles.get(0);
//		URL shpFileURL=data.getURL();
//		ShapefileDataStore ds = new ShapefileDataStore(shpFileURL);
//		ContentFeatureSource fs = ds.getFeatureSource();
//		FeatureCollection fc = (FeatureCollection) fs.getFeatures();
//
//		TableColumnModel columnModel=table.getTableHeader().getColumnModel();
//		int colNum = columnModel.getColumnCount();
//		Variable[] vars = model.getVariables();
//
//
//		// create the output shapefile data store
//		try {
//			GeometryFactory geomFactory = new GeometryFactory();
//			// NOTE: better to replace with StringBuilder
//			String fieldsSpec = "geom:MultiPolygon,GeoName:String,GeoLayer:String,Area:double";
//			for(int i=0;i<vars.length;i++){
//				fieldsSpec=fieldsSpec+","+vars[i].getName()+":double";
//				fieldsSpec=fieldsSpec+","+vars[i].getName()+"_A"+":double";
//			}
////			FeatureType featureType = DataUtilities.createType("target", fieldsSpec); 
//			SimpleFeatureType featureType = DataUtilities.createType("target", fieldsSpec); 
////			ShapefileDataStore outStore = new ShapefileDataStore(outFile.toURL());	// documentation says deprecated because does not do conversion correctly
//			ShapefileDataStore outStore = new ShapefileDataStore(outFile.toURI().toURL());
//			outStore.createSchema( (SimpleFeatureType) featureType );
//			featureType=outStore.getSchema();
//
//
//			FeatureWriter outFeatureWriter = outStore.getFeatureWriter(outStore.getTypeNames()[0], Transaction.AUTO_COMMIT);
//
//
////			Object[] att = null;
//			List att = null;
//			int count = 0;
//			// loop through all of the targets
//			for(Target target:(ArrayList<Target>)targets){
//
//
//				// Extract geometry
//				Geometry featureGeometry = target.getDataObject();
//
//				Geometry newGeom=featureGeometry;
//				// The original schema is expecting the Geometry type to be MultiPolygon,
//				// but the clip method sometimes returns the type Polygon.  If that is the
//				// case, convert to MultiPolygon
//				if (featureGeometry instanceof Polygon) {             
//					newGeom = geomFactory.createMultiPolygon(new Polygon[] { (Polygon)newGeom });
//				}
//
//				// create a feature for the geometry
//				Object[] attributes=new Object[colNum+2];
//				attributes[0]=newGeom;
//				attributes[1]=target.toString();
//				attributes[2]=target.getSource().getName();
//				attributes[3]=target.area;
//				for(int i=2;i<colNum;i++){
//					attributes[i+2]=model.getValueAt(count,i);
//				}
//
////				Feature feature = featureType.create(attributes,target.toString());
//				SimpleFeature aSimpleFeature = SimpleFeatureBuilder.build(featureType, attributes, target.toString());
//
//				SimpleFeature writeFeature = (SimpleFeature) outFeatureWriter.next();		// returns a Feature
//
////				att=feature.getAttributes(att);			// returns List but att is array []
//				att = aSimpleFeature.getAttributes();	
////				for(int i=0;i<att.length;i++){
////					writeFeature.setAttribute(i,att[i]);
//				int attSize = att.size();
//				
//				for(int i=0; i<attSize; i++){
//					Object anObject = att.get(i);
//					writeFeature.setValue(anObject);	//.setValue(i, anObject); 2014
//				}
//
//				outFeatureWriter.write();
//
//				count++;
//			}
//
//			// close the files
//			outFeatureWriter.close();
//
//			ProjectionInfo.writePRJFile(baseName+".prj",((SourceData)data).projInfo,true);
//			String projString = ((SourceData)data).projInfo.toWKT();
//			projString=projString.replaceAll("\\r","").replaceAll("\\n","");
//			//outStore.forceSchemaCRS(((SourceData)data).projInfo);
//		}catch(Exception e){
//			e.printStackTrace(); 
//		} finally {
//
//		}
//
//	}
//
//
////	private String findExtension(File f) {
////		String ext = null;
////		String s = f.getName();
////		int i = s.lastIndexOf('.');
////
////		if (i > 0 && i < s.length() - 1) {
////			ext = s.substring(i + 1).toLowerCase();
////		}
////		return ext;
////	}
//	
//	private String findExtension(File f)		// 2014 implemented library method
//	{
//		String fileName = f.toString();
//		String ext = org.apache.commons.io.FilenameUtils.getExtension(fileName);
//		return ext;
//	}
//
//	public boolean isExportHeader() {
//		return exportHeader;
//	}
//
//	public void setExportHeader(boolean exportHeader) {
//		this.exportHeader = exportHeader;
//	}
//
//
//}
