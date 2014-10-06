package anl.verdi.commandline;

import gov.epa.emvl.MapLines;

import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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
//import anl.verdi.plot.config.VectorPlotConfiguration;		// 2014 removed old Vector Plot
import anl.verdi.plot.config.VertCrossPlotConfiguration;
import anl.verdi.plot.gui.DefaultPlotCreator;
import anl.verdi.plot.gui.Plot;
import anl.verdi.plot.gui.PlotPanel;
import anl.verdi.plot.gui.ScatterPlotCreator;
//import anl.verdi.plot.gui.VectorPlotCreator;
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
	private static List<String> mapNames = new ArrayList<String>();	//stores list of map layer bin file locations
	
	
	private static PlotConfiguration config = new PlotConfiguration();
	private static VertCrossPlotConfiguration vConfig = new VertCrossPlotConfiguration();
//	private static VectorPlotConfiguration vectorConfig = new VectorPlotConfiguration();
	
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
	private static String aliasFileName = System.getProperty("user.home") + "verdi.alias";
	private static int levelMin = -1;
	private static int levelMax = -1;
	
	
	private static final String HELPTEXT = "[-alias <aliasname=definition> ]\n"
											+ "[-animatedGIF<filename> ]\n"
											+ "[-avi<filename> ]\n"
											+ "[-closeWindow<windowid> ]\n"
											+ "[-configFile<configFileName> ]\n"
											+ "[-copyright ]\n"
											+ "[ -drawDomainTicks ON|OFF (NEW) ]\n"
											+ "[ -drawRangeTicks ON|OFF (NEW) ]\n"
											+ "[ -drawLegendTicks ON|OFF (NEW) ]\n"
											+ "[ -drawGridLines ON|OFF (NEW) ]\n"
											+ "[-f [<host>:]<filename> ]\n"
											+ "[-fulldomain ]\n"
											+ "[-g <tile|fasttile|line|bar|contour> ]\n"
											+ "[-gtype <tile|fasttile|line|bar|contour> ]\n"
											+ "[-help|fullhelp|usage ]\n"
											+ "[-legendBins \"<bin0,bin1,...,bin_n>\" ]\n"
											+ "[-level <level> ]\n"
											+ "[-levelRange <levelMax> <levelMin> ]\n"
											+ "[-mapName \"<pathname>/<mapFileName>\" ]\n"
//											+ "[ -multitime <Nformulas> \"<formula1>\" ... \"<formulaN>\" ]\n"
											+ "[ -openProject <VERDIProjectName> (NEW)]\n"
											+ "[ -printAlias ]\n"
											+ "[ -project \"<VERDIProjectName>\"]\n"
											+ "[ -quit|exit ]\n"
											+ "[ -quicktime (NEW)]\n"
											+ "[ -raiseWindow <windowid> ]\n"
											+ "[ -s \"<formula>\" ]\n"
											+ "[ -save2ascii \"<filename>\" ]\n"
											+ "[ -saveImage \"<image type>\" <file name> ]\n"
											+ "[ -scatter \"<formula1>\" \"<formula2>\" ]\n"
//											+ "[ -showWindow <windowId> <timestep> ]\n"
											+ "[ -subDomain <xmin> <ymin> <xmax> <ymax> ]\n"
											+ "[ -subTitle1\"<sub title 1 string>\" ]\n"
											+ "[ -subTitle2\"<sub title 2 string>\" ]\n"
											+ "[ -subTitleFont <fontSize> ]\n"
											+ "[ -system \"<system command>\" ]\n"
											+ "[ -tfinal <final time step> ]\n"
											+ "[ -tinit <initial time step> ]\n"
											+ "[ -titleFont <fontSize> ]\n"
											+ "[ -titleString \"<title string>\" ]\n"
											+ "[ -ts <time step> ]\n"
											+ "[ -unalias <aliasname> ]\n"
											+ "[ -unitString \"<unit string>\" ]\n"
