/**
 * VerdiStyle.java
 * Get a Style for a map file in shapefile format
 * Already have a shapefile. Pass its File object as an argument into the VerdiStyle constructor.
 * Parts of this class definition are adapted from the StyleLab tutorial of GeoTools v12.
 * NOTE: No member function is provided for writing out an SLD file.
 */
package anl.verdi.plot.gui;

import gov.epa.emvl.Mapper;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;		// using log4j instead of System.out.println for messages
import org.apache.logging.log4j.Logger;
import org.geotools.data.FeatureSource;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
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

import anl.gui.color.MoreColor;
import anl.verdi.area.RangeLevelFilter;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.gui.ObsAnnotation.Symbol;
import ucar.unidata.geoloc.Projection;
/**
 * @author Jo Ellen Brandmeyer, Institute for the Environment, 2015
 *
 */
public class VerdiStyle implements Callable<Boolean> {
	
	private StyleFactory styleFactory = CommonFactoryFinder.getStyleFactory(null);
	private FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
	static final Logger Logger = LogManager.getLogger(VerdiStyle.class.getName());
	private Style vStyle = null;		// Style associated with this Shapefile
	private Stroke vStroke = null;
	private FeatureSource vFeatureSource = null;
	private File vFile = null;			// File associated with this Shapefile
	private String shpPath = null;		// path (as a String) to this Shapefile
	private Layer vLayer = null;
	private Layer pannedLayer = null;
	List<Layer> layerList = null;
	private CoordinateReferenceSystem vCRS = null;	// CRS for this Shapefile
	private FileDataStore vStore = null;	// this is used despite what Eclipse says!
	private Projection vProjection = null;

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
		vStroke = null;
		vCRS = null;
		vLayer = null;
		vStore = null;
	}
	
	static ExecutorService backgroundExecutor = Executors.newFixedThreadPool(1);
	static ExecutorService foregroundExecutor = Executors.newFixedThreadPool(1);
	
	Projection sourceProjection = null;
	CoordinateReferenceSystem projectingCRS = null;
	Future<Boolean> projectionCalculated = null;
	
	private void waitForProjection() {
		if (projectionCalculated != null)
			try {
				projectionCalculated.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	
	public Boolean call() {
		projectShapefile(sourceProjection, projectingCRS);
		return Boolean.TRUE;
	}
	
	public void projectShapefile(Projection proj, CoordinateReferenceSystem targetCRS, boolean async) {
		sourceProjection = proj;
		projectingCRS = targetCRS;
		ExecutorService executor = null;
		synchronized (backgroundExecutor) {
			if (async)
				executor = backgroundExecutor;
			else
				executor = foregroundExecutor;
			if (projectionCalculated != null) {
				if (!async)
					waitForProjection();
				return;
			}
			projectionCalculated = executor.submit(this);
		}
		if (!async) {
			waitForProjection();
		}
	}
		
	private void projectShapefile(Projection proj, CoordinateReferenceSystem targetCRS) {
		if (proj != null && targetCRS != null)
			vFeatureSource = VerdiShapefileUtil.projectShapefile(vFile.getName(), (SimpleFeatureSource)vFeatureSource, proj, targetCRS);
		vProjection = proj;
		vCRS = vFeatureSource.getSchema().getCoordinateReferenceSystem();
				
		/*double xMin = targetCRS.getCoordinateSystem().getAxis(0).getMinimumValue();
		double xMax = targetCRS.getCoordinateSystem().getAxis(0).getMaximumValue();
		double panX = 0;
		double crsWidth = xMax - xMin;
		try {
			ReferencedEnvelope bounds = vFeatureSource.getBounds();
			if (bounds.getWidth() / crsWidth >= 359 / 360) { //map spans the globe
				if (bounds.getMinX() < xMin && bounds.getMaxX() > xMin)
					panX = crsWidth;
				else if (bounds.getMinX() < xMax && bounds.getMaxX() > xMax)
					panX -= crsWidth;
				if (panX != 0) {
					SimpleFeatureSource pannedMap = VerdiShapefileUtil.panShapefile((SimpleFeatureSource)vFeatureSource, panX, 0);
					//pannedLayer = new FeatureLayer(pannedMap, vStyle);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
	}

	private void findFeatureSource()	// find the FeatureSource for the shapefile
	{
		vFeatureSource = VerdiShapefileUtil.getCachedShapefile(vFile.getName(), vProjection, null);
		if (vFeatureSource != null)
			return;
		
		try{
			vStore = FileDataStoreFinder.getDataStore(vFile);
			Logger.debug("got FileDataStore = " + vStore);		// JEB YES
			vFeatureSource = vStore.getFeatureSource();
			Logger.debug("got vFeatureSource = " + vFeatureSource);	// JEB YES
		} catch (IOException ioEx) {
			Logger.error("Data store or feature source for file " + vFile + " could not be found.", ioEx);
		}
	}

	private void createVerdiStyle()	// create the style from the SLD file, type of geometry, or dialog box
	{
		Logger.debug("in createStyle; ready to create Style vStyle as null");		// JEB YES
		vStyle = null;
		vStroke = null;
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
		
	public Style buildRangeStyle(double[] ranges, Color[] colors, boolean showSelectedOnly) {		
        // create a partially opaque outline stroke
        Stroke selectedStroke = styleFactory.createStroke(
                filterFactory.literal(MoreColor.darkBlue),
                filterFactory.literal(3),
                filterFactory.literal(1));
        
        Stroke unSelectedStroke = styleFactory.createStroke(
                filterFactory.literal(MoreColor.forestGreen),
                filterFactory.literal(1),
                filterFactory.literal(1));
        
        int numRules = colors.length * 2;
        //if (showSelectedOnly)
        	//numRules *= 2;
        Rule[] rules = new Rule[numRules];
        --numRules;
        for (int i = 0; i < colors.length; ++i) {

	        // create a partial opaque fill
	        Fill selectedFill = styleFactory.createFill(
	                filterFactory.literal(colors[i]),
	                filterFactory.literal(1));
	        PolygonSymbolizer selectedSym = styleFactory.createPolygonSymbolizer(selectedStroke, selectedFill, null);
	        
	        PolygonSymbolizer unSelectedSym = styleFactory.createPolygonSymbolizer(unSelectedStroke, null, null);
	        
	        PolygonSymbolizer unSelectedForcedSym = styleFactory.createPolygonSymbolizer(unSelectedStroke, selectedFill, null);
	        
	        
	        //TODO if enforcing selection add the rule with filter to match 
	        
	        Rule rule = styleFactory.createRule();
	        rule.symbolizers().add(selectedSym);
	        rule.setFilter(new RangeLevelFilter(ranges[i], false));
	        rules[numRules--] = rule;
	        
	        if (showSelectedOnly) {
		        rule = styleFactory.createRule();
		        rule.symbolizers().add(unSelectedSym);
		        rule.setFilter(new RangeLevelFilter(ranges[i], true));
		        rules[numRules--] = rule;
	        } else {
		        rule = styleFactory.createRule();
		        rule.symbolizers().add(unSelectedForcedSym);
		        rule.setFilter(new RangeLevelFilter(ranges[i], true));
		        rules[numRules--] = rule;
	        }
        }
        ((RangeLevelFilter)rules[0].getFilter()).setInitialFilter(true);
        
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(rules);
        Style rangeStyle = styleFactory.createStyle();
        rangeStyle.featureTypeStyles().add(fts);
        return rangeStyle;
	}
	
	private void getStyleFromDialog()
	{		// display dialog box for user to interactively specify style attributes (not available in script mode)
		waitForProjection();
		SimpleFeatureType schema = (SimpleFeatureType)vFeatureSource.getSchema();
		Logger.debug("in getStyleFromDialog; SimpleFeatureType schema = " + schema.toString());
		vStyle = JSimpleStyleDialog.showDialog(null, schema);
	}

	private void getStyleFromPgm()
	{		// get style based on type of geometry in the File
		waitForProjection();
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
	
	public Style getPointStyle() {
		createPointStyle();
		return vStyle;
	}

	public Style getObsStyle(int strokeSize, int shapeSize, ColorMap map, Symbol symbol) {
		try {
			createObsStyle(strokeSize, shapeSize, map, symbol);
			return vStyle;
		} catch (Exception e) {
			Logger.error("Error creating OBS style", e);
			return null;
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
		/*
		Fill fill = styleFactory.createFill(
				filterFactory.literal(Color.LIGHT_GRAY), 
				filterFactory.literal(0));
				*/
		PolygonSymbolizer sym = styleFactory.createPolygonSymbolizer(stroke, null, null); // null means default geometry
		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(sym);
		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		Logger.debug("created polygon style: " + style.toString());
		vStyle = style;
		vStroke = stroke;
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
		vStroke = stroke;
	}
	
	private void createPointStyle()	// create a style to draw points as circles
	{
		Graphic gr = styleFactory.createDefaultGraphic();
		Mark mark = styleFactory.getCircleMark();
		mark.setStroke(styleFactory.createStroke( 				filterFactory.literal(Color.BLACK), 
				filterFactory.literal(1))); //1
		mark.setFill(styleFactory.createFill(filterFactory.literal(Color.GREEN))); //Color.TRANSLUCENT)));
		gr.graphicalSymbols().clear();gr.graphicalSymbols().add(mark);
		gr.setSize(filterFactory.literal(5)); //5
		
		PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);	// null means default geometry
		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(sym);
		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		Logger.debug("created point style: " + style.toString());
		vStyle = style;
	}
	
	private PointSymbolizer getObsSymbolizer(int index, int strokeSize, int shapeSize, ColorMap map, Symbol symbol)	// create a style to draw points as circles
	{
		Graphic gr = styleFactory.createDefaultGraphic();
		Mark mark = styleFactory.getCircleMark();
		switch (symbol) {
		case CIRCLE:
			mark = styleFactory.getCircleMark();
			break;
		case TRIANGLE:
			mark = styleFactory.getTriangleMark();
			break;
		case SQUARE:
			mark = styleFactory.getSquareMark();
			break;
		case STAR:
			mark = styleFactory.getStarMark();
			break;
		case DIAMOND:
		case SUN:
			mark = styleFactory.getDefaultMark();
			mark.setWellKnownName(filterFactory.literal("ttf://Dialog#0x25C7"));
			break;
			//, SUN
		}
		mark.setStroke(styleFactory.createStroke( 				filterFactory.literal(Color.BLACK), 
				filterFactory.literal(strokeSize))); //1
		
		mark.setFill(styleFactory.createFill(filterFactory.literal(map.getColor(map.getColorCount() - index - 1)))); //Color.TRANSLUCENT)));
		gr.graphicalSymbols().clear();
		gr.graphicalSymbols().add(mark);
		gr.setSize(filterFactory.literal(shapeSize)); //5
		
		PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);	// null means default geometry
		return sym;
	}
	
	private void createObsStyle(int strokeSize, int shapeSize, ColorMap map, Symbol symbol) throws Exception{	// create a style to draw points as circles

		int numRules = map.getColorCount();
		Rule[] rules = new Rule[numRules];
		for (int i = 0; i < numRules; ++i) {
			rules[i] = styleFactory.createRule();
			RangeLevelFilter f = new RangeLevelFilter(map.getIntervalStart(numRules - i - 1), false);
			f.setDebug(true);
	        rules[i].setFilter(f);
	        //rules[i].setFilter(new RangeLevelFilter(map.getIntervalStart(numRules - i - 1), false));
	        //rules[i].setFilter(new RangeLevelFilter(0, false));
			rules[i].symbolizers().add(getObsSymbolizer(i, strokeSize, shapeSize, map, symbol));
		}
		

        ((RangeLevelFilter)rules[0].getFilter()).setInitialFilter(true);
        
        FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(rules);
        Style style = styleFactory.createStyle();
        style.featureTypeStyles().add(fts);
        
        
		Logger.debug("created obs style: " + style.toString());
		vStyle = style;
	}
	
	private void oldCreateObsStyle(int strokeSize, int shapeSize, ColorMap map, Symbol symbol)	// create a style to draw points as circles
	{
		Graphic gr = styleFactory.createDefaultGraphic();
		Mark mark = styleFactory.getCircleMark();
		switch (symbol) {
		case CIRCLE:
			mark = styleFactory.getCircleMark();
			break;
		case TRIANGLE:
			mark = styleFactory.getTriangleMark();
			break;
		case SQUARE:
			mark = styleFactory.getSquareMark();
			break;
		case STAR:
			mark = styleFactory.getStarMark();
			break;
		case DIAMOND:
		case SUN:
			mark = styleFactory.getDefaultMark();
			mark.setWellKnownName(filterFactory.literal("ttf://Dialog#0x25C7"));
			break;
			//, SUN
		}
		mark.setStroke(styleFactory.createStroke( 				filterFactory.literal(Color.BLACK), 
				filterFactory.literal(strokeSize))); //1
		mark.setFill(styleFactory.createFill(filterFactory.literal(Color.GREEN))); //Color.TRANSLUCENT)));
		gr.graphicalSymbols().clear();gr.graphicalSymbols().add(mark);
		gr.setSize(filterFactory.literal(shapeSize)); //5
		
		PointSymbolizer sym = styleFactory.createPointSymbolizer(gr, null);	// null means default geometry
		Rule rule = styleFactory.createRule();
		rule.symbolizers().add(sym);
		
		
		FeatureTypeStyle fts = styleFactory.createFeatureTypeStyle(new Rule[]{rule});
		Style style = styleFactory.createStyle();
		style.featureTypeStyles().add(fts);
		Logger.debug("created obs style: " + style.toString());
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
		waitForProjection();
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
	
	public void setLayerLine(Color color, int width) {
		if (vStroke != null) {
			vStroke.setColor(filterFactory.literal(color));
			vStroke.setWidth(filterFactory.literal(width));
		}
	}
	
	public List<Layer> getLayers()			// get Layer based on FeatureSource and Style
	{
		if(vLayer == null)
		{
			waitForProjection();
			vLayer = new FeatureLayer(vFeatureSource, vStyle);
		}
		if (layerList == null) {
			layerList = new ArrayList<Layer>();
			layerList.add(vLayer);
			if (pannedLayer != null)
				layerList.add(pannedLayer);
		}
			
		return layerList;
	}
	
	public CoordinateReferenceSystem getCoordinateReferenceSystem()	// get CoordinateReferenceSystem
	{
		return vCRS;
	}
}
