package anl.verdi.plot.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;


public class VerdiTileUtil {
	
	public static final int TILE_INDEX_ZOOM = 0;
	public static final int TILE_INDEX_X = 1;
	public static final int TILE_INDEX_Y = 2;
	
	private static final String WEB_MERCATOR_PRJ = "PROJCS[\"WGS 84 / Pseudo-Mercator\",\n" + 
			"    GEOGCS[\"WGS 84\", \n" + 
			"      DATUM[\"World Geodetic System 1984\", \n" + 
			"        SPHEROID[\"WGS 84\", 6378137.0, 6378137.0, AUTHORITY[\"EPSG\",\"7030\"]], \n" + 
			"        AUTHORITY[\"EPSG\",\"6326\"]], \n" + 
			"      PRIMEM[\"Greenwich\", 0.0, AUTHORITY[\"EPSG\",\"8901\"]], \n" + 
			"      UNIT[\"degree\", 0.017453292519943295], \n" + 
			"      AXIS[\"Geodetic longitude\", EAST], \n" + 
			"      AXIS[\"Geodetic latitude\", NORTH], \n" + 
			"      AUTHORITY[\"EPSG\",\"4326\"]], \n" + 
			"    PROJECTION[\"Popular Visualisation Pseudo Mercator\", AUTHORITY[\"EPSG\",\"1024\"]], \n" + 
			"    PARAMETER[\"semi_minor\", 6378137.0], \n" + 
			"    PARAMETER[\"latitude_of_origin\", 0.0], \n" + 
			"    PARAMETER[\"central_meridian\", 0.0], \n" + 
			"    PARAMETER[\"scale_factor\", 1.0], \n" + 
			"    PARAMETER[\"false_easting\", 0.0], \n" + 
			"    PARAMETER[\"false_northing\", 0.0], \n" + 
			"    UNIT[\"m\", 1.0], \n" + 
			"    AXIS[\"Easting\", EAST], \n" + 
			"    AXIS[\"Northing\", NORTH], \n" + 
			"    AUTHORITY[\"EPSG\",\"3857\"]]\n" + 
			"";
	
	static GeometryFactory GEOMETRY_FACTORY = JTSFactoryFinder.getGeometryFactory(null);
	static Map<CoordinateReferenceSystem, MathTransform> MATH_TRANSFORM_MAP = new HashMap<CoordinateReferenceSystem, MathTransform>();

	private static VerdiTileUtil INSTANCE = new VerdiTileUtil();
	
	public static VerdiTileUtil getInstance() {
		return INSTANCE;
	}
	
