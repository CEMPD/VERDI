package anl.verdi.plot.types;

import static anl.verdi.formula.Formula.Type.TILE;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.GregorianCalendar;

import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.vecmath.Point4i;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import simphony.util.messages.MessageCenter;
import ucar.ma2.InvalidRangeException;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.ObsEvaluator;
import anl.verdi.data.Range;
import anl.verdi.data.Slice;
import anl.verdi.data.VectorEvaluator;
import anl.verdi.formula.Formula;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.data.DataFrameXYZDataset;
import anl.verdi.plot.gui.AreaSelectionEvent;
import anl.verdi.plot.gui.ObsAnnotation;
import anl.verdi.plot.gui.OverlayRequest;
import anl.verdi.plot.gui.TimeLayerPanel;
import anl.verdi.plot.gui.VectorAnnotation;
import anl.verdi.plot.jfree.XYBlockRenderer;
import anl.verdi.plot.probe.ProbeEvent;
import anl.verdi.util.Tools;
import anl.verdi.util.Utilities;

/**
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class TilePlot extends AbstractTilePlot {

	private static MessageCenter center = MessageCenter.getMessageCenter(TilePlot.class);

	private DataFrameXYZDataset dataset;
	private boolean hasNoLayer = false;
	private boolean processTimeChange = true;
	private TimeLayerPanel timeLayerPanel;

	public TilePlot(DataFrame frame) {
		this(frame, new TilePlotConfiguration());
System.out.println("in constructor for anl.verdi.plot.types.TilePlot");
	}

	public TilePlot(DataFrame frame, PlotConfiguration config) {
		super(frame);
System.out.println("in alternate constructor for anl.verdi.plot.types.TilePlot");
		hasNoLayer = frame.getAxes().getZAxis() == null;
		dataset = new DataFrameXYZDataset();
		dataset.addSeries(frame, timeStep, layer);
		chart = createChart(dataset);
		panel = new VerdiTileChartPanel(chart, true);
		AreaFinder finder = new AreaFinder();
		panel.addMouseListener(finder);
		panel.addMouseMotionListener(finder);
		
		if (config.getSubtitle1() == null || config.getSubtitle1().trim().isEmpty())
			config.setSubtitle1(Tools.getDatasetNames(frame));
		
		PlotConfiguration defaultConfig = getPlotConfiguration();
		defaultConfig.merge(config);
		configure(defaultConfig);
	}

	public void addVectorAnnotation(VectorEvaluator eval) {
		VectorAnnotation annotation = new VectorAnnotation(eval, timeStep, frame.getAxes().getBoundingBoxer());
		((XYPlot)chart.getPlot()).addAnnotation(annotation);
	}

	public void addObsAnnotation(ObsEvaluator eval, int shapeSize, int strokeSize) {
		addObsAnnotation(eval, shapeSize, strokeSize, ObsAnnotation.Symbol.CIRCLE);
	}
	
	public void addObsAnnotation(ObsEvaluator eval, int shapeSize, int strokeSize, ObsAnnotation.Symbol symbol) {
		ObsAnnotation annotation = new ObsAnnotation(eval, frame.getAxes(), timeStep, layer);
		annotation.setDrawingParams(symbol, strokeSize, shapeSize, map);
		((XYPlot)chart.getPlot()).addAnnotation(annotation);
	}


	protected void addObsOverlay() {
		OverlayRequest<ObsEvaluator> request = new OverlayRequest<ObsEvaluator>(OverlayRequest.Type.OBS, this);
		eventProducer.fireOverlayRequest(request);
	}

	protected void addVectorsOverlay() {
		OverlayRequest<ObsEvaluator> request = new OverlayRequest<ObsEvaluator>(OverlayRequest.Type.VECTOR, this);
		eventProducer.fireOverlayRequest(request);
	}

	/**
	 * Gets a tool bar for this plot. This may return null if there is no tool
	 * bar.
	 *
	 * @return a tool bar for this plot.
	 */
	public JToolBar getToolBar() {
		JToolBar bar = new JToolBar();
		bar.setFloatable(false);
		timeLayerPanel = new TimeLayerPanel();
		final DataFrameAxis layerAxis = frame.getAxes().getZAxis();
		if (hasNoLayer) {
			timeLayerPanel.init(frame.getAxes(),
							timeStep + frame.getAxes().getTimeAxis().getOrigin(), 0, false);
		} else {
			timeLayerPanel.init(frame.getAxes(), timeStep + frame.getAxes().getTimeAxis().getOrigin(), layer
							+ layerAxis.getOrigin(), layerAxis.getExtent() > 1);
		}
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (processTimeChange) {
					int time = timeLayerPanel.getTime() - frame.getAxes().getTimeAxis().getOrigin();
					if (hasNoLayer) updateTimeStepLayer(time, 0);
					else {
						int layer = timeLayerPanel.getLayer() - layerAxis.getOrigin();
						updateTimeStepLayer(time, layer);
					}
				}
			}
		};

		timeLayerPanel.addSpinnerListeners(changeListener, changeListener);
		bar.add(timeLayerPanel);
		return bar;
	}


	/**
	 * Gets the MinMax points for this plot.
	 *
	 * @return the MinMax points for this plot.
	 */
	protected DataUtilities.MinMaxPoint getMinMaxPoints() {
		try {
			if (hasNoLayer) return DataUtilities.minMaxPoint(frame, timeStep);
			return DataUtilities.minMaxTLPoint(frame, timeStep, layer);
		} catch (InvalidRangeException e) {
			center.error("Error getting min max points", e);
		}

		return null;
	}

	private void performControlAction(Rectangle axisRect, Rectangle2D screenRect) {
		switch (controlAction) {
			case ZOOM:
				if (screenRect.getWidth() < 0 || screenRect.getHeight() < 0) {
					panel.restoreAutoBounds();
					//mapAnnotation.resetAOE();
				} else {
					/*
					Point loc = axisRect.getLocation();
					Point start = getScreenCoordForAxis(loc);
					Point end = getScreenCoordForAxis(new Point(loc.x + axisRect.width,
									loc.y + axisRect.height));
					panel.doZoom(new Rectangle(start.x, start.y, end.x - start.x, Math.abs(end.y - start.y)));
					*/
					panel.doZoom(screenRect);
					//Range xRange = new Range(axisRect.x,  axisRect.width);
					//Range yRange = new Range(axisRect.y - axisRect.height, axisRect.height);
					//ReferencedEnvelope env = frame.getAxes().getBoundingBox(xRange, yRange);
					//mapAnnotation.setAOE(env);
				}
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
		if (!hasNoLayer) slice.setLayerRange(layer, 1);
		Axes<DataFrameAxis> axes = frame.getAxes();
		slice.setXRange(axisRect.x - axes.getXAxis().getOrigin(), axisRect.width + 1);
		slice.setYRange(axisRect.y - axisRect.height - axes.getYAxis().getOrigin(), axisRect.height + 1);

		try {
			DataFrame subsection = frame.slice(slice);
			probedSlice = slice;
			enableProbeItems(true);
			eventProducer.fireProbeEvent(new ProbeEvent(this, subsection, slice, TILE));
		} catch (InvalidRangeException e) {
			center.error("Error while probing", e);
		}
	}

	private Point getScreenCoordForAxis(Point2D axisPoint) {
		XYPlot plot = (XYPlot) chart.getPlot();
		ChartRenderingInfo info = panel.getChartRenderingInfo();
		Rectangle2D dataArea = info.getPlotInfo().getDataArea();

		ValueAxis domainAxis = plot.getDomainAxis();
		RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
		ValueAxis rangeAxis = plot.getRangeAxis();
		RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();

		double x = domainAxis.valueToJava2D(axisPoint.getX(), dataArea, domainAxisEdge);
		double y = rangeAxis.valueToJava2D(axisPoint.getY(), dataArea, rangeAxisEdge);

		return panel.translateJava2DToScreen(new Point2D.Double(x, y));
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
		double x = domainAxis.java2DToValue(p.getX(), dataArea, domainAxisEdge);
		double y = rangeAxis.java2DToValue(p.getY(), dataArea, rangeAxisEdge);
		x = Math.rint(x);
		y = Math.rint(y);

		Range xRange = frame.getAxes().getXAxis().getRange();
		if (x < xRange.getOrigin())
			x = xRange.getOrigin();
		else if (x > xRange.getOrigin() + (xRange.getExtent() - 1))
			x = xRange.getOrigin() + (xRange.getExtent() - 1);

		Range yRange = frame.getAxes().getYAxis().getRange();
		if (y < yRange.getOrigin())
			y = yRange.getOrigin();
		else if (y > yRange.getOrigin() + (yRange.getExtent() - 1))
			y = yRange.getOrigin() + (yRange.getExtent() - 1);

		return new Point((int) x, (int) y);
	}


	/**
	 * Updates the plot to show the data at the new timestep.
	 *
	 * @param step the new step index
	 */
	public void updateTimeStep(int step) {
System.out.println("TilePlot updateTimeStep");
		processTimeChange = false;
		updateTimeStepLayer(step, layer);
		timeLayerPanel.setTime(step + frame.getAxes().getTimeAxis().getOrigin());
		processTimeChange = true;
	}

	public void updateTimeStepLayer(int timeStep, int layer) {
		this.timeStep = timeStep;
		this.layer = layer;

		// updateScaleAxis((XYPlot) chart.getPlot());
		dataset.addSeries(frame, timeStep, layer);
		createSubtitle();
		chart.setTitle(createTitle());

		java.util.List annotations = ((XYPlot)chart.getPlot()).getAnnotations();
		for(Object obj : annotations) {
			if (obj instanceof ObsAnnotation) {
				((ObsAnnotation)obj).update(timeStep);
			} else if (obj instanceof VectorAnnotation) {
				((VectorAnnotation)obj).update(timeStep);
			}
		}
	}

	private String createTitle() {
		if (hasNoLayer) {
			return frame.getVariable().getName();
		} else {
			int displayedLayer = frame.getAxes().getZAxis().getOrigin() + layer + 1;
			return "Layer " + displayedLayer + " " + frame.getVariable().getName();
		}
	}

	protected DataUtilities.MinMax getMinMax() {
		try {
			if (hasNoLayer) return DataUtilities.minMax(frame, timeStep);
			else return DataUtilities.minMax(frame, timeStep, layer);
		} catch (InvalidRangeException ex) {
			center.error("Error creating scale axis", ex);
		}
		return null;
	}

	private JFreeChart createChart(XYZDataset dataset) {
		NumberAxis xAxis = new NumberAxis("X");
		xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

		xAxis.setAutoRange(true);
		xAxis.setUpperMargin(0.0);
		xAxis.setLowerMargin(0.0);
		xAxis.setAutoRangeIncludesZero(false);
		xAxis.setNumberFormatOverride(new AxisNumberFormatter(new DecimalFormat()));

		NumberAxis yAxis = new NumberAxis("Y");
		//Range yRange = frame.getAxes().getYAxis().getRange();
		//yAxis.setRange(yRange.getOrigin(), yRange.getOrigin() + (yRange.getExtent() - 1));
		yAxis.setAutoRange(true);
		yAxis.setAutoRangeIncludesZero(false);
		yAxis.setUpperMargin(0.0);
		yAxis.setLowerMargin(0.0);
		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
		yAxis.setNumberFormatOverride(new AxisNumberFormatter(new DecimalFormat()));

		XYBlockRenderer renderer = new XYBlockRenderer();
		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
		chart = new JFreeChart(createTitle(), plot);
		chart.removeLegend();
		chart.setBackgroundPaint(Color.white);

		createSubtitles();
		updateScaleAxis(plot);

		plot.setBackgroundPaint(Color.lightGray);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
		createMapAnnotation();
		plot.addAnnotation(mapAnnotation);
		createSubtitle();

		return chart;
	}

	protected void createSubtitle() {

		DataUtilities.MinMaxPoint minMax = getMinMaxPoints();
		Point minPoint = minMax.getMinPoints().iterator().next();
		Point maxPoint = minMax.getMaxPoints().iterator().next();

		int xOrigin = frame.getAxes().getXAxis().getOrigin();
		minPoint.x += xOrigin;
		maxPoint.x += xOrigin;
		int yOrigin = frame.getAxes().getYAxis().getOrigin();
		minPoint.y += yOrigin;
		maxPoint.y += yOrigin;

		StringBuilder builder = new StringBuilder("Min (");
		if (showLatLon) {
			Point2D latLon = getLatLonForAxisPoint(minPoint);
			builder.append(Utilities.formatNumber(latLon.getX()));
			builder.append(", ");
			builder.append(Utilities.formatNumber(latLon.getY()));
		} else {
			builder.append(minPoint.x + 1);
			builder.append(", ");
			builder.append(minPoint.y + 1);
		}
		builder.append(")");
		if (minMax.getMinPoints().size() > 1) builder.append("...");
		builder.append(" = ");
		builder.append(Utilities.formatNumber(minMax.getMin()));

		builder.append(", Max (");
		if (showLatLon) {
			Point2D latLon = getLatLonForAxisPoint(maxPoint);
			builder.append(Utilities.formatNumber(latLon.getX()));
			builder.append(", ");
			builder.append(Utilities.formatNumber(latLon.getY()));
		} else {
			builder.append(maxPoint.x + 1);
			builder.append(", ");
			builder.append(maxPoint.y + 1);
		}
		builder.append(")");
		if (minMax.getMaxPoints().size() > 1) builder.append("...");
		builder.append(" = ");
		builder.append(Utilities.formatNumber(minMax.getMax()));

		TextTitle title = (TextTitle) chart.getSubtitle(bottomTitle2Index);
		title.setText(builder.toString());
		GregorianCalendar aCalendar = frame.getAxes().getDate(timeStep + frame.getAxes().getTimeAxis().getOrigin());
System.out.println("in verdi TilePlot createSubtitle, compute GregorianCalendar aCalendar = " + aCalendar.toString());
		title = (TextTitle) chart.getSubtitle(bottomTitle1Index);
		title.setText(Utilities.formatDate(aCalendar));

		title = (TextTitle)chart.getSubtitle(subTitle1Index);
	}

	protected Point4i[] rectToPoints(Rectangle rect) {
		Point4i[] points = new Point4i[2];
		// rect is x,y
		Point4i point = new Point4i(rect.x, rect.y, NO_VAL, NO_VAL);
		points[0] = point;

		if (rect.getWidth() > 0 || rect.getHeight() > 0) {
			point = new Point4i(rect.x + rect.width, rect.y - rect.height, NO_VAL, NO_VAL);
			points[1] = point;
		}
		return points;
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
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect),
								false));
			} else {
				start = null;
			}
		}

		private Point getPointInRectangle(int x, int y, Rectangle2D area) {
			x = (int) Math.max(Math.ceil(area.getMinX()), Math.min(x, Math.floor(area.getMaxX())));
			y = (int) Math.max(Math.ceil(area.getMinY()), Math.min(y, Math.floor(area.getMaxY())));
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
					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect),
									finished));
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

					//start = getScreenCoordForAxis(new Point2D.Float(rect.x - .5f, rect.y - .5f));
					//end = getScreenCoordForAxis(new Point2D.Float(p.x + .5f, p.y + .5f));

					performControlAction(rect, createRect(start, end));
				}
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, true));
			}
		}

		private Rectangle2D createRect(Point start, Point end) {
			return new Rectangle2D.Double(start.x, start.y, end.x - start.x, end.y - start.y);
		}
	}

	/**
	 * Gets the type of the Plot.
	 *
	 * @return the type of the Plot.
	 */
	public Formula.Type getType() {
		return Formula.Type.TILE;
	}

	@Override
	public String getTitle() {
		return chart.getTitle().getText();
	}
}
