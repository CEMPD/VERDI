/** Mapper.java - Manage a set of maps for drawing.
 * 2008-09-01 plessel.todd@epa.gov
 * javac Map*.java
 */

package gov.epa.emvl;

//import gov.epa.emvl.Projector;
//import org.geotools.referencing.CRS;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.MapContent;
import org.geotools.swing.JMapPane;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.gui.VerdiBoundaries;
import anl.verdi.util.Tools;
import ucar.unidata.geoloc.Projection;

// Contains a set of VerdiBoundaries and draws them clipped to a domain.
public class Mapper {

	// Attributes:
	static final Logger Logger = LogManager.getLogger(Mapper.class.getName());

	private static final int X = 0;
	private static final int Y = 1;
	private static final int MINIMUM = 0;
	private static final int MAXIMUM = 1;
	// 2015 removed ".bin" from hard-coded file names & replaced with ".shp"
	private static final String worldMapFileName = "Department_of_State_Valid_QGIS.shp";		// OK as Shapefile	
	private static final String northAmericaMapFileName = "ne_50m_admin_1_states_provinces_lakes/states_us_mexico.shp";	// OK as Shapefile
	private static final String stateMapFileName = "map_state/Detailed_State_Lines.shp";		// match county boundary file
	private static final String countyMapFileName = "map_county/cb_2014_us_county_500k.shp";	// OK as Shapefile
	private static final String hucsMapFileName = "huc250k_shp/huc250k.shp";
	private static final String riversMapFileName = "ne_10m_rivers_lake_centerlines/ne_10m_rivers_lake_centerlines.shp";
	private static final String roadsMapFileName = "map_roads/tl_2015_us_primaryroads.shp";
	private static final Color mapColor = Color.black;
	private final String mapFileDirectory; // URL/dir containing above files.
	private boolean showSatelliteMap = false;
	private VerdiBoundaries satelliteMap = null;
	private VerdiBoundaries worldMap = null;
	private VerdiBoundaries northAmericaMap = null;
	private VerdiBoundaries stateMap = null;
	private VerdiBoundaries countyMap = null;
	private VerdiBoundaries hucsMap = null;
	private VerdiBoundaries riversMap = null;
	private VerdiBoundaries roadsMap = null;
	private VerdiBoundaries satellieMap = null;
	private List<VerdiBoundaries> layers = new CopyOnWriteArrayList<VerdiBoundaries>();
	private boolean initialDraw = true;
	private static String defaultMapFileDirectory = null;
	private Projection projection;
	private CoordinateReferenceSystem targetCRS;
	
	public Mapper(String directoryName) {
		this(directoryName, null, null);
	}
	
	public Mapper(Projection projection, CoordinateReferenceSystem targetCRS) {
		this(Mapper.getDefaultMapFileDirectory(), projection, targetCRS);
	}

	public void cacheMaps() {
		try {
			getWorldMap(true);
			getNorthAmericaMap(true);
			getUsaStatesMap(true);
			getUsaCountiesMap(true);
			getUSHucMap(true);
			getUSRiversMap(true);
			getUSRoadsMap(true);
			Logger.debug("done with get map functions, leaving Mapper constructor");
		} catch (Exception e) {
			Logger.debug("caught an exception caching maps from " + mapFileDirectory, e);
		}
	}
	
	// Construct with URL or directory containing the map files.
	public Mapper(String directoryName, Projection projection, CoordinateReferenceSystem targetCRS) {
		Logger.debug("in constructor for Mapper, directoryName = " + directoryName);
		this.projection = projection;
		this.targetCRS = targetCRS;
		mapFileDirectory = directoryName + "/";
		cacheMaps();
		Logger.debug("Number of layers = " + layers.size());
	}
		
	public static String getDefaultMapFileDirectory() {
		if (defaultMapFileDirectory != null )
			return defaultMapFileDirectory;
		
		defaultMapFileDirectory = Tools.getVerdiHome() + "/plugins/bootstrap/data";
		if (new File(defaultMapFileDirectory).exists())
			return defaultMapFileDirectory;
		defaultMapFileDirectory = ".." + "/verdi_bootstrap/data";	
		return defaultMapFileDirectory;			
	}
	
	public void drawSatellite(final double[][] domain, final double[][] gridBounds,
			final CoordinateReferenceSystem gridCRS, final Graphics graphics, int xOffset,
			int yOffset, int width, int height, boolean withHucs,
			boolean withRivers, boolean withRoads) {
		System.out.println("Mapper drawing satellite");
		if (!showSatelliteMap)
			return;
		satelliteMap.draw(domain, gridBounds, gridCRS, graphics, xOffset,
				yOffset, width, height);	// execute the draw function for this VerdiBoundaries layer
	}


