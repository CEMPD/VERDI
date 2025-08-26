/**
 * MPASTilePlot - Subclass of TilePlot used to customize behavior for unstructured grid plots
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package gov.epa.emvl;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import anl.verdi.area.target.GridInfo;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.gui.MeshPlot;
import anl.verdi.util.Utilities;

public class MPASTilePlot extends TilePlot {
	
	protected int footerHeight = 83;
	
	Color labelColor = null;
	String variable = null;
	int layer;
	int steplapse;
	int firstRow;
	int lastRow;
	int firstColumn;
	int lastColumn;
	double[] plotMinMaxValues;
	double[] layerMinMaxValues;
	double[] statMinMaxValues;
	double[] currentMinMaxValues;
	double clipYMin;
	double clipYMax;
	double clipXMin;
	double clipXMax;
	protected GridInfo gridInfo = null;
	
	NumberFormat plotFormat;
	
	private static NumberFormat percentFormat = NumberFormat.getInstance();
	static {
		percentFormat.setMaximumFractionDigits(2);
	}

	
	boolean useStats = false;
	
	public MPASTilePlot(GregorianCalendar startDate, long timestepSize, double[] plotMinMaxValues, double[] statMinMaxValues) {
		super(startDate, timestepSize);
		this.plotMinMaxValues = plotMinMaxValues;
		this.statMinMaxValues = statMinMaxValues;
	}	
	
	public void setUseStats(boolean stats) {
		useStats = stats;
	}
	
	public GridInfo createGridInfo(double[][] gridBounds, double[][] domain){
		if (gridInfo == null)
			gridInfo = new GridInfo(gridBounds,domain);
		return gridInfo;
	}
	
	public GridInfo getGridInfo() {
		return gridInfo;
	}
	
	protected void drawAxis(final Graphics graphics, int xMinimum, int xMaximum,
			int yMinimum, int yMaximum, int firstRow, int lastRow,
			int firstColumn, int lastColumn) {
	}
	
	public void drawGridLines(final Graphics graphics, int xMinimum,
			int xMaximum, int yMinimum, int yMaximum, int firstRow,
			int lastRow, int firstColumn, int lastColumn,
			final Color gridLineColor ) {
	}
	
	//This needs to run after MeshPlot.drawAxis() so footerYOffset will be available, not within TilePlot.draw()
	protected void drawLabels(final Graphics graphics, Color labelColor, int xMinimum,
			int xMaximum, int yMinimum, int yMaximum, final String variable,
			int steplapse, int layer, int firstRow, int lastRow,
			int firstColumn, int lastColumn, final float[][] data) {
		this.labelColor = labelColor;
		this.variable = variable;
		this.layer = layer;
		this.firstRow = firstRow;
		this.lastRow = lastRow;
		this.firstColumn = firstColumn;
		this.lastColumn = lastColumn;
		this.steplapse = steplapse;
	}

	public void drawAxis(final Graphics graphics, int xMinimum, int xMaximum,
			int yMinimum, int yMaximum, double panX, double visibleWidth,
			double panY, double visibleHeight, String selectedStat ) {
		final int xAxisOffset = 5; // Pixel offset left of west edge of grid.
		final int yAxisOffset = 5; // Pixel offset below south edge of grid.
		final int xTicLength = 5; // Pixel legth of tic mark on X-axis.
		final int yTicLength = 3; // Pixel legth of tic mark on Y-axis.
		final int labelWidth = 25; // Pixel width of axis tic-mark text labels.
		final int labelHeight = 13; // Pixel height of axis tic-mark text labels.
		final int xAxis = xMinimum - xAxisOffset; // X-coordinate of Y-axis.
		final int yAxis = yMaximum + yAxisOffset; // Y-coordinate of X-axis.
		//final int rows = 1 + lastRow - firstRow;
		//final int columns = 1 + lastColumn - firstColumn;
		final int xRange = xMaximum - xMinimum;
		final int yRange = yMaximum - yMinimum;
		//final float columnSize = xRange / columns; // Pixels per column.
		//final float rowSize = yRange / rows; // Pixels per row.

		final int xDelta = labelWidth * 3;
		final int yDelta = labelHeight * 2;
		final int xTics = Math.round(xRange / xDelta) + 1;
		final int yTics = Math.round(yRange / yDelta) + 1;
		//final double columnStep = columns / xTics;
		//final double rowStep = rows / yTics;
		final int fSize = graphics.getFont().getSize();
		final int xspace = 6; // Space between two visual components
		final int yspace = 16; // Space between two visual components
		final Color gColor = graphics.getColor();
		final Font gFont = graphics.getFont();

		String domainX = config.getString(PlotConfiguration.DOMAIN_LABEL);
		Boolean dShowTick = (Boolean) config.getObject(PlotConfiguration.DOMAIN_SHOW_TICK);
		Number dLabelCnt = (Number) config.getObject(TilePlotConfiguration.DOMAIN_TICK_NUMBER);
		Color domainClr = config.getColor(PlotConfiguration.DOMAIN_COLOR);
		Color domainTickClr = config.getColor(PlotConfiguration.DOMAIN_TICK_COLOR);
		Font domainFont = config.getFont(PlotConfiguration.DOMAIN_FONT);
		Font domainTickFont = config.getFont(PlotConfiguration.DOMAIN_TICK_FONT);
		dShowTick = (dShowTick == null ? true : dShowTick);
		
		String rangeY = config.getString(PlotConfiguration.RANGE_LABEL);
		Boolean rShowTick = (Boolean) config.getObject(PlotConfiguration.RANGE_SHOW_TICK);
		Color rangeClr = config.getColor(PlotConfiguration.RANGE_COLOR);
		Color rangeTickClr = config.getColor(PlotConfiguration.RANGE_TICK_COLOR);
		Font rangeFont = config.getFont(PlotConfiguration.RANGE_FONT);
		Font rangeTickFont = config.getFont(PlotConfiguration.RANGE_TICK_FONT);
		Number rLabelCnt = (Number) config.getObject(TilePlotConfiguration.RANGE_TICK_NUMBER);
		rShowTick = (rShowTick == null ? true : rShowTick);

		if (dShowTick) graphics.drawLine(xMinimum, yAxis, xMaximum, yAxis); // X-axis.
		if (rShowTick) graphics.drawLine(xAxis, yMinimum, xAxis, yMaximum); // Y-axis.

		// Draw tic marks and labels on Y-axis:
		if (rangeTickClr != null) graphics.setColor(rangeTickClr);
		if (rangeTickFont != null) graphics.setFont(rangeTickFont);
		int maxLblWidth = 0;
		int tickLblBase = xAxis - yTicLength - xspace;
		NumberFormat format = NumberFormat.getInstance();
		int digits = getDisplayPrecision(visibleHeight);
		format.setMaximumFractionDigits(digits);
		format.setMinimumFractionDigits(digits);
		clipYMax = -1 * panY;
		clipYMin = -1 * (visibleHeight + panY);
		
		for (int tick = 0; tick <= yTics; ++tick) {
			double pct = tick / (double)yTics;
			int yCoord = yMinimum + (int)Math.round(pct * yRange);
			String label = format.format(-1 * (pct * visibleHeight + panY));
			final int lblWidth = graphics.getFontMetrics().stringWidth(label);
			final int xpostn = tickLblBase - lblWidth;
			if (rShowTick) graphics.drawString(label, xpostn, yCoord + fSize / 2);
			if (rShowTick) graphics.drawLine(xAxis, yCoord, xAxis - yTicLength, yCoord);
			if (maxLblWidth < lblWidth) maxLblWidth = lblWidth;
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
		
		digits = getDisplayPrecision(visibleWidth);
		format.setMaximumFractionDigits(digits);
		format.setMinimumFractionDigits(digits);
		
		clipXMin = panX;
		clipXMax = visibleWidth + panX;
     
		for (int tick = 0; tick <= xTics; ++tick) {
			double pct = tick / (double)xTics;
			int xCoord = (int)Math.round(pct * xRange) + xMinimum;
			String label = format.format(pct * visibleWidth + panX);
			
			final int xpostn = xCoord - graphics.getFontMetrics().stringWidth(label) / 2;
			if (dShowTick) graphics.drawString(label, xpostn, dTickYBase + yspace);
			if (dShowTick) graphics.drawLine(xCoord, yAxis, xCoord, dTickYBase);
		}
		
		footerYOffset = xTicLength + yspace + graphics.getFontMetrics().getHeight() + yspace;
		int fontHeight = graphics.getFontMetrics().getHeight() + yspace;
		
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
			footerYOffset += fontHeight;
			
			// Resume graphics default settings
			graphics.setColor(gColor);
			graphics.setFont(gFont);
		}
		
		footerHeight = footerYOffset + fontHeight;
				
		config.putObject(PlotConfiguration.DOMAIN_LABEL, domainX);
		config.putObject(PlotConfiguration.DOMAIN_FONT, domainFont == null ? gFont : domainFont);
		config.putObject(PlotConfiguration.DOMAIN_COLOR, domainClr == null ? gColor : domainClr);
		config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, dShowTick);
		config.putObject(PlotConfiguration.DOMAIN_TICK_NUMBER, dLabelCnt != null ? dLabelCnt : xTics);
		config.putObject(PlotConfiguration.DOMAIN_TICK_COLOR, domainTickClr == null ? gColor : domainTickClr);
		config.putObject(PlotConfiguration.DOMAIN_TICK_FONT, domainTickFont == null ? gFont : domainTickFont);
		
		config.putObject(PlotConfiguration.RANGE_LABEL, rangeY);
		config.putObject(PlotConfiguration.RANGE_FONT, rangeFont == null ? gFont : rangeFont);
		config.putObject(PlotConfiguration.RANGE_COLOR, rangeClr == null ? gColor : rangeClr);
		config.putObject(PlotConfiguration.RANGE_SHOW_TICK, rShowTick);
		config.putObject(PlotConfiguration.RANGE_TICK_NUMBER, rLabelCnt != null ? rLabelCnt : yTics);
		config.putObject(PlotConfiguration.RANGE_TICK_COLOR, rangeTickClr == null ? gColor : rangeTickClr);
		config.putObject(PlotConfiguration.RANGE_TICK_FONT, rangeTickFont == null ? gFont : rangeTickFont);

		super.drawLabels(graphics, labelColor, xMinimum, xMaximum, yMinimum, yMaximum, variable,
				steplapse, layer, firstRow, lastRow, firstColumn, lastColumn, null, selectedStat);
	}
	
	int stepUnits = Calendar.MILLISECOND;
	
	public void setTimestepUnits(int units) {
		stepUnits = units;
	}
	
	protected String dateTime(GregorianCalendar startDate, long timestepSize, int steplapse) {
		if (stepUnits == Calendar.MONTH){
			GregorianCalendar endDate = new GregorianCalendar();
			endDate.setTime(startDate.getTime());
			endDate.add(Calendar.MONTH, steplapse);
			return Utilities.formatDate(endDate);
		}
		return super.dateTime(startDate, timestepSize, steplapse);
	}
	
	public void setMinMax(double[] values) {
		layerMinMaxValues = values;
	}
	
	protected double clip(double value, double min, double max) {
		if (value < min)
			value = min;
		if (value > max)
			value = max;
		return value;
	}
	
	protected String getMinMaxLabel(int firstRow, int lastRow,
			int firstColumn, int lastColumn, final float[][] data) {
		
		double[] minMaxValues = layerMinMaxValues;
		String suffix = "";
		
		
		if (useStats) {
			minMaxValues = statMinMaxValues;
		} else if (plotMinMaxValues[MeshPlot.PLOT_CACHE_PERCENT_COMPLETE] < 100) {
			suffix = " (Loading, " + percentFormat.format(plotMinMaxValues[MeshPlot.PLOT_CACHE_PERCENT_COMPLETE]) + "% complete)";
		}

		return "Min (" + plotFormat.format(clip(minMaxValues[MeshPlot.LEVELS_CACHE_MIN_LON], clipXMin, clipXMax)) + ", "
				+ plotFormat.format(clip(minMaxValues[MeshPlot.LEVELS_CACHE_MIN_LAT], clipYMin, clipYMax)) + ") = "
				+ gFormat(minMaxValues[MeshPlot.LEVELS_CACHE_MIN_VALUE]) + ", "
				+ "Max (" + plotFormat.format(clip(minMaxValues[MeshPlot.LEVELS_CACHE_MAX_LON], clipXMin, clipXMax)) + ", "
				+ plotFormat.format(clip(minMaxValues[MeshPlot.LEVELS_CACHE_MAX_LAT], clipYMin, clipYMax)) + ") = " 
				+ gFormat(minMaxValues[MeshPlot.LEVELS_CACHE_MAX_VALUE]) + suffix;
		
	}
	
	public void setRenderVars(int xTranslation, NumberFormat plotFormat, double[] minMax) {
		this.xTranslation = xTranslation;
		this.plotFormat = plotFormat;
		this.layerMinMaxValues = minMax;
	}
	
	public int getFooterHeight() {
		return footerHeight;
	}
	
	public int getLegendBoxWidth() {
		return 52 + legendBoxWidth; //20 offset *2 + 6 space *2
	}
	
	public static int getDisplayPrecision(double range) {
		if (range < 1) {
			return countLeadingZeros(range) + 2;
		}
		else if (range < 10)
			return 2;
		else if (range < 15)
			return 1;
		return 0;
	}
	
	public static int countLeadingZeros(double decimal) {
		if (decimal >= 1)
			return 0;
		int cnt = -1;
		while (decimal < 1) {
			decimal *= 10;
			++cnt;
		}
		return cnt;
		
	}

}
