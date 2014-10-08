package anl.verdi.plot.types;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleEdge;

//import simphony.util.messages.MessageCenter;
import ucar.ma2.InvalidRangeException;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataUtilities;
import anl.verdi.formula.Formula;
import anl.verdi.plot.config.JFreeChartConfigurator;
import anl.verdi.plot.config.LoadConfiguration;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.PlotConfigurationIO;
import anl.verdi.plot.config.SaveConfiguration;
import anl.verdi.plot.config.TimeSeriesPlotConfiguration;
import anl.verdi.plot.config.UnitsConfigurator;
import anl.verdi.plot.data.ScatterXYDataset;
import anl.verdi.plot.gui.PlotListener;
import anl.verdi.plot.gui.TimeLayerPanel;
import anl.verdi.plot.probe.PlotEventProducer;
import anl.verdi.plot.util.PlotExporterAction;
import anl.verdi.plot.util.PlotPrintAction;
import anl.verdi.util.Tools;
import anl.verdi.util.Utilities;
import anl.verdi.util.VUnits;

public class ScatterPlot extends AbstractPlot {

	static final Logger Logger = LogManager.getLogger(ScatterPlot.class.getName());
//	private static final MessageCenter msg = MessageCenter.getMessageCenter(ScatterPlot.class);
	private static final String ls = System.getProperty("line.separator");

	private DataFrame xFrame, yFrame;
	private ScatterXYDataset dataset;
	private JFreeChart chart;
	private int timeStep, layer;
	private PlotEventProducer eventProducer = new PlotEventProducer();
	private boolean hasNoLayer = false;
	private boolean singleTimeStep = false;
	private boolean singleLayer = false;
	private int[] allTimeSteps, allLayers;
	private JChartTitlesLabels titlesLabels;
	private PlotConfiguration config;
	private TextTitle subTitle;
	private File curFolder;
	
	public void viewClosed() {
		
		dataset = null;
		xFrame = null;
		yFrame = null;
		dataset = null;
		chart = null;
		eventProducer = null;
		allTimeSteps = null;
		allLayers = null;
		titlesLabels = null;
		config = null;
		subTitle = null;
		curFolder = null;
		
	}	

	public ScatterPlot(DataFrame xFrame, DataFrame yFrame) {
		this(xFrame, yFrame, new PlotConfiguration());
		Logger.debug("in constructor for ScatterPlot");
	}

