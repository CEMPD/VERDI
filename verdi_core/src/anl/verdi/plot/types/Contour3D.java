package anl.verdi.plot.types;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.swing.JMapFrame;
import org.jfree.chart.axis.ValueAxis;

import saf.core.ui.util.FileChooserUtilities;
//import simphony.util.messages.MessageCenter;
import ucar.ma2.InvalidRangeException;
import visad.AxisScale;
import visad.ColorControl;
import visad.ContourControl;
import visad.DataReferenceImpl;
import visad.Display;
import visad.FieldImpl;
import visad.FlatField;
import visad.FunctionType;
import visad.GraphicsModeControl;
import visad.Integer1DSet;
import visad.Integer2DSet;
import visad.RealTupleType;
import visad.RealType;
import visad.ScalarMap;
import visad.VisADException;
import visad.java3d.DefaultDisplayRendererJ3D;
import visad.java3d.DisplayImplJ3D;
import visad.java3d.DisplayRendererJ3D;
import visad.java3d.ProjectionControlJ3D;
import visad.java3d.VisADCanvasJ3D;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.DataUtilities;
import anl.verdi.formula.Formula;
import anl.verdi.plot.anim.AnimationPanelContour3D;	// 2014 copy of AnimationPanel specifically for Contour3D
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.PavePaletteCreator;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.PlotConfigurationIO;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.config.Title;
import anl.verdi.plot.gui.ConfigDialog;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotListener;
import anl.verdi.plot.gui.TimeLayerPanel;
import anl.verdi.plot.probe.PlotEventProducer;
import anl.verdi.plot.util.LegendPanel;
import anl.verdi.plot.util.OffsetNumberFormat;
import anl.verdi.plot.util.PlotExporterAction;
import anl.verdi.plot.util.VerdiCanvas3D;
import anl.verdi.util.Tools;
import anl.verdi.util.Utilities;
import anl.verdi.util.VUnits;

public class Contour3D implements Plot, TimeAnimatablePlot, Printable {

	static final Logger Logger = LogManager.getLogger(Contour3D.class.getName());
//	private static final MessageCenter msg = MessageCenter.getMessageCenter(Contour3D.class);

	private PlotEventProducer eventProducer = new PlotEventProducer();
	private DataFrame frame;
	private int timeStep, layer;
	private DisplayImplJ3D display;
	private FunctionType timeToGrid;
	private RealTupleType timeType;
	private RealTupleType domain;
	private FunctionType gridType;
	private DataReferenceImpl dataRef;
	private boolean processTimeChange = true;

	private boolean hasNoLayer = false;
	private DataUtilities.MinMax minMax;
	private JPanel panel;
	private VerdiCanvas3D canvas;
	private ColorMap map;
	private ScalarMap zMap;
	private ScalarMap valueMap;
	private LegendPanel legend;
	private PlotConfiguration config = new PlotConfiguration();
	private ScalarMap yMap;
	private ScalarMap xMap;
	private ScalarMap isoMap;
	private boolean latLonOn = false;
	private TimeLayerPanel timeLayerPanel;

	/**
	 * Creates a contour style 3D image graph
	 *
	 * @param frame
	 * @throws RemoteException
	 * @throws VisADException
	 */
	public Contour3D(DataFrame frame)
					throws RemoteException, VisADException {
		this(frame, new TilePlotConfiguration());
	}

