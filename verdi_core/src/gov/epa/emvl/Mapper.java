/** Mapper.java - Manage a set of maps for drawing.
 * 2008-09-01 plessel.todd@epa.gov
 * javac Map*.java ; appletviewer Mapplet.html
 */

package gov.epa.emvl;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

// Contains a set of MapLines and draws them clipped to a domain.

public class Mapper {

	// Attributes:
	static final Logger Logger = LogManager.getLogger(Mapper.class.getName());

	private static final int X = 0;
	private static final int Y = 1;
	private static final int MINIMUM = 0;
	private static final int MAXIMUM = 1;
	private static final String worldMapFileName = "map_world.bin";
	private static final String northAmericaMapFileName = "map_na.bin";
	private static final String stateMapFileName = "map_states.bin";
	private static final String countyMapFileName = "map_counties.bin";
	private static final String hucsMapFileName = "map_hucs.bin";
	private static final String riversMapFileName = "map_rivers.bin";
	private static final String roadsMapFileName = "map_roads.bin";
	private static final Color mapColor = Color.black;
	private final String mapFileDirectory; // URL/dir containing above files.
	private MapLines worldMap = null;
	private MapLines northAmericaMap = null;
	private MapLines stateMap = null;
	private MapLines countyMap = null;
	private MapLines hucsMap = null;
	private MapLines riversMap = null;
	private MapLines roadsMap = null;
	private List<MapLines> layers = new ArrayList<MapLines>();
	private boolean initialDraw = true;

	// Construct with URL or directory containing the map files.
	public Mapper(String directoryName) {
		Logger.debug("in constructor for Mapper, directoryName = " + directoryName);
		mapFileDirectory = directoryName + "/";
		try {
			getWorldMap();
			getNorthAmericaMap();
			getUsaStatesMap();
			getUsaCountiesMap();
			getUSHucMap();
			getUSRiversMap();
			getUSRoadsMap();
			Logger.debug("done with get map functions, leaving Mapper constructor");
		} catch (Exception e) {
			//
		}
	}

	// Draw a domain-clipped, projected, grid-clipped map to graphics screen.
	// E.g., domain[ 2 ][ 2 ] = { { -90.0, -88.0 }, { 28.0, 33.0 } }.
	public void draw(final double[][] domain, final double[][] gridBounds,
			final Projector projector, final Graphics graphics, int xOffset,
			int yOffset, int width, int height, boolean withHucs,
			boolean withRivers, boolean withRoads) {
		Logger.debug("in Mapper.draw function");
		final MapLines mapLines = chooseMap(domain); // MERCATOR

		graphics.setColor(mapColor);

		if (initialDraw && mapLines != null && !layers.contains(mapLines)) {
			layers.add(mapLines);
			initialDraw = false;
		}

		if (layers.contains(hucsMap) && !withHucs)
			layers.remove(hucsMap);

		if (layers.contains(riversMap) && !withRivers)
			layers.remove(riversMap);

		if (layers.contains(roadsMap) && !withRoads)
			layers.remove(roadsMap);

		for (MapLines layer : layers) {
			layer.draw(domain, gridBounds, projector, graphics, xOffset,
					yOffset, width, height);
			graphics.setColor(mapColor); // to reset graphics color
		}
	}

	// Choose a map based on a domain.

