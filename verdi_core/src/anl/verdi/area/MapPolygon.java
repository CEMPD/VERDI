/** MapPolygon.java - Read and draw projected grid-clipped map polygons.
 * 2008-09-01 plessel.todd@epa.gov
 * javac Map*.java
 */

package anl.verdi.area;

import gov.epa.emvl.MPASTilePlot;
import gov.epa.emvl.Numerics;
import gov.epa.emvl.TilePlot;
import ucar.unidata.geoloc.Projection;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;

import org.geotools.data.FeatureSource;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.referencing.CRS;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.opengis.feature.Feature;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import anl.verdi.area.target.Target;
import anl.verdi.data.MeshCellInfo;
import anl.verdi.data.MeshDataReader;
import anl.verdi.plot.gui.VerdiBoundaries;
import anl.verdi.plot.gui.VerdiShapefileUtil;
import anl.verdi.plot.gui.VerdiStyle;

//import visad.Unit;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;


public class MapPolygon {
//	private java.awt.Color xorColor = new java.awt.Color(255,127,0);
	// Attributes:

	
	TilePlot tilePlot = null;
	
	private boolean cachedShowSelectedOnly = false;

	/*public static CoordinateReferenceSystem PLACEHOLDER_CRS = null;
	static {
		try {
			PLACEHOLDER_CRS = CRS.decode("EPSG:4326");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	public MapPolygon(TilePlot plot){
		tilePlot = plot;
	}
	
	public void draw( TilePlot plot,final double[][] domain, final double[][] gridBounds,
			final CoordinateReferenceSystem gridCRS, Projection projection, double[] legendLevels,Color[] legendColors, 
			final Graphics graphics,double[][] data,String units,int firstColumn,int firstRow,
			int xOffset, int yOffset, int width, int height,int currentView, boolean showSelectedOnly ) {
		draw(plot, domain, gridBounds, gridCRS, projection, legendLevels, legendColors, graphics, data, null, units,
				firstColumn, firstRow, xOffset, yOffset, width, height, currentView, showSelectedOnly);
	}
	
	// Draw domain-clipped projected grid-clipped polygons to graphics:

	public void draw( TilePlot plot,final double[][] domain, final double[][] gridBounds,
			final CoordinateReferenceSystem gridCRS, Projection projection, double[] legendLevels,Color[] legendColors, 
			final Graphics graphics,Object data, MeshDataReader reader, String units,int firstColumn,int firstRow,
			int xOffset, int yOffset, int width, int height,int currentView, boolean showSelectedOnly ) {
		
		Shape oldclip = graphics.getClip();
		graphics.setClip(new Rectangle((int)xOffset, (int)yOffset, (int)width, (int)height));
				
		MapContent vMap = new MapContent();
		Set<VerdiStyle> styles = Target.getSourceStyles();
		Style shapeStyle = null;
		
		ArrayList polygons=Target.getTargets();
		Target.setCurrentGridInfo(plot.getGridInfo());

		for(Target polygon:(ArrayList<Target>)polygons){
			if (!polygon.depositionCalculated())
				continue;
			if (polygon.containsDeposition()) {
				if (currentView == AreaTilePlot.AVERAGES || currentView == AreaTilePlot.GRID) {
					if (data instanceof float[][])
						polygon.calculateAverageDeposition((double[][])data);
					else
						polygon.calculateAverageDeposition((MeshCellInfo[])data, reader);
				}
				else if (currentView == AreaTilePlot.TOTALS) {
					if (data instanceof float[][])
						polygon.calculateTotalDeposition((double[][])data);
					else
						polygon.calculateTotalDeposition((MeshCellInfo[])data, reader);
				}
			}
		}


		
		for (VerdiStyle style : styles) {
			if (shapeStyle == null || cachedShowSelectedOnly == showSelectedOnly) {
				shapeStyle = style.buildRangeStyle(legendLevels, legendColors, showSelectedOnly);
				cachedShowSelectedOnly = showSelectedOnly;
			}
			
			//graphics.setColor(Color.BLACK); //TODO - vColor from VerdiBoundaries
						
			FeatureSource source = style.getFeatureSource();
		    source = VerdiShapefileUtil.projectShapefile(style.getShapePath(), (SimpleFeatureSource)source, projection, gridCRS, true);

			Layer aLayer = new FeatureLayer(source, shapeStyle);
			vMap.addLayer(aLayer);
			vMap.getViewport().setCoordinateReferenceSystem(gridCRS);
			ReferencedEnvelope displayBounds = new ReferencedEnvelope(gridBounds[0][0], gridBounds[0][1], gridBounds[1][0], gridBounds[1][1], gridCRS);
			vMap.getViewport().setBounds(displayBounds);

			GTRenderer renderer = new StreamingRenderer();
			renderer.setMapContent(vMap);
			Rectangle outputArea = new Rectangle(xOffset, yOffset, width, height);
			
			//TODO - Once we figure out how to draw directly onto graphics without making the existing image fade out, uncomment
			//this line and remove the d2d.drawImage() code.
			
			//renderer.paint((Graphics2D)graphics, outputArea, vMap.getViewport().getBounds());
			Graphics2D g2d = (Graphics2D)graphics;
			g2d.setComposite(((AlphaComposite)g2d.getComposite()).derive(AlphaComposite.SRC_OVER));
			java.awt.image.BufferedImage shapefile =  new java.awt.image.BufferedImage(width + xOffset, height + yOffset,
                    java.awt.image.BufferedImage.TYPE_INT_ARGB);
			Graphics2D gShape = (Graphics2D)shapefile.getGraphics();

			renderer.paint(gShape, outputArea, vMap.getViewport().getBounds());
			g2d.drawImage(shapefile, 0,  0, null);

		}
		
		graphics.setClip(oldclip);
		vMap.dispose();
		
	}


	//Draw domain-clipped projected grid-clipped polygons to graphics:

	/*public void calculateValues( AreaTilePlot plot,final double[][] domain, final double[][] gridBounds,
			final CoordinateReferenceSystem gridCRS,double[] legendLevels,Color[] legendColors, 
			final Graphics graphics,float[][] data,String units,int firstColumn,int firstRow,
			int xOffset, int yOffset, int width, int height,int currentView,boolean showSelectedOnly ) {

		if (units==null || units.trim().equals(""))
			units = "none";
		
		final int yHeightOffset = height + yOffset;

		final double xMinimum = gridBounds[ X ][ MINIMUM ];
		final double xMaximum = gridBounds[ X ][ MAXIMUM ];
		final double yMinimum = gridBounds[ Y ][ MINIMUM ];
		final double yMaximum = gridBounds[ Y ][ MAXIMUM ];

		final double xRange   = xMaximum - xMinimum;
		final double yRange   = yMaximum - yMinimum;
		final double xScale   = width  / xRange;
		final double yScale   = height / yRange;

		double[] t = { 0.0, 1.0 };*/

		
		/*
		// get the polygons from the target overlay
		ArrayList polygons=Target.getTargets();
		if(showSelectedOnly)polygons=Target.getSelectedTargets();
//		Target.setCurrentTilePlot(plot);
		Target.setCurrentGridInfo(plot.getGridInfo());

		for(Target polygon:(ArrayList<Target>)polygons){
			Geometry poly=polygon.getDataObject();
			if(poly instanceof MultiPolygon){
				for(int i=0;i<((MultiPolygon)poly).getNumGeometries();i++){
					Geometry geo=((MultiPolygon)poly).getGeometryN(i);
					// get the vertices
					Polygon geoPolygon=(Polygon)geo;
					Coordinate[] coords=geoPolygon.getCoordinates();
					// draw that geometry

					// calculate the cachedCoords and path
					if(!useCache){
						Coordinate[] cachedCoords = new Coordinate[coords.length];
						for(int j=0;j<coords.length;j++){
							projector.project( coords[j].x, coords[j].y, t );
							cachedCoords[j]=new Coordinate(( t[X] - xMinimum ) * xScale + xOffset + 0.5,
									yHeightOffset - ( ( t[Y] - yMinimum ) * yScale ) + 0.5);
						}

						// create a general path for it
						GeneralPath p =new GeneralPath();
						for(int j=0;j<coords.length;j++){
							if(j==0)p.moveTo(cachedCoords[0].x,cachedCoords[0].y);
							else p.lineTo(cachedCoords[j].x,cachedCoords[j].y);
						}

						p.closePath();
						geoPolygon.setUserData(p);
					}

					// draw the polygon using cachedCoords
					if(coords.length>0){

						// calculate the deposition and draw filled
						if(polygon.containsDeposition()&&currentView!=AreaTilePlot.GRID){
							if(currentView==AreaTilePlot.AVERAGES)polygon.calculateAverageDeposition(data);
							if(currentView==AreaTilePlot.TOTALS){
								polygon.calculateTotalDeposition(data);
							}
						}
					}
				}
			}
		}*/


