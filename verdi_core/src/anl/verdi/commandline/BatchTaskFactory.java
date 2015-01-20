package anl.verdi.commandline;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.commandline.task.TilePlotTask;
import anl.verdi.commandline.task.TimeSeriesPlotTask;
//import anl.verdi.commandline.task.VectorPlotTask;		// 2014 removed old Vector Plot
import anl.verdi.core.VerdiApplication;
import anl.verdi.core.VerdiConstants;
import anl.verdi.formula.Formula;
import anl.verdi.util.VerdiFileNameFilter;

public class BatchTaskFactory {
	static final Logger Logger = LogManager.getLogger(BatchTaskFactory.class.getName());

	public BatchTaskFactory() {
		//
	}
	
	public static List<AbstractTask> createTasks(Map<String, String> map, VerdiApplication app) {
		String gstring = map.get(VerdiConstants.GRAPHICS);
		String gshort = map.get(VerdiConstants.GRAPHICS_SHORT);
		String gtype = (gstring == null || gstring.trim().isEmpty() ? gshort : gstring.trim());
		
		if (gtype == null || gtype.trim().isEmpty())
			return null;
		
		String dir = map.get(VerdiConstants.DATA_DIR);
		String pattern = map.get(VerdiConstants.PATTERN);
		String f = map.get(VerdiConstants.DATA_FILE);
		String subDomain = map.get(VerdiConstants.SUBDOMAIN);
		File[] dataFiles = getDataFiles(f, dir);
		boolean useFilePattern = dataFiles.length == 0;
		File[] files = useFilePattern ? getFilesByPattern(dir, pattern) : dataFiles;
		
		if (files == null || files.length == 0)
			return null;
		
		return constructTasks(map, app, useFilePattern, gtype, files, subDomain);
	}

	private static List<AbstractTask> constructTasks(Map<String, String> map,
			VerdiApplication app, boolean usePattern, String gtype,
			File[] files, String subDomain) {
		List<AbstractTask> tasks = new ArrayList<AbstractTask>();
		List<Map<String, String>> maps = getMaps(map, files, gtype, usePattern);
		
		if (!usePattern)
			map = resetMap(map, files, gtype);
		
		if (gtype.equalsIgnoreCase(VerdiConstants.TILE_PLOT) || gtype.equalsIgnoreCase(VerdiConstants.FAST_TILE_PLOT)) {
			// here protect from NullPointerException when not using subDomain
			if(subDomain != null)	// use subDomain
			{
				Logger.debug("BatchTaskFactory.constructTasks: using subdomain for " + gtype);
				if (!usePattern)
					tasks.add(new TilePlotTask(map, files, app, subDomain.split(" ")));
				
				for (Map<String, String> m : maps)
					tasks.add(new TilePlotTask(m, new File[]{new File(m.get(VerdiConstants.DATA_FILE))}, app, subDomain.split(" ")));
			}
			else
			{	// 2014 not using subDomain
				Logger.debug("BatchTaskFactory.constructTasks: NOT using subdomain for " + gtype);
				if (!usePattern)
					tasks.add(new TilePlotTask(map, files, app));
				
				for (Map<String, String> m : maps)
					tasks.add(new TilePlotTask(m, new File[]{new File(m.get(VerdiConstants.DATA_FILE))}, app));
			}
		} else if (gtype.equalsIgnoreCase(VerdiConstants.LINE_PLOT)) {
			if (!usePattern)
				tasks.add(new TimeSeriesPlotTask(map, files, app, Formula.Type.TIME_SERIES_LINE));
			
			for (Map<String, String> m : maps)
				tasks.add(new TimeSeriesPlotTask(m, new File[]{new File(m.get(VerdiConstants.DATA_FILE))}, app, Formula.Type.TIME_SERIES_LINE));
		} else if (gtype.equalsIgnoreCase(VerdiConstants.BAR_PLOT)) {
			if (!usePattern)
				tasks.add(new TimeSeriesPlotTask(map, files, app, Formula.Type.TIME_SERIES_BAR));
			
			for (Map<String, String> m : maps)
				tasks.add(new TimeSeriesPlotTask(m, new File[]{new File(m.get(VerdiConstants.DATA_FILE))}, app, Formula.Type.TIME_SERIES_BAR));
		} else if (gtype.equalsIgnoreCase(VerdiConstants.CONTOUR_PLOT)) {
			if (!usePattern)
				tasks.add(new TimeSeriesPlotTask(map, files, app, Formula.Type.CONTOUR));
			
			for (Map<String, String> m : maps)
				tasks.add(new TimeSeriesPlotTask(m, new File[]{new File(m.get(VerdiConstants.DATA_FILE))}, app, Formula.Type.CONTOUR));
		} 
//		else if (gtype.equalsIgnoreCase(VerdiConstants.VECTOR) || gtype.equalsIgnoreCase(VerdiConstants.VECTOR_TILE)) {
//			if (!usePattern)
//				tasks.add(new VectorPlotTask(map, files, app));
//			
//			for (Map<String, String> m : maps)
//				tasks.add(new VectorPlotTask(m, new File[]{new File(m.get(VerdiConstants.DATA_FILE))}, app));
//		}	// 2014 removed old Vector Plot
		else {
			Logger.warn("Invalid plot type: " + gtype);		// 2015 warn user if invalid plot type at this point
		}
		return tasks;
	}

