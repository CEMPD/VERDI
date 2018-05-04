/**
 * MeshPlot - Plot to display unstructured grid data
 * @author Tony Howard
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.gui;

import gov.epa.emvl.ASCIIGridWriter;
import gov.epa.emvl.GridCellStatistics;
import gov.epa.emvl.MPASShapefileWriter;
import gov.epa.emvl.MPASTilePlot;
//import gov.epa.emvl.GridShapefileWriter;		// 2014 disable write shapefile VERDI 1.5.0
import gov.epa.emvl.Mapper;
import gov.epa.emvl.MeshCellStatistics;
import gov.epa.emvl.Projector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
//import java.util.Date;		// functions deprecated, replaced by GregorianCalendar
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.vecmath.Point4i;

import net.sf.epsgraphics.ColorMode;
import net.sf.epsgraphics.Drawable;
import net.sf.epsgraphics.EpsTools;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.Query;
//import org.geotools.data.DefaultQuery;	// deprecated, replacing with Query
import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;	// deprecated
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
//import org.geotools.map.DefaultMapLayer;	// deprecated, replacing with FeatureLayer
import org.geotools.map.FeatureLayer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.swing.JMapPane;
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import saf.core.ui.event.DockableFrameEvent;
import ucar.ma2.ArrayLogFactory;
import ucar.ma2.InvalidRangeException;
import ucar.unidata.geoloc.projection.LatLonProjection;
import anl.map.coordinates.Decidegrees;
import anl.verdi.area.MapPolygon;
import anl.verdi.area.Units;
import anl.verdi.area.target.DepositionRange;
import anl.verdi.area.target.GridInfo;
import anl.verdi.area.target.Target;
import anl.verdi.area.target.TargetDeposition;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiGUI;
import anl.verdi.data.ArrayReader;
import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameBuilder;
import anl.verdi.data.DataLoader;
import anl.verdi.data.DataManager;
import anl.verdi.data.DataReader;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.DataUtilities.MinMax;
import anl.verdi.data.Dataset;
import anl.verdi.data.MPASDataFrameIndex;
import anl.verdi.data.MPASPlotDataFrame;
import anl.verdi.data.MeshCellInfo;
import anl.verdi.data.MeshDataReader;
import anl.verdi.data.ObsEvaluator;
import anl.verdi.data.Slice;
import anl.verdi.data.Variable;
import anl.verdi.data.VectorEvaluator;
import anl.verdi.formula.Formula;
import anl.verdi.gis.FastTileLayerEditor;
import anl.verdi.gis.OverlayObject;
import anl.verdi.plot.anim.AnimationPanel;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.Palette;
import anl.verdi.plot.color.PavePaletteCreator;
import anl.verdi.plot.config.LoadConfiguration;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.PlotConfigurationIO;
import anl.verdi.plot.config.SaveConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.data.IMPASDataset;
import anl.verdi.plot.data.MinMaxInfo;
import anl.verdi.plot.data.MinMaxLevelListener;
import anl.verdi.plot.probe.PlotEventProducer;
import anl.verdi.plot.probe.ProbeEvent;
import anl.verdi.plot.types.TimeAnimatablePlot;
import anl.verdi.plot.util.PlotExporter;
import anl.verdi.plot.util.PlotExporterAction;
import anl.verdi.util.Tools;

import com.vividsolutions.jts.geom.Envelope;

public class MeshPlot extends AbstractPlotPanel implements ActionListener, Printable,
		ChangeListener, MouseListener, MinMaxLevelListener,
		TimeAnimatablePlot, Plot, PopupMenuListener {
	
	//private final MapContent myMapContent = new MapContent();
	static final Logger Logger = LogManager.getLogger(MeshPlot.class.getName());
	private static final long serialVersionUID = 5835232088528761729L;

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

	// Attributes:
	private static final int X = 0;
	private static final int Y = 1;
	private static final int MINIMUM = 0;
	private static final int MAXIMUM = 1;
	private static final int LONGITUDE = 0;
	private static final int LATITUDE = 1;
	
	public static final double RAD_TO_DEG = 180 / Math.PI;
	
	private boolean forceRedraw = false;
	
	// Log related
	
	protected boolean log = false;
	@SuppressWarnings("unused")
	private boolean preLog = false;
	private double logBase = 10.0; //Math.E;	

	// 2D grid parameters:

	protected GregorianCalendar startDate; 
	protected long timestepSize; // ms
	protected int timesteps; // 24.
	protected int layers = 1; // 14.
	protected int rows; // 259.
	protected int cells;
	protected int columns; // 268.
	protected CoordAxis columnAxis;
	protected CoordAxis rowAxis;
	protected DataFrameAxis timeAxis;
	protected String layerAxisName = null;
	protected DataFrameAxis layerAxis;
	protected int rowOrigin;
	protected int columnOrigin;
	private double westEdge; // -420000.0 meters from projection center
	private double southEdge; // -1716000.0 meters from projection center
	private double cellWidth; // 12000.0 meters.
	private NumberFormat format;
	private NumberFormat plotFormat;
	private NumberFormat coordFormat;
	private NumberFormat valueFormat;
	private AreaFinder finder;
	
	IMPASDataset dataset;

	/** Cell id are stored as colors in an image to facilitate a quick mapping of
	 *  pixels to cell id.  Retrieved values must be adjusted by COLOR_BASE to get cell id
	 **/
	private static final int COLOR_BASE = 16777216;

	//Screen width / height in pixels
	private int screenWidth;
	private int screenHeight;
	private int xOffset = 0;
	private int yOffset = 0;
	private int xTranslation = 0;
	
	private int canvasSize = 0;
	// For legend-colored grid cells and annotations:

	protected MPASTilePlot tilePlot; // EMVL TilePlot.

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
	double minLon = 0;
	double minLat = 0;
	double maxLon = 0;
	double maxLat = 0;

	protected double[] legendLevels;
	private Object legendLock = new Object();
	private Object waitObject = new Object();

	protected Palette defaultPalette;
	private Color[] legendColors;
	//private ColorMap map;
	protected ColorMap map;

	private Color axisColor = Color.darkGray;
	private Color labelColor = Color.black;

	// layerData[ rows ][ columns ][ timesteps ]
	private float[][] layerData = null;
	//private float[][][] layerDataLog = null;
	private float[][][] statisticsData = null;
	//private float[][][] statisticsDataLog = null;
	private CoordinateReferenceSystem gridCRS = null;	// axes -> ReferencedEnvelope -> gridCRS
	
	private long renderTime = 200;
	
	private boolean asyncEnabled = true;
	
	private BufferedImage cellIdMap = null;

	// For clipped/projected/clipped map lines:

	private String mapFileDirectory = Mapper.getDefaultMapFileDirectory();

	private //final 
	Mapper mapper = null;

	protected Projector projector = null;

	protected double[][] gridBounds = { { 0.0, 0.0 }, { 0.0, 0.0 } };
	protected double[][] domain = { { 0.0, 0.0 }, { 0.0, 0.0 } };

	private final String variable; // "PM25".
	protected String units; // "ug/m3".
	private String unitString; // "ug/m3".

	private boolean withHucs = false; // Draw watersheds on map?
	private boolean withRivers = false; // Draw rivers on map?
	private boolean withRoads = false; // Draw roads on map?

	protected //final 
	DataFrame dataFrame;
	protected DataFrame dataFrameLog;
	protected DataFrame currentDataFrame;
	protected List<OverlayObject> obsData = new ArrayList<OverlayObject>();
	protected List<ObsAnnotation> obsAnnotations;
	protected VectorAnnotation vectAnnotation;

	// It remains a mystery why eventProducer is needed.
	private PlotEventProducer eventProducer = new PlotEventProducer();

	// GUI attributes:
	private JButton playStopButton;
	private JButton leftStepButton;
	private JButton rightStepButton;
	private final String LEFT = "<";
	private final String RIGHT = ">";
	private final String PLAY = "|>";
	private final String STOP = "||";
	private final String LEFT_TIP = "Move One Timestep to the Left";
	private final String RIGHT_TIP = "Move One Timestep to the Right";
	private final String PLAY_TIP = "Play/Stop";
	private TimeLayerPanel timeLayerPanel;
	private final JToolBar toolBar = new JToolBar();
	@SuppressWarnings("rawtypes")
	private JComboBox statisticsMenu;
	private int preStatIndex = -1;
	private JTextField threshold;
	private boolean recomputeStatistics = false;

	private final int DRAW_NONE = 0;
	private final int DRAW_ONCE = 1;
	private final int DRAW_CONTINUOUS = 2;
	private final int DRAW_END = 3;
	private int drawMode = DRAW_ONCE;
	private int draw_once_requests = -1;
	private final String DELAY_LABEL = "Slow:";
	private JTextField delayField;
	private JTextField firstRowField;
	private JTextField lastRowField;
	private JTextField firstColumnField;
	private JTextField lastColumnField;
	protected final Rubberband rubberband = new Rubberband(this);
	protected boolean zoom = true;
	protected boolean probe = false;
	private boolean hasNoLayer = false;
	private boolean hasNoTime = false;
	private int delay = 50; // In milliseconds.
	private final int MAXIMUM_DELAY = 3000; // 3 seconds per frame.

	protected boolean showObsLegend = false;

	private final JPanel threadParent = this;
	private BufferedImage bImage = null;
	private BufferedImage crossSectionImage = null;
	private BufferedImage rotatedImage = null;
	private int crossXOrigin;
	private int crossYOrigin;
	private boolean reverseAxes = false;
	private double layerHeight = 0;
	private int displayHeight = 0;

	private boolean forceBufferedImage = false;
	private static final Object lock = new Object();
	private JPopupMenu popup;
	protected Rectangle dataArea = new Rectangle();
	private boolean inDataArea = false;
	private Point popUpLocation = new Point(1,1);
	protected Slice probedSlice;
	protected JCheckBoxMenuItem showGridLines;
	protected JCheckBoxMenuItem showWindVectors;
	final JMenu mapLayersMenu = new JMenu("Add Map Layers");
	
	private boolean screenInitted = false;
	
	private int currentClickedCell = 0;
	private int previousClickedCell = 0;
	private MeshCellInfo[] cellsToRender = null;


	//private static final boolean SHOW_ZOOM_LOCATION = true;
	
	private ConfigDialog dialog = null;
	@SuppressWarnings("unused")
	private Plot.ConfigSource configSource = Plot.ConfigSource.GUI;

	VerdiApplication app;
	
	// Create a Thread that contains the above Runnable:

	private Thread doubleBufferedRendererThread = null;
	protected MinMax minMax;
	protected PlotConfiguration config;
	private boolean processTimeChange = true;
//	private MapLayer controlLayer;
	private FeatureLayer controlLayer;
	Graphics2D exportGraphics = null;
	
	MPASDataFrameIndex cellIndex = null;
	MPASDataFrameIndex hoverCellIndex = null;

	private int renderMode = MODE_PLOT;
	
	public static int MODE_PLOT = 1;
	public static int MODE_INTERPOLATION = 3;
	public static int MODE_CROSS_SECTION = 3;
	
	private boolean depositionRangeAlreadySet = false;
	
	protected Action timeSeriesSelected = new AbstractAction(
			"Time Series of Visible Cell(s)") {
		private static final long serialVersionUID = -2940008125642497962L;

		public void actionPerformed(ActionEvent e) {
			requestTimeSeries(dataset.getAllCells(), Formula.Type.TIME_SERIES_LINE);
		}
	};

	protected Action timeSeriesBarSelected = new AbstractAction(
			"Time Series Bar of Visible Cell(s)") {
		private static final long serialVersionUID = 2455217937515200807L;

		public void actionPerformed(ActionEvent e) {
			requestTimeSeries(dataset.getAllCells(), Formula.Type.TIME_SERIES_BAR);
		}
	};

	protected Action timeSeriesMin = new AbstractAction(
			"Time Series of Min. Cell(s)") {
		private static final long serialVersionUID = 5282480503103839989L;

		public void actionPerformed(ActionEvent e) {
			requestTimeSeries(getMinCells(), "Min. cells ");
		}
	};

	protected Action timeSeriesMax = new AbstractAction(
			"Time Series of Max. Cell(s)") {
		private static final long serialVersionUID = -4465758432397962782L;

		public void actionPerformed(ActionEvent e) {
			requestTimeSeries(getMaxCells(), "Max. cells ");
		}
	};
	
	String sTitle1 = null;
	String sTitle2 = null;
	Font tFont = null;
	Font sFont1 = null;
	Font sFont2 = null;
	int fontSize =0;
	
	private void updateConfigVariables() {
		sTitle1 = config.getSubtitle1();
		sTitle2 = config.getSubtitle2();
		tFont = config.getFont(PlotConfiguration.TITLE_FONT);
		sFont1 = config.getFont(PlotConfiguration.SUBTITLE_1_FONT);
		sFont2 = config.getFont(PlotConfiguration.SUBTITLE_2_FONT);
		fontSize = (tFont == null) ? 20 : tFont.getSize();
		yOffset = 20 + fontSize;
		if (sTitle1 != null && !sTitle1.trim().isEmpty()) {
			fontSize = (sFont1 == null) ? 20 : sFont1.getSize();
			yOffset += fontSize + 6;
		}
		if (sTitle2 != null && !sTitle2.trim().isEmpty()) {
			fontSize = (sFont2 == null) ? 20 : sFont2.getSize();

			if (sTitle1 == null || sTitle1.trim().isEmpty()) {
				yOffset += 26;
			}

			yOffset += fontSize + 6;
		}
		xOffset = 100;
	}

	// Declare a Runnable attribute which will create and run a thread
	// whose run method draws double-buffered to a graphics iff graphics
	// is not null:

	public Runnable doubleBufferedRenderer = new Runnable() {

		private final RepaintManager repaintManager =
			RepaintManager.currentManager(threadParent);
		
		public void run() {
			try {

			updateConfigVariables();
			int prevLegendBoxWidth = 0;
			int legendBoxWidth = 0;
			
			do {
				VerdiGUI.unlock();
				
				boolean iconified = VerdiGUI.isIconified();
				boolean hidden = VerdiGUI.isHidden((Plot)threadParent);

				if ( drawMode != DRAW_NONE && drawMode != DRAW_END &&
						! hidden) {
					 //! VerdiGUI.isHidden( (Plot) threadParent ) ) {
					VerdiGUI.lock();
					if (VerdiGUI.isHidden( (Plot) threadParent ))
						continue;
					
					if (drawMode == DRAW_ONCE) {
//						synchronized (lock) {
							if (getDrawOnceRequests() > 0) {
								draw_once_requests = 0;
							}
							/*if ( get_draw_once_requests() >=0 ) {
								showBusyCursor();
							}*/
					}
					
					// When animating, pause based on user-set delay rate:

					if (drawMode == DRAW_CONTINUOUS && delay != 0) {
						try {
							Thread.sleep(delay);
						} catch (Exception unused) {}
					}

					final int canvasWidth = getWidth();
					final int canvasHeight = getHeight();
										
					int width;
					int height;
					
					xTranslation = 0;
					prevLegendBoxWidth = legendBoxWidth;
					legendBoxWidth = tilePlot.getLegendBoxWidth();
					if (prevLegendBoxWidth != legendBoxWidth) {
						//This changes as large data sets are being incrementally loaded and the values with differing string width
						//are used in the legend.  When that happens, force recalculation for the new width.
						screenInitted = false;
					}
					if (clippedDataRatio > 1) {
						width = Math.round(canvasWidth - (tilePlot.getLegendBoxWidth() + xOffset));
						height = (int)Math.round(width / clippedDataRatio);
						int minHeight = canvasHeight - tilePlot.getFooterHeight() * 2;
						if (minHeight < height) {
							height = minHeight;
							int fullWidth = width;
							width = (int)Math.round(height * clippedDataRatio);
							xTranslation = (fullWidth - width) / 2;
						}
					}
					else {
						height = Math.round(canvasHeight - tilePlot.getFooterHeight() * 2);
						width = (int)Math.round(height * clippedDataRatio);
						int fullWidth = Math.round(canvasWidth - (tilePlot.getLegendBoxWidth() + xOffset));
						xTranslation = (fullWidth - width) / 2;
					}
					canvasSize = width;					

					if (canvasSize < 1) {
						/*
						if ( get_draw_once_requests() < 0) 
							restoreCursor();
							*/
						continue;
					}

					// Use off-screen graphics for double-buffering:
					// don't start processing until graphics system is ready!

					Image offScreenImage = null;
					
					try {
					offScreenImage =
						repaintManager.getOffscreenBuffer(threadParent, canvasWidth, canvasHeight);
					} catch (NullPointerException e) {}

					// offScreenImage = (Image) (offScreenImage.clone());

					if (offScreenImage == null) {
						/*
						if ( get_draw_once_requests() < 0) 
							restoreCursor();
							*/
						continue;// graphics system is not ready
					}

					final Graphics2D offScreenGraphics = exportGraphics == null ? (Graphics2D)offScreenImage.getGraphics() : exportGraphics;

					if (offScreenGraphics == null) {
						/*
						if ( get_draw_once_requests() < 0) 
							restoreCursor();
							*/
						continue;// graphics system is not ready
					}


					if (tFont == null) {
						tFont = offScreenGraphics.getFont();
						tFont = new Font(tFont.getFontName(), Font.BOLD, tFont.getSize() * 2);
						config.putObject(PlotConfiguration.TITLE_FONT, tFont);
						fontSize = (tFont == null) ? 20 : tFont.getSize();
						yOffset = 20 + fontSize;
					}
					if (sFont1 == null) {
						config.putObject(PlotConfiguration.SUBTITLE_1_FONT, offScreenGraphics.getFont());
						sFont1 = config.getFont(PlotConfiguration.SUBTITLE_1_FONT);
						if (sTitle1 != null && !sTitle1.trim().isEmpty()) {
							fontSize = (sFont1 == null) ? 20 : sFont1.getSize();
							yOffset += fontSize + 6;
						}
					}
					if (sFont2 == null) {
						config.putObject(PlotConfiguration.SUBTITLE_2_FONT, offScreenGraphics.getFont());
						sFont2 = config.getFont(PlotConfiguration.SUBTITLE_2_FONT);
						if (sTitle2 != null && !sTitle2.trim().isEmpty()) {
							fontSize = (sFont2 == null) ? 20 : sFont2.getSize();

							if (sTitle1 == null || sTitle1.trim().isEmpty()) {
								yOffset += 26;
							}

							yOffset += fontSize + 6;
						}
					}

					boolean windChanged = locChanged || dataChanged;
					if (locChanged) {
						transformCells(/*gr,*/ canvasSize, xOffset, yOffset);	
						previousClippedDataRatio = clippedDataRatio;
						previousPanX = panX;
						previousPanY = panY;
						// on pan or zoom (locChanged), recalculate dimensions, visibility
						locChanged = false;
					}
					if (dataChanged) {
						// on layer or timestep change (dataChanged), recaluclate color
						updateCellData();
						dataChanged = false;
					}
					if (windChanged && renderWind)
						updateWindData();


					final Graphics graphics = threadParent.getGraphics();

					if (graphics == null) {
						/*
						if ( get_draw_once_requests() < 0) 
							restoreCursor();
							*/
						continue;// graphics system is not ready
					}

					// graphics system should now be ready
					assert offScreenImage != null;
					assert offScreenGraphics != null;
					assert graphics != null;

					if (drawMode == DRAW_CONTINUOUS) {
						dataChanged = true;
						timestep = nextValue(1, timestep, firstTimestep, lastTimestep);
						timeLayerPanel.setTime(timestep);
						drawOverLays();
					}
					
//					synchronized (lock) {
						if (getDrawOnceRequests() > 0) {
							draw_once_requests = 0;
							/*
							if ( get_draw_once_requests() < 0) 
								restoreCursor();
								*/
							continue;
						}
//					}

					copySubsetLayerData(log); // Based on current timestep and layer.

					
					synchronized (lock) {
						
						// Erase canvas:

						offScreenGraphics.setColor(Color.white);
						offScreenGraphics.fillRect(0, 0, canvasWidth,
								canvasHeight);						

						// Draw legend-colored grid cells, axis, text labels and
						// legend:

						final Boolean showGridLines = (Boolean)
							config.getObject( TilePlotConfiguration.SHOW_GRID_LINES );
						final Color gridLineColor = (Color)
							( ( showGridLines == null || showGridLines == false ) ? null
								: config.getObject( TilePlotConfiguration.GRID_LINE_COLOR ) );

						final int stepsLapsed = timestep - (firstTimestep >= 0 ? firstTimestep : 0);
						final int statisticsSelection = statisticsMenu.getSelectedIndex();
						Logger.debug("statisticsSelection = " + statisticsSelection);
						final String statisticsUnits =
							statisticsSelection == 0 ? null : GridCellStatistics.units( statisticsSelection - 1 );
						Logger.debug("statisticsUnits = " + statisticsUnits);
						final String plotVariable =
							statisticsSelection == 0 ? variable
							: variable + GridCellStatistics.shortName( statisticsSelection - 1 );
						final String plotUnits =
							statisticsUnits != null ? statisticsUnits : units;
						Logger.debug("units = " + units);
						
						if (getDrawOnceRequests() > 0) {
							draw_once_requests = 0;
							//System.err.println("Resetting 3 draw once requests");
							/*
							if ( get_draw_once_requests() < 0) 
								restoreCursor();
								*/
							continue;
						}
						
						try {
							Logger.debug("ready to call tilePlot.draw(); first resolve any function calls for argument values, thread = " + Thread.currentThread().toString());
							int aRow = firstRow + rowOrigin;
							Logger.debug("aRow = " + aRow);
							int bRow = lastRow + rowOrigin;
							Logger.debug("bRow = " + bRow);
							int aCol = firstColumn + columnOrigin;
							Logger.debug("aCol = " + aCol);
							int bCol = lastColumn + columnOrigin;
							Logger.debug("bCol = " + bCol);
							int aLegendLevelsLength = legendLevels.length;
							Logger.debug("LegendLevels.length = " + aLegendLevelsLength);
							int aLegendColorsLength = legendColors.length;
							Logger.debug("LegendColors.length = " + aLegendColorsLength);
							Logger.debug("plotUnits = " + plotUnits);
							String aPlotUnits = (plotUnits==null || plotUnits.trim().equals(""))?"none":plotUnits;
							Logger.debug("aPlotUnits = " + aPlotUnits);
							NumberFormat aNumberFormat = map.getNumberFormat();
							Logger.debug("aNumberFormat = " + aNumberFormat);
							Logger.debug("ready to make revised function call to tilePlot.draw, thread = " + Thread.currentThread().toString());

							//debug
							
							Font defaultFont = offScreenGraphics.getFont();
							tilePlot.setUseStats(preStatIndex > 0);
							if (xTranslation != 0) {
								offScreenGraphics.translate(xTranslation,  0);
							}
							/*if (zoomFactor == 1)
								tilePlot.setRenderVars(xTranslation, coordFormat, layerMinMaxCache[layer]);
							else*/
								tilePlot.setRenderVars(xTranslation, coordFormat, currentMinMaxCache);
							tilePlot.draw(offScreenGraphics, xOffset, yOffset,
									screenWidth, screenHeight, stepsLapsed, MeshPlot.this.layer, aRow,
									bRow, aCol, bCol, null, legendLevels,
									legendColors, axisColor, labelColor, plotVariable,
									aPlotUnits, 
									config, aNumberFormat, gridLineColor,
									null);
							//Cells are sized incorrectly during the first redraw, partially due to tilePlot.getLegendBoxWidth() being wrong before
							//tilePlot.drawLegend() first called.  Quick workaround is to draw twice.
							if (!screenInitted) {
								screenInitted = true;
								locChanged = true;
								continue;
							}
							
							offScreenGraphics.setFont(defaultFont);
							offScreenGraphics.setColor(axisColor);
							tilePlot.drawAxis(offScreenGraphics, xOffset, xOffset + screenWidth, yOffset, yOffset + screenHeight, panX * RAD_TO_DEG + columnOrigin, visibleDataWidth * RAD_TO_DEG,
									panY * RAD_TO_DEG + rowOrigin, visibleDataHeight * RAD_TO_DEG);


//							tilePlot.draw(offScreenGraphics, xOffset, yOffset,
//									width, height, stepsLapsed, layer, firstRow + rowOrigin,
//									lastRow + rowOrigin, firstColumn + columnOrigin, lastColumn + columnOrigin, legendLevels,
//									legendColors, axisColor, labelColor, plotVariable,
//									((plotUnits==null || plotUnits.trim().equals(""))?"none":plotUnits), config, map.getNumberFormat(), gridLineColor,
//									subsetLayerData);
						} catch (Exception e) {
							Logger.error("MeshPlot run method", e);
						}

						if (renderMode == MODE_PLOT || currentView == GRID)
							renderCells(offScreenGraphics, xOffset, yOffset, true);
						
						if (renderMode == MODE_INTERPOLATION) {
							mapPolygon.draw(tilePlot, domain, gridBounds, gridCRS, null, legendLevels,
									legendColors,offScreenGraphics, dataset.getAllCellsArray(), renderReader ,units,firstColumn,firstRow,
									xOffset, yOffset, width, height,currentView, isShowSelectedOnly());
						}

						dataArea.setRect(xOffset + xTranslation, yOffset, screenWidth, screenHeight);
						

						// Draw projected/clipped map border lines over grid
						// cells:

						if (getDrawOnceRequests() > 0) {
							draw_once_requests = 0;
							/*
							if ( get_draw_once_requests() < 0)
								restoreCursor();
								*/
							continue;
						}
						
						mapper.draw(domain, gridBounds, gridCRS,
								offScreenGraphics, xOffset, yOffset, width,
								height, withHucs, withRivers, withRoads);

						if (obsAnnotations != null) {
							for (ObsAnnotation ann : obsAnnotations)
								ann.draw(offScreenGraphics, xOffset, yOffset, width, height, 
										legendLevels, legendColors, gridCRS, domain, gridBounds);
						}
						
						if (vectAnnotation != null) {
							vectAnnotation.draw(offScreenGraphics, xOffset, yOffset, width, height, 
									firstRow, lastRow, firstColumn, lastColumn);
						}

						resetMenuItems(mapLayersMenu);
						Toolkit.getDefaultToolkit().sync();

						try {
							if (canvasWidth > 0 && canvasHeight > 0) {
								//bImage needed for animated gif support
								if (forceBufferedImage) {
									BufferedImage copiedImage = toBufferedImage(offScreenImage, BufferedImage.TYPE_INT_RGB, canvasWidth, canvasHeight);
									bImage = copiedImage;
									if (animationHandler != null) {
										ActionEvent e = new ActionEvent(copiedImage, this.hashCode(), "");
										animationHandler.actionPerformed(e);
									}
									else
										forceBufferedImage = false;
								}
								VerdiGUI.showIfVisible(threadParent, graphics, offScreenImage);
							}
						} finally {
							graphics.dispose();
							offScreenGraphics.dispose();
						}

						Toolkit.getDefaultToolkit().sync();
					
					} // End of synchronized block.
					if (drawMode == DRAW_ONCE ) {
						decreaseDrawOnceRequests();
						draw_once_requests = -1;
						if (getDrawOnceRequests() < 0 && animationHandler == null) {
							drawMode = DRAW_NONE;
							//restoreCursor();
						}
					} else {
						//drawMode = DRAW_NONE;
					}
					VerdiGUI.unlock();
					
				} else {
					//paint() gets called before windowDeiconified(), so if we're iconified, sleep long enough to see if 
					//windowDeiconified gets called later
					if (iconified && drawMode != DRAW_NONE && drawMode != DRAW_END) {
						try {
							Thread.sleep(50); /* ms. */
							//We were in the process of being deiconified, try to paint again
							if (!VerdiGUI.isHidden((Plot)threadParent))
								continue;
						} catch (Exception unused) {}
					}
					try {
						if (!forceRedraw) {
							synchronized (waitObject) {
								waitObject.wait();
							}
						}
						if (forceRedraw) {
							drawMode = DRAW_ONCE;
						}
						forceRedraw = false;
					} catch (Exception unused) {}
				}
			} while (drawMode != DRAW_END);
		} catch (Throwable t) {
			VerdiGUI.unlock();
			restoreCursor();
			if (currentDataFrame != null) { //Ignore errors if dataFrame is null - that means window is closing
				Logger.error("Error rendering MeshPlot", t);
				String errInfo = t.getClass().getName();
				if (t.getMessage() != null && !t.getMessage().equals(""))
					errInfo += ": " + t.getMessage();										
				if (app != null)
					JOptionPane.showMessageDialog(app.getGui().getFrame(), "An error occured while rendering the plot:\n" + errInfo + "\nPlease see the log for more details.", "Error", JOptionPane.ERROR_MESSAGE);
				try {
					app.getGui().getViewManager().getDockable(viewId).close();
				} catch (Throwable tr) {}
			}
		}
		}
	};
	
	private double getCurrentWidthDeg() {
		return visibleDataWidth * RAD_TO_DEG;
	}
	
	private double getCurrentLonMaxDeg() {
		return (panX + visibleDataWidth) * RAD_TO_DEG + columnOrigin;
	}
	
	private double getCurrentLonMinDeg() {
		return panX * RAD_TO_DEG + columnOrigin;
	}
	
	private double getCurrentLatMaxDeg() {
		return (dataHeight - panY) * RAD_TO_DEG + rowOrigin;
	}
	
	private double getCurrentLatMinDeg() {
		return (dataHeight - panY - visibleDataHeight) * RAD_TO_DEG + rowOrigin;
	}
	