	public static MathTransform getWebMercatorTransform(CoordinateReferenceSystem sourceCRS) {
		MathTransform transform = MATH_TRANSFORM_MAP.get(sourceCRS);
		if (transform == null) {
			try {
				transform = org.geotools.referencing.CRS.findMathTransform(sourceCRS, VerdiShapefileUtil.WEB_MERC_CRS);
			} catch (FactoryException e) {
				e.printStackTrace();
			}
		}
		return transform;
	}
	
	
/*public static void mainOSM(String[] args) {
	VerdiTileUtil util = new VerdiTileUtil();
  int zoom = 10;
  double lat = 47.968056d;
  double lon = -136.9331504476835;
  lat = 35.4851;
  lon = -75.4476;
  zoom = 5;
  String tileString = getTileNumber(lat, lon, zoom);
 // System.out.println("https://tile.openstreetmap.org/" + getTileNumber(lat, lon, zoom) + ".png");
  String[] tile = tileString.split("/");
  BoundingBox bb = util.tile2boundingBox(Integer.parseInt(tile[1]),Integer.parseInt(tile[2]),Integer.parseInt(tile[0]));
  System.out.println(bb);
}*/

/*public static void mainn(String[] args) {
	double lat = 0;
	double lon = 0;
	lat = 20.39212991649467;   // 3 0 3
	//lat = 58.24472951716452; // 3 2 2 

	lon = -136.9331504476835;
	//lon = -57.53838628100842;
	int zoom = 3;
	
	VerdiTileUtil util = new VerdiTileUtil();
	
	String[] tileNumber = getTileNumber(lat, lon, zoom).split("/");
	System.out.println("Getting tile " + Arrays.toString(tileNumber));
	BufferedImage tile = util.retrieveTile(Integer.toString(zoom), tileNumber[1], tileNumber[2]);
	File outputFile = new File("/tmp/tileimage.png");
	try {
		ImageIO.write(tile, "png", outputFile);
	} catch (IOException e) {
		e.printStackTrace();
	}
}*/

public static void main(String[] args) {
	double lonMin = -136.9331504476835;
	double lonMax = -57.53838628100842;
	double latMin = 20.39212991649467;
	double latMax = 58.24472951716452;
	
	int mapWidth = 560;
	int mapHeight = 394;
	double[][] mapDomain = new double[2][2];
	mapDomain[FastTilePlot.LONGITUDE][FastTilePlot.MINIMUM] = lonMin;
	mapDomain[FastTilePlot.LONGITUDE][FastTilePlot.MAXIMUM] = lonMax;
	mapDomain[FastTilePlot.LATITUDE][FastTilePlot.MINIMUM] = latMin;
	mapDomain[FastTilePlot.LATITUDE][FastTilePlot.MAXIMUM] = latMax;;
	
	String savedImage;
	try {
		savedImage = getInstance().retrieveImage(mapDomain, mapWidth, mapHeight);
		System.out.println("Retrieved: " + savedImage);
	} catch (Exception e) {
		e.printStackTrace();
	}

}

/*
 * 
 * 
 * 
 * 
 */

public int calculateZoom( double[][] mapDomain, int screenWidth ) {
	double lonMin = mapDomain[FastTilePlot.LONGITUDE][FastTilePlot.MINIMUM];
	double lonMax = mapDomain[FastTilePlot.LONGITUDE][FastTilePlot.MAXIMUM];
	double latMin = mapDomain[FastTilePlot.LATITUDE][FastTilePlot.MINIMUM];
	//double latMax = mapDomain[FastTilePlot.LATITUDE][FastTilePlot.MAXIMUM];
	double mapRatio = (lonMax- lonMin) / screenWidth;

	//zoom/x/y
	int zoom = 0;
	int x = 0;
	int y = 0;
	BoundingBox bb = null;
	BufferedImage tile = null;
	double tileRatio = mapRatio + 1;
	
	for (int i = 0; i <= 19 && tileRatio > mapRatio; ++i) {
		String[] tileNumber = getTileNumber(latMin, lonMin, i).split("/");
		x = Integer.parseInt(tileNumber[1]);
		y = Integer.parseInt(tileNumber[2]);
		tile = retrieveTile(Integer.toString(i), tileNumber[1], tileNumber[2]);
		if (tile == null)
			break;
		bb = tile2boundingBox(x, y, i);
		double tileDeg = bb.east - bb.west;
		double tilePx = tile.getWidth();
		tileRatio = tileDeg / tilePx;
		if (tileRatio > mapRatio)
			zoom = i;
	}
	return zoom;
}

public static void retrieveImage(double[][] mapDomain, int screenWidth, int screenHeight, AbstractPlotPanel listener) throws Exception {
	new Thread(new TileDownloader(mapDomain, screenWidth, screenHeight, listener)).start();
}

public String retrieveImage(double[][] mapDomain, int screenWidth, int screenHeight) throws Exception {
	
	int zoom = calculateZoom(mapDomain, screenWidth);
	String[] tileNumber = getTileNumber(mapDomain[FastTilePlot.LATITUDE][FastTilePlot.MINIMUM], mapDomain[FastTilePlot.LONGITUDE][FastTilePlot.MINIMUM], zoom).split("/");
	BufferedImage tile = retrieveTile(Integer.toString(zoom), tileNumber[1], tileNumber[2]);
	int tileWidth = tile.getWidth();
	int tileHeight = tile.getHeight();
	
	//Have map origin, map width
	
	String originTile = getTileNumber(mapDomain[FastTilePlot.LATITUDE][FastTilePlot.MINIMUM], 
			mapDomain[FastTilePlot.LONGITUDE][FastTilePlot.MINIMUM],
			zoom);
	String[] originCoords = originTile.split("/");
	
	String endTile = getTileNumber(mapDomain[FastTilePlot.LATITUDE][FastTilePlot.MAXIMUM], 
			mapDomain[FastTilePlot.LONGITUDE][FastTilePlot.MAXIMUM],
			zoom);
	String[] endCoords = endTile.split("/");
	
	int minX = Integer.parseInt(originCoords[TILE_INDEX_X]);
	int maxX = Integer.parseInt(endCoords[TILE_INDEX_X]);
	int minY = Integer.parseInt(endCoords[TILE_INDEX_Y]);
	int maxY = Integer.parseInt(originCoords[TILE_INDEX_Y]);
	
	if (minX > maxX) {
		int temp = minX;
		minX = maxX;
		maxX = temp;
	}	
	if (minY > maxY) {
		int temp = minY;
		minY = maxY;
		maxY = temp;
	}
	//System.out.println("VerdiTileUtil  origin tile " + originTile + " endTile "+ endTile);
	
	int xCoord;
	int yCoord;
	int tileSize = 0;
	BufferedImage image = null;
	Graphics g = null;
	int imageWidth = 0;
	int imageHeight = 0;
	for (int tileX = minX; tileX <= maxX; ++tileX)
		for (int tileY = minY; tileY <= maxY; ++tileY) {
			tile = retrieveTile(Integer.toString(zoom), Integer.toString(tileX), Integer.toString(tileY));
			xCoord = tileWidth * (tileX - minX);
			yCoord = tileHeight  * (tileY - minY);
			if (image == null) {
				tileSize = tile.getWidth();
				imageWidth = tileSize * (maxX - minX + 1);
				imageHeight = tileSize * (maxY - minY + 1);
				image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);	
				g = image.getGraphics();
			}
			xCoord = (tileX - minX) * tileSize;
			yCoord = (tileY - minY) * tileSize;
			g.drawImage(tile,  xCoord,  yCoord, null);
		}
	BoundingBox minCorner = tile2boundingBox(minX,minY,zoom);
	BoundingBox maxCorner = tile2boundingBox(maxX, maxY, zoom);
	BoundingBox imageBox = new BoundingBox();
	imageBox.north = minCorner.north;
	imageBox.south = maxCorner.south;
	imageBox.east = maxCorner.east;
	imageBox.west = minCorner.west;
	