	private static File[] getDataFiles(String file, String dir) {
		if (file == null || file.trim().equals(""))
			return new File[0];

		String[] names = file.split(VerdiConstants.SEPARATOR);
		File[] files = new File[names.length];
		
		for (int i = 0; i < names.length; i++) {
			File temp = new File(names[i]);
			File temp2 = new File(dir, names[i]);
			
			if (temp.isAbsolute())
				files[i] = temp;
			else
				files[i] = temp2;
		}
			
		return files;
	}

	private static List<Map<String, String>> getMaps(Map<String, String> map,
			File[] dataFiles, String gtype, boolean usePattern) {
		List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
		
		if (!usePattern) 
			return maps;
			
		for (File file : dataFiles) {
			Map<String, String> m = new HashMap<String, String>();
			m.putAll(map);
			resetDataFile(file, m);
			resetImageFiles(map, gtype, file, m);
			maps.add(m);
		}
		
		return maps;
	}
	
	private static Map<String, String> resetMap(Map<String, String> map,
			File[] dataFiles, String gtype) {
		Map<String, String> m = new HashMap<String, String>();
		m.putAll(map);
		File file = (dataFiles == null || dataFiles.length == 0) ? new File(map.get(VerdiConstants.DATA_DIR), "auto_generated") : dataFiles[0];
		resetImageFiles(map, gtype, file, m);
		
		return m;
			
	}

	private static void resetImageFiles(Map<String, String> map, String gtype,
			File file, Map<String, String> m) {
		String imgDirValue = map.get(VerdiConstants.IMAGE_DIR);
		String imgFileValue = map.get(VerdiConstants.IMAGE_FILE);
		String imgFileName = file.getName() + "_" + gtype;
		File imgDir = null;
		File imgFile = null;
		
		if (imgFileValue != null && !imgFileValue.trim().isEmpty()) {
			imgFile = new File(imgFileValue);
			imgDir = (imgFile.isAbsolute()) ? imgFile.getParentFile() : new File(imgDirValue);
			imgFileName = imgFile.getName();
		}
		
		if (imgDir == null)
			imgDir = (imgDirValue != null && !imgDirValue.trim().isEmpty()) ? new File(imgDirValue) : file.getParentFile();
			
		m.remove(VerdiConstants.IMAGE_FILE);
		m.put(VerdiConstants.IMAGE_FILE, new File(imgDir, imgFileName).getAbsolutePath());
	}

	private static void resetDataFile(File file, Map<String, String> m) {
		m.remove(VerdiConstants.DATA_FILE);
		m.put(VerdiConstants.DATA_FILE, file.getAbsolutePath());
	}

	private static File[] getFilesByPattern(String dir, String pattern) {
		File dirFile = (dir == null || dir.trim().isEmpty()) ? null : new File(dir);
		
		if (dirFile == null || !dirFile.exists() || !dirFile.isDirectory())
			return new File[0];
			
		if (pattern == null || pattern.trim().isEmpty()) 
			return new File(dir).listFiles();
		
		VerdiFileNameFilter filter = new VerdiFileNameFilter(pattern);
		
		return dirFile.listFiles(filter);
	}
	
	public static void main(String[] args) {
		VerdiFileNameFilter filter = new VerdiFileNameFilter("*.[lL][oO][gG]");
		File[] files = new File(System.getProperty("user.home")).listFiles(filter);
		
		if (files != null)
			for (File file : files)
				Logger.debug(file.getAbsolutePath());
	}
	
}

