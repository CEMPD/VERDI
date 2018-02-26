package anl.verdi.area.target;

/******************************************************/
// Developed by Mary Ann Bitz                      
// Argonne National Laboratory                        
// Date: March 2004                               

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

//import javax.measure.converters.UnitConverter;
//import javax.measure.converter.UnitConverter;
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.eclipse.uomo.units.AbstractConverter;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.FeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
//import org.geotools.feature.DefaultFeature; 	// DefaultFeature no longer available, old document said to use
//	SimpleFeature instead
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.unitsofmeasurement.unit.Unit;
import org.unitsofmeasurement.unit.UnitConverter;

import anl.verdi.area.Area;
import anl.verdi.area.AreaFile;
import anl.verdi.area.Units;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.MeshCellInfo;
import anl.verdi.data.MeshDataReader;
import anl.verdi.plot.gui.VerdiStyle;
import anl.verdi.util.VUnits;
import gov.epa.emvl.TilePlot;
import ucar.unidata.geoloc.Projection;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * 
 * File Name:Target.java
 * Description:
 * This object stores a polygon used to show hydrological regions (watersheds).
 * These targets can be selected or organized into sets.
 * 
 * @version March, 2004
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 */
public class Target implements Area{
	static final Logger Logger = LogManager.getLogger(Target.class.getName());

	Geometry dataObject;

	Map<Integer, int[]> cellIndex;
	Map<Integer, int[]> rowIndex;
	Map<Integer, int[]> colIndex;
	Map<Integer, float[]> cellOverlapArea;
	Map<Integer, float[]> overlapArea;
	static ArrayList <TilePlot> plots=new ArrayList<TilePlot>();
	Color color;
	ArrayList<Float> deposition;
	double area;
	String keyName;
	String fullName;
	//String source;
	SourceData sourceData;
	public static boolean useFixedWidth=false;
	public final static String NAME="Watershed Segment";
	static TilePlot currentTilePlot;
	public static int currentGridNum=-1;
	public static UnitConverter converterGrid=null;
	public static UnitConverter converterTargetStandard=null;
	public static UnitConverter converterTargetGrid=null;
	
	float currentDeposition = 0;
	
	private static Map<Geometry, Target> geometryMap = new HashMap<Geometry, Target>();

	public void setAreaInfo(int gridNum,int[] cellIndex, float[] overlapArea){
		this.cellIndex.put(gridNum,cellIndex);
		this.cellOverlapArea.put(gridNum,overlapArea);
	}
	public void setAreaInfo(int gridNum,int[] rowIndex,int[] colIndex, float[] overlapArea){
		this.rowIndex.put(gridNum,rowIndex);
		this.colIndex.put(gridNum,colIndex);
		this.overlapArea.put(gridNum,overlapArea);
	}
	// map of source files and if their indexes have been loaded
	protected static class SourceData implements AreaFile{
		String fileName;
		String nameField;
		CoordinateReferenceSystem projInfo;
		boolean indexed;
		SourceData(String n,String name,CoordinateReferenceSystem p,boolean index){
			fileName=n;
			nameField=name;
			projInfo=p;
			indexed=index;
		}
		@Override
		public void close() throws IOException {
			// TODO Auto-generated method stub

		}
		@Override
		public String getAlias() {
			// TODO Auto-generated method stub
			return null;
		}
		@Override
		public List<String> getAreaNames() {
			// TODO Auto-generated method stub
			List<Area> areas = getAreas();
			ArrayList<String> names = new ArrayList<String>();
			for(Area area:areas){
				names.add(area.getName());
			}
			return names;
		}
		@Override
		public List<Area> getAreas() {
			return Target.getTargetsForSource(this);
		}
		@Override
		public int getIndexInURL() {
			// TODO Auto-generated method stub
			return 0;
		}
		@Override
		public String getName() {
			return nameField;
		}
		@Override
		public URL getURL() {
			File file = new File(fileName);
			try{
//				return file.toURL();	// file.toURL() is deprecated; Java 7 says to file => URI => URL
				return file.toURI().toURL();
			}catch(Exception e){
				return null;
			}
		}
		@Override
		public void setAlias(String alias) {
			// TODO Auto-generated method stub

		}
	}
	
	public Geometry getGeometry(Projection projection, CoordinateReferenceSystem crs) {
		return dataObject;
	}
	
	static HashMap<String,SourceData> sourceMap = new HashMap();
	static HashMap<String,VerdiStyle> styleMap = new HashMap();

	static ArrayList targets = new ArrayList();
	static ArrayList selectedTargets = new ArrayList();
	static ArrayList selectedTargetsAndSets = new ArrayList();
	static HashMap targetMap = new HashMap();
	/** name of the target GeoLayer */

	public static String targetLayer = "Watershed Segments";
	/** name of the GeoLayer holding selected targets */
	public static String selectedTargetLayer = "Selected Watershed Segments";
	/** name of the temporary GeoLayer used to load objects */
	public static String loadLayer = "Load Objects";

