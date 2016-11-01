/** TilePlot.java - Draw a tile plot.
 * 2008-09-01 plessel.todd@epa.gov
 */

package gov.epa.emvl; // TODO: log color legend

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

import javax.swing.ImageIcon;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.area.target.GridInfo;
import anl.verdi.data.DataUtilities;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.gui.ObsAnnotation;
import anl.verdi.plot.gui.ObsAnnotation.Symbol;
import anl.verdi.util.Tools;
import anl.verdi.util.Utilities;		// 2014 to handle footer date/time correctly

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
 * @invariant yMaximum = yMinimum + rows * cellHeight
 */

public class TilePlot {
	static final Logger Logger = LogManager.getLogger(TilePlot.class.getName());

	// Attributes:

	private static final int COLUMN = 0; // Index label.
	private static final int ROW = 1; // Index label.
	private static final int MINIMUM = 0; // Index label.
	private static final int MAXIMUM = 1; // Index label.

	final private GregorianCalendar startDate;
	final private long timestepSize; // Size of each timestep, ms.

	protected PlotConfiguration config;
	protected NumberFormat numberFormat;
	protected int layer = 0;
	protected int legendBoxWidth = 100;
	private List<ObsAnnotation> obsAnnotations;
	private boolean showObsLegend = false;
	private String plotTitle;
	protected int footerYOffset = 0;
	protected int titleOffset = 0;
	protected int xMinimum = 0;
	
	private int plotWidth = 0;
	private int plotHeight = 0;
	
	private boolean log = false;
	private int logBase = 10; //Math.E;
	protected int xTranslation = 0;
	
//	static int callInx = 1;

	/**
	 * Constructor - inputs 2D grid parameters.
	 * 
	 * @pre startDate >= 1900001
	 * @pre startTime >= 0
	 * @pre timestepSize > 0
	 */

	public TilePlot(GregorianCalendar startDate, long timestepSize) {
		this.startDate = startDate;
		this.timestepSize = timestepSize;
	}
	
	public void setObsLegend(List<ObsAnnotation> obsAnnot, boolean showLegend) {
		showObsLegend = showLegend;
		obsAnnotations = obsAnnot;
	}
	
	/**
	 * draw - Draw a tile plot: colored rectangles with labels and legend.
	 * @param numberFormat 
	 * 
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
	 * @pre data.length == (1 + lastRow - firstRow) * (1 +
	 *      lastColumn-firstColumn)
	 */
	
	public synchronized void draw(final Graphics graphics, int xOffset, int yOffset,
			int width, int height, int steplapse, int layer, int firstRow,
			int lastRow, int firstColumn, int lastColumn,
			final double[] legendLevels, final Color[] legendColors,
			final Color axisColor, final Color labelColor,
			final String variable, final String units,
			PlotConfiguration config, NumberFormat format,
			final Color gridLineColor, final float[][] data) {
		draw(graphics, xOffset, yOffset, width, height, steplapse, layer, firstRow,
				lastRow, firstColumn, lastColumn, legendLevels, legendColors, axisColor,
				labelColor, variable, units, config, format, gridLineColor, data, null);
	}
	
	public synchronized void draw(final Graphics graphics, int xOffset, int yOffset,
			int width, int height, int steplapse, int layer, int firstRow,
			int lastRow, int firstColumn, int lastColumn,
			final double[] legendLevels, final Color[] legendColors,
			final Color axisColor, final Color labelColor,
			final String variable, final String units,
			PlotConfiguration config, NumberFormat format,
			final Color gridLineColor, final float[][] data, final byte[][] colorIndexCache) 
	{
		Logger.debug("in gov.epa.emvl.TilePlot.draw(lots of parameters), thread = " + Thread.currentThread().toString());
		this.config = config;
		this.numberFormat = format;
		this.layer = layer;
		this.plotWidth = width;
		this.plotHeight = height;
		xMinimum = xOffset;
		final int xMaximum = xOffset + width;
		final int yMinimum = yOffset;
		final int yHeightOffset = yMinimum + height;
		final int yMaximum = yHeightOffset;

		// Draw grid boundary rectangle, labeled row/column axis and legend:
		Logger.debug("ready to call graphics.setColor");
		graphics.setColor(axisColor);
		Logger.debug("ready to call drawGridBoundary");
		drawGridBoundary(graphics, xMinimum, xMaximum, yMinimum, yMaximum);		// draw 4 lines
		Logger.debug("ready to call drawAxis");
		drawAxis(graphics, xMinimum, xMaximum, yMinimum, yMaximum, firstRow,
				lastRow, firstColumn, lastColumn);
		Logger.debug("ready to call drawLegend");
		drawLegend(graphics, xMaximum, yMinimum, yMaximum, legendLevels,
				legendColors, units);

		// Draw text label annotations (date-time, data min/max cells):
		Logger.debug("ready to call graphics.setColor");
		graphics.setColor(labelColor);
		Logger.debug("ready to call drawLabels");
		drawLabels(graphics, labelColor, xMinimum, xMaximum, yMinimum, yMaximum, variable,
				steplapse, layer, firstRow, lastRow, firstColumn, lastColumn,
				data);	// anl.verdi.gui.DatasetListModel - in DatasetListModel getElementAt

		// Draw legend-colored grid cells:
		Logger.debug("ready to call drawGridCells");
		drawGridCells(graphics, xMinimum, xMaximum, yMinimum, yMaximum,
				firstRow, lastRow, firstColumn, lastColumn, legendLevels,
				legendColors, data, colorIndexCache);
		
		// Draw grid lines:

		if ( gridLineColor != null ) {
			Logger.debug("ready to call drawGridLines");	// 2015 not printed in log file for fast tile plot
															// apparently gridLineColor == null
			drawGridLines( graphics, xMinimum, xMaximum, yMinimum, yMaximum,
				         firstRow, lastRow, firstColumn, lastColumn, gridLineColor);
		}
		Logger.debug("all done with TilePlot.draw");	// 2015 OK to here
	}
	
	public synchronized void drawBatchImage(final Graphics graphics, int xOffset, int yOffset,
			int canvasWidth, int canvasHeight, int steplapse, int layer, int firstRow,
			int lastRow, int firstColumn, int lastColumn,
			final double[] legendLevels, final Color[] legendColors,
			final Color axisColor, final Color labelColor,
			final String variable, final String units,
			PlotConfiguration config, NumberFormat format,
			final Color gridLineColor, final float[][] data
			)
	{

		this.config = config;
		this.numberFormat = format;
		final int xMinimum = xOffset;
		final int legendWidth = getLegendWidth(graphics, legendLevels, legendColors, units);
		final int footnoteHeight = getFootnoteHeight(graphics);
		int plotwidth = Math.min(canvasWidth - legendWidth - xOffset, canvasHeight - footnoteHeight - yOffset);
		final int plotSize = (plotwidth < 100) ? 100 : plotwidth;
		final int subsetRows = 1 + lastRow - firstRow;
		final int subsetColumns = 1 + lastColumn - firstColumn;
		final float subsetMax = Math.max(subsetRows, subsetColumns);
		final float rowScale = subsetRows / subsetMax;
		final float columnScale = subsetColumns / subsetMax;
		plotWidth = Math.round(plotSize * columnScale);
		plotHeight = Math.round(plotSize * rowScale);
		
		final int xMaximum = xMinimum + plotWidth;
		final int yMinimum = yOffset;
		final int yMaximum = yMinimum + plotHeight;
		
		if (canvasWidth < xMaximum + legendWidth || canvasHeight < yMaximum + footnoteHeight)
			Logger.debug("Your image size (" + canvasWidth + ", " + canvasHeight + ") was not set properly.");

		// Draw grid boundary rectangle, labeled row/column axis and legend:

		graphics.setColor(axisColor);

		drawGridBoundary(graphics, xMinimum, xMaximum, yMinimum, yMaximum);

		drawAxis(graphics, xMinimum, xMaximum, yMinimum, yMaximum, firstRow,
				lastRow, firstColumn, lastColumn);

		drawLegend(graphics, xMaximum, yMinimum, yMaximum, legendLevels,
				legendColors, units);

		// Draw text label annotations (date-time, data min/max cells):

		graphics.setColor(labelColor);

		drawLabels(graphics, labelColor, xMinimum, xMaximum, yMinimum, yMaximum, variable,
				steplapse, layer, firstRow, lastRow, firstColumn, lastColumn,
				data);

		// Draw legend-colored grid cells:

		drawGridCells(graphics, xMinimum, xMaximum, yMinimum, yMaximum,
				firstRow, lastRow, firstColumn, lastColumn, legendLevels,
				legendColors, data);
		
		// Draw grid lines:

		if ( gridLineColor != null ) {
		  drawGridLines( graphics, xMinimum, xMaximum, yMinimum, yMaximum,
				         firstRow, lastRow, firstColumn, lastColumn, gridLineColor);
		}
	}

