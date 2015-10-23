package anl.verdi.plot.types;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Currency;
import java.util.GregorianCalendar;

import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.vecmath.Point4i;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickUnitSource;
import org.jfree.chart.axis.TickUnits;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

//import simphony.util.messages.MessageCenter;
import ucar.ma2.InvalidRangeException;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.Range;
import anl.verdi.data.Slice;
import anl.verdi.formula.Formula;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.VertCrossPlotConfiguration;
import anl.verdi.plot.data.CrossSectionXYZDataset;
import anl.verdi.plot.gui.AreaSelectionEvent;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.TimeConstantAxisPanel;
import anl.verdi.plot.jfree.XYBlockRenderer;
import anl.verdi.plot.probe.ProbeEvent;
import anl.verdi.util.Tools;
import anl.verdi.util.Utilities;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class VerticalCrossSectionPlot extends AbstractTilePlot {
	static final Logger Logger = LogManager.getLogger(VerticalCrossSectionPlot.class.getName());

	private DataFrameAxis constantAxis, domainAxis;
	private TimeConstantAxisPanel timePanel;
	private boolean processTimeChange = true;

	public static enum CrossSectionType {
		X, Y
	}

//	private static MessageCenter center = MessageCenter.getMessageCenter(VerticalCrossSectionPlot.class);

	private int constant;
	private CrossSectionXYZDataset dataset;
	private CrossSectionType type;
	private DataUtilities.MinMax rangedMinMax;

	/**
	 * Creates a VerticalCrossSectionPlot.
	 *
	 * @param frame  the data for the plot
	 * @param config the configuration info for this plot. This must contain valid
	 *               CrossSectionType and contant row/col
	 */
	public VerticalCrossSectionPlot(DataFrame frame, VertCrossPlotConfiguration config) {
		this(frame, config.getCrossSectionType(), config.getCrossSectionRowCol());
		Logger.debug("in constructor for VerticalCrossSectionPlot");
		if (config.getSubtitle1() == null || config.getSubtitle1().trim().isEmpty())
			config.setSubtitle1(Tools.getDatasetNames(frame));
		
		PlotConfiguration defaultConfig = getPlotConfiguration();
		defaultConfig.merge(config);
		configure(defaultConfig);
	}

	public void configure(PlotConfiguration config, Plot.ConfigSoure source) {
		PlotConfiguration defaultConfig = getPlotConfiguration();
		defaultConfig.merge(config);
		configure(defaultConfig);
	}

	public int getConstant() {
		return constant;
	}

	public CrossSectionType getCrossSectionType() {
		return type;
	}

	/**
	 * Creates a VerticalCrossSectionPlot.
	 *
	 * @param frame    the data for the plot
	 * @param type     the type of x / y cross section
	 * @param constant the constant column or row. Whether this is a column or row
	 *                 is dependent on the cross section type.
	 */
	public VerticalCrossSectionPlot(DataFrame frame, CrossSectionType type, int constant) {
		super(frame);
		Logger.debug("in alternate constructor for VerticalCrossSectionPlot");
		this.type = type;
		this.constant = constant;

		constantAxis = frame.getAxes().getXAxis();
		domainAxis = frame.getAxes().getYAxis();
		if (type == CrossSectionType.Y) {
			constantAxis = frame.getAxes().getYAxis();
			domainAxis = frame.getAxes().getXAxis();
		}

		// createMapAnnotation();
		dataset = new CrossSectionXYZDataset();
		if (type == CrossSectionType.X) dataset.addColSeries(frame, timeStep, constant);
		else dataset.addRowSeries(frame, timeStep, constant);
		chart = createChart(dataset);
		panel = new VerdiTileChartPanel(chart, true);
		AreaFinder finder = new AreaFinder();
		panel.addMouseListener(finder);
		panel.addMouseMotionListener(finder);
	}


	/**
	 * Gets the MinMax points for this plot.
	 *
	 * @return the MinMax points for this plot.
	 */
	protected DataUtilities.MinMaxPoint getMinMaxPoints() {
		try {
			if (type == CrossSectionType.X) return DataUtilities.minMaxTXPoint(frame, timeStep, constant);
			else return DataUtilities.minMaxTYPoint(frame, timeStep, constant);
		} catch (InvalidRangeException e) {
			Logger.error("Error getting min max points " + e.getMessage());
		}

		return null;
	}

	/**
	 * Gets a tool bar for this plot. This may
	 * return null if there is no tool bar.
	 *
	 * @return a tool bar for this plot.
	 */
	public JToolBar getToolBar() {

		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		timePanel = new TimeConstantAxisPanel();
		timePanel.setConstantAxisLabel(getRowOrCol() + ":");
		timePanel.init(frame.getAxes(), constantAxis,
						timeStep + frame.getAxes().getTimeAxis().getOrigin(),
						constant + constantAxis.getOrigin());
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (processTimeChange) {
					int time = timePanel.getTime() - frame.getAxes().getTimeAxis().getOrigin();
					int cons = timePanel.getAxisValue() - constantAxis.getOrigin();
					updateTimeStepAndConstant(time, cons);
				}
			}
		};

		timePanel.addSpinnerListeners(changeListener, changeListener);
		bar.add(timePanel);
		return bar;

	}

	private void performControlAction(Rectangle axisRect, Rectangle2D screenRect) {
		switch (controlAction) {
			case ZOOM:
				if (screenRect.getWidth() < 0 || screenRect.getHeight() < 0) panel.restoreAutoBounds();
				else panel.doZoom(screenRect);
				break;
			case PROBE:
				probe(axisRect);
				break;
			default:
		}
	}

	private void probe(Rectangle axisRect) {
		Slice slice = new Slice();
		slice.setTimeRange(timeStep, 1);
		int y = (axisRect.y - axisRect.height) - frame.getAxes().getZAxis().getOrigin();
		slice.setLayerRange(y, axisRect.height + 1);
		Axes<DataFrameAxis> axes = frame.getAxes();
		if (type == CrossSectionType.X) {
			slice.setXRange(constant, 1);
			slice.setYRange(axisRect.x - axes.getYAxis().getOrigin(), axisRect.width + 1);
		} else {
			slice.setXRange(axisRect.x - axes.getXAxis().getOrigin(), axisRect.width + 1);
			slice.setYRange(constant, 1);
		}

		try {
			DataFrame subsection = frame.slice(slice);
			probedSlice = slice;
			enableProbeItems(true);
			ProbeEvent event = new ProbeEvent(this, subsection, slice, Formula.Type.VERTICAL_CROSS_SECTION);
			event.setXConstant(Boolean.valueOf(type == CrossSectionType.X));
			eventProducer.fireProbeEvent(event);
		} catch (InvalidRangeException e) {
			Logger.error("Error while probing " + e.getMessage());
		}
	}

	private Point getAxisCoordForMouse(Point mousePoint) {
		Point2D p = panel.translateScreenToJava2D(mousePoint);
		XYPlot plot = (XYPlot) chart.getPlot();
		ChartRenderingInfo info = panel.getChartRenderingInfo();
		Rectangle2D dataArea = info.getPlotInfo().getDataArea();

		ValueAxis domainAxis = plot.getDomainAxis();
		RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
		ValueAxis rangeAxis = plot.getRangeAxis();
		RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
		double x = domainAxis.java2DToValue(p.getX(), dataArea,
						domainAxisEdge);
		double y = rangeAxis.java2DToValue(p.getY(), dataArea,
						rangeAxisEdge);
		x = Math.rint(x);
		y = Math.rint(y);

		Range domainRange = this.domainAxis.getRange();
		if (x < domainRange.getOrigin()) x = domainRange.getOrigin();
		else if (x > domainRange.getOrigin() + (domainRange.getExtent() - 1))
			x = domainRange.getOrigin() + (domainRange.getExtent() - 1);

		Range yRange = frame.getAxes().getZAxis().getRange();
		y += yRange.getOrigin();
		if (y < yRange.getOrigin()) y = yRange.getOrigin();
		else if (y > yRange.getOrigin() + (yRange.getExtent() - 1)) y = yRange.getOrigin() + (yRange.getExtent() - 1);

		return new Point((int) x, (int) y);
	}

	/**
	 * Updates the plot to show the data at the new timestep.
	 *
	 * @param step the new step index
	 */
	public void updateTimeStep(int step) {
		processTimeChange = false;
		updateTimeStepAndConstant(step, constant);
		timePanel.setTime(step + frame.getAxes().getTimeAxis().getOrigin());
		processTimeChange = true;

	}

	public void updateTimeStepAndConstant(int timeStep, int constant) {
		this.timeStep = timeStep;
		this.constant = constant;
		rangedMinMax = null;

		// updateScaleAxis((XYPlot) chart.getPlot());

		if (type == CrossSectionType.X) dataset.addColSeries(frame, timeStep, constant);
		else dataset.addRowSeries(frame, timeStep, constant);
		createSubtitle();

		int offset = frame.getAxes().getYAxis().getOrigin();
		if (type == CrossSectionType.X) {
			offset = frame.getAxes().getXAxis().getOrigin();
		}

		// + 1 so row or col appears to start 1
		//Keep with the pattern of Row/Col Index, i.e., Row 1 then Row 2 ....
		//look for Row 12 or Column 34, if present keep with the same trend but update with current the Row/Column Number
		String title = chart.getTitle().getText() != null ? chart.getTitle().getText() : "";
		title = title.replaceAll("\\b(?i)" + getRowOrCol() + "\\b\\s\\b\\d+\\b", getRowOrCol() + " " + (constant + offset + 1));
		chart.setTitle(title);
	}

	private String getRowOrCol() {
		String val = "Column";
		if (type == CrossSectionType.Y) val = "Row";
		return val;
	}

	private JFreeChart createChart(XYZDataset dataset) {
		String val = "Column";
		// offset is for the title -- what the actual index for the constant col / row
		// is
		int offset = frame.getAxes().getYAxis().getOrigin();
		if (type == CrossSectionType.X) {
			val = "Row";
			offset = frame.getAxes().getXAxis().getOrigin();
		}
		NumberAxis xAxis = new NumberAxis("Domain " + val);
		xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		Range domainRange = domainAxis.getRange();
		xAxis.setRange(domainRange.getOrigin(), domainRange.getOrigin() + (domainRange.getExtent() - 1));
		xAxis.setUpperMargin(0.0);
		xAxis.setLowerMargin(0.0);
		xAxis.setNumberFormatOverride(new AxisNumberFormatter(new DecimalFormat()));

		NumberAxis yAxis = new NumberAxis("Layer");
		yAxis.setUpperMargin(0.0);
		yAxis.setLowerMargin(0.0);
		yAxis.setStandardTickUnits(createIntegerTickUnits());

		XYBlockRenderer renderer = new XYBlockRenderer();
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

		String title = getRowOrCol() + " " + (constant + offset + 1) + " " + frame.getVariable().getName();
		chart = new JFreeChart(title, plot);
		chart.removeLegend();
		chart.setBackgroundPaint(Color.white);
		createSubtitles();
		updateScaleAxis(plot);

		plot.setBackgroundPaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
		// plot.addAnnotation(mapAnnotation);

		createSubtitle();
		return chart;
	}

	/**
	 * Returns a collection of tick units for integer values.
	 *
	 * @return A collection of tick units for integer values.
	 */
	private TickUnitSource createIntegerTickUnits() {
		TickUnits units = new TickUnits();
		int layerOrigin = frame.getAxes().getZAxis().getOrigin() + 1;
		NumberFormat df0 = new LayerAxisFormatter(new DecimalFormat("0"), layerOrigin);
		NumberFormat df1 = new LayerAxisFormatter(new DecimalFormat("#,##0"), layerOrigin);
		units.add(new NumberTickUnit(1, df0));
		units.add(new NumberTickUnit(2, df0));
		units.add(new NumberTickUnit(5, df0));
		units.add(new NumberTickUnit(10, df0));
		units.add(new NumberTickUnit(20, df0));
		units.add(new NumberTickUnit(50, df0));
		units.add(new NumberTickUnit(100, df0));
		units.add(new NumberTickUnit(200, df0));
		units.add(new NumberTickUnit(500, df0));
		units.add(new NumberTickUnit(1000, df1));
		units.add(new NumberTickUnit(2000, df1));
		units.add(new NumberTickUnit(5000, df1));
		units.add(new NumberTickUnit(10000, df1));
		units.add(new NumberTickUnit(20000, df1));
		units.add(new NumberTickUnit(50000, df1));
		units.add(new NumberTickUnit(100000, df1));
		units.add(new NumberTickUnit(200000, df1));
		units.add(new NumberTickUnit(500000, df1));
		units.add(new NumberTickUnit(1000000, df1));
		units.add(new NumberTickUnit(2000000, df1));
		units.add(new NumberTickUnit(5000000, df1));
		units.add(new NumberTickUnit(10000000, df1));
		units.add(new NumberTickUnit(20000000, df1));
		units.add(new NumberTickUnit(50000000, df1));
		units.add(new NumberTickUnit(100000000, df1));
		units.add(new NumberTickUnit(200000000, df1));
		units.add(new NumberTickUnit(500000000, df1));
		units.add(new NumberTickUnit(1000000000, df1));
		units.add(new NumberTickUnit(2000000000, df1));
		units.add(new NumberTickUnit(5000000000.0, df1));
		units.add(new NumberTickUnit(10000000000.0, df1));

		return units;
	}

	protected Point4i[] rectToPoints(Rectangle rect) {
		Point4i[] points = new Point4i[2];
		// rect is layer, row/col
		Axes<DataFrameAxis> axes = frame.getAxes();
		int xOrigin = axes.getXAxis().getOrigin();
		int yOrigin = axes.getYAxis().getOrigin();
		if (type == CrossSectionType.X) {
			// x is constant, layer is y
			Point4i point = new Point4i(constant + xOrigin, rect.x, rect.y, NO_VAL);
			points[0] = point;
		} else {
			// y is constant, layer is y
			Point4i point = new Point4i(rect.x, constant + yOrigin, rect.y, NO_VAL);
			points[0] = point;
		}

		if (rect.getWidth() > 0 || rect.getHeight() > 0) {
			if (type == CrossSectionType.X) {
				Point4i point = new Point4i(constant + xOrigin, rect.x + rect.width, rect.y - rect.height, NO_VAL);
				points[1] = point;
			} else {
				Point4i point = new Point4i(rect.x + rect.width, constant + yOrigin, rect.y - rect.height, NO_VAL);
				points[1] = point;
			}
		}
		return points;
	}


	/**
	 * Gets the min max values for the current plot.
	 *
	 * @return the min max values for the current plot.
	 */
	protected DataUtilities.MinMax getMinMax() {
		try {
			if (rangedMinMax == null) {
				if (type == CrossSectionType.X) rangedMinMax = DataUtilities.minMaxTX(frame, timeStep, constant);
				else rangedMinMax = DataUtilities.minMaxTY(frame, timeStep, constant);
			}
		} catch (InvalidRangeException e) {
			Logger.error("Error getting min max " + e.getMessage());
		}
		return rangedMinMax;
	}

	protected void createSubtitle() {
		getMinMax();
		TextTitle title = (TextTitle) chart.getSubtitle(bottomTitle2Index);
		title.setText("Min = " + Utilities.formatNumber(rangedMinMax.getMin()) + ", Max = " +
						Utilities.formatNumber(rangedMinMax.getMax()));

		GregorianCalendar aCalendar = frame.getAxes().getDate(timeStep + frame.getAxes().getTimeAxis().getOrigin());
		Logger.debug("in VerticalCrossSectionPlot createSubtitle set GregorianCalendar aCalendar = " + aCalendar.toString());
		title = (TextTitle) chart.getSubtitle(bottomTitle1Index);
		title.setText(Utilities.formatDate(aCalendar));
	}

	class AreaFinder extends MouseInputAdapter {

		private Point start, end;
		// this rect measured axis coordinates
		private Rectangle rect;

		public void mousePressed(MouseEvent e) {
			Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
			if (screenDataArea != null) {
				start = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
				rect = new Rectangle(getAxisCoordForMouse(start), new Dimension(0, 0));
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect), false));
			} else {
				start = null;
			}
		}

		private Point getPointInRectangle(int x, int y, Rectangle2D area) {
			x = (int) Math.max(Math.ceil(area.getMinX()), Math.min(x,
							Math.floor(area.getMaxX())));
			y = (int) Math.max(Math.ceil(area.getMinY()), Math.min(y,
							Math.floor(area.getMaxY())));
			return new Point(x, y);
		}

		public void mouseDragged(MouseEvent e) {
			if (start != null) {
				Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
				if (screenDataArea != null) {
					end = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
					Point p = getAxisCoordForMouse(end);
					rect.width = p.x - rect.x;
					rect.height = rect.y - p.y;
					boolean finished = rect.width < 0 || rect.height < 0;
					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect), finished));
				} else {
					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, true));
				}
			}
		}

		public void mouseMoved(MouseEvent e) {
			Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
			if (screenDataArea != null) {
				Point p = getAxisCoordForMouse(e.getPoint());
				Rectangle rect = new Rectangle(p.x, p.y, 0, 0);
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect), false));
			} else {
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(new Rectangle(0, 0, 0, 0), true));
			}
		}

		public void mouseExited(MouseEvent e) {
			eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(new Rectangle(0, 0, 0, 0), true));
		}

		public void mouseReleased(MouseEvent e) {
			if (start != null) {
				Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
				if (screenDataArea != null) {
					end = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
					Point p = getAxisCoordForMouse(end);
					rect.width = p.x - rect.x;
					rect.height = rect.y - p.y;
					performControlAction(rect, createRect(start, end));
				}
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect), true));
			}
		}

		private Rectangle2D createRect(Point start, Point end) {
			return new Rectangle2D.Double(start.x, start.y, end.x - start.x, end.y - start.y);
		}
	}

	private static class LayerAxisFormatter extends NumberFormat {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8645842600166219972L;
		private int layerOrigin;
		private DecimalFormat format;

		public LayerAxisFormatter(DecimalFormat format, int layerOrigin) {
			this.format = format;
			this.layerOrigin = layerOrigin;
		}


		public void applyLocalizedPattern(String pattern) {
			format.applyLocalizedPattern(pattern);
		}

		public void applyPattern(String pattern) {
			format.applyPattern(pattern);
		}

		public Object clone() {
			return format.clone();
		}

		public boolean equals(Object obj) {
			return format.equals(obj);
		}

		public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
			return format.format(number + layerOrigin, result, fieldPosition);
		}

		public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
			return format.format(number + layerOrigin, result, fieldPosition);
		}

		public StringBuffer format(Object number, StringBuffer toAppendTo, FieldPosition pos) {
			double val = ((Number) number).doubleValue() + layerOrigin;
			return format.format(val, toAppendTo, pos);
		}

		public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
			return format.formatToCharacterIterator(obj);
		}

		public Currency getCurrency() {
			return format.getCurrency();
		}

		public DecimalFormatSymbols getDecimalFormatSymbols() {
			return format.getDecimalFormatSymbols();
		}

		public int getGroupingSize() {
			return format.getGroupingSize();
		}

		public int getMaximumFractionDigits() {
			return format.getMaximumFractionDigits();
		}

		public int getMaximumIntegerDigits() {
			return format.getMaximumIntegerDigits();
		}

		public int getMinimumFractionDigits() {
			return format.getMinimumFractionDigits();
		}

		public int getMinimumIntegerDigits() {
			return format.getMinimumIntegerDigits();
		}

		public int getMultiplier() {
			return format.getMultiplier();
		}

		public String getNegativePrefix() {
			return format.getNegativePrefix();
		}

		public String getNegativeSuffix() {
			return format.getNegativeSuffix();
		}

		public String getPositivePrefix() {
			return format.getPositivePrefix();
		}

		public String getPositiveSuffix() {
			return format.getPositiveSuffix();
		}

		public RoundingMode getRoundingMode() {
			return format.getRoundingMode();
		}

		public int hashCode() {
			return format.hashCode();
		}

		public boolean isDecimalSeparatorAlwaysShown() {
			return format.isDecimalSeparatorAlwaysShown();
		}

		public boolean isParseBigDecimal() {
			return format.isParseBigDecimal();
		}

		public Number parse(String text, ParsePosition pos) {
			return format.parse(text, pos);
		}

		public void setCurrency(Currency currency) {
			format.setCurrency(currency);
		}

		public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
			format.setDecimalFormatSymbols(newSymbols);
		}

		public void setDecimalSeparatorAlwaysShown(boolean newValue) {
			format.setDecimalSeparatorAlwaysShown(newValue);
		}

		public void setGroupingSize(int newValue) {
			format.setGroupingSize(newValue);
		}

		public void setMaximumFractionDigits(int newValue) {
			format.setMaximumFractionDigits(newValue);
		}

		public void setMaximumIntegerDigits(int newValue) {
			format.setMaximumIntegerDigits(newValue);
		}

		public void setMinimumFractionDigits(int newValue) {
			format.setMinimumFractionDigits(newValue);
		}

		public void setMinimumIntegerDigits(int newValue) {
			format.setMinimumIntegerDigits(newValue);
		}

		public void setMultiplier(int newValue) {
			format.setMultiplier(newValue);
		}

		public void setNegativePrefix(String newValue) {
			format.setNegativePrefix(newValue);
		}

		public void setNegativeSuffix(String newValue) {
			format.setNegativeSuffix(newValue);
		}

		public void setParseBigDecimal(boolean newValue) {
			format.setParseBigDecimal(newValue);
		}

		public void setPositivePrefix(String newValue) {
			format.setPositivePrefix(newValue);
		}

		public void setPositiveSuffix(String newValue) {
			format.setPositiveSuffix(newValue);
		}

		public void setRoundingMode(RoundingMode roundingMode) {
			format.setRoundingMode(roundingMode);
		}

		public String toLocalizedPattern() {
			return format.toLocalizedPattern();
		}

		public String toPattern() {
			return format.toPattern();
		}
	}

	/**
	 * Gets the type of the Plot.
	 *
	 * @return the type of the Plot.
	 */
	public Formula.Type getType() {
		return Formula.Type.VERTICAL_CROSS_SECTION;
	}


	@Override
	public String getTitle() {
		return chart.getTitle().getText();
	}
}
