package anl.verdi.loaders;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import anl.verdi.data.Dataset;

/**
 * Creates Dataset-s from Generic Data files.
 *
 * @author Nick Collier
 * @author Eric Tatara
 * @version $Revision$ $Date$
 */
public class GenericDatasetFactory {

	/**
	 * Creates a list of Datasets from the specified URL. The url
	 * should point to a data file
	 *
	 * @param url a url that points to a data file 
	 * 
	 * @param wrapper a GereicDataWrapper that can handle the specified url
	 * 
	 * @return a list of Datasets from the specified URL
	 */
	public List<Dataset> createGenericDatasets(URL url, GenericDataWrapper wrapper) {
		
    	return createDatasets(url, wrapper);
	}

	private List<Dataset> createDatasets(URL url, GenericDataWrapper wrapper) {
		List<Dataset> sets = wrapper.createDatasets(url);
		
		new ArrayList<Dataset>();
		
	  //sets.add(new GenericDataset(url, wrapper));
 
		return sets;
	}
}
