package anl.verdi.commandline;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiConstants;

/**
 * Class whose main purpose is to handle and carry out the commands received
 * from the command line
 * 
 * @author IE, UNC Chapel Hill
 */
public class BatchScriptHandler {
	static final Logger Logger = LogManager.getLogger(BatchScriptHandler.class.getName());

	static HashMap<String, CommandScript> dataMap = new HashMap<String, CommandScript>();

	protected static VerdiApplication verdiApp = null;

	private ArrayList<ArrayList<String>> commands = null;
	
	private boolean fromFile = true;
	
	private String[] scripts;

	static HashMap<String, String> aliasMap = new HashMap<String, String>();

	private static final String HELPTEXT = "\trun.bat|verdi.sh -b|-batch [[batch file path]]\n\n" +
		"\t####################################################################################\n" +
		"\t# NOTE: Batch Scripting Language                                                   #\n" +
		"\t#                                                                                  #\n" +
		"\t#    * All parameter/value pairs should be inside one of the two blocks --         #\n" +
		"\t#        <Global/> or <Task/>                                                      #\n" +
		"\t#    * Number of blocks is not limited                                             #\n" +
		"\t#    * Only one <Global/> block is recommended. <Global/> blocks should            #\n" +
		"\t#        contains different items if use multiple <Global/> blocks                 #\n" +
		"\t#    * Parameter values in <Task/> blocks will override those in <Global/>         #\n" +
		"\t#    * Currently supported parameters (keys, case insensitive):                    #\n" +
		"\t#        configFile    -- configuration file full path                             #\n" +
		"\t#        f             -- dataset file path/name                                   #\n" +
		"\t#        dir           -- dataset file folder                                      #\n" +
		"\t#        pattern       -- dataset file name pattern                                #\n" +
		"\t#        gtype         -- plot type (tile, line, bar)                              #\n" +	// 2014 removed vector
//		"\t#        vector        -- vector plot variables                                    #\n" +
//		"\t#        vectorTile    -- vector plot variables                                    #\n" +
		"\t#        s             -- variable name                                            #\n" +
		"\t#        ts            -- time step (1-based)                                      #\n" +
		"\t#        titleString   -- plot title                                               #\n" +
		"\t#[       subDomain     -- <xmin> <ymin> <xmax> <ymax>                              #\n" +	// 2014 added subdomain processing 
		"\t#        subTitle1     -- plot subtitle one                                        #\n" +
		"\t#        subTitle2     -- plot subtitle two                                        #\n" +
		"\t#        saveImage     -- image file type (png, jpeg, eps, etc.)                   #\n" +
		"\t#        imageFile     -- image file path/name                                     #\n" +
		"\t#        imageDir      -- image file folder                                        #\n" +
		"\t#        drawGridLines -- draw grid lines on the tile plot if 'yes'                #\n" +
		"\t#        imageWidth    -- image width                                              #\n" +
		"\t#        imageHeight   -- image height                                             #\n" +
		"\t#        unitString    -- units                                                    #\n" +
//		"\t#        VectorIncr    -- Vector Sampling Increment (Fast Tile Plot only)          #\n" +
		"\t#    * Currently supported formula functions:                                      #\n" +
		"\t#      max(), min(), mean(), sum() -- functions will calculate relevant max,       #\n" +
		"\t#      min, mean, sum values over the time steps (within the same layer)           #\n" +
		"\t#      for each individual grid cell in the domain.                                #\n" +
		"\t#                                                                                  #\n" +
		"\t#                                                                                  #\n" +
		"\t# Author: IE, UNC at Chapel Hill                                                   #\n" +
		"\t# Date: 12/06/2010                                                                 #\n" +
		"\t# Version: 0                                                                       #\n" +
		"\t####################################################################################\n\n";

	public BatchScriptHandler(String[] args, VerdiApplication vApp, boolean fromFile) {
		verdiApp = vApp;
		this.fromFile = fromFile;
		this.scripts = args;
		
		if (fromFile)
			commands = CommandLineParser.parseCommands(args);
	}
	
	public void run() throws Exception {
		if (!fromFile) {
			List<AbstractTask> tasks = processBatchScript(scripts);
			processTasks(tasks);
			return;
		}

		if(commands.size() > 0)	
		{
			ArrayList<String> thisCommand = commands.get(0);
			String option = thisCommand.get(0);

			if (thisCommand.size() < 2) {			// looks at size of commands.get(0) and based on that assumes user asked for help
				Logger.debug(HELPTEXT);				// 2014 prints out help to logger
				System.out.println(HELPTEXT);		// prints out help to stdout
				return;
			}

			String value = thisCommand.get(1);		// name of existing data file

			if (!(new File(value).exists()))		// checks that file exists before looking for batch designation
			{
				Logger.error("Cannot find file: " + value + ".");
				System.out.println("BatchScriptHandler: Cannot find file: " + value);
			}

			if (option.equalsIgnoreCase("-b") || option.equalsIgnoreCase("-batch")) {	// if not have existing file name but say batch, program proceeds ???
				Logger.debug("Batch processing file: " + value);
				List<AbstractTask> tasks = processBatchFile(value);
				processTasks(tasks);
				Logger.debug("Finished batch processing.");
			} else {
				Logger.error(option + " is not a valid command.");
				Logger.error("Usage: \n" + HELPTEXT);
				System.out.println("BatchScriptHandler: " + option + " is not a valid command.");
				System.out.println("Usage: \n" + HELPTEXT);
			}
		}
	}