//											+ "[ -vectobs <formula> <formula> ]\n"
//											+ "[ -vector \"<U>\" \"<V>\"]\n"
//											+ "[ -vectorTile \"<formula>\" \"<U>\" \"<V>\"]\n"
											+ "[ -version ]\n"
											+ "[ -verticalCrossPlot X|Y <row/column> (NEW)]\n"
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
					if(levelMax != -1 && levelMin != -1)
					{
						e.setLayerMax(levelMax);
						e.setLayerMin(levelMin);
						e.setLayerUsed(true);
					}
					
					verdiApp.getProject().getFormulas().addFormula(e);
					verdiApp.getProject().setSelectedFormula(e);
				}
				catch(NullPointerException e){}
			}				
		});
		
		dataMap.put("f".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				try{
					File[] f = {new File(args.get(1))};
					verdiApp.loadDataset(f);
				}
				catch(NullPointerException e){
					e.printStackTrace();
				}
				catch( Exception e) {
					e.printStackTrace();
				}
			}				
		});
		
		dataMap.put("copyright".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
//				try {	// warning resource leak: reader never closed; changing to try-with-resources 2014
//					BufferedReader reader = new BufferedReader(new FileReader(new File(copyrightFile)));
				try (BufferedReader reader = new BufferedReader(new FileReader(new File(copyrightFile))))
				{
					String line = null;
					
					while ((line = reader.readLine()) != null)
						Logger.debug(line);
				} catch (Exception e) {
					Logger.debug(copyright);
				} finally {
					System.exit(0);
				}
			}				
		});
		
		dataMap.put("alias".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				
				if(args.get(1).indexOf('=') != -1)
				{
					String aliasName = args.get(1).substring(0, args.get(1).indexOf("="));
					String aliasFormula = args.get(1).substring( args.get(1).indexOf("=") + 1);
	
					try{
						if(aliasMap.containsKey(aliasName))
						{
							Logger.warn(
									"WARNING: Alias '" +
									aliasName + "' already defined, new definition ignored. ");
						}
						else
						{
							aliasMap.put(aliasName, aliasFormula);
							writeToAliasFile();
						}
					}catch(NullPointerException e){}
				}else{
					Logger.error(args.get(1) + " is an invalid alias");
				}
			}				
		});
		dataMap.put("animatedGIF".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
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
					
				}catch (NullPointerException e){}
			}				
		});
		
		dataMap.put("avi".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				//assume that the AVI should be the same dimensions as the plot displayed.
				try{
					Plot plot = plotMap.get(curView);
					PlotAnimator animator = new PlotAnimator((TimeAnimatablePlot)plot);
					DataFrame frame = plot.getData().get(0);
					Axes<DataFrameAxis> axes = frame.getAxes();

					animator.start(axes.getTimeAxis().getOrigin(), 
							axes.getTimeAxis().getExtent()-1, null, null, new File(args.get(1)));
					
				}catch (NullPointerException e){}
			}				
		});
		
		dataMap.put("closeWindow".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
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
				try{
					config = new PlotConfiguration(new File(args.get(1)));
					vConfig = new VertCrossPlotConfiguration(new PlotConfiguration(new File(args.get(1))));
//					vectorConfig = new VectorPlotConfiguration(new PlotConfiguration(new File(args.get(1))));
//					config.setConfigFileName(args.get(1));
					configFile = args.get(1);
				}
				catch(IOException e){}
				catch(NullPointerException e){}
			}				
		});
		dataMap.put("drawGridLines".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				
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
//					vectorConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, show);
					
					showGridLines = show;
				}
				catch(NullPointerException e){}
			}
		});
		
		dataMap.put("drawDomainTicks".toUpperCase(), new CommandScript(){
		public void run(ArrayList<String> args){
			
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
					
//				vectorConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, show);
				
				showDomainTicks = show;

			}catch(NullPointerException e){}
		
		}				
	});
		dataMap.put("drawRangeTicks".toUpperCase(), new CommandScript(){
		public void run(ArrayList<String> args){

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
					
//				vectorConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, show);
				
				showRangeTicks = show;
				
			}catch(NullPointerException e){}
		}				
	});
		dataMap.put("drawLegendTicks".toUpperCase(), new CommandScript(){
		public void run(ArrayList<String> args){

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
					
//				vectorConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, show);
				
				 showLegendTicks = show;
				
			}catch(NullPointerException e){}
		}				
	});
		dataMap.put( "fulldomain".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
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
								plot.configure(new TilePlotConfiguration(config), Plot.ConfigSoure.FILE);

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
					
					//only reset the configuration if we are not using a 
					//configuration file
					if(configFile == null)
					{	
						config = new PlotConfiguration();
						vConfig = new VertCrossPlotConfiguration();
//						vectorConfig = new VectorPlotConfiguration();
						
						if(cmap != null)
						{
							config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
							vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
//							vectorConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
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
//						vectorConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
					
						config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
//						vectorConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						
						config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showLegendTicks);
//						vectorConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						
						config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
						vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
//						vectorConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
					}
				}catch(NullPointerException e){}
				
			}				
		});
		dataMap.put("gtype".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				//should be tile|line|bar|contour

				Plot plot = null;
				try{
					if(args.get(1).equalsIgnoreCase("tile") ||
							args.get(1).equalsIgnoreCase("fasttile"))
					{
//						plot = new DefaultPlotCreator(verdiApp, Formula.Type.TILE, config).createPlot();
//						
//						DataFrame frame = plot.getData().get(0);
//						Axes<DataFrameAxis> axes = frame.getAxes();
//						
//						//assume the user is passing in the actual number of the 
//						//timestep
//						((TilePlot)plot).updateTimeStep(selectedTimeStep - axes.getTimeAxis().getOrigin());
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
					          plot.configure(new TilePlotConfiguration(config), Plot.ConfigSoure.FILE);
					          
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
					
					//only reset the configuration if we are not using a 
					//configuration file
					if(configFile == null)
					{
						config = new PlotConfiguration();
						vConfig = new VertCrossPlotConfiguration();
//						vectorConfig = new VectorPlotConfiguration();
						
						if(cmap != null)
						{
							config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
							vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
//							vectorConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
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
//						vectorConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
					
						config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
//						vectorConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						
						config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showLegendTicks);
//						vectorConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						
						config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
						vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
//						vectorConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
					}
				}catch(NullPointerException e){}
				
			}				
		});
		dataMap.put("help".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug(HELPTEXT);
				System.exit(0);
			}				
		});
		dataMap.put("fullhelp".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug(HELPTEXT);
				System.exit(0);
			}				
		});
		dataMap.put("usage".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
				Logger.debug(HELPTEXT);
				System.exit(0);
			}				
		});
		dataMap.put("legendBins".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
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
										// TODO Auto-generated catch block
										e.printStackTrace();
									} catch (Exception e) {
										// TODO Auto-generated catch block
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
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						
						if(cmap!= null)
						{
							config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
							vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
//							vectorConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
						}
						
					}catch (NullPointerException e){}
				}				
			});
		dataMap.put("level".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{
						levelMin = Integer.parseInt(args.get(1));
						levelMax = Integer.parseInt(args.get(1));
					}catch(NumberFormatException e){}
					catch(NullPointerException e){}
					
					FormulaListModel flm = verdiApp.getProject().getFormulas();
					for(int i = 0; i < flm.getSize(); i++)
					{
						try{
							FormulaListElement obj = (FormulaListElement)flm.getElementAt(i);
							obj.setLayerMax(Integer.parseInt(args.get(1)));
							obj.setLayerMin(Integer.parseInt(args.get(1)));
							obj.setLayerUsed(true);
						}catch(NumberFormatException e){}
						catch(NullPointerException e){}
					}
				}				
			});
		dataMap.put("levelRange".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					FormulaListModel flm = verdiApp.getProject().getFormulas();
					try{
						levelMin = Integer.parseInt(args.get(1));
						levelMax = Integer.parseInt(args.get(2));
					}catch(NumberFormatException e){}
					catch(NullPointerException e){}
					
					for(int i = 0; i < flm.getSize(); i++)
					{
						try{
							FormulaListElement obj = (FormulaListElement)flm.getElementAt(i);
							obj.setLayerMax(Integer.parseInt(args.get(1)));
							obj.setLayerMin(Integer.parseInt(args.get(2)));
							obj.setLayerUsed(true);
						}catch(NumberFormatException e){}
						catch(NullPointerException e){}
					}
				}				
			});
		dataMap.put("mapName".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					
					mapNames.add(args.get(1));
					

////					Plot plot = plotMap.get(curView);
////					DataFrame frame = plot.getData().get(0);
////					AddLayerWizard wizard = new AddLayerWizard();
////					
////					MapContext context;
////					context = verdiApp.getDomainPanelContext();
////					
////					MapLayer layer = plot.get//wizard.display((JFrame)dialog.getParent());
////
////					context.addLayer(layer);
////					
////					if (layer != null) {
////						((DefaultListModel) layerList.getModel()).add(0, layer);
////						addLayers.add(layer);
////						layerList.setSelectedIndex(0);
////						layerList.scrollRectToVisible(layerList.getCellBounds(0, 0));
////					}
//					File shpFile = new File("C:\\Documents and Settings\\wagner\\workspace\\VERDI\\pave_bootstrap\\data\\lakes.shp");
////					if (modelShpFile == null
////							|| (modelShpFile != null && !shpFile.equals(modelShpFile))) {
//						// create a new default map layer from the shape file.
//						try {
//							URL url = shpFile.toURL();
//							Map<String, Serializable> params = new HashMap<String, Serializable>();
//							params.put(IndexedShapefileDataStoreFactory.URLP.key, url);
//							params.put(
//											IndexedShapefileDataStoreFactory.CREATE_SPATIAL_INDEX.key,
//											true);
//							params.put(IndexedShapefileDataStoreFactory.URLP.key, url);
//							params.put("wkb enabled", "true");
//							params.put("loose bbox", "true");
//							IndexedShapefileDataStoreFactory dsfac = new IndexedShapefileDataStoreFactory();
//							DataStore datastore = dsfac.createDataStore(params);
//							datastore.getFeatureReader(new DefaultQuery(datastore
//									.getTypeNames()[0], Filter.NONE, new String[0]),
//									Transaction.AUTO_COMMIT);
//							FeatureSource fc = datastore.getFeatureSource(datastore
//									.getTypeNames()[0]);
//							Class geomtype = fc.getSchema().getDefaultGeometry().getType();
//							StyleBuilder builder = new StyleBuilder();
//							Style style = null;
//							if (geomtype.equals(com.vividsolutions.jts.geom.Point.class)
//									|| geomtype.equals(MultiPoint.class)) {
//								style = builder.createStyle(builder
//										.createPointSymbolizer(builder.createGraphic(null,
//												builder.createMark("square"), null)));
//							} else if (geomtype.equals(LineString.class)
//									|| geomtype.equals(MultiLineString.class)) {
//								style = builder.createStyle(builder.createLineSymbolizer());
//							} else {
//								style = builder.createStyle(builder
//										.createPolygonSymbolizer());
//							}
//							MapLayer layer = new DefaultMapLayer(fc, style);
//							MapContext context;
//							context = verdiApp.getDomainPanelContext();
//							context.addLayer(layer);
////							model.setLayer(layer);
////							model.setShpFile(shpFile);
//
//						} catch (MalformedURLException e) {
//							//msg.error("Error creating layer from shapefile", e);
//						} catch (IOException e) {
////							msg.error("Error creating layer from shapefile", e);
//						}
//					}
				}				
			});
