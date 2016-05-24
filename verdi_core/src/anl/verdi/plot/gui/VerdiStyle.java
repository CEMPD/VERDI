/**
 * VerdiStyle.java
 * Get a Style for a map file in shapefile format
 * Already have a shapefile. Pass its File object as an argument into the VerdiStyle constructor.
 * Parts of this class definition are adapted from the StyleLab tutorial of GeoTools v12.
 * NOTE: No member function is provided for writing out an SLD file.
 */
package anl.verdi.plot.gui;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;		// using log4j instead of System.out.println for messages
import org.apache.logging.log4j.Logger;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.map.FeatureLayer;
import org.geotools.map.Layer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Fill;
import org.geotools.styling.Graphic;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.Mark;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLDParser;
import org.geotools.styling.Stroke;
import org.geotools.styling.Style;
import org.geotools.styling.StyleFactory;
import org.geotools.swing.styling.JSimpleStyleDialog;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.FilterFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
/**
 * @author Jo Ellen Brandmeyer, Institute for the Environment, 2015
 *
 */
public class VerdiStyle {

	private StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
	private FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
	static final Logger Logger = LogManager.getLogger(VerdiStyle.class.getName());
	private Style vStyle = null;		// Style associated with this Shapefile
	private FeatureSource vFeatureSource = null;
	private File vFile = null;			// File associated with this Shapefile
	private String shpPath = null;		// path (as a String) to this Shapefile
	private Layer vLayer = null;
	private CoordinateReferenceSystem vCRS = null;	// CRS for this Shapefile
	private FileDataStore vStore = null;	// this is used despite what Eclipse says!

	public VerdiStyle(File shpFile)		// File must previously exist and be for a .shp file
	{									// constructor called from VerdiBoundaries.java
		Logger.debug("in constructor of VerdiStyle with passed File");	// JEB YES
		if(shpFile == null)
		{
			Logger.error("No shapefile defined for map layer");
			return;
		}
		Logger.debug("have absolute path for shpFile = " + shpFile.getAbsolutePath());	// JEB YES
		if(vFile != null)
			reset();			// already had file set so must reset everything
		vFile = shpFile;		// store shapefile into class data member
		Logger.debug("stored shpFile as vFile = " + vFile.toString());	// JEB YES
		shpPath = vFile.getAbsolutePath();
		Logger.debug("shpPath set to: " + shpPath);	// JEB YES
		calcVerdiStyle();			// JEB YES
	}			// JEB YES returning to calling pgm (VerdiBoundaries)
	
	public VerdiStyle()		// default constructor
	{
		Logger.debug("in default constructor for VerdiStyle");
		reset();		// no shapefile provided so create object with null data members
	}
	
	public boolean setShapefile(File shpFile)	// assigns value to vFile and shpPath based on the file parameter
	{	// returns true == set shapefile and path, false == reset data members only
		if(shpFile == null)
		{
			reset();
			return false;
		}
		if(shpFile == vFile)
			return true;
		vFile = shpFile;
		shpPath = vFile.getAbsolutePath();
		calcVerdiStyle();
		return true;
	}
	
	private void calcVerdiStyle()		// get Style and CRS for a shapefile
	{
		Logger.debug("starting calcVerdiStyle");		// JEB YES
		findFeatureSource();	// gets the FeatureSource for the shapefile
		Logger.debug("vFeatureSource = " + vFeatureSource.toString());		// JEB YES (back from findFeatureSource()
		if (vFeatureSource == null)
		{
			Logger.debug("vFeatureSource is null");
			return;
		}
		createVerdiStyle();			// JEB YES
		Logger.debug("vStyle = " + vStyle.toString());		// JEB YES StyleImpl[ name=Default Styler]
		vCRS = vFeatureSource.getSchema().getCoordinateReferenceSystem();
		Logger.debug("vCRS = " + vCRS.toString());			// JEB YES GEOGCS["SPHERE",
	}

	private void reset()	// reset member variables to null
	{
		vFile = null;
		vFeatureSource = null;
		shpPath = null;
		vStyle = null;
		vCRS = null;
		vLayer = null;
		vStore = null;
	}

	private void findFeatureSource()	// find the FeatureSource for the shapefile
	{
		vFeatureSource = null;
		try{
			vStore = FileDataStoreFinder.getDataStore(vFile);
			Logger.debug("got FileDataStore = " + vStore.toString());		// JEB YES
			vFeatureSource = vStore.getFeatureSource();
			Logger.debug("got vFeatureSource = " + vFeatureSource.toString());	// JEB YES
		} catch (IOException ioEx) {
			Logger.error("Data store or feature source for file " + vFile + " could not be found.");
			Logger.error(ioEx.getMessage());
		}
	}

	private void createVerdiStyle()	// create the style from the SLD file, type of geometry, or dialog box
	{
		Logger.debug("in createStyle; ready to create Style vStyle as null");		// JEB YES
		vStyle = null;
		File sld = toSLDFile();			// JEB YES, there & returned with a null
		if(sld != null)
		{
			Logger.debug("sld != null: " + sld.getAbsolutePath());
			createFromSLD(sld);
			Logger.debug("back from createFromSLD");
			if(vStyle != null)
				return;
		}
		// sld == null || vStyle == null: style could not be generated from .SLD file
		Logger.debug("sld could not be generated from .SLD file, trying from pgm");		// JEB YES
		getStyleFromPgm();
		if(vStyle != null)
		{
			Logger.debug("got vStyle, returning: " + vStyle.toString());		// JEB YES StyleImp[ name=Default Styler]
			return;
		}
		// sld == null: style could not be programmatically generated
		// last resort: create style via dialog box for user
		Logger.debug("still no sld, trying getStyleFromDialog for vFeatureSource = " + vFeatureSource.toString());
		getStyleFromDialog();
		Logger.debug("returning vStyle = " + vStyle.toString());
		return;
	}

