package anl.verdi.commandline;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.core.VerdiApplication;
import anl.verdi.data.Axes;
import anl.verdi.data.DataFrame;
import anl.verdi.data.DataFrameAxis;
import anl.verdi.formula.Formula;
import anl.verdi.gui.DatasetListElement;
import anl.verdi.gui.DatasetListModel;
import anl.verdi.gui.FormulaListElement;
import anl.verdi.gui.FormulaListModel;
import anl.verdi.plot.anim.PlotAnimator;
import anl.verdi.plot.color.ColorMap;
import anl.verdi.plot.color.Palette;
import anl.verdi.plot.color.PavePaletteCreator;
import anl.verdi.plot.config.PlotConfiguration;
import anl.verdi.plot.config.TilePlotConfiguration;
import anl.verdi.plot.config.VertCrossPlotConfiguration;
import anl.verdi.plot.gui.DefaultPlotCreator;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotPanel;
import anl.verdi.plot.gui.ScatterPlotCreator;
import anl.verdi.plot.gui.VerdiBoundaries;
import anl.verdi.plot.gui.VerticalCrossPlotCreator;
import anl.verdi.plot.types.TimeAnimatablePlot;
import anl.verdi.plot.types.VerticalCrossSectionPlot;
import anl.verdi.util.Save2Ascii;
import anl.verdi.util.VersionInfo;

/**Class whose main purpose is to handle and carry out the commands received 
 * from the command line
 * 
 * @author A. Wagner
 */
public class ScriptHandler {
	static final Logger Logger = LogManager.getLogger(ScriptHandler.class.getName());
	static HashMap<String, CommandScript> dataMap = new HashMap<String, CommandScript> ();

	protected static VerdiApplication verdiApp = null;

	private ArrayList<ArrayList<String>> commands = null;

	static HashMap<String, String> aliasMap = new HashMap<String, String> ();

	private static String copyright = "GNU General Public License version 3 (GPLv3)\n" +
			"Submitted by nelson on Tue, 2007-10-23 03:13\n" +
			"GNU GENERAL PUBLIC LICENSE";
	private static String copyrightFile = "../../licenses/verdi_gpl-3.0.txt";
	private static String version = "Version: " + VersionInfo.getVersion() + " " + VersionInfo.getDate();

	private static String curView = "";
	private static HashMap<String, Plot> plotMap = new HashMap<String, Plot>();
	private static List<String> mapNames = new ArrayList<String>();	//stores list of map layer file locations


	private static PlotConfiguration config = new PlotConfiguration();
	private static VertCrossPlotConfiguration vConfig = new VertCrossPlotConfiguration();
	
	private static String subtitle1 = "";
	private static String subtitle2 = "";
	private static String units = "";
	private static String title = "";
	private static int titleSize = -1;
	private static int subtitle1Size = -1;
	private static int subtitle2Size = -1;
	private static boolean showLegendTicks = true;
	private static boolean showDomainTicks = true;
	private static boolean showRangeTicks = true;
	private static boolean showGridLines = false;

	private static int tinit = -1;
	private static int tfinal = -1;

	private static String configFile = null;

	private static ColorMap cmap = null;

	private static int selectedTimeStep = 1; //Default to 1-based first step
	private static String aliasFileName = System.getProperty("user.home") +  File.separatorChar + "verdi" + File.separatorChar + "verdi.alias";
	private static int layerMin = -1;	// minimum layer not set
	private static int layerMax = -1;	// maximum layer not set


	private static final String HELPTEXT = "[ -alias <aliasname=definition> ]\n"
			+ "[ -animatedGIF <filename> ]\n"
			+ "[ -avi <filename> ]\n"
			+ "[ -closeWindow <windowid> ]\n"
			+ "[ -configFile <configFileName> ]\n"
			+ "[ -copyright ]\n"
			+ "[ -drawDomainTicks ON|OFF (NEW) ]\n"
			+ "[ -drawRangeTicks ON|OFF (NEW) ]\n"
			+ "[ -drawLegendTicks ON|OFF (NEW) ]\n"
			+ "[ -drawGridLines ON|OFF (NEW) ]\n"
			+ "[ -f [<host>:]<filename> ]\n"
			+ "[ -fulldomain ]\n"
			+ "[ -g <tile|fasttile|line|bar|contour> ]\n"
			+ "[ -gtype <tile|fasttile|line|bar|contour> ]\n"
			+ "[ -help ]\n"
			+ "[ -legendBins \"<bin0,bin1,...,bin_n>\" ]\n"
			+ "[ -layer <layer> ]\n"
			+ "[ -layerRange <layerMin> <layerMax> ]\n"		// 2015 changed from <layerMax> <layerMin>
			+ "[ -mapName \"<pathname>/<mapFileName>\" ]\n"
			+ "[ -openProject <VERDIProjectName> (NEW)]\n"
			+ "[ -printAlias ]\n"
			+ "[ -project \"<VERDIProjectName>\"]\n"
			+ "[ -quit|exit ]\n"
			+ "[ -quicktime ]\n"
			+ "[ -raiseWindow <windowid> ]\n"
			+ "[ -s \"<formula>\" ]\n"
			+ "[ -save2ascii \"<filename>\" ]\n"
			+ "[ -saveImage \"<image type>\" <file name> ]\n"
			+ "[ -scatter \"<formula1>\" \"<formula2>\" ]\n"
			+ "[ -subDomain <xmin> <ymin> <xmax> <ymax> ]\n"
			+ "[ -subTitle1\"<subtitle 1 string>\" ]\n"
			+ "[ -subTitle2\"<subtitle 2 string>\" ]\n"
			+ "[ -subTitleFont <fontSize> ]\n"
			+ "[ -system \"<system command>\" ]\n"
			+ "[ -tfinal <final time step> ]\n"
			+ "[ -tinit <initial time step> ]\n"
			+ "[ -titleFont <fontSize> ]\n"
			+ "[ -titleString \"<title string>\" ]\n"
			+ "[ -ts <time step> ]\n"
			+ "[ -unalias <aliasname> ]\n"
			+ "[ -unitString \"<unit string>\" ]\n"
			+ "[ -version ]\n"
			+ "[ -verticalCrossPlot X|Y <column/row> (NEW)]\n"
			+ "[ -windowid ]";


