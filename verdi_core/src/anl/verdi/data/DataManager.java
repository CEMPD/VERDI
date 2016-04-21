package anl.verdi.data;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages
import org.java.plugin.PluginLifecycleException;
import org.java.plugin.PluginManager;
import org.java.plugin.registry.Extension;
import org.java.plugin.registry.ExtensionPoint;

//import simphony.util.messages.MessageCenter;
import anl.verdi.util.AliasGenerator;

/**
 * Manages the loading of data and the creation of data sets.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DataManager {
	static final Logger Logger = LogManager.getLogger(DataManager.class.getName());

//	private static final MessageCenter msgCenter = MessageCenter.getMessageCenter(DataManager.class);

	public static List<Dataset> NULL_DATASETS = new ArrayList<Dataset>();

	private static final String DATA_LOADER_EXT = "data.DataLoader";
	private static final String DATA_LOADER_PLUGIN = "anl.verdi";

	private AliasGenerator aliasGenerator = new AliasGenerator();
	private List<DataLoader> dataLoaders = new ArrayList<DataLoader>();
	// alias, dataset
	private Map<String, Dataset> datasets = new HashMap<String, Dataset>();
	private Map<Dataset, DataLoader> setLoaderMap = new HashMap<Dataset, DataLoader>();

	/**
	 * Creates a DataManager that uses the specified DataLoaders to
	 * do the data loading.
	 *
	 * @param dataLoaders the data loaders to use
	 */
	public DataManager(Collection<DataLoader> dataLoaders) {
		this.dataLoaders.addAll(dataLoaders);
	}

	/**
	 * Creates a DataManager that creates its data loaders via plugin extensions.
	 *
	 * @param manager the plugin manager to used to find the plugin extensions.
	 * @throws PluginLifecycleException on a plugin error
	 */
	public DataManager(PluginManager manager) throws PluginLifecycleException {

		ExtensionPoint extPoint = manager.getRegistry().getExtensionPoint(DATA_LOADER_PLUGIN, DATA_LOADER_EXT);
		for (Iterator iter = extPoint.getConnectedExtensions().iterator(); iter.hasNext();) {

			Extension ext = (Extension) iter.next();
			String className = ext.getParameter("class").valueAsString();
			try {
				Class clazz = manager.getPluginClassLoader(ext.getDeclaringPluginDescriptor()).loadClass(className);
				if (DataLoader.class.isAssignableFrom(clazz)) {
					dataLoaders.add((DataLoader) clazz.newInstance());
				} else {
					Logger.warn("Error during DataLoader creation: data.DataLoader extension point class must implement DataLoader");
				}
			} catch (IllegalAccessException e) {
				Logger.error("Error during data loader plugin creation " + e.getMessage());
			} catch (InstantiationException e) {
				Logger.error("Error during data loader plugin creation " + e.getMessage());
			} catch (ClassNotFoundException e) {
				Logger.error("Error during data loader plugin creation " + e.getMessage());
			}
		}
	}


	/**
	 * Creates a Dataset from the data at the specified URL. This DataManager
	 * iterate through its collection of DataLoaders and use the first one
	 * that can handle the data at the specified URL to create the Dataset. It
	 * will also assign the Dataset an alias.
	 *
	 * @param url the url of the data
	 * @return a Dataset created from the data at the specified URL. If no Dataset
	 *         can be created the null object {@link #DataManager#NULL_DATASET DataManager.NULL_DATASET}
	 *         will be returned.
	 * @throws Exception 
	 */
	public List<Dataset> createDatasets(URL url) throws IOException {
		Exception wrapper = null;
		for (DataLoader loader : dataLoaders) {
			//System.err.println( "DataManager.java:createDatasets() trying loader = " + loader );
			try {
				if (loader.canHandle(url)) {
					try {
						Logger.debug("before creating dataset.");
						List<Dataset> data = loader.createDatasets(url);
						Logger.debug("after creating dataset. Dataset list size: " + data.size());
						for (Dataset dataset : data) {
							setLoaderMap.put(dataset, loader);
							String alias = aliasGenerator.getNextAlias();
							dataset.setAlias(alias);
							datasets.put(alias, dataset);
							Logger.debug("alias: " + alias);
						}
						return data;
					} catch (IOException e) {
						//e.printStackTrace();
						if (wrapper == null)
							wrapper = e;
						else
							wrapper = new IOException(e);
						Logger.warn("Error while creating a Dataset " + e.getMessage());
					}
				} 
			} catch (Exception e) {
				//e.printStackTrace();
				if (wrapper == null)
					wrapper = e;
				else
					wrapper = new Exception(e);
			}
		}

		if ( wrapper != null ){	
			throw new IOException(wrapper);
		}

		return NULL_DATASETS;
	}

	/**
	 * Replaces the specified datasets current alias with the new one.
	 * This does not effect formulas in any way.
	 *
	 * @param aliasMap key is the dataset and value is the new alias.
	 */
	public void replaceAliases(Map<Dataset, String> aliasMap) {
		for (Dataset dataset : aliasMap.keySet()) {
			datasets.remove(dataset.getAlias());
		}

		for (Dataset dataset : aliasMap.keySet()) {
			String alias = aliasMap.get(dataset);
			dataset.setAlias(alias);
			aliasGenerator.markAliasUsed(alias);
			datasets.put(alias, dataset);
		}
	}

	/**
	 * Creates a Dataset from the data at the specified URL. This DataManager
	 * iterate through its collection of DataLoaders and use the first one
	 * that can handle the data at the specified URL to create the Dataset.
	 * The dataset is assigned the specified alias. This method of creating a
	 * dataset should be only used when creating Datasets as part of loading
	 * a scenario file.
	 *
	 * @param url   the url of the data
	 * @param alias alias of the dataset.
	 * @return a Dataset created from the data at the specified URL. If no Dataset
	 *         can be created the null object {@link #DataManager#NULL_DATASET DataManager.NULL_DATASET}
	 *         will be returned.
	 *
	public List<Dataset> createDatasets(URL url, String alias) {
	for (DataLoader loader : dataLoaders) {
	if (loader.canHandle(url)) {
	try {
	List<Dataset> data = loader.createDatasets(url);
	for (Dataset dataset : data) {
	setLoaderMap.put(dataset, loader);
	dataset.setAlias(alias);
	aliasGenerator.markAliasUsed(alias);
	datasets.put(alias, dataset);
	}
	return data;
	} catch (IOException e) {
	Logger.warn("Error while creating a Dataset " + e.getMessage());
	}
	}
	}
	return NULL_DATASETS;
	}
	 */

	/**
	 * Gets the datareader appropriate for reading the specified dataset.
	 *
	 * @param set the set that we want to read.
	 * @return the datareader appropriate for reading the specified dataset.
	 */
	public DataReader getDataReader(Dataset set) {
		return setLoaderMap.get(set).createReader(set);
	}

	/**
	 * Gets the number of datasets currently loaded.
	 *
	 * @return the number of datasets currently loaded.
	 */
	public int getDatasetCount() {
		return datasets.size();
	}

	/**
	 * Gets the dataset with the specified alias.
	 *
	 * @param alias the alias of the dataset to get.
	 * @return the dataset with the specified alias.
	 */
	public Dataset getDataset(String alias) {
		return datasets.get(alias);
	}

	/**
	 * Splits a formula variable name into the dataset alias and
	 * variable name.
	 *
	 * @param variableName the name to split
	 * @return a string array whose first element is the
	 *         alias and whose second element is the unaliased
	 *         variable name.
	 */
	public String[] splitVarName(String variableName) {
		return aliasGenerator.splitAlias(variableName);
	}

	/**
	 * Closes the specified dataset and removes it
	 * from the datasets managed by this manager.
	 *
	 * @param alias the alias of the dataset to close
	 * @throws IOException if there is an error closing the dataset.
	 */
	public void closeDataset(String alias) throws IOException {	
		Dataset set = datasets.remove(alias);
		if (set != null) {
			setLoaderMap.remove(set);
			set.close();		
		}
		if (datasets.size() == 0) aliasGenerator.clearAlias();
	}

	/**
	 * Closes all the datasets managed by this manager
	 * and clears the list of datasets managed by the DatasetManager.
	 *
	 * @throws IOException if there is an error closing a dataset.
	 */
	public void closeAllDatasets() throws IOException {
		for (Dataset set : datasets.values()) {
			set.close();
		}
		datasets.clear();
		aliasGenerator.clearAlias();
	}
}