	private void getStyleFromDialog()
	{		// display dialog box for user to interactively specify style attributes (not available in script mode)
		SimpleFeatureType schema = (SimpleFeatureType)vFeatureSource.getSchema();
		Logger.debug("in getStyleFromDialog; SimpleFeatureType schema = " + schema.toString());
		vStyle = JSimpleStyleDialog.showDialog(null, schema);
	}

	private void getStyleFromPgm()
	{		// get style based on type of geometry in the File
		SimpleFeatureType schema = (SimpleFeatureType)vFeatureSource.getSchema();
		Logger.debug("in getStyleFromPgm; SimpleFeatureType schema = " + schema.toString());		// JEB YES knows map_world and MultiLineString
		Class geomType = schema.getGeometryDescriptor().getType().getBinding();
		Logger.debug("geomType = " + geomType.toString());		// JEB YES class com.vividsolutions.jts.geom.MultiLineString

		if(Polygon.class.isAssignableFrom(geomType) || MultiPolygon.class.isAssignableFrom(geomType))
		{
			Logger.debug("returning PolygonStyle");
			createPolygonStyle();
			return;
		}
		else if (LineString.class.isAssignableFrom(geomType) || MultiLineString.class.isAssignableFrom(geomType))
		{
			Logger.debug("returning LineStyle");		// JEB YES for map_world
			createLineStyle();			// JEB YES
			return;
		}
		else
		{
			Logger.debug("returning PointStyle");
			createPointStyle();
			return;
		}
	}

	private File toSLDFile()		// get existing SLD file
	{
		Logger.debug("looking for existing .sld/.SLD file");			// JEB YES from createStyle
		String base = shpPath.substring(0, shpPath.length()-4);
		String sldPath = base + ".sld";		// look for file with lower-case extension
		File sldFile = new File(sldPath);
		if(sldFile.exists())
			return sldFile;

		sldPath = base + ".SLD";			// look for file with upper-case extension
		sldFile = new File(sldPath);
		if(sldFile.exists())
			return sldFile;
		Logger.debug("Did not find existing .sld/.SLD file; returning null");		// JEB YES
		return null;						// did not find the .sld file
	}

	private void createFromSLD(File sld)		// create a Style from the definition in an SLD file
	{
		Logger.debug("Trying to create a style from the SLD file " + sld.toString());
		try{
			SLDParser styleReader = new SLDParser(styleFactory, sld.toURI().toURL());
			Style[] aStyle = styleReader.readXML();
			Logger.debug("setting vStyle = " + aStyle[0].toString());
			vStyle = aStyle[0];
		} catch(Exception e) {
			Logger.error("Unable to create a Style from the SLD file associated with :" + vFile);
		}
	}
	
	private void createPolygonStyle()	// create a style to draw polygons
	{
		// create an opaque outline stroke
		Stroke stroke = styleFactory.createStroke(
				filterFactory.literal(Color.black), 
				filterFactory.literal(1), 
				filterFactory.literal(1));
		// create a transparent fill
		Fill fill = styleFactory.createFill(
				filterFactory.literal(Color.LIGHT_GRAY), 
				filterFactory.literal(0));
		PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, fill, null); // null means default geometry
		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(sym);
		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		Logger.debug("created polygon style: " + style.toString());
		vStyle = style;
	}
	
	private void createLineStyle()		// create a style to draw lines
	{
		Stroke stroke = styleFactory.createStroke(
				filterFactory.literal(Color.BLACK), 
				filterFactory.literal(1));
		LineSymbolizer sym = styleFactory.createLineSymbolizer(stroke, null);	// null means default geometry
		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(sym);
		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		Logger.debug("created line style: " + style.toString());		// JEB YES StyleImp[ name=Default Styler]
		vStyle = style;
	}
	
	private void createPointStyle()	// create a style to draw points as circles
	{
		Graphic gr = styleFactory.createDefaultGraphic();
		Mark mark = styleFactory.getCircleMark();
		mark.setStroke(styleFactory.createStroke( 
				filterFactory.literal(Color.BLACK), 
				filterFactory.literal(1)));
		mark.setFill(styleFactory.createFill(filterFactory.literal(Color.TRANSLUCENT)));
		gr.graphicalSymbols().clear();gr.graphicalSymbols().add(mark);
		gr.setSize(filterFactory.literal(5));
		
		PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);	// null means default geometry
		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(sym);
		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		Logger.debug("created point style: " + style.toString());
		vStyle = style;
	}
	
	public File getShapeFile()	// get shapefile (vFile)
	{
		return vFile;
	}
	
	public String getShapePath()	// get string representing path for shapefile (shpPath)
	{
		return shpPath;
	}
	
	public FeatureSource getFeatureSource()	// get FeatureSource associated with this map layer
	{
		return vFeatureSource;
	}
	
	public Style getStyle()			// get Style
	{
		Logger.debug("in VerdiStyle.getStyle()");
		return vStyle;
	}
	
	public void setStyle(Style aStyle)
	{
		vStyle = aStyle;			// set the style from another part of the program
	}
	
	public Layer getLayer()			// get Layer based on FeatureSource and Style
	{
		if(vLayer == null)
		{
			vLayer = new FeatureLayer(vFeatureSource, vStyle);
		}
		return vLayer;
	}
	
	public CoordinateReferenceSystem getCoordinateReferenceSystem()	// get CoordinateReferenceSystem
	{
		return vCRS;
	}
}