	public ScatterPlot(DataFrame xFrame, DataFrame yFrame,
			PlotConfiguration config) {
		Logger.debug("in alternate constructor for ScatterPlot");
		hasNoLayer = xFrame.getAxes().getZAxis() == null
				|| yFrame.getAxes().getZAxis() == null;
		this.xFrame = xFrame;
		this.yFrame = yFrame;
		dataset = new ScatterXYDataset();
		dataset.addSeries(xFrame, yFrame, timeStep, layer);
		chart = createChart(dataset);
		createSubtitle();
		panel = new VerdiChartPanel(chart, true);
		titlesLabels = new JChartTitlesLabels(chart);

		if (config.getSubtitle1() == null || config.getSubtitle1().trim().isEmpty())
			config.setSubtitle1(Tools.getDatasetNames(xFrame));
		
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
		java.util.List<DataFrame> list = new ArrayList();
		list.add(xFrame);
		list.add(yFrame);
		return list;
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
		final TimeLayerPanel timeLayerPanel = new TimeLayerPanel();
		if (hasNoLayer) {
			timeLayerPanel.init(xFrame.getAxes(), timeStep
					+ xFrame.getAxes().getTimeAxis().getOrigin(), 0, false);
		} else {
			timeLayerPanel.init(xFrame.getAxes(), timeStep
					+ xFrame.getAxes().getTimeAxis().getOrigin(), layer
					+ xFrame.getAxes().getZAxis().getOrigin(), xFrame.getAxes()
					.getZAxis().getExtent() > 1);
		}

		singleTimeStep = timeLayerPanel.hasNoTimeSteps();
		singleLayer = timeLayerPanel.hasNoLayers();
		allTimeSteps = timeLayerPanel.getAllTimeSteps();
		allLayers = timeLayerPanel.getAllLayers();

		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int time = timeLayerPanel.getTime()
						- xFrame.getAxes().getTimeAxis().getOrigin();
				if (hasNoLayer)
					updateTimeStepLayer(time, 0);
				else {
					int layer = timeLayerPanel.getLayer()
							- xFrame.getAxes().getZAxis().getOrigin();
					updateTimeStepLayer(time, layer);
				}
			}
		};

		timeLayerPanel.addSpinnerListeners(changeListener, changeListener);
		bar.add(timeLayerPanel);
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
		menu.add(new AbstractAction("Export Data") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e) {
				exportData();
			}
		});
		bar.add(menu);

		menu = new JMenu("Configure");
		menu.add(new AbstractAction("Configure Plot") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -8857410690090558236L;

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
		// only allow zooming
		// probing doesn't make much sense in this context
		JMenuItem item = menu.add(new JCheckBoxMenuItem(new AbstractAction("Zoom") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -2596112858319312136L;

			public void actionPerformed(ActionEvent e) {
			}
		}));
		item.setSelected(true);
		grp.add(item);
		return bar;
	}

	/**
	 * Adds the specified PlotListener.
	 * 
	 * @param listener
	 *            the plot listener to add
	 */
	public void addPlotListener(PlotListener listener) {
		eventProducer.addListener(listener);
	}

	/**
	 * Removes the specified PlotListener.
	 * 
	 * @param listener
	 *            the plot listener to remove
	 */
	public void removePlotListener(PlotListener listener) {
		eventProducer.removeListener(listener);
	}

	public void updateTimeStepLayer(int timeStep, int layer) {
		this.timeStep = timeStep;
		this.layer = layer;
		dataset.addSeries(xFrame, yFrame, timeStep, layer);

		NumberAxis domainAxis = (NumberAxis) ((XYPlot) chart.getPlot())
				.getDomainAxis();

		try {
			DataUtilities.MinMax minMax = null;
			if (hasNoLayer) {
				minMax = DataUtilities.minMax(xFrame, timeStep);
			} else {
				minMax = DataUtilities.minMax(xFrame, timeStep, layer);
			}
			double interval = (minMax.getMax() - minMax.getMin()) / 10;
			domainAxis.setRange(minMax.getMin(), minMax.getMax() + interval);
			domainAxis.setTickUnit(new NumberTickUnit(interval,
					new DecimalFormat("0.00E00")));
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}

		NumberAxis rangeAxis = (NumberAxis) ((XYPlot) chart.getPlot())
				.getRangeAxis();
		try {
			DataUtilities.MinMax minMax = null;
			if (hasNoLayer) {
				minMax = DataUtilities.minMax(yFrame, timeStep);
			} else {
				minMax = DataUtilities.minMax(yFrame, timeStep, layer);
			}
			double interval = (minMax.getMax() - minMax.getMin()) / 10;
			rangeAxis.setRange(minMax.getMin(), minMax.getMax() + interval);
			rangeAxis.setTickUnit(new NumberTickUnit(interval,
					new DecimalFormat("0.00E00")));
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}

		// Title title1 = chart.getSubtitle(1);
		// chart.removeSubtitle(title1);
		createSubtitle();
	}

	private JFreeChart createChart(XYDataset dataset) {
		String xAxisLabel = xFrame.getVariable().getName() + " "
				+ VUnits.getFormattedName(xFrame.getVariable().getUnit());
		String yAxisLabel = yFrame.getVariable().getName() + " "
				+ VUnits.getFormattedName(yFrame.getVariable().getUnit());
		JFreeChart chart = ChartFactory.createScatterPlot("Scatter Plot",
				xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL,
				true, true, false);

		XYPlot plot = (XYPlot) chart.getPlot();
		plot.setNoDataMessage("NO DATA");
		plot.setRangeZeroBaselineVisible(true);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot
				.getRenderer();
		renderer.setSeriesOutlinePaint(0, Color.black);
		renderer.setUseOutlinePaint(true);
//		renderer.setShapesVisible(true);	// 2014 deprecated; changed to setSeriesShapesVisible for series=0
											// NOTE: series number from above function call to setSeriesOutlinePaint
		renderer.setSeriesShapesVisible(0, true);
		renderer.setDrawOutlines(true);
		renderer.setUseFillPaint(true);
//		renderer.setFillPaint(Color.RED);	// 2014 deprecated; changed to setSeriesFillPaint for series=0
		renderer.setSeriesFillPaint(0, Color.RED);
		renderer.setSeriesOutlineStroke(0, new BasicStroke(1.0f));
		renderer.setSeriesShape(0, new Ellipse2D.Double(-4.0, -4.0, 8.0, 8.0));

		NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);

		try {
			DataUtilities.MinMax minMax = null;
			if (hasNoLayer) {
				minMax = DataUtilities.minMax(xFrame, timeStep);
			} else {
				minMax = DataUtilities.minMax(xFrame, timeStep, layer);
			}
			double interval = (minMax.getMax() - minMax.getMin()) / 10;
			domainAxis.setRange(minMax.getMin(), minMax.getMax() + interval);
			domainAxis.setTickUnit(new NumberTickUnit(interval,
					new DecimalFormat("0.00E00")));
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}

		NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
		rangeAxis.setAutoRangeIncludesZero(false);

		try {
			DataUtilities.MinMax minMax = null;
			if (hasNoLayer) {
				minMax = DataUtilities.minMax(yFrame, timeStep);
			} else {
				minMax = DataUtilities.minMax(yFrame, timeStep, layer);
			}
			double interval = (minMax.getMax() - minMax.getMin()) / 10;
			rangeAxis.setRange(minMax.getMin(), minMax.getMax() + interval);
			rangeAxis.setTickUnit(new NumberTickUnit(interval,
					new DecimalFormat("0.00E00")));
		} catch (InvalidRangeException e) {
			e.printStackTrace();
		}

		chart.setBackgroundPaint(Color.WHITE);
		return chart;
	}

	private void createSubtitle() { 		// changed Date to GregorianCalendar
		GregorianCalendar aCalendar = xFrame.getAxes().getDate(timeStep + (long)xFrame.getAxes().getTimeAxis().getOrigin());
		Logger.debug("in ScatterPlot createSubtitle GregorianCalendar aCalendar = " + aCalendar.toString());
		TextTitle title = null;
		if (hasNoLayer) {
			title = new TextTitle(Utilities.formatDate(aCalendar));
		} else {
			int layer = this.layer + xFrame.getAxes().getZAxis().getOrigin();
			title = new TextTitle(Utilities.formatDate(aCalendar) + ", Layer: "
					+ (layer + 1));
		}

		title.setPosition(RectangleEdge.BOTTOM);
		if (subTitle == null) {
			subTitle = title;
			chart.addSubtitle(title);
		} else {
			subTitle.setText(title.getText());
		}
	}

	public JPanel getPanel() {
		return panel;
	}

	/**
	 * Gets the type of the Plot.
	 * 
	 * @return the type of the Plot.
	 */
	public Formula.Type getType() {
		return Formula.Type.SCATTER_PLOT;
	}

	/**
	 * Configure this Plot according to the specified PlotConfiguration.
	 * 
	 * @param config
	 *            the new plot configuration
	 */
	@Override
	public void configure(PlotConfiguration config) {
		String configFile = config.getConfigFileName();
		if (configFile != null) {
			try {
				configure(new PlotConfigurationIO().loadConfiguration(new File(
						configFile)));
			} catch (IOException ex) {
				Logger.error("Error loading configuration " + ex.getMessage());
			}
		}

		UnitsConfigurator unitsConfig = new UnitsConfigurator() {
			public void configureUnits(String text, Font font, Color color) {
				LegendTitle legend = chart.getLegend();
				if (!text.equals(dataset.getSeriesKey(0))) {
					dataset.setSeriesKey(0, text);
				}
				if (font != null)
					legend.setItemFont(font);
				if (color != null)
					legend.setItemPaint(color);
			}

			public void configureUnitsTick(Boolean show, Font font, Color color) {
			}
		};
		// do the titles and the labels
		JFreeChartConfigurator configurator = new JFreeChartConfigurator(chart,
				titlesLabels.getTitleConfigurator(), unitsConfig);
		configurator.configure(config);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) ((XYPlot) chart
				.getPlot()).getRenderer();
		Color color = config.getColor(TimeSeriesPlotConfiguration.SERIES_COLOR);
		if (color != null)
	//		renderer.setFillPaint(color);	// 2014 deprecated; changed to setSeriesFillPaint for series=0
			renderer.setSeriesFillPaint(0, color);

		this.config = config;
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

		config.putObject(PlotConfiguration.UNITS, dataset.getSeriesKey(0));
		config.putObject(PlotConfiguration.UNITS_FONT, chart.getLegend()
				.getItemFont());
		config.putObject(PlotConfiguration.UNITS_COLOR, chart.getLegend()
				.getItemPaint());
		config.putObject(PlotConfiguration.FOOTER1_SHOW_LINE, true);
		config.putObject(PlotConfiguration.FOOTER2_SHOW_LINE, true);
		config.putObject(PlotConfiguration.OBS_SHOW_LEGEND, false);

		XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) ((XYPlot) chart
				.getPlot()).getRenderer();
		config.putObject(TimeSeriesPlotConfiguration.SERIES_COLOR,
				(Color) renderer.getSeriesFillPaint(0));
		return config;
	}

	/**
	 * Export this plot's XY values to a file
	 */
	public void exportData() {
		if (singleTimeStep && singleLayer) {
			showSaveDialog(false, false);
			return;
		}

		TimeLayerDialog dialog = new TimeLayerDialog(
				"Export Scatter Plot Data into a CSV File", panel, timeStep, layer);
		int option = dialog.showDialog();

		if (option == TimeLayerDialog.CANCEL_OPTION) {
			return;
		}
		
		boolean allSteps = false;
		boolean allLayers = false;
		
		if (option == TimeLayerDialog.ALL_STEPS_ALL_LAYERS) {
			allSteps = true;
			allLayers = true;
		}
		
		if (option == TimeLayerDialog.ALL_STEPS_CURRENT_LAYER) {
			allSteps = true;
			allLayers = false;
		}
		
		if (option == TimeLayerDialog.CURRENT_STEP_ALL_LAYERS) {
			allSteps = false;
			allLayers = true;
		}
			
		showSaveDialog(allSteps, allLayers);
	}

	public void showSaveDialog(boolean allSteps, boolean allLayers) {
		JFileChooser chooser = new JFileChooser();
		
		if (curFolder != null)
			chooser.setCurrentDirectory(curFolder);
		
		int res = chooser.showSaveDialog(panel);
		if (res == JFileChooser.APPROVE_OPTION) {
			File file = chooser.getSelectedFile();
			chooser.setVisible(false);
			saveData(file, allSteps, allLayers);
			curFolder = file.getParentFile();
		}
	}

	/**
	 * Save scatter plot xy values into a csv file
	 * 
	 * @param file -- ascii data will be written to this file
	 * @param allSteps -- if true export data for all time steps, for current step otherwise
	 * @param allLayers -- if true export data for all layers, for current layer otherwise
	 * 
	 */
	private void saveData(File file, boolean forAllSteps, boolean forAllLayers) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			OutputStreamWriter osw = new OutputStreamWriter(fos);
			BufferedWriter writer = new BufferedWriter(osw);

			writer.write("layer,time," + xFrame.getVariable().getName() + ","
					+ yFrame.getVariable().getName());
			writer.write(ls);

			if (!forAllSteps && !forAllLayers)
				writeSingleTimeLayer(writer, dataset, timeStep, layer);

			else if (forAllSteps && forAllLayers)
				writeAllTimeLayers(writer, allTimeSteps, allLayers);
			
			else if (!forAllSteps && forAllLayers)
				writeAllTimeLayers(writer, new int[]{timeStep}, allLayers);
			
			else if (forAllSteps && !forAllLayers)
				writeAllTimeLayers(writer, allTimeSteps, new int[]{layer});

			writer.close();
		} catch (Exception e) {
			Logger.error("Error opening/writing output file " + e.getMessage());
		}
	}

	private void writeAllTimeLayers(BufferedWriter writer, int[] timeSteps,
			int[] layers) throws IOException {
		int timeLen = timeSteps.length;
		int layerLen = layers.length;
		
		if (timeLen == 0) {
			timeLen = 1;
			timeSteps = new int[] {0};
		}
		
		if (layerLen == 0) {
			layerLen = 1;
			layers = new int[] {0};
		}

		for (int i = 0; i < layerLen; i++) {
			for (int j = 0; j < timeLen; j++) {
				ScatterXYDataset xydataset = new ScatterXYDataset();
				xydataset.addSeries(xFrame, yFrame, timeSteps[j], layers[i]);
				writeSingleTimeLayer(writer, xydataset, timeSteps[j], layers[i]);
			}
		}
	}

	private void writeSingleTimeLayer(BufferedWriter writer,
			ScatterXYDataset xyDataset, int ts, int ly) throws IOException {
		int seriesCount = xyDataset.getSeriesCount();
		int curSerInd = seriesCount - 1;
		int size = xyDataset.getItemCount(curSerInd);

		for (int i = 0; i < size; i++) {
			writer.write((ly + 1) + "," + (ts + 1) + ","
					+ xyDataset.getXValue(curSerInd, i) + ","
					+ xyDataset.getYValue(curSerInd, i));
			writer.write(ls);
		}
	}

	private class TimeLayerDialog extends JDialog {
		private static final long serialVersionUID = 6510096129096912422L;
		public static final int ALL_STEPS_ALL_LAYERS = 0;
		public static final int ALL_STEPS_CURRENT_LAYER = 1;
		public static final int CURRENT_STEP_ALL_LAYERS = 2;
		public static final int CURRENT_STEP_CURRENT_LAYER = 3;
		public static final int CANCEL_OPTION = -1;
		private boolean cancelled = false;
		private JRadioButton allLayers;
		private JRadioButton currentLayer;
		private JRadioButton allSteps;
		private JRadioButton currentStep;
		private int currentStepNumber = 0;
		private int currentLayerNumber = 0;

		public TimeLayerDialog(String title, Component parent, int currentTS, int currentL) {
			super.setTitle(title);
			super.setLocation(getCenterPoint(parent));
			super.setModal(true);
			super.setPreferredSize(new Dimension(300, 200));
			this.currentLayerNumber = currentL;
			this.currentStepNumber = currentTS;
			this.getContentPane().add(createLayout());
		}

		public int showDialog() {
			this.pack();
			this.setVisible(true);

			if (this.cancelled)
				return CANCEL_OPTION;

			if (this.allLayers.isSelected() && this.allSteps.isSelected())
				return ALL_STEPS_ALL_LAYERS;

			if (this.allLayers.isSelected() && this.currentStep.isSelected())
				return CURRENT_STEP_ALL_LAYERS;

			if (this.currentLayer.isSelected() && this.allSteps.isSelected())
				return ALL_STEPS_CURRENT_LAYER;

			return CURRENT_STEP_CURRENT_LAYER;
		}

		private JPanel createLayout() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			panel.add(createLayerPanel());
			panel.add(createTimePanel());
			panel.add(createButtonsPanel());

			return panel;
		}

		private JPanel createLayerPanel() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			
			JPanel labelPanel = new JPanel(new BorderLayout());
			JLabel label = new JLabel("Layers:");
			labelPanel.add(label, BorderLayout.WEST);
			
			JPanel buttonPanel = new JPanel(new BorderLayout(20, 10));
			currentLayer = new JRadioButton("Current layer (" + (currentLayerNumber + 1) + ")");
			currentLayer.setSelected(true);
			allLayers = new JRadioButton("All layers");
			ButtonGroup group = new ButtonGroup();
			group.add(currentLayer);
			group.add(allLayers);
			buttonPanel.add(currentLayer, BorderLayout.LINE_START);
			buttonPanel.add(allLayers);


			panel.add(labelPanel);
			panel.add(buttonPanel);
			panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
			
			return panel;
		}

		private JPanel createTimePanel() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			
			JPanel labelPanel = new JPanel(new BorderLayout());
			JLabel label = new JLabel("Time steps:");
			labelPanel.add(label, BorderLayout.WEST);
			
			JPanel buttonPanel = new JPanel(new BorderLayout(20, 10));
			currentStep = new JRadioButton("Current time step (" + (currentStepNumber + 1) + ")");
			currentStep.setSelected(true);
			allSteps = new JRadioButton("All time steps");
			ButtonGroup group = new ButtonGroup();
			group.add(currentStep);
			group.add(allSteps);

			buttonPanel.add(currentStep, BorderLayout.LINE_START);
			buttonPanel.add(allSteps);


			panel.add(labelPanel);
			panel.add(buttonPanel);
			
			return panel;
		}

		private JPanel createButtonsPanel() {
			JPanel container = new JPanel();
			FlowLayout layout = new FlowLayout();
			layout.setHgap(20);
			layout.setVgap(2);
			container.setLayout(layout);

			JButton okButton = new JButton("OK");
			okButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelled = false;
					dispose();
				}
			});

			container.add(okButton);
			getRootPane().setDefaultButton(okButton);

			JButton cancelButton = new JButton("Cancel");
			cancelButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					cancelled = true;
					dispose();
				}
			});
			container.add(cancelButton);
			container.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

			return container;
		}

		private Point getCenterPoint(Component comp) {
			Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

			if (comp == null) {
				return new Point((int) screenSize.getWidth() / 2,
						(int) screenSize.getHeight() / 2);
			}

			Dimension frameSize = comp.getSize();

			if (frameSize.height > screenSize.height) {
				frameSize.height = screenSize.height;
			}

			if (frameSize.width > screenSize.width) {
				frameSize.width = screenSize.width;
			}

			return new Point((screenSize.width - frameSize.width) / 2,
					(screenSize.height - frameSize.height) / 2);
		}
	}

	@Override
	public String getTitle() {
		return chart.getTitle().getText();
	}

}
