package anl.verdi.commandline.task;

import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.commandline.AbstractTask;
import anl.verdi.commandline.CommandScript;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiConstants;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.formula.Formula;
import anl.verdi.gui.DatasetListElement;
import anl.verdi.gui.DatasetListModel;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.Palette;
import anl.verdi.plot.color.PavePaletteCreator;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
//import anl.verdi.plot.config.VectorPlotConfiguration;		// 2014 removed old Vector Plot
import anl.verdi.plot.config.VertCrossPlotConfiguration;
import anl.verdi.plot.gui.FastTilePlot;
import anl.verdi.plot.gui.Plot;

public class TilePlotTask implements AbstractTask {
	static final Logger Logger = LogManager.getLogger(TilePlotTask.class.getName());
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
	private ColorMap cmap = null;
	private File[] datafiles;
	private String[] subDomainArgs;
	// 2014: 4 parameters for subdomain
	private int xmin = 0;	// == first column
	private int xmax = 0;	// == last column
	private int ymin = 0;	// == first row
	private int ymax = 0;	// == last row
	private int vectorSamplingIncr = 1;	// vector sampling increment for overlaying vectors

	FormulaListElement uWindElement = null;
	FormulaListElement vWindElement = null;
	
	public TilePlotTask(Map<String, String> map, File[] dataFiles, VerdiApplication vApp, String[] subDomainArgs) {
		this.map = map;
		this.verdiApp = vApp;
		this.datafiles = dataFiles;
		this.subDomainArgs = subDomainArgs;
	}

	public TilePlotTask(Map<String, String> map, File[] dataFiles, VerdiApplication vApp) {	// 2014 constructor for not using subDomain
		this.map = map;
		this.verdiApp = vApp;
		this.datafiles = dataFiles;
		this.subDomainArgs = null;
	}

