package anl.verdi.util;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import anl.verdi.data.DataFrame;
import anl.verdi.data.Dataset;
import anl.verdi.plot.config.PlotConfiguration;

/**
 * @author Qun He
 * @version $Revision$ $Date$
 */
public class Tools {
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
	
	public static String getVerdiHome() 
	{	// 2014
		return System.getenv(VERDI_HOME);
	}

	
}
