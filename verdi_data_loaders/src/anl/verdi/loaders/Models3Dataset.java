package anl.verdi.loaders;

import ucar.nc2.dataset.conv.M3IOConvention;
import ucar.nc2.dt.grid.GridDataset;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

import anl.verdi.data.DatasetMetadata;

/**
 * Model3 dataset.
 *
 * @author Nick Collier
 * @author Mary Ann Bitz
 * @version $Revision$ $Date$
 */
public class Models3Dataset extends AbstractNetcdfDataset {

	static final Logger Logger = LogManager.getLogger(Models3Dataset.class.getName());
	private int conv = -1;


	/**
	 * Creates an AbstractDataset.
	 *
	 * @param url the url of the dataset
	 */
	protected Models3Dataset(URL url) {
		super(url);
		try {
			String urlString = url.toExternalForm();
			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}
			gridDataset = GridDataset.open(urlString);
			if (!M3IOConvention.isMine(gridDataset.getNetcdfDataset())) {
				throw new IOException("Loading non-models3 file into Models3Dataset");
			}
		} catch (Exception io) {
			Logger.error("Error reading netcdf file " + io.getMessage());
			try {
				if (gridDataset != null)
					gridDataset.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Get the dataset meta data
	 *
	 * @return the DatasetMetadata object
	 */	
	@Override
	public DatasetMetadata getMetadata() {
		// TODO depend on what we need during the development
		return null;
	}
	
	@Override
	public int getNetcdfCovn() {
		return conv ;
	}


	@Override
	public void setNetcdfConv(int conv) {
		this.conv = conv;
	}
}
