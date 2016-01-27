/** GTTilePlot: GeoTools version of a tile plot
 * developed 2016 based on VERDI TilePlot and FastTilePlot
 * uses new FastTilePlotPanel for GTTilePlot frame layout and to supply GeoTools geographic data support
 */

package anl.verdi.plot.gui;

import gov.epa.emvl.GridCellStatistics;
import gov.epa.emvl.TilePlot;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import anl.verdi.plot.color.Palette;

import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.geotools.map.MapContent;
import org.geotools.swing.JMapPane;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameIndex;
import anl.verdi.data.DataUtilities;
import anl.verdi.data.DataUtilities.MinMax;
import anl.verdi.formula.Formula;
import anl.verdi.formula.Formula.Type;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.PlotConfigurationIO;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.probe.PlotEventProducer;
import anl.verdi.plot.types.TimeAnimatablePlot;
import anl.verdi.plot.util.PlotExporter;

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
	
	// GUI attributes
	
	private boolean recomputeStatistics = false;
	private boolean statError = false;
	private JComboBox statisticsMenu;
	private JTextField threshold;
	private boolean recomputeLegend = false;
	protected JCheckBoxMenuItem showGridLines;
	private BufferedImage bImage;
	
	@SuppressWarnings("unused")
	private Plot.ConfigSoure configSource = Plot.ConfigSoure.GUI;

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public GTTilePlot() {
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
	public void viewClosed() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateTimeStep(int timestep) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
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

}
