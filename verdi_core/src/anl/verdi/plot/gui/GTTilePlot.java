/** GTTilePlot: GeoTools version of a tile plot
 * developed 2016 based on VERDI TilePlot and FastTilePlot
 * uses new FastTilePlotPanel for GTTilePlot frame layout and to supply GeoTools geographic data support
 */

package anl.verdi.plot.gui;

import gov.epa.emvl.GridCellStatistics;
import gov.epa.emvl.Mapper;
import gov.epa.emvl.TilePlot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import anl.verdi.plot.color.Palette;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.vecmath.Point4i;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.geotools.map.MapContent;
import org.geotools.swing.JMapPane;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import saf.core.ui.event.DockableFrameEvent;
import anl.verdi.core.VerdiApplication;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.DataManager;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.DataUtilities.MinMax;
import anl.verdi.data.Dataset;
import anl.verdi.data.ObsEvaluator;
import anl.verdi.data.VectorEvaluator;
import anl.verdi.formula.Formula;
import anl.verdi.formula.Formula.Type;
import anl.verdi.gis.OverlayObject;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.PlotConfigurationIO;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.probe.PlotEventProducer;
import anl.verdi.plot.types.TimeAnimatablePlot;
import anl.verdi.plot.util.PlotExporter;
import anl.verdi.util.Utilities;

/**
 * @author ellenjo
 *
 */
