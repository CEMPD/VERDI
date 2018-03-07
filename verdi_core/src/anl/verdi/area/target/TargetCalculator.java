package anl.verdi.area.target;

import gov.epa.emvl.Numerics;
import gov.epa.emvl.Projector;
import gov.epa.emvl.TilePlot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
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
import anl.verdi.data.MeshCellInfo;
import anl.verdi.data.MeshDataReader;
import anl.verdi.plot.data.IMPASDataset;
import anl.verdi.plot.data.LonCellComparator;

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
	// TODO complete redesign and rewrite for GeoTools functions
	// What did this code do?
	// What should this code do?
	static final Logger Logger = LogManager.getLogger(TargetCalculator.class.getName());
	
	// whether depositions should be shown
	public boolean showIt=false;

	/**
   * An estimate of the length of the task
   */
  public int getLengthOfTask() {
    ArrayList targets = Target.getTargets();
    return targets.size();
  }
   
  private int rows; // 259.
	private int columns; // 268.
	private double westEdge; // -420000.0 meters from projection center
	private double southEdge; // -1716000.0 meters from projection center
	private double cellWidth; // 12000.0 meters.
	private double cellHeight; // 12000.0 meters.
	private Projector projector;
	
	private static GeometryFactory factory = new GeometryFactory();
	
	public class CoordinateTransform implements CoordinateFilter{
		Projector projector;
		double[] t = { 0.0, 1.0 }; 
		public CoordinateTransform(Projector projection){
			projector=projection;
		}

		@Override
		public void filter(Coordinate arg0) {
			if(projector==null)return;
			projector.project( arg0.x, arg0.y, t );
			arg0.x=t[0];
			arg0.y=t[1];
		}
	}
	
	public static GeometryFactory getGeometryFactory() {
		return factory;
	}
	
  public boolean calculateIntersections(ArrayList targets,DataFrame dataFrame, AreaTilePlot plot) {
	  
	    statMessage = "Calculating Intersections...";
	    Logger.debug("TargetCalculator.calculateIntersections for multiple args " + statMessage);
	    Logger.debug("targets = " + targets);
	    Logger.debug("dataFrame = " + dataFrame);
	    //Logger.debug("plot = " + plot);
	    // change to busy cursor
	    boolean didCalcs = false;
	    try {
	      if (targets == null || targets.isEmpty())
	      {
	    	  Logger.debug("in TargetCalculater.calculateIntersections: targets are null or empty; returning false.");
	    	  return false;
	      }

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
	      Logger.debug("Dataset = " + dataset.getName());
			final Axes<CoordAxis> coordinateAxes = dataset.getCoordAxes();
			Logger.debug("coordinateAxes = " + coordinateAxes);
			final Projection projection = coordinateAxes.getProjection();
			if (projection != null)
				Logger.debug("projection = " + projection != null ? projection.getProjectionParameters() : null);
			else
				Logger.debug("projection = " + null);
//			final double majorSemiaxis = 6370997.0; // UGLY: not in Projection.
//			final double majorSemiaxis = 6370000.0; // NOTE: Changed according to the communication between Todd Plessel and Donna Schwede on 	April 26, 2011
//			final double minorSemiaxis = majorSemiaxis;
			final DataFrameAxis rowAxis = axes.getYAxis();
			Logger.debug("rowAxis = " + rowAxis); 
			if (projection == null || projection instanceof LatLonProjection) {
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
			if (projector != null)
				Logger.debug("projector = " + projector.getProjection());
			else
				Logger.debug("projector = " + null);
			Logger.debug("rows = " + rows);

			final DataFrameAxis columnAxis = axes.getXAxis();

			if (columnAxis == null) {
				columns = 1;
			} else {
				columns = columnAxis.getExtent();
			}
			Logger.debug("columnAxis = " + columnAxis);
			Logger.debug("columns = " + columns);

			final Envelope envelope = coordinateAxes.getBoundingBox(dataset.getNetcdfCovn());
			Logger.debug( "envelope = " + envelope );

			westEdge = envelope.getMinX(); // E.g., -420000.0.
			southEdge = envelope.getMinY(); // E.g., -1716000.0.
			cellWidth = Numerics.round1(envelope.getWidth() / columns); // 12000.0.
			cellHeight = Numerics.round1(envelope.getHeight() / rows); // 12000.0.
			Logger.debug("westEdge = " + westEdge + ", southEdge = " + southEdge + ", cellWidth = " + 
					cellWidth + ", cellHeight = " + cellHeight);
//			double[] t = { 0.0, 1.0 }; 
			
			// only do this if new plot
			Target.setCurrentTilePlot(plot);
			Target.setCurrentGridInfo(plot.getGridInfo());
			int num=Target.getCurrentGridNum();
			
	      //  get all the selected target polygons
	      for (int targetNum = 0; targetNum < targets.size(); targetNum++) {
	        
	        current = targetNum;
	        Target target = ((Target)targets.get(targetNum));
	        // update the message
	        statMessage = "Polygon " + target + " (" + (targetNum + 1) + " of " + targets.size() + ")";
	        Logger.debug(statMessage);

	        CoordinateReferenceSystem gridCRS = null;
;
	        Geometry obj = target.getGeometry(null, null);
	        
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
	      			}
	      			poly.geometryChanged();
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
	            if(col1<0&&col2<0)
	            	continue;
	            if(row1<0&&row2<0)
	            	continue;
	            if(col1<0)
	            	col1=0;
	            if(row1<0)
	            	row1=0;
	            if(col2>=columns)
	            	col2=columns-1;
	            if(row2>=rows)
	            	row2=rows-1;
	            
	            for (int i = col1; i <= col2; i++) {
	              for (int j = row1; j <= row2; j++) {
	                if (canceled)
	                  return false;
	                  
	                // calculate the area of intersection
	               Geometry cellPolygon=factory.toGeometry(new Envelope
	            		   (i*cellWidth+westEdge,
	            		   (i+1)*cellWidth+westEdge,
	            		   j*cellHeight+southEdge,
	            		   (j+1)*cellHeight+southEdge));
	                
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
	        else
	        	if (!didCalcs && target.overlapsGrid(num))
	        		didCalcs = true;
	        }
	        
	    } catch (Exception e) {
	      Logger.error("An exception occurred here");
	      e.printStackTrace();
	      return false;
	    } finally {
	      //update();
	    }
	    
	    return didCalcs;	// 2014 had returned true; now calling program can test for success
	  }
  
  public boolean calculateIntersections(ArrayList targets, IMPASDataset dataset, TilePlot plot) {
	  
	    statMessage = "Calculating Intersections...";
	    Logger.debug("TargetCalculator.calculateIntersections for multiple args " + statMessage);
	    Logger.debug("targets = " + targets);
	    Logger.debug("plot = " + plot);
	    // change to busy cursor
	    boolean didCalcs = false;
	    try {
	      if (targets == null || targets.isEmpty())
	      {
	    	  Logger.debug("in TargetCalculater.calculateIntersections: targets are null or empty; returning false.");
	    	  return false;
	      }

	      // get all the polygons in the grid
//	      Axes<DataFrameAxis> axes = dataFrame.getAxes();
	      //JeoObjectGrid grid = ModelData.getOverallGrid();
	      //String units = ModelData.getBaseData().getDataUnits();
//	      Unit unit=dataFrame.getVariable().getUnit();
	      //units=Units.getAreaFromLength(units);
	      Units.setCurrentArea("km2"); 
	      double areaConversion=Units.conversionArea("m2");
	      Logger.debug("in TargetCalculator, just calculated areaConversion (conversion factor to m2) = " + areaConversion);	      
	      // get the conversion from the grid to the standard target area units
	      //double conversion = Units.convertArea(units, Target.getUnits(), 1.0);
	      //GridGeometry geometry = ((GridGeometry)grid.getGeometry().getGeometry());
//	      final Dataset dataset = dataFrame.getDataset().get(0);
	      //Logger.debug("Dataset = " + dataset.getName());
//			final Axes<CoordAxis> coordinateAxes = dataset.getCoordAxes();
//			Logger.debug("coordinateAxes = " + coordinateAxes);
//			final Projection projection = coordinateAxes.getProjection();
//			Logger.debug("projection = " + projection.getProjectionParameters());
//			final double majorSemiaxis = 6370997.0; // UGLY: not in Projection.
//			final double majorSemiaxis = 6370000.0; // NOTE: Changed according to the communication between Todd Plessel and Donna Schwede on 	April 26, 2011
//			final double minorSemiaxis = majorSemiaxis;
//			final DataFrameAxis rowAxis = axes.getYAxis();
//			Logger.debug("rowAxis = " + rowAxis); 
//			if (projection instanceof LatLonProjection) {
				projector = null;
//			} else {
//				projector = new Projector(projection);
//			}
			CoordinateTransform filter = new CoordinateTransform(projector);
/*			if (rowAxis == null) {
				rows = 1;
			} else {
				rows = rowAxis.getExtent();
			}
			Logger.debug("projector = " + projector.getProjection());
			Logger.debug("rows = " + rows);

			final DataFrameAxis columnAxis = axes.getXAxis();

			if (columnAxis == null) {
				columns = 1;
			} else {
				columns = columnAxis.getExtent();
			}
			Logger.debug("columnAxis = " + columnAxis);
			Logger.debug("columns = " + columns);

			final Envelope envelope = coordinateAxes.getBoundingBox(dataset.getNetcdfCovn());
			Logger.debug( "envelope = " + envelope );

			westEdge = envelope.getMinX(); // E.g., -420000.0.
			southEdge = envelope.getMinY(); // E.g., -1716000.0.
			cellWidth = Numerics.round1(envelope.getWidth() / columns); // 12000.0.
			cellHeight = Numerics.round1(envelope.getHeight() / rows); // 12000.0.
			Logger.debug("westEdge = " + westEdge + ", southEdge = " + southEdge + ", cellWidth = " + 
					cellWidth + ", cellHeight = " + cellHeight);
//			double[] t = { 0.0, 1.0 }; 
*/			
			// only do this if new plot
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

	        Geometry obj = target.getGeometry(null, null);
	        
	        // if it hasn't been done yet
	        if(!target.areaCalculatedForGrid(num)){
	        	target.setAreaInfo(num, null,null);
	        	Geometry poly = (Geometry)obj.clone();
	      		if(poly instanceof MultiPolygon){
	      			for(int i=0;i<((MultiPolygon)poly).getNumGeometries();i++){
	      				Geometry geo=((MultiPolygon)poly).getGeometryN(i);
	      				// get the vertices
	      				Polygon geoPolygon=(Polygon)geo;
	      				// transform the polygon
	      				geoPolygon.apply(filter);
	      			}
	      			poly.geometryChanged();
	      			// convert the area
		  	        target.area = poly.getArea() * areaConversion;
		  	        
		  	        //TODO - sort cell infos by position to avoid linear search
		  	        
	      			// get the bounding box of the geometry
	      			/*Envelope env = poly.getEnvelopeInternal();
	      			// get the corresponding grid cell 
	      			int col1=(int)Math.floor((env.getMinX()-westEdge)/cellWidth);
	      			int col2=(int)Math.floor((env.getMaxX()-westEdge)/cellWidth);
	      			int row1=(int)Math.floor((env.getMinY()-southEdge)/cellHeight);
	      			int row2=(int)Math.floor((env.getMaxY()-southEdge)/cellHeight);
	        		      			
				// create the arrays to store the grid index data
	            ArrayList<Integer> rowArray = new ArrayList<Integer>();
	            ArrayList<Integer> colArray = new ArrayList<Integer>();*/
		  	    ArrayList<Integer> cellArray = new ArrayList<Integer>();
	            ArrayList<Float> areas = new ArrayList<Float>(); 
	            /* if(col1<0&&col2<0)
	            	continue;
	            if(row1<0&&row2<0)
	            	continue;
	            if(col1<0)
	            	col1=0;
	            if(row1<0)
	            	row1=0;
	            if(col2>=columns)
	            	col2=columns-1;
	            if(row2>=rows)
	            	row2=rows-1;*/
	            
//	            for (int i = col1; i <= col2; i++) {
	            Envelope env = poly.getEnvelopeInternal();
	            MeshCellInfo[] data = dataset.getLonSortedCellsArray();
	            

	            int idx = Arrays.binarySearch(data, env.getMinX(), LonCellComparator.getInstance() );
	            if (idx < 0)
	            	idx *= -1;
	            if (idx > 0)
	            	--idx;
	            Map<MeshCellInfo, Integer> potentialCells = new HashMap<MeshCellInfo, Integer>();
	            for (int cellIdx = idx; cellIdx < data.length && env.getMaxX() >= data[cellIdx].getLon(data[cellIdx].getMinXPosition()); ++cellIdx) {
	            	if (data[cellIdx].getLat(data[cellIdx].getMinYPosition()) <= env.getMaxY() && data[cellIdx].getLat(data[cellIdx].getMaxYPosition()) >= env.getMinY())
	            		potentialCells.put(data[cellIdx], cellIdx);
	            }
	           

	            for (MeshCellInfo cellInfo : potentialCells.keySet()) {
	                if (canceled)
	                  return false;
	                  

	                Geometry cellPolygon = cellInfo.toGeometry();
	                // calculate the area of intersection
	                //factory.to
	               /*Geometry cellPolygon=factory.toGeometry(new Envelope
	            		   (i*cellWidth+westEdge,
	            		   (i+1)*cellWidth+westEdge,
	            		   j*cellHeight+southEdge,
	            		   (j+1)*cellHeight+southEdge));*/
	                
	               float intersectionArea = 0;
	               try {
	               Geometry intersection = poly.intersection(cellPolygon);
	               intersectionArea=(float)intersection.getArea();
	               } catch (Throwable t) {
	            	   t.printStackTrace();
	               }
	                if (intersectionArea > 0) {
	                  // get the area of the intersection
		              cellArray.add(potentialCells.get(cellInfo));
	                  areas.add(new Float(intersectionArea));
	                }

	              }
//	            }
	            // if there were some cells, make the arrays
	            if (!cellArray.isEmpty()) {
	            	
	              int[] cellIndex = new int[cellArray.size()];
	              float[] overlapArea = new float[cellArray.size()];
	              for (int i = 0; i < cellArray.size(); i++) {
	                cellIndex[i] = ((Integer)cellArray.get(i)).intValue();
	                // convert the area
	                overlapArea[i] = ((Float)areas.get(i)).floatValue() * (float)areaConversion;
	                didCalcs = true;
	              }
	              // set the areas for that grid
	              target.setAreaInfo(num, cellIndex, overlapArea);
	            }
	          }
//	          didCalcs = true;	// 2014 moved to calculate overlapArea[i]
	        }
	        else
	        	if (!didCalcs && target.overlapsMesh(num))
	        		didCalcs = true;
	        }
	        
	    } catch (Exception e) {
	      Logger.error("An exception occurred", e);
	      return false;
	    } finally {
	      //update();
	    }
	    
	    return didCalcs;	// 2014 had returned true; now calling program can test for success
	  }

  }
