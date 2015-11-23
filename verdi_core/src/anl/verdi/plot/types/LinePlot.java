package anl.verdi.plot.types;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.swing.AbstractAction;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartTheme;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.Millisecond;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.Range;
import anl.verdi.data.Slice;
import anl.verdi.formula.Formula;
import anl.verdi.formula.Formula.Type;
import anl.verdi.plot.config.JFreeChartConfigurator;
import anl.verdi.plot.config.LoadConfiguration;
import anl.verdi.plot.config.LoadTheme;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.PlotConfigurationIO;
import anl.verdi.plot.config.SaveConfiguration;
import anl.verdi.plot.config.SaveTheme;
import anl.verdi.plot.config.TimeSeriesPlotConfiguration;
import anl.verdi.plot.config.UnitsConfigurator;
import anl.verdi.plot.gui.AreaSelectionEvent;
import anl.verdi.plot.gui.LayerChartPanel;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotFactory;
import anl.verdi.plot.gui.PlotListener;
import anl.verdi.plot.probe.PlotEventProducer;
import anl.verdi.plot.probe.ProbeEvent;
import anl.verdi.plot.util.PlotExporterAction;
import anl.verdi.plot.util.PlotPrintAction;
import anl.verdi.plot.util.PlotProperties;
import anl.verdi.util.Tools;
import anl.verdi.util.VUnits;
//import simphony.util.messages.MessageCenter;
import ucar.ma2.InvalidRangeException;

/**
 * Class used to create time series line plot
 *
 * @author Mark Altaweel
 */

public class LinePlot extends AbstractPlot implements ChartProgressListener {

	static final Logger Logger = LogManager.getLogger(LinePlot.class.getName());
//	private static MessageCenter center = MessageCenter.getMessageCenter(LinePlot.class);
	private TimeSeriesCollection dataset;

	private java.util.List<DataFrame> frames;
	private JFreeChart chart;
	private ControlAction controlAction = ControlAction.ZOOM;
	private PlotEventProducer eventProducer = new PlotEventProducer();
	private int layer = 0;
	private PlotConfiguration config;
	private JChartTitlesLabels titlesLabels;
	
	public void viewClosed() {
		
		dataset = null;
		frames = null;
		chart = null;
		eventProducer = null;
		config = null;
		titlesLabels = null;
		
	}	

	public LinePlot(java.util.List<DataFrame> frames, PlotConfiguration config) {
		Logger.debug("in constructor for LinePlot.java");
		this.frames = frames;
		this.config = config;
		XYDataset dataset = createDataset();
		chart = createChart(dataset, config);
		panel = new VerdiChartPanel(chart, true);
		AreaFinder finder = new AreaFinder();
		panel.addMouseListener(finder);
		panel.addMouseMotionListener(finder);
		titlesLabels = new JChartTitlesLabels(chart);
		
		if (config.getSubtitle1() == null || config.getSubtitle1().trim().isEmpty())
			config.setSubtitle1(Tools.getDatasetNames(frames.get(0)));
		
		PlotConfiguration defaultConfig = getPlotConfiguration();
		defaultConfig.merge(config);
		configure(defaultConfig);
		
		/** Check if a chart theme has been loaded. */
		ChartTheme theme = PlotProperties.getInstance().getCurrentTheme();
		if (theme != null) theme.apply(chart);
	}