//	tilePlot.drawAxis(offScreenGraphics, xOffset, xOffset + screenWidth, yOffset, yOffset + screenHeight, panX * RAD_TO_DEG + columnOrigin, visibleDataWidth * RAD_TO_DEG,
//	panY * RAD_TO_DEG + rowOrigin, visibleDataHeight * RAD_TO_DEG, rowLabels, columnLabels);

	//firstRow = (int)Math.round((dataHeight - panY - visibleDataHeight) * RAD_TO_DEG);
	//lastRow = (int)Math.round((dataHeight - panY) * RAD_TO_DEG);
	//firstColumn = (int)Math.round(panX * RAD_TO_DEG);
	//lastColumn = (int)Math.round((panX + visibleDataWidth) * RAD_TO_DEG);
	
	private BufferedImage toBufferedImage(Image image, int type, int width, int height) {
        BufferedImage result = new BufferedImage(width, height, type);
        Graphics2D g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }

	public void drawBatchImage(int wdth, int hght) {
			final int canvasWidth = wdth;
			final int canvasHeight = hght;
			// around plot window.

			String sTitle1 = config.getSubtitle1();
			String sTitle2 = config.getSubtitle2();
			Font tFont = config.getFont(PlotConfiguration.TITLE_FONT);
			Font sFont1 = config.getFont(PlotConfiguration.SUBTITLE_1_FONT);
			Font sFont2 = config.getFont(PlotConfiguration.SUBTITLE_2_FONT);
			int fontSize = (tFont == null) ? 20 : tFont.getSize();
			int yOffset = 20 + fontSize;

			if (sTitle1 != null && !sTitle1.trim().isEmpty()) {
				fontSize = (sFont1 == null) ? 20 : sFont1.getSize();
				yOffset += fontSize + 6;
			}

			if (sTitle2 != null && !sTitle2.trim().isEmpty()) {
				fontSize = (sFont2 == null) ? 20 : sFont2.getSize();

				if (sTitle1 == null || sTitle1.trim().isEmpty()) {
					yOffset += 26;
				}

				yOffset += fontSize + 6;
			}

			final int xOffset = 100;

			final Image offScreenImage = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
			final Graphics offScreenGraphics = offScreenImage.getGraphics();

			// graphics system should now be ready
			assert offScreenImage != null;
			assert offScreenGraphics != null;

			copySubsetLayerData(this.log); // Based on current timestep and layer.

				offScreenGraphics.setColor(Color.white);
				offScreenGraphics.fillRect(0, 0, canvasWidth, canvasHeight);

				// Draw legend-colored grid cells, axis, text labels and legend:

				final Boolean showGridLines = (Boolean)
					config.getObject( TilePlotConfiguration.SHOW_GRID_LINES );
				final Color gridLineColor = (Color)
					( ( showGridLines == null || showGridLines == false ) ? null
						: config.getObject( TilePlotConfiguration.GRID_LINE_COLOR ) );
				
				final int statisticsSelection = statisticsMenu.getSelectedIndex();
				final String statisticsUnits =
					statisticsSelection == 0 ? null : GridCellStatistics.units( statisticsSelection - 1 );
				Logger.debug("statisticsUnit = " + statisticsUnits);
				final String plotVariable =
					statisticsSelection == 0 ? variable
					: variable + GridCellStatistics.shortName( statisticsSelection - 1 );
				Logger.debug("plotVariable = " + plotVariable);
				final String plotUnits = statisticsUnits != null ? statisticsUnits : units;
				Logger.debug("plotUnits = " + plotUnits);
				final int stepsLapsed = timestep - (firstTimestep >= 0 ? firstTimestep : 0);
				try {
					tilePlot.drawBatchImage(offScreenGraphics, xOffset, yOffset,
							canvasWidth, canvasHeight, stepsLapsed, layer, firstRow,
							lastRow, firstColumn, lastColumn, legendLevels,
							legendColors, axisColor, labelColor, plotVariable,
							((plotUnits==null || plotUnits.trim().equals(""))?"none":plotUnits), config, map.getNumberFormat(), gridLineColor,
							null);
				} catch (Exception e) {
					Logger.error("FastTilePlot's drawBatch method" + e.getMessage());
					e.printStackTrace();
				}
				
				dataArea.setRect(xOffset, yOffset, tilePlot.getPlotWidth(), tilePlot.getPlotHeight());

				// Draw projected/clipped map border lines over grid cells:

				mapper.draw(domain, gridBounds, gridCRS,
						offScreenGraphics, xOffset, yOffset, tilePlot.getPlotWidth(),
						tilePlot.getPlotHeight(), withHucs, withRivers, withRoads);

				try {
					bImage = (BufferedImage) offScreenImage;
				} finally {
					offScreenGraphics.dispose();
				}
	}
	


	// Methods --------------------------------------------------------

	// GUI Callbacks:

	// Plot frame closed:

	public void stopThread() {
		drawMode = DRAW_END;
		waitObject.notifyAll();
		draw();
	}
	
	public void markDirty() {
		screenInitted = false;
		locChanged = true;
		draw();
	}


	protected void showPopup( MouseEvent me ) {
		popupShown = true;
		rubberband.setActive(false);

		int mod = me.getModifiers();
		int mask = MouseEvent.BUTTON3_MASK;

		if ((mod & mask) != 0) {
			popup.show(this, me.getPoint().x, me.getPoint().y);
		}
	}
	
	// Mouse callbacks:

	public void mousePressed( MouseEvent unused_ ) { }
	public void mouseEntered( MouseEvent unused_ ) { }
	public void mouseExited( MouseEvent unused_ ) { }
	public void mouseReleased( MouseEvent unused_ ) { }
	public void mouseClicked( MouseEvent unused_ ) { }

	public void viewClosed() {
		super.viewClosed();
		stopThread();
		try {
			Thread.sleep(500);
		} catch( Exception e) {
		}
		
		mapper.dispose();
		mapper = null;
		dataFrameLog = null;
		dataFrame = null;
		cellIndex = null;
		hoverCellIndex = null;
		
		obsData = null;
		obsAnnotations = null;
		vectAnnotation = null;
		eventProducer = null;
		
		bImage = null;
		
		dialog = null;
		
		controlLayer = null;
		
		config = null;
		
		doubleBufferedRendererThread = null;
		
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

		timeLayerPanel.close();
		timeLayerPanel = null;
		popup = null;
		dataArea = null;
		popUpLocation = null;
		probedSlice = null;
		showGridLines = null;
		dialog = null;
		app = null;
		doubleBufferedRenderer = null;
		doubleBufferedRendererThread = null;
		minMax = null;
		config = null;
		controlLayer = null;
		cellsToRender = null;
		
		renderVariable = null;
		
		cellInfo = null;
		splitCellInfo = null;
		dataset = null;
		finder = null;
		animationHandler = null;
		
		loadConfig.close();
		saveConfig.close();
		rubberband.close();
		
	}
	public void viewFloated(DockableFrameEvent unused_ ) { }
	public void viewRestored(DockableFrameEvent unused_ ) { }		

	public void draw() {
		/*StackTraceElement elem = Thread.currentThread().getStackTrace()[2];
		System.out.println("MeshPlot draw: " + elem.getFileName() + " " + elem.getMethodName() + ": " + elem.getLineNumber());*/
		if (drawMode == DRAW_NONE) {
			drawMode = DRAW_ONCE;
		}
		
		if (drawMode == DRAW_ONCE) {
			increaseDrawOnceRequests();
		}
		synchronized (waitObject) {
			waitObject.notifyAll();
		}
	}
	
	public void forceDraw() {
		forceRedraw = true;
		draw();
	}

	// Buttons and fields:

	public void actionPerformed(ActionEvent event) {
		final Object source = event.getSource();

		if ( source == statisticsMenu || source == threshold ) {
			
			if ( statisticsMenu.getSelectedIndex() != this.preStatIndex) {
				if( statisticsMenu.getSelectedIndex() != 0) {
					recomputeStatistics = true;
				} else {
					//reset to no statistics...
					copySubsetLayerData(this.log);
				}
				this.preStatIndex = statisticsMenu.getSelectedIndex();
			}
			
			updateLegendLevels();
			
	    } else if (source == playStopButton) {

			if (playStopButton.getText().equals(PLAY)) {
				processTimeChange = false;
				playStopButton.setText(STOP);
				leftStepButton.setEnabled(false);
				rightStepButton.setEnabled(false);
				drawMode = DRAW_CONTINUOUS;
				int holdDelay = delay;
						try {
							delay = Integer.parseInt(delayField.getText().toString());
						} catch (Exception unused) { }

							if (delay < 0) {
								delay = holdDelay;
								delayField.setText(Integer.toString(delay));
							} else if (delay > MAXIMUM_DELAY) {
								delay = MAXIMUM_DELAY;
								delayField.setText(Integer.toString(delay));
							}
			} else {
				processTimeChange = true;
				playStopButton.setText(PLAY);
				drawMode = DRAW_NONE;
				leftStepButton.setEnabled(true);
				rightStepButton.setEnabled(true);
				try {
					Thread.sleep(100);
				} catch (Exception unused) {}
				
				timeLayerPanel.setTime(timestep);
			}
		} else if (source == leftStepButton) {
			doStep(-1);
		} else if (source == rightStepButton) {
			doStep(1);
		} else if (source instanceof JTextField) {
			final JTextField field = (JTextField) source;
			int value = 0;

			try {
				value = Integer.parseInt(field.getText().toString());
			} catch (Exception unused) {
				value = -1;
			}

			if (source == delayField) {

				if (value < 0) {
					value = 0;
					delayField.setText(Integer.toString(value));
				} else if (value > MAXIMUM_DELAY) {
					value = MAXIMUM_DELAY;
					delayField.setText(Integer.toString(value));
				}

				delay = value;
			} 
		}

		String command = event.getActionCommand();
		Rectangle rect = new Rectangle(new Point(getCol(popUpLocation), getRow(popUpLocation)), new Dimension(0, 0));

		if (command.equals(PROPERTIES_COMMAND)) {
			editChartProperties();
		} else if (command.equals(SAVE_COMMAND)) {
			try {
				PlotExporterAction save = new PlotExporterAction(this);
				save.actionPerformed(event);
			} catch (Exception e) {
				e.printStackTrace();
			}
		/*} else if (command.equals(PRINT_COMMAND)) {
			FastTilePlotPrintAction print = new FastTilePlotPrintAction(this);
			print.actionPerformed(event);*/
		} else if (command.equals(ZOOM_IN_BOTH_COMMAND) && inDataArea) {
			zoom(false, false, true, false, false, rect);
		} else if (command.equals(ZOOM_OUT_BOTH_COMMAND) && inDataArea) {
			zoom(false, false, false, false, true, rect);
		} else if (command.equals(ZOOM_OUT_MAX_COMMAND)) {
			resetZooming();
		}

		draw();
	}

	public void resetRowsNColumns(int fRow, int lRow, int fColumn, int lColumn) {
		if (fRow < 1)
			fRow = 1;

		if (fRow > rows)
			fRow = rows;

		firstRow = fRow - 1;

		if (lRow < 1)
			lRow = 1;

		if (lRow > rows)
			lRow = rows;

		lastRow = lRow - 1;

		if (firstRow > lastRow) {
			int temp = firstRow;
			firstRow = lastRow;
			lastRow = temp;
		}

		if (fColumn < 1)
			fColumn = 1;

		if (fColumn > columns)
			fColumn = columns;

		firstColumn = fColumn - 1;

		if (lColumn < 1)
			lColumn = 1;

		if (lColumn > columns)
			lColumn = columns;

		lastColumn = lColumn - 1;

		if (firstColumn > lastColumn) {
			int temp = firstColumn;
			firstColumn = lastColumn;
			lastColumn = temp;
		}

		//computeDerivedAttributes();
	}
	
	public void processMouseMotionEvent(MouseEvent me) {
		if(!isInDataArea(me)){
			app.getGui().setStatusTwoText("");
		}
		
		super.processMouseMotionEvent(me);
	}

	// Mouse-based zoom callback:

	public void processMouseEvent(MouseEvent me) {
		inDataArea = isInDataArea(me);
		int mod = me.getModifiers();
		int mask = MouseEvent.BUTTON3_MASK;
		
		if ((mod & mask) != 0) {
			showPopup(me);
			popUpLocation = me.getPoint();
			return;
		}
		
		super.processMouseEvent(me);
	}
	
	protected boolean isInDataArea(MouseEvent me) {
		int x = me.getPoint().x;
		int y = me.getPoint().y;
		
		if (x < dataArea.x || x > dataArea.x + dataArea.width)
			return false;
		
		if (y < dataArea.y || y > dataArea.y + dataArea.height)
			return false;
		
		return true;
	}
	
	protected int getRow(Point p) {
		int dist =  (p.y - dataArea.y);
		return dist;
		/*
		int div = dist * (lastRow - firstRow + 1);
		int den = dataArea.height;
		
		return firstRow +  div/den;*/
	}
	
	protected int getCol(Point p) {
		int dist = p.x - dataArea.x;
		return dist;
		/*
		int div = dist * (lastColumn - firstColumn + 1);
		int den = dataArea.width;
		
		return firstColumn +  div/den;*/
	}
	
	protected double getXDistance(Point p) {
		int dist = p.x - dataArea.x;
		double xCoord = (dist / compositeFactor + panX) * RAD_TO_DEG;
		
		return xCoord;
	}
	
	protected double getYDistance(Point p) {
		int dist = p.y - dataArea.y;
		double yCoord = (latMax - dist / compositeFactor + panY) * RAD_TO_DEG;
		
		return yCoord;
	}
	
	private void zoom(boolean rightClick, boolean leftClick, boolean popZoomIn, boolean reset, boolean zoomOut, Rectangle bounds) {
		if (reset) {
			resetZooming();
			return;
		}
		
		if (rightClick || ((bounds.width == 0 || bounds.height == 0) && !popZoomIn && !zoomOut))
			return;
		

		boolean popZoom = true;
		
		boolean setLatLonRange = false;
		if (previousPanX == -1) {
			//previousPanX/Y was set already, ignore
			//also there was no clicked cell, so don't try to set it
			setLatLonRange = true;
		}
		else {
			previousPanX = panX;
			previousPanY = panY;
		}
		dataChanged = true;
		
		if (popZoomIn) { // click to zoom in or popup menu zoom in
			if (zoomFactor < MAX_ZOOM) {
				locChanged = true;
				//zoomFactor += 0.5;
				zoomFactor *= 4/3.0;
			}
		} else if (zoomOut) {  //zoom out
			if (zoomFactor > MIN_ZOOM) {
				locChanged = true;
				//zoomFactor -= 0.5;
				zoomFactor /= 4/3.0;
				if (zoomFactor < MIN_ZOOM)
					zoomFactor = MIN_ZOOM;
			} else {
				locChanged = true;
				zoomFactor = MIN_ZOOM;
				
				if (clippedDataRatio < dataRatio) {
					previousClippedDataRatio = clippedDataRatio;
					clippedDataRatio *= 1.25;
					if (clippedDataRatio > dataRatio)
						clippedDataRatio = dataRatio;
					screenWidth = (int)Math.round(screenHeight * clippedDataRatio);
				}
				else if (clippedDataRatio >= dataRatio) {
					previousClippedDataRatio = clippedDataRatio;
					clippedDataRatio /= 1.25;
					if (clippedDataRatio < dataRatio)
						clippedDataRatio = dataRatio;
					screenHeight = (int)Math.round(screenWidth / clippedDataRatio);
				}
				
			}
		} else if (leftClick && bounds.height == 0 && bounds.width == 0) {
			return;
		} else { // regular zoom in
			previousClippedDataRatio = clippedDataRatio;
			clippedDataRatio = bounds.width / (double)bounds.height;
			panX = bounds.x / compositeFactor + panX;
			panY = bounds.y / compositeFactor + panY;
			locChanged = true;
			zoomFactor *= screenWidth / (double)bounds.width;
			popZoom = false;
		}
		
		if (clippedDataRatio > previousClippedDataRatio) {		
			screenHeight = (int)Math.round(screenWidth / clippedDataRatio);
		}
		else {
			screenWidth = (int)Math.round(screenHeight * clippedDataRatio);
		}
		
		double zoomedFactor = screenWidth / dataWidth * zoomFactor;
		visibleDataWidth = screenWidth / zoomedFactor;
				
		if (!setLatLonRange)
			setCellClicked(getCellIdByCoord(bounds.x, bounds.y));
		
		double xClickDistance = bounds.x / compositeFactor + panX;
		if (popZoom)
			panX = xClickDistance - visibleDataWidth / 2;
		if (panX < 0)
			panX = 0;
		if (panX > dataWidth - visibleDataWidth)
			panX = dataWidth - visibleDataWidth;
		
		double yClickDistance = bounds.y / compositeFactor + panY;
		visibleDataHeight = screenHeight / zoomedFactor;  //half visible height
		if (popZoom)
			panY = yClickDistance - visibleDataHeight / 2;
		if (panY < 0)
			panY = 0;
		if (panY > dataHeight - visibleDataHeight)
			panY = dataHeight - visibleDataHeight;
				
		minLat = (dataHeight - panY - visibleDataHeight) * RAD_TO_DEG;
		firstRow = (int)Math.round(minLat);
		maxLat = (dataHeight - panY) * RAD_TO_DEG;
		lastRow = (int)Math.round(maxLat);
		minLon = panX * RAD_TO_DEG;
		firstColumn = (int)Math.round(minLon);
		maxLon = (panX + visibleDataWidth) * RAD_TO_DEG;
		lastColumn = (int)Math.round(maxLon);
		minLat += rowOrigin;
		maxLat += rowOrigin;
		minLon += columnOrigin;
		maxLon += columnOrigin;

		//computeDerivedAttributes();
		draw();
	}
	
	private void setCellClicked(int cellId) {
		previousClickedCell = currentClickedCell;
		currentClickedCell = cellId;
		LocalCellInfo cell = null;
		cell = getCellInfo(previousClickedCell);
		if (cell != null)
			cell.cellClicked = false;
		
		cell = getCellInfo(currentClickedCell);
		if (cell != null)
			cell.cellClicked = true;
	}

	private void doStep(int steps) {
		final int newValue = nextValue(steps, timestep, firstTimestep, lastTimestep);
		setTimestep(newValue); // Calls draw().
		timeLayerPanel.setTime(newValue);
	}

	// Sliders:

	public void stateChanged(ChangeEvent event) {
		final Object source = event.getSource();
		if (source instanceof TimeLayerPanel) {
			final TimeLayerPanel timelayer = (TimeLayerPanel) source;
			final int tValue = timelayer.getTime();
			final int lValue = timelayer.getLayer();

			if (tValue > 0) {
				final int newValue = nextValue(tValue - timestep, timestep, firstTimestep, lastTimestep);
				timeLayerPanel.setTime(newValue);
				setTimestep(newValue); // Calls draw().
			} else if (lValue > 0) {
				final int newValue = nextValue(lValue - layer, layer, firstLayer, lastLayer);
				timeLayerPanel.setLayer(newValue);
				setLayer(newValue); // Calls draw().
			}
		}
	}
	
	private void redrawTimeLayer() {
		final int tValue = timeLayerPanel.getTime();
		final int lValue = timeLayerPanel.getLayer();

		if (tValue >= firstTimestep) {
			setTimestep(tValue); // Calls draw().
		} 
		
        if (lValue >= firstLayer) {
			setLayer(lValue); // Calls draw().
		}
	}

	// Helper.

	private int nextValue(int delta, int current, int min, int max) {
		int result = current + delta;

		if (result < min) {
			result = max - (min - result) + 1;
		} else if (result > max) {
			result = min + (result - max) - 1;
		}

		return result;
	}

	public void setTimestep(int timestep) {
		if (timestep >= firstTimestep && timestep <= lastTimestep && timestep != this.timestep) {
			dataChanged = true;
			this.timestep = timestep;
			copySubsetLayerData(this.log);
			forceDraw();
			drawOverLays();
		}
	}

	public void setLayer(int layer) {
		if (layer >= firstLayer && layer <= lastLayer && layer != this.layer) {
			dataChanged = true;
			this.layer = layer;
			final int selection = statisticsMenu.getSelectedIndex();

			if ( selection > 0 ) {
				recomputeStatistics = true;
			}

			copySubsetLayerData(this.log);
			draw();
		}
	}
	
	private static byte indexOfObsValue(float value, final double[] values) {
		if (Float.isNaN(value))
			return -1;
		
		if (value <= DataUtilities.BADVAL3 || value <= DataUtilities.AMISS3 || value >= DataUtilities.NC_FILL_FLOAT) 	// 2014 changed AMISS3 comparison from == to <=
			return -1;

		final byte count = (byte)values.length;

		if (values[0] == values[values.length - 1])
			return 0;

		for (byte index = 1; index < count; index++) {
			if (values[index] > value)
				return (byte)(index - 1);
		}

		return (byte)(count - 2);
	}
	
	public LocalCellInfo getCellInfo(int id) {
		if (id >= 0 && id < cellInfo.length)
			return cellInfo[id];
		return null;
	}
	
	public class LocalCellInfo {
		boolean visible;
		boolean cellClicked;
		int[] lonTransformed;
		int[] latTransformed;
		byte colorIndex;
		
		int[] windStart = new int[2];
		int[] windEnd = new int[2];
		double scaledDiameter;
		double windAngle;
		double windVelocity;
		
		MeshCellInfo source;
		
		public LocalCellInfo(MeshCellInfo source) {
			this.source = source;
			lonTransformed = new int[source.getNumVertices()];
			latTransformed = new int[source.getNumVertices()];
		}
		
		public double getValue() {
			return source.getValue(currentVariable, currentDataFrame, cellIndex, timestep - firstTimestep, layer - firstLayer);
		}
		
		public int getId() {
			return source.getId();
		}
		
		private void transformCell(double xFactor, double yFactor, int xOffset, int yOffset) {
			for (int i = 0; i < source.getNumVertices(); ++i) {
				lonTransformed[i] = (int)Math.round((source.getLonRad(i) - lonMin - panX) * xFactor) + xOffset;
				latTransformed[i] = (int)Math.round((source.getLatRad(i) * -1 - latMin - panY) * yFactor) + yOffset;
			}
			visible = false;
		}
		
		public void calculateWindVector() {
			//TODO - cache scaledDiameter after transformCell if needed
			scaledDiameter = source.getMaxLat() - source.getMinLat();
			double d2 = source.getMaxLon() - source.getMinLon();
			if (d2 < scaledDiameter)
				scaledDiameter = d2;
			scaledDiameter *= compositeFactor;
			
			//TODO - cache zonal, meridional, windAngle, windVolicty after updateCellData if needed
			double zonal = source.getZonal(layer, timestep);
			double meridional = source.getMeridional(layer,  timestep);		
			windVelocity = Math.sqrt(zonal * zonal + meridional * meridional);
			
			if (zonal == 0) {
				windAngle = 90;
			} else if (meridional == 0) {
				windAngle = 0;
			} else {
				if (zonal > 0 && meridional >0)
					windAngle = Math.toDegrees(Math.atan(meridional / zonal));
				else if (zonal < 0 && meridional > 0)
					windAngle = 90 - Math.toDegrees(Math.atan(zonal / meridional)); //90 -
				else if (zonal < 0 && meridional < 0)
					windAngle = 180 + Math.toDegrees(Math.atan(meridional / zonal)); //180 +
				else //zonal > 0 && meridional < 0
					windAngle =  270 - Math.toDegrees(Math.atan(zonal / meridional)); // 270 -
			}
			
			//TODO - calculate after updateCellData
			double length = .75 * scaledDiameter * .5;
			double windScale = length / windVelocity;

			windStart[0] = (int)Math.round((source.getLon() / RAD_TO_DEG - lonMin - panX) * compositeFactor) + xOffset;
			windStart[1] = (int)Math.round((source.getLat() / RAD_TO_DEG * -1 - latMin - panY) * compositeFactor) + yOffset;
			windEnd[0] = (int)Math.round(windScale * zonal) + windStart[0];
			windEnd[1] = (int)Math.round(windScale * -meridional) + windStart[1];
		}
		
		public MeshCellInfo getSource() {
			return source;
		}
		
	}
	
	/*public class CellInfo implements MeshCellInfo { 
		double[] latCoords;
		double[] lonCoords;
		int[] latTransformed;
		int[] lonTransformed;
		boolean visible;
		int cellId;
		int colorIndex;
		double lon = 0;
		double lat = 0;
		int minX;
		int minY;
		int maxX;
		int maxY;
		boolean cellClicked = false;
		
		private CellInfo(int id, int numVertices) {
			latCoords = new double[numVertices];
			lonCoords = new double[numVertices];
			latTransformed = new int[numVertices];
			lonTransformed = new int[numVertices];
			cellId = id;
			cellIdInfoMap.put(id,  this);
		}
		
		public void setLat(double val) {
			lat = val;
		}
		
		public void setLon(double val) {
			lon = val;
		}
		
		public int getNumVertices() {
			return latCoords.length;
		}
		
		public double getLon() {
			return lon;
		}
		
		public double getLon(int index) {
			return lonCoords[index] * RAD_TO_DEG;
		}
		
		public double getLat() {
			return lat;
		}

		public double getLat(int index) {
			return latCoords[index] * RAD_TO_DEG;
		}
		
		public int getId() {
			return cellId;
		}

		public String getElevation() {
			String e = null;
			if (layerAxis != null) {
				if (layerAxis.getName().equals(VAR_ELEVATION)) {
					double h1 = elevation.get(cellId, MeshPlot.this.layer);
					double h2 = elevation.get(cellId, MeshPlot.this.layer + 1);
					e = Long.toString(Math.round((h2 - h1) / 2 + h1));
				}
				if (layerAxis.getName().equals(VAR_DEPTH) && depth != null) {
					double h1 = Math.round(depth.get(MeshPlot.this.timestep, cellId, MeshPlot.this.layer));
					double h2 = Math.round(depth.get(MeshPlot.this.timestep, cellId, MeshPlot.this.layer + 1));
					e = Long.toString(Math.round((h2 - h1) / 2 + h1));

				}
				if (e != null)
					return ", " + e + "m";
			}
			return "";
		}
		
		public double getValue() {
			if (timeAxis == null) {
				if (layerAxis != null)
					return renderVariable.get(cellId, MeshPlot.this.layer - firstLayer);
				return renderVariable.get(cellId);
			}
			else if (renderVariable.getRank() == 3)
				return renderVariable.get(MeshPlot.this.timestep - firstTimestep, cellId, MeshPlot.this.layer - firstLayer);
			else
				return renderVariable.get(MeshPlot.this.timestep - firstTimestep, cellId);
		}
		
		public double getMinX() {
			return getLon(minX);
		}
		
		public double getMaxX() {
			return getLon(maxX);
		}
		
		public double getMinY() {
			return getLat(minY);
		}
		
		public double getMaxY() {
			return getLat(maxY);
		}
		
		public double getValue(int time, int level) {
			if (time < 0)
				time = MeshPlot.this.timestep;
			if (level < 0)
				level = MeshPlot.this.layer;
			if (timeAxis == null)
				return renderVariable.get(cellId, level);
			else if (renderVariable.getRank() == 3)
				return renderVariable.get(time, cellId, level);
			else
				return renderVariable.get(time, cellId);

		}
		
		private void transformCell(double factor, int xOffset, int yOffset) {
			for (int i = 0; i < lonCoords.length; ++i) {
				lonTransformed[i] = (int)Math.round((lonCoords[i] - lonMin - panX) * factor) + xOffset;
				latTransformed[i] = (int)Math.round((latCoords[i] * -1 - latMin - panY) * factor) + yOffset;
			}
			visible = false;
		}
		
		public void calculateCellBounds() {
			for (int j = 0; j < latCoords.length; ++j) {
				if (lonCoords[j] < lonCoords[minX])
					minX = j;
				if (lonCoords[j] > lonCoords[maxX])
					maxX = j;
				if (latCoords[j] < latCoords[minY])
					minY = j;
				if (latCoords[j] > latCoords[maxY])
					maxY = j;
			}	
		}
		
		public CellInfo split(int index) {			
			CellInfo clone = new CellInfo(cellId, lonCoords.length);
			clone.latCoords = Arrays.copyOf(latCoords, latCoords.length);
			clone.lonCoords = Arrays.copyOf(lonCoords, lonCoords.length);
			
			for (int j = 0; j < lonCoords.length; ++j) {
				if (lonCoords[j] < 0) {
					lonCoords[j] = Math.PI;
				}
				else if (clone.lonCoords[j] > 0)
					clone.lonCoords[j] = Math.PI * -1;
			}
			
			calculateCellBounds();
			clone.calculateCellBounds();
			
			clone.lat = lat;
			
			lon = (lonCoords[maxX] + lonCoords[minX]) / 2;
			clone.lon = (clone.lonCoords[clone.maxX] + clone.lonCoords[clone.minX]) / 2;
			
			
			splitCells.put(clone, index);
			
			//System.out.println("Split cell " + cell.cellId + " negative: " + negativeCell);
			//System.out.println("longitudes " + Arrays.toString(cell.lonCoords));
			//System.out.println("latitudes " + Arrays.toString(cell.latCoords));
			//System.out.println("longitudes " + Arrays.toString(cellHalf.lonCoords));
			//System.out.println("latitudes " + Arrays.toString(cellHalf.latCoords));

			return clone;
		}
	}*/
	
	
	ArrayReader renderVariable = null;
	ArrayReader logRenderVariable = null;
	ArrayReader currentVariable = null;
	
	LoadConfiguration loadConfig = null;
	SaveConfiguration saveConfig = null;
	
	ucar.ma2.ArrayInt.D2 vertexList;
	double latMin = Double.POSITIVE_INFINITY;
	double lonMin = Double.POSITIVE_INFINITY;
	double latMax = Double.NEGATIVE_INFINITY;
	double lonMax = Double.NEGATIVE_INFINITY;
	boolean locChanged = true;
	boolean dataChanged = true;
	double panX = 0;
	double panY = 0;
	double previousPanX = 0;
	double previousPanY = 0;
	final double MAX_ZOOM = Double.POSITIVE_INFINITY;
	final double MIN_ZOOM = 1.0;
	double zoomFactor = 1;
	double compositeFactor;
	double dataWidth = 0;
	double dataHeight = 0;
	double visibleDataWidth = 0;
	double visibleDataHeight = 0;
	double dataRatio = 0;
	double clippedDataRatio = 0;
	double previousClippedDataRatio = 0;
	double clippedScreenWidth = 0;
	double clippedScreenHeight = 0;
	double avgCellDiam = 0;
	boolean renderBorder = true; //preseverd at the moment but replaced with Show Cell Border menu item
	double transformedCellDiam = 0;
	double borderDisplayCutoff = 0.02; //Map colors become hidden by cell borders if borders are drawn
										//when cells don't take up enough pixels.  Only allow borders
										//if cell width / screen width is above this percentage;
	Axes<CoordAxis> mpasAxes;
	LocalCellInfo[] cellInfo = null;
	Map<LocalCellInfo, Integer> splitCellInfo = null;
	
	private void getCellStructure() throws IOException {

		visibleDataWidth = dataset.getDataWidth();
		visibleDataHeight = dataset.getDataHeight();

		cellsToRender = dataset.getCellsToRender();
		cellInfo = new LocalCellInfo[cellsToRender.length];
		Map<MeshCellInfo, Integer> splitCells = dataset.getSplitCells();
		splitCellInfo = new HashMap<LocalCellInfo, Integer>();
		renderVariable = ArrayReader.getReader(dataFrame.getArray());
		
		if (this.log) {
			currentVariable = logRenderVariable;
			currentDataFrame = dataFrameLog;
		}
		else {
			currentVariable = renderVariable;
			currentDataFrame = dataFrame;
		}
		dataRatio = dataset.getDataRatio();
		previousClippedDataRatio = clippedDataRatio;
		clippedDataRatio = dataRatio;
		dataWidth = dataset.getDataWidth();
		dataHeight = dataset.getDataHeight();
		lonMin = dataset.getLonMin();
		lonMax = dataset.getLonMax();
		latMin = dataset.getLatMin();
		latMax = dataset.getLatMax();
		locChanged = true;
		avgCellDiam = dataset.getAvgCellDiam();
		for (MeshCellInfo cell : dataset.getCellsToRender()) {
			cellInfo[cell.getId()] = new LocalCellInfo(cell);
		}
		for (MeshCellInfo cell : splitCells.keySet()) {
			splitCellInfo.put(new LocalCellInfo(cell),  cell.getId());
		}
	}
	
	private void transformCells(/*Graphics gr, */ int canvasSize, int xOrigin, int yOrigin) {
		transformCells(canvasSize, xOrigin, yOrigin, 1, 1);
	}
	
	private void transformCells(/*Graphics gr, */ int canvasSize, int xOrigin, int yOrigin, double xScale, double yScale) {
		long start = System.currentTimeMillis();
		

		
		if (reverseAxes) {
			compositeFactor = zoomFactor;
		} else {
			screenWidth = canvasSize;
			screenHeight = (int)Math.round(screenWidth / clippedDataRatio);
			compositeFactor = screenWidth / dataWidth * zoomFactor;
		}
		
		transformedCellDiam = (int)Math.round(avgCellDiam * compositeFactor);
		renderBorder = transformedCellDiam / screenWidth > borderDisplayCutoff;

		double xFactor = compositeFactor * xScale;
		double yFactor = compositeFactor * yScale;
		for (int i = 0; i < cellsToRender.length; ++i) {
			getCellInfo(i).transformCell(xFactor, yFactor, xOrigin, yOrigin);
		}
		
		for (LocalCellInfo cell : splitCellInfo.keySet()) {
			cell.transformCell(xFactor, yFactor, xOrigin, yOrigin);
		}
		
		gridBounds[X][MINIMUM] = westEdge + panX * RAD_TO_DEG;
		gridBounds[X][MAXIMUM] = westEdge + (panX + visibleDataWidth) * RAD_TO_DEG;
		gridBounds[Y][MINIMUM] = southEdge + (dataHeight - panY - visibleDataHeight) * RAD_TO_DEG;
		gridBounds[Y][MAXIMUM] = southEdge + (dataHeight - panY) * RAD_TO_DEG;

		domain[LONGITUDE][MINIMUM] = gridBounds[X][MINIMUM];
		domain[LONGITUDE][MAXIMUM] = gridBounds[X][MAXIMUM];
		domain[LATITUDE][MINIMUM] = gridBounds[Y][MINIMUM];
		domain[LATITUDE][MAXIMUM] = gridBounds[Y][MAXIMUM];
		
		if (screenWidth > 0) {
			cellIdMap = new BufferedImage(screenWidth + 1, screenHeight + 1, BufferedImage.TYPE_INT_RGB);
			java.awt.Graphics2D g = cellIdMap.createGraphics();
	        g.setColor(new Color(COLOR_BASE * -1 - 1));
	        g.fillRect(0,  0,  screenWidth+1, screenHeight + 1);
	        g.translate(xOrigin * -1,  yOrigin * -1);
	        forceHideBorders = true;
	        renderCells(g, 0, 0);
	        forceHideBorders = false;
	       /* File outputfile = new File("/tmp/data.bmp");
	        try {
				ImageIO.write(cellIdMap,  "bmp", outputfile);
			} catch (IOException e) {
				e.printStackTrace();
			}*/
	        findVisibleCells();
		}
        
		Logger.info("Scaled cells in " + (System.currentTimeMillis() - start) + "ms");
	}
	
	//Prevents overwriting cell id values when drawing to id map
	boolean forceHideBorders = false;
	
	boolean renderWind = false;
	
	public void updateCellData() {
		long start = System.currentTimeMillis();
		updateCellColors();
		calculateVisibleMinMax();
		Logger.info("Updated cell data in " + (System.currentTimeMillis() - start) + "ms");

	}
	
	public void updateWindData() {
		long start = System.currentTimeMillis();
		for (int i = 0; i < cellsToRender.length; ++i) {
			LocalCellInfo cell = getCellInfo(i);
			cell.calculateWindVector();
		}
		
		for (LocalCellInfo cell : splitCellInfo.keySet()) {
			cell.calculateWindVector();
		}
		Logger.info("Updated wind data in " + (System.currentTimeMillis() - start) + "ms");

	}
	
	public void updateCellColors() {
		long start = System.currentTimeMillis();
		synchronized (legendLock) {
		for (int i = 0; i < cellsToRender.length; ++i) {
			LocalCellInfo cell = getCellInfo(i);
			cell.colorIndex = indexOfObsValue((float)cell.getValue(), legendLevels);
		}
		
		for (LocalCellInfo cell : splitCellInfo.keySet()) {
			cell.colorIndex = indexOfObsValue((float)cell.getValue(), legendLevels);
		}
		}
		Logger.info("Updated cell data in " + (System.currentTimeMillis() - start) + "ms");

	}
	
	public void renderCells(Graphics2D gr, int xOffset, int yOffset) {
		renderCells(gr, xOffset, yOffset, false);
	}
	
	public void renderCells(Graphics2D gr, int xOffset, int yOffset, boolean visibleOnly) {
		
		long renderStart = System.currentTimeMillis();
		
		if (xOffset != 0) {
			if (!reverseAxes)
				gr.setClip(xOffset, yOffset, screenWidth, screenHeight);
		}
		
		final Boolean showGridLines = (Boolean)
				config.getObject( TilePlotConfiguration.SHOW_GRID_LINES );
		final boolean showCellBorder = showGridLines != null && showGridLines.booleanValue() && !forceHideBorders;

		synchronized (legendLock) {
			for (int i = 0; i < cells; ++i) { //for each cell
				LocalCellInfo cell = getCellInfo(i);
				if (visibleOnly && !cell.visible && i != 0)
					continue;
				renderCell(gr, xOffset, yOffset, cell, showCellBorder, i);
			}
			for (LocalCellInfo cell : splitCellInfo.keySet()) {
				if (visibleOnly && !cell.visible)
					continue;
				renderCell(gr, xOffset, yOffset, cell, showCellBorder, cell.getId());
			}
		}
		
		gr.setClip(null);
		renderTime = System.currentTimeMillis() - renderStart;
		
		Logger.info("Finished drawing image in " + renderTime + "ms");
		//System.out.println("Var min " + varMin + " max " + varMax);
		/*
		java.io.File outputFile = new java.io.File("/tmp/mpasout.png");
		try {
			boolean res = javax.imageio.ImageIO.write(img, "png", outputFile);
			System.out.println("Image written: " + res + " " + new Date());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		gr.drawImage(img.getScaledInstance(windowWidth, -1, Image.SCALE_FAST), 0, 0, null);
		*/
		
	}
	
	private void renderCell(Graphics2D gr, int xOffset, int yOffset, LocalCellInfo cell, boolean showCellBorder, int index) {
		if (cell.colorIndex == -1)
			return;

		/**
		 * 0 xOffset means this is the image used to map coordinates to cell IDs, not the actual screen.
		 * Store the cell ID as the image's color.
		 */
		if (xOffset == 0)
			gr.setColor(new Color(cell.getId() + 1));
		/*else if (SHOW_ZOOM_LOCATION && cell.cellClicked) {
			gr.setColor(Color.BLACK);
			System.err.println("Rendering clicked cell location " + cell.lon + ", " + cell.lat + " id " + cell.cellId);
		}*/
		else if (preStatIndex < 1)
			gr.setColor(legendColors[cell.colorIndex]);
		else {
			gr.setColor(legendColors[indexOfObsValue(statisticsData[preStatIndex - 1][0][cell.getId()], legendLevels)]);
		}

		gr.fillPolygon(cell.lonTransformed, cell.latTransformed, cell.lonTransformed.length);
		if (showCellBorder) {
			gr.setColor(Color.BLACK);
			int length = cell.lonTransformed.length - 1;
			for (int j = 0; j < length; ++j) {
				gr.drawLine(cell.lonTransformed[j], cell.latTransformed[j],
						cell.lonTransformed[j + 1], cell.latTransformed[j + 1]);
			}
			gr.drawLine(cell.lonTransformed[length],  cell.latTransformed[length], 
					cell.lonTransformed[0], cell.latTransformed[0]);
		}
		
		if (renderWind) {
			gr.setColor(Color.BLACK);			
			AffineTransform trx = gr.getTransform();
			//Render arrow line
			gr.drawLine(cell.windStart[0],  cell.windStart[1],  cell.windEnd[0], cell.windEnd[1]);
			int arrowLength = (int)Math.round(cell.scaledDiameter * .5 * .2);
			//Render arrow head
			gr.translate(cell.windEnd[0], cell.windEnd[1]);
			gr.rotate(Math.toRadians(45 - cell.windAngle));
			gr.fillPolygon(new int[] { 0, 0, -arrowLength}, new int[] { 0, arrowLength, 0}, 3 );
			gr.setTransform(trx);
		}
	}
	
	public void exportShapeFile(String filename) {
		try {
			MPASShapefileWriter.write(filename, 
					(double)firstColumn + columnOrigin,
					(double)lastColumn + columnOrigin,
					(double)firstRow + rowOrigin,
					(double)lastRow + rowOrigin,
					variable, currentVariable,
					currentDataFrame,
					timestep,
					layer,
					cellsToRender);
		} catch (IOException e) {
			e.printStackTrace();
		}


	}
	
	// Construct but do not draw yet.

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public MeshPlot(VerdiApplication app, DataFrame dataFrame, int mode) {
		super(true);
		this.app=app;
		if (app == null)
			asyncEnabled = false;
		setDoubleBuffered(true);
		assert dataFrame != null;
		this.dataFrame = dataFrame;
		this.calculateDataFrameLog();
		cellIndex = new MPASDataFrameIndex(dataFrame);
		hoverCellIndex = new MPASDataFrameIndex(dataFrame);
		hasNoLayer = (dataFrame.getAxes().getZAxis() == null);
		format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(4);
		
		renderMode = mode;
		
		plotFormat = NumberFormat.getInstance();
		
		coordFormat = NumberFormat.getInstance();
		coordFormat.setMaximumFractionDigits(3);

		finder = new AreaFinder();
		this.addMouseListener(finder);
		this.addMouseMotionListener(finder);
		// Initialize attributes from dataFrame argument:
		
		final Variable dataFrameVariable = dataFrame.getVariable();
		variable = dataFrameVariable.getName();
		Logger.debug("dataFrameVariable = " + dataFrameVariable);
		Logger.debug("dataFrameVariable name = " + variable);
		units = dataFrameVariable.getUnit().toString();
		Logger.debug("units of dataFrameVariable = " + units);
		if ( units==null || units.trim().equals("")) {
			units = "none";
			unitString = "";
		}
		else if (units.equals("-"))
			unitString = "";
		else
			unitString = " " + units;
		Logger.debug("now units = " + units);
		File vFile = new File(mapFileDirectory);
		if (!vFile.exists() || !vFile.canRead() || !vFile.isDirectory()) {
			vFile = JFileDataStoreChooser.showOpenFile("shp", null);
			if(!vFile.exists() || !vFile.canRead() || !vFile.isDirectory()) {
				Logger.error("incorrect map file directory: " + vFile.getAbsolutePath());
				//TODO - make them choose a good one, or throw exception, also in FastTilePlot
				return;
			}
			mapFileDirectory = vFile.getAbsolutePath();
		}
		
		assert dataFrame.getAxes() != null;
		final Axes<DataFrameAxis> axes = dataFrame.getAxes();

		// Create cartographic projector (used by mapper.draw):

		dataset = (IMPASDataset)dataFrame.getDataset().get(0);
		final Axes<CoordAxis> coordinateAxes = dataset.getCoordAxes();
		mpasAxes = coordinateAxes;

		
		/*final Projection projection = coordinateAxes.getProjection();

		if (projection instanceof LatLonProjection) {
			projector = null;
		} else {
			projector = new Projector(projection);
		}*/

		gridCRS = coordinateAxes.getBoundingBoxer().getCRS();
				
		mapper = new Mapper(mapFileDirectory, new LatLonProjection(), gridCRS);


		timeAxis = axes.getTimeAxis();

		if (timeAxis == null) {
			timesteps = 1;
			firstTimestep = timestep = lastTimestep  = 0;
			hasNoTime = true;
		} else {
			timesteps = timeAxis.getExtent();
			firstTimestep = timestep = timeAxis.getOrigin();
			lastTimestep = firstTimestep + timesteps - 1;
		}

		layerAxis = axes.getZAxis();

		if (layerAxis == null) {
			layers = 1;
			firstLayer = layer = lastLayer = 0;
		} else {
			layers = layerAxis.getExtent();
			firstLayer = layer = layerAxis.getOrigin();
			lastLayer = firstLayer + layers - 1;
			layerAxisName = layerAxis.getName();
		}
		//min/max, lon/lat, pct complete
		layerMinMaxCache = new double[layers][7];
		currentMinMaxCache = new double[7];
		logLayerMinMaxCache = new double[layers][7];
		//min/max, lon/lat
		statMinMaxCache = new double[6];
		//min/max
		plotMinMaxCache = new double[3];
		logPlotMinMaxCache = new double[3];

		
		final CoordAxis cellAxis = axes.getCellAxis();
		cells = (int)cellAxis.getRange().getExtent();
		
		rowAxis = mpasAxes.getYAxis();
		rows = rowAxis != null ? (int)rowAxis.getRange().getExtent() : 1;
		rowOrigin = rowAxis != null ? (int)rowAxis.getRange().getOrigin() : 0;
		firstRow = 0;
		lastRow = firstRow + rows - 1;

		columnAxis = mpasAxes.getXAxis();
		columns = columnAxis != null ? (int)columnAxis.getRange().getExtent() : 1;
		columnOrigin = columnAxis != null ? (int)columnAxis.getRange().getOrigin() : 0;
		firstColumn = 0;
		lastColumn = firstColumn + columns - 1;
		
		minLat = rowAxis.getRange().getOrigin();
		maxLat = minLat + rowAxis.getRange().getExtent() - 1;
		minLon = columnAxis.getRange().getOrigin();
		maxLon = minLon + columnAxis.getRange().getExtent() - 1;
		
		final Envelope envelope = mpasAxes.getBoundingBox(dataFrame.getDataset().get(0).getNetcdfCovn());

		westEdge = envelope.getMinX(); // E.g., -420000.0.
		southEdge = envelope.getMinY(); // E.g., -1716000.0.
			
		dataWidth = dataset.getDataWidth();
		dataHeight = dataset.getDataHeight();
		
		//We need these accurate to build correct gridBounds, but zooming expects them to be 0 until loadCellStructure runs
		visibleDataWidth = dataWidth;
		visibleDataHeight = dataHeight;
		
		gridBounds[X][MINIMUM] = westEdge + panX * RAD_TO_DEG;
		gridBounds[X][MAXIMUM] = westEdge + (panX + visibleDataWidth) * RAD_TO_DEG;
		gridBounds[Y][MINIMUM] = southEdge + (dataHeight - panY - visibleDataHeight) * RAD_TO_DEG;
		gridBounds[Y][MAXIMUM] = southEdge + (dataHeight - panY) * RAD_TO_DEG;

		domain[LONGITUDE][MINIMUM] = gridBounds[X][MINIMUM];
		domain[LONGITUDE][MAXIMUM] = gridBounds[X][MAXIMUM];
		domain[LATITUDE][MINIMUM] = gridBounds[Y][MINIMUM];
		domain[LATITUDE][MAXIMUM] = gridBounds[Y][MAXIMUM];
		
		//So for now reset to zero, and let get set normally
		visibleDataWidth = 0;
		visibleDataHeight = 0;
		
		cellWidth = envelope.getWidth() / columns; // 12000.0.
		
		GregorianCalendar dsDate = axes.getDate(firstTimestep);
		startDate = dsDate == null ? new GregorianCalendar() : dsDate;

		if (timesteps > 1) {
			final GregorianCalendar date1 = axes.getDate(firstTimestep + 1);
			timestepSize = date1.getTimeInMillis() - startDate.getTimeInMillis();
		} else {
			timestepSize = 1 * 60 * 60;
		}
		
		//populate legend colors and ranges on initiation

		//default to not a log scale
		enableLog(false);
		//calculate the non log min/max values, keep the code here
		//first part of IF ELSE will use the min/max values
		Logger.info("Calculating data range " + new Date());
		computeDataRange(false);
		if ( this.map == null) {
			
			Logger.debug("in FastTilePlot, this.map == null so calling new PavePaletteCreator");
			defaultPalette = new PavePaletteCreator().createPavePalette();
			map = new ColorMap(defaultPalette, plotMinMaxCache[0], plotMinMaxCache[1]);
		} else {
			ColorMap.ScaleType sType = map.getScaleType();
			if ( sType != null && sType == ColorMap.ScaleType.LOGARITHM ) {
				enableLog(true);
			}
		if(map.getPalette() == null)
		{
			Logger.debug("map.getPalette is null so calling new PavePaletteCreator");
		}
			defaultPalette = (map.getPalette() != null) ? map.getPalette() : new PavePaletteCreator().createPavePalette();
			map.setPalette(defaultPalette);
		}

		//set min/max for both log and non log values...
		map.setMinMax( plotMinMaxCache[0], plotMinMaxCache[1]);
		try {
			valueFormat = map.getNumberFormat();
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		computeDataRange(true);
		Logger.info("Calculating log data range " + new Date());
		map.setLogMinMax( logPlotMinMaxCache[0], logPlotMinMaxCache[1]);
		//this final one is for the below legend value calculations
		double[] localMinMax = plotMinMaxCache;
		if (this.log)
			localMinMax = logPlotMinMaxCache;
		Logger.info("Data ranges calculated " + new Date());

		//default to this type...
		map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
		legendColors = defaultPalette.getColors();
		final double minimum = localMinMax[0];
		final double maximum = localMinMax[1];
		minMax = new DataUtilities.MinMax(minimum, maximum);
		int count = legendColors.length + 1;
		final double delta = (maximum - minimum) / (count - 1);
		legendLevels = new double[count];
		for (int level = 0; level < count; ++level) {
			legendLevels[level] = minimum + level * delta;
		}
//		computeLegend();
		config = new TilePlotConfiguration();
		((TilePlotConfiguration) config).setColorMap(map);
		((TilePlotConfiguration) config).setGridLines(false, Color.gray);
		((TilePlotConfiguration) config).setSubtitle1(Tools.getDatasetNames(getDataFrame()));

		// Compute attributes derived from the above attributes and dataFrame:

		//computeDerivedAttributes();

		// Create EMVL TilePlot (but does not draw yet - see draw()):

		tilePlot = new MPASTilePlot(startDate, timestepSize, plotMinMaxCache, statMinMaxCache);
		
		if (renderMode == MODE_INTERPOLATION) {
			tilePlot.createGridInfo(gridBounds, domain);
		}
		
		if (timeAxis != null && timeAxis.getName().equals("nMonths"))
			tilePlot.setTimestepUnits(GregorianCalendar.MONTH);


		// Create GUI.

		timeLayerPanel = new TimeLayerPanel();
		timeLayerPanel.init(dataFrame.getAxes(), hasNoTime ? 0 : firstTimestep, hasNoLayer ? 0 : firstLayer, !hasNoLayer);
		
		ChangeListener timeStepListener = new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (processTimeChange) {
					redrawTimeLayer();
				}
			}
		};
		
		timeLayerPanel.addSpinnerListeners(timeStepListener, timeStepListener);
		
		playStopButton = new JButton(PLAY);
		playStopButton.setToolTipText(PLAY_TIP);
		playStopButton.addActionListener(this);
		playStopButton.setEnabled(timesteps > 1);
		leftStepButton = new JButton(LEFT);
		leftStepButton.addActionListener(this);
		leftStepButton.setToolTipText(LEFT_TIP);
		leftStepButton.setEnabled(timesteps > 1);
		rightStepButton = new JButton(RIGHT);
		rightStepButton.addActionListener(this);
		rightStepButton.setToolTipText(RIGHT_TIP);
		rightStepButton.setEnabled(timesteps > 1);
		delayField = new JTextField("50", 4);
		delayField.addActionListener(this);						// 2014 needed to handle user changing delay in an animation
		delayField.setToolTipText("Set animation delay (ms)");	// 2014
		delayField.setEnabled(timesteps > 1);
		firstRowField = new JTextField("1", 4);
		firstRowField.addActionListener(this);
		lastRowField = new JTextField(Integer.toString(rows), 4);
		lastRowField.addActionListener(this);
		firstColumnField = new JTextField("1", 4);
		firstColumnField.addActionListener(this);
		lastColumnField = new JTextField(Integer.toString(columns), 4);
		lastColumnField.addActionListener(this);

		GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel panel = new JPanel(gridbag);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        c.insets = new Insets(0, 0, 0, 10);

		final JPanel statisticsPanel = new JPanel();
		final String[] statisticsNames = new String[ GridCellStatistics.STATISTICS ];
		statisticsNames[ 0 ] = "None";

		for ( int index = 1; index < GridCellStatistics.STATISTICS; ++index ) {
			statisticsNames[ index ] = GridCellStatistics.name( index - 1 );
		}

		// Force menu visible on WIN32?
		javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled( false );
		statisticsMenu = new JComboBox( statisticsNames );
		statisticsMenu.addActionListener( this ); // Just so draw() is called.
		statisticsPanel.add( new JLabel( "Stats:" ) );
		statisticsPanel.add( statisticsMenu );
		statisticsPanel.add( new JLabel( ">" ) );
		threshold = new JTextField( "0.12" );
		threshold.addActionListener( this );
		statisticsPanel.add( threshold );

		JPanel animate = new JPanel();
		animate.add(leftStepButton); 
		animate.add(playStopButton);
		animate.add(rightStepButton);
		animate.add(new JLabel(DELAY_LABEL));
		animate.add(delayField);
		
        gridbag.setConstraints(timeLayerPanel, c);
        c.weightx = 0.2;
        gridbag.setConstraints(animate, c);
        panel.add(timeLayerPanel);
        panel.add( statisticsPanel );
        panel.add(animate);
		toolBar.add(panel);

		try {
			getCellStructure();
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
		
		initLayerCache(layers);
		
		popup = createPopupMenu(true, true, true, zoom);
		popup.addPopupMenuListener(this);
		if (hasNoTime)
			firstTimestep = 0;
		if (hasNoLayer)
			firstLayer = 0;
		
		if (renderMode == MODE_INTERPOLATION)
			renderReader = createDataReader();
		
		if (renderMode == MODE_INTERPOLATION || renderMode == MODE_PLOT) {
			// add(toolBar);
			doubleBufferedRendererThread = new Thread(doubleBufferedRenderer);
			doubleBufferedRendererThread.start(); // Calls
			
			draw();
		}
	}
	
	public void initInterpolation() {
		if (renderMode == MODE_INTERPOLATION) {
			currentView = AVERAGES;
			
			mapPolygon = new MapPolygon(tilePlot);
			
			gridNum = GridInfo.getGridNumber(tilePlot.getGridInfo());
				
			calcGlobalDepositionRange();
			
			// calc range for this set of numbers
			double[] minmax = { 0.0, 0.0 };

//			this.range;
			minmax[0] = range.averageMin;
			minmax[1] = range.averageMax;

			// initialize colormap to these min max values
			minMax=new MinMax(minmax[0],minmax[1]);
			// TODO: JIZHEN - need the old map?
			//ColorMap map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
			if ( map == null) {
				map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
			} else {
				map.setPalette(defaultPalette);
				map.setMinMax( minmax[0], minmax[1]);
			}
			map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
			config.putObject(TilePlotConfiguration.COLOR_MAP, map);
		
			updateColorMap(map);
		}
	}
	
	private boolean statError = false;

	private void computeStatistics(boolean log) {

		if ( layerData == null ) {
			layerData = new float[ cellsToRender.length ][ timesteps ];
			//layerDataLog = new float[ rows ][ columns ][ timesteps ];
			statisticsData = new float[ GridCellStatistics.STATISTICS ][1][ cellsToRender.length ];
			//statisticsDataLog = new float[ GridCellStatistics.STATISTICS ][ rows ][ columns ];
		}
			
		// Copy from dataFrame into layerData[ rows ][ columns ][ timesteps ]:
		
		DataFrame dataFrame = getDataFrame(log);
		final MPASDataFrameIndex dataFrameIndex = new MPASDataFrameIndex(dataFrame);
			
		for (int timestep = 0; timestep < timesteps; ++timestep) {
			for (int cell = 0; cell < cellsToRender.length; ++cell) {
					dataFrameIndex.set(timestep, layer, cell);
					//float value = this.dataFrame.getFloat( dataFrameIndex ); // do not replace this one with getDataFrame()
					float value = this.getDataFrame(log).getFloat( dataFrameIndex ); 
					layerData[ cell ][ timestep ] = value;
					//value = this.dataFrameLog.getFloat( dataFrameIndex );
					//layerDataLog[ row ][ column ][ timestep ] = value;
				
			}
		}

		final double threshold = Double.parseDouble( this.threshold.getText() );
		final double hoursPerTimestep = 1.0;
		
		try {
			MeshCellStatistics.computeStatistics( layerData,
					threshold, hoursPerTimestep,
					statisticsData, this.statisticsMenu.getSelectedIndex()-1 );
			//GridCellStatistics.computeStatistics( layerDataLog,
			//		threshold, hoursPerTimestep,
			//		statisticsDataLog, this.statisticsMenu.getSelectedIndex()-1 );
			this.statError = false;
		} catch ( Exception e) {
			e.printStackTrace();
			Logger.error("Error occurred during computing statistics: " + e.getMessage());
			this.statError = true;
			if ( map != null && map.getScaleType() == ColorMap.ScaleType.LOGARITHM) {
				this.preLog = true;
				enableLog(false);
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( false);
				}
				map.setScaleType( ColorMap.ScaleType.LINEAR);
				
				// 2014 this section previously commented out
				//this.computeLegend();
//				double[] minmax = { 0.0, 0.0 };
//				computeDataRange(minmax);
//				final double minimum = minmax[0];
//				final double maximum = minmax[1];
//				minMax = new DataUtilities.MinMax(minimum, maximum);
//				this.recomputeLegend = true;
//				this.recomputeStatistics = true;				
//
//				if ( this.dialog != null) {
//					this.dialog.initColorMap(map, minMax);
//				}
				draw();
			}
		}
	}

	// Compute derived attributes:

	/*private void computeDerivedAttributes() {
		if (true)
			return;

		// Compute grid bounds and domain:

		gridBounds[X][MINIMUM] = westEdge + firstColumn * cellWidth;
		gridBounds[X][MAXIMUM] = westEdge + (1 + lastColumn) * cellWidth;
		gridBounds[Y][MINIMUM] = southEdge + firstRow * cellHeight;
		gridBounds[Y][MAXIMUM] = southEdge + (1 + lastRow) * cellHeight;

		if (projector != null) {
			computeMapDomain(projector, gridBounds, domain);
		} else {
			domain[LONGITUDE][MINIMUM] = gridBounds[X][MINIMUM];
			domain[LONGITUDE][MAXIMUM] = gridBounds[X][MAXIMUM];
			domain[LATITUDE][MINIMUM] = gridBounds[Y][MINIMUM];
			domain[LATITUDE][MAXIMUM] = gridBounds[Y][MAXIMUM];
		}
	}*/

	// Compute map domain from grid bounds:

	/*
	private static void computeMapDomain(final Projector projector,
			final double[][] gridBounds, double[][] mapDomain) {

		final double margin = 1.0; // Degrees lon/lat beyond grid corners.
		final double xMinimum = gridBounds[X][MINIMUM];
		final double xMaximum = gridBounds[X][MAXIMUM];
		final double yMinimum = gridBounds[Y][MINIMUM];
		final double yMaximum = gridBounds[Y][MAXIMUM];
		final double xMean = (xMinimum + xMaximum) * 0.5;
		double[] longitudeLatitude = { 0.0, 0.0 };

		// Unproject corners of bottom edge of grid for latitude minimum:
		projector.unproject(xMinimum, yMinimum, longitudeLatitude);
		mapDomain[LONGITUDE][MINIMUM] = longitudeLatitude[LONGITUDE];
		mapDomain[LATITUDE][MINIMUM] = longitudeLatitude[LATITUDE];
		projector.unproject(xMaximum, yMinimum, longitudeLatitude);
		mapDomain[LONGITUDE][MAXIMUM] = longitudeLatitude[LONGITUDE];
		mapDomain[LATITUDE][MINIMUM] = Math.min(mapDomain[LATITUDE][MINIMUM],
				longitudeLatitude[LATITUDE]);

		// Unproject corners and center of top edge of grid for latitude maximum:

		projector.unproject(xMinimum, yMaximum, longitudeLatitude);
		mapDomain[LONGITUDE][MINIMUM] = Math.min(mapDomain[LONGITUDE][MINIMUM],
				longitudeLatitude[LONGITUDE]);
		mapDomain[LATITUDE][MAXIMUM] = longitudeLatitude[LATITUDE];
		projector.unproject(xMaximum, yMaximum, longitudeLatitude);
		mapDomain[LONGITUDE][MAXIMUM] = Math.max(mapDomain[LONGITUDE][MAXIMUM],
				longitudeLatitude[LONGITUDE]);
		mapDomain[LATITUDE][MAXIMUM] = Math.max(mapDomain[LATITUDE][MAXIMUM],
				longitudeLatitude[LATITUDE]);

		if ( projector.getProjection() instanceof
				ucar.unidata.geoloc.projection.Stereographic ) {

			// Must be a polar projection so
			// use full domain in case grid crosses the equator:

			mapDomain[LATITUDE ][MINIMUM] = -90.0;
			mapDomain[LATITUDE ][MAXIMUM] =  90.0;
			mapDomain[LONGITUDE][MINIMUM] = -180.0;
			mapDomain[LONGITUDE][MAXIMUM] = 180.0;
		} else { // Non-polar projection:
			projector.unproject(xMean, yMaximum, longitudeLatitude);
			mapDomain[LATITUDE][MAXIMUM] = Math.max(
					mapDomain[LATITUDE][MAXIMUM], longitudeLatitude[LATITUDE]);

			// Expand domain by margin all around, within valid range:

			mapDomain[LONGITUDE][MINIMUM] = Numerics.clamp(
					mapDomain[LONGITUDE][MINIMUM] - margin, -180.0, 180.0);
			mapDomain[LONGITUDE][MAXIMUM] = Numerics.clamp(
					mapDomain[LONGITUDE][MAXIMUM] + margin, -180.0, 180.0);
			mapDomain[LATITUDE][MINIMUM] = Numerics.clamp(
					mapDomain[LATITUDE][MINIMUM] - margin, -90.0, 90.0);
			mapDomain[LATITUDE][MAXIMUM] = Numerics.clamp(
					mapDomain[LATITUDE][MAXIMUM] + margin, -90.0, 90.0);
		}
	}
	*/

	// Copy current timestep, layer and row/column subset data from dataFrame
	// into subsetlayerdata[][]:

	private void copySubsetLayerData(boolean log) {


		final int selection = statisticsMenu.getSelectedIndex();
		
		if ( selection > 0 ) {

			if ( statisticsData == null || recomputeStatistics ) {
				computeStatistics(log);
				/*final boolean doLog = log;
				new Thread(new Runnable() { public void run() {
					computeStatistics(doLog);
				}}).start();*/
				recomputeStatistics = false;
			}

		}

	}

	// Compute data range excluding BADVAL3 values:

	public void computeDataRange(boolean log) {
		final int selection = statisticsMenu != null ? statisticsMenu.getSelectedIndex() : 0;
		if ( selection < 1 ) {
			DataFrame dataFrame = getDataFrame(log);
			MinMaxInfo minMaxInfo = dataset.getPlotMinMax(dataFrame, this);
			if (log) {
				logPlotMinMaxCache[0] = minMaxInfo.getMin();
				logPlotMinMaxCache[1] = minMaxInfo.getMax();
				logPlotMinMaxCache[2] = minMaxInfo.getCompletion();
			} else {
				plotMinMaxCache[0] = minMaxInfo.getMin();
				plotMinMaxCache[1] = minMaxInfo.getMax();
				plotMinMaxCache[2] = minMaxInfo.getCompletion();
			}
		} else {
						
			this.computeStatistics(log);					
						
			final int statistic = selection - 1;
			statMinMaxCache[LEVELS_CACHE_MIN_VALUE] = Double.POSITIVE_INFINITY;
			statMinMaxCache[LEVELS_CACHE_MAX_VALUE] = Double.NEGATIVE_INFINITY;

			for ( int cell = firstRow; cell < cellsToRender.length; ++cell ) {

				final float value = statisticsData[ statistic ][0][ cell ];
				if (value < statMinMaxCache[LEVELS_CACHE_MIN_VALUE]) {
					statMinMaxCache[LEVELS_CACHE_MIN_VALUE] = value;
					statMinMaxCache[LEVELS_CACHE_MIN_LON] = cellsToRender[cell].getLon();
					statMinMaxCache[LEVELS_CACHE_MIN_LAT] = cellsToRender[cell].getLat();
				}
				else if (value > statMinMaxCache[LEVELS_CACHE_MAX_VALUE]) {
					statMinMaxCache[LEVELS_CACHE_MAX_VALUE] = value;
					statMinMaxCache[LEVELS_CACHE_MAX_LON] = cellsToRender[cell].getLon();
					statMinMaxCache[LEVELS_CACHE_MAX_LAT] = cellsToRender[cell].getLat();
				}
			}
		}	
	}
	
	public double[][] getGridBounds() {
		return gridBounds;
	}
	
	public double[][] getDomain() {
		return domain;
	}
	

	/**
	 * Gets the panel that contains the plot component.
	 * 
	 * @return the panel that contains the plot component.
	 */

	public JPanel getPanel() {
		return this;
	}

	/**
	 * Gets a menu bar for this Plot. This may return null if there is no menu
	 * bar.
	 * 
	 * @return a menu bar for this Plot.
	 */

	public JMenuBar getMenuBar() {
		javax.swing.JPopupMenu.setDefaultLightWeightPopupEnabled(false);
		JMenuBar bar = new JMenuBar();

		JMenu menu = new JMenu("File");
		menu.setMnemonic('F');
		//menu.add(new FastTilePlotPrintAction(this));
		menu.add(new PlotExporterAction(this));
		bar.add(menu);

		menu = new JMenu("Configure");
		menu.add(new AbstractAction("Configure Plot") {
			private static final long serialVersionUID = 2455217937515200807L;

			public void actionPerformed(ActionEvent e) {
				editChartProperties();
			}
		});
		loadConfig = new LoadConfiguration(this);
		saveConfig = new SaveConfiguration(this);
		menu.add(loadConfig);
		menu.add(saveConfig);
		//configureMapMenu(menu);
		bar.add(menu);

		menu = new JMenu("Controls");
		bar.add(menu);
		/*ButtonGroup grp = new ButtonGroup();
		JMenuItem item = menu.add(new JRadioButtonMenuItem(new AbstractAction("Zoom") {
			private static final long serialVersionUID = 5282480503103839989L;

			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem src = (JRadioButtonMenuItem) e.getSource();
				zoom = src.isSelected();
				probe = !zoom;
				if(probe)	// 2014 added logic and ability to turn probe off 
				{
					activateRubberBand();
					// change cursor
					setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				}
				else
				{
					deactivateRubberBand();
					// change cursor
					setCursor(Cursor.getDefaultCursor());
				}
			}
		}));
		item.setSelected(true);
		grp.add(item);


		item = menu.add(new JRadioButtonMenuItem(new AbstractAction("Probe") {
			private static final long serialVersionUID = 8777942675687929471L;

			public void actionPerformed(ActionEvent e) {
				JRadioButtonMenuItem src = (JRadioButtonMenuItem) e.getSource();
				probe = src.isSelected();
				if(probe)
				{	// 2014 changed logic to allow user to turn probe mode on/off
					activateRubberBand();
					// change cursor
					setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
				}
				else
				{
					deactivateRubberBand();
					// change cursor
					setCursor(Cursor.getDefaultCursor());
				}
			}
		}));
		grp.add(item);
		menu.add(item);
		
		menu.addSeparator();*/
		
		// item.setSelected(true);

		activateRubberBand();

		JMenuItem menuItem = new JMenuItem(
				new AbstractAction("Set Longitude and Latitude Ranges") {
					private static final long serialVersionUID = -4465758432397962782L;

					@Override
					public void actionPerformed(ActionEvent arg0) {
						setDataRanges();
					}
				});
		menu.add(menuItem);

		menu.addSeparator();
		showGridLines = new JCheckBoxMenuItem(new AbstractAction("Show Cell Borders") {
			private static final long serialVersionUID = 2699330329257731588L;

			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem gridlines = (JCheckBoxMenuItem) e.getSource();
				Color configColor = (Color)config.getObject(TilePlotConfiguration.GRID_LINE_COLOR);
				Color glColor = (configColor == null ? Color.GRAY : configColor);
				((TilePlotConfiguration) config).setGridLines(gridlines.isSelected(), glColor);
				draw();
			}
		});
		menu.add(showGridLines);
		showWindVectors = new JCheckBoxMenuItem(new AbstractAction("Show Wind Vectors") {
			private static final long serialVersionUID = 2699330329257731588L;

			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem windVectors = (JCheckBoxMenuItem) e.getSource();
				((TilePlotConfiguration) config).setWindVectors(windVectors.isSelected());
				renderWind = windVectors.isSelected();
				if (renderWind) {
					dataChanged = true;
					locChanged = true;
				}
				draw();
			}
		});
		menu.add(showWindVectors);
		
		bar.add(menu);

		menu = new JMenu("Plot");
		bar.add(menu);
		menu.add(timeSeriesSelected);
		menu.add(timeSeriesBarSelected);
		menu.add(timeSeriesMin);
		menu.add(timeSeriesMax);

		menu.addSeparator();

		JMenuItem item2 = new JMenuItem(new AbstractAction("Animate Plot") {
			private static final long serialVersionUID = 6336130019191512947L;

			public void actionPerformed(ActionEvent e) {
				AnimationPanel panel = new AnimationPanel();
				panel.init(getDataFrame().getAxes(), MeshPlot.this);
			}
		});
		menu.add(item2);

		/*if (this.getClass().equals(MeshPlot.class)) {
			JMenu sub = new JMenu("Add Overlay");
			item2 = sub.add(new JMenuItem(new AbstractAction("Observations") {
				private static final long serialVersionUID = 2699330329257731588L;

				public void actionPerformed(ActionEvent e) {
					addObsOverlay();
				}
			}));

			item2 = sub.add(new JMenuItem(new AbstractAction("Vectors") {
				private static final long serialVersionUID = 1408918800912242196L;

				public void actionPerformed(ActionEvent e) {
					addVectorOverlay();
				}
			}));

			menu.add(sub);
		}*/
		
		menu = new JMenu("GIS Layers");	// in VERDI versions through 1.5.0 does not pertain to shapefiles
		gisLayersMenu(menu);
		bar.add(menu);
		
		if (renderMode == MODE_INTERPOLATION) {
			// add in my extra option menu
			menu = new JMenu("Options");
			ButtonGroup group = new ButtonGroup();

			JRadioButtonMenuItem radioButton=new JRadioButtonMenuItem(new AbstractAction("Show Area Averages") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 4673389754505180377L;

				public void actionPerformed(ActionEvent e) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
					if(item.isSelected())MeshPlot.this.showAverages();
					draw();
				}
			});
			radioButton.setSelected(true);
			group.add(radioButton);
			menu.add(radioButton);

			JRadioButtonMenuItem showTotalButton=new JRadioButtonMenuItem(new AbstractAction("Show Area Totals") {
				/**
				 * 
				 */
				private static final long serialVersionUID = -4078259126612838335L;

				public void actionPerformed(ActionEvent e) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
					if(item.isSelected())MeshPlot.this.showTotals();
					draw();
				}
			});
			group.add(showTotalButton);
			menu.add(showTotalButton);
			// disable the radiobutton if needed
			// see if the current formula type allows this
			if(Units.isConcentration(getDataFrame().getVariable().getUnit().toString())){
				showTotalButton.setEnabled(false);
			}

			radioButton=new JRadioButtonMenuItem(new AbstractAction("Show Cell Data") {
				/**
				 * 
				 */
				private static final long serialVersionUID = -6206984806970043891L;

				public void actionPerformed(ActionEvent e) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
					if(item.isSelected())MeshPlot.this.showGrid();
					draw();
				}
			});
			group.add(radioButton);
			menu.add(radioButton);

			menu.addSeparator();

			// make radio buttons for filling options
			ButtonGroup group2 = new ButtonGroup();

			radioButton=new JRadioButtonMenuItem(new AbstractAction("Selected Areas") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 8704295164108322755L;

				public void actionPerformed(ActionEvent e) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
					if(item.isSelected())MeshPlot.this.showSelected();
					draw();
				}
			});
			radioButton.setSelected(false);
			group2.add(radioButton);
			menu.add(radioButton);

			radioButton=new JRadioButtonMenuItem(new AbstractAction("All Areas") {
				/**
				 * 
				 */
				private static final long serialVersionUID = 841441627768110972L;

				public void actionPerformed(ActionEvent e) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem) e.getSource();
					if(item.isSelected())MeshPlot.this.showAll();
					draw();
				}
			});
			radioButton.setSelected(true);
			group2.add(radioButton);
			menu.add(radioButton);

			bar.add(menu);

		}
		
		// change cursor for initial zoom state
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		
		return bar;
	}
	
	
	/*protected void addObsOverlay() {
		OverlayRequest<ObsEvaluator> request = new OverlayRequest<ObsEvaluator>(OverlayRequest.Type.OBS, this);
		eventProducer.fireOverlayRequest(request);
	}
	
	protected void addVectorOverlay() {
		OverlayRequest<VectorEvaluator> request = new OverlayRequest<VectorEvaluator>(OverlayRequest.Type.VECTOR, this);
		eventProducer.fireOverlayRequest(request);
	}*/
	
	public boolean isShowSelectedOnly() {
		return showSelectedOnly;
	}

	public void setShowSelectedOnly(boolean showSelectedOnly) {
		this.showSelectedOnly = showSelectedOnly;
	}
	
	public void showAll() {
		setShowSelectedOnly(false);
		invalidate();
		repaint();
	}
	public void showSelected() {
		setShowSelectedOnly(true);
		invalidate();
		repaint();
	}
	public void showAverages() {
		currentView = AVERAGES;
		calculateAverageLevels();
		invalidate();
		repaint();
	}
	public void showTotals() {
		currentView = TOTALS;
		calculateTotalLevels();
		invalidate();
		repaint();
	}
	public void showGrid() {
		currentView = GRID;
		calculateGridLevels();
		invalidate();
		repaint();
	}

	protected void activateRubberBand() {
		rubberband.setActive(true);
	}
	
	protected void deactivateRubberBand() {	// 2014 to allow user to turn OFF probe
		rubberband.setActive(false);
	}
	
	private void resetZooming() {
		locChanged = true;
		dataChanged = true;
		clippedDataRatio = dataRatio;
		zoomFactor = 1;
		compositeFactor = screenWidth / dataWidth;
		previousPanX = panX;
		previousPanY = panY;
		panX = 0;
		panY = 0;
		firstRow = 0;
		lastRow = rows - 1;
		firstColumn = 0;
		lastColumn = columns - 1;
		visibleDataWidth = dataWidth;
		visibleDataHeight = dataHeight;
		setCellClicked(0);
		draw();
		//computeDerivedAttributes();
		copySubsetLayerData(this.log);
	}

	private void gisLayersMenu(JMenu menu) {
		menu.add(mapLayersMenu);

		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				Object srcObj = evt.getSource();
				
				if (srcObj instanceof JCheckBoxMenuItem) {
					JCheckBoxMenuItem item = (JCheckBoxMenuItem) srcObj;
				 	showLayer(item.getActionCommand(),  item.isSelected(), mapLayersMenu);
				} else 
					showLayer(OTHER_MAPS, false, mapLayersMenu);
			}
		};
		
		// String defaultMaps = System.getProperty("default.maps", "");

		JCheckBoxMenuItem item = new JCheckBoxMenuItem("World", false);
		item.setActionCommand(WORLD_LAYER);
		item.addActionListener(listener);
		mapLayersMenu.add(item);

		item = new JCheckBoxMenuItem("North America", false);
		item.setActionCommand(NA_LAYER);
		item.addActionListener(listener);
		mapLayersMenu.add(item);
		
		item = new JCheckBoxMenuItem("USA States", false);
		item.setActionCommand(STATES_LAYER);
		item.addActionListener(listener);
		mapLayersMenu.add(item);

		item = new JCheckBoxMenuItem("USA Counties", false);
		item.setActionCommand(COUNTIES_LAYER);
		item.addActionListener(listener);
		mapLayersMenu.add(item);

		item = new JCheckBoxMenuItem("HUCs", false);
		item.setActionCommand(HUCS);
		item.addActionListener(listener);
		mapLayersMenu.add(item);

		item = new JCheckBoxMenuItem("Rivers", false);
		item.setActionCommand(RIVERS);
		item.addActionListener(listener);
		mapLayersMenu.add(item);

		item = new JCheckBoxMenuItem("Roads", false);
		item.setActionCommand(ROADS);
		item.addActionListener(listener);
		mapLayersMenu.add(item);
		
		JMenuItem otheritem = new JMenuItem("Other...");
		otheritem.setActionCommand(OTHER_MAPS);
		otheritem.addActionListener(listener);
		mapLayersMenu.add(otheritem);

		menu.add(new AbstractAction("Configure GIS Layers") {
			private static final long serialVersionUID = -3679673290623274686L;

			public void actionPerformed(ActionEvent e) {
				FastTileLayerEditor editor = showGISLayersDialog();

				if (!editor.wasCanceled()) {
					resetMenuItems(mapLayersMenu);
					draw();
				}
			}
		});

		menu.addSeparator();

		JMenuItem defaultItem = new JMenuItem(new AbstractAction(
				"Set Current Maps As Plot Default") {
			private static final long serialVersionUID = 2403382186582489960L;

			public void actionPerformed(ActionEvent e) {
				// TBI
			}
		});
		defaultItem.setEnabled(false);
		menu.add(defaultItem);
	}
	
	protected void resetMenuItems(JMenu addLayers) {
		if (addLayers == null) return;
		
		JCheckBoxMenuItem world = (JCheckBoxMenuItem)addLayers.getItem(0);
		JCheckBoxMenuItem na = (JCheckBoxMenuItem)addLayers.getItem(1);
		JCheckBoxMenuItem states = (JCheckBoxMenuItem)addLayers.getItem(2);
		JCheckBoxMenuItem counties = (JCheckBoxMenuItem)addLayers.getItem(3);
		JCheckBoxMenuItem hucs = (JCheckBoxMenuItem)addLayers.getItem(4);
		JCheckBoxMenuItem rivers = (JCheckBoxMenuItem)addLayers.getItem(5);
		JCheckBoxMenuItem roads = (JCheckBoxMenuItem)addLayers.getItem(6);
		
		world.setSelected(mapper.worldMapIncluded());
		states.setSelected(mapper.usStatesMapIncluded());
		counties.setSelected(mapper.usCountiesMapIncluded());
		hucs.setSelected(mapper.usHucsMapIncluded());
		rivers.setSelected(mapper.usRiversMapIncluded());
		roads.setSelected(mapper.usRoadsMapIncluded());
		na.setSelected(mapper.naMapIncluded());
	}

	private FastTileLayerEditor showGISLayersDialog() {
		Logger.debug("in FastTilePlot.showGISLayersDialog()");
		Window frame = SwingUtilities.getWindowAncestor(this);
		FastTileLayerEditor editor = null;
		
		if (frame instanceof JFrame)
			editor = new FastTileLayerEditor((JFrame) frame);
		else
			editor = new FastTileLayerEditor((JDialog) frame);
		
		editor.init(mapper);
		//editor.setLayerControl(createControlLayer());
		editor.setLocationRelativeTo(frame);
		Point p = editor.getLocation();
		editor.setLocation(0, p.y);
		editor.setVisible(true);
		editor.pack();
		return editor;
	}
	
	protected FeatureLayer createControlLayer(String shapefile) {	// JEB not currently being called
		Logger.debug("in FastTilePlot.createControlLayer");
		
		Logger.debug("ready to get map");
		try {
			URL url = null;
			Logger.debug("initialized url = null");
			Logger.debug("shapefile = " + shapefile);
			if(shapefile != null)
			{
				Logger.debug("shapefile is not null");
				url = new File(shapefile).toURI().toURL();
			}
			else
			{
				Logger.debug("shapefile is null, bringing up file selection widget");
				// here bring up file chooser to select a shapefile
				File file = JFileDataStoreChooser.showOpenFile("shp", null);
				if(file == null)
				{
					Logger.error("No shapefile selected for map of type OTHER. You must select a shapefile or a standard map.");
					return null;
				}
				url = file.toURI().toURL();
			}
			Map<String, Serializable> params = new HashMap<String, Serializable>();
			Logger.debug("created new params structure");
			params.put(ShapefileDataStoreFactory.URLP.key, url);
			Logger.debug("put 1st into params");
			params.put(ShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key, true);
			Logger.debug("put 2nd into params");
			DataStoreFactorySpi fac = new ShapefileDataStoreFactory();
			Logger.debug("created fac: " + fac.toString());		// GET TO HERE BEFORE THROWS Exception in FastTilePlot.createControlLayer: null
			ShapefileDataStore ds = (ShapefileDataStore) fac.createNewDataStore(params);
			Logger.debug("created ds");
			StyleBuilder builder = new StyleBuilder();
			Logger.debug("created builder");
			Style style = builder.createStyle(builder.createLineSymbolizer());
			Logger.debug("created style");
			Query query = new Query("projectMap");
			Logger.debug("created query");
			// now ask to project into the CRS of the FastTilePlot axes
			query.setCoordinateSystemReproject(gridCRS);
			Logger.debug("setCoordinateSystemReproject");
			controlLayer = new FeatureLayer(ds.getFeatureSource().getFeatures(query), style);
			Logger.debug("assigned to controlLayer");
		} catch (Exception e) {
			Logger.error("Exception in FastTilePlot.createControlLayer", e);
		}
		
		return controlLayer;
	}
	
	public void setLayerMapLine(VerdiBoundaries aVerdiBoundaries) { // called by ScriptHandler only
		mapper.getLayers().add(aVerdiBoundaries);
	}

	private void showLayer(String layerKey, boolean show, JMenu addJMenuLayers) {
		try {
			if (show && layerKey.equals(STATES_LAYER)) {
//				MapLines map2Add = getEditedMapLayer(mapper.getUsaStatesMap());
				VerdiBoundaries map2Add = mapper.getUsaStatesMap();
				mapper.getLayers().add(map2Add);
			}

			if (!show && layerKey.equals(STATES_LAYER)) {
				mapper.removeUsaStates();
			}
			
			if (show && layerKey.equals(COUNTIES_LAYER)) {
				VerdiBoundaries map2Add = mapper.getUsaCountiesMap();
				mapper.getLayers().add(map2Add);
			}

			if (!show && layerKey.equals(COUNTIES_LAYER)) {
				mapper.removeUsaCounties();
			}

			if (show && layerKey.equals(WORLD_LAYER)) {
				VerdiBoundaries map2Add = mapper.getWorldMap();
				mapper.getLayers().add(map2Add);
			}

			if (!show && layerKey.equals(WORLD_LAYER)) {
				mapper.removeWorld();
			}

			if (show && layerKey.equals(NA_LAYER)) {
				VerdiBoundaries map2Add = mapper.getNorthAmericaMap();
				mapper.getLayers().add(map2Add);
			}

			if (!show && layerKey.equals(NA_LAYER)) {
				mapper.removeNorthAmerica();
			}

			if (show && layerKey.equals(HUCS)) {
				withHucs = show;
				mapper.getLayers().add(mapper.getUSHucMap());
			}
			
			if (!show && layerKey.equals(HUCS)) {
				withHucs = show;
				mapper.removeUSHucMap();
			}

			if (show && layerKey.equals(RIVERS)) {
				withRivers = show;
				mapper.getLayers().add(mapper.getUSRiversMap());
			}
			
			if (!show && layerKey.equals(RIVERS)) {
				withRivers = show;
				mapper.removeUSRiversMap();
			}

			if (show && layerKey.equals(ROADS)) {
				withRoads = show;
				mapper.getLayers().add(mapper.getUSRoadsMap());
			}
			
			if (!show && layerKey.equals(ROADS)) {
				withRoads = show;
				mapper.removeUSRoadsMap();
			}
			
			if (layerKey.equals(OTHER_MAPS)) {
				//showGISLayersDialog();
				File selectFile = JFileDataStoreChooser.showOpenFile("shp", null);
				VerdiBoundaries aVerdiBoundaries = new VerdiBoundaries();
				aVerdiBoundaries.setFileName(selectFile.getAbsolutePath());
				mapper.getLayers().add(aVerdiBoundaries);
			}
			draw();
		} catch (Exception e) {
			Logger.error("Error adding layer " + e.getMessage());
		}
	}


	/**
	 * Displays a dialog that allows the user to edit the properties for the current chart.
	 * 
	 * @since 1.0.5
	 */
	public void editChartProperties() {
		Window window = SwingUtilities.getWindowAncestor(MeshPlot.this);
		dialog = null;
		if (window instanceof JFrame)
			dialog = new ConfigDialog((JFrame) window);
		else
			dialog = new ConfigDialog((JDialog) window);
		dialog.init(MeshPlot.this, minMax);
		dialog.enableScale( !this.statError);
		Point p = getLocation();
		setLocation(0, p.y);
		dialog.setVisible(true);
	}

	/**
	 * Gets a tool bar for this plot. This may return null if there is no tool
	 * bar.
	 * 
	 * @return a tool bar for this plot.
	 */

	public JToolBar getToolBar() {
		return toolBar;
	}

	/**
	 * Gets the type of the Plot.
	 * 
	 * @return the type of the Plot.
	 */

	public Formula.Type getType() {
		return Formula.Type.TILE;
	}

	/**
	 * Gets the data that this Plot plots.
	 * 
	 * @return the data that this Plot plots.
	 */

	public List<DataFrame> getData() {
		final List<DataFrame> result = new ArrayList<DataFrame>();
		result.add(getDataFrame());
		return result;
	}

	/**
	 * Exports an image of this Plot to the specified file in the specified
	 * format.
	 * 
	 * @param format
	 *            the image format. One of PlotExporter.JPG, PlotExporter.TIF,
	 *            PlotExporter.PNG, or PlotExporter.BMP
	 * @param file
	 *            the file to save the image to.
	 * @param width
	 *            width of image in pixels
	 * @param height
	 *            height of image in pixels
	 * @throws IOException
	 *             if there is an error creating the image
	 */

	public void exportImage(String format, File file, int width, int height)
			throws IOException {
		drawBatchImage(width, height);
		PlotExporter exporter = new PlotExporter(this);
		exporter.save(format, file, width, height);
	}

	/**
	 * Configure this Plot according to the specified PlotConfiguration.
	 * 
	 * @param config
	 *            the new plot configuration
	 */

	public void configure(PlotConfiguration config) {
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
				if (logRenderVariable == null)
					logRenderVariable = ArrayReader.getReader(dataFrameLog.getArray());
				enableLog(true);
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( true);
					this.tilePlot.setLogBase( (int)map.getLogBase());
				}					
				
				//we need to also populate the non log intervals to some default range...
				//a new map object is created and doesn't keep the interval ranges that was created in one of the constructors
				//so we need to make sure and pre populate just in case user changes between linear and log scales
				computeDataRange(false);
				map.setMinMax( minmax[0], minmax[1]);
				try {
					valueFormat = map.getNumberFormat();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				enableLog(false);
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( false);
				}				
				//we need to also populate the log intervals to some default range...
				//a new map object is created and doesn't keep the interval ranges that was created in one of the constructors
				//so we need to make sure and pre populate just in case user changes between linear and log scales
				computeDataRange(true);
				map.setLogMinMax( minmax[0], minmax[1]);
			}
			updateColorMap(map);
			recomputeStatistics = true;
		}

		updateConfigVariables();
		this.draw();
		this.config = new TilePlotConfiguration(config);
		
		if (this.showGridLines != null) {
			Boolean gridlines = (Boolean)config.getObject(TilePlotConfiguration.SHOW_GRID_LINES);
			this.showGridLines.setSelected(gridlines == null ? false : gridlines);
		}
	}
	
	public void configure(PlotConfiguration config, Plot.ConfigSource source) {
		String configFile = config.getConfigFileName();

		if (configFile != null) {
			try {
				configure(new PlotConfigurationIO().loadConfiguration(new File(configFile)), source);
			} catch (IOException ex) {
				Logger.error("IOException in FastTilePlot.configure: loading configuration: " + ex.getMessage());
			}
		}
		
		this.configSource = source;

		ColorMap map = (ColorMap) config.getObject(TilePlotConfiguration.COLOR_MAP);

		if (map != null) {
			
			// set log related info
			ColorMap.ScaleType sType = map.getScaleType();
			this.preLog = this.log;
			if ( sType == ColorMap.ScaleType.LOGARITHM ) {
				enableLog(true);
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( true);
					this.tilePlot.setLogBase( (int)map.getLogBase());
				}					
				
				//we need to also populate the non log intervals to some default range...
				//a new map object is created and doesn't keep the interval ranges that was created in one of the constructors
				//so we need to make sure and pre populate just in case user changes between linear and log scales
				map.setMinMax( plotMinMaxCache[0], plotMinMaxCache[1]);
				try {
					valueFormat = map.getNumberFormat();
				} catch (Exception e) {
					e.printStackTrace();
				}

			} else {
				enableLog(false);
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( false);
				}				
				//we need to also populate the log intervals to some default range...
				//a new map object is created and doesn't keep the interval ranges that was created in one of the constructors
				//so we need to make sure and pre populate just in case user changes between linear and log scales
				map.setLogMinMax( logPlotMinMaxCache[0], logPlotMinMaxCache[1]);
				try {
					valueFormat = map.getNumberFormat();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			updateColorMap(map);
			recomputeStatistics = true;	
		}

		this.config = new TilePlotConfiguration(config);
		updateConfigVariables();
		this.draw();
		
		if (this.showGridLines != null) {
			Boolean gridlines = (Boolean)config.getObject(TilePlotConfiguration.SHOW_GRID_LINES);
			this.showGridLines.setSelected(gridlines == null ? false : gridlines);
		}
	}	

	private void enableLog(boolean enabled) {
		if (this.log != enabled)
			dataChanged = true;
		this.log = enabled;
		if (enabled) {
			if (logRenderVariable == null)
				logRenderVariable = ArrayReader.getReader(dataFrameLog.getArray());
			currentVariable = logRenderVariable;
			currentDataFrame = dataFrameLog;
		} else {
			currentVariable = renderVariable;
			currentDataFrame = dataFrame;
		}

	}
	private void updateColorMap(ColorMap map) {
		this.map = map;
		
		try {
			minMax = new DataUtilities.MinMax(map.getMin(), map.getMax());
			valueFormat = map.getNumberFormat();
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
		screenInitted = false;
		dataChanged = true;
		draw();
	}

	/**
	 * Gets this Plot's configuration data.
	 * 
	 * @return this Plot's configuration data.
	 */

	public PlotConfiguration getPlotConfiguration() {
		return tilePlot.getPlotConfiguration();
	}

	/**
	 * Gets a BufferedImage of the plot.
	 * 
	 * @return a BufferedImage of the plot.
	 */

	public BufferedImage getBufferedImage() {
		return getBufferedImage(getWidth(), getHeight());
	}
	
	public void setAnimationHandler(ActionListener listener) {
		super.setAnimationHandler(listener);
		forceBufferedImage = true;
	}

	/**
	 * Gets a BufferedImage of the plot.
	 * 
	 * @param width
	 *            the width of the image in pixels
	 * @param height
	 *            the height of the image in pixels
	 * @return a BufferedImage of the plot.
	 */

	public BufferedImage getBufferedImage(int width, int height) {
		bImage = null;
		forceBufferedImage = true;
		draw();
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < 2000 && bImage == null )
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Logger.error("Caught exception waiting for buffered image", e);
				break;
			}
		return bImage;
	}
	
	public Graphics2D getBufferedImage(Graphics2D g) {
		exportGraphics = g;
		bImage = null;
		forceBufferedImage = true;
		draw();
		long start = System.currentTimeMillis();
		while (System.currentTimeMillis() - start < 2000 && bImage == null )
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Logger.error("Caught exception waiting for buffered image", e);
				break;
			}
		exportGraphics = null;
		return g;
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

	@Override
	public int print(Graphics g, PageFormat pf, int pageIndex)
			throws PrinterException {
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
		double origRatio = (double) getWidth() / (double) getHeight();
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

	private void setDataRanges() {
		DataRangeDialog dialog = new DataRangeDialog("Set Longitude and Latitude Ranges",
				MeshPlot.this, firstColumn + columnOrigin, lastColumn + columnOrigin, firstRow + rowOrigin, lastRow + rowOrigin);
		dialog.showDialog();
	}

	private class DataRangeDialog extends JDialog {
		private static final long serialVersionUID = -1110292652911018568L;
		public static final int CANCEL_OPTION = -1;
		public static final int YES_OPTION = 1;
		public static final int ERROR = 0;
		private JTextField fLonField;
		private JTextField lLonField;
		private JTextField fLatField;
		private JTextField lLatField;
		private boolean cancelled = false;
		private int lonLow, lonHigh, latLow, latHigh;

		public DataRangeDialog(String title, MeshPlot plot, int lonLow,
				int lonHigh, int latLow, int latHigh) {
			super.setTitle(title);
			super.setLocation(getCenterPoint(plot));
			super.setModal(true);
			super.setPreferredSize(new Dimension(400, 300));
			this.lonLow = lonLow;
			this.lonHigh = lonHigh;
			this.latLow = latLow;
			this.latHigh = latHigh;
			this.fLonField = new JTextField("1", 4);
			this.lLonField = new JTextField("1", 4);
			this.fLatField = new JTextField("1", 4);
			this.lLatField = new JTextField("1", 4);
			this.getContentPane().add(createLayout());
		}

		public int showDialog() {
			this.pack();
			Point p = getLocation();
			setLocation(0, p.y);
			this.setVisible(true);

			if (this.cancelled)
				return CANCEL_OPTION;

			try {
				lonLow = Integer.valueOf(fLonField.getText());
				lonHigh = Integer.valueOf(lLonField.getText());
				latLow = Integer.valueOf(fLatField.getText());
				latHigh = Integer.valueOf(lLatField.getText());
				if (lonLow > lonHigh) {
					int temp = lonLow;
					lonLow = lonHigh;
					lonHigh = temp;
				}
				if (latLow > latHigh) {
					int temp = latLow;
					latLow = latHigh;
					latHigh = temp;
				}
				
				if (dataRatio > 1) {		
					screenWidth = canvasSize;
					screenHeight = (int)Math.round(screenWidth / dataRatio);
				}
				else {
					screenHeight = canvasSize;
					screenWidth = (int)Math.round(screenHeight * dataRatio);
				}
				
				previousPanX = -1;
				previousPanY = -1;
				panX = 0;
				panY = 0;
				zoomFactor = 1;
				compositeFactor = screenWidth / dataWidth;
				
				int x = (int)Math.round((lonLow - columnOrigin) / RAD_TO_DEG / dataWidth * screenWidth);
				int y = (int)Math.round((dataHeight - (latHigh - rowOrigin) / RAD_TO_DEG) / dataHeight * screenHeight);
				int width = (int)Math.round((lonHigh - lonLow) / RAD_TO_DEG / dataWidth * screenWidth);
				int height = (int)Math.round((latHigh - latLow) / RAD_TO_DEG / dataHeight * screenHeight);	

				
				Rectangle rect = new Rectangle(x, y, width, height);
				
				zoom(false, true, false, false, false, rect);

				return YES_OPTION;
			} catch (NumberFormatException e) {
				Logger.error("Number Format Exception in FastTilePlot.showDialog: Set Rows and Columns: " + e.getMessage());
			}

			return ERROR;
		}

		private JPanel createLayout() {
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

			panel.add(createMiddlePanel());
			panel.add(createButtonsPanel());

			return panel;
		}

		private JPanel createMiddlePanel() {
			GridBagLayout gridbag = new GridBagLayout();
			GridBagConstraints c = new GridBagConstraints();
			JPanel contentPanel = new JPanel(gridbag);

			c.fill = GridBagConstraints.HORIZONTAL;
			c.weightx = 1.0;
			c.insets = new Insets(1, 1, 7, 5);

			JLabel lonLabel = new JLabel("Longitude:");
			JPanel lonPanel = new JPanel();
			lonPanel.add(fLonField, BorderLayout.LINE_START);
			fLonField.setText(Integer.toString(this.lonLow));
			lonPanel.add(new JLabel("..."));
			lonPanel.add(lLonField, BorderLayout.LINE_END);
			lLonField.setText(Integer.toString(this.lonHigh));
			JLabel holder1 = new JLabel();

			gridbag.setConstraints(lonLabel, c);
			gridbag.setConstraints(lonPanel, c);
			c.gridwidth = GridBagConstraints.REMAINDER; // end row
			gridbag.setConstraints(holder1, c);
			contentPanel.add(lonLabel);
			contentPanel.add(lonPanel);
			contentPanel.add(holder1);

			c.gridwidth = 1; // next-to-last in row

			JLabel latLabel = new JLabel("Latitude:");
			JPanel latPanel = new JPanel();
			latPanel.add(fLatField, BorderLayout.LINE_START);
			fLatField.setText(Integer.toString(this.latLow));
			latPanel.add(new JLabel("..."));
			latPanel.add(lLatField, BorderLayout.LINE_END);
			lLatField.setText(Integer.toString(this.latHigh));
			JLabel holder2 = new JLabel();

			gridbag.setConstraints(latLabel, c);
			gridbag.setConstraints(lonPanel, c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			gridbag.setConstraints(holder2, c);
			contentPanel.add(latLabel);
			contentPanel.add(latPanel);
			contentPanel.add(holder2);

			return contentPanel;
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

	/**
	 * Creates a popup menu for the panel.
	 * 
	 * @param properties
	 *            include a menu item for the chart property editor.
	 * @param save
	 *            include a menu item for saving the chart.
	 * @param print
	 *            include a menu item for printing the chart.
	 * @param zoom
	 *            include menu items for zooming.
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

	@Override
	public void updateTimeStep(int step) {
		processTimeChange = false;
		drawMode = DRAW_ONCE;
		dataChanged = true;
		timestep = firstTimestep + step;
		
		try {
			timeLayerPanel.setTime(timestep);
		} catch (Exception e) {
			Logger.error("Exception setting time step. Time step = " + timestep + ". Is this 1-based? " + e.getMessage());
		}
		
		drawOverLays();
		processTimeChange = true;

		forceDraw();
	}
	
	/**
	 * 
	 * Calculates min/max for visible cells
	 */
	protected void calculateVisibleMinMax() {
		// If not zoomed use default zoom
		if (zoomFactor == 1) {
			MinMaxInfo info = dataset.getTimestepMinMax(currentDataFrame, layer, timestep);
			if (info != null && info.getCompletion() == 100) {
				int index = info.getMinIndex();
				MeshCellInfo cell = null;
				if (index >= 0) {
					cell = cellsToRender[index];
					currentMinMaxCache[LEVELS_CACHE_MIN_VALUE] = info.getMin();
					currentMinMaxCache[LEVELS_CACHE_MIN_LON] = cell.getLon();
					currentMinMaxCache[LEVELS_CACHE_MIN_LAT] = cell.getLat();
				}
				index = info.getMaxIndex();
				if (index >= 0) {
					cell = cellsToRender[index];
					currentMinMaxCache[LEVELS_CACHE_MAX_VALUE] = info.getMax();
					currentMinMaxCache[LEVELS_CACHE_MAX_LON] = cell.getLon();
					currentMinMaxCache[LEVELS_CACHE_MAX_LAT] = cell.getLat();
				}
				//System.out.println("Updated min layer " + layer + " step " + timestep + " value " + currentMinMaxCache[LEVELS_CACHE_MIN_VALUE]);
				return;
			}
		}
		currentMinMaxCache[LEVELS_CACHE_MIN_LON] = Double.POSITIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_MAX_LON] = Double.NEGATIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_MIN_LAT] = Double.POSITIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_MAX_LAT] = Double.NEGATIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_MAX_VALUE] = Double.NEGATIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_MIN_VALUE] = Double.POSITIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_PERCENT_COMPLETE] = layerMinMaxCache[layer - firstLayer][LEVELS_CACHE_PERCENT_COMPLETE];

		for (LocalCellInfo cell : cellInfo) {
			if (cell.visible){
				MeshCellInfo meshCell = cell.getSource();
				double value = cell.getValue();
				if (value < currentMinMaxCache[LEVELS_CACHE_MIN_VALUE] && value > DataUtilities.BADVAL3 && value < DataUtilities.NC_FILL_FLOAT) {
					currentMinMaxCache[LEVELS_CACHE_MIN_VALUE] = value;
					currentMinMaxCache[LEVELS_CACHE_MIN_LAT] = meshCell.getLat();
					currentMinMaxCache[LEVELS_CACHE_MIN_LON] = meshCell.getLon();
				}
				if (value > currentMinMaxCache[LEVELS_CACHE_MAX_VALUE] && value > DataUtilities.BADVAL3 && value < DataUtilities.NC_FILL_FLOAT) {
					currentMinMaxCache[LEVELS_CACHE_MAX_VALUE] = value;
					currentMinMaxCache[LEVELS_CACHE_MAX_LAT] = meshCell.getLat();
					currentMinMaxCache[LEVELS_CACHE_MAX_LON] = meshCell.getLon();					
				}

			}
		}
	}
	
	public void calculateAverageLevels(){

		//if (((AreaTilePlot) tilePlot).mouseOverOK) {
		if (true) {
//			final int count = legendLevels.length;
			
			Logger.debug("calculateAverageLevels ");

			// calc range for this set of numbers
			double[] minmax = { 0.0, 0.0 };

			DepositionRange range = this.getGlobalDepositionRange();
			minmax[0] = range.averageMin;
			minmax[1] = range.averageMax;

			{
				// initialize colormap to these min max values
				minMax=new MinMax(minmax[0],minmax[1]);
				if ( map == null) {
					map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
				} else {
					map.setPalette(defaultPalette);
					map.setMinMax( minmax[0], minmax[1]);
				}
				map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
				config.putObject(TilePlotConfiguration.COLOR_MAP, map);


				Logger.debug("minmax: " + minmax[0] + " " + minmax[1]);
				updateColorMap(map);
			}
		}
	}

	public void calculateGridLevels(){

		//if (((AreaTilePlot) tilePlot).mouseOverOK) {
		if (true) {
//			final int count = legendLevels.length;
			
			Logger.debug("calculateGridLevels ");

			// calc range for this set of numbers
			double[] minmax = this.log ? this.logPlotMinMaxCache : this.plotMinMaxCache;

			{
				// initialize colormap to these min max values
				minMax=new MinMax(minmax[0],minmax[1]);
				if ( map == null) {
					map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
				} else {
					map.setPalette(defaultPalette);
					map.setMinMax( minmax[0], minmax[1]);
				}
				map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
				config.putObject(TilePlotConfiguration.COLOR_MAP, map);


				Logger.debug("minmax: " + minmax[0] + " " + minmax[1]);
				updateColorMap(map);
			}
			Logger.debug("minMax "+minMax.getMin()+" "+minMax.getMax());
		}
	}
	
	/**
	 * Gets the visible cells for this plot, and calculates min/max.
	 *
	 * @return the visible 
	 * cells for this plot.
	 */
	Set<Integer> cellIdList = new HashSet<Integer>();
	protected void findVisibleCells() {		
		cellIdList.clear();
		currentMinMaxCache[LEVELS_CACHE_MIN_LON] = Double.POSITIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_MAX_LON] = Double.NEGATIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_MIN_LAT] = Double.POSITIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_MAX_LAT] = Double.NEGATIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_MAX_VALUE] = Double.NEGATIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_MIN_VALUE] = Double.POSITIVE_INFINITY;
		currentMinMaxCache[LEVELS_CACHE_PERCENT_COMPLETE] = layerMinMaxCache[layer - firstLayer][LEVELS_CACHE_PERCENT_COMPLETE];
		int width = cellIdMap.getWidth();
		int height = cellIdMap.getHeight();
		for (int i = 0; i < width; ++i)
			for (int j = 0; j < height; ++j) {
				cellIdList.add(getCellIdByCoord(i, j));
			}
		for (LocalCellInfo cell : cellInfo) {
			MeshCellInfo meshCell = cell.getSource();
			if (cellIdList.contains(cell.getId()))
				cell.visible = true;
			else if (cell.lonTransformed[meshCell.getMinXPosition() ]>= 0 && cell.lonTransformed[meshCell.getMaxXPosition()] <= width && 
					cell.latTransformed[meshCell.getMinYPosition()] >=0 && cell.latTransformed[meshCell.getMaxYPosition()] <= height)
				cell.visible = true;
			if (cell.visible && meshCell.getLon() >= minLon && meshCell.getLon() <= maxLon && meshCell.getLat() <= maxLat && meshCell.getLat() >= minLat) { //&& zoomFactor != 1) {
				double value = cell.getValue();
				if (value < currentMinMaxCache[LEVELS_CACHE_MIN_VALUE] && value > DataUtilities.BADVAL3 && value < DataUtilities.NC_FILL_FLOAT) {
					currentMinMaxCache[LEVELS_CACHE_MIN_VALUE] = value;
					currentMinMaxCache[LEVELS_CACHE_MIN_LAT] = meshCell.getLat();
					currentMinMaxCache[LEVELS_CACHE_MIN_LON] = meshCell.getLon();
				} else if (value > currentMinMaxCache[LEVELS_CACHE_MAX_VALUE] && value > DataUtilities.BADVAL3 && value < DataUtilities.NC_FILL_FLOAT) {
					currentMinMaxCache[LEVELS_CACHE_MAX_VALUE] = value;
					currentMinMaxCache[LEVELS_CACHE_MAX_LAT] = meshCell.getLat();
					currentMinMaxCache[LEVELS_CACHE_MAX_LON] = meshCell.getLon();					
				}
			}
		}
		for (LocalCellInfo cell : splitCellInfo.keySet()) {
			MeshCellInfo meshCell = cell.getSource();
			if (cellIdList.contains(cell.getId()))
				cell.visible = true;
			else if (cell.lonTransformed[meshCell.getMinXPosition() ]>= 0 && cell.lonTransformed[meshCell.getMaxXPosition()] <= width && 
					cell.latTransformed[meshCell.getMinYPosition()] >=0 && cell.latTransformed[meshCell.getMaxYPosition()] <= height)
				cell.visible = true;
		}
	}
	
	/**
	 * Gets the MinMax points for this plot.
	 *
	 * @return the MinMax points for this plot.
	 */
	protected Set<MeshCellInfo> getMinCells() {
		Set<MeshCellInfo> minCells = new HashSet<MeshCellInfo>();
		double min = Double.POSITIVE_INFINITY;
		double value = 0;
		MPASDataFrameIndex index = new MPASDataFrameIndex(currentDataFrame);
		for (MeshCellInfo cell : cellsToRender) {
			value = cell.getValue(currentVariable, currentDataFrame, index, timestep - firstTimestep, layer - firstLayer);
			if (value == min && value > DataUtilities.BADVAL3 && value < DataUtilities.NC_FILL_FLOAT)
				minCells.add(cell);
			else if (value < min && value > DataUtilities.BADVAL3 && value < DataUtilities.NC_FILL_FLOAT) {
				min = value;
				minCells.clear();
				minCells.add(cell);
			}			
		}
		return minCells;
	}
	
	protected Set<MeshCellInfo> getMaxCells() {
		Set<MeshCellInfo> maxCells = new HashSet<MeshCellInfo>();
		double max = Double.NEGATIVE_INFINITY;
		double value;
		MPASDataFrameIndex index = new MPASDataFrameIndex(currentDataFrame);
		for (MeshCellInfo cell : cellsToRender) {
			value = cell.getValue(currentVariable, currentDataFrame, index, timestep - firstTimestep, layer - firstLayer);
			if (value == max && value > DataUtilities.BADVAL3 && value < DataUtilities.NC_FILL_FLOAT)
				maxCells.add(cell);
			else if (value > max && value > DataUtilities.BADVAL3 && value < DataUtilities.NC_FILL_FLOAT) {
				max = value;
				maxCells.clear();
				maxCells.add(cell);
			}			
		}
		return maxCells;
	}

	private void probe(Rectangle axisRect) {
		synchronized (lock) {
			Slice slice = new Slice();
			slice.setTimeRange(timestep - firstTimestep, 1);
			if (!hasNoLayer) slice.setLayerRange(layer - firstLayer, 1);
			Axes<DataFrameAxis> axes = getDataFrame().getAxes();
			final int probeFirstColumn = axisRect.x - axes.getXAxis().getOrigin(); 
			final int probeColumns = axisRect.width + 1;
			final int probeFirstRow = axisRect.y - axisRect.height - axes.getYAxis().getOrigin();
			final int probeRows = axisRect.height + 1;

			slice.setXRange( probeFirstColumn, probeColumns );
			slice.setYRange( probeFirstRow, probeRows );

			try {
				DataFrame subsection = null;

				//			boolean isLog = false;		// isLog is not used
				//double logBase = 10.0;
				ColorMap map = (ColorMap) config.getObject(TilePlotConfiguration.COLOR_MAP);
				if (map != null) {
					// set log related info
					ColorMap.ScaleType iType = map.getScaleType();
					if ( iType == ColorMap.ScaleType.LOGARITHM ) {
						//					isLog = true;
						logBase = map.getLogBase();
					}
				}			

				
				if ( statisticsMenu.getSelectedIndex() == 0 ) {
					subsection = getDataFrame().slice(slice);
				}

				probedSlice = slice;
				ProbeEvent ent = new ProbeEvent(this, subsection, slice, Formula.Type.TILE);	// 2014 fixed code not knowing what TILE meant
				ent.setIsLog( false); //isLog); // JIZHEN: always set to false, take log inside this class
				ent.setLogBase( logBase);
				eventProducer.fireProbeEvent(ent);//new ProbeEvent(this, subsection, slice, TILE));
			} catch (InvalidRangeException e) {
				Logger.error("Invalid Range Exception in FastTilePlot.Probe: " + e.getMessage());
			}
		}
	}

	private void requestTimeSeries(Collection<MeshCellInfo> cells, Formula.Type type) {
		
		int digits = MPASTilePlot.getDisplayPrecision(getCurrentWidthDeg());
		plotFormat.setMaximumFractionDigits(digits);
		plotFormat.setMinimumFractionDigits(digits);
		String label = "(" + plotFormat.format(getCurrentLonMinDeg()) + " - " + plotFormat.format(getCurrentLonMaxDeg()) + ", " + plotFormat.format(getCurrentLatMinDeg()) + " - " + plotFormat.format(getCurrentLatMaxDeg()) + ") ";
		TimeSeriesPlotRequest request = new TimeSeriesPlotRequest(null, null, type, label);
		for (MeshCellInfo cell : cells) {
			if (!getCellInfo(cell.getId()).visible)
				continue;
			Slice slice = new Slice();
			// slice needs to be in terms of the actual array indices
			// of the frame, but the axes ranges refer to the range
			// of the original dataset. So, the origin will always
			// be 0 and the extent is the frame's extent.
			slice.setTimeRange(0, timeAxis.getExtent());
			if (layerAxis != null) slice.setLayerRange(0, layerAxis.getExtent());
			slice.setCellRange(cell.getId(), 1);
			
			try {
				DataFrame subsection = new MPASPlotDataFrame(label, currentVariable.getArray(), slice, currentDataFrame.getVariable(), currentDataFrame.getAxes(), dataset);
				request.addItem(subsection, true);
			} catch (InvalidRangeException e1) {
				Logger.error("InvalidRangeException in FastTilePlot.requestTimeSeries: " + e1.getMessage());
			}
		}
		eventProducer.firePlotRequest(request);
	}
	
	private void requestTimeSeries(Collection<MeshCellInfo> cells, String title) {
		MultiTimeSeriesPlotRequest request = new MultiTimeSeriesPlotRequest(title);
		for (MeshCellInfo cell : cells) {
			Slice slice = new Slice();
			// slice needs to be in terms of the actual array indices
			// of the frame, but the axes ranges refer to the range
			// of the original dataset. So, the origin will always
			// be 0 and the exent is the frame's exent.
			slice.setTimeRange(0, getDataFrame().getAxes().getTimeAxis().getExtent());
			DataFrameAxis layerAxis = getDataFrame().getAxes().getZAxis();
			if (layerAxis != null) slice.setLayerRange(0, layerAxis.getExtent());
			slice.setCellRange(cell.getId(), 1);

			try {
				String label = formatLatLon(cell.getLon(), cell.getLat(), cell.getId(), false, 0, 0);
				DataFrame subsection = new MPASPlotDataFrame(label, currentVariable.getArray(), slice, currentDataFrame.getVariable(), currentDataFrame.getAxes(), dataset);

			
				request.addItem(subsection);
			} catch (InvalidRangeException e1) {
				Logger.error("InvalidRangeException in FastTilePlot.requestTimeSeries", e1);
			}
		}
		eventProducer.firePlotRequest(request);
	}

	public Decidegrees getLatLonFor(int screenx,int screeny){
		int yHeightOffset = (int)(dataArea.getHeight() + dataArea.getY());
		int xOffset=(int)dataArea.getX();
		int width=(int)dataArea.getWidth();
		int height=(int)dataArea.getHeight();
		    
		double xMinimum = gridBounds[ 0 ][ 0 ];
		double xMaximum = gridBounds[ 0 ][ 1 ];
		double yMinimum = gridBounds[ 1 ][ 0 ];
		double yMaximum = gridBounds[ 1 ][ 1 ];
		
		double xRange   = xMaximum - xMinimum;
		double yRange   = yMaximum - yMinimum;
		double xScale   = width  / xRange;
		double yScale   = height / yRange;
		  
		double tx = (screenx-xOffset+0.5)/xScale+xMinimum;
		double ty = -(screeny-yHeightOffset+0.5)/yScale+yMinimum;					
		double[] longitudeLatitude = { 0.0, 0.0 };
		
		projector.unproject(tx, ty, longitudeLatitude);
		
		return new Decidegrees(longitudeLatitude[1], longitudeLatitude[0]);
	}
	
	public String createAreaString(int[] point) {
		Point4i p = new Point4i();
		p.w = point[0];
		p.z = point[1];
		p.x = point[2];
		p.y = point[3];
		return formatPointLatLon(p);
	}
	
	public String formatPoint(Point4i point) {
		StringBuilder builder = new StringBuilder("(");
		int[] vals = new int[4];
//		vals[0] = point.getW();
//		vals[1] = point.getZ();
//		vals[2] = point.getX();
//		vals[3] = point.getY();
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
	
	
	public static void debugMain(String[] args) {
		//ARGB String: Color 1 -16777194 color 2 -1677717 from 22 / 44
		//BufferedImage imgMap = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		//BufferedImage imgMap = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_3BYTE_BGR);
		BufferedImage imgMap = new java.awt.image.BufferedImage(10, 10, java.awt.image.BufferedImage.TYPE_INT_RGB);
		Graphics g = imgMap.getGraphics();
		g.setColor(new Color(22));
		g.fillRect(0,  0,  2,  2);
		g.setColor(new Color(44));
		g.fillRect(2, 0, 2, 2);
		g.setColor(new Color(199990));
		g.fillRect(3, 0, 2, 2);
		//System.out.println("Color 1 " + (imgMap.getRGB(0,  0) + COLOR_BASE) + " color 3 " + (imgMap.getRGB(3, 0) + COLOR_BASE));
		//System.out.println("Color 1 " + imgMap.getRGB(0,  0) + " color 3 " + imgMap.getRGB(3, 0));

	}
	
	private int getCellIdByCoord(int x, int y) {
		return cellIdMap.getRGB(x, y) + COLOR_BASE - 1;
	}
	
	private String formatPointLatLon(Point4i point) {
		double lonCoord = ((point.x / compositeFactor) + panX) * RAD_TO_DEG + columnOrigin;
		double latCoord = (dataHeight - ((point.y / compositeFactor)  + panY )) * RAD_TO_DEG + rowOrigin;
		try {
			int hoveredId = getCellIdByCoord(point.x, point.y);
			return formatLatLon(lonCoord, latCoord, hoveredId, true, point.x, point.y);
		}
		//This usually happens right after a zoom (screenHeight changes) and before the new dataArea.setRect() has been called.
		//Either synchronize resizes (not ideal) or ignore the exception.  Doesn't indicate a real problem unless hovering over 
		//the map produces a blank status area.
		catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}
	
	private String formatLatLon(double lonCoord, double latCoord, int hoveredId, boolean extendedFormat, int xCoord, int yCoord) {	

		String ret = "(" + coordFormat.format(lonCoord) + "\u00B0, " + coordFormat.format(latCoord) + "\u00B0";
		if (!extendedFormat)
			return ret + ")";
		//ret += " xy " + xCoord + "," + yCoord;

		try {
			LocalCellInfo localCell = getCellInfo(hoveredId);
			if (localCell == null) //Hovered over an unpainted space, where the model did not include a cell
				return "";
			MeshCellInfo cell = localCell.getSource();
			double value;
			if (preStatIndex < 1)
				value = cell.getValue(currentVariable, currentDataFrame, hoverCellIndex, timestep - firstTimestep, layer - firstLayer);
			else
				value = statisticsData[preStatIndex - 1][0][cell.getId()];
			if (value < DataUtilities.BADVAL3 || value > DataUtilities.NC_FILL_FLOAT)
				value = DataUtilities.BADVAL3;
			if (cell != null) {
				//TODO - Add the cell.getId() line for testing
				//ret += " " + cell.getId() + " ";
				ret += " " + cell.getElevation(layerAxisName, layer, timestep) + ") ";
				if (renderWind)
					ret += valueFormat.format(localCell.windVelocity) + "m/s" + " " + valueFormat.format(localCell.windAngle) + "\u00B0 ";
				ret += variable + " " + valueFormat.format(value) + unitString;
				//ret += ") " + cell.getId() + " " + " " + valueFormat.format(cell.getValue()) + " c " + coordFormat.format(cell.lon) + "," + coordFormat.format(cell.lat) + " " + point.x + "," + point.y;

			}
		} catch (ArrayIndexOutOfBoundsException e) {
			ret += "ArrayIndexOutOfBoundsException";
		}
		return ret;
	}
	
	protected Point2D getLatLonForAxisPoint(Point axisPoint) {
		//Since the netcdf boxer used middle of the grid as origin of the grid
		//FastTilePlot use SW corner as the origin of the grid, hence the minus 1
//		return getDataFrame().getAxes().getBoundingBoxer().axisPointToLatLonPoint(axisPoint.x-1, axisPoint.y-1); 
		return getDataFrame().getAxes().getBoundingBoxer().axisPointToLatLonPoint(axisPoint.x, axisPoint.y); //NOTE: the shift has been considered for in the netcdf boxer!!!
	}
	
	// get the JMapPane portion (the mapping rectangle containing the raster and shapefiles) of the FastTilePlot
	public JMapPane getMapPane()
	{
		return null;
	}
	
	boolean popupShown = false;
	boolean popupHiding = false;
	
	class AreaFinder extends MouseInputAdapter {
		
		private Point mpStart, mpEnd;
		boolean drawDeferred = false;

		// this rect measured axis coordinates
		private Rectangle rect;

		public void mousePressed(MouseEvent e) {
			synchronized (this) {
				if (popupShown) {
					return;
				}
	
				if (isInDataArea(e)) {
					mpStart = new Point(getCol(e.getPoint()), getRow(e.getPoint()));
					rect = new Rectangle(mpStart, new Dimension(0, 0));
					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect),
									false));
				} else {
					mpStart = null;
				}
			}
		}
		
		/* 
		 * Rendering errors occur if draw happens while mouse drag is happening.  If mouse is down, set
		 * drawDeferred to true, callers will know not to draw, and draw will happen when exiting data area
		 * or selection is made
		*/
		public boolean deferredDraw() {
			drawDeferred = mpStart != null;
			return drawDeferred;
		}

		public void mouseDragged(MouseEvent e) {
			if (mpStart != null) {
				if (isInDataArea(e)) {
					mpEnd = new Point(getCol(e.getPoint()), getRow(e.getPoint()));
					rect.width = mpEnd.x - rect.x;
					rect.height = rect.y - mpEnd.y;
					
					boolean finished = rect.width < 0 || rect.height < 0;
					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect),
									finished));
				} else {
					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, true));
				}
			}
		}

		public void mouseMoved(MouseEvent e) {
			if (popupHiding) {
				popupHiding = false;
				popupShown = false;
				rubberband.setActive(true);
				return;
			}
			if (isInDataArea(e)) {
				Point p = new Point(getCol(e.getPoint()), getRow(e.getPoint()));
				Rectangle rect = new Rectangle(p.x, p.y, 0, 0);
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect), false));
			} else {
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(new Rectangle(0, 0, 0, 0), true));
			}
		}

		public void mouseExited(MouseEvent e) {
			eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(new Rectangle(0, 0, 0, 0), true));
			mpStart = null;
			if (drawDeferred)
				draw();
		}

		public void mouseReleased(MouseEvent e) {
			if (popupHiding) {
				popupHiding = false;
				popupShown = false;
				rubberband.setActive(true);
				return;
			}
			if (mpStart != null) {
				if (isInDataArea(e)) {
					mpEnd = new Point(getCol(e.getPoint()), getRow(e.getPoint()));
					rect.width = mpEnd.x - rect.x;
					rect.height = mpEnd.y - rect.y;

					if (probe)
						probe(rect);
					else {
						int mod = e.getModifiers();
						int mask = MouseEvent.BUTTON3_MASK;
						boolean rightclick = (mod & mask) != 0;
						
						zoom(rightclick, !rightclick, false, mpEnd.x < mpStart.x || mpEnd.y < mpStart.y, false, rect);
					}
				}
				mpStart = null;
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, true));
			}
		}

		private String createAreaString(Rectangle rect) {
			Point4i[] points = rectToPoints(rect);
			return createLonLatAreaString(points);
		}
		
		private Point4i[] rectToPoints(Rectangle rect) {
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
		
		// rect is in axis coords
		/*private String createAxisCoordAreaString(Point4i[] points) {
			return createLonLatAreaString(points);
		}*/
		
		// rect is in axis coordinates
		private String createLonLatAreaString(Point4i[] points) {
			StringBuilder builder = new StringBuilder();
			builder.append(formatPointLatLon(points[0]));
			if (points[1] != null) {
				builder.append(" - ");
				builder.append(formatPointLatLon(points[1]));
			}
			return builder.toString();
		}
	}