	/**
	 * Creates a contour style 3D image graph
	 *
	 * @param frame
	 * @throws RemoteException
	 * @throws VisADException
	 */
	public Contour3D(DataFrame frame, PlotConfiguration config)
					throws RemoteException, VisADException {

		hasNoLayer = frame.getAxes().getZAxis() == null;
		minMax = DataUtilities.minMax(frame);

		// Create the quantities
		// Use RealType(String name, Unit unit, Set set);
		this.frame = frame;
		this.timeStep = 0;
		this.layer = 0;

		RealType x = RealType.getRealType("x");
		RealType y = RealType.getRealType("y");
		domain = new RealTupleType(y, x);

		RealType value = RealType.getRealType(VUnits.getFormattedName(frame.getVariable().getUnit()));
		RealTupleType range = new RealTupleType(value);

		// function type for grid
		gridType = new FunctionType(domain, range);
		RealType time = RealType.getRealType("time");
		timeType = new RealTupleType(time);
		timeToGrid = new FunctionType(timeType, gridType);
		FieldImpl timeField = createData();


		GraphicsConfiguration gConfig = VisADCanvasJ3D.getDefaultConfig();
		DisplayRendererJ3D renderer = new DefaultDisplayRendererJ3D() {

			private Vector<String> cursorVec = new Vector<String>();

			@Override
			public Vector getCursorStringVector() {
				synchronized (cursorVec) {
					//super.getCursorStringVector() = [frameY = 6.8298655, frameX = 3.399344, PPM = 0.054499093]
					cursorVec.clear();
					Vector v = super.getCursorStringVector();
					Logger.debug("super.getCursorStringVector() = " + super.getCursorStringVector());
					DataFrame frame = Contour3D.this.frame;
					Axes<DataFrameAxis> axes = frame.getAxes();

					int frameY = (int) Math.floor(getValue((String) v.get(0)));
					if (frameY < 0) frameY = 0;
					else if (frameY > axes.getYAxis().getExtent() - 1) frameY = (int) axes.getYAxis().getExtent() - 1;


					int frameX = (int) Math.floor(getValue((String) v.get(1)));
					if (frameX < 0) frameX = 0;
					else if (frameX > axes.getXAxis().getExtent() - 1) frameX = (int) axes.getXAxis().getExtent() - 1;

					int y = frameY + axes.getYAxis().getOrigin();
					int x = frameX + axes.getXAxis().getOrigin();
					DataFrameIndex index = frame.getIndex();
					if (hasNoLayer) {
						index.setTime(timeStep);
						index.setXY(frameX, frameY);
					} else {
						index.set(timeStep, layer, frameX, frameY);
					}
					double val = frame.getDouble(index);
					if (latLonOn) {
						Point2D pt = frame.getAxes().getBoundingBoxer().axisPointToLatLonPoint(x, y);
						cursorVec.add("latitude = " + Utilities.formatLat(pt.getY(), 4));
						cursorVec.add("longitude = " + Utilities.formatLon(pt.getX(), 4));
					} else {
						cursorVec.add("y = " + y);
						cursorVec.add("x = " + x);
					}
					// 2014 get scalar name of the scalar map ???
					cursorVec.add(zMap.getAxisScale().getTitle() + " = " + val);	// 2014 .getLabel() deprecated, replacing it with .getTitle()
					return cursorVec;
				}
			}

			private double getValue(String item) {
				String val = item.substring(item.indexOf("=") + 1, item.length()).trim();
				return Double.valueOf(val);
			}
		};
		canvas = new VerdiCanvas3D(renderer, gConfig);

		canvas.setTitleText(createTitle());
		canvas.setTimeText(Utilities.formatDate(frame.getAxes().
						getDate(timeStep + frame.getAxes().getTimeAxis().getOrigin())));
		display = new DisplayImplJ3D("display1", renderer, DisplayImplJ3D.JPANEL,
						gConfig, canvas);
		((ProjectionControlJ3D) display.getProjectionControl()).setOrthoView(ProjectionControlJ3D.Y_MINUS);

		// Get display's graphics mode control and draw scales
		GraphicsModeControl dispGMC = (GraphicsModeControl) display.getGraphicsModeControl();
		dispGMC.setScaleEnable(true);
		dispGMC.setTextureEnable(false);
		//dispGMC.setPolygonMode(DisplayImplJ3D.POLYGON_LINE);

		Font font = Font.decode(null).deriveFont(11f);

		yMap = new ScalarMap(y, Display.YAxis);
		AxisScale axisScale = yMap.getAxisScale();
		NumberFormat format = new OffsetNumberFormat(axisScale.getNumberFormat(),
						frame.getAxes().getYAxis().getOrigin() + 1);
		axisScale.setNumberFormat(format);
		//axisScale.setGridLinesVisible(true);
		axisScale.setFont(font);
		axisScale.setLabelAllTicks(true);
		axisScale.setMinorTickSpacing(2);

		xMap = new ScalarMap(x, Display.XAxis);
		axisScale = xMap.getAxisScale();
		format = new OffsetNumberFormat(axisScale.getNumberFormat(),
						frame.getAxes().getXAxis().getOrigin() + 1);
		axisScale.setNumberFormat(format);
		//axisScale.setGridLinesVisible(true);
		axisScale.setFont(font);
		axisScale.setLabelAllTicks(true);
		axisScale.setMinorTickSpacing(2);

		zMap = new ScalarMap(value, Display.ZAxis);
		zMap.setRange(minMax.getMin(), minMax.getMax());
		axisScale = zMap.getAxisScale();
		axisScale.setFont(font);
		axisScale.setLabelAllTicks(true);
		axisScale.setMinorTickSpacing(2);

		valueMap = new ScalarMap(value, Display.RGB);
		valueMap.setRange(minMax.getMin(), minMax.getMax());
		isoMap = new ScalarMap(value, Display.IsoContour);

		// Add maps to display
		display.addMap(yMap);
		display.addMap(xMap);
		display.addMap(valueMap);
		display.addMap(zMap);

		initColors(this.map);

		dataRef = new DataReferenceImpl("data_ref");
		dataRef.setData(timeField);
		display.addReference(dataRef);
		display.getDisplayRenderer().setBoxOn(false);
		
		if (config.getSubtitle1() == null || config.getSubtitle1().trim().isEmpty())
			config.setSubtitle1(Tools.getDatasetNames(frame));
		
		PlotConfiguration defaultConfig = getPlotConfiguration();
		defaultConfig.merge(config);
		configure(defaultConfig);
	}