	private void processTasks(List<AbstractTask> tasks) throws Exception {
		if (tasks.isEmpty())
		{
			Logger.error("Exception in BatchScriptHandler.processTasks: There is no task to run.");
			System.out.println("BatchScriptHandler: Exception in BatchScriptHandler.processTasks: There is no task to run.");
			throw new Exception("Error: There is no task to run.");
		}
		for (AbstractTask task : tasks) {
			try {
				if (task != null)
					task.run();		// here is how program gets to TilePlotTask to execute
			} catch (Throwable e) {
				Logger.error("Error in BatchScriptHandler.processTasks: Task is null. " + e.getMessage());
				System.out.println("BatchScriptHandler: Error in BatchScriptHandler.processTasks: Task is null. " + e.getMessage());
				e.printStackTrace();
				throw new Exception(e == null ? "Error running task: "
						+ task.toString() : e.getMessage());
			}
		}
	}

	private List<AbstractTask> processBatchFile(String batchfile) throws Exception {
		try {
			BufferedReader in = new BufferedReader(new FileReader(batchfile));
			List<String> scripts = new ArrayList<String>();

			String str;
			while ((str = in.readLine()) != null) {
				if (isComment(str))			// strip out comment lines
					continue;

				scripts.add(str);
			}

			in.close();

			return processBatchScript(scripts.toArray(new String[0]));
		} catch (IOException e) {
			Logger.error("IOException in BatchScriptHandler.processBatchFile: " + e.getMessage());
			System.out.println("BatchScriptHandler: IOException in BatchScriptHandler.processBatchFile: " + e.getMessage());
			throw e;
		}
	}
	
	private List<AbstractTask> processBatchScript(String[] scripts) throws Exception {
		try {
			List<HashMap<String, String>> maps = new ArrayList<HashMap<String, String>>();
			HashMap<String, String> global = new HashMap<String, String>();
			HashMap<String, String> map = null;
			String dataFiles = "";

			boolean processGlobal = false;
			for (String str : scripts) {
				if (isComment(str))		// if processBatchScript was called from processBatchFile then should not contain any comment lines
					continue;

				if (str.trim().equalsIgnoreCase(VerdiConstants.GLOBAL)) {	// ELSE start GLOBAL section
					processGlobal = true;
					dataFiles = "";
				} else if (str.trim().equalsIgnoreCase(VerdiConstants.END_GLOBAL)) {	// ELSE end GLOBAL section
					processGlobal = false;

					if (dataFiles != null && !dataFiles.trim().isEmpty()) 	// if have dataFiles value 
						global.put(VerdiConstants.DATA_FILE, dataFiles);			// put into HashMap global
				} else if (str.trim().equalsIgnoreCase(VerdiConstants.TASK)) {	// ELSE if not starting or ending global section, see if TASK
					dataFiles = "";												// if starting TASK, clear out String dataFiles
					map = new HashMap<String, String>();						// and define map as new HashMap
				} else if (str.trim().equalsIgnoreCase(VerdiConstants.END_TASK)) {	// ELSE if ending TASK section
					if (dataFiles != null && !dataFiles.trim().isEmpty())			// if have dataFiles value
						map.put(VerdiConstants.DATA_FILE, dataFiles);				//		store datafile name in map

					maps.add(map);													// last thing in ending task is to add map to maps
				} else																		// ELSE all other input lines
					dataFiles = populateMaps(global, map, processGlobal, str, dataFiles);	//	call populateMaps
			}										// finished processing all lines in script file

			return getAllTasks(global, maps);		// value returned from getAllTasks is return value for processBatchScript
		} catch (Exception e) {
			Logger.error("error in BatchScriptHandler.processBatchScript: " + e.getMessage());
			System.out.println("BatchScriptHandler: Error in BatchScriptHandler.processBatchScript: " + e.getMessage());
			throw e;
		}
	}

	private String populateMaps(HashMap<String, String> global,
			HashMap<String, String> map, boolean processGlobal, String str, String dataFiles) {
		if (str == null || str.trim().isEmpty())
			return dataFiles;
		
		String[] keyVal = str.split("=");
		String key = keyVal[0].trim().toUpperCase();
		String value = (keyVal.length == 2) ? keyVal[1] : "";
		
		if (key.equalsIgnoreCase(VerdiConstants.DATA_FILE)) {
			dataFiles += value + VerdiConstants.SEPARATOR;
			return dataFiles;
		}

		if (processGlobal)
			global.put(key, value);
		else
			map.put(key, value);
		
		return dataFiles;
	}

	private List<AbstractTask> getAllTasks(HashMap<String, String> global,
			List<HashMap<String, String>> maps) {
		List<HashMap<String, String>> mergedMaps = merge(global, maps);
		List<AbstractTask> tasks = new ArrayList<AbstractTask>();
		
		for (HashMap<String, String> map : mergedMaps){
			tasks.addAll(BatchTaskFactory.createTasks(map, verdiApp));
		}
		
		int i = 0;	// 2015 display values stored in list of tasks
		for (AbstractTask t : tasks)
		{
			i++;
			Logger.debug("AbstractTask #" + i + ": t = " + t.toString());
		}
	
		return tasks;
	}

	private List<HashMap<String, String>> merge(HashMap<String, String> global,
			List<HashMap<String, String>> maps) {

		if (global == null || global.isEmpty())
			return maps;

		Set<String> kset = global.keySet();

		for (HashMap<String, String> map : maps) {
			for (Iterator<String> iter = kset.iterator(); iter.hasNext();) {
				String key = iter.next();
				String value = global.get(key);

				if (!map.containsKey(key))
					map.put(key, value);
			}
		}

		return maps;
	}

	private boolean isComment(String line) {
		if (line == null)
			return true;
		
		String str = line.trim();
		return str.isEmpty() || str.startsWith("#") || str.startsWith("*");
	}

	protected static VerdiApplication getVerdiApp() {
		return verdiApp;
	}
}
