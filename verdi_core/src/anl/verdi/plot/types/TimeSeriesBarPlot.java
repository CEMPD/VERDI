package anl.verdi.plot.types;


import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import simphony.util.messages.MessageCenter;
import ucar.ma2.InvalidRangeException;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.Range;
import anl.verdi.data.Slice;
import anl.verdi.formula.Formula;
import anl.verdi.plot.config.JFreeChartConfigurator;
import anl.verdi.plot.config.LoadConfiguration;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.PlotConfigurationIO;
import anl.verdi.plot.config.SaveConfiguration;
import anl.verdi.plot.config.TimeSeriesPlotConfiguration;
import anl.verdi.plot.config.UnitsConfigurator;
import anl.verdi.plot.gui.LayerChartPanel;
import anl.verdi.plot.gui.PlotFactory;
import anl.verdi.plot.gui.PlotListener;
import anl.verdi.plot.probe.PlotEventProducer;
import anl.verdi.plot.probe.ProbeEvent;
import anl.verdi.plot.util.PlotExporterAction;
import anl.verdi.plot.util.PlotPrintAction;
import anl.verdi.util.Tools;
import anl.verdi.util.VUnits;


public class TimeSeriesBarPlot extends AbstractPlot {

	private static final MessageCenter center = MessageCenter.getMessageCenter(TimeSeriesBarPlot.class);
	private static final Range BAD_RANGE = new Range(-1, -1);

	private DataFrame frame;
	private JFreeChart chart;
	private ControlAction controlAction = ControlAction.ZOOM;
	private PlotEventProducer eventProducer = new PlotEventProducer();
	private DefaultCategoryDataset dataset;
	private AreaFinder finder;
	private SimpleDateFormat dateFormat;
	private int layer = 0;
	private PlotConfiguration config;
	private JChartTitlesLabels titlesLabels;
	private RowKey rowKey;

	public void viewClosed() {

		frame = null;
		chart = null;
		eventProducer = null;
		dataset = null;
		finder = null;
		dateFormat = null;

		config = null;
		titlesLabels = null;
		rowKey = null;
	}

	public TimeSeriesBarPlot(DataFrame frame, PlotConfiguration config) {
		this.frame = frame;
		this.config = config;
		dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		dataset = createDataset();
		chart = createChart(dataset);
		panel = new VerdiChartPanel(chart, true);
		panel.setZoom(true);
		panel.setDomainZoomable(true);
		panel.setRangeZoomable(true);
		finder = new AreaFinder();

		titlesLabels = new JChartTitlesLabels(chart);
		
		if (config.getSubtitle1() == null || config.getSubtitle1().trim().isEmpty())
			config.setSubtitle1(Tools.getDatasetNames(frame));
		
		PlotConfiguration defaultConfig = getPlotConfiguration();
		defaultConfig.merge(config);
		configure(defaultConfig);
	}


	/**
	 * Gets the data that this Plot plots.
	 *
	 * @return the data that this Plot plots.
	 */
	public List<DataFrame> getData() {
		java.util.List<DataFrame> list = new ArrayList<DataFrame>();
		list.add(frame);
		return list;
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
		final Axes<DataFrameAxis> axes = frame.getAxes();
		int layerVal = axes.getZAxis() == null ? LayerChartPanel.NO_LAYER :
						layer + axes.getZAxis().getOrigin();
		layerPanel.init(axes, layerVal);
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
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

	private String createTitle() {
		String titlePrefix = config.getProperty(PlotFactory.TITLE_PREFIX);
		if (titlePrefix == null) titlePrefix = "";
		Axes<DataFrameAxis> axes = frame.getAxes();
		if (axes.getZAxis() == null) {
			return titlePrefix + frame.getVariable().getName();
		} else {
			return titlePrefix + "Layer: " + (axes.getZAxis().getOrigin() + layer + 1) + " " + frame.getVariable().getName();
		}
	}

	public void updateLayer(int layer) {
		this.layer = layer;
		chart.setTitle(createTitle());

		/* // commented out because we want to keep the same axis while layers are updated
		NumberAxis rangeAxis = (NumberAxis) ((CategoryPlot) chart.getPlot()).getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(false);
		try {
			DataUtilities.MinMax minMax = DataUtilities.minMaxForTimeLayer(frame, layer);
			double interval = (minMax.getMax() - minMax.getMin()) / 10;
			rangeAxis.setRange(minMax.getMin() - interval, minMax.getMax() + interval);
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}
		*/

		dataset.clear();
		Axes<DataFrameAxis> axes = frame.getAxes();
		DataFrameAxis time = axes.getTimeAxis();
		int origin = time.getOrigin();
		DataFrameIndex index = frame.getIndex();
		index.setLayer(layer);
		for (int t = 0; t < time.getExtent(); t++) {
			index.setTime(t);
			GregorianCalendar date = axes.getDate(t + origin);		// 2014 changed Date to GregorianCalendar
			dataset.addValue(frame.getDouble(index), "Series 1", dateFormat.format(date.getTimeInMillis()));
		}
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
			private static final long serialVersionUID = 4326456845968924502L;

			public void actionPerformed(ActionEvent e) {
				panel.doEditChartProperties();
			}
		});
		menu.add(new LoadConfiguration(this));
		menu.add(new SaveConfiguration(this));
		bar.add(menu);

