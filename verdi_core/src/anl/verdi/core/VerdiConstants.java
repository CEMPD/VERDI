package anl.verdi.core;

/**
 * Constants used by the VERDI application
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public interface VerdiConstants {

	// perspectives, group, etc., ids need to match
	// those in the plugin.xml file

	String MAIN_GROUP_ID = "anl.verdi.core.main_group";
	String PERSPECTIVE_ID = "anl.verdi.core.perspective_one";

	String DATASET_VIEW = "anl.verdi.core.dataset_view";
	String FORMULA_VIEW = "anl.verdi.core.formula_view";
	String AREA_VIEW = "anl.verdi.core.area_view";
	String FORMULA_DATASET_GROUP = "anl.verdi.core.formula_dataset_group";
	String FORMULA_BAR_GROUP = "anl.verdi.core.bar.formula_group";
	// id for the formula label in the tool bar
	String FORMULA_LABEL = "anl.verdi.core.bar.formula_label";

	String TILE_BUTTON_ID = "anl.verdi.plot.gui.action.TilePlot";
	String FAST_TILE_BUTTON_ID = "anl.verdi.plot.gui.action.FastTilePlot";
//	String GT_TILE_BUTTON_ID = "anl.verdi.plot.gui.action.GTTilePlotAction";
	String VERT_CROSS_BUTTON_ID = "anl.verdi.plot.gui.action.VerticalCrossSection";
	String TIME_SERIES_LINE_BUTTON_ID = "anl.verdi.plot.gui.action.TimeSeriesPlot";
	String TIME_SERIES_BAR_BUTTON_ID = "anl.verdi.plot.gui.action.TimeSeriesBarPlot";
	String SCATTER_BUTTON_ID = "anl.verdi.plot.gui.action.ScatterPlot";
//	String VECTOR_BUTTON_ID = "anl.verdi.plot.gui.action.VectorPlot";	// 2014 removed old Vector Plot
	String CONTOUR_BUTTON_ID = "anl.verdi.plot.gui.action.Contour3DPlot";
	String AREAL_INTERPOLATION_BUTTON_ID = "anl.verdi.plot.gui.action.ArealInterpolation";

	String SAVE_ID = "anl.verdi.core.action.SaveAction";
	String SAVE_AS_ID = "anl.verdi.core.action.SaveAsAction";

	String VERDI_LIST_OBJECT = "VerdiListObject";					// 2014 had been VeridListObject
	String VERDI_LIST_OBJECT_NUMBER = "VerdiListObjectNumber";		// 2014 had been VeridListObjectNumber

	// added for batch scripting language
	String GLOBAL = "<Global>".toUpperCase();
	String END_GLOBAL = "</Global>".toUpperCase();
	String TASK = "<Task>".toUpperCase();
	String END_TASK = "</Task>".toUpperCase();
	String SEPARATOR = "//verdi_separator//".toUpperCase();
	String TILE_PLOT = "tile".toUpperCase();
	String FAST_TILE_PLOT = "fasttile".toUpperCase();
	String LINE_PLOT = "line".toUpperCase();
	String BAR_PLOT = "bar".toUpperCase();
	String VECTOR = "vector".toUpperCase();
	String VECTOR_TILE = "vectorTile".toUpperCase();
	String CONTOUR_PLOT = "contour".toUpperCase();
	String GLOBAL_ATTRIBUTE = "global".toUpperCase();
	String GRAPHICS = "gtype".toUpperCase();
	String GRAPHICS_SHORT = "g".toUpperCase();
	String PATTERN = "pattern".toUpperCase();
	String SUBDOMAIN = "subdomain".toUpperCase();
	String DATA_DIR = "dir".toUpperCase();
	String DATA_FILE = "f".toUpperCase();
	String FORMULA = "s".toUpperCase();
	String IMAGE_TYPE = "saveImage".toUpperCase();
	String IMAGE_FILE = "imageFile".toUpperCase();
	String IMAGE_DIR = "imageDir".toUpperCase();
	String IMAGE_WIDTH = "imageWidth".toUpperCase();
	String IMAGE_HEIGHT = "imageHeight".toUpperCase();
	String TITLE = "titleString".toUpperCase();
	String SUB_TITLE_ONE = "subTitle1".toUpperCase();
	String SUB_TITLE_TWO = "subTitle2".toUpperCase();
	String CONFIG_FILE = "configFile".toUpperCase();
	String LEGEND_BINS = "legendBins".toUpperCase();
	String GRID_LINES = "drawGridLines".toUpperCase();
	String UNIT_STRING = "unitString".toUpperCase();
	String TIME_STEP = "ts".toUpperCase();
	String LAYER = "layer".toUpperCase();

	public static final float NC_FILL_FLOAT = 9.9692099683868690e+36f;		// 2014 changed from -9.999E36f to assist floating point comparison
	public static final float BADVAL3 = -9.998E36f;		// 2014 changed from -9.999E36f to assist floating point comparison
	public static final float AMISS3  = -8.999E36f;		// 2014 changed from -9.000E36f to assist floating point comparison
	
	public static final int NETCDF_CONV_ARW_WRF = 10;

}