//	2014 called from anl.verdi.plot.util.PlotExporter; disabling exporting of Shapefiles for VERDI v1.5.0
//		public void exportShapefile( String baseFileName ) throws IOException {
//		final int subsetLayerRows = 1 + lastRow - firstRow;
//		final int subsetLayerColumns = 1 + lastColumn - firstColumn;
//		// Filter variable name/expression so operators aren't a problem in Excel:
//		
//		// changed to this in v. 529
//		final int end = variable.indexOf( '[' );
//		final String filteredVariableName = variable.substring( 0, end );
//		
//		// change back now 2012-06-14
////		final String filteredVariableName =
////			variable.replaceAll( "[\\[\\d\\]]", "" ).replaceAll( "\\W", "" );
//		
//		final double subsetWestEdge = westEdge + firstColumn * cellWidth;
//		final double subsetSouthEdge = southEdge + firstRow * cellWidth;
//		GridShapefileWriter.write( baseFileName,
//				subsetLayerRows, subsetLayerColumns,
//				subsetWestEdge, subsetSouthEdge,
//				cellWidth, cellHeight,
//				filteredVariableName, subsetLayerData, projector );
//	}
	
	public void exportASCIIGrid( String baseFileName ) {
		final double subsetWestEdge = westEdge + firstColumn * cellWidth;
		final double subsetSouthEdge = southEdge + firstRow * cellWidth;
		float[][] exportCellData = new float[1][cellsToRender.length];
		
		MPASDataFrameIndex index = new MPASDataFrameIndex(currentDataFrame);
		for ( int cell = 0; cell < cellsToRender.length; ++cell ) {
			exportCellData[0][cell] = (float)cellsToRender[cell].getValue(currentVariable, currentDataFrame, index, timestep - firstTimestep, layer - firstLayer);
			if (exportCellData[0][cell] < DataUtilities.BADVAL3 || exportCellData[0][cell] > DataUtilities.NC_FILL_FLOAT)
				exportCellData[0][cell] = (float)DataUtilities.BADVAL3;
		}
		ASCIIGridWriter.write( baseFileName + ".asc",
		1, cellsToRender.length,
		subsetWestEdge, subsetSouthEdge,
		cellWidth, exportCellData );
	}

