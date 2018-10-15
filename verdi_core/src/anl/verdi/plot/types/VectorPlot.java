package anl.verdi.plot.types;
// 2014 removed old Vector Plot
//import static anl.verdi.formula.Formula.Type.TILE;
//
//import java.awt.Color;
//import java.awt.Dimension;
//import java.awt.Font;
//import java.awt.Point;
//import java.awt.Rectangle;
//import java.awt.event.ActionEvent;
//import java.awt.event.MouseEvent;
//import java.awt.geom.Point2D;
//import java.awt.geom.Rectangle2D;
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.GregorianCalendar;
//import java.util.List;
//
//import javax.swing.AbstractAction;
//import javax.swing.JMenu;
//import javax.swing.JMenuBar;
//import javax.swing.JMenuItem;
//import javax.swing.JPanel;
//import javax.swing.JToolBar;
//import javax.swing.event.ChangeEvent;
//import javax.swing.event.ChangeListener;
//import javax.swing.event.MouseInputAdapter;
//import javax.vecmath.Point4i;
//
//import org.jfree.chart.ChartRenderingInfo;
//import org.jfree.chart.JFreeChart;
//import org.jfree.chart.axis.NumberAxis;
//import org.jfree.chart.axis.ValueAxis;
//import org.jfree.chart.plot.XYPlot;
////import org.jfree.experimental.chart.renderer.xy.VectorRenderer;
////import org.jfree.experimental.data.xy.VectorXYDataset;
//import org.jfree.chart.renderer.xy.VectorRenderer;
//import org.jfree.chart.title.TextTitle;
//import org.jfree.data.xy.VectorXYDataset;
//import org.jfree.ui.RectangleEdge;
//import org.jfree.ui.RectangleInsets;
//
//import simphony.util.messages.MessageCenter;
//import ucar.ma2.InvalidRangeException;
//import anl.verdi.data.Axes;
//import anl.verdi.data.DataFrame;
//import anl.verdi.data.DataFrameAxis;
//import anl.verdi.data.DataUtilities;
//import anl.verdi.data.Range;
//import anl.verdi.data.Slice;
//import anl.verdi.formula.Formula;
//import anl.verdi.plot.anim.AnimationPanel;
//import anl.verdi.plot.config.JFreeChartConfigurator;
//import anl.verdi.plot.config.PlotConfiguration;
//import anl.verdi.plot.config.TitleConfigurator;
//import anl.verdi.plot.config.VectorPlotConfiguration;
//import anl.verdi.plot.data.DFVectorXYDataset;
//import anl.verdi.plot.data.DataFrameXYZDataset;
//import anl.verdi.plot.gui.AreaSelectionEvent;
//import anl.verdi.plot.gui.TimeLayerPanel;
//import anl.verdi.plot.jfree.XYBlockRenderer;
//import anl.verdi.plot.probe.ProbeEvent;
//import anl.verdi.util.Tools;
//import anl.verdi.util.Utilities;
//
///**
// * @author Nick Collier
// * @version $Revision$ $Date$
// */
//public class VectorPlot extends AbstractTilePlot {
//
//	private static final MessageCenter center = MessageCenter.getMessageCenter(VectorPlot.class);
//
//	private DataFrame xVect, yVect, tileFrame;
//	private int timeStep, layer;
//	private boolean hasNoLayer = false;
//
//	private DFVectorXYDataset vectDataset;
//	private DataFrameXYZDataset tileDataset;
//	private boolean processTimeChange = true;
//	private TimeLayerPanel timeLayerPanel;
//
//	public VectorPlot(DataFrame xVect, DataFrame yVect) {
//		this(xVect, yVect, null);
//	}
//
//	public VectorPlot(DataFrame xVect, DataFrame yVect, DataFrame tileFrame) {
//		this(xVect, yVect, tileFrame, new VectorPlotConfiguration());
//System.out.println("in constructor for VectorPlot");
//	}
//
//	public VectorPlot(DataFrame xVect, DataFrame yVect, DataFrame tileFrame, PlotConfiguration config) {
//		// this would be the tileFrame if it existed.
//		super(tileFrame != null ? tileFrame : xVect);
//System.out.println("in alternate constructor for VectorPlot");
//		hasNoLayer = xVect.getAxes().getZAxis() == null;
//		this.xVect = xVect;
//		this.yVect = yVect;
//		this.tileFrame = tileFrame;
//		vectDataset = new DFVectorXYDataset();
//		vectDataset.addSeries(xVect, yVect, timeStep, layer);
//		if (tileFrame != null) {
//			tileDataset = new DataFrameXYZDataset();
//			tileDataset.addSeries(tileFrame, timeStep, layer);
//		}
//		createChart(vectDataset);
//		panel = new VerdiTileChartPanel(chart, true);
//		AreaFinder finder = new AreaFinder();
//		panel.addMouseListener(finder);
//		panel.addMouseMotionListener(finder);
//
//		if (config.getSubtitle1() == null || config.getSubtitle1().trim().isEmpty())
//			config.setSubtitle1(Tools.getDatasetNames(frame));
//		
//		PlotConfiguration defaultConfig = getPlotConfiguration();
//		defaultConfig.merge(config);
//		configure(defaultConfig);
//	}
//
//
//	/**
//	 * Gets the data that this Plot plots.
//	 *
//	 * @return the data that this Plot plots.
//	 */
//	public List<DataFrame> getData() {
//		List<DataFrame> data = new ArrayList<DataFrame>();
//		data.add(xVect);
//		data.add(yVect);
//		if (tileFrame != null) data.add(tileFrame);
//		return data;
//	}
//
//	/**
//	 * Gets a menu bar for this Plot. This may return null if
//	 * there is no menu bar.
//	 *
//	 * @return a menu bar for this Plot.
//	 */
//	public JMenuBar getMenuBar() {
//		JMenuBar bar = super.getMenuBar();
//		JMenu plotMenu = null;
//		for (int i = 0; i < bar.getMenuCount(); i++) {
//			JMenu menu = bar.getMenu(i);
//			if (menu.getText().equals("Plot")) {
//				plotMenu = menu;
//				break;
//			}
//		}
//
//
//		plotMenu.removeAll();
//		if (tileFrame != null) {
//			JMenuItem item = plotMenu.add(timeSeriesSelected);
//			item.setEnabled(false);
//			probeItems.add(item);
//
//			item = plotMenu.add(timeSeriesBarSelected);
//			item.setEnabled(false);
//			probeItems.add(item);
//
//			item = plotMenu.add(timeSeriesMin);
//			item = plotMenu.add(timeSeriesMax);
//			plotMenu.addSeparator();
//		}
//
//		plotMenu.add(new JMenuItem(new AbstractAction("Animate Plot") {
//			/**
//			 * 
//			 */
//			private static final long serialVersionUID = 6773265658811734474L;
//
//			public void actionPerformed(ActionEvent e) {
//				AnimationPanel panel = new AnimationPanel();
//				panel.init(frame.getAxes(), VectorPlot.this);
//			}
//		}));
//
//		return bar;
//	}
//
//
//	/**
//	 * Gets the panel that contains the plot component.
//	 *
//	 * @return the panel that contains the plot component.
//	 */
//	public JPanel getPanel() {
//		return panel;
//	}
//
//	/**
//	 * Gets a tool bar for this plot. This may
//	 * return null if there is no tool bar.
//	 *
//	 * @return a tool bar for this plot.
//	 */
//	public JToolBar getToolBar() {
//		JToolBar bar = new JToolBar();
//		bar.setFloatable(false);
//		timeLayerPanel = new TimeLayerPanel();
//		// we have at least two frames to coordinate here
//		// and they may have different indices, we just xVect as
//		// the base.
//		final DataFrameAxis layerAxis = xVect.getAxes().getZAxis();
//		if (hasNoLayer) {
//			timeLayerPanel.init(xVect.getAxes(), timeStep + xVect.getAxes().getTimeAxis().getOrigin(), 0, false);
//		} else {
//			timeLayerPanel.init(xVect.getAxes(), timeStep + xVect.getAxes().getTimeAxis().getOrigin(), layer
//							+ layerAxis.getOrigin(), layerAxis.getExtent() > 1);
//		}
//		ChangeListener changeListener = new ChangeListener() {
//			public void stateChanged(ChangeEvent e) {
//				if (processTimeChange) {
//					int time = timeLayerPanel.getTime() - xVect.getAxes().getTimeAxis().getOrigin();
//					if (hasNoLayer) updateTimeStepLayer(time, 0);
//					else {
//						int layer = timeLayerPanel.getLayer() - layerAxis.getOrigin();
//						updateTimeStepLayer(time, layer);
//					}
//				}
//			}
//		};
//
//		timeLayerPanel.addSpinnerListeners(changeListener, changeListener);
//		bar.add(timeLayerPanel);
//		return bar;
//	}
//
//	/**
//	 * Updates the plot to show the data at the new timestep.
//	 *
//	 * @param step the new step index
//	 */
//	public void updateTimeStep(int step) {
//		processTimeChange = false;
//		updateTimeStepLayer(step, layer);
//		timeLayerPanel.setTime(step + xVect.getAxes().getTimeAxis().getOrigin());
//		processTimeChange = true;
//	}
//
//	public void updateTimeStepLayer(int timeStep, int layer) {
//		this.timeStep = timeStep;
//		this.layer = layer;
//
//		vectDataset.addSeries(xVect, yVect, timeStep, layer);
//		if (tileDataset != null) {
//			tileDataset.addSeries(tileFrame, timeStep, layer);
//		}
//		createSubtitle();
//		chart.setTitle(createTitle());
//	}
//
//
//	/**
//	 * Gets the type of the Plot.
//	 *
//	 * @return the type of the Plot.
//	 */
//	public Formula.Type getType() {
//		return Formula.Type.VECTOR;
//	}
//
//	private String createTitle() {
//		String baseTitle = xVect.getVariable().getName() + " x " + yVect.getVariable().getName();
//		if (hasNoLayer) {
//			return baseTitle;
//		} else {
//			int displayedLayer = xVect.getAxes().getZAxis().getOrigin() + layer + 1;
//			return "Layer " + displayedLayer + " " + baseTitle;
//		}
//	}
//
//	private JFreeChart createChart(VectorXYDataset dataset) {
//		NumberAxis xAxis = new NumberAxis("X");
//		xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//
//		xAxis.setAutoRange(true);
//		xAxis.setUpperMargin(0.0);
//		xAxis.setLowerMargin(0.0);
//		xAxis.setAutoRangeIncludesZero(false);
//		xAxis.setNumberFormatOverride(new AxisNumberFormatter(new DecimalFormat()));
//
//		NumberAxis yAxis = new NumberAxis("Y");
//		yAxis.setAutoRange(true);
//		yAxis.setAutoRangeIncludesZero(false);
//		yAxis.setUpperMargin(0.0);
//		yAxis.setLowerMargin(0.0);
//		yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//		yAxis.setNumberFormatOverride(new AxisNumberFormatter(new DecimalFormat()));
//
//		VectorRenderer renderer = new VectorRenderer();
//		renderer.setSeriesPaint(0, Color.BLACK);
//		XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
//		chart = new JFreeChart(createTitle(), plot);
//		chart.removeLegend();
//		chart.setBackgroundPaint(Color.white);
//
//		plot.setBackgroundPaint(Color.WHITE);
//		plot.setRangeGridlinesVisible(false);
//		plot.setDomainGridlinesVisible(false);
//		plot.setAxisOffset(new RectangleInsets(5, 5, 5, 5));
//		createSubtitles();
//
//		if (tileDataset != null) {
//			XYBlockRenderer blockRenderer = new XYBlockRenderer();
//			plot.setDataset(1, tileDataset);
//			plot.setRenderer(1, blockRenderer);
//			updateScaleAxis(plot);
//		}
//		createMapAnnotation();
//		plot.addAnnotation(mapAnnotation);
//		createSubtitle();
//
//		return chart;
//	}
//
//	/**
//	 * Configure this plot according to the specifed configure info.
//	 *
//	 * @param config the configuration data
//	 */
//	@Override
//	public void configure(PlotConfiguration config) {
//		if (tileFrame != null) super.configure(config);
//		else {
//			TitleConfigurator titleConfig = new TitleConfigurator() {
//				public void configureSubtitle1(String text, Font font, Color color) {
//					TextTitle title = (TextTitle) chart.getSubtitle(subTitle1Index);
//					updateTextTitle(title, text, color, font);
//				}
//
//				public void configureSubtitle2(String text, Font font, Color color) {
//					TextTitle title = (TextTitle) chart.getSubtitle(subTitle2Index);
//					updateTextTitle(title, text, color, font);
//				}
//
//				public void configureTitle(String text, Font font, Color color) {
//					TextTitle title = chart.getTitle();
//					updateTextTitle(title, text, color, font);
//				}
//			};
//
//			JFreeChartConfigurator configurator = new JFreeChartConfigurator(chart, titleConfig, null);
//			configurator.configure(config);
//		}
//
//		VectorRenderer renderer = (VectorRenderer) ((XYPlot) chart.getPlot()).getRenderer();
//		Color color = config.getColor(VectorPlotConfiguration.VECTOR_COLOR);
//		if (color != null) renderer.setSeriesPaint(0, color);
//		this.config = config;
//	}
//
//	@Override
//	public PlotConfiguration getPlotConfiguration() {
//		PlotConfiguration config;
//		if (tileFrame != null) config = super.getPlotConfiguration();
//		else {
//			config = new VectorPlotConfiguration();
//			config.putObject(PlotConfiguration.FOOTER1_SHOW_LINE, true);
//			config.putObject(PlotConfiguration.FOOTER2_SHOW_LINE, true);
//			config.putObject(PlotConfiguration.OBS_SHOW_LEGEND, false);
//		}
//
//		VectorRenderer renderer = (VectorRenderer) ((XYPlot) chart.getPlot()).getRenderer();
//		config.putObject(VectorPlotConfiguration.VECTOR_COLOR, renderer.getSeriesPaint(0));
//		return getTitlesLabelsConfig(config);
//	}
//
//	protected void createSubtitle() {
//		if (tileFrame != null) {
//			DataUtilities.MinMaxPoint minMax = getMinMaxPoints();
//			Point minPoint = minMax.getMinPoints().iterator().next();
//			Point maxPoint = minMax.getMaxPoints().iterator().next();
//
//			int xOrigin = frame.getAxes().getXAxis().getOrigin();
//			minPoint.x += xOrigin;
//			maxPoint.x += xOrigin;
//			int yOrigin = frame.getAxes().getYAxis().getOrigin();
//			minPoint.y += yOrigin;
//			maxPoint.y += yOrigin;
//
//			StringBuilder builder = new StringBuilder("Min (");
//			if (showLatLon) {
//				Point2D latLon = getLatLonForAxisPoint(minPoint);
//				builder.append(Utilities.formatNumber(latLon.getX()));
//				builder.append(", ");
//				builder.append(Utilities.formatNumber(latLon.getY()));
//			} else {
//				builder.append(minPoint.x);
//				builder.append(", ");
//				builder.append(minPoint.y);
//			}
//			builder.append(")");
//			if (minMax.getMinPoints().size() > 1) builder.append("...");
//			builder.append(" = ");
//			builder.append(Utilities.formatNumber(minMax.getMin()));
//
//			builder.append(", Max (");
//			if (showLatLon) {
//				Point2D latLon = getLatLonForAxisPoint(maxPoint);
//				builder.append(Utilities.formatNumber(latLon.getX()));
//				builder.append(", ");
//				builder.append(Utilities.formatNumber(latLon.getY()));
//			} else {
//				builder.append(maxPoint.x);
//				builder.append(", ");
//				builder.append(maxPoint.y);
//			}
//			builder.append(")");
//			if (minMax.getMaxPoints().size() > 1) builder.append("...");
//			builder.append(" = ");
//			builder.append(Utilities.formatNumber(minMax.getMax()));
//
//			TextTitle title = (TextTitle) chart.getSubtitle(bottomTitle2Index);
//			title.setText(builder.toString());
//		}
//
////		Date date = frame.getAxes().getDate(timeStep + frame.getAxes().getTimeAxis().getOrigin());
//		GregorianCalendar date = frame.getAxes().getDate(timeStep + frame.getAxes().getTimeAxis().getOrigin());
//System.out.println("in VectorPlot createSubtitle set GregorianCalendar date = " + date.toString());
//		TextTitle title = (TextTitle) chart.getSubtitle(bottomTitle1Index);
//		title.setText(Utilities.formatDate(date));
//	}
//
//	/**
//	 * Gets the min max values for the current plot.
//	 *
//	 * @return the min max values for the current plot.
//	 */
//	protected DataUtilities.MinMax getMinMax() {
//		try {
//			if (hasNoLayer) return DataUtilities.minMax(frame, timeStep);
//			else return DataUtilities.minMax(frame, timeStep, layer);
//		} catch (InvalidRangeException ex) {
//			center.error("Error creating scale axis", ex);
//		}
//		return null;
//	}
//
//	/**
//	 * Gets the MinMax points for this plot.
//	 *
//	 * @return the MinMax points for this plot.
//	 */
//	protected DataUtilities.MinMaxPoint getMinMaxPoints() {
//		try {
//			if (hasNoLayer) return DataUtilities.minMaxPoint(frame, timeStep);
//			return DataUtilities.minMaxTLPoint(frame, timeStep, layer);
//		} catch (InvalidRangeException e) {
//			center.error("Error getting min max points", e);
//		}
//
//		return null;
//	}
//
//	/**
//	 * Converts the rectangle to 1 or 2 Point3i. The length
//	 * of the array must be two, but the second item can be null.
//	 * The points represent the top left hand corner and the bottom
//	 * right hand corner of the rect.
//	 *
//	 * @param rect the rect to convert.
//	 * @return an array of Point4i
//	 */
//	protected Point4i[] rectToPoints(Rectangle rect) {
//		Point4i[] points = new Point4i[2];
//		// rect is x,y
//		Point4i point = new Point4i(rect.x, rect.y, NO_VAL, NO_VAL);
//		points[0] = point;
//
//		if (rect.getWidth() > 0 || rect.getHeight() > 0) {
//			point = new Point4i(rect.x + rect.width, rect.y - rect.height, NO_VAL, NO_VAL);
//			points[1] = point;
//		}
//		return points;
//	}
//
//	private void performControlAction(Rectangle axisRect, Rectangle2D screenRect) {
//		switch (controlAction) {
//			case ZOOM:
//				if (screenRect.getWidth() < 0 || screenRect.getHeight() < 0) {
//					panel.restoreAutoBounds();
//					//mapAnnotation.resetAOE();
//				} else {
//					panel.doZoom(screenRect);
//					//Range xRange = new Range(axisRect.x,  axisRect.width);
//					//Range yRange = new Range(axisRect.y - axisRect.height, axisRect.height);
//					//ReferencedEnvelope env = frame.getAxes().getBoundingBox(xRange, yRange);
//					//mapAnnotation.setAOE(env);
//				}
//				break;
//			case PROBE:
//				probe(axisRect);
//				break;
//			default:
//		}
//	}
//
//	private void probe(Rectangle axisRect) {
//		Slice slice = new Slice();
//		slice.setTimeRange(timeStep, 1);
//		if (!hasNoLayer) slice.setLayerRange(layer, 1);
//		Axes<DataFrameAxis> axes = tileFrame.getAxes();
//		slice.setXRange(axisRect.x - axes.getXAxis().getOrigin(), axisRect.width + 1);
//		slice.setYRange(axisRect.y - axisRect.height - axes.getYAxis().getOrigin(), axisRect.height + 1);
//
//		try {
//			DataFrame subsection = tileFrame.slice(slice);
//			probedSlice = slice;
//			enableProbeItems(true);
//			eventProducer.fireProbeEvent(new ProbeEvent(this, subsection, slice, TILE));
//		} catch (InvalidRangeException e) {
//			center.error("Error while probing", e);
//		}
//	}
//
//	private Point getAxisCoordForMouse(Point mousePoint) {
//		Point2D p = panel.translateScreenToJava2D(mousePoint);
//		XYPlot plot = (XYPlot) chart.getPlot();
//		ChartRenderingInfo info = panel.getChartRenderingInfo();
//		Rectangle2D dataArea = info.getPlotInfo().getDataArea();
//
//		ValueAxis domainAxis = plot.getDomainAxis();
//		RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
//		ValueAxis rangeAxis = plot.getRangeAxis();
//		RectangleEdge rangeAxisEdge = plot.getRangeAxisEdge();
//		double x = domainAxis.java2DToValue(p.getX(), dataArea, domainAxisEdge);
//		double y = rangeAxis.java2DToValue(p.getY(), dataArea, rangeAxisEdge);
//		x = Math.rint(x);
//		y = Math.rint(y);
//
//		Range xRange = xVect.getAxes().getXAxis().getRange();
//		if (x < xRange.getOrigin())
//			x = xRange.getOrigin();
//		else if (x > xRange.getOrigin() + (xRange.getExtent() - 1))
//			x = xRange.getOrigin() + (xRange.getExtent() - 1);
//
//		Range yRange = xVect.getAxes().getYAxis().getRange();
//		if (y < yRange.getOrigin())
//			y = yRange.getOrigin();
//		else if (y > yRange.getOrigin() + (yRange.getExtent() - 1))
//			y = yRange.getOrigin() + (yRange.getExtent() - 1);
//
//		return new Point((int) x, (int) y);
//	}
//
//
//	class AreaFinder extends MouseInputAdapter {
//
//		private Point start, end;
//
//		// this rect measured axis coordinates
//		private Rectangle rect;
//
//		public void mousePressed(MouseEvent e) {
//			Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
//			if (screenDataArea != null) {
//				start = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
//				rect = new Rectangle(getAxisCoordForMouse(start), new Dimension(0, 0));
//				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect),
//								false));
//			} else {
//				start = null;
//			}
//		}
//
//		private Point getPointInRectangle(int x, int y, Rectangle2D area) {
//			x = (int) Math.max(Math.ceil(area.getMinX()), Math.min(x, Math.floor(area.getMaxX())));
//			y = (int) Math.max(Math.ceil(area.getMinY()), Math.min(y, Math.floor(area.getMaxY())));
//			return new Point(x, y);
//		}
//
//		public void mouseDragged(MouseEvent e) {
//			if (start != null) {
//				Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
//				if (screenDataArea != null) {
//					end = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
//					Point p = getAxisCoordForMouse(end);
//					rect.width = p.x - rect.x;
//					rect.height = rect.y - p.y;
//					boolean finished = rect.width < 0 || rect.height < 0;
//					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect),
//									finished));
//				} else {
//					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, true));
//				}
//			}
//		}
//
//		public void mouseMoved(MouseEvent e) {
//			Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
//			if (screenDataArea != null) {
//				Point p = getAxisCoordForMouse(e.getPoint());
//				Rectangle rect = new Rectangle(p.x, p.y, 0, 0);
//				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect), false));
//			} else {
//				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(new Rectangle(0, 0, 0, 0), true));
//			}
//		}
//
//		public void mouseExited(MouseEvent e) {
//			eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(new Rectangle(0, 0, 0, 0), true));
//		}
//
//		public void mouseReleased(MouseEvent e) {
//			if (start != null) {
//				Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
//				if (screenDataArea != null) {
//					end = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
//					Point p = getAxisCoordForMouse(end);
//					rect.width = p.x - rect.x;
//					rect.height = rect.y - p.y;
//					performControlAction(rect, createRect(start, end));
//				}
//				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, true));
//			}
//		}
//
//		private Rectangle2D createRect(Point start, Point end) {
//			return new Rectangle2D.Double(start.x, start.y, end.x - start.x, end.y - start.y);
//		}
//	}
//
//
//	@Override
//	public String getTitle() {
//		return chart.getTitle().getText();
//	}
//}
