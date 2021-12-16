package anl.verdi.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

//import ucar.util.Logger;
import anl.verdi.core.VerdiPlugin;
import anl.verdi.data.DataFrame;
import anl.verdi.data.Dataset;
import anl.verdi.plot.config.PlotConfiguration;

/**
 * @author Qun He
 * @version $Revision$ $Date$
 */
public class Tools {
	public static final String VERDI_BASE = "verdi.install.home";	// 2014 to be used from config.properties
																	// if VERDI_HOME is not in the user's environment
	public static final String CONFIG_HOME = "verdi.config.home";
	public static final String DATASET_HOME = "verdi.dataset.home";
	public static final String SCRIPT_HOME = "verdi.script.home";
	public static final String PROJECT_HOME = "verdi.project.home";
	public static final String TEMP_DIR = "verdi.temporary.dir";
	public static final String REMOTE_HOSTS = "verdi.remote.hosts";
	public static final String SSH_PATH = "verdi.remote.ssh";
	public static final String REMOTE_UTIL_PATH = "remote.file.util";
	public static final String USER_HOME = "user.home";
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static final String PROPERTY_FILE = "/verdi/config.properties";
	public static final String PROPERTY_FILE_TEMP = "/verdi/config.properties.TEMPLATE";
	public static final String VERDI_HOME = "VERDI_HOME";
	static final Logger Logger = LogManager.getLogger(VerdiPlugin.class.getName());	// 2014
	
	private static String iconsDir = null;

	public static File getConfigFolder(PlotConfiguration config) {
		File prevFld = (config == null ? null : config.getPreviousFolder());

		if (prevFld == null) {
			String configFld = System.getProperty(CONFIG_HOME);

			if (configFld == null || configFld.trim().isEmpty())
				configFld = System.getProperty(USER_HOME);
			
			prevFld = new File(configFld);
		}
		
		return prevFld;
	}
	
	public static String getUserHome() {
		Logger.debug("in Tools.java, getUserHome: " + System.getProperty(USER_HOME));
		return System.getProperty(USER_HOME);
	}
	
	public static String getPropertyFile() {
		String file = getUserHome() + PROPERTY_FILE;
		String temp = getUserHome() + PROPERTY_FILE_TEMP;
		
		if (!(new File(file).exists())) {
			File tempFile = new File(temp);
			tempFile.renameTo(new File(file));
		}
		
		return file;
	}
	
	/***
	 * Return a series of dataset names delimited by ';'
	 */
	public static String getDatasetNames(DataFrame dataFrame) {
		if (dataFrame == null)
			return null;
		
		List<Dataset> datasets = dataFrame.getDataset();
		
		if (datasets.size() == 0)
			return null;
		
		int[] indices = new int[datasets.size()];
		HashMap<Integer, String> names = new HashMap<Integer, String>();
		String nameStr = "";
		
		for (int i = 0; i < datasets.size(); i++) {
			String name = datasets.get(i).getName().trim();
			int indx = name.indexOf("]");
			indices[i] = Integer.parseInt(name.substring(1, indx));
			names.put(indices[i], name.subSequence(0, ++indx) + "=" + name.substring(indx).trim());
		}
		
		Arrays.sort(indices);
		
		for(int ind : indices)
			nameStr += names.get(ind) + "; ";
		
		return nameStr.substring(0, nameStr.length() - 2);
	}
	
	public static String getIconsDir() {
		if (iconsDir == null) {
			String home = getVerdiHome();
			String icons = home + File.separator + "plugins" + File.separator + "core" + File.separator + "icons" + File.separator;
			// Standalone path
			if (new File(icons).exists()) {
				iconsDir = icons;
				return iconsDir;
			}
			icons = ".." + File.separator + "core" + File.separator + "icons" + File.separator;
			if (new File(icons).exists()) {
				iconsDir = icons;
				return iconsDir;
			}
			// Path when running from eclipse
			icons = ".." + File.separator + "verdi_core" + File.separator + "icons" + File.separator;
			if (new File(icons).exists()) {
					iconsDir = icons;
					return iconsDir;
			}
			//standalone path relative to root
			icons = "plugins" + File.separator + "core" + File.separator + "icons" + File.separator;
			iconsDir = icons;

		}
		return iconsDir;
	}
	
	// Callers should be aware of filesystem differences between Eclipse and standalone.
	public static String getVerdiHome() 
	{	// 2014
		String vHome = System.getProperty("VERDI_HOME");
		if (vHome == null || vHome.trim().equals(""))
			vHome = System.getenv("VERDI_HOME");
		if(vHome == null || vHome.isEmpty())
		{
			vHome = System.getProperty(VERDI_BASE);
		}
		return vHome;
	}

	
}