	//}



	public static Target getTargetWithin(double lat,double lon  ) {

		// get the polygons from the target overlay
		ArrayList polygons=Target.getTargets();

		// make a point for the screen location
		Point pt= new GeometryFactory().createPoint(new Coordinate(lon,lat));
		for(Target polygon:(ArrayList<Target>)polygons){
			Geometry poly=polygon.getDataObject();
//			boolean isSelected = polygon.isSelectedPolygon();
			if(poly instanceof MultiPolygon){
				for(int i=0;i<((MultiPolygon)poly).getNumGeometries();i++){
					Geometry geo=((MultiPolygon)poly).getGeometryN(i);
					// get the vertices
					Polygon geoPolygon=(Polygon)geo;
//					Coordinate[] coords=geoPolygon.getCoordinates();
					if(geo.contains(pt))return polygon;
				}
			}

		}
		return null;
	}
	public static ArrayList<Target> getTargetsWithin(Rectangle rect) {

		// get the polygons from the target overlay
		ArrayList polygons=Target.getTargets();
		ArrayList matches=new ArrayList();	
		// make a point for the screen location

//		Geometry bounds = new GeometryFactory().toGeometry(
//				new Envelope(rect.getX(),rect.getX()+rect.getWidth(),
//						rect.getY(),rect.getY()+rect.getHeight()));
		for(Target polygon:(ArrayList<Target>)polygons){
			Geometry poly=polygon.getDataObject();

			if(poly instanceof MultiPolygon){
				for(int i=0;i<((MultiPolygon)poly).getNumGeometries();i++){
					Geometry geo=((MultiPolygon)poly).getGeometryN(i);
					// get the vertices
					Polygon geoPolygon=(Polygon)geo;
					GeneralPath coords=(GeneralPath)geoPolygon.getUserData();
					Rectangle boundBox = coords.getBounds();
					if(rect.contains(boundBox))matches.add(polygon);
				}
			}

		}
		return matches;
	}
}