	@Override
	public void run() {
		//TilePlotConfiguration tconfig = null;
		try {
			createConfig();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		int timeStep = 0;
		int layer = 0;

		if (datafiles == null || datafiles.length == 0) {
			Logger.error("No data files found.");
			return;
		}
		
		try {
			verdiApp.loadDataset(datafiles);
		} catch (Exception e1) {
			Logger.error("Exception in TilePlotTask when calling loadDataset", e1);
		}
		Logger.debug("TilePlotTask: subDomainArgs = " + subDomainArgs);
		if(subDomainArgs != null && subDomainArgs.length == 4) {	// make sure we have a subdomain (i.e., 4 strings here)
			try{
				//since there is no way to change the dataset programmatically, 
				//we will just use the last added one
				DatasetListModel dlm = verdiApp.getProject().getDatasets();
	
				DatasetListElement dle = 
					(DatasetListElement)dlm.getElementAt(dlm.getSize() - 1);
	
				xmin = Integer.parseInt(subDomainArgs[0]);
				xmax = Integer.parseInt(subDomainArgs[2]);
				ymin = Integer.parseInt(subDomainArgs[1]);
				ymax = Integer.parseInt(subDomainArgs[3]);
				dle.setXYUsed(false); 	// using subdomain
		//		Logger.debug("TilePlotTask using subdomain: xmin = " + xmin + ", xmax = " + xmax
		//				+ ", ymin = " + ymin + ", ymax = " + ymax);
//				dle.setDomain(xmin, xmax, ymin, ymax);	// no longer need this here - causes a subdomain of the subdomain
			}catch(Exception e){
				Logger.error("Exception in TilePlotTask when calling setDomain with a subDomain", e);
			}	
		}
		else {	// 2014 not using subdomain feature
			try{
				//since there is no way to change the dataset programmatically, 
				//we will just use the last added one
				DatasetListModel dlm = verdiApp.getProject().getDatasets();
	
				DatasetListElement dle = 
					(DatasetListElement)dlm.getElementAt(dlm.getSize() - 1);
	
				dle.setXYUsed(false); 	// not using subdomain
				Logger.debug("TilePlotTask is NOT using subdomain");
			}catch(Exception e){
				Logger.error("Exception in TilePlotTask when calling setDomain and no subDomain", e);
			}	
			
		}
		createFormula();
		FastTilePlot plot = null;

		if (verdiApp.getProject().getSelectedFormula() != null) {
			final DataFrame dataFrame = verdiApp.evaluateFormula(Formula.Type.TILE);

			if (dataFrame != null) {
	   	
				plot = new FastTilePlot(verdiApp, dataFrame);
	        	plot.configure(config, Plot.ConfigSource.FILE);
	        	// here handle subdomain
	        	if(subDomainArgs != null)	// have a subdomain defined
	        	{
	        		plot.resetRowsNColumns(ymin, ymax, xmin, xmax);
	        	}
			    
				try {
					Axes<DataFrameAxis> axes = dataFrame.getAxes();
					String aTimeStep = map.get(VerdiConstants.TIME_STEP);
					if(aTimeStep == null)
						timeStep = 0;
					else
						timeStep = Integer.parseInt(map.get(VerdiConstants.TIME_STEP)) - 1; //assume it is 1-based
					plot.updateTimeStep(timeStep - axes.getTimeAxis().getOrigin());
				} catch (NumberFormatException e) {
					Logger.error("Number Format Exception in TilePlotTask when calling updateTimeStep", e);
				}
				try {
					Axes<DataFrameAxis> axes = dataFrame.getAxes();
					String aLayer = map.get(VerdiConstants.LAYER);
					if(aLayer == null)
						layer = 0;
					else
						layer = Integer.parseInt(map.get(VerdiConstants.LAYER)) - 1; //assume it is 1-based
					plot.updateLayer(layer - axes.getZAxis().getOrigin());
				} catch (NumberFormatException e) {
					Logger.error("Number Format Exception in TilePlotTask when calling updateTimeStep", e);
				}
				if (uWindElement != null) {
					verdiApp.addVectorOverlay(uWindElement, vWindElement,  vectorSamplingIncr, plot, null);
				}
			}
	    }

		try {
			if (plot != null) {
				save(plot);
				plot.stopThread();
			}
		} catch (Exception e) {
			Logger.error("Error in TilePlotTask with saving plot", e);
			e.printStackTrace();
		} finally {
			verdiApp.getProject().getFormulas().clear();
			verdiApp.getProject().getDatasets().clear();
		}
	}
	
	/*private static void resetConfigurationsWithoutColorMap()
	{
		Logger.debug("ScriptHandler.resetConfigurationsWithoutColorMap");
		config = new PlotConfiguration();
		vConfig = new VertCrossPlotConfiguration();

		if(configFile != null)
		{
			try{
				config = new PlotConfiguration(new File(configFile));
				vConfig = new VertCrossPlotConfiguration(new PlotConfiguration(new File(configFile)));
			}catch(IOException e){
				Logger.error("IOException in ScriptHandler.resetConfigurationsWithoutColorMap", e);
			}
		}
		else{
			config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
			vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);

			config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
			vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);

			config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
			vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);

			config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
			vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);

			if(titleSize != -1)
			{
				config.putObject(PlotConfiguration.TITLE_FONT, new Font("SansSerif", Font.PLAIN, titleSize));
				vConfig.putObject(PlotConfiguration.TITLE_FONT, new Font("SansSerif", Font.PLAIN, titleSize));
			}
			if(subtitle1Size != -1)
			{
				config.putObject(PlotConfiguration.SUBTITLE_1_FONT, 
						new Font("SansSerif", Font.PLAIN, subtitle1Size));
				vConfig.putObject(PlotConfiguration.SUBTITLE_1_FONT, new Font("SansSerif", Font.PLAIN, subtitle1Size));
			}
			if(subtitle2Size != -1)
			{
				config.putObject(PlotConfiguration.SUBTITLE_2_FONT, 
						new Font("SansSerif", Font.PLAIN, subtitle2Size));
				vConfig.putObject(PlotConfiguration.SUBTITLE_2_FONT, new Font("SansSerif", Font.PLAIN, subtitle2Size));
			}


			config.setUnits(units);
			vConfig.setUnits(units);

			config.setTitle(title);
			vConfig.setTitle(title);

			config.setSubtitle2(subtitle2);
			vConfig.setSubtitle2(subtitle2);

			config.setSubtitle1(subtitle1);
			vConfig.setSubtitle1(subtitle1);
		}
	}*/

	
	private void handleColorMap(Map<String, String> map) {
		String legendBins = map.get(VerdiConstants.LEGEND_BINS);

		String[] allItems = null;
		if (legendBins != null)
			allItems = legendBins.split(",");

		PavePaletteCreator p = new PavePaletteCreator();

		if (allItems == null || allItems[0].equalsIgnoreCase("DEFAULT"))
		{
			cmap = null;

			//resetConfigurationsWithoutColorMap();
		}
		else
		{
			int numColors = allItems.length;

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
	
	private TilePlotConfiguration createConfig() throws IOException {
		config = new PlotConfiguration();
		vConfig = new VertCrossPlotConfiguration();
//		vectorConfig = new VectorPlotConfiguration();
		
		String configFileStr = map.get(VerdiConstants.CONFIG_FILE);
		
		File configFile = (configFileStr == null || configFileStr.trim().isEmpty()) ? null : new File(configFileStr);
		
		if (configFile != null) {
//			try{
				config = new PlotConfiguration(configFile);
				vConfig = new VertCrossPlotConfiguration(new PlotConfiguration(configFile));
//				vectorConfig = new VectorPlotConfiguration(new PlotConfiguration(configFile));
//			} catch (Exception e) {
//				Logger.error("Exception in TilePlotTask.createConfig creating plot configuration", e);
//			} 
		}
				
		handleColorMap(map);
		
		String title = map.get(VerdiConstants.TITLE) == null ? "" : map.get(VerdiConstants.TITLE);
		String subtitle1 = map.get(VerdiConstants.SUB_TITLE_ONE) == null ? "" : map.get(VerdiConstants.SUB_TITLE_ONE);
		String subtitle2 = map.get(VerdiConstants.SUB_TITLE_TWO) == null ? "" : map.get(VerdiConstants.SUB_TITLE_TWO);
		boolean showLegendTicks = true;
		boolean showDomainTicks = true;
		boolean showRangeTicks = true;
		String gridlines = map.get(VerdiConstants.GRID_LINES);
		boolean showGridLines = (gridlines != null && gridlines.trim().equalsIgnoreCase("YES") ? true : false);
		String unitStr = map.get(VerdiConstants.UNIT_STRING);
		
		config.setTitle(title);
		vConfig.setTitle(title);
//		vectorConfig.setTitle(title);
		
		config.setSubtitle1(subtitle1);
		vConfig.setSubtitle1(subtitle1);
//		vectorConfig.setSubtitle1(subtitle1);
		
		config.setSubtitle2(subtitle2);
		vConfig.setSubtitle2(subtitle2);
//		vectorConfig.setSubtitle2(subtitle2);
		
		if (config.getObject(PlotConfiguration.UNITS_SHOW_TICK) == null) {
			config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
			vConfig.putObject(VertCrossPlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
	//		vectorConfig.putObject(VectorPlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
		}
	
		if (config.getObject(PlotConfiguration.DOMAIN_SHOW_TICK) == null) {		
			config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
			vConfig.putObject(VertCrossPlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
	//		vectorConfig.putObject(VectorPlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
		}
		
		if (config.getObject(PlotConfiguration.RANGE_SHOW_TICK) == null) {		
			config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
			vConfig.putObject(VertCrossPlotConfiguration.RANGE_SHOW_TICK, showLegendTicks);
	//		vectorConfig.putObject(VectorPlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
		}
		
		if (config.getObject(TilePlotConfiguration.SHOW_GRID_LINES) == null) {		
			config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
			vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
	//		vectorConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
		}
		
		if (unitStr==null || unitStr.trim().equals(""))
			unitStr = "none";
		
		if (config.getUnits() == null && unitStr != null && !unitStr.trim().isEmpty() && !unitStr.equals("none")) {
			config.setUnits(unitStr.trim());
			vConfig.setUnits(unitStr.trim());
//			vectorConfig.setUnits(unitStr.trim());
		}
		
		TilePlotConfiguration tconfig = new TilePlotConfiguration(config);
		tconfig.setGridLines(showGridLines, Color.gray);
		
		return tconfig;
	}

	private void createFormula() {
		try {
			//check for vectorTile
			String formula = map.get(VerdiConstants.VECTOR_TILE);
			if (formula != null) {
				String[] args = formula.split(",");
				String variable = args[0];
				String uWind = args[1];
				String vWind = args[2];
				String strSamplingIncrement = "1";
				if (args.length > 3) {
					strSamplingIncrement = args[3];
					vectorSamplingIncr = Integer.parseInt(strSamplingIncrement);
				}
				
				FormulaListElement form = verdiApp.create(variable);
				verdiApp.getProject().getFormulas().addFormula(form);
				verdiApp.getProject().setSelectedFormula(form);

				uWindElement = verdiApp.create(uWind);
				verdiApp.getProject().getFormulas().addFormula(uWindElement);

				vWindElement = verdiApp.create(vWind);
				verdiApp.getProject().getFormulas().addFormula(vWindElement);
				
				
				return;
			}
			if (formula == null) {
				formula = map.get(VerdiConstants.VECTOR);
				if (formula != null) {			
					String[] args = formula.split(",");
					String uWind = args[0];
					String vWind = args[1];
					String strSamplingIncrement = "1";
					if (args.length > 2) {
						strSamplingIncrement = args[2];
						vectorSamplingIncr = Integer.parseInt(strSamplingIncrement);
					}
	
					uWindElement = verdiApp.create(uWind);
					verdiApp.getProject().getFormulas().addFormula(uWindElement);
					verdiApp.getProject().setSelectedFormula(uWindElement);
	
					vWindElement = verdiApp.create(vWind);
					verdiApp.getProject().getFormulas().addFormula(vWindElement);
					return;
				}

			}
			if (formula == null) {		
				formula = map.get(VerdiConstants.FORMULA);
				if (formula != null)
					formula = formula.trim();
			}
			if (formula == null) {
				Logger.error("TilePlotTask.createFormula() could not determine formula from config file");
				return;
			}
			formula = formula.trim();
			FormulaListElement e = verdiApp.create(formula);
			verdiApp.getProject().getFormulas().addFormula(e);
			verdiApp.getProject().setSelectedFormula(e);
		} catch (NullPointerException e) {
			Logger.error("Null Pointer Exception in TilePlotTask.createFormula", e);
			e.printStackTrace();
		}
	}

	private void save(Plot plot) throws IOException {
		int width = 1024;
		int height = 768;
		
		try {
			width = Integer.parseInt(map.get(VerdiConstants.IMAGE_WIDTH));
			height = Integer.parseInt(map.get(VerdiConstants.IMAGE_HEIGHT));
		} catch (Exception e) {
			//Logger.error("Exception in TilePlotTask.save", e);
		}
		
		String ext = map.get(VerdiConstants.IMAGE_TYPE);
		String imgFile = map.get(VerdiConstants.IMAGE_FILE);
		imgFile = (ext != null && !imgFile.endsWith(ext)) ? imgFile + "." + ext : imgFile;
		
		File file = new File(imgFile);

		if (ext == null) {
			file = new File(file.getAbsolutePath() + "." + JPEG);
			ext = JPEG;
		}

		if (plot instanceof FastTilePlot && ext.equalsIgnoreCase(EPS)) {
			String filename = file.getAbsolutePath();
			int extPos = filename.indexOf("." + ext);

			if (extPos > 0)
				filename = filename.substring(0, extPos);

			((FastTilePlot) plot).exportEPSImage(filename, width, height);

			return;
		} else if (plot instanceof FastTilePlot) {
			((FastTilePlot) plot).drawBatchImage(width, height);
		}

		BufferedImage image = plot.getBufferedImage(width, height);
		System.out.println("Writing: " + file);
		ImageIO.write(image, ext, file);
	}

	/**
	 * @return the vectorSamplingIncr
	 */
	public int getVectorSamplingIncr() {
		return vectorSamplingIncr;
	}

	/**
	 * @param vectorSamplingIncr the vectorSamplingIncr to set
	 */
	public void setVectorSamplingIncr(int vectorSamplingIncr) {
		this.vectorSamplingIncr = vectorSamplingIncr;
	}

}
