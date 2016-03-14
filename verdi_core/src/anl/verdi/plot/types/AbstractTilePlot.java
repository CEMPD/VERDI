package anl.verdi.plot.types;

import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.vecmath.Point4i;


//import org.geotools.styling.StyleFactoryFinder;	// replaced for GeoTools v10
//import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;	// 2014 using shapefile-old jar to get this functionality
//import org.geotools.map.DefaultMapContext;	// replaced for GeoTools v10
//import org.geotools.map.DefaultMapLayer;		// replaced for GeoTools v10
//import org.geotools.map.MapContext;			// replaced for GeoTools v10
//import org.geotools.map.MapLayer;				// replaced for GeoTools v10
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.coverage.CoverageFactoryFinder;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.data.Query;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.MapViewport;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.swing.JMapPane;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import ucar.ma2.InvalidRangeException;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.Slice;
import anl.verdi.formula.Formula;
import anl.verdi.gis.LayerEditor;
import anl.verdi.plot.anim.AnimationPanel;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.PavePaletteCreator;
import anl.verdi.plot.config.JFreeChartConfigurator;
import anl.verdi.plot.config.LoadConfiguration;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.PlotConfigurationIO;
import anl.verdi.plot.config.SaveConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.config.TitleConfigurator;
import anl.verdi.plot.config.UnitsConfigurator;
import anl.verdi.plot.gui.ConfigDialog;
import anl.verdi.plot.gui.MapAnnotation;
import anl.verdi.plot.gui.MultiTimeSeriesPlotRequest;
import anl.verdi.plot.gui.ObsAnnotation;
import anl.verdi.plot.gui.PlotListener;
import anl.verdi.plot.gui.TimeSeriesPlotRequest;
import anl.verdi.plot.jfree.XYBlockRenderer;
import anl.verdi.plot.probe.PlotEventProducer;
import anl.verdi.plot.util.PlotExporterAction;
import anl.verdi.plot.util.PlotPrintAction;
import anl.verdi.util.Tools;
import anl.verdi.util.Utilities;