	TileImage tileImg = new TileImage(image, imageBox);
	
	String assembledName = (originTile + endTile).replace("/", "-");
	return createTileMetadata(assembledName, tileImg);
}

public String createTileMetadata(String assembledName, TileImage tileImg) throws Exception {
    int width = tileImg.image.getWidth();
    int height = tileImg.image.getHeight();
    
    BufferedImage flippedImage = new BufferedImage(width, height, tileImg.image.getType());

    for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
            flippedImage.setRGB(x, height - 1 - y, tileImg.image.getRGB(x, y));
        }
    }

    String path = getTempDirectory() + File.separator + "assembled" + File.separator + assembledName + ".jpg";
	File outputFile = new File(path);
	outputFile.mkdirs();
	
    ImageIO.write(flippedImage, "jpg", outputFile);
    //ImageIO.write(tileImg.image, "jpg", outputFile);
    
    String fileBase = outputFile.getAbsolutePath().substring(0, outputFile.getAbsolutePath().length() - 3);
    File wldFile  = new File(fileBase + "wld");
    File prjFile  = new File(fileBase + "prj");
    outputFile.deleteOnExit();
    wldFile.deleteOnExit();
    prjFile.deleteOnExit();  

    String wldString = tileImg.getWldData();
    
    Files.write(wldString, wldFile, Charsets.UTF_8);
    //Files.write(WGS84_PRJ, prjFile, Charsets.UTF_8);
    //Files.write(WEB_MERCATOR_PRJ, prjFile, Charsets.UTF_8);
    Files.write(WEB_MERCATOR_PRJ, prjFile, Charsets.UTF_8);
    //Files.write(WEB_MERCATOR_3857_PRJ, prjFile, Charsets.UTF_8);
    //Files.write(SPHERE_PRJ, prjFile, Charsets.UTF_8);
    
    
    		
	String downloadedImage = outputFile.getAbsolutePath();
	return downloadedImage;

}