	// Draw a domain-clipped, projected, grid-clipped map to graphics screen.
	// E.g., domain[ 2 ][ 2 ] = { { -90.0, -88.0 }, { 28.0, 33.0 } }.
	public void draw(final double[][] domain, final double[][] gridBounds,
			final CoordinateReferenceSystem gridCRS, final Graphics graphics, int xOffset,
			int yOffset, int width, int height, boolean withHucs,
			boolean withRivers, boolean withRoads) {
		System.out.println("Mapper drawing base");
		// 2015 get to this upon completion of gov.epa.emvl.TilePlot - all done with TilePlot.draw
		// gov.epa.emvl.Projector projector: projection of the overall map to be drawn
		// java.awt.Graphics graphics
		// int xOffset
		// int yOffset
		// int width
		// int height
		// boolean withHucs: include HUCs map layer or not
		// boolean withRivers: include Rivers map layer or not
		// boolean withRoads: include Roads map layer or not
		// myMapPane: trying for reference to the JMapPane of the calling FastTilePlot
		// myMapContent: current MapContent for all of the shapefiles to be drawn on the FastTilePlot
		Logger.debug("in Mapper.draw function; number of layers = " + layers.size());

		VerdiBoundaries aVerdiBoundaries = new VerdiBoundaries();
		aVerdiBoundaries = chooseMap(domain); // based on map range assigns base map as
		// US counties, US states, North America, or world
		if(aVerdiBoundaries == null)
			Logger.debug("aVerdiBoundaries is null");
		else
			Logger.debug("have aVerdiBoundaries = " + aVerdiBoundaries.getFileName());	// OK

		graphics.setColor(mapColor);
		Logger.debug("did graphics.setColor for mapColor = " + mapColor); // java.awt.Color[r=0,g=0,b=0]

		if (initialDraw && aVerdiBoundaries != null && !layers.contains(aVerdiBoundaries)) {
			// List<VerdiBoundaries> does not yet include this shapefile
			Logger.debug("have to add base map to layers");	// OK to here
			layers.add(aVerdiBoundaries);	// do not need call to addLayer - already has a VerdiBoundaries object for List
			Logger.debug("number of layers now = " + layers.size());	// now = 1
			initialDraw = false;
		}

		if (layers.contains(hucsMap) && !withHucs)	// if have HUCs layer and don't want it
		{
			layers.remove(hucsMap);
		}

		if (layers.contains(riversMap) && !withRivers)	// if have Rivers layer and don't want it
		{
			layers.remove(riversMap);
		}

		if (layers.contains(roadsMap) && !withRoads)	// if have Roads layer and don't want it
		{
			layers.remove(roadsMap);
		}
		Logger.debug("number of layers now = " + layers.size());	// now = 1
		System.out.println("number of layers now = " + layers.size());	// now = 1

		// start looping through the VerdiBoundaries
		for (VerdiBoundaries layer : layers) {
			Logger.debug("drawing for a layer");	// OK to here
			Logger.debug("layer = " + layer.toString());
			Logger.debug("Instead of passing Projector object to VerdiBoundaries.draw get the CRS and pass it. Calling layer.draw");
			// NEED TO COMPLETE LAYER.DRAW TO ACTUALLY DRAW THE MAP!!! 
// JEB  layers Each VerdiBoundaries has a MapContent
			layer.draw(domain, gridBounds, gridCRS, graphics, xOffset,
					yOffset, width, height);	// execute the draw function for this VerdiBoundaries layer

			Logger.debug("back from drawing for a layer - alternate method from Displaying a Shapefile geotools v14");
			
//			MapContent aThing = layer.getMap();				// TEST
//			Logger.debug("instantiated aThing");
//			JMapFrame mapFrame = new JMapFrame(aThing);	
//			Logger.debug("instantiated the JMapFrame");		
//			mapFrame.enableToolBar(false);
//			mapFrame.enableStatusBar(false);
//			mapFrame.setSize(800, 600);						
//			Logger.debug("set the size");					
//			mapFrame.setVisible(true);						
//			Logger.debug("set mapFrame to visible");
			
//			JMapFrame.showMap(layer.getMap());	// THIS SINGLE LINE: spawns a JMapFrame with no map
												// and a JMapFrame with the one layer map
												// in appropriate projection (that of tile plot)
												// and with its own JMapFrame widget bars
//			Logger.debug("just did the showMap");			// OK here
			graphics.setColor(mapColor); // to reset graphics color
			Logger.debug("just reset graphics color to: " + mapColor);	// OK here
		}
	}
	
