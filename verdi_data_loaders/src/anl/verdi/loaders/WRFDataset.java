package anl.verdi.loaders;

import java.io.IOException;
import java.net.URI;
import java.net.URL;

import org.apache.logging.log4j.LogManager;		// 2014
import org.apache.logging.log4j.Logger;			// 2014 replacing System.out.println with logger messages

//import simphony.util.messages.MessageCenter;
import ucar.nc2.dataset.conv.WRFConvention;
import ucar.nc2.dt.grid.GridDataset;
import anl.verdi.data.DatasetMetadata;

/**
 * WRF dataset.
 *
 * @author Nick Collier
 * @version $Revision$ $Date$
 */
public class WRFDataset extends AbstractNetcdfDataset {
	static final Logger Logger = LogManager.getLogger(WRFDataset.class.getName());

//	private static MessageCenter msgCenter = MessageCenter.getMessageCenter(WRFDataset.class);
	private int conv = -1;

	public WRFDataset(URL url) {
		super(url);
		try {
			String urlString = url.toExternalForm();
			if (url.getProtocol().equals("file")) {
				urlString = new URI(urlString).getPath();
			}
			gridDataset = GridDataset.open(urlString);
			if (!WRFConvention.isMine(gridDataset.getNetcdfDataset())) {
				throw new IOException("Loading non-wrf file into WRFDataset");
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

	@Override
	public DatasetMetadata getMetadata() {
		// TODO Auto-generated method stub
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