	/**
	 * Constructor for creating a target
	 * @param obj the object that draws the polygon
	 * @param nameString the name of the target (HUC code)
	 * @param sourceFile the file the target was originally within
	 */
	Target(Geometry obj, String nameString, SourceData source) {
		dataObject = obj;
		keyName = nameString;
		//fullName=RegionNames.getRegionName(keyName);
		sourceData = source;
		area = 0;
		colIndex=new HashMap<Integer, int[]>();
		rowIndex=new HashMap<Integer, int[]>();
		cellIndex = rowIndex;
		deposition=new ArrayList<Float>();
		cellOverlapArea=new HashMap<Integer, float[]>();
		overlapArea=new HashMap<Integer, float[]>();
  		if(obj instanceof MultiPolygon){
  			for(int i=0;i<((MultiPolygon)obj).getNumGeometries();i++){
  				Geometry geo=((MultiPolygon)obj).getGeometryN(i);
  				geometryMap.put(geo, this);
  			}
  		} else
  			geometryMap.put(obj, this);
	}
	public boolean areaCalculatedForGrid(int num){
		return rowIndex.containsKey(num);
	}

	/**
	 * Update the list of selected targets in case one is deleted
	 *
	 */
	public static void syncSelectedTargetSets() {
		ArrayList targetSets = TargetSet.getTargetSets();
		ArrayList adjustedTargets = (ArrayList)selectedTargetsAndSets.clone();
		// remove any selected target sets that are no longer in list
		for (int i = 0; i < selectedTargetsAndSets.size(); i++) {
			Object obj = selectedTargetsAndSets.get(i);

			if (obj instanceof TargetSet) {
				if (!targetSets.contains(obj))
					adjustedTargets.remove(obj);
			}
		}
		setSelectedTargetsAndSets(adjustedTargets);
		setSelectedTargets(TargetSet.includedTargets(adjustedTargets));
	}
	/**
	 * Load a bunch of polygons in from a file
	 * @param fileName the filename
	 * @param nameField the name of the field used for the id
	 * @param projString the projection the data is in
	 * @param redrawIt whether or not a redraw should be done
	 */