//		dataMap.put("multitime".toUpperCase(), new CommandScript(){
//				public void run(ArrayList<String> args){
//					try{
//						int numFormulas = Integer.parseInt(args.get(1));
//						MultiTimeSeriesPlotRequest plotRequest = new MultiTimeSeriesPlotRequest("Time Series Line");
//
//						for(int i = 0; i < numFormulas; i++)
//						{
//							String formula = args.get(i + 2);
//						
//							//see if this is an alias, if it is, we need to convert it
//							if(aliasMap.containsKey(formula))
//							{
//								DatasetListModel dlm = verdiApp.getProject().getDatasets();
//								DatasetListElement dle = 
//									(DatasetListElement)dlm.getElementAt(dlm.getSize() - 1);
//								formula = convertFormula(aliasMap.get(formula), dle.getDataset().getAlias());
//							}
//							
//							FormulaListElement e = verdiApp.create(formula);
//							
//							verdiApp.getProject().getFormulas().addFormula(e);
//							verdiApp.getProject().setSelectedFormula(e);
//
//							DataFrame frame = verdiApp.evaluateFormula(Formula.Type.TIME_SERIES_LINE);
//							plotRequest.addItem(frame, false);
//						}
//						plotRequest.doCreatePlot();
//					}catch(NullPointerException e){}
//
//				}				
//			});				
		dataMap.put("openProject".toUpperCase(), new CommandScript(){
		public void run(ArrayList<String> args){
				try{
					verdiApp.openProject(new File(args.get(1)));
				}catch(NullPointerException e){}	
			}
		});
		dataMap.put("printAlias".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					
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
					System.exit(0);
				}				
			});
		dataMap.put("exit".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					System.exit(0);
				}				
			});
		dataMap.put("raiseWindow".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{

						verdiApp.getGui().getViewManager().getDockable(args.get(1)).toFront();
						curView = args.get(1);
					}catch(NullPointerException e){}
				}				
			});
		dataMap.put("save2ascii".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){

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
						
					}catch(IOException e){}
					 
				}				
			});
		dataMap.put("saveImage".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){

				Plot plot = plotMap.get(curView);
				
				try{
					plot.exportImage(args.get(1), new File(args.get(2)), 800, 600);
				}
				catch(NullPointerException e){}
				catch(IOException e){}
			}				
		});
		dataMap.put("scatter".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
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

					}catch(NullPointerException e){}
				}				
			});
