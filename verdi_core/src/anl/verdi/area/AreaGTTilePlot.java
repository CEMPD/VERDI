/** AreaTilePlot.java - Draw an areal interpolation plot.
 * @author Mary Ann Bitz
 * @author Argonne National Lab
 */

package anl.verdi.area;

import gov.epa.emvl.Projector;
import gov.epa.emvl.TilePlot;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.text.NumberFormat;

import anl.verdi.area.target.GridInfo;
import anl.verdi.area.target.Target;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.gui.FastAreaTilePlot;

/**
 * @invariant startDate >= 1900001
 * @invariant startTime >= 0
 * @invariant timestepSize > 0
 * @invariant layers > 0
 * @invariant rows > 0
 * @invariant columns > 0
 * @invariant ! Numerics.isNan( xMinimum )
 * @invariant ! Numerics.isNan( yMinimum )
 * @invariant ! Numerics.isNan( cellWidth )
 * @invariant ! Numerics.isNan( cellHeight )
 * @invariant ! Numerics.isNan( xMaximum )
 * @invariant ! Numerics.isNan( yMaximum )
 * @invariant xMinimum > -1e30
 * @invariant yMinimum > -1e30
 * @invariant cellWidth >= 1.0
 * @invariant cellHeight >= 1.0
 * @invariant xMaximum = xMinimum + columns * cellWidth
 * @invariant yMaximum = yMinimum + rows    * cellHeight
 */

public class AreaGTTilePlot extends TilePlot{

	final double[][] domain;
	final double[][] gridBounds;
	final Projector projector;

	protected static final int AVERAGES=0; 
	protected static final int TOTALS=1; 
	protected static final int GRID=2; 
	protected int currentView=AVERAGES;
	protected FastAreaTilePlot tilePlotPanel;
	protected GridInfo gridInfo;
	
	public boolean mouseOverOK = false;
	
	private Cursor oldCursor = null;

	/** Constructor - inputs 2D grid parameters.
	 * @pre startDate >= 1900001
	 * @pre startTime >= 0
	 * @pre timestepSize > 0
	 */

	public AreaGTTilePlot(FastAreaTilePlot tilePlotPanel,int startDate, int startTime, int timestepSize, double[][] domain, double[][] gridBounds,
			Projector projector) {
		super(startDate,startTime,timestepSize);
		this.tilePlotPanel=tilePlotPanel;
		this.domain=domain;
		this.gridBounds=gridBounds;
		this.projector=projector;
		gridInfo = new GridInfo(gridBounds,domain);
	}

	public GridInfo getGridInfo(){
		return gridInfo;
	}