/**
 * Abstract base class for Tile style plots.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public abstract class AbstractTilePlot extends AbstractPlot implements TimeAnimatablePlot {
	static final Logger Logger = LogManager.getLogger(AbstractTilePlot.class.getName());

	public static final int NO_VAL = Integer.MIN_VALUE;
	private static final String COUNTIES_LAYER = "COUNTIES";
	private static final String WORLD_LAYER = "WORLD";
	private static final String NA_LAYER = "NA";

	private NumberFormat format;

	protected DataFrame frame;
	protected int timeStep;
	protected int layer;

	protected JFreeChart chart;
	protected PlotEventProducer eventProducer = new PlotEventProducer();
	protected ColorMap map;
	protected Slice probedSlice;
	protected List<JMenuItem> probeItems = new ArrayList<JMenuItem>();
	protected MapAnnotation mapAnnotation;
	protected ControlAction controlAction = ControlAction.ZOOM;
	private DataUtilities.MinMax minMax;
	protected boolean showLatLon = false;
	protected PlotConfiguration config;

	protected int subTitle1Index, subTitle2Index, bottomTitle1Index, bottomTitle2Index;
	protected int legendIndex = -1;
	protected String units;

	protected Map<String, FeatureLayer> mapLayers = new HashMap<String, FeatureLayer>();
	
	public void viewClosed() {
		Logger.debug("in AbstractTilePlot.viewClosed");		// not output in log
		format = null;
		frame = null;
		chart = null;
		eventProducer = null;
		map = null;
		probedSlice = null;
		probeItems = null;
		mapAnnotation = null;
		minMax = null;
		config = null;
		mapLayers = null;
	}

	protected Action timeSeriesSelected = new AbstractAction("Time Series of Probed Cell(s)") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1284894656862350012L;

		public void actionPerformed(ActionEvent e) {
			requestTimeSeries(Formula.Type.TIME_SERIES_LINE);
		}
	};

	protected Action timeSeriesBarSelected = new AbstractAction("Time Series Bar of Probed Cell(s)") {
		/**
		 * 
		 */
		private static final long serialVersionUID = -2272293540115554751L;

		public void actionPerformed(ActionEvent e) {
			requestTimeSeries(Formula.Type.TIME_SERIES_BAR);
		}
	};

	protected Action timeSeriesMin = new AbstractAction("Time Series of Min. Cell(s)") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 7008434214818635293L;

		public void actionPerformed(ActionEvent e) {
			DataUtilities.MinMaxPoint points = getMinMaxPoints();
			requestTimeSeries(points.getMinPoints(), "Min. cells ");

		}
	};

	protected Action timeSeriesMax = new AbstractAction("Time Series of Max. Cell(s)") {
		/**
		 * 
		 */
		private static final long serialVersionUID = 2328718281191666454L;

		public void actionPerformed(ActionEvent e) {
			DataUtilities.MinMaxPoint points = getMinMaxPoints();
			requestTimeSeries(points.getMaxPoints(), "Max. cells ");
		}
	};


	public AbstractTilePlot(DataFrame frame) {
		Logger.debug("in AbstractTilePlot constructor for a DataFrame");
		subTitle2Index = subTitle1Index = bottomTitle1Index = bottomTitle2Index = -1;
		layer = 0;
		timeStep = 0;
		this.frame = frame;
		minMax = DataUtilities.minMax(frame);
		format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(4);
	}

	/**
	 * Gets the data that this Plot plots.
	 *
	 * @return the data that this Plot plots.
	 */
	public List<DataFrame> getData() {
		List<DataFrame> list = new ArrayList<DataFrame>();
		list.add(frame);
		return list;
	}

	/**
	 * Configure this plot according to the specified configure info.
	 *
	 * @param config the configuration data
	 */
	public void configure(PlotConfiguration config) {
		Logger.debug("in AbstractTilePlot.configure");

		if (mapAnnotation != null) mapAnnotation.setUpdate(false);
		String configFile = config.getConfigFileName();
		if (configFile != null) {
			try {
				configure(new PlotConfigurationIO().loadConfiguration(new File(configFile)));
			} catch (IOException ex) {
				Logger.error("Error loading configuration " + ex.getMessage());
			}
		}

		ColorMap map = (ColorMap) config.getObject(TilePlotConfiguration.COLOR_MAP);
		if (map != null) updateColorMap(map);

		TitleConfigurator titleConfig = new TitleConfigurator() {
			public void configureSubtitle1(Boolean show, String text, Font font, Color color) {
				TextTitle title = (TextTitle) chart.getSubtitle(subTitle1Index);
				title.setVisible(show);
				updateTextTitle(title, text, color, font);
			}

			public void configureSubtitle2(Boolean show, String text, Font font, Color color) {
				TextTitle title = (TextTitle) chart.getSubtitle(subTitle2Index);
				title.setVisible(show);
				updateTextTitle(title, text, color, font);
			}

			public void configureTitle(Boolean show, String text, Font font, Color color) {
				TextTitle title = chart.getTitle();
				title.setVisible(show);
				updateTextTitle(title, text, color, font);
			}
		};

		UnitsConfigurator unitsConfig = new UnitsConfigurator() {

			public void configureUnits(String text, Font font, Color color) {
				if (text == null) text = "";
				PaintScaleLegend legend = (PaintScaleLegend) chart.getSubtitle(legendIndex);
				ValueAxis axis = legend.getAxis();
				axis.setLabel(text);
				if (font != null) axis.setLabelFont(font);
				if (color != null) axis.setLabelPaint(color);
				units = text;
			}

			public void configureUnitsTick(Boolean show, Font font, Color color) {
				PaintScaleLegend legend = (PaintScaleLegend) chart.getSubtitle(legendIndex);
				ValueAxis axis = legend.getAxis();
				if (show != null) axis.setTickLabelsVisible(show);
				if (font != null) axis.setTickLabelFont(font);
				if (color != null) axis.setTickLabelPaint(color);
			}
		};

		JFreeChartConfigurator configurator = new JFreeChartConfigurator(chart, titleConfig, unitsConfig);
		configurator.configure(config);

		Boolean show = (Boolean) config.getObject(TilePlotConfiguration.SHOW_GRID_LINES);
		if (show == null) show = false;

		XYBlockRenderer renderer;
		XYPlot plot = (XYPlot) chart.getPlot();
		if (plot.getRenderer() instanceof XYBlockRenderer) renderer = (XYBlockRenderer) plot.getRenderer();
		else renderer = (XYBlockRenderer) plot.getRenderer(1);

		renderer.setGridLinesEnabled(show);
		Color color = config.getColor(TilePlotConfiguration.GRID_LINE_COLOR);
		if (color != null) renderer.setGridLineColor(color);

		if (mapAnnotation != null) mapAnnotation.setUpdate(true);

		if (config.getObject(TilePlotConfiguration.OBS_SHAPE_SIZE) != null) {
			int stroke = ((Integer) config.getObject(TilePlotConfiguration.OBS_STROKE_SIZE)).intValue();
			int shape = ((Integer) config.getObject(TilePlotConfiguration.OBS_SHAPE_SIZE)).intValue();
			List annotations = ((XYPlot) chart.getPlot()).getAnnotations();
			for (Object obj : annotations) {
				if (obj instanceof ObsAnnotation) {
					ObsAnnotation obs = (ObsAnnotation) obj;
					obs.setDrawingParams(stroke, shape, this.map);
				}
			}
		}

		chart.fireChartChanged();
		this.config = config;
	}

	protected void updateTextTitle(TextTitle title, String text, Color color, Font font) {
		Logger.debug("in AbstractTilePlot.updateTextTitle");
		if (title != null) {
			if (text != null && text.length() > 0) {
				if (text != null) title.setText(text);
				if (color != null) title.setPaint(color);
				if (font != null) title.setFont(font);
			} else {
				title.setText("");
			}
		}
	}

	/**
	 * Gets the global min max. The global min max
	 * is the min max of the entire data frame, not just
	 * the particular slice of it that a plot may be currently
	 * displaying.
	 *
	 * @return the global min max.
	 */
	public DataUtilities.MinMax getGlobalMinMax() {
		return minMax;
	}

	/**
	 * Gets current min max. It is nice for user to continuously
	 * modify the current chart.
	 * 
	 * @return the current min max.
	 */
	public DataUtilities.MinMax getCurrentMinMax() {
		if (map == null)
			return getGlobalMinMax();
		
		try {
			return new DataUtilities.MinMax(map.getMin(), map.getMax());
		} catch ( Exception e) {
			Logger.error("AbstractTilePlot's getCurrentMinMax " + e.getMessage());
		}
		return null;
	}
	
	/**
	 * Gets a BufferedImage of the plot.
	 *
	 * @return a BufferedImage of the plot.
	 */
	public BufferedImage getBufferedImage() {
		Logger.debug("in AbstractTilePlot.getBufferedImage()");
		return getBufferedImage(panel.getWidth(), panel.getHeight());
	}

	/**
	 * Gets a BufferedImage of the plot.
	 *
	 * @param width  the width of the image in pixels
	 * @param height the height of the image in pixels
	 * @return a BufferedImage of the plot.
	 */
	public BufferedImage getBufferedImage(int width, int height) {
		Logger.debug("in AbstractTilePlot.getBufferedImage(width, height)");
		return chart.createBufferedImage(width, height);
	}


	/**
	 * Gets the current color map.
	 *
	 * @return the current color map.
	 */
	public ColorMap getColorMap() {
		Logger.debug("in AbstractTilePlot.getColorMap");

		return map;
	}

	protected GridCoverage2D getCoverage(DataFrame frame) {
		Logger.debug("in AbstractTilePlot.getCoverage(DataFrame)");

		Axes<DataFrameAxis> axes = frame.getAxes();
	//	GridCoverageFactory fac = new GridCoverageFactory();	// replaced for GeoTools v10
		GridCoverageFactory fac = CoverageFactoryFinder.getGridCoverageFactory(null);
		float[][] data = new float[axes.getXAxis().getExtent()][axes.getYAxis().getExtent()];
		return (GridCoverage2D) fac.create("data", data, axes.getBoundingBox(frame.getDataset().get(0).getNetcdfCovn()));

	}

	protected FeatureLayer createLayer(File file, File styleFile) throws IOException {		// replaced MapLayer with FeatureLayer for GeoTools v10
		Logger.debug("in AbstractTilePlot.createLayer(File, File)");
		URL url = file.toURI().toURL();
		Map<String, Serializable> params = new HashMap<String, Serializable>();
//		params.put(IndexedShapefileDataStoreFactory.URLP.key, url);
//		params.put(IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, true);
//		IndexedShapefileDataStoreFactory fac = new IndexedShapefileDataStoreFactory();
		params.put(ShapefileDataStoreFactory.URLP.key, url);
		params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, true);
		ShapefileDataStoreFactory fac = new ShapefileDataStoreFactory();
		ShapefileDataStore ds = (ShapefileDataStore) fac.createDataStore(params);
		SLDParser parser = new SLDParser(CommonFactoryFinder.getStyleFactory());	// replaced StyleFactoryFinder with CommonFactoryFinder for GeoTools v10
		parser.setInput(styleFile);
		//Style style = parser.readXML()[0];
		StyleBuilder builder = new StyleBuilder();
		Style style = builder.createStyle(builder.createLineSymbolizer());
		Query query = new Query();
		query.setCoordinateSystemReproject(frame.getAxes().getBoundingBox(frame.getDataset().get(0).getNetcdfCovn()).getCoordinateReferenceSystem());