//		dataMap.put("showWindow".toUpperCase(), new CommandScript(){
//				public void run(ArrayList<String> args){
//					try{
//						verdiApp.getGui().getViewManager().getDockable(args.get(1)).toFront();
//						curView = args.get(1);
//						Plot plot = plotMap.get(curView);
//						
//						DataFrame frame = plot.getData().get(0);
//						Axes<DataFrameAxis> axes = frame.getAxes();
//						
//						//assume the user is passing in the actual number of the 
//						//timestep
//						((TilePlot)plot).updateTimeStep(
//								Integer.parseInt(args.get(2)) - axes.getTimeAxis().getOrigin());
//					}catch(NullPointerException e){}
//				}				
//			});
		dataMap.put("subDomain".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					
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
					}catch(NullPointerException e){}
					
				}				
			});
		dataMap.put("subTitle1".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{
						config.setSubtitle1(args.get(1));
						vConfig.setSubtitle1(args.get(1));
//						vectorConfig.setSubtitle1(args.get(1));
						subtitle1 = args.get(1);
												
					}catch(NullPointerException e) {}
				}				
			});
		dataMap.put("subTitle2".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{
						config.setSubtitle2(args.get(1));
						vConfig.setSubtitle2(args.get(1));
//						vectorConfig.setSubtitle2(args.get(1));
						subtitle2 = args.get(1);
					}catch(NullPointerException e) {}
				}				
			});
		dataMap.put("subTitleFont".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{
						config.putObject(PlotConfiguration.SUBTITLE_1_FONT, 
								new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
						config.putObject(PlotConfiguration.SUBTITLE_2_FONT, 
								new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
						
						vConfig.putObject(PlotConfiguration.SUBTITLE_1_FONT, 
								new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
						vConfig.putObject(PlotConfiguration.SUBTITLE_2_FONT, 
								new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
						
//						vectorConfig.putObject(PlotConfiguration.SUBTITLE_1_FONT, 
//								new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
//						vectorConfig.putObject(PlotConfiguration.SUBTITLE_2_FONT, 
//								new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
					}catch(NullPointerException e){}
				}				
			});
		dataMap.put("system".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					String commandStr = args.get(1);
//					boolean successfullyScheduled = true;
			    	Process process = null;
					//  Execute command as a sub-process
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
			      		} catch(InterruptedException e) {}
			      	} catch(IOException e) {}
				}				
			});
		dataMap.put("tfinal".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{
						tfinal = Integer.parseInt(args.get(1));
					}catch(NumberFormatException e){}
					
					FormulaListModel flm = verdiApp.getProject().getFormulas();
					for(int i = 0; i < flm.getSize(); i++)
					{
						try{
							
							FormulaListElement obj = (FormulaListElement)flm.getElementAt(i);
							obj.setTimeMax(Integer.parseInt(args.get(1)));
							obj.setTimeUsed(true);
						}catch(NumberFormatException e){}
						catch(NullPointerException e){}
					}
				}				
			});
		dataMap.put("tinit".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					FormulaListModel flm = verdiApp.getProject().getFormulas();
					try{
						tinit = Integer.parseInt(args.get(1));
						selectedTimeStep = tinit;
					}catch(NumberFormatException e){}
					
					for(int i = 0; i < flm.getSize(); i++)
					{
						try{
							FormulaListElement obj = (FormulaListElement)flm.getElementAt(i);
							obj.setTimeMin(Integer.parseInt(args.get(1)));
							obj.setTimeUsed(true);
						}catch(NumberFormatException e){}
						catch(NullPointerException e){}
					}
				}				
			});
		dataMap.put("titleFont".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{
						config.putObject(PlotConfiguration.TITLE_FONT, new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
						vConfig.putObject(PlotConfiguration.TITLE_FONT, new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
//						vectorConfig.putObject(PlotConfiguration.TITLE_FONT, new Font("SansSerif", Font.PLAIN, Integer.parseInt(args.get(1))));
					}catch(NullPointerException e){}
				}				
			});
		dataMap.put("titleString".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{
						config.setTitle(args.get(1));
						vConfig.setTitle(args.get(1));
//						vectorConfig.setTitle(args.get(1));
						title = args.get(1);
					}catch(NullPointerException e) {}
				}				
			});
		dataMap.put("ts".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{
						selectedTimeStep = Integer.parseInt(args.get(1));
					}catch(NullPointerException e){}
				}				
			});
		dataMap.put("quicktime".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
//					assume that the movie should be the same dimensions as the plot displayed.
					try{
						Plot plot = plotMap.get(curView);
						PlotAnimator animator = new PlotAnimator((TimeAnimatablePlot)plot);
						DataFrame frame = plot.getData().get(0);
						Axes<DataFrameAxis> axes = frame.getAxes();
					
						animator.start(axes.getTimeAxis().getOrigin(), axes.getTimeAxis().getExtent()-1, new File(args.get(1)), null, null);
					}catch (NullPointerException e){}
				}
		});
		dataMap.put("unalias".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{
						aliasMap.remove(args.get(1));
						writeToAliasFile();
					}catch(NullPointerException e){}

				}				
			});
		dataMap.put("unitString".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					try{
						config.setUnits(args.get(1));
						vConfig.setUnits(args.get(1));
//						vectorConfig.setUnits(args.get(1));
						units = args.get(1);
					}catch(NullPointerException e) {}
				}				
			});