	/** draw - Draw a tile plot: colored rectangles with labels and legend.
	 * @pre graphics != null
	 * @pre xOffset > 0
	 * @pre xOffset < width
	 * @pre yOffset > 0
	 * @pre yOffset < height
	 * @pre width > 0
	 * @pre width < 5000
	 * @pre height > 0
	 * @pre height < 5000
	 * @pre timestep >= 0
	 * @pre layer >= 0
	 * @pre firstRow >= 0
	 * @pre firstRow <= lastRow
	 * @pre firstColumn >= 0
	 * @pre firstColumn <= lastColumn
	 * @pre legendLevels != null
	 * @pre legendLevels.length >= 2
	 * @pre legendColors != null
	 * @pre legendColors.length == legendLevels.length - 1
	 * @pre axisColor != null
	 * @pre labelColor != null
	 * @pre variable != null
	 * @pre units != null
	 * @pre data != null
	 * @pre data.length == (1 + lastRow - firstRow) * (1 + lastColumn-firstColumn)
	 */

//	@Override
	public void draw( final Graphics graphics,
			int xOffset, int yOffset,
			int width, int height,
			int timelapse, int layer,
			int firstRow, int lastRow,
			int firstColumn, int lastColumn,
			final double[] legendLevels,
			final Color[] legendColors,
			final Color axisColor,
			final Color labelColor,
			final String variable, String units,
			PlotConfiguration config, NumberFormat format,
			final Color gridLineColor, 
			final float[][] data ) {

		if (units==null || units.trim().equals(""))
			units = "none";
		
		final int yHeightOffset = height + yOffset;
		final int xMinimum = xOffset;
		final int xMaximum = xOffset + width;
		final int yMinimum = yOffset;
		final int yMaximum = yHeightOffset;
		this.config = config;
		this.numberFormat = format;

		// set converters for current grid units
		// test out the converters code
		//Target.setUnitConverters("kg/ha");
		//Target.setUnitConverters("mg/m3");
		//Target.setUnitConverters("PPM");
		Target.setUnitConverters(units);

		// set legend colors
		// Draw grid boundary rectangle, labeled row/column axis and legend:

		graphics.setColor( axisColor );

		drawGridBoundary( graphics, xMinimum, xMaximum, yMinimum, yMaximum );

		drawAxis( graphics, xMinimum, xMaximum, yMinimum, yMaximum,
				firstRow, lastRow, firstColumn, lastColumn );

		String u = config.getProperty(PlotConfiguration.UNITS);
		String massString=Units.getTotalVariable(units);

		Font gFont = graphics.getFont();

//		// Draw text label annotations (date-time, data min/max cells):
//
//		drawLabels( graphics, labelColor,xMinimum, xMaximum, yMinimum, yMaximum,
//				variable,
//				timelapse, layer, firstRow, lastRow, firstColumn, lastColumn,
//				data );

		// Draw legend-colored grid cells or polygons

		MapPolygon mapPolygon=new MapPolygon();	// 2015 REDO all related to MapPolygon
												// replace with GeoTools functionality
		//showBusyCursor();
		switch(currentView){
		case GRID:
//			tilePlotPanel.calculateGridLevels();
			drawGridCells( graphics, xMinimum, xMaximum, yMinimum, yMaximum,

					firstRow, lastRow, firstColumn, lastColumn,
					legendLevels, legendColors, data );
			// draw the area polygons
//			mapPolygon.draw(this, domain, gridBounds, projector,legendLevels,
//					legendColors,graphics, data,units,firstColumn,firstRow,
//					xOffset, yOffset, width, height,currentView, FastAreaTilePlot.isShowSelectedOnly());
			break;
		case AVERAGES:
			float[][] allData=tilePlotPanel.getAllLayerData();
			// draw the area polygons
//			mapPolygon.draw(this, domain, gridBounds, projector,legendLevels,
//					legendColors,graphics, allData,units,firstColumn,firstRow,
//					xOffset, yOffset, width, height,currentView,FastAreaTilePlot.isShowSelectedOnly() );
//			mouseOverOK = true;
//			tilePlotPanel.calculateAverageLevels();
//			mapPolygon.draw(this, domain, gridBounds, projector,legendLevels,
//					legendColors,graphics, allData,units,firstColumn,firstRow,
//					xOffset, yOffset, width, height,currentView,tilePlotPanel.isShowSelectedOnly() );
			break;
		case TOTALS:
			float[][] allData2=tilePlotPanel.getAllLayerData();
			// draw the area polygons
//			mapPolygon.calculateValues(this,domain, gridBounds, projector,legendLevels,
//					legendColors,graphics, allData2,units,firstColumn,firstRow,
//					xOffset, yOffset, width, height,currentView ,FastAreaTilePlot.isShowSelectedOnly() );
//			mouseOverOK = true;
//			tilePlotPanel.calculateTotalLevels();
//			mapPolygon.draw(this, domain, gridBounds, projector,legendLevels,
//					legendColors,graphics, allData2,units,firstColumn,firstRow,
//					xOffset, yOffset, width, height,currentView,FastAreaTilePlot.isShowSelectedOnly() );
			break;	
		}

		graphics.setFont(gFont);
		graphics.setColor( Color.BLACK );
		if(currentView==TOTALS){
			if(u!=null)config.setProperty(PlotConfiguration.UNITS,massString);
			drawLegend( graphics, xMaximum, yMinimum, yMaximum,
					legendLevels, legendColors, massString );
			if(u!=null)config.setProperty(PlotConfiguration.UNITS,u);
		}else{
			drawLegend( graphics, xMaximum, yMinimum, yMaximum,
					legendLevels, legendColors, units );
		}

		if ( gridLineColor != null ) {
			drawGridLines( graphics, xMinimum, xMaximum, yMinimum, yMaximum,
					firstRow, lastRow, firstColumn, lastColumn, gridLineColor);
		}
		
		//tilePlotPanel.draw();
		//restoreCursor();

	}

	public void showAverages() {
		currentView=AVERAGES;
	}
	public void showTotals() {
		currentView=TOTALS;
	}
	public void showGrid() {
		currentView=GRID;
	}

	public void updatePlot(){
		tilePlotPanel.recalculateAreas();
		tilePlotPanel.validate();
		tilePlotPanel.repaint();
	}

	public double[][] getDomain() {
		return domain;
	}

	public double[][] getGridBounds() {
		return gridBounds;
	}
	
	public void showBusyCursor() {
		oldCursor = tilePlotPanel.getCursor();
		tilePlotPanel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	}
	
	public void restoreCursor() {
		if ( oldCursor != null ) {
			tilePlotPanel.setCursor(oldCursor);
		} else {
			tilePlotPanel.setCursor(Cursor.getDefaultCursor());
		}
	}
}
