package anl.verdi.data;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Interface for classes that handle the loading of data from
 * various formats. A DataLoader is responsible for
 * determining whether it can handle some format, and for
 * creating a Dataset from that format.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 * @see Dataset
 */
public interface DataLoader {

	/**
	 * Returns whether or not this DataLoader can handle
	 * the data at the specified url.
	 *
	 * @param url the location of the data
	 * @return true if this DataLoader can handle loading the data, otherwise
	 *         false.
	 * @throws Exception 
	 */
	boolean canHandle(URL url) throws Exception;

	/**
	 * Creates a list of Datasets from the data at the specified URL.
	 *
	 * @param url the url of the data
	 * @return a list Datasets created from the data at the specified URL.
	 * @throws IOException if there is an error creating the Dataset.
	 */
	List<Dataset> createDatasets(URL url) throws IOException;

	/**
	 * Creates a DataReader that can read a particular type of Dataset.
	 *
	 * @param set the data set
	 * @return a DataReader created for the dataset.
	 */
	DataReader createReader(Dataset set);

}
