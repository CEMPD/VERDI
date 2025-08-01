/**
 * FastTilePlot - Fast alternative to the original VERDI TilePlot.
 * @author plessel.todd@epa.gov cathey.tommy@epa.gov 2008-09-16
 * @version $Revision$ $Date$
 **/

package anl.verdi.plot.gui;

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
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.GregorianCalendar;
//import java.util.Date;		// functions deprecated, replaced by GregorianCalendar
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.measure.unit.BaseUnit;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
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
import org.jogamp.vecmath.Point4i;	// 4 integers (x, y, z, w coordinates)
import org.jsoup.select.Evaluator;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.data.DataStoreFactorySpi;
import org.geotools.data.Query;
//import org.geotools.data.DefaultQuery;	// deprecated, replacing with Query
import org.geotools.data.shapefile.ShapefileDataStore;
//import org.geotools.data.shapefile.indexed.IndexedShapefileDataStoreFactory;	// deprecated
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.geometry.jts.ReferencedEnvelope;
//import org.geotools.map.DefaultMapLayer;	// deprecated, replacing with FeatureLayer
import org.geotools.map.FeatureLayer;
import org.geotools.referencing.CRS;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.geotools.swing.JMapPane;			// JEB Sept 2015; for mapping support in GeoTools
import org.geotools.swing.data.JFileDataStoreChooser;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.unitsofmeasurement.unit.Unit;

import com.vividsolutions.jts.geom.Envelope;

import anl.map.coordinates.Decidegrees;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiGUI;
import anl.verdi.data.Axes;
import anl.verdi.data.CoordAxis;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.DataManager;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.DataUtilities.MinMax;
import anl.verdi.data.Dataset;
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
import anl.verdi.plot.probe.PlotEventProducer;
import anl.verdi.plot.probe.ProbeEvent;
import anl.verdi.plot.types.TimeAnimatablePlot;
import anl.verdi.plot.util.FastTilePlotPrintAction;
import anl.verdi.plot.util.PlotExporter;
import anl.verdi.plot.util.PlotExporterAction;
import anl.verdi.util.Tools;
import anl.verdi.util.Utilities;
import anl.verdi.util.VUnits;
import gov.epa.emvl.ASCIIGridWriter;
import gov.epa.emvl.GridCellStatistics;
import gov.epa.emvl.GridShapefileWriter;
import gov.epa.emvl.Mapper;
import gov.epa.emvl.Numerics;
import gov.epa.emvl.Projector;
import gov.epa.emvl.SatelliteImageManager;
import gov.epa.emvl.TilePlot;
import net.sf.epsgraphics.ColorMode;
import net.sf.epsgraphics.Drawable;
import net.sf.epsgraphics.EpsTools;
import saf.core.ui.event.DockableFrameEvent;
import ucar.ma2.IndexIterator;
import ucar.ma2.InvalidRangeException;
import ucar.unidata.geoloc.LatLonPoint;
import ucar.unidata.geoloc.Projection;
import ucar.unidata.geoloc.ProjectionPoint;
import ucar.unidata.geoloc.projection.LatLonProjection;

