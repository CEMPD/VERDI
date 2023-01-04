package anl.verdi.loaders;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import anl.verdi.data.Dataset;

/**
 * Creates Dataset-s from ICT files.
 *
 * @author Nick Collier
 * @author Eric Tatara
 * @version $Revision$ $Date$
 */
public class ICTDatasetFactory {

	/**
	 * Creates a list of Datasets from the specified URL. The url
	 * should point to an ICT file
	 *
	 * @param url a url that points to an ICT file 
	 * 
	 * @return a list of Datasets from the specified URL
	 */
	public List<Dataset> createICTDatasets(URL url) {
		
    return createDatasets(url);
	}

	private List<Dataset> createDatasets(URL url) {
		List<Dataset> sets = new ArrayList<Dataset>();
		
	  sets.add(new ICTDataset(url));
 
		return sets;
	}
}
