package gov.epa.emvl;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.imageio.ImageIO;

import org.geotools.geometry.jts.ReferencedEnvelope;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.vividsolutions.jts.geom.Envelope;

import anl.verdi.plot.gui.VerdiShapefileUtil;

public class SatelliteImageManager {
	
	private static final String WGS84_PRJ = "GEOGCS[\"WGS 84\",\n" + 
			"    DATUM[\"WGS_1984\",\n" + 
			"        SPHEROID[\"WGS 84\",6378137,298.257223563,\n" + 
			"            AUTHORITY[\"EPSG\",\"7030\"]],\n" + 
			"        AUTHORITY[\"EPSG\",\"6326\"]],\n" + 
			"    PRIMEM[\"Greenwich\",0,\n" + 
			"        AUTHORITY[\"EPSG\",\"8901\"]],\n" + 
			"    UNIT[\"degree\",0.0174532925199433,\n" + 
			"        AUTHORITY[\"EPSG\",\"9122\"]],\n" + 
			"    AUTHORITY[\"EPSG\",\"4326\"]]";
	
	private static final String LATLON_PRJ = "GEOGCS[\"Normal Sphere (r=6370000.0\", \n" + 
			"    DATUM[\"World Geodetic System 1984\",\n" + 
			"      SPHEROID[\"SPHERE\", 6370000.0, 0.0]],\n" + 
			"    PRIMEM[\"Greenwich\", 0.0],\n" + 
			"    UNIT[\"degree\", 0.017453292519943295],\n" + 
			"    AXIS[\"Geodetic longitude\", EAST],\n" + 
			"    AXIS[\"Geodetic latitude\", NORTH]]\n" + 
			"";
	
	//java.text.ParseException: Error in "PROJCS": Parameter "EXTENSION" was not expected.
	private static final String WEB_MERCATOR_3857_PRJ = "PROJCS[\"WGS 84 / Pseudo-Mercator\",\n" + 
			"    GEOGCS[\"WGS 84\",\n" + 
			"        DATUM[\"WGS_1984\",\n" + 
			"            SPHEROID[\"WGS 84\",6378137,298.257223563,\n" + 
			"                AUTHORITY[\"EPSG\",\"7030\"]],\n" + 
			"            AUTHORITY[\"EPSG\",\"6326\"]],\n" + 
			"        PRIMEM[\"Greenwich\",0,\n" + 
			"            AUTHORITY[\"EPSG\",\"8901\"]],\n" + 
			"        UNIT[\"degree\",0.0174532925199433,\n" + 
			"            AUTHORITY[\"EPSG\",\"9122\"]],\n" + 
			"        AUTHORITY[\"EPSG\",\"4326\"]],\n" + 
			"    PROJECTION[\"Mercator_1SP\"],\n" + 
			"    PARAMETER[\"central_meridian\",0],\n" + 
			"    PARAMETER[\"scale_factor\",1],\n" + 
			"    PARAMETER[\"false_easting\",0],\n" + 
			"    PARAMETER[\"false_northing\",0],\n" + 
			"    UNIT[\"metre\",1,\n" + 
			"        AUTHORITY[\"EPSG\",\"9001\"]],\n" + 
			"    AXIS[\"Easting\",EAST],\n" + 
			"    AXIS[\"Northing\",NORTH],\n" + 
			"    AUTHORITY[\"EPSG\",\"3857\"]]";
	
	private static final String WEB_MERCATOR_3857_PRJ_ORIG = "PROJCS[\"WGS 84 / Pseudo-Mercator\",\n" + 
			"    GEOGCS[\"WGS 84\",\n" + 
			"        DATUM[\"WGS_1984\",\n" + 
			"            SPHEROID[\"WGS 84\",6378137,298.257223563,\n" + 
			"                AUTHORITY[\"EPSG\",\"7030\"]],\n" + 
			"            AUTHORITY[\"EPSG\",\"6326\"]],\n" + 
			"        PRIMEM[\"Greenwich\",0,\n" + 
			"            AUTHORITY[\"EPSG\",\"8901\"]],\n" + 
			"        UNIT[\"degree\",0.0174532925199433,\n" + 
			"            AUTHORITY[\"EPSG\",\"9122\"]],\n" + 
			"        AUTHORITY[\"EPSG\",\"4326\"]],\n" + 
			"    PROJECTION[\"Mercator_1SP\"],\n" + 
			"    PARAMETER[\"central_meridian\",0],\n" + 
			"    PARAMETER[\"scale_factor\",1],\n" + 
			"    PARAMETER[\"false_easting\",0],\n" + 
			"    PARAMETER[\"false_northing\",0],\n" + 
			"    UNIT[\"metre\",1,\n" + 
			"        AUTHORITY[\"EPSG\",\"9001\"]],\n" + 
			"    AXIS[\"Easting\",EAST],\n" + 
			"    AXIS[\"Northing\",NORTH],\n" + 
			"    EXTENSION[\"PROJ4\",\"+proj=merc +a=6378137 +b=6378137 +lat_ts=0 +lon_0=0 +x_0=0 +y_0=0 +k=1 +units=m +nadgrids=@null +wktext +no_defs\"],\n" + 
			"    AUTHORITY[\"EPSG\",\"3857\"]]";
	