//TODO - ensure assmebledName identifies TILE_PROVIDER
static String TILE_PROVIDER = "https://tile.opentopomap.org/{z}/{x}/{y}.png";

public static String getTempDirectory() {
	return System.getProperty("user.home") + File.separator + "verdi" + File.separator + "tmp";
}

//TODO - choose tile provider
//https://wiki.openstreetmap.org/wiki/Raster_tile_providers
public static BufferedImage retrieveTile(String zoom, String x, String y) {
	BufferedImage tile = null;
	String path = getTempDirectory() + File.separator + "tile" + File.separator + zoom + File.separator + x + File.separator;
	File tileDir = new File(path);
	tileDir.mkdirs();
	File tileFile = new File(path + File.separator + y + ".png");
	String remotePath =TILE_PROVIDER.replace("{z}", zoom).replace("{x}", x).replace("{y}", y);

	//TODO check tile age also
	if (!tileFile.exists()) {
		try {
			URL remoteURL = new URL(remotePath);
			HttpClient client = new HttpClient();
			org.apache.commons.httpclient.HttpMethod get = new GetMethod(remotePath);
			int ret = client.executeMethod(get);
			if (ret == 404)
				return null;
			//System.out.println(remotePath + ": " + ret);
		    FileUtils.copyInputStreamToFile(get.getResponseBodyAsStream(), tileFile);

		} catch (Throwable e) {
			//TODO - handle java.net.SocketTimeoutException: Read timed out
			e.printStackTrace();
		}
	}
	if (!tileFile.exists()) {
		System.err.println("Could not retrieve " + remotePath);
		return null;
	}
	tileFile.setLastModified(System.currentTimeMillis());
	try {
		tile = ImageIO.read(tileFile);
	} catch (IOException e) {
		e.printStackTrace();
	}
	return tile;

}



public static String getTileNumber(final double lat, final double lon, final int zoom) {
  int xtile = (int)Math.floor( (lon + 180) / 360 * (1<<zoom) ) ;
  int ytile = (int)Math.floor( (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1<<zoom) ) ;
   if (xtile < 0)
    xtile=0;
   if (xtile >= (1<<zoom))
    xtile=((1<<zoom)-1);
   if (ytile < 0)
    ytile=0;
   if (ytile >= (1<<zoom))
    ytile=((1<<zoom)-1);
   return(zoom + "/" + xtile + "/" + ytile);
  }

public static Envelope transformToWebMercator(ReferencedEnvelope envelope) throws TransformException {
	return JTS.transform(envelope, getWebMercatorTransform(envelope.getCoordinateReferenceSystem()));
}

class TileImage {
	BoundingBox bb;
	BufferedImage image;
	
	public TileImage(BufferedImage image, BoundingBox bb) {
		this.image = image;
		this.bb = bb;
	}
	