	private String createTitle() {
		if (hasNoLayer) {
			return frame.getVariable().getName();
		} else {
			int displayedLayer = frame.getAxes().getZAxis().getOrigin() + layer + 1;
			return frame.getVariable().getName() + " - " + "Layer " + displayedLayer;
		}
	}

	private void initColors(ColorMap map) throws RemoteException, VisADException {
		if (map == null) {
//			this.map = new ColorMap(new PavePaletteCreator().createPalettes(8).get(0), minMax.getMin(),
//							minMax.getMax());
			this.map = new ColorMap(new PavePaletteCreator().createPavePalette(), minMax.getMin(),
					minMax.getMax());
			this.map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);

			legend = new LegendPanel(this.map, minMax.getMin(), minMax.getMax(), zMap.getAxisScale().getTitle());	// 2014 get scalar name of the scalar map ???
			legend.getLegend().setBackgroundPaint(Color.BLACK);
			legend.getLegend().getAxis().setTickMarkPaint(Color.WHITE);
			legend.getLegend().getAxis().setLabelPaint(Color.WHITE);
			legend.getLegend().getAxis().setTickLabelPaint(Color.WHITE);

		} else {
			this.map = map;
			legend.initLegend(this.map, minMax.getMin(), minMax.getMax(), zMap.getAxisScale().getTitle());	// 2014 get scalar name of the scalar map ???
		}
		int colorCount = this.map.getColorCount();
		float[][] cMap = new float[3][colorCount];
		float[] colorComps = new float[4];
		for (int i = 0; i < colorCount; i++) {
			colorComps = this.map.getColor(i).getRGBComponents(colorComps);
			cMap[0][i] = colorComps[0];
			cMap[1][i] = colorComps[1];
			cMap[2][i] = colorComps[2];
		}