	private static final String WEB_MERCATOR_PRJ = "PROJCRS[\"WGS 84 / Pseudo-Mercator\",\n" + 
			"    BASEGEOGCRS[\"WGS 84\",\n" + 
			"        ENSEMBLE[\"World Geodetic System 1984 ensemble\",\n" + 
			"            MEMBER[\"World Geodetic System 1984 (Transit)\", ID[\"EPSG\",1166]],\n" + 
			"            MEMBER[\"World Geodetic System 1984 (G730)\",    ID[\"EPSG\",1152]],\n" + 
			"            MEMBER[\"World Geodetic System 1984 (G873)\",    ID[\"EPSG\",1153]],\n" + 
			"            MEMBER[\"World Geodetic System 1984 (G1150)\",   ID[\"EPSG\",1154]],\n" + 
			"            MEMBER[\"World Geodetic System 1984 (G1674)\",   ID[\"EPSG\",1155]],\n" + 
			"            MEMBER[\"World Geodetic System 1984 (G1762)\",   ID[\"EPSG\",1156]],\n" + 
			"            MEMBER[\"World Geodetic System 1984 (G2139)\",   ID[\"EPSG\",1309]],\n" + 
			"            ELLIPSOID[\"WGS 84\", 6378137, 298.257223563, LENGTHUNIT[\"metre\", 1, ID[\"EPSG\",9001]], ID[\"EPSG\",7030]],\n" + 
			"            ENSEMBLEACCURACY[2], ID[\"EPSG\",6326]],\n" + 
			"        ID[\"EPSG\",4326]],\n" + 
			"    CONVERSION[\"Popular Visualisation Pseudo-Mercator\",\n" + 
			"        METHOD[\"Popular Visualisation Pseudo Mercator\", ID[\"EPSG\",1024]],\n" + 
			"        PARAMETER[\"Latitude of natural origin\",  0, ANGLEUNIT[\"degree\", 0.0174532925199433, ID[\"EPSG\",9102]], ID[\"EPSG\",8801]],\n" + 
			"        PARAMETER[\"Longitude of natural origin\", 0, ANGLEUNIT[\"degree\", 0.0174532925199433, ID[\"EPSG\",9102]], ID[\"EPSG\",8802]],\n" + 
			"        PARAMETER[\"False easting\",               0, LENGTHUNIT[\"metre\", 1,                  ID[\"EPSG\",9001]], ID[\"EPSG\",8806]],\n" + 
			"        PARAMETER[\"False northing\",              0, LENGTHUNIT[\"metre\", 1,                  ID[\"EPSG\",9001]], ID[\"EPSG\",8807]],\n" + 
			"        ID[\"EPSG\",3856]],\n" + 
			"    CS[Cartesian, 2, ID[\"EPSG\",4499]],\n" + 
			"    AXIS[\"Easting (X)\", east],\n" + 
			"    AXIS[\"Northing (Y)\", north],\n" + 
			"    LENGTHUNIT[\"metre\", 1, ID[\"EPSG\",9001]],\n" + 
			"    ID[\"EPSG\",3857]]";
	
	private static final String STD_PRJ = "PROJCS[\"unknown\", \n" + 
			"  GEOGCS[\"GCS_unknown\", \n" + 
			"    DATUM[\"D_unknown\", \n" + 
			"      SPHEROID[\"unknown\", 6370001.0, 0.0]], \n" + 
			"    PRIMEM[\"Greenwich\", 0.0], \n" + 
			"    UNIT[\"degree\", 0.017453292519943295], \n" + 
			"    AXIS[\"Geodetic longitude\", EAST], \n" + 
			"    AXIS[\"Geodetic latitude\", NORTH]], \n" + 
			"  PROJECTION[\"Lambert_Conformal_Conic_2SP\"], \n" + 
			"  PARAMETER[\"central_meridian\", -97.0], \n" + 
			"  PARAMETER[\"latitude_of_origin\", 40.0], \n" + 
			"  PARAMETER[\"standard_parallel_1\", 45.0], \n" + 
			"  PARAMETER[\"false_easting\", 0.0], \n" + 
			"  PARAMETER[\"false_northing\", 0.0], \n" + 
			"  PARAMETER[\"scale_factor\", 1.0], \n" + 
			"  PARAMETER[\"standard_parallel_2\", 33.0], \n" + 
			"  UNIT[\"m\", 1.0], \n" + 
			"  AXIS[\"x\", EAST], \n" + 
			"  AXIS[\"y\", NORTH]]\n" + 
			"";
	private static final NumberFormat COORD_FORMATTER = new DecimalFormat("#0.00000");
	
