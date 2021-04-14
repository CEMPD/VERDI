package anl.verdi.commandline.task;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;		// 2015
import org.apache.logging.log4j.Logger;			// 2015 replacing System.out.println with logger messages

// import ucar.util.Logger;						// 2015 ucar logger replaced by apache logger
import anl.verdi.commandline.AbstractTask;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiConstants;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.formula.Formula;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.Palette;
import anl.verdi.plot.color.PavePaletteCreator;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
//import anl.verdi.plot.config.VectorPlotConfiguration;		// 2014 removed old Vector Plot
import anl.verdi.plot.config.VertCrossPlotConfiguration;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotFactory;
import anl.verdi.plot.gui.PlotPanel;

public class TimeSeriesPlotTask implements AbstractTask {
	static final Logger Logger = LogManager.getLogger(TimeSeriesPlotTask.class.getName());
	private Map<String, String> map;
	private VerdiApplication verdiApp;
	public final static String JPEG = "jpeg";
	public final static String JPG = "jpg";
	public final static String TIFF = "tiff";
	public final static String TIF = "tif";
	public final static String PNG = "png";
	public final static String BMP = "bmp";
	public final static String EPS = "eps";
	private PlotConfiguration config = new PlotConfiguration();
	private VertCrossPlotConfiguration vConfig = new VertCrossPlotConfiguration();
//	private VectorPlotConfiguration vectorConfig = new VectorPlotConfiguration();
	private Formula.Type type;
	private File[] datafiles;

	public TimeSeriesPlotTask(Map<String, String> map, File[] dataFiles, VerdiApplication vApp, Formula.Type type) {
		this.map = map;
		this.verdiApp = vApp;
		this.type = type;
		this.datafiles = dataFiles;
		Logger.debug("in TimeSeriesPlotTask constructor");
	}