	/**
	 * Gets the data that this Plot plots.
	 *
	 * @return the data that this Plot plots.
	 */
	public List<DataFrame> getData() {
		return new ArrayList<DataFrame>(frames);
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
		final LayerChartPanel layerPanel = new LayerChartPanel();
		DataFrame frame = frames.get(0);
		int layerVal = frame.getAxes().getZAxis() == null ? LayerChartPanel.NO_LAYER :
						layer + frame.getAxes().getZAxis().getOrigin();
		layerPanel.init(frame.getAxes(), layerVal);
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				DataFrame frame = frames.get(0);
				if (frame.getAxes().getZAxis() != null) {
					int layer = layerPanel.getLayer() - frame.getAxes().getZAxis().getOrigin();
					updateLayer(layer);
				}
			}
		};

		layerPanel.addSpinnerListener(changeListener);
		bar.add(layerPanel);
		return bar;
	}


	/**
	 * Gets a menu bar for this Plot.
	 *
	 * @return a menu bar for this Plot.
	 */
	public JMenuBar getMenuBar() {
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic('F');
		menu.add(new PlotPrintAction(panel));
		menu.add(new PlotExporterAction(this));
		bar.add(menu);

		menu = new JMenu("Configure");
		menu.add(new AbstractAction("Configure Plot") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -798912159185168000L;

			public void actionPerformed(ActionEvent e) {
				panel.doEditChartProperties();
			}
		});
		menu.add(new LoadConfiguration(this));
		menu.add(new SaveConfiguration(this));
		menu.add(new LoadTheme(this, chart));
		
		menu.add(new AbstractAction("Edit Chart Theme") {
			private static final long serialVersionUID = 5016577781516485377L;

			public void actionPerformed(ActionEvent e) {
				panel.doEditChartProperties();
			}
		});
		
		menu.add(new SaveTheme(this));
		bar.add(menu);

		menu = new JMenu("Controls");
		bar.add(menu);
		ButtonGroup grp = new ButtonGroup();
		JMenuItem item = menu.add(new JCheckBoxMenuItem(new AbstractAction("Zoom") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5016577781516485377L;

			public void actionPerformed(ActionEvent e) {
				//JCheckBoxMenuItem box = (JCheckBoxMenuItem) e.getSource();
				//panel.setMouseZoomEnabled(box.isSelected());
				controlAction = ControlAction.ZOOM;
				panel.setRangeZoomable(true);
			}
		}));
		item.setSelected(true);
		grp.add(item);

		item = menu.add(new JCheckBoxMenuItem(new AbstractAction("Probe") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 565472785378938457L;

			public void actionPerformed(ActionEvent e) {
				//panel.setMouseZoomEnabled(false);
				controlAction = ControlAction.PROBE;
				panel.setRangeZoomable(false);
			}
		}));
		grp.add(item);
		menu.add(item);

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
		int origin = frames.get(0).getAxes().getTimeAxis().getOrigin();
		slice.setTimeRange(axisRect.x - origin, axisRect.width + 1);
		if (frames.get(0).getAxes().getZAxis() != null) slice.setLayerRange(layer, 1);
		try {
			for (DataFrame frame : frames) {
				DataFrame subsection = frame.slice(slice);
				eventProducer.fireProbeEvent(new ProbeEvent(this, subsection, slice, Formula.Type.TIME_SERIES_LINE));
			}
		} catch (InvalidRangeException e) {
			Logger.error("Error while probing " + e.getMessage());
		}
	}

	/**
	 * Adds the specified PlotListener.
	 *
	 * @param listener the plot listener to add
	 */
	public void addPlotListener(PlotListener listener) {
		eventProducer.addListener(listener);
	}

	/**
	 * Removes the specified PlotListener.
	 *
	 * @param listener the plot listener to remove
	 */
	public void removePlotListener(PlotListener listener) {
		eventProducer.removeListener(listener);
	}

	private long getTimeForMouse(Point mousePoint) {
		Point2D p = panel.translateScreenToJava2D(mousePoint);
		XYPlot plot = (XYPlot) chart.getPlot();
		ChartRenderingInfo info = panel.getChartRenderingInfo();
		Rectangle2D dataArea = info.getPlotInfo().getDataArea();

		ValueAxis domainAxis = plot.getDomainAxis();
		RectangleEdge domainAxisEdge = plot.getDomainAxisEdge();
		return (long) domainAxis.java2DToValue(p.getX(), dataArea, domainAxisEdge);
	}

	private int findGETS(Date date) {
		DataFrame frame = frames.get(0);
		Range timeRange = frame.getAxes().getTimeAxis().getRange();
		int n = (int) (timeRange.getOrigin() + timeRange.getExtent());
//		for (int i = timeRange.getOrigin(); i < n; i++) {
//			Date d = frame.getAxes().getDate(i);
//			if (d.getTime() >= date.getTime()) return i;
		int iOrigin = (int) timeRange.getOrigin();
		for (int i = iOrigin; i < n; i++) {
			GregorianCalendar d = frame.getAxes().getDate(i);
			Logger.debug("in LinePlot findGETS computed GregorianCalendar d");
			if (d.getTimeInMillis() >= date.getTime()) return i;
		}
		return Axes.TIME_STEP_NOT_FOUND;

	}

	private int findLETS(Date date) {
		DataFrame frame = frames.get(0);
		Range timeRange = frame.getAxes().getTimeAxis().getRange();
		int n = (int) (timeRange.getOrigin() + timeRange.getExtent());
//		for (int i = n - 1; i >= timeRange.getOrigin(); i--) {
//			Date d = frame.getAxes().getDate(i);
//			if (d.getTime() <= date.getTime()) return i;
		int iOrigin = (int) timeRange.getOrigin();
		for (int i = n - 1; i >= iOrigin; i--) {
			GregorianCalendar d = frame.getAxes().getDate(i);
			Logger.debug("in LinePlot findLETS computed GregorianCalendar d");
			if (d.getTimeInMillis() <= date.getTime()) return i;
		}

		return Axes.TIME_STEP_NOT_FOUND;

	}

	private String createTitle() {
		String titlePrefix = config.getProperty(PlotFactory.TITLE_PREFIX);
		if (titlePrefix == null) titlePrefix = "";
		DataFrame frame = frames.get(0);
		if (frame.getAxes().getZAxis() != null) {
			return titlePrefix + "Layer: " +
							(frame.getAxes().getZAxis().getOrigin() + layer + 1) + " " + frame.getVariable().getName();
		} else {
			return titlePrefix + frame.getVariable().getName();
		}
	}

	public void updateLayer(int layer) {
		this.layer = layer;
		chart.setTitle(createTitle());
		int i = 0;
		for (DataFrame frame : frames) {
			TimeSeries series = dataset.getSeries(i++);
			series.clear();
			Axes<DataFrameAxis> rAxes = frame.getAxes();
			DataFrameAxis dft = rAxes.getTimeAxis();
			DataFrameIndex index = frame.getIndex();
			index.setLayer(layer);
			int timeOrigin = dft.getOrigin();
			for (int t = 0; t < dft.getExtent(); t++) {
				index.setTime(t);
				//series.add(new Millisecond(rAxes.getDate(t + timeOrigin)), frame.getDouble(index));
					// change for GregorianCalendar vs. old Date
				double aValue = frame.getDouble(index);
				GregorianCalendar aCalendar = rAxes.getDate(t + timeOrigin);
				Logger.debug("in LinePlot, updateLayer, computed GregorianCalendar aCalendar");
				Date aDate = aCalendar.getTime();
				series.add(new Millisecond(aDate),aValue);
			}
		}
	}

	private DataUtilities.MinMax getMinMax() {
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;

		for (DataFrame frame : frames) {
			DataUtilities.MinMax minMax = DataUtilities.minMax(frame);
			min = Math.min(min, minMax.getMin());
			max = Math.max(max, minMax.getMax());
		}

		return new DataUtilities.MinMax(min, max);
	}

	private JFreeChart createChart(XYDataset dataset, PlotConfiguration config) {
		JFreeChart chart = ChartFactory.createTimeSeriesChart(
						createTitle(),  // title
						"Time Step",             // x-axis label
						VUnits.getFormattedName(frames.get(0).getVariable().getUnit()),   // y-axis label
						dataset,            // data
						true,               // create legend?
						true,               // generate tooltips?
						false               // generate URLs?
		);

		chart.setBackgroundPaint(Color.WHITE);
		chart.addProgressListener(this);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinePaint(Color.GRAY);
		plot.setRangeGridlinePaint(Color.GRAY);
		plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
		plot.setDomainCrosshairVisible(true);
		plot.setRangeCrosshairVisible(true);

		NumberAxis rAxis = (NumberAxis) plot.getRangeAxis();
		DataUtilities.MinMax minMax = getMinMax();
		double tenth = (minMax.getMax() - minMax.getMin()) / 10;
		if (tenth == 0) {
			rAxis.setRangeAboutValue(minMax.getMin(), .01);
		} else {
			rAxis.setRange(minMax.getMin() - tenth, minMax.getMax() + tenth);
		}
		rAxis.setAutoRange(false);


		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseShapesVisible(true);
		renderer.setBaseShapesFilled(true);
		
		DateAxis axis = (DateAxis) plot.getDomainAxis();
		axis.setTimeZone(TimeZone.getTimeZone("UTC"));
		// new SimpleDateFormat("MMMMM dd, yyyy HH:mm:ss z");
//		SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMM d, h a");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		axis.setDateFormatOverride(dateFormat);
		axis.setVerticalTickLabels(false);

		return chart;
	}

	public JPanel getPanel() {
		return panel;
	}

	// this handles cross-hair type probing.
	public void chartProgress(ChartProgressEvent event) {
		if (event.getType() != ChartProgressEvent.DRAWING_FINISHED) return;
		if (this.panel != null) {
			JFreeChart chart = this.panel.getChart();
			if (chart != null) {
				XYPlot plot = (XYPlot) chart.getPlot();
				long xx = (long) plot.getDomainCrosshairValue();
				Date date = new Date(xx);
				int step = frames.get(0).getAxes().getTimeStep(date);
				if (step != Axes.TIME_STEP_NOT_FOUND) {
					Slice slice = new Slice();
					int origin = frames.get(0).getAxes().getTimeAxis().getOrigin();
					slice.setTimeRange(step - origin, 1);
					slice.setLayerRange(layer, 1);
					try {
						if (controlAction == ControlAction.PROBE) {
							for (DataFrame frame : frames) {
								DataFrame subsection = frame.slice(slice);
								eventProducer.fireProbeEvent(new ProbeEvent(this, subsection, slice, Formula.Type.TIME_SERIES_LINE));
							}
						}
					} catch (InvalidRangeException e) {
						Logger.error("Error while probing " + e.getMessage());
						return;
					}
				}

			}
		}
	}

	class AreaFinder extends MouseInputAdapter {

		private Point start, end;
		// this rect measured axis coordinates
		private Rectangle rect;

		public void mousePressed(MouseEvent e) {
			Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
			if (screenDataArea != null) {
				start = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
				long time = getTimeForMouse(start);
				int step = findGETS(new Date(time));
				rect = new Rectangle(step, -1, 0, 0);
				// eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, false));
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

		/*
		public void mouseDragged(MouseEvent e) {
			if (start != null) {
				Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
				if (screenDataArea != null) {
					end = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
					Point p = getTimeForMouse(end);
					rect.width = p.x - rect.x;
					rect.height = rect.y - p.y;
					boolean finished = rect.width < 0 || rect.height < 0;
					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, finished));
				} else {
					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, true));
				}
			}
		}
		*/

		public void mouseReleased(MouseEvent e) {
			if (start != null) {
				Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
				if (screenDataArea != null) {
					end = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
					long time = getTimeForMouse(end);
					int step = findLETS(new Date(time));
					if (step != Axes.TIME_STEP_NOT_FOUND && step >= rect.x && rect.x != -1) {
						rect.width = step - rect.x;
						rect.height = -1;
						performControlAction(rect, createRect(start, end));
					}
				}
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, true));
			}
		}

		private Rectangle2D createRect(Point start, Point end) {
			return new Rectangle2D.Double(start.x, start.y, end.x - start.x, end.y - start.y);
		}
	}

	private XYDataset createDataset() {
		dataset = new TimeSeriesCollection();
		if (frames.size() == 1) {
			DataFrame frame = frames.get(0);
			String title = (String) config.getObject(frame);
			if (title == null) title = config.getProperty(PlotFactory.SUBTITLE);
			if (title == null) title = "Avg. Value (" + VUnits.getFormattedName(frame.getVariable().getUnit()) + ")";
			TimeSeries series1 = new TimeSeries(title);		//, Millisecond.class
			Axes<DataFrameAxis> rAxes = frame.getAxes();
			DataFrameAxis dft = rAxes.getTimeAxis();
			DataFrameIndex index = frame.getIndex();
			if (rAxes.getZAxis() != null) index.setLayer(layer);
			int timeOrigin = dft.getOrigin();
			for (int t = 0; t < dft.getExtent(); t++) {
				index.setTime(t);
				// change for GregorianCalendar vs. old Date
				//series1.add(new Millisecond(rAxes.getDate(t + timeOrigin)), frame.getDouble(index));
				double aValue = frame.getDouble(index);
				GregorianCalendar aCalendar = rAxes.getDate(t + timeOrigin);
				Logger.debug("in LinePlot, createDataset, created GregorianCalendar aCalendar");
				Date aDate = aCalendar.getTime();
				series1.add(new Millisecond(aDate), aValue);
			}
			dataset.addSeries(series1);
		} else {
			for (DataFrame frame : frames) {
				TimeSeries series = new TimeSeries(config.getObject(frame).toString());	//, Millisecond.class
				Axes<DataFrameAxis> rAxes = frame.getAxes();
				DataFrameAxis dft = rAxes.getTimeAxis();
				DataFrameIndex index = frame.getIndex();
				if (rAxes.getZAxis() != null) index.setLayer(layer);
				int timeOrigin = dft.getOrigin();
				for (int t = 0; t < dft.getExtent(); t++) {
					index.setTime(t);
					// change for GregorianCalendar vs. old Date
					//series.add(new Millisecond(rAxes.getDate(t + timeOrigin)), frame.getDouble(index));
					double aValue = frame.getDouble(index);
					GregorianCalendar aCalendar = rAxes.getDate(t + timeOrigin);
					Logger.debug("in LinePlot createDataset part 2, create GregorianCalendar aCalendar");
					Date aDate = aCalendar.getTime();
					series.add(new Millisecond(aDate),aValue);
				}
				dataset.addSeries(series);
			}
		}
		return dataset;
	}

	/**
	 * Gets the type of the Plot.
	 *
	 * @return the type of the Plot.
	 */
	public Formula.Type getType() {
		return Formula.Type.TIME_SERIES_LINE;
	}


	/**
	 * Configure this Plot according to the specified PlotConfiguration.
	 *
	 * @param config the new plot configuration
	 */
	@Override
	public void configure(PlotConfiguration config, Plot.ConfigSoure source) {
	configure(config);
	}

	@Override
	public void configure(PlotConfiguration config) {
		String configFile = config.getConfigFileName();
		if (configFile != null) {
			try {
				configure(new PlotConfigurationIO().loadConfiguration(new File(configFile)));
			} catch (IOException ex) {
				Logger.error("Error loading configuration " + ex.getMessage());
			}
		}

		UnitsConfigurator unitsConfig = new UnitsConfigurator() {
			public void configureUnits(String text, Font font, Color color) {
				LegendTitle legend = chart.getLegend();
				if (!text.equals(dataset.getSeries(0).getKey())) {
					dataset.getSeries(0).setKey(text);
				}
				if (font != null) legend.setItemFont(font);
				if (color != null) legend.setItemPaint(color);
			}

			public void configureUnitsTick(Boolean show, Font font, Color color) {}
		};
		// do the titles and the labels
		JFreeChartConfigurator configurator = new JFreeChartConfigurator(chart,
						titlesLabels.getTitleConfigurator(), unitsConfig);
		configurator.configure(config);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) ((XYPlot)chart.getPlot()).getRenderer();
		Color color = config.getColor(TimeSeriesPlotConfiguration.SERIES_COLOR);