public class FastTilePlot extends AbstractPlotPanel implements ActionListener, Printable,
		ChangeListener, ComponentListener, MouseListener,
		TimeAnimatablePlot, Plot {
	//private final MapContent myMapContent = new MapContent();	// JEB Nov 2015
	static final Logger Logger = LogManager.getLogger(FastTilePlot.class.getName());
	private static final long serialVersionUID = 5835232088528761729L;
	public static final int NO_VAL = Integer.MIN_VALUE;
	private static final String SATELLITE_LAYER = "SATELLITE";
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
	public static final int MINIMUM = 0;
	public static final int MAXIMUM = 1;
	public static final int LONGITUDE = 0;
	public static final int LATITUDE = 1;
	private static final double MINIMUM_VALID_VALUE = -900.0;
	
	// Log related
	
	protected boolean log = false;
	private boolean preLog = false;
	private double logBase = 10.0; //Math.E;	

	// 2D grid parameters:

	protected GregorianCalendar startDate;
	protected int timesteps; 
	protected int layers; 
	protected int rows;
	protected int columns;
	protected int rowOrigin;
	protected int columnOrigin;
	protected double westEdge; 		// meters from projection center
	protected double southEdge; 	// meters from projection center
	protected double cellWidth; 	// meters.
	protected double cellHeight; 	// meters.
	private NumberFormat format;
	private boolean invertRows; // HACK: Invert rows of AURAMS / GEM / CF Convention data?

	// For legend-colored grid cells and annotations:

	protected TilePlot tilePlot; // EMVL TilePlot.

	protected int prevTimestep = -1;
	protected int timestep = 0; // 0..timesteps - 1.
	protected int firstTimestep = 0;
	protected int lastTimestep = 0;
	protected int prevLayer = -1;
	protected int layer = 0; // 0..layers - 1.
	protected int firstLayer = 0;
	protected int lastLayer = 0;
	private int firstRow = 0; // 0..lastRow.
	private int lastRow = 0; // firstRow..rows - 1.
	private int firstColumn = 0; // 0..lastColumn.
	private int lastColumn = 0; // firstColumn..columns - 1.
	
	private int prevFirstRow = -1;
	private int prevLastRow = -1;
	private int prevFirstColumn = -1;
	private int prevLastColumn = -1;
	private int prevSelection = -1;
	private boolean prevLog = false;

	protected double[] legendLevels;

	protected Palette defaultPalette;
	private Color[] legendColors;
	//private ColorMap map;
	protected ColorMap map;

	private Color axisColor = Color.darkGray;
	private Color labelColor = Color.black;
	
	Graphics2D exportGraphics = null;
	
	private Envelope envelope = null;
	
	private String satellitePath = null;
	private String backgroundImage = null;
	private boolean displayBackground = false;


	// subsetLayerData[ 1 + lastRow - firstRow ][ 1 + lastColumn - firstColumn ]
	// at current timestep and layer.
	private double[][] subsetLayerData = null;
	private byte[][] colorIndexCache = null;

	// layerData[ rows ][ columns ][ timesteps ]
	private double[][][][] layerData = null;
	//private float[][][] layerDataLog = null;
	private double[][][][] statisticsData = null;
	//private float[][][] statisticsDataLog = null;
	protected CoordinateReferenceSystem gridCRS = null;	// axes -> ReferencedEnvelope -> gridCRS
	protected CoordinateReferenceSystem originalCRS = null;
	Projection projection = null;
	
	private int plotWidth = 0;
	private int plotHeight = 0;

	// For clipped/projected/clipped map lines:

	private String mapFileDirectory = Mapper.getDefaultMapFileDirectory();

	private //final 
	Mapper mapper = null;

	protected Projector projector;

	protected double[][] gridBounds = { { 0.0, 0.0 }, { 0.0, 0.0 } };
	protected double[][] domain = { { 0.0, 0.0 }, { 0.0, 0.0 } };
	protected double[][] visibleLatLon = new double[2][2];	

	private final String variable; // "PM25".
	protected String units; // "ug/m3".
	protected Unit unitVar;

	private boolean withHucs = false; // Draw watersheds on map?
	private boolean withRivers = false; // Draw rivers on map?
	private boolean withRoads = false; // Draw roads on map?

	protected //final 
	DataFrame dataFrame;
	protected DataFrame dataFrameLog;
	protected List<OverlayObject> obsData = new ArrayList<OverlayObject>();
	protected List<ObsAnnotation> obsAnnotations;
	protected VectorAnnotation vectAnnotation;

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
	private JComboBox statisticsMenu;
	private String customPercentile = null;
	private int preStatIndex = -1;
	private JTextField threshold;
	private boolean recomputeStatistics = false;
	private boolean recomputeLegend = false;

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
	boolean statPointProbed = false;
	private boolean hasNoLayer = false;
	private int delay = 50; // In milliseconds.
	private final int MAXIMUM_DELAY = 3000; // 3 seconds per frame.

	protected boolean showLatLon = false;
	protected boolean showObsLegend = false;

	private final JPanel threadParent = this;
	private BufferedImage bImage;
	private boolean forceBufferedImage = false;
	boolean rescaleBuffer = false;
	int bufferedWidth, bufferedHeight;
	private static final Object lock = new Object();
	protected java.util.List<JMenuItem> probeItems = new ArrayList<JMenuItem>();
	private JPopupMenu popup;
	protected Rectangle dataArea = new Rectangle();
	private boolean inDataArea = false;
	private Point popUpLocation = new Point(1,1);
	protected Slice probedSlice;
	protected JCheckBoxMenuItem showGridLines;
	final JMenu mapLayersMenu = new JMenu("Add Map Layers");
		
	private ConfigDialog dialog = null;
	@SuppressWarnings("unused")										// had been out; put back in
	private Plot.ConfigSource configSource = Plot.ConfigSource.GUI;	// had been out; put back in

	VerdiApplication app;
	
	// Create a Thread that contains the above Runnable:

	private Thread doubleBufferedRendererThread = null;
	protected MinMax minMax;
	protected PlotConfiguration config;
	private boolean processTimeChange = true;
	private boolean processLayerChange = true;
//	private MapLayer controlLayer;
	private FeatureLayer controlLayer;
	
	protected Action timeSeriesSelected = new AbstractAction(
			"Time Series of Probed Cell(s)") {
		private static final long serialVersionUID = -2940008125642497962L;

		public void actionPerformed(ActionEvent e) {
			requestTimeSeries(Formula.Type.TIME_SERIES_LINE);
		}
	};

	protected Action timeSeriesBarSelected = new AbstractAction(
			"Time Series Bar of Probed Cell(s)") {
		private static final long serialVersionUID = 2455217937515200807L;

		public void actionPerformed(ActionEvent e) {
			requestTimeSeries(Formula.Type.TIME_SERIES_BAR);
		}
	};

	protected Action timeSeriesMin = new AbstractAction(
			"Time Series of Min. Cell(s)") {
		private static final long serialVersionUID = 5282480503103839989L;

		public void actionPerformed(ActionEvent e) {
			DataUtilities.MinMaxPoint points = getMinMaxPoints();
			requestTimeSeries(points.getMinPoints(), "Min. cells ");
		}
	};

	protected Action timeSeriesMax = new AbstractAction(
			"Time Series of Max. Cell(s)") {
		private static final long serialVersionUID = -4465758432397962782L;

		public void actionPerformed(ActionEvent e) {
			DataUtilities.MinMaxPoint points = getMinMaxPoints();
			requestTimeSeries(points.getMaxPoints(), "Max. cells ");
		}
	};

	// Declare a Runnable attribute which will create and run a thread
	// whose run method draws double-buffered to a graphics iff graphics
	// is not null:

	public final Runnable doubleBufferedRenderer = new Runnable() {

		private final RepaintManager repaintManager =
			RepaintManager.currentManager(threadParent);

		public void run() {
			Logger.debug("within FastTilePlot.run()");
			Logger.debug("391: mapFileDirectory = " + mapFileDirectory);

			try {
			do {
				
				VerdiGUI.unlock();
				if ( drawMode != DRAW_NONE &&
					 (! VerdiGUI.isHidden( (Plot) threadParent ) || forceBufferedImage)) {
Logger.debug("within drawMode != DRAW_NONE && !VerdiGUI.isHidden((Plot) threadParent)");
					VerdiGUI.lock();
					if (drawMode == DRAW_ONCE) {
//						synchronized (lock) {
							if (get_draw_once_requests() > 0) {
								draw_once_requests = 0;
								Logger.debug("set draw_once_requests = 0");
							}
							if ( get_draw_once_requests() >=0 ) {
								showBusyCursor();
								Logger.debug("get_draw_once_requests >= 0 so showBusyCursor()");
							}
					}
					
					// When animating, pause based on user-set delay rate:

					if (drawMode == DRAW_CONTINUOUS && delay != 0) {
						Logger.debug("within DRAW_CONTINUOUS with a delay");	// DO NOT SEE THIS LOGGER MSG
						try {
							Thread.sleep(delay);
						} catch (Exception unused) {}
					}
Logger.debug("set up drawing space, titles, fonts, etc.");
					int canvasWidth = getWidth();
					int canvasHeight = getHeight();
					if (canvasWidth == 0 && rescaleBuffer) {
						canvasWidth = bufferedWidth;
						canvasHeight = bufferedHeight;
					}
					if (canvasWidth > 0) {
						canvasWidth *= 1;
					}
					float marginScale = 0.95f; // Controls whitespace margin around plot window.
					String sTitle1 = config.getSubtitle1();
					String sTitle2 = config.getSubtitle2();
					Font tFont = config.getFont(PlotConfiguration.TITLE_FONT);
					Font sFont1 = config.getFont(PlotConfiguration.SUBTITLE_1_FONT);
					Font sFont2 = config.getFont(PlotConfiguration.SUBTITLE_2_FONT);
					int fontSize = (tFont == null) ? 20 : tFont.getSize();
					int yOffset = 20 + fontSize;
					marginScale *= 20f / (fontSize - 20 < 0 ? 20 : 20 + fontSize / 10f);

					if (sTitle1 != null && !sTitle1.trim().isEmpty()) {
						fontSize = (sFont1 == null) ? 20 : sFont1.getSize();
						yOffset += fontSize + 6;
						marginScale *= 20f / (fontSize - 20 < 0 ? 20
								: 20 + fontSize / 10f);
					}

					if (sTitle2 != null && !sTitle2.trim().isEmpty()) {
						fontSize = (sFont2 == null) ? 20 : sFont2.getSize();

						if (sTitle1 == null || sTitle1.trim().isEmpty()) {
							yOffset += 26;
							marginScale *= 20f / 22f;
						}

						yOffset += fontSize + 6;
						marginScale *= 20f / (fontSize - 20 < 0 ? 20
								: 20 + fontSize / 10f);
					}

					final int xOffset = 100;
					final int legendWidth = 80 + xOffset;
					
					int availCanvasWidth = canvasWidth - legendWidth;
					int availCanvasHeight = canvasHeight - yOffset;
					final int subsetRows = 1 + lastRow - firstRow;
					final int subsetColumns = 1 + lastColumn - firstColumn;
					
					double canvasRatio = (double) availCanvasWidth / availCanvasHeight;
					double plotRatio = (double)subsetColumns / subsetRows;
					
					int width = 0;
					int height = 0;
					if (canvasRatio > plotRatio) { // canvas is wider than plot 
						height = Math.round(availCanvasHeight * marginScale); //data height = canvas height, scale data width
						width = (int)Math.round(height * subsetColumns / (double)subsetRows);  
					} else { // canvas is narrower than plot
						width = Math.round(availCanvasWidth * marginScale);//data width = canvas width, scale data height
						height = (int)Math.round(width * subsetRows / (double)subsetColumns);
					}

					if (width == 0 || height == 0) {
						if ( get_draw_once_requests() < 0) 
							restoreCursor();
						continue;
					}

					// Use off-screen graphics for double-buffering:
					// don't start processing until graphics system is ready!
Logger.debug("here create offScreenImage");		// SEE THIS MSG 3 times
					Image offScreenImage =	null; // get actual size after call to AddPlotListener
					try {
						if (rescaleBuffer) {
							offScreenImage = new BufferedImage(bufferedWidth, bufferedHeight, BufferedImage.TYPE_INT_RGB);
						}
						else
							offScreenImage = repaintManager.getOffscreenBuffer(threadParent, canvasWidth, canvasHeight);
					} catch (NullPointerException e) {}

					// offScreenImage = (Image) (offScreenImage.clone());	// commented out in 2/2014 version

					if (offScreenImage == null) {
						if ( get_draw_once_requests() < 0) 
							restoreCursor();
						continue;// graphics system is not ready
					}					

					final Graphics2D offScreenGraphics = exportGraphics == null ? (Graphics2D)offScreenImage.getGraphics() : exportGraphics;

					if (offScreenGraphics == null) {
						if ( get_draw_once_requests() < 0) 
							restoreCursor();
						continue;// graphics system is not ready
					}
					
					if (rescaleBuffer) {
						double factor = ((double)bufferedWidth) / ((double)getWidth());
						if (factor > 0 && canvasWidth != bufferedWidth) {
							offScreenGraphics.scale(factor, factor);
							offScreenGraphics.fillRect(0,  0,  bufferedWidth,  bufferedHeight);
						}
					}

					final Graphics graphics = threadParent.getGraphics();

					if (graphics == null && !rescaleBuffer) {
						if ( get_draw_once_requests() < 0) 
							restoreCursor();
						continue;// graphics system is not ready
					}

					// graphics system should now be ready
					assert offScreenImage != null;
					assert offScreenGraphics != null;
					assert graphics != null;

					if (drawMode == DRAW_CONTINUOUS) {
						doStep(1);
						//prevTimestep = timestep;
						//timestep = nextValue(1, timestep, firstTimestep, lastTimestep);
						Logger.debug("in DRAW_CONTINUOUS for timestep = " + timestep);
						//timeLayerPanel.setTime(timestep);
						//drawOverLays();
						Logger.debug("back from drawOverLays() & done with DRAW_CONTINUOUS block");
					}
					
//					synchronized (lock) {	// commented out in 2/2014 version
						if (get_draw_once_requests() > 0) {
							draw_once_requests = 0;
							if ( get_draw_once_requests() < 0) 
								restoreCursor();
							continue;
						}
//					}						// commented out in 2/2014 version
Logger.debug("calling copySubsetLayerData from FastTilePlot.Runnable.run");

					copySubsetLayerData(log); // Based on current timestep and layer.

					
					synchronized (lock) {
						
						// Erase canvas:
Logger.debug("working with offScreenGraphics; first reset to blank");
						offScreenGraphics.setColor(Color.white);
						offScreenGraphics.fillRect(0, 0, canvasWidth,
								canvasHeight);

						// Draw legend-colored grid cells, axis, text labels and
						// legend:
Logger.debug("now set up time step, color, statistics, plot units, etc.");
						final Boolean showGridLines = (Boolean)
							config.getObject( TilePlotConfiguration.SHOW_GRID_LINES );
						final Color gridLineColor = (Color)
							( ( showGridLines == null || showGridLines == false ) ? null
								: config.getObject( TilePlotConfiguration.GRID_LINE_COLOR ) );

						final int stepsLapsed = timestep - firstTimestep;
						final int statisticsSelection = statisticsMenu.getSelectedIndex();
						Logger.debug("statisticsSelection = " + statisticsSelection);	// JEB after drawMode is == DRAW_ONCE
																						// then in DatasetListModel getElement At
						final String statisticsUnits =
							statisticsSelection == 0 ? null : GridCellStatistics.units( statisticsSelection - 1 );
						Logger.debug("statisticsUnits = " + statisticsUnits);
						//final String plotVariable =
						//	statisticsSelection == 0 ? variable
						//	: variable + GridCellStatistics.shortName( statisticsSelection - 1 );
						String plotVariable = variable;
						final String plotUnits =
							statisticsUnits != null ? statisticsUnits : units;
						Logger.debug("units = " + units);
						
						if (get_draw_once_requests() > 0) {
							draw_once_requests = 0;
							if ( get_draw_once_requests() < 0) 
								restoreCursor();
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
							//NumberFormat aNumberFormat = map.getNumberFormat();
							String formatString = map.getFormatString();
							Logger.debug("aFormatString = " + formatString);
							int aSubsetLayerDataLength = subsetLayerData.length;
							Logger.debug("subsetLayerData.length = " + aSubsetLayerDataLength);
							Logger.debug("ready to make revised function call to tilePlot.draw, thread = " + Thread.currentThread().toString());

							double percentile = 0;
							if (customPercentile != null)
								percentile = Double.parseDouble(customPercentile);
							String selectedStat = GridCellStatistics.getDisplayString(statisticsMenu.getSelectedIndex(), percentile, firstLayer, lastLayer);
							plotWidth = width;
							plotHeight = height;
//							tilePlot.draw(offScreenGraphics, (FastTilePlotPanel)this, // HOW TO GET TO FastTilePlotPanel FROM HERE???
//									xOffset, yOffset,
//									width, height, stepsLapsed, layer, aRow,
//									bRow, aCol, bCol, legendLevels,
//									legendColors, axisColor, labelColor, plotVariable,
//									aPlotUnits, 
//									config, aNumberFormat, gridLineColor,
//									subsetLayerData);
							mapper.drawSatellite(domain, gridBounds, gridCRS, projector,	// NOTE: JEB
									// 1st time here gridCRS is baseCRS: DefaultGeographicCRS
									// conversionFromBase: DefaultConicProjection
									// coordinateSystem: DefaultCartesianCS
									// datum: DefaultGeodeticDatum
										offScreenGraphics, xOffset, yOffset, width,
										height, withHucs, withRivers, withRoads);
							
							tilePlot.draw(offScreenGraphics, xOffset, yOffset,
									width, height, stepsLapsed, layer, firstRow + rowOrigin,
									lastRow + rowOrigin, firstColumn + columnOrigin, lastColumn + columnOrigin, projection, legendLevels,
									legendColors, axisColor, labelColor, plotVariable,
									((plotUnits==null || plotUnits.trim().equals(""))?"none":plotUnits), config, map.getNumberFormat(), gridLineColor,
									subsetLayerData, colorIndexCache, selectedStat);
						} catch (Exception e) {
							Logger.debug("FastTilePlot's run method", e);
						}
// by this point drew panel, panel title (O3[1]), panel menu, and panel bar (time step, layer, etc.)
						dataArea.setRect(xOffset, yOffset, width, height);	// same 4 values sent to tilePlot.draw(...)

						// Draw projected/clipped map border lines over grid cells:

						if (get_draw_once_requests() > 0) {
							draw_once_requests = 0;
							if ( get_draw_once_requests() < 0) 
								restoreCursor();
							continue;		// goes to while drawMode != DRAW_END
						}
						
						// NOTE: mapper.draw calls VerdiBoundaries.draw
						mapper.draw(domain, gridBounds, gridCRS,	// NOTE: JEB
																	// 1st time here gridCRS is baseCRS: DefaultGeographicCRS
																	// conversionFromBase: DefaultConicProjection
																	// coordinateSystem: DefaultCartesianCS
																	// datum: DefaultGeodeticDatum
								offScreenGraphics, xOffset, yOffset, width,
								height, withHucs, withRivers, withRoads);
						Logger.debug("back from mapper.draw, ready to check for ObsAnnotation");
						
						if (obsAnnotations != null) {
							for (ObsAnnotation ann : obsAnnotations)
								ann.draw(offScreenGraphics, xOffset, yOffset, width, height, 
										legendLevels, legendColors, gridCRS, domain, gridBounds, projection, unitVar);
						}
						
						if (vectAnnotation != null) {
							Logger.debug("ready for vectAnnotation.draw");
							vectAnnotation.draw(offScreenGraphics, xOffset, yOffset, width, height, 
									firstRow, lastRow, firstColumn, lastColumn);
						}

						Logger.debug("ready for resetMenuItems");
						resetMenuItems(mapLayersMenu);
						Logger.debug("ready for Toolkit.getDefaultToolkit");
						Toolkit.getDefaultToolkit().sync();

						try {
							Logger.debug("ready to call toBufferedImage");
							if (forceBufferedImage) {
								int w = canvasWidth, h = canvasHeight;
								if (rescaleBuffer) {
									w = bufferedWidth;
									h = bufferedHeight;
								}
								if (w > 0 && h > 0)
									bImage = toBufferedImage(offScreenImage, BufferedImage.TYPE_INT_RGB, w, h);
								Logger.debug("back from toBufferedImage, ready to call VerdiGUI.showIfVisible");
							
								if (animationHandler != null) {
									ActionEvent e = new ActionEvent(bImage, this.hashCode(), "");
									animationHandler.actionPerformed(e);
								} else
									forceBufferedImage = false;
							}
							if (!rescaleBuffer)
								VerdiGUI.showIfVisible(threadParent, graphics, offScreenImage);
							Logger.debug("back from VerdiGUI.showIfVisible");
						} finally {
							if (graphics != null)
								graphics.dispose();
							
							Logger.debug("just did graphics.dispose in finally block");
							offScreenGraphics.dispose();
							Logger.debug("just did offScreenGraphics.dispose in finally block");
						}

						Logger.debug("ready for Toolkit.getDefaultToolkit 2nd time");	// 2015 debug HIT THIS REPEATEDLY
						Toolkit.getDefaultToolkit().sync();
					
					} // End of synchronized block.
					Logger.debug("ended synchronized block. Now check drawMode.");	// 2015 debug HIT THIS REPEATEDLY
					if (drawMode == DRAW_ONCE ) {
						Logger.debug("drawMode is == DRAW_ONCE");		// 2015 debug HIT THIS REPEATEDLY
																		// JEB this message is followed by DatasetListModel getElementAt
																		// and then statisticsSelection = 0
						decrease_draw_once_requests();
						if (get_draw_once_requests() < 0) {
							drawMode = DRAW_NONE;
							restoreCursor();
						}
					} else {
						//drawMode = DRAW_NONE;			// commented out in 2/2014 version
					}
					VerdiGUI.unlock();
					
				} else {
					if ( drawMode == DRAW_ONCE) {
						decrease_draw_once_requests();
					}
					long sleep = 100;
					if (drawMode == DRAW_NONE)
						sleep = 1000;					
					try {
						Thread.sleep(sleep); /* ms. */
					} catch (Exception unused) {}
				}
			} while (drawMode != DRAW_END);		// drawMode set to DRAW_END in stopThread()
			} catch (Throwable t) {
				VerdiGUI.unlock();
				restoreCursor();
				if (dataFrame != null) { //Ignore errors if dataFrame is null - that means window is closing
					Logger.error("Error rendering FastTilePlot", t);
					String errInfo = t.getClass().getName();
					if (t.getMessage() != null && !t.getMessage().equals(""))
						errInfo += ": " + t.getMessage();										
					try {
						JOptionPane.showMessageDialog(app.getGui().getFrame(), "An error occured while rendering the plot:\n" + errInfo + "\nPlease see the log for more details.", "Error", JOptionPane.ERROR_MESSAGE);
						app.getGui().getViewManager().getDockable(viewId).close();
					} catch (Throwable tr) {}
				}
			} finally {
				VerdiGUI.unlock();
			}
		}		// HERE FINALLY DREW FastTilePlot - NO MAP BOUNDARIES YET; waiting for user input (change time step, layer, etc.)
				// end run()
	};		// end Runnable()
	
	public void setAnimationHandler(ActionListener listener) {
		super.setAnimationHandler(listener);
		forceBufferedImage = true;
	}
	
	private BufferedImage toBufferedImage(Image image, int type, int width, int height) {
		// NEEDS COMPLETE REWRITE
        BufferedImage result = new BufferedImage(width, height, type);
        Graphics2D g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return result;
    }

	public void drawBatchImage(int wdth, int hght) {
		Logger.debug("within FastTilePlot.drawBatchImage");
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

		Logger.debug("ready to call copySubsetLayerData from drawBatchImage");
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
		//final String plotVariable =
		//		statisticsSelection == 0 ? variable
		//				: variable + GridCellStatistics.shortName( statisticsSelection - 1 );
		String plotVariable = variable;
		Logger.debug("plotVariable = " + plotVariable);
		final String plotUnits = statisticsUnits != null ? statisticsUnits : units;
		Logger.debug("plotUnits = " + plotUnits);
		final int stepsLapsed = timestep - firstTimestep;
		try {
			double percentile = 0;
			if (customPercentile != null)
				percentile = Double.parseDouble(customPercentile);
			String selectedStat = GridCellStatistics.getDisplayString(statisticsMenu.getSelectedIndex(), percentile, firstLayer, lastLayer);

			tilePlot.drawBatchImage(offScreenGraphics,
					xOffset, yOffset,
					canvasWidth, canvasHeight, stepsLapsed, layer, firstRow,
					lastRow, firstColumn, lastColumn, legendLevels,
					legendColors, axisColor, labelColor, plotVariable,
					((plotUnits==null || plotUnits.trim().equals(""))?"none":plotUnits), config,
					map.getNumberFormat(), gridLineColor,
					subsetLayerData, selectedStat);
		} catch (Exception e) {
			Logger.error("FastTilePlot's drawBatch method", e);
			e.printStackTrace();
		}

		dataArea.setRect(xOffset, yOffset, tilePlot.getPlotWidth(), tilePlot.getPlotHeight());

		// Draw projected/clipped map border lines over grid cells:
		// NOTE: mapper.draw calls VerdiBoundaries.draw
		Logger.debug("in FastTilePlot (797); getting ready to call mapper.draw");
		mapper.draw(domain, gridBounds, gridCRS,
				offScreenGraphics, xOffset, yOffset, tilePlot.getPlotWidth(),
				tilePlot.getPlotHeight(), withHucs, withRivers, withRoads);

		try {
			bImage = (BufferedImage) offScreenImage;
			Logger.debug("just did bImage = (BufferedImage) offScreenImage");
		} finally {
			offScreenGraphics.dispose();
			Logger.debug("just did offScreenGraphics.dispose in finally block");
		}
	}
	


	// Methods --------------------------------------------------------

	// GUI Callbacks:

	// Plot frame closed:

	public void stopThread() {	// called by anl.verdi.plot.gui.PlotPanel
		drawMode = DRAW_END;
		draw();
	}

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
		super.viewClosed();
		stopThread();
		try {
			Thread.sleep(500);
		} catch( Exception e) {
		}
		// 
		mapper.dispose();
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
		
		doubleBufferedRendererThread = null;
		
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

	public void draw() {

		if (drawMode == DRAW_NONE) {
			drawMode = DRAW_ONCE;
		}
		
		if (drawMode == DRAW_ONCE) {
			increase_draw_once_requests();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void getCustomPercentile() {
		VerdiApplication.getInstance().getGui().getFrame();
		String s = (String)JOptionPane.showInputDialog(
				VerdiApplication.getInstance().getGui().getFrame(),
                "Enter the percentile you would like to plot:",
                "Custom Percentile",
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                customPercentile);
		try {
			Double.parseDouble(s);
			customPercentile = s;
			int index = statisticsMenu.getSelectedIndex();
			DefaultComboBoxModel<String> model = (DefaultComboBoxModel<String>)statisticsMenu.getModel();
			model.insertElementAt("custom_percentile (" + s + ")", index);
			model.removeElementAt(index + 1);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(VerdiApplication.getInstance().getGui().getFrame(),
				    "Please enter a numeric value.",
				    "NumberFormatException",
				    JOptionPane.ERROR_MESSAGE);
			statisticsMenu.setSelectedIndex(preStatIndex);
		}
	}

	// Buttons and fields:

	public void actionPerformed(ActionEvent event) {
		final Object source = event.getSource();
				
		if (statisticsMenu.getSelectedItem().toString().startsWith("layer_mean") || 
				statisticsMenu.getSelectedItem().toString().startsWith("layer_sum")) {
			timeLayerPanel.setLayerEnabled(false);
			threshold.setEnabled(false);
		} else {
			timeLayerPanel.setLayerEnabled(layers > 1);
			threshold.setEnabled(true);
		}

		if ( source == statisticsMenu || source == threshold ) {
								
			//TAH
			if ( statisticsMenu.getSelectedIndex() != this.preStatIndex || statisticsMenu.getSelectedItem().toString().startsWith("custom_percentile")) {
				if (statisticsMenu.getSelectedItem().toString().startsWith("custom_percentile") ) {
					getCustomPercentile();
				}
				if( statisticsMenu.getSelectedIndex() != 0) {
					recomputeStatistics = true;
				} else {
					//reset to no statistics...
					Logger.debug("ready to call copySubsetLayerData from reset to no statistics");
					copySubsetLayerData(this.log);
				}
				this.preStatIndex = statisticsMenu.getSelectedIndex();
			} else if (source != threshold)
				return;
			
			recomputeLegend = true;
			
			//populate legend colors and ranges on initiation
			//default to not a log scale
			double[] minmax = { 0.0, 0.0 };
			//calculate the non log min/max values, keep the code here
			//first part of IF ELSE will use the min/max values
			computeDataRange(minmax, false);
//			ColorMap.ScaleType sType = map.getScaleType();		// local variable sType not used
			//computeDataRange function need this.log set correctly...
			if (map.getPalette() == null)
			{
				Logger.debug("no palette so calling new PavePaletteCreator");
			}
			defaultPalette = (map.getPalette() != null) ? map.getPalette() : new PavePaletteCreator().createPavePalette(); // new PavePaletteCreator().createPalettes(8).get(0);
			map.setPalette(defaultPalette);
			
			//set min/max for both log and non log values...
			map.setMinMax( minmax[0], minmax[1]);
			double[] logminmax = { 0.0, 0.0 };
			computeDataRange(logminmax, true);
			map.setLogMinMax( logminmax[0], logminmax[1]);
			//this final one is for the below legend value calculations
			computeDataRange(minmax, this.log);
			if (this.log)
				minmax = logminmax;

			legendColors = defaultPalette.getColors();
			final double minimum = minmax[0];
			final double maximum = minmax[1];
			minMax = new DataUtilities.MinMax(minimum, maximum);
			int count = legendColors.length + 1;
			final double delta = (minmax[1] - minmax[0]) / (count - 1);
			legendLevels = new double[count];
			for (int level = 0; level < count; ++level) {
				legendLevels[level] = minmax[0] + level * delta;
			}
			config.setUnits("");
	    } else if (source == playStopButton) {

			if (playStopButton.getText().equals(PLAY)) {
				processTimeChange = false;
				playStopButton.setText(STOP);
				leftStepButton.setEnabled(false);
				rightStepButton.setEnabled(false);
				drawMode = DRAW_CONTINUOUS;
				timeLayerPanel.setRedrawing(true);
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
				timeLayerPanel.setRedrawing(false);
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
		Point start = new Point(getCol(popUpLocation), getRow(popUpLocation));
		Rectangle rect = new Rectangle(start, new Dimension(0, 0));

		if (command.equals(PROPERTIES_COMMAND)) {
			editChartProperties();
		} else if (command.equals(SAVE_COMMAND)) {
			try {
				PlotExporterAction save = new PlotExporterAction(this);
				save.actionPerformed(event);
			} catch (Exception e) {
				Logger.error("Error exporting image", e);
			}
		} else if (command.equals(PRINT_COMMAND)) {
			FastTilePlotPrintAction print = new FastTilePlotPrintAction(this);
			print.actionPerformed(event);
		} else if (command.equals(ZOOM_IN_BOTH_COMMAND) && inDataArea) {
			zoom(false, false, true, false, false, rect);
		} else if (command.equals(ZOOM_OUT_BOTH_COMMAND) && inDataArea) {
			zoom(false, false, false, false, true, rect);
		} else if (command.equals(ZOOM_OUT_MAX_COMMAND) && inDataArea) {
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

		computeDerivedAttributes();
	}
	
	public void processMouseMotionEvent(MouseEvent me) {
		if(isInDataArea(me)){
			if(me.getID() == MouseEvent.MOUSE_MOVED && showLatLon){
				Decidegrees gp = getLatLonFor(me.getX(), me.getY());
				app.getGui().setStatusTwoText(gp.toString());
			}
		}else{
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
	
	protected boolean isInDataArea(MouseEvent me) {	// JEB 2016 MAY NEED TO CHANGE THIS
			// WANT WITHIN THE JMapPane DATA MEMBER OF OVERALL JPanel
		// dataArea is a Rectangle object, so those functions will not work as they are
		int x = me.getPoint().x;
		int y = me.getPoint().y;
		
		if (x < dataArea.x || x > dataArea.x + dataArea.width)
			return false;
		
		if (y < dataArea.y || y > dataArea.y + dataArea.height)
			return false;
		
		return true;
	}
	
	protected int getRow(Point p) {	// JEB 2016 NEED TO CHANGE THIS
		// no longer working with a Rectangle object
		int dist = dataArea.y + dataArea.height - p.y;
		int div = dist * (lastRow - firstRow + 1);
		int den = dataArea.height;
		
		return firstRow +  div/den;
	}
	
	protected int getCol(Point p) {	// JEB 2016 NEED TO CHANGE THIS
		// no longer working with a Rectangle object
		int dist = p.x - dataArea.x;
		int div = dist * (lastColumn - firstColumn + 1);
		int den = dataArea.width;
		
		return firstColumn +  div/den;
	}
	
	private void zoom(boolean rightClick, boolean leftClick, boolean popZoomIn, boolean reset, boolean zoomOut, Rectangle bounds) {
		if (reset) {
			resetZooming();
			draw();
			return;
		}
		
		if (rightClick)
			return;
		
		int rowSpan = lastRow - firstRow;
		int colSpan = lastColumn - firstColumn;
		int inScale = 5;
		int rowInc = rowSpan < inScale * 2 ? rowSpan/2 - 1 : rowSpan / (inScale * 2);
		int colInc = colSpan < inScale * 2 ? colSpan/2 - 1 : colSpan / (inScale * 2);
		
		if (popZoomIn) { // click to zoom in or popup menu zoom in
			if (rowSpan != 0) {
				firstRow = bounds.y - rowInc < 1 ? 1 : bounds.y - rowInc;
				lastRow = bounds.y + rowInc > rows ? rows : bounds.y + rowInc;
			}
			
			if (colSpan != 0) {
				firstColumn = bounds.x - colInc < 1 ? 1 : bounds.x - colInc;
				lastColumn = bounds.x + colInc > columns ? columns : bounds.x + colInc;
			}
		} else if (zoomOut) {  //zoom out
			int outInc = 1 + colSpan  / 5;
			firstRow = firstRow - outInc < 1 ? 1 : firstRow - outInc;
			lastRow = lastRow + outInc > rows ? rows : lastRow + outInc;
			firstColumn = firstColumn - outInc < 1 ? 1 : firstColumn - outInc;
			lastColumn = lastColumn + outInc > columns ? columns : lastColumn + outInc;
		} else if (leftClick && bounds.height == 0 && bounds.width == 0) {
			return;
		} else { // regular zoom in
			firstRow = bounds.y - bounds.height;
			lastRow = bounds.y;
			firstColumn = bounds.x;
			lastColumn = bounds.x + bounds.width;
		}
		
		lastRow = Numerics.clampInt(lastRow, firstRow, rows - 1);
		lastColumn = Numerics.clampInt(lastColumn, firstColumn, columns - 1);
		firstColumnField.setText(Integer.toString(firstColumn + 1));
		lastColumnField.setText(Integer.toString(lastColumn + 1));
		firstRowField.setText(Integer.toString(firstRow + 1));
		lastRowField.setText(Integer.toString(lastRow + 1));
		computeDerivedAttributes();
		draw();
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
			prevTimestep = this.timestep;
			this.timestep = timestep;
			/*final int selection = statisticsMenu.getSelectedIndex() - 1;
			if (selection == GridCellStatistics.LAYER_MEAN ||
					selection == GridCellStatistics.LAYER_SUM) {
				recomputeStatistics = true;
			}*/
			Logger.debug("ready to call copySubsetLayerData from setTimestep");
			copySubsetLayerData(this.log);
			draw();
			if (!obsData.isEmpty())
				addObservationData(app.getDataManager(), showObsLegend);
			drawOverLays();
		}
	}

	public void setLayer(int layer) {
		if (layer >= firstLayer && layer <= lastLayer && layer != this.layer) {
			prevLayer = this.layer;
			this.layer = layer;
			final int selection = statisticsMenu.getSelectedIndex();

			if ( selection > 0 ) {
				recomputeStatistics = true;
			}

			Logger.debug("ready to call copySubsetLayerData from setLayer");
			copySubsetLayerData(this.log);
			draw();
		}
	}

	// Construct but do not draw yet.

	public FastTilePlot(VerdiApplication app, DataFrame dataFrame) {
		super(true);
//		this.setRenderer(new StreamingRenderer());

		this.app=app;
		setDoubleBuffered(true);
		assert dataFrame != null;
		this.dataFrame = dataFrame;
		
		this.calculateDataFrameLog();
		hasNoLayer = (dataFrame.getAxes().getZAxis() == null);
		format = NumberFormat.getInstance();
		format.setMaximumFractionDigits(4);

//		AreaFinder finder = new AreaFinder();
		// NOTE: 2015 appears AreaFinder is an inner class; based on Oracle's JavaSE tutorials,
		// proper way to instantiate an inner class is to first instantiate the outer class, and
		// then create the inner object with this syntax:
		// OuterClass.InnerClass innerObject = outerObject.new InnerClass();
		FastTilePlot.AreaFinder finder = this.new AreaFinder();
		this.addMouseListener(finder);
		this.addMouseMotionListener(finder);
		// Initialize attributes from dataFrame argument:

		final Variable dataFrameVariable = dataFrame.getVariable();
		variable = dataFrameVariable.getName();
		Logger.debug("1335: mapFileDirectory = " + mapFileDirectory);
		// JEB:  HERE NEED TO TEST FOR EXISTENCE OF mapFileDirectory & POP UP A FILE CHOOSER IF DOESN'T EXIST
		File vFile = new File(mapFileDirectory);
		if (!vFile.exists() || !vFile.canRead() || !vFile.isDirectory())
		{
			vFile = JFileDataStoreChooser.showOpenFile("shp", null);
			if(!vFile.exists() || !vFile.canRead() || !vFile.isDirectory())
			{
				Logger.error("incorrect map file directory: " + vFile.getAbsolutePath());
				return;
			}
			mapFileDirectory = vFile.getAbsolutePath();
			Logger.debug("mapFileDirectory now set to: " + mapFileDirectory);
		}
		Logger.debug("dataFrameVariable = " + dataFrameVariable);
		Logger.debug("dataFrameVariable name = " + variable);
		units = dataFrameVariable.getUnit().toString();
		unitVar = dataFrameVariable.getUnit();

		Logger.debug("units of dataFrameVariable = " + units);
		if ( units==null || units.trim().equals(""))
			units = "none";
		Logger.debug("now units = " + units);
		
		assert dataFrame.getAxes() != null;
		final Axes<DataFrameAxis> axes = dataFrame.getAxes();

		// Create cartographic projector (used by mapper.draw):

		final Dataset dataset = dataFrame.getDataset().get(0);
		final Axes<CoordAxis> coordinateAxes = dataset.getCoordAxes();
		projection = coordinateAxes.getProjection();
		Logger.debug("NOTE: in FastTilePlot using Projection = coordinateAxes.getProjection(), = " + 
				projection.getName());
		Logger.debug("coordinateAxes.getProjection = " + projection);

		if (projection instanceof LatLonProjection) {
			Logger.debug("projector being set to null because it is an instance of LatLonProjection");
			projector = null;
		} else {
			projector = new Projector(projection);
			Logger.debug("projector set to: " + projector.toString());
		}
		gridCRS = coordinateAxes.getBoundingBoxer().getCRS();
		
		originalCRS = coordinateAxes.getBoundingBoxer().getOriginalCRS();
		
		VerdiApplication.getInstance().setLastCoordinateReferenceSystem(gridCRS);
		
		// Initialize grid dimensions: timesteps, layers, rows, columns:

		final DataFrameAxis timeAxis = axes.getTimeAxis();

		if (timeAxis == null) {
			timesteps = 1;
			firstTimestep = timestep = lastTimestep  = 0;
		} else {
			timesteps = timeAxis.getExtent();
			firstTimestep = timestep = timeAxis.getOrigin();
			lastTimestep = firstTimestep + timesteps - 1;
		}

		final DataFrameAxis layerAxis = axes.getZAxis();

		if (layerAxis == null) {
			layers = 1;
			firstLayer = layer = lastLayer = 0;
		} else {
			layers = layerAxis.getExtent();
			firstLayer = layer = layerAxis.getOrigin();
			lastLayer = firstLayer + layers - 1;
		}

		final DataFrameAxis rowAxis = axes.getYAxis();
		rows = rowAxis != null ? rowAxis.getExtent() : 1;
		rowOrigin = rowAxis != null ? rowAxis.getOrigin() : 0;
		firstRow = 0;
		lastRow = firstRow + rows - 1;

		final DataFrameAxis columnAxis = axes.getXAxis();
		columns = columnAxis != null ? columnAxis.getExtent() : 1;
		columnOrigin = columnAxis != null ? columnAxis.getOrigin() : 0;
		firstColumn = 0;
		lastColumn = firstColumn + columns - 1;
		envelope = axes.getBoundingBox(dataFrame.getDataset().get(0).getNetcdfCovn());
		VerdiApplication.getInstance().setLastBounds((ReferencedEnvelope)envelope);

		westEdge = envelope.getMinX(); // E.g., -420000.0.
		southEdge = envelope.getMinY(); // E.g., -1716000.0.
//		cellWidth = Numerics.round1(envelope.getWidth() / columns); // 12000.0.
//		cellHeight = Numerics.round1(envelope.getHeight() / rows); // 12000.0.
		
		cellWidth = envelope.getWidth() / columns; // 12000.0.
		cellHeight = envelope.getHeight() / rows; // 12000.0.

		// HACK to invert row indexing for AURAMS / GEM / CF Convention files:

		final CoordAxis yAxis = coordinateAxes.getYAxis();
		final double firstYValue = yAxis.getValue( 0 );
		final double lastYValue  = yAxis.getValue( rows - 1 );
		invertRows = firstYValue > lastYValue;

		// 2014 footer date/time for footer1
		
		GregorianCalendar dsDate = axes.getDate(firstTimestep);
		startDate = dsDate == null ? new GregorianCalendar() : dsDate;
		
		if (timesteps > 1) {
			final GregorianCalendar date1 = axes.getDate(firstTimestep + 1);
			timestepSize = date1.getTimeInMillis() - startDate.getTimeInMillis();			
		} else {
			timestepSize = 1 * 60 * 60;
		}
		
		//populate legend colors and ranges on initiation
		double[] minmax = { 0.0, 0.0 };
		double[] minmaxl = { 0.0, 0.0 };
		//default to not a log scale
		this.log = false;
		//calculate the non log min/max values, keep the code here
		//first part of IF ELSE will use the min/max values
		computeDataRange(minmax, false);
		if ( this.map == null) {
			
			Logger.debug("in FastTilePlot, this.map == null so calling new PavePaletteCreator");
			defaultPalette = new PavePaletteCreator().createPavePalette(); // new PavePaletteCreator().createPalettes(8).get(0);
			map = new ColorMap(defaultPalette, minmax[0], minmax[1]);
		} else {
			ColorMap.ScaleType sType = map.getScaleType();
			if ( sType != null && sType == ColorMap.ScaleType.LOGARITHM ) 
				this.log = true;
		if(map.getPalette() == null)
		{
			Logger.debug("map.getPalette is null so calling new PavePaletteCreator");	// JEB does not print this message
		}
			defaultPalette = (map.getPalette() != null) ? map.getPalette() : new PavePaletteCreator().createPavePalette(); // new PavePaletteCreator().createPalettes(8).get(0);
			map.setPalette(defaultPalette);
		}

		//set min/max for both log and non log values...
		map.setMinMax( minmax[0], minmax[1]);
		computeDataRange(minmaxl, true);
		map.setLogMinMax( minmaxl[0], minmaxl[1]);
		//this final one is for the below legend value calculations
		if (this.log)
			minmax = minmaxl;

		//default to this type...
		map.setPaletteType(ColorMap.PaletteType.SEQUENTIAL);
		legendColors = defaultPalette.getColors();
		final double minimum = minmax[0];
		final double maximum = minmax[1];
		minMax = new DataUtilities.MinMax(minimum, maximum);
		int count = legendColors.length + 1;
		final double delta = (minmax[1] - minmax[0]) / (count - 1);
		legendLevels = new double[count];
		for (int level = 0; level < count; ++level) {
			legendLevels[level] = minmax[0] + level * delta;
		}
//		computeLegend();
		config = new TilePlotConfiguration();
		((TilePlotConfiguration) config).setColorMap(map);
		((TilePlotConfiguration) config).setGridLines(false, Color.gray);
		((TilePlotConfiguration) config).setSubtitle1(Tools.getDatasetNames(getDataFrame()));
		((TilePlotConfiguration) config).setLayerColor(Color.black);
		((TilePlotConfiguration) config).setLayerLineSize(1);
		
		mapper = new Mapper(mapFileDirectory, projection, gridCRS);
		mapper.setLayerStyle((TilePlotConfiguration)config);
		VerdiApplication.getInstance().setLastMapper(mapper);
		
		

		// Compute attributes derived from the above attributes and dataFrame:

		computeDerivedAttributes();

		// Create EMVL TilePlot (but does not draw yet - see draw()):

		tilePlot = new TilePlot(startDate, timestepSize);

		// Create GUI.

		// 	BEGINNING OF NEW FUNCTION createToolBar to construct the toolbar for GTTilePlotPanel
		timeLayerPanel = new TimeLayerPanel();
		final DataFrameAxis lAxis = dataFrame.getAxes().getZAxis();
		if (hasNoLayer) {
			timeLayerPanel.init(dataFrame.getAxes(), firstTimestep, 0, false);
		} else {
			timeLayerPanel.init(dataFrame.getAxes(), firstTimestep, firstLayer, lAxis.getExtent() > 1);
		}
		
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
		leftStepButton = new JButton(LEFT);
		leftStepButton.addActionListener(this);
		leftStepButton.setToolTipText(LEFT_TIP);
		rightStepButton = new JButton(RIGHT);
		rightStepButton.addActionListener(this);
		rightStepButton.setToolTipText(RIGHT_TIP);
		delayField = new JTextField("50", 4);
		delayField.addActionListener(this);						// 2014 needed to handle user changing delay in an animation
		delayField.setToolTipText("Set animation delay (ms)");	// 2014
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

		for ( int index = 1; index < GridCellStatistics.STATISTICS; index++ ) {
			statisticsNames[ index ] = GridCellStatistics.name( index - 1);
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
		toolBar.add(panel);		// END createToolBar function

		// add(toolBar);
		doubleBufferedRendererThread = new Thread(doubleBufferedRenderer);
		doubleBufferedRendererThread.start(); // Calls
//		super(toolBar);
		draw();
	}
	
//	// Compute legend levels from data range:
//
//	private void computeLegend() {		// JEB 2/2016 THIS FUNCTION IS NEVER CALLED
//
//		//populate legend colors and ranges on initiation
//		//default to not a log scale
//		double[] minmax = { 0.0, 0.0 };
//		//calculate the non log min/max values, keep the code here
//		//first part of IF ELSE will use the min/max values
//		computeDataRange(minmax, false);
//		//computeDataRange function need this.log set correctly...
//		if(map.getPalette() == null)
//		{
//			Logger.debug("getPalette is null here also so getting ready to call PavePaletteCreator");
//		}
//		defaultPalette = (map.getPalette() != null) ? map.getPalette() : new PavePaletteCreator().createPavePalette(); // new PavePaletteCreator().createPalettes(8).get(0);
//		map.setPalette(defaultPalette);
//		
//		//set min/max for both log and non log values...
//		map.setMinMax( minmax[0], minmax[1]);
//		computeDataRange(minmax, true);
//		map.setLogMinMax( minmax[0], minmax[1]);
//		//this final one is for the below legend value calculations
//		computeDataRange(minmax, this.log);
//
//		legendColors = defaultPalette.getColors();
//		final double minimum = minmax[0];
//		final double maximum = minmax[1];
//		minMax = new DataUtilities.MinMax(minimum, maximum);
//		int count = legendColors.length + 1;
//		final double delta = (minmax[1] - minmax[0]) / (count - 1);
//		legendLevels = new double[count];
//		for (int level = 0; level < count; ++level) {
//			legendLevels[level] = minmax[0] + level * delta;
//		}
//		config.setUnits("");
//	}
	
	private boolean statError = false;	// JEB WHY IS THIS HERE INSTEAD OF WITH THE OTHER CLASS DATA MEMBERS?

	private void computeStatistics(boolean log) {
		
		if (log != this.log)
			return;

		if ( layerData == null ) {
			layerData = new double[ rows ][ columns ][ timesteps ][ lastLayer + 1 ];
			statisticsData = new double[timesteps][ GridCellStatistics.STATISTICS ][ rows ][ columns ];
		}
			
		// System.out.println("FastTilePlot computeStatistics log " + log + " thislog " + this.log + " prevlog " + this.prevLog + " layer " + firstLayer + " to " + lastLayer);
		// Copy from dataFrame into layerData[ rows ][ columns ][ timesteps ]:

		final DataFrameIndex dataFrameIndex = getDataFrame(log).getIndex();


		for ( int row = 0; row < rows; ++row ) {
			final int dataRow = ! invertRows ? row : rows - 1 - row;

			for ( int column = 0; column < columns; ++column ) {

				for ( int timestep = 0; timestep < timesteps; ++timestep ) {
					
					//for (int dataLayer = 0; dataLayer < layers; ++dataLayer) {
					for (int dataLayer = firstLayer; dataLayer <= lastLayer; ++dataLayer) {
						try {
							dataFrameIndex.set( timestep, dataLayer - firstLayer, column, dataRow );
						} catch (Throwable t) {
							System.err.println("Error setting timestep " + timestep + " layer " + dataLayer + " column " + column + " row " + dataRow);
							dataFrameIndex.set( timestep, dataLayer - firstLayer, column, dataRow );
						}
						double value = this.getDataFrame(log).getDouble( dataFrameIndex );
						/*if (value < 0 && !log) {
							System.err.println("Negative value log " + log + " timestep " + timestep + " layer " + dataLayer + " column " + column + " row " + dataRow + " value " + value + " Inf: " + Double.isInfinite(value) + " nan " + Double.isNaN(value));

						}*/
						layerData[ row ][ column ][ timestep ][ dataLayer ] = value;
					}
				}
			}
		}

		final double threshold = Double.parseDouble( this.threshold.getText() );
		final double hoursPerTimestep = 1.0;
		
		try {
			double percentile = 0;
			if (customPercentile != null)
				percentile = Double.parseDouble(customPercentile);
			GridCellStatistics.computeStatistics( layer, firstLayer, lastLayer, timestep, layerData,
					threshold, hoursPerTimestep,
					statisticsData, this.statisticsMenu.getSelectedIndex()-1 , percentile);
			this.statError = false;
		} catch ( Exception e) {
			Logger.error("Error occurred during computing statistics", e);
			this.statError = true;
			if ( map != null && map.getScaleType() == ColorMap.ScaleType.LOGARITHM) {
				this.preLog = true;
				this.log = false;
				if ( this.tilePlot != null) {
					this.tilePlot.setLog( false);
				}
				map.setScaleType( ColorMap.ScaleType.LINEAR);
				
				draw();
			}
		}
	}

	// Compute derived attributes:

	private void computeDerivedAttributes() {	// called by zooming and resetRowsNColumns
		// JEB 2016 figure this out & rewrite for JMapPane
		// & get rid of Projector (use CRS) ???

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
		VerdiApplication.getInstance().setLastDomain(domain);
		
		//Point2D min = getLatLonForAxisPoint(new Point(firstColumn, firstRow));
		//Point2D max = getLatLonForAxisPoint(new Point(lastColumn, lastRow));
		
		int firstVisibleColumn = Math.max(firstColumn - 1, 0);
		int lastVisibleColumn = Math.min(lastColumn + 1, columns - 1);
		int firstVisibleRow = Math.max(firstRow - 1, 0);
		int lastVisibleRow = Math.min(lastRow + 1, rows - 1);
		
		Point2D bl = getLatLonForAxisPoint(new Point(firstVisibleColumn, firstVisibleRow));
		Point2D br = getLatLonForAxisPoint(new Point(lastVisibleColumn, firstVisibleRow));
		Point2D tl = getLatLonForAxisPoint(new Point(firstVisibleColumn, lastVisibleRow));
		Point2D tr = getLatLonForAxisPoint(new Point(lastVisibleColumn, lastVisibleRow));
		
		visibleLatLon[LONGITUDE][MINIMUM] = Math.min(tl.getX(), bl.getX());
		visibleLatLon[LATITUDE][MINIMUM] = Math.min(bl.getY(), br.getY());
		visibleLatLon[LONGITUDE][MAXIMUM] = Math.max(tr.getX(), br.getX());
		visibleLatLon[LATITUDE][MAXIMUM] = Math.max(tl.getY(), tr.getY());
		
		/*System.err.println("Visible image domain " + visibleLatLon[LONGITUDE][MINIMUM] + ":" +
				visibleLatLon[LONGITUDE][MAXIMUM] + ", " +
				visibleLatLon[LATITUDE][MINIMUM] + ":" + 
				visibleLatLon[LATITUDE][MAXIMUM]);*/
		
		try {
			//System.out.println("Retrieve image width " + plotWidth + " height " + plotHeight);
			if (plotWidth > 0 && plotHeight > 0)
				VerdiTileUtil.retrieveImage(visibleLatLon, plotWidth, plotHeight, this);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public Projection getPojection() {
		if (projector == null)
			return null;
		return projector.getProjection();
	}

	// Compute map domain from grid bounds:

	private void computeMapDomain(final Projector projector,		// 2016 replace Projector with CRS ???
			final double[][] gridBounds, double[][] mapDomain) {
		// JEB 2016 figure this out & rewrite for JMapPane
		// & get rid of Projector (use CRS) ???

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
				ucar.unidata.geoloc.projection.Stereographic ) {	// JEB 2016 probably need to change
						// testing for a polar projection

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

	// Copy current timestep, layer and row/column subset data from dataFrame
	// into subsetlayerdata[][]:
	
	protected int getFirstRow() {
		return firstRow;
	}
	
	protected int getFirstColumn() {
		return firstColumn;
	}

	private void copySubsetLayerData(boolean log) {
		
		final int selection = statisticsMenu.getSelectedIndex();

		if (prevFirstRow == firstRow && !recomputeLegend &&
				prevLastRow == lastRow &&
				prevFirstColumn == firstColumn &&
				prevLastColumn == lastColumn &&
				prevSelection == selection &&
				prevTimestep == timestep &&
				prevLayer == layer &&
				prevLog == log) {
			return;
		}

		// Reallocate the subsetLayerData[][] only if needed:

		final int subsetLayerRows = 1 + lastRow - firstRow;
		final int subsetLayerColumns = 1 + lastColumn - firstColumn;

		Logger.debug("into function copySubsetLayerData");	// NOT IN LOG FILE
		
		if (subsetLayerData == null
				|| subsetLayerData.length != subsetLayerRows * subsetLayerColumns
				|| subsetLayerData[0].length != subsetLayerColumns) {
			subsetLayerData = new double[subsetLayerRows][subsetLayerColumns];
		}
		
		if ( selection == 0 ) {

			// Copy from dataFrame into subsetLayerData[ rows ][ columns ]:

			final DataFrameIndex dataFrameIndex = getDataFrame(log).getIndex();

			for ( int row = firstRow; row <= lastRow; ++row ) {
				final int dataRow = ! invertRows ? row : rows - 1 - row;

				for ( int column = firstColumn; column <= lastColumn; ++column ) {
					dataFrameIndex.set( timestep-firstTimestep, layer-firstLayer, column, dataRow ) ;
					final double value = getDataFrame(log).getDouble( dataFrameIndex );
					subsetLayerData[row - firstRow][column - firstColumn] = value;
				}
			}
		} else {
			final int statistic = selection - 1;

			if ( statisticsData == null || recomputeStatistics ) {
				computeStatistics(log);
				recomputeStatistics = false;
			}

			// Copy from statisticsData into subsetLayerData[ rows ][ columns ]:

			int tsIndex = 0;
			
			if (statistic == GridCellStatistics.LAYER_MEAN ||
					statistic == GridCellStatistics.LAYER_SUM)
				tsIndex = timestep;
			for ( int row = firstRow; row <= lastRow; ++row ) {

				for ( int column = firstColumn; column <= lastColumn; ++column ) {
					final double value = statisticsData[tsIndex][ statistic ][ row ][ column ];
					subsetLayerData[row - firstRow][column - firstColumn] = value;
				}
			}
		}

		if ( recomputeLegend ) {
//			computeLegend();
			recomputeLegend = false;
		}
		
		colorIndexCache = tilePlot.calculateColorIndices(subsetLayerData, legendLevels);
		prevFirstRow = firstRow;
		prevLastRow = lastRow;
		prevFirstColumn = firstColumn;
		prevLastColumn = lastColumn;
		prevSelection = selection;
		prevTimestep = timestep;
		prevLayer = layer;
		prevLog = log;
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
							try {
							dataFrameIndex.set(timestep, layer, column, row);
							} catch (Throwable t) {
								//System.err.println("pause");
								throw t;
							}
							final double value = 
								dataFrame.getDouble(dataFrameIndex);
	
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

			for (int timestep = 0; timestep < timesteps; ++timestep) {

				for ( int row = firstRow; row <= lastRow; ++row ) {
	
					for ( int column = firstColumn; column <= lastColumn; ++column ) {
						final double value = statisticsData[timestep][ statistic ][ row ][ column ];
						
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
		menu.add(new FastTilePlotPrintAction(this));
		menu.add(new PlotExporterAction(this));
		bar.add(menu);

		menu = new JMenu("Configure");
		menu.add(new AbstractAction("Configure Plot") {
			private static final long serialVersionUID = 2455217937515200807L;

			public void actionPerformed(ActionEvent e) {
				editChartProperties();
			}
		});
		menu.add(new LoadConfiguration(this));
		menu.add(new SaveConfiguration(this));
		//configureMapMenu(menu);		// HERE IS WHERE THE CONFIGURE GIS LAYERS GOES??? JEB (Also commented out in v1.4.1
		bar.add(menu);

		menu = new JMenu("Controls");
		bar.add(menu);
		ButtonGroup grp = new ButtonGroup();
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

		activateRubberBand();

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
		
		menu.addSeparator();
		
		JMenuItem menuItem = new JMenuItem(
				new AbstractAction("Set Row and Column Ranges") {
					private static final long serialVersionUID = -4465758432397962782L;

					@Override
					public void actionPerformed(ActionEvent arg0) {
						setDataRanges();
					}
				});
		menu.add(menuItem);

		menu.addSeparator();
		showGridLines = new JCheckBoxMenuItem(new AbstractAction("Show Grid Lines") {
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
		
		menu.addSeparator();
		item = new JCheckBoxMenuItem(new AbstractAction("Show Lat / Lon") {
			private static final long serialVersionUID = 2699330329257731588L;

			public void actionPerformed(ActionEvent e) {
				JCheckBoxMenuItem latlon = (JCheckBoxMenuItem) e.getSource();
				showLatLon = latlon.isSelected();
			}
		});
		menu.add(item);
		
		bar.add(menu);

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

		JMenuItem item2 = new JMenuItem(new AbstractAction("Animate Plot") {
			private static final long serialVersionUID = 6336130019191512947L;

			public void actionPerformed(ActionEvent e) {
				AnimationPanel panel = new AnimationPanel();
				panel.init(getDataFrame().getAxes(), FastTilePlot.this);
			}
		});
		menu.add(item2);

		if (this.getClass().equals(FastTilePlot.class)) {
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
		}
		
		menu = new JMenu("GIS Layers");	
		gisLayersMenu(menu);
		bar.add(menu);
		
		// change cursor for initial zoom state
		setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		
		return bar;
	}
	
	protected void addObsOverlay() {
		OverlayRequest<ObsEvaluator> request = new OverlayRequest<ObsEvaluator>(OverlayRequest.Type.OBS, this);
		eventProducer.fireOverlayRequest(request);
	}
	
	protected void addVectorOverlay() {
		OverlayRequest<VectorEvaluator> request = new OverlayRequest<VectorEvaluator>(OverlayRequest.Type.VECTOR, this);
		eventProducer.fireOverlayRequest(request);
	}

	protected void activateRubberBand() {
		rubberband.setActive(true);
	}
	
	protected void deactivateRubberBand() {	// 2014 to allow user to turn OFF probe
		rubberband.setActive(false);
	}
	
	private void resetZooming() {
		firstRow = 0;
		lastRow = rows - 1;
		firstColumn = 0;
		lastColumn = columns - 1;
		computeDerivedAttributes();
		Logger.debug("ready to call copySubsetLayerData from resetZooming()");
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
		
		item = new JCheckBoxMenuItem("Satellite", false);
		item.setActionCommand(SATELLITE_LAYER);
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
		if (addLayers == null || addLayers.getItemCount() == 0) return;
		
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

	private FastTileLayerEditor showGISLayersDialog() {	// JEB possibly replace this by JFileDataStoreChooser
				// and separate GeoTools dialog to handle layer order and features (colors, etc.)
		Logger.debug("in FastTilePlot.showGISLayersDialog()");
		Window frame = SwingUtilities.getWindowAncestor(this);
		FastTileLayerEditor editor = null;
		
		if (frame instanceof JFrame)
			editor = new FastTileLayerEditor((JFrame) frame);
		else
			editor = new FastTileLayerEditor((JDialog) frame);
		
		editor.init(mapper);
		editor.setLocationRelativeTo(frame);
		editor.setVisible(true);
		Point p = editor.getLocation();
		editor.setLocation(0, p.y);
		editor.pack();
		return editor;
	}

//	protected MapLayer createControlLayer() {	// had been hard-coded to map of North America only
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
			query.setCoordinateSystemReproject(getDataFrame().getAxes().getBoundingBox(getDataFrame().getDataset().get(0).getNetcdfCovn()).getCoordinateReferenceSystem());
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
			
			if (layerKey.equals(SATELLITE_LAYER)) {
				int alphaValue = 255;
				if (show)
					alphaValue = 64;
				Color [] newColors = new Color[legendColors.length];
				for (int i = 0; i < legendColors.length; ++i) {
					newColors[i] = new Color(legendColors[i].getRed(), legendColors[i].getGreen(), legendColors[i].getBlue(), alphaValue);
					legendColors[i] = newColors[i];
				}
				if (show) {
					try {	
						alphaValue = 0;
						displayBackground = true;
						/*System.err.println("Retrieving image domain " + visibleLatLon[LONGITUDE][MINIMUM] + ":" +
								visibleLatLon[LONGITUDE][MAXIMUM] + ", " +
								visibleLatLon[LATITUDE][MINIMUM] + ":" + 
								visibleLatLon[LATITUDE][MAXIMUM]);
						System.err.println("width " + plotWidth + " height " + plotHeight);*/
						
						
						VerdiTileUtil.retrieveImage(visibleLatLon, plotWidth, plotHeight, this);
						//satellitePath = SatelliteImageManager.prepareImage(envelope, domain[LATITUDE][MINIMUM], domain[LATITUDE][MAXIMUM], domain[LONGITUDE][MINIMUM], domain[LONGITUDE][MAXIMUM], plotWidth, plotHeight);

						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
	
			}
			
			if (!show && layerKey.equals(SATELLITE_LAYER)) {
				mapper.removeSatelliteMap();
				tilePlot.setTransparentBackground(false);
				displayBackground = false;
			}
			
			if (layerKey.equals(OTHER_MAPS)) {
//				showGISLayersDialog();	// 2015 CHANGEd THIS TO BRING UP FILE BROWSER FOR .SHP FILES
				File selectFile = JFileDataStoreChooser.showOpenFile("shp", null);
				VerdiBoundaries aVerdiBoundaries = new VerdiBoundaries();
				aVerdiBoundaries.setProjection(projection, gridCRS);
				aVerdiBoundaries.setFileName(selectFile.getAbsolutePath());
				mapper.getLayers().add(aVerdiBoundaries);
			}
			draw();
		} catch (Exception e) {
			Logger.error("Error adding layer", e);
		}
	}


	/**
	 * Displays a dialog that allows the user to edit the properties for the current chart.
	 * 
	 * @since 1.0.5
	 */
	public void editChartProperties() {
		Window window = SwingUtilities.getWindowAncestor(FastTilePlot.this);
		dialog = null;
		if (window instanceof JFrame)
			dialog = new ConfigDialog((JFrame) window);	// ConfigDialog is anl.verdi.plot.gui.ConfigDialog
		else
			dialog = new ConfigDialog((JDialog) window);
		dialog.init(FastTilePlot.this, minMax);
		dialog.enableScale( !this.statError);
		dialog.setVisible(true);
	}

	/**
	 * Gets a tool bar for this plot. This may return null if there is no tool bar.
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
	 * Exports an image of this Plot to the specified file in the specified format.
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
	 * @param config    the new plot configuration
	 */

	public void configure(PlotConfiguration config) {
		String configFile = config.getConfigFileName();
		double[] minmax = { 0.0, 0.0 };

		if (configFile != null) {
			try {
				configure(new PlotConfigurationIO().loadConfiguration(new File(configFile)));
			} catch (IOException ex) {
				Logger.error("IOException in FastTilePlot.configure: loading configuration", ex);
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
			
			if (obsAnnotations != null) {
				for (ObsAnnotation ann : obsAnnotations) {
					ann.updateDrawingParams(map);
				}
			}

		}

		this.draw();
		this.config.updateConfig(config);
		
		if (this.showGridLines != null) {
			Boolean gridlines = (Boolean)config.getObject(TilePlotConfiguration.SHOW_GRID_LINES);
			this.showGridLines.setSelected(gridlines == null ? false : gridlines);
		}
	}
	
	public void configure(PlotConfiguration config, Plot.ConfigSource source) {
		String configFile = config.getConfigFileName();
		double[] minmax = { 0.0, 0.0 };

		if (configFile != null) {
			try {
				configure(new PlotConfigurationIO().loadConfiguration(new File(configFile)), source);
			} catch (IOException ex) {
				Logger.error("IOException in FastTilePlot.configure: loading configuration", ex);
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
			if ( source == Plot.ConfigSource.FILE) {
				this.recomputeLegend = true;
			} 
		}

		this.config.updateConfig(config);
		mapper.setLayerStyle((TilePlotConfiguration)this.config);
		if (obsAnnotations != null) {
			for (ObsAnnotation ann : obsAnnotations) {
				ann.updateDrawingParams(map);
			}
		}
		this.draw();
		
		if (this.showGridLines != null) {
			Boolean gridlines = (Boolean)config.getObject(TilePlotConfiguration.SHOW_GRID_LINES);
			this.showGridLines.setSelected(gridlines == null ? false : gridlines);
		}
	}	

	private void updateColorMap(ColorMap map) {
		this.map = map;
		
		try {
			minMax = new DataUtilities.MinMax(map.getMin(), map.getMax());
		} catch (Exception e) {
			Logger.error("Exception in FastTilePlot.updateColorMap", e);
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
				Logger.error("Exception in FastTilePlot.updateColorMap", e);
				e.printStackTrace();
			}

		try {
			legendLevels[count] = map.getMax();
			if (subsetLayerData != null && legendLevels != null)
				colorIndexCache = tilePlot.calculateColorIndices(subsetLayerData, legendLevels);
		} catch (Exception e) {
			Logger.error("Exception in FastTilePlot.updateColorMap", e);
			return;
		}
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
		Logger.debug("sending bImage in getBufferedImage member function");
		return getBufferedImage(null, width, height);
	}
	
	public void setScriptSize(int width, int height) {
		if (width == 0 || height == 0) {
			rescaleBuffer = false;
			return;
		}
		rescaleBuffer = true;
		bufferedWidth = width;
		bufferedHeight = height;
	}
	
	public BufferedImage getBufferedImage(Graphics2D g, int width, int height) {
		exportGraphics = g;
		bImage = null;
		forceBufferedImage = true;
		if (width != getWidth()) {
			rescaleBuffer = true;
			if (getWidth() == 0) {
				bufferedWidth = width;
				bufferedHeight = height;
			} else {
				bufferedWidth = width;
				bufferedHeight = bufferedWidth * getHeight() / getWidth();
			}
		}
		draw();
		long start = System.currentTimeMillis();
		//Allow more time when image is being rescaled
		while ((rescaleBuffer || System.currentTimeMillis() - start < 2000) && bImage == null )
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				Logger.error("Caught exception waiting for buffered image", e);
				break;
			}
		if (rescaleBuffer) {
			rescaleBuffer = false;
			bufferedWidth = 0;
			bufferedHeight = 0;
			draw();
		}
		exportGraphics = null;
		return bImage;
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
			throws PrinterException {	// JEB 2016 REDO; NO LONGER HAVE JUST 1 Graphics OBJECT TO PRINT
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
//		DataRangeDialog dialog = new DataRangeDialog("Set Row and Column Ranges",
//				FastTilePlot.this, firstRow + 1, lastRow + 1, firstColumn + 1,
//				lastColumn + 1);

		// NOTE: 2015 appears DataRangeDialog is an inner class; based on Oracle's JavaSE tutorials,
		// proper way to instantiate an inner class is to first instantiate the outer class, and
		// then create the inner object with this syntax:
		// OuterClass.InnerClass innerObject = outerObject.new InnerClass();
		FastTilePlot.DataRangeDialog dialog = this.new DataRangeDialog("Set Row and Column Ranges",
				FastTilePlot.this, firstRow + 1, lastRow + 1, firstColumn + 1,
				lastColumn + 1);
		dialog.showDialog();
	}

	private class DataRangeDialog extends JDialog {
		private static final long serialVersionUID = -1110292652911018568L;
		public static final int CANCEL_OPTION = -1;
		public static final int YES_OPTION = 1;
		public static final int ERROR = 0;
		private FastTilePlot plot;
		private JTextField fRowField;
		private JTextField lRowField;
		private JTextField fColumnField;
		private JTextField lColumnField;
		private boolean cancelled = false;
		private int firstRow, lastRow, firstColumn, lastColumn;

		public DataRangeDialog(String title, FastTilePlot plot, int firstRow,
				int lastRow, int firstColumn, int lastColumn) {
			super.setTitle(title);
			super.setLocation(getCenterPoint(plot));
			super.setModal(true);
			super.setPreferredSize(new Dimension(400, 300));
			this.firstRow = firstRow;
			this.lastRow = lastRow;
			this.firstColumn = firstColumn;
			this.lastColumn = lastColumn;
			this.fRowField = new JTextField("1", 4);
			this.lRowField = new JTextField("1", 4);
			this.fColumnField = new JTextField("1", 4);
			this.lColumnField = new JTextField("1", 4);
			this.plot = plot;
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
				firstRow = Integer.valueOf(fRowField.getText());
				lastRow = Integer.valueOf(lRowField.getText());
				firstColumn = Integer.valueOf(fColumnField.getText());
				lastColumn = Integer.valueOf(lColumnField.getText());
				plot.resetRowsNColumns(firstRow, lastRow, firstColumn,
						lastColumn);
				plot.draw();
				return YES_OPTION;
			} catch (NumberFormatException e) {
				Logger.error("Number Format Exception in FastTilePlot.showDialog: Set Rows and Columns", e);
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

			JLabel rowLabel = new JLabel("Rows:");
			JPanel rowPanel = new JPanel();
			rowPanel.add(fRowField, BorderLayout.LINE_START);
			fRowField.setText(Integer.toString(this.firstRow));
			rowPanel.add(new JLabel("..."));
			rowPanel.add(lRowField, BorderLayout.LINE_END);
			lRowField.setText(Integer.toString(this.lastRow));
			JLabel holder1 = new JLabel();

			gridbag.setConstraints(rowLabel, c);
			gridbag.setConstraints(rowPanel, c);
			c.gridwidth = GridBagConstraints.REMAINDER; // end row
			gridbag.setConstraints(holder1, c);
			contentPanel.add(rowLabel);
			contentPanel.add(rowPanel);
			contentPanel.add(holder1);

			c.gridwidth = 1; // next-to-last in row

			JLabel colLabel = new JLabel("Columns:");
			JPanel columnPanel = new JPanel();
			columnPanel.add(fColumnField, BorderLayout.LINE_START);
			fColumnField.setText(Integer.toString(this.firstColumn));
			columnPanel.add(new JLabel("..."));
			columnPanel.add(lColumnField, BorderLayout.LINE_END);
			lColumnField.setText(Integer.toString(this.lastColumn));
			JLabel holder2 = new JLabel();

			gridbag.setConstraints(colLabel, c);
			gridbag.setConstraints(columnPanel, c);
			c.gridwidth = GridBagConstraints.REMAINDER;
			gridbag.setConstraints(holder2, c);
			contentPanel.add(colLabel);
			contentPanel.add(columnPanel);
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
		prevTimestep = timestep;
		timestep = firstTimestep + step;
		
		try {
			timeLayerPanel.setTime(timestep);
		} catch (Exception e) {
			Logger.error("Exception setting time step. Time step = " + timestep + ". Is this 1-based?", e);
		}
		
		drawOverLays();
		draw();
		processTimeChange = true;

		try {
			Thread.sleep(500); //wait for the drawing thread to finish drawing
		} catch (InterruptedException e) {
			Logger.error("Interrupted Exception in FastTilePlot.updateTimeStep", e);
		}
	}
	public void updateLayer(int step) {
		processLayerChange = false;
		drawMode = DRAW_ONCE;
		prevLayer = layer;
		layer = firstLayer + step;
		
		try {
			timeLayerPanel.setLayer(layer);
		} catch (Exception e) {
			Logger.error("Exception setting layer. Layer = " + layer + ". Is this 1-based?", e);
		}
		
		drawOverLays();
		draw();
		processTimeChange = true;
		processLayerChange = true;

		try {
			Thread.sleep(500); //wait for the drawing thread to finish drawing
		} catch (InterruptedException e) {
			Logger.error("Interrupted Exception in FastTilePlot.updateTimeStep", e);
		}
	}
	
	/**
	 * Gets the MinMax points for this plot.
	 *
	 * @return the MinMax points for this plot.
	 */
	protected DataUtilities.MinMaxPoint getMinMaxPoints() {
		try {
			if (hasNoLayer) return DataUtilities.minMaxPoint(getDataFrame(), timestep - firstTimestep);
			return DataUtilities.minMaxTLPoint(getDataFrame(), timestep - firstTimestep, layer - firstLayer);
		} catch (InvalidRangeException e) {
			Logger.error("Invalid Range Exception in FastTilePlot getMinMaxPoints", e);
		}
		return null;
	}

	private void probe(Rectangle axisRect) {	// JEB 2016 REWRITE: no longer using Rectangle object
		synchronized (lock) {
			Slice slice = new Slice();
			slice.setTimeRange(timestep - firstTimestep, 1);
			if (!hasNoLayer) slice.setLayerRange(layer - firstLayer, 1);
			Axes<CoordAxis> fullAxes = dataFrame.getDataset().get(0).getCoordAxes();
			CoordAxis fullXAxis = fullAxes.getXAxis();
			CoordAxis fullYAxis = fullAxes.getYAxis();
			Axes<DataFrameAxis> axes = getDataFrame().getAxes();
			int subdomainXOffset = axes.getXAxis().getOrigin() - (int)fullXAxis.getRange().getOrigin();
			int subdomainYOffset = axes.getYAxis().getOrigin() - (int)fullYAxis.getRange().getOrigin();
			int probeFirstColumn = axisRect.x - axes.getXAxis().getOrigin() + subdomainXOffset; 
			final int probeColumns = axisRect.width + 1;
			int probeFirstRow = axisRect.y - axisRect.height - axes.getYAxis().getOrigin() + subdomainYOffset;
			final int probeRows = axisRect.height + 1;
			if (statisticsMenu.getSelectedIndex() != 0) {
				++probeFirstColumn;
				++probeFirstRow;
			}
			if (statisticsMenu.getSelectedIndex() != 0 && (probeColumns == 1 && probeRows ==1))
				statPointProbed = true;
			else
				statPointProbed = false;

			slice.setXRange( probeFirstColumn, probeColumns );
			slice.setYRange( probeFirstRow, probeRows );

			try {
				DataFrame subsection = null;

				//			boolean isLog = false;		// isLog is not used
//				double logBase = 10.0;					// JEB 2015 already have logBase as class data member
				ColorMap map = (ColorMap) config.getObject(TilePlotConfiguration.COLOR_MAP);
				if (map != null) {
					// set log related info
					ColorMap.ScaleType iType = map.getScaleType();
					if ( iType == ColorMap.ScaleType.LOGARITHM ) {
						//					isLog = true;
						logBase = map.getLogBase();
					}
				}			

				if ( statisticsMenu.getSelectedIndex() != 0 ) {
					// HACK: copy and overwrite subsection.array with subsetLayerData:
					subsection = getDataFrame().sliceCopy( slice );
					final int probeLastColumn = probeFirstColumn + probeColumns -1 ;
					final int probeLastRow = probeFirstRow + probeRows - 1 ;
					final ucar.ma2.Array array = subsection.getArray();
					final ucar.ma2.Index index = array.getIndex();

					for ( int row = probeFirstRow; row <= probeLastRow; ++row ) {
						final int sliceRow = (probeFirstRow - firstRow) + (row - probeFirstRow) - 1;
						final int sliceRowIndex = (row - probeFirstRow);
						index.set2( sliceRowIndex );

						for ( int column = probeFirstColumn; column <= probeLastColumn; ++column ) {
							final int sliceColumn = (probeFirstColumn - firstColumn) +  (column - probeFirstColumn) -1;
							final int sliceColumnIndex = (column - probeFirstColumn);
							index.set3( sliceColumnIndex );
							final double value = subsetLayerData[ sliceRow ][ sliceColumn ];
							array.setDouble( index, value );
						}
					}

				} else {
					subsection = getDataFrame().slice(slice);
				}

				probedSlice = slice;
				enableProbeItems(true);
				ProbeEvent ent = new ProbeEvent(this, subsection, slice, Formula.Type.TILE);	// 2014 fixed code not knowing what TILE meant
				ent.setIsLog( false); //isLog); // JIZHEN: always set to false, take log inside this class
				ent.setLogBase( logBase);
				eventProducer.fireProbeEvent(ent);//new ProbeEvent(this, subsection, slice, TILE));
			} catch (InvalidRangeException e) {
				Logger.error("Invalid Range Exception in FastTilePlot.Probe", e);
			}
		}
	}

	private void requestTimeSeries(Formula.Type type) {
		Slice slice = new Slice();
		// slice needs to be in terms of the actual array indices
		// of the frame, but the axes ranges refer to the range
		// of the original dataset. So, the origin will always
		// be 0 and the extent is the frame's extent.
		slice.setTimeRange(0, getDataFrame().getAxes().getTimeAxis().getExtent());
		DataFrameAxis frameAxis = getDataFrame().getAxes().getZAxis();
		if (frameAxis != null) slice.setLayerRange(0, frameAxis.getExtent());
		slice.setXRange(probedSlice.getXRange());
		slice.setYRange(probedSlice.getYRange());

		try {
			DataFrame subsection = getDataFrame().slice(slice);
			eventProducer.firePlotRequest(new TimeSeriesPlotRequest(subsection, slice, type));
		} catch (InvalidRangeException e1) {
			Logger.error("InvalidRangeException in FastTilePlot.requestTimeSeries", e1);
		}
	}

	private void requestTimeSeries(Set<Point> points, String title) {
		MultiTimeSeriesPlotRequest request = new MultiTimeSeriesPlotRequest(title);
		for (Point point : points) {
			Slice slice = new Slice();
			// slice needs to be in terms of the actual array indices
			// of the frame, but the axes ranges refer to the range
			// of the original dataset. So, the origin will always
			// be 0 and the exent is the frame's exent.
			slice.setTimeRange(0, getDataFrame().getAxes().getTimeAxis().getExtent());
			DataFrameAxis frameAxis = getDataFrame().getAxes().getZAxis();
			if (frameAxis != null) slice.setLayerRange(0, frameAxis.getExtent());
			slice.setXRange(point.x, 1);
			slice.setYRange(point.y, 1);
			try {
				DataFrame subsection = getDataFrame().slice(slice);
				request.addItem(subsection);
			} catch (InvalidRangeException e1) {
				Logger.error("InvalidRangeException in FastTilePlot.requestTimeSeries", e1);
			}
		}
		eventProducer.firePlotRequest(request);
	}

	/**
	 * Enables / disables the menu items that work with
	 * the currently probed point.
	 *
	 * @param val true to enable
	 */
	protected void enableProbeItems(boolean val) {
		for (JMenuItem item : probeItems) {
			item.setEnabled(val);
		}
	}

	public Decidegrees getLatLonFor(int screenx,int screeny){	// JEB 2016 REWRITE no longer using Rectangle object
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
		
		if (projector != null)
			projector.unproject(tx, ty, longitudeLatitude);
		else { //LatLonProjection
			//This is fixes world_NOX_latlon.ncf and cpc_global_daily_precip.2016.nc
			if (projection instanceof LatLonProjection && ((LatLonProjection)projection).getCenterLon() != 0) {
				if (tx > 180)
					tx -= 360;
			}
			longitudeLatitude[0] = tx;
			longitudeLatitude[1] = ty;
		}
			
			
		
		return new Decidegrees(longitudeLatitude[1], longitudeLatitude[0]);
	}
	
	public String createAreaString(int[] point) {
		Point4i p = new Point4i();
		p.w = point[0];
		p.z = point[1];
		p.x = point[2];
		p.y = point[3];
		if (showLatLon) return formatPointLatLon(p);
		return formatPoint(p);
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
		int offsetAmount = 1;
		if (statPointProbed) {
			offsetAmount = 0;
			statPointProbed = false;
		}
		for (int val : vals) {
			if (val != NO_VAL) {
				if (addComma) builder.append(", ");
				builder.append(val + offsetAmount);
				addComma = true;
			}
		}
		builder.append(")");
		return builder.toString();
	}
	
	private String formatPointLatLon(Point4i point) {
		Point2D llul = getLatLonForAxisPoint(new Point(point.x, point.y));
		StringBuilder builder = new StringBuilder("(");
		double[] vals = new double[4];
//		vals[0] = point.getW();
//		vals[1] = point.getZ();
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
	
	protected Point2D getLatLonForAxisPoint(Point axisPoint) {
		//Since the netcdf boxer used middle of the grid as origin of the grid
		//FastTilePlot use SW corner as the origin of the grid, hence the minus 1
//		return getDataFrame().getAxes().getBoundingBoxer().axisPointToLatLonPoint(axisPoint.x-1, axisPoint.y-1); 
		return getDataFrame().getAxes().getBoundingBoxer().axisPointToLatLonPoint(axisPoint.x, axisPoint.y); //NOTE: the shift has been considered for in the netcdf boxer!!!
	}
	
	class AreaFinder extends MouseInputAdapter {

		private Point start, end;
		
		private Point mpStart, mpEnd;

		// this rect measured axis coordinates
		private Rectangle rect;

		public void mousePressed(MouseEvent e) {
			if (isInDataArea(e)) {
				mpStart = e.getPoint();
				start = new Point(getCol(mpStart), getRow(mpStart));
				rect = new Rectangle(start, new Dimension(0, 0));
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect),
								false));
			} else {
				start = null;
			}
		}

		public void mouseDragged(MouseEvent e) {
			if (start != null) {
				if (isInDataArea(e)) {
					mpEnd = e.getPoint();
					end = new Point(getCol(mpEnd), getRow(mpEnd));
					rect.width = end.x - rect.x;
					rect.height = rect.y - end.y;
					
					boolean finished = rect.width < 0 || rect.height < 0;
					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect),
									finished));
				} else {
					eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, true));
				}
			}
		}

		public void mouseMoved(MouseEvent e) {
			if (isInDataArea(e)) {
				Point p = new Point(getCol(e.getPoint()), getRow(e.getPoint()));
				Rectangle rect = new Rectangle(p.x, p.y, 0, 0);
				if (!showLatLon) eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, createAreaString(rect), false));
			} else {
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(new Rectangle(0, 0, 0, 0), true));
			}
		}

		public void mouseExited(MouseEvent e) {
			eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(new Rectangle(0, 0, 0, 0), true));
		}

		public void mouseReleased(MouseEvent e) {
			if (start != null) {
				if (isInDataArea(e)) {
					mpEnd = e.getPoint();
					end = new Point(getCol(mpEnd), getRow(mpEnd));
					rect.width = end.x - rect.x;
					rect.height = rect.y - end.y;

					if (probe)
						probe(rect);
					else {
						int mod = e.getModifiers();
						int mask = MouseEvent.BUTTON3_MASK;
						boolean rightclick = (mod & mask) != 0;
						
						zoom(rightclick, !rightclick, false, mpEnd.x < mpStart.x || mpEnd.y < mpStart.y, false, rect);
					}
				}
				
				eventProducer.fireAreaSelectionEvent(new AreaSelectionEvent(rect, true));
			}
		}

		private String createAreaString(Rectangle rect) {
			Point4i[] points = rectToPoints(rect);
			if (showLatLon) return createLonLatAreaString(points);
			else return createAxisCoordAreaString(points);
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
	
//	public void exportShapefileOld( String baseFileName ) throws IOException {		// 2014 appears to not be used
//		final int subsetLayerRows = 1 + lastRow - firstRow;
//		final int subsetLayerColumns = 1 + lastColumn - firstColumn;
//		// Filter variable name/expression so operators aren't a problem in Excel:
//		final String filteredVariableName =
//			variable.replaceAll( "[\\[\\d\\]]", "" ).replaceAll( "\\W", "" );
//		GridShapefileWriter.write( baseFileName,
//									subsetLayerRows, subsetLayerColumns,
//									westEdge, southEdge,
//									cellWidth, cellHeight,
//									filteredVariableName, subsetLayerData, projector );
//	}

	public void exportShapefile( String baseFileName ) throws IOException {
		final int subsetLayerRows = 1 + lastRow - firstRow;
		final int subsetLayerColumns = 1 + lastColumn - firstColumn;

		
		final double subsetWestEdge = westEdge + firstColumn * cellWidth;
		final double subsetSouthEdge = southEdge + firstRow * cellWidth;
		GridShapefileWriter.write( baseFileName,
				subsetLayerRows, subsetLayerColumns,
				subsetWestEdge, subsetSouthEdge,
				cellWidth, cellHeight,
				filterVariableName(variable), subsetLayerData, originalCRS );
	}
	
	protected double[][] getLayerData() {
		return subsetLayerData;
	}
	
	protected String getVariableName() {
		return filterVariableName(variable);
	}
	
	protected String filterVariableName(String name) {
		// Filter variable name/expression so operators aren't a problem in Excel:
		
		// changed to this in v. 529
		final int end = name.indexOf( '[' );
		final String filteredVariableName = name.substring( 0, end );
		
		// change back now 2012-06-14
//		final String filteredVariableName =
//			variable.replaceAll( "[\\[\\d\\]]", "" ).replaceAll( "\\W", "" );
		return filteredVariableName;
	}
	
	public void exportASCIIGrid( String baseFileName ) {
		Logger.debug("in FastTilePlot.exportASCIIGrid");
		final int subsetLayerRows = 1 + lastRow - firstRow;
		final int subsetLayerColumns = 1 + lastColumn - firstColumn;
		final double subsetWestEdge = westEdge + firstColumn * cellWidth;
		final double subsetSouthEdge = southEdge + firstRow * cellWidth;
		ASCIIGridWriter.write( baseFileName + ".asc",
		subsetLayerRows, subsetLayerColumns,
		subsetWestEdge, subsetSouthEdge,
		cellWidth, subsetLayerData );
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
		
		exportEPSImage(filename, width, height);
	}
	
	public void exportEPSImage(String filename, int width, int height) {
//		EpsRenderer renderer = new EpsRenderer(width, height);
		// NOTE: 2015 appears EpsRenderer is an inner class; based on Oracle's JavaSE tutorials,
		// proper way to instantiate an inner class is to first instantiate the outer class, and
		// then create the inner object with this syntax:
		// OuterClass.InnerClass innerObject = outerObject.new InnerClass();
		FastTilePlot.EpsRenderer renderer = this.new EpsRenderer(width, height);
		EpsTools.createFromDrawable(renderer, filename, width, height, ColorMode.COLOR_RGB);
	}
	
	class EpsRenderer implements Drawable {
		final int canvasWidth, canvasHeight;
		
		public EpsRenderer(int width, int height) {
			canvasWidth = width;
			canvasHeight = height;
			Logger.debug("within subclass EpsRenderer, setting canvasWidth = " + width + "; canvasHeight = " + canvasHeight);
		}
		
		@Override
		public void draw(Graphics2D g, Rectangle2D rect) {
			getBufferedImage(g, canvasWidth, canvasHeight);
		}
		
	}
	
	public void setBackgroundImage(String path) {
		backgroundImage = path;
		if (displayBackground) {
			mapper.showSatelliteMap(backgroundImage);
			tilePlot.setTransparentBackground(true);
			draw();
		}
	}
	
	// get the JMapPane portion (the mapping rectangle containing the raster and shapefiles) of the FastTilePlot
	public JMapPane getMapPane()
	{
		return null;
	}

	public void addVectorAnnotation(VectorEvaluator eval) {
		vectAnnotation = new VectorAnnotation(eval, timestep, getDataFrame().getAxes().getBoundingBoxer());
		draw();
	}

	public void addObservationData(DataManager manager, boolean showLegend) {
		showObsLegend = showLegend;
		obsAnnotations = new ArrayList<ObsAnnotation>();
		Axes<DataFrameAxis> axs = getDataFrame().getAxes();
		GregorianCalendar initDate = getDataFrame().getAxes().getDate(0);
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
				ObsEvaluator eval = new ObsEvaluator(manager, obs.getVariable(), matchObsTimesteps);
				ObsAnnotation ann = null;
				ann = new ObsAnnotation(eval, axs, layer, obs, timestepSize);
				try {
					GregorianCalendar currentDate = getDataFrame().getAxes().getDate(timestep);

					ann.update(currentDate.getTime());
					ann.setDrawingParams(obs.getSymbol(), obs.getStrokeSize(), obs.getShapeSize(), map);
					obsAnnotations.add(ann);
					matchObsTimesteps = eval.getMatchTimesteps();
				} catch (IllegalArgumentException e) {
					GregorianCalendar endDate = getDataFrame().getAxes().getDate(lastTimestep);
					if (!eval.dataWithin(initDate.getTime(), endDate.getTime()))						
						throw e;
					/*GregorianCalendar currentDate = getDataFrame().getAxes().getDate(timestep);
					ann = new ObsAnnotation(eval, axs, currentDate, layer, obs, false);
					GregorianCalendar endDate = getDataFrame().getAxes().getDate(lastTimestep);
					if (eval.dataWithin(initDate.getTime(), endDate.getTime())) {
						ann.update(currentDate.getTime());
					}else
						throw e;*/
				}
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
			app.getGui().showError("Error", e.getMessage());
			Logger.error(e.getMessage(), e);
			drawMode = DRAW_NONE;
		}
	}
	
	public List<OverlayObject> getObservationData() {
		return this.obsData;
	}
	
	private void drawOverLays() {
		try {
			if (obsAnnotations != null)  {
				java.util.Date currentDate = getDataFrame().getAxes().getDate(timestep).getTime();
				for (ObsAnnotation ann : obsAnnotations) 
					ann.update(currentDate);
			}
			
			if (vectAnnotation != null) {
				vectAnnotation.update(timestep);
			}
		} catch (Exception e) {
			app.getGui().showError("Error", e.getMessage());
			Logger.error("", e);
			drawMode = DRAW_NONE;
		}
	}
	
	private void setOverlayErrorMsgg(String msg) {
		if (msg == null) msg = "";
		JOptionPane.showMessageDialog(app.getGui().getFrame(), "Please check if the overlay time steps match the underlying data.\n" + msg, "Overlay Error", JOptionPane.ERROR_MESSAGE, null);
	}

	public String getTitle() {
		return tilePlot.getTitle();
	}
	
	private void calculateDataFrameLog() {
		if ( this.dataFrame == null) {
			return;
		}
		
		boolean doDebug = false;
		
		this.dataFrameLog = DataUtilities.createDataFrame( this.dataFrame);
 
		if ( doDebug) {
			Logger.debug( "debug print 1:");
		}
		
		int count = 0;	    	
		IndexIterator iter2 = this.dataFrameLog.getArray().getIndexIterator();
		IndexIterator iter1 = this.dataFrame.getArray().getIndexIterator();
		if ( doDebug) 
			Logger.debug( "debug print 2:");
		double val1, val2;
		double currentLogBase = Math.log(this.logBase);
		while (iter2.hasNext()) {
			val1 = iter1.getDoubleNext(); 
			val2 = iter2.getDoubleNext(); 
			if ( doDebug && count<100) 
				Logger.debug(val1 + " " + val2);
			val2 = (double)(Math.log(val1) / currentLogBase);
			iter2.setDoubleCurrent( (double)( val2));

			val2 = iter2.getDoubleCurrent();

			if ( doDebug && count++<100) 
				Logger.debug( " : " + val1 + " " + val2);
		}
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
		//   cursor restored
		//synchronized(this) {
		VerdiGUI gui = app.getGui();
		if (gui != null)
			gui.defaultCursor();
//		}
		if ( zoom) 
			setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
		else 
			setCursor(Cursor.getDefaultCursor());
	}
	
	private void increase_draw_once_requests() {
		draw_once_requests ++;
	}
	
	private void decrease_draw_once_requests() {
		draw_once_requests --;
	}
	
	private int get_draw_once_requests() {
		return draw_once_requests;
	}
}