	public static void loadData(File file, String nameField, CoordinateReferenceSystem proj,boolean redrawIt) {
		try {
			// Connection parameters
			Map<String,Serializable>
			connectParameters = new HashMap<String,Serializable>();

			connectParameters.put("url", file.toURI().toURL());
			connectParameters.put("create spatial index", true );

			DataStore dataStore = DataStoreFinder.getDataStore(connectParameters);
			if(dataStore==null)return;

			// we are now connected
			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];

//			Logger.debug("Reading content " + typeName);

			FeatureSource featureSource;
			FeatureCollection collection;
			FeatureIterator iterator;

			featureSource = dataStore.getFeatureSource(typeName);
			collection = featureSource.getFeatures();
			iterator = collection.features();

			int totalLength=0;
			// make the sourceData object for this target file
			SourceData sourceData = new SourceData(file.getAbsolutePath(),file.getName(),proj,false);
			String baseName=file.getCanonicalPath();
			baseName=baseName.substring(0, baseName.length()-4);
			// write projection file if it does not exist
			ProjectionInfo.writePRJFile(baseName+".prj",proj,false);

			sourceMap.put(file.getAbsolutePath(),sourceData);
			styleMap.put(file.getAbsolutePath(), new VerdiStyle(file));
			try {
				while (iterator.hasNext()) {
//					DefaultFeature feature = (DefaultFeature)iterator.next();
					SimpleFeatureImpl feature = (SimpleFeatureImpl)iterator.next();
					Object id=feature.getAttribute(nameField);
					String name=null;
					if(id!=null)name=id.toString();
					Geometry geometry = (Geometry) feature.getDefaultGeometry();
					if(!((geometry instanceof MultiPolygon)||(geometry instanceof com.vividsolutions.jts.geom.Polygon))){
						// ignore this object
						continue;
					}else{
						// make target for it
//						com.vividsolutions.jts.geom.Polygon polygon =null;
//						if(geometry instanceof MultiPolygon){
//							int dim=((MultiPolygon)geometry).getNumGeometries();
//							//if(dim>1)continue;
//							polygon=(com.vividsolutions.jts.geom.Polygon)((MultiPolygon)geometry).getGeometryN(0);
//							//Logger.debug("polygon found\n");
//						}
						Target data = new Target(geometry, name, sourceData);
						geometry.setUserData(data);
						targets.add(data);
						targetMap.put(data.keyName, data);
					}
					totalLength ++;

				}

			}
			finally {
				if( iterator != null ){
					// YOU MUST CLOSE THE ITERATOR!
					iterator.close();
				}
			}
			Logger.debug("Total Objects " + totalLength);

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}



	}
	public static void removeSource(AreaFile source){
		if(source instanceof SourceData) {
			sourceMap.remove(((SourceData)source).fileName);
			styleMap.remove(((SourceData)source).fileName);
		}
	}
	public static CoordinateReferenceSystem loadProjectionInfo(String fileName) {
//		FeatureIterator iterator=null;
		try {
			File file = new File(fileName);

			// Connection parameters
			Map<String,Serializable>
			connectParameters = new HashMap<String,Serializable>();

			connectParameters.put("url", file.toURI().toURL());
			connectParameters.put("create spatial index", true );
			DataStore dataStore = DataStoreFinder.getDataStore(connectParameters);
			if(dataStore==null)
				return null;

			// we are now connected
			String[] typeNames = dataStore.getTypeNames();
			String typeName = typeNames[0];

			FeatureSource featureSource;
//			FeatureCollection collection;

			featureSource = dataStore.getFeatureSource(typeName);

			CoordinateReferenceSystem crs = featureSource.getSchema().getCoordinateReferenceSystem();
//			CoordinateReferenceSystem crs = featureSource.getSchema().getCRS();
			return crs;

		} catch (Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}

		return null;
	}

	/**
	 * Generates the name of the index file from the source filename
	 * @param sourceName the original hydrological file
	 * @return the corresponding index file name
	 */
	public static String indexFileName(String sourceName) {
		String fileName = "";
		//strip off the filename extension
		int i = sourceName.lastIndexOf('.');
		if (i >= 0)
			fileName = sourceName.substring(0, i);

		return fileName + ".idx";
	}
	/**
	 * Get the list of source shape files for the targets.
	 * @return the list of source files
	 */
	public static ArrayList<AreaFile> getSources() {
		if (sourceMap.isEmpty())
			return new ArrayList<AreaFile>();
		return new ArrayList<AreaFile>(sourceMap.values());
	}
	/**
	 * Get the list of source shape file names for the targets.
	 * @return the list of names
	 */
	public static ArrayList getSourceNames() {
		if (sourceMap.isEmpty())
			return new ArrayList();
		ArrayList sources = new ArrayList(sourceMap.values());
		ArrayList names = new ArrayList(sources.size());
		for(int i=0;i<sources.size();i++){
			SourceData source = (SourceData)sources.get(i);
			names.add(source.fileName);
		}
		return names;
	}
	
	public static Set<VerdiStyle> getSourceStyles() {
		HashSet<VerdiStyle> styles = new HashSet<VerdiStyle>();
		styles.addAll(styleMap.values());
		return styles;

	}
	/**
	 * Get the entire list of targets
	 * @return the whole list of them
	 */
	public static ArrayList getTargets() {
		return targets;
	}
	
	/** 
	 * Find a target that corresponds to a given polygon
	 * @param obj the Geometry object
	 * @return the target that matches
	 */
	public static Target getTarget(Geometry obj) {
		Target tgt = null;//projectionMap.get(new GeometryWrapper(obj));
		if (obj instanceof MultiPolygon) {
			if (((MultiPolygon)obj).getNumGeometries() > 1) {
				return null;
			}
			obj = ((MultiPolygon)obj).getGeometryN(0);
		}
		if (tgt == null)
			tgt = geometryMap.get(obj);
		return tgt;
	}
		
	public static void mapProjection(Geometry source, Geometry projected) {
		Target tgt = geometryMap.get(source);
		if (projected instanceof MultiPolygon) {
			Geometry nested = ((MultiPolygon)projected).getGeometryN(0);
			geometryMap.put(nested, tgt);
			
		} else
			geometryMap.put(projected, tgt);
	}
		
	public static Target linearSearchGeometry(Geometry obj) {
		for (int i = 0; i < targets.size(); i++) {
			Target data = (Target)targets.get(i);
			if (data.dataObject.equals(obj))
				return data;
		}
		return null;	
	}

	/**
	 * Get the targets for a particular source file
	 * @param source the name of the source file
	 * @return the matching targets or null if none exist
	 */
	public static ArrayList getTargetsForSource(AreaFile sourceFile) {

		if(!(sourceFile instanceof SourceData)){return null;}

		ArrayList results=new ArrayList();
		for (int i = 0; i < targets.size(); i++) {
			Target data = (Target)targets.get(i);
			if (data.sourceData==sourceFile){
				results.add(data);
			}
		}
		if(results.isEmpty())return null;
		return results;
	}
	/**
	 * Find a target with the given name
	 * @param name the name of the target to look for
	 * @return the target that matches
	 */
	public static Target findTarget(String name) {
		return (Target)targetMap.get(name);
	}
	public static Target findTargetByFullName(String name) {
		for(Target target:(ArrayList<Target>)targets){
			if(name.equals(target.fullName))return target;
		}
		return null;
	}
	/**
	 * Set the entire list of targets to the passed in one
	 * @param list the new list
	 */
	public static void setTargets(ArrayList list) {
		targets = (ArrayList)list.clone();
	}

	/**
	 * Get the string representing the target
	 * @return the string to show for it
	 */
	public String toString() {
		if(fullName!=null)return fullName;
		return keyName;
	}
	String getFullNameString(){
		if(fullName==null)return "";
		return fullName;
	}
	/**
	 * Get the object corresponding to a target
	 * @return the object
	 */
	public Geometry getDataObject() {
		return dataObject;
	}

	/**
	 * Get a list of all selected targets
	 * @return the list of selected targets
	 */
	public static ArrayList getSelectedTargets() {
		return selectedTargets;
	}

	/**
	 * Set the list of selected targets
	 * @param list the list of targets to select
	 */
	public static void setSelectedTargets(ArrayList list) {
		selectedTargets = list;
	}