//		MapLayer layer = new DefaultMapLayer(ds.getFeatureSource().getFeatures(query), style);	// replaced MapLayer and DefaultMapLayer with FeatureLayer for GeoTools v10
		FeatureLayer layer = new FeatureLayer(ds.getFeatureSource().getFeatures(query), style);
		layer.setTitle(file.getAbsolutePath());
		return layer;
	}

	private void requestTimeSeries(Set<Point> points, String title) {
		Logger.debug("in AbstractTilePlot.requestTimeSeries for points and title");
		MultiTimeSeriesPlotRequest request = new MultiTimeSeriesPlotRequest(title);
		for (Point point : points) {
			Slice slice = new Slice();
			// slice needs to be in terms of the actual array indices
			// of the frame, but the axes ranges refer to the range
			// of the original dataset. So, the origin will always
			// be 0 and the exent is the frame's exent.
			slice.setTimeRange(0, frame.getAxes().getTimeAxis().getExtent());
			DataFrameAxis frameAxis = frame.getAxes().getZAxis();
			if (frameAxis != null) slice.setLayerRange(0, frameAxis.getExtent());
			slice.setXRange(point.x, 1);
			slice.setYRange(point.y, 1);
			try {
				DataFrame subsection = frame.slice(slice);
				request.addItem(subsection);
			} catch (InvalidRangeException e1) {
				Logger.error("Error while creating time series from tile " + e1.getMessage());
			}
		}
		eventProducer.firePlotRequest(request);
	}

	private void requestTimeSeries(Formula.Type type) {
		Logger.debug("in AbstractTilePlot.requestTimeSeries for a Formula.Type");
		Slice slice = new Slice();
		// slice needs to be in terms of the actual array indices
		// of the frame, but the axes ranges refer to the range
		// of the original dataset. So, the origin will always
		// be 0 and the exent is the frame's exent.
		slice.setTimeRange(0, frame.getAxes().getTimeAxis().getExtent());
		DataFrameAxis frameAxis = frame.getAxes().getZAxis();
		if (frameAxis != null) slice.setLayerRange(0, frameAxis.getExtent());
		slice.setXRange(probedSlice.getXRange());
		slice.setYRange(probedSlice.getYRange());

		try {
			DataFrame subsection = frame.slice(slice);
			eventProducer.firePlotRequest(new TimeSeriesPlotRequest(subsection, slice, type));
		} catch (InvalidRangeException e1) {
			Logger.error("Error while creating time series from tile " + e1.getMessage());
		}
	}

	// creates the TextTitles for the subtitles, but doesn't
	// give them any content
	protected void createSubtitles() {
		Logger.debug("in AbstractTilePlot.createSubtitles");
		if (bottomTitle1Index == -1) {
			TextTitle title = new TextTitle();
			title.setPosition(RectangleEdge.BOTTOM);
			int index = chart.getSubtitleCount();
			chart.addSubtitle(title);
			// 2 is first because titles are pushed "down" as they are added
			bottomTitle2Index = index++;

			title = new TextTitle();
			title.setPosition(RectangleEdge.BOTTOM);
			chart.addSubtitle(title);
			bottomTitle1Index = index++;

			title = new TextTitle();
			title.setPosition(RectangleEdge.TOP);
			chart.addSubtitle(title);
			// 2 is first because titles are pushed "down" as they are added
			subTitle2Index = index++;

			title = new TextTitle();
			title.setPosition(RectangleEdge.TOP);
			chart.addSubtitle(title);
			subTitle1Index = index++;
		}
	}

	protected Point2D getLatLonForAxisPoint(Point axisPoint) {
		return frame.getAxes().getBoundingBoxer().axisPointToLatLonPoint(axisPoint.x, axisPoint.y);
	}

	/**
	 * Converts the rectangle to 1 or 2 Point3i. The length
	 * of the array must be two, but the second item can be null.
	 * The points represent the top left hand corner and the bottom
	 * right hand corner of the rect.
	 *
	 * @param rect the rect to convert.
	 * @return an array of Point4i
	 */
	protected abstract Point4i[] rectToPoints(Rectangle rect);

	protected String createAreaString(Rectangle rect) {
		Point4i[] points = rectToPoints(rect);
		if (showLatLon) return createLonLatAreaString(points);
		else return createAxisCoordAreaString(points);
	}

	public String createAreaString(int[] point) {
		Point4i p = new Point4i();
		p.w = point[0];
		p.z = point[1];
		p.x = point[2];
		p.y = point[3];
		if (showLatLon) return formatPointLatLon(p);
		else return formatPoint(p);
	}

	private String formatPointLatLon(Point4i point) {
		Point2D llul = getLatLonForAxisPoint(new Point(point.x, point.y));	// 2014 replaced .getX() with x and .getY() with y
		StringBuilder builder = new StringBuilder("(");
		double[] vals = new double[4];
		vals[0] = point.w;		// 2014 getW();
		vals[1] = point.z;		// 2014 getZ();
		vals[2] = llul.getY();
		vals[3] = llul.getX();
		boolean addComma = false;
		for (int i = 0; i < 4; i++) {
			double val = vals[i];
			if (val != NO_VAL) {
				if (addComma) builder.append(", ");

				if (i == 2) builder.append(Utilities.formatLat(val, 4));
				else if (i == 3) builder.append(Utilities.formatLon(val, 4));
				else builder.append(format.format(val));
				addComma = true;
			}
		}
		builder.append(")");
		return builder.toString();
	}

	public String formatPoint(Point4i point) {
		StringBuilder builder = new StringBuilder("(");
		int[] vals = new int[4];
		vals[0] = point.w;		// 2014 getW();
		vals[1] = point.z;		// 2014 getZ();
		vals[2] = point.x;		// 2014 getX();
		vals[3] = point.y;		// 2014 getY();
		boolean addComma = false;
		for (int val : vals) {
			if (val != NO_VAL) {
				if (addComma) builder.append(", ");
				builder.append(val + 1);
				addComma = true;
			}
		}
		builder.append(")");
		return builder.toString();
	}

	// rect is in axis coords
	private String createAxisCoordAreaString(Point4i[] points) {
		StringBuilder builder = new StringBuilder();
		builder.append(formatPoint(points[0]));
		if (points[1] != null) {
			builder.append(" - ");
			builder.append(formatPoint(points[1]));
		}
		return builder.toString();
	}

	// rect is in axis coordinates
	protected String createLonLatAreaString(Point4i[] points) {
		StringBuilder builder = new StringBuilder();
		builder.append(formatPointLatLon(points[0]));
		if (points[1] != null) {
			builder.append(" - ");
			builder.append(formatPointLatLon(points[1]));
		}
		return builder.toString();
	}

	public JPanel getPanel() {
		Logger.debug("in AbstractTilePlot.getPanel(); returning a VerdiChartPanel as JPanel");
		return panel;
	}

	// creates the map annotation
	protected void createMapAnnotation() {
		//GridCoverage2D coverage = getCoverage(frame);
//		MapContext context = new DefaultMapContext(frame.getAxes().getBoundingBox(frame.getDataset().get(0).getNetcdfCovn()).getCoordinateReferenceSystem());
				// replaced MapContext, DefaultMapContext with MapContent for GeoTools v10
				// moved BoundingBox and CoordinateReferenceSystem to MapViewport object associated with the MapContent object
		MapContent context = new MapContent();	//		frame.getAxes().getBoundingBox(frame.getDataset().get(0).getNetcdfCovn()).getCoordinateReferenceSystem());

		MapViewport aViewport = new MapViewport(frame.getAxes().getBoundingBox(frame.getDataset().get(0).getNetcdfCovn()));	// defined ReferencedEnvelope
		aViewport.setCoordinateReferenceSystem(frame.getAxes().getBoundingBox(frame.getDataset().get(0).getNetcdfCovn()).getCoordinateReferenceSystem());	// defined CRS
		context.setViewport(aViewport);	// assign Viewport (ReferencedEnvelope and BoundingBox) to MapContent object

		String defaultMaps = System.getProperty("default.maps", "");
		try {
			File file = new File("data/map_na.shp");
//			MapLayer layer = createLayer(file, new File("data/states.sld"));	// replace MapLayer with FeatureLayer
			FeatureLayer layer = createLayer(file, new File("data/states.sld"));
			mapLayers.put(NA_LAYER, layer);
			if (defaultMaps.contains(NA_LAYER) || defaultMaps.length() == 0) context.addLayer(layer);
		} catch (Exception e) {
			Logger.warn("Unable to load map data " + e.getMessage());
		}

		try {
			File file = new File("data/map_counties.shp");
//			MapLayer layer = createLayer(file, new File("data/counties.sld"));
			FeatureLayer layer = createLayer(file, new File("data/counties.sld"));
			mapLayers.put(COUNTIES_LAYER, layer);
			if (defaultMaps.contains(COUNTIES_LAYER)) context.addLayer(layer);
		} catch (Exception e) {
			Logger.warn("Unable to load map data " + e.getMessage());
		}

		try {
			File file = new File("data/map_world.shp");
//			MapLayer layer = createLayer(file, new File("data/world.sld"));
			FeatureLayer layer = createLayer(file, new File("data/world.sld"));
			mapLayers.put(WORLD_LAYER, layer);
			if (defaultMaps.contains(WORLD_LAYER)) context.addLayer(layer);
		} catch (Exception e) {
			Logger.warn("Unable to load map data " + e.getMessage());
		}

		StringTokenizer tok = new StringTokenizer(defaultMaps, ",");
		while (tok.hasMoreTokens()) {
			String shpFile = tok.nextToken().trim();
			if (!shpFile.equals(WORLD_LAYER) && !shpFile.equals(COUNTIES_LAYER) && !
							shpFile.equals(NA_LAYER)) {
				try {
					File file = new File(shpFile);
//					MapLayer layer = createLayer(file, new File("data/world.sld"));
					FeatureLayer layer = createLayer(file, new File("data/world.sld"));
					context.addLayer(layer);
				} catch (Exception e) {
					Logger.warn("Unable to load map data " + e.getMessage());
				}
			}
		}

//		context.setAreaOfInterest(frame.getAxes().getBoundingBox(-1));	// function deprecated; already set ReferencedEnvelope above. Do we need to do it again here?
		mapAnnotation = new MapAnnotation(context, frame.getAxes(), -1); 
	}

	/**
	 * Enables / disables the menu items that work with the currently probed point.
	 *
	 * @param val true to enable
	 */
	protected void enableProbeItems(boolean val) {
		for (JMenuItem item : probeItems) {
			item.setEnabled(val);
		}
	}

	private void configureMapMenu(JMenu menu) {
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JCheckBoxMenuItem source = (JCheckBoxMenuItem) evt.getSource();
				showLayer(source.getActionCommand(), source.isSelected());
			}
		};

		String defaultMaps = System.getProperty("default.maps", "");

		JMenu mapMenu = new JMenu("Maps");
		menu.add(mapMenu);
		JCheckBoxMenuItem item = new JCheckBoxMenuItem("World", defaultMaps.contains(WORLD_LAYER));
		item.setActionCommand(WORLD_LAYER);
		item.addActionListener(listener);
		mapMenu.add(item);

		item = new JCheckBoxMenuItem("North America", defaultMaps.contains(NA_LAYER));
		item.setActionCommand(NA_LAYER);
		item.addActionListener(listener);
		mapMenu.add(item);

		item = new JCheckBoxMenuItem("USA Counties", defaultMaps.contains(COUNTIES_LAYER));
		item.setActionCommand(COUNTIES_LAYER);
		item.addActionListener(listener);
		mapMenu.add(item);

		mapMenu.add(new AbstractAction("Configure GIS Layers") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -7478330674957172004L;

			public void actionPerformed(ActionEvent e) {
				Logger.debug("in AbstractTilePlot.actionPerformed; ready to get frame from SwingUtilities.getWindowAncestor(panel)");
				Window frame = SwingUtilities.getWindowAncestor(panel);
				LayerEditor editor = null;
				if (frame instanceof JFrame) 
					editor = new LayerEditor((JFrame) frame);
				else 
					{
					Logger.debug("casting frame to JDialog");
					editor = new LayerEditor((JDialog) frame);
					}
				editor.init(mapAnnotation);
				editor.setLocationRelativeTo(frame);
				editor.pack();
				editor.setVisible(true);
				if (!editor.wasCanceled()) {
					chart.getPlot().notifyListeners(new PlotChangeEvent(chart.getPlot()));
				}
			}
		});

		mapMenu.addSeparator();