	/**
	 * drawGridBoundary - draw grid boundary (edges).
	 * 
	 * @pre graphics != null
	 * @pre xMinimum > 0
	 * @pre yMinimum > 0
	 * @pre xMaximum > xMinimum
	 * @pre yMaximum > yMinimum
	 * @pre xMaximum < 5000
	 * @pre yMaximum < 5000
	 */

	protected void drawGridBoundary(final Graphics graphics, int xMinimum,
			int xMaximum, int yMinimum, int yMaximum) {

		graphics.drawLine(xMinimum, yMinimum, xMaximum, yMinimum);
		graphics.drawLine(xMaximum, yMinimum, xMaximum, yMaximum);
		graphics.drawLine(xMaximum, yMaximum, xMinimum, yMaximum);
		graphics.drawLine(xMinimum, yMaximum, xMinimum, yMinimum);
	}

	/**
	 * drawAxis - draw row/column axis with tic marks and text labels.
	 * 
	 * @pre graphics != null
	 * @pre xMinimum > 0
	 * @pre yMinimum > 0
	 * @pre xMaximum > xMinimum
	 * @pre yMaximum > yMinimum
	 * @pre xMaximum < 5000
	 * @pre yMaximum < 5000
	 * @pre firstRow >= 0
	 * @pre firstRow <= lastRow
	 * @pre firstColumn >= 0
	 * @pre firstColumn <= lastColumn
	 */

	protected void drawAxis(final Graphics graphics, int xMinimum, int xMaximum,
			int yMinimum, int yMaximum, int firstRow, int lastRow,
			int firstColumn, int lastColumn) {

		final int xAxisOffset = 5; // Pixel offset left of west edge of grid.
		final int yAxisOffset = 5; // Pixel offset below south edge of grid.
		final int xTicLength = 5; // Pixel legth of tic mark on X-axis.
		final int yTicLength = 3; // Pixel legth of tic mark on Y-axis.
		final int labelWidth = 25; // Pixel width of axis tic-mark text labels.
		final int labelHeight = 13; // Pixel height of axis tic-mark text labels.
		final int xAxis = xMinimum - xAxisOffset; // X-coordinate of Y-axis.
		final int yAxis = yMaximum + yAxisOffset; // Y-coordinate of X-axis.
		final int rows = 1 + lastRow - firstRow;
		final int columns = 1 + lastColumn - firstColumn;
		final float xRange = xMaximum - xMinimum;
		final float yRange = yMaximum - yMinimum;
		final float columnSize = xRange / columns; // Pixels per column.
		final float rowSize = yRange / rows; // Pixels per row.

		final float xDelta = labelWidth * 3;
		final float yDelta = labelHeight * 2;
		final int xTics = Math.min(columns, replaceRound(xRange / xDelta + 0.5f));
		final int yTics = Math.min(rows, replaceRound(yRange / yDelta + 0.5f));
		final int columnStep = columns / xTics;
		final int rowStep = rows / yTics;
		final int fSize = graphics.getFont().getSize();
		final int xspace = 6; // Space between two visual components
		final int yspace = 16; // Space between two visual components
		final Color gColor = graphics.getColor();
		final Font gFont = graphics.getFont();

		String domainX = config.getString(PlotConfiguration.DOMAIN_LABEL);
		Boolean dShowTick = (Boolean) config.getObject(PlotConfiguration.DOMAIN_SHOW_TICK);
		Integer dLabelCnt = (Integer) config.getObject(TilePlotConfiguration.DOMAIN_TICK_NUMBER);
		Color domainClr = config.getColor(PlotConfiguration.DOMAIN_COLOR);
		Color domainTickClr = config.getColor(PlotConfiguration.DOMAIN_TICK_COLOR);
		Font domainFont = config.getFont(PlotConfiguration.DOMAIN_FONT);
		Font domainTickFont = config.getFont(PlotConfiguration.DOMAIN_TICK_FONT);
		dShowTick = (dShowTick == null ? true : dShowTick);
		boolean[] dLevelValues = getLevelValues(dLabelCnt == null ? xTics : dLabelCnt, columns / columnStep + 1);
		
		String rangeY = config.getString(PlotConfiguration.RANGE_LABEL);
		Boolean rShowTick = (Boolean) config.getObject(PlotConfiguration.RANGE_SHOW_TICK);
		Color rangeClr = config.getColor(PlotConfiguration.RANGE_COLOR);
		Color rangeTickClr = config.getColor(PlotConfiguration.RANGE_TICK_COLOR);
		Font rangeFont = config.getFont(PlotConfiguration.RANGE_FONT);
		Font rangeTickFont = config.getFont(PlotConfiguration.RANGE_TICK_FONT);
		Integer rLabelCnt = (Integer) config.getObject(TilePlotConfiguration.RANGE_TICK_NUMBER);
		rShowTick = (rShowTick == null ? true : rShowTick);
		boolean[] rLevelValues = getLevelValues(rLabelCnt == null ? yTics : rLabelCnt, rows / rowStep + 1);

		if (dShowTick) graphics.drawLine(xMinimum, yAxis, xMaximum, yAxis); // X-axis.
		if (rShowTick) graphics.drawLine(xAxis, yMinimum, xAxis, yMaximum); // Y-axis.

		// Draw tic marks and labels on Y-axis:
		if (rangeTickClr != null) graphics.setColor(rangeTickClr);
		if (rangeTickFont != null) graphics.setFont(rangeTickFont);
		int maxLblWidth = 0;
		int tickLblBase = xAxis - yTicLength - xspace;
		int i = 0;
		
		for (int row = firstRow; row <= lastRow; row += rowStep) {
			final int offsetRow = row - firstRow;
			final int yTic = yMaximum - replaceRound(offsetRow * rowSize + 0.5f);
			final String label;
			label = Integer.toString(row + 1);
			final int lblWidth = graphics.getFontMetrics().stringWidth(label);
			final int xpostn = tickLblBase - lblWidth;
			if (rShowTick && rLevelValues[i]) graphics.drawString(label, xpostn, yTic + fSize / 2);
			if (rShowTick) graphics.drawLine(xAxis, yTic, xAxis - yTicLength, yTic);
			if (maxLblWidth < lblWidth) maxLblWidth = lblWidth;
			i++;
		}
		
		// Resume graphics default settings
		graphics.setColor(gColor);
		graphics.setFont(gFont);
		
		// Draw Range label if necessary
		if (rangeY != null && !rangeY.trim().isEmpty()) {
			if (rangeClr != null) graphics.setColor(rangeClr);
			if (rangeFont != null) graphics.setFont(rangeFont);
			int rYStartX = tickLblBase - maxLblWidth - graphics.getFontMetrics().getHeight() / 2;
			int rYStartY = yAxis - (int)yRange / 2;
			final double theta = Math.toRadians(90.0);
			graphics.translate(rYStartX, rYStartY);
			((Graphics2D)graphics).rotate(-theta);
			graphics.drawString(rangeY.trim(), 0, 0);
			((Graphics2D)graphics).rotate(theta);
			graphics.translate(-rYStartX, -rYStartY);
			
			// Resume graphics default settings
			graphics.setColor(gColor);
			graphics.setFont(gFont);
		}
		
		// Draw tic marks and labels on X-axis:
		if (domainTickClr != null) graphics.setColor(domainTickClr);
		if (domainTickFont != null) graphics.setFont(domainTickFont);
		int dTickYBase = yAxis + + xTicLength;
        i = 0;
        
		for (int column = firstColumn; column <= lastColumn; column += columnStep) {
			final int offsetColumn = column - firstColumn;
			final int xTic = xMinimum
					+ replaceRound(offsetColumn * columnSize + 0.5f);
			final String label;
			label = Integer.toString(column + 1);
			final int xpostn = xTic - graphics.getFontMetrics().stringWidth(label) / 2;
			if (dShowTick && dLevelValues[i]) graphics.drawString(label, xpostn, dTickYBase + yspace);
			if (dShowTick) graphics.drawLine(xTic, yAxis, xTic, dTickYBase);
			i++;
		}
		
		footerYOffset = xTicLength + yspace + graphics.getFontMetrics().getHeight() + yspace;
		
		// Resume graphics default settings
		graphics.setColor(gColor);
		graphics.setFont(gFont);
		
		// Draw domain label if necessary
		if (domainX != null && !domainX.trim().isEmpty()) {
			if (domainClr != null) graphics.setColor(domainClr);
			if (domainFont != null) graphics.setFont(domainFont);
			final int dLabelX = xAxis + (int)xRange / 2;
			final int dLabelY = dTickYBase + yspace + graphics.getFontMetrics().getHeight();
			graphics.drawString(domainX, dLabelX, dLabelY);
			footerYOffset += graphics.getFontMetrics().getHeight() + yspace;
			
			// Resume graphics default settings
			graphics.setColor(gColor);
			graphics.setFont(gFont);
		}
		
		config.putObject(PlotConfiguration.DOMAIN_LABEL, domainX);
		config.putObject(PlotConfiguration.DOMAIN_FONT, domainFont == null ? gFont : domainFont);
		config.putObject(PlotConfiguration.DOMAIN_COLOR, domainClr == null ? gColor : domainClr);
		config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, dShowTick);
		config.putObject(PlotConfiguration.DOMAIN_TICK_NUMBER, dLabelCnt != null ? dLabelCnt : columns / columnStep + 1);
		config.putObject(PlotConfiguration.DOMAIN_TICK_COLOR, domainTickClr == null ? gColor : domainTickClr);
		config.putObject(PlotConfiguration.DOMAIN_TICK_FONT, domainTickFont == null ? gFont : domainTickFont);
		