//	public static void setCurrentSelectedTargets(Object[] list){
//		// change selection list
//		ArrayList tList = new ArrayList(list.length);
//		for(Object target:list){
//			if(target instanceof Target)tList.add(target);
//		}
//		setSelectedTargets(tList);
//	}
	public static void setCurrentSelectedTargets(List list){
		// change selection list
		ArrayList tList = new ArrayList(list.size());
		for(Object target:list){
			if(target instanceof Target)
				tList.add(target);
		}
		setSelectedTargets(tList);
	}
	/**
	 * Update the layer showing selected targets.  Called 
	 * if the target selection window closes
	 *
	 */
	public static void updateSelectedTargetsLayer() {
		//    JeoViewerApp japp = WDTMainWindow.mainWindow.japp;
		//    JeoViewer jeoViewer = WDTMainWindow.mainWindow.jeoViewer;
		//    GeoLayer layer = japp.getGdbv().getLayerManager().getLayer(selectedTargetLayer);
		//    japp.getGdbv().defaultGDB().removeAllObjects(selectedTargetLayer);
		//    for (int i = 0; i < selectedTargets.size(); i++) {
		//      Target target = (Target)selectedTargets.get(i);
		//      jeoViewer.getGdbv().defaultGDB().addGeoObject(target.dataObject, selectedTargetLayer);
		//    }
		//    layer.setDrawFlag(GeoLayer.GLAlwaysDraw);
	}
	/**
	 * Hides the layer showing selected targets. Called 
	 * if the target selection window opens.
	 *
	 */
	public static void hideSelectedTargetsLayer() {
		//    JeoViewerApp japp = WDTMainWindow.mainWindow.japp;
		//    JeoViewer jeoViewer = WDTMainWindow.mainWindow.jeoViewer;
		//    GeoLayer layer = japp.getGdbv().getLayerManager().getLayer(selectedTargetLayer);
		//    layer.setDrawFlag(GeoLayer.GLNeverDraw);
	}
	public void setSelected(boolean state){
		if(state){
			selectedTargets.add(this);
		}else selectedTargets.remove(this);
	}
	/**
	 * Whether a target has been selected by the user.
	 * @return if it's selected
	 */
	public boolean isSelectedPolygon() {
		return selectedTargets.contains(this);
	}
	
//	public static void calculateAreasNow() {		// 2014 does not appear to be used
//		(new TargetCalculator()).doWork();
//	}
	/**
	 * Calculate the polygon area intersections and update the screen when done
	 * @return whether it had to spawn a process to do it
	 */
//	public static boolean calculateAreas() {		// 2014 does not appear to be used
//
//		boolean calculateAreas = false;
//		TargetCalculator calc = new TargetCalculator();
//		calc.calculateIntersections(targets);
//
//		return calculateAreas;
//	}
	/**
	 * Get the default area units for target polygons.
	 * @return the default units
	 */
	public static String getDefaultUnits() {
		return "km2";
	}
	
	public boolean depositionCalculated() {
		int gridIndex=getCurrentGridNum();
		return rowIndex.containsKey(gridIndex);
	}
	public boolean containsDeposition(){
		int gridIndex=getCurrentGridNum();

		if(!rowIndex.containsKey(gridIndex)) {
			Logger.error("problem here");
		}
		int[]rows=rowIndex.get(gridIndex);
		return (rows != null);
	}
