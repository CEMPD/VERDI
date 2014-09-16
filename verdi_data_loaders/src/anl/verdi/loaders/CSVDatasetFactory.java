package anl.verdi.loaders;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import anl.verdi.data.Dataset;

/**
 * Creates Dataset-s from CSV files.
 *
 * @author Nick Collier
 * @author Eric Tatara
 * @version $Revision$ $Date$
 */
public class CSVDatasetFactory {

	/**
	 * Creates a list of Datasets from the specified URL. The url
	 * should point to a CSV file
	 *
	 * @param url a url that points to a CSV file 
	 * 
	 * @return a list of Datasets from the specified URL
	 */
	public List<Dataset> createCSVDatasets(URL url) {
		
    return createDatasets(url);
	}

	private List<Dataset> createDatasets(URL url) {
		List<Dataset> sets = new ArrayList<Dataset>();
		
	  sets.add(new CSVDataset(url));
 
		return sets;
	}
}
