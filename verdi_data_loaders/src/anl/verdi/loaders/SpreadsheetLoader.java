/**
 * SpreadsheetLoader.java - Reads tab-delimited ASCII spreadsheet data files.
 * @author Todd Plessel
 * @version $Revision$ $Date$
 */

package anl.verdi.loaders;

import java.util.List;
import java.util.ArrayList;
import java.io.RandomAccessFile;
import java.net.URL;
import java.net.URI;
import anl.verdi.data.DataLoader;
import anl.verdi.data.DataReader;
import anl.verdi.data.Dataset;

public class SpreadsheetLoader implements DataLoader {

	/**
	 * Returns whether or not this DataLoader can read the data at the url.
	 *
	 * @param url the location of the data
	 * @return true if this DataLoader can read the data, otherwise false.
	 * @throws Exception 
	 */
	public boolean canHandle( final URL url ) throws Exception {
		boolean result = false;
		RandomAccessFile file = null;

		try {

			if ( url.getProtocol().equals( "file" ) ) {
				final String fileName = new URI( url.toExternalForm() ).getPath();
				file = new RandomAccessFile( fileName, "r" );
				final String firstLine = file.readLine().toLowerCase();
				//result = firstLine.startsWith( SpreadsheetReader.headerStart );
				SpreadsheetReader.validateHeaderAndGetTimezone(firstLine);
				result = true;
			}
		} catch ( Exception e ) {
			throw e;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			try { if ( file != null ) file.close(); } catch ( Exception e ) {}
		}

		file = null;
		return result;
	}

	/**
	 * Creates a Dataset from the data at the specified URL.
	 *
	 * @param url the url of the data
	 * @return a Dataset created from the data at the specified URL.
	 */
	public List<Dataset> createDatasets( URL url ) {
		final List<Dataset> result = new ArrayList<Dataset>( 1 );

		try {
			final String fileName = new URI( url.toExternalForm() ).getPath();
			final SpreadsheetReader reader = new SpreadsheetReader( fileName );
			final Dataset dataset = reader.getDataset();
			result.add( dataset );
		} catch ( Exception e ) {
		}

		return result;
	}

	/**
	 * Creates a DataReader that can read a particular type of Dataset.
	 *
	 * @param dataset the data set
	 * @return a DataReader created for the dataset.
	 */
	public DataReader createReader( Dataset dataset ) {
		final DataReader result =
			new SpreadsheetReader( ( (SpreadsheetDataset) dataset ).getFileName() );
		return result;
	}
}