//	public static UnitConverter converterGrid=null;
//	public static UnitConverter converterTargetStandard=null;
//	public static UnitConverter converterTargetGrid=null;
	public static void setUnitConverters(String gridUnit){
//		UnitConverter identity = UnitConverter.IDENTITY;		// not an option, use isIdentity() as indicator if a converter is the identity converter
		UnitConverter identity = AbstractConverter.IDENTITY;
		if(Units.isUnitPerArea(gridUnit)){
			Unit gridTopUnit=(Unit) Units.getTopUnit(gridUnit);
			Unit gridAreaUnit=(Unit) Units.getAreaUnit(gridUnit);
			Unit gridOriginalUnit = VUnits.createUnit(gridUnit);

			Unit targetUnit=VUnits.createUnit(getDefaultUnits());
//			Unit standardOne = gridAreaUnit.getSystemUnit();	// JScience function name change but returns
			// "the system unit this unit is derived from"
			Unit standardOne = gridAreaUnit.getSystemUnit();	//.getStandardUnit();	is not an option
			Unit gridFinalUnit = gridTopUnit.divide(standardOne); 

			converterGrid=gridOriginalUnit.getConverterTo(gridFinalUnit);
			converterTargetStandard = targetUnit.getConverterTo(standardOne);
			converterTargetGrid = targetUnit.getConverterTo(gridAreaUnit);
		}else{
			if(Units.isConcentration(gridUnit)){
				converterGrid=identity;
				converterTargetStandard = identity;
				converterTargetGrid = identity;
			}else{
				if(Units.isLength(gridUnit)){
					Unit targetUnit=VUnits.createUnit(getDefaultUnits());
					Unit gridOriginalUnit = VUnits.createUnit(gridUnit);
					converterGrid=identity;
					Unit gridUnitSquared = gridOriginalUnit.pow(2);		//.times(gridOriginalUnit);	// changed to squared

					converterTargetStandard = targetUnit.getConverterTo(gridUnitSquared);
					converterTargetGrid = converterTargetStandard;
				}
				else{
					converterGrid=identity;
					converterTargetStandard = identity;
					converterTargetGrid = identity;
				}
			}
		}
	}
	
	/**
	 * Calculate the deposition in a target using the grid intersection areas
	 * and the value for selected variables at each grid location.
	 *
	 */
	public float calculateAverageDeposition(MeshCellInfo[] data, MeshDataReader reader) {
		return handleAvgDeposition(calculateTotalDeposition(data, reader));
	}
	public float calculateAverageDeposition(float[][] data) {
		return handleAvgDeposition(calculateTotalDeposition(data));
	}
	public float handleAvgDeposition(float val) {

		val= val/(float)(converterTargetGrid.convert((float)area));		// 2014 JEB
//		val = val / (float)area;
		currentDeposition = val;
		return val;
	}
	public float calcAverageDeposition(float[][] data) {

		float val=calcTotalDeposition(data);

		val= val/(float)(converterTargetGrid.convert((float)area));		// 2014 JEB
//		val = val / (float)area;
		currentDeposition = val;
		return val;
	}
	public float getAverageDeposition() {

		float val=getDeposition();

		val= val/(float)(converterTargetGrid.convert((float)area));		// 2014 JEB
//		val = val / (float)area;
		return val;
	}
	
	public float calcTotalDeposition(float[][] data) {
		int gridIndex=getCurrentGridNum();

		int[]rows=rowIndex.get(gridIndex);
		int[]cols=colIndex.get(gridIndex);
		float[]areas=overlapArea.get(gridIndex);
		float dep=0.0f;
		// skip if there is no overlap with the grid
		Logger.debug("into Target.calcTotalDeposition");
		if (rows!=null){

			// sum up contributions from each cell
			for (int i = 0; i < rows.length; i++) {
				if(rows[i]<0||cols[i]<0){
					Logger.debug("stop here");
					continue;
				}
				if(rows[i]>data.length){
					Logger.debug("stop here");
					continue;		// 2014 nothing was done after previous line saying "stop here"
				}
				float dataPoint = data[ rows[i] ][ cols[i] ];
				
				if ( "NaN".equalsIgnoreCase(new Float(dataPoint).toString())  || dataPoint <= DataUtilities.BADVAL3 || dataPoint <= DataUtilities.AMISS3) 
				{	// 2014 changed comparison to AMISS3 from == to <=
					//Logger.debug("  === ");
					continue;
				}
				// TODO: NaN

//				dep = dep + areas[i] * dataPoint;		// 2014 JEB
						// 2014 remove unit conversion here 
				dep = dep + (float) (converterTargetStandard.convert(areas[i]) * converterGrid.convert(dataPoint));
			}
		}
//Logger.debug("returning from calcTotalDeposition, dep = " + dep);
		return dep;
	}
	
	public float calcTotalDeposition(MeshCellInfo[] data, MeshDataReader reader) {
		int gridIndex=getCurrentGridNum();

		int[]cells=cellIndex.get(gridIndex);
		float[]areas=cellOverlapArea.get(gridIndex);
		float dep=0.0f;
		// skip if there is no overlap with the grid
		Logger.debug("into Target.calcTotalDeposition");
		if (cells!=null){

			// sum up contributions from each cell
			for (int i = 0; i < cells.length; i++) {
				if(cells[i]<0){
					Logger.debug("stop here");
					continue;
				}
				if(cells[i]>data.length){
					Logger.debug("stop here");
					continue;		// 2014 nothing was done after previous line saying "stop here"
				}
				float dataPoint = (float)data[ cells[i] ].getValue(reader);
				
				if ( "NaN".equalsIgnoreCase(new Float(dataPoint).toString())  || dataPoint <= DataUtilities.BADVAL3 || dataPoint <= DataUtilities.AMISS3) 
				{	// 2014 changed comparison to AMISS3 from == to <=
					//Logger.debug("  === ");
					continue;
				}
				// TODO: NaN

//				dep = dep + areas[i] * dataPoint;		// 2014 JEB
						// 2014 remove unit conversion here 
				dep = dep + (float) (converterTargetStandard.convert(areas[i]) * converterGrid.convert(dataPoint));
			}
		}
//Logger.debug("returning from calcTotalDeposition, dep = " + dep);
		return dep;
	}
	
	public float calculateTotalDeposition(MeshCellInfo[] data, MeshDataReader reader) {
		return handleCalculateTotalDeposition(calcTotalDeposition(data, reader));
	}
	public float calculateTotalDeposition(float[][] data) {
		return handleCalculateTotalDeposition(calcTotalDeposition(data));
	}
	public float handleCalculateTotalDeposition(float dep) {

		int plotIndex=getCurrentPlotNum();
		// convert final deposition to grid unit
		while(deposition.size()<(plotIndex+1)){
			deposition.add(null);
		}
		deposition.set(plotIndex,new Float(dep));
		currentDeposition = dep;

		return dep;
	}
	
	public float getCurrentDeposition() {
		return currentDeposition;
	}

	/**
	 * Get the list of only selected target sets
	 * @return the list of selected target sets
	 */
	public static ArrayList getSelectedSets(){
		ArrayList selectedSets = new ArrayList();
		for(int i=0;i<selectedTargetsAndSets.size();i++){
			Object obj = selectedTargetsAndSets.get(i);
			if(obj instanceof TargetSet)selectedSets.add(obj);
		}
		return selectedSets;
	}

	/**
	 * Get the list of selected targets and target sets
	 * @return the current list
	 */
	public static ArrayList getSelectedTargetsAndSets() {
		return selectedTargetsAndSets;
	}

	/**
	 * Set the list of selected targets and target sets
	 * @param list the new list
	 */
	public static void setSelectedTargetsAndSets(ArrayList list) {
		selectedTargetsAndSets = list;
	}

	/**
	 * Get the current deposition value
	 * @return the deposition
	 */
	public float getDeposition() {
		return deposition.get(getCurrentPlotNum());
	}


	/**
	 * Get the area of the entire polygon
	 * @return the area
	 */
	public double getArea() {
		return area;
	}
	/**
	 * Get the area converted into default units
	 * @return
	 */
	public double getConvertedArea() {
		return Units.convertArea(Target.getDefaultUnits(), area);
	}
	public double getConvertedArea(double conversionFactor) {
		return conversionFactor*area;
	}
	
	/**
	 * Get the name (HUC code) of the target
	 * @return the name
	 */
	public String getKeyName() {
		return keyName;
	}

	/**
	 * Get the name of the source file the target came from
	 * @return the source file
	 */
	public AreaFile getSource() {
		return sourceData;
	}

	/**
	 * Load the data from the vector of options
	 * @param data the vector containing the file data
	 * @param offset the current offset
	 * @return if it was successful
	 * @throws IOException
	 */
	public static int load(Vector data, int offset) throws IOException{
		try{
			unloadAll();

			String fileName = (String)((Vector)data.get(offset)).get(0);
			offset=offset+1;

			// get the number of source files
			Number numSource = (Number)((Vector)data.get(offset)).get(0);
			offset=offset+1;

			// load the sources
			for(int i=0;i<numSource.intValue();i++){
//				String sourceName = (String)((Vector)data.get(offset)).get(0);
//				String nameInfo = (String)((Vector)data.get(offset)).get(1);
//				String projInfo = (String)((Vector)data.get(offset)).get(2);
				// load it in
				//TODO mab commented out for now
				// loadData(new File(sourceName),nameInfo,projInfo,false);
				offset=offset+1;
			}

			// load in the target sets
			//offset=TargetSet.load(data,offset);		

			// load in selected targets
			Number numTargets = (Number)((Vector)data.get(offset)).get(0);
			offset=offset+1;
			for(int i=0;i<numTargets.intValue();i++){
				String name = (String)((Vector)data.get(offset)).get(0);

				// get the associated target or set
				Object set = TargetSet.findTargetSet(name);
				if(set==null)set = Target.findTarget(name);
				if(set==null){
					throw(new IOException("Load Error:Unable to find "+Target.NAME+" or set named "+name));
				}
				selectedTargetsAndSets.add(set);
				offset=offset+1;
			}
			setSelectedTargets(TargetSet.includedTargets(selectedTargetsAndSets));
			Target.updateSelectedTargetsLayer();

//			String showSelectedString = ((String) ((Vector)data.get(offset)).get(0)).trim();
			offset = offset+1;
			//TargetDataObject.setShowSelectedOnly(Boolean.valueOf(showSelectedString).booleanValue());

		}catch(ClassCastException ex){
			throw(new IOException("Load Error: Incorrect format reading "+Target.NAME+" data"));
		}
		return offset;
	}
	
	/**
	 * Export the targets to an ESRI shape file
	 * Currently not used.
	 */