// --------------------------------------------------------------------------------------------------------------
		mapMenu.add(new AbstractAction("Set Current Maps As Plot Default") 
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 4424815817465758570L;

			public void actionPerformed(ActionEvent e) 
			{
				StringBuilder buf = new StringBuilder();
//				MapContext context = mapAnnotation.getMapContext();		// replaced for GeoTools v10
//				for (int i = 0; i < context.getLayerCount(); i++) // change to get the List<Layer> and then iterate through to handle each one
//				{
////					MapLayer layer = context.getLayer(i);
//					FeatureLayer layer = context.getLayer(i);
//					boolean layerFound = false;
//					for (String key : mapLayers.keySet()) {
//						if (mapLayers.get(key).equals(layer)) {
//							buf.append(key);
//							buf.append(",");
//							layerFound = true;
//							break;
//						}
//					}
//
//					if (!layerFound) {
//						buf.append(layer.getTitle());
//						buf.append(",");
//					}
//				}
				MapContent context = mapAnnotation.getMapContext();		// getMapContext() now returns MapContent
				List<Layer> lstLayers = context.layers();		// list of layers for this MapContent
				int numLayers = lstLayers.size();				// # elements in lstLayers
				for (int i = 0; i < numLayers; i++)				// process each Layer in LstLayers
				{
					Layer thisLayer = lstLayers.get(i);
					boolean layerFound = false;
					for (String key : mapLayers.keySet())
					{
						if (mapLayers.get(key).equals(thisLayer))
						{
							buf.append(key);
							buf.append(",");
							layerFound = true;
							break;
						}		// close if equals
					}		// close for key in mapLayers
					if(!layerFound)
					{
						buf.append(thisLayer.getTitle());
						buf.append(",");
					}		// close if not layerFound
				}		// close for each Layer in lstLayers

				String defaultMaps = buf.deleteCharAt(buf.length() - 1).toString();
				System.getProperties().put("default.maps", defaultMaps);
				Properties props = new Properties();
				props.put("default.maps", defaultMaps);
				try {
					File file = new File(Tools.getPropertyFile());	// 2014 changed to use static function directly
					props.store(new FileOutputStream(file), "");
				} catch (IOException ex) {
					Logger.warn("Error while saving map properties " + ex.getMessage());
				}
			}
		});
	// --------------------------------------------------------------------------------------------------------------
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
			private static final long serialVersionUID = -4734285724218311200L;

			public void actionPerformed(ActionEvent e) {
				panel.doEditChartProperties();
			}
		});

		menu.add(new LoadConfiguration(this));
		menu.add(new SaveConfiguration(this));

		if (mapAnnotation != null) {
			configureMapMenu(menu);
		}
		bar.add(menu);

		menu = new JMenu("Controls");
		bar.add(menu);
		ButtonGroup grp = new ButtonGroup();
		JMenuItem item = menu.add(new JCheckBoxMenuItem(new AbstractAction("Zoom") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6259164410161552407L;

			public void actionPerformed(ActionEvent e) {
				// JCheckBoxMenuItem box = (JCheckBoxMenuItem) e.getSource();
				// panel.setMouseZoomEnabled(box.isSelected());
				controlAction = ControlAction.ZOOM;
			}
		}));
		item.setSelected(true);
		grp.add(item);

		item = menu.add(new JCheckBoxMenuItem(new AbstractAction("Probe") {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3865544500553578820L;

			public void actionPerformed(ActionEvent e) {
				// panel.setMouseZoomEnabled(false);
				controlAction = ControlAction.PROBE;
			}
		}));
		grp.add(item);
		menu.add(item);
		menu.addSeparator();
		menu.add(new JCheckBoxMenuItem(new AbstractAction("Show Lat / Lon") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6168773604594413948L;

			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem item = (JCheckBoxMenuItem) e.getSource();
				showLatLon = item.isSelected();
				createSubtitle();
			}
		}));

		menu = new JMenu("Plot");
		bar.add(menu);
		item = menu.add(timeSeriesSelected);
		item.setEnabled(false);
		probeItems.add(item);

		item = menu.add(timeSeriesBarSelected);
		item.setEnabled(false);
		probeItems.add(item);

		item = menu.add(timeSeriesMin);
		item = menu.add(timeSeriesMax);

		menu.addSeparator();

		item = menu.add(new JMenuItem(new AbstractAction("Animate Plot") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 4703396199631792253L;

			public void actionPerformed(ActionEvent e) {
				AnimationPanel panel = new AnimationPanel();
				panel.init(frame.getAxes(), AbstractTilePlot.this);
			}
		}));

		if (this.getClass().equals(TilePlot.class)) {
			JMenu sub = new JMenu("Add Overlay");
			item = sub.add(new JMenuItem(new AbstractAction("Observations") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 2653908993422232092L;

				public void actionPerformed(ActionEvent e) {
					addObsOverlay();
				}
			}));

			item = sub.add(new JMenuItem(new AbstractAction("Vectors") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 235766771298572717L;

				public void actionPerformed(ActionEvent e) {
					addVectorsOverlay();
				}
			}));

			menu.add(sub);
		}

		return bar;
	}

	private void showLayer(String layerKey, boolean show) {
//		MapLayer layer = mapLayers.get(layerKey);
		FeatureLayer layer = mapLayers.get(layerKey);
//		if (show) mapAnnotation.getMapContext().addLayer(layer);	// replaced for GeoTools v10
//		else mapAnnotation.getMapContext().removeLayer(layer);		// replaced for GeoTools v10
		if (show)
			mapAnnotation.getMapContext().addLayer(layer);	// getMapContext() now returns mapContent
		else 
			mapAnnotation.getMapContext().removeLayer(layer);	// getMapContext() now returns mapContent
		mapAnnotation.reset();
		chart.getPlot().notifyListeners(new PlotChangeEvent(chart.getPlot()));
	}

	protected abstract void createSubtitle();

	/**
	 * Gets the MinMax points for this plot.
	 *
	 * @return the MinMax points for this plot.
	 */
	protected abstract DataUtilities.MinMaxPoint getMinMaxPoints();

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

	protected void addObsOverlay() {
	}

	protected void addVectorsOverlay() {
	}

	/**
	 * Updates the color map.
	 *
	 * @param map the new map
	 */
	public void updateColorMap(ColorMap map) {
		this.map = map;
		updateScaleAxis((XYPlot) chart.getPlot());
//		java.util.List annotations = ((XYPlot) chart.getPlot()).getAnnotations();
		List annotations = ((XYPlot) chart.getPlot()).getAnnotations();
		for (Object obj : annotations) {
			if (obj instanceof ObsAnnotation) {
				((ObsAnnotation) obj).updateMap(map);
			}
		}
	}

	/**
	 * Gets the min max values for the current plot.
	 *
	 * @return the min max values for the current plot.
	 */
	protected abstract DataUtilities.MinMax getMinMax();

	// updates the legend scale axis
	protected void updateScaleAxis(XYPlot plot) {
		Logger.debug("in AbstractTilePlot.updateScaleAxis");
		units = anl.verdi.util.VUnits.getFormattedName(frame.getVariable().getUnit());

		XYBlockRenderer renderer;
		if (plot.getRenderer() instanceof XYBlockRenderer) renderer = (XYBlockRenderer) plot.getRenderer();
		else renderer = (XYBlockRenderer) plot.getRenderer(1);

		double min = 0;
		double max = 1;
		if (map == null) {
			min = minMax.getMin();
			max = minMax.getMax();
			Logger.debug("still in updateScaleAxis: ready to call PavePalletCreator");
//			map = new ColorMap(new PavePaletteCreator().createPalettes(8).get(0), min, max);
			map = new ColorMap(new PavePaletteCreator().createPavePalette(), min, max);
			map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
		} else {
			try {
				min = map.getMin();
				max = map.getMax();
			} catch ( Exception e) {
				Logger.error("AbstractTilePlot's updateScaleAxis " + e.getMessage());
			}
			
		}

		LegendAxis scaleAxis;
		try {
			scaleAxis = new LegendAxis(units, min, max, map);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("AbstractFastTile's updateScaleAxis " + e.getMessage());
			return;
		}
		scaleAxis.setTickMarkPaint(Color.BLACK);
		scaleAxis.setTickLabelFont(new Font("Dialog", Font.PLAIN, 9));

		LookupPaintScale paintScale = createPaintScale(map, min, max);
		renderer.setPaintScale(paintScale);

		if (legendIndex == -1) {
			PaintScaleLegend legend = new PaintScaleLegend(scaleAxis.getPaintScale(), scaleAxis);
			legend.setAxisLocation(AxisLocation.BOTTOM_OR_LEFT);
			legend.setAxisOffset(5.0);
			legend.setMargin(new RectangleInsets(5, 5, 5, 5));
			legend.setBorder(new BlockBorder(Color.red));
//TODO ASAP: setBorder is deprecated; says to use org.jfree.chart.block Interface BlockFrame instead but works differently
//			legend.setFrame(BlockFrame(Color.red));
			legend.setPadding(new RectangleInsets(10, 10, 10, 10));
			legend.setStripWidth(10);
			legend.setPosition(RectangleEdge.RIGHT);
			legend.setBackgroundPaint(Color.WHITE);
			chart.addSubtitle(legend);
			for (int i = 0; i < chart.getSubtitleCount(); i++) {
				if (legend == chart.getSubtitle(i)) {
					legendIndex = i;
					break;
				}
			}
		} else {
			PaintScaleLegend legend = (PaintScaleLegend) chart.getSubtitle(legendIndex);
			legend.setScale(scaleAxis.getPaintScale());
			legend.setAxis(scaleAxis);
		}
	}

	// creates the legend scale from the color map
	protected LookupPaintScale createPaintScale(ColorMap map, double min, double max) {
		int colorCount = map.getColorCount();
		LookupPaintScale paintScale = null;
		if (min < max) paintScale = new LookupPaintScale(min, max, Color.GRAY);
		else paintScale = new LookupPaintScale();
		
//		if ( map.getScaleType() == ColorMap.ScaleType.LINEAR) {
//			map.setMinMax(min, max);
//		}
		for (int i = 0; i < colorCount; i++) {
			try {
//				paintScale.add(new Double(map.getIntervalStart(i)), map.getColor(i));
				paintScale.add(map.getIntervalStart(i), map.getColor(i));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
//		if (map.getIntervalType() == ColorMap.IntervalType.AUTOMATIC) { 
//			//ZHAO need to add logarithm 
//			// ZHAO: 
//			double interval = (max - min) / colorCount;
//			if (max == min) {
//				paintScale.add(new Double(min), map.getColor(0));
//			} else {
//				for (int i = 0; i < colorCount; i++) {
//					paintScale.add(new Double(min + (i * interval)), map.getColor(i));
//				}
//			}
//		} else if (map.getIntervalType() == ColorMap.IntervalType.CUSTOM) {
//			// use the values directly from the color map itself
//			for (int i = 0; i < colorCount; i++) {
//				paintScale.add(new Double(map.getIntervalStart(i)), map.getColor(i));
//			}
//		} else if (map.getIntervalType() == ColorMap.IntervalType.LOGARITHM){
//			// TODO JIZHEN
//
//		} else {
//			
//		}

		return paintScale;
	}


	protected class VerdiTileChartPanel extends VerdiChartPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = -3972226173948793513L;
		JFreeChart chart;

		public VerdiTileChartPanel(JFreeChart chart) {
			super(chart);
			this.chart = chart;
		}

		public VerdiTileChartPanel(JFreeChart chart, boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips) {
			super(chart, properties, save, print, zoom, tooltips);
			this.chart = chart;
		}

		public VerdiTileChartPanel(JFreeChart chart, boolean useBuffer) {
			super(chart, useBuffer);
			this.chart = chart;
		}

		public VerdiTileChartPanel(JFreeChart chart, int width, int height, int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth, int maximumDrawHeight, boolean useBuffer, boolean properties, boolean save, boolean print, boolean zoom, boolean tooltips) {
			super(chart, width, height, minimumDrawWidth, minimumDrawHeight, maximumDrawWidth, maximumDrawHeight, useBuffer, properties, save, print, zoom, tooltips);
			this.chart = chart;
		}

		/**
		 * Displays a dialog that allows the user to edit the properties for the
		 * current chart.
		 *
		 * @since 1.0.3
		 */
		public void doEditChartProperties() {
			Logger.debug("in AbstractTilePlot.doEditChartProperties");
			Window window = SwingUtilities.getWindowAncestor(panel);
			ConfigDialog dialog = null;
			if (window instanceof JFrame) 
				dialog = new ConfigDialog((JFrame) window);
			else 
				dialog = new ConfigDialog((JDialog) window);
			dialog.init(AbstractTilePlot.this, getCurrentMinMax());
			dialog.setSize(500, 600);
			dialog.setVisible(true);
		}
	}

	public PlotConfiguration getPlotConfiguration() {
		Logger.debug("in AbstractTilePlot.getPlotConfiguration");
		TilePlotConfiguration config = new TilePlotConfiguration();
		config.setColorMap(map);

		config = (TilePlotConfiguration) getTitlesLabelsConfig(config);

		XYPlot plot = (XYPlot) chart.getPlot();
		PaintScaleLegend legend = (PaintScaleLegend) chart.getSubtitle(legendIndex);
		ValueAxis axis = legend.getAxis();
		config.putObject(PlotConfiguration.UNITS, axis.getLabel());
		config.putObject(PlotConfiguration.UNITS_FONT, axis.getLabelFont());
		config.putObject(PlotConfiguration.UNITS_COLOR, axis.getLabelPaint());
		config.putObject(PlotConfiguration.UNITS_SHOW_TICK, axis.isTickLabelsVisible());
		config.putObject(PlotConfiguration.UNITS_TICK_COLOR, (Color) axis.getTickLabelPaint());
		config.putObject(PlotConfiguration.UNITS_TICK_FONT, axis.getTickLabelFont());
		config.putObject(PlotConfiguration.FOOTER1_SHOW_LINE, true);
		config.putObject(PlotConfiguration.FOOTER2_SHOW_LINE, true);
		config.putObject(PlotConfiguration.OBS_SHOW_LEGEND, false);

		XYBlockRenderer renderer;
		if (plot.getRenderer() instanceof XYBlockRenderer) renderer = (XYBlockRenderer) plot.getRenderer();
		else renderer = (XYBlockRenderer) plot.getRenderer(1);
		config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, renderer.isGridLinesEnabled());
		config.putObject(TilePlotConfiguration.GRID_LINE_COLOR, renderer.getGridLineColor());

//		java.util.List annotations = ((XYPlot) chart.getPlot()).getAnnotations();	// 2014 List no longer ambiguous
		List annotations = ((XYPlot) chart.getPlot()).getAnnotations();
		for (Object obj : annotations) {
			if (obj instanceof ObsAnnotation) {
				ObsAnnotation obs = (ObsAnnotation) obj;
				config.putObject(TilePlotConfiguration.OBS_STROKE_SIZE, obs.getStrokeSize());
				config.putObject(TilePlotConfiguration.OBS_SHAPE_SIZE, obs.getShapeSize());
				break;
			}
		}

		return config;
	}

	protected PlotConfiguration getTitlesLabelsConfig(PlotConfiguration config) {
		Logger.debug("in AbstractTilePlot.getTitlesLabelsConfig");
		config.setShowTitle(chart.getTitle().isVisible() ? "TRUE" : "FALSE");
		config.setTitle(chart.getTitle().getText());
		config.putObject(PlotConfiguration.TITLE_FONT, chart.getTitle().getFont());
		config.putObject(PlotConfiguration.TITLE_COLOR, (Color) chart.getTitle().getPaint());

		TextTitle title = (TextTitle) chart.getSubtitle(subTitle1Index);
		config.setShowSubtitle1(title.isVisible() ? "TRUE" : "FALSE");
		config.setSubtitle1(title.getText());
		config.putObject(PlotConfiguration.SUBTITLE_1_FONT, title.getFont());
		config.putObject(PlotConfiguration.SUBTITLE_1_COLOR, (Color) title.getPaint());

		title = (TextTitle) chart.getSubtitle(subTitle2Index);
		config.setShowSubtitle2(title.isVisible() ? "TRUE" : "FALSE");
		config.setSubtitle2(title.getText());
		config.putObject(PlotConfiguration.SUBTITLE_2_FONT, title.getFont());
		config.putObject(PlotConfiguration.SUBTITLE_2_COLOR, (Color) title.getPaint());

		XYPlot plot = (XYPlot) chart.getPlot();
		ValueAxis axis = plot.getDomainAxis();
		config.putObject(PlotConfiguration.DOMAIN_LABEL, axis.getLabel());
		config.putObject(PlotConfiguration.DOMAIN_FONT, axis.getLabelFont());
		config.putObject(PlotConfiguration.DOMAIN_COLOR, axis.getLabelPaint());
		config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, axis.isTickLabelsVisible());
		config.putObject(PlotConfiguration.DOMAIN_TICK_COLOR, (Color) axis.getTickLabelPaint());
		config.putObject(PlotConfiguration.DOMAIN_TICK_FONT, axis.getTickLabelFont());


		axis = plot.getRangeAxis();
		config.putObject(PlotConfiguration.RANGE_LABEL, axis.getLabel());
		config.putObject(PlotConfiguration.RANGE_FONT, axis.getLabelFont());
		config.putObject(PlotConfiguration.RANGE_COLOR, axis.getLabelPaint());
		config.putObject(PlotConfiguration.RANGE_SHOW_TICK, axis.isTickLabelsVisible());
		config.putObject(PlotConfiguration.RANGE_TICK_COLOR, (Color) axis.getTickLabelPaint());
		config.putObject(PlotConfiguration.RANGE_TICK_FONT, axis.getTickLabelFont());

		return config;
	}


	public int getLayer() {
		Logger.debug("in AbstractTilePlot.getLayer");
		return layer;
	}

	public void setLayer(int layer) {
		Logger.debug("in AbstractTilePlot.setLayer");
		this.layer = layer;
	}
}