	public void setLayerStyle(TilePlotConfiguration config) {
		Color color = config.getLayerColor();
		int width = config.getLayerLineSize();
		try {
			getWorldMap(false).getVerdiStyle().setLayerLine(color, width);
			getNorthAmericaMap(false).getVerdiStyle().setLayerLine(color, width);
			getUsaStatesMap(false).getVerdiStyle().setLayerLine(color, width);
			getUsaCountiesMap(false).getVerdiStyle().setLayerLine(color, width);
			getUSHucMap(false).getVerdiStyle().setLayerLine(color, width);
			getUSRiversMap(false).getVerdiStyle().setLayerLine(color, width);
			getUSRoadsMap(false).getVerdiStyle().setLayerLine(color, width);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public void projectShapefiles() {
		try {
			getWorldMap(true);
			getNorthAmericaMap(true);
			getUsaStatesMap(true);
			getUsaCountiesMap(true);
			getUSHucMap(true);
			getUSRiversMap(true);
			getUSRoadsMap(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public VerdiBoundaries chooseMap(final double[][] domain) {
		return chooseMap(domain, false);
	}

	// Choose a map based on a domain.
	public VerdiBoundaries chooseMap(final double[][] domain, boolean forceReturn) {
		Logger.debug("in Mapper.chooseMap");	// 2015 after Mapper.draw function
		final double yMinimum = domain[Y][MINIMUM];
		Logger.debug("yMinimum = " + yMinimum);
		final double xMaximum = domain[X][MAXIMUM];
		Logger.debug("xMaximum = " + xMaximum);
		VerdiBoundaries result = null;		// this will be the new layer (Shapefile) for the map

		if (xMaximum < -30.0 && yMinimum > 10.0) 
		{ // North America:
			Logger.debug("in North America");

			final double xMinimum = domain[X][MINIMUM];
			final double yMaximum = domain[Y][MAXIMUM];
			final double xRange = xMaximum - xMinimum;
			final double yRange = yMaximum - yMinimum;

			if (xMinimum > -127.0 && xMaximum < -64.0 && yMinimum > 23.0
					&& yMaximum < 50.0) { // US:

				if (xRange < 12.5 && yRange < 12.5) { // County level:
					Logger.debug("in County Level");

					if (countyMap == null) {
						try {
							countyMap = new VerdiBoundaries();
							countyMap.setFileName(mapFileDirectory + countyMapFileName);
							Logger.debug("got county map");
						} catch (Exception unused) {
						}
					}

					result = countyMap;
				} else { // Domain is in the US at the state level:
					Logger.debug("in State Level");

					if (stateMap == null) {
						try {
							stateMap = new VerdiBoundaries();
							stateMap.setFileName(mapFileDirectory + stateMapFileName);
							Logger.debug("got state map");
						} catch (Exception unused) {
						}
					}
					result = stateMap;
				}

			} else { // Domain North America beyond just US:

				Logger.debug("in North America beyond just US");
				if (northAmericaMap == null) {
					try {
						northAmericaMap = new VerdiBoundaries();
						northAmericaMap.setFileName(mapFileDirectory + northAmericaMapFileName);
						Logger.debug("got North America map");
					} catch (Exception unused) {
					}
				}
				result = northAmericaMap;
			}

		} else { // Domain is not entirely in North America:
			Logger.debug("domain is not entirely in North America");

			if (worldMap == null) {
				try {
					worldMap = new VerdiBoundaries();
					worldMap.setFileName(mapFileDirectory + worldMapFileName);
					Logger.debug("got new VerdiBoundaries worldMap = " + worldMap);

				} catch (Exception unused) {
				}
			}
			result = worldMap;
		}

		Logger.debug("result = " + result.getFileName());
		boolean containsResult = layers.contains(result);
		Logger.debug("getting ready to return from Mapper.chooseMap");
		Logger.debug("layers.contains(result) == " + containsResult);	// says false: selected map is not already in layers
		return containsResult && !forceReturn ? null : result;		// if already contained map, return null; if not already contained map return the map as a VerdiBoundaries object
	}

	public VerdiBoundaries getUSHucMap() {
		return getUSHucMap(false);
	}
	
	public VerdiBoundaries getUSHucMap(boolean async) {
		if (hucsMap == null) {
			try {
				hucsMap = new VerdiBoundaries();
				hucsMap.setProjection(projection, targetCRS);
				hucsMap.setFileName(mapFileDirectory + hucsMapFileName, async);
				Logger.debug("got new VerdiBoundaries hucsMap = " + hucsMap);
			} catch (Exception unused) {
			}
		}
		return hucsMap;
	}

	public void removeUSHucMap() {
		if (layers.contains(hucsMap))
			layers.remove(hucsMap);
		Logger.debug("removed hucsMap");
	}

	public VerdiBoundaries getUSRoadsMap() {
		return getUSRoadsMap(false);
	}
	
	public VerdiBoundaries getUSRoadsMap(boolean async) {
		if (roadsMap == null) {
			try {
				roadsMap = new VerdiBoundaries();
				roadsMap.setProjection(projection, targetCRS);
				roadsMap.setFileName(mapFileDirectory + roadsMapFileName, async);
				Logger.debug("got new VerdiBoundaries roadsMap = " + roadsMap);
			} catch (Exception unused) {
			}
		}
		return roadsMap;
	}

	public void removeUSRoadsMap() {
		if (layers.contains(roadsMap))
			layers.remove(roadsMap);
		Logger.debug("removed roadsMap");
	}

	public VerdiBoundaries getUSRiversMap() {
		return getUSRiversMap(false);
	}
	
	public VerdiBoundaries getUSRiversMap(boolean async) {
		if (riversMap == null) {
			try {
				riversMap = new VerdiBoundaries();
				riversMap.setProjection(projection, targetCRS);
				riversMap.setFileName(mapFileDirectory + riversMapFileName, async);
				Logger.debug("got new VerdiBoundaries riversMap = " + riversMap);
			} catch (Exception unused) {
			}
		}
		return riversMap;
	}

	public void removeUSRiversMap() {
		if (layers.contains(riversMap))
			layers.remove(riversMap);
		Logger.debug("removed riversMap");
	}

	public VerdiBoundaries getUsaStatesMap() throws Exception {
		return getUsaStatesMap(false);
	}
	
	public VerdiBoundaries getUsaStatesMap(boolean async) throws Exception {
		try {
			if (stateMap == null)
			{
				stateMap = new VerdiBoundaries();
				stateMap.setProjection(projection, targetCRS);
				stateMap.setFileName(mapFileDirectory + stateMapFileName, async);
			}
			Logger.debug("got new VerdiBoundaries stateMap = " + stateMap);

			return stateMap;
		} catch (Exception e) {
			throw new Exception("Error reading USA states map ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public void removeUsaStates() throws Exception {
		try {
			if (layers.contains(stateMap))
				layers.remove(stateMap);
			Logger.debug("removed layer stateMap");
		} catch (Exception e) {
			throw new Exception("Error removing USA states layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}
	
	public void showSatelliteMap(String path ) {
		getSatelliteMap(path);
		showSatelliteMap = true;
	}
	
	public VerdiBoundaries getSatelliteMap(String path) {
		if (satelliteMap == null) {
			//Get Coordinates
			//download map for coordinates
			//write map to temp dir
			
			//String downloadedMap = System.getProperty("java.io.tmpdir") + File.separator + "dat.jpg";
			//downloadedMap = flipVertically(downloadedMap);
			//create world file /tmp/data.wld
			//create projection file /tmp/data.prj
			satelliteMap = new VerdiBoundaries();
			satelliteMap.setFileName(path);
		}
		return satelliteMap;
	}
	
    private static String flipVertically(String imagePath) {
    	File imageFile = new File(imagePath);
    	BufferedImage image = null;
		try {
			image = ImageIO.read(imageFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        int width = image.getWidth();
        int height = image.getHeight();
        BufferedImage flippedImage = new BufferedImage(width, height, image.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                flippedImage.setRGB(x, height - 1 - y, image.getRGB(x, y));
            }
        }

        File outputFile = new File(System.getProperty("java.io.tmpdir") + File.separator + "data.jpg");
        try {
			ImageIO.write(flippedImage, "jpg", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        return outputFile.getAbsolutePath();
    }
	
	public void removeSatelliteMap() throws Exception {
		showSatelliteMap = false;
	}

	public VerdiBoundaries getUsaCountiesMap() throws Exception {
		return getUsaCountiesMap(false);
	}
	
	public VerdiBoundaries getUsaCountiesMap(boolean async) throws Exception {
		try {
			if (countyMap == null)
			{
				countyMap = new VerdiBoundaries();
				countyMap.setProjection(projection, targetCRS);
				countyMap.setFileName(mapFileDirectory + countyMapFileName, async);
			}
			Logger.debug("got new VerdiBoundaries countyMap = " + countyMap);

			return countyMap;
		} catch (Exception e) {
			throw new Exception("Error finding USA counties map file ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public void removeUsaCounties() throws Exception {
		try {
			if (layers.contains(countyMap))
				layers.remove(countyMap);
			Logger.debug("removed layer countyMap");
		} catch (Exception e) {
			throw new Exception("Error removing USA counties layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public VerdiBoundaries getWorldMap() throws Exception {
		return getWorldMap(false);
	}
	
	public VerdiBoundaries getWorldMap(boolean async) throws Exception {
		try {
			if (worldMap == null)
			{
				worldMap = new VerdiBoundaries();
				worldMap.setProjection(projection, targetCRS);
				worldMap.setFileName(mapFileDirectory + worldMapFileName, async);
			}		
			Logger.debug("got new VerdiBoundaries worldMap = " + worldMap);
			return worldMap;
		} catch (Exception e) {
			throw new Exception("Error reading world layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public void removeWorld() throws Exception {
		try {
			if (layers.contains(worldMap))
				layers.remove(worldMap);
			Logger.debug("removed worldMap");
		} catch (Exception e) {
			throw new Exception("Error removing world layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public VerdiBoundaries getNorthAmericaMap() throws Exception {
		return getNorthAmericaMap(false);
	}
	
	public VerdiBoundaries getNorthAmericaMap(boolean async) throws Exception {
		try {
			if (northAmericaMap == null)
			{
				northAmericaMap = new VerdiBoundaries();
				northAmericaMap.setProjection(projection, targetCRS);
				northAmericaMap.setFileName(mapFileDirectory + northAmericaMapFileName, async);
			}		
			Logger.debug("got new VerdiBoundaries northAmericaMap = " + northAmericaMap);
			return northAmericaMap;
		} catch (Exception e) {
			throw new Exception("Error reading North America layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public void removeNorthAmerica() throws Exception {
		try {
			if (layers.contains(northAmericaMap))
				layers.remove(northAmericaMap);
			Logger.debug("removed northAmericaMap");
		} catch (Exception e) {
			throw new Exception("Error removing North America layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public void addLayer(String path) throws Exception {	// useful for adding "other" map layer
		try {
			VerdiBoundaries layer = new VerdiBoundaries();
			layer.setFileName(path);
			Logger.debug("added map for path = " + path);
			layers.add(layer);
			Logger.debug("\tlayer = " + layer);
		} catch (Exception e) {
			throw new Exception("Error adding a new layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public void removeLayer(String path) throws Exception {	// useful for removing "other" map layer
		try {
			VerdiBoundaries layer = new VerdiBoundaries();
			layer.setFileName(path);
			layers.remove(layer);
			Logger.debug("removed layer");
		} catch (Exception e) {
			throw new Exception("Error removing the layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public List<VerdiBoundaries> getLayers() {
		return layers;
	}

	public void resetLayers() {
		if (layers != null && layers.size() > 0)
			layers.clear();
	}

	public boolean naMapIncluded() {
		return layers.contains(northAmericaMap);
	}

	public boolean usStatesMapIncluded() {
		return layers.contains(stateMap);
	}

	public boolean usCountiesMapIncluded() {
		return layers.contains(countyMap);
	}

	public boolean worldMapIncluded() {
		return layers.contains(worldMap);
	}

	public boolean usHucsMapIncluded() {
		return layers.contains(hucsMap);
	}

	public boolean usRiversMapIncluded() {
		return layers.contains(riversMap);
	}

	public boolean usRoadsMapIncluded() {
		return layers.contains(roadsMap);
	}
	
	public void dispose() {
		if (worldMap != null)
			worldMap.getMap().dispose();
		if (northAmericaMap != null)
			northAmericaMap.getMap().dispose();
		if (stateMap != null)
			stateMap.getMap().dispose();
		if (countyMap != null)
			countyMap.getMap().dispose();
		if (hucsMap != null)
			hucsMap.getMap().dispose();
		if (riversMap != null)
			riversMap.getMap().dispose();
		if (roadsMap != null)
			roadsMap.getMap().dispose();
	}
}
