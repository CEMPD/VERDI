package anl.verdi.loaders;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

//import simphony.util.messages.MessageCenter;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.conv.M3IOConvention;
import anl.verdi.data.DataLoader;
import anl.verdi.data.DataReader;
import anl.verdi.data.Dataset;

/**
 * Loader for Models3 observation data files.
 * 
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class Models3ObsLoader implements DataLoader {
	static final Logger Logger = LogManager.getLogger(Models3ObsLoader.class.getName());

//	private static final MessageCenLoggernter = MessageCenter.getMessageCenter(Models3ObsLoader.class);

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
			return M3IOConvention.isMine(file) && hasLatLon(file);

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


	private boolean hasLatLon(NetcdfFile file) {
		List<Variable> vars = file.getVariables();
		boolean hasLat = false;
		boolean hasLon = false;
		for (Variable var : vars) {
//			if (var.getName().equals("LAT")) hasLat = true;			// 2014 Variable.getName() deprecated with message
//			else if (var.getName().equals("LON")) hasLon = true;	// to use getFullName or getShortName; based on comparisons selected getShortName()
			if (var.getShortName().equals("LAT")) hasLat = true;
			else if (var.getShortName().equals("LON")) hasLon = true;
		}

		return hasLat && hasLon;
	}


	/**
	 * Creates a Dataset from the data at the specified URL.
	 *
	 * @param url the url of the data
	 * @return a Dataset created from the data at the specified URL.
	 */
	public List<Dataset> createDatasets(URL url) {
		NetcdfDatasetFactory factory = new NetcdfDatasetFactory();
		Dataset set = factory.createObsDataset(url);
		List<Dataset> sets = new ArrayList<Dataset>();
		sets.add(set);
		return sets;
	}

	/**
	 * Creates a DataReader that can read a particular type of Dataset.
	 *
	 * @param set the data set
	 * @return a DataReader created for the dataset.
	 */
	public DataReader createReader(Dataset set) {
		return new Models3ObsReader((Models3ObsDataset)set);
	}
}