public class GTTilePlot extends FastTilePlotPanel 
	implements ActionListener, Printable, ChangeListener, ComponentListener, MouseListener, TimeAnimatablePlot, Plot
	{
	private PlotEventProducer eventProducer = new PlotEventProducer();
	protected DataFrame dataFrame;
	protected DataFrame dataFrameLog;
	static final Logger Logger = LogManager.getLogger(GTTilePlot.class.getName());
	private final MapContent myMapContent = new MapContent();
	public static final int NO_VAL = Integer.MIN_VALUE;
	private static final String STATES_LAYER = "STATES";
	private static final String COUNTIES_LAYER = "COUNTIES";
	private static final String WORLD_LAYER = "WORLD";
	private static final String NA_LAYER = "NA";
	private static final String HUCS = "HUCs";
	private static final String RIVERS = "Rivers";
	private static final String ROADS = "Roads";
	private static final String OTHER_MAPS = "Other_Maps";
	public static final String PROPERTIES_COMMAND = "PROPERTIES";
	public static final String SAVE_COMMAND = "SAVE";
	public static final String PRINT_COMMAND = "PRINT";
	public static final String ZOOM_IN_BOTH_COMMAND = "ZOOM_IN_BOTH";
	public static final String ZOOM_OUT_BOTH_COMMAND = "ZOOM_OUT_BOTH";
	public static final String ZOOM_OUT_MAX_COMMAND = "ZOOM_OUT_MAX";
	
	// Attributes
	private static final double MINIMUM_VALID_VALUE = -900.0;

	// Log related
	
	protected boolean log = false;
	private boolean preLog = false;
	private double logBase = 10.0; //Math.E;	
	
	// 2D grid parameters

	protected int startDate; 		// (YYYYDDD).
	protected int startTime; 		// 0 (HHMMSS).
	protected int timestepSize; 	// 10000 (HHMMSS)
	protected int timesteps; 
	protected int layers; 
	protected int rows;
	protected int columns;
	protected int rowOrigin;
	protected int columnOrigin;
	private double westEdge; 		// meters from projection center
	private double southEdge; 	// meters from projection center
	private double cellWidth; 	// meters.
	private double cellHeight; 	// meters.
	private NumberFormat format;
	private boolean invertRows; // HACK: Invert rows of AURAMS / GEM / CF Convention data?

	// For legend-colored grid cells and annotations:

	protected TilePlot tilePlot; // EMVL TilePlot.

	protected int timestep = 0; // 0..timesteps - 1.
	protected int firstTimestep = 0;
	protected int lastTimestep = 0;
	protected int layer = 0; // 0..layers - 1.
	protected int firstLayer = 0;
	protected int lastLayer = 0;
	private int firstRow = 0; // 0..lastRow.
	private int lastRow = 0; // firstRow..rows - 1.
	private int firstColumn = 0; // 0..lastColumn.
	private int lastColumn = 0; // firstColumn..columns - 1.

	protected double[] legendLevels;
	protected MinMax minMax;
	protected PlotConfiguration config;

	protected Palette defaultPalette;
	private Color[] legendColors;
	protected ColorMap map;

	private Color axisColor = Color.darkGray;
	private Color labelColor = Color.black;

	// subsetLayerData[ 1 + lastRow - firstRow ][ 1 + lastColumn - firstColumn ]
	// at current timestep and layer.
	private float[][] subsetLayerData = null;

	// layerData[ rows ][ columns ][ timesteps ]
	private float[][][] layerData = null;
	//private float[][][] layerDataLog = null;
	private float[][][] statisticsData = null;
	//private float[][][] statisticsDataLog = null;
	private CoordinateReferenceSystem gridCRS = null;	// axes -> ReferencedEnvelope -> gridCRS

	// For clipped/projected/clipped map lines:

	private String mapFileDirectory = System.getenv("VERDI_HOME") + "/plugins/bootstrap/data";	// nov 2015

	private Mapper mapper = new Mapper(mapFileDirectory);

	protected List<OverlayObject> obsData = new ArrayList<OverlayObject>();
	protected List<ObsAnnotation> obsAnnotations;
	protected VectorAnnotation vectAnnotation;
	
	// GUI attributes
	
	private boolean recomputeStatistics = false;
	private boolean statError = false;
	private JComboBox statisticsMenu;
	private JTextField threshold;
	private boolean recomputeLegend = false;
	protected JCheckBoxMenuItem showGridLines;
	protected boolean zoom = true;
	private int delay = 50; // animation delay in milliseconds.
	private final int MAXIMUM_DELAY = 3000; // maximum animation delay: 3 seconds per frame.

	protected boolean showLatLon = false;
	protected boolean showObsLegend = false;
	private BufferedImage bImage;
	private JPopupMenu popup;
	
	@SuppressWarnings("unused")
	private Plot.ConfigSoure configSource = Plot.ConfigSoure.GUI;
	VerdiApplication app;

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public GTTilePlot(VerdiApplication app, DataFrame dataFrame) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public JPanel getPanel() {	// return the largest JPanel (contains all others)
		return getEntirePane();
	}

	private JPanel getEntirePane() {
		return (JPanel) this;	// FastTilePlotPanel is-a JPanel
	}

	@Override
	public JMapPane getMapPane() {	// return the container that contains just the geographic component
		return super.getMap();
	}

	@Override
	public JMenuBar getMenuBar() {	// return the container of the menu bar at the top of the overall frame
		return super.getMenuBar();
	}

	/**
	 * Adds the specified PlotListener.
	 * 
	 * @param listener
	 *            the plot listener to add
	 */
	@Override
	public void addPlotListener(PlotListener listener) {
		eventProducer.addListener(listener);
	}

	/**
	 * Removes the specified PlotListener.
	 * 
	 * @param listener
	 *            the plot listener to remove
	 */
	@Override
	public void removePlotListener(PlotListener listener) {
		eventProducer.removeListener(listener);
	}

	/**
	 * Gets the type of the Plot.
	 * 
	 * @return the type of the Plot.
	 */
	@Override
	public Type getType() {
		return Formula.Type.TILE;
	}

	/**
	 * Gets the data that this Plot plots.
	 * 
	 * @return the data that this Plot plots.
	 */
	@Override
	public List<DataFrame> getData() {
		final List<DataFrame> result = new ArrayList<DataFrame>();
		result.add(getDataFrame());
		return result;
	}
	
	protected DataFrame getDataFrame() {
		if ( this.log) {
			return this.dataFrameLog;
		} else {
			return this.dataFrame;
		}
	}
	
	protected DataFrame getDataFrame(boolean log) {
		if (log) {
			return this.dataFrameLog;
		} else {
			return this.dataFrame;
		}
	}


	/**
	 * Exports an image of this Plot to the specified file in the specified format.
	 * 
	 * @param format	the image format. One of PlotExporter.JPG, PlotExporter.TIF,
	 *            PlotExporter.PNG, or PlotExporter.BMP
	 * @param file	the file to save the image.
	 * @param width	width of image in pixels
	 * @param height	height of image in pixels
	 * @throws IOException	if there is an error creating the image
	 */
	@Override
	public void exportImage(String format, File file, int width, int height)
			throws IOException {
		// TODO rewrite exportImage function
		drawBatchImage(width, height);
		PlotExporter exporter = new PlotExporter(this);
		exporter.save(format, file, width, height);
	}
		
	private void drawBatchImage(int width, int height) {
		// TODO Auto-generated method stub
		// TODO rewrite from FastTilePlot.java (uses BufferedImage, offScreenGraphics)
	}


	/**
	 * Configure this Plot according to the specified PlotConfiguration.
	 * 
	 * @param config    the new plot configuration
	 */
	@Override
	public void configure(PlotConfiguration config) {
		// TODO copied from FastTilePlot; may need some rewriting
		String configFile = config.getConfigFileName();
		double[] minmax = { 0.0, 0.0 };

		if (configFile != null) {
			try {
				configure(new PlotConfigurationIO().loadConfiguration(new File(configFile)));
			} catch (IOException ex) {
				Logger.error("IOException in FastTilePlot.configure: loading configuration: " + ex.getMessage());
			}
		}

		ColorMap map = (ColorMap) config.getObject(TilePlotConfiguration.COLOR_MAP);

		if (map != null) {
			
			// set log related info
			ColorMap.ScaleType sType = map.getScaleType();
			this.preLog = this.log;
			if ( sType == ColorMap.ScaleType.LOGARITHM ) {
				this.log = true;
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( true);
					this.tilePlot.setLogBase( (int)map.getLogBase());
				}					
				
				//we need to also populate the non log intervals to some default range...
				//a new map object is created and doesn't keep the interval ranges that was created in one of the constructors
				//so we need to make sure and pre populate just in case user changes between linear and log scales
				computeDataRange(minmax, false);
				map.setMinMax( minmax[0], minmax[1]);

			} else {
				this.log = false;
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( false);
				}				
				//we need to also populate the log intervals to some default range...
				//a new map object is created and doesn't keep the interval ranges that was created in one of the constructors
				//so we need to make sure and pre populate just in case user changes between linear and log scales
				computeDataRange(minmax, true);
				map.setLogMinMax( minmax[0], minmax[1]);
			}
			updateColorMap(map);
			recomputeStatistics = true;
		}

		this.draw();
		this.config = new TilePlotConfiguration(config);
		
		if (this.showGridLines != null) {
			Boolean gridlines = (Boolean)config.getObject(TilePlotConfiguration.SHOW_GRID_LINES);
			this.showGridLines.setSelected(gridlines == null ? false : gridlines);
		}
	}

	@Override
	public void configure(PlotConfiguration config, ConfigSoure source) {
		// TODO copied from FastTilePlot; may need some rewriting
		String configFile = config.getConfigFileName();
		double[] minmax = { 0.0, 0.0 };

		if (configFile != null) {
			try {
				configure(new PlotConfigurationIO().loadConfiguration(new File(configFile)), source);
			} catch (IOException ex) {
				Logger.error("IOException in FastTilePlot.configure: loading configuration: " + ex.getMessage());
			}
		}
		
		this.configSource = source;		// not used, but was not commentedo out in v1.4.1

		ColorMap map = (ColorMap) config.getObject(TilePlotConfiguration.COLOR_MAP);

		if (map != null) {
			
			// set log related info
			ColorMap.ScaleType sType = map.getScaleType();
			this.preLog = this.log;
			if ( sType == ColorMap.ScaleType.LOGARITHM ) {
				this.log = true;
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( true);
					this.tilePlot.setLogBase( (int)map.getLogBase());
				}					
				
				//we need to also populate the non log intervals to some default range...
				//a new map object is created and doesn't keep the interval ranges that was created in one of the constructors
				//so we need to make sure and pre populate just in case user changes between linear and log scales
				computeDataRange(minmax, false);
				map.setMinMax( minmax[0], minmax[1]);

			} else {
				this.log = false;
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( false);
				}				
				//we need to also populate the log intervals to some default range...
				//a new map object is created and doesn't keep the interval ranges that was created in one of the constructors
				//so we need to make sure and pre populate just in case user changes between linear and log scales
				computeDataRange(minmax, true);
				map.setLogMinMax( minmax[0], minmax[1]);
			}
			
			updateColorMap(map);
			recomputeStatistics = true;	
			if ( source == Plot.ConfigSoure.FILE) {
				this.recomputeLegend = true;
			} 
		}

		this.config = new TilePlotConfiguration(config);
		this.draw();
		
		if (this.showGridLines != null) {
			Boolean gridlines = (Boolean)config.getObject(TilePlotConfiguration.SHOW_GRID_LINES);
			this.showGridLines.setSelected(gridlines == null ? false : gridlines);
		}
		
	}


	// Compute data range excluding BADVAL3 values:

	public void computeDataRange(double[] minmax, boolean log) {
		final int selection = statisticsMenu != null ? statisticsMenu.getSelectedIndex() : 0;
		boolean initialized = false;
		minmax[0] = minmax[1] = 0.0;
		if ( selection == 0 ) {
			DataFrame dataFrame = getDataFrame(log);
			final DataFrameIndex dataFrameIndex = 
				dataFrame.getIndex();
	
			for (int timestep = 0; timestep < timesteps; ++timestep) {
				for (int layer = 0; layer < layers; ++layer) {
					for (int row = 0; row < rows; ++row) {
						for (int column = 0; column < columns; ++column) {
							dataFrameIndex.set(timestep, layer, column, row);
							final float value = 
								dataFrame.getFloat(dataFrameIndex);
	
							if (value > MINIMUM_VALID_VALUE) {
	
								if (initialized) {
	
									if (value < minmax[0]) {
										minmax[0] = value;
									} else if (value > minmax[1]) {
										minmax[1] = value;
									}
								} else {
									minmax[0] = minmax[1] = value;
									initialized = true;
								}
							}
						}
					}
				}
			}
		} else {
						
				this.computeStatistics(log);					
			
			final int statistic = selection - 1;

			for ( int row = firstRow; row <= lastRow; ++row ) {

				for ( int column = firstColumn; column <= lastColumn; ++column ) {
					final float value = statisticsData[ statistic ][ row ][ column ];
					
					if (value > MINIMUM_VALID_VALUE) {
						
						if (initialized) {

							if (value < minmax[0]) {
								minmax[0] = value;
							} else if (value > minmax[1]) {
								minmax[1] = value;
							}
						} else {
							minmax[0] = minmax[1] = value;
							initialized = true;
						}
					}
				}
			}
		}
	}


	private void computeStatistics(boolean log) {

		if ( layerData == null ) {
			layerData = new float[ rows ][ columns ][ timesteps ];
			statisticsData = new float[ GridCellStatistics.STATISTICS ][ rows ][ columns ];
		}
			
		// Copy from dataFrame into layerData[ rows ][ columns ][ timesteps ]:

		final DataFrameIndex dataFrameIndex = getDataFrame(log).getIndex();

		for ( int row = 0; row < rows; ++row ) {
			final int dataRow = ! invertRows ? row : rows - 1 - row;

			for ( int column = 0; column < columns; ++column ) {

				for ( int timestep = 0; timestep < timesteps; ++timestep ) {
					dataFrameIndex.set( timestep, layer, column, dataRow );
					float value = this.getDataFrame(log).getFloat( dataFrameIndex ); 
					layerData[ row ][ column ][ timestep ] = value;
				}
			}
		}

		final double threshold = Double.parseDouble( this.threshold.getText() );
		final double hoursPerTimestep = 1.0;
		
		try {
			GridCellStatistics.computeStatistics( layerData,
					threshold, hoursPerTimestep,
					statisticsData, this.statisticsMenu.getSelectedIndex()-1 );
			this.statError = false;
		} catch ( Exception e) {
			Logger.error("Error occurred during computing statistics: " + e.getMessage());
			this.statError = true;
			if ( map != null && map.getScaleType() == ColorMap.ScaleType.LOGARITHM) {
				this.preLog = true;
				this.log = false;
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( false);
				}
				map.setScaleType( ColorMap.ScaleType.LINEAR);
				
				draw();	// TODO Why is this section, including draw(), within a catch block?
			}
		}
	}
	

	private void draw() {
		// TODO Auto-generated method stub; need to rewrite this
		
	}

	private void updateColorMap(ColorMap map) {
		this.map = map;
		
		try {
			minMax = new DataUtilities.MinMax(map.getMin(), map.getMax());
		} catch (Exception e) {
			Logger.error("Exception in FastTilePlot.updateColorMap: " + e.getMessage());
			e.printStackTrace();
			return;
		}
		
		defaultPalette = map.getPalette();
		legendColors = defaultPalette.getColors();
		int count = legendColors.length;
		legendLevels = new double[count + 1];

		for (int i = 0; i < count; i++)
			try {
				legendLevels[i] = map.getIntervalStart(i);
			} catch (Exception e) {
				Logger.error("Exception in FastTilePlot.updateColorMap: " + e.getMessage());
				e.printStackTrace();
			}

		try {
			legendLevels[count] = map.getMax();
		} catch (Exception e) {
			Logger.error("Exception in FastTilePlot.updateColorMap: " + e.getMessage());
			e.printStackTrace();
			Logger.error("FastTilePlot's updateColorMap method "+ e.getMessage());
			return;
		}
	}

	
	/**
	 * Gets this Plot's configuration data.
	 * 
	 * @return this Plot's configuration data.
	 */
	@Override
	public PlotConfiguration getPlotConfiguration() {
		// TODO copied from FastTilePlot; is this OK?
		return tilePlot.getPlotConfiguration();
	}

	/**
	 * Gets a BufferedImage of the plot.
	 * 
	 * @return a BufferedImage of the plot.
	 */
	@Override
	public BufferedImage getBufferedImage() {
		// TODO copied from FastTilePlot; needs rewrite
		return getBufferedImage(getWidth(), getHeight());
	}

	/**
	 * Gets a BufferedImage of the plot.
	 * 
	 * @param width	the width of the image in pixels
	 * @param height	the height of the image in pixels
	 * @return a BufferedImage of the plot.
	 */
	@Override
	public BufferedImage getBufferedImage(int width, int height) {
		// TODO copied from FastTilePlot; needs rewrite
		return bImage;
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateTimeStep(int timestep) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
			throws PrinterException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		
	}


	/**
	 * from FastTilePlot.java; converts 4 ints to a Point4i to a string for display
	 */
	public String createAreaString(int[] point) {
		// TODO rewrite; currently converts int[] to Point4i and then calls formatPointLatLon or formatPoint; will this work?
		Point4i p = new Point4i();
		p.w = point[0];
		p.z = point[1];
		p.x = point[2];
		p.y = point[3];
		if (showLatLon) return formatPointLatLon(p);
		return formatPoint(p);
	}
	
	/**
	 * from FastTilePlot.java; converts a Point4i to a string for display
	 * @param point	coordinate as Point4i
	 * @return	string to display point formatted as a string
	 */
	public String formatPoint(Point4i point) {
		StringBuilder builder = new StringBuilder("(");
		int[] vals = new int[4];
		vals[0] = point.w;
		vals[1] = point.z;
		vals[2] = point.x;
		vals[3] = point.y;
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
	
	/**
	 * from FastTilePlot.java; converts a Point4i to lat/lon for display
	 * @param point	coordinate as Point4i
	 * @return	string to display point as latitude/longitude coordinates
	 */
	private String formatPointLatLon(Point4i point) {
		Point2D llul = getLatLonForAxisPoint(new Point(point.x, point.y));
		StringBuilder builder = new StringBuilder("(");
		double[] vals = new double[4];
		vals[2] = llul.getY();
		vals[3] = llul.getX();
		vals[0] = point.w;
		vals[1] = point.z;
		boolean addComma = false;
		for (int i = 0; i < 4; i++) {
			double val = vals[i];
			if (val != NO_VAL) {
				if (addComma) builder.append(", ");

				if (i == 2) builder.append(Utilities.formatLat(val, 4));
				else if (i == 3) builder.append(Utilities.formatLon(val, 4));
				else builder.append(format.format(val + 1)); //NOTE: to make the notation of timesteps and layers 1-based
				addComma = true;
			}
		}
		builder.append(")");
		return builder.toString();
	}

	/**
	 * from FastTilePlot.java
	 * @param axisPoint
	 * @return
	 */
	protected Point2D getLatLonForAxisPoint(Point axisPoint) {
		//Since the NetCDF boxer used middle of the grid as origin of the grid
		//FastTilePlot use SW corner as the origin of the grid, hence the minus 1
		// TODO may need to change this: GeoTools uses top-left corner of each JPanel as (0,0)
//		return getDataFrame().getAxes().getBoundingBoxer().axisPointToLatLonPoint(axisPoint.x-1, axisPoint.y-1); 
		return getDataFrame().getAxes().getBoundingBoxer().axisPointToLatLonPoint(axisPoint.x, axisPoint.y); //NOTE: the shift has been considered for in the netcdf boxer!!!
	}

	/**
	 * copied from FastTilePlot
	 * @param manager
	 * @param showLegend
	 */
	public void addObservationData(DataManager manager, boolean showLegend) {
		// TODO edit as needed
		showObsLegend = showLegend;
		obsAnnotations = new ArrayList<ObsAnnotation>();
		Axes<DataFrameAxis> axs = getDataFrame().getAxes();
		GregorianCalendar initDate = getDataFrame().getAxes().getDate(timestep);
		List<String> subtitles = new ArrayList<String>();
		String subtitle1 = "";
		boolean showST1 = false;
		
		if (config != null) {
			subtitle1 += (config.getSubtitle1() == null ? "" : config.getSubtitle1()).trim();
			showST1 = !subtitle1.isEmpty();
			if (showST1) subtitles.add(subtitle1);
		}
		
		try {
			for (OverlayObject obs : obsData) {
				ObsEvaluator eval = new ObsEvaluator(manager, obs.getVariable());
				ObsAnnotation ann = new ObsAnnotation(eval, axs, initDate, layer);
				ann.setDrawingParams(obs.getSymbol(), obs.getStrokeSize(), obs.getShapeSize(), map);
				obsAnnotations.add(ann);
				Dataset ds = eval.getVariable().getDataset();
				
				if (showST1 && ds != null) {
					StringBuffer sb = new StringBuffer(ds.getName());
					String alias = ds.getAlias();
					int index = sb.indexOf(alias) + alias.length();
					String temp = sb.replace(index, ++index, "=").toString();
					if (subtitle1.indexOf(temp) < 0 && !subtitles.contains(temp)) 
						subtitles.add(temp);
				}
			}
			
			tilePlot.setObsLegend(obsAnnotations, showLegend);
			config.putObject(PlotConfiguration.OBS_SHOW_LEGEND, showLegend);
			
			if (showST1) {
				Collections.sort(subtitles);
				subtitle1 = "";
			
				for (String str : subtitles)
					subtitle1 += str + "  ";
			
				config.setSubtitle1(subtitle1.trim());
			}
		
			draw();
		} catch (Exception e) {
			setOverlayErrorMsg(e.getMessage());
			Logger.error("Check if overlay time steps match the underlying data");
			Logger.error(e.getMessage());
			// TODO evaluate what drawing activity should take place
//			drawMode = DRAW_NONE;	// no longer using the drawMode etc. for drawing the tile plot
		}
	}

	
	/**
	 * copied from FastTilePlot
	 * @return	observation data
	 */
	public List<OverlayObject> getObservationData() {
		return this.obsData;
	}

	/**
	 * Function to throw up a dialog box to tell the user that the overlay time steps may not match the time steps of the underlying data.
	 * Copied from FastTilePlot
	 * @param msg	The getMessage() exception message caught in the try/catch block
	 */
	private void setOverlayErrorMsg(String msg) {
		if (msg == null) 
			msg = "";
		JOptionPane.showMessageDialog(app.getGui().getFrame(), "Please check if the overlay time steps match the underlying data.\n" + msg, "Overlay Error", JOptionPane.ERROR_MESSAGE, null);
	}

	/**
	 * copied from FastTilePlot
	 * @param eval a VectorEvaluator object
	 */
	public void addVectorAnnotation(VectorEvaluator eval) {
		// TODO check if anything here needs to be changed (expect different method required to draw vectors)
		vectAnnotation = new VectorAnnotation(eval, timestep, getDataFrame().getAxes().getBoundingBoxer());
	}
	
	// GUI Callbacks:

	// Plot frame closed:

	public void stopThread() {	// called by anl.verdi.plot.gui.PlotPanel
//		drawMode = DRAW_END;
		// TODO figure out what stopThread needs to do because not using drawMode
		draw();
	}

	// Window hidden callback:

	@Override
	public void componentHidden(ComponentEvent unused) { }

	// Window shown callback:

	@Override
	public void componentShown(ComponentEvent unused) {
		draw();
	}

	// Window resized callback:

	@Override
	public void componentResized(ComponentEvent unused) {
		draw();
	}

	// Window moved callback:

	@Override
	public void componentMoved(ComponentEvent unused) { }

	// Mouse callbacks:

	protected void showPopup( MouseEvent me ) {
		popup = createPopupMenu(true, true, true, zoom);

		int mod = me.getModifiers();
		int mask = MouseEvent.BUTTON3_MASK;

		if ((mod & mask) != 0) {
			popup.show(this, me.getPoint().x, me.getPoint().y);
		}
	}

	public void mousePressed( MouseEvent unused_ ) { }
	public void mouseEntered( MouseEvent unused_ ) { }
	public void mouseExited( MouseEvent unused_ ) { }
	public void mouseReleased( MouseEvent unused_ ) { }
	public void mouseClicked( MouseEvent unused_ ) { }

	public void viewClosed() { 
		// 
		mapper = null;
		dataFrameLog = null;
		dataFrame = null;
		
		obsData = null;
		obsAnnotations = null;
		vectAnnotation = null;
		eventProducer = null;
		
		bImage = null;
		
		dialog = null;
		controlLayer = null;
		config = null;
		
//		doubleBufferedRendererThread = null;
		
		subsetLayerData = null;
		layerData = null;
		statisticsData = null;	
		
		format = null;
		tilePlot = null;
		legendLevels = null;
		defaultPalette = null;
		legendColors = null;
		map = null;
		gridBounds = null;
		domain = null;

		timeLayerPanel = null;
		probeItems = null;
		popup = null;
		dataArea = null;
		popUpLocation = null;
		probedSlice = null;
		showGridLines = null;
		app = null;
		minMax = null;
	}
	
	public void viewFloated(DockableFrameEvent unused_ ) { }
	public void viewRestored(DockableFrameEvent unused_ ) { }		

	/**
	 * Creates a popup menu for the panel. Copied from FastTilePlot
	 * 
	 * @param properties	include a menu item for the chart property editor.
	 * @param save	include a menu item for saving the chart.
	 * @param print	include a menu item for printing the chart.
	 * @param zoom	include menu items for zooming.
	 * @return The popup menu.
	 */
	protected JPopupMenu createPopupMenu(boolean properties, boolean save,
			boolean print, boolean zoomable) {

		JPopupMenu result = new JPopupMenu("FastTile:");
		boolean separator = false;

		if (properties) {
			JMenuItem propertiesItem = new JMenuItem("Properties...");
			propertiesItem.setActionCommand(PROPERTIES_COMMAND);
			propertiesItem.addActionListener(this);
			result.add(propertiesItem);
			separator = true;
		}

		if (save) {
			if (separator) {
				result.addSeparator();
				separator = false;
			}
			JMenuItem saveItem = new JMenuItem("Save Image As...");
			saveItem.setActionCommand(SAVE_COMMAND);
			saveItem.addActionListener(this);
			result.add(saveItem);
			separator = true;
		}

		if (print) {
			if (separator) {
				result.addSeparator();
				separator = false;
			}
			JMenuItem printItem = new JMenuItem("Print...");
			printItem.setActionCommand(PRINT_COMMAND);
			printItem.addActionListener(this);
			result.add(printItem);
			separator = true;
		}

		if (zoomable) {
			if (separator) {
				result.addSeparator();
				separator = false;
			}

			JMenuItem zoomInItem = new JMenuItem("Zoom_In");
			zoomInItem.setActionCommand(ZOOM_IN_BOTH_COMMAND);
			zoomInItem.addActionListener(this);
			result.add(zoomInItem);

			JMenuItem zoomOutItem = new JMenuItem("Zoom_Out");
			zoomOutItem.setActionCommand(ZOOM_OUT_BOTH_COMMAND);
			zoomOutItem.addActionListener(this);
			result.add(zoomOutItem);
			
			JMenuItem zoomOut2Pic = new JMenuItem("Max_Zoom_Out");
			zoomOut2Pic.setActionCommand(ZOOM_OUT_MAX_COMMAND);
			zoomOut2Pic.addActionListener(this);
			result.add(zoomOut2Pic);
		}

		return result;
	}
	
}