//	public static void exportTargets() {		// 2014 not used
//		String exporterName = "ESRIShapefileExporter";
//		HashMap l = new HashMap();
//		ArrayList targs = new ArrayList();
//		ArrayList targets = getTargets();
//		for (int i = 0; i < targets.size(); i++) {
//			targs.add(((Target)targets.get(i)).dataObject);
//		}
//		l.put(targetLayer, targs);
//
//		ArrayList props = new ArrayList();
//		props.addAll(Arrays.asList(new String[] { "CoordinateSystem:", "GeoCentric" }));
//		props.addAll(
//				Arrays.asList(
//						new String[] { anl.jeoviewer.exporters.AbstractGeoObjectExportAdaptor.EXPORT_FLAG_PROPERTY, anl.jeoviewer.exporters.AbstractGeoObjectExportAdaptor.EXPORT_ALL_FIELDS }));
//
//		//    String filename = ((Target)targets.get(0)).source;
//		//    // strip off the filename extension
//		//    int i = filename.lastIndexOf('.');
//		//    if (i >= 0)
//		//      filename = filename.substring(0, i);
//		//    filename = filename + "-3";
//		String filename="c:\\sample.shp";
//
//		Exporter.export(exporterName, filename, props, l, null);
//	}
	/**
	 * Indicate that a source file has had its index loaded.
	 * @param source the source file that was indexed
	 */
	static void setIndexLoaded(String source) {
		SourceData sourceData = (SourceData)sourceMap.get(source);
		sourceData.indexed=true;
	}
	/**
	 * Get whether the indicated file has had its index loaded
	 * @param source the source file
	 * @return whether it's been indexed
	 */
	static boolean hasIndexBeenLoaded(String source) {
		SourceData val = (SourceData)sourceMap.get(source);
		return val.indexed;
	}
	/**
	 * Gets whether index files have been loaded for all source files
	 * @return if they have all been indexed
	 */
	static boolean allIndexFilesLoaded() {
		Iterator it = sourceMap.values().iterator();
		while (it.hasNext()) {
			SourceData val = (SourceData)it.next();
			if (!val.indexed)
				return false;
		}
		return true;
	}
	/**
	 * Unload the indicated file of target polygons
	 * @param sourceFile the file to unload
	 */
	public static void unloadAll() {
		if (sourceMap.isEmpty() || targets.isEmpty())
			return;


		// clear the selection lists
		selectedTargets.clear();
		selectedTargetsAndSets.clear();

		TargetSet.unload();
		targets.clear();
		targetMap.clear();
		sourceMap.clear();
		geometryMap.clear();
	}

	/**
	 * Clear out the grid intersection data for all of the targets
	 *
	 */
	static public void clearGridIntersections() {
		for (int i = 0; i < targets.size(); i++) {
			Target target = (Target)targets.get(i);
			target.area = 0.0;
			target.cellIndex = null;
			target.rowIndex = null;
			target.colIndex = null;
			target.overlapArea = null;
			target.cellOverlapArea = null;
		}
		Iterator it = sourceMap.values().iterator();
		while (it.hasNext()) {
			SourceData val = (SourceData)it.next();
			val.indexed=false;
		}
	}


	@Override
	public AreaFile getAreaFile() {
		return sourceData;
	}

	@Override
	public String getDescription() {
		return toString();
	}

	@Override
	public String getName() {
		return getKeyName();
	}

	public static void computeDepositionRange(TilePlot plot,double[] minmax,boolean selectedOnly){
		minmax[1]=0;
		minmax[0]=-1;
		setCurrentTilePlot(plot);
		setCurrentGridInfo(plot.getGridInfo());
		for(Target target:(ArrayList<Target>)targets){
			if(selectedOnly&& !target.isSelectedPolygon())continue;
			double val=target.getDeposition();
			if(minmax[0]==-1)minmax[0]=val;
			if(minmax[0]>val)minmax[0]=val;
			if(minmax[1]<val)minmax[1]=val;
		}
	}
	
	public static void computeAverageDepositionRange(TilePlot plot,double[] minmax,boolean selectedOnly){
		minmax[1]=0;
		minmax[0]=-1;
		setCurrentTilePlot(plot);
		setCurrentGridInfo(plot.getGridInfo());
		for(Target target:(ArrayList<Target>)targets){
			if(selectedOnly&& !target.isSelectedPolygon())continue;
			double val=target.getAverageDeposition();
			if(minmax[0]==-1)minmax[0]=val;
			if(minmax[0]>val)minmax[0]=val;
			if(minmax[1]<val)minmax[1]=val;
		}
	}

	private int getCurrentPlotNum(){
		if(currentTilePlot==null)return 0;
		return plots.indexOf(currentTilePlot);
	}

	private int getCurrentMeshPlotNum(){
		if(currentTilePlot==null)return 0;
		return plots.indexOf(currentTilePlot);
	}

	// returns whether or not tile plot already in list
	public static boolean setCurrentTilePlot(TilePlot currentTilePlot) {
		boolean plotExisting=plots.contains(currentTilePlot);
		if(currentTilePlot!=null){
			if(!plotExisting){
				plots.add(currentTilePlot);
			}
		}

		Target.currentTilePlot = currentTilePlot;
		return plotExisting;
	}

	public static void setCurrentGridInfo(double[][]gridBounds,double[][] domain){
		GridInfo grid=new GridInfo(gridBounds,domain);
		setCurrentGridInfo(grid);
	}

	public static void setCurrentGridInfo(GridInfo grid){
		int num=GridInfo.getGridNumber(grid);
		if(num<0)num=GridInfo.addGrid(grid);
		setCurrentGridNum(num);

	}

	public static int getCurrentGridNum() {
		return currentGridNum;
	}
	public static void setCurrentGridNum(int currentGridNum) {
		Target.currentGridNum = currentGridNum;
	}
	
	public boolean overlapsGrid(int currentGridNum) {
		if (overlapArea == null)
			System.out.println("Detected invalid target, null overlap");
		return overlapArea.size() > currentGridNum && overlapArea.get(currentGridNum).length > 0;
	}

	public boolean overlapsMesh(int currentGridNum) {
		if (cellOverlapArea == null)
			System.out.println("Detected invalid target, null overlap");
		return cellOverlapArea.size() > currentGridNum && cellOverlapArea.get(currentGridNum).length > 0;
	}

	static{
		// add plot of null to list of plots
		plots.add(null);
	}
	
	/*
	 * The following is because the existing code can not calculate total and average deposition for 
	 * all the frames for selected targets.
	 * These methods go over all the time and layer to calculate them, and get the ranges, which are used 
	 * to build the legend on a plot.
	 * 
	 * Author: Jizhen Zhao @ IE UNC
	 * Version: 2012-05-10
	 */
	
	private float computeTotalDeposition(float[][] data, int gridIndex) {

		int[]rows=rowIndex.get(gridIndex);
		int[]cols=colIndex.get(gridIndex);
		float[]areas=overlapArea.get(gridIndex);
		float dep=0.0f;
		// skip if there is no overlap with the grid
		if (rows!=null){

			// sum up contributions from each cell
			for (int i = 0; i < rows.length; i++) {
				if(rows[i]<0||cols[i]<0){
					Logger.debug("stop here");
					continue;
				}
				if(rows[i]>data.length){
					Logger.debug("stop here");
					continue;		// 2014 nothing had been done after previous line saying "stop here"
				}
				float dataPoint = data[ rows[i] ][ cols[i] ];
				
				// Logger.debug(new Float(dataPoint).toString());

				if ( "NaN".equalsIgnoreCase(new Float(dataPoint).toString())  || dataPoint <= DataUtilities.BADVAL3 || dataPoint <= DataUtilities.AMISS3) 
				{	// 2014 changed AMISS3 comparison from == to <=
					// Logger.debug("  === ");
					continue;
				}
				// TODO: NaN
//				dep = dep + (float)(areas[i] * dataPoint);		// 2014 JEB
				dep = dep + (float) (converterTargetStandard.convert(areas[i]) * converterGrid.convert(dataPoint));
			}
		}
		return dep;
	}
		
	public void computeAverageDeposition(float[][] data, int gridIndex, TargetDeposition deposition) {

		if ( data == null || deposition == null || gridIndex <0) {
			Logger.error("Target computeAverageDeposition(...): invalid inputs! Check that areas overlay grid cells.");
			return;
		}
		float total=computeTotalDeposition(data, gridIndex);

		float ave = total/(float)(converterTargetGrid.convert((float)area));		// 2014 JEB
//		float ave = total / (float)area;
		deposition.average = ave;
		deposition.total = total;
	}
	
	private float computeTotalDeposition(MeshCellInfo[] data, MeshDataReader reader, int gridIndex) {

		int[]cells=cellIndex.get(gridIndex);
		float[]areas=cellOverlapArea.get(gridIndex);
		float dep=0.0f;
		// skip if there is no overlap with the grid
		if (cells!=null){

			// sum up contributions from each cell
			for (int i = 0; i < cells.length; i++) {
				if(cells[i]<0){
					Logger.debug("stop here");
					continue;
				}
				if(cells[i]>data.length){
					Logger.debug("stop here");
					continue;		// 2014 nothing had been done after previous line saying "stop here"
				}
				float dataPoint = (float)data[ i ].getValue(reader);
				
				// Logger.debug(new Float(dataPoint).toString());

				if ( "NaN".equalsIgnoreCase(new Float(dataPoint).toString())  || dataPoint <= DataUtilities.BADVAL3 || dataPoint <= DataUtilities.AMISS3) 
				{	// 2014 changed AMISS3 comparison from == to <=
					// Logger.debug("  === ");
					continue;
				}
				// TODO: NaN
//				dep = dep + (float)(areas[i] * dataPoint);		// 2014 JEB
				dep = dep + (float) (converterTargetStandard.convert(areas[i]) * converterGrid.convert(dataPoint));
			}
		}
		return dep;
	}
	
	public void computeAverageDeposition(MeshCellInfo[] data, MeshDataReader reader, int gridIndex, TargetDeposition deposition) {

		if ( data == null || deposition == null || gridIndex <0) {
			Logger.error("Target computeAverageDeposition(...): invalid inputs! Check that areas overlay grid cells.");
			return;
		}
		float total=computeTotalDeposition(data, reader, gridIndex);

		float ave = total/(float)(converterTargetGrid.convert((float)area));		// 2014 JEB
//		float ave = total / (float)area;
		deposition.average = ave;
		deposition.total = total;
	}
}