	@Override
	public void run() {
		Logger.debug("in TimeSeriesPlotTask.run()");
		createConfig();
		Logger.debug("in TimeSeriesPlotTask.run(), back from createConfig()");
		try {
			verdiApp.loadDataset(datafiles);
			Logger.debug("in TimeSeriesPlotTask.run(), back from loadDataset(datafiles)");

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		createFormula();
		Logger.debug("in TimeSeriesPlotTask.run(), back from createFormula()");

		Plot plot = null;

		if (verdiApp.getProject().getSelectedFormula() != null) {
			final DataFrame dataFrame = verdiApp.evaluateFormula(type);
			Logger.debug("in TimeSeriesPlotTask.run(), back from evaluateFormula(type)");

			if (dataFrame != null) {
				PlotFactory factory = new PlotFactory();
				Logger.debug("in TimeSeriesPlotTask.run(),back from new PlotFactory()");

				PlotPanel panel = factory.getPlot(type, verdiApp.getProject().getSelectedFormula().getFormula(),
						dataFrame, config);
				Logger.debug("in TimeSeriesPlotTask.run(), back from factory.getPlot(...)");

				plot = panel.getPlot();
				Logger.debug("in TimeSeriesPlotTask.run(), back from panel.getPlot()");
				
				String aLayer = map.get(VerdiConstants.LAYER);			
				if (aLayer != null)
					PlotFactory.setLayer(dataFrame,  plot,  aLayer);

			}
		}

		try {
			if (plot != null){
				Logger.debug("in TimeSeriesPlotTask.run(), plot is not null");
				save(plot);
				Logger.debug("in TimeSeriesPlotTask.run(), back from save(plot)");
				}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			verdiApp.getProject().getFormulas().clear();
			verdiApp.getProject().getDatasets().clear();
			Logger.debug("in TimeSeriesPlotTask.run(), in finally cleared formulas");
		}
	}
	
	private void handleColorMap(Map<String, String> map) {
		String legendBins = map.get(VerdiConstants.LEGEND_BINS);
		ColorMap cmap = null;

		String[] allItems = null;
		if (legendBins != null)
			allItems = legendBins.split(",");

		int numColors = allItems.length;
		PavePaletteCreator p = new PavePaletteCreator();

		if(allItems == null || allItems[0].equalsIgnoreCase("DEFAULT"))
		{
			cmap = null;

			//resetConfigurationsWithoutColorMap();
		}
		else
		{

			List<Palette> paletteList = p.createPalettes(numColors - 1);

			cmap = new ColorMap(paletteList.get(0), 
					Double.parseDouble(allItems[0]), 
					Double.parseDouble(allItems[allItems.length - 1]));

			for(int i = 0; i < numColors - 1; i++)
			{
				try {
					cmap.setIntervalStart(i, Double.parseDouble(allItems[i]));
				} catch (NumberFormatException e) {
					Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'LEGENDBINS'", e);
					e.printStackTrace();
				} catch (Exception e) {
					Logger.error("Exception in ScriptHandler.dataMap.put 'LEGENDBINS'", e);
					e.printStackTrace();
				}
			}
		}
		


		if(cmap!= null)
		{
			config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
			vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
		}
	}

	private void createConfig() {
		config = new PlotConfiguration();
		vConfig = new VertCrossPlotConfiguration();
//		vectorConfig = new VectorPlotConfiguration();
		
		String configFileStr = map.get(VerdiConstants.CONFIG_FILE);
		Logger.debug("in TimeSeriesPlotTask.createConfig(), configFileStr = " + configFileStr);
		File configFile = (configFileStr == null || configFileStr.trim().isEmpty()) ? null : new File(configFileStr);
		
		if (configFile != null && configFile.isFile() && configFile.exists()) {
			try{
				config = new PlotConfiguration(configFile);
				vConfig = new VertCrossPlotConfiguration(new PlotConfiguration(configFile));
//				vectorConfig = new VectorPlotConfiguration(new PlotConfiguration(configFile));
			} catch (Exception e) {
				//
			} 
		}
		
		String title = map.get(VerdiConstants.TITLE) == null ? "" : map.get(VerdiConstants.TITLE);
		String subtitle1 = map.get(VerdiConstants.SUB_TITLE_ONE) == null ? "" : map.get(VerdiConstants.SUB_TITLE_ONE);
		String subtitle2 = map.get(VerdiConstants.SUB_TITLE_TWO) == null ? "" : map.get(VerdiConstants.SUB_TITLE_TWO);
		boolean showLegendTicks = true;
		boolean showDomainTicks = true;
		boolean showRangeTicks = true;
		String gridlines = map.get(VerdiConstants.GRID_LINES);
		boolean showGridLines = (gridlines != null && gridlines.trim().equalsIgnoreCase("YES") ? true : false);
		String unitStr = map.get(VerdiConstants.UNIT_STRING);
		if (unitStr==null || unitStr.trim().equals(""))
			unitStr = "none";
		
		config.setTitle(title);
		vConfig.setTitle(title);
//		vectorConfig.setTitle(title);
		
		config.setSubtitle1(subtitle1);
		vConfig.setSubtitle1(subtitle1);
//		vectorConfig.setSubtitle1(subtitle1);
		
		config.setSubtitle2(subtitle2);
		vConfig.setSubtitle2(subtitle2);
//		vectorConfig.setSubtitle2(subtitle2);
		
		config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
		vConfig.putObject(VertCrossPlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
//		vectorConfig.putObject(VectorPlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
	
		config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
		vConfig.putObject(VertCrossPlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
//		vectorConfig.putObject(VectorPlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
		
		config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
		vConfig.putObject(VertCrossPlotConfiguration.RANGE_SHOW_TICK, showLegendTicks);
//		vectorConfig.putObject(VectorPlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
		
		config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
		vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
//		vectorConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
		
		if (unitStr != null && !unitStr.trim().isEmpty()) {
			config.setUnits(unitStr.trim());
			vConfig.setUnits(unitStr.trim());
//			vectorConfig.setUnits(unitStr.trim());
		}
	}

	private void createFormula() {
		try {
			String formula = map.get(VerdiConstants.FORMULA).trim();
			FormulaListElement e = verdiApp.create(formula);
			verdiApp.getProject().getFormulas().addFormula(e);
			verdiApp.getProject().setSelectedFormula(e);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void save(Plot plot) throws IOException {
		String ext = map.get(VerdiConstants.IMAGE_TYPE);
		String imgFile = map.get(VerdiConstants.IMAGE_FILE);
		imgFile = (ext != null && !imgFile.endsWith(ext)) ? imgFile + "." + ext : imgFile;
		File file = new File(imgFile);

		if (ext == null) {
			file = new File(file.getAbsolutePath() + "." + JPEG);
			ext = JPEG;
		}

		int width = 800;
		int height = 600;
		
		try {
			width = Integer.parseInt(map.get(VerdiConstants.IMAGE_WIDTH));
			height = Integer.parseInt(map.get(VerdiConstants.IMAGE_HEIGHT));
		} catch (Exception e) {
			//NOTE: no-op
		}

		BufferedImage image = plot.getBufferedImage(width, height);
		ImageIO.write(image, ext, file);
	}

}