	private static final String MAPBOX_URL = "https://api.mapbox.com/styles/v1/mapbox/satellite-v9/static/[LON_MIN,LAT_MIN,LON_MAX,LAT_MAX]/XRESxYRES?access_token=MAPBOX_TOKEN";
	private static final String MAPBOX_TOKEN = "pk.eyJ1IjoidGFob3dhcmQ5NiIsImEiOiJjbTBjdGphbWowNjdqMmlxMTEyejNkNndoIn0.gQYDB3yt--NRlehJqPSwfQ";	
	
	public static String prepareImage(Envelope envelope, double latMin, double latMax, double lonMin, double lonMax, int xres, int yres) throws IOException {
		
		System.out.println("SatelliteManager.prepareImage TODO: handle exceptions");
		System.out.println(envelope + " latMin " + latMin + " latMax " + latMax + " lonMin " + lonMin + " lonMax " + lonMax + " xres " + xres + " yres " + yres);
		
		//Max Mapbox image resolution is 1280x1280
		double scaleFactor = 1;
		if (xres > 1280)
			scaleFactor = 1280.0 / xres;
		else if (yres > 1280)
			scaleFactor = 1280.0 / yres;
		if (scaleFactor != 1) {
			xres = (int)Math.round(xres * scaleFactor);
			yres = (int)Math.round(yres * scaleFactor);
		}
		int imgWidth = xres;
		int imgHeight = yres;
		
		String url = MAPBOX_URL.replace("LAT_MIN", COORD_FORMATTER.format(latMin));
		url = url.replace("LON_MIN", COORD_FORMATTER.format(lonMin));
		url = url.replace("LAT_MAX", COORD_FORMATTER.format(latMax));
		url = url.replace("LON_MAX", COORD_FORMATTER.format(lonMax));
		url = url.replace("XRES", Integer.toString(imgWidth));
		url = url.replace("YRES", Integer.toString(imgHeight));
		url = url.replace("MAPBOX_TOKEN", MAPBOX_TOKEN);
		
		System.out.println(url);
		URL urlObj = new URL(url);
		//BufferedImage img = ImageIO.read(urlObj);
		BufferedImage img = ImageIO.read(new File("/tmp/tileimage.png"));
		
        int width = img.getWidth();
        int height = img.getHeight();
        BufferedImage flippedImage = new BufferedImage(width, height, img.getType());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                flippedImage.setRGB(x, height - 1 - y, img.getRGB(x, y));
            }
        }

        File outputFile = File.createTempFile("verdibkg", ".jpg");
        try {
			ImageIO.write(flippedImage, "jpg", outputFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        String fileBase = outputFile.getAbsolutePath().substring(0, outputFile.getAbsolutePath().length() - 3);
        File wldFile  = new File(fileBase + "wld");
        File prjFile  = new File(fileBase + "prj");
        outputFile.deleteOnExit();
        wldFile.deleteOnExit();
        prjFile.deleteOnExit();
        
         
        ReferencedEnvelope customEnvelope = new ReferencedEnvelope(-9857000, 2504000, 87780, 3809000, VerdiShapefileUtil.LAT_LON_CRS);
        envelope = customEnvelope;
        
        int xUnitsPx = (int)Math.round(envelope.getWidth() / width);
        int yUnitsPx = (int)Math.round(envelope.getHeight() / height);

        String wldString = xUnitsPx + "\n" +
        		"0\n" +
        		"0\n" +
        		yUnitsPx + "\n" +
        		envelope.getMinX() + "\n" +
        		envelope.getMinY();
        
        Files.write(wldString, wldFile, Charsets.UTF_8);
        //Files.write(WGS84_PRJ, prjFile, Charsets.UTF_8);
        //Files.write(WEB_MERCATOR_PRJ, prjFile, Charsets.UTF_8);
        Files.write(STD_PRJ, prjFile, Charsets.UTF_8);
        //Files.write(WEB_MERCATOR_3857_PRJ, prjFile, Charsets.UTF_8);
        //Files.write(SPHERE_PRJ, prjFile, Charsets.UTF_8);
        
        
        		
		String downloadedImage = outputFile.getAbsolutePath();
		System.out.println(downloadedImage);
		return downloadedImage;
		
	}
	

	/*public static String getImagePath(String coordinates) {
		return "";
	}*/

}
