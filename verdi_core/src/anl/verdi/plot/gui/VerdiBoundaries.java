/**
 * VerdiBoundaries.java
 * Read a shapefile, clip it to a raster (grid) file, clip a raster (grid) file to this shapefile
 * Replaces old gov.epa.emvl.MapLines.java, which provided similar functionality for old .BIN files
 * 
 * @author Jo Ellen Brandmeyer, Institute for the Environment, 2015
 *
 */

package anl.verdi.plot.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.map.Layer;
import org.geotools.map.MapContent;
import org.geotools.map.StyleLayer;
import org.geotools.renderer.GTRenderer;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.ContrastEnhancement;
import org.geotools.styling.RasterSymbolizer;
import org.geotools.styling.SLD;
import org.geotools.styling.SelectedChannelType;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.JMapFrame;
import org.geotools.swing.action.SafeAction;
import org.opengis.filter.FilterFactory;
//import org.opengis.geometry.Geometry;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.TransformException;
import org.opengis.style.ChannelSelection;
import org.opengis.style.ContrastMethod;

import com.vividsolutions.jts.geom.Envelope;

// VerdiBoundaries provides VerdiStyle and CRS for a shapefile (e.g., state boundaries)
// & transforms it to the CRS of a grid file
// independent of time & vertical layer

import ucar.unidata.geoloc.Projection;

public class VerdiBoundaries {

	static final Logger Logger = LogManager.getLogger(VerdiBoundaries.class.getName());
	private VerdiStyle aVerdiStyle = null;
	private Color vColor = Color.BLACK;		// set default boundaries color as BLACK
	private String vFileName = null;		// name of shapefile
	private File vFile = null;				// shapefile represented as a File
	private String vPath = null;			// vPath is absolute path to shapefile
	//private FileDataStore vStore = null;	// vStore is the FileDataStore associated with the vFile
	//private FeatureSource vFeatureSource = null;
	private MapContent vMap = null;			// 
	//private MathTransform vTransform = null;	// math transform from shapefile to grid CRS
	private Projection vProjection = null;
	private CoordinateReferenceSystem vCRS = null;
	
	public VerdiBoundaries()		// default constructor
	{
		reset();
		Logger.debug("done with default constructor for VerdiBoundaries");	// JEB done 9 times - each of supplied shapefiles + selected shapefile
	}
	
	public boolean setFileName(String aFileName) {
		return setFileName(aFileName, false);
	}
	
	public boolean setFileName(String aFileName, boolean async)	// set values from a file name belonging to a shapefile
	{
		Logger.debug("setting file name in VerdiBoundaries to: " + aFileName);		// JEB OK done once per std shapefile (7 times)
		if(aFileName == null)		// cannot pass in a null for the file name
			return false;
		if(vFileName != null)		// already had one, so reset before storing this new one
			reset();
		vFileName = new String(aFileName);
									// looking for "shp" at end of file name to designate a Shapefile
		String last3Chars = new String(vFileName.substring(vFileName.length() - 3, vFileName.length()));
		Logger.debug("fileExtension = " + last3Chars);		// JEB OK done 7 times
		
		boolean isShapefile = true;
		// check that file name has extension "shp" or "jpg" ignorecase
		// if not, reset() and return false;
		if(last3Chars.compareToIgnoreCase("jpg") == 0) {
			//Check for jgw
			isShapefile = false;
		}

		else if(last3Chars.compareToIgnoreCase("shp") != 0)
		{
			Logger.error("File name must have extension shp or jpg - select a different file.");
			reset();
			return false;
		}
		
		
		// here check that the file exists
		// if not, reset() and return false;
		
		vFile = new File(vFileName);			// get File object for this file name
		if(!vFile.exists() || !vFile.canRead() || vFile.isDirectory())
		{
			Logger.error("File " + vFileName + " does not exist, cannot be read, or is a directory");
			reset();
			return false;
		}
		vPath = vFile.getAbsolutePath();		// definition taken from FastTileAddLayerWizard
		vMap = new MapContent();	// THINK THROUGH THIS SOME MORE - JEB
		aVerdiStyle = new VerdiStyle(vFile);	// read/compute everything to make a VerdiStyle for this File
		if(aVerdiStyle == null)
		{
			Logger.debug("VerdiBoundaries.setFileName failed; unable to create the aVerdiStyle.");
			return false;
		}
		if (isShapefile)
			aVerdiStyle.projectShapefile(vProjection, vCRS, async);
//		else
//			aVerdiStyle.projectImage(vProjection,  vCRS);
		
		Logger.debug("Successfully completed VerdiBoundaries.setFileName for: " + vPath +
				" and back from instantiating VerdiStyle.");	// JEB BACK FROM CREATING new VerdiStyle
		return true;	// successfully set data members		// JEB YES returning to calling pgm (Mapper)
	}
	
	public void setProjection(Projection proj, CoordinateReferenceSystem targetCRS) {
		vProjection = proj;
		vCRS = targetCRS;
	}
	