//		dataMap.put("vectobs".toUpperCase(), new CommandScript(){
//				public void run(ArrayList<String> args){
//
//				}				
//			});
		dataMap.put("vector".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					
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
							
						// create a vector plot with no tile
//						vectorConfig.setVectorsComponents(u, v);
//						Plot plot = new VectorPlotCreator(verdiApp, vectorConfig).createPlot();
						
						List<String> viewList = verdiApp.getGui().getViewList();
						curView = viewList.get(viewList.size() - 1);
//						plotMap.put(curView, plot);
						
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
//							vectorConfig = new VectorPlotConfiguration();
							vConfig = new VertCrossPlotConfiguration();
							config = new PlotConfiguration();
							
							if(cmap != null)
							{
								config.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
								vConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
//								vectorConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
							}
							
							config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
							vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
//							vectorConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
						
							config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
							vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
//							vectorConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
							
							config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
							vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showLegendTicks);
//							vectorConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
							
							config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
							vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
//							vectorConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
						}
						
					}catch(NullPointerException e){}
						
				}				
			});
		dataMap.put("vectorTile".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){

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
												
						// create a vector plot with no tile
//						vectorConfig.setVectorsComponents(u, v, form);
//						Plot plot = new VectorPlotCreator(verdiApp, vectorConfig).createPlot();
						
						List<String> viewList = verdiApp.getGui().getViewList();
						curView = viewList.get(viewList.size() - 1);
//						plotMap.put(curView, plot);
						
						if(configFile == null)
						{
							//reset the subtitles, titles and units
//							vectorConfig = new VectorPlotConfiguration();
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
//								vectorConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
							}
							
							config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
							vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
//							vectorConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
						
							config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
							vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
//							vectorConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
							
							config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
							vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showLegendTicks);
