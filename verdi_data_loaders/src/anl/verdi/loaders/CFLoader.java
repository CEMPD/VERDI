package anl.verdi.loaders;

import anl.verdi.data.DataLoader;
import anl.verdi.data.DataReader;
import anl.verdi.data.Dataset;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;

import java.net.URI;
import java.net.URL;
import java.util.List;

/**
 * @author Todd Plessel
 * @version $Revision$ $Date$
 */
public class CFLoader implements DataLoader {


	/**
	 * Returns whether or not this DataLoader can read the data at the url.
	 *
	 * @param url the location of the data
	 * @return true if this DataLoader can read the data, otherwise false.
	 * @throws Exception 
	 */
	public boolean canHandle(URL url) throws Exception {
		NetcdfFile file = null;
		boolean result = false;

		try {
			String urlString = url.toExternalForm();

			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}

			file = NetcdfFile.open(urlString);
			final Attribute attribute =
				file.findGlobalAttribute( "Conventions" );
			result =
				attribute != null &&
				attribute.getStringValue().startsWith( "CF-" );
			return result;

		} catch ( Exception unused ) {
			throw unused;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			try { if ( file != null ) file.close(); } catch ( Exception e ) {}
		}

		return result;
	}

	/**
	 * Creates a Dataset from the data at the specified URL.
	 *
	 * @param url the url of the data
	 * @return a Dataset created from the data at the specified URL.
	 */
	public List<Dataset> createDatasets(URL url) {
		NetcdfDatasetFactory factory = new NetcdfDatasetFactory();
		final List<Dataset> result = factory.createCFDatasets(url);

		// Remove any datasets that contain no variables:

		final int count = result.size();

		for ( int dataset = 0; dataset < count; ++dataset ) {

			if ( result.get( dataset ).getVariables().size() == 0 ) {
				result.remove( dataset );
			}
		}

		return result;
	}

	/**
	 * Creates a DataReader that can read a particular type of Dataset.
	 *
	 * @param set the data set
	 * @return a DataReader created for the dataset.
	 */
	public DataReader createReader(Dataset set) {
		return new GridNetcdfReader((GridNetcdfDataset)set);
	}
}
