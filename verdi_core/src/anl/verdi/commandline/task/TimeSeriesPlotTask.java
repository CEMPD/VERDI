package anl.verdi.commandline.task;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;

import anl.verdi.commandline.AbstractTask;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiConstants;
import anl.verdi.data.DataFrame;
import anl.verdi.formula.Formula;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
//import anl.verdi.plot.config.VectorPlotConfiguration;		// 2014 removed old Vector Plot
import anl.verdi.plot.config.VertCrossPlotConfiguration;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotFactory;
import anl.verdi.plot.gui.PlotPanel;

public class TimeSeriesPlotTask implements AbstractTask {
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
	}

	@Override
	public void run() {
		createConfig();
		try {
			verdiApp.loadDataset(datafiles);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		createFormula();
		Plot plot = null;

		if (verdiApp.getProject().getSelectedFormula() != null) {
			final DataFrame dataFrame = verdiApp.evaluateFormula(type);

			if (dataFrame != null) {
				PlotFactory factory = new PlotFactory();
				PlotPanel panel = factory.getPlot(type, verdiApp.getProject().getSelectedFormula().getFormula(),
						dataFrame, config);
				plot = panel.getPlot();
			}
		}

		try {
			if (plot != null)
				save(plot);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			verdiApp.getProject().getFormulas().clear();
			verdiApp.getProject().getDatasets().clear();
		}
	}
	
	private void createConfig() {
		config = new PlotConfiguration();
		vConfig = new VertCrossPlotConfiguration();
//		vectorConfig = new VectorPlotConfiguration();
		
		String configFileStr = map.get(VerdiConstants.CONFIG_FILE);
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
