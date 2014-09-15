package anl.verdi.io;

import java.net.URL;

import anl.verdi.gui.DatasetListElement;


/**
 * Wraps a DatasetListElement in an easier to persist form.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class DatasetElementDescriptor extends ListElementDescriptor {

	private URL datasetURL;
	private String alias;
	private int urlIndex;

	public DatasetElementDescriptor(DatasetListElement element) {
		super(element);
		datasetURL = element.getDataset().getURL();
		alias = element.getDataset().getAlias();
		urlIndex = element.getDataset().getIndexInURL();
	}

	public int getUrlIndex() {
		return urlIndex;
	}

	public String getAlias() {
		return alias;
	}

	public URL getDatasetURL() {
		return datasetURL;
	}
}