//		if (color != null) renderer.setSeriesPaint(0, color);
	}

	/**
	 * Gets this Plot's configuration data.
	 *
	 * @return this Plot's configuration data.
	 */
	@Override
	public PlotConfiguration getPlotConfiguration() {
		PlotConfiguration config = new PlotConfiguration();
		config = titlesLabels.getConfiguration(config);

		config.putObject(PlotConfiguration.PLOT_TYPE, Type.TIME_SERIES_LINE); //NOTE: to differentiate plot types
		config.putObject(PlotConfiguration.UNITS, dataset.getSeries(0).getKey());
		config.putObject(PlotConfiguration.UNITS_FONT, chart.getLegend().getItemFont());
		config.putObject(PlotConfiguration.UNITS_COLOR, chart.getLegend().getItemPaint());
		config.putObject(PlotConfiguration.FOOTER1_SHOW_LINE, true);
		config.putObject(PlotConfiguration.FOOTER2_SHOW_LINE, true);
		config.putObject(PlotConfiguration.OBS_SHOW_LEGEND, false);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) ((XYPlot)chart.getPlot()).getRenderer();
		config.putObject(TimeSeriesPlotConfiguration.SERIES_COLOR, (Color)renderer.getSeriesPaint(0));
		return config;
	}


	@Override
	public String getTitle() {
		return chart.getTitle().getText();
	}
}
