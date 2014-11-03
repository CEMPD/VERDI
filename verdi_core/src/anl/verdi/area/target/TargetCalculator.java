package anl.verdi.area.target;

import gov.epa.emvl.Numerics;
import gov.epa.emvl.Projector;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
//import javax.measure.units.Unit;		// JScience changed its hierarchy
//import javax.measure.unit.Unit;
import org.unitsofmeasurement.unit.Unit;

import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.projection.LatLonProjection;
import anl.verdi.area.AreaTilePlot;
import anl.verdi.area.LongTask;
import anl.verdi.area.Units;
import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.Dataset;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateFilter;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
/**
 * 
 * File Name:TargetCalculator.java
 * Description:
 * Calculates grid intersections and depositions for targets
 * 
 * @version April 2004
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 */
public class TargetCalculator extends LongTask {
	static final Logger Logger = LogManager.getLogger(TargetCalculator.class.getName());
	
	// whether depositions should be shown
	public boolean showIt=false;
	/**
	 * Called by the progress monitor to start the task
	 */
//  public void doWork() {		// 2014 does not appear to be used
//		
//		if(!Target.allIndexFilesLoaded()){
//			// try to read them in 
//			//readInIntersections();
//			
//			// see if they broke out of it
//			if(canceled)return;
//			
//			// if they did not load all of them in
//			if(!Target.allIndexFilesLoaded()){
//				// calculate where the grid cell intersections are located
//				boolean didCalcs = calculateIntersections(Target.getTargets());
//				if (didCalcs == false)return;
//			
//				// see if they broke out of it
//				if(canceled)return;
//			}
//		}
//		// calc and display the new depositions
//    if(showIt)TargetCalculator.calculateNewDepositions();
//    
//    done=true;
//  }
  /**
   * An estimate of the length of the task
   */
  public int getLengthOfTask() {
    ArrayList targets = Target.getTargets();
    return targets.size();
  }
  
// 2014 this function is commented out everywhere it was called in VERDI
//	/**
//	 * Calculate the intersections of the grid cells with each polygon
//	 * @param targets the targets to check
//	 * @return if it was successful and not cancelled
//	 */
//  public boolean calculateIntersections(ArrayList targets) {
//    statMessage = "Calculating Intersections...";
//    Logger.warn("WARNING: TargetCalculator.calculateIntersections called for only 1 argument, does nothing but returns TRUE always " + statMessage);
////    // change to busy cursor
////    boolean didCalcs = false;
////    try {
////      if (targets == null || targets.isEmpty())return false;
////      final VerdiApplication application = workspace.getApplicationMediator();
////
////      if ( application.getProject().getSelectedFormula() != null ) {
////        final DataFrame dataFrame =
////          application.evaluateFormula( Formula.Type.TILE );
////      final Dataset dataset = dataFrame.getDataset().get(0);
////		final Axes<CoordAxis> coordinateAxes = dataset.getCoordAxes();
////		final Projection projection = coordinateAxes.getProjection();
//      
////      //JeoViewer jeoViewer = WDTMainWindow.mainWindow.jeoViewer;
////      //JeoViewerApp japp = WDTMainWindow.mainWindow.japp;
////      //GeoScreenInfo si = jeoViewer.getSi();
////      
////      // get all the polygons in the grid
////      JeoObjectGrid grid = ModelData.getOverallGrid();
////      String units = ModelData.getBaseData().getDataUnits();
////      units=Units.getAreaFromLength(units);
////      
////      // get the conversion from the grid to the standard target area units
////      double conversion = Units.convertArea(units, Target.getUnits(), 1.0);
////      GridGeometry geometry = ((GridGeometry)grid.getGeometry().getGeometry());
////
////      //  get all the selected target polygons
////      for (int targetNum = 0; targetNum < targets.size(); targetNum++) {
////        if (canceled)
////          return false;
////        current = targetNum;
////        Target target = ((Target)targets.get(targetNum));
////        // update the message
////        statMessage = "Polygon " + target + " (" + (targetNum + 1) + " of " + targets.size() + ")";
////        Logger.debug(statMessage);
////
////        Geometry obj = target.dataObject;
////        // if it hasn't been done yet
////        if (target.area == 0) {
////          Geometry shp = ((JeoObjectSimple)obj).getGeometry().getGeometry();
////          shp = shp.transform(obj.getCoordSys(), grid.getCoordSys());
////
////          PackedPolygon box = (PackedPolygon) ((PackedPolygon)shp).boundingBox();
////
////          //calculate the candidate areas
////          Location[] list = box.pointsArray();
////          //Logger.debug("list " + list);
////          GridGeometry.GridIndex index1=null;
////          GridGeometry.GridIndex index2=null;
////          try{
////            index1 = geometry.query(list[0], grid.getDataType());
////            index2 = geometry.query(list[2], grid.getDataType());
////          }catch(Exception e){
////            index1=null;index2=null;
////          }
////
////          // if it intersects with that grid cell
////          if (index1 != null && index2 != null) {
////            int col1 = index1.column;
////            int col2 = index2.column;
////            int row1 = index1.row;
////            int row2 = index2.row;
////            if (col1 > col2) {
////              int temp = col1;
////              col1 = col2;
////              col2 = temp;
////            }
////            if (row1 > row2) {
////              int temp = row1;
////              row1 = row2;
////              row2 = temp;
////            }
////						// create the arrays to store the grid index data
////            ArrayList rows = new ArrayList();
////            ArrayList cols = new ArrayList();
////            ArrayList areas = new ArrayList();
////            for (int i = col1; i <= col2; i++) {
////              for (int j = row1; j <= row2; j++) {
////                if (canceled)
////                  return false;
////                  
////                // calculate the area of intersection
////                anl.spatial.geometry.Polygon cellPolygon = geometry.cellPolygon(j, i, grid.getDataType());
////                anl.spatial.geometry.Polygon intersection = ((PackedPolygon)shp).intersect(cellPolygon);
////                if (intersection != null) {
////                  // get the area of the intersection
////                  rows.add(new Integer(j));
////                  cols.add(new Integer(i));
////                  areas.add(new Float((float)intersection.area()));
////                }
////
////              }
////            }
////            // if there were some cells, make the arrays
////            if (!rows.isEmpty()) {
////              target.rowIndex = new int[rows.size()];
////              target.colIndex = new int[rows.size()];
////              target.overlapArea = new float[rows.size()];
////              for (int i = 0; i < rows.size(); i++) {
////                target.rowIndex[i] = ((Integer)rows.get(i)).intValue();
////                target.colIndex[i] = ((Integer)cols.get(i)).intValue();
////                // convert the area
////                target.overlapArea[i] = ((Float)areas.get(i)).floatValue() * (float)conversion;
////              }
////            }
////          }
////          // convert the area
////          target.area = ((PackedPolygon)shp).area() * conversion;
////          didCalcs = true;
////        }
////      }
////    } catch (Exception e) {
////      Logger.error("An exception occurred ");
////      e.printStackTrace();
////    } finally {
////      //update();
////    }
////    
////		//	write out the indices
////		if (didCalcs)writeOutIntersections();
//    return true;
//  }
  
