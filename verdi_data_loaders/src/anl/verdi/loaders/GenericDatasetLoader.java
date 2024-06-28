package anl.verdi.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import simphony.util.messages.MessageCenter;
import anl.verdi.data.DataLoader;
import anl.verdi.data.DataReader;
import anl.verdi.data.Dataset;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

/**
 * @author Nick Collier
 * @author Mary Ann Bitz
 * @author Eric Tatara
 * @version $Revision$ $Date$
 */
public class GenericDatasetLoader implements DataLoader {
	static final Logger Logger = LogManager.getLogger(GenericDatasetLoader.class.getName());
	
	static List<GenericDataWrapper> DATA_WRAPPERS = new ArrayList<GenericDataWrapper>();
	
	Map<String, GenericDataWrapper> KNOWN_WRAPPERS = new HashMap<String, GenericDataWrapper>();
	
	static {
		DATA_WRAPPERS.add(new GLMDataWrapper());
	}
	
	public GenericDatasetLoader() {
		
	}

//	private static final MessageCenter msgCenter = MessageCenter.getMessageCenter(GenericDatasetLoader.class);

	private GenericDataWrapper getSupportedWrapper(URL url) {
		GenericDataWrapper wp = KNOWN_WRAPPERS.get(url.getFile());
		if (wp != null)
			return wp;
		for (GenericDataWrapper wrapper : DATA_WRAPPERS) {
			if (wrapper.canHandle(url)) {
				KNOWN_WRAPPERS.put(url.getFile(), wrapper);
				return wrapper;
			}
		}
		return null;
	}
	
	/**
	 * Returns whether or not this DataLoader can handle the data at the specified
	 * url.
	 *
	 * @param url the location of the data
	 * @return true if this DataLoader can handle loading the data, otherwise
	 *         false.
	 */
	public boolean canHandle(URL url) throws Exception{
		return getSupportedWrapper(url) != null;
	}

	/**
	 * Creates a Dataset from the data at the specified URL.
	 *
	 * @param url the url of the data
	 * @return a Dataset created from the data at the specified URL.
	 */
	public List<Dataset> createDatasets(URL url) {
		GenericDatasetFactory factory = new GenericDatasetFactory();
		GenericDataWrapper wrapper = getSupportedWrapper(url);
		if (wrapper == null)
			return null;
		return factory.createGenericDatasets(url, wrapper.openInstance(url));
	}


	/**
	 * Creates a DataReader that can read a particular type of Dataset.
	 *
	 * @param set the data set
	 * @return a DataReader created for the dataset.
	 */
	public DataReader<?> createReader(Dataset set) {
		return new GenericReader((GenericDataset) set);
	}
}