		ColorControl colorControl = (ColorControl) valueMap.getControl();
		colorControl.setTable(cMap);
	}

	private void enableIsoContours(boolean enable) {
		try {
			if (enable) {
				display.addMap(isoMap);
				DataUtilities.MinMax localMinMax = null;
				if (hasNoLayer) localMinMax = DataUtilities.minMax(frame, timeStep);
				else localMinMax = DataUtilities.minMax(frame, timeStep, layer);
				ContourControl control = (ContourControl) isoMap.getControl();
				float interval = (float) (localMinMax.getMax() - localMinMax.getMin()) / 20;
				control.setContourInterval(interval, (float) localMinMax.getMin(), (float) localMinMax.getMax(),
								(float) localMinMax.getMin() - 1);
				control.enableLabels(true);
			} else {
				display.removeMap(isoMap);
			}
		} catch (RemoteException e) {
			Logger.error("Error enabling iso contours " + e.getMessage());
		} catch (InvalidRangeException e) {
			Logger.error("Error enabling iso contours " + e.getMessage());
		} catch (VisADException e) {
			Logger.error("Error enabling iso contours " + e.getMessage());
		}
	}

	public BufferedImage getBufferedImage() {
		return getBufferedImage(panel.getWidth(), panel.getHeight());
	}

	/**
	 * This will not respect the width and height arguments due to
	 * visad limitations.
	 *
	 * @param width  ignored
	 * @param height ignored
	 * @return the created image
	 */
	public synchronized BufferedImage getBufferedImage(int width, int height) {
		final BlockingQueue<BufferedImage> queue = new ArrayBlockingQueue<BufferedImage>(1);
		Thread t = new Thread(new Runnable() {
			public void run() {
				try {
					queue.put(display.getImage());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});

		t.start();
		BufferedImage image = null;
		while (true) {
			try {
				image = queue.take();
				break;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		BufferedImage legendImg = new BufferedImage(legend.getWidth(), legend.getHeight(), image.getType());
		Graphics g = legendImg.getGraphics();
		legend.paint(g);
		g.dispose();
		BufferedImage img = new BufferedImage(image.getWidth() + legend.getWidth(),
						image.getHeight(), image.getType());
		Graphics graphics = img.getGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, img.getWidth(), img.getHeight());
		graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		graphics.drawImage(legendImg, image.getWidth(), 0, legendImg.getWidth(), legendImg.getHeight(), null);
		graphics.dispose();

		return img;
	}

	/**
	 * Creates a print job for the chart.
	 */
	public void createPrintJob() {
		PrinterJob job = PrinterJob.getPrinterJob();
		PageFormat pf = job.defaultPage();
		PageFormat pf2 = job.pageDialog(pf);
		if (pf2 != pf) {
			job.setPrintable(this, pf2);
			if (job.printDialog()) {
				try {
					job.print();
				}
				catch (PrinterException e) {
					Logger.error("Error while printing " + e.getMessage());
				}
			}
		}

	}

	/**
	 * Prints the chart on a single page.
	 *
	 * @param g         the graphics context.
	 * @param pf        the page format to use.
	 * @param pageIndex the index of the page. If not <code>0</code>, nothing
	 *                  gets print.
	 * @return The result of printing.
	 */
	public int print(Graphics g, PageFormat pf, int pageIndex) {

		if (pageIndex != 0) {
			return NO_SUCH_PAGE;
		}
		Graphics2D g2 = (Graphics2D) g;
		double x = pf.getImageableX();
		double y = pf.getImageableY();
		double w = pf.getImageableWidth();
		double h = pf.getImageableHeight();
		BufferedImage img = getBufferedImage();
		
		// scale image to fit paper while maintaining original aspect ratio
		double origRatio = (double) img.getWidth() / (double) img.getHeight();
		double paperRatio = w / h;
		
		if (origRatio > paperRatio) {
			// constrain width
			h = w / origRatio;
		} else {
			// constrain height
			w = h * origRatio;
		}

		g2.drawImage(img, (int) x, (int) y, (int) w, (int) h, null);
		return PAGE_EXISTS;
	}


	public void updateTimeStep(int timestep) {
		processTimeChange = false;
		updateTimeStepLayer(timestep, layer);
		timeLayerPanel.setTime(timestep + frame.getAxes().getTimeAxis().getOrigin());
		processTimeChange = true;

	}

	public void updateTimeStepLayer(int timestep, int layer) {
		this.timeStep = timestep;
		this.layer = layer;
		try {
			//display.removeReference(dataRef);
			FieldImpl data = createData();
			dataRef.setData(data);
			//display.addReference(dataRef);
			canvas.setTimeText(Utilities.formatDate(frame.getAxes().
							getDate(timeStep + frame.getAxes().getTimeAxis().getOrigin())));

			if (config.getTitle() == null || config.getTitle().toUpperCase().contains("LAYER")) {
				canvas.setTitleText(createTitle());
			}

		} catch (VisADException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	private FieldImpl createData() throws VisADException, RemoteException {
		Axes<DataFrameAxis> axes = frame.getAxes();
		int xLen = axes.getXAxis().getExtent();
		int yLen = axes.getYAxis().getExtent();

		Integer2DSet gridSet = new Integer2DSet(domain, yLen, xLen);
		Integer1DSet timeSet = new Integer1DSet(timeType, axes.getTimeAxis().getExtent());

		FieldImpl timeField = new FieldImpl(timeToGrid, timeSet);
		DataFrameIndex index = frame.getIndex();
		FlatField grid = new FlatField(gridType, gridSet);
		double[][] data = new double[1][xLen * yLen];
		if (hasNoLayer) {
			index.setTime(timeStep);
			index.setXY(0, 0);
		} else index.set(timeStep, layer, 0, 0);
		for (int c = 0; c < xLen; c++) {
			for (int r = 0; r < yLen; r++) {
				index.setXY(c, r);
				data[0][c * yLen + r] = frame.getDouble(index);
			}
		}
		grid.setSamples(data);
		timeField.setSample(timeStep, grid, false);

		return timeField;
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
	 * Adds the specified PlotListener.
	 *
	 * @param listener the plot listener to add
	 */
	public void addPlotListener(PlotListener listener) {
		eventProducer.addListener(listener);
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
	 * Gets a menu bar for this Plot.
	 *
	 * @return a menu bar for this Plot.
	 */
	public JMenuBar getMenuBar() {
		JMenuBar bar = new JMenuBar();
		JMenu menu = new JMenu("File");
		menu.setMnemonic('F');
		menu.add(new AbstractAction("Print") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -616290068091431446L;

			public void actionPerformed(ActionEvent e) {
				createPrintJob();
			}
		});
		menu.add(new PlotExporterAction(this));
		menu.getPopupMenu().setLightWeightPopupEnabled(false);

		bar.add(menu);


		menu = new JMenu("Configure");
		menu.getPopupMenu().setLightWeightPopupEnabled(false);
		menu.add(new AbstractAction("Configure Plot") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3629523824425438179L;

			public void actionPerformed(ActionEvent e) {
				editChartProps();
			}
		});
		// todo make this into a separate action that can use
		// any plots' getPlotConfiguration
		menu.add(new AbstractAction("Load Configuration") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 5003322117318126264L;

			public void actionPerformed(ActionEvent e) {
				PlotConfiguration defaultConfig = getPlotConfiguration();
				File file = FileChooserUtilities.getSaveFile(Tools.getConfigFolder(defaultConfig));
				
				if (file != null) {
					try {
						PlotConfiguration newConfig = new PlotConfigurationIO().loadConfiguration(file);
						defaultConfig.merge(newConfig);
						configure(defaultConfig);
					} catch (IOException ex) {
						Logger.error("Error loading configuration " + ex.getMessage());
					}
				}
			}
		});
		menu.add(new AbstractAction("Save Configuration") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 8055577605446435883L;

			public void actionPerformed(ActionEvent e) {
				boolean saveTitle = saveTile(panel);
				PlotConfiguration config = getPlotConfiguration();
				File file = FileChooserUtilities.getSaveFile(Tools.getConfigFolder(config));
				
				if (file != null) {
					try {
						config.save(file, saveTitle);
					} catch (IOException ex) {
						Logger.error("Error saving configuration " + ex.getMessage());
					}
				}
			}
		});

		bar.add(menu);

		menu = new JMenu("Controls");
		menu.getPopupMenu().setLightWeightPopupEnabled(false);

		menu.add(new JCheckBoxMenuItem(new AbstractAction("Show Lat/Lon") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4664278040132583133L;

			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
				latLonOn = item.isSelected();
			}
		}));

		menu.add(new JCheckBoxMenuItem(new AbstractAction("Show IsoContours") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 7941827806718584070L;

			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
				enableIsoContours(item.isSelected());
			}
		}));
		bar.add(menu);

		menu = new JMenu("Plot");
		menu.getPopupMenu().setLightWeightPopupEnabled(false);
		bar.add(menu);
		menu.add(new JMenuItem(new AbstractAction("Animate Plot") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 7533234754863706319L;

			public void actionPerformed(ActionEvent e) {
				Logger.debug("in Countour3D.actionPerformed for Animate Plot; calling AnimationPanelContour3D");
				AnimationPanelContour3D panel = new AnimationPanelContour3D();
				panel.init(frame.getAxes(), Contour3D.this);
			}
		}));

		return bar;
	}

	private boolean saveTile(Component parent) {
		String title = "Save Title?";
		String msg = "Would you like to save the title also?";
		int option = JOptionPane.showConfirmDialog(parent, msg, title, JOptionPane.YES_OPTION);
		
		if (option == JOptionPane.YES_OPTION)
			return true;
		
		return false;
	}
	
	private void editChartProps() {
		Window window = SwingUtilities.getWindowAncestor(panel);
		ConfigDialog dialog = null;
//		if (window instanceof JFrame) dialog = new ConfigDialog((JFrame) window);
		if (window instanceof JFrame) 
			dialog = new ConfigDialog((JMapFrame) window);
		else dialog = new ConfigDialog((JDialog) window);

		dialog.init(this, minMax);
		dialog.setSize(500, 600);
		dialog.setVisible(true);
	}

	public JPanel getPanel() {
		if (panel == null) {
			panel = new JPanel(new BorderLayout(), false);
			panel.add(display.getComponent(), BorderLayout.CENTER);
			panel.add(legend, BorderLayout.EAST);
			panel.setBackground(Color.BLACK);
		}
		return panel;
	}

	/**
	 * Removes the specified PlotListener.
	 *
	 * @param listener the plot listener to remove
	 */
	public void removePlotListener(PlotListener listener) {
		eventProducer.removeListener(listener);
	}

	/**
	 * Gets the type of the Plot.
	 *
	 * @return the type of the Plot.
	 */
	public Formula.Type getType() {
		return Formula.Type.CONTOUR;
	}


	/**
	 * Exports an image of this Plot to the specified file in the
	 * specified format.
	 *
	 * @param format the image format. One of PlotExporter.JPG, PlotExporter.TIF,
	 *               PlotExporter.PNG, or PlotExporter.BMP
	 * @param file   the file to save the image to.
	 * @param width  width of image in pixels
	 * @param height height of image in pixels
	 */
	public void exportImage(String format, File file, int width, int height) throws IOException {

	}


	/**
	 * Configure this Plot according to the specified PlotConfiguration.
	 *
	 * @param config the new plot configuration
	 */
	public void configure(PlotConfiguration config, Plot.ConfigSoure source) {
		configure( config);
	}
	public void configure(PlotConfiguration config) {
		String configFile = config.getConfigFileName();
		if (configFile != null) {
			try {
				configure(new PlotConfigurationIO().loadConfiguration(new File(configFile)));
			} catch (IOException ex) {
				Logger.error("Error loading configuration " + ex.getMessage());
			}
		}
		ColorMap map = (ColorMap) config.getObject(TilePlotConfiguration.COLOR_MAP);

		try {
			initColors(map);
		} catch (RemoteException e) {
			Logger.error("Error setting color map " + e.getMessage());
		} catch (VisADException e) {
			Logger.error("Error setting color map " + e.getMessage());
		}

		configureTitle(canvas.getTitle(), config.getTitle(),
						config.getColor(PlotConfiguration.TITLE_COLOR),
						config.getFont(PlotConfiguration.TITLE_FONT));
		configureTitle(canvas.getSub1(), config.getSubtitle1(),
						config.getColor(PlotConfiguration.SUBTITLE_1_COLOR),
						config.getFont(PlotConfiguration.SUBTITLE_1_FONT));
		configureTitle(canvas.getSub2(), config.getSubtitle2(),
						config.getColor(PlotConfiguration.SUBTITLE_2_COLOR),
						config.getFont(PlotConfiguration.SUBTITLE_2_FONT));

		AxisScale axis = xMap.getAxisScale();
		String label = config.getString(PlotConfiguration.DOMAIN_LABEL);
		axis.setTitle(label == null ? "" : label);	// 2014 default is the Scalar Name of the Scalar Map ???
				// 2014 .setLabel deprecated, replacing with .setTitle
		Font font = config.getFont(PlotConfiguration.DOMAIN_FONT);
		if (font != null) axis.setFont(font);
		Color color = config.getColor(PlotConfiguration.DOMAIN_COLOR);
		if (color != null) axis.setColor(color);
		Boolean show = (Boolean) config.getObject(PlotConfiguration.DOMAIN_SHOW_TICK);
		if (show != null) axis.setTicksVisible(show);

		axis = yMap.getAxisScale();
		label = config.getString(PlotConfiguration.RANGE_LABEL);
		axis.setTitle(label == null ? "" : label);	// 2014 default is the Scalar Name of the Scalar Map ???
		font = config.getFont(PlotConfiguration.RANGE_FONT);
		if (font != null) axis.setFont(font);
		color = config.getColor(PlotConfiguration.RANGE_COLOR);
		if (color != null) axis.setColor(color);
		show = (Boolean) config.getObject(PlotConfiguration.RANGE_SHOW_TICK);
		if (show != null) axis.setTicksVisible(show);

		axis = zMap.getAxisScale();
		label = config.getString(PlotConfiguration.Z_LABEL);
		axis.setTitle(label == null ? "" : label);	// 2014 default is the Scalar Name of the Scalar Map ???
		font = config.getFont(PlotConfiguration.Z_FONT);
		if (font != null) axis.setFont(font);
		color = config.getColor(PlotConfiguration.Z_COLOR);
		if (color != null) axis.setColor(color);
		show = (Boolean) config.getObject(PlotConfiguration.Z_SHOW_TICK);
		if (show != null) axis.setTicksVisible(show);

		String units = config.getString(PlotConfiguration.UNITS);
		if (units == null) units = "";
		ValueAxis legendAxis = legend.getLegend().getAxis();
		legendAxis.setLabel(units);
		font = config.getFont(PlotConfiguration.UNITS_FONT);
		if (font != null) {
			legendAxis.setLabelFont(font);
		}
		color = config.getColor(PlotConfiguration.UNITS_COLOR);
		if (color != null) legendAxis.setLabelPaint(color);

		show = (Boolean) config.getObject(PlotConfiguration.UNITS_SHOW_TICK);
		color = config.getColor(PlotConfiguration.UNITS_TICK_COLOR);
		font = config.getFont(PlotConfiguration.UNITS_TICK_FONT);

		if (color == null) legendAxis.setTickLabelPaint(Color.WHITE);
		else legendAxis.setTickLabelPaint(color);
		if (show != null) legendAxis.setTickLabelsVisible(show);
		if (font != null) legendAxis.setTickLabelFont(font);

		legend.invalidate();
		legend.repaint();
		this.config = config;
	}

	private void configureTitle(Title title, String text, Color color, Font font) {
		if (text == null) text = "";
		title.setText(text);
		title.setColor(color);
		title.setFont(font);
	}

	/**
	 * Gets this Plot's configuration data.
	 *
	 * @return this Plot's configuration data.
	 */
	public PlotConfiguration getPlotConfiguration() {
		TilePlotConfiguration config = new TilePlotConfiguration();
		config.setColorMap(map);

		config = (TilePlotConfiguration) getTitlesLabelsConfig(config);

		ValueAxis axis = legend.getLegend().getAxis();
		config.putObject(PlotConfiguration.UNITS, axis.getLabel());
		Font font = axis.getLabelFont();
		config.putObject(PlotConfiguration.UNITS_FONT, font);
		config.putObject(PlotConfiguration.UNITS_COLOR, axis.getLabelPaint());

		config.putObject(PlotConfiguration.UNITS_SHOW_TICK, axis.isTickLabelsVisible());
		config.putObject(PlotConfiguration.UNITS_TICK_COLOR, (Color) axis.getTickLabelPaint());
		config.putObject(PlotConfiguration.UNITS_TICK_FONT, axis.getTickLabelFont());
		
		config.putObject(PlotConfiguration.FOOTER1_SHOW_LINE, true);
		config.putObject(PlotConfiguration.FOOTER2_SHOW_LINE, true);
		config.putObject(PlotConfiguration.OBS_SHOW_LEGEND, false);

		AxisScale zAxis = zMap.getAxisScale();
		font = zAxis.getFont();
		if (font == null) font = Font.decode(null);
		config.putObject(PlotConfiguration.Z_LABEL, zAxis.getTitle()); 	// 2014 default is the Scalar Name of the Scalar Map ???
		config.putObject(PlotConfiguration.Z_FONT, font);
		config.putObject(PlotConfiguration.Z_COLOR, zAxis.getColor());
		config.putObject(PlotConfiguration.Z_SHOW_TICK, zAxis.getTicksVisible());

		return config;
	}

	protected PlotConfiguration getTitlesLabelsConfig(PlotConfiguration config) {
		Title title = canvas.getTitle();
		config.setTitle(title.getText());
		config.putObject(PlotConfiguration.TITLE_FONT, title.getFont());
		config.putObject(PlotConfiguration.TITLE_COLOR, title.getColor());

		title = canvas.getSub1();
		config.setSubtitle1(title.getText());
		config.putObject(PlotConfiguration.SUBTITLE_1_FONT, title.getFont());
		config.putObject(PlotConfiguration.SUBTITLE_1_COLOR, (Color) title.getColor());

		title = canvas.getSub2();
		config.setSubtitle2(title.getText());
		config.putObject(PlotConfiguration.SUBTITLE_2_FONT, title.getFont());
		config.putObject(PlotConfiguration.SUBTITLE_2_COLOR, (Color) title.getColor());

		AxisScale axis = xMap.getAxisScale();
		config.putObject(PlotConfiguration.DOMAIN_LABEL, axis.getTitle());	// 2014 default is the Scalar Name of the Scalar Map ???
		Font font = axis.getFont();
		if (font == null) font = Font.decode(null);
		config.putObject(PlotConfiguration.DOMAIN_FONT, font);
		config.putObject(PlotConfiguration.DOMAIN_COLOR, axis.getColor());
		config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, axis.getTicksVisible());

		axis = yMap.getAxisScale();
		font = axis.getFont();
		if (font == null) font = Font.decode(null);
		config.putObject(PlotConfiguration.RANGE_LABEL, axis.getTitle());	// 2014 default is the Scalar Name of the Scalar Map ???
		config.putObject(PlotConfiguration.RANGE_FONT, font);
		config.putObject(PlotConfiguration.RANGE_COLOR, axis.getColor());
		config.putObject(PlotConfiguration.RANGE_SHOW_TICK, axis.getTicksVisible());
		return config;
	}
	
	public String getTitle() {
		return canvas.getTitle().getText();
	}
	
	public void viewClosed() {
		
		frame = null;
		eventProducer = null;
		map = null;
		minMax = null;
		config = null;
		display = null;
		timeToGrid = null;
		timeType = null;
		domain = null;
		gridType = null;
		dataRef = null;
		panel = null;
		canvas = null;
		zMap = null;
		valueMap = null;
		legend = null;
		yMap = null;
		xMap = null;
		isoMap = null;
		timeLayerPanel = null;		
		
	}	
}