		menu = new JMenu("Controls");
		bar.add(menu);
		ButtonGroup grp = new ButtonGroup();
		JMenuItem item = menu.add(new JCheckBoxMenuItem(new AbstractAction("Zoom") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -6307695766431600549L;

			public void actionPerformed(ActionEvent e) {
				controlAction = ControlAction.ZOOM;
				panel.setRangeZoomable(true);
				panel.removeMouseListener(finder);
				panel.removeMouseMotionListener(finder);
			}
		}));
		item.setSelected(true);
		grp.add(item);

		item = menu.add(new JCheckBoxMenuItem(new AbstractAction("Probe") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5502954087561812958L;

			public void actionPerformed(ActionEvent e) {
				controlAction = ControlAction.PROBE;
				panel.setRangeZoomable(false);
				panel.addMouseListener(finder);
				panel.addMouseMotionListener(finder);
			}
		}));
		grp.add(item);
		menu.add(item);

		return bar;
	}

	private void performControlAction(Rectangle2D screenRect) {
		switch (controlAction) {
			case ZOOM:
				// jfreechart will handle the zoom
				break;
			case PROBE:
				probe(screenRect);
				break;
			default:
		}
	}

	private void probe(Rectangle2D screenRect) {
		try {
			Range range = getTimeRange(screenRect);
			probe(range);
		} catch (ParseException e) {
			center.error("Error while probing", e);
		}
	}

	// assumes the range has already been
	// fixed to match the current time range constraints.
	private void probe(Range range) {
		try {
			if (!range.equals(BAD_RANGE) && range.getExtent() > 0) {
				Slice slice = new Slice();
				slice.setTimeRange(range.getOrigin(), range.getExtent());
				if (frame.getAxes().getZAxis() != null) slice.setLayerRange(layer, 1);
				DataFrame subsection = frame.slice(slice);
				eventProducer.fireProbeEvent(new ProbeEvent(this, subsection, slice, Formula.Type.TIME_SERIES_BAR));
			}
		} catch (InvalidRangeException e) {
			center.error("Error while probing", e);
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


	private Range getTimeRange(Rectangle2D screenRect) throws ParseException {
		//Point2D p = panel.translateScreenToJava2D(new Point((int) screenRect.getX(),
		//				(int) screenRect.getY()));
		Insets insets = panel.getInsets();
		int x = (int) ((screenRect.getX() - insets.left) / panel.getScaleX());

		ChartRenderingInfo info = panel.getChartRenderingInfo();
		EntityCollection collection = info.getEntityCollection();

		CategoryItemEntity entity = null;
		for (int i = 0; i < collection.getEntityCount(); i++) {
			ChartEntity obj = collection.getEntity(i);
			if (obj instanceof CategoryItemEntity) {
				entity = (CategoryItemEntity) obj;
				Rectangle2D bounds = entity.getArea().getBounds2D();
				if (bounds.getX() >= x) break;
			}
		}
		if (entity == null) return BAD_RANGE;
		// CategoryItemEntity.getCategory() deprecated; replace by getColumnKey(), returns Comparable
		int start = frame.getAxes().getTimeStep(dateFormat.parse(entity.getColumnKey().toString()));

		//p = panel.translateScreenToJava2D(new Point((int) (screenRect.getX() + screenRect.getWidth()),
		//				(int) (screenRect.getY() + screenRect.getHeight())));
		x = (int) ((screenRect.getX() + screenRect.getWidth() - insets.left) / panel.getScaleX());
		for (int i = collection.getEntityCount() - 1; i >= 0; i--) {
			ChartEntity obj = collection.getEntity(i);
			if (obj instanceof CategoryItemEntity) {
				entity = (CategoryItemEntity) obj;
				Rectangle2D bounds = entity.getArea().getBounds2D();
				if (bounds.getX() + bounds.getWidth() <= x) break;
			}
		}
		if (entity == null) return BAD_RANGE;
		// CategoryItemEntity.getCategory() deprecated; replace by getColumnKey(), returns Comparable
		int end = frame.getAxes().getTimeStep(dateFormat.parse(entity.getColumnKey().toString()));
		return new Range(start, (end - start) + 1);
	}


	private JFreeChart createChart(CategoryDataset dataset) {
		JFreeChart chart = ChartFactory.createBarChart(
						createTitle(),
						"Time Step",
						VUnits.getFormattedName(frame.getVariable().getUnit()),   // y axis label
						dataset,
						PlotOrientation.VERTICAL,
						true,
						true,
						false
		);
		chart.setBackgroundPaint(Color.white);

		CategoryPlot plot = chart.getCategoryPlot();
		plot.setDomainGridlinesVisible(true);

		CategoryAxis axis = plot.getDomainAxis();
		axis.setCategoryLabelPositions(CategoryLabelPositions.DOWN_90);
		axis.setLabel("Time Step");

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(false);
		rangeAxis.setAutoRange(false);

		DataUtilities.MinMax minMax = DataUtilities.minMax(frame);
		double interval = (minMax.getMax() - minMax.getMin()) / 10;
		if (interval == 0) {
			rangeAxis.setRangeAboutValue(minMax.getMin(), .01);
		} else rangeAxis.setRange(minMax.getMin() - interval, minMax.getMax() + interval);

		
		return chart;
	}

	public JPanel getPanel() {
		return panel;
	}

	class AreaFinder extends MouseInputAdapter {

		private Point start;
		private Rectangle2D rect;

		public void mousePressed(MouseEvent e) {
			Rectangle2D screenDataArea = panel.getScreenDataArea(e.getX(), e.getY());
			if (screenDataArea != null) {
				this.start = getPointInRectangle(e.getX(), e.getY(), screenDataArea);
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
			Rectangle2D screenDataArea = panel.getScreenDataArea((int) start.getX(), (int) start.getY());

			Graphics2D g2 = (Graphics2D) panel.getGraphics();
			// use XOR to erase the previous rectangle (if any)...
			g2.setXORMode(java.awt.Color.gray);
			if (this.rect != null) g2.draw(this.rect);

			if (screenDataArea != null) {
				double xmax = Math.min(e.getX(), screenDataArea.getMaxX());
				double ymax = Math.min(e.getY(), screenDataArea.getMaxY());
				this.rect = new Rectangle2D.Double(
								this.start.getX(), this.start.getY(),
								xmax - this.start.getX(), ymax - this.start.getY());
			}

			if (this.rect != null) g2.draw(this.rect);
			g2.dispose();
		}


		public void mouseReleased(MouseEvent e) {
			if (this.rect != null) {
				Graphics2D g2 = (Graphics2D) panel.getGraphics();
				g2.setXORMode(java.awt.Color.gray);
				g2.draw(this.rect);
				g2.dispose();
				// need to get the list of time series in the draw rect.
				performControlAction(rect);
			}

			start = null;
			rect = null;
		}

		public void mouseClicked(MouseEvent e) {
			start = null;
			rect = null;
			Insets insets = panel.getInsets();
			int x = (int) ((e.getX() - insets.left) / panel.getScaleX());
			int y = (int) ((e.getY() - insets.top) / panel.getScaleY());

			ChartRenderingInfo info = panel.getChartRenderingInfo();
			EntityCollection collection = info.getEntityCollection();
			ChartEntity obj = collection.getEntity(x, y);
			if (obj != null && obj instanceof CategoryItemEntity) {
				CategoryItemEntity entity = (CategoryItemEntity) obj;
				try {		
					// CategoryItemEntity.getCategory() deprecated; replace by getColumnKey(), returns Comparable
					int start = frame.getAxes().getTimeStep(dateFormat.parse(entity.getColumnKey().toString()));
					if (start != Axes.TIME_STEP_NOT_FOUND) {
						probe(new Range(start, 1));
					}
				} catch (ParseException e1) {
					e1.printStackTrace();
				}

			}
		}
	}

	private static class RowKey implements Comparable {
		private String val;

		public RowKey(String val) {
			this.val = val;
		}

		public String getVal() {
			return val;
		}

		public void setVal(String val) {
			this.val = val;
		}

		public String toString() {
			return val;
		}

		public int compareTo(Object o) {
			return this.toString().compareTo(o.toString());
		}
	}


	/**
	 * Creates the dataset.
	 *
	 * @return the created dataset.
	 */
	private DefaultCategoryDataset createDataset() {
		dataset = new DefaultCategoryDataset();

		String title = (String) config.getObject(frame);
		if (title == null) title = config.getProperty(PlotFactory.SUBTITLE);
		if (title == null) title = "Avg. Value (" + VUnits.getFormattedName(frame.getVariable().getUnit()) + ")";
		rowKey = new RowKey(title);

		Axes<DataFrameAxis> axes = frame.getAxes();
		DataFrameAxis time = axes.getTimeAxis();
		int origin = time.getOrigin();
		DataFrameIndex index = frame.getIndex();
		if (axes.getZAxis() != null) index.setLayer(layer);
		for (int t = 0; t < time.getExtent(); t++) {
			index.setTime(t);
			GregorianCalendar date = axes.getDate(t + origin);		// 2014 changed Date to GregorianCalendar
			dataset.addValue(frame.getDouble(index), rowKey, dateFormat.format(date.getTimeInMillis()));
		}

		return dataset;
	}

	/**
	 * Gets the type of the Plot.
	 *
	 * @return the type of the Plot.
	 */
	public Formula.Type getType() {
		return Formula.Type.TIME_SERIES_BAR;
	}

	/**
	 * Configure this Plot according to the specified PlotConfiguration.
	 *
	 * @param config the new plot configuration
	 */
	@Override
	public void configure(PlotConfiguration config) {
		String configFile = config.getConfigFileName();
		if (configFile != null) {
			try {
				configure(new PlotConfigurationIO().loadConfiguration(new File(configFile)));
			} catch (IOException ex) {
				center.error("Error loading configuration", ex);
			}
		}

		UnitsConfigurator unitsConfig = new UnitsConfigurator() {
			public void configureUnits(String text, Font font, Color color) {
				LegendTitle legend = chart.getLegend();
				if (!text.equals(dataset.getRowKey(0).toString())) {
					rowKey.setVal(text);
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

		CategoryItemRenderer renderer = (CategoryItemRenderer) ((CategoryPlot) chart.getPlot()).getRenderer(0);
		Color color = config.getColor(TimeSeriesPlotConfiguration.SERIES_COLOR);
		if (color != null) renderer.setSeriesPaint(0, color);
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
		config.putObject(PlotConfiguration.UNITS, dataset.getRowKey(0).toString());
		config.putObject(PlotConfiguration.UNITS_FONT, chart.getLegend().getItemFont());
		config.putObject(PlotConfiguration.UNITS_COLOR, chart.getLegend().getItemPaint());
		config.putObject(PlotConfiguration.FOOTER1_SHOW_LINE, true);
		config.putObject(PlotConfiguration.FOOTER2_SHOW_LINE, true);
		config.putObject(PlotConfiguration.OBS_SHOW_LEGEND, false);

		CategoryItemRenderer renderer = (CategoryItemRenderer) ((CategoryPlot) chart.getPlot()).getRenderer(0);
		config.putObject(TimeSeriesPlotConfiguration.SERIES_COLOR, (Color) renderer.getSeriesPaint(0));
		return config;
	}


	@Override
	public String getTitle() {
		return chart.getTitle().getText();
	}

}
