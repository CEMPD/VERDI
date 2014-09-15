package anl.verdi.commandline.task;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import anl.verdi.commandline.AbstractTask;
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiConstants;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.config.VectorPlotConfiguration;
import anl.verdi.plot.config.VertCrossPlotConfiguration;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.VectorPlotCreator;

public class VectorPlotTask implements AbstractTask {
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
	private VectorPlotConfiguration vectorConfig = new VectorPlotConfiguration();
	private ColorMap cmap = null;
	private File[] datafiles;

	public VectorPlotTask(Map<String, String> map, File[] dataFiles, VerdiApplication vApp) {
		this.map = map;
		this.verdiApp = vApp;
		this.datafiles = dataFiles;
	}

	@Override
	public void run() {
		if (datafiles == null || datafiles.length == 0) {
			System.out.print("No data files found.");
			return;
		}

		createConfig();
		try {
			verdiApp.loadDataset(datafiles);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		createFormula();
		Plot plot = new VectorPlotCreator(verdiApp, vectorConfig).createPlotInBackground();;

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

	private TilePlotConfiguration createConfig() {
		config = new PlotConfiguration();
		vConfig = new VertCrossPlotConfiguration();
		vectorConfig = new VectorPlotConfiguration();

		String configFileStr = map.get(VerdiConstants.CONFIG_FILE);
		File configFile = (configFileStr == null || configFileStr.trim().isEmpty()) ? null : new File(configFileStr);

		if (configFile != null && configFile.isFile() && configFile.exists()) {
			try {
				config = new PlotConfiguration(configFile);
				vConfig = new VertCrossPlotConfiguration(new PlotConfiguration(
						configFile));
				vectorConfig = new VectorPlotConfiguration(
						new PlotConfiguration(configFile));
			} catch (Exception e) {
				//
			}
		}

		if (cmap != null) {
			config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
			vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
			vectorConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
		}

		String title = map.get(VerdiConstants.TITLE) == null ? "" : map.get(VerdiConstants.TITLE);
		String subtitle1 = map.get(VerdiConstants.SUB_TITLE_ONE) == null ? ""
				: map.get(VerdiConstants.SUB_TITLE_ONE);
		String subtitle2 = map.get(VerdiConstants.SUB_TITLE_TWO) == null ? ""
				: map.get(VerdiConstants.SUB_TITLE_TWO);
		boolean showLegendTicks = true;
		boolean showDomainTicks = true;
		boolean showRangeTicks = true;
		String gridlines = map.get(VerdiConstants.GRID_LINES);
		boolean showGridLines = (gridlines != null
				&& gridlines.trim().equalsIgnoreCase("YES") ? true : false);
		String unitStr = map.get(VerdiConstants.UNIT_STRING);
		if (unitStr==null || unitStr.trim().equals(""))
			unitStr = "none";

		config.setTitle(title);
		vConfig.setTitle(title);
		vectorConfig.setTitle(title);

		config.setSubtitle1(subtitle1);
		vConfig.setSubtitle1(subtitle1);
		vectorConfig.setSubtitle1(subtitle1);

		config.setSubtitle2(subtitle2);
		vConfig.setSubtitle2(subtitle2);
		vectorConfig.setSubtitle2(subtitle2);

		config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
		vConfig.putObject(VertCrossPlotConfiguration.UNITS_SHOW_TICK,
				showLegendTicks);
		vectorConfig.putObject(VectorPlotConfiguration.UNITS_SHOW_TICK,
				showLegendTicks);

		config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
		vConfig.putObject(VertCrossPlotConfiguration.DOMAIN_SHOW_TICK,
				showDomainTicks);
		vectorConfig.putObject(VectorPlotConfiguration.DOMAIN_SHOW_TICK,
				showDomainTicks);

		config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
		vConfig.putObject(VertCrossPlotConfiguration.RANGE_SHOW_TICK,
				showLegendTicks);
		vectorConfig.putObject(VectorPlotConfiguration.RANGE_SHOW_TICK,
				showRangeTicks);

		config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
		vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
		vectorConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES,
				showGridLines);
		
		if (unitStr != null && !unitStr.trim().isEmpty()) {
			config.setUnits(unitStr.trim());
			vConfig.setUnits(unitStr.trim());
			vectorConfig.setUnits(unitStr.trim());
		}

		TilePlotConfiguration tconfig = new TilePlotConfiguration(config);
		tconfig.setGridLines(showGridLines, Color.gray);

		return tconfig;
	}

	private void createFormula() {
		try {
			String varStr = map.get(VerdiConstants.VECTOR);
			varStr = (varStr == null || varStr.trim().isEmpty()) ? map.get(VerdiConstants.VECTOR_TILE) : varStr;
			
			if (varStr == null || varStr.trim().isEmpty())
				return;
			
			String[] vars = varStr.trim().split(",");
			String uWind = null;
			String vWind = null;
			String formula = null;

			if (vars.length < 2)
				vars = varStr.trim().split("\"");

			if (vars.length < 2)
				return;

			List<String> list = new ArrayList<String>();
			@SuppressWarnings("unused")
			boolean found = false;

			for (String var : vars)
				found = (var != null && !var.trim().isEmpty()) ? list.add(var.trim()) : false;
					
			uWind = list.get(0);
			vWind = list.get(1);
			
			if (list.size() == 3) {
				formula = list.get(0);
				uWind = list.get(1);
				vWind = list.get(2);
			} 

			FormulaListElement u = verdiApp.create(uWind);
			verdiApp.getProject().getFormulas().addFormula(u);

			FormulaListElement v = verdiApp.create(vWind);
			verdiApp.getProject().getFormulas().addFormula(v);
			
			if (formula == null) {
				vectorConfig.setVectorsComponents(u, v);
				return;
			}
			
			FormulaListElement e = verdiApp.create(formula);
			verdiApp.getProject().getFormulas().addFormula(e);
			vectorConfig.setVectorsComponents(u, v, e);
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	private void save(Plot plot) throws IOException {
		String ext = map.get(VerdiConstants.IMAGE_TYPE);
		String imgFile = map.get(VerdiConstants.IMAGE_FILE);
		imgFile = (ext != null && !imgFile.endsWith(ext)) ? imgFile + "." + ext
				: imgFile;

		File file = new File(imgFile);

		if (ext == null) {
			file = new File(file.getAbsolutePath() + "." + JPEG);
			ext = JPEG;
		}

		int width = 400;
		int height = 400;
		
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
