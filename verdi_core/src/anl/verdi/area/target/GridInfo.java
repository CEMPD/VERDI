/**
 * 
 */
package anl.verdi.area.target;

import gov.epa.emvl.Numerics;
import gov.epa.emvl.Projector;

import java.util.ArrayList;

import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.projection.LatLonProjection;
import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.Dataset;

import com.vividsolutions.jts.geom.Envelope;
/**
 * 
 * File Name:GridInfo.java
 * Description:
 * This object stores grid information for areas.
 * 
 * @version May, 2009
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 *
 */

public class GridInfo{
	  @Override
	public boolean equals(Object obj) {
	  if(obj instanceof GridInfo){
	  GridInfo grid=(GridInfo)(obj);
		if(gridBounds[X][MINIMUM]==grid.gridBounds[X][MINIMUM]&&
				gridBounds[X][MAXIMUM]==grid.gridBounds[X][MAXIMUM]&&
				gridBounds[Y][MINIMUM]==grid.gridBounds[Y][MINIMUM]&&
				gridBounds[Y][MAXIMUM]==grid.gridBounds[Y][MAXIMUM]&&
				domain[LONGITUDE][MINIMUM]==grid.domain[LONGITUDE][MINIMUM]&&
				domain[LONGITUDE][MAXIMUM]==grid.domain[LONGITUDE][MAXIMUM]&&
				domain[LATITUDE][MINIMUM]==grid.domain[LATITUDE][MINIMUM]&&
				domain[LATITUDE][MAXIMUM]==grid.domain[LATITUDE][MAXIMUM])return true;
	  }
					
		return false;
	}
	private static final int X = 0;
	  private static final int Y = 1;
	  private static final int MINIMUM = 0;
	  private static final int MAXIMUM = 1;
	  private static final int LONGITUDE = 0;
	  private static final int LATITUDE = 1;
	  double[][] gridBounds = { { 0.0, 0.0 }, { 0.0, 0.0 } };
	  double[][] domain = { { 0.0, 0.0 }, { 0.0, 0.0 } };
	  
		public GridInfo(double[][] gridBounds, double[][] domain){
			this.gridBounds[X][MINIMUM]=gridBounds[X][MINIMUM];
            this.gridBounds[X][MAXIMUM]=gridBounds[X][MAXIMUM];
			this.gridBounds[Y][MINIMUM]=gridBounds[Y][MINIMUM];
			this.gridBounds[Y][MAXIMUM]=gridBounds[Y][MAXIMUM];
			this.domain[LONGITUDE][MINIMUM]=domain[LONGITUDE][MINIMUM];
			this.domain[LONGITUDE][MAXIMUM]=domain[LONGITUDE][MAXIMUM];
			this.domain[LATITUDE][MINIMUM]=domain[LATITUDE][MINIMUM];
			this.domain[LATITUDE][MAXIMUM]=domain[LATITUDE][MAXIMUM];
		}
		
		static ArrayList<GridInfo> grids=new ArrayList<GridInfo>();
		