		config.putObject(PlotConfiguration.RANGE_LABEL, rangeY);
		config.putObject(PlotConfiguration.RANGE_FONT, rangeFont == null ? gFont : rangeFont);
		config.putObject(PlotConfiguration.RANGE_COLOR, rangeClr == null ? gColor : rangeClr);
		config.putObject(PlotConfiguration.RANGE_SHOW_TICK, rShowTick);
		config.putObject(PlotConfiguration.RANGE_TICK_NUMBER, rLabelCnt != null ? rLabelCnt : rows / rowStep + 1);
		config.putObject(PlotConfiguration.RANGE_TICK_COLOR, rangeTickClr == null ? gColor : rangeTickClr);
		config.putObject(PlotConfiguration.RANGE_TICK_FONT, rangeTickFont == null ? gFont : rangeTickFont);
	}
	
	public int getFootnoteHeight(final Graphics graphics) {

		final Font gFont = graphics.getFont();
		int footnoteHeight = 0;
		
		final int yAxisOffset = 5; // Pixel offset below south edge of grid.
		final int xTicLength = 5; // Pixel legth of tic mark on X-axis.
		final int yAxis = yAxisOffset; // Y-coordinate of X-axis.

		final int yspace = 16; // Space between two visual components
		footnoteHeight += yAxis + xTicLength + yspace;

		String domainX = config.getString(PlotConfiguration.DOMAIN_LABEL);
		Font domainFont = config.getFont(PlotConfiguration.DOMAIN_FONT);
		Font domainTickFont = config.getFont(PlotConfiguration.DOMAIN_TICK_FONT);
		
		// Add ticks space
		footnoteHeight += graphics.getFontMetrics((domainTickFont == null) ? gFont : domainTickFont).getHeight() + yspace;
		
		// Add domain label space if necessary
		if (domainX != null && !domainX.trim().isEmpty())
			footnoteHeight += graphics.getFontMetrics((domainFont == null) ? gFont : domainFont).getHeight() + yspace;
		
		// Footer configurations:
		Boolean showFooter1 = (Boolean)config.getObject(PlotConfiguration.FOOTER1_SHOW_LINE);
		Font f1Font = config.getFont(PlotConfiguration.FOOTER1_FONT);
		Boolean showFooter2 = (Boolean)config.getObject(PlotConfiguration.FOOTER2_SHOW_LINE);
		Font f2Font = config.getFont(PlotConfiguration.FOOTER2_FONT);
		Boolean showObs = (Boolean)config.getObject(PlotConfiguration.OBS_SHOW_LEGEND);
		Font obsFont = config.getFont(PlotConfiguration.OBS_LEGEND_FONT);
		
		if (showObs == null || !(showObsLegend || showObs) || obsAnnotations == null || obsAnnotations.size() == 0) 
			showObs = false;
		
		// Timestamp label:
		final Font timestampFont = new Font(gFont.getFontName(), Font.BOLD, gFont.getSize());
		
		// Add time stamp and min/max string space
		if (showFooter1 == null || showFooter1) 
			footnoteHeight += graphics.getFontMetrics((f1Font == null) ? timestampFont : f1Font).getHeight() + yspace;
		
		if (showFooter2 == null || showFooter2) 
			footnoteHeight += graphics.getFontMetrics((f2Font == null) ? timestampFont : f2Font).getHeight() + yspace;
		
		if (showObs) footnoteHeight += graphics.getFontMetrics((obsFont == null) ? timestampFont : f2Font).getHeight() + yspace;

		return footnoteHeight;
	}

	/**
	 * drawLabels - draw text label annotations.
	 * @param labelColor 
	 * 
	 * @pre graphics != null
	 * @pre xMinimum > 0
	 * @pre yMinimum > 0
	 * @pre xMaximum > xMinimum
	 * @pre yMaximum > yMinimum
	 * @pre xMaximum < 5000
	 * @pre yMaximum < 5000
	 * @pre variable != null
	 * @pre timestep >= 0
	 * @pre layer >= 0
	 * @pre firstRow >= 0
	 * @pre firstRow <= lastRow
	 * @pre firstColumn >= 0
	 * @pre firstColumn <= lastColumn
	 * @pre data != null
	 * @pre data.length == (1 + lastRow - firstRow) * (1 +
	 *      lastColumn-firstColumn)
	 */

	protected void drawLabels(final Graphics graphics, Color labelColor, int xMinimum,
			int xMaximum, int yMinimum, int yMaximum, final String variable,
			int steplapse, int layer, int firstRow, int lastRow,
			int firstColumn, int lastColumn, final float[][] data) {
		// NOTE: steplapse = timestep - firstTimestep (from FastTilePlot.java code)
		// where firstTimestep is either 0 or the origin of the time axis

		final int xRange = xMaximum - xMinimum;
		final int xCenter = xMinimum + xRange / 2;
		final int space = 20; // Space between visual components
		final Font gFont = graphics.getFont();
		final Color gColor = graphics.getColor();
		String title;
		
		// Title label:
		String TITLE = config.getProperty(PlotConfiguration.TITLE);
//		final String titleStr = "Layer " + (layer + 1) + " " + variable;	// do NOT want to recalculate title here
																			// part of bug where user blanks title and it shows up again
		String defaultTitle = "Layer " + (layer + 1) + " " + variable;
		String theTitle = config.getTitle();
		Font tFont = config.getFont(PlotConfiguration.TITLE_FONT);
		Color tColor = config.getColor(PlotConfiguration.TITLE_COLOR);
		tColor = (tColor == null) ? labelColor : tColor;
		String sTitle1 = config.getSubtitle1();
		Font sFont1 = config.getFont(PlotConfiguration.SUBTITLE_1_FONT);
		sFont1 = (sFont1 == null) ? gFont : sFont1;
		Color sColor1 = config.getColor(PlotConfiguration.SUBTITLE_1_COLOR);
		sColor1 = (sColor1 == null) ? labelColor : sColor1;
		String sTitle2 = config.getSubtitle2();
		Font sFont2 = config.getFont(PlotConfiguration.SUBTITLE_2_FONT);
		sFont2 = (sFont2 == null) ? gFont : sFont2;
		Color sColor2 = config.getColor(PlotConfiguration.SUBTITLE_2_COLOR);
		sColor2 = (sColor2 == null) ? labelColor : sColor2;
		//Evaluate Title Naming Structure, if Layer # (where # is the layer number) then update to current layer
		//Keep with the pattern of Layer Number, i.e., Layer 1 then Layer 2 ....
		//look for Layer 1, if present keep with the same trend but update with current the Layer Number
//		final String title = (TITLE == null || TITLE.isEmpty() ? titleStr : TITLE).replaceAll("\\b(?i)Layer\\b\\s\\b\\d+\\b", "Layer " + (layer + 1));

		if(TITLE == null || TITLE.length()<2)	// rest of fix for allowing user to blank out a title
			title = defaultTitle; // TODO: needs more work
		else
			title = TITLE.replaceAll("\\b(?i)Layer\\b\\s\\b\\d+\\b", "Layer " + (layer + 1));
		
		Font currentFont = new Font(gFont.getFontName(), Font.BOLD, gFont.getSize() * 2);
		tFont = (tFont == null ? currentFont : tFont);
		FontMetrics tMetrx = graphics.getFontMetrics(tFont);
		final int xTitle = xCenter - tMetrx.stringWidth(title) / 2;
		int yTitle = space - 5 + tFont.getSize() / 2;
		plotTitle = title;
		
		// Footer configurations:
		Boolean showFooter1 = (Boolean)config.getObject(PlotConfiguration.FOOTER1_SHOW_LINE);
		Boolean footer1AutoText = (Boolean)config.getObject(PlotConfiguration.FOOTER1_AUTO_TEXT);
		String footer1 = config.getString(PlotConfiguration.FOOTER1);
		Color f1Color = config.getColor(PlotConfiguration.FOOTER1_COLOR);
		Font f1Font = config.getFont(PlotConfiguration.FOOTER1_FONT);
		Boolean showFooter2 = (Boolean)config.getObject(PlotConfiguration.FOOTER2_SHOW_LINE);
		Boolean footer2AutoText = (Boolean)config.getObject(PlotConfiguration.FOOTER2_AUTO_TEXT);
		String footer2 = config.getString(PlotConfiguration.FOOTER2);
		Color f2Color = config.getColor(PlotConfiguration.FOOTER2_COLOR);
		Font f2Font = config.getFont(PlotConfiguration.FOOTER2_FONT);
		Boolean showObs = (Boolean)config.getObject(PlotConfiguration.OBS_SHOW_LEGEND);
		Color obsColor = config.getColor(PlotConfiguration.OBS_LEGEND_COLOR);
		Font obsFont = config.getFont(PlotConfiguration.OBS_LEGEND_FONT);
		
		if (showObs == null || !(showObsLegend || showObs) || obsAnnotations == null || obsAnnotations.size() == 0) 
			showObs = false;
		
		graphics.setFont(tFont);
		graphics.setColor(tColor);
		graphics.drawString(title, xTitle, yTitle);
		
		if (sTitle1 != null && !sTitle1.trim().isEmpty()) {
			graphics.setFont(sFont1);
			graphics.setColor(sColor1);
			FontMetrics sMetrx1 = graphics.getFontMetrics(sFont1);
			int xsTitle = xCenter - sMetrx1.stringWidth(sTitle1) / 2;
			titleOffset = sMetrx1.stringWidth(sTitle1) / 2;
			graphics.drawString(sTitle1, xsTitle, yTitle + space + sFont1.getSize() / 2);
		}

		if (sTitle2 != null && !sTitle2.trim().isEmpty()) {
			graphics.setFont(sFont2);
			graphics.setColor(sColor2);
			FontMetrics sMetrx2 = graphics.getFontMetrics(sFont2);
			int xsTitle = xCenter - sMetrx2.stringWidth(sTitle2) / 2;
			int title2Offset = xsTitle + sMetrx2.stringWidth(sTitle2);
			if (title2Offset > titleOffset)
				titleOffset = title2Offset;
			graphics.drawString(sTitle2, xsTitle, yTitle + space * 2 + sFont2.getSize() / 2);
		}

		// Timestamp label:
		final Font timestampFont = new Font(gFont.getFontName(), Font.BOLD, gFont.getSize());
		final String timestamp = dateTime(startDate, timestepSize, steplapse);
		final int yTimestamp = yMaximum + footerYOffset;
		
		if (footer1AutoText == null || footer1 == null || footer1.trim().isEmpty()) footer1AutoText = true;
		
		if (showFooter1 == null) showFooter1 = true;
			
		if (footer1AutoText) footer1 = timestamp;
			
		if (showFooter1) {
			if (f1Color == null) graphics.setColor(labelColor);
			else graphics.setColor(f1Color);
			
			if (f1Font == null) graphics.setFont(timestampFont);
			else graphics.setFont(f1Font);
			
			int xTimestamp = xCenter - graphics.getFontMetrics().stringWidth(footer1) / 2;

			graphics.drawString(footer1, xTimestamp, yTimestamp);
			graphics.setColor(gColor);
			graphics.setFont(gFont);
		}

		// Range label:

		String minMaxLabel = getMinMaxLabel(firstRow, lastRow, firstColumn, lastColumn, data);
		
		final int yMinMax = yTimestamp + graphics.getFontMetrics().getHeight() + 5;
		footerYOffset += graphics.getFontMetrics().getHeight() + 5;

		if (footer2AutoText == null || footer2 == null || footer2.trim().isEmpty()) footer2AutoText = true;
		
		if (showFooter2 == null) showFooter2 = true;
			
		if (footer2AutoText) footer2 = minMaxLabel;
		
		if (showFooter2) {
			if (f2Color == null) graphics.setColor(labelColor);
			else graphics.setColor(f2Color);
			
			if (f2Font == null) graphics.setFont(timestampFont);
			else graphics.setFont(f2Font);
			
			int xMinMax = xCenter - graphics.getFontMetrics().stringWidth(footer2) / 2;
			if (xMinMax + xTranslation < xMinimum/2) xMinMax = xMinimum/2;
			graphics.drawString(footer2, xMinMax, yMinMax);
			graphics.setColor(gColor);
			graphics.setFont(gFont);
		}
		
		if (showObs) {
			if (obsColor != null) graphics.setColor(obsColor);
			if (obsFont != null) graphics.setFont(obsFont);
			drawObsLegend(graphics, xMinimum, xMaximum, yMinimum, yMaximum, footerYOffset);
			graphics.setColor(gColor);
			graphics.setFont(gFont);
		}

		graphics.setFont(currentFont); // Restore original font.

		config.setShowTitle("TRUE");
		config.putObject(PlotConfiguration.TITLE, title);
//		config.putObject(PlotConfiguration.TITLE, (!title.equals(titleStr) && preLayer == layer) ? title : "");
		config.putObject(PlotConfiguration.TITLE_FONT, tFont);
		config.putObject(PlotConfiguration.TITLE_COLOR, tColor);
		config.setShowSubtitle1("TRUE");
		config.putObject(PlotConfiguration.SUBTITLE_1, (sTitle1 == null) ? "" : sTitle1);
		config.putObject(PlotConfiguration.SUBTITLE_1_COLOR, sColor1);
		config.putObject(PlotConfiguration.SUBTITLE_1_FONT, sFont1);
		config.setShowSubtitle2("TRUE");
		config.putObject(PlotConfiguration.SUBTITLE_2, (sTitle2 == null) ? "" : sTitle2);
		config.putObject(PlotConfiguration.SUBTITLE_2_COLOR, sColor2);
		config.putObject(PlotConfiguration.SUBTITLE_2_FONT, sFont2);
		config.putObject(PlotConfiguration.FOOTER1, footer1);
		config.putObject(PlotConfiguration.FOOTER1_SHOW_LINE, showFooter1);
		config.putObject(PlotConfiguration.FOOTER1_AUTO_TEXT, footer1AutoText);
		config.putObject(PlotConfiguration.FOOTER1_COLOR, (f1Color == null) ? labelColor : f1Color);
		config.putObject(PlotConfiguration.FOOTER1_FONT, (f1Font == null) ? timestampFont : f1Font);
		config.putObject(PlotConfiguration.FOOTER2, footer2);
		config.putObject(PlotConfiguration.FOOTER2_SHOW_LINE, showFooter2);
		config.putObject(PlotConfiguration.FOOTER2_AUTO_TEXT, footer2AutoText);
		config.putObject(PlotConfiguration.FOOTER2_COLOR, (f2Color == null) ? labelColor : f2Color);
		config.putObject(PlotConfiguration.FOOTER2_FONT, (f2Font == null) ? timestampFont : f2Font);
		config.putObject(PlotConfiguration.OBS_SHOW_LEGEND, showObs);
		config.putObject(PlotConfiguration.OBS_LEGEND_COLOR, (obsColor == null) ? labelColor : obsColor);
		config.putObject(PlotConfiguration.OBS_LEGEND_FONT, (obsFont == null) ? gFont : obsFont);
	}
	
	protected String getMinMaxLabel(int firstRow, int lastRow,
			int firstColumn, int lastColumn, final float[][] data) {
		final int[] minimumCell = { 0, 0 };
		final int[] maximumCell = { 0, 0 };
		final float[] range = { 0.0f, 0.0f };

		layerMinimumMaximum(firstRow, lastRow, firstColumn, lastColumn, data,
				minimumCell, maximumCell, range);

		return "Min (" + (1 + minimumCell[COLUMN]) + ", "
				+ (1 + minimumCell[ROW]) + ") = " + gFormat(range[0]) + ", "
				+ "Max (" + (1 + maximumCell[COLUMN]) + ", "
				+ (1 + maximumCell[ROW]) + ") = " + gFormat(range[1]);
	}

	private void drawObsLegend(Graphics g, int xmin, int xmax, int ymin, int ymax, int top) {
		final int charWidth = 10; // Of level value characters in pixels.
		final int charHeight = 8; // Of level value characters in pixels.
		final int halfCharHeight = 4;
		int legendLen = 0;
		
		String pathName = Tools.getIconsDir();
		String fileCircle = new String(pathName + "circle.png");
		String fileDiamond = new String(pathName + "diamond.png");
		String fileSquare = new String(pathName + "square.png");
		String fileStar = new String(pathName + "star.png");
		String fileSun = new String(pathName + "sun.png");
		String fileTriangle = new String(pathName + "triangle.png");
		
		HashMap<Symbol, ImageIcon> symbols = new HashMap<Symbol, ImageIcon>();
		symbols.put(Symbol.CIRCLE, new ImageIcon(fileCircle));
		symbols.put(Symbol.DIAMOND, new ImageIcon(fileDiamond));
		symbols.put(Symbol.SQUARE, new ImageIcon(fileSquare));
		symbols.put(Symbol.STAR, new ImageIcon(fileStar));
		symbols.put(Symbol.SUN, new ImageIcon(fileSun));
		symbols.put(Symbol.TRIANGLE, new ImageIcon(fileTriangle));
		
		HashMap<String, ImageIcon> legends = new HashMap<String, ImageIcon>();
		List<String> names = new ArrayList<String>();
		
		for (ObsAnnotation obs : obsAnnotations) {
			String temp = " -- " + obs.getVarString();
			ImageIcon icon = symbols.get(obs.getSymbol());
			legends.put(temp, icon);
			names.add(temp);
			legendLen += temp.length() * charWidth + icon.getIconWidth() + charWidth * 2;
		}
		
		if (legendLen > xmax - xmin) legendLen = xmax - xmin;

		int startx = (xmax - xmin - legendLen)/2 + xmin;
		int y = ymax + top + charHeight;
		g.drawString("Symbol" + (names.size() > 1 ? "s:" : ":"), startx, y + charHeight + halfCharHeight);
		startx += charWidth * 9;
		
		for (String name : names) {
			ImageIcon icon = legends.get(name);
			g.drawImage(icon.getImage(), startx, y, null);
			g.drawString(name, startx + icon.getIconWidth(), y + charHeight + halfCharHeight);
			startx += (name.length()) * charWidth;
		}
		
		footerYOffset += g.getFontMetrics().getHeight() + 5;
	}
	
	public int getLegendWidth(final Graphics graphics, final double[] legendLevels, final Color[] legendColors, final String units) {
		Boolean uShowTick = (Boolean) config.getObject(PlotConfiguration.UNITS_SHOW_TICK);
		uShowTick = (uShowTick == null ? true : uShowTick);
		Font labelFont = config.getFont(TilePlotConfiguration.UNITS_TICK_FONT);
		Font unitsFont = config.getFont(TilePlotConfiguration.UNITS_FONT);
		
		final int binWidth = 20; // Of color bar in pixels.
		final int ticSize = 3; // Of level tic marks in pixels.
		int space = 6; // Space between two visual components.
		final Font gFont = graphics.getFont();
		final int xOffset = binWidth;

		// Estimate the margin between the plot and the legend
		String maxLenLabel = "";
		
		for (int color = 0; color <= legendColors.length; ++color) {
			String label = gFormat(legendLevels[color]);
			if (maxLenLabel.length() < label.length()) maxLenLabel = label;
		}
		
		int maxLabelLen = graphics.getFontMetrics(labelFont == null ? gFont : labelFont).stringWidth(maxLenLabel);
		int unitHeight = graphics.getFontMetrics(unitsFont == null ? gFont : unitsFont).getHeight();
		
		int legendBoxWidth = 0;
		
		if (uShowTick) 
			legendBoxWidth += unitHeight + unitHeight/2 + space + maxLabelLen + space + ticSize + binWidth + unitHeight / 2;
		else
			legendBoxWidth += unitHeight + unitHeight + space + binWidth + unitHeight / 2;

		return xOffset + legendBoxWidth + space;
	}

	/**
	 * drawLegend - draw colorbar with text labels.
	 * 
	 * @pre graphics != null
	 * @pre yMinimum > 0
	 * @pre yMaximum > yMinimum
	 * @pre xMaximum < 5000
	 * @pre yMaximum < 5000
	 * @pre legendLevels != null
	 * @pre legendLevels.length >= 2
	 * @pre legendColors != null
	 * @pre legendColors.length == legendLevels.length - 1
	 * @pre units != null
	 */

	protected void drawLegend(final Graphics graphics, int xMaximum,
			int yMinimum, int yMaximum, final double[] legendLevels,
			final Color[] legendColors, final String units) {
		Boolean showLegend = (Boolean) config.getObject(PlotConfiguration.LEGEND_SHOW);
		showLegend = (showLegend == null ? true : showLegend);
		String unitStr = config.getProperty(PlotConfiguration.UNITS); 
		unitStr = (unitStr == null || unitStr.isEmpty() ? units : unitStr); // Must use units if grid cell statistics.
		String logStr = " (Log";
		String baseStr; 	
		String unitStrAll = unitStr;
		AttributedString as = null;
		
		Boolean uShowTick = (Boolean) config.getObject(PlotConfiguration.UNITS_SHOW_TICK);
		uShowTick = (uShowTick == null ? true : uShowTick);
		Color uTickColor = (Color) config.getObject(PlotConfiguration.UNITS_TICK_COLOR);
		uTickColor = (uTickColor == null ? Color.BLACK : uTickColor);
		Integer labelCnt = (Integer) config.getObject(TilePlotConfiguration.UNITS_TICK_NUMBER);
		Font labelFont = config.getFont(TilePlotConfiguration.UNITS_TICK_FONT);
		Font unitsFont = config.getFont(TilePlotConfiguration.UNITS_FONT);
		Color unitsClr = (Color)config.getObject(TilePlotConfiguration.UNITS_COLOR);
		
		final int binWidth = 20; // Of color bar in pixels.
		final int ticSize = 3; // Of level tic marks in pixels.
		int space = 6; // Space between two visual components.
		final Color currentColor = graphics.getColor();
		final Font gFont = graphics.getFont();
		final int yRange = yMaximum - yMinimum;
		final int colors = legendColors.length;
		final int binHeight = yRange / colors;
		final int xOffset = binWidth;
		int subStart=0, subEnd = 0;

		if ( this.log) {
			if ( this.logBase == Math.E) {
				baseStr = "E";
			} else {
				baseStr = this.logBase + "";
			}
			unitStrAll += logStr;
			subStart = unitStrAll.length();
			unitStrAll += baseStr;
			subEnd = unitStrAll.length();
			unitStrAll += " )";
		} 
			
		as = new AttributedString( unitStrAll);
		
		// Estimate the margin between the plot and the legend
		String maxLenLabel = "";
		
		for (int color = 0; color <= colors; ++color) {
			String label = gFormat(legendLevels[color]);
			if (maxLenLabel.length() < label.length()) maxLenLabel = label;
		}
		
		int maxLabelLen = graphics.getFontMetrics(labelFont == null ? gFont : labelFont).stringWidth(maxLenLabel);
		
		final int xRange = xMaximum - xMinimum;
		final int xCenter = xMinimum + xRange / 2;
		if (titleOffset != 0 && xMaximum < xCenter + titleOffset)
			xMaximum = titleOffset + xCenter;
		
		final int x = xMaximum + xOffset;
		int legendBoxX = x, legendBoxY = yMinimum - space;

		// Draw unit string:
		if (unitsFont != null) {
			graphics.setFont(unitsFont);
			
			if (log) {
				as.addAttribute(TextAttribute.FONT, unitsFont, 0, subStart);
				as.addAttribute(TextAttribute.FONT, unitsFont, subEnd +1, unitStrAll.length());
				as.addAttribute(TextAttribute.SUPERSCRIPT, TextAttribute.SUPERSCRIPT_SUB, subStart, subEnd);
			} else
				as.addAttribute(TextAttribute.FONT, unitsFont);
		}
		
		if (unitsClr != null) {
			graphics.setColor(unitsClr);
		}
		int unitHeight = graphics.getFontMetrics().getHeight();
		int unitWidth = graphics.getFontMetrics().getMaxAdvance();
		int unitStrX = x + unitHeight;
		//int unitStrY = yMaximum - yRange/2 + unitStr.length()/2;
		int unitStrY = yMaximum - yRange/2 + (unitStrAll.length() * unitWidth)/8;
		final double theta = Math.toRadians(90.0);
		graphics.translate(unitStrX, unitStrY);
		((Graphics2D)graphics).rotate(-theta);
		//graphics.drawString(unitStr, 0, 0);
		AttributedCharacterIterator aci = as.getIterator();
		if (showLegend) graphics.drawString(aci, 0, 0);
		((Graphics2D)graphics).rotate(theta);
		graphics.translate(-unitStrX, -unitStrY);
		graphics.setFont(gFont);
		
		// Draw level values and tic marks in the current color:
		final int xTic = unitStrX + unitHeight/2 + space + maxLabelLen + space;
		final boolean[] showLevelValues = getLevelValues(labelCnt == null ? colors : labelCnt, legendLevels.length);
		
		graphics.setColor(uTickColor);
		if (labelFont != null) graphics.setFont(labelFont);
		FontMetrics lFontMtrx = graphics.getFontMetrics(); // Of level value characters in pixels.
		int halfCharHeight = lFontMtrx.getHeight() / 2;
		
		for (int color = 0; color <= colors; ++color) {
			final double value = legendLevels[color];
			final String label = gFormat(value);
			final int labelLen = lFontMtrx.stringWidth(label);
			final int xLabel = xTic - labelLen - space;
			final int yTic = yMaximum - color * binHeight;
			final int yLabel = yTic + halfCharHeight;
			
			if (showLevelValues[color] && uShowTick)
				if (showLegend) graphics.drawString(label, xLabel, yLabel);

			if (showLegend && uShowTick) graphics.drawLine(xTic, yTic, xTic + ticSize, yTic);
		}
		
		// Draw color bar:
		int colorBarX = (uShowTick) ? xTic + ticSize : unitStrX + unitHeight + space;
		
		if (showLegend) {
			for (int color = 0; color < colors; ++color) {
				final int y = yMaximum - (color + 1) * binHeight;
				graphics.setColor(legendColors[color]);
				graphics.fillRect(colorBarX, y, binWidth, binHeight);
			}
		}

		// Draw box around color bar:
		final int y = yMaximum - colors * binHeight;
		graphics.setColor(Color.BLACK);
		if (showLegend) graphics.drawRect(colorBarX, y, binWidth, colors*binHeight);
		
		legendBoxWidth = colorBarX - x + binWidth + unitHeight / 2;
		int legendBoxHeight = space + yRange + halfCharHeight * 2 + 2 * space; //add space to top and bottom of the legend
		
		// Draw legend box
		graphics.setColor(Color.BLACK);

		if (showLegend) graphics.drawRect(legendBoxX, legendBoxY - halfCharHeight, legendBoxWidth, legendBoxHeight);
		
		graphics.setColor(currentColor); // Restore original color.

		config.putObject(PlotConfiguration.LEGEND_SHOW, showLegend);
		config.putObject(PlotConfiguration.UNITS, unitStr);
		config.putObject(PlotConfiguration.UNITS_COLOR, unitsClr == null ? currentColor : unitsClr);
		config.putObject(PlotConfiguration.UNITS_FONT, unitsFont == null ? gFont : unitsFont);
		config.putObject(PlotConfiguration.UNITS_TICK_FONT, labelFont == null ? gFont : labelFont);
		config.putObject(PlotConfiguration.UNITS_SHOW_TICK, uShowTick);
		config.putObject(PlotConfiguration.UNITS_TICK_COLOR, uTickColor);
		config.putObject(PlotConfiguration.UNITS_TICK_NUMBER, labelCnt == null ? colors + 1 : labelCnt);
	}

	private boolean[] getLevelValues(int labelCnt, int length) {
		boolean[] show = new boolean[length];
		int labels = labelCnt - 1;
		int multiple = Math.round((float)(length - 1) / (float)labels);
		
		for (int i = 0; i < length; i++)
			show[i] = true;
		
		if (multiple == 0) return show;
		
		for (int i = 1; i < length - 1; i++) 
			show[i] = (i % multiple == 0);
		
		return show;
	}

	/**
	 * drawGridCells - draw colored rectangles of the grid cells.
	 * 
	 * @pre graphics != null
	 * @pre xMinimum > 0
	 * @pre yMinimum > 0
	 * @pre xMaximum > xMinimum
	 * @pre yMaximum > yMinimum
	 * @pre xMaximum < 5000
	 * @pre yMaximum < 5000
	 * @pre firstRow >= 0
	 * @pre firstRow <= lastRow
	 * @pre firstColumn >= 0
	 * @pre firstColumn <= lastColumn
	 * @pre firstRow >= 0
	 * @pre firstRow <= lastRow
	 * @pre firstColumn >= 0
	 * @pre firstColumn <= lastColumn
	 * @pre legendLevels != null
	 * @pre legendLevels.length >= 2
	 * @pre legendColors != null
	 * @pre legendColors.length == legendLevels.length - 1
	 * @pre data != null
	 * @pre data.length == (1 + lastRow - firstRow) * (1 +
	 *      lastColumn-firstColumn)
	 */
	
	public void drawGridCells(final Graphics graphics, int xMinimum,
			int xMaximum, int yMinimum, int yMaximum, int firstRow,
			int lastRow, int firstColumn, int lastColumn,
			final double[] legendLevels, final Color[] legendColors,
			final float[][] data) {
		drawGridCells(graphics, xMinimum, xMaximum, yMinimum, yMaximum,
				firstRow, lastRow, firstColumn, lastColumn, legendLevels,
				legendColors, data, null);
	}

	public void drawGridCells(final Graphics graphics, int xMinimum,
			int xMaximum, int yMinimum, int yMaximum, int firstRow,
			int lastRow, int firstColumn, int lastColumn,
			final double[] legendLevels, final Color[] legendColors,
			final float[][] data, final byte[][] colorIndexCache) {
		if (data == null)
			return;

		final int rows = 1 + lastRow - firstRow;
		final int columns = 1 + lastColumn - firstColumn;
		final float width = xMaximum - xMinimum;
		final float height = yMaximum - yMinimum;
		final float xDelta = width / columns; // Width in pixels of a grid cell.
		final float yDelta = height / rows; // Height in pixels of a grid cell.
		final int rectangleHeight = replaceRound(yDelta + 0.5f);
		float rectangleWidth = xDelta;
		final Color backgroundColor = legendColors[0];
		final Color gColor = graphics.getColor();
		Color previousCellColor = null;

		// Color entire grid area with the lowest legend color then
		// avoid drawing the (usually numerous) grid cells that have that color.

		graphics.setColor(backgroundColor);
		
		if (rows == 1 && columns == 1) {
			float dat = data[0][0];
			int index = colorIndexCache == null ? indexOfValue(dat, legendLevels) : colorIndexCache[0][0];
			graphics.setColor(index == -1 ? Color.WHITE : legendColors[index]);
		}
		
		graphics.fillRect(xMinimum, yMinimum, (int) width, (int) height);
		
		// Draw only non-background color cells.
		// Draw cells as rectangles whose width is extended to cover consecutive
		// cells with the same color along a row so fewer rectangles are drawn.
		
		float yMaxAdj = yMaximum + 0.5f;
		float xMinAdj = xMinimum + 0.5f;
		float xDeltaAdj = xDelta + 0.5f;
		final int lastRectangleWidth =replaceRound(xDeltaAdj);
		for (int row = firstRow; row <= lastRow; ++row) {
			final int dataRow = row - firstRow;
			final int y = replaceRound(yMaxAdj - (1 + dataRow) * yDelta);
			float x = xMinimum;
			previousCellColor = null;

			for (int column = firstColumn; column <= lastColumn; ++column) {
				final int dataColumn = column - firstColumn;
				//final 
				float datum = data[dataRow][dataColumn];
				
				final int colorIndex = colorIndexCache == null ? indexOfValue(datum, legendLevels) : colorIndexCache[dataRow][dataColumn];

				final Color cellColor = (colorIndex == -1 ? Color.WHITE : legendColors[colorIndex]);
				
				if (column == lastColumn && cellColor != backgroundColor) { //draw the last cell of row
					final int lastX = replaceRound(xMinAdj + dataColumn * xDelta);
					graphics.setColor(cellColor);
					graphics.fillRect(lastX, y, lastRectangleWidth, rectangleHeight);
				}

				if (previousCellColor == null) {
					previousCellColor = cellColor; // Draw this colored cell later.
				} else if (cellColor != previousCellColor || column == lastColumn) {

					if (previousCellColor != backgroundColor) {

						// Draw rectangle for previous cells:

						final int thisRectangleWidth = replaceRound(rectangleWidth + 0.5f);
						final int cellX = replaceRound(x + 0.5f);
						graphics.setColor(previousCellColor);
						graphics.fillRect(cellX, y, thisRectangleWidth,
								rectangleHeight);
					} else {
						x += xDelta; // Skip over this background-colored
						// cell.
						previousCellColor = backgroundColor;
					}

					// Set-up for the next rectangle:

					rectangleWidth = xDelta;
					previousCellColor = cellColor;
					x = xMinimum + dataColumn * xDelta;
				} else {
					rectangleWidth += xDelta; // Widen rectangle to draw later.
				}
			}
		}
		
		graphics.setColor(Color.LIGHT_GRAY);
		graphics.drawRect(xMinimum, yMinimum, (int) width, (int) height);
		graphics.setColor(gColor);
	}
	
	public byte[][] calculateColorIndices(final float[][] data, final double[] legendLevels) {
		byte[][] indices = new byte[data.length][data[0].length];
		for (int i = 0; i < data.length; ++i)
			for (int j = 0; j < data[i].length; ++j)
				indices[i][j] = indexOfValue(data[i][j], legendLevels);
		return indices;
	}
	
	/**
	 * drawGridLines - draw grid lines.
	 * 
	 * @pre graphics != null
	 * @pre xMinimum > 0
	 * @pre yMinimum > 0
	 * @pre xMaximum > xMinimum
	 * @pre yMaximum > yMinimum
	 * @pre xMaximum < 5000
	 * @pre yMaximum < 5000
	 * @pre firstRow >= 0
	 * @pre firstRow <= lastRow
	 * @pre firstColumn >= 0
	 * @pre firstColumn <= lastColumn
	 * @pre firstRow >= 0
	 * @pre firstRow <= lastRow
	 * @pre firstColumn >= 0
	 * @pre firstColumn <= lastColumn
	 * @pre gridLineColor != null
	 */

	public void drawGridLines(final Graphics graphics, int xMinimum,
			int xMaximum, int yMinimum, int yMaximum, int firstRow,
			int lastRow, int firstColumn, int lastColumn,
			final Color gridLineColor ) {

		final int rows = 1 + lastRow - firstRow;
		final int columns = 1 + lastColumn - firstColumn;
		final float width = xMaximum - xMinimum;
		final float height = yMaximum - yMinimum;
		final float xDelta = width / columns; // Width in pixels of grid cell.
		final float yDelta = height / rows; // Height in pixels of grid cell.
		final int xpMinimum = replaceRound( xMinimum + 0.5f );
		final int xpMaximum = replaceRound( xMaximum + 0.5f );
		final int ypMinimum = replaceRound( yMinimum + 0.5f );
		final int ypMaximum = replaceRound( yMaximum + 0.5f );
		float x = xMinimum + xDelta;

		graphics.setColor(gridLineColor);

		for ( int column = firstColumn; column <= lastColumn;
		      ++column, x += xDelta ) {
			final int xp = replaceRound( x + 0.5f);
			graphics.drawLine( xp, ypMinimum, xp, ypMaximum );
		}
		
		for ( int row = firstRow; row <= lastRow; ++row ) {
			final int yp =
				replaceRound( yMaximum - ( 1 + row - firstRow ) * yDelta + 0.5f);
			graphics.drawLine( xpMinimum, yp, xpMaximum, yp );
		}
	}

	/**
	 * indexOfValue - Clamped index of value within range.
	 * 
	 * @pre ! Numerics.isNan( value )
	 * @pre values != null
	 * @pre values.length >= 2
	 * @post return >= 0
	 * @post return < values.length - 1
	 * @post value >= values[ return ]
	 * @post value <= values[ return ]
	 */

	private static byte indexOfValue(float value, final double[] values) { // TODO: log color legend: take log on value
		if (new Float(value).toString().equals("NaN") || value <= DataUtilities.AMISS3 || value <= DataUtilities.BADVAL3)
			return -1;
		
		final int count = values.length;
		
		if (values[0] == values[values.length-1])
			return 0;

		for (int index = 1; index < count; index++) {
			if (values[index] > value)
				return (byte)(index - 1);
		}

		return (byte)(count - 2);
	}
	
	/**
	 * dateTime - create date-time string.
	 * 
	 * @pre startDate >= 1900001
	 * @pre startTime >= 0
	 * @pre timestepSize > 0
	 * @pre timestep >= 0
	 * @post return != null
	 */
	
	private static final int MS_IN_SECONDS = 1000;
	private static final int SEC_IN_MIN = 60;
	private static final int MIN_IN_HOUR = 60;

	protected String dateTime(GregorianCalendar startDate, long timestepSize, int steplapse) {
		GregorianCalendar endDate = new GregorianCalendar();
		endDate.setTime(startDate.getTime());
		if (timestepSize * timestepSize > Integer.MAX_VALUE) {
			//prevent integer overflow
			int ms = (int)(timestepSize % MS_IN_SECONDS);
			long totalSec = timestepSize / MS_IN_SECONDS;
			int sec = (int)totalSec % SEC_IN_MIN;
			long totalMin = totalSec / SEC_IN_MIN;
			int min = (int)totalMin % MIN_IN_HOUR;
			int hour = (int)totalMin / MIN_IN_HOUR;
			ms *= steplapse;
			sec *= steplapse;
			min *= steplapse;
			hour *= steplapse;
			endDate.add(Calendar.MILLISECOND, ms);
			endDate.add(Calendar.SECOND, sec);
			endDate.add(Calendar.MINUTE, min);
			endDate.add(Calendar.HOUR, hour);
		}
		else
			endDate.add(Calendar.MILLISECOND, (int)timestepSize * steplapse);
		if (timestepSize % 1000 == 0)
			return Utilities.formatDate(endDate); 
		return Utilities.formatDateMS(endDate);
	}

	/**
	 * layerMinimumMaximum - find (row, column) of minimum and maximum.
	 * 
	 * @pre firstRow >= 0
	 * @pre firstRow <= lastRow
	 * @pre firstColumn >= 0
	 * @pre firstColumn <= lastColumn
	 * @pre firstRow >= 0
	 * @pre firstRow <= lastRow
	 * @pre firstColumn >= 0
	 * @pre firstColumn <= lastColumn
	 * @pre data != null
	 * @pre data.length >= (1 + lastRow - firstRow) * (1 +
	 *      lastColumn-firstColumn)
	 * @pre minimumCell != null
	 * @pre minimumCell.length == 2
	 * @pre maximumCell != null
	 * @pre maximumCell.length == 2
	 * @pre range != null
	 * @pre range.length == 2
	 * @post minimumCell[ ROW ] >= firstRow
	 * @post minimumCell[ ROW ] <= lastRow
	 * @post maximumCell[ ROW ] >= firstRow
	 * @post maximumCell[ ROW ] <= lastRow
	 * @post minimumCell[ COLUMN ] >= firstColumn
	 * @post minimumCell[ COLUMN ] <= firstColumn
	 * @post maximumCell[ COLUMN ] >= firstColumn
	 * @post maximumCell[ COLUMN ] <= firstColumn
	 * @post ! Numerics.isNan( range[ MINIMUM ] )
	 * @post ! Numerics.isNan( range[ MAXIMUM ] )
	 * @post range[ MINIMUM ] <= range[ MAXIMUM ]
	 * @post range[ MINIMUM ] == data[ minimumCell[ ROW ] ][ minimumCell[
	 *       COLUMN]]
	 * @post range[ MAXIMUM ] == data[ maximumCell[ ROW ] ][ maximumCell[
	 *       COLUMN]]
	 */

	private static void layerMinimumMaximum(int firstRow, int lastRow,
			int firstColumn, int lastColumn, final float[][] data,
			int[] minimumCell, int[] maximumCell, float[] range) {

		float minimumValue = (float)Double.POSITIVE_INFINITY;
		float maximumValue = (float)Double.NEGATIVE_INFINITY;;
		range[MINIMUM] = range[MAXIMUM] = minimumValue;
		minimumCell[ROW] = maximumCell[ROW] = firstRow;
		minimumCell[COLUMN] = maximumCell[COLUMN] = firstColumn;

		for (int row = firstRow; row <= lastRow; ++row) {
			final int dataRow = row - firstRow;

			for (int column = firstColumn; column <= lastColumn; ++column) {
				final int dataColumn = column - firstColumn;
				final float value = data[dataRow][dataColumn];

				if (value <= DataUtilities.BADVAL3 || value <= DataUtilities.AMISS3)
					continue;
				
				if (value < minimumValue) {
					minimumValue = value;
					minimumCell[ROW] = row;
					minimumCell[COLUMN] = column;
				} else if (value > maximumValue) {
					maximumValue = value;
					maximumCell[ROW] = row;
					maximumCell[COLUMN] = column;
				}
			}
		}

		range[MINIMUM] = minimumValue;
		range[MAXIMUM] = maximumValue;
	}
	
	public GridInfo getGridInfo(){
		return null;
	}

	/**
	 * gFormat - %g-like formatted string of value.
	 * 
	 * @post return != null
	 * @post return.length() <= 11
	 */

	protected String gFormat(double value) {
		return numberFormat.format(value);
	}

	public PlotConfiguration getPlotConfiguration() {
		return this.config;
	}

	public String getTitle() {
		return plotTitle;
	}

	public void setLog(boolean log) {
		this.log = log;
	}

	public boolean isLog() {
		return log;
	}

	public void setLogBase(int logBase) {
		this.logBase = logBase;
	}

	public double getLogBase() {
		return logBase;
	}
	
	public int getPlotWidth() {
		return plotWidth;
	}
	
	public int getPlotHeight() {
		return plotHeight;
	}
	
	protected int replaceRound(float num) {
		return Math.round(num);
	}

}