	private MapLines chooseMap(final double[][] domain) {
		Logger.debug("in Mapper.chooseMap");
		final double yMinimum = domain[Y][MINIMUM];
		final double xMaximum = domain[X][MAXIMUM];
		MapLines result = null;

		if (xMaximum < -30.0 && yMinimum > 10.0) { // North America:

			final double xMinimum = domain[X][MINIMUM];
			final double yMaximum = domain[Y][MAXIMUM];
			final double xRange = xMaximum - xMinimum;
			final double yRange = yMaximum - yMinimum;

			if (xMinimum > -127.0 && xMaximum < -64.0 && yMinimum > 23.0
					&& yMaximum < 50.0) { // US:

				if (xRange < 12.5 && yRange < 12.5) { // County level:

					if (countyMap == null) {
						try {
							countyMap = new MapLines(mapFileDirectory
									+ countyMapFileName);
						} catch (Exception unused) {
						}
					}

					result = countyMap;
				} else { // Domain is in the US at the state level:

					if (stateMap == null) {
						try {
							stateMap = new MapLines(mapFileDirectory
									+ stateMapFileName);
						} catch (Exception unused) {
						}
					}

					result = stateMap;
				}

			} else { // Domain North America beyond just US:

				if (northAmericaMap == null) {
					try {
						northAmericaMap = new MapLines(mapFileDirectory
								+ northAmericaMapFileName);
					} catch (Exception unused) {
					}
				}

				result = northAmericaMap;

			}

		} else { // Domain is not entirely in North America:

			if (worldMap == null) {
				try {
					worldMap = new MapLines(mapFileDirectory + worldMapFileName);
					Logger.debug("got new MapLines worldMap = " + worldMap);

				} catch (Exception unused) {
				}
			}

			result = worldMap;
		}

		Logger.debug("getting ready to return from Mapper.chooseMap, value = " + (layers.contains(result) ? null : result));
		return layers.contains(result) ? null : result;		// 2014 Why is the line this way instead of the way that was commented out (below)?
		//return layers.contains(result) ? result : null;
	}

	public MapLines getUSHucMap() {
		if (hucsMap == null) {
			try {
				hucsMap = new MapLines(mapFileDirectory + hucsMapFileName);
				Logger.debug("got new MapLines hucsMap = " + hucsMap);
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

	public MapLines getUSRoadsMap() {
		if (roadsMap == null) {
			try {
				roadsMap = new MapLines(mapFileDirectory + roadsMapFileName);
				Logger.debug("got new MapLines roadsMap = " + roadsMap);
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

	public MapLines getUSRiversMap() {
		if (riversMap == null) {
			try {
				riversMap = new MapLines(mapFileDirectory + riversMapFileName);
				Logger.debug("got new MapLines riversMap = " + riversMap);
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

	public MapLines getUsaStatesMap() throws Exception {
		try {
			if (stateMap == null)
				stateMap = new MapLines(mapFileDirectory + stateMapFileName);
			Logger.debug("got new MapLines stateMap = " + stateMap);

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

	public MapLines getUsaCountiesMap() throws Exception {
		try {
			if (countyMap == null)
				countyMap = new MapLines(mapFileDirectory + countyMapFileName);
			Logger.debug("got new MapLines countyMap = " + countyMap);

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

	public MapLines getWorldMap() throws Exception {
		try {
			if (worldMap == null)
				worldMap = new MapLines(mapFileDirectory + worldMapFileName);
			Logger.debug("got new MapLines worldMap = " + worldMap);
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

	public MapLines getNorthAmericaMap() throws Exception {
		try {
			if (northAmericaMap == null)
				northAmericaMap = new MapLines(mapFileDirectory
						+ northAmericaMapFileName);
			Logger.debug("got new MapLines northAmericaMap = " + northAmericaMap);
			return northAmericaMap;
		} catch (Exception e) {
			throw new Exception("Error reading north America layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public void removeNorthAmerica() throws Exception {
		try {
			if (layers.contains(northAmericaMap))
				layers.remove(northAmericaMap);
			Logger.debug("removed northAmericaMap");
		} catch (Exception e) {
			throw new Exception("Error removing north America layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public void addLayer(String path) throws Exception {
		try {
			MapLines layer = new MapLines(path);
			Logger.debug("added map for path = " + path);
			layers.add(layer);
			Logger.debug("\tlayer = " + layer);
		} catch (Exception e) {
			throw new Exception("Error adding a new layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public void removeLayer(String path) throws Exception {
		try {
			MapLines layer = new MapLines(path);
			layers.remove(layer);
			Logger.debug("removed layer");
		} catch (Exception e) {
			throw new Exception("Error removing the layer ("
					+ e.getLocalizedMessage() + ").");
		}
	}

	public List<MapLines> getLayers() {
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
}
