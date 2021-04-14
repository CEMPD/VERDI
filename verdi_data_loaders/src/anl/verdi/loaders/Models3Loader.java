package anl.verdi.loaders;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import ucar.nc2.Attribute;
//import simphony.util.messages.MessageCenter;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.conv.M3IOConvention;
import anl.verdi.data.DataLoader;
import anl.verdi.data.DataReader;
import anl.verdi.data.Dataset;

/**
 * @author Nick Collier
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 */
public class Models3Loader implements DataLoader {
	static final Logger Logger = LogManager.getLogger(Models3Loader.class.getName());

//	private static final MessageCenter msgCenter = MessageCenter.getMessageCenter(Models3Loader.class);

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
			return M3IOConvention.isMine(file) && hasRowCol(file) && notCustom(file);

		} catch (IOException io) {
			//io.printStackTrace();
			// just warn here because it be correct that
			// this is not a netcdf file
			Logger.warn("Error reading netcdf file " + io.getMessage());
			throw io;
		} catch (URISyntaxException e) {
			//e.printStackTrace();
			Logger.warn("Error reading netcdf file " + e.getMessage());
			throw e;
		} catch (Exception e) {
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
	
	private boolean notCustom(NetcdfFile file) {
		String id = file.getFileTypeId();
		String version = file.getFileTypeVersion();
		List<Attribute> attrs = file.getGlobalAttributes();
		for (Attribute attr : attrs) {
			if ("FTYPE".equals(attr.getFullName())) {
				return (!Integer.valueOf(-1).equals(attr.getValue(0)));
			}
		}
		return true;
	}

	private boolean hasRowCol(NetcdfFile file) {
		List<Dimension> dims = file.getDimensions();
		boolean hasRow = false;
		boolean hasCol = false;
		for (Dimension dim : dims) {
//			if (dim.getName().equals("ROW")) hasRow = true;			// getName() is deprecated & no replacement suggested
//			else if (dim.getName().equals("COL")) hasCol = true;	// Based on code examples selected getShortName()
			if (dim.getShortName().equals("ROW")) hasRow = true;
			else if (dim.getShortName().equals("ny")) hasRow = true;
			else if (dim.getShortName().equals("COL")) hasCol = true;
			else if (dim.getShortName().equals("nx")) hasCol = true;
		}

		return hasRow &&  hasCol;
	}

	/**
	 * Creates a Dataset from the data at the specified URL.
	 *
	 * @param url the url of the data
	 * @return a Dataset created from the data at the specified URL.
	 */
	public List<Dataset> createDatasets(URL url) throws IOException {
		NetcdfDatasetFactory factory = new NetcdfDatasetFactory();
		return factory.createModels3Datasets(url);
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