//	public void exportASCIIGrid( String baseFileName ) {
//		final int subsetLayerRows = 1 + lastRow - firstRow;
//		final int subsetLayerColumns = 1 + lastColumn - firstColumn;
//		ASCIIGridWriter.write( baseFileName + ".asc",
//								subsetLayerRows, subsetLayerColumns,
//								westEdge, southEdge,
//								cellWidth, subsetLayerData );
//	}

	public void exportEPSImage(String filename) {
		int width = (getWidth() <= 0) ? 800 : getWidth();
		int height = (getHeight() <= 0) ? 600 : getHeight();
		
		exportEPSImage(filename, getBufferedImage().getWidth(), getBufferedImage().getHeight());
	}
	
	public void exportEPSImage(String filename, int width, int height) {
		EpsRenderer renderer = new EpsRenderer(width, height);
		EpsTools.createFromDrawable(renderer, filename, width, height, ColorMode.COLOR_RGB);
	}
	
	class EpsRenderer implements Drawable {
		final int canvasWidth, canvasHeight;
		
		public EpsRenderer(int width, int height) {
			canvasWidth = width;
			canvasHeight = height;
		}
		
		@Override
		public void draw(Graphics2D g, Rectangle2D rect) {
			getBufferedImage(g);
		}
		
	}
	
	public void addVectorAnnotation(VectorEvaluator eval) {
		vectAnnotation = new VectorAnnotation(eval, timestep, getDataFrame().getAxes().getBoundingBoxer());
		draw();
	}

	public void addObservationData(DataManager manager, boolean showLegend) {
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
					if (subtitle1.indexOf(temp) < 0 && !subtitles.contains(temp)) subtitles.add(temp);
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
			drawMode = DRAW_NONE;
		}
	}
	
	public List<OverlayObject> getObservationData() {
		return this.obsData;
	}
	
	private void drawOverLays() {
		try {
			if (obsAnnotations != null)  {
				for (ObsAnnotation ann : obsAnnotations) 
					ann.update(timestep);
			}
			
			if (vectAnnotation != null) {
				vectAnnotation.update(timestep);
			}
		} catch (Exception e) {
			setOverlayErrorMsg(e.getMessage());
			drawMode = DRAW_NONE;
		}
	}
	
	private void setOverlayErrorMsg(String msg) {
		if (msg == null) msg = "";
		JOptionPane.showMessageDialog(app.getGui().getFrame(), "Please check if the overlay time steps match the underlying data.\n" + msg, "Overlay Error", JOptionPane.ERROR_MESSAGE, null);
	}

	public String getTitle() {
		return tilePlot.getTitle();
	}
	
	public MPASTilePlot getTilePlot() {
		return tilePlot;
	}
	
	public DataFrame createLogDataFrame(DataFrame frame) {
		DataFrameBuilder builder = new DataFrameBuilder();
		builder.addDataset(dataset);
		builder.setVariable(frame.getVariable());
		builder.setArray(ArrayLogFactory.getArray(frame.getArray(), this.logBase));
		Axes<DataFrameAxis> axes = frame.getAxes();
		if (axes.getTimeAxis() != null)
			builder.addAxis(DataFrameAxis.createDataFrameAxis(axes.getTimeAxis(), axes.getTimeAxis().getArrayIndex()));
		if (axes.getZAxis() != null)
			builder.addAxis(DataFrameAxis.createDataFrameAxis(axes.getZAxis(), axes.getZAxis().getArrayIndex()));
		if (axes.getXAxis() != null)
			builder.addAxis(DataFrameAxis.createDataFrameAxis(axes.getXAxis(), axes.getXAxis().getArrayIndex()));
		if (axes.getYAxis() != null)
			builder.addAxis(DataFrameAxis.createDataFrameAxis(axes.getYAxis(), axes.getYAxis().getArrayIndex()));
		if (axes.getCellAxis() != null)
			builder.addAxis(DataFrameAxis.createDataFrameAxis(axes.getCellAxis(), axes.getCellAxis().getArrayIndex()));
		return builder.createDataFrame();
	}


	//Only read single timestep at a time
	private void calculateDataFrameLog() {
		if ( this.dataFrame == null) {
			return;
		}
				
		this.dataFrameLog = createLogDataFrame( this.dataFrame);
 
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
	
	public void showBusyCursor() {
		//  show busy cursor
		if (app!=null && app.getGui()!=null) {
			app.getGui().busyCursor(); 
		}
		synchronized(this) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}
	}
	
	public void restoreCursor() {
		if (app != null)
		//   cursor restored
		//synchronized(this) {
			app.getGui().defaultCursor(); 
//		}
		if ( zoom) 
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		else 
			setCursor(Cursor.getDefaultCursor());
	}
	
	private void increaseDrawOnceRequests() {
		draw_once_requests ++;
		//System.err.println("Increased requests to " + draw_once_requests + " from " + Thread.currentThread().getStackTrace()[3]);
	}
	
	private void decreaseDrawOnceRequests() {
		draw_once_requests --;
		//System.err.println("Decreased requests to " + draw_once_requests);
	}
	
	private int getDrawOnceRequests() {
		//System.err.println("Got " + draw_once_requests + " requests from " + Thread.currentThread().getStackTrace()[3]);
		return draw_once_requests;
	}
	
	double[][] layerMinMaxCache = null;
	double[] currentMinMaxCache = null;
	double[] plotMinMaxCache = null;
	double[] statMinMaxCache = null;
	double[][] logLayerMinMaxCache = null;
	double[] logPlotMinMaxCache = null;
	
	boolean layerCacheInitted = false;
	
	public static final int LEVELS_CACHE_MIN_VALUE = 0;
	public static final int LEVELS_CACHE_MIN_LON = 1;
	public static final int LEVELS_CACHE_MIN_LAT = 2;
	public static final int LEVELS_CACHE_MAX_VALUE = 3;
	public static final int LEVELS_CACHE_MAX_LON = 4;
	public static final int LEVELS_CACHE_MAX_LAT = 5;
	public static final int LEVELS_CACHE_PERCENT_COMPLETE = 6;
	public static final int PLOT_CACHE_PERCENT_COMPLETE = 2;

	@Override
	public void datasetUpdated(double min, int minIndex, double max, int maxIndex, double percentComplete, boolean isLog) {
		double[] updatedInfo = plotMinMaxCache;
		if (isLog)
			updatedInfo = logPlotMinMaxCache;
		updatedInfo[0] = min;
		updatedInfo[1] = max;
		updatedInfo[2] = percentComplete;
		if (map != null) {
			if (isLog)
				map.setLogMinMax(min, max);
			else
				map.setMinMax(min, max);
		}
		
			
		//if (preStatIndex < 1) {
		if (isLog == log) {
			updateLegendLevels();
			if (drawMode == DRAW_NONE) {
				drawMode = DRAW_ONCE;
			}
			//System.out.println("Legend " + pctComplete + "% complete, min " + min + " max " + max + " log " + isLog + " redrawn");
		}
		if (finder == null)
			return;
		synchronized (finder) {
			if (!finder.deferredDraw()) {
				draw();
			}
		}
	}
	
	private void updateLegendLevels() {
		ColorMap colorMap = map;
		if (colorMap == null || config == null)
			return;
		
		synchronized (legendLock) {
		//populate legend colors and ranges on initiation
		//default to not a log scale
		double[] localMinMax = { 0.0, 0.0 };
		double[] localLogMinMax = { 0.0, 0.0 };
		if (this.preStatIndex < 1) {
			localMinMax[0] = plotMinMaxCache[0];
			localMinMax[1] = plotMinMaxCache[1];
		} else {
			computeDataRange(false);
			localMinMax[0] = statMinMaxCache[0];
			localMinMax[1] = statMinMaxCache[1];
		}
		
		
		//set min/max for both log and non log values...
		map.setMinMax( localMinMax[0], localMinMax[1]);
		if (this.preStatIndex < 1) {
			localLogMinMax[0] = logPlotMinMaxCache[0];
			localLogMinMax[1] = logPlotMinMaxCache[1];
		}
		else {
			computeDataRange(true);
			localLogMinMax[0] = statMinMaxCache[0];
			localLogMinMax[1] = statMinMaxCache[1];
		}
		map.setLogMinMax( localLogMinMax[0], localLogMinMax[1]);
		try {
			valueFormat = map.getNumberFormat();
		} catch (Exception e) {
			e.printStackTrace();
		}
		//this final one is for the below legend value calculations
		if (this.log)
			localMinMax = localLogMinMax;

		legendColors = defaultPalette.getColors();
		final double minimum = localMinMax[0];
		final double maximum = localMinMax[1];
		minMax = new DataUtilities.MinMax(minimum, maximum);
		int count = legendColors.length + 1;
		final double delta = (maximum - minimum) / (count - 1);
		legendLevels = new double[count];
		for (int level = 0; level < count; ++level) {
			legendLevels[level] = minimum + level * delta;
		}
		if (drawMode == DRAW_NONE) {
			drawMode = DRAW_ONCE;
			draw_once_requests = 1;
		}
		}
		config.setUnits("");
		dataChanged = true;
	}
	
	private void initLayerCache(int maxLayer) {
		for (int i = 0; i < maxLayer; ++i) {
			MinMaxInfo info = dataset.getLayerMinMax(currentDataFrame, i, this);
			int minIndex = info.getMinIndex();
			if (minIndex < 0) //Has not been calculated yet - break and wait for notifications
				break;
			MeshCellInfo cell = cellsToRender[info.getMinIndex()];
			layerMinMaxCache[i][LEVELS_CACHE_MIN_VALUE] = info.getMin();
			layerMinMaxCache[i][LEVELS_CACHE_MIN_LON] = cell.getLon();
			layerMinMaxCache[i][LEVELS_CACHE_MIN_LAT] = cell.getLat();
			cell = cellsToRender[info.getMaxIndex()];
			layerMinMaxCache[i][LEVELS_CACHE_MAX_VALUE] = info.getMax();
			layerMinMaxCache[i][LEVELS_CACHE_MAX_LON] = cell.getLon();
			layerMinMaxCache[i][LEVELS_CACHE_MAX_LAT] = cell.getLat();
		}
	}

	@Override
	public void layerUpdated(int updLayer, double min, int minIndex, double max, int maxIndex,
			double percentComplete, boolean isLog) {
		double[][] localCache = layerMinMaxCache;
		if (isLog)
			localCache = logLayerMinMaxCache;
		if (minIndex >= 0)
			localCache[updLayer][LEVELS_CACHE_MIN_VALUE] = min;
		if (maxIndex >= 0)
			localCache[updLayer][LEVELS_CACHE_MAX_VALUE] = max;
		localCache[updLayer][LEVELS_CACHE_PERCENT_COMPLETE] = percentComplete;
		currentMinMaxCache[LEVELS_CACHE_PERCENT_COMPLETE] = percentComplete;
		if (cellsToRender == null || cellsToRender[cellsToRender.length - 1] == null) //not until a few ms after this is 1st called
			return;
		if (minIndex >= 0) {
			localCache[updLayer][LEVELS_CACHE_MIN_LON] = cellsToRender[minIndex].getLon();
			localCache[updLayer][LEVELS_CACHE_MIN_LAT] = cellsToRender[minIndex].getLat();
		}
		if (maxIndex >= 0) {
			localCache[updLayer][LEVELS_CACHE_MAX_LON] = cellsToRender[maxIndex].getLon();
			localCache[updLayer][LEVELS_CACHE_MAX_LAT] = cellsToRender[maxIndex].getLat();
		}
		if (!layerCacheInitted) { //Updates any layers that were modified between initial opening and first update notification - this happens when opening, closing, and re-opening a plot
			initLayerCache(updLayer);
			layerCacheInitted = true;
		}
	}

	@Override
	public long getRenderTime() {
		return renderTime;
	}

	@Override
	public boolean isAsyncListener() {
		return asyncEnabled;
	}

	@Override
	public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
	}

	@Override
	public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
		popupHiding = true;		
	}
	
	@Override
	public void popupMenuCanceled(PopupMenuEvent e) {		
	}
	
	private DepositionRange getGlobalDepositionRange() {

		if ( !depositionRangeAlreadySet ) {

			if (app != null)
				app.getGui().setStatusOneText("Calculating deposition range.");
			calcGlobalDepositionRange();
			// calc range for this set of numbers
			double[] minmax = { 0.0, 0.0 };

			minmax[0] = range.averageMin;
			minmax[1] = range.averageMax;

			// if(minMax==null || minMax.getMin()>minmax[0] || minMax.getMax()<minmax[1])
			{
				//System.out.println("computing average data minmax...");
				//			double[] minmax = { 0.0, 0.0 };
				//			computeDataRange(minmax);

				// initialize colormap to these min max values
				minMax=new MinMax(minmax[0],minmax[1]);
				if ( map == null) {
					map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
				} else {
					map.setPalette(defaultPalette);
					map.setMinMax( minmax[0], minmax[1]);
				}
				map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
				config.putObject(TilePlotConfiguration.COLOR_MAP, map);

				updateColorMap(map);
			}
		}

		return range;
	}
	
	public static MeshPlot createTestPlot() {
		DataLoader loader = null;
		try {
			loader = (DataLoader)Class.forName("anl.verdi.loaders.MPASLoader").newInstance();

		
			String path = "file:///home/verdi/data/mpas-orig.nc";
			List<Dataset> sets = loader.createDatasets(new URL(path));
			IMPASDataset ds = (IMPASDataset)sets.get(0);
			ds.setAlias("[1] mpas-orig.nc");
			DataReader reader = loader.createReader(sets.get(0));
			Variable pressure = ds.getVariable("pressure");
			DataFrame frame = reader.getValues(ds, null, pressure);


			return new anl.verdi.plot.gui.MeshPlot(null, frame, MODE_CROSS_SECTION);
			

		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private class CrossSectionRenderInfo {
		int startX;
		int startY;
		int startWidth;
		int startHeight;
		int timeStep;
		String fixedAxis;
		double sliceHeight;
		double startDegree;
		
		public CrossSectionRenderInfo(int startX, int startY, int startWidth, int startHeight, String fixedAxis, double sliceDataHeightDeg, double startDegree) {
			this.startX = startX;
			this.startY = startY;
			this.startWidth = startWidth;
			this.startHeight = startHeight;
			this.fixedAxis = fixedAxis;
			this.sliceHeight = sliceDataHeightDeg;
			this.startDegree = startDegree;
		}

		
		public boolean equals(CrossSectionRenderInfo info) {
			return info != null &&
					startX == info.startX &&
					startY == info.startY &&
					startWidth == info.startWidth &&
					startHeight == info.startHeight &&
					fixedAxis.equals(info.fixedAxis) &&
					sliceHeight == info.sliceHeight &&
					startDegree == info.startDegree;
		}
	}
	
	CrossSectionRenderInfo renderInfo = null;
	CrossSectionRenderInfo previousRenderInfo = null;
	
	/*
	 * 
	 * pxWidth            - width of area to display plot (not including labels, legend, etc
	 * pxHeight           - height of area to display plot (not including labels, legend, etc
	 * sliceDataHeightDeg - height in degrees of each slice
	 * numLayers          - number of layers
	 * startDegreee       - location to start plotting graph
	 * 
	 */
	public void initDisplayParameters(int startX, int startY, int startWidth, int startHeight, String fixedAxis, double sliceDataHeightDeg, int step, double startDegree) {
		previousRenderInfo = renderInfo;
		this.timestep = step;
		renderInfo = new CrossSectionRenderInfo(startX, startY, startWidth, startHeight, fixedAxis, sliceDataHeightDeg, startDegree);
		if (renderInfo.equals(previousRenderInfo))
			return;
		reverseAxes = !fixedAxis.equals("y");
		double yScale = 1;
		double xScale = 1;
		
		int imgWidth = startWidth;
		int imgHeight = startHeight;
		crossXOrigin = startX;
		crossYOrigin = startY;		
		
		displayHeight = startHeight;		
		layerHeight = startHeight / (double)layers;
		
		if (reverseAxes) {		
			
			double screenRatio = startWidth / (double)startHeight;

			imgHeight = startWidth;
			imgWidth = (int)Math.round(imgHeight * screenRatio);
			
			
			zoomFactor = imgHeight / dataset.getExactHeight();

			double displayLayerWidth = startHeight / (double)layers;
			double zoomedLayerWidth = (Math.PI * 2 * zoomFactor ) * sliceDataHeightDeg / 360;
			
			xScale = displayLayerWidth / zoomedLayerWidth;
			imgWidth = (int)Math.round(startHeight / (double)layers);
		
			rotatedImage = new BufferedImage(imgHeight, imgWidth, BufferedImage.TYPE_INT_RGB);


		}

		crossSectionImage = new BufferedImage(imgWidth, imgHeight, BufferedImage.TYPE_INT_RGB);


		screenWidth = imgWidth;
		screenHeight = imgHeight;
		double startRad = 0;
		

		//If y, account for axis numbers being inverted
		if (!reverseAxes) {
			//TODO - commented section expects unconverted values, try with converted
			/*
			startDegree += sliceDataHeightDeg;
			startDegree = startDegree * -1 + 90;
			*/
			//startDegree -= sliceDataHeightDeg;
			startDegree = 179 - startDegree;
			clippedDataRatio = screenWidth / (screenHeight / (double)layers);
		}
		
		startRad = startDegree / RAD_TO_DEG;
		
		panX = 0;
		panY = 0;
		
		if (reverseAxes)
			panX = startRad;
		else {
			yScale = (dataset.getExactHeight() * RAD_TO_DEG) / sliceDataHeightDeg / layers; 
			panY = startRad;
		}
		
		//TODO - investigate locChanged / dataChanged optimizations here if needed
		transformCells(screenWidth, 0, 0, xScale, yScale);
		
		/*for (int i = 0; i < cellsToRender.length; ++i) {
			LocalCellInfo cell = getCellInfo(i);
			for (int j = 0; j < cell.lonTransformed.length; ++j) {
				if (!cell.visible)
					continue;
			}
		}

		for (LocalCellInfo cell : splitCellInfo.keySet()) {
			for (int j = 0; j < cell.lonTransformed.length; ++j) {
				if (!cell.visible)
					continue;
			}
		}*/
		
		
	}
		
	private void buildTestPlotFrame() {
		
		layers = 41;
		double sliceWidth = 1;
		//TODO - conversion: x' = x * -1 + 90
		//double startDeg = -70;
		double startDeg = 3;
		String fixedAxis = "y";
		int startWidth = 700;
		int startHeight = 400;
		
		startDeg = 179;
		
		//TODO - uncomment this to test x axis
		layers = 41;
		sliceWidth = 1;
		//TODO - conversion: x' = x + 180
		//startDeg = -160;
		startDeg = 20;
		startDeg = 359;
		fixedAxis = "x";
		
		/*layers = 2;
		sliceWidth = 45;*/
		
		initDisplayParameters(0, 0, startWidth, startHeight, fixedAxis, sliceWidth, 0, startDeg);
		
		JFrame mainFrame = new JFrame("Mesh Debug");
        
		
        mainFrame.setSize(701, 400);
        final MeshPlot plot = this;
		JPanel panel = new JPanel() { 
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				plot.renderVerticalCrossSection(g);
			}	
		};
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.getContentPane().add(panel);
        panel.setSize(701, 410);
        //Frame is always 2 x 19 too small (linux bug?), expand dimensions to compensate
		mainFrame.setSize(713, 439);
        mainFrame.setVisible(true);
	}
	
	public void renderVerticalCrossSection(Graphics g) {
		Graphics2D targetGraphics = null;
		Graphics2D rotateGraphics = null;
		AffineTransform rotateTransform = null;
		AffineTransform targetTransform = null;
		g.setColor(Color.WHITE);
		if (reverseAxes) {
			rotateGraphics = crossSectionImage.createGraphics();
			rotateGraphics.setColor(Color.WHITE);
			rotateGraphics.fillRect(0,  0, crossSectionImage.getWidth(), crossSectionImage.getHeight());
			rotateTransform = rotateGraphics.getTransform();
			//rotateGraphics.translate(-1.0, -2.0);
			targetGraphics = (Graphics2D)g;
			targetTransform = targetGraphics.getTransform();
			targetGraphics.translate(crossXOrigin, crossYOrigin);
		}
		else {
			targetGraphics = crossSectionImage.createGraphics();
			targetGraphics.setColor(Color.WHITE);
			targetGraphics.fillRect(0,  0, crossSectionImage.getWidth(), crossSectionImage.getHeight());
			rotateGraphics = targetGraphics;
		}
						
		targetGraphics.translate(0, displayHeight);
		/*if (!reverseAxes) {
			//targetGraphics.translate(0, 0);
		}*/

		for (int i = 0; i < layers; ++i) {
			targetGraphics.translate(0,  layerHeight * -1);

			layer = i;
			updateCellColors();
	        //xOffset has to be nonzero - normally will be anyway
			
	        renderCells(rotateGraphics, 1, 0, true);
	        if (reverseAxes) {
				Graphics2D tmpImg  = rotatedImage.createGraphics();
				tmpImg.rotate(90 / RAD_TO_DEG);
				tmpImg.translate(0, -rotatedImage.getWidth());
				tmpImg.drawImage(crossSectionImage, 0, 0, null);
				
				//targetGraphics.drawImage(rotatedImage,  crossXOrigin,  crossYOrigin,  null);
				targetGraphics.drawImage(rotatedImage,  0,  0,  null);
				
				/*File io = new File("/tmp/out" + i + ".png");
				try {
					ImageIO.write(crossSectionImage, "png", io);
				} catch (IOException e) {
					e.printStackTrace();
				}*/
				
				/*
				File out = new File("/tmp/rot" + i + ".png");
				try {
					ImageIO.write(rotatedImage, "png", out);
				} catch (IOException e) {
					e.printStackTrace();
				}*/
				
				

	        }
	        
		}
		if (!reverseAxes) {
			targetGraphics = (Graphics2D)g;
			targetTransform = targetGraphics.getTransform();
			targetGraphics.translate(crossXOrigin, crossYOrigin);
			targetGraphics.drawImage(crossSectionImage,  0,  0,  null);
		}
		File io = new File("/tmp/out.png");
		try {
			ImageIO.write(crossSectionImage, "png", io);
		} catch (IOException e) {
			e.printStackTrace();
		}
		targetGraphics.setTransform(targetTransform);
		if (rotateTransform != null)
			rotateGraphics.setTransform(rotateTransform);
	}
	
	public static void main(String[] args) {

		MeshPlot plot = createTestPlot();
		plot.buildTestPlotFrame();
		
	}
	
	private MeshDataReader createDataReader() {
		MeshDataReader dataReader = new MeshDataReader(currentVariable, currentDataFrame, cellIndex, 0, 0);
		return dataReader;
	}

	private void calcGlobalDepositionRange() {
		//TODO - build data reader
		MeshDataReader reader = createDataReader();
		for (int timestep=0; timestep<this.timesteps; timestep++) {
			reader.setTimestep(timestep);
			for (int layer=0; layer<this.layers; layer++) {
				reader.setLayer(layer);
	    		MeshCellInfo[] cells = dataset.getAllCellsArray();
				calcFrameDepositionRange(cells, reader, gridNum, range);
			}
		}

		depositionRangeAlreadySet = true;
	}

	private void calcFrameDepositionRange(MeshCellInfo[] data, MeshDataReader reader, int gridIndex, DepositionRange range) {
		ArrayList polygons=Target.getTargets();
		
		TargetDeposition deposition = new TargetDeposition();
		for(Target polygon:(ArrayList<Target>)polygons){
			Target.setUnitConverters(units);
			polygon.computeAverageDeposition(data, reader, gridIndex, deposition);
			if (deposition.total > range.totalMax) {
				range.totalMax = deposition.total;
			}
			if (deposition.total < range.totalMin) {
				range.totalMin = deposition.total;
			}
			if (deposition.average > range.averageMax) {
				range.averageMax = deposition.average;
			}
			if (deposition.average < range.averageMin) {
				range.averageMin = deposition.average;
			}
		}
	}
	
	public void calculateTotalLevels(){
		//if (((AreaTilePlot) tilePlot).mouseOverOK) {
		if (true) {
			Logger.debug("calculateTotalLevels");

			// calc range for this set of numbers
			double[] minmax = { 0.0, 0.0 };
			DepositionRange range = this.getGlobalDepositionRange();
			minmax[0] = range.totalMin;
			minmax[1] = range.totalMax;

			// if never set before or if larger range than last set of numbers
			{
				Logger.debug("computing total data minmax...");

				// initialize colormap to these min max values
				minMax=new MinMax(minmax[0],minmax[1]);
				if ( map == null){
					map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
				} else {
					map.setPalette(defaultPalette);
					map.setMinMax( minmax[0], minmax[1]);
				}
				map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
				config.putObject(TilePlotConfiguration.COLOR_MAP, map);

				Logger.debug("minmax: " + minmax[0] + " " + minmax[1]);
				updateColorMap(map);
			}
			
		}
	}

	
	private DepositionRange range = new DepositionRange();
	boolean showSelectedOnly=false;
	
	public static final int AVERAGES=0; 
	public static final int TOTALS=1; 
	public static final int GRID=2; 
	protected int currentView=GRID;
	
	MapPolygon mapPolygon = null;
	
	int gridNum = -1;
	
	MeshDataReader renderReader = null;


}