//							vectorConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
							
							config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
							vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
//							vectorConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
						}
												
					}catch(NullPointerException e){}
				}				
			});
		dataMap.put("version".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
					Logger.debug(version);
					System.exit(0);
				}				
			});
		
		dataMap.put("verticalCrossPlot".toUpperCase(), new CommandScript(){
			public void run(ArrayList<String> args){
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
					
					//assume the user is passing in the actual number of the 
					//timestep
					((VerticalCrossSectionPlot)plot).updateTimeStep(selectedTimeStep - axes.getTimeAxis().getOrigin());
					
					List<String> viewList = verdiApp.getGui().getViewList();
					curView = viewList.get(viewList.size() - 1);
					plotMap.put(curView, plot);
					
					if(configFile == null)
					{
						//reset the subtitles, titles and units
//						vectorConfig = new VectorPlotConfiguration();
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
//							vectorConfig.putObject(TilePlotConfiguration.COLOR_MAP, cmap);
						}
						
						config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
						vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
//						vectorConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
					
						config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
//						vectorConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
						
						config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showLegendTicks);
//						vectorConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
						
						config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
						vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
//						vectorConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
					}
				}catch(NullPointerException e){}
			}				
		});
		dataMap.put("windowid".toUpperCase(), new CommandScript(){
				public void run(ArrayList<String> args){
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
			
			
			//now that all the work has been done to create plots, lets see if we can apply some map layers to these plots
			//NOTE:  the map layers are only applicable to the areal interp and fast tile plots!
			
	        for (Map.Entry<String, Plot> entry : plotMap.entrySet())
	        {
	        	Plot plot = entry.getValue();
				Formula.Type type = plot.getType();
				
				if (type == Formula.Type.TILE && mapNames.size() > 0) {
					
			          MapLines mapLine = null;
						try {
							for (String mapNameFileURL : mapNames) {
								mapLine = new MapLines(mapNameFileURL);
								((anl.verdi.plot.gui.FastTilePlot)plot).setLayerMapLine(mapLine);
							}
						//suppress all issues (i.e., wrong file path or wrong format)
						} catch (FileNotFoundException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							//e.printStackTrace();
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
		config = new PlotConfiguration();
		vConfig = new VertCrossPlotConfiguration();
//		vectorConfig = new VectorPlotConfiguration();
		
		if(configFile != null)
		{
			try{
				config = new PlotConfiguration(new File(configFile));
				vConfig = new VertCrossPlotConfiguration(new PlotConfiguration(new File(configFile)));
//				vectorConfig = new VectorPlotConfiguration(new PlotConfiguration(new File(configFile)));
			}catch(IOException e){}
		}
		else{
			config.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
			vConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
//			vectorConfig.putObject(PlotConfiguration.UNITS_SHOW_TICK, showLegendTicks);
		
			config.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
			vConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
//			vectorConfig.putObject(PlotConfiguration.DOMAIN_SHOW_TICK, showDomainTicks);
			
			config.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
			vConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showLegendTicks);
//			vectorConfig.putObject(PlotConfiguration.RANGE_SHOW_TICK, showRangeTicks);
			
			config.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
			vConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
//			vectorConfig.putObject(TilePlotConfiguration.SHOW_GRID_LINES, showGridLines);
	
	
			if(titleSize != -1)
			{
				config.putObject(PlotConfiguration.TITLE_FONT, new Font("SansSerif", Font.PLAIN, titleSize));
				vConfig.putObject(PlotConfiguration.TITLE_FONT, new Font("SansSerif", Font.PLAIN, titleSize));
//				vectorConfig.putObject(PlotConfiguration.TITLE_FONT, new Font("SansSerif", Font.PLAIN, titleSize));
			}
			if(subtitle1Size != -1)
			{
				config.putObject(PlotConfiguration.SUBTITLE_1_FONT, 
						new Font("SansSerif", Font.PLAIN, subtitle1Size));
				vConfig.putObject(PlotConfiguration.SUBTITLE_1_FONT, new Font("SansSerif", Font.PLAIN, subtitle1Size));
//				vectorConfig.putObject(PlotConfiguration.SUBTITLE_1_FONT, new Font("SansSerif", Font.PLAIN, subtitle1Size));
			}
			if(subtitle2Size != -1)
			{
				config.putObject(PlotConfiguration.SUBTITLE_2_FONT, 
						new Font("SansSerif", Font.PLAIN, subtitle2Size));
				vConfig.putObject(PlotConfiguration.SUBTITLE_2_FONT, new Font("SansSerif", Font.PLAIN, subtitle2Size));
//				vectorConfig.putObject(PlotConfiguration.SUBTITLE_2_FONT, new Font("SansSerif", Font.PLAIN, subtitle2Size));
			}
			
	
			config.setUnits(units);
			vConfig.setUnits(units);
//			vectorConfig.setUnits(units);
	
			config.setTitle(title);
			vConfig.setTitle(title);
//			vectorConfig.setTitle(title);
	
			config.setSubtitle2(subtitle2);
			vConfig.setSubtitle2(subtitle2);
//			vectorConfig.setSubtitle2(subtitle2);
	
			config.setSubtitle1(subtitle1);
			vConfig.setSubtitle1(subtitle1);
//			vectorConfig.setSubtitle1(subtitle1);
		}
	}
	
	private static void loadAliasFile()
	{
		
//		String aliasFile = System.getProperty("user.home") + ".verdi.alias";
////		FileOutputStream out;// = new FileOutputStream(aliasFile); // declare a file output object
//		
//		 BufferedWriter out; 
//	        
//					
//			//Load the file userFile
//			try{
//				out = new BufferedWriter(new FileWriter("aliasFile"));
//				
//				Iterator it = aliasMap.keySet().iterator();
//				while(it.hasNext())
//				{
//					String key = (String)it.next();
//					String aliasFormula = (String)aliasMap.get(key);
//					out.write(key + " " + aliasFormula);
//			        out.close();
//				}
//	               	 
//	        }catch(IOException i){
//	             //System.exit(-1);
//	        	Logger.error("File Not Found");
//	       }
		
		 try {
		        BufferedReader in = new BufferedReader(new FileReader(aliasFileName));
		        String str;
		        while ((str = in.readLine()) != null) {
		            String[] aliasitem = str.split(" ");
		            aliasMap.put(aliasitem[0], aliasitem[1]);
		        }
		        in.close();
		    } catch (IOException e) {
		    }

	}
		
		private static void writeToAliasFile()
		{
//			String aliasFile = System.getProperty("user.home") + "verdi.alias";
//			String aliasFile = "C:\\verdi.alias.txt";
			//BufferedWriter out; 
			
				//Load the file userFile
				try{
			 		FileWriter outFile = new FileWriter(aliasFileName);
			 		PrintWriter out = new PrintWriter(outFile);
					//out = new BufferedWriter(new FileWriter("aliasFile"));
					Iterator it = aliasMap.keySet().iterator();
					while(it.hasNext())
					{
						Logger.debug("Printing file");
						String key = (String)it.next();
						String aliasFormula = (String)aliasMap.get(key);
						
						out.println(key + " " + aliasFormula);
//						out.write(key + " " + aliasFormula);
				      
					}
					out.close();
		               	 
		        }catch(IOException i){
		        	Logger.error("File Not Found");
		        }
		}
}