	public void reset()	// reset member variables to null
	{
		aVerdiStyle = null;
		vColor = Color.black;
		vFileName = null;
		vFile = null;
		vPath = null;
		//vStore = null;
		//vFeatureSource = null;
		if (vMap != null)
			vMap.dispose();
		vMap = null;
		//vTransform = null;
	}
	
	Exception creation = new Exception();
	public void draw(double[][] domain, double[][] gridBounds, CoordinateReferenceSystem gridCRS, Graphics graphics,
			int xOffset, int yOffset, int width, int height)	// execute the draw function for this VerdiBoundaries layer
	{	
		boolean isImage = false;
		if (vFile.toString().endsWith(".jpg")) {
			isImage = true;
		}
		Logger.debug("in VerdiBoundaries.draw; ready to getStyle() and return theStyle");
		if (aVerdiStyle == null) {
			System.err.println("Pending failure: " + this);
			creation.printStackTrace();
		}
		Style theStyle = aVerdiStyle.getStyle();
		if (!vFile.toString().endsWith(".jpg"))
			graphics.setColor(vColor);		// set color for this graphics drawing to color stored for this VerdiBoundaries object
		Logger.debug("just back from getting style");
		Logger.debug("in VerdiBoundaries.draw; aVerdiStyle.getStyle() = " + theStyle.toString());
		Logger.debug("in VerdiBoundaries.draw, CRS for tile plot (gridCRS) = " + gridCRS);
		Logger.debug("   and name of file = " + vFileName);
		/*try {
			vStore = FileDataStoreFinder.getDataStore(vFile);
			vFeatureSource = vStore.getFeatureSource();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}*/
		Logger.debug("vFile = " + vFile);
		Logger.debug("vPath = " + vPath);
		Logger.debug("vMap = " + vMap);
		Logger.debug("map Coordinate Reference System (vCRS) = " + aVerdiStyle.getCoordinateReferenceSystem());
		Logger.debug("Feature Source = " + aVerdiStyle.getFeatureSource());
		//Logger.debug("vStore = " + vStore.toString());
		//Logger.debug("vFeatureSource = " + vFeatureSource.toString());
		Logger.debug("Layer = " + aVerdiStyle.getLayers().get(0));
		Logger.debug("ShapeFile = " + aVerdiStyle.getShapeFile());
		Logger.debug("Shapefile Path = " + aVerdiStyle.getShapePath());
		Logger.debug("Style = " + aVerdiStyle.getStyle());
		Logger.debug("now set the CRS to gridCRS");	// NOTE: do NOT use Projector method (old UCAR operates point-by-point
		Logger.debug("set CRS of Viewport to gridCRS: " + gridCRS);
		ReferencedEnvelope displayBounds = new ReferencedEnvelope(gridBounds[0][0], gridBounds[0][1], gridBounds[1][0], gridBounds[1][1], gridCRS);
		vMap.getViewport().setBounds(displayBounds);
		
		for (Layer layer : aVerdiStyle.getLayers()) {
			layer.setVisible(true);
		}

		vMap.addLayers(aVerdiStyle.getLayers());	// FIGURE THIS ONE OUT BECAUSE TRANSFORM IS HERE
		

		/*if (frame == null) {
			//frame = new JMapFrame(vMap);
			try {
				//ImageLab.main(null);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		/*
        final GridCoverageRenderer gcr =
                new GridCoverageRenderer(
                        destinationCRS,
                        originalMapExtent,
                        screenSize,
                        worldToScreen,
                        java2dHints);
                        */

		StreamingRenderer renderer = new StreamingRenderer();
		renderer.setMapContent(vMap);
		Rectangle outputArea = new Rectangle(xOffset, yOffset, width, height);
		
		renderer.paint((Graphics2D)graphics, outputArea, vMap.getViewport().getBounds());
		graphics.setClip(null);
	}	// end of draw function
		
	//JMapFrame frame = null;
		
	public MapContent getMap()	// send vMap back to calling program
	{
		return vMap;
	}

	public boolean clipShapefileToGrid()	// clip extent of shapefile to match spatial grid (raster)
	{	// TODO
		return true;
	}
	
	public boolean clipGridToShapefile()	// clip extent of the spatial grid (raster) to match shapefile
	{	// TODO
		return true;
	}

	public String getFileName() {
		return vFileName;
	}
	
	public File getFile() {
		return vFile;
	}
	
	public void setPath(String aPath)
	{
		vPath = aPath;
	}
	
	public String getPath()
	{
		return vPath;
	}
	
	public VerdiStyle getVerdiStyle()
	{
		return aVerdiStyle;
	}
	
	public CoordinateReferenceSystem getCRS()
	{
		if(aVerdiStyle == null)
			return null;
		return aVerdiStyle.getCoordinateReferenceSystem();	// get the Coordinate Reference System for this File
	}
	
	public Color getColor()
	{
		return vColor;
	}
	
	public void setColor(Color aColor)
	{
		vColor = aColor;
	}
	
	public void finalize() throws Throwable {
		super.finalize();
	}
}