		static int addGrid(GridInfo grid){
		  grids.add(new GridInfo(grid.gridBounds,grid.domain));
		  return grids.size()-1;
		}
		public static int getGridNumber(GridInfo grid){
			
			for(int i=0;i<grids.size();i++){
				if(grids.get(i).equals(grid)){
					return i;
				}
			}
			return -1;
		}
		public static GridInfo formGridInfo(DataFrame dataFrame,int firstColumn,int lastColumn,int firstRow,int lastRow){
			final Dataset dataset = dataFrame.getDataset().get(0);
			final Axes<CoordAxis> coordinateAxes = dataset.getCoordAxes();
			Axes<DataFrameAxis> axes = dataFrame.getAxes();
			final Envelope envelope = coordinateAxes.getBoundingBox(dataset.getNetcdfCovn());
			final Projection projection = coordinateAxes.getProjection();
//			final double majorSemiaxis = 6370997.0; // UGLY: not in Projection.
//			final double majorSemiaxis = 6370000.0; // NOTE: Changed according to the communication between Todd Plessel and Donna Schwede on 	April 26, 2011
//			final double minorSemiaxis = majorSemiaxis;
			Projector projector=null;
			if (!(projection instanceof LatLonProjection)) {
				projector = new Projector(projection);
			}
			
			final DataFrameAxis rowAxis = axes.getYAxis();
			int rows,columns;
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

			double westEdge = envelope.getMinX(); // E.g., -420000.0.
			double southEdge = envelope.getMinY(); // E.g., -1716000.0.
			double cellWidth = Numerics.round1(envelope.getWidth() / columns); // 12000.0.
			double cellHeight = Numerics.round1(envelope.getHeight() / rows); // 12000.0.
			
			
			// Compute grid bounds and domain:
			double[][] gridBounds = { { 0.0, 0.0 }, { 0.0, 0.0 } };
			double[][] domain = { { 0.0, 0.0 }, { 0.0, 0.0 } };

			gridBounds[X][MINIMUM] = westEdge + firstColumn * cellWidth;
			gridBounds[X][MAXIMUM] = westEdge + (1 + lastColumn) * cellWidth;
			gridBounds[Y][MINIMUM] = southEdge + firstRow * cellHeight;
			gridBounds[Y][MAXIMUM] = southEdge + (1 + lastRow) * cellHeight;

			if (projector != null) {
				computeMapDomain(projector, gridBounds, domain);
			} else {
				domain[LONGITUDE][MINIMUM] = gridBounds[X][MINIMUM];
				domain[LONGITUDE][MAXIMUM] = gridBounds[X][MAXIMUM];
				domain[LATITUDE][MINIMUM] = gridBounds[Y][MINIMUM];
				domain[LATITUDE][MAXIMUM] = gridBounds[Y][MAXIMUM];
			}
			
			return new GridInfo(gridBounds,domain);
				
		}
		private static void computeMapDomain(final Projector projector,
				final double[][] gridBounds, double[][] mapDomain) {

			final double margin = 1.0; // Degrees lon/lat beyond grid corners.
			final double xMinimum = gridBounds[X][MINIMUM];
			final double xMaximum = gridBounds[X][MAXIMUM];
			final double yMinimum = gridBounds[Y][MINIMUM];
			final double yMaximum = gridBounds[Y][MAXIMUM];
			final double xMean = (xMinimum + xMaximum) * 0.5;
			;
			double[] longitudeLatitude = { 0.0, 0.0 };

			// Unproject corners of bottom edge of grid for latitude minimum:
			projector.unproject(xMinimum, yMinimum, longitudeLatitude);
			mapDomain[LONGITUDE][MINIMUM] = longitudeLatitude[LONGITUDE];
			mapDomain[LATITUDE][MINIMUM] = longitudeLatitude[LATITUDE];
			projector.unproject(xMaximum, yMinimum, longitudeLatitude);
			mapDomain[LONGITUDE][MAXIMUM] = longitudeLatitude[LONGITUDE];
			mapDomain[LATITUDE][MINIMUM] = Math.min(mapDomain[LATITUDE][MINIMUM],
					longitudeLatitude[LATITUDE]);

			// Unproject corners and center of top edge of grid for latitude
			// maximum:

			projector.unproject(xMinimum, yMaximum, longitudeLatitude);
			mapDomain[LONGITUDE][MINIMUM] = Math.min(mapDomain[LONGITUDE][MINIMUM],
					longitudeLatitude[LONGITUDE]);
			mapDomain[LATITUDE][MAXIMUM] = longitudeLatitude[LATITUDE];
			projector.unproject(xMaximum, yMaximum, longitudeLatitude);
			mapDomain[LONGITUDE][MAXIMUM] = Math.max(mapDomain[LONGITUDE][MAXIMUM],
					longitudeLatitude[LONGITUDE]);
			mapDomain[LATITUDE][MAXIMUM] = Math.max(mapDomain[LATITUDE][MAXIMUM],
					longitudeLatitude[LATITUDE]);

			if (Numerics.aboutEqual(mapDomain[LATITUDE][MINIMUM],
					mapDomain[LATITUDE][MAXIMUM])) {
				// Must be a polar projection.
				mapDomain[LATITUDE][MAXIMUM] = Numerics
						.sign(mapDomain[LATITUDE][MAXIMUM]) * 90.0;
				mapDomain[LONGITUDE][MINIMUM] = -180.0;
				mapDomain[LONGITUDE][MAXIMUM] = 180.0;
			} else { // Non-polar projection:
				projector.unproject(xMean, yMaximum, longitudeLatitude);
				mapDomain[LATITUDE][MAXIMUM] = Math.max(
						mapDomain[LATITUDE][MAXIMUM], longitudeLatitude[LATITUDE]);

				// Expand domain by margin all around, within valid range:

				mapDomain[LONGITUDE][MINIMUM] = Numerics.clamp(
						mapDomain[LONGITUDE][MINIMUM] - margin, -180.0, 180.0);
				mapDomain[LONGITUDE][MAXIMUM] = Numerics.clamp(
						mapDomain[LONGITUDE][MAXIMUM] + margin, -180.0, 180.0);
				mapDomain[LATITUDE][MINIMUM] = Numerics.clamp(
						mapDomain[LATITUDE][MINIMUM] - margin, -90.0, 90.0);
				mapDomain[LATITUDE][MAXIMUM] = Numerics.clamp(
						mapDomain[LATITUDE][MAXIMUM] + margin, -90.0, 90.0);
			}
		}

  }