	public ScriptHandler(String[] args, VerdiApplication vApp) 
	{
		constructMap();
		verdiApp = vApp;

		loadAliasFile();
		commands = CommandLineParser.parseCommands(args);
	}

	public void run()
	{
		handleOptions();
	}

	public static void constructMap()
	{
		dataMap.put("s".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.S");
				try{
					String formula = args.get(1);
					DatasetListModel dlm = verdiApp.getProject().getDatasets();
					DatasetListElement dle = 
							(DatasetListElement)dlm.getElementAt(dlm.getSize() - 1);

					//see if this is an alias, if it is, we need to convert it
					if(aliasMap.containsKey(formula))
					{
						formula = convertFormula(aliasMap.get(formula), dle.getDataset().getAlias());
					}

					FormulaListElement e = verdiApp.create(formula);

					if(tfinal != -1)
					{
						e.setTimeMax(tfinal);
						e.setTimeUsed(true);
					}
					if(tinit != -1)
					{
						e.setTimeMin(tinit);
						e.setTimeUsed(true);
					}
					if(layerMax != -1 && layerMin != -1)
					{
						e.setLayerMax(layerMax);
						e.setLayerMin(layerMin);
						e.setLayerUsed(true);
					}

					verdiApp.getProject().getFormulas().addFormula(e);
					verdiApp.getProject().setSelectedFormula(e);
				}
				catch(NullPointerException e){
					Logger.debug("Null pointer exception in ScriptHandler.dataMap.put 'S'", e);
				}
			}				
		});

		dataMap.put("f".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.F");
				try{
					File[] f = {new File(args.get(1))};
					verdiApp.loadDataset(f);
				}
				catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'F': " + e.getMessage());
					e.printStackTrace();
				}
				catch( Exception e) {
					Logger.error("Exception in ScriptHandler.dataMap.put 'F': " + e.getMessage());
					e.printStackTrace();
				}
			}				
		});

		dataMap.put("copyright".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.COPYRIGHT");
				//				try {	// warning resource leak: reader never closed; changing to try-with-resources 2014
				//					BufferedReader reader = new BufferedReader(new FileReader(new File(copyrightFile)));
				try (BufferedReader reader = new BufferedReader(new FileReader(new File(copyrightFile))))
				{
					String line = null;

					while ((line = reader.readLine()) != null)
						Logger.debug(line);
				} catch (Exception e) {
					Logger.error(copyright);
				} finally {
					System.exit(0);
				}
			}				
		});

		dataMap.put("alias".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.ALIAS");
				if(args.get(1).indexOf('=') != -1)
				{
					String aliasName = args.get(1).substring(0, args.get(1).indexOf("="));
					String aliasFormula = args.get(1).substring( args.get(1).indexOf("=") + 1);

					try{
						if(aliasMap.containsKey(aliasName))
						{
							Logger.warn("WARNING: Alias '" +
									aliasName + "' already defined, new definition ignored. ");
						}
						else
						{
							aliasMap.put(aliasName, aliasFormula);
							writeToAliasFile();
						}
					}catch(NullPointerException e){
						Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'ALIAS': " + e.getMessage());
					}
				}else{
					Logger.error(args.get(1) + " is an invalid alias");
				}
			}				
		});
		dataMap.put("animatedGIF".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.ANIMATEDGIF");
				//assume that the GIF should be the same dimensions as the plot displayed.
				try{
					Plot plot = plotMap.get(curView);
					PlotAnimator animator = new PlotAnimator((TimeAnimatablePlot)plot);
					DataFrame frame = plot.getData().get(0);
					Axes<DataFrameAxis> axes = frame.getAxes();

					//					int minTimeStep = axes.getTimeAxis().getOrigin();
					//					int maxTimeStep = axes.getTimeAxis().getExtent() + axes.getTimeAxis().getOrigin() - 1;


					animator.start(axes.getTimeAxis().getOrigin(), 
							axes.getTimeAxis().getExtent()-1, null, new File(args.get(1)), null);

				}catch (NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'ANIMATEDGIF': " + e.getMessage());
				}
			}				
		});

		dataMap.put("avi".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.AVI");
				//assume that the AVI should be the same dimensions as the plot displayed.
				try{
					Plot plot = plotMap.get(curView);
					PlotAnimator animator = new PlotAnimator((TimeAnimatablePlot)plot);
					DataFrame frame = plot.getData().get(0);
					Axes<DataFrameAxis> axes = frame.getAxes();

					animator.start(axes.getTimeAxis().getOrigin(), 
							axes.getTimeAxis().getExtent()-1, null, null, new File(args.get(1)));

				}catch (NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'AVI': " + e.getMessage());
				}
			}				
		});

		dataMap.put("closeWindow".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.CLOSEWINDOW");
				//verdiApp.getGui().getViewManager().setActiveView(args.get(1));
				curView = args.get(1);
				verdiApp.getGui().getViewManager().getPerspective().removeDockable(verdiApp.getGui().getViewManager().getDockable(curView));


				Iterator it = plotMap.keySet().iterator();
				ArrayList<String> orderedPlotName = new ArrayList<String>();

				while(it.hasNext())
				{
					orderedPlotName.add(it.next().toString());
				}

				if(orderedPlotName.size() > 1)
				{
					//bubble sort the ArrayList
					for(int i = 0; i < orderedPlotName.size() - 1; i++)
					{
						String thisPlot = orderedPlotName.get(i);
						String nextPlot = orderedPlotName.get(i + 1);
						if(thisPlot.charAt(thisPlot.length() - 1) > nextPlot.charAt(nextPlot.length() - 1))
						{
							String temp = thisPlot;
							orderedPlotName.set(i, nextPlot);
							orderedPlotName.set(i + 1, temp);
						}
					}
				}

				plotMap.remove(curView);

				int indexOfPlot = orderedPlotName.indexOf(curView);
				if( indexOfPlot != orderedPlotName.size() -1)
				{
					curView = orderedPlotName.get(indexOfPlot + 1);
				}
				else if( indexOfPlot != 0)
				{
					curView = orderedPlotName.get(indexOfPlot - 1);
				}
				else
				{
					curView = "";
				}

				//				verdiApp.getGui().getViewManager().getViewport().g
				//				verdiApp.getGui().getViewManager().getViewport().setActiveView(args.get(1));

				//				Iterator it = plots

				//it appears that when a window is closed, the one after it is 
				//opened (if there is one), if there is no window after, 
				//the one before is opened (if there is one)

				//get current position of item
				//delete the item from the list of plots
				//if the position saved still exists, set that one to the current view
				//if the position before it exists, set that one to the current view
				//else set curView to dummy value

				//				verdiApp.getGui().getViewManager().setActiveView(args.get(1));
				//				
				//				View v = verdiApp.getGui().getViewManager().getView(args.get(1));
			}				
		});
		dataMap.put("configFile".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.CONFIGFILE");
				try{
					config = new PlotConfiguration(new File(args.get(1)));
					vConfig = new VertCrossPlotConfiguration(new PlotConfiguration(new File(args.get(1))));
					configFile = args.get(1);
				}
				catch(IOException e){
					Logger.error("IOException in ScriptHandler.dataMap.put 'CONFIGFILE': " + e.getMessage());
				}
				catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'CONFIGFILE': " + e.getMessage());
				}
			}				
		});
		dataMap.put("drawGridLines".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.DRAWGRIDLINES");
				Boolean show = new Boolean(true);
				try{
					if(args.get(1).equalsIgnoreCase("ON"))
					{
						show = true;

					}
					else if(args.get(1).equalsIgnoreCase("OFF"))
					{
						show = false;
					}
					else
					{
						throw new IllegalArgumentException();
					}
					config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, show);
					vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, show);

					showGridLines = show;
				}
				catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'DRAWGRIDLINES': " + e.getMessage());
				}
			}
		});

		dataMap.put("drawDomainTicks".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.DRAWDOMAINTICKS");
				Boolean show = new Boolean(true);
				try{
					if(args.get(1).equalsIgnoreCase("ON"))
					{
						show = true;

					}
					else if(args.get(1).equalsIgnoreCase("OFF"))
					{
						show = false;
					}
					else
					{
						throw new IllegalArgumentException();
					}
					config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, show);

					vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, show);

					showDomainTicks = show;

				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'DRAWDOMAINTICKS': " + e.getMessage());
				}
			}				
		});
		dataMap.put("drawRangeTicks".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.DRAWRANGETICKS");
				Boolean show = new Boolean(true);
				try{
					if(args.get(1).equalsIgnoreCase("ON"))
					{
						show = true;

					}
					else if(args.get(1).equalsIgnoreCase("OFF"))
					{
						show = false;
					}
					else
					{
						throw new IllegalArgumentException();
					}
					config.putObject(PlotConfiguration.RANGE_SHOW_TICK, show);

					vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, show);

					showRangeTicks = show;

				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'DRAWRANGETICKS': " + e.getMessage());
				}
			}				
		});
		dataMap.put("drawLegendTicks".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.DRAWLEGENDTICKS");
				Boolean show = new Boolean(true);
				try{
					if(args.get(1).equalsIgnoreCase("ON"))
					{
						show = true;

					}
					else if(args.get(1).equalsIgnoreCase("OFF"))
					{
						show = false;
					}
					else
					{
						throw new IllegalArgumentException();
					}
					config.putObject(PlotConfiguration.UNITS_SHOW_TICK, show);

					vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, show);

					showLegendTicks = show;

				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'DRAWLEGENDTICKS': " + e.getMessage());
				}
			}				
		});
		dataMap.put( "fulldomain".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.FULLDOMAIN");
				//since there is no way to change the dataset programmatically, 
				//we will just use the last added one
				DatasetListModel dlm = verdiApp.getProject().getDatasets();
				DatasetListElement dle = 
						(DatasetListElement)dlm.getElementAt(dlm.getSize() - 1);

				dle.setXYUsed(false);
			}				
		});
		dataMap.put("g".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.G");
				//should be tile|line|bar|contour

				Plot plot = null;
				try{
					if(args.get(1).equalsIgnoreCase("tile") ||
							args.get(1).equalsIgnoreCase("fasttile")) {
						//					{
						//						plot = new DefaultPlotCreator(verdiApp, Formula.Type.TILE, config).createPlot();
						//						
						//						DataFrame frame = plot.getData().get(0);
						//						Axes<DataFrameAxis> axes = frame.getAxes();
						//						
						//						//assume the user is passing in the actual number of the 
						//						//timestep
						//						((TilePlot)plot).updateTimeStep(selectedTimeStep - axes.getTimeAxis().getOrigin());
						//
						//
						//					}
						//					else if (args.get(1).equalsIgnoreCase("fasttile")) {
						if ( verdiApp.getProject().getSelectedFormula() != null ) {
							final DataFrame dataFrame =
									verdiApp.evaluateFormula( Formula.Type.TILE );

							if ( dataFrame != null ) {
								plot = new anl.verdi.plot.gui.FastTilePlot(verdiApp, dataFrame);

								//assume the user is passing in the actual number of the 
								//timestep 1-based
								Axes<DataFrameAxis> axes = dataFrame.getAxes();
								((anl.verdi.plot.gui.FastTilePlot)plot).updateTimeStep(selectedTimeStep - 1 - axes.getTimeAxis().getOrigin());
								plot.configure(new TilePlotConfiguration(config), Plot.ConfigSource.FILE);

								PlotPanel panel = new PlotPanel( plot, "Tile: " + verdiApp.getProject().getSelectedFormula());
								verdiApp.getGui().addPlot( panel );
								panel.addPlotListener( verdiApp );
							}
						}
					}
					else if(args.get(1).equalsIgnoreCase("line"))
					{
						plot = new DefaultPlotCreator(verdiApp, Formula.Type.TIME_SERIES_LINE, config).createPlot();
					}
					else if(args.get(1).equalsIgnoreCase("bar"))
					{
						plot = new DefaultPlotCreator(verdiApp, Formula.Type.TIME_SERIES_BAR, config).createPlot();
					}
					else if(args.get(1).equalsIgnoreCase("contour"))
					{
						plot = new DefaultPlotCreator(verdiApp, Formula.Type.CONTOUR, config).createPlot();
					}
					else
					{
						throw new IllegalArgumentException();
					}

					List<String> viewList = verdiApp.getGui().getViewList();

					if (viewList.size() == 0) throw new IllegalArgumentException("Please check your argument sequence (like -g is before -f and/or -s).");
					curView = viewList.get(viewList.size() - 1);
					plotMap.put(curView, plot);

					//only reset the configuration if we are not using a configuration file
					if(configFile == null)
					{	
						config = new PlotConfiguration();
						vConfig = new VertCrossPlotConfiguration();
						if(cmap != null)
						{
							config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
							vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
						}
						titleSize = -1;
						subtitle1Size = -1;
						subtitle2Size = -1;
						subtitle1 = "";
						subtitle2 = "";
						units = "";
						title = "";

						config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
						vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);

						config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);

						config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);

						config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
						vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
					}
				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'G': " + e.getMessage());
				}

			}				
		});
		dataMap.put("gtype".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.GTYPE");
				//should be tile|line|bar|contour

				Plot plot = null;
				try{
					if(args.get(1).equalsIgnoreCase("tile") ||
							args.get(1).equalsIgnoreCase("fasttile"))
					{
						if ( verdiApp.getProject().getSelectedFormula() != null ) {
							final DataFrame dataFrame =
									verdiApp.evaluateFormula( Formula.Type.TILE );

							if ( dataFrame != null ) {
								plot = new anl.verdi.plot.gui.FastTilePlot(verdiApp, dataFrame);

								//assume the user is passing in the actual number of the 
								//timestep 1-based
								Axes<DataFrameAxis> axes = dataFrame.getAxes();
								((anl.verdi.plot.gui.FastTilePlot)plot).updateTimeStep(selectedTimeStep - 1 - axes.getTimeAxis().getOrigin());
								plot.configure(new TilePlotConfiguration(config), Plot.ConfigSource.FILE);

								PlotPanel panel = new PlotPanel( plot, "Tile: " + verdiApp.getProject().getSelectedFormula());
								verdiApp.getGui().addPlot( panel );
								panel.addPlotListener( verdiApp );
							}
						}
					}
					else if(args.get(1).equalsIgnoreCase("line"))
					{
						plot = new DefaultPlotCreator(verdiApp, Formula.Type.TIME_SERIES_LINE, config).createPlot();
					}
					else if(args.get(1).equalsIgnoreCase("bar"))
					{
						plot = new DefaultPlotCreator(verdiApp, Formula.Type.TIME_SERIES_BAR, config).createPlot();
					}
					else if(args.get(1).equalsIgnoreCase("contour"))
					{
						plot = new DefaultPlotCreator(verdiApp, Formula.Type.CONTOUR, config).createPlot();
					}

					List<String> viewList = verdiApp.getGui().getViewList();
					curView = viewList.get(viewList.size() - 1);

					plotMap.put(curView, plot);

					//only reset the configuration if we are not using a configuration file
					if(configFile == null)
					{
						config = new PlotConfiguration();
						vConfig = new VertCrossPlotConfiguration();

						if(cmap != null)
						{
							config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
							vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
						}


						subtitle1 = "";
						subtitle2 = "";
						units = "";
						title = "";
						titleSize = -1;
						subtitle1Size = -1;
						subtitle2Size = -1;

						config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
						vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);

						config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);

						config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);

						config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
						vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
					}
				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'GTYPE': " + e.getMessage());
				}

			}				
		});
		dataMap.put("help".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.HELP");
				Logger.debug(HELPTEXT);
				System.out.println(HELPTEXT);
				System.exit(0);
			}				
		});
		dataMap.put("legendBins".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.LEGENDBINS");
				try{
					String[] allItems = null;
					int numColors = args.size() - 1;
					PavePaletteCreator p = new PavePaletteCreator();

					if(args.size() == 2)
					{
						if(args.get(1).equalsIgnoreCase("DEFAULT"))
						{
							cmap = null;

							resetConfigurationsWithoutColorMap();
						}
						else
						{
							allItems = args.get(1).split(",");
							numColors = allItems.length;

							List<Palette> paletteList = p.createPalettes(numColors - 1);

							cmap = new ColorMap(paletteList.get(0), 
									Double.parseDouble(allItems[0]), 
									Double.parseDouble(allItems[allItems.length - 1]));

							for(int i = 0; i < numColors - 1; i++)
							{
								try {
									cmap.setIntervalStart(i, Double.parseDouble(allItems[i]));
								} catch (NumberFormatException e) {
									Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'LEGENDBINS': " + e.getMessage());
									e.printStackTrace();
								} catch (Exception e) {
									Logger.error("Exception in ScriptHandler.dataMap.put 'LEGENDBINS': " + e.getMessage());
									e.printStackTrace();
								}
							}
						}
					}
					else
					{
						numColors = args.size() - 1;

						List<Palette> paletteList = p.createPalettes(numColors - 1);

						cmap = new ColorMap(paletteList.get(0), 
								Double.parseDouble(args.get(1)), 
								Double.parseDouble(args.get(args.size() - 1)));

						for(int i = 1; i < numColors - 1; i++)
						{
							try {
								cmap.setIntervalStart(i - 1, Double.parseDouble(args.get(i)));
							} catch (NumberFormatException e) {
								Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'LEGENDBINS': " + e.getMessage());
								e.printStackTrace();
							} catch (Exception e) {
								Logger.error("Exception in ScriptHandler.dataMap.put 'LEGENDBINS': " + e.getMessage());
								e.printStackTrace();
							}
						}
					}

					if(cmap!= null)
					{
						config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
						vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
					}

				}catch (NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'LEGENDBINS': " + e.getMessage());
				}
			}				
		});
		dataMap.put("layer".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.LAYER");
				try{
					layerMin = Integer.parseInt(args.get(1)) - 1;
					if(layerMin < 0)
						layerMin = 0;
					layerMax = layerMin;		// Integer.parseInt(args.get(1));
				}catch(NumberFormatException e){
					Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'LAYER': " + e.getMessage());
				}
				catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'LAYER': " + e.getMessage());
				}

				FormulaListModel flm = verdiApp.getProject().getFormulas();
				for(int i = 0; i < flm.getSize(); i++)
				{
					try{
						FormulaListElement obj = (FormulaListElement)flm.getElementAt(i);
						obj.setLayerMax(layerMax);		// obj.setLayerMax(Integer.parseInt(args.get(1)));
						obj.setLayerMin(layerMin);		// obj.setLayerMin(Integer.parseInt(args.get(1)));
						obj.setLayerUsed(true);
					}catch(NumberFormatException e){
						Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'LAYER': " + e.getMessage());
					}
					catch(NullPointerException e){
						Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'LAYER': " + e.getMessage());
					}
				}
			}				
		});
		dataMap.put("layerRange".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.LAYERRANGE");
				FormulaListModel flm = verdiApp.getProject().getFormulas();
				try{
					layerMin = Integer.parseInt(args.get(1)) - 1;
					if(layerMin < 0)
						layerMin = 0;
					layerMax = Integer.parseInt(args.get(2)) - 1;
					if(layerMax < 0)
						layerMax = 0;
					Logger.debug("layerMin = " + layerMin);
					Logger.debug("layerMax = " + layerMax);
				}catch(NumberFormatException e){
					Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'LAYERRANGE': " + e.getMessage());
				}
				catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'LAYERRANGE': " + e.getMessage());
				}

				for(int i = 0; i < flm.getSize(); i++)
				{
					try{
						FormulaListElement obj = (FormulaListElement)flm.getElementAt(i);
						// 2015 NOTE: switched order of assignment to min then max
						obj.setLayerMin(layerMin);
						obj.setLayerMax(layerMax);
						obj.setLayerUsed(true);
					}catch(NumberFormatException e){
						Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'LAYERRANGE': " + e.getMessage());
					}
					catch(NullPointerException e){
						Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'LAYERRANGE': " + e.getMessage());
					}
				}
			}				
		});
		dataMap.put("mapName".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.MAPNAME");

				mapNames.add(args.get(1));
			}				
		});
		dataMap.put("openProject".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.OPENPROJECT");
				try{
					verdiApp.openProject(new File(args.get(1)));
				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'OPENPROJECT': " + e.getMessage());
				}	
			}
		});
		dataMap.put("printAlias".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.PRINTALIAS");

				Logger.debug("Aliases Defined:");
				Set keys = aliasMap.keySet();         // The set of keys in the map.
				Iterator keyIter = keys.iterator();
				while (keyIter.hasNext()) {

					Object key = keyIter.next();
					Logger.debug(key + "=" + aliasMap.get(key));
				}
			}				
		});
		dataMap.put("quit".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.QUIT");
				System.exit(0);
			}				
		});
		dataMap.put("exit".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.EXIT");	// same as QUIT
				System.exit(0);
			}				
		});
		dataMap.put("raiseWindow".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.RAISEWINDOW");
				try{

					verdiApp.getGui().getViewManager().getDockable(args.get(1)).toFront();
					curView = args.get(1);
				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'RAISEWINDOW': " + e.getMessage());
				}
			}				
		});
		dataMap.put("save2ascii".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.SAVE2ASCII");
				try{
					Plot plot = plotMap.get(curView);
					Formula.Type type = plot.getType();

					if (type == Formula.Type.TILE 
							|| type == Formula.Type.TIME_SERIES_LINE 
							|| type == Formula.Type.TIME_SERIES_BAR
							|| type == Formula.Type.VERTICAL_CROSS_SECTION) {

						Save2Ascii saver = new Save2Ascii(plot);
						saver.save(new File(args.get(1)));
					}
					else{
						Logger.error("Cannot create ASCII file:  "
								+ args.get(1) + " Cannot make ASCII " 
								+ "version of file for selected plot.");
					}

				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'SAVE2ASCII': " + e.getMessage());
				}catch(IOException e){
					Logger.error("IOException in ScriptHandler.dataMap.put 'LAYER': " + e.getMessage());
				}

			}				
		});
		dataMap.put("saveImage".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.SAVEIMAGE");
				Plot plot = plotMap.get(curView);

				try{
					plot.exportImage(args.get(1), new File(args.get(2)), 800, 600);
				}
				catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'SAVEIMAGE': " + e.getMessage());
				}
				catch(IOException e){
					Logger.error("IOException in ScriptHandler.dataMap.put 'SAVEIMAGE': " + e.getMessage());
				}
			}				
		});
		dataMap.put("scatter".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.SCATTER");
				try{
					String formulaX = args.get(1);
					String formulaY = args.get(2);
					DatasetListModel dlm = verdiApp.getProject().getDatasets();
					DatasetListElement dle = 
							(DatasetListElement)dlm.getElementAt(dlm.getSize() - 1);
					if(aliasMap.containsKey(formulaX))
					{			
						formulaX = convertFormula(aliasMap.get(formulaX), dle.getDataset().getAlias());
					}


					if(aliasMap.containsKey(formulaY))
					{
						formulaY = convertFormula(aliasMap.get(formulaY), dle.getDataset().getAlias());
					}

					FormulaListElement x = verdiApp.create(formulaX);
					verdiApp.getProject().getFormulas().addFormula(x);

					FormulaListElement y = verdiApp.create(formulaY);
					verdiApp.getProject().getFormulas().addFormula(y);

					ScatterPlotCreator spc = new ScatterPlotCreator(verdiApp);
					Plot plot = spc.createPlotFromCommandLine(x, y);

					List<String> viewList = verdiApp.getGui().getViewList();
					curView = viewList.get(viewList.size() - 1);
					plotMap.put(curView, plot);

				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'SCATTER': " + e.getMessage());
				}
			}				
		});
		dataMap.put("subDomain".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.SUBDOMAIN");
				try{
					//since there is no way to change the dataset programmatically, 
					//we will just use the last added one
					//					verdiApp.getProject().getSelectedFormula();
					DatasetListModel dlm = verdiApp.getProject().getDatasets();

					DatasetListElement dle = 
							(DatasetListElement)dlm.getElementAt(dlm.getSize() - 1);

					//						String arg1 = args.get(1);
					//						String arg2 = args.get(2);
					//						String arg3 = args.get(3);
					//						String arg4 = args.get(4);

					//zero based indexing...
					dle.setXMin(Integer.parseInt(args.get(1)) - 1);
					dle.setYMin(Integer.parseInt(args.get(2)) - 1);
					dle.setXMax(Integer.parseInt(args.get(3)) - 1);
					dle.setYMax(Integer.parseInt(args.get(4)) - 1);
					dle.setXYUsed(true);
				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'SUBDOMAIN': " + e.getMessage());
				}

			}				
		});
		dataMap.put("subTitle1".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.SUBTITLE1");
				try{
					config.setSubtitle1(args.get(1));
					vConfig.setSubtitle1(args.get(1));
					subtitle1 = args.get(1);

				}catch(NullPointerException e) {
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'SUBTITLE1': " + e.getMessage());
				}
			}				
		});
		dataMap.put("subTitle2".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.SUBTITLE2");
				try{
					config.setSubtitle2(args.get(1));
					vConfig.setSubtitle2(args.get(1));
					subtitle2 = args.get(1);
				}catch(NullPointerException e) {
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'SUBTITLE2': " + e.getMessage());
				}
			}				
		});
		dataMap.put("subTitleFont".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.SUBTITLEFONT");
				try{
					config.putObject(PlotConfiguration.SUBTITLE_1_FONT, 
							new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
					config.putObject(PlotConfiguration.SUBTITLE_2_FONT, 
							new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));

					vConfig.putObject(PlotConfiguration.SUBTITLE_1_FONT, 
							new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
					vConfig.putObject(PlotConfiguration.SUBTITLE_2_FONT, 
							new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));

				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'SUBTITLEFONT': " + e.getMessage());
				}
			}				
		});
		dataMap.put("system".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.SYSTEM");
				String commandStr = args.get(1);
				Process process = null;
				//  Execute command as a subprocess
				try {
					process = Runtime.getRuntime().exec(commandStr);
					BufferedReader in = new BufferedReader(new InputStreamReader(
							process.getInputStream()));
					String curLine = "";
					while ( (curLine = in.readLine()) != null ) {
						Logger.debug(curLine);
					}
					try {
						process.waitFor();
					} catch(InterruptedException e) {
						Logger.error("Interrupted Exception in ScriptHandler.dataMap.put 'SYSTEM': " + e.getMessage());
					}
				} catch(IOException e) {
					Logger.error("IOException in ScriptHandler.dataMap.put 'SYSTEM': " + e.getMessage());
				}
			}				
		});
		dataMap.put("tfinal".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.TFINAL");
				try{
					tfinal = Integer.parseInt(args.get(1));
				}catch(NumberFormatException e){
					Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'TFINAL': " + e.getMessage());
				}

				FormulaListModel flm = verdiApp.getProject().getFormulas();
				for(int i = 0; i < flm.getSize(); i++)
				{
					try{
						FormulaListElement obj = (FormulaListElement)flm.getElementAt(i);
						obj.setTimeMax(Integer.parseInt(args.get(1)));
						obj.setTimeUsed(true);
					}catch(NumberFormatException e){
						Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'TFINAL': " + e.getMessage());
					}
					catch(NullPointerException e){
						Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'TFINAL': " + e.getMessage());
					}
				}
			}				
		});
		dataMap.put("tinit".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.TINIT");
				FormulaListModel flm = verdiApp.getProject().getFormulas();
				try{
					tinit = Integer.parseInt(args.get(1));
					selectedTimeStep = tinit;
				}catch(NumberFormatException e){
					Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'TINIT': " + e.getMessage());
				}

				for(int i = 0; i < flm.getSize(); i++)
				{
					try{
						FormulaListElement obj = (FormulaListElement)flm.getElementAt(i);
						obj.setTimeMin(Integer.parseInt(args.get(1)));
						obj.setTimeUsed(true);
					}catch(NumberFormatException e){
						Logger.error("Number Format Exception in ScriptHandler.dataMap.put 'TINIT': " + e.getMessage());
					}
					catch(NullPointerException e){
						Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'TINIT': " + e.getMessage());
					}
				}
			}				
		});
		dataMap.put("titleFont".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.TITLEFONT");
				try{
					config.putObject(PlotConfiguration.TITLE_FONT, new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
					vConfig.putObject(PlotConfiguration.TITLE_FONT, new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'TITLEFONT': " + e.getMessage());
				}
			}				
		});
		dataMap.put("titleString".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.TITLESTRING");
				try{
					config.setTitle(args.get(1));
					vConfig.setTitle(args.get(1));
					title = args.get(1);
				}catch(NullPointerException e) {
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'TITLESTRING': " + e.getMessage());
				}
			}				
		});
		dataMap.put("ts".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.TS");
				try{
					selectedTimeStep = Integer.parseInt(args.get(1));
				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'TS': " + e.getMessage());
				}
			}				
		});
		dataMap.put("quicktime".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.QUICKTIME");
				//					assume that the movie should be the same dimensions as the plot displayed.
				try{
					Plot plot = plotMap.get(curView);
					PlotAnimator animator = new PlotAnimator((TimeAnimatablePlot)plot);
					DataFrame frame = plot.getData().get(0);
					Axes<DataFrameAxis> axes = frame.getAxes();

					animator.start(axes.getTimeAxis().getOrigin(), axes.getTimeAxis().getExtent()-1, new File(args.get(1)), null, null);
				}catch (NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'QUICKTIME': " + e.getMessage());
				}
			}
		});
		dataMap.put("unalias".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.UNALIAS");
				try{
					aliasMap.remove(args.get(1));
					writeToAliasFile();
				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'UNALIAS': " + e.getMessage());
				}

			}				
		});
		dataMap.put("unitString".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.UNITSTRING");
				try{
					config.setUnits(args.get(1));
					vConfig.setUnits(args.get(1));
					units = args.get(1);
				}catch(NullPointerException e) {
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'UNITSTRING': " + e.getMessage());
				}
			}				
		});
		dataMap.put("vector".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.VECTOR");
				try{
					String uWind = args.get(1);
					String vWind = args.get(2);
					DatasetListModel dlm = verdiApp.getProject().getDatasets();
					DatasetListElement dle = 
							(DatasetListElement)dlm.getElementAt(dlm.getSize() - 1);
					if(aliasMap.containsKey(uWind))
					{			
						uWind = convertFormula(aliasMap.get(uWind), dle.getDataset().getAlias());
					}


					if(aliasMap.containsKey(vWind))
					{
						vWind = convertFormula(aliasMap.get(vWind), dle.getDataset().getAlias());
					}


					FormulaListElement u = verdiApp.create(uWind);
					verdiApp.getProject().getFormulas().addFormula(u);

					FormulaListElement v = verdiApp.create(vWind);
					verdiApp.getProject().getFormulas().addFormula(v);

					List<String> viewList = verdiApp.getGui().getViewList();
					curView = viewList.get(viewList.size() - 1);

					if(configFile == null)
					{
						//reset the subtitles, titles and units
						subtitle1 = "";
						subtitle2 = "";
						units = "";
						title = "";
						titleSize = -1;
						subtitle1Size = -1;
						subtitle2Size = -1;
						vConfig = new VertCrossPlotConfiguration();
						config = new PlotConfiguration();

						if(cmap != null)
						{
							config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
							vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
						}

						config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
						vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);

						config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);

						config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);

						config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
						vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
					}

				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'VECTOR': " + e.getMessage());
				}
			}				
		});
		dataMap.put("vectorTile".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.VECTORTILE");
				try{
					String formula = args.get(1);
					String uWind = args.get(2);
					String vWind = args.get(3);
					DatasetListModel dlm = verdiApp.getProject().getDatasets();
					DatasetListElement dle = 
							(DatasetListElement)dlm.getElementAt(dlm.getSize() - 1);

					if(aliasMap.containsKey(formula))
					{			
						formula = convertFormula(aliasMap.get(formula), dle.getDataset().getAlias());
					}
					if(aliasMap.containsKey(uWind))
					{			
						uWind = convertFormula(aliasMap.get(uWind), dle.getDataset().getAlias());
					}

					if(aliasMap.containsKey(vWind))
					{
						vWind = convertFormula(aliasMap.get(vWind), dle.getDataset().getAlias());
					}


					FormulaListElement form = verdiApp.create(formula);
					verdiApp.getProject().getFormulas().addFormula(form);

					FormulaListElement u = verdiApp.create(uWind);
					verdiApp.getProject().getFormulas().addFormula(u);

					FormulaListElement v = verdiApp.create(vWind);
					verdiApp.getProject().getFormulas().addFormula(v);

					List<String> viewList = verdiApp.getGui().getViewList();
					curView = viewList.get(viewList.size() - 1);
					//						plotMap.put(curView, plot);

					if(configFile == null)
					{
						//reset the subtitles, titles and units
						vConfig = new VertCrossPlotConfiguration();
						config = new PlotConfiguration();
						subtitle1 = "";
						subtitle2 = "";
						units = "";
						title = "";
						titleSize = -1;
						subtitle1Size = -1;
						subtitle2Size = -1;

						if(cmap != null)
						{
							config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
							vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
						}

						config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
						vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);

						config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);

						config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);

						config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
						vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
					}

				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'VECTORTILE': " + e.getMessage());
				}
			}				
		});
		dataMap.put("version".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.VERSION");
				Logger.debug(version);
				System.out.println(version);
				System.exit(0);
			}				
		});

		dataMap.put("verticalCrossPlot".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.VERTICALCROSSPLOT");
				try{					
					if(args.get(1).equalsIgnoreCase("X"))
					{
						vConfig.setCrossSectionType(VerticalCrossSectionPlot.CrossSectionType.X);
					}
					else if(args.get(1).equalsIgnoreCase("Y"))
					{
						vConfig.setCrossSectionType(VerticalCrossSectionPlot.CrossSectionType.Y);
					}

					vConfig.setCrossSectionRowCol(Integer.parseInt(args.get(2)));

					//					vConfig.setTitle("My Vert Cross");
					Plot plot = new VerticalCrossPlotCreator(verdiApp, vConfig).createPlot();

					DataFrame frame = plot.getData().get(0);
					Axes<DataFrameAxis> axes = frame.getAxes();

					//assume the user is passing in the actual number of the timestep
					((VerticalCrossSectionPlot)plot).updateTimeStep(selectedTimeStep - axes.getTimeAxis().getOrigin());

					List<String> viewList = verdiApp.getGui().getViewList();
					curView = viewList.get(viewList.size() - 1);
					plotMap.put(curView, plot);

					if(configFile == null)
					{
						//reset the subtitles, titles and units
						vConfig = new VertCrossPlotConfiguration();
						config = new PlotConfiguration();
						subtitle1 = "";
						subtitle2 = "";
						units = "";
						title = "";
						titleSize = -1;
						subtitle1Size = -1;
						subtitle2Size = -1;

						if(cmap != null)
						{
							config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
							vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
						}

						config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
						vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);

						config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);

						config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);

						config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
						vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
					}
				}catch(NullPointerException e){
					Logger.error("Null Pointer Exception in ScriptHandler.dataMap.put 'VERTICALCROSSPLOT': " + e.getMessage());
				}
			}				
		});
		dataMap.put("windowid".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug("ScriptHandler.constructMap.WINDOWID");
				List<String> viewList = verdiApp.getGui().getViewList();
				if(viewList.size() > 0)
				{
					Logger.debug(viewList.get(viewList.size() - 1));
				}
				else
				{
					Logger.error("Error: No window open");
				}
			}				
		});
	}


	private void handleOptions()
	{
		if(commands.size() > 0)
		{
			ArrayList<String> thisCommand = null;
			for(int i = 0; i < commands.size(); i++)
			{
				thisCommand = commands.get(i);

				CommandScript cScript = dataMap.get(thisCommand.get(0).substring(1).toUpperCase());

				if(cScript != null)
				{	
					if(thisCommand.size() > 1)
					{
						cScript.run(thisCommand);
					}
					else
					{
						cScript.run(null);
					}
				}
				else
				{
					Logger.error(thisCommand.get(0) + " is not a valid command." +
							"  Skipping this command...");
				}
			}


			//now that all the work has been done to create plots, let's see if we can apply some map layers to these plots
			//NOTE:  the map layers are only applicable to the areal interp and fast tile plots!

			for (Map.Entry<String, Plot> entry : plotMap.entrySet())
			{
				Plot plot = entry.getValue();
				Formula.Type type = plot.getType();

				if (type == Formula.Type.TILE && mapNames.size() > 0) {
					VerdiBoundaries mapLine = null;
					try {
						for (String mapNameFileURL : mapNames) {
							mapLine = new VerdiBoundaries();
							mapLine.setFileName(mapNameFileURL);
							((anl.verdi.plot.gui.FastTilePlot)plot).setLayerMapLine(mapLine);
						}
						//suppress all issues (i.e., wrong file path or wrong format)
//					} catch (FileNotFoundException e) {
//						Logger.error("File Not Found Exception in ScriptHandler.handleOptions: " + e.getMessage());
//					} catch (IOException e) {
//						Logger.error("IOException in ScriptHandler.handleOptions: " + e.getMessage());
					} catch(Exception ex)
					{
						Logger.error("Exception in ScriptHandler.handleOptions: " + ex.getMessage());
					}
				}
			}
		}
	}

	public static HashMap getCommands(){
		return dataMap;
	}

	protected static VerdiApplication getVerdiApp()
	{
		return verdiApp;
	}

	/**This will create a new usable string from an alias, making it dataset-specific
	 * 
	 * @param datasetAlias String
	 * @return String
	 */
	private static String convertFormula(String aliasFormula, String datasetAlias)
	{
		Logger.debug("ScriptHandler.convertFormula");
		String newFormula = "";

		if(aliasFormula != null && !aliasFormula.equalsIgnoreCase(""))
		{
			String tempString = "";

			for(int i = 0; i < aliasFormula.length(); i++)
			{
				if(aliasFormula.substring(i, i + 1).equalsIgnoreCase("\\")
						|| aliasFormula.substring(i, i + 1).equalsIgnoreCase("/")
						|| aliasFormula.substring(i, i + 1).equalsIgnoreCase("+")
						|| aliasFormula.substring(i, i + 1).equalsIgnoreCase("-")
						|| aliasFormula.substring(i, i + 1).equalsIgnoreCase("*"))
				{
					newFormula = newFormula + tempString + datasetAlias 
							+ aliasFormula.substring(i, i + 1);
					tempString = "";
				}
				else if( i == aliasFormula.length() - 1)
				{
					newFormula = newFormula + tempString //+ aliasFormula.length() 
							+ aliasFormula.substring(i, i + 1) + datasetAlias;
					tempString = "";
				}
				else
				{
					tempString = tempString + aliasFormula.substring(i, i + 1);
				}
			}
		}
		return newFormula;
	}

	private static void resetConfigurationsWithoutColorMap()
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
				Logger.error("IOException in ScriptHandler.resetConfigurationsWithoutColorMap: " + e.getMessage());
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
	}

	private static void loadAliasFile()
	{
		Logger.debug("ScriptHandler.loadAliasFile");
		try {
			BufferedReader in = new BufferedReader(new FileReader(aliasFileName));
			String str;
			while ((str = in.readLine()) != null) {
				String[] aliasitem = str.split(" ");
				aliasMap.put(aliasitem[0], aliasitem[1]);
			}
			in.close();
		} catch (IOException e) {
			Logger.debug("IOException in ScriptHandler.loadAliasFile: " + e.getMessage());
		} catch (Exception e) {
			Logger.error("Exception in ScriptHandler.loadAliasFile: " + e.getMessage());
		} 
	}

	private static void writeToAliasFile()
	{
		//Load the file userFile
		Logger.debug("ScriptHandler.constructMap.writeToAliasFile");
		try{
			FileWriter outFile = new FileWriter(aliasFileName);
			PrintWriter out = new PrintWriter(outFile);
			Iterator it = aliasMap.keySet().iterator();
			while(it.hasNext())
			{
				Logger.debug("Printing file");
				String key = (String)it.next();
				String aliasFormula = (String)aliasMap.get(key);

				out.println(key + " " + aliasFormula);
			}
			out.close();

		}catch(IOException i){
			Logger.error("IOException in ScriptHandler.writeToAliasFile: " + i.getMessage());
		} catch (Exception e) {
			Logger.error("Exception in ScriptHandler.writeToAliasFile: " + e.getMessage());
		}
	}
}
