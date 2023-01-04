package anl.verdi.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

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
public class ICTLoader implements DataLoader {
	static final Logger Logger = LogManager.getLogger(ICTLoader.class.getName());

//	private static final MessageCenter msgCenter = MessageCenter.getMessageCenter(ICTLoader.class);

	/**
	 * Returns whether or not this DataLoader can handle the data at the specified
	 * url.
	 *
	 * @param url the location of the data
	 * @return true if this DataLoader can handle loading the data, otherwise
	 *         false.
	 */
	public boolean canHandle(URL url) throws Exception{
		//  try to open up the file
		File file = null;
		BufferedReader reader = null;
		try {
			String urlString = url.toExternalForm();
			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}
			file = new File(urlString);
			
			reader  = new BufferedReader(new FileReader(file));
			
//			return WRFConvention.isMine(file);
      if (urlString.toLowerCase().contains(".ict"))
			  return true;
			
		} catch (IOException io) {
			// just warn here because it be correct that
			// this is not a netcdf file
			Logger.error("Error reading ict file", io);
		} catch (URISyntaxException e) {
			Logger.error("Error reading ict file ", e);
		} catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			try {
				if (reader != null) 
					reader.close();
			} catch (IOException e) {}
		}

		return false;
	}

	/**
	 * Creates a Dataset from the data at the specified URL.
	 *
	 * @param url the url of the data
	 * @return a Dataset created from the data at the specified URL.
	 */
	public List<Dataset> createDatasets(URL url) {
		ICTDatasetFactory factory = new ICTDatasetFactory();
		return factory.createICTDatasets(url);
	}


	/**
	 * Creates a DataReader that can read a particular type of Dataset.
	 *
	 * @param set the data set
	 * @return a DataReader created for the dataset.
	 */
	public DataReader<?> createReader(Dataset set) {
		return new ICTReader((ICTDataset) set);
	}
}
