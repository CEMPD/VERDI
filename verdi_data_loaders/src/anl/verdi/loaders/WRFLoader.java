package anl.verdi.loaders;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

//import simphony.util.messages.MessageCenter;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.conv.WRFConvention;
import anl.verdi.data.DataLoader;
import anl.verdi.data.DataReader;
import anl.verdi.data.Dataset;

/**
 * @author Nick Collier
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 */
public class WRFLoader implements DataLoader {
	static final Logger Logger = LogManager.getLogger(WRFLoader.class.getName());

//	private static final MessageCenter msgCenter = MessageCenter.getMessageCenter(WRFLoader.class);

	/**
	 * Returns whether or not this DataLoader can handle the data at the specified
	 * url.
	 *
	 * @param url the location of the data
	 * @return true if this DataLoader can handle loading the data, otherwise
	 *         false.
	 * @throws Exception 
	 */
	public boolean canHandle(URL url) throws Exception {
		//  try to open up the file
		NetcdfFile file = null;
		try {
			String urlString = url.toExternalForm();
			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}
			file = NetcdfFile.open(urlString);
			return WRFConvention.isMine(file);

		} catch (IOException io) {
			// just warn here because it be correct that
			// this is not a netcdf file
			Logger.warn("Error reading netcdf file " + io.getMessage());
			throw io;
		} catch (URISyntaxException e) {
			Logger.warn("Error reading netcdf file " + e.getMessage());
			throw e;
		}  catch (Exception e) {
			throw e;
		} catch (Throwable t) {
			t.printStackTrace();
		}
		finally {
			try {
				if (file != null) file.close();
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
		NetcdfDatasetFactory factory = new NetcdfDatasetFactory();
		return factory.createWRFDatasets(url);
	}


	/**
	 * Creates a DataReader that can read a particular type of Dataset.
	 *
	 * @param set the data set
	 * @return a DataReader created for the dataset.
	 */
	public DataReader createReader(Dataset set) {
		return new GridNetcdfReader((GridNetcdfDataset) set);
	}
}