    public String getWldData() throws Exception {
    	//Coordinates should be specified (lat,lon)
    	//Y should be lower coordinate
		double tileImageWidthPx = image.getWidth();
		double tileImageHeightPx = image.getHeight();
		double tileImageNorthDeg = bb.north;
		double tileImageSouthDeg = bb.south;
		double tileImageEastDeg = bb.east;
		double tileImageWestDeg = bb.west;
    	
		//Order worked in ImageLab but opposite documentation
    	//com.vividsolutions.jts.geom.Point topLeftDeg = (com.vividsolutions.jts.geom.Point)geometryFactory.createPoint(new Coordinate(tileImageNorthDeg, tileImageWestDeg));
    	//com.vividsolutions.jts.geom.Point bottomRightDeg = (com.vividsolutions.jts.geom.Point)geometryFactory.createPoint(new Coordinate(tileImageSouthDeg, tileImageEastDeg));
    	
		MathTransform transform = getWebMercatorTransform(VerdiShapefileUtil.LAT_LON_CRS);
    	Point topLeftDeg = (Point)GEOMETRY_FACTORY.createPoint(new Coordinate(tileImageWestDeg, tileImageNorthDeg));
    	Point bottomRightDeg = (Point)GEOMETRY_FACTORY.createPoint(new Coordinate(tileImageEastDeg, tileImageSouthDeg));
    	
		Point topLeftM = (Point)JTS.transform(topLeftDeg, transform);
		Point bottomRightM = (Point)JTS.transform(bottomRightDeg, transform);
    	
//    	Point topLeftDeltaDeg = (Point)GEOMETRY_FACTORY.createPoint(new Coordinate(tileImageWestDeg, 90 - tileImageNorthDeg));
//		Point topLeftDeltaM = (Point)JTS.transform(topLeftDeltaDeg, transform);

//    	Point bottomRightDeltaDeg = (Point)GEOMETRY_FACTORY.createPoint(new Coordinate(tileImageWestDeg, 90 - tileImageSouthDeg));

		double tileWidthM = bottomRightM.getX() - topLeftM.getX();
		double tileHeightM = topLeftM.getY() - bottomRightM.getY();
		
		double unitsPxX = tileWidthM / tileImageWidthPx;
		double unitsPxY = tileHeightM / tileImageHeightPx;
		
		StringBuffer wldBuffer = new StringBuffer();
		wldBuffer.append(unitsPxX + "\n");
		wldBuffer.append("0\n");
		wldBuffer.append("0\n");
		wldBuffer.append(unitsPxY + "\n");
		wldBuffer.append(String.format("%.9f", topLeftM.getX()) + "\n");
		wldBuffer.append(String.format("%.9f", bottomRightM.getY()) + "\n");
		return wldBuffer.toString();
    }
	
}

class BoundingBox {
    double north;
    double south;
    double east;
    double west;   
    
    public String toString() {
    	return north + "," + south + "," + east + "," + west;
    }
  }

  BoundingBox tile2boundingBox(final int x, final int y, final int zoom) {
    BoundingBox bb = new BoundingBox();
    bb.north = tile2lat(y, zoom);
    bb.south = tile2lat(y + 1, zoom);
    bb.west = tile2lon(x, zoom);
    bb.east = tile2lon(x + 1, zoom);
    return bb;
  }

  static double tile2lon(int x, int z) {
     return x / Math.pow(2.0, z) * 360.0 - 180;
  }

  static double tile2lat(int y, int z) {
    double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
    return Math.toDegrees(Math.atan(Math.sinh(n)));
  }
  
  private static final long MAX_TEMP_FILE_AGE = 30l * 24 * 60 * 60 * 1000;
  
  public static void purgeTempDirectory() {
	  File tempDir = new File(getTempDirectory());
	  Date thresholdDate = new Date(System.currentTimeMillis() - MAX_TEMP_FILE_AGE);	  

	  if (tempDir.exists()) {
	      Iterator<File> filesToDelete =
	          FileUtils.iterateFiles(tempDir, new AgeFileFilter(thresholdDate), TrueFileFilter.INSTANCE);
	      while (filesToDelete.hasNext()) {
	          FileUtils.deleteQuietly(filesToDelete.next());
	      }  
	  }
	  
  }
  
}