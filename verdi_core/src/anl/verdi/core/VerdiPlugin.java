package anl.verdi.core;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.apache.velocity.app.Velocity;
import org.java.plugin.Plugin;
import org.java.plugin.PluginLifecycleException;

import saf.core.runtime.IApplicationRunnable;
import saf.core.ui.GUICreator;
import saf.core.ui.IAppConfigurator;
import saf.core.ui.ISAFDisplay;
import saf.core.ui.Workspace;
import simphony.util.messages.MessageCenter;
import anl.verdi.commandline.BatchScriptHandler;
import anl.verdi.commandline.ScriptHandler;
import anl.verdi.data.DataManager;
import anl.verdi.util.Tools;

/**
 * As an implementor of IApplicationRunnable, this is the "Main"
 * class for Verdi. {link #run(String[]) run} takes the place of
 * the main method in this case.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */

/* MAIN class of VERDI */

public class VerdiPlugin extends Plugin implements IApplicationRunnable {

	// this makes the application menu etc. appear correctly on Mac OS X
	static {
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "VERDI");
	}

	static final Logger logger = LogManager.getLogger(VerdiPlugin.class.getName());	// 2014
	
	protected void doStart() throws Exception {
	}

	protected void doStop() throws Exception {
	}

	public void run(String[] args) {
		IAppConfigurator configurator = null;

		// 2014 from log4j/2.x/manual/configuration.html example
		logger.trace("Entering application at VerdiPlugin.run");
		try {
			logger.trace("in try block of run");
			// initialize velocity
			Properties p = new Properties();
			p.setProperty("resource.loader", "class");
			p.setProperty("class.resource.loader.class",
					"org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");

			Velocity.init(p);
			logger.trace("just called Velocity.init for p");
			Properties pFRL = new Properties();		// 2014 added all pertaining to pFRL
			pFRL.setProperty("resource.loader", "file");
			pFRL.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
			pFRL.setProperty("runtime.log", System.getProperty("user.home") + '/' + "verdi"
					+ '/' + "velocity.log");
			Velocity.init(pFRL);
			logger.trace("just called Velocity.init for pFRL");
			try {
				File file = new File(Tools.getPropertyFile());
				logger.trace("property file = " + Tools.getPropertyFile());
				if (file.exists()) 
					{
						System.getProperties().load(new FileInputStream(file));
						logger.debug("property file exists, found, and loaded");
					}
				else
				{
					logger.error("property file does not exist");
				}
			} catch (Exception e1) {
				MessageCenter.getMessageCenter(getClass()).error("Error", e1);
			}

			DataManager manager = new DataManager(getManager());

			//			if ( 1==2 ) {
			//				System.out.println("===============================================");
			//				System.out.println("VerdiPlugin.");
			//				System.out.println("===============================================");
			//			}
			// for non gui run we would process the args here.

			// assuming gui app for now.
			// The typical pattern for a SAF application is followed below.
			boolean batchmode = false;
			VerdiApplication verdi = new VerdiApplication(manager);

			if (args.length > 0 && args[0].toLowerCase().startsWith("-b")) {
				batchmode = true;
				verdi.setSkipSplash(batchmode);
			}

			if (args.length > 0 && (args[0].toLowerCase().startsWith("-help") || args[0].toLowerCase().startsWith("-version")) ) {
				verdi.setSkipSplash(true);   //NOTE: just to skip the splash window if not running in real batch mode
			}

			// for debugging verdi.setSkipSplash(true);

			// BUG3611 - need to do initialization w/o the GUI
			configurator = new VerdiAppConfigurator(verdi);
			Workspace<VerdiApplication> workspace = new Workspace<VerdiApplication>(verdi);
			ISAFDisplay display = GUICreator.createDisplay(configurator, workspace);

			if (batchmode) {
				BatchScriptHandler bHandler = new BatchScriptHandler(args, verdi, true);
				try {
					bHandler.run();
					System.exit(0);
				} catch (Throwable e) {
					e.printStackTrace();
					System.exit(1);
				}
			}


			//			if ( 1==2 ) {
			//				System.out.println("===============================================");
			//				System.out.println("VerdiPlugin - after batch mode.");
			//				System.out.println("===============================================");
			//			}

			//			configurator = new VerdiAppConfigurator(verdi);
			//			Workspace<VerdiApplication> workspace = new Workspace<VerdiApplication>(verdi);
			//			ISAFDisplay display = GUICreator.createDisplay(configurator, workspace);

			if(args.length > 0) {
				ScriptHandler sHandler = new ScriptHandler(args, verdi);
				sHandler.run();
			}
			GUICreator.runDisplay(configurator, display);

		} catch (PluginLifecycleException e) {
			MessageCenter.getMessageCenter(getClass()).error("Error while loading core VERDI plugin", e);
		} catch (Exception ex) {
			if (configurator != null) ((VerdiAppConfigurator)configurator).closeSplash();
			MessageCenter.getMessageCenter(getClass()).error("Error", ex);
		}
	}

	//	// example of how to automate verdi functionality
	//	// the idea being that processing cmd line options would lead to
	//	// something like this
	//	private void automateExample(VerdiApplication verdi) throws IOException {
	//		//verdi.loadDataset(new File[]{new File("C:\\src\\pave2\\data\\pave_example_data\\RADM_CONC_1")});
	//		/*
	//		verdi.loadDataset(new File[]{new File("e:/VERDI data/CCTM_CB05_A.200107.combine.conc")});
	//		verdi.loadDataset(new File[]{new File("e:/VERDI data/CCTM_CB4_A.200107.combine.conc")});
	//		verdi.loadDataset(new File[]{new File("e:/VERDI data/2001ah_k4a_eus12b.12km.std.20010101.conc")});
	//		*/
	//		try {
	//			verdi.loadDataset(new File[]{new File("e:/VERDI data/METDOT3D_010724")});
	//		} catch (Exception e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		try {
	//			verdi.loadDataset(new File[]{new File("C:\\src\\pave2\\data\\pave_example_data\\RADM_CONC_1")});
	//		} catch (Exception e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		FormulaListElement uWind = verdi.create("UWIND[1]");
	//		verdi.getProject().getFormulas().addFormula(uWind);
	//		FormulaListElement vWind = verdi.create("VWIND[1]");
	//		verdi.getProject().getFormulas().addFormula(vWind);
	//		FormulaListElement o3 = verdi.create("O3[2]");
	//		verdi.getProject().getFormulas().addFormula(o3);
	//		o3.setXMin(10);
	//		o3.setYMin(12);
	//		o3.setXMax(20);
	//		o3.setYMax(28);
	//		o3.setXYUsed(true);
	//		o3.setTimeMin(2);
	//		o3.setTimeMax(3);
	//		//o3.setTimeUsed(true);
	//
	//		verdi.getProject().setSelectedFormula(o3);
	//		//Plot plot = new DefaultPlotCreator(verdi, Formula.Type.TIME_SERIES_LINE, new TilePlotConfiguration()).createPlot();
	//		VertCrossPlotConfiguration vConfig = new VertCrossPlotConfiguration();
	//		vConfig.setCrossSectionType(VerticalCrossSectionPlot.CrossSectionType.Y);
	//		vConfig.setCrossSectionRowCol(15);
	//		Plot plot = new VerticalCrossPlotCreator(verdi, vConfig).createPlot();
	//		Save2Ascii saver = new Save2Ascii(plot);
	//		saver.save(new File("c:/plot.csv"));
	//
	//		VectorPlotConfiguration config = new VectorPlotConfiguration();
	//		config.setTitle("My Plot");
	//		// create a vector plot with no tile
	//		config.setVectorsComponents(uWind, vWind);
	//		//new VectorPlotCreator(verdi, config).createPlot();
	//
	//		config = new VectorPlotConfiguration();
	//		// create a vector plot with a background tile plot
	//		config.setVectorsComponents(uWind, vWind, vWind);
	//		//new VectorPlotCreator(verdi, config).createPlot();
	//
	//		/*
	//		TilePlotConfiguration contourConfig = new TilePlotConfiguration();
	//		contourConfig.setTitle("My Plot");
	//		contourConfig.setSubtitle1("A Subtitle");
	//		contourConfig.putObject(PlotConfiguration.TITLE_COLOR, Color.GREEN);
	//		verdi.getProject().setSelectedFormula(o3);
	//		new DefaultPlotCreator(verdi, Formula.Type.CONTOUR, contourConfig).createPlot();
	//		*/
	//
	//		/*
	//		//element.setTimeMin(10);
	//		//element.setTimeMax(44);
	//		element.setTimeUsed(true);
	//
	//		verdi.getProject().getFormulas().addFormula(element);
	//		verdi.getProject().setSelectedFormula(element);
	//		TilePlotConfiguration config = new TilePlotConfiguration();
	//		config.setTitle("My Tile Plot");
	//		Plot plot = new DefaultPlotCreator(verdi, Formula.Type.TILE, config).createPlot();
	//
	//		element = verdi.create("O3[2]");
	//		verdi.getProject().getFormulas().addFormula(element);
	//		verdi.getProject().setSelectedFormula(element);
	//		plot = new DefaultPlotCreator(verdi, Formula.Type.TILE, config).createPlot();
	//
	//		element = verdi.create("O3[3]");
	//		verdi.getProject().getFormulas().addFormula(element);
	//		verdi.getProject().setSelectedFormula(element);
	//		plot = new DefaultPlotCreator(verdi, Formula.Type.TILE, config).createPlot();
	//
	//		*/
	//		//Plot plot = new DefaultPlotCreator(verdi, Formula.Type.TIME_SERIES_LINE, config).createPlot();
	//		/*
	//		VertCrossPlotConfiguration vConfig = new VertCrossPlotConfiguration();
	//		vConfig.setCrossSectionType(VerticalCrossSectionPlot.CrossSectionType.X);
	//		vConfig.setCrossSectionRowCol(18);
	//		vConfig.setTitle("My Vert Cross");
	//		Plot plot = new VerticalCrossPlotCreator(verdi, vConfig).createPlot();
	//		//plot.exportImage(PlotExporter.PNG, new File("c:/src/pave2/sample.png"), 800, 600);
	//		//PlotAnimator animator = new PlotAnimator((TimeAnimatablePlot)plot, 400, 500);
	//		//animator.start(0, 10, null, new File("c:/src/pave2/plot.gif"));
	//		*/
	//	}


}