  private int rows; // 259.
	private int columns; // 268.
	private double westEdge; // -420000.0 meters from projection center
	private double southEdge; // -1716000.0 meters from projection center
	private double cellWidth; // 12000.0 meters.
	private double cellHeight; // 12000.0 meters.
	private Projector projector;
	
	public class CoordinateTransform implements CoordinateFilter{
		Projector projector;
		double[] t = { 0.0, 1.0 }; 
		CoordinateTransform(Projector projection){
			projector=projection;
		}

		@Override
		public void filter(Coordinate arg0) {
			// TODO Auto-generated method stub
			if(projector==null)return;
			projector.project( arg0.x, arg0.y, t );
			arg0.x=t[0];
			arg0.y=t[1];
		}
	}
	
  public boolean calculateIntersections(ArrayList targets,DataFrame dataFrame,AreaTilePlot plot) {
	  
	    statMessage = "Calculating Intersections...";
	    Logger.debug("TargetCalculator.calculateIntersections for multiple args " + statMessage);
	    // change to busy cursor
	    boolean didCalcs = false;
	    try {
	      if (targets == null || targets.isEmpty())
	        return false;

	      // get all the polygons in the grid
	      Axes<DataFrameAxis> axes = dataFrame.getAxes();
	      //JeoObjectGrid grid = ModelData.getOverallGrid();
	      //String units = ModelData.getBaseData().getDataUnits();
	      Unit unit=dataFrame.getVariable().getUnit();
	      //units=Units.getAreaFromLength(units);
	      Units.setCurrentArea("km2"); 
	      double areaConversion=Units.conversionArea("m2");
	      Logger.debug("in TargetCalculator, just calculated areaConversion (conversion factor to m2) = " + areaConversion);	      
	      // get the conversion from the grid to the standard target area units
	      //double conversion = Units.convertArea(units, Target.getUnits(), 1.0);
	      //GridGeometry geometry = ((GridGeometry)grid.getGeometry().getGeometry());
	      final Dataset dataset = dataFrame.getDataset().get(0);
			final Axes<CoordAxis> coordinateAxes = dataset.getCoordAxes();
			final Projection projection = coordinateAxes.getProjection();
//			final double majorSemiaxis = 6370997.0; // UGLY: not in Projection.
//			final double majorSemiaxis = 6370000.0; // NOTE: Changed according to the communication between Todd Plessel and Donna Schwede on 	April 26, 2011
//			final double minorSemiaxis = majorSemiaxis;
			final DataFrameAxis rowAxis = axes.getYAxis();
			if (projection instanceof LatLonProjection) {
				projector = null;
			} else {
				projector = new Projector(projection);
			}
			CoordinateTransform filter = new CoordinateTransform(projector);
			if (rowAxis == null) {
				rows = 1;
			} else {
				rows = rowAxis.getExtent();
			}

			final DataFrameAxis columnAxis = axes.getXAxis();

			if (columnAxis == null) {
				columns = 1;
			} else {
				columns = columnAxis.getExtent();
			}

			final Envelope envelope = coordinateAxes.getBoundingBox(dataset.getNetcdfCovn());
			Logger.debug( "envelope = " + envelope );

			westEdge = envelope.getMinX(); // E.g., -420000.0.
			southEdge = envelope.getMinY(); // E.g., -1716000.0.
			cellWidth = Numerics.round1(envelope.getWidth() / columns); // 12000.0.
			cellHeight = Numerics.round1(envelope.getHeight() / rows); // 12000.0.
//			double[] t = { 0.0, 1.0 }; 
			
			// only do this is new plot
			if(plot!=null){
				Target.setCurrentTilePlot(plot);
				Target.setCurrentGridInfo(plot.getGridInfo());
			}
			int num=Target.getCurrentGridNum();
			
	      //  get all the selected target polygons
	      for (int targetNum = 0; targetNum < targets.size(); targetNum++) {
	        
	        current = targetNum;
	        Target target = ((Target)targets.get(targetNum));
	        // update the message
	        statMessage = "Polygon " + target + " (" + (targetNum + 1) + " of " + targets.size() + ")";
	        Logger.debug(statMessage);

	        Geometry obj = target.dataObject;
	        
	        // if it hasn't been done yet
	        if(!target.areaCalculatedForGrid(num)){
	        	target.setAreaInfo(num, null,null,null);
	        	Geometry poly = (Geometry)obj.clone();
	      		if(poly instanceof MultiPolygon){
	      			for(int i=0;i<((MultiPolygon)poly).getNumGeometries();i++){
	      				Geometry geo=((MultiPolygon)poly).getGeometryN(i);
	      				// get the vertices
	      				Polygon geoPolygon=(Polygon)geo;
	      				// transform the polygon
	      				geoPolygon.apply(filter);
	      				geoPolygon.geometryChanged();
	      			}
	      			// convert the area
		  	        target.area = poly.getArea() * areaConversion;
		  	        
	      			// get the bounding box of the geometry
	      			Envelope env = poly.getEnvelopeInternal();
	      			// get the corresponding grid cell 
	      			int col1=(int)Math.floor((env.getMinX()-westEdge)/cellWidth);
	      			int col2=(int)Math.floor((env.getMaxX()-westEdge)/cellWidth);
	      			int row1=(int)Math.floor((env.getMinY()-southEdge)/cellHeight);
	      			int row2=(int)Math.floor((env.getMaxY()-southEdge)/cellHeight);
	        		      			
				// create the arrays to store the grid index data
	            ArrayList<Integer> rowArray = new ArrayList<Integer>();
	            ArrayList<Integer> colArray = new ArrayList<Integer>();
	            ArrayList<Float> areas = new ArrayList<Float>();
	            GeometryFactory factory=new GeometryFactory();
	            if(col1<0&&col2<0)continue;
	            if(row1<0&&row2<0)continue;
	            if(col1<0)col1=0;
	            if(row1<0)row1=0;
	            if(col2>=columns)col2=columns-1;
	            if(row2>=rows)row2=rows-1;
	            
	            for (int i = col1; i <= col2; i++) {
	              for (int j = row1; j <= row2; j++) {
	                if (canceled)
	                  return false;
	                  
	                // calculate the area of intersection
	               Geometry cellPolygon=factory.toGeometry(new Envelope(i*cellWidth+westEdge,(i+1)*cellWidth+westEdge,j*cellHeight+southEdge,(j+1)*cellHeight+southEdge));
	                
	               Geometry intersection = poly.intersection(cellPolygon);
	               float intersectionArea=(float)intersection.getArea();
	                if (intersectionArea > 0) {
	                  // get the area of the intersection
	                  rowArray.add(new Integer(j));
	                  colArray.add(new Integer(i));
	                  areas.add(new Float(intersectionArea));
	                }

	              }
	            }
	            // if there were some cells, make the arrays
	            if (!rowArray.isEmpty()) {
	            	
	              int[] rowIndex = new int[rowArray.size()];
	              int[] colIndex = new int[rowArray.size()];
	              float[] overlapArea = new float[rowArray.size()];
	              for (int i = 0; i < rowArray.size(); i++) {
	                rowIndex[i] = ((Integer)rowArray.get(i)).intValue();
	                colIndex[i] = ((Integer)colArray.get(i)).intValue();
	                // convert the area
	                overlapArea[i] = ((Float)areas.get(i)).floatValue() * (float)areaConversion;
	                didCalcs = true;
	              }
	              // set the areas for that grid
	              target.setAreaInfo(num, rowIndex, colIndex, overlapArea);
	            }
	          }
//	          didCalcs = true;	// 2014 moved to calculate overlapArea[i]
	        }
	        }
	        
	    } catch (Exception e) {
	      Logger.error("An exception occurred ");
	      e.printStackTrace();
	    } finally {
	      //update();
	    }
	    
	    return didCalcs;	// 2014 had returned true; now calling program can test for success
	  }
  
//  /**
//   * Calculate new target depositions for all targets
//   *
//   */
//  public static void calculateNewDepositions() {		// 2014 does not appear to be used
//		// calculate the deposition locations
////    Target.calculateDepositions();
////    
////    //	update the range
////    ThematicRangeList.updateIfNeeded(OptionsWindow.rebuildThematicMap(), Target.getMinValue(), Target.getMaxValue(), Units.getCurrentTypeString());
////    JeoViewer jeoViewer = WDTMainWindow.mainWindow.jeoViewer;
////    JeoViewerApp japp = WDTMainWindow.mainWindow.japp;
////
////    Target.showColors();
////    japp.getGdbv().createMissingLayerCodes();
////    japp.getGdbv().updateLayerManager();
////
////    japp.getGdbv().getLayerManager().getLayer(Target.targetLayer).setThematicKey(Units.getCurrentTypeString());
////
////    // update
////    ThematicRangeList.update();
////    WDTMainWindow.mainWindow.updateThematicLegend();
////
////    // redo in the options window table
////    OptionsWindow.redoThematicMapTable();
////
////    WDTMainWindow.mainWindow.redisplay();
////
////    // enable the thematic map tab
////    OptionsWindow.updateTabs();
//
//  }
//  /**
//   * Read in any grid files that exist
//   * @return if it was successful and not cancelled
//   */
//  boolean readInIntersections() {
//    ArrayList sources = Target.getSourceNames();
//    for (int i = 0; i < sources.size(); i++) {
//      readInIntersections((String)sources.get(i));
//			if (canceled)return false;
//    }
//    return true;
//  }
//  /**
//   * Read in the grid intersections for a particular source file
//   * @param sourceFile the source file to index
//   * @return if the index file was found and successfully read in
//   */
//  boolean readInIntersections(String sourceFile) {
//  	if(Target.hasIndexBeenLoaded(sourceFile))return true;
//    int numTargets = 0;
//    // create name of index file
//    String fileName = Target.indexFileName(sourceFile);
//    // see if it exists
//    File file = new File(fileName);
//    if (!file.exists())
//      return false;
//
//    try {
//      // create the streams
//      FileInputStream fileStream = new FileInputStream(fileName);
//      DataInputStream stream = new DataInputStream(fileStream);
//
//      String gridId = ModelData.getBaseData().toString();
//
//      while (true) {
//        String currentId = stream.readUTF();
//        if (currentId == null)
//          break;
//        // read in sizes
//        int streamSize = stream.readInt();
//        // if it is the wrong grid
//        if (!gridId.equals(currentId)) {
//          // skip the stream size
//          stream.skipBytes(streamSize);
//        }
//        // found the right grid
//        else {
//          // read in sizes
//          numTargets = stream.readInt();
//          // read targets
//          Target.readAllIndices(stream, numTargets);
//          break;
//        }
//      }
//      //	close up the streams
//      stream.close();
//      fileStream.close();
//
//			// signal that the data has been read in or calculated and written out
//			Target.setIndexLoaded(sourceFile);
//			
//    } catch (EOFException e) {} catch (IOException e) {
//      Logger.error("Error reading index file for shape file " + sourceFile);
//      return false;
//    }
//
//    return true;
//  }
//  /**
//   * Write out the grid intersection data for all targets.
//   * @return
//   */
//  boolean writeOutIntersections() {
//    ArrayList sources = Target.getSourceNames();
//    for (int i = 0; i < sources.size(); i++) {
//      writeOutIntersections((String)sources.get(i));
//			if (canceled)return false;
//    }
//    return true;
//  }
//  /**
//   * Write out the grid intersection file for a given source file
//   * @param sourceFile the target source file
//   * @return if it was successful
//   */
//  boolean writeOutIntersections(String sourceFile) {
//		if(Target.hasIndexBeenLoaded(sourceFile))return true;
//		
//    // create the index file name
//    String file = Target.indexFileName(sourceFile);
//    ByteArrayOutputStream out = new ByteArrayOutputStream();
//    DataOutputStream stream = new DataOutputStream(out);
//    try {
//      Target.writeAllIndices(stream, sourceFile);
//      int streamSize = out.size();
//      FileOutputStream fileStream = new FileOutputStream(file, true);
//      stream = new DataOutputStream(fileStream);
//
//      // write out the grid id
//      String gridId = ModelData.getBaseData().toString();
//      Logger.debug("Writing index file for sourceFile " + sourceFile + " for grid " + gridId);
//      stream.writeUTF(gridId);
//
//      // write out the stream size
//      stream.writeInt(streamSize);
//
//      stream.write(out.toByteArray());
//      stream.close();
//      fileStream.close();
//      
//      // signal that the data has been read in or calculated and written out
//			Target.setIndexLoaded(sourceFile);
//      
//    } catch (IOException e) {
//      Logger.error("Error writing index file for shape file " + sourceFile);
//      return false;
//    }
//    return true;
//  }
  
}